package ru.alfastrah.site.avto.ws.contact.signed.service;

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
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.AltcraftClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.logging.CBLogger;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.request.RequestFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.impl.AltcraftEmailSender;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;

@ExtendWith(MockitoExtension.class)
class AltcraftEmailSenderTest {

    private AltcraftEmailSender underTest;
    @Mock
    private AltcraftClient altcraftApi;
    @Mock
    private UnicusUsrService usrService;
    @Mock
    private RequestFactory requestFactory;
    @Mock
    private CBLogger cbLogger;
    @Mock
    private ClientInfoFactory clientInfoFactory;

    @BeforeEach
    void setUp() {
        underTest = new AltcraftEmailSender(
                altcraftApi,
                usrService,
                requestFactory,
                cbLogger,
                clientInfoFactory);
    }

    @Test
    void send_shouldThrowBadDataExceptionWhenEmailIsBlank() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("");
        request.setContractId(BigInteger.ONE);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage(NO_EMAIL_FOR_SENDING);
    }

    @Test
    void send_shouldThrowBadDataExceptionWhenEmailIsNull() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.ONE);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage(NO_EMAIL_FOR_SENDING);
    }

    @SneakyThrows
    @Test
    void send_shouldSendEmailSuccessfully() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        Response successResponse = new Response();
        successResponse.setError(0);
        given(altcraftApi.sendEmail(altcraftRequest)).willReturn(successResponse);

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setContractName("OTHER");
        given(usrService.getFullSaleContract(10L)).willReturn(rSaleContract);

        underTest.send(request);

        verify(altcraftApi).sendEmail(altcraftRequest);
        verify(cbLogger, never()).logCurrentContractSignedRequest(any(), any());
    }

    @Test
    void send_shouldThrowSendContractServerExceptionWhenAltcraftReturnsError() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        Response errorResponse = new Response();
        errorResponse.setError(1);
        errorResponse.setErrorText("Internal error");
        given(altcraftApi.sendEmail(altcraftRequest)).willReturn(errorResponse);

        assertThatThrownBy(() -> underTest.send(request))
                .isInstanceOf(SendContractServerException.class)
                .hasMessage("Ошибка отправки письма - Internal error");
    }

    @SneakyThrows
    @Test
    void send_shouldSendTestEmailsWhenEmailContainsSemicolon() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test1@example.com;test2@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        SaleContract saleContract = new SaleContract();
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscriptions.add(subscription);
        saleContract.setSubscriptions(subscriptions);
        altcraftRequest.setData(saleContract);
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setContractName("OTHER");
        given(usrService.getFullSaleContract(10L)).willReturn(rSaleContract);

        underTest.send(request);

        verify(altcraftApi, times(2)).sendEmail(altcraftRequest);
        verify(cbLogger, never()).logCurrentContractSignedRequest(any(), any());
    }

    @SneakyThrows
    @Test
    void send_shouldLogToCbWhenContractNameIsAlfaSite() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        Response successResponse = new Response();
        successResponse.setError(0);
        given(altcraftApi.sendEmail(altcraftRequest)).willReturn(successResponse);

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setContractName("ALFA_SITE");
        given(usrService.getFullSaleContract(10L)).willReturn(rSaleContract);

        underTest.send(request);

        verify(altcraftApi).sendEmail(altcraftRequest);
        verify(cbLogger).logCurrentContractSignedRequest(any(), eq("test@example.com"));
    }

    @SneakyThrows
    @Test
    void send_shouldNotLogToCbWhenContractNameIsNotAlfaSite() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        Response successResponse = new Response();
        successResponse.setError(0);
        given(altcraftApi.sendEmail(altcraftRequest)).willReturn(successResponse);

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setContractName("OTHER_SITE");
        given(usrService.getFullSaleContract(10L)).willReturn(rSaleContract);

        underTest.send(request);

        verify(altcraftApi).sendEmail(altcraftRequest);
        verify(cbLogger, never()).logCurrentContractSignedRequest(any(), any());
    }

    @Test
    void send_shouldNotPropagateExceptionWhenCbLoggerThrows() throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("test@example.com");
        request.setContractId(BigInteger.TEN);

        Request altcraftRequest = new Request();
        given(requestFactory.create(request)).willReturn(altcraftRequest);

        Response successResponse = new Response();
        successResponse.setError(0);
        given(altcraftApi.sendEmail(altcraftRequest)).willReturn(successResponse);

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setContractName("ALFA_SITE");
        given(usrService.getFullSaleContract(10L)).willReturn(rSaleContract);
        given(clientInfoFactory.bySubjectId(anyLong())).willThrow(new RuntimeException("CB error"));

        underTest.send(request);

        verify(altcraftApi).sendEmail(altcraftRequest);
    }

    @Test
    void product_shouldReturnOsago() {
        assertThat(underTest.product()).isEqualTo(Product.OSAGO);
    }
}