package ru.alfastrah.site.avto.ws.contact.signed.service.send.process;

import jakarta.mail.internet.MimeMessage;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.model.exception.AltcraftSenderNotImplemented;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.EmailSendLogService;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSender;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSenderFactory;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractSignedProcessServiceTest {

    @Mock
    private JavaMailSender emailSender;
    @Mock
    private BuildEmail buildEmail;
    @Mock
    private PartnersDb partnersDb;
    @Mock
    private MessageSenderFactory senderFactory;
    @Mock
    private MessageSender messageSender;
    @Mock
    private EmailSendLogService emailSendLogService;

    private ContractSignedProcessService contractSignedProcessService;

    @BeforeEach
    void setUp() {
        contractSignedProcessService = new ContractSignedProcessService(
                "test@example.com",
                "sender@alfastrah.ru",
                emailSender,
                buildEmail,
                partnersDb,
                senderFactory,
                emailSendLogService
        );
    }

    @Test
    void shouldThrowBadDataExceptionWhenContractInfoIsNull() {
        SendContractSignedRequest request = createRequest();
        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(null);

        assertThatThrownBy(() -> contractSignedProcessService.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Не найден продукт в договоре с номером - " + request.getContractId());
    }

    @Test
    void shouldThrowBadDataExceptionWhenContractHasNoVariants() {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = new RSaleContract();
        contract.withVariants(List.of());

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);

        assertThatThrownBy(() -> contractSignedProcessService.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Не найден продукт в договоре с номером");
    }

    @Test
    void shouldProcessSuccessfullyWhenMessageSenderExists() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(messageSender).send(request);

        boolean result = contractSignedProcessService.processSendContractSigned(request);

        assertTrue(result);
        verify(messageSender).send(request);
        verify(emailSender, times(2)).createMimeMessage();
    }

    @Test
    void shouldThrowBadDataExceptionWhenEOsagoSaveExceptionOccurs() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        doThrow(new EOsagoSaveException("Test error message", "ERROR_CODE")).when(messageSender).send(request);

        assertThatThrownBy(() -> contractSignedProcessService.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage("Test error message");
    }

    @Test
    void shouldThrowBadDataExceptionWhenEOsagoExceptionMailNoStacktraceOccurs() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        doThrow(new EOsagoExceptionMailNoStacktrace("Test mail error", "MAIL_ERROR")).when(messageSender).send(request);

        assertThatThrownBy(() -> contractSignedProcessService.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage("Test mail error");
    }

    @Test
    void shouldProcessWithEmailWhenAltcraftSenderNotImplemented() throws Exception {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("KASKO");
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("046")).thenReturn(messageSender);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new AltcraftSenderNotImplemented("Not implemented")).when(messageSender).send(request);

        boolean result = contractSignedProcessService.processSendContractSigned(request);

        assertTrue(result);
        verify(messageSender).send(request);
    }

    @Test
    void shouldHandleBadDataExceptionInGuaranteedProcess() {
        SendContractSignedRequest request = createRequest();
        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(null);

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Не найден продукт в договоре с номером");

        verify(emailSendLogService).handleException(eq(request), eq(false), anyString());
    }

    @Test
    void shouldHandlePersistenceExceptionInGuaranteedProcess() {
        SendContractSignedRequest request = createRequest();
        when(partnersDb.getContractInfo(request.getContractId())).thenThrow(new PersistenceException("DB error"));

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(PersistenceException.class)
                .hasMessage("DB error");

        verify(emailSendLogService).handleException(eq(request), eq(true), eq("DB error"));
    }

    @Test
    void shouldHandleDataAccessExceptionInGuaranteedProcess() {
        SendContractSignedRequest request = createRequest();
        DataAccessException dataAccessException = mock(DataAccessException.class);
        when(dataAccessException.getMessage()).thenReturn("Data access error");
        when(partnersDb.getContractInfo(request.getContractId())).thenThrow(dataAccessException);

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(PersistenceException.class)
                .hasMessage("Data access error");

        verify(emailSendLogService).handleException(eq(request), eq(true), eq("Data access error"));
    }

    @Test
    void shouldHandleGenericExceptionInGuaranteedProcess() {
        SendContractSignedRequest request = createRequest();
        when(partnersDb.getContractInfo(request.getContractId())).thenThrow(new RuntimeException("Generic error"));

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(PersistenceException.class)
                .hasMessage("Generic error");

        verify(emailSendLogService).handleException(eq(request), eq(false), eq("Generic error"));
    }

    @Test
    void shouldHandleSuccessInGuaranteedProcess() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(messageSender).send(request);

        contractSignedProcessService.guaranteedProcessSend(request);

        verify(emailSendLogService).handleSuccess(request);
        verify(messageSender).send(request);
    }

    @Test
    void shouldHandleSenderExceptionInGuaranteedProcess() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        doThrow(new EOsagoSaveException("Sender error", "ERROR_CODE")).when(messageSender).send(request);

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage("Sender error");

        verify(emailSendLogService).handleException(eq(request), eq(false), eq("Sender error"));
        verify(emailSendLogService, never()).handleSuccess(request);
    }

    @Test
    void shouldHandleAltcraftNotImplementedInGuaranteedProcess() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new AltcraftSenderNotImplemented("Not implemented")).when(messageSender).send(request);

        contractSignedProcessService.guaranteedProcessSend(request);

        verify(emailSendLogService).handleException(eq(request), eq(false), eq("Not implemented"));
    }

    @Test
    void shouldHandleGenericExceptionInMessageSending() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = createRequest();
        RSaleContract contract = createContract("OSAGO");

        when(partnersDb.getContractInfo(request.getContractId())).thenReturn(contract);
        when(senderFactory.byProduct("OSAGO")).thenReturn(messageSender);
        doThrow(new RuntimeException("Unexpected error")).when(messageSender).send(request);

        assertThatThrownBy(() -> contractSignedProcessService.guaranteedProcessSend(request))
                .isInstanceOf(BadDataException.class)
                .hasMessage("Unexpected error");

        verify(emailSendLogService).handleException(eq(request), eq(null), eq("Unexpected error"));
    }

    private SendContractSignedRequest createRequest() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.valueOf(12345L));
        request.setEmail("test@example.com");
        return request;
    }

    private RSaleContract createContract(String productId) {
        RSaleContract contract = new RSaleContract();
        RContractVariant variant = new RContractVariant();
        variant.setProductId(getProductIdByName(productId));
        contract.withVariants(List.of(variant));
        return contract;
    }

    private String getProductIdByName(String productName) {
        return switch (productName) {
            case "OSAGO" -> "OSAGO";
            case "KASKO" -> "046";
            case "WHITE_CARD" -> "190";
            default -> productName;
        };
    }
}