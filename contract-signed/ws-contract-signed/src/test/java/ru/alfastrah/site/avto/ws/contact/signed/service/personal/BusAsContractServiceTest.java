package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.as.contracts.GetContract;
import ru.alfastrah.interplat4.as.contracts.GetContractResponse;
import ru.alfastrah.site.avto.bus.services.AsContractsService;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;

import javax.xml.namespace.QName;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusAsContractServiceTest {

    @Mock
    private AsContractsService mockAsContractsService;

    private BusAsContractService busAsContractServiceUnderTest;

    @BeforeEach
    void setUp() {
        busAsContractServiceUnderTest = new BusAsContractService(mockAsContractsService);
    }

    private final static String AS_FORMAT_DEFAULT = "http://schemas.alfastrah.ru/interplat4/as-contracts-1.0";

    @Test
    void testGetContractId_exception() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        GetContract getContractRequest = new GetContract();
        getContractRequest.setContractNumber(request.getContractNumber());
        getContractRequest.setContractSeria(request.getContractSeria());
        getContractRequest.setFormat(AS_FORMAT_DEFAULT);

        when(mockAsContractsService.getContract(getContractRequest)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> busAsContractServiceUnderTest.getContractId(request))
                .isInstanceOf(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () ->
                busAsContractServiceUnderTest.getContractId(request));
    }

    @Test
    void testGetContractId_notException() {
        SendingPersonalPolicyEmailRequest request = new SendingPersonalPolicyEmailRequest();
        request.setContractNumber("number");
        request.setContractSeria("seria");
        request.setEmail("email");
        request.setSystem("system");
        request.setPrintedFormId("123");

        GetContract getContractRequest = new GetContract();
        getContractRequest.setContractNumber(request.getContractNumber());
        getContractRequest.setContractSeria(request.getContractSeria());
        getContractRequest.setFormat(AS_FORMAT_DEFAULT);

        GetContractResponse expected = new GetContractResponse();
        GetContractResponse.Contract contract = new GetContractResponse.Contract();
        contract.setAny(new JAXBElement<>(new QName(
                "http://schemas.alfastrah.ru/interplat4/as-contracts-1.0", "ContractId"),
                BigInteger.class, BigInteger.TEN));
        expected.setContract(contract);

        when(mockAsContractsService.getContract(getContractRequest)).thenReturn(expected);

        BigInteger result = busAsContractServiceUnderTest.getContractId(request);

        assertNotNull(result);
        verify(mockAsContractsService, times(1)).getContract(getContractRequest);
        assertEquals(result, ((JAXBElement<BigInteger>) expected.getContract().getAny()).getValue());
    }
}
