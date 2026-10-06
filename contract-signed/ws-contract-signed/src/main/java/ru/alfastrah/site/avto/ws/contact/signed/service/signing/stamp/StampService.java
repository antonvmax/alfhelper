package ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp.SignatureRepository;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StampService {

    private final PartnersDb partnersDb;
    private final SignatureRepository signatureRepository;

    public StampService(PartnersDb partnersDb, SignatureRepository signatureRepository) {
        this.partnersDb = partnersDb;
        this.signatureRepository = signatureRepository;
    }

    public List<StampParams> getStampData(BigInteger contractId, String formId) {
        log.info("Получаем информацию о штампе подписи. ContractId {}, formId {}", contractId, formId);
        Product product = Product.fromId(partnersDb.getContractInfo(contractId).getVariants().get(0).getProductId());
        if (Product.UNKNOWN == product) {
            log.warn("Не удалось определить продукт договора {}", contractId);
            return new ArrayList<>();
        }
        return signatureRepository.getStampParams(product, formId);
    }
}
