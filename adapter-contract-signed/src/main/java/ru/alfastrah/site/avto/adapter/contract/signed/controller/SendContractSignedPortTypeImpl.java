package ru.alfastrah.site.avto.adapter.contract.signed.controller;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedPortType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedRequest;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedResponse;
import ru.alfastrah.site.avto.adapter.contract.signed.client.ContractSignedRestClient;

@Controller
public class SendContractSignedPortTypeImpl implements SendContractSignedPortType {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendContractSignedPortTypeImpl.class);

    @Autowired
    private ContractSignedRestClient contractSignedRestClient;
    @Override
    @Timed(value = "cxf_requests", histogram = true)
    public SendContractSignedResponse sendContractSigned(SendContractSignedRequest request) {
        LOGGER.trace("SOAP SendContractSigned redirected to Rest");
        return contractSignedRestClient.sendContractSigned(request);
    }
}
