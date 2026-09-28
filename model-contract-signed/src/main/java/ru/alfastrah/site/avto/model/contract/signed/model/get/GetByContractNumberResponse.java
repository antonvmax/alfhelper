package ru.alfastrah.site.avto.model.contract.signed.model.get;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.activation.DataHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(
        name = "GetByContractNumberResponse",
        description = "Содержит подписанный электронной подписью документ"
)
public class GetByContractNumberResponse {

    @Schema(description = "Тип возвращаемого значения", example = "application/pdf")
    private String mime;

    @Schema(description = "Подписанный договор в виде строки base64", example = "JVBERi0xLjUKJeLjz9MKMy...")
    private DataHandler content;
}
