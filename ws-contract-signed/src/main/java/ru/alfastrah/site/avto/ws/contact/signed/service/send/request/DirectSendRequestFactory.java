package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceException;
import ru.alfastrah.interplat4.altcraft.model.Attachment;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.interplat4.altcraft.util.ProductConstants;
import ru.alfastrah.interplat4.altcraft.util.TriggerConstants;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.send.EntityType;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.MsPaymentClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.model.ReceiptContract;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.model.ReceiptInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.hid.FuzzySearchClientHidService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.JuridicalPersonClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.usr.RSaleContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_DB_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_FIELD_NAME;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_MATCHING;

@Slf4j
@Service
public class DirectSendRequestFactory {

    private final FuzzySearchClientHidService hidClientService;
    private final ClientInfoFactory clientInfoFactory;
    private final SubscriptionFactory subscriptionFactory;
    private final AttachmentFactory attachmentFactory;
    private final BuildEmail buildEmail;
    private final PartnersDb partnersDb;
    private final MsPaymentClient client;
    private final UnicusUsrService usrService;

    @Value("${altcraft.token}")
    private String altcraftToken;

    public DirectSendRequestFactory(FuzzySearchClientHidService hidClientService, ClientInfoFactory clientInfoFactory, SubscriptionFactory subscriptionFactory, AttachmentFactory attachmentFactory, BuildEmail buildEmail, PartnersDb partnersDb, MsPaymentClient client, UnicusUsrService usrService) {
        this.hidClientService = hidClientService;
        this.clientInfoFactory = clientInfoFactory;
        this.subscriptionFactory = subscriptionFactory;
        this.attachmentFactory = attachmentFactory;
        this.buildEmail = buildEmail;
        this.partnersDb = partnersDb;
        this.client = client;
        this.usrService = usrService;
    }

    public Request create(DirectSendContractSignedRequest sendRequest) {
        ReceiptInfoResponse receiptInfo = new ReceiptInfoResponse();
        RSaleContract contract;
        if (sendRequest.getEntityType() == EntityType.SINGLE_ACC) {
            receiptInfo = client.getInfo(sendRequest.getEntityId());
            contract = getRSaleContract(receiptInfo.getMasterContractId().longValue());
        } else {
            contract = getRSaleContract(sendRequest.getEntityId().longValue());
        }
        Long subjectId = contract.getContractSubjects().get(0).getOwnerSubjectId();
        ClientInfo fio = clientInfoFactory.byContract(contract);
        SaleContract data = fillDataField(fio);
        Request request = metaData();
        if (fio instanceof JuridicalPersonClientInfo) {
            request.setFieldValue(subjectId.toString());
        } else {
            request.setFieldValue(Optional.ofNullable(hidClientService.searchHid(fio, sendRequest.getEmail()))
                    .orElse(subjectId).toString());
        }
        request.setData(data);
        request.setTriggerId(TriggerConstants.THANKS_POVTOR);
        ContentFormatted contentFormatted = new ContentFormatted();
        contentFormatted.setProduct(ProductConstants.THANKS_POVTOR);
        request.setContent(contentFormatted);
        if (sendRequest.getEntityType() == EntityType.CONTRACT) {
            try {
                RSaleContract rContract = Optional.ofNullable(partnersDb.getContractInfo(sendRequest.getEntityId()))
                        .orElse(new RSaleContract());
                rContract.setContractId(sendRequest.getEntityId().longValue());
                String printFormId = buildEmail.getPrintFormId(rContract, StringUtils.EMPTY);
                request.setAttach(attachmentFactory.create(sendRequest.getEntityId(), printFormId));
                contentFormatted.setTema("\"АльфаСтрахование\". Ваш полис");
            } catch (EOsagoException e) {
                log.error("Ошибка добавления печатной формы к письму {}", sendRequest.getEntityId(), e);
            }
        } else {
            contentFormatted.setTema("\"АльфаСтрахование\". Ваши полисы");
            List<Attachment> attachments = new ArrayList<>();
            request.setAttach(attachments);
            for (ReceiptContract receiptContract : receiptInfo.getContractList()) {
                try {
                    RSaleContract rContract = Optional.ofNullable(partnersDb.getContractInfo(receiptContract.getId()))
                            .orElse(new RSaleContract());
                    rContract.setContractId(receiptContract.getId().longValue());
                    String printFormId = buildEmail.getPrintFormId(rContract, StringUtils.EMPTY);
                    attachments.addAll(attachmentFactory.create(receiptContract.getId(), printFormId));
                } catch (Exception e) {
                    log.error("Ошибка добавления печатной формы к письму {}", receiptContract.getId(), e);
                }
            }

        }
        return request;
    }

    public void fillDataSubscription(Request request, String email) {
        SaleContract data = (SaleContract) request.getData();
        data.setSubscriptions(subscriptionFactory.create(email));
    }

    private SaleContract fillDataField(ClientInfo fio) {
        SaleContract saleContract = new SaleContract();

        String firstName = StringUtils.capitalize(fio.firstName());
        String lastName = StringUtils.capitalize(fio.lastName());
        String middleName = StringUtils.capitalize(fio.middleName());

        saleContract.setFirstName(firstName);
        saleContract.setLastName(lastName);
        saleContract.setMiddleName(middleName);
        return saleContract;
    }

    private RSaleContract getRSaleContract(Long contractId) throws EOsagoSaveException {
        RSaleContract contract = null;
        log.debug("Запрос на clientUsr.getContract. Параметры: contractId={}", contractId);
        try {
            contract = usrService.getFullSaleContract(contractId);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
        log.debug("Ответ из clientUsr.getContract:{}", contract);
        if (contract == null) {
            throw new EOsagoSaveException("Контракт не найден. contractId=" + contractId, "clientUsr.getContract");
        }
        return contract;
    }

    private Request metaData() {
        Request request = new Request();
        request.setToken(altcraftToken);
        request.setDbId(ALTCRAFT_DB_ID);
        request.setMatching(ALTCRAFT_MATCHING);
        request.setFieldName(ALTCRAFT_FIELD_NAME);
        return request;
    }
}