package ru.alfastrah.site.avto.ws.contact.signed.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.client.LoyaltyServiceClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyBalanceResponse;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyData;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientResponse;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class LoyaltyClientDataService {

    private final LoyaltyServiceClient loyaltyServiceClient;

    public LoyaltyClientDataService(LoyaltyServiceClient loyaltyServiceClient) {
        this.loyaltyServiceClient = loyaltyServiceClient;
    }

    public LoyaltyData findLoyaltyDataByClientEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            log.info("Не передан email клиента для поиска в системе лояльности");
            return new LoyaltyData();
        }

        final SearchClientResponse searchClientResponse = loyaltyServiceClient.searchClient(email);
        Optional<LoyaltyClient> client = Optional.ofNullable(searchClientResponse.getClients()).orElse(new ArrayList<>()).stream().findFirst();
        LoyaltyData loyaltyData = new LoyaltyData();
        if (client.isPresent()) {
            final LoyaltyBalanceResponse clientBalance = loyaltyServiceClient.findClientBalance(client.get().getClientId());
            loyaltyData.setPoints(clientBalance.getAmountPoints());
            loyaltyData.setPercentage(clientBalance.getPercent());
            loyaltyData.setStatus(LoyaltyData.LoyaltyStatus.findByName(clientBalance.getStatus()));
            loyaltyData.setClientId(client.get().getClientId());
        }

        return loyaltyData;
    }

}
