package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.Result;

import java.util.Objects;

@Service
public class SignatureClient {
    @Value("${signature.hub.url}")
    private String url;
    @Value("${signature.hub.login}")
    private String login;
    @Value("${signature.hub.password}")
    private String pass;

    private final RestTemplate restTemplate;

    public SignatureClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] signBytes(byte[] fileHash, int port) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(login, pass);
        Result r = new Result();
        r.setBytes(fileHash);
        HttpEntity<Result> entity = new HttpEntity<>(r, headers);
        ResponseEntity<Result> responseEntity =
                restTemplate.exchange(
                        url + "/" + port + "/sign",
                        HttpMethod.POST,
                        entity,
                        Result.class);
        return Objects.requireNonNull(responseEntity.getBody()).getBytes();
    }
}
