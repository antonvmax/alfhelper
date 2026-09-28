package ru.alfastrah.site.avto.ws.contact.signed.service;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.client.OsagoReplaceClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.RealContractResponse;

import java.io.IOException;
import java.math.BigInteger;

@Service
@Slf4j
public class ReplaceService {

    private final OsagoReplaceClient osagoReplaceApi;
    private final PdfClient printFormService;

    public ReplaceService(OsagoReplaceClient osagoReplaceApi, PdfClient printFormService) {
        this.osagoReplaceApi = osagoReplaceApi;
        this.printFormService = printFormService;
    }

    public GetContractSignedResponseType getPrintFormForFakeId(GetContractSignedRequestType getContractSignedRequestType) {
        GetContractSignedResponseType responseType = new GetContractSignedResponseType();
        responseType.setPrintedFormId("541");
        DataHandler printFormExample = printFormService.getPrintedFormByContractId(getContractSignedRequestType.getContractId(), "541", null);
        try {
            responseType.setContent(
                    new DataHandler(
                            new ByteArrayDataSource(
                                    printFormExample.getDataSource().getInputStream().readAllBytes(),
                                    MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));
        } catch (IOException e) {
            log.error("Не удалось получить данные формы");
        }
        responseType.setMime("application/pdf");
        return responseType;
    }

    public void replaceFakeContractId(GetContractSignedRequestType getContractSignedRequestType) {
        BigInteger contractId = getContractSignedRequestType.getContractId();
        log.info("Replace method for => {}", contractId);
        if (BigInteger.ZERO.compareTo(contractId) > 0) {
            log.debug("Вызываем сервис подмены с запросом => {}", getContractSignedRequestType);
            RealContractResponse partnerInfo = osagoReplaceApi.getPartnerInfo(contractId.longValue());
            getContractSignedRequestType.setContractId(BigInteger.valueOf(partnerInfo.getRealContractId()));
            log.info("contractId = {} замененен на {}", contractId, partnerInfo.getRealContractId());
        }
    }
}
