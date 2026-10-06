package ru.alfastrah.site.avto.payment.internet.contract.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InternetContractPayment {

    String paymentDictId;

    String mdOrder;

    /** Дата оплаты — в строке платежа это date_response, аналог is_paid в самом договоре. */
    LocalDateTime paid;

    BigDecimal paidAmount;
}
