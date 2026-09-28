package ru.alfastrah.site.avto.ws.contact.signed.service.send;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.EmailSendLogService;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.process.ContractSignedProcessService;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSender;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.sender.MessageSenderFactory;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

@ExtendWith(MockitoExtension.class)
class ContractSignedProcessServiceTest {

    @Mock
    private JavaMailSender emailSender;
    @Mock
    private BuildEmail buildEmail;
    @Mock
    private PartnersDb partnersDbBean;
    @Mock
    private MessageSenderFactory senderFactory;

    @Mock
    private MessageSender messageSender;

    @Mock
    private EmailSendLogService emailSendLogService;

    private ContractSignedProcessService underTest;

    @BeforeEach
    void setUp() {
        RSaleContract contract = new RSaleContract();
        RContractVariant contractVariant = new RContractVariant();
        contractVariant.setProductId("OSAGO");
        contract.withVariants(contractVariant);
        when(partnersDbBean.getContractInfo(any())).thenReturn(contract);
        underTest = new ContractSignedProcessService(
                "",
                "",
                emailSender,
                buildEmail,
                partnersDbBean,
                senderFactory,
                emailSendLogService
        );
    }

    @Test
    void shouldThrowExceptionWhenContractNotFound() {
        SendContractSignedRequest request = new SendContractSignedRequest();
        when(partnersDbBean.getContractInfo(any())).thenReturn(null);

        assertThatThrownBy(() -> underTest.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class).
                hasMessageContaining("Не найден продукт в договоре с номером - " + request.getContractId());
    }

    @Test
    void shouldUseMessageSenderWhenFoundForProduct() throws MessagingException {
        when(senderFactory.byProduct(any())).thenReturn(messageSender);
        SendContractSignedRequest request = new SendContractSignedRequest();
        when(emailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));

        underTest.processSendContractSigned(request);

        verify(buildEmail, noInteractions()).createEmailBody(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenProblemWithAltkraftSend() throws EOsagoExceptionMailNoStacktrace {
        SendContractSignedRequest request = new SendContractSignedRequest();
        when(senderFactory.byProduct(any())).thenReturn(messageSender);
        doThrow(EOsagoSaveException.class).when(messageSender).send(any());


        assertThatThrownBy(() -> underTest.processSendContractSigned(request))
                .isInstanceOf(BadDataException.class);
    }
}