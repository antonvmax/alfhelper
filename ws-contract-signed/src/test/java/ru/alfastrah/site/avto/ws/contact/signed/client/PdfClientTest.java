package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.soap.client.SoapFaultClientException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.GetPrintedFormByContractId;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.atMostOnce;

@ExtendWith(MockitoExtension.class)
class PdfClientTest {

    @Mock
    private ReportClient reportClient;
    private PdfClient underTest;

    @BeforeEach
    public void setUp() {
        underTest = new PdfClient(reportClient);
    }

    @Test
    void shouldCallPrintForServiceWithGivenData() {
        BigInteger calledContractId = BigInteger.valueOf(123L);
        String calledPrintFormId = "1000";
        GetPrintedFormByContractId expectedRequest = new GetPrintedFormByContractId();
        expectedRequest.setContractId(calledContractId);
        expectedRequest.setPrintedFormId(calledPrintFormId);
        byte[] expectedResult = new byte[2];
        given(reportClient.getPrintedFormByContractId(any())).willReturn(expectedResult);

        underTest.getPrintedFormByContractId(calledContractId, calledPrintFormId, null);

        ArgumentCaptor<GetPrintedFormByContractId> requestArgument =
                ArgumentCaptor.forClass(GetPrintedFormByContractId.class);
        verify(reportClient, atMostOnce()).getPrintedFormByContractId(any());
        verify(reportClient).getPrintedFormByContractId(requestArgument.capture());
        assertThat(requestArgument.getValue()).usingRecursiveComparison().isEqualTo(expectedRequest);
    }

    @Test
    void shouldRethrowException() {
        BigInteger calledContractId = BigInteger.valueOf(123L);
        String calledPrintFormId = "1000";
        given(reportClient.getPrintedFormByContractId(any()))
                .willThrow(Mockito.mock(SoapFaultClientException.class));

        assertThatThrownBy(() -> underTest.getPrintedFormByContractId(calledContractId, calledPrintFormId, null))
                .isInstanceOf(EOsagoProccessException.class);
    }
}
