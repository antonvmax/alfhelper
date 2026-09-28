package ru.alfastrah.site.avto.ws.contact.signed.service.send.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.DirectSendRequestFactory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DirectSendProcessService {

    private final DirectSendRequestFactory requestFactory;
    private final AltcraftClient altcraftClient;

    public DirectSendProcessService(DirectSendRequestFactory requestFactory,
                                    AltcraftClient altcraftClient) {
        this.requestFactory = requestFactory;
        this.altcraftClient = altcraftClient;
    }

    public DirectSendContractSignedResponse send(DirectSendContractSignedRequest request) {

        List<String> errorEmails = new ArrayList<>();
        Request altcraftRequest = requestFactory.create(request);

        for (String email : request.getEmail()) {
            requestFactory.fillDataSubscription(altcraftRequest, email);
            try {
                Response response = altcraftClient.sendEmail(altcraftRequest);
                if (response.getError() != 0) {
                    log.error("Не получилось отправить письмо на почту: {}. Ошибка: {}", email, response.getErrorText());
                    errorEmails.add(email);
                }
            } catch (Exception e) {
                log.error("Не получилось отправить письмо на почту: {}. Ошибка: {}", email, e.getMessage());
                errorEmails.add(email);
            }
        }

        if (errorEmails.isEmpty()) {
            return new DirectSendContractSignedResponse(true, request.getEmail());
        }

        return new DirectSendContractSignedResponse(false, errorEmails);
    }
}