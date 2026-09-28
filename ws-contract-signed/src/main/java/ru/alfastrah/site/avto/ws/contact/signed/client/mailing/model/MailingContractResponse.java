package ru.alfastrah.site.avto.ws.contact.signed.client.mailing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailingContractResponse {
    private String email;
    private String phone;
    private Long partyHid;
}