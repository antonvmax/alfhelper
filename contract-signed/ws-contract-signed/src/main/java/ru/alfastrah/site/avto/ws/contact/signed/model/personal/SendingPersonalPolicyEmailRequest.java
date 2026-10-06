package ru.alfastrah.site.avto.ws.contact.signed.model.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@Schema(name = "SendingPersonalPolicyEmailRequest")
public class SendingPersonalPolicyEmailRequest {

    @NotBlank(message = "Не передан contractNumber")
    @Schema(description = "Номер договора", example = "0536748201")
    private String contractNumber;
    @Schema(description = "Серия договора", example = "ХХХ")
    private String contractSeria;
    @NotNull(message = "Не передан printedFormId")
    @Schema(description = "Идентификатор печатной формы", example = "525")
    private String printedFormId;
    @Schema(description = "Идентификатор Системы", example = "AVIS")
    private String system;
    @Email(message = "Неверный формат email")
    @Schema(description = "email получателя", example = "test_email@alfastrah.ru")
    private String email;
    @Schema(description = "Имя получаетя")
    private String fio;
    @Schema(description = "Идентификатор договора", example = "2814378")
    private BigInteger contractId;
}
