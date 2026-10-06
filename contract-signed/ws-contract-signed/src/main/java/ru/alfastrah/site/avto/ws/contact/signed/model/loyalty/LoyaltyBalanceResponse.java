package ru.alfastrah.site.avto.ws.contact.signed.model.loyalty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyBalanceResponse {
    private String status;
    private String percent;
    private Long amountPoints;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public Long getAmountPoints() {
        return amountPoints;
    }

    public void setAmountPoints(Long amountPoints) {
        this.amountPoints = amountPoints;
    }
}
