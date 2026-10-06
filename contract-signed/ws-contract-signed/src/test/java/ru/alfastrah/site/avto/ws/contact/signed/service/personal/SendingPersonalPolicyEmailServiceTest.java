package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.soap.client.SoapFaultClientException;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.impl.AltcraftEmailClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailResponse;
import ru.alfastrah.site.avto.model.contract.signed.exception.InvalidPrintFormException;
import ru.alfastrah.site.avto.model.contract.signed.exception.InvalidRequestException;
import ru.alfastrah.site.avto.ws.contact.signed.utils.FIOParser;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendingPersonalPolicyEmailServiceTest {

    @Mock
    private GetPolicyBase64 mockGetPolicyBase64;
    @Mock
    private BusAsContractService mockAsContractService;
    @Mock
    private AltcraftEmailClient mockAltcraftEmailClient;
    @Mock
    private UnicusUsrService mockClientUsr;
    @Mock
    private UnicusSubjectService mockClientSubject;
    @Mock
    private FIOParser fioParser;

    private SendingPersonalPolicyEmailService sendingPersonalPolicyEmailServiceUnderTest;

    @BeforeEach
    void setUp() {
        sendingPersonalPolicyEmailServiceUnderTest = new SendingPersonalPolicyEmailService(mockGetPolicyBase64,
                mockAsContractService, mockAltcraftEmailClient, mockClientUsr, mockClientSubject, fioParser);
    }

    @Test
    void testSendingPolicy() {
        Long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setSubjectId(555);

        RPhysicalPerson physicalPerson = new RPhysicalPerson();
        physicalPerson.setFirstName("first");
        physicalPerson.setLastName("last");
        physicalPerson.setMiddleName("middle");
        physicalPerson.setBirthDate(LocalDate.now());

        Response expected = new Response();
        expected.setError(0);

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(contractId)).thenReturn(rSaleContract);
        when(mockClientSubject.getPhysicalPerson(rSaleContract.getSubjectId())).thenReturn(physicalPerson);
        when(mockAltcraftEmailClient.sendEmail(any())).thenReturn(expected);

        SendingPersonalPolicyEmailResponse result = sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request);

        assertNotNull(result);
        verify(mockAsContractService, times(1)).getContractId(request);
        verify(mockGetPolicyBase64, times(1)).getPolicyByContractId(contractId, request);
        verify(mockClientUsr, times(1)).getContract(contractId);
        verify(mockClientSubject, times(1)).getPhysicalPerson(rSaleContract.getSubjectId());
        verify(mockAltcraftEmailClient, times(1)).sendEmail(any());
        assertEquals(new SendingPersonalPolicyEmailResponse(true, "Successful operation"), result);
    }

    @Test
    void testSendingPolicy_unicus_exception_SoapFaultClientException() {
        long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(any())).thenThrow(SoapFaultClientException.class);

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(SendContractServerException.class);
        assertThrows(SendContractServerException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
    }

    @Test
    void testSendingPolicy_unicus_exception_EOsagoSaveException() {
        long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(any())).thenReturn(null);

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(EOsagoSaveException.class);
        EOsagoSaveException exception = assertThrows(EOsagoSaveException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
        assertEquals("Контракт не найден. contractId=" + contractId, exception.getMessage());
    }

    @Test
    void testSendingPolicy_altcraft_error() {
        Long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setSubjectId(555);

        RPhysicalPerson physicalPerson = new RPhysicalPerson();
        physicalPerson.setFirstName("first");
        physicalPerson.setLastName("last");
        physicalPerson.setMiddleName("middle");
        physicalPerson.setBirthDate(LocalDate.now());

        Response expected = new Response();
        expected.setError(1);

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(contractId)).thenReturn(rSaleContract);
        when(mockClientSubject.getPhysicalPerson(rSaleContract.getSubjectId())).thenReturn(physicalPerson);
        when(mockAltcraftEmailClient.sendEmail(any())).thenReturn(expected);

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(InvalidRequestException.class);
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
        assertEquals("Invalid request", exception.getMessage());
    }

    @Test
    void testSendingPolicy_altcraft_exception() {
        Long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setSubjectId(555);

        RPhysicalPerson physicalPerson = new RPhysicalPerson();
        physicalPerson.setFirstName("first");
        physicalPerson.setLastName("last");
        physicalPerson.setMiddleName("middle");
        physicalPerson.setBirthDate(LocalDate.now());

        Response expected = new Response();
        expected.setError(1);

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(contractId)).thenReturn(rSaleContract);
        when(mockClientSubject.getPhysicalPerson(rSaleContract.getSubjectId())).thenReturn(physicalPerson);
        when(mockAltcraftEmailClient.sendEmail(any())).thenThrow(InvalidRequestException.class);

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(InvalidRequestException.class);
        assertThrows(InvalidRequestException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
    }

    @Test
    void testSendingPolicy_exception() {
        long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("");

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(InvalidPrintFormException.class);
        InvalidPrintFormException exception = assertThrows(InvalidPrintFormException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
        assertEquals("Invalid PrintForm", exception.getMessage());
    }

    @Test
    void testSendingPolicy_clientSubject_exception() {
        Long contractId = 321L;
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        RSaleContract rSaleContract = new RSaleContract();
        rSaleContract.setSubjectId(555);

        when(mockAsContractService.getContractId(request)).thenReturn(BigInteger.valueOf(contractId));
        when(mockGetPolicyBase64.getPolicyByContractId(contractId, request)).thenReturn("policyBase64");
        when(mockClientUsr.getContract(contractId)).thenReturn(rSaleContract);
        when(mockClientSubject.getPhysicalPerson(rSaleContract.getSubjectId())).thenThrow(SoapFaultClientException.class);

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(SendContractServerException.class);
        assertThrows(SendContractServerException.class,
                () -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));
    }

    @Test
    void shouldThrowExceptionWhenNoContractIdInRequestWithAvisSystem() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setSystem("AVIS");

        assertThatThrownBy(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request))
                .isInstanceOf(InvalidRequestException.class).hasMessageContaining("Недостаточно данных. Отсутствует contractId");
    }

    @Test
    void shouldGetContractIdAndFIOFromRequestWhenAvisSystem() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setSystem("AVIS");
        request.setContractId(BigInteger.valueOf(123));
        request.setFio("Алексеенко Алексей Алексеевич");
        request.setContractNumber("number");
        request.setEmail("email@mail.ru");
        request.setPrintedFormId("7800");

        Response responseAltcraft = new Response();
        responseAltcraft.setError(0);

        when(mockGetPolicyBase64.getPolicyByContractId(any(), any())).thenReturn("policyBase64");
        when(mockClientUsr.getContract(any())).thenReturn(new RSaleContract());
        when(mockAltcraftEmailClient.sendEmail(any())).thenReturn(responseAltcraft);
        when(fioParser.parseFIO(any())).thenCallRealMethod();

        assertDoesNotThrow(() -> sendingPersonalPolicyEmailServiceUnderTest.sendingPolicy(request));

        verify(mockAsContractService, times(0)).getContractId(any());
        verify(mockClientSubject, times(0)).getPhysicalPerson(anyLong());
        verify(fioParser, times(1)).parseFIO(any());
    }
}
