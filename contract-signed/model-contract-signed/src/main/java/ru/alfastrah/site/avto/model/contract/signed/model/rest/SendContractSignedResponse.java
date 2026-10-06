package ru.alfastrah.site.avto.model.contract.signed.model.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Ответ на запрос отправки подписанного договора по почте")
public class SendContractSignedResponse {
//
//    @Schema(description = "Статус отправки", example = "SUCCESS")
//    private String status;
//
//    @Schema(description = "Идентификатор отправленного сообщения", example = "")
//    private String messageId;
//
//    @Schema(description = "Описание результата отправки", example = "Договор успешно отправлен на почту: client@example.com")
//    private String description;

    @Schema(description = "Флаг успешной отправки", example = "true")
    private boolean result;
}