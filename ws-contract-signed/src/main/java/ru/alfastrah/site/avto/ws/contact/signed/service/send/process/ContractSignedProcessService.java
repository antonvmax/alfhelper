package ru.alfastrah.site.avto.ws.contact.signed.service.send.process;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.model.exception.AltcraftSenderNotImplemented;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.EmailSendLogService;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSenderFactory;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.util.Optional;

@Slf4j
@Service
public class ContractSignedProcessService {

    @Value("${eosago.ok.double.mail.list}")
    private String sendDoubleEmailTo;
    @Value("${mail.sender}@alfastrah.ru")
    private String from;

    private final JavaMailSender emailSender;
    private final BuildEmail buildEmail;
    private final PartnersDb partnersDbBean;
    private final MessageSenderFactory senderFactory;
    private final EmailSendLogService emailSendLogService;

    public ContractSignedProcessService(@Value("${eosago.ok.double.mail.list}") String sendDoubleEmailTo,
                                        @Value("${mail.sender}@alfastrah.ru") String from,
                                        JavaMailSender emailSender,
                                        BuildEmail buildEmail,
                                        PartnersDb partnersDbBean,
                                        MessageSenderFactory factory,
                                        EmailSendLogService emailSendLogService) {
        this.sendDoubleEmailTo = sendDoubleEmailTo;
        this.from = from;
        this.emailSender = emailSender;
        this.buildEmail = buildEmail;
        this.partnersDbBean = partnersDbBean;
        this.senderFactory = factory;
        this.emailSendLogService = emailSendLogService;
    }

    public boolean processSendContractSigned(SendContractSignedRequest contractSignedRequest) {
        String productId = Optional.ofNullable(partnersDbBean.getContractInfo(contractSignedRequest.getContractId()))
                .map(RSaleContract::getVariants)
                .flatMap(variants -> variants.stream().findFirst().map(RContractVariant::getProductId))
                .orElseThrow(() -> new BadDataException("Не найден продукт в договоре с номером - " + contractSignedRequest.getContractId()));

        log.info("Processing SendContractSignedRequest = {}, productID = {}",
                contractSignedRequest.getContractId(), productId);

        try {
            this.senderFactory.byProduct(productId).send(contractSignedRequest);
        } catch (EOsagoSaveException | EOsagoExceptionMailNoStacktrace e) {
            log.error("Ошибка отправки письма через AltCraft contractSignedRequest - {}",
                    contractSignedRequest.getContractId(), e);
            throw new BadDataException(e.getMessage());
        } catch (AltcraftSenderNotImplemented e) {
            log.info(e.getMessage());
            configureEmail(contractSignedRequest, false);
        }
        configureAdditionalEmail(contractSignedRequest);
        configureNotificationEmail(contractSignedRequest);
        return true;
    }

    public void guaranteedProcessSend(SendContractSignedRequest contractSignedRequest) {
        String productId = null;
        try {
            productId = Optional.ofNullable(partnersDbBean.getContractInfo(contractSignedRequest.getContractId()))
                    .map(RSaleContract::getVariants)
                    .flatMap(variants -> variants.stream().findFirst().map(RContractVariant::getProductId))
                    .orElseThrow(() -> new BadDataException("Не найден продукт в договоре с номером - " + contractSignedRequest.getContractId()));

        } catch (BadDataException e) {
            emailSendLogService.handleException(contractSignedRequest, false, e.getMessage());
            throw new BadDataException(e.getMessage());
        } catch (PersistenceException | DataAccessException e) {
            emailSendLogService.handleException(contractSignedRequest, true, e.getMessage());
            throw new PersistenceException(e.getMessage());
        } catch (Exception e) {
            emailSendLogService.handleException(contractSignedRequest, false, e.getMessage());
            throw new PersistenceException(e.getMessage());
        }

        try {
            this.senderFactory.byProduct(productId).send(contractSignedRequest);
            emailSendLogService.handleSuccess(contractSignedRequest);
        } catch (EOsagoSaveException | EOsagoExceptionMailNoStacktrace e) {
            log.error("Ошибка отправки письма через AltCraft contractSignedRequest - {}",
                    contractSignedRequest.getContractId(), e);
            emailSendLogService.handleException(contractSignedRequest, false, e.getMessage());
            throw new BadDataException(e.getMessage());
        } catch (AltcraftSenderNotImplemented e) {
            log.info(e.getMessage());
            emailSendLogService.handleException(contractSignedRequest, false, e.getMessage());
            configureEmail(contractSignedRequest, true);
        } catch (Exception e) {
            emailSendLogService.handleException(contractSignedRequest, null, e.getMessage());
            throw new BadDataException(e.getMessage());
        }

        configureAdditionalEmail(contractSignedRequest);
        configureNotificationEmail(contractSignedRequest);
    }

    private MimeMessage configureEmail(SendContractSignedRequest contractSignedRequest, boolean isRetryAble) {
        MimeMessage mimeMailMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMailMessage, true);
            helper.setFrom(from);
            helper.setTo(contractSignedRequest.getEmail().split(";"));
            log.debug("Отпраляем письмо на \"{}\"", contractSignedRequest.getEmail());
            buildEmail.createEmailBody(helper, contractSignedRequest);
            buildEmail.addAttachments(helper, contractSignedRequest);
            emailSender.send(mimeMailMessage);
            if (StringUtils.isNotEmpty(this.sendDoubleEmailTo)) {
                helper.setTo(sendDoubleEmailTo);
                emailSender.send(mimeMailMessage);
            }
        } catch (Exception e) {
            log.error("Основной Email не отправлен: {}", e.getMessage());
            if (isRetryAble) {
                emailSendLogService.handleException(contractSignedRequest, null, e.getMessage());
            }
            throw new BadDataException(e.getMessage());
        }
        return mimeMailMessage;
    }

    private MimeMessage configureAdditionalEmail(SendContractSignedRequest contractSignedRequest) {
        MimeMessage mimeMailMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMailMessage, true);
            helper.setFrom(from);
            helper.setTo(contractSignedRequest.getEmail());
            buildEmail.createAdditionalEmailBody(helper, contractSignedRequest);
            if (helper.getRootMimeMultipart().getCount() > 0) {
                emailSender.send(mimeMailMessage);
            }
        } catch (MessagingException e) {
            log.error("Доп Email не отправлен: {0}", e);
        }
        return mimeMailMessage;
    }

    private MimeMessage configureNotificationEmail(SendContractSignedRequest contractSignedRequest) {
        MimeMessage mimeMailMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMailMessage, true);
            helper.setFrom(from);
            buildEmail.createNotificationEmail(helper, contractSignedRequest);
            if (helper.getRootMimeMultipart().getCount() > 0) {
                emailSender.send(mimeMailMessage);
            }
        } catch (MessagingException | EOsagoExceptionMailNoStacktrace e) {
            log.error("Уведомительный Email не отправлен: {0}", e);
        }
        return mimeMailMessage;
    }
}
