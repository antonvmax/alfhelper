package ru.alfastrah.site.avto.ws.contact.signed.model.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "SendingPersonalPolicyEmailResponse",
        description = "Возвращает информацию получилось ли отправить письмо"
)
public class SendingPersonalPolicyEmailResponse {

    @Schema(description = "Успешность отправки письма", allowableValues = {"true", "false"})
    private Boolean success;
    @Schema(description = "Сообщение о завершении операции", example = "Successful operation")
    private String text;
}
