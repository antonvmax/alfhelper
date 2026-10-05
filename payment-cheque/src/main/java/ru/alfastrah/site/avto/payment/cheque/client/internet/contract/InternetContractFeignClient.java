package ru.alfastrah.site.avto.payment.cheque.client.internet.contract;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.alfastrah.site.avto.payment.cheque.model.unicus.InternetContractPayment;

@FeignClient(name="interner-contract", url = "#{environment.acceptsProfiles('default') ? 'http://localhost:11004' : 'http://internet-contract:11004'}")
public interface InternetContractFeignClient {
    @GetMapping(path = "/internet-contract-details/mdOrder-{mdOrder}")
    InternetContractPayment getPaymentDictByMdOrder(@PathVariable(name = "mdOrder") String mdOrder);

    @GetMapping(path = "/internet-contract-details/contractId-{contractId}")
    InternetContractPayment getPaymentDictByContractId(@PathVariable(name = "contractId") String contractId);
}
