package ru.alfastrah.site.avto.model.contract.signed.exception;

/**
 * Класс для возбуждения исключительных ситуаций при пролонгации договора
 */
public class EOsagoSaveException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String faultCode;

    public EOsagoSaveException(String message, String mCode) {
        super(message);
        faultCode = mCode;
    }

    public EOsagoSaveException(String message, String mCode, Throwable cause) {
        super(message, cause);
        faultCode = mCode;
    }

    public String getFaultCode() {
        return faultCode;
    }
}
