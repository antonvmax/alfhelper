package ru.alfastrah.site.avto.payment.internet.contract.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.payment.internet.contract.service.InternetContractDetailsService;

/**
 * Чтение деталей интернет-договора по идентификатору договора или по заказу платёжного шлюза.
 */
@RestController
@RequiredArgsConstructor
public class InternetContractDetailsController {

    private final InternetContractDetailsService service;

    @GetMapping(path = "/internet-contract-details/contractId-{contractId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public InternetContractDetails findByContractId(@PathVariable("contractId") String contractId) {
        return service.findByContractId(contractId);
    }

    @GetMapping(path = "/internet-contract-details/mdOrder-{mdOrder}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public InternetContractDetails findByMdOrder(@PathVariable("mdOrder") String mdOrder) {
        return service.findByMdOrder(mdOrder);
    }
}
