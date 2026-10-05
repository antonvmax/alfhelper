package ru.alfastrah.site.avto.payment.cheque.client.payment.methods;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.alfastrah.site.avto.payment.cheque.model.ChequeResponse;

@FeignClient(name="payment-methods", url = "#{environment.acceptsProfiles('default') ? 'http://localhost:11000' : 'http://payment-methods:11000'}")
public interface PaymentMethodsFeignClient {
    @GetMapping(path = "/cheque/unicusPaymentId-{unicusPaymentId}/mdOrder-{mdOrder}")
    ChequeResponse getCheque(@PathVariable(name = "mdOrder") String mdOrder,
                             @PathVariable(name = "unicusPaymentId") String unicusPaymentId);
}
