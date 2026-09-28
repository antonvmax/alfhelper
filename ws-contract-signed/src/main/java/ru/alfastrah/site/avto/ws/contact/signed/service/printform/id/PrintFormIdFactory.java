package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import tops.unicus.usr.RSaleContract;

import java.util.List;

@Service
public class PrintFormIdFactory {
    public static final String JASPER_PRINTED_FORM_ID_888 = "-888";

    private final List<PrintFormIdRetriever> printFormIdRetrievers;

    public PrintFormIdFactory(List<PrintFormIdRetriever> printFormIdRetrievers) {
        this.printFormIdRetrievers = printFormIdRetrievers;
    }

    public String byProduct(RSaleContract contractInfo) {
        return printFormIdRetrievers.stream()
                .filter(id -> id.product().getProductId().equals(
                        contractInfo.getVariants().get(0).getProductId()))
                .findFirst()
                .map(printFormIdRetriever -> {
                    try {
                        return printFormIdRetriever.retrieve(contractInfo);
                    } catch (EOsagoException e) {
                        return JASPER_PRINTED_FORM_ID_888;
                    }
                })
                .orElse(JASPER_PRINTED_FORM_ID_888);
    }
}
