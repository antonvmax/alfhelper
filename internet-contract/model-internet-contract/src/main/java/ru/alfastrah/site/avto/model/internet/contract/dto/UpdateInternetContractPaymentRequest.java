package ru.alfastrah.site.avto.model.internet.contract.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdateInternetContractPaymentRequest(
        Long contractId,
        String orderId,
        LocalDateTime paidDate
) {
}
