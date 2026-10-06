package ru.alfastrah.site.avto.adapter.contract.signed.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetAnyContractSignedRequestType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedRequestType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedResponseType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedRequest;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedResponse;
@Slf4j
@Service
public class ContractSignedRestClient {

    @Value("${contract-signed.rest.service.url}")
    private String restControllerUrl;

    @Autowired
    private RestTemplate restTemplate;

    public GetContractSignedResponseType getAnyContractSigned(GetAnyContractSignedRequestType request) {
        HttpEntity<GetAnyContractSignedRequestType> requestEntity = new HttpEntity<>(request);
        return restTemplate.postForObject(restControllerUrl + "GetAnyContractSignedLocal", requestEntity, GetContractSignedResponseType.class);
    }

    public GetContractSignedResponseType getContractSigned(GetContractSignedRequestType request) {
        HttpEntity<GetContractSignedRequestType> requestEntity = new HttpEntity<>(request);
        return restTemplate.postForObject(restControllerUrl + "GetContractSigned", requestEntity, GetContractSignedResponseType.class);
    }

    public GetContractSignedResponseType getContractSignedLocal(GetContractSignedRequestType request) {
        HttpEntity<GetContractSignedRequestType> requestEntity = new HttpEntity<>(request);
        return restTemplate.postForObject(restControllerUrl + "GetContractSignedLocal", requestEntity, GetContractSignedResponseType.class);
    }

    public SendContractSignedResponse sendContractSigned(SendContractSignedRequest request) {
        HttpEntity<SendContractSignedRequest> requestEntity = new HttpEntity<>(request);
        return restTemplate.postForObject(restControllerUrl + "SendContractSigned", requestEntity, SendContractSignedResponse.class);
    }
}
