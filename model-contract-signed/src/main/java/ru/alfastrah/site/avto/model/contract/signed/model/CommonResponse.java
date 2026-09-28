package ru.alfastrah.site.avto.model.contract.signed.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "CommonResponse",
        description = "Возвращает статус и сообщение об ошибке"
)
public class CommonResponse {

    @Schema(description = "Статус ошибки")
    private String statusCode;

    @Schema(example = "Сообщение об ошибке")
    private String message;

    @Schema(example = "Список всех ошибок, возникших во время работы")
    private Object[] detailList;
}
