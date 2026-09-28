package ru.alfastrah.site.avto.ws.contact.signed.client.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.model.ReceiptInfoResponse;

import java.math.BigInteger;

@Slf4j
@Service
public class MsPaymentClient {

    private static final String URL = "http://ms-payment:8140/payment/receipt/";

    private final RestTemplate restTemplate;

    public MsPaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ReceiptInfoResponse getInfo(BigInteger receiptId) {
        try {
            return restTemplate.getForObject(URL + receiptId, ReceiptInfoResponse.class);
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка получения информации по единому чеку {}", receiptId, e);
        }
        return new ReceiptInfoResponse();
    }
}
