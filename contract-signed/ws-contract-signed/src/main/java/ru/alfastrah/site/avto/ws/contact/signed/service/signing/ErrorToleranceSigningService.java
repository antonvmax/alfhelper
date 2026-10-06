package ru.alfastrah.site.avto.ws.contact.signed.service.signing;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.FileSigningException;
import ru.alfastrah.site.avto.ws.contact.signed.client.CertificateInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.SignatureClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp.SignatureRepository;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ErrorToleranceSigningService extends SigningService {
    private static final String SEND_UNSIGNED_PARAM_NAME = "sendUnsignedPolicies";

    private final SignatureRepository signatureRepository;

    public ErrorToleranceSigningService(SignatureClient signatureClient, CertificateInfoClient infoClient, SignatureRepository signatureRepository) {
        super(signatureClient, infoClient);
        this.signatureRepository = signatureRepository;
    }

    public DataHandler sign(byte[] sourceByte, List<StampParams> optStampParams, BigInteger contractId) {
        boolean sendFailedFiles = Optional.ofNullable(signatureRepository.getConfigValue(SEND_UNSIGNED_PARAM_NAME))
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);

        return this.sign(sourceByte, optStampParams, sendFailedFiles, contractId);
    }

    public DataHandler sign(byte[] sourceByte, List<StampParams> stampParams, boolean sendFailedFiles,
                            BigInteger contractId) {
        byte[] reservedPdf = Arrays.copyOf(sourceByte, sourceByte.length);
        if (stampParams.isEmpty()) {
            log.debug("Не подписываем");
            return new DataHandler(new ByteArrayDataSource(reservedPdf, "application/octet-stream"));
        }
        try {
            return super.signFile(sourceByte, stampParams);
        } catch (Exception e) {
            log.error("Ошибка подписи", e);
            if (sendFailedFiles) {
                signatureRepository.logNotSignedPolicy(contractId);
                return new DataHandler(new ByteArrayDataSource(reservedPdf, "application/octet-stream"));

            }
        }
        throw new FileSigningException("Ошибка подписи файла");
    }

}
