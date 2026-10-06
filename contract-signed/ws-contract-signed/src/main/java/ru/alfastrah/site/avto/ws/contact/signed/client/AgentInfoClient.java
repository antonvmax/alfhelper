package ru.alfastrah.site.avto.ws.contact.signed.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.model.RealContractResponse;
import ru.alfastrah.site.avto.ws.contact.signed.model.juridical.AgentInfoResponse;

@Slf4j
@Service
public class AgentInfoClient {
    @Value("${ms.references.rest.service.url}")
    private String url;

    private final RestTemplate restTemplate;

    public AgentInfoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AgentInfoResponse getAgentInfo(Long agentId) {
        try {
            ResponseEntity<AgentInfoResponse> response = restTemplate.exchange(url + "/partner/info?agentId={1}",
                    HttpMethod.GET,
                    null,
                    AgentInfoResponse.class,
                    agentId);
            return response.getBody();
        } catch (HttpStatusCodeException exception) {
            log.error("Ошибка получения AgentInfo => {}", exception.toString());
        }
        return new AgentInfoResponse();
    }
}
