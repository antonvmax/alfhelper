package ru.alfastrah.site.avto.ws.contact.signed.service.signing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.client.CertificateInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignatureInfo;
import ru.alfastrah.site.avto.ws.contact.signed.model.signing.CertificateInfoResponse;

@Service
@RequiredArgsConstructor
public class CertificateInfoService {

    private final CertificateInfoClient infoClient;

    public CertificateInfoResponse getInfo() {
        SignatureInfo info = infoClient.getInfo().getSignatureInfo();
        return new CertificateInfoResponse()
                .withFingerprint(info.getFingerprint())
                .withExpirationDate(info.getExpirationDate())
                .withIssuer(info.getIssuer())
                .withAttorneyNumber("6dca1c2a-95d2-4d6d-ab0f-c50b6a4b01a9")
                .withAttorneyGranted("2024-03-14");
    }
}
