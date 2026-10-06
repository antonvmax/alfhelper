package ru.alfastrah.site.avto.payment.internet.contract.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastrah.site.avto.model.internet.contract.dto.Response;
import ru.alfastrah.site.avto.payment.internet.contract.controller.dto.PF2M1MessageRequest;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;
import ru.alfastrah.site.avto.payment.internet.contract.service.InternetContractMessageService;

/**
 * Сообщения об оплате для юникуса (процедуры p_f2_amount_message и f2_m1_message).
 * Пути исторически разнесены между /internet-contract-details и /internet-contract, поэтому заданы полностью.
 */
@RestController
@RequiredArgsConstructor
public class InternetContractMessageController {

    private final InternetContractMessageService service;

    @PostMapping(path = "/internet-contract-details/pf2AmountMessage", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String pf2AmountMessage(@RequestBody PF2AmountMessage request) {
        return service.pf2AmountMessage(request);
    }

    @PostMapping(path = "/internet-contract/pF2m1Message", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response pF2m1Message(@RequestBody PF2M1MessageRequest request) {
        String error = service.pf2m1Message(request);
        return new Response(error);
    }

    @PostMapping(path = "/internet-contract/f2m1MessageWithoutEmail", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Response f2m1MessageWithoutEmail(@RequestBody PF2M1MessageRequest request) {
        String error = service.f2m1MessageWithoutEmail(request);
        return new Response(error);
    }
}
