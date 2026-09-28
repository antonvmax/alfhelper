package ru.alfastrah.site.avto.model.contract.signed.exception;

public class BadDataException extends RuntimeException {
    public BadDataException(String message) {
        super(message);
    }
}
