package ru.alfastrah.site.avto.ws.contact.signed.service.send.request;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.ContentFormatted;
import ru.alfastrah.interplat4.altcraft.model.osago.SaleContract;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.mailing.MailingClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.MsOrangeClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.Transaction;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.TransactionInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.orange.model.UserInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfoFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.AttachmentFactory;
import tops.unicus.usr.RContractPolicy;
import tops.unicus.usr.RContractSubject;
import tops.unicus.usr.RContractVariantCondition;
import tops.unicus.usr.RSaleContract;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class KaskoRequestFactory extends RequestFactory {

    private static final String PRODUCT = "kasko_thanks";
    private static final Integer TRIGGER_ID = 5332;
    private static final long INSURANCE_OBJECT_TYPE_ID_KASKO = 173;
    private static final Set<String> KASKO_BUY_CATEGORIES = Set.of("POLICY_BUY_KASKO_MINI", "POLICY_BUY_KASKO_FULL");

    private final MsOrangeClient msOrangeClient;

    protected KaskoRequestFactory(ClientInfoFactory clientInfoFactory,
                                  SubscriptionFactory subscriptionFactory,
                                  UnicusUsrService clientUsr,
                                  UnicusSubjectService clientSubject,
                                  AttachmentFactory attachmentFactory,
                                  MailingClient mailingClient,
                                  MsOrangeClient msOrangeClient) {
        super(clientInfoFactory, subscriptionFactory, clientUsr, clientSubject, attachmentFactory, mailingClient);
        this.msOrangeClient = msOrangeClient;
    }

    @Override
    protected ContentFormatted createContent(ClientInfo clientInfo, RSaleContract contract, SendContractSignedRequest request, SaleContract data) {
        List<RContractSubject> subjectList = getSubjectList(request.getContractId().longValue());

        ContentFormatted contentFormatted = new ContentFormatted();
        contentFormatted.setProduct(PRODUCT);
        contentFormatted.setContractNumber(contract.getContractNumber());
        contentFormatted.setDateStartContractOld(contract.getActionBeginDate());
        contentFormatted.setDateEndContractOld(contract.getActionEndDate());
        contentFormatted.setUrl(createPassbookUrl(request.getContractId()));
        contentFormatted.setPolicyholder(getFirstLastNames(clientInfo));
        contentFormatted.setObject(getMarkaModelNames(subjectList.get(0)));
        contentFormatted.setInsuranceAmount(getInsuranceAmount(contract));
        contentFormatted.setInsurancePremium(contract.getContractPremium().toString());
        fillOrangeInfo(contentFormatted, contract);
        return contentFormatted;
    }

    private void fillOrangeInfo(ContentFormatted contentFormatted, RSaleContract contract) {
        TransactionInfoResponse transactionInfo;
        try {
            transactionInfo = msOrangeClient.getTransactionInfoByNumber(contract.getContractNumber());
        } catch (FeignException e) {
            log.error("Ошибка получения TransactionInfo => {}", e.toString());
            return;
        }
        if (transactionInfo == null) {
            return;
        }
        Transaction lastPolicyBuyTransaction = Optional.ofNullable(transactionInfo.getTransactionList())
                .orElse(new ArrayList<>())
                .stream()
                .filter(transaction -> KASKO_BUY_CATEGORIES.contains(transaction.getCategory()))
                .max(Comparator.comparing(Transaction::getDate))
                .orElse(new Transaction());
        contentFormatted.setKolichestvoAp(lastPolicyBuyTransaction.getPoints());
        Optional.ofNullable(lastPolicyBuyTransaction.getDate())
                .map(LocalDateTime::toLocalDate)
                .ifPresent(contentFormatted::setDataAp);
        contentFormatted.setVsegoAp(Optional.ofNullable(transactionInfo.getUserInfo())
                .map(UserInfo::getBalance).orElse(0));
    }

    private String getInsuranceAmount(RSaleContract contract) {
        RContractVariantCondition condition = contract.getVariants().get(0).getConditions()
                .stream().filter(x -> INSURANCE_OBJECT_TYPE_ID_KASKO == x.getInsuranceObjectTypeId())
                .findFirst().orElse(null);

        return Optional.ofNullable(condition)
                .map(RContractVariantCondition::getPolicyList)
                .map(x -> x.get(0))
                .map(RContractPolicy::getInsuranceLimit)
                .map(Objects::toString)
                .orElse(contract.getInsuranceLimit().toString());
    }

    private String getMarkaModelNames(RContractSubject subjectId) {
        final String[] subjectFields = subjectId.getSubjectName().split(" ");
        String transportMark = subjectFields[0].toLowerCase();
        String transportModel = subjectFields[1].toLowerCase();

        return StringUtils.capitalize(transportMark) + " " + StringUtils.capitalize(transportModel);
    }

    @Override
    protected Integer triggerId() {
        return TRIGGER_ID;
    }
}
