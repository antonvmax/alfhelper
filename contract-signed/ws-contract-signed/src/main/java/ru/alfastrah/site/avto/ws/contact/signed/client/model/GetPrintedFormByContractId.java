package ru.alfastrah.site.avto.ws.contact.signed.client.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class GetPrintedFormByContractId {
    private BigInteger contractId;
    private String printedFormId;
    private String params;
}