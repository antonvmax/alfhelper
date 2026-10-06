package ru.alfastrah.site.avto.ws.contact.signed.client.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureInfo {

    private String expirationDate;
    private String fingerprint;
    private String issuer;
    private String algorithm;

}
