package ru.alfastrah.site.avto.payment.internet.contract.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetContractStatusRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetPaymentDictRequest;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractService.class})
class InternetContractServiceTest {
    @Autowired
    InternetContractService service;

    @MockBean
    InternetContractRepository internetContractRepository;
    @MockBean
    ProcedureRepository procedureRepository;
    @MockBean
    InternetContractMirrorService internetContractMirrorService;

    @Test
    void createContract() {
        InternetContractRequest request = InternetContractRequest.builder()
                .contractId(1234L)
                .internetContractNumber("CONTRACT_NUMBER")
                .dealerId("DEALER_ID")
                .productId("PRODUCT_ID")
                .build();

        assertDoesNotThrow(() -> service.createContract(request));

        verify(procedureRepository, times(1)).createInternetContract(
                request.contractId(), request.internetContractNumber(), request.dealerId(), request.productId());
        verify(internetContractMirrorService, times(1)).mirrorContractCreate(request);
    }

    @Test
    void createContract_whenMirrorThrowsThenNoError() {
        InternetContractRequest request = InternetContractRequest.builder()
                .contractId(1234L)
                .build();

        doThrow(new RuntimeException("Error")).when(internetContractMirrorService).mirrorContractCreate(request);

        assertDoesNotThrow(() -> service.createContract(request));

        verify(procedureRepository, times(1)).createInternetContract(
                request.contractId(), request.internetContractNumber(), request.dealerId(), request.productId());
    }

    @Test
    void updateContract_throwExceptionWhenUpdate() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID";
        LocalDateTime paidDate = LocalDateTime.now();

        doThrow(new RuntimeException("Error")).when(internetContractRepository).updateInternetContract(contractId, orderId, paidDate);

        assertDoesNotThrow(() -> service.updateContract(contractId, orderId, paidDate));

        verify(internetContractRepository, times(1)).updateInternetContract(contractId, orderId, paidDate);

        String result = service.updateContract(contractId, orderId, paidDate);
        assertTrue(result.contains("Не удалось обновить internet-contract: "));
    }

    @Test
    void updateContract() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID";
        LocalDateTime paidDate = LocalDateTime.now();

        assertDoesNotThrow(() -> service.updateContract(contractId, orderId, paidDate));

        verify(internetContractRepository, times(1)).updateInternetContract(contractId, orderId, paidDate);

        assertNull(service.updateContract(contractId, orderId, paidDate));
    }

    @Test
    void cancelContract() {
        Long contractId = 1234567L;

        assertNull(service.cancelContract(contractId));

        verify(internetContractRepository, times(1)).updateCancelDate(contractId);
        verify(internetContractMirrorService, times(1)).mirrorCancelDate(contractId);
    }

    @Test
    void cancelContract_throwExceptionWhenUpdate() {
        Long contractId = 1234567L;

        doThrow(new RuntimeException("Error")).when(internetContractRepository).updateCancelDate(contractId);

        String result = service.cancelContract(contractId);

        assertTrue(result.contains("Не удалось проставить дату отмены internet-contract: "));
        verify(internetContractMirrorService, times(0)).mirrorCancelDate(contractId);
    }

    @Test
    void refundContract() {
        Long contractId = 1234567L;
        String mdOrder = "MD_ORDER";

        assertNull(service.refundContract(contractId, mdOrder));

        verify(internetContractRepository, times(1)).updateRefund(contractId, mdOrder);
        verify(internetContractMirrorService, times(1)).mirrorRefund(contractId, mdOrder);
    }

    @Test
    void refundContract_throwExceptionWhenUpdate() {
        Long contractId = 1234567L;
        String mdOrder = "MD_ORDER";

        doThrow(new RuntimeException("Error")).when(internetContractRepository).updateRefund(contractId, mdOrder);

        String result = service.refundContract(contractId, mdOrder);

        assertTrue(result.contains("Не удалось проставить возврат internet-contract: "));
        verify(internetContractMirrorService, times(0)).mirrorRefund(contractId, mdOrder);
    }

    @Test
    void setPaymentDict() {
        SetPaymentDictRequest request = SetPaymentDictRequest.builder()
                .contractId(1234567L)
                .mdOrder("MD_ORDER")
                .paymentDictId(5L)
                .build();

        assertNull(service.setPaymentDict(request));

        verify(procedureRepository, times(1)).setPaymentDict(request.contractId(), request.mdOrder(), request.paymentDictId());
        verify(internetContractMirrorService, times(1)).mirrorPaymentDict(request.contractId(), request.paymentDictId());
    }

    @Test
    void setPaymentDict_throwExceptionWhenProcedureCall() {
        SetPaymentDictRequest request = SetPaymentDictRequest.builder()
                .contractId(1234567L)
                .mdOrder("MD_ORDER")
                .paymentDictId(5L)
                .build();

        doThrow(new RuntimeException("Error")).when(procedureRepository)
                .setPaymentDict(request.contractId(), request.mdOrder(), request.paymentDictId());

        String result = service.setPaymentDict(request);

        assertTrue(result.contains("Не удалось установить платежную систему: "));
        verify(internetContractMirrorService, times(0)).mirrorPaymentDict(request.contractId(), request.paymentDictId());
    }

    @Test
    void setContractStatus() {
        SetContractStatusRequest request = SetContractStatusRequest.builder()
                .contractId(1234567L)
                .statusTypeId(5)
                .build();

        assertNull(service.setContractStatus(request));

        verify(procedureRepository, times(1)).setContractStatus(request.contractId(), request.statusTypeId());
    }

    @Test
    void setContractStatus_throwExceptionWhenProcedureCall() {
        SetContractStatusRequest request = SetContractStatusRequest.builder()
                .contractId(1234567L)
                .statusTypeId(5)
                .build();

        doThrow(new RuntimeException("Error")).when(procedureRepository)
                .setContractStatus(request.contractId(), request.statusTypeId());

        String result = service.setContractStatus(request);

        assertTrue(result.contains("Не удалось установить статус договора: "));
    }

    @Test
    void countAdditionals_withExceptionFromRepo() {
        Long contractId = 1234567L;
        when(internetContractRepository.countAdditionals(contractId)).thenThrow(new RuntimeException());

        assertDoesNotThrow(() -> service.countAdditionals(contractId));

        verify(internetContractRepository, times(1)).countAdditionals(contractId);
        assertNull(service.countAdditionals(contractId));
    }

    @Test
    void countAdditionals() {
        Long contractId = 1234567L;
        Long result = 5L;
        when(internetContractRepository.countAdditionals(contractId)).thenReturn(result);

        assertDoesNotThrow(() -> service.countAdditionals(contractId));

        verify(internetContractRepository, times(1)).countAdditionals(contractId);
        assertEquals(result, service.countAdditionals(contractId));
    }
}
