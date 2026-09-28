package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.interplat4.altcraft.util.TriggerConstants;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.model.MailingContractResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.subject.RTransport;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhiteCardRequestFactoryTest {

    @Mock
    private ClientInfoFactory clientInfoFactory;
    @Mock
    private SubscriptionFactory subscriptionFactory;
    @Mock
    private UnicusUsrService clientUsr;
    @Mock
    private UnicusSubjectService clientSubject;
    @Mock
    private AttachmentFactory attachmentFactory;
    @Mock
    private MailingClient mailingClient;

    private WhiteCardRequestFactory underTest;
    private final String altcraftToken = "test-token";

    @BeforeEach
    void setUp() {
        underTest = new WhiteCardRequestFactory(
                clientInfoFactory,
                subscriptionFactory,
                clientUsr,
                clientSubject,
                attachmentFactory,
                mailingClient
        );
        ReflectionTestUtils.setField(underTest, "altcraftToken", altcraftToken);
    }

    @Test
    void shouldReturnWhiteCardTriggerId() {
        assertThat(underTest.triggerId()).isEqualTo(TriggerConstants.WHITE_CARD);
    }

    @Test
    void shouldCreateRequestWithCorrectFields() {
        long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(contractId);
        contract.setContractSeria("XXX");
        contract.setContractNumber("123456");
        contract.setBeginDate(LocalDateTime.now().minusDays(1));
        contract.setEndDate(LocalDateTime.now().plusYears(1));
        contract.setContractPremium(new BigDecimal("5000.00"));
        contract.setSubjectId(999L);

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("Иван");
        when(clientInfo.lastName()).thenReturn("Иванов");
        when(clientInfo.middleName()).thenReturn("Иванович");

        RContractSubject subject = new RContractSubject();
        subject.setSubjectId(999L);
        List<RContractSubject> subjectList = List.of(subject);

        RTransport transport = new RTransport();
        transport.setTransportMark("Toyota");
        transport.setTransportModel("Camry");
        List<RTransport> transportList = List.of(transport);

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(clientSubject.getTransport(999L)).thenReturn(transportList);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(List.of());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(List.of());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        assertEquals(altcraftToken, request.getToken());
        assertEquals(TriggerConstants.WHITE_CARD, request.getTriggerId());
        assertEquals("888", request.getFieldValue());

        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("Иван", data.getFirstName());
        assertEquals("Иванов", data.getLastName());
        assertEquals("Иванович", data.getMiddleName());

        ContentFormatted content = (ContentFormatted) request.getContent();
        assertNotNull(content);
        assertEquals("belaya_karta_thanks", content.getProduct());
        assertEquals("123456", content.getContractNumber());
        assertEquals("Иван Иванов", content.getPolicyholder());
        assertEquals("Toyota Camry", content.getObject());
        assertEquals("0", content.getInsuranceAmount());
        assertNotNull(content.getInsurancePremium());
        assertThat(content.getInsurancePremium()).doesNotContain("₽");
        assertNotNull(content.getUrl());
        assertThat(content.getUrl()).contains(Long.toString(contractId));
    }

    @Test
    void shouldCreateRequestWithJuridicalPersonClientInfo() {
        long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(contractId);
        contract.setContractSeria("XXX");
        contract.setContractNumber("123456");
        contract.setBeginDate(LocalDateTime.now().minusDays(1));
        contract.setEndDate(LocalDateTime.now().plusYears(1));
        contract.setContractPremium(new BigDecimal("5000.00"));
        contract.setSubjectId(999L);

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("ООО Компания");
        when(clientInfo.lastName()).thenReturn("");
        when(clientInfo.middleName()).thenReturn("");

        RContractSubject subject = new RContractSubject();
        subject.setSubjectId(999L);
        List<RContractSubject> subjectList = List.of(subject);

        RTransport transport = new RTransport();
        transport.setTransportMark("Toyota");
        transport.setTransportModel("Camry");
        List<RTransport> transportList = List.of(transport);

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(clientSubject.getTransport(999L)).thenReturn(transportList);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(List.of());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(List.of());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("ООО Компания", data.getFirstName());
        assertEquals("", data.getLastName());
        assertEquals("", data.getMiddleName());

        ContentFormatted content = (ContentFormatted) request.getContent();
        assertNotNull(content);
        assertEquals("ООО Компания ", content.getPolicyholder());
    }
}