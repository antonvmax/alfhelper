package ru.alfastrah.site.avto.ws.contact.signed.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyBalanceResponse;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientResponse;

@Slf4j
@Service
public class LoyaltyServiceClient {

    private String searchClientAddress;
    private String balanceAddress;

    private final RestTemplate restTemplate;

    public LoyaltyServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }

    public SearchClientResponse searchClient(String email) {
        HttpEntity<SearchClientRequest> request = new HttpEntity<>(new SearchClientRequest(email));
        try {
            return restTemplate.postForObject(searchClientAddress, request, SearchClientResponse.class);
        } catch (HttpStatusCodeException exception) {
            log.error("Ошибка поиска клиента в системе лояльности, {}", exception.getMessage());
        }
        return new SearchClientResponse();
    }

    public LoyaltyBalanceResponse findClientBalance(Long clientId) {
        try {
            return restTemplate.getForObject(balanceAddress, LoyaltyBalanceResponse.class, clientId);
        } catch (HttpStatusCodeException ex) {
            log.error("Ошибка получения баланса лояльности по {}, {}", clientId, ex.getMessage());
        }
        return new LoyaltyBalanceResponse();
    }

    @Value("${loyalty.rest.service.url}")
    public void setDefaultAddress(String url) {
        this.searchClientAddress = url + "/searchClient";
        this.balanceAddress = url + "/alfa/getBalance?clientId={clientId}";
    }
}
