package ru.alfastrah.site.avto.payment.internet.contract.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContract;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;
import ru.alfastrah.site.avto.payment.internet.contract.mapper.InternetContractDetailsMapper;
import ru.alfastrah.site.avto.payment.internet.contract.mapper.InternetContractDetailsMapperImpl;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractPaymentRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractDetailsService.class, InternetContractDetailsMapperImpl.class})
class InternetContractDetailsServiceTest {
    @Autowired
    InternetContractDetailsService service;
    @Autowired
    InternetContractDetailsMapper internetContractDetailsMapper;

    @MockBean
    InternetContractRepository internetContractRepository;
    @MockBean
    InternetContractPaymentRepository internetContractPaymentRepository;
    @MockBean
    InternetContractMirrorService internetContractMirrorService;

    @Test
    void findByContractId_whenFindByContractIdInContractPayment() {
        String contractId = "12345";

        InternetContract internetContract = new InternetContract();
        internetContract.setMdOrder("MD_ORDER");
        internetContract.setPaymentDictId("PAYMENT_DICT_ID");
        internetContract.setPaid(LocalDateTime.now());

        when(internetContractRepository.findByContractId(contractId)).thenReturn(internetContract);

        assertDoesNotThrow(() -> service.findByContractId(contractId));

        verify(internetContractRepository, times(1)).findByContractId(contractId);
        verify(internetContractRepository, never()).findByMdOrder(any());

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(internetContract.getPaymentDictId(), result.paymentDictId());
        assertEquals(internetContract.getMdOrder(), result.mdOrder());
        assertEquals(internetContract.getPaid(), result.paid());
    }

    @Test
    void findByContractId_whenInternetContractReturnNullMdOrder() {
        String contractId = "12345";

        InternetContract internetContract = new InternetContract();
        internetContract.setPaymentDictId("PAYMENT_DICT_ID");
        internetContract.setPaid(LocalDateTime.now());

        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("PAYMENT_DICT_ID");
        internetContractPayment.setMdOrder("MD_ORDER");
        internetContractPayment.setPaid(LocalDateTime.now());

        when(internetContractRepository.findByContractId(contractId)).thenReturn(internetContract);
        when(internetContractPaymentRepository.findByContractId(contractId)).thenReturn(internetContractPayment);

        assertDoesNotThrow(() -> service.findByContractId(contractId));

        verify(internetContractRepository, times(1)).findByContractId(contractId);
        verify(internetContractPaymentRepository, times(1)).findByContractId(contractId);

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(internetContractPayment.getPaymentDictId(), result.paymentDictId());
        assertEquals(internetContractPayment.getMdOrder(), result.mdOrder());
        assertEquals(internetContractPayment.getPaid(), result.paid());
    }

    @Test
    void findByContractId_whenInternetContractReturnNull() {
        String contractId = "12345";

        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("PAYMENT_DICT_ID");
        internetContractPayment.setMdOrder("MD_ORDER");
        internetContractPayment.setPaid(LocalDateTime.now());

        when(internetContractRepository.findByContractId(contractId)).thenReturn(null);
        when(internetContractPaymentRepository.findByContractId(contractId)).thenReturn(internetContractPayment);

        assertDoesNotThrow(() -> service.findByContractId(contractId));

        verify(internetContractRepository, times(1)).findByContractId(contractId);
        verify(internetContractPaymentRepository, times(1)).findByContractId(contractId);

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(internetContractPayment.getPaymentDictId(), result.paymentDictId());
        assertEquals(internetContractPayment.getMdOrder(), result.mdOrder());
        assertEquals(internetContractPayment.getPaid(), result.paid());
    }

    @Test
    void findByContractId_whenContractPaymentAndInternetContractReturnNull() {
        String contractId = "12345";

        when(internetContractRepository.findByContractId(contractId)).thenReturn(null);
        when(internetContractPaymentRepository.findByContractId(contractId)).thenReturn(null);

        assertDoesNotThrow(() -> service.findByContractId(contractId));

        verify(internetContractRepository, times(1)).findByContractId(contractId);
        verify(internetContractPaymentRepository, times(1)).findByContractId(contractId);

        InternetContractDetails result = service.findByContractId(contractId);

        assertNull(result);
    }

