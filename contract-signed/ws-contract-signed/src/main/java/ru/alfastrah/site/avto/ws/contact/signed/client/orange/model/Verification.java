package ru.alfastrah.site.avto.ws.contact.signed.client.orange.model;

import java.time.LocalDate;

public class Verification {
    private Boolean isVerified;
    private LocalDate lastVerifiedDate;

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public LocalDate getLastVerifiedDate() {
        return lastVerifiedDate;
    }

    public void setLastVerifiedDate(LocalDate lastVerifiedDate) {
        this.lastVerifiedDate = lastVerifiedDate;
    }
}
