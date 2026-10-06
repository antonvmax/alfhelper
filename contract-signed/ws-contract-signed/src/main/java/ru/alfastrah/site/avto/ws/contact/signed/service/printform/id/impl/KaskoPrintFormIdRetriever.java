package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.db.MarketName;
import ru.alfastrah.site.avto.ws.contact.signed.service.MarketNameService;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.PrintFormIdRetriever;
import tops.unicus.usr.RSaleContract;

import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.STATEMENT;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_10;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_5;

@Service
public class KaskoPrintFormIdRetriever implements PrintFormIdRetriever {

    private final MarketNameService marketService;

    public KaskoPrintFormIdRetriever(MarketNameService marketService) {
        this.marketService = marketService;
    }

    @Override
    public String retrieve(RSaleContract contractInfo) {
        String printedFormId = "-1";
        MarketName marketName = marketService.getKaskoMarketName(contractInfo);
        if (KASKO_5 == marketName) {
            printedFormId = STATEMENT == contractInfo.getContractStatusTypeId() ? "930" : "929";
        } else if (KASKO_10 == marketName) {
            printedFormId = "559";
        }
        return printedFormId;
    }

    @Override
    public Product product() {
        return Product.KASKO;
    }
}
