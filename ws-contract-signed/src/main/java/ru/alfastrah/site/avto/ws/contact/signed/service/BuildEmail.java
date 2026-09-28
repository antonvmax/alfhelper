package ru.alfastrah.site.avto.ws.contact.signed.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.ws.Holder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceException;
import ru.alfastrah.site.avto.client.unicus.db.samplePak.SamplePakClient;
import ru.alfastrah.site.avto.model.unicus.db.dto.samplePak.EmailTemplateDto;
import ru.alfastrah.j8helper.NullUtils;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.unicus.db.dto.contract.ContractInfoDto;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.MarketName;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.EmailRecipientFio;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.model.MessageProperies;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl.OsagoPrintFormIdRetriever;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;
import ru.alfastrah.site.avto.ws.contact.signed.utils.Const;
import tops.unicus.subject.RJuridicalPerson;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RContractRestrictionUser;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
import static ru.alfastrah.interplat4.bus.utils.print.form.Constant.JASPER_PRINTED_FORM_ID_1;
import static ru.alfastrah.interplat4.bus.utils.print.form.Constant.JASPER_PRINTED_FORM_ID_888;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractOption.PROLONGATION;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONCLUDED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONFIRMED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.STATEMENT;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_10;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_5;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.PROPERTY_REPAIR;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.KASKO;

@Service
public class BuildEmail {

    private static final String NEIGHBOURHOOD_PROLONG_SAMPLE_PRINTFORM_ID = "921";
    private static final String NEIGHBOURHOOD_PROLONG_PRINTFORM_ID = "920";
    private static final String ALFAREPAIR_PROLONG_SAMPLE_PRINTFORM_ID = "919";
    private static final String ALFAREPAIR_PROLONG_PRINTFORM_ID = "918";
    private static final String NEIGHBOURHOOD_PRINTFORM_ID = "399";
    private static final String ALFAREPAIR_PRINGFORM_ID = "336";
    private static final String CAR_OWNER_SAMPLE_PRINTFORM_ID = "982";
    private static final String CAR_OWNER_PRINTFORM_ID = "981";
    private static final Map<String, String> FORM_ID_NAME = new HashMap<>();

    public static final String NO_EMAIL_FOR_SENDING = "Не указан e-mail для отправки!";
    private static final String CONTRACT_NOT_FOUND = "Отсутствует контракт";
    public static final String BR_BR = " <br/><br/>";
    public static final String NO_SAVED_CONTRACT_FOR_UPID = "К UPID %s нет сохраненного договора %s";
    private static final String NO_CONTRACT_FOR_UPID = "К UPID %s не привязан контракт %s";
    private static final String NO_DS_FOR_UPID = "UPID %s не соответствует ДС %s";
    private static final String NO_CONTRACT_FOR_NS = "Для контракта с ID %s не найден соответствующий контракт НС с UPID %s";

