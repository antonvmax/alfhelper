package ru.alfastrah.site.avto.ws.contact.signed.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.model.contract.signed.exception.*;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.partners.interaction.PartnersInteractionClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetContractSignedService;
import ru.alfastrah.site.avto.ws.contact.signed.utils.PrintFormType;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetContractSignedServiceTest {

    @Mock
    private BuildEmail buildEmail;
    @Mock
    private PartnersDb partnersDbBean;
    @Mock
    private ReplaceService fakeContractIdService;
    @Mock
    private UnicusUsrService unicusUsrService;
    @Mock
    private PartnersInteractionClient partnersInteractionClient;

    private GetContractSignedService underTest;

    @BeforeEach
    void setUp() {
        underTest = new GetContractSignedService(partnersDbBean, fakeContractIdService, buildEmail, unicusUsrService, partnersInteractionClient);
    }

    @Test
    void shouldCallReplaceServiceWhenGetContractSignedForFakeId() {
        GetContractSignedRequestType requestType = new GetContractSignedRequestType();
        requestType.setContractId(BigInteger.valueOf(-111111L));

        underTest.processGetContractSigned(requestType, PrintFormType.DEFAULT);

        verify(fakeContractIdService, atMostOnce()).getPrintFormForFakeId(any());
    }

    @Test
    void shouldRethrowEOsagoProcessExceptionWhenProblemOccurredForGetContractSigned()
            throws EOsagoException {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("42");
        RSaleContract contractInfo = createContract(Product.OSAGO.getProductId());
        given(unicusUsrService.getContract(any())).willReturn(null);
        when(partnersInteractionClient.searchByUpidAndContractId("42", 123L)).thenReturn(123L);
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(buildEmail.getPrintFormId(any(), any()))
                .willThrow(EOsagoException.class)
                .willReturn("");

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.DEFAULT)).isInstanceOf(EOsagoProccessException.class);
    }

    @Test
    void shouldCallCreateSignedContentWhenOtherEngineForGetContractSigned() throws EOsagoSaveException, EOsagoExceptionMailNoStacktrace {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        GetContractSignedResponseType expectedResult = new GetContractSignedResponseType();
        expectedResult.setPrintedFormId("id");
        expectedResult.setMime("mime");
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("42");
        RSaleContract contractInfo = createContract(Product.ALFA_REPAIR.getProductId());
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(buildEmail.createSignedContent(any(), any())).willReturn(expectedResult);

        GetContractSignedResponseType actualResult = underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
        verify(buildEmail, times(1)).createSignedContent(any(), any());
    }

    @Test
    void shouldContractRootEqualsContractIdWhenGetFullSaleContractResponseIsNull() throws EOsagoExceptionMailNoStacktrace {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        GetContractSignedResponseType expectedResult = new GetContractSignedResponseType();
        expectedResult.setPrintedFormId("id");
        expectedResult.setMime("mime");
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("42");
        RSaleContract contractInfo = createContract(Product.ALFA_REPAIR.getProductId());
        given(unicusUsrService.getContract(any())).willReturn(null);
        given(partnersInteractionClient.searchByUpidAndContractId("42", 123L)).willReturn(123L);
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(buildEmail.createSignedContent(any(), any())).willReturn(expectedResult);

        underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        verify(partnersDbBean, times(1)).getContractInfo(eq(BigInteger.valueOf(123L)));
    }

    @Test
    void shouldContractRootEqualsRootContractIdFromGetFullSaleContractResponseWhenContractOptionIdIs3() throws EOsagoExceptionMailNoStacktrace {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        GetContractSignedResponseType expectedResult = new GetContractSignedResponseType();
        expectedResult.setPrintedFormId("id");
        expectedResult.setMime("mime");
        RSaleContract contract = new RSaleContract();
        contract.setRootContractId(1234L);
        contract.setContractOptionId(3);
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("42");
        RSaleContract contractInfo = createContract(Product.ALFA_REPAIR.getProductId());
        given(unicusUsrService.getContract(any(Long.class))).willReturn(contract);
        given(partnersInteractionClient.searchByUpidAndContractId("42", 1234L)).willReturn(1234L);
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(buildEmail.createSignedContent(any(), any())).willReturn(expectedResult);

        underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        verify(partnersDbBean, times(1)).getContractInfo(eq(BigInteger.valueOf(1234L)));
    }

    @Test
    void shouldContractRootEqualsRootContractIdFromGetFullSaleContractResponseWhenContractOptionIdIsNot3() throws EOsagoExceptionMailNoStacktrace {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        GetContractSignedResponseType expectedResult = new GetContractSignedResponseType();
        expectedResult.setPrintedFormId("id");
        expectedResult.setMime("mime");
        RSaleContract contract = new RSaleContract();
        contract.setRootContractId(1234L);
        contract.setContractOptionId(2);
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("42");
        RSaleContract contractInfo = createContract(Product.ALFA_REPAIR.getProductId());
        given(unicusUsrService.getContract(any(Long.class))).willReturn(contract);
        when(partnersInteractionClient.searchByUpidAndContractId("42", 123L)).thenReturn(123L);
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(buildEmail.createSignedContent(any(), any())).willReturn(expectedResult);

        underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        verify(partnersDbBean, times(1)).getContractInfo(eq(BigInteger.valueOf(123L)));
    }

    @Test
    void shouldThrowEOsagoReplaceExceptionWhenContractIdIsNegativeAndPrintFormTypeIsStatementOsago() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(-123L));

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.STATEMENT_OSAGO))
                .isInstanceOf(EOsagoReplaceException.class)
                .hasMessage("ПФ доступна только после оплаты договора");
    }

    @Test
    void shouldThrowEOsagoReplaceExceptionWhenContractIdIsNegativeAndPrintFormTypeIsNotificationOsago() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(-123L));

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.NOTIFICATION_OSAGO))
                .isInstanceOf(EOsagoReplaceException.class)
                .hasMessage("ПФ доступна только после оплаты договора");
    }

    @Test
    void shouldCallGetPrintFormForFakeIdWhenContractIdRemainsNegativeAfterReplace() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(-123L));
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();
        when(fakeContractIdService.getPrintFormForFakeId(request)).thenReturn(expectedResponse);

        GetContractSignedResponseType result = underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        assertThat(result).isEqualTo(expectedResponse);
        verify(fakeContractIdService).replaceFakeContractId(request);
        verify(fakeContractIdService).getPrintFormForFakeId(request);
    }

    @SneakyThrows
    @Test
    void shouldProcessNormallyWhenContractIdIsZero() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.ZERO);
        request.setUpid("42");
        RSaleContract contract = createContract(Product.OSAGO.getProductId());
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();

        when(unicusUsrService.getContract(0L)).thenReturn(null);
        when(partnersInteractionClient.searchByUpidAndContractId("42", 0L)).thenReturn(0L);
        when(partnersDbBean.getContractInfo(BigInteger.ZERO)).thenReturn(contract);
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("formId");
        when(buildEmail.createSignedContent(any(), any())).thenReturn(expectedResponse);

        GetContractSignedResponseType result = underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        assertThat(result).isEqualTo(expectedResponse);
        verify(fakeContractIdService, never()).replaceFakeContractId(any());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenContractIdIsNull() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(null);

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.DEFAULT))
                .isInstanceOf(NullPointerException.class);
    }

    @SneakyThrows
    @Test
    void shouldCallGetAdditionalKaskoToOsagoWhenProductIsKasko() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("upid123");
        RSaleContract contract = createContract(Product.KASKO.getProductId());
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();

        when(unicusUsrService.getContract(123L)).thenReturn(null);
        when(partnersDbBean.getContractInfo(BigInteger.valueOf(123L))).thenReturn(contract);
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("formId");
        when(buildEmail.createSignedContent(any(), any())).thenReturn(expectedResponse);
        doNothing().when(buildEmail).getAdditionalKaskoToOsago(anyString(), any(BigInteger.class), eq(false));

        underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        verify(buildEmail).getAdditionalKaskoToOsago("upid123", BigInteger.valueOf(123L), false);
    }

    @SneakyThrows
    @Test
    void shouldUseRootContractIdAndSetIsDsTrueWhenContractOptionIdEquals3() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("upid123");

        RSaleContract unicusContract = new RSaleContract();
        unicusContract.setContractOptionId(3);
        unicusContract.setRootContractId(456L);

        RSaleContract partnerContract = createContract(Product.OSAGO.getProductId());
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();

        when(unicusUsrService.getContract(123L)).thenReturn(unicusContract);
        when(partnersDbBean.getContractInfo(BigInteger.valueOf(456L))).thenReturn(partnerContract);
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("formId");
        when(buildEmail.createSignedContent(any(), any())).thenReturn(expectedResponse);

        underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        verify(partnersDbBean).getContractInfo(BigInteger.valueOf(456L));
    }

    @Test
    void shouldHandleNullContractFromPartnersDb() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("upid123");

        RSaleContract emptyContract = new RSaleContract();
        emptyContract.setContractId(123L);
        emptyContract.withVariants(new ArrayList<>());
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();

        when(unicusUsrService.getContract(123L)).thenReturn(null);
        when(partnersDbBean.getContractInfo(BigInteger.valueOf(123L))).thenReturn(null);

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.DEFAULT))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void shouldHandleContractWithEmptyVariants() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("upid123");

        RSaleContract contract = new RSaleContract();
        contract.withVariants(new ArrayList<>());

        when(unicusUsrService.getContract(123L)).thenReturn(null);
        when(partnersDbBean.getContractInfo(BigInteger.valueOf(123L))).thenReturn(contract);

        assertThatThrownBy(() -> underTest.processGetContractSigned(request, PrintFormType.DEFAULT))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @SneakyThrows
    @Test
    void shouldProcessPositiveContractIdWithoutReplace() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.valueOf(123L));
        request.setUpid("upid123");
        RSaleContract contract = createContract(Product.OSAGO.getProductId());
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();

        when(unicusUsrService.getContract(123L)).thenReturn(null);
        when(partnersDbBean.getContractInfo(BigInteger.valueOf(123L))).thenReturn(contract);
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("formId");
        when(buildEmail.createSignedContent(any(), any())).thenReturn(expectedResponse);

        GetContractSignedResponseType result = underTest.processGetContractSigned(request, PrintFormType.DEFAULT);

        assertThat(result).isEqualTo(expectedResponse);
        verify(fakeContractIdService, never()).replaceFakeContractId(any());
        verify(fakeContractIdService, never()).getPrintFormForFakeId(any());
    }

    private RSaleContract createContract(String productId) {
        RSaleContract contract = new RSaleContract();
        RContractVariant contractVariant = new RContractVariant();
        contractVariant.setProductId(productId);
        contract.withVariants(contractVariant);
        return contract;
    }
}