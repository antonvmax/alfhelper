package ru.alfastrah.site.avto.model.contract.signed.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.activation.DataHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Ответ с подписанным договором")
public class GetContractSignedResponseType {

    @Schema(description = "Идентификатор печатной формы", example = "526")
    @JsonProperty("PrintedFormId")
    private String printedFormId;

    @Schema(description = "MIME-тип содержимого", example = "application/pdf", type = "string")
    @JsonProperty("MIME")
    private String mime;

    @Schema(description = "Содержимое документа в формате base64", example = "JVBERi0xLjUKJ...")
    @JsonProperty("Content")
    private DataHandler content;
}
