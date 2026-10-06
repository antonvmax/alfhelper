package ru.alfastrah.site.avto.ws.contact.signed.client.orange;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.TransactionInfoResponse;

@FeignClient(name = "msOrangeClient", url = "${ms.orange.rest.service.url}")
public interface MsOrangeClient {

    @GetMapping("/orange/contracts/byContractNumber?system=UNICUS")
    TransactionInfoResponse getTransactionInfo(@RequestParam("contractSeria") String series,
                                               @RequestParam("contractNumber") String number);

    @GetMapping("/orange/contracts/byContractNumber?system=UNICUS")
    TransactionInfoResponse getTransactionInfoByNumber(@RequestParam("contractNumber") String number);
}
