package ru.alfastrah.site.avto.ws.contact.signed.service.printform;

import jakarta.activation.DataHandler;

public interface PrintForm {
    DataHandler content();
    String id();
}
