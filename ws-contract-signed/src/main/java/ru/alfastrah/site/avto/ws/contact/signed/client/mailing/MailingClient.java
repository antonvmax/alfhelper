package ru.alfastrah.site.avto.ws.contact.signed.client.mailing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.model.MailingContractResponse;

@Slf4j
@Service
public class MailingClient {

    @Value("${mailing.url}")
    private String mailingUrl;
    private static final String CONTRACT = "/mailing/prolong/contact?contractId={1}";
    private final RestTemplate restTemplate;

    public MailingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MailingContractResponse getInfo(Long contractId) {
        try {
            ResponseEntity<MailingContractResponse> response = restTemplate.exchange(mailingUrl + CONTRACT,
                    HttpMethod.GET,
                    null,
                    MailingContractResponse.class,
                    contractId);

            return response.getBody();
        } catch (HttpStatusCodeException exception) {
            log.error("Ошибка получения TransactionInfo => {}", exception.toString());
        }
        return null;
    }
}