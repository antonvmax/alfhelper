package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceException;
import ru.alfastrah.interplat4.altcraft.model.Attachment;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.interplat4.altcraft.model.Subscription;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.exception.*;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.EmailRecipientFio;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailResponse;
import ru.alfastrah.site.avto.ws.contact.signed.utils.FIOParser;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALCRAFT_FORMAT_BASE64;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_CHANNEL;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_DB_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_FIELD_NAME;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_MATCHING;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_PERSONAL_TRIGGER_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_RESOURCE_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_STATUS;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ATTACHMENT_FILENAME;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.LK_PRODUCT;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.UNICUS_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendingPersonalPolicyEmailService {

    private final GetPolicyBase64 getPolicyBase64;
    private final BusAsContractService asContractService;
    private final AltcraftClient altcraftEmailClient;
    private final UnicusUsrService clientUsr;
    private final UnicusSubjectService clientSubject;
    private final FIOParser fioParser;

    @Value("${altcraft.token}")
    private String altcraftToken;

    public SendingPersonalPolicyEmailResponse sendingPolicy(SendingPersonalPolicyEmailRequest request) {
        BigInteger contractId;
        if (Optional.ofNullable(request.getSystem()).orElse("").contains("AVIS")) {
            contractId = Optional.ofNullable(request.getContractId())
                    .orElseThrow(() -> new InvalidRequestException("Недостаточно данных. Отсутствует contractId"));
        } else {
            contractId = asContractService.getContractId(request);
        }

        Request alcraftRequest = createAlcraftRequest(contractId.longValue(), request);

        Response response = sendAlcraft(alcraftRequest);
        log.info("AlcraftResponse {}", response);

        if (0 == response.getError()) {
            return new SendingPersonalPolicyEmailResponse(true, "Successful operation");
        } else {
            throw new InvalidRequestException("Invalid request");
        }
    }

    private Response sendAlcraft(Request alcraftRequest) {
        try {
            return altcraftEmailClient.sendEmail(alcraftRequest);
        } catch (Exception e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    @NotNull
    private Request createAlcraftRequest(Long contractId, SendingPersonalPolicyEmailRequest request) {
        String policyBase64 = getPolicyBase64.getPolicyByContractId(contractId, request);
        if (policyBase64.isBlank()) {
            log.error("policyBase64: {}", policyBase64);
            throw new InvalidPrintFormException("Invalid PrintForm");
        }

        Request altcraftRequest = new Request();
        altcraftRequest.setToken(altcraftToken);
        altcraftRequest.setDbId(ALTCRAFT_DB_ID);
        altcraftRequest.setMatching(ALTCRAFT_MATCHING);
        altcraftRequest.setFieldName(ALTCRAFT_FIELD_NAME);
        altcraftRequest.setTriggerId(ALTCRAFT_PERSONAL_TRIGGER_ID);
        RSaleContract rSaleContract = getRSaleContract(contractId);
        altcraftRequest.setFieldValue(UNICUS_SUBJECT + rSaleContract.getSubjectId());
        altcraftRequest.setContent(fillContent(request));
        Attachment attachment = new Attachment();
        attachment.setData(ALCRAFT_FORMAT_BASE64 + policyBase64);
        attachment.setName(ATTACHMENT_FILENAME);
        altcraftRequest.setAttach(List.of(attachment));

        if (Optional.ofNullable(request.getSystem()).orElse("").contains("AVIS")) {
            altcraftRequest.setData(fillDataField(request.getEmail(), fioParser.parseFIO(request.getFio())));
        } else {
            altcraftRequest.setData(fillDataField(request.getEmail(), getFIO(rSaleContract.getSubjectId())));
        }
        return altcraftRequest;
    }

    private RSaleContract getRSaleContract(Long contractId) {
        RSaleContract contract;
        log.debug("Запрос на clientUsr.getContract. Параметры: contractId={}", contractId);
        try {
            contract = clientUsr.getContract(contractId);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
        log.debug("Ответ из clientUsr.getContract:{}", contract);
        if (contract == null) {
            throw new EOsagoSaveException("Контракт не найден. contractId=" + contractId, "clientUsr.getContract");
        }
        return contract;
    }

    private SaleContract fillDataField(String email, EmailRecipientFio fio) {
        SaleContract saleContract = new SaleContract();

        String firstName = StringUtils.capitalize(fio.getFirstName());
        String lastName = StringUtils.capitalize(fio.getLastName());
        String middleName = StringUtils.capitalize(fio.getMiddleName());

        saleContract.setFirstName(firstName);
        saleContract.setLastName(lastName);
        saleContract.setMiddleName(middleName);
        saleContract.setSubscriptions(fillSubscriptions(email));
        return saleContract;
    }

    private List<Subscription> fillSubscriptions(String email) {
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscription.setChannel(ALTCRAFT_CHANNEL);
        subscription.setEmail(email);
        subscription.setResourceId(ALTCRAFT_RESOURCE_ID);
        subscription.setStatus(ALTCRAFT_STATUS);
        subscriptions.add(subscription);
        return subscriptions;
    }

    private EmailRecipientFio getFIO(Long subjectId) {
        RPhysicalPerson subjectInfo;
        log.debug("Запрос на clientSubject.getPhysicalPerson. Параметры: subjectId={}", subjectId);
        try {
            subjectInfo = clientSubject.getPhysicalPerson(subjectId);
        } catch (WebServiceException e) {
            throw new SendContractServerException(e.getMessage());
        }
        log.debug("Ответ из clientSubject.getPhysicalPerson:{}", subjectInfo);
        EmailRecipientFio fio = new EmailRecipientFio();
        if (subjectInfo != null) {
            fio.setFirstName(subjectInfo.getFirstName().toLowerCase());
            fio.setMiddleName(Optional.ofNullable(subjectInfo.getMiddleName()).orElse("").toLowerCase());
            fio.setLastName(subjectInfo.getLastName().toLowerCase());
            fio.setBirthDate(subjectInfo.getBirthDate());
        }
        return fio;
    }

    private ContentFormatted fillContent(SendingPersonalPolicyEmailRequest request) {
        ContentFormatted contentFormatted = new ContentFormatted();
        contentFormatted.setProduct(LK_PRODUCT);
        contentFormatted.setContractNumber(request.getContractNumber());
        return contentFormatted;
    }
}

