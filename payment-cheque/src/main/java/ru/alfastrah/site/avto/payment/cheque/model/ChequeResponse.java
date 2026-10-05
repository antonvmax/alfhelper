package ru.alfastrah.site.avto.payment.cheque.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChequeResponse {
    private String mdOrder;
    private BigDecimal amountTotal;
    private String paymentName;
    private String ofdReceiptUrl;
    private LocalDateTime receiptDate;
    private String message;
}