    @Test
    void findByContractId_whenFoundInPostgresContract() {
        String contractId = "12345";

        InternetContractDetails postgresDetails = InternetContractDetails.builder()
                .paymentDictId("1")
                .mdOrder("MD_ORDER")
                .paid(LocalDateTime.now())
                .paidAmount(new BigDecimal("10000.00"))
                .build();

        when(internetContractMirrorService.findContractByContractId(12345L)).thenReturn(postgresDetails);

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(postgresDetails, result);
        verify(internetContractRepository, never()).findByContractId(any());
        verify(internetContractPaymentRepository, never()).findByContractId(any());
    }

    @Test
    void findByContractId_whenPostgresContractEmptyThenPostgresPayment() {
        String contractId = "12345";

        InternetContractDetails postgresDetails = InternetContractDetails.builder()
                .paymentDictId("1")
                .mdOrder("MD_ORDER")
                .paidAmount(new BigDecimal("10000.00"))
                .build();

        when(internetContractMirrorService.findContractByContractId(12345L)).thenReturn(null);
        when(internetContractRepository.findByContractId(contractId)).thenReturn(null);
        when(internetContractMirrorService.findPaymentByContractId(12345L)).thenReturn(postgresDetails);

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(postgresDetails, result);
        verify(internetContractRepository, times(1)).findByContractId(contractId);
        verify(internetContractPaymentRepository, never()).findByContractId(any());
    }

    @Test
    void findByContractId_whenPostgresReadThrowsThenReadFromOracle() {
        String contractId = "12345";

        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("PAYMENT_DICT_ID");
        internetContractPayment.setMdOrder("MD_ORDER");

        when(internetContractMirrorService.findContractByContractId(12345L))
                .thenThrow(new RuntimeException("Error"));
        when(internetContractMirrorService.findPaymentByContractId(12345L))
                .thenThrow(new RuntimeException("Error"));
        when(internetContractRepository.findByContractId(contractId)).thenReturn(null);
        when(internetContractPaymentRepository.findByContractId(contractId)).thenReturn(internetContractPayment);

        assertDoesNotThrow(() -> service.findByContractId(contractId));

        InternetContractDetails result = service.findByContractId(contractId);

        assertEquals(internetContractPayment.getMdOrder(), result.mdOrder());
    }

    @Test
    void findByContractId_whenContractIdIsNotNumberThenPostgresSkipped() {
        String contractId = "NOT_A_NUMBER";

        when(internetContractRepository.findByContractId(contractId)).thenReturn(null);
        when(internetContractPaymentRepository.findByContractId(contractId)).thenReturn(null);

        assertNull(service.findByContractId(contractId));

        verify(internetContractMirrorService, never()).findContractByContractId(any());
        verify(internetContractMirrorService, never()).findPaymentByContractId(any());
    }

    @Test
    void findByMdOrder_whenFindByContractIdInContractPayment() {
        String mdOrder = "MD_ORDER";

        InternetContract internetContract = new InternetContract();
        internetContract.setMdOrder("MD_ORDER");
        internetContract.setPaymentDictId("PAYMENT_DICT_ID");
        internetContract.setPaid(LocalDateTime.now());

        when(internetContractRepository.findByMdOrder(mdOrder)).thenReturn(internetContract);

        assertDoesNotThrow(() -> service.findByMdOrder(mdOrder));

        verify(internetContractRepository, times(1)).findByMdOrder(mdOrder);

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertEquals(internetContract.getPaymentDictId(), result.paymentDictId());
        assertEquals(internetContract.getMdOrder(), result.mdOrder());
        assertEquals(internetContract.getPaid(), result.paid());
    }

