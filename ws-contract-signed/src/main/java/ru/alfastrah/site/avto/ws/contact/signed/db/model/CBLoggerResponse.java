package ru.alfastrah.site.avto.ws.contact.signed.db.model;

public class CBLoggerResponse {
    private String uuid;
    private boolean success;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "CBResponse{" +
                "uuid='" + uuid + '\'' +
                ", success=" + success +
                '}';
    }
}
