package ru.alfastrah.site.avto.adapter.contract.signed.controller;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetAnyContractSignedPortType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetAnyContractSignedRequestType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedResponseType;
import ru.alfastrah.site.avto.adapter.contract.signed.client.ContractSignedRestClient;

@Controller
public class GetAnyContractSignedPortTypeImpl implements GetAnyContractSignedPortType {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAnyContractSignedPortTypeImpl.class);

    @Autowired
    private ContractSignedRestClient contractSignedRestClient;

    @Override
    @Timed(value = "cxf_requests", histogram = true)
    public GetContractSignedResponseType getAnyContractSigned(GetAnyContractSignedRequestType request) {
        LOGGER.trace("SOAP GetAnyContractSignedLocal redirected to Rest");
        return contractSignedRestClient.getAnyContractSigned(request);
    }
}
