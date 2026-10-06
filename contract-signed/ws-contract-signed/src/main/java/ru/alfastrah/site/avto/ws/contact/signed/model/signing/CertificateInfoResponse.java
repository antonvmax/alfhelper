package ru.alfastrah.site.avto.ws.contact.signed.model.signing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(
        name = "CertificateInfoResponse",
        description = "Информация о подписи"
)
public class CertificateInfoResponse {
    @Schema(description = "Истечение срока действия подписи", example = "24.04.2025")
    private String expirationDate;
    @Schema(description = "Отпечаток подписи", example = "98BEED60C5B7C68478CD0FCA09B00A12CF1D1041")
    private String fingerprint;
    @Schema(description = "Номер доверенности", example = "6dca1c2a-95d2-4d6d-ab0f-c50b6a4b01a9")
    private String attorneyNumber;
    @Schema(description = "Дата выдача доверенности", example = "2024-03-14")
    private String attorneyGranted;
    @Schema(description = "Удостоверяющий центр:", example = "Alfastrakhovanie Gost Root CA")
    private String issuer;

    public CertificateInfoResponse() {

    }

    public CertificateInfoResponse(String expirationDate, String fingerprint) {
        this.expirationDate = expirationDate;
        this.fingerprint = fingerprint;
    }

    public CertificateInfoResponse withExpirationDate(String date) {
        this.expirationDate = date;
        return this;
    }

    public CertificateInfoResponse withFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public CertificateInfoResponse withAttorneyNumber(String attorneyNumber) {
        this.attorneyNumber = attorneyNumber;
        return this;
    }

    public CertificateInfoResponse withAttorneyGranted(String attorneyGranted) {
        this.attorneyGranted = attorneyGranted;
        return this;
    }

    public CertificateInfoResponse withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

}
