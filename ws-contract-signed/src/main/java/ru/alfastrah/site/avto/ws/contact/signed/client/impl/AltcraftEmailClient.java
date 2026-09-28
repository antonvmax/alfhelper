package ru.alfastrah.site.avto.ws.contact.signed.client.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;

@Service
public class AltcraftEmailClient implements AltcraftClient {

    @Value("${altcraft.rest.service.url:https://akd.alfastrahmail.ru:7443}")
    private String altcraftUrl;
    private static final String IMPORT_AND_START = "/api/v1.1/campaigns/triggers/import_and_start";
    private final RestTemplate restTemplate;

    public AltcraftEmailClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Response sendEmail(Request request) {
        HttpEntity<Request> requestEntity = new HttpEntity<>(request);
        return restTemplate.postForObject(altcraftUrl + IMPORT_AND_START, requestEntity, Response.class);
    }
}