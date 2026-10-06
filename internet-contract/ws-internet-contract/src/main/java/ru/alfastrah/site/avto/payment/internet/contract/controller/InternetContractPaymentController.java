package ru.alfastrah.site.avto.payment.internet.contract.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.Response;
import ru.alfastrah.site.avto.model.internet.contract.dto.UpdateInternetContractPaymentRequest;
import ru.alfastrah.site.avto.payment.internet.contract.service.InternetContractPaymentService;

import java.util.List;

/**
 * Платежи по интернет-договору: создание строки оплаты и её обновление.
 */
@RestController
@RequiredArgsConstructor
public class InternetContractPaymentController {

    private final InternetContractPaymentService paymentService;

    @PostMapping(path = "/internet-contract-payment", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createPayment(@RequestBody InternetContractPaymentRequest request) {
        paymentService.createPayment(request);
    }

    @GetMapping(path = "/internet-contract-payment/contract-ids/mdOrder-{mdOrder}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Long> findContractIdsByMdOrder(@PathVariable("mdOrder") String mdOrder) {
        return paymentService.findContractIdsByMdOrder(mdOrder);
    }

    @PostMapping(path = "/internet-contract-payment/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response updateInternetContractPayment(@RequestBody UpdateInternetContractPaymentRequest request) {
        String error = paymentService.updatePayment(request.contractId(), request.orderId(), request.paidDate());
        return new Response(error);
    }
}
