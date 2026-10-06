package ru.alfastrah.site.avto.model.contract.signed.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.alfastrah.schemas.interplat4.send_contract_signed.TypeSignature;

import java.math.BigInteger;

@Getter
@Setter
@Schema(description = "Запрос на получение подписанного договора")
public class GetAnyContractSignedRequestType {

    @JsonProperty("ContractId")
    @Schema(description = "Идентификатор договора", example = "281437803")
    private BigInteger contractId;

    @JsonProperty("PrintedFormId")
    @Schema(description = "Идентификатор печатной формы", example = "526")
    private String printedFormId;

    @JsonProperty("Params")
    @Schema(description = "Дополнительные параметры для печатной формы", example = "null")
    private String params;

    @JsonProperty("Signature")
    @Schema(description = "Подпись документа", example = "Горин А.Э.")
    private TypeSignature signature;
}

