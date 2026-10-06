package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignServiceInfoResponse;

@Service
public class CertificateInfoClient {

    @Value("${signature.hub.url}")
    private String url;
    @Value("${signature.hub.login}")
    private String login;
    @Value("${signature.hub.password}")
    private String pass;

    private final RestTemplate restTemplate;

    public CertificateInfoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SignServiceInfoResponse getInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(login, pass);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url + "/info", HttpMethod.GET, entity, SignServiceInfoResponse.class).getBody();
    }
}