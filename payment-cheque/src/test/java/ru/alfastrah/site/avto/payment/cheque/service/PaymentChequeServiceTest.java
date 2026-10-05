package ru.alfastrah.site.avto.payment.cheque.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.payment.cheque.client.internet.contract.InternetContractFeignClient;
import ru.alfastrah.site.avto.payment.cheque.client.payment.methods.PaymentMethodsFeignClient;
import ru.alfastrah.site.avto.payment.cheque.model.unicus.InternetContractPayment;
import ru.alfastrah.site.avto.payment.cheque.repositories.PartnerCalculationRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentChequeService.class})
class PaymentChequeServiceTest {
    @Autowired
    PaymentChequeService service;
    @MockBean
    PartnerCalculationRepository partnerCalculationRepository;
    @MockBean
    CheckRealContractService checkRealContractService;
    @MockBean
    InternetContractFeignClient internetContractFeignClient;
    @MockBean
    PaymentMethodsFeignClient paymentMethodsFeignClient;

    @Test
    void throwExceptionMdOrderNotFoundWhenInternetContractClientReturnEmptyResult() {
        when(internetContractFeignClient.getPaymentDictByMdOrder(anyString())).thenReturn(new InternetContractPayment());

        assertThatThrownBy(() -> service.getChequeInfo(null, null, "123")).hasMessageContaining("Указан несуществующий mdorder");
    }

    @Test
    void throwExceptionMdOrderNotFoundWhenInternetContractClientReturnEmptyResultByContractId() {
        when(internetContractFeignClient.getPaymentDictByContractId(anyString())).thenReturn(new InternetContractPayment());
        when(partnerCalculationRepository.isAssociatedUpidWithContract(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.getChequeInfo("123", "123", null)).hasMessageContaining("Указан несуществующий mdorder");
    }

    @Test
    void throwExceptionMdOrderNotAssociatedWithContract() {
        when(internetContractFeignClient.getPaymentDictByContractId(anyString())).thenReturn(new InternetContractPayment());
        when(partnerCalculationRepository.isAssociatedUpidWithContract(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.getChequeInfo("1234", "123", null)).hasMessageContaining("К UPID 1234 не привязан контракт 123");
    }

    @Test
    void doesNotThrowWithFakeContractIdWhenItIsPaid() {
        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("6");
        internetContractPayment.setMdOrder("123");

        when(checkRealContractService.isRealContract(anyString())).thenReturn("123");
        when(internetContractFeignClient.getPaymentDictByContractId(anyString())).thenReturn(internetContractPayment);
        when(partnerCalculationRepository.isAssociatedUpidWithContract(anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> service.getChequeInfo("1234", "-123", null));
    }

    @Test
    void throwExceptionWhenPaymentMethodsIdNotAvailable() {
        InternetContractPayment internetContractPayment = new InternetContractPayment();
        internetContractPayment.setPaymentDictId("6");
        internetContractPayment.setMdOrder("123");

        when(checkRealContractService.isRealContract(anyString())).thenReturn("123");
        when(internetContractFeignClient.getPaymentDictByContractId(anyString())).thenReturn(internetContractPayment);
        when(partnerCalculationRepository.isAssociatedUpidWithContract(anyString(), anyString())).thenReturn(true);
        when(paymentMethodsFeignClient.getCheque(anyString(), anyString())).thenThrow(new RuntimeException("Ошибка при вызове сервиса paymentMethods"));

        assertThatThrownBy(() -> service.getChequeInfo("1234", "-123", null)).hasMessageContaining("Ошибка при вызове сервиса paymentMethods");
    }
}