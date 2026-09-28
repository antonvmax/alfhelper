package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.RequestFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.AltcraftEmailSendHelper;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSender;

import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;

@Service
public class KaskoMessageSender implements MessageSender {

    private final AltcraftClient altcraftEmailClient;
    private final RequestFactory kaskoRequestFactory;

    public KaskoMessageSender(AltcraftClient altcraftEmailClient, RequestFactory kaskoRequestFactory) {
        this.altcraftEmailClient = altcraftEmailClient;
        this.kaskoRequestFactory = kaskoRequestFactory;
    }

    @Override
    public void send(SendContractSignedRequest sendRequest) {
        if (StringUtils.isBlank(sendRequest.getEmail())) {
            throw new BadDataException(NO_EMAIL_FOR_SENDING);
        }
        Request altcraftRequest = kaskoRequestFactory.create(sendRequest);
        AltcraftEmailSendHelper.send(altcraftEmailClient, sendRequest.getEmail(), altcraftRequest);
    }

    @Override
    public Product product() {
        return Product.KASKO;
    }
}
