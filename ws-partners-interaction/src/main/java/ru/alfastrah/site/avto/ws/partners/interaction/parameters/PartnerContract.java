package ru.alfastrah.site.avto.ws.partners.interaction.parameters;

import java.math.BigInteger;

public class PartnerContract {
    private BigInteger contractId;
    private BigInteger code;
    private String message;

    public BigInteger getContractId() {
        return contractId;
    }

    public void setContractId(BigInteger contractId) {
        this.contractId = contractId;
    }

    public BigInteger getCode() {
        return code;
    }

    public void setCode(BigInteger code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
