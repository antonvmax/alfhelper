package ru.alfastrah.site.avto.payment.cheque.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.alfadigital.RealContractResponse;
import ru.alfastrah.site.avto.payment.cheque.client.osago.replace.OsagoReplaceFeignClient;
import ru.alfastrah.site.avto.payment.cheque.exception.ReceivingChequeException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckRealContractService {
    private final OsagoReplaceFeignClient client;

    public String isRealContract(String contractId) {
        try {
            RealContractResponse response = client.realContract(contractId);
            if (response.getRealContractId() < 0 ) {
                throw new ReceivingChequeException("Договор еще не оплачен");
            }
            return String.valueOf(response.getRealContractId());
        } catch (Exception e) {
            log.error("Ошибка при вызове сервиса ms-osago-replace: {}", e.getMessage());
            throw new ReceivingChequeException("Договор еще не оплачен");
        }
    }
}
