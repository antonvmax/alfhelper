package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.interplat4.altcraft.model.Subscription;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;

import java.util.List;

/**
 * Отправка письма в Altcraft. Если в строке несколько адресов через ';', письмо
 * отправляется отдельным запросом на каждый адрес, т.к. Altcraft воспринимает
 * всю строку как один e-mail и рассылку не делает.
 */
public final class AltcraftEmailSendHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AltcraftEmailSendHelper.class);
    private static final String EMAIL_DELIMITER = ";";

    private AltcraftEmailSendHelper() {
    }

    public static void send(AltcraftClient altcraftClient, String emails, Request request) {
        if (emails != null && emails.contains(EMAIL_DELIMITER)) {
            sendToEachEmail(altcraftClient, emails, request);
        } else {
            Response response = altcraftClient.sendEmail(request);
            if (response.getError() != 0) {
                throw new SendContractServerException("Ошибка отправки письма - " + response.getErrorText());
            }
        }
    }

    private static void sendToEachEmail(AltcraftClient altcraftClient, String emails, Request request) {
        SaleContract saleContract = (SaleContract) request.getData();
        List<Subscription> subscriptions = saleContract.getSubscriptions();
        for (String rawEmail : emails.split(EMAIL_DELIMITER)) {
            String email = rawEmail.trim();
            if (email.isEmpty()) {
                continue;
            }
            subscriptions.get(0).setEmail(email);
            LOGGER.trace("Try send email TO:{}", email);
            Response response = altcraftClient.sendEmail(request);
            if (response != null && response.getError() != 0) {
                LOGGER.error("Ошибка отправки письма на {} - {}", email, response.getErrorText());
            }
        }
    }
}
