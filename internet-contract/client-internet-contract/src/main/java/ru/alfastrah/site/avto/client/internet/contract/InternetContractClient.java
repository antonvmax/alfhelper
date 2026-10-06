package ru.alfastrah.site.avto.client.internet.contract;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.Response;
import ru.alfastrah.site.avto.model.internet.contract.dto.UpdateInternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.UpdateInternetContractRequest;

@FeignClient(name = "internet-contract-client", url = "${internet-contract.url}")
public interface InternetContractClient {

    @PostMapping(path = "/internet-contract", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Long createContract(@RequestBody InternetContractRequest request);

    @PostMapping(path = "/internet-contract-payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Long createPayment(@RequestBody InternetContractPaymentRequest request);

    @PostMapping(path = "/internet-contract/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Response updateInternetContract(@RequestBody UpdateInternetContractRequest request);

    @PostMapping(path = "/internet-contract-payment/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Response updateInternetContractPayment(@RequestBody UpdateInternetContractPaymentRequest request);

    @GetMapping(path = "/internet-contract-details/mdOrder-{mdOrder}", produces = MediaType.APPLICATION_JSON_VALUE)
    InternetContractDetails findByMdOrder(@PathVariable("mdOrder") String mdOrder);

    @GetMapping(path = "/internet-contract-details/contractId-{contractId}", produces = MediaType.APPLICATION_JSON_VALUE)
    InternetContractDetails findByContractId(@PathVariable("contractId") String contractId);
}
