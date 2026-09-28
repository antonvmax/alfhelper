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
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.MsOrangeClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.Transaction;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.TransactionInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.UserInfo;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyData;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyData.LoyaltyStatus;
import ru.alfastrah.site.avto.ws.contact.signed.service.LoyaltyClientDataService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.JuridicalPersonClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.subject.RJuridicalPerson;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.subject.RTransport;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsagoRequestFactoryTest {

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
    @Mock
    private LoyaltyClientDataService loyaltyClientDataService;
    @Mock
    private MsOrangeClient msOrangeClient;

    private OsagoRequestFactory underTest;
    private final String altcraftToken = "test-token";

    @BeforeEach
    void setUp() {
        underTest = new OsagoRequestFactory(
                clientInfoFactory,
                subscriptionFactory,
                clientUsr,
                clientSubject,
                attachmentFactory,
                loyaltyClientDataService,
                msOrangeClient,
                mailingClient
        );
        ReflectionTestUtils.setField(underTest, "altcraftToken", altcraftToken);
    }

    @Test
    void shouldReturnOsagoTriggerId() {
        assertThat(underTest.triggerId()).isEqualTo(TriggerConstants.THANKS_OSAGO_ORANGE);
    }

    @Test
    void shouldCreateRequestWithCorrectFields() {
        Long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(contractId.intValue());
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

        LoyaltyData loyaltyData = new LoyaltyData();
        loyaltyData.setPoints(100L);
        loyaltyData.setStatus(LoyaltyStatus.GOLD);
        loyaltyData.setPercentage("10");
        loyaltyData.setClientId(777L);

        TransactionInfoResponse transactionInfo = new TransactionInfoResponse();
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone("+79991234567");
        userInfo.setBalance(500);
        transactionInfo.setUserInfo(userInfo);
        Transaction transaction = new Transaction();
        transaction.setCategory("POLICY_BUY_OSAGO");
        transaction.setPoints(50);
        transaction.setDate(LocalDateTime.now().minusDays(5));
        transactionInfo.setTransactionList(List.of(transaction));
        transactionInfo.setReverseList(new ArrayList<>());

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(clientSubject.getTransport(999L)).thenReturn(transportList);
        when(loyaltyClientDataService.findLoyaltyDataByClientEmail("test@example.com")).thenReturn(loyaltyData);
        when(msOrangeClient.getTransactionInfo("XXX", "123456")).thenReturn(transactionInfo);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(new ArrayList<>());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(new ArrayList<>());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        assertEquals(altcraftToken, request.getToken());
        assertEquals(TriggerConstants.THANKS_OSAGO_ORANGE, request.getTriggerId());
        assertEquals("888", request.getFieldValue());

        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("Иван", data.getFirstName());
        assertEquals("Иванов", data.getLastName());
        assertEquals("Иванович", data.getMiddleName());
        assertEquals("+79991234567", data.getIdApelsin());
        assertEquals(777L, data.getClientId());

        ContentFormatted content = (ContentFormatted) request.getContent();
        assertNotNull(content);
        assertEquals("XXX&nbsp;123456", content.getContractNumber());
        assertEquals("Иван Иванов", content.getPolicyholder());
        assertEquals("Toyota Camry", content.getObject());
        assertEquals("400 000", content.getInsuranceAmount());
        assertNotNull(content.getInsurancePremium());
        assertEquals(100, content.getPoints());
        assertEquals("GOLD", content.getPointsStatus());
        assertEquals("10", content.getPointsPercentage());
        assertEquals(50, content.getKolichestvoAp());
        assertEquals(500, content.getVsegoAp());
    }

    @Test
    void shouldCreateRequestWithPhysicalPersonClientInfo() {
        Long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(contractId.intValue());
        contract.setContractSeria("XXX");
        contract.setContractNumber("123456");
        contract.setBeginDate(LocalDateTime.now().minusDays(1));
        contract.setEndDate(LocalDateTime.now().plusYears(1));
        contract.setContractPremium(new BigDecimal("5000.00"));
        contract.setSubjectId(999L);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("иван");
        person.setLastName("иванов");
        person.setMiddleName("иванович");
        PhysicalPersonClientInfo clientInfo = new PhysicalPersonClientInfo(person);

        RContractSubject subject = new RContractSubject();
        subject.setSubjectId(999L);
        List<RContractSubject> subjectList = List.of(subject);

        RTransport transport = new RTransport();
        transport.setTransportMark("Toyota");
        transport.setTransportModel("Camry");
        List<RTransport> transportList = List.of(transport);

        LoyaltyData loyaltyData = new LoyaltyData();
        loyaltyData.setPoints(100L);
        loyaltyData.setStatus(LoyaltyStatus.GOLD);
        loyaltyData.setPercentage("10");
        loyaltyData.setClientId(777L);

        TransactionInfoResponse transactionInfo = new TransactionInfoResponse();
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone("+79991234567");
        userInfo.setBalance(500);
        transactionInfo.setUserInfo(userInfo);
        Transaction transaction = new Transaction();
        transaction.setCategory("POLICY_BUY_OSAGO");
        transaction.setPoints(50);
        transaction.setDate(LocalDateTime.now().minusDays(5));
        transactionInfo.setTransactionList(List.of(transaction));
        transactionInfo.setReverseList(new ArrayList<>());

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(clientSubject.getTransport(999L)).thenReturn(transportList);
        when(loyaltyClientDataService.findLoyaltyDataByClientEmail("test@example.com")).thenReturn(loyaltyData);
        when(msOrangeClient.getTransactionInfo("XXX", "123456")).thenReturn(transactionInfo);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(new ArrayList<>());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(new ArrayList<>());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("Иван", data.getFirstName());
        assertEquals("Иванов", data.getLastName());
        assertEquals("Иванович", data.getMiddleName());
    }

    @Test
    void shouldCreateRequestWithJuridicalPersonClientInfo() {
        Long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(contractId.intValue());
        contract.setContractSeria("XXX");
        contract.setContractNumber("123456");
        contract.setBeginDate(LocalDateTime.now().minusDays(1));
        contract.setEndDate(LocalDateTime.now().plusYears(1));
        contract.setContractPremium(new BigDecimal("5000.00"));
        contract.setSubjectId(999L);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmShortName("ООО Компания");
        JuridicalPersonClientInfo clientInfo = new JuridicalPersonClientInfo(person);

        RContractSubject subject = new RContractSubject();
        subject.setSubjectId(999L);
        List<RContractSubject> subjectList = List.of(subject);

        RTransport transport = new RTransport();
        transport.setTransportMark("Toyota");
        transport.setTransportModel("Camry");
        List<RTransport> transportList = List.of(transport);

        LoyaltyData loyaltyData = new LoyaltyData();
        loyaltyData.setPoints(100L);
        loyaltyData.setStatus(LoyaltyStatus.GOLD);
        loyaltyData.setPercentage("10");
        loyaltyData.setClientId(777L);

        TransactionInfoResponse transactionInfo = new TransactionInfoResponse();
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone("+79991234567");
        userInfo.setBalance(500);
        transactionInfo.setUserInfo(userInfo);
        Transaction transaction = new Transaction();
        transaction.setCategory("POLICY_BUY_OSAGO");
        transaction.setPoints(50);
        transaction.setDate(LocalDateTime.now().minusDays(5));
        transactionInfo.setTransactionList(List.of(transaction));
        transactionInfo.setReverseList(new ArrayList<>());

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(clientSubject.getTransport(999L)).thenReturn(transportList);
        when(loyaltyClientDataService.findLoyaltyDataByClientEmail("test@example.com")).thenReturn(loyaltyData);
        when(msOrangeClient.getTransactionInfo("XXX", "123456")).thenReturn(transactionInfo);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(new ArrayList<>());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(new ArrayList<>());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("ООО Компания", data.getFirstName());
        assertEquals("", data.getLastName());
        assertEquals("", data.getMiddleName());
    }
}