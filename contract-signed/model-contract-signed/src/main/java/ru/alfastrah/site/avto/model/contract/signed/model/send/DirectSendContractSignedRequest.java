package ru.alfastrah.site.avto.model.contract.signed.model.send;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Schema(
        name = "DirectSendContractSignedRequest",
        description = "Данные по типу договора и email для отправки письма"
)
public class DirectSendContractSignedRequest {

    @JsonProperty("entityId")
    @Schema(description = "Идентификатор сущности", example = "2814378")
    private BigInteger entityId;

    @JsonProperty("entityType")
    @Schema(description = "Тип сущности: " +
            "CONTRACT - Договор, SINGLE_ACC - Единый чек",
            type = "string",
            example = "CONTRACT")
    private EntityType entityType;

    @JsonProperty("email")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @Schema(description = "Почта клиента для отправки письма", example = "[\"test_email@alfastrah.ru\"]")
    private List<String> email;
}
