package ru.alfastrah.site.avto.ws.contact.signed.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.model.RealContractResponse;

@Service
@Slf4j
public class OsagoReplaceClient {

    @Value("${osago.replace.rest.service.url}")
    private String url;

    private final RestTemplate restTemplate;

    public OsagoReplaceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RealContractResponse getPartnerInfo(Long fakeId) {
        try {
            ResponseEntity<RealContractResponse> response = restTemplate.exchange(url + "?fakeContractId={1}",
                    HttpMethod.GET,
                    null,
                    RealContractResponse.class,
                    fakeId);
            return response.getBody();
        } catch (HttpStatusCodeException exception) {
            log.error("Ошибка получения PartnerInfo => {}", exception.toString());
        }
        return new RealContractResponse().realContractId(fakeId);
    }
}
