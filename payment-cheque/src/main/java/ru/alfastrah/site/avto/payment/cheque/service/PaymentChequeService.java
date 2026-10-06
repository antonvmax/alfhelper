package ru.alfastrah.site.avto.payment.cheque.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.payment.cheque.client.internet.contract.InternetContractFeignClient;
import ru.alfastrah.site.avto.payment.cheque.client.partners.interaction.PartnersInteractionClient;
import ru.alfastrah.site.avto.payment.cheque.client.payment.methods.PaymentMethodsFeignClient;
import ru.alfastrah.site.avto.payment.cheque.exception.ReceivingChequeException;
import ru.alfastrah.site.avto.payment.cheque.model.ChequeResponse;
import ru.alfastrah.site.avto.payment.cheque.model.unicus.InternetContractPayment;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentChequeService {
    private final PartnersInteractionClient partnersInteractionClient;
    private final CheckRealContractService checkRealContractService;
    private final InternetContractFeignClient internetContractFeignClient;
    private final PaymentMethodsFeignClient paymentMethodsFeignClient;

    public ChequeResponse getChequeInfo(String upid, String contractId, String mdorder) {
        InternetContractPayment internetContractPayment;
        if (StringUtils.isNotEmpty(mdorder)) {
            internetContractPayment = internetContractFeignClient.getPaymentDictByMdOrder(mdorder);
        } else {
            if (contractId.contains("-")) {
                contractId = checkRealContractService.isRealContract(contractId);
            }
            checkAssociatedUpidWithContract(upid, contractId);
            internetContractPayment = internetContractFeignClient.getPaymentDictByContractId(contractId);
        }

        if (Optional.ofNullable(internetContractPayment).map(InternetContractPayment::getPaymentDictId).isEmpty()) {
            throw new ReceivingChequeException("Указан несуществующий mdorder");
        }

        try {
            return paymentMethodsFeignClient.getCheque(internetContractPayment.getMdOrder(), internetContractPayment.getPaymentDictId());
        } catch (Exception e) {
            log.error("Произошла ошибка при вызове payment-methods: {}", e.getMessage());
            throw new ReceivingChequeException(e.getMessage());
        }
    }

    private void checkAssociatedUpidWithContract(String upid, String contractId) {
        Long contractIdLong = Long.parseLong(contractId);
        if (partnersInteractionClient.searchByUpidAndContractId(upid, contractIdLong) == null) {
            String message = String.format("К UPID %s не привязан контракт %s", upid, contractId);
            throw new ReceivingChequeException(message);
        }
    }
}
