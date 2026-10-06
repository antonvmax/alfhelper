package ru.alfastrah.site.avto.model.contract.signed.exception;

/**
 * Created by FlominTV on 19.04.2016.
 * Класс для возбуждения исключительных ситуаций при пролонгации договора
 * в результате которых в ответе не будет stacktrace'a и отправляется письмо на почту
 */
public class EOsagoExceptionMailNoStacktrace extends Exception {

    private static final long serialVersionUID = 1L;
    private final String faultCode;

    public EOsagoExceptionMailNoStacktrace(String message, String mCode) {
        super(message);
        faultCode = mCode;
    }

    public EOsagoExceptionMailNoStacktrace(String message, String mCode, Throwable cause) {
        super(message, cause);
        faultCode = mCode;
    }

    public String getFaultCode() {
        return faultCode;
    }
}