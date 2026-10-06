package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.CBLoggerResponse;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.SiteRecord;

@Service
public class CBLogClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CBLogClient.class);

    private final String cbLoggerUrl;
    private final String authorization;
    private final RestTemplate restTemplate;

    public CBLogClient(RestTemplate restTemplate,
                       @Value("${cblogger.rest.service.url}")
                       String cbLoggerUrl,
                       @Value("${cblogger.rest.service.authorization}")
                       String authorization) {
        this.restTemplate = restTemplate;
        this.authorization = authorization;
        this.cbLoggerUrl = cbLoggerUrl;
    }

    public void logContractSignedRequest(SiteRecord siteRecord) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);
        headers.set("Accept", MediaType.ALL_VALUE);
        HttpEntity<SiteRecord> requestEntity = new HttpEntity<>(siteRecord, headers);
        CBLoggerResponse response = restTemplate
                .exchange(cbLoggerUrl, HttpMethod.POST, requestEntity, CBLoggerResponse.class, siteRecord).getBody();
        if (response != null && (!response.isSuccess())) {
            LOGGER.error("Логирование прошло неудачно {}", response);
        }
    }

}
