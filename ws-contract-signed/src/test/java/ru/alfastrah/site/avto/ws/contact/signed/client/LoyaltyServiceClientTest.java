package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyBalanceResponse;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.LoyaltyClient;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.loyalty.SearchClientResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private LoyaltyServiceClient underTest;

    @BeforeEach
    void setUp() {
        underTest = new LoyaltyServiceClient(restTemplate);
        underTest.setDefaultAddress("localhost");
    }

    @Test
    void shouldMakePostCallWhenSearchingClient() {
        String email = null;
        HttpEntity<SearchClientRequest> request = new HttpEntity<>(new SearchClientRequest(email));
        SearchClientResponse clientResponse = new SearchClientResponse();
        LoyaltyClient client = new LoyaltyClient();
        client.setClientId(22L);
        clientResponse.setClients(List.of(client));
        given(restTemplate.postForObject("localhost/searchClient", request, SearchClientResponse.class)).willReturn(clientResponse);

        final SearchClientResponse actualResponse = underTest.searchClient(email);

        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(clientResponse);
    }

    @Test
    void shouldReturnEmptyResponseWhenExceptionOccurred() {
        String email = null;
        HttpEntity<SearchClientRequest> request = new HttpEntity<>(new SearchClientRequest(email));
        given(restTemplate.postForObject("localhost/searchClient", request, SearchClientResponse.class)).willThrow(HttpServerErrorException.class);

        final SearchClientResponse clientResponse = underTest.searchClient(email);

        assertThat(clientResponse).usingRecursiveComparison().isEqualTo(new SearchClientResponse());
    }

    @Test
    void shouldMakeGetCallWhenGettingClientBalanceInfo() {
        Long clientId = 123L;
        LoyaltyBalanceResponse loyaltyBalanceResponse = new LoyaltyBalanceResponse();

        given(restTemplate.getForObject("localhost/alfa/getBalance?clientId={clientId}", LoyaltyBalanceResponse.class, clientId)).willReturn(loyaltyBalanceResponse);

        final LoyaltyBalanceResponse clientBalance = underTest.findClientBalance(clientId);

        assertThat(clientBalance).usingRecursiveComparison().isEqualTo(loyaltyBalanceResponse);

    }

    @Test
    void shouldReturnEmptyBalanceInfoWhenExceptionOccurred() {
        Long clientId = 123L;
        given(restTemplate.getForObject("localhost/alfa/getBalance?clientId={clientId}", LoyaltyBalanceResponse.class, clientId)).willThrow(HttpServerErrorException.class);

        final LoyaltyBalanceResponse clientBalance = underTest.findClientBalance(clientId);

        assertThat(clientBalance).usingRecursiveComparison().isEqualTo(new LoyaltyBalanceResponse());
    }
}