package ru.alfastrah.site.avto.payment.cheque.model.unicus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InternetContractPayment {
    String paymentDictId;
    String mdOrder;
}
