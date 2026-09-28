package ru.alfastrah.site.avto.ws.contact.signed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.model.CommonResponse;
import ru.alfastrah.site.avto.model.contract.signed.model.get.GetByContractNumberRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.get.GetByContractNumberResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetByContractNumberService;

@Slf4j
@RestController
@RequestMapping("/")
public class GetContractSignedByNumberController {

    @Autowired
    private GetByContractNumberService getByContractNumberService;

    @Operation(description = "Получение подписанного полиса по номеру contractNumber")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetByContractNumberResponse.class))}),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неправильные параметры запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "statusCode": "400",
                                      "message": "Сообщение об ошибке",
                                      "detailList": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Ошибка при выполнении запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "statusCode": "500",
                                      "message": "Сообщение об ошибке",
                                      "detailList": null
                                    }
                                    """))),
    })
    @PostMapping("GetSignPrintedFormByContractNumber")
    public ResponseEntity<?> getSignedFormByContractNumber(@RequestBody GetByContractNumberRequest request) {
        return ResponseEntity.ok(getByContractNumberService.getPrintForm(request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse> handleError(Exception e) {
        log.error("Ошибка во время выполнения: {}", e.getMessage(), e);
        CommonResponse response = new CommonResponse();
        response.setStatusCode("500");
        response.setMessage(e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(BadDataException.class)
    public ResponseEntity<CommonResponse> handleClientError(Exception e) {
        log.error("Ошибка во время выполнения: {}", e.getMessage(), e);
        CommonResponse response = new CommonResponse();
        response.setStatusCode("400");
        response.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
