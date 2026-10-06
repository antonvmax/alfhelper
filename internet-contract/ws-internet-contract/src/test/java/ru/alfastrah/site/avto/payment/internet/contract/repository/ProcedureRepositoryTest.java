package ru.alfastrah.site.avto.payment.internet.contract.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProcedureRepository.class})
class ProcedureRepositoryTest {
    @Autowired
    ProcedureRepository repository;

    @MockBean(name = "pF2AmountMessageJdbcCall")
    SimpleJdbcCall pF2AmountMessageJdbcCall;

    @MockBean(name = "pF2M1MessageJdbcCall")
    SimpleJdbcCall pF2M1MessageJdbcCall;

    @MockBean(name = "f2m1MessageWithoutEmailJdbcCall")
    SimpleJdbcCall f2m1MessageWithoutEmailJdbcCall;

    @MockBean(name = "setPaymentDictJdbcCall")
    SimpleJdbcCall setPaymentDictJdbcCall;

    @MockBean(name = "updateStatusLastTransactJdbcCall")
    SimpleJdbcCall updateStatusLastTransactJdbcCall;

    @MockBean(name = "setContractStatusJdbcCall")
    SimpleJdbcCall setContractStatusJdbcCall;

    @MockBean(name = "fixInternetSaleJdbcCall")
    SimpleJdbcCall fixInternetSaleJdbcCall;

    @Test
    void pF2AmountMessage_whenProcedureReturnsError_returnsErrorMessage() {
        // Given
        PF2AmountMessage entity = new PF2AmountMessage();
        entity.setContractId(BigDecimal.valueOf(456));
        entity.setMdOrder("ORDER-002");
        entity.setAmount(BigDecimal.valueOf(2000));
        entity.setPaymentDictId(8888L);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_error", "CONTRACT_NOT_FOUND");

        when(pF2AmountMessageJdbcCall.execute(anyMap()))
                .thenReturn(mockResult);

        // When
        String result = repository.pF2AmountMessage(entity);

        // Then
        assertEquals("CONTRACT_NOT_FOUND", result);
    }

    @Test
    void pF2AmountMessage_whenProcedureReturnsEmptyError_returnsSuccess() {
        // Given
        PF2AmountMessage entity = new PF2AmountMessage();
        entity.setContractId(BigDecimal.ONE);
        entity.setMdOrder("ORDER-003");
        entity.setAmount(BigDecimal.TEN);
        entity.setPaymentDictId(111L);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_error", ""); // Пустая строка

        when(pF2AmountMessageJdbcCall.execute(anyMap()))
                .thenReturn(mockResult);

        // When
        String result = repository.pF2AmountMessage(entity);

        // Then
        assertEquals("success", result);
    }

    @Test
    void pF2AmountMessage_whenResultMissingErrorKey_returnsSuccess() {
        // Given
        PF2AmountMessage entity = new PF2AmountMessage();
        entity.setContractId(BigDecimal.valueOf(789));
        entity.setMdOrder("ORDER-004");
        entity.setAmount(BigDecimal.valueOf(500));
        entity.setPaymentDictId(222L);

        Map<String, Object> mockResult = new HashMap<>();
        // Ключ "p_error" отсутствует

        when(pF2AmountMessageJdbcCall.execute(anyMap()))
                .thenReturn(mockResult);

        // When
        String result = repository.pF2AmountMessage(entity);

        // Then
        assertEquals("success", result);
    }

    @Test
    void pF2M1Message_whenSuccessFalse_returnsErrorMessage() {
        // Given
        BigDecimal contractId = BigDecimal.valueOf(222);
        String mdOrder = "MD-ORDER-222";
        String xml = "<request><invalid/></request>";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_success", "false");
        mockResult.put("p_message", "XML_VALIDATION_ERROR");

        when(pF2M1MessageJdbcCall.execute(anyMap())).thenReturn(mockResult);

        // When
        String result = repository.pF2M1Message(contractId, mdOrder, xml);

        // Then
        assertEquals("XML_VALIDATION_ERROR", result);
    }

    @Test
    void pF2M1Message_whenSuccessNotTrue_returnsMessageOrDefault() {
        // Given
        BigDecimal contractId = BigDecimal.valueOf(333);
        String mdOrder = "MD-ORDER-333";
        String xml = "<request/>";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_success", "false");

        when(pF2M1MessageJdbcCall.execute(anyMap())).thenReturn(mockResult);

        // When
        String result = repository.pF2M1Message(contractId, mdOrder, xml);

        // Then
        assertNull(result);
    }

