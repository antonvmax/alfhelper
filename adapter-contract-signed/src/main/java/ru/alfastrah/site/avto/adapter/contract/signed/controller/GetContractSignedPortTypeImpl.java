package ru.alfastrah.site.avto.adapter.contract.signed.controller;

import io.micrometer.core.annotation.Timed;
import org.apache.cxf.binding.soap.SoapFault;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.HttpServerErrorException;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedPortType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedRequestType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedResponseType;
import ru.alfastrah.site.avto.adapter.contract.signed.client.ContractSignedRestClient;
import ru.alfastrah.site.avto.adapter.contract.signed.utils.CreateSoapFault;


@Controller
public class GetContractSignedPortTypeImpl implements GetContractSignedPortType {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetContractSignedPortTypeImpl.class);

    @Autowired
    private ContractSignedRestClient contractSignedRestClient;
    @Autowired
    private CreateSoapFault createFault;

    @Override
    @Timed(value = "cxf_requests", histogram = true)
    public GetContractSignedResponseType getContractSigned(GetContractSignedRequestType request) {
        LOGGER.trace("SOAP GetContractSigned redirected to Rest");
        try {
            return contractSignedRestClient.getContractSigned(request);
        } catch (HttpServerErrorException ex) {
            JSONObject jsonObject = new JSONObject(ex.getResponseBodyAsString());
            SoapFault soapFault = createFault.createSoapFault(ex, jsonObject.get("message").toString());
            soapFault.setStackTrace(new StackTraceElement[]{});
            LOGGER.error("SoapFault: {}", soapFault.toString());
            throw soapFault;
        }
    }
}
