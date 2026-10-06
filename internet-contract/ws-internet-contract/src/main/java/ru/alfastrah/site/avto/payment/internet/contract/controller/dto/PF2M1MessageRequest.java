package ru.alfastrah.site.avto.payment.internet.contract.controller.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PF2M1MessageRequest {
    String orderNumber;
    Long contractId;
    String mdOrder;
    String operation;
    String status;
    /** Статус последней транзакции для loyal_pkg — не имеет отношения к {@link #status} из XML колбэка. */
    Integer statusId;
}
