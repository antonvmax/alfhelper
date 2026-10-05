package ru.alfastrah.site.avto.payment.cheque.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.alfadigital.RealContractResponse;
import ru.alfastrah.site.avto.payment.cheque.client.osago.replace.OsagoReplaceFeignClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CheckRealContractService.class})
class CheckRealContractServiceTest {
    @Autowired
    CheckRealContractService service;
    @MockBean
    OsagoReplaceFeignClient osagoReplaceFeignClient;

    @Test
    void throwExceptionWhenMsOsagoReplaceReturnFakeContractId() {
        RealContractResponse response = new RealContractResponse();
        response.setRealContractId(-123L);
        when(osagoReplaceFeignClient.realContract(anyString())).thenReturn(response);

        assertThatThrownBy(() -> service.isRealContract("-123")).hasMessageContaining("Договор еще не оплачен");
    }

    @Test
    void throwExceptionWhenOsagoReplaceThrowException() {
        when(osagoReplaceFeignClient.realContract(anyString())).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> service.isRealContract("-123")).hasMessageContaining("Договор еще не оплачен");
    }

    @Test
    void notThrowExceptionWhenMsOsagoReplaceReturnRealContractId() {
        RealContractResponse response = new RealContractResponse();
        response.setRealContractId(123L);

        when(osagoReplaceFeignClient.realContract(anyString())).thenReturn(response);

        assertDoesNotThrow(() -> service.isRealContract("-123"));
    }

}