package ru.alfastrah.site.avto.ws.contact.signed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.contract.signed.exception.InvalidPrintFormException;
import ru.alfastrah.site.avto.model.contract.signed.exception.InvalidRequestException;
import ru.alfastrah.site.avto.model.contract.signed.exception.NoPersonalDataException;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.personal.SendingPersonalPolicyEmailService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SendingPersonalPolicyEmailController {

    private final SendingPersonalPolicyEmailService service;

    @Operation(description = "Сервис по отправке печатной формы на email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неправильное тело запроса",
                    content = {@Content(mediaType = "application/json", examples = {@ExampleObject(value = """
                                {
                                   "timestamp": "2025-07-10T00:00:00.000+00:00",
                                   "status": 400,
                                   "error": "Bad Request",
                                   "trace": "string"
                                }
                            """
                    )})}),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка при выполнении запроса",
                    content = {@Content(mediaType = "application/json", examples = {@ExampleObject(value = """
                                {
                                  "timestamp": "2025-07-10T00:00:00.000+00:00",
                                  "status": 500,
                                  "error": "Internal Server Error",
                                  "trace": "string"
                                }
                            """
                    )})}),
    })

    @PostMapping(path = "/sendPrintedForm/email", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody SendingPersonalPolicyEmailResponse sendPersonalEmail(
            @RequestBody SendingPersonalPolicyEmailRequest request) {
        return service.sendingPolicy(request);
    }

    @ExceptionHandler({InvalidRequestException.class, NoPersonalDataException.class})
    public SendingPersonalPolicyEmailResponse handleException(RuntimeException e) {
        log.error(e.getMessage());
        return new SendingPersonalPolicyEmailResponse(false, "Invalid request");
    }

    @ExceptionHandler(InvalidPrintFormException.class)
    public SendingPersonalPolicyEmailResponse handleException(InvalidPrintFormException e) {
        log.error(e.getMessage());
        return new SendingPersonalPolicyEmailResponse(false, "Invalid PrintForm");
    }
}
