package ru.alfastrah.site.avto.ws.contact.signed.client.payment.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ReceiptContract {
    private BigInteger id;
    private BigDecimal premium;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }
}
