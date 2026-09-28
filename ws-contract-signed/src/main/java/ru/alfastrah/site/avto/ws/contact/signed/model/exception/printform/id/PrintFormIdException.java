package ru.alfastrah.site.avto.ws.contact.signed.model.exception.printform.id;

public class PrintFormIdException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String faultCode;

    public PrintFormIdException(String message, String mCode) {
        super(message);
        faultCode = mCode;
    }

    public PrintFormIdException(String message, String mCode, Throwable cause) {
        super(message, cause);
        faultCode = mCode;
    }

    public String getFaultCode() {
        return faultCode;
    }
}
