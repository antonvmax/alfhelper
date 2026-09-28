package ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl;

import jakarta.activation.DataHandler;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;


public class RawPrintForm implements PrintForm {
    private final DataHandler content;
    private final String id;
    public RawPrintForm(DataHandler content, String id) {
        this.content = content;
        this.id = id;
    }

    public DataHandler content() {
        return this.content;
    }

    public String id() {
        return this.id;
    }
}
