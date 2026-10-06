package ru.alfastrah.site.avto.payment.internet.contract.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractPaymentRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractPaymentService.class})
class InternetContractPaymentServiceTest {
    @Autowired
    InternetContractPaymentService service;

    @MockBean
    InternetContractPaymentRepository internetContractPaymentRepository;
    @MockBean
    ProcedureRepository procedureRepository;
    @MockBean
    InternetContractMirrorService internetContractMirrorService;

    @Test
    void createPayment() {
        InternetContractPaymentRequest request = InternetContractPaymentRequest.builder()
                .contractId(1234567L)
                .mdOrder("MD_ORDER")
                .paidAmount(new BigDecimal("10000.00"))
                .paymentDictId(5L)
                .build();

        when(internetContractMirrorService.mirrorPaymentCreate(request)).thenReturn(99L);

        assertEquals(99L, service.createPayment(request));

        verify(procedureRepository, times(1)).pF2AmountMessage(BigDecimal.valueOf(request.contractId()),
                request.mdOrder(), request.paidAmount(), request.paymentDictId());
    }

    @Test
    void createPayment_whenMirrorThrowsThenNull() {
        InternetContractPaymentRequest request = InternetContractPaymentRequest.builder()
                .contractId(1234567L)
                .mdOrder("MD_ORDER")
                .build();

        when(internetContractMirrorService.mirrorPaymentCreate(request)).thenThrow(new RuntimeException("Error"));

        assertNull(service.createPayment(request));

        verify(procedureRepository, times(1)).pF2AmountMessage(BigDecimal.valueOf(request.contractId()),
                request.mdOrder(), request.paidAmount(), request.paymentDictId());
    }

    @Test
    void findContractIdsByMdOrder() {
        String mdOrder = "MD_ORDER";
        when(internetContractPaymentRepository.findContractIdsByMdOrder(mdOrder)).thenReturn(List.of(1L, 2L));

        assertEquals(List.of(1L, 2L), service.findContractIdsByMdOrder(mdOrder));

        verify(internetContractPaymentRepository, times(1)).findContractIdsByMdOrder(mdOrder);
    }

    @Test
    void updatePayment_throwExceptionWhenUpdate() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID";
        LocalDateTime paidDate = LocalDateTime.now();

        doThrow(new RuntimeException("Error")).when(internetContractPaymentRepository).updateInternetContractPayment(contractId, orderId, paidDate);

        assertDoesNotThrow(() -> service.updatePayment(contractId, orderId, paidDate));

        verify(internetContractPaymentRepository, times(1)).updateInternetContractPayment(contractId, orderId, paidDate);

        String result = service.updatePayment(contractId, orderId, paidDate);
        assertTrue(result.contains("Не удалось обновить internet-contract-payment: "));
    }

    @Test
    void updatePayment() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID";
        LocalDateTime paidDate = LocalDateTime.now();

        assertDoesNotThrow(() -> service.updatePayment(contractId, orderId, paidDate));

        verify(internetContractPaymentRepository, times(1)).updateInternetContractPayment(contractId, orderId, paidDate);

        assertNull(service.updatePayment(contractId, orderId, paidDate));
    }
}
