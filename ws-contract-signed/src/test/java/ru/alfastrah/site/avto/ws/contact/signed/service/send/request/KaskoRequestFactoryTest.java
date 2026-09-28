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
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.model.MailingContractResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.MsOrangeClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.Transaction;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.TransactionInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.UserInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.usr.RContractPolicy;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RContractVariantCondition;
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
class KaskoRequestFactoryTest {

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
    private MsOrangeClient msOrangeClient;

    private KaskoRequestFactory underTest;
    private final String altcraftToken = "test-token";

    @BeforeEach
    void setUp() {
        underTest = new KaskoRequestFactory(clientInfoFactory, subscriptionFactory, clientUsr,
                clientSubject, attachmentFactory, mailingClient, msOrangeClient);
        ReflectionTestUtils.setField(underTest, "altcraftToken", altcraftToken);
    }

    @Test
    void shouldReturnKaskoTriggerId() {
        assertThat(underTest.triggerId()).isEqualTo(5332);
    }

    @Test
    void shouldCreateRequestWithCorrectFields() {
        long contractId = 12345L;
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(contractId));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId((int) contractId);
        contract.setContractSeria("XXX");
        contract.setContractNumber("123456");
        contract.setActionBeginDate(LocalDateTime.now().minusDays(1));
        contract.setActionEndDate(LocalDateTime.now().plusYears(1));
        contract.setContractPremium(new BigDecimal("5000.00"));
        contract.setSubjectId(999L);
        contract.setInsuranceLimit(new BigDecimal("1000000.00"));

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("Иван");
        when(clientInfo.lastName()).thenReturn("Иванов");
        when(clientInfo.middleName()).thenReturn("Иванович");

        RContractSubject subject = new RContractSubject();
        subject.setSubjectId(999L);
        subject.setSubjectName("Toyota Camry");
        List<RContractSubject> subjectList = List.of(subject);

        RContractVariantCondition condition = new RContractVariantCondition();
        condition.setInsuranceObjectTypeId(173L);
        RContractPolicy policy = new RContractPolicy();
        policy.setInsuranceLimit(new BigDecimal("800000.00"));
        condition.withPolicyList(List.of(policy));
        List<RContractVariantCondition> conditions = List.of(condition);
        tops.unicus.usr.RContractVariant variant = new tops.unicus.usr.RContractVariant();
        variant.withConditions(conditions);
        contract.withVariants(List.of(variant));

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        TransactionInfoResponse transactionInfo = new TransactionInfoResponse();
        UserInfo userInfo = new UserInfo();
        userInfo.setBalance(5220);
        transactionInfo.setUserInfo(userInfo);
        Transaction transaction = new Transaction();
        transaction.setCategory("POLICY_BUY_KASKO_FULL");
        transaction.setPoints(566);
        transaction.setDate(LocalDateTime.now().minusDays(3));
        transactionInfo.setTransactionList(List.of(transaction));

        when(clientUsr.getFullSaleContract(contractId)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(subjectList);
        when(mailingClient.getInfo(contractId)).thenReturn(mailingResponse);
        when(msOrangeClient.getTransactionInfoByNumber("123456")).thenReturn(transactionInfo);
        when(subscriptionFactory.create("test@example.com")).thenReturn(List.of());
        when(attachmentFactory.create(BigInteger.valueOf(contractId), "print-form-id")).thenReturn(List.of());

        Request request = underTest.create(signedRequest);

        assertNotNull(request);
        assertEquals(altcraftToken, request.getToken());
        assertEquals(5332, request.getTriggerId());
        assertEquals("888", request.getFieldValue());

        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("Иван", data.getFirstName());
        assertEquals("Иванов", data.getLastName());
        assertEquals("Иванович", data.getMiddleName());

        ContentFormatted content = (ContentFormatted) request.getContent();
        assertNotNull(content);
        assertEquals("kasko_thanks", content.getProduct());
        assertEquals("123456", content.getContractNumber());
        assertEquals("Иван Иванов", content.getPolicyholder());
        assertEquals("Toyota Camry", content.getObject());
        assertEquals("800000.00", content.getInsuranceAmount());
        assertNotNull(content.getInsurancePremium());
        assertThat(content.getInsurancePremium()).doesNotContain("₽");
        assertNotNull(content.getUrl());
        assertThat(content.getUrl()).contains(Long.toString(contractId));
        assertEquals(566, content.getKolichestvoAp());
        assertEquals(5220, content.getVsegoAp());
        assertNotNull(content.getDataAp());
    }
}