package ru.alfastrah.site.avto.ws.contact.signed.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.GetPrintedFormByContractId;

@Slf4j
@Service
public class ReportClient {

    @Value("${print-form.rest.service.url}")
    private String url;

    private final RestTemplate restTemplate;

    public ReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] getPrintedFormByContractId(GetPrintedFormByContractId request) {
        try {
            return restTemplate.postForObject(url + "/contract/id", request, byte[].class);
        } catch (HttpStatusCodeException exception) {
            log.error("Ошибка получения ПФ => {}", exception.toString());
            throw new RuntimeException(exception.getMessage());
        }
    }
}