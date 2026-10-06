package ru.alfastrah.site.avto.ws.contact.signed.client.payment.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ReceiptInfoResponse {
    private BigInteger id;
    private String number;
    private BigDecimal total;
    private BigInteger masterContractId;
    private List<ReceiptContract> contractList;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigInteger getMasterContractId() {
        return masterContractId;
    }

    public void setMasterContractId(BigInteger masterContractId) {
        this.masterContractId = masterContractId;
    }

    public List<ReceiptContract> getContractList() {
        if (contractList == null) {
            return new ArrayList<>();
        }
        return contractList;
    }

    public void setContractList(List<ReceiptContract> contractList) {
        this.contractList = contractList;
    }

}
