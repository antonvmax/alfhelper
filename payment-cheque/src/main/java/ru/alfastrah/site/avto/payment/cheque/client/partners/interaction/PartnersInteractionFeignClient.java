package ru.alfastrah.site.avto.payment.cheque.client.partners.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "partners-interaction-client", url = "http://partners-interaction:8080")
public interface PartnersInteractionFeignClient {

    @PostMapping(path = "/searchByUpidAndContractId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Long searchByUpidAndContractId(@RequestBody SearchByUpidAndContractIdRequest request);

}