    static {
        // Идентификатор формы, Название печатной формы
        FORM_ID_NAME.put("716", "Заявление.pdf");
        FORM_ID_NAME.put("525", "Полис.pdf");
        FORM_ID_NAME.put("526", "Полис.pdf");
        FORM_ID_NAME.put("917", "Уведомление о заключении ЕОСАГО.pdf");
        FORM_ID_NAME.put("1492", "Полис с КВ.pdf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildEmail.class);
    private static final EnumSet<MarketName> NS_PRODUCT_LIST = EnumSet.of(MarketName.NS_FAMILY_PROTECTION, MarketName.NS_NO_CORONAVIRUS,
            MarketName.NS_ADDITIONAL_PROTECTION, MarketName.NS_VIRUS_PROTECTION, MarketName.NS_ALFA_VACCINE, MarketName.NS_CHILD_SPORT,
            MarketName.NS_A_SPORT);
    private static final Set<Integer> PAYED_STATUS_LIST = new HashSet<>(Arrays.asList(CONCLUDED, CONFIRMED));

    private final PartnersDb partnersDbBean;
    private final SamplePakClient samplePakClient;
    private final MarketNameService marketNameBean;
    private final PdfClient selfMadePdfClient;
    private final UnicusUsrService unicusUsrClient;
    private final UnicusSubjectService unicusSubjectClient;
    private final MessageProperies messageProperies;
    private final OsagoPrintFormIdRetriever osagoPrintFormIdRetriever;
    private final StampService stampService;
    private final ErrorToleranceSigningService signingService;

    private final String sendNotificationKaskoEmailList;
    private final String sendNotificationGeneralEmailList;

    public BuildEmail(PartnersDb partnersDbBean,
                      SamplePakClient samplePakClient,
                      MarketNameService marketNameBean,
                      PdfClient selfMadePdfClient,
                      UnicusUsrService unicusUsrClient,
                      UnicusSubjectService unicusSubjectClient,
                      MessageProperies messageProperies,
                      OsagoPrintFormIdRetriever osagoPrintFormIdRetriever,
                      StampService stampService,
                      ErrorToleranceSigningService signingService,
                      @Value("${notification.kasko.email.list}") String sendNotificationKaskoEmailList,
                      @Value("${notification.general.email.list}") String sendNotificationGeneralEmailList) {
        this.partnersDbBean = partnersDbBean;
        this.samplePakClient = samplePakClient;
        this.marketNameBean = marketNameBean;
        this.selfMadePdfClient = selfMadePdfClient;
        this.unicusUsrClient = unicusUsrClient;
        this.unicusSubjectClient = unicusSubjectClient;
        this.messageProperies = messageProperies;
        this.osagoPrintFormIdRetriever = osagoPrintFormIdRetriever;
        this.stampService = stampService;
        this.signingService = signingService;
        this.sendNotificationKaskoEmailList = sendNotificationKaskoEmailList;
        this.sendNotificationGeneralEmailList = sendNotificationGeneralEmailList;
    }

    public void createEmailBody(MimeMessageHelper helper, SendContractSignedRequest sendContractSignedRequest)
            throws MessagingException, EOsagoSaveException {
        if (StringUtils.isBlank(sendContractSignedRequest.getEmail())) {
            throw new BadDataException(NO_EMAIL_FOR_SENDING);
        }
        EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
        emailTemplateDto.setContractId(sendContractSignedRequest.getContractId());
        emailTemplateDto.setEventId(BigInteger.valueOf(Const.POLICY_PURCHASE_EVENT_ID));

        LOGGER.debug("Запрос на samplePakClient.getBigEmailLetter. Параметры: contractId={}, eventId={}",
                sendContractSignedRequest.getContractId(), Const.POLICY_PURCHASE_EVENT_ID);
        EmailTemplateDto response = samplePakClient.getBigEmailLetter(emailTemplateDto);
        LOGGER.debug("Ответ от samplePakClient получен, тема: {}", response.getLetterSubject());

        LOGGER.info("Тема письма {}", response.getLetterSubject());
        LOGGER.info("Тело письма {}", response.getLetterText());
        helper.setSubject(response.getLetterSubject());
        helper.setText(response.getLetterText(), true);
        messageProperies.setEmailBody(response.getLetterText());
        messageProperies.setRecipientEmail(sendContractSignedRequest.getEmail());
        messageProperies.setFio(getFIO(sendContractSignedRequest.getContractId().longValue()));
    }

    public void createAdditionalEmailBody(MimeMessageHelper helper, SendContractSignedRequest sendContractSignedRequest)
            throws MessagingException {
        LOGGER.trace("createAdditionalEmailBody: Get request {}", sendContractSignedRequest);
        if (org.apache.commons.lang3.StringUtils.isBlank(sendContractSignedRequest.getEmail())) {
            throw new BadDataException(NO_EMAIL_FOR_SENDING);
        }

        EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
        emailTemplateDto.setContractId(sendContractSignedRequest.getContractId());
        emailTemplateDto.setEventId(BigInteger.valueOf(Const.SPECIAL_OFFER_EVENT_ID));

        LOGGER.debug("Запрос на samplePakClient.getEmailLetter. Параметры: contractId={}, eventId={}",
                sendContractSignedRequest.getContractId(), Const.SPECIAL_OFFER_EVENT_ID);
        EmailTemplateDto response = samplePakClient.getEmailLetter(emailTemplateDto);
        LOGGER.debug("Ответ от samplePakClient.getEmailLetter получен, тело {}", response.getLetterText());

        String additionalEmailBody = response.getLetterText();
        if (StringUtils.isEmpty(additionalEmailBody)) {
            helper.getRootMimeMultipart().removeBodyPart(0);
            LOGGER.info("Тело дополнительного письма пустое для ContractId = {}", sendContractSignedRequest.getContractId());
            return;
        }

        LOGGER.info("Итоговое тело дополнительного письма: {}", additionalEmailBody);
        helper.setText(additionalEmailBody);
        String additionalEmailSubject = response.getLetterSubject();
        LOGGER.info("Итоговый заголовок дополнительного письма: {}", additionalEmailSubject);
        helper.setSubject(additionalEmailSubject);

        while (helper.getRootMimeMultipart().getCount() > 1) {
            helper.getRootMimeMultipart().removeBodyPart(helper.getRootMimeMultipart().getCount() - 1);
        }
    }

    public void createNotificationEmail(MimeMessageHelper helper, SendContractSignedRequest contractSignedRequest)
            throws EOsagoExceptionMailNoStacktrace, MessagingException {
        RSaleContract contractInfo = partnersDbBean.getContractInfo(contractSignedRequest.getContractId());
        LOGGER.trace("createNotificationEmail: Get request {}", contractSignedRequest);

        //проверка на тип договора
        if (!isSendNotificationEmail(contractSignedRequest.getContractId(), contractInfo)) {
            helper.getRootMimeMultipart().removeBodyPart(0);
            LOGGER.trace("Email is not notification");
            LOGGER.info("Не отправляем уведомительное письмо - {}", contractInfo);
            return;
        }

        //получение получателя письма
        String destination = getSendNotificationDestination(contractInfo);
        if (org.apache.commons.lang3.StringUtils.isBlank(destination)) {
            helper.getRootMimeMultipart().removeBodyPart(0);
            LOGGER.trace("Email is empty");
            return;
        }
        helper.setTo(destination);
        //подгрузка информации по договору
        RSaleContract contract = unicusUsrClient.getFullSaleContract(contractSignedRequest.getContractId().longValue());
        if (contract == null) {
            throw new EOsagoExceptionMailNoStacktrace("Не удалось получить доп. информацию по договору", CONTRACT_NOT_FOUND);
        }

        StringBuilder notificationEmailBody = new StringBuilder();
        notificationEmailBody.append("Информируем, что произошло оформление нового полиса с параметрами: <br/><br/>");
        notificationEmailBody.append("Номер полиса: " + contract.getContractNumber() + BR_BR);

        //подгрузка информации по страхователю
        RPhysicalPerson insurer = unicusSubjectClient.getPhysicalPerson(contract.getSubjectId());
        if (insurer == null) {
            //пробуем загрузить юрика
            RJuridicalPerson insurerJuridical = unicusSubjectClient.getJuridicalPerson(contract.getSubjectId());
            if (insurerJuridical != null) {
                notificationEmailBody.append("Юр. лицо страхователя: " + insurerJuridical.getSubjectName() + BR_BR);
            }
        } else {
            notificationEmailBody.append("Ф.И.О. Страхователя: " + insurer.getSubjectName() + BR_BR);
        }

        notificationEmailBody.append("Страховая премия:" + contract.getContractPremium() + BR_BR);
        notificationEmailBody.append("Данное сообщение сформировано и направлено роботом и не требует ответа. <br/><br/>");

        final String notificationEmailBodyString = notificationEmailBody.toString();
        LOGGER.info("Итоговое тело уведомительного письма: {}", notificationEmailBodyString);
        helper.setText(notificationEmailBodyString, true);

        String additionalEmailSubject = "Оформлен новый полис: " + contract.getContractNumber();
        helper.setSubject(additionalEmailSubject);
        LOGGER.info("Итоговая тема уведомительного письма: {}", additionalEmailSubject);
    }

    public boolean isSendNotificationEmail(BigInteger contractId, RSaleContract contractInfo) {
        if (contractId == null || contractInfo == null) {
            return false;
        }
        final MarketName marketName = marketNameBean.getMarketName(contractInfo);
        final boolean result;
        switch (Product.fromId(contractInfo.getVariants().get(0).getProductId())) {
            case KASKO:
                result = MarketName.KASKO_500 == marketName
                        || MarketName.KASKO_1 == marketName
                        || MarketName.KASKO_3 == marketName;
                break;
            case NS397:
                result = NS_PRODUCT_LIST.contains(marketName);
                break;
            case PRODUCT_MORTGAGE_LIFE:
            case PRODUCT_MORTGAGE_PROPERTY:
                result = true;
                break;
            default:
                return false;
        }
        return result;
    }

    public String getSendNotificationDestination(RSaleContract contractInfo) {
        if (contractInfo == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        if (KASKO.getProductId().equalsIgnoreCase(contractInfo.getVariants().get(0).getProductId())) {
            return this.sendNotificationKaskoEmailList;
        } else {
            return this.sendNotificationGeneralEmailList;
        }
    }

    public void addAttachments(MimeMessageHelper helper, SendContractSignedRequest contractSignedRequest)
            throws EOsagoSaveException, MessagingException, EOsagoExceptionMailNoStacktrace {
        createAttachmentsForExplicitForms(contractSignedRequest, helper);
    }

    private void createAttachmentsForExplicitForms(SendContractSignedRequest contractSignedRequest,
                                                   MimeMessageHelper helper)
            throws EOsagoSaveException, MessagingException, EOsagoExceptionMailNoStacktrace {
        Holder<DataHandler> content;
        String[] forms = contractSignedRequest.getPrintedFormId().split(",");
        for (String form : forms) {
            if (StringUtils.isEmpty(form)) {
                form = "-1";
            }
            try {
                LOGGER.debug(
                        "Запрос на clientPdf.getPrintedFormByContractId. " +
                                "Параметры: request.getContractId()={}, request.getPrintedFormId()={}",
                        contractSignedRequest.getContractId(), form);
                content = new Holder(selfMadePdfClient.getPrintedFormByContractId(contractSignedRequest.getContractId(), form, null));
                LOGGER.debug("Ответ от clientPdf.getPrintedFormByContractId получен");
            } catch (ClassCastException | EOsagoProccessException e) {
                LOGGER.warn("Не могу сформировать форму: {}. Для contractId={}. Ошибка:{}", form, contractSignedRequest.getContractId(), e);
                continue;
            }
            this.createAttachment(helper, content, form);
        }
    }

    @SuppressWarnings("unchecked")
    private void createAttachment(MimeMessageHelper helper,
                                  Holder<DataHandler> content,
                                  String form) throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace, MessagingException {
        DataSource data = null;
        try {
            byte[] sourceByte = IOUtils.toByteArray(content.value.getInputStream());
            content.value.getInputStream().close();
            if (sourceByte.length == 0) {
                throw new EOsagoExceptionMailNoStacktrace("Отсутствует контент для подписи", "Empty content");
            }

            data = new ByteArrayDataSource(sourceByte, APPLICATION_OCTET_STREAM_VALUE);
            LOGGER.trace("получили dataSource: {}", data);

        } catch (IOException | EOsagoSaveException e) {
            throw new EOsagoSaveException(e.getMessage(), "Create DataHandler error", e);
        }

        LOGGER.trace("перед получением имени form = {}", form);
        String fileName = FORM_ID_NAME.getOrDefault(form, Const.ATTACHMENT_FILENAME);
        LOGGER.trace("перед addAttachment");
        helper.addAttachment(encodNameFile(fileName), data);
        LOGGER.info("Файл {} добавлен в письмо", fileName);
    }

    private static String encodNameFile(String nameFile) {
        return String.format("=?UTF-8?B?%s?=", new String(Base64.encodeBase64(nameFile.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    }

    private EmailRecipientFio getFIO(Long contractId) throws EOsagoSaveException {

        RSaleContract contract = null;
        LOGGER.debug("Запрос на clientUsr.getContract. Параметры: contractId={}", contractId);
        try {
            contract = unicusUsrClient.getContract(contractId);
        } catch (WebServiceException ex) {
            throw new SendContractServerException(ex.getMessage());
        }
        LOGGER.debug("Ответ из clientUsr.getContract:{}", contract);
        if (contract == null) {
            throw new EOsagoSaveException("Договор не найден. contractId=" + contractId, "clientUsr.getContract");
        }
        long subjectId = contract.getSubjectId();

        RPhysicalPerson subjectInfo = null;
        LOGGER.debug("Запрос на clientSubject.getPhysicalPerson. Параметры: subjectId={}", subjectId);
        subjectInfo = unicusSubjectClient.getPhysicalPerson(subjectId);
        LOGGER.debug("Ответ из clientSubject.getPhysicalPerson:{}", subjectInfo);
        EmailRecipientFio fio = new EmailRecipientFio();
        if (subjectInfo != null) {
            fio.setFirstName(subjectInfo.getFirstName());
            fio.setMiddleName(Optional.ofNullable(subjectInfo.getMiddleName()).orElse(""));
            fio.setLastName(subjectInfo.getLastName());
            fio.setBirthDate(subjectInfo.getBirthDate());
        }
        return fio;
    }

    public void getAdditionalKaskoToOsago(String uuid, BigInteger contractId, boolean isDs) throws EOsagoExceptionMailNoStacktrace {
        LOGGER.debug("Запрос на partnersDbBean.isAdditionalKaskoToOsago. uuid={}, contractId={}", uuid, contractId);
        ContractInfoDto additionalKaskoInfo = partnersDbBean.getAdditionalKaskoInfo(uuid, contractId);

        if (additionalKaskoInfo == null) {
            return;
        }
        if (!PAYED_STATUS_LIST.contains(additionalKaskoInfo.getStatusId())) {
            String message = String.format("По UPID %s основной договор находиться в статусе %s",
                    uuid, additionalKaskoInfo.getStatusId());

            throw new EOsagoExceptionMailNoStacktrace(message, "Некорректный статус");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(additionalKaskoInfo.getProductId())) {
            String message = String.format(isDs ? NO_DS_FOR_UPID : NO_CONTRACT_FOR_UPID,
                    uuid, contractId);

            throw new EOsagoExceptionMailNoStacktrace(message, CONTRACT_NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    public GetContractSignedResponseType createSignedContent(BigInteger contractId, String printedFormId) throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace {
        LOGGER.debug(
                "Запрос на clientPdf.getPrintedFormByContractId. Параметры: contractId={}, printedFormId={}",
                contractId, printedFormId);
        Holder<jakarta.activation.DataHandler> content = new Holder(selfMadePdfClient.getPrintedFormByContractId(contractId, printedFormId, null));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Ответ от clientPdf.getPrintedFormByContractId получен {}", NullUtils.reflectionToString(content));
        }
        DataHandler dataHandler;
        try {
            byte[] sourceByte = IOUtils.toByteArray(content.value.getInputStream());
            content.value.getInputStream().close();
            if (sourceByte.length == 0) {
                throw new EOsagoExceptionMailNoStacktrace("Отсутствует контент для подписи", "Empty content");
            }
            List<StampParams> stampData = stampService.getStampData(contractId, printedFormId);
            dataHandler = signingService.sign(sourceByte, stampData, false, contractId);
        } catch (IOException | EOsagoSaveException e) {
            throw new EOsagoSaveException(e.getMessage(), "Create DataHandler error", e);
        }

        GetContractSignedResponseType responseType = new GetContractSignedResponseType();
        responseType.setPrintedFormId(printedFormId);
        responseType.setMime("application/pdf");
        responseType.setContent(dataHandler);
        return responseType;
    }

    public String getPrintFormId(RSaleContract contractInfo,
                                 String printedFormId) throws EOsagoException {
        LOGGER.trace("Enter to getPrintFormId");
        LOGGER.debug("Параметры: contractId={}", contractInfo.getContractId());

        if (StringUtils.isNotBlank(printedFormId) && !"-1".equals(printedFormId)) {
            // Это сделано, чтобы без всякого гемора вызывать кастомные формы джаспера через МС, а также старые формы с оракла
            return printedFormId;
        }

        String productId = contractInfo.getVariants().get(0).getProductId();

        switch (Product.fromId(productId)) {
            case OSAGO:
                printedFormId = osagoPrintFormIdRetriever.retrieve(contractInfo);
                break;
            case WHITE_CARD:
                printedFormId = CONFIRMED == contractInfo.getContractStatusTypeId() ? "1525" : "1540";
                break;
            case KASKO:
                printedFormId = this.findKaskoPrintFormId(contractInfo, printedFormId);
                break;
            case NS398:
                printedFormId = "711";
                break;
            case NS397:
                printedFormId = getNSPrintFormId(contractInfo);
                break;
            case GOOD_NEIGHBORS:
                //т.к. нет образца, возвращаем только полис
                printedFormId = getPrintFormId(contractInfo, NEIGHBOURHOOD_PROLONG_SAMPLE_PRINTFORM_ID, NEIGHBOURHOOD_PROLONG_PRINTFORM_ID,
                        NEIGHBOURHOOD_PRINTFORM_ID, NEIGHBOURHOOD_PRINTFORM_ID);
                break;
            case ALFA_REPAIR:
                printedFormId = this.findAlfaRepairPrintFormId(contractInfo);
                break;
            default:
                printedFormId = JASPER_PRINTED_FORM_ID_888;
                LOGGER.debug("Не удалось определить идентификатор ПФ (возможно это Jasper): " +
                        "contractId={}, contractInfo={}", contractInfo.getContractId(), contractInfo);
        }

        LOGGER.trace("printedFormId {}", printedFormId);
        return printedFormId;
    }

    private String findKaskoPrintFormId(RSaleContract contractInfo, String printedFormId) {
        MarketName marketName = marketNameBean.getKaskoMarketName(contractInfo);
        if (KASKO_5 == marketName) {
            printedFormId = STATEMENT == contractInfo.getContractStatusTypeId() ? "930" : "929";
        } else if (KASKO_10 == marketName) {
            printedFormId = "559";
        }
        return printedFormId;
    }

    private String findAlfaRepairPrintFormId(RSaleContract contractInfo) {
        String printedFormId;
        MarketName propertyMarketName = marketNameBean.getAlfaRepairMarketNameId(contractInfo);
        LOGGER.trace("Property market name {}", propertyMarketName);
        if (PROPERTY_REPAIR == propertyMarketName) {
            //т.к. нет образца, возвращаем только полис
            printedFormId = getPrintFormId(contractInfo, ALFAREPAIR_PROLONG_SAMPLE_PRINTFORM_ID, ALFAREPAIR_PROLONG_PRINTFORM_ID,
                    ALFAREPAIR_PRINGFORM_ID, ALFAREPAIR_PRINGFORM_ID);
        } else {
            printedFormId = isSamplePolicy(contractInfo) ? CAR_OWNER_SAMPLE_PRINTFORM_ID : CAR_OWNER_PRINTFORM_ID;
        }
        return printedFormId;
    }

    private String getPrintFormId(RSaleContract contractInfo, String prolongSampleId, String prolongId, String primarySampleId, String primaryId) {
        String printedFormId;
        boolean isSamplePolicy = isSamplePolicy(contractInfo);
        if (PROLONGATION == contractInfo.getContractOptionId()) {
            printedFormId = isSamplePolicy ? prolongSampleId : prolongId;
        } else {
            printedFormId = isSamplePolicy ? primarySampleId : primaryId;
        }
        return printedFormId;
    }

    private boolean isSamplePolicy(RSaleContract contractInfo) {
        if (PAYED_STATUS_LIST.contains(contractInfo.getContractStatusTypeId())) {
            return false;
        } else {
            return contractInfo.getContractStatusTypeId() == STATEMENT;
        }
    }

    private String getNSPrintFormId(RSaleContract contractId) {
        return switch (marketNameBean.getNSMarketNameId(contractId)) {
            case NS_CHILD_SPORT,
                 NS_FAMILY_PROTECTION,
                 NS_NO_CORONAVIRUS,
                 NS_VIRUS_PROTECTION,
                 NS_ALFA_VACCINE,
                 NS_ADDITIONAL_PROTECTION,
                 NS_A_SPORT -> JASPER_PRINTED_FORM_ID_1;
            default -> "";
        };
    }

    /**
     * Метод подсчитывает количество водителей
     *
     * @param contractId идентификатор полиса
     * @return количество водителей
     * @throws EOsagoSaveException
     */
    public int contDriver(Long contractId) {
        List<RContractVariant> contractVariants;
        List<RContractRestrictionUser> contractRestrictionUserList = null;
        contractVariants = unicusUsrClient.getContractVariantList(contractId);
        contractRestrictionUserList = unicusUsrClient.getContractRestrictionUserList(contractVariants.get(0).getContractVariantId());
        return contractRestrictionUserList.size();
    }
}
