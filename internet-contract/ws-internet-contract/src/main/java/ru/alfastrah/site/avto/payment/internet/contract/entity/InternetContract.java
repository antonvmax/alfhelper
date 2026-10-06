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
public class InternetContract {

    String paymentDictId;

    String mdOrder;

    LocalDateTime paid;

    BigDecimal paidAmount;
}
