package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.CBLoggerResponse;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.SiteRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CBLogClientTest {

    private final String cbLoggerUrl = "cbLoggingUrl";
    private final String authorization = "authorizaion";
    @Mock
    private RestTemplate restTemplate;
    private CBLogClient underTest;

    @BeforeEach
    void setUp() {
        underTest = new CBLogClient(restTemplate, cbLoggerUrl, authorization);
    }

    @Test
    void shouldMakeAuthorizedCall() {
        HttpEntity<SiteRecord> expectedEntity = this.buildEntity(authorization);
        CBLoggerResponse expectedResponse = new CBLoggerResponse();
        expectedResponse.setSuccess(true);
        given(restTemplate.exchange(cbLoggerUrl,
                HttpMethod.POST,
                expectedEntity,
                CBLoggerResponse.class,
                expectedEntity.getBody())).willReturn(ResponseEntity.ok(expectedResponse));

        assertThatNoException().isThrownBy(() -> underTest.logContractSignedRequest(expectedEntity.getBody()));
    }

    @Test
    void shouldNotThrowExceptionOnNot2xxStatus() {
        HttpEntity<SiteRecord> expectedEntity = this.buildEntity(authorization);
        CBLoggerResponse expectedResponse = new CBLoggerResponse();
        expectedResponse.setSuccess(true);
        given(restTemplate.exchange(cbLoggerUrl,
                HttpMethod.POST,
                expectedEntity,
                CBLoggerResponse.class,
                expectedEntity.getBody())).willReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(expectedResponse));

        assertThatNoException().isThrownBy(() -> underTest.logContractSignedRequest(expectedEntity.getBody()));
    }

    @Test
    void shouldNotThrowExceptionWhenIsNotSuccess() {
        HttpEntity<SiteRecord> expectedEntity = this.buildEntity(authorization);
        CBLoggerResponse expectedResponse = new CBLoggerResponse();
        expectedResponse.setSuccess(false);
        given(restTemplate.exchange(cbLoggerUrl,
                HttpMethod.POST,
                expectedEntity,
                CBLoggerResponse.class,
                expectedEntity.getBody())).willReturn(ResponseEntity.ok(expectedResponse));

        assertThatNoException().isThrownBy(() -> underTest.logContractSignedRequest(expectedEntity.getBody()));
    }

    @Test
    void shouldNotThrowExceptionOnEmptyResponse() {
        HttpEntity<SiteRecord> expectedEntity = this.buildEntity(authorization);
        given(restTemplate.exchange(cbLoggerUrl,
                HttpMethod.POST,
                expectedEntity,
                CBLoggerResponse.class,
                expectedEntity.getBody())).willReturn(ResponseEntity.ok(null));

        assertThatNoException().isThrownBy(() -> underTest.logContractSignedRequest(expectedEntity.getBody()));
    }

    private HttpEntity<SiteRecord> buildEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        SiteRecord request = new SiteRecord();
        request.setActionCode(1);
        return new HttpEntity<>(request, headers);
    }
}