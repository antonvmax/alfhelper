package ru.alfastrah.site.avto.ws.contact.signed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.FileSigningException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedResponse;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.send.DirectSendContractSignedResponse;
import ru.alfastrah.site.avto.ws.contact.signed.controller.model.SendContractSignedSyncResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.SendContractSignedRequestRegister;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.process.ContractSignedProcessService;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.process.DirectSendProcessService;

@Slf4j
@RestController
@RequestMapping("/")
public class SendContractSignedRestController {

    private final SendContractSignedRequestRegister requestRegister;
    private final ContractSignedProcessService contractSignedProcessService;
    private final DirectSendProcessService directSendProcessService;

    public SendContractSignedRestController(SendContractSignedRequestRegister requestRegister,
                                            ContractSignedProcessService contractSignedProcessService, DirectSendProcessService directSendProcessService) {
        this.requestRegister = requestRegister;
        this.contractSignedProcessService = contractSignedProcessService;
        this.directSendProcessService = directSendProcessService;
    }

    @Operation(description = "Запрос на асинхронную отправку полиса на email получателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "status": null,
                                        "messageId": null,
                                        "description": null,
                                        "result": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "503", description = "Ошибка при отправке данных",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "Result": false,
                                        "error": "Сообщение об ошибке"
                                    }
                                    """)))
    })
    @PostMapping("SendContractSigned")
    public SendContractSignedResponse registerContractSignedRequest(
            @RequestBody SendContractSignedRequest contractSignedRequest) {
        return requestRegister.registerContractSignedRequest(contractSignedRequest);
    }

    @Operation(description = "Запрос на синхронную отправку подписанного полиса на email получателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "status": null,
                                        "messageId": null,
                                        "description": null,
                                        "result": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Ошибка при обработке запроса",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "Result": false,
                                        "error": "Сообщение об ошибке"
                                    }
                                    """))),
            @ApiResponse(
                    responseCode = "503",
                    description = "Ошибка при попытке отправить сообщение",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "Result": false,
                                        "error": "Сообщение об ошибке"
                                    }
                                    """
                            )))
    })
    @PostMapping("SendContractSignedSync")
    public SendContractSignedResponse registerContractSignedRequestSync(@RequestBody SendContractSignedRequest contractSignedRequest) {
        boolean result = contractSignedProcessService.processSendContractSigned(contractSignedRequest);
        SendContractSignedResponse response = new SendContractSignedResponse();
        response.setResult(result);
        return response;
    }

    @Operation(description = "Сервис отправляет на email получателя все доступные полисы по идентификатору договора или единого чека")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                       "success": true,
                                       "message": "Письмо успешно отправлено на следующие email: [ test_email@alfastrah.ru ]"
                                    }
                                    """))),
            @ApiResponse(
                    responseCode = "503",
                    description = "Ошибка валидации входных данных",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                        {
                                          "result": false,
                                          "error": "Сообщение об ошибке"
                                        }
                                    """
                            )))
    })
    @PostMapping("sendPrintedFormByContractId")
    public DirectSendContractSignedResponse sendPrintForm(@Valid @RequestBody DirectSendContractSignedRequest request) {
        return directSendProcessService.send(request);
    }

    @ExceptionHandler({FileSigningException.class, BadDataException.class})
    public ResponseEntity<SendContractSignedSyncResponse> mailClientException(Exception exception) {
        SendContractSignedSyncResponse response = new SendContractSignedSyncResponse();
        response.setResult(false);
        response.setError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler({Exception.class, SendContractServerException.class})
    public ResponseEntity<SendContractSignedSyncResponse> mailServerException(Exception exception) {
        log.error("Произошла ошибка при отправке письма в SendContractSignedRestController: {}", exception.getMessage(), exception);
        SendContractSignedSyncResponse response = new SendContractSignedSyncResponse();
        response.setResult(false);
        response.setError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
