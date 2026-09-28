package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.interplat4.altcraft.model.Subscription;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.client.impl.AltcraftEmailClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.KaskoRequestFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class KaskoMessageSenderTest {
    @Mock
    private AltcraftEmailClient altcraftEmailClient;
    @Mock
    private KaskoRequestFactory requestFactory;

    private KaskoMessageSender underTest;

    @BeforeEach
    void setUp() {
        underTest = new KaskoMessageSender(altcraftEmailClient, requestFactory);
    }

    @Test
    void shouldReturnKaskoProduct() {
        assertThat(underTest.product()).isEqualTo(Product.KASKO);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        SendContractSignedRequest request = new SendContractSignedRequest();

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Не указан e-mail для отправки!");
    }

    @Test
    void shouldCreateRequestAndSendMailWhenEmailIsNotEmpty() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("email");
        Response response = new Response();
        response.setError(0);
        when(altcraftEmailClient.sendEmail(any())).thenReturn(response);
        when(requestFactory.create(any())).thenReturn(new Request());


        underTest.send(request);

        verify(requestFactory, times(1)).create(any());
        verify(altcraftEmailClient, times(1)).sendEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenAltcraftResponseHasError() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("email");
        Response response = new Response();
        response.setError(2);
        when(altcraftEmailClient.sendEmail(any())).thenReturn(response);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(SendContractServerException.class)
                .hasMessageContaining("Ошибка отправки письма");
    }

    @Test
    void shouldSendToEachEmailWhenEmailContainsSemicolon() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test1@example.com;test2@example.com");

        Request altcraftRequest = new Request();
        SaleContract saleContract = new SaleContract();
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(new Subscription());
        saleContract.setSubscriptions(subscriptions);
        altcraftRequest.setData(saleContract);
        when(requestFactory.create(any())).thenReturn(altcraftRequest);

        underTest.send(request);

        verify(altcraftEmailClient, times(2)).sendEmail(altcraftRequest);
    }
}