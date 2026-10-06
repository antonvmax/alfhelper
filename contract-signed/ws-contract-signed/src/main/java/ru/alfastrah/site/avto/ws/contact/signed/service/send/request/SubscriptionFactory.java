package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.Subscription;

import java.util.ArrayList;
import java.util.List;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_CHANNEL;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_RESOURCE_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_STATUS;

@Service
public class SubscriptionFactory {
    public List<Subscription> create(String email) {
        List<Subscription> subscriptions = new ArrayList<>();
            Subscription subscription = new Subscription();
            subscription.setChannel(ALTCRAFT_CHANNEL);
            subscription.setEmail(email);
            subscription.setResourceId(ALTCRAFT_RESOURCE_ID);
            subscription.setStatus(ALTCRAFT_STATUS);
            subscriptions.add(subscription);

        return subscriptions;
    }
}
