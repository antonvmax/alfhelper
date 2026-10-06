package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.db.MarketName;
import tops.unicus.usr.RContractVariantAdditional;
import tops.unicus.usr.RSaleContract;

import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_5;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.UNKNOWN;

@Service
public class MarketNameService {

    private static final Long KASKO_ADDITIONAL_PRODUCT_ADD_ID = 6871812L;
    private static final Long PRODUCT_ADD_ID = 5962413L;
    private static final Long ALFA_PRODUCT_ADD_ID = 6422376L;
    private static final Long NS_PRODUCT_ADD_ID = 6878371L;

    public MarketName getMarketName(RSaleContract contract) {
        return switch (Product.fromId(contract.getVariants().get(0).getProductId())) {
            case KASKO -> getKaskoMarketNameWithAdditionalProgram(contract);
            case NS397 -> getNSMarketNameId(contract);
            default -> UNKNOWN;
        };
    }

    public MarketName getKaskoMarketName(RSaleContract contract) {
        return MarketName.fromCode(getProgramId(contract, PRODUCT_ADD_ID));
    }

    public MarketName getAlfaRepairMarketNameId(RSaleContract contract) {
        return MarketName.fromCode(getProgramId(contract, ALFA_PRODUCT_ADD_ID));
    }

    public MarketName getNSMarketNameId(RSaleContract contract) {
        return MarketName.fromCode(getProgramId(contract, NS_PRODUCT_ADD_ID));
    }

    private MarketName getKaskoMarketNameWithAdditionalProgram(RSaleContract contract) {
        MarketName marketName = getKaskoMarketName(contract);
        if (KASKO_5 == marketName) {
            MarketName additionalProgram = MarketName.fromCode(getProgramId(contract, KASKO_ADDITIONAL_PRODUCT_ADD_ID));
            if (additionalProgram != UNKNOWN) {
                return additionalProgram;
            }
        }
        return marketName;
    }

    private String getProgramId(RSaleContract contract, Long productId) {
        return contract.getVariants().get(0).getAdditionals().stream()
                .filter(add -> productId.equals(add.getProductAddId()))
                .findFirst()
                .map(RContractVariantAdditional::getValueText)
                .orElse(null);
    }
}
