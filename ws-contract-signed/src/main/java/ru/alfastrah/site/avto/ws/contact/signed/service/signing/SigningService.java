package ru.alfastrah.site.avto.ws.contact.signed.service.signing;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.TextField;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.CryptoPro.JCP.JCP;
import ru.alfastrah.site.avto.ws.contact.signed.client.CertificateInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.SignatureClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignServiceInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignatureInfo;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class SigningService {

    private final SignatureClient signatureClient;
    private final CertificateInfoClient infoClient;

    public SigningService(SignatureClient signatureClient, CertificateInfoClient infoClient) {
        this.signatureClient = signatureClient;
        this.infoClient = infoClient;
    }

    public DataHandler signFile(byte[] sourceByte, List<StampParams> optStampParams) {

        try {
            Security.addProvider(new JCP());
            SignServiceInfoResponse infoResponse = infoClient.getInfo();
            SignatureInfo info = infoResponse.getSignatureInfo();

            PdfReader reader = new PdfReader(sourceByte);
            PdfReader.unethicalreading = true;
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            for (StampParams stampParams : optStampParams.stream().filter(StampParams::isStampVisible).toList()) {
                ClassPathResource res = new ClassPathResource("times.ttf");
                BaseFont baseFont = BaseFont.createFont(res.getPath(), "cp1251", BaseFont.EMBEDDED);
                StringBuilder sb = new StringBuilder();
                sb.append("Подписано электронной подписью: ").append(info.getFingerprint()).append("\n");
                sb.append("Действительна до: ").append(info.getExpirationDate())
                        .append(" Удостоверяющий центр:").append(info.getIssuer()).append("\n");
                sb.append("Номер доверенности: ").append("6dca1c2a-95d2-4d6d-ab0f-c50b6a4b01a9")
                        .append(" Дата выдачи доверенности: ").append("2024-03-14");
                TextField tf = new TextField(stp.getWriter(), new Rectangle(stampParams.getLlx(),
                        stampParams.getLly(),
                        stampParams.getUrx(),
                        stampParams.getUry()), "stmp" + stampParams.getPage());
                tf.setFont(baseFont);
                tf.setFontSize(stampParams.getFontSize());
                tf.setText(sb.toString());
                tf.setOptions(BaseField.READ_ONLY + BaseField.MULTILINE);
                stp.addAnnotation(tf.getTextField(), stampParams.getPage());
            }

            PdfSignature dic = new PdfSignature(PdfName.ADOBE_CryptoProPDF, PdfName.ADBE_PKCS7_DETACHED);

            dic.setDate(new PdfDate(sap.getSignDate()));
            sap.setCryptoDictionary(dic);
            int estimatedSize = 8192;

            HashMap<PdfName, Integer> exc = new HashMap<>();
            exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2);

            sap.preClose(exc);

            InputStream data = sap.getRangeStream();

            String keyAlgorithm = info.getAlgorithm();
            String fileDigestAlgorithm = JCP.GOST_DIGEST_2012_256_NAME;
            if (keyAlgorithm.equals(JCP.GOST_EL_2012_256_NAME) ||
                    keyAlgorithm.equals(JCP.GOST_DH_2012_256_NAME)) {
                fileDigestAlgorithm = JCP.GOST_DIGEST_2012_256_NAME;
            } else if (
                    keyAlgorithm.equals(JCP.GOST_EL_2012_512_NAME) ||
                            keyAlgorithm.equals(JCP.GOST_DH_2012_512_NAME)) {
                fileDigestAlgorithm = JCP.GOST_DIGEST_2012_512_NAME;
            }
            MessageDigest md = MessageDigest.getInstance(fileDigestAlgorithm);
            byte[] fileHash = DigestAlgorithms.digest(data, md);
            byte[] paddedSig = signatureClient.signBytes(fileHash, infoResponse.getPort());

            PdfDictionary dic2 = new PdfDictionary();
            dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));

            sap.close(dic2);
            stp.close();

            fout.close();
            reader.close();
            DataHandler dataHandler;
            dataHandler = new DataHandler(new ByteArrayDataSource(fout.toByteArray(), "application/octet-stream"));
            return dataHandler;
        } catch (IOException | DocumentException | GeneralSecurityException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
