package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Store;
import org.springframework.core.io.ClassPathResource;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class PDFSignBean {
    private static final String KEYSTORE_PKCS12 = "PKCS12";
    private final COSName cryptoProPdf = COSName.getPDFName("CryptoPro PDF");

    private static final int BUFFER_SIZE = 8 * 1024;

    private Certificate[] certificateChain;
    private PrivateKey privateKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PDFSignBean(String fileP12, String password)
            throws EOsagoSaveException {
        try (final InputStream fileInputStream = new ClassPathResource(fileP12).getInputStream()) {
            this.initKeyStore(fileInputStream, password);
        } catch (IOException e) {
            throw new EOsagoSaveException(e.getMessage(), "initKeyStore: ", e);
        }
    }

    public PDFSignBean(byte[] content, String password)
            throws EOsagoSaveException {
        this.initKeyStore(new ByteArrayInputStream(content), password);
    }

    private void initKeyStore(InputStream fileInputStream, String password)
            throws EOsagoSaveException {
        try {
            KeyStore keystore = KeyStore.getInstance(KEYSTORE_PKCS12, BouncyCastleProvider.PROVIDER_NAME);
            Properties.setThreadOverride("org.bouncycastle.pkcs12.ignore_useless_passwd", true);
            keystore.load(fileInputStream, password.toCharArray());
            String alias = getFirstKeyAlias(keystore);
            privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
            certificateChain = keystore.getCertificateChain(alias);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException |
                UnrecoverableKeyException | IOException | NoSuchProviderException | KeyException e) {
            throw new EOsagoSaveException(e.getMessage(), "initKeyStore: ", e);
        }
    }

    @SuppressWarnings("squid:S2095")
    public ByteArrayOutputStream signPdf(byte[] content, String path)
            throws EOsagoSaveException {
        // Временно делаем через файл
        // Определяемся с именем файла
        String filePDF = path + UUID.randomUUID().toString() + ".pdf";
        InputStream input = new ByteArrayInputStream(content);
        final File outputFile = new File(filePDF);

        ByteArrayOutputStream outputba = new ByteArrayOutputStream();
        PDDocument doc;

        try (FileOutputStream output = new FileOutputStream(filePDF)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int c;
            while ((c = input.read(buffer)) != -1) {
                output.write(buffer, 0, c);
            }
            input.close();
            input = new FileInputStream(outputFile);

            doc = PDDocument.load(new ByteArrayInputStream(content));
            PDSignature signature = new PDSignature();
            signature.setFilter(cryptoProPdf);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setSignDate(Calendar.getInstance());
            doc.addSignature(signature, buildSignatureInterface(certificateChain));
            doc.saveIncremental(input, output);

            doc.close();
            input.close();
            input = new FileInputStream(outputFile);
            while ((c = input.read(buffer)) != -1) {
                outputba.write(buffer, 0, c);
            }
            input.close();
            // Удаляем уже ненужный файл
            deleteTempFile(filePDF);
        } catch (IOException ex) {
            throw new EOsagoSaveException(ex.getMessage(), "signPdf2: ", ex);
        } catch (SignatureException | COSVisitorException ex) {
            throw new EOsagoSaveException(ex.getMessage(), "signPdf1: ", ex);
        }


        return outputba;
    }

    @SuppressWarnings("java:S4042")
    private void deleteTempFile(String filePDF) throws EOsagoSaveException {
        if (!new File(filePDF).delete()) {
            throw new EOsagoSaveException("Файл не удален:", filePDF);
        }
    }

    private SignatureInterface buildSignatureInterface(final Certificate[] certificates) {

        return content -> {
            try {
                List<Certificate> certificateList = Arrays.asList(certificates);
                Store<Certificate> certs = new JcaCertStore(certificateList);
                CMSSignedData sigData = generateData(this.buildDataGenerator(certificateList, certs), content);
                return sigData.getEncoded();
            } catch (IOException | CertificateEncodingException | OperatorCreationException | CMSException e) {
                throw new SignatureException(e);
            }
        };
    }

    private CMSSignedDataGenerator buildDataGenerator(List<Certificate> certificateList, Store<Certificate> certificateStore)
            throws OperatorCreationException, CMSException, CertificateEncodingException {
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        // Учитывается новый алгоритм ГОСТ 2012
        // пред.алгоритм GOST3411withECGOST3410
        final ContentSigner contentSigner = new JcaContentSignerBuilder("GOST3411WITHGOST3410-2012-256")
                .setProvider("BC")
                .build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder()
                        .setProvider("BC")
                        .build())
                .build(contentSigner, (X509Certificate) certificateList.get(0)));
        gen.addCertificates(certificateStore);
        return gen;
    }
    private CMSSignedData generateData(CMSSignedDataGenerator gen, InputStream content) throws IOException, CMSException {
        CMSTypedData msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));
        return gen.generate(msg, false);
    }

    /**
     * Метод вовращает первого альяса из хранилища ключей
     *
     * @param keyStore - хранилище ключей
     * @return альяс
     * @throws KeyException
     */
    @SuppressWarnings({"java:S1854", "java:S1481"})
    private String getFirstKeyAlias(KeyStore keyStore) throws KeyException, KeyStoreException {
        if (keyStore != null) {
            Enumeration<String> aliases = keyStore.aliases();
            if (aliases.hasMoreElements()) {
                return aliases.nextElement();
            }
        }

        throw new KeyException("KeyStore is empty. Not found key alias.");
    }

}
