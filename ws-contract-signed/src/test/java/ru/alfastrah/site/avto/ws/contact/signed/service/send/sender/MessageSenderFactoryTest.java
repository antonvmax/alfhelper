package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.model.exception.AltcraftSenderNotImplemented;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl.KaskoMessageSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSenderFactoryTest {

    private MessageSenderFactory underTest;
    @Mock
    private KaskoMessageSender kaskoMessageSender;

    @BeforeEach
    void setUp() {
        underTest = new MessageSenderFactory(List.of(kaskoMessageSender));
    }

    @Test
    void shouldReturnMessageSenderWhenProductIdGiven() {
        when(kaskoMessageSender.product()).thenReturn(Product.KASKO);

        MessageSender messageSender = underTest.byProduct(Product.KASKO.getProductId());

        assertThat(messageSender).isInstanceOf(KaskoMessageSender.class);
    }

    @Test
    void shouldThrowExceptionWhenMessageSenderNotFound() {
        when(kaskoMessageSender.product()).thenReturn(Product.KASKO);

        assertThatThrownBy(() -> underTest.byProduct("unknownProduct"))
                .isInstanceOf(AltcraftSenderNotImplemented.class)
                .hasMessageContaining("Для продукта unknownProduct не реализована отправка через алькрафт");
    }
}