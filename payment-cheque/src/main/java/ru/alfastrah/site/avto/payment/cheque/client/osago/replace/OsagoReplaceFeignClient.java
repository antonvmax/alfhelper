package ru.alfastrah.site.avto.payment.cheque.client.osago.replace;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alfastrah.alfadigital.RealContractResponse;

@FeignClient(name="osago-replace", url = "#{environment.acceptsProfiles('default', 'dev') ? 'http://ms-adt-inner.vesta.ru' : 'http://ms-osago-replace:8480'}")
public interface OsagoReplaceFeignClient {
    @GetMapping(path = "/osago-replace/partner/info", consumes = {MediaType.APPLICATION_JSON_VALUE})
    RealContractResponse realContract(
            @RequestParam(name = "fakeContractId", required = false) String fakeContractId);
}
