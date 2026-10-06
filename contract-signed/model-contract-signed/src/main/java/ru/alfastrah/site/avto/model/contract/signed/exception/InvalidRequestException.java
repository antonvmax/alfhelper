package ru.alfastrah.site.avto.model.contract.signed.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
