package ru.alfastrah.site.avto.model.internet.contract.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InternetContractRequest(
        @JsonAlias("contractNumber")
        String internetContractNumber,
        Short riskDouble,
        Long contractId,
        Long managerId,
        Short isDeliv,
        Long invoiceId,
        String clientId,
        Long intContractStatusId,
        String pdp,
        String browser,
        String productId,
        String dealerId,
        String mdOrder,
        Long paymentPrimaryRc,
        Long paymentSecondaryRc,
        LocalDateTime changePaymentDate,
        LocalDateTime cancelDate,
        BigDecimal totalSumRur,
        BigDecimal totalRate,
        Long paymentDictId,
        String emoneyDictId,
        LocalDateTime isPaid,
        String platronPgOrderId,
        Long platronPgPaymentId,
        BigDecimal paidAmount,
        LocalDateTime platronIsRefund,
        BigDecimal platronRefundAmount,
        LocalDateTime dateInsert,
        String statementId,
        Long userIdentityCodeId,
        String statementFile
) {
}
