package ru.alfastrah.site.avto.ws.contact.signed.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Тип печатной формы")
public enum PrintFormType {
    @Schema(description = "Уведомление о заключении ОСАГО")
    NOTIFICATION_OSAGO("917"),

    @Schema(description = "Заявление на ОСАГО")
    STATEMENT_OSAGO("716"),

    @Schema(description = "Тип печатной формы по умолчанию")
    DEFAULT("-1");

    @Getter
    @Setter
    private String unicusPrintFormId;

    PrintFormType(String unicusPrintFormId) {
        this.unicusPrintFormId = unicusPrintFormId;
    }
}
