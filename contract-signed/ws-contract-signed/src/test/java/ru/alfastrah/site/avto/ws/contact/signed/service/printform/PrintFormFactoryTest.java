package ru.alfastrah.site.avto.ws.contact.signed.service.printform;

import jakarta.activation.DataHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class PrintFormFactoryTest {

    @Mock
    private PdfClient pdfClient;

    private PrintFormFactory underTest;

    @BeforeEach
    void setUp() {
        underTest = new PrintFormFactory(pdfClient);
    }

    @Test
    void shouldCreateRawPrintFormWhenDataReceived() {
        BigInteger contractId = BigInteger.TEN;
        String form = "123";
        final DataHandler mockedDataHandler = Mockito.mock(DataHandler.class);
        given(pdfClient.getPrintedFormByContractId(any(), any(), any())).willReturn(mockedDataHandler);

        final PrintForm printForm = underTest.create(contractId, form);

        ArgumentCaptor<BigInteger> contractIdCaptor = ArgumentCaptor.forClass(BigInteger.class);
        ArgumentCaptor<String> formCaptor = ArgumentCaptor.forClass(String.class);
        verify(pdfClient, times(1)).getPrintedFormByContractId(contractIdCaptor.capture(), formCaptor.capture(), any());
        assertThat(printForm).isInstanceOf(RawPrintForm.class);
        assertThat(printForm.id()).isEqualTo(form);
        assertThat(printForm.content()).isEqualTo(mockedDataHandler);
        assertThat(contractIdCaptor.getValue()).isEqualTo(contractId);
        assertThat(formCaptor.getValue()).isEqualTo(form);
    }
}