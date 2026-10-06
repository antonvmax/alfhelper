package ru.alfastrah.site.avto.model.contract.signed.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "ContractId", "PrintedFormId", "Email", "Subject", "Recurrent" })
@Schema(description = "Запрос на отправку подписанного договора по почте")
public class SendContractSignedRequest {

    @JsonProperty("ContractId")
    @Schema(description = "Идентификатор договора в Юникус", example = "2814378")
    private BigInteger contractId;

    @JsonProperty("PrintedFormId")
    @Schema(description = "Идентификатор печатной формы", example = "525")
    private String printedFormId;

    @JsonProperty("Email")
    @Schema(description = "Email получателя", example = "test_email@alfastrah.ru")
    private String email;

    @JsonProperty("Subject")
    @Schema(description = "Тема письма", example = "АльфаСтрахование – заявление и электронный полис ОСАГО")
    private String subject;

    @JsonProperty("Recurrent")
    @Schema(description = "Флаг рекуррентного платежа", example = "false")
    private Boolean recurrent;
}
