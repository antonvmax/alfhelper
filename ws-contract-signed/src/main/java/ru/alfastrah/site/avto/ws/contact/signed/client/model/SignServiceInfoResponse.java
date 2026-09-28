package ru.alfastrah.site.avto.ws.contact.signed.client.model;

import lombok.Getter;

@Getter
public class SignServiceInfoResponse {
    private SignatureInfo signatureInfo;
    private Integer port;
}