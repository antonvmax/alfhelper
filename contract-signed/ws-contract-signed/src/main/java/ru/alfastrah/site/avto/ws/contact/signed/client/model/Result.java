package ru.alfastrah.site.avto.ws.contact.signed.client.model;

public class Result {
    public Result() {

    }

    public Result(byte[] bytes) {
        this.bytes = bytes;
    }
    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
