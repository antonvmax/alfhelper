package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.as.contracts.GetContract;
import ru.alfastrah.interplat4.as.contracts.GetContractResponse;
import ru.alfastrah.site.avto.bus.services.AsContractsService;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;

import java.math.BigInteger;

@Slf4j
@Service
public class BusAsContractService {

    private  static final String AS_FORMAT_DEFAULT = "http://schemas.alfastrah.ru/interplat4/as-contracts-1.0";
    private final AsContractsService asContractsService;

    public BusAsContractService(AsContractsService asContractsService) {
        this.asContractsService = asContractsService;
    }

    public BigInteger getContractId(SendingPersonalPolicyEmailRequest request) {
        GetContract getContractRequest = new GetContract();
        getContractRequest.setContractNumber(request.getContractNumber());
        getContractRequest.setContractSeria(request.getContractSeria());
        getContractRequest.setFormat(AS_FORMAT_DEFAULT);
        try {
            GetContractResponse response = asContractsService.getContract(getContractRequest);
            return ((JAXBElement<BigInteger>) response.getContract().getAny()).getValue();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
