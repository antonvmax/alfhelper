package ru.alfastrah.site.avto.model.contract.signed.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@Schema(description = "Запрос на получение подписанного договора по идентификатору расчета договора")
public class GetContractSignedRequestType {

    @JsonProperty("UPID")
    @Schema(description = "Уникальный идентификатор расчета", example = "04ac5302-96b1-4c31-8cdd-5334660bd90b")
    private String upid;

    @JsonProperty("ContractId")
    @Schema(description = "Идентификатор договора", example = "295029830")
    private BigInteger contractId;
}
