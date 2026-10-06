package ru.alfastrah.site.avto.payment.internet.contract.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.internet.contract.dto.CancelInternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.RefundInternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.Response;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetContractStatusRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetPaymentDictRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.UpdateInternetContractRequest;
import ru.alfastrah.site.avto.payment.internet.contract.service.InternetContractService;

/**
 * Жизненный цикл интернет-договора: создание, обновление, справки по договору.
 */
@RestController
@RequiredArgsConstructor
public class InternetContractController {

    private final InternetContractService service;

    @PostMapping(path = "/internet-contract", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void createContract(@RequestBody InternetContractRequest request) {
        service.createContract(request);
    }

    @PostMapping(path = "/internet-contract/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response updateInternetContract(@RequestBody UpdateInternetContractRequest request) {
        String error = service.updateContract(request.contractId(), request.orderId(), request.paidDate());
        return new Response(error);
    }

    @PostMapping(path = "/internet-contract/cancel", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response cancelContract(@RequestBody CancelInternetContractRequest request) {
        return new Response(service.cancelContract(request.contractId()));
    }

    @PostMapping(path = "/internet-contract/refund", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response refundContract(@RequestBody RefundInternetContractRequest request) {
        return new Response(service.refundContract(request.contractId(), request.mdOrder()));
    }

    @PostMapping(path = "/internet-contract/payment-dict", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response setPaymentDict(@RequestBody SetPaymentDictRequest request) {
        return new Response(service.setPaymentDict(request));
    }

    @PostMapping(path = "/internet-contract/contract-status", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response setContractStatus(@RequestBody SetContractStatusRequest request) {
        return new Response(service.setContractStatus(request));
    }

    @GetMapping(path = "/internet-contract/count-additionals/{contractId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Long countAdditionals(@PathVariable("contractId") Long contractId) {
        return service.countAdditionals(contractId);
    }
}
