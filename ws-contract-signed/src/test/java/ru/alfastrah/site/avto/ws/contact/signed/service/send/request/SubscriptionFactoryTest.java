package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.alfastrah.interplat4.altcraft.model.Subscription;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_CHANNEL;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_RESOURCE_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_STATUS;

class SubscriptionFactoryTest {


    @Test
    void shouldCreateSubscriptionWhenEmailGiven() {
        SubscriptionFactory underTest = new SubscriptionFactory();
        String email = "email";

        List<Subscription> subscriptions = underTest.create(email);

        assertThat(subscriptions.size()).isOne();
        assertThat(subscriptions.get(0).getChannel()).isEqualTo(ALTCRAFT_CHANNEL);
        assertThat(subscriptions.get(0).getResourceId()).isEqualTo(ALTCRAFT_RESOURCE_ID);
        assertThat(subscriptions.get(0).getStatus()).isEqualTo(ALTCRAFT_STATUS);
    }
}