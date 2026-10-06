package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Subscription;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.interplat4.altcraft.util.ProductConstants;
import ru.alfastrah.interplat4.altcraft.util.TriggerConstants;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.send.EntityType;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.MsPaymentClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.model.ReceiptContract;
import ru.alfastrah.site.avto.ws.contact.signed.client.payment.model.ReceiptInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.hid.FuzzySearchClientHidService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.JuridicalPersonClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.impl.PhysicalPersonClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.subject.RJuridicalPerson;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_CHANNEL;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_RESOURCE_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_STATUS;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {DirectSendRequestFactory.class,
        SubscriptionFactory.class,
        ClientInfoFactory.class})
public class DirectSendRequestFactoryTest {

    @MockBean
    private FuzzySearchClientHidService hidClientService;
    @MockBean
    private AttachmentFactory attachmentFactory;
    @MockBean
    private BuildEmail buildEmail;
    @MockBean
    private PartnersDb partnersDb;
    @MockBean
    private MsPaymentClient client;
    @MockBean
    private UnicusUsrService usrService;
    @MockBean
    private UnicusSubjectService subjectService;
    @MockBean
    private ClientInfoFactory clientInfoFactory;

    @Autowired
    private DirectSendRequestFactory directSendRequestFactory;
    @Autowired
    private SubscriptionFactory subscriptionFactory;

    private DirectSendContractSignedRequest sendRequest;

    @BeforeEach
    public void init() {
        directSendRequestFactory = new DirectSendRequestFactory(hidClientService, clientInfoFactory, subscriptionFactory,
                attachmentFactory, buildEmail, partnersDb, client, usrService);

        sendRequest = new DirectSendContractSignedRequest();
        sendRequest.setEntityId(new BigInteger("283024210"));
        sendRequest.setEmail(List.of("test@mail.ru"));

    }

    @Test
    @SneakyThrows
    @DisplayName("Успешное создание запроса для юр. лиц, один договор")
    void validContractCreateJuridicalRequest() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmName("ООО Firm");
        person.setFirmShortName("Firm");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("JURIDICAL_PERSON");
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(clientInfoFactory.byContract(any())).thenReturn(new JuridicalPersonClientInfo(person));
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals("3", request.getFieldValue());
        assertEquals(person.getFirmShortName(), ((SaleContract) request.getData()).getFirstName());
        assertEquals("\"АльфаСтрахование\". Ваш полис", ((ContentFormatted) request.getContent()).getTema());
        verify(hidClientService, never()).searchHid(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Успешное создание запроса для юр. лиц, единый чек")
    void validSingleAccCreateJuridicalRequest() {
        sendRequest.setEntityType(EntityType.SINGLE_ACC);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmName("ООО Firm");
        person.setFirmShortName("Firm");

        ReceiptInfoResponse receiptInfoResponse = new ReceiptInfoResponse();
        receiptInfoResponse.setMasterContractId(sendRequest.getEntityId());

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(client.getInfo(any())).thenReturn(receiptInfoResponse);
        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(clientInfoFactory.byContract(any())).thenReturn(new JuridicalPersonClientInfo(person));

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals("3", request.getFieldValue());
        assertEquals(person.getFirmShortName(), ((SaleContract) request.getData()).getFirstName());
        assertEquals("\"АльфаСтрахование\". Ваши полисы", ((ContentFormatted) request.getContent()).getTema());
        verify(hidClientService, never()).searchHid(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Успешное создание запроса для физ. лиц, один договор")
    void validContractCreatePhysicalRequest() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("FirstName");
        person.setMiddleName("MiddleName");
        person.setLastName("LastName");


        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("PHYSICAL_PERSON");
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(hidClientService.searchHid(any(), any())).thenReturn(null);
        when(clientInfoFactory.byContract(any())).thenReturn(new PhysicalPersonClientInfo(person));
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals("3", request.getFieldValue());
        assertEquals("Firstname", ((SaleContract) request.getData()).getFirstName());
        assertEquals("Middlename", ((SaleContract) request.getData()).getMiddleName());
        assertEquals("Lastname", ((SaleContract) request.getData()).getLastName());
        assertEquals("\"АльфаСтрахование\". Ваш полис", ((ContentFormatted) request.getContent()).getTema());
        verify(hidClientService, times(1)).searchHid(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Успешное создание запроса для физ. лиц, Единый Чек")
    void validSingleAccCreatePhysicalRequest() {
        sendRequest.setEntityType(EntityType.SINGLE_ACC);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("FirstName");
        person.setMiddleName("MiddleName");
        person.setLastName("LastName");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        ReceiptInfoResponse receiptInfoResponse = new ReceiptInfoResponse();
        receiptInfoResponse.setMasterContractId(sendRequest.getEntityId());

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("PHYSICAL_PERSON");
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(client.getInfo(any())).thenReturn(receiptInfoResponse);
        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(hidClientService.searchHid(any(), any())).thenReturn(null);
        when(clientInfoFactory.byContract(any())).thenReturn(new PhysicalPersonClientInfo(person));


        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals("3", request.getFieldValue());
        assertEquals("Firstname", ((SaleContract) request.getData()).getFirstName());
        assertEquals("Middlename", ((SaleContract) request.getData()).getMiddleName());
        assertEquals("Lastname", ((SaleContract) request.getData()).getLastName());
        assertEquals("\"АльфаСтрахование\". Ваши полисы", ((ContentFormatted) request.getContent()).getTema());
        verify(hidClientService, times(1)).searchHid(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Исключение при неправильном номере договора")
    void invalidContractNumberThrowsException() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        when(usrService.getFullSaleContract(any())).thenReturn(null);

        assertThatThrownBy(() -> directSendRequestFactory.create(sendRequest))
                .isInstanceOf(EOsagoSaveException.class);
    }

    @Test
    @SneakyThrows
    @DisplayName("Исключение при неправильном номере единого чека")
    void invalidEntityIdThrowsException() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        when(client.getInfo(any())).thenReturn(null);

        assertThatThrownBy(() -> directSendRequestFactory.create(sendRequest))
                .isInstanceOf(EOsagoSaveException.class);
    }

    @Test
    @SneakyThrows
    @DisplayName("Успешное заполнение данных получателя письма")
    void validFillDataSubscription() {
        Request altcraftRequest = new Request();
        SaleContract data = new SaleContract();
        altcraftRequest.setData(data);

        directSendRequestFactory.fillDataSubscription(altcraftRequest, sendRequest.getEmail().get(0));

        Subscription subscription = ((SaleContract) altcraftRequest.getData()).getSubscriptions().get(0);

        assertEquals(ALTCRAFT_CHANNEL, subscription.getChannel());
        assertEquals(sendRequest.getEmail().get(0), subscription.getEmail());
        assertEquals(ALTCRAFT_RESOURCE_ID, subscription.getResourceId());
        assertEquals(ALTCRAFT_STATUS, subscription.getStatus());
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка обработки пустых значений в именах физического лица")
    void shouldHandleEmptyNamesForPhysicalPerson() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("");
        person.setMiddleName("");
        person.setLastName("");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("PHYSICAL_PERSON");
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(hidClientService.searchHid(any(), any())).thenReturn(null);
        when(clientInfoFactory.byContract(any())).thenReturn(new PhysicalPersonClientInfo(person));
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        SaleContract saleContract = (SaleContract) request.getData();
        assertNotNull(saleContract.getFirstName());
        assertNotNull(saleContract.getLastName());
        assertNotNull(saleContract.getMiddleName());
        assertEquals("", saleContract.getFirstName());
        assertEquals("", saleContract.getLastName());
        assertEquals("", saleContract.getMiddleName());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обработка исключения при получении печатной формы для единого чека")
    void shouldHandleExceptionInSingleAccPrintForm() {
        sendRequest.setEntityType(EntityType.SINGLE_ACC);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmName("ООО Firm");
        person.setFirmShortName("Firm");

        ReceiptInfoResponse receiptInfoResponse = new ReceiptInfoResponse();
        receiptInfoResponse.setMasterContractId(sendRequest.getEntityId());

        ReceiptContract receiptContract1 = new ReceiptContract();
        receiptContract1.setId(BigInteger.valueOf(111));
        ReceiptContract receiptContract2 = new ReceiptContract();
        receiptContract2.setId(BigInteger.valueOf(222));
        receiptInfoResponse.setContractList(List.of(receiptContract1, receiptContract2));

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        when(client.getInfo(any())).thenReturn(receiptInfoResponse);
        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(clientInfoFactory.byContract(any())).thenReturn(new JuridicalPersonClientInfo(person));
        when(partnersDb.getContractInfo(receiptContract1.getId())).thenReturn(new RSaleContract());
        when(partnersDb.getContractInfo(receiptContract2.getId())).thenReturn(new RSaleContract());
        when(buildEmail.getPrintFormId(any(), any())).thenThrow(new RuntimeException("Print form error"));
        when(attachmentFactory.create(any(), any())).thenReturn(new ArrayList<>());

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertTrue(request.getAttach().isEmpty());
        verify(buildEmail, times(2)).getPrintFormId(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка работы с HID для физического лица когда HID найден")
    void shouldUseFoundHidForPhysicalPerson() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("TestFirst");
        person.setMiddleName("TestMiddle");
        person.setLastName("TestLast");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(3);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("PHYSICAL_PERSON");
        contract.setSubjectId(3);
        contract.withContractSubjects(List.of(contractSubject));

        Long foundHid = 999L;

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(hidClientService.searchHid(any(), any())).thenReturn(foundHid);
        when(clientInfoFactory.byContract(any())).thenReturn(new PhysicalPersonClientInfo(person));
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals(foundHid.toString(), request.getFieldValue());
        verify(hidClientService).searchHid(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Тест обработки EOsagoException при создании вложений для контракта")
    void shouldHandleEOsagoExceptionInContractAttachment() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmName("ООО Test");
        person.setFirmShortName("Test");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(5);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(5);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(clientInfoFactory.byContract(any())).thenReturn(new JuridicalPersonClientInfo(person));
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(buildEmail.getPrintFormId(any(), any())).thenThrow(new EOsagoException("Test exception", "TEST"));

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals("5", request.getFieldValue());
        verify(buildEmail).getPrintFormId(any(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка корректной установки триггера и продукта")
    void shouldSetCorrectTriggerAndProduct() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RJuridicalPerson person = new RJuridicalPerson();
        person.setFirmShortName("TestFirm");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(7);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(7);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(clientInfoFactory.byContract(any())).thenReturn(new JuridicalPersonClientInfo(person));
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        assertEquals(TriggerConstants.THANKS_POVTOR, request.getTriggerId());

        ContentFormatted content = (ContentFormatted) request.getContent();
        assertEquals(ProductConstants.THANKS_POVTOR, content.getProduct());
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка капитализации имен с разным регистром")
    void shouldCapitalizeNamesWithMixedCase() {
        sendRequest.setEntityType(EntityType.CONTRACT);

        RPhysicalPerson person = new RPhysicalPerson();
        person.setFirstName("jOHN");
        person.setMiddleName("aLEXANDER");
        person.setLastName("sMITH");

        RContractSubject contractSubject = new RContractSubject();
        contractSubject.setOwnerSubjectId(123);

        RSaleContract contract = new RSaleContract();
        contract.setSubjectTypeId("PHYSICAL_PERSON");
        contract.setSubjectId(123);
        contract.withContractSubjects(List.of(contractSubject));

        when(usrService.getFullSaleContract(any())).thenReturn(contract);
        when(partnersDb.getContractInfo(any())).thenReturn(new RSaleContract());
        when(hidClientService.searchHid(any(), any())).thenReturn(null);
        when(clientInfoFactory.byContract(any())).thenReturn(new PhysicalPersonClientInfo(person));
        when(buildEmail.getPrintFormId(any(), any())).thenReturn("");

        Request request = directSendRequestFactory.create(sendRequest);

        assertNotNull(request);
        SaleContract saleContract = (SaleContract) request.getData();
        assertEquals("John", saleContract.getFirstName());
        assertEquals("Alexander", saleContract.getMiddleName());
        assertEquals("Smith", saleContract.getLastName());
    }
}