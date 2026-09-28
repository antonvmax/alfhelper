package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.model.exception.AltcraftSenderNotImplemented;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSender;

import java.text.MessageFormat;
import java.util.List;

@Service
public class MessageSenderFactory {

    private final List<MessageSender> messageSenderList;

    public MessageSenderFactory(List<MessageSender> messageSenderList) {
        this.messageSenderList = messageSenderList;
    }

    public MessageSender byProduct(String productId) {
        return messageSenderList.stream()
                .filter(sender -> sender.product().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new AltcraftSenderNotImplemented(MessageFormat.format("Для продукта {0} не реализована отправка через алькрафт", productId)));
    }
}
