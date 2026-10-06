package ru.alfastrah.site.avto.model.contract.signed.exception;

public class EOsagoProccessException extends RuntimeException {
    private final String message;
    private final String code;

    public EOsagoProccessException() {
        message = "";
        code = "";
    }

    public EOsagoProccessException(String message, String code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "EOsagoProccessException{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
