package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id;

import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import tops.unicus.usr.RSaleContract;

public interface PrintFormIdRetriever {
    String retrieve(RSaleContract info) throws EOsagoException;
    Product product();
}
