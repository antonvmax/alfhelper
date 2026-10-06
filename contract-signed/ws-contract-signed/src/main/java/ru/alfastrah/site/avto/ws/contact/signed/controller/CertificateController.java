package ru.alfastrah.site.avto.ws.contact.signed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.ws.contact.signed.model.signing.CertificateInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.CertificateInfoService;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateInfoService certificateInfoService;

    @Operation(summary = "Получение информации о сертификате",
            description = "Возвращает информацию о текущем цифровом сертификате системы")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CertificateInfoResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "expirationDate": "28.08.2026",
                              "fingerprint": "83519A8A26E236895E068A280CA52B165382DF3D",
                              "attorneyNumber": "6dca1c2a-95d2-4d6d-ab0f-c50b6a4b01a9",
                              "attorneyGranted": "2024-03-14",
                              "issuer": "АО \\"ПФ \\"СКБ Контур\\""
                            }
                            """)))
    @GetMapping(value = "/cert/info", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateInfoResponse> info() {
        return ResponseEntity.ok().body(certificateInfoService.getInfo());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle() {
        return ResponseEntity.badRequest().body("Некорректный сертификат");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle500(Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
