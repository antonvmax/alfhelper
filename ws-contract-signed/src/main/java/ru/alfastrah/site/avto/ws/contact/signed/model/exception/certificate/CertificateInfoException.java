package ru.alfastrah.site.avto.ws.contact.signed.model.exception.certificate;

public class CertificateInfoException extends RuntimeException {
    public CertificateInfoException(String message) {
        super(message);
    }

    public CertificateInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
