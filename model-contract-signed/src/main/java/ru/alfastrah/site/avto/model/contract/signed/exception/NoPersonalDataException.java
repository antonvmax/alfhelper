package ru.alfastrah.site.avto.model.contract.signed.exception;

public class NoPersonalDataException extends RuntimeException{

    public NoPersonalDataException(String message) {
       super(message);
    }
}
