package ru.alfastrah.site.avto.payment.cheque.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.payment.cheque.exception.ReceivingChequeException;
import ru.alfastrah.site.avto.payment.cheque.model.ChequeResponse;
import ru.alfastrah.site.avto.payment.cheque.service.PaymentChequeService;

@RestController
@RequiredArgsConstructor
public class PaymentChequeController {
    private final PaymentChequeService service;

    @GetMapping(path = "/partner", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ChequeResponse getCheck(
            @RequestParam(required = false) String upid,
            @RequestParam(required = false) String contractId,
            @RequestParam(required = false) String mdOrder
    ) {
        return service.getChequeInfo(upid, contractId, mdOrder);
    }

    @ExceptionHandler(ReceivingChequeException.class)
    @SuppressWarnings("java:S1452")
    public ResponseEntity<?> unacceptableDocument(ReceivingChequeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
