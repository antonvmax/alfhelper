package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.interplat4.altcraft.util.TriggerConstants;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.MsOrangeClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.Transaction;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.TransactionInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.UserInfo;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyData;
import ru.alfastrah.site.avto.ws.contact.signed.service.LoyaltyClientDataService;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ALTCRAFT_PRODUCT;

@Slf4j
@Service
public class OsagoRequestFactory extends RequestFactory {

    private static final String OSAGO_REWARD = "400 000";

    private final LoyaltyClientDataService loyaltyClientDataService;
    private final MsOrangeClient msOrangeClient;

    protected OsagoRequestFactory(ClientInfoFactory clientInfoFactory,
                                  SubscriptionFactory subscriptionFactory,
                                  UnicusUsrService clientUsr,
                                  UnicusSubjectService clientSubject,
                                  AttachmentFactory attachmentFactory,
                                  LoyaltyClientDataService loyaltyClientDataService,
                                  MsOrangeClient msOrangeClient,
                                  MailingClient mailingClient) {
        super(clientInfoFactory, subscriptionFactory, clientUsr, clientSubject, attachmentFactory, mailingClient);
        this.loyaltyClientDataService = loyaltyClientDataService;
        this.msOrangeClient = msOrangeClient;
    }


    protected ContentFormatted createContent(ClientInfo clientfio,
                                             RSaleContract contract,
                                             SendContractSignedRequest request,
                                             SaleContract data) throws EOsagoSaveException {
        List<RContractSubject> subjectList = getSubjectList(contract.getContractId());
        ContentFormatted contentFormatted = new ContentFormatted();
        contentFormatted.setProduct(ALTCRAFT_PRODUCT);
        contentFormatted.setContractNumber(getSeriaNumberOfContract(contract));
        contentFormatted.setDateStartContractOld(contract.getBeginDate());
        contentFormatted.setDateEndContractOld(contract.getEndDate());
        contentFormatted.setUrl(createPassbookUrl(BigInteger.valueOf(contract.getContractId())));
        contentFormatted.setPolicyholder(getFirstLastNames(clientfio));
        contentFormatted.setObject(getMarkaModelNames(subjectList.get(0).getSubjectId()));
        contentFormatted.setInsuranceAmount(OSAGO_REWARD);
        contentFormatted.setInsurancePremium(formatInsurancePremium(contract.getContractPremium()));

        Long clientId = fillLoyaltyInfo(request, contentFormatted);

        fillOrangeInfo(contentFormatted, contract, data, clientId);
        return contentFormatted;
    }

    private Long fillLoyaltyInfo(SendContractSignedRequest request, ContentFormatted contentFormatted) {
        LoyaltyData loyaltyData = loyaltyClientDataService.findLoyaltyDataByClientEmail(request.getEmail());
        contentFormatted.setPoints(loyaltyData.getPoints());
        contentFormatted.setPointsStatus(loyaltyData.getStatus().getName());
        contentFormatted.setPointsPercentage(loyaltyData.getPercentage());
        return loyaltyData.getClientId();
    }

    private void fillOrangeInfo(ContentFormatted contentFormatted, RSaleContract contract, SaleContract data, Long clientId) {
        TransactionInfoResponse transactionInfo;
        try {
            transactionInfo = msOrangeClient.getTransactionInfo(contract.getContractSeria(), contract.getContractNumber());
        } catch (FeignException e) {
            log.error("Ошибка получения TransactionInfo => {}", e.toString());
            return;
        }
        if (transactionInfo != null) {
            data.setIdApelsin(Optional.ofNullable(transactionInfo.getUserInfo()).map(UserInfo::getPhone).orElse(null));

            Transaction lastPolicyBuyTransaction = Optional.ofNullable(transactionInfo.getTransactionList())
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(transaction -> "POLICY_BUY_OSAGO".equals(transaction.getCategory()))
                    .max(Comparator.comparing(Transaction::getDate))
                    .orElse(new Transaction());
            contentFormatted.setKolichestvoAp(lastPolicyBuyTransaction.getPoints());
            Optional.ofNullable(lastPolicyBuyTransaction.getDate()).map(LocalDateTime::toLocalDate).ifPresent(contentFormatted::setDataAp);
            contentFormatted.setVsegoAp(Optional.ofNullable(transactionInfo.getUserInfo())
                    .map(UserInfo::getBalance).orElse(0));

            if (Optional.ofNullable(transactionInfo.getReverseList()).orElse(new ArrayList<>()).size() == 1) {
                data.setProcentAp(transactionInfo.getReverseList().get(0).getAmount());
            }

            data.setClientId(clientId);
        }
    }

    @Override
    protected SaleContract fillDataField(ClientInfo fio) {
        return super.fillDataField(fio);
    }

    @Override
    protected Integer triggerId() {
        return TriggerConstants.THANKS_OSAGO_ORANGE;
    }

    private String getSeriaNumberOfContract(RSaleContract rSaleContract) {
        return rSaleContract.getContractSeria() + "&nbsp;" + rSaleContract.getContractNumber();
    }
}
