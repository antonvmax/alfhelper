package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl;

import lombok.SneakyThrows;
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
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.logging.CBLogger;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.RequestFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;

@ExtendWith(MockitoExtension.class)
class WhiteCardMessageSenderTest {

    @Mock
    private AltcraftClient altcraftApi;
    @Mock
    private UnicusUsrService usrService;
    @Mock
    private RequestFactory whiteCardRequestFactory;
    @Mock
    private CBLogger cbLogger;
    @Mock
    private ClientInfoFactory clientInfoFactory;

    private WhiteCardMessageSender underTest;

    @BeforeEach
    void setUp() {
        underTest = new WhiteCardMessageSender(
                altcraftApi,
                usrService,
                whiteCardRequestFactory,
                cbLogger,
                clientInfoFactory
        );
    }

    @Test
    void shouldReturnWhiteCardProduct() {
        assertThat(underTest.product()).isEqualTo(Product.WHITE_CARD);
    }

    @Test
    void shouldThrowBadDataExceptionWhenEmailIsBlank() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("");

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage(NO_EMAIL_FOR_SENDING);
    }

    @Test
    void shouldThrowBadDataExceptionWhenEmailIsNull() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail(null);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage(NO_EMAIL_FOR_SENDING);
    }

    @SneakyThrows
    @Test
    void shouldCreateRequestAndSendMailWhenEmailIsNotEmpty() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");

        Request altcraftRequest = new Request();
        when(whiteCardRequestFactory.create(request)).thenReturn(altcraftRequest);

        Response successResponse = new Response();
        successResponse.setError(0);
        when(altcraftApi.sendEmail(altcraftRequest)).thenReturn(successResponse);

        underTest.send(request);

        verify(whiteCardRequestFactory).create(request);
        verify(altcraftApi).sendEmail(altcraftRequest);
        verify(cbLogger, never()).logCurrentContractSignedRequest(any(), any());
        verify(usrService, never()).getFullSaleContract(any());
    }

    @Test
    void shouldThrowSendContractServerExceptionWhenAltcraftReturnsError() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");

        Request altcraftRequest = new Request();
        when(whiteCardRequestFactory.create(request)).thenReturn(altcraftRequest);

        Response errorResponse = new Response();
        errorResponse.setError(1);
        errorResponse.setErrorText("Internal error");
        when(altcraftApi.sendEmail(altcraftRequest)).thenReturn(errorResponse);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(SendContractServerException.class)
                .hasMessage("Ошибка отправки письма - Internal error");
    }

    @SneakyThrows
    @Test
    void shouldSendTestEmailsWhenEmailContainsSemicolon() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test1@example.com; test2@example.com");

        Request altcraftRequest = new Request();
        SaleContract saleContract = new SaleContract();
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscriptions.add(subscription);
        saleContract.setSubscriptions(subscriptions);
        altcraftRequest.setData(saleContract);
        when(whiteCardRequestFactory.create(request)).thenReturn(altcraftRequest);

        underTest.send(request);

        verify(altcraftApi, times(2)).sendEmail(altcraftRequest);
        verify(cbLogger, never()).logCurrentContractSignedRequest(any(), any());
        verify(usrService, never()).getFullSaleContract(any());
    }
}