package ru.alfastrah.site.avto.model.internet.contract.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InternetContractPaymentRequest(
        Long internetContractId,
        Long contractId,
        String mdOrder,
        Long paymentPrimaryRc,
        Long paymentSecondaryRc,
        LocalDateTime dateRequest,
        LocalDateTime dateResponse,
        BigDecimal paidAmount,
        Long paymentDictId
) {
}