    @Test
    void findByMdOrder_whenInternetContractReturnNull() {
        String mdOrder = "MD_ORDER";

        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("PAYMENT_DICT_ID");
        internetContractPayment.setMdOrder("MD_ORDER");
        internetContractPayment.setPaid(LocalDateTime.now());

        when(internetContractRepository.findByMdOrder(mdOrder)).thenReturn(null);
        when(internetContractPaymentRepository.findByMdOrder(mdOrder)).thenReturn(internetContractPayment);

        assertDoesNotThrow(() -> service.findByMdOrder(mdOrder));

        verify(internetContractRepository, times(1)).findByMdOrder(mdOrder);
        verify(internetContractPaymentRepository, times(1)).findByMdOrder(mdOrder);

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertEquals(internetContractPayment.getPaymentDictId(), result.paymentDictId());
        assertEquals(internetContractPayment.getMdOrder(), result.mdOrder());
        assertEquals(internetContractPayment.getPaid(), result.paid());
    }

    @Test
    void findByMdOrder_whenContractPaymentAndInternetContractReturnNull() {
        String mdOrder = "MD_ORDER";

        when(internetContractRepository.findByMdOrder(mdOrder)).thenReturn(null);
        when(internetContractPaymentRepository.findByMdOrder(mdOrder)).thenReturn(null);

        assertDoesNotThrow(() -> service.findByMdOrder(mdOrder));

        verify(internetContractRepository, times(1)).findByMdOrder(mdOrder);
        verify(internetContractPaymentRepository, times(1)).findByMdOrder(mdOrder);

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertNull(result);
    }

    @Test
    void findByMdOrder_whenFoundInPostgresContract() {
        String mdOrder = "MD_ORDER";

        InternetContractDetails postgresDetails = InternetContractDetails.builder()
                .paymentDictId("1")
                .mdOrder(mdOrder)
                .paid(LocalDateTime.now())
                .paidAmount(new BigDecimal("10000.00"))
                .build();

        when(internetContractMirrorService.findContractByMdOrder(mdOrder)).thenReturn(postgresDetails);

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertEquals(postgresDetails, result);
        verify(internetContractRepository, never()).findByMdOrder(any());
        verify(internetContractPaymentRepository, never()).findByMdOrder(any());
    }

    @Test
    void findByMdOrder_whenPostgresContractEmptyThenPostgresPayment() {
        String mdOrder = "MD_ORDER";

        InternetContractDetails postgresDetails = InternetContractDetails.builder()
                .paymentDictId("1")
                .mdOrder(mdOrder)
                .paidAmount(new BigDecimal("10000.00"))
                .build();

        when(internetContractMirrorService.findContractByMdOrder(mdOrder)).thenReturn(null);
        when(internetContractRepository.findByMdOrder(mdOrder)).thenReturn(null);
        when(internetContractMirrorService.findPaymentByMdOrder(mdOrder)).thenReturn(postgresDetails);

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertEquals(postgresDetails, result);
        verify(internetContractRepository, times(1)).findByMdOrder(mdOrder);
        verify(internetContractPaymentRepository, never()).findByMdOrder(any());
    }

    @Test
    void findByMdOrder_whenPostgresReadThrowsThenReadFromOracle() {
        String mdOrder = "MD_ORDER";

        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("PAYMENT_DICT_ID");
        internetContractPayment.setMdOrder(mdOrder);

        when(internetContractMirrorService.findContractByMdOrder(mdOrder))
                .thenThrow(new RuntimeException("Error"));
        when(internetContractMirrorService.findPaymentByMdOrder(mdOrder))
                .thenThrow(new RuntimeException("Error"));
        when(internetContractRepository.findByMdOrder(mdOrder)).thenReturn(null);
        when(internetContractPaymentRepository.findByMdOrder(mdOrder)).thenReturn(internetContractPayment);

        assertDoesNotThrow(() -> service.findByMdOrder(mdOrder));

        InternetContractDetails result = service.findByMdOrder(mdOrder);

        assertEquals(internetContractPayment.getMdOrder(), result.mdOrder());
    }
}
