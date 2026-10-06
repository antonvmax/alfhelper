package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.WebServiceException;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.model.MailingContractResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.subject.RTransport;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_DB_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_FIELD_NAME;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_MATCHING;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.PASSBOOK;

public abstract class RequestFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestFactory.class);

    private static final int CONTRACT_PREMIUM_UNWANTED_DIGITS = 2;

    private final ClientInfoFactory clientInfoFactory;
    private final SubscriptionFactory subscriptionFactory;
    private final UnicusUsrService clientUsr;
    private final UnicusSubjectService clientSubject;
    private final AttachmentFactory attachmentFactory;
    private final MailingClient mailingClient;

    @Value("${altcraft.token}")
    protected String altcraftToken;

    protected RequestFactory(ClientInfoFactory clientInfoFactory,
                             SubscriptionFactory subscriptionFactory,
                             UnicusUsrService clientUsr,
                             UnicusSubjectService clientSubject,
                             AttachmentFactory attachmentFactory,
                             MailingClient mailingClient) {
        this.clientInfoFactory = clientInfoFactory;
        this.subscriptionFactory = subscriptionFactory;
        this.clientUsr = clientUsr;
        this.clientSubject = clientSubject;
        this.attachmentFactory = attachmentFactory;
        this.mailingClient = mailingClient;
    }

    public Request create(SendContractSignedRequest signedRequest) {
        RSaleContract contract = getRSaleContract(signedRequest.getContractId().longValue());
        Request request = metaData();
        request.setFieldValue(getHid(contract));
        request.setTriggerId(triggerId());
        ClientInfo fio = clientInfoFactory.byContract(contract);
        SaleContract data = fillDataField(fio);
        request.setData(data);
        request.setContent(this.createContent(fio, contract, signedRequest, data));
        data.setSubscriptions(subscriptionFactory.create(signedRequest.getEmail().trim()));
        logRequestToAltcraftWithoutAttachments(request);
        request.setAttach(attachmentFactory.create(signedRequest.getContractId(), signedRequest.getPrintedFormId()));
        return request;
    }

    private Request metaData() {
        Request request = new Request();
        request.setToken(altcraftToken);
        request.setDbId(ALTCRAFT_DB_ID);
        request.setMatching(ALTCRAFT_MATCHING);
        request.setFieldName(ALTCRAFT_FIELD_NAME);
        return request;
    }

    protected SaleContract fillDataField(ClientInfo fio) {
        SaleContract saleContract = new SaleContract();

        String firstName = StringUtils.capitalize(fio.firstName());
        String lastName = StringUtils.capitalize(fio.lastName());
        String middleName = StringUtils.capitalize(fio.middleName());

        saleContract.setFirstName(firstName);
        saleContract.setLastName(lastName);
        saleContract.setMiddleName(middleName);
        return saleContract;
    }



    protected String formatInsurancePremium(BigDecimal insurancePremium) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        String result = numberFormat.format(insurancePremium);

        if (StringUtils.isNotEmpty(result)) {
            return result.substring(0, result.length() - CONTRACT_PREMIUM_UNWANTED_DIGITS);
        }

        return insurancePremium.toString();
    }

    private RSaleContract getRSaleContract(Long contractId) throws EOsagoSaveException {
        RSaleContract contract = null;
        LOGGER.debug("Запрос на clientUsr.getContract. Параметры: contractId={}", contractId);
        try {
            contract = clientUsr.getFullSaleContract(contractId);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
        LOGGER.debug("Ответ из clientUsr.getContract:{}", contract);
        if (contract == null) {
            throw new EOsagoSaveException("Контракт не найден. contractId=" + contractId, "clientUsr.getContract");
        }
        return contract;
    }


    protected String createPassbookUrl(BigInteger contractId) {
        return String.format(PASSBOOK,
                contractId == null ? StringUtils.EMPTY : contractId.toString(),
                DigestUtils.md5Hex((contractId == null ? StringUtils.EMPTY : contractId.toString()) + "alfastrah"));
    }

    protected String getFirstLastNames(ClientInfo fio) {
        return StringUtils.capitalize(fio.firstName()) + " " + StringUtils.capitalize(fio.lastName());
    }

    protected String getMarkaModelNames(Long subjectId) {
        List<RTransport> transportList = null;
        transportList = clientSubject.getTransport(subjectId);
        if (transportList.isEmpty()) {
            return null;
        }
        RTransport rTransport = transportList.get(0);
        String transportMark = rTransport.getTransportMark().toLowerCase();
        String transportModel = rTransport.getTransportModel().toLowerCase();

        return StringUtils.capitalize(transportMark) + " " + StringUtils.capitalize(transportModel);
    }

    protected List<RContractSubject> getSubjectList(Long contractId) throws EOsagoSaveException {
        List<RContractSubject> subjectList = null;
        LOGGER.debug("Запрос на clientUsr.getContractSubject. Параметры: contractId={}", contractId);
        try {
            subjectList = clientUsr.getContractSubject(contractId, 0L);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
        LOGGER.debug("Ответ из clientUsr.getContractSubject:{}", subjectList);
        if (subjectList == null) {
            throw new EOsagoSaveException("Subject не найден. contractId=" + contractId, "clientUsr.getContractSubject");
        }
        return subjectList;
    }

    protected String getHid(RSaleContract contract) {
        try {
            MailingContractResponse response = mailingClient.getInfo(contract.getContractId());
            Long hid = response.getPartyHid();

            return hid == null ? String.valueOf(contract.getSubjectId()) : hid.toString();
        } catch (Exception e) {
            LOGGER.error("Произошла ошибка при поиске hid : " + e.getMessage());
            return String.valueOf(contract.getSubjectId());
        }
    }

    private void logRequestToAltcraftWithoutAttachments(Request request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonRequest = mapper.writeValueAsString(request);
            LOGGER.trace(jsonRequest);
        } catch (JsonProcessingException e) {
            LOGGER.error("Cant writeValueAsString from request to altcraft");
        }
    }

    protected abstract Integer triggerId();

    protected abstract ContentFormatted createContent(ClientInfo clientInfo, RSaleContract contract, SendContractSignedRequest request, SaleContract data);
}
