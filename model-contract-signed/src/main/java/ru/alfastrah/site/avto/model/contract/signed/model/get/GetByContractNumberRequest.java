package ru.alfastrah.site.avto.model.contract.signed.model.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(
        name = "GetByContractNumberRequest",
        description = "Запрос для получения подписанного договора"
)
public class GetByContractNumberRequest {

    @Schema(description = "Серия договора", example = "ХХХ")
    private String contractSeries;

    @Schema(description = "Номер договора", example = "0347466902")
    private String contractNumber;

    @Schema(description = "Идентификатор печатной формы", example = "525")
    private String printedFormId;
}
