package ru.alfastrah.site.avto.ws.contact.signed.service.printform;

import jakarta.activation.DataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;

import java.math.BigInteger;

@Slf4j
@Service
public class PrintFormFactory {

    private final PdfClient pdfClient;

    public PrintFormFactory(PdfClient pdfClient) {
        this.pdfClient = pdfClient;
    }

    public PrintForm create(BigInteger contractId, String printedFormId) {
        DataHandler content = pdfClient.getPrintedFormByContractId(contractId, printedFormId, null);
        return new RawPrintForm(content, printedFormId);
    }
}
