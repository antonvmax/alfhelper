package ru.alfastrah.site.avto.model.contract.signed.model.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "DirectSendContractSignedResponse",
        description = "Сообщение об успешности отправки письма в altcraft")
public class DirectSendContractSignedResponse {

    public static final String ERROR_MESSAGE = "Письмо не было отправлено на следующие email: ";
    public static final String SUCCESS_MESSAGE = "Письмо успешно отправлено на следующие email: ";

    @Schema(description = "Получилось ли успешно отправить письмо в aktcraft", allowableValues = {"true", "false"})
    private Boolean success;

    @Schema(description = "Сообщение при успешной отправке письма в altcraft", example = SUCCESS_MESSAGE)
    private String message;

    @Schema(description = "Сообщение при возникновении ошибки во время отправки письма в altcraft", example = ERROR_MESSAGE)
    private String error;

    public DirectSendContractSignedResponse(Boolean success, List<String> emails) {
        this.success = success;
        if (success) {
            this.message = SUCCESS_MESSAGE + emails;
        } else {
            this.error = ERROR_MESSAGE + emails;
        }
    }
}
