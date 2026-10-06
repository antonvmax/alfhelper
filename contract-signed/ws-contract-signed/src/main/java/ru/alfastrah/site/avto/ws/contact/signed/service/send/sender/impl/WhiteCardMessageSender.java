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

import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;

@Service
public class WhiteCardMessageSender implements MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhiteCardMessageSender.class);

    private final AltcraftClient altcraftApi;
    private final UnicusUsrService usrService;
    private final RequestFactory whiteCardRequestFactory;
    private final CBLogger cbLogger;
    private final ClientInfoFactory clientInfoFactory;


    public WhiteCardMessageSender(AltcraftClient altcraftApi,
                                  UnicusUsrService usrService,
                                  RequestFactory whiteCardRequestFactory,
                                  CBLogger cbLogger,
                                  ClientInfoFactory clientInfoFactory) {
        this.altcraftApi = altcraftApi;
        this.usrService = usrService;
        this.whiteCardRequestFactory = whiteCardRequestFactory;
        this.cbLogger = cbLogger;
        this.clientInfoFactory = clientInfoFactory;
    }

    public void send(SendContractSignedRequest contractSignedRequest) throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace {
        LOGGER.info("Отправка письма через altcraft");
        if (StringUtils.isBlank(contractSignedRequest.getEmail())) {
            throw new BadDataException(NO_EMAIL_FOR_SENDING);
        }

        final Request request = whiteCardRequestFactory.create(contractSignedRequest);
        AltcraftEmailSendHelper.send(altcraftApi, contractSignedRequest.getEmail(), request);
        LOGGER.info("Успешная отправка через Altcraft");
    }

    @Override
    public Product product() {
        return Product.WHITE_CARD;
    }
}
