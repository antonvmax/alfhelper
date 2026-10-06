package ru.alfastrah.site.avto.payment.internet.contract.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PF2AmountMessage {
    private BigDecimal contractId;
    private String mdOrder;
    private BigDecimal amount;
    private Long paymentDictId;
    private String error;
}
