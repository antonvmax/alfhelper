package ru.alfastrah.site.avto.ws.contact.signed.service.get;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.*;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.ReplaceService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.partners.interaction.PartnersInteractionClient;
import ru.alfastrah.site.avto.ws.contact.signed.utils.PrintFormType;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class GetContractSignedService {

    private final static Integer OPTION_ID = 3;

    private final PartnersDb partnersDbBean;
    private final ReplaceService replaceService;
    private final BuildEmail buildEmail;
    private final UnicusUsrService unicusUsrService;
    private final PartnersInteractionClient partnersInteractionClient;

    public GetContractSignedService(PartnersDb partnersDbBean, ReplaceService replaceService, BuildEmail buildEmail, UnicusUsrService unicusUsrService, PartnersInteractionClient partnersInteractionClient) {
        this.partnersDbBean = partnersDbBean;
        this.replaceService = replaceService;
        this.buildEmail = buildEmail;
        this.unicusUsrService = unicusUsrService;
        this.partnersInteractionClient = partnersInteractionClient;
    }

    public GetContractSignedResponseType processGetContractSigned(
            GetContractSignedRequestType getContractSignedRequestType,
            PrintFormType printFormType) {

        if (BigInteger.ZERO
                .compareTo(Optional.ofNullable(getContractSignedRequestType.getContractId())
                        .orElse(BigInteger.ZERO)) > 0) {
            replaceService.replaceFakeContractId(getContractSignedRequestType);
            if (getContractSignedRequestType.getContractId().compareTo(BigInteger.ZERO) < 0) {
                if (printFormType == PrintFormType.STATEMENT_OSAGO || printFormType == PrintFormType.NOTIFICATION_OSAGO) {
                    throw new EOsagoReplaceException("ПФ доступна только после оплаты договора");
                }
                log.info("Запрашиваем образец");
                return replaceService.getPrintFormForFakeId(getContractSignedRequestType);
            }
            log.info("Возвращаемся к стандартному пути");
        }

        RSaleContract contract = unicusUsrService.getContract(
                Objects.requireNonNull(getContractSignedRequestType.getContractId()).longValue());
        BigInteger rootContractId = getContractSignedRequestType.getContractId();
        boolean isDs = false;

        if (contract != null) {
            if (OPTION_ID.equals(contract.getContractOptionId())) {
                rootContractId = BigInteger.valueOf(contract.getRootContractId());
                isDs = true;
            }
        }

        if (getContractSignedRequestType.getUpid() == null || rootContractId == null) {
            log.info("Невозможно проверить upid + contractId");
            throw new InvalidRequestException("Невозможно проверить upid + contractId");
        }
        Long contractByUpidAndContractId = partnersInteractionClient.searchByUpidAndContractId(
                getContractSignedRequestType.getUpid(),
                rootContractId.longValue()
        );
        if (contractByUpidAndContractId == null) {
            log.info("Не найден по upid + contractId");
            throw new InvalidRequestException("Не найден по upid + contractId");
        }

        RSaleContract rContract = Optional.ofNullable(partnersDbBean.getContractInfo(rootContractId))
                .orElse(new RSaleContract());
        rContract.setContractId(rootContractId.longValue());
        //Проверка сопоставления контракта по номеру UPID + выставление подписи
        try {
            String productId = Optional.ofNullable(rContract.getVariants().get(0).getProductId()).orElse(StringUtils.EMPTY);
            if (Product.fromId(productId) == Product.KASKO) {
                    buildEmail.getAdditionalKaskoToOsago(getContractSignedRequestType.getUpid(), rootContractId, isDs);
            }
            String formId = buildEmail.getPrintFormId(rContract, printFormType.getUnicusPrintFormId());
            return buildEmail.createSignedContent(getContractSignedRequestType.getContractId(), formId);
        } catch (EOsagoExceptionMailNoStacktrace eOsagoExceptionMailNoStacktrace) {
            throw new EOsagoProccessException(eOsagoExceptionMailNoStacktrace.getMessage(), eOsagoExceptionMailNoStacktrace.getFaultCode());
        } catch (EOsagoException eOsagoException) {
            throw new EOsagoProccessException(eOsagoException.getMessage(), eOsagoException.getFaultCode());
        } catch (EOsagoSaveException eOsagoSaveException) {
            throw new EOsagoProccessException(eOsagoSaveException.getMessage(), eOsagoSaveException.getFaultCode());
        }
    }
}
