package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import com.sun.istack.ByteArrayDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MimeTypeUtils;
import ru.alfastrah.interplat4.model.print.form.ResponseType;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;

import jakarta.activation.DataHandler;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class GetPolicyBase64Test {

    @Mock
    private PdfClient mockPdfClient;
    @Mock
    private StampService mockStampService;
    @Mock
    private ErrorToleranceSigningService mockSigningService;

    private GetPolicyBase64 getPolicyBase64UnderTest;

    @BeforeEach
    void setUp() {
        getPolicyBase64UnderTest = new GetPolicyBase64(mockPdfClient, mockStampService, mockSigningService);
    }

    @Test
    void testGetPolicyByContractId() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        ResponseType expectedResult = new ResponseType();
        expectedResult.setContent(new DataHandler(new ByteArrayDataSource(new byte[2], MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));

        when(mockPdfClient.getPrintedFormByContractId(any(), any(), any())).thenReturn(expectedResult.getContent());

        List<StampParams> stampData = new ArrayList<>();
        stampData.add(new StampParams());
        when(mockStampService.getStampData(any(), any())).thenReturn(stampData);

        when(mockSigningService.sign(any(), any(), anyBoolean(), any()))
                .thenReturn(new DataHandler(new ByteArrayDataSource(new byte[0], "application/octet-stream")));

        String result = getPolicyBase64UnderTest.getPolicyByContractId(123L, request);

        verify(mockPdfClient, times(1)).getPrintedFormByContractId(any(), any(), any());
        assertNotNull(result);
    }

    @Test
    void testGetPolicyByContractId_exception() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        when(mockPdfClient.getPrintedFormByContractId(any(), any(), any())).thenThrow(EOsagoProccessException.class);

        String result = getPolicyBase64UnderTest.getPolicyByContractId(123L, request);

        verify(mockPdfClient, times(1)).getPrintedFormByContractId(any(), any(), any());
        assertNull(result);
    }

    @Test
    void testGetPolicyByContractId_notException_DataHandler_null() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        ResponseType expectedResult = new ResponseType();
        expectedResult.setContent(new DataHandler(new ByteArrayDataSource(new byte[2], MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));
        when(mockPdfClient.getPrintedFormByContractId(any(), any(), any())).thenReturn(expectedResult.getContent());

        String result = getPolicyBase64UnderTest.getPolicyByContractId(123L, request);

        verify(mockPdfClient, times(1)).getPrintedFormByContractId(any(), any(), any());
        assertNull(result);
    }

    @Test
    void shouldSetParamWhenRequestWithAvisSystem() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system=AVIS");
        request.setPrintedFormId("123");

        ResponseType expectedResult = new ResponseType();
        expectedResult.setContent(new DataHandler(new ByteArrayDataSource(new byte[2], MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));
        when(mockPdfClient.getPrintedFormByContractId(any(), any(), any())).thenReturn(expectedResult.getContent());

        assertDoesNotThrow(() -> getPolicyBase64UnderTest.getPolicyByContractId(123L, request));

        ArgumentCaptor<String> param = ArgumentCaptor.forClass(String.class);
        verify(mockPdfClient).getPrintedFormByContractId(any(BigInteger.class), anyString(), param.capture());

        assertEquals("system=AVIS", param.getValue());
    }
}