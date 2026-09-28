package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_DB_ID;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_FIELD_NAME;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_MATCHING;
import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.PASSBOOK;

@ExtendWith(MockitoExtension.class)
class RequestFactoryTest {

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

    private RequestFactoryStub requestFactoryStub;
    private final String altcraftToken = "test-token";

    @BeforeEach
    void setUp() {
        requestFactoryStub = new RequestFactoryStub(
                clientInfoFactory,
                subscriptionFactory,
                clientUsr,
                clientSubject,
                attachmentFactory,
                mailingClient
        );
        ReflectionTestUtils.setField(requestFactoryStub, "altcraftToken", altcraftToken);
    }

    private static class RequestFactoryStub extends RequestFactory {
        RequestFactoryStub(ClientInfoFactory clientInfoFactory,
                           SubscriptionFactory subscriptionFactory,
                           UnicusUsrService clientUsr,
                           UnicusSubjectService clientSubject,
                           AttachmentFactory attachmentFactory,
                           MailingClient mailingClient) {
            super(clientInfoFactory, subscriptionFactory, clientUsr, clientSubject, attachmentFactory, mailingClient);
        }

        @Override
        protected Integer triggerId() {
            return 123;
        }

        @Override
        protected ContentFormatted createContent(ClientInfo clientInfo, RSaleContract contract,
                                                 SendContractSignedRequest request, SaleContract data) {
            ContentFormatted content = new ContentFormatted();
            content.setTema("Test tema");
            return content;
        }
    }

    @Test
    @DisplayName("Создание запроса: успешный сценарий")
    void create_success() throws EOsagoSaveException {
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(999L));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(999L);
        contract.setSubjectId(777L);

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("Ivan");
        when(clientInfo.lastName()).thenReturn("Ivanov");
        when(clientInfo.middleName()).thenReturn("Ivanovich");

        when(clientUsr.getFullSaleContract(999L)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(mailingClient.getInfo(999L)).thenReturn(new MailingContractResponse());
        when(subscriptionFactory.create("test@example.com")).thenReturn(List.of());
        when(attachmentFactory.create(BigInteger.valueOf(999L), "print-form-id")).thenReturn(List.of());

        Request request = requestFactoryStub.create(signedRequest);

        assertNotNull(request);
        assertEquals(altcraftToken, request.getToken());
        assertEquals(ALTCRAFT_DB_ID, request.getDbId());
        assertEquals(ALTCRAFT_MATCHING, request.getMatching());
        assertEquals(ALTCRAFT_FIELD_NAME, request.getFieldName());
        assertEquals("777", request.getFieldValue());
        assertEquals(123, request.getTriggerId());

        SaleContract data = (SaleContract) request.getData();
        assertNotNull(data);
        assertEquals("Ivan", data.getFirstName());
        assertEquals("Ivanov", data.getLastName());
        assertEquals("Ivanovich", data.getMiddleName());

        assertNotNull(request.getContent());
        assertEquals("Test tema", ((ContentFormatted) request.getContent()).getTema());
        assertTrue(request.getAttach().isEmpty());

        verify(clientUsr).getFullSaleContract(999L);
        verify(clientInfoFactory).byContract(contract);
        verify(mailingClient).getInfo(999L);
        verify(subscriptionFactory).create("test@example.com");
        verify(attachmentFactory).create(BigInteger.valueOf(999L), "print-form-id");
    }

    @Test
    @DisplayName("Создание запроса: hid из mailingClient")
    void create_withHid() {
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(999L));
        signedRequest.setEmail("test@example.com");
        signedRequest.setPrintedFormId("print-form-id");

        RSaleContract contract = new RSaleContract();
        contract.setContractId(999L);
        contract.setSubjectId(777L);

        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("Ivan");
        when(clientInfo.lastName()).thenReturn("Ivanov");
        when(clientInfo.middleName()).thenReturn("Ivanovich");

        MailingContractResponse mailingResponse = new MailingContractResponse();
        mailingResponse.setPartyHid(888L);

        when(clientUsr.getFullSaleContract(999L)).thenReturn(contract);
        when(clientInfoFactory.byContract(contract)).thenReturn(clientInfo);
        when(mailingClient.getInfo(999L)).thenReturn(mailingResponse);
        when(subscriptionFactory.create("test@example.com")).thenReturn(List.of());
        when(attachmentFactory.create(BigInteger.valueOf(999L), "print-form-id")).thenReturn(List.of());

        Request request = requestFactoryStub.create(signedRequest);

        assertEquals("888", request.getFieldValue());
    }

    @Test
    @DisplayName("Создание запроса: ошибка при получении контракта")
    void create_contractNotFound_throwsEOsagoSaveException() {
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(999L));

        when(clientUsr.getFullSaleContract(999L)).thenReturn(null);

        assertThrows(EOsagoSaveException.class, () -> requestFactoryStub.create(signedRequest));
    }

    @Test
    @DisplayName("Создание запроса: WebServiceException при получении контракта")
    void create_webServiceException_throwsSendContractServerException() {
        SendContractSignedRequest signedRequest = new SendContractSignedRequest();
        signedRequest.setContractId(BigInteger.valueOf(999L));

        when(clientUsr.getFullSaleContract(999L)).thenThrow(new org.springframework.ws.client.WebServiceTransportException("Network error"));

        assertThrows(SendContractServerException.class, () -> requestFactoryStub.create(signedRequest));
    }

    @Test
    @DisplayName("fillDataField: корректное заполнение SaleContract")
    void fillDataField_success() {
        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("ivan");
        when(clientInfo.lastName()).thenReturn("ivanov");
        when(clientInfo.middleName()).thenReturn("ivanovich");

        SaleContract saleContract = requestFactoryStub.fillDataField(clientInfo);

        assertNotNull(saleContract);
        assertEquals("Ivan", saleContract.getFirstName());
        assertEquals("Ivanov", saleContract.getLastName());
        assertEquals("Ivanovich", saleContract.getMiddleName());
    }

    @Test
    @DisplayName("formatInsurancePremium: форматирование рублевой суммы")
    void formatInsurancePremium_rub() {
        BigDecimal premium = new BigDecimal("1234.56");
        String result = requestFactoryStub.formatInsurancePremium(premium);
        assertThat(result).doesNotContain("₽");
    }

    @Test
    @DisplayName("formatInsurancePremium: пустая строка возвращает строку премии")
    void formatInsurancePremium_emptyString() {
        BigDecimal premium = new BigDecimal("0");
        String result = requestFactoryStub.formatInsurancePremium(premium);
        assertNotNull(result);
    }

    @Test
    @DisplayName("createPassbookUrl: формирование URL с contractId")
    void createPassbookUrl_withContractId() {
        BigInteger contractId = BigInteger.valueOf(12345);
        String url = requestFactoryStub.createPassbookUrl(contractId);
        assertThat(url).contains("12345");
        assertThat(url).contains("contract_id=12345");
    }

    @Test
    @DisplayName("createPassbookUrl: null contractId")
    void createPassbookUrl_nullContractId() {
        String url = requestFactoryStub.createPassbookUrl(null);
        assertThat(url).contains("contract_id=&key=");
    }

    @Test
    @DisplayName("getFirstLastNames: объединение имени и фамилии с капитализацией")
    void getFirstLastNames_success() {
        ClientInfo clientInfo = mock(ClientInfo.class);
        when(clientInfo.firstName()).thenReturn("ivan");
        when(clientInfo.lastName()).thenReturn("ivanov");
        String result = requestFactoryStub.getFirstLastNames(clientInfo);
        assertEquals("Ivan Ivanov", result);
    }

    @Test
    @DisplayName("getMarkaModelNames: успешное получение марки и модели")
    void getMarkaModelNames_success() {
        Long subjectId = 555L;
        RTransport transport = new RTransport();
        transport.setTransportMark("toyota");
        transport.setTransportModel("camry");
        when(clientSubject.getTransport(subjectId)).thenReturn(List.of(transport));

        String result = requestFactoryStub.getMarkaModelNames(subjectId);
        assertEquals("Toyota Camry", result);
    }

    @Test
    @DisplayName("getMarkaModelNames: пустой список возвращает null")
    void getMarkaModelNames_emptyList() {
        Long subjectId = 555L;
        when(clientSubject.getTransport(subjectId)).thenReturn(List.of());

        String result = requestFactoryStub.getMarkaModelNames(subjectId);
        assertNull(result);
    }

    @Test
    @DisplayName("getSubjectList: успешное получение списка субъектов")
    void getSubjectList_success() throws EOsagoSaveException {
        Long contractId = 999L;
        List<RContractSubject> expectedList = List.of(new RContractSubject());
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(expectedList);

        List<RContractSubject> result = requestFactoryStub.getSubjectList(contractId);
        assertSame(expectedList, result);
    }

    @Test
    @DisplayName("getSubjectList: null результат вызывает исключение")
    void getSubjectList_nullResult_throwsEOsagoSaveException() {
        Long contractId = 999L;
        when(clientUsr.getContractSubject(contractId, 0L)).thenReturn(null);

        assertThrows(EOsagoSaveException.class, () -> requestFactoryStub.getSubjectList(contractId));
    }

    @Test
    @DisplayName("getSubjectList: WebServiceException вызывает SendContractServerException")
    void getSubjectList_webServiceException_throwsSendContractServerException() {
        Long contractId = 999L;
        when(clientUsr.getContractSubject(contractId, 0L))
                .thenThrow(new org.springframework.ws.client.WebServiceTransportException("Error"));

        assertThrows(SendContractServerException.class, () -> requestFactoryStub.getSubjectList(contractId));
    }

    @Test
    @DisplayName("getHid: успешное получение hid из mailingClient")
    void getHid_success() {
        RSaleContract contract = new RSaleContract();
        contract.setContractId(999L);
        contract.setSubjectId(777L);
        MailingContractResponse response = new MailingContractResponse();
        response.setPartyHid(888L);
        when(mailingClient.getInfo(999L)).thenReturn(response);

        String hid = requestFactoryStub.getHid(contract);
        assertEquals("888", hid);
    }

    @Test
    @DisplayName("getHid: hid null возвращает subjectId")
    void getHid_nullHid() {
        RSaleContract contract = new RSaleContract();
        contract.setContractId(999L);
        contract.setSubjectId(777L);
        MailingContractResponse response = new MailingContractResponse();
        response.setPartyHid(null);
        when(mailingClient.getInfo(999L)).thenReturn(response);

        String hid = requestFactoryStub.getHid(contract);
        assertEquals("777", hid);
    }

    @Test
    @DisplayName("getHid: исключение при вызове mailingClient возвращает subjectId")
    void getHid_exception() {
        RSaleContract contract = new RSaleContract();
        contract.setContractId(999L);
        contract.setSubjectId(777L);
        when(mailingClient.getInfo(999L)).thenThrow(new RuntimeException("Network error"));

        String hid = requestFactoryStub.getHid(contract);
        assertEquals("777", hid);
    }
}