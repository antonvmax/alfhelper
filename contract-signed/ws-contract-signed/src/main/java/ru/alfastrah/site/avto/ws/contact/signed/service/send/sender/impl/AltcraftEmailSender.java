package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.logging.CBLogger;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.RequestFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.AltcraftEmailSendHelper;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSender;
import tops.unicus.usr.RSaleContract;

import java.util.Optional;

import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;

@Service
public class AltcraftEmailSender implements MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(AltcraftEmailSender.class);

    private final AltcraftClient altcraftApi;
    private final UnicusUsrService usrService;
    private final RequestFactory osagoRequestFactory;
    private final CBLogger cbLogger;
    private final ClientInfoFactory clientInfoFactory;


    public AltcraftEmailSender(AltcraftClient altcraftApi,
                               UnicusUsrService usrService,
                               RequestFactory osagoRequestFactory,
                               CBLogger cbLogger,
                               ClientInfoFactory clientInfoFactory) {
        this.altcraftApi = altcraftApi;
        this.usrService = usrService;
        this.osagoRequestFactory = osagoRequestFactory;
        this.cbLogger = cbLogger;
        this.clientInfoFactory = clientInfoFactory;
    }

    public void send(SendContractSignedRequest contractSignedRequest) throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace {
        LOGGER.info("Отправка письма через altcraft");
        if (StringUtils.isBlank(contractSignedRequest.getEmail())) {
            throw new BadDataException(NO_EMAIL_FOR_SENDING);
        }

        final Request request = osagoRequestFactory.create(contractSignedRequest);
        AltcraftEmailSendHelper.send(altcraftApi, contractSignedRequest.getEmail(), request);
        RSaleContract rSaleContract = usrService.getFullSaleContract(contractSignedRequest.getContractId().longValue());
        if (sendCbLogs(rSaleContract)) {
            try {
                cbLogger.logCurrentContractSignedRequest(
                        clientInfoFactory.bySubjectId(Long.parseLong(String.valueOf(rSaleContract.getSubjectId()))),
                        contractSignedRequest.getEmail());
            } catch (Exception e) {
                LOGGER.error("Ошибка логирования в цб", e);
            }
        }

        LOGGER.info("Успешная отправка через Altcraft");
    }

    private boolean sendCbLogs(RSaleContract rSaleContract) {
        return "ALFA_SITE".equalsIgnoreCase(Optional.ofNullable(rSaleContract.getContractName()).orElse(""));
    }

    @Override
    public Product product() {
        return Product.OSAGO;
    }
}
