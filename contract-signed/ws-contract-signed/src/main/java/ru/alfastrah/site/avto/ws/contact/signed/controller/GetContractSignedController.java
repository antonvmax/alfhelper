package ru.alfastrah.site.avto.ws.contact.signed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoReplaceException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetAnyContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetAnyContractSignedService;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetContractSignedService;
import ru.alfastrah.site.avto.ws.contact.signed.utils.CreateFault;
import ru.alfastrah.site.avto.ws.contact.signed.utils.PrintFormType;

@RestController
@RequestMapping("/")
@Slf4j
public class GetContractSignedController {

    @Autowired
    private GetContractSignedService getContractSignedService;
    @Autowired
    private GetAnyContractSignedService getAnyContractSignedService;
    @Autowired
    private CreateFault createFault;


    @Operation(description = "Возвращает подписанный договор в формате Base64")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject("""
                                    {
                                      "timestamp": "2025-09-25T10:11:05.026+00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "trace": "..."
                                    }
                                    """)})}),
            @ApiResponse(responseCode = "500", description = "Ошибка при выполнении запроса",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject("""
                                    {
                                      "cause": null,
                                      "stackTrace": [],
                                      "message": "Текст ошибки",
                                      "code": "Код ошибки",
                                      "suppressed": [],
                                      "localizedMessage": "Локализованное сообщение об ошибке"
                                    }
                                    """)})})
    })
    @PostMapping("GetAnyContractSignedLocal")
    public GetContractSignedResponseType getAnyContractSignedLocal(
            @RequestBody GetAnyContractSignedRequestType getAnyContractSignedRequestType) {
        return getAnyContractSignedService.processGetAnyContractSigned(getAnyContractSignedRequestType);
    }

    @Operation(description = "Возвращает подписанный полис, по указанному идентификатору расчёта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "500", description = "Ошибка при выполнении запроса",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject("""
                                    {
                                      "cause": null,
                                      "stackTrace": [],
                                      "message": "К UPID 44ceb2c9-d72f-41da-bd64-bfa68746dde0 не привязан контракт 281437803",
                                      "code": "Отсутствует контракт",
                                      "suppressed": [],
                                      "localizedMessage": "К UPID 44ceb2c9-d72f-41da-bd64-bfa68746dde0 не привязан контракт 281437803"
                                    }
                                    """)
                    })})
    })
    @PostMapping("GetContractSignedLocal")
    public GetContractSignedResponseType getContractSignedLocal(
            @RequestParam(name = "printFormType", required = false, defaultValue = "DEFAULT") PrintFormType printFormType,
            @RequestBody GetContractSignedRequestType getContractSignedRequestType) {
        return getContractSignedService.processGetContractSigned(getContractSignedRequestType, printFormType);
    }

    @Operation(description = "Возвращает подписанный полис, по указанному идентификатору расчёта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "500", description = "Ошибка при выполнении запроса",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject("""
                                    {
                                      "cause": null,
                                      "stackTrace": [],
                                      "message": "К UPID 44ceb2c9-d72f-41da-bd64-bfa68746dde0 не привязан контракт 281437803",
                                      "code": "Отсутствует контракт",
                                      "suppressed": [],
                                      "localizedMessage": "К UPID 44ceb2c9-d72f-41da-bd64-bfa68746dde0 не привязан контракт 281437803"
                                    }
                                    """)
                    })})
    })
    @PostMapping("GetContractSigned")
    public GetContractSignedResponseType getContractSigned(
            @RequestParam(name = "printFormType", required = false, defaultValue = "DEFAULT") PrintFormType printFormType,
            @RequestBody GetContractSignedRequestType getContractSignedRequestType) {
        return getContractSignedService.processGetContractSigned(getContractSignedRequestType, printFormType);
    }

    @ExceptionHandler(EOsagoProccessException.class)
    public ResponseEntity<EOsagoProccessException> unacceptableDocument(EOsagoProccessException ex) {
        log.error("Exception => {}", ex.toString());
        EOsagoProccessException error = createFault.getError(ex);
        error.setStackTrace(new StackTraceElement[]{});
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(EOsagoReplaceException.class)
    public ResponseEntity<EOsagoProccessException> replaceException(EOsagoReplaceException exception) {
        log.error("Exception => {}", exception.toString());
        EOsagoProccessException error = createFault.getError(new EOsagoProccessException(exception.getMessage(), "Replace Error"));
        error.setStackTrace(new StackTraceElement[]{});
        return ResponseEntity.internalServerError().body(error);
    }
}
