package ru.alfastrah.site.avto.ws.contact.signed.service.client.partners.interaction;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartnersInteractionClient {

    private final PartnersInteractionFeignClient client;

    public Long searchByUpidAndContractId(
            String upid,
            Long contractId
    ) {
        try {
            return client.searchByUpidAndContractId(request(upid, contractId));
        } catch (FeignException.FeignClientException e) {
            log.error("PartnersInteraction exception", e);
            throw e;
        }
    }

    private SearchByUpidAndContractIdRequest request(
            String upid,
            Long contractId
    ) {
        return SearchByUpidAndContractIdRequest.builder()
                .upid(upid)
                .contractId(contractId)
                .build();
    }

}
