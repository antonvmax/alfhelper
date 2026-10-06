package ru.alfastrah.site.avto.ws.contact.signed.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MimeTypeUtils;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.client.OsagoReplaceClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.RealContractResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

@ExtendWith(MockitoExtension.class)
class ReplaceServiceTest {

    @Mock
    private OsagoReplaceClient osagoReplaceApi;
    @Mock
    private PdfClient printFormService;

    private ReplaceService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ReplaceService(osagoReplaceApi, printFormService);
    }

    @Test
    void shouldNotSignReceivedPrintFormDataForFakeContractId() {
        BigInteger actualContractId = BigInteger.valueOf(-5);
        BigInteger expectedContractId = BigInteger.valueOf(-5);
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(actualContractId);
        given(printFormService.getPrintedFormByContractId(any(), any(), any()))
                .willReturn(
                        new DataHandler(
                                new ByteArrayDataSource(new byte[]{1}, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));

        underTest.getPrintFormForFakeId(request);

        verify(printFormService, times(1)).getPrintedFormByContractId(any(), any(), any());
        ArgumentCaptor<BigInteger> realContractIdArgumentCaptor = ArgumentCaptor.forClass(BigInteger.class);
        verify(printFormService).getPrintedFormByContractId(realContractIdArgumentCaptor.capture(), any(), any());
        assertThat(realContractIdArgumentCaptor.getValue()).isEqualTo(expectedContractId.longValue());
    }

    @Test
    void shouldLeavePositiveContractId() {
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(BigInteger.TEN);

        underTest.replaceFakeContractId(request);

        assertThat(request.getContractId()).isEqualByComparingTo(BigInteger.TEN);
        verify(osagoReplaceApi, noInteractions()).getPartnerInfo(any());
    }

    @Test
    void shouldReplaceNegativeContractId() {
        BigInteger actualContractId = BigInteger.valueOf(-7);
        BigInteger expectedContractId = BigInteger.valueOf(12345L);
        GetContractSignedRequestType request = new GetContractSignedRequestType();
        request.setContractId(actualContractId);
        given(osagoReplaceApi.getPartnerInfo(actualContractId.longValue()))
                .willReturn(new RealContractResponse().realContractId(expectedContractId.longValue()));

        underTest.replaceFakeContractId(request);

        verify(osagoReplaceApi, times(1)).getPartnerInfo(any());
        ArgumentCaptor<Long> osagoReplaceArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(osagoReplaceApi).getPartnerInfo(osagoReplaceArgumentCaptor.capture());
        assertThat(osagoReplaceArgumentCaptor.getValue()).isEqualTo(actualContractId.longValue());
        assertThat(request.getContractId()).isEqualTo(expectedContractId);
    }

    @Test
    void shouldReturnEmptyResponseWhenIOError() throws IOException {
        GetContractSignedRequestType getContractSignedRequestType = new GetContractSignedRequestType();
        DataHandler dh = Mockito.mock(DataHandler.class);
        DataSource ds = Mockito.mock(DataSource.class);
        InputStream is = Mockito.mock(InputStream.class);
        given(dh.getDataSource()).willReturn(ds);
        given(ds.getInputStream()).willReturn(is);
        given(is.readAllBytes()).willThrow(IOException.class);
        given(printFormService.getPrintedFormByContractId(any(), any(), any())).willReturn(dh);
        GetContractSignedResponseType expectedResponse = new GetContractSignedResponseType();
        expectedResponse.setMime("application/pdf");
        expectedResponse.setPrintedFormId("541");
        expectedResponse.setContent(null);
        
        GetContractSignedResponseType actualResult = underTest.getPrintFormForFakeId(getContractSignedRequestType);

        assertThatNoException().isThrownBy(() -> underTest.getPrintFormForFakeId(getContractSignedRequestType));
        assertThat(actualResult).isNotNull().usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}