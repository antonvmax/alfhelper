package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.client.LoyaltyServiceClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyBalanceResponse;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyData;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

@ExtendWith(MockitoExtension.class)
class LoyaltyClientDataServiceTest {

    @Mock
    private LoyaltyServiceClient loyaltyClient;

    private LoyaltyClientDataService underTest;
    @BeforeEach
    void setUp() {
        underTest = new LoyaltyClientDataService(loyaltyClient);
    }

    @Test
    void shouldReturnEmptyDataWhenNoEmailGiven() {
        String blankEmail = "";
        String nullEmail = null;
        LoyaltyData expectedResponse = new LoyaltyData();

        LoyaltyData actualResultOnBlankEmail = underTest.findLoyaltyDataByClientEmail(blankEmail);
        LoyaltyData actualResultOnNullEmail = underTest.findLoyaltyDataByClientEmail(nullEmail);

        assertThat(actualResultOnBlankEmail).usingRecursiveComparison().isEqualTo(expectedResponse);
        assertThat(actualResultOnNullEmail).usingRecursiveComparison().isEqualTo(expectedResponse);
        verify(loyaltyClient, noInteractions()).searchClient(anyString());
    }

    @Test
    void shouldFindClientIdAndRequestLoyaltyDataWhenEmailProvided() {
        String email = "mail@mail.ru";
        LoyaltyData expectedLoyaltyResult = new LoyaltyData(22L, LoyaltyData.LoyaltyStatus.SILVER, "3", 2L);
        SearchClientResponse clientResponse = new SearchClientResponse();
        LoyaltyClient client = new LoyaltyClient();
        Long clientId = 2L;
        client.setClientId(clientId);
        clientResponse.setClients(List.of(client));
        given(loyaltyClient.searchClient(email)).willReturn(clientResponse);
        LoyaltyBalanceResponse balanceResponse = new LoyaltyBalanceResponse();
        balanceResponse.setPercent("3");
        balanceResponse.setStatus(LoyaltyData.LoyaltyStatus.SILVER.getName());
        balanceResponse.setAmountPoints(22L);
        given(loyaltyClient.findClientBalance(clientId)).willReturn(balanceResponse);

        final LoyaltyData actualResult = underTest.findLoyaltyDataByClientEmail(email);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedLoyaltyResult);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(loyaltyClient).searchClient(emailCaptor.capture());
        assertThat(email).isEqualTo(emailCaptor.getValue());
        ArgumentCaptor<Long> clientIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(loyaltyClient).findClientBalance(clientIdCaptor.capture());
        assertThat(clientIdCaptor.getValue()).isEqualTo(clientId);
    }

    @Test
    void shouldReturnEmptyDataWhenNoClientIsFound() {
        given(loyaltyClient.searchClient(anyString())).willReturn(new SearchClientResponse());

        LoyaltyData actualResult = underTest.findLoyaltyDataByClientEmail("email");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(new LoyaltyData());
        verify(loyaltyClient, times(0)).findClientBalance(any());
    }
}