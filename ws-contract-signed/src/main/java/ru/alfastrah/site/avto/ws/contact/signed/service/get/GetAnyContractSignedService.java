package ru.alfastrah.site.avto.ws.contact.signed.service.get;

import jakarta.activation.DataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetAnyContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintFormFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;
import tops.unicus.usr.RSaleContract;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAnyContractSignedService {

    private final BuildEmail buildEmail;
    private final PartnersDb partnersDbBean;
    private final PrintFormFactory printFormFactory;
    private final GetContractResponseFactory responseFactory;
    private final StampService stampService;
    private final ErrorToleranceSigningService signingService;

    public GetContractSignedResponseType processGetAnyContractSigned(
            GetAnyContractSignedRequestType request) {
        RSaleContract contractInfo = partnersDbBean.getContractInfo(request.getContractId());
        if (contractInfo == null) {
            log.warn("GetAnyContractSigned: Нет ContractInfo по ContractId = {}", request.getContractId());
            return null;
        }
        try {
            String printFormId = buildEmail.getPrintFormId(contractInfo, request.getPrintedFormId());
            PrintForm printForm = printFormFactory.create(request.getContractId(), printFormId);
            List<StampParams> stampData = stampService.getStampData(request.getContractId(), printFormId);
            byte[] sourceByte = IOUtils.toByteArray(printForm.content().getInputStream());
            DataHandler dataHandler = signingService.sign(sourceByte, stampData, false, request.getContractId());
            return responseFactory.create(new RawPrintForm(dataHandler, printFormId));
        } catch (EOsagoSaveException | EOsagoException | IOException e) {
            log.warn("GetAnyContractSigned process error: {0}", e);
        }
        return new GetContractSignedResponseType();
    }
}
