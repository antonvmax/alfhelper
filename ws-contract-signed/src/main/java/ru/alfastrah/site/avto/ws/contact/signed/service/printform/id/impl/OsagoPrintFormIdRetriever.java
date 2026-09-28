package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.PrintFormIdRetriever;
import tops.unicus.usr.RContractRestrictionUser;
import tops.unicus.usr.RContractVariant;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.partner.AgentBlockPrintFormCheck;
import tops.unicus.usr.RSaleContract;

import java.util.List;

import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CANCELED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONCLUDED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONFIRMED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.STATEMENT;

@Service
public class OsagoPrintFormIdRetriever implements PrintFormIdRetriever {

    private static final int OSAGO_DRIVERS_THRESHOLD = 5;

    private final UnicusUsrService unicusUsrService;
    private final AgentBlockPrintFormCheck agentBlockPrintFormCheck;

    public OsagoPrintFormIdRetriever(UnicusUsrService unicusUsrService, AgentBlockPrintFormCheck agentBlockPrintFormCheck) {
        this.unicusUsrService = unicusUsrService;
        this.agentBlockPrintFormCheck = agentBlockPrintFormCheck;
    }

    @Override
    public String retrieve(RSaleContract info) throws EOsagoException {
        String printedFormId;
        switch (info.getContractStatusTypeId()) { //todo проверка статуса договора
            case CONCLUDED:
            case CONFIRMED:
                printedFormId = confirmedPrintformId(info.getContractId());
                break;
            case STATEMENT:
                printedFormId = "541";
                break;
            case CANCELED:
                throw new EOsagoException("Договор был аннулирован.", "PrintFormIdentification");
            default:
                throw new EOsagoException("Неизвестный статус договора " + info.getOpenContractStatusId(), "PrintFormIdentification");
        }
        return printedFormId;
    }

    public int contDriver(Long contractId) {
        List<RContractVariant> contractVariants;
        List<RContractRestrictionUser> contractRestrictionUserList = null;
        contractVariants = unicusUsrService.getContractVariantList(contractId);
        contractRestrictionUserList = unicusUsrService.getContractRestrictionUserList(contractVariants.get(0).getContractVariantId());
        return contractRestrictionUserList.size();
    }

    private String confirmedPrintformId(Long contractId) {
        if (agentBlockPrintFormCheck.check(contractId)) {
            return "1492";
        }
        return contDriver(contractId) < OSAGO_DRIVERS_THRESHOLD ? "525" : "526";
    }

    @Override
    public Product product() {
        return Product.OSAGO;
    }
}
