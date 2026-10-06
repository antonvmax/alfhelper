package ru.alfastrah.site.avto.ws.contact.signed.service.client.partners.interaction;

import lombok.Builder;

@Builder
public record SearchByUpidAndContractIdRequest(
        String upid,
        Long contractId
) {
}
