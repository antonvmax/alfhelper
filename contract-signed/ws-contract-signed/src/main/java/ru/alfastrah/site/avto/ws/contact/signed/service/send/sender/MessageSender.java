package ru.alfastrah.site.avto.ws.contact.signed.service.send.sender;

import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;

public interface MessageSender {
    void send(SendContractSignedRequest request) throws EOsagoExceptionMailNoStacktrace;
    Product product();
}
