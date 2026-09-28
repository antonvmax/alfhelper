package ru.alfastrah.site.avto.ws.contact.signed.service.send;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.altcraft.model.Attachment;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintFormFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttachmentFactoryTest {

    @Mock
    private PrintFormFactory printFormFactory;

    @Mock
    private ErrorToleranceSigningService signingService;

    @Mock
    private StampService stampService;

    @InjectMocks
    private AttachmentFactory underTest;

    @Test
    @SneakyThrows
    void shouldCreateAttachmentForSingleFormId() {
        BigInteger contractId = BigInteger.valueOf(123L);
        String formSequence = "525";
        byte[] pdfBytes = "fake pdf content".getBytes();
        DataHandler dataHandler = createDataHandler(pdfBytes);
        PrintForm mockPrintForm = createMockPrintForm(pdfBytes);
        given(printFormFactory.create(contractId, formSequence)).willReturn(mockPrintForm);
        given(stampService.getStampData(contractId, formSequence)).willReturn(List.of());
        given(signingService.sign(eq(pdfBytes), eq(List.of()), eq(contractId))).willReturn(dataHandler);

        List<Attachment> attachments = underTest.create(contractId, formSequence);

        assertThat(attachments).hasSize(1);
        Attachment attachment = attachments.get(0);
        assertThat(attachment.getName()).isEqualTo("Полис.pdf");
        assertThat(attachment.getData()).startsWith("data:application/pdf;base64,");
        verify(printFormFactory).create(contractId, formSequence);
        verify(stampService).getStampData(contractId, formSequence);
        verify(signingService).sign(eq(pdfBytes), eq(List.of()), eq(contractId));
    }

    @Test
    @SneakyThrows
    void shouldCreateAttachmentsForMultipleFormIds() {
        BigInteger contractId = BigInteger.valueOf(456L);
        String formSequence = "525,526,716";
        byte[] pdfBytes1 = "pdf1".getBytes();
        byte[] pdfBytes2 = "pdf2".getBytes();
        byte[] pdfBytes3 = "pdf3".getBytes();
        DataHandler dataHandler1 = createDataHandler(pdfBytes1);
        DataHandler dataHandler2 = createDataHandler(pdfBytes2);
        DataHandler dataHandler3 = createDataHandler(pdfBytes3);
        PrintForm mockPrintForm1 = createMockPrintForm(pdfBytes1);
        PrintForm mockPrintForm2 = createMockPrintForm(pdfBytes2);
        PrintForm mockPrintForm3 = createMockPrintForm(pdfBytes3);
        given(printFormFactory.create(contractId, "525")).willReturn(mockPrintForm1);
        given(printFormFactory.create(contractId, "526")).willReturn(mockPrintForm2);
        given(printFormFactory.create(contractId, "716")).willReturn(mockPrintForm3);
        given(stampService.getStampData(contractId, "525")).willReturn(List.of());
        given(stampService.getStampData(contractId, "526")).willReturn(List.of());
        given(stampService.getStampData(contractId, "716")).willReturn(List.of());
        given(signingService.sign(eq(pdfBytes1), eq(List.of()), eq(contractId))).willReturn(dataHandler1);
        given(signingService.sign(eq(pdfBytes2), eq(List.of()), eq(contractId))).willReturn(dataHandler2);
        given(signingService.sign(eq(pdfBytes3), eq(List.of()), eq(contractId))).willReturn(dataHandler3);

        List<Attachment> attachments = underTest.create(contractId, formSequence);

        assertThat(attachments).hasSize(3);
        assertThat(attachments.get(0).getName()).isEqualTo("Полис.pdf");
        assertThat(attachments.get(1).getName()).isEqualTo("Полис.pdf");
        assertThat(attachments.get(2).getName()).isEqualTo("Заявление.pdf");
    }

    @Test
    @SneakyThrows
    void shouldUseDefaultFilenameWhenFormIdNotInMap() {
        BigInteger contractId = BigInteger.valueOf(789L);
        String formSequence = "999";
        byte[] pdfBytes = "pdf".getBytes();
        DataHandler dataHandler = createDataHandler(pdfBytes);
        PrintForm mockPrintForm = createMockPrintForm(pdfBytes);
        given(printFormFactory.create(contractId, "999")).willReturn(mockPrintForm);
        given(stampService.getStampData(contractId, "999")).willReturn(List.of());
        given(signingService.sign(eq(pdfBytes), eq(List.of()), eq(contractId))).willReturn(dataHandler);

        List<Attachment> attachments = underTest.create(contractId, formSequence);

        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getName()).isEqualTo("Полис.pdf");
    }

    @Test
    @SneakyThrows
    void shouldHandleEmptyFormSequence() {
        BigInteger contractId = BigInteger.valueOf(111L);
        String formSequence = "";
        byte[] pdfBytes = "pdf".getBytes();
        DataHandler dataHandler = createDataHandler(pdfBytes);
        PrintForm mockPrintForm = createMockPrintForm(pdfBytes);
        given(printFormFactory.create(contractId, "-1")).willReturn(mockPrintForm);
        given(stampService.getStampData(contractId, "-1")).willReturn(List.of());
        given(signingService.sign(eq(pdfBytes), eq(List.of()), eq(contractId))).willReturn(dataHandler);

        List<Attachment> attachments = underTest.create(contractId, formSequence);

        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getName()).isEqualTo("Полис.pdf");
    }

    @Test
    @SneakyThrows
    void shouldThrowSendContractServerExceptionWhenSigningFails() {
        BigInteger contractId = BigInteger.valueOf(222L);
        String formSequence = "525";
        byte[] pdfBytes = "pdf".getBytes();
        PrintForm mockPrintForm = createMockPrintForm(pdfBytes);
        given(printFormFactory.create(contractId, "525")).willReturn(mockPrintForm);
        given(stampService.getStampData(contractId, "525")).willReturn(List.of());
        given(signingService.sign(eq(pdfBytes), eq(List.of()), eq(contractId)))
                .willThrow(new RuntimeException("Signing error"));

        assertThatThrownBy(() -> underTest.create(contractId, formSequence))
                .isInstanceOf(SendContractServerException.class)
                .hasMessageContaining("Signing error");
    }

    @Test
    @SneakyThrows
    void shouldHandleStampParamsNotEmpty() {
        BigInteger contractId = BigInteger.valueOf(333L);
        String formSequence = "525";
        byte[] pdfBytes = "pdf".getBytes();
        DataHandler dataHandler = createDataHandler(pdfBytes);
        PrintForm mockPrintForm = createMockPrintForm(pdfBytes);
        StampParams stampParam = new StampParams();
        List<StampParams> stampParams = List.of(stampParam);
        given(printFormFactory.create(contractId, "525")).willReturn(mockPrintForm);
        given(stampService.getStampData(contractId, "525")).willReturn(stampParams);
        given(signingService.sign(eq(pdfBytes), eq(stampParams), eq(contractId))).willReturn(dataHandler);

        List<Attachment> attachments = underTest.create(contractId, formSequence);

        assertThat(attachments).hasSize(1);
        verify(signingService).sign(eq(pdfBytes), eq(stampParams), eq(contractId));
    }

    private DataHandler createDataHandler(byte[] content) {
        return new DataHandler(new ByteArrayDataSource(content, "application/pdf"));
    }

    @SneakyThrows
    private PrintForm createMockPrintForm(byte[] content) {
        DataHandler dataHandler = createDataHandler(content);
        return new RawPrintForm(dataHandler, "someId");
    }
}