package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.interplat4.altcraft.util.TriggerConstants;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.List;

@Service
public class WhiteCardRequestFactory extends RequestFactory {

    private static final String WHITE_CARD_PRODUCT = "belaya_karta_thanks";

    protected WhiteCardRequestFactory(ClientInfoFactory clientInfoFactory,
                                      SubscriptionFactory subscriptionFactory,
                                      UnicusUsrService clientUsr,
                                      UnicusSubjectService clientSubject,
                                      AttachmentFactory attachmentFactory,
                                      MailingClient mailingClient) {
        super(clientInfoFactory, subscriptionFactory, clientUsr, clientSubject, attachmentFactory, mailingClient);
    }


    protected ContentFormatted createContent(ClientInfo clientfio,
                                             RSaleContract contract,
                                             SendContractSignedRequest request,
                                             SaleContract data) throws EOsagoSaveException {
        List<RContractSubject> subjectList = getSubjectList(contract.getContractId());
        ContentFormatted contentFormatted = new ContentFormatted();
        contentFormatted.setProduct(WHITE_CARD_PRODUCT);
        contentFormatted.setContractNumber(contract.getContractNumber());
        contentFormatted.setDateStartContractOld(contract.getBeginDate());
        contentFormatted.setDateEndContractOld(contract.getEndDate());
        contentFormatted.setUrl(createPassbookUrl(BigInteger.valueOf(contract.getContractId())));
        contentFormatted.setPolicyholder(getFirstLastNames(clientfio));
        contentFormatted.setObject(getMarkaModelNames(subjectList.get(0).getSubjectId()));
        contentFormatted.setInsuranceAmount("0");
        contentFormatted.setInsurancePremium(formatInsurancePremium(contract.getContractPremium()));
        return contentFormatted;
    }


    @Override
    protected SaleContract fillDataField(ClientInfo fio) {
        SaleContract saleContract = super.fillDataField(fio);
        return saleContract;
    }

    @Override
    protected Integer triggerId() {
        return TriggerConstants.WHITE_CARD;
    }
}
