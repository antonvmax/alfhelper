package ru.alfastrah.site.avto.model.internet.contract.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InternetContractDetails(
        String paymentDictId,
        String mdOrder,
        LocalDateTime paid,
        BigDecimal paidAmount
) {
}
