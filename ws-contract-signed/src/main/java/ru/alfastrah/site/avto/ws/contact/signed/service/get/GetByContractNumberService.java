package ru.alfastrah.site.avto.ws.contact.signed.service.get;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetAnyContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.model.get.GetByContractNumberRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.get.GetByContractNumberResponse;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;

import java.math.BigInteger;

@Service
public class GetByContractNumberService {
    private final GetAnyContractSignedService getService;
    private final UnicusUsrService usrService;

    public GetByContractNumberService(GetAnyContractSignedService getService, UnicusUsrService usrService) {
        this.getService = getService;
        this.usrService = usrService;
    }

    public GetByContractNumberResponse getPrintForm(GetByContractNumberRequest request) {
        long contractId = usrService.findContract(request.getContractSeries(), request.getContractNumber(), 0, null);
        if (contractId == 0) {
            throw new BadDataException("Не удалось найти договор");
        }
        GetAnyContractSignedRequestType getAnyRequest = new GetAnyContractSignedRequestType();
        getAnyRequest.setContractId(BigInteger.valueOf(contractId));
        getAnyRequest.setPrintedFormId(request.getPrintedFormId());
        GetContractSignedResponseType getContractSignedResponseType = getService.processGetAnyContractSigned(getAnyRequest);
        GetByContractNumberResponse response = new GetByContractNumberResponse();
        response.setContent(getContractSignedResponseType.getContent());
        response.setMime(getContractSignedResponseType.getMime());
        return response;
    }
}
