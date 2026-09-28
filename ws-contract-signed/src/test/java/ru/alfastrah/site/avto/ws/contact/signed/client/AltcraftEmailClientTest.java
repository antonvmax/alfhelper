package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.interplat4.altcraft.model.Request;
import ru.alfastrah.interplat4.altcraft.model.Response;
import ru.alfastrah.site.avto.ws.contact.signed.client.impl.AltcraftEmailClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class AltcraftEmailClientTest {

    private static final String IMPORT_AND_START = "/api/v1.1/campaigns/triggers/import_and_start";
    private AltcraftEmailClient underTest;
    @Mock
    private RestTemplate restTemplate;
    @BeforeEach
    void setUp() {
        underTest = new AltcraftEmailClient(restTemplate);
    }

    @Test
    void shouldSendEmailWithGivenRequestData() {
        Request expectedRequest = new Request();
        HttpEntity<Request> expectedEntity = new HttpEntity<>(expectedRequest);
        Response expectedResponse = new Response();
        expectedResponse.setError(0);
        expectedResponse.setErrorText("none");
        given(restTemplate.postForObject(null + IMPORT_AND_START, expectedEntity, Response.class)).willReturn(expectedResponse);

        Response actualResponse = underTest.sendEmail(expectedRequest);

        verify(restTemplate, atMostOnce()).postForObject(null + IMPORT_AND_START, expectedRequest, Response.class);
        ArgumentCaptor<HttpEntity> actualRequest = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), actualRequest.capture(), any());
        assertThat(actualRequest.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }
}