package ru.alfastrah.site.avto.payment.cheque.client.partners.interaction;

import lombok.Builder;

@Builder
public record SearchByUpidAndContractIdRequest(
        String upid,
        Long contractId
) {
}