    @Test
    void pF2M1Message_whenSuccessTrue() {
        // Given
        BigDecimal contractId = BigDecimal.valueOf(333);
        String mdOrder = "MD-ORDER-333";
        String xml = "<request/>";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_success", "true");

        when(pF2M1MessageJdbcCall.execute(anyMap())).thenReturn(mockResult);

        // When
        String result = repository.pF2M1Message(contractId, mdOrder, xml);

        // Then
        assertEquals("success", result);
    }

    @Test
    void f2m1MessageWithoutEmail_whenSuccessNotTrue_returnsMessageOrDefault() {
        // Given
        BigDecimal contractId = BigDecimal.valueOf(333);
        String mdOrder = "MD-ORDER-333";
        String xml = "<request/>";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_success", "false");
        mockResult.put("p_message", "XML_VALIDATION_ERROR");


        when(f2m1MessageWithoutEmailJdbcCall.execute(anyMap())).thenReturn(mockResult);

        // When
        String result = repository.f2m1MessageWithoutEmail(contractId, mdOrder, xml);

        // Then
        assertEquals("XML_VALIDATION_ERROR", result);
    }

    @Test
    void f2m1MessageWithoutEmail_whenSuccessTrue() {
        // Given
        BigDecimal contractId = BigDecimal.valueOf(333);
        String mdOrder = "MD-ORDER-333";
        String xml = "<request/>";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("p_success", "true");

        when(f2m1MessageWithoutEmailJdbcCall.execute(anyMap())).thenReturn(mockResult);

        // When
        String result = repository.f2m1MessageWithoutEmail(contractId, mdOrder, xml);

        // Then
        assertEquals("success", result);
    }


    @Test
    void setPaymentDict_passesProcedureParams() {
        // Given
        Long contractId = 555666L;
        String mdOrder = "MD-ORDER-555";
        Long paymentDictId = 7L;

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        assertDoesNotThrow(() -> repository.setPaymentDict(contractId, mdOrder, paymentDictId));

        verify(setPaymentDictJdbcCall).execute(paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(BigDecimal.valueOf(contractId), capturedParams.get("p_contract_id"));
        assertEquals(mdOrder, capturedParams.get("p_order"));
        assertEquals(BigDecimal.valueOf(paymentDictId), capturedParams.get("p_payment_id"));
    }

    @Test
    void updateStatusLastTransact_passesProcedureParams() {
        // Given
        Long contractId = 777888L;
        Integer statusId = 3;

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        assertDoesNotThrow(() -> repository.updateStatusLastTransact(contractId, statusId));

        verify(updateStatusLastTransactJdbcCall).execute(paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(BigDecimal.valueOf(contractId), capturedParams.get("p_contract_id"));
        assertEquals(statusId, capturedParams.get("p_status_id"));
    }

    @Test
    void setContractStatus_passesProcedureParams() {
        // Given
        Long contractId = 999111L;
        Integer newStatusTypeId = 6;

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        assertDoesNotThrow(() -> repository.setContractStatus(contractId, newStatusTypeId));

        verify(setContractStatusJdbcCall).execute(paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(BigDecimal.valueOf(contractId), capturedParams.get("p_contract_id"));
        assertEquals(newStatusTypeId, capturedParams.get("p_new_stype_id"));
    }

    @Test
    void createInternetContract_whenSuccessNotTrue_returnsMessageOrDefault() {
        // Given
        Long contractId = 333444L;
        String contractNumber = "CONTRACT_NUMBER-333";
        String dealerId = "DEALER_ID";
        String productId = "PRODUCT_UD";

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        assertDoesNotThrow(() -> repository.createInternetContract(contractId, contractNumber, dealerId, productId));

        verify(fixInternetSaleJdbcCall).execute(paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(BigDecimal.valueOf(contractId), capturedParams.get("p_contract_id"));
        assertEquals(contractNumber, capturedParams.get("p_contract_number"));
        assertEquals(dealerId, capturedParams.get("p_dealer_id"));
        assertEquals(productId, capturedParams.get("p_product_id"));
    }
}