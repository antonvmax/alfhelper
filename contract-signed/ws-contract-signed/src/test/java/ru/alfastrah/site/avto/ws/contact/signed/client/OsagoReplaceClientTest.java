package ru.alfastrah.site.avto.ws.contact.signed.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.alfastrah.site.avto.ws.contact.signed.model.RealContractResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class OsagoReplaceClientTest {

    private OsagoReplaceClient underTest;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        underTest = new OsagoReplaceClient(restTemplate);
    }

    @Test
    void shouldMakeCallToReplaceServiceWithGivenArguments() {
        Long expectedContractId = -12354L;
        Long actualContractId = 12354L;
        given(restTemplate.exchange("null?fakeContractId={1}", HttpMethod.GET, null, RealContractResponse.class, expectedContractId))
                .willReturn(ResponseEntity.of(Optional.of(new RealContractResponse().realContractId(actualContractId))));

        RealContractResponse partnerInfo = underTest.getPartnerInfo(expectedContractId);

        verify(restTemplate, times(1))
                .exchange("null?fakeContractId={1}", HttpMethod.GET, null, RealContractResponse.class, expectedContractId);

        ArgumentCaptor<Long> contractIdArgument = ArgumentCaptor.forClass(Long.class);
        verify(restTemplate)
                .exchange(any(), any(), any(), any(Class.class), contractIdArgument.capture());
        assertThat(contractIdArgument.getValue()).isEqualTo(expectedContractId);
        assertThat(partnerInfo.getRealContractId()).isEqualTo(actualContractId);
    }

    @Test
    void shouldReturnFakeIdWhenExceptionCaught() {
        Long expectedContractId = -12354L;
        given(restTemplate.exchange("null?fakeContractId={1}", HttpMethod.GET, null, RealContractResponse.class, expectedContractId))
                .willThrow(HttpClientErrorException.class);

        assertThatNoException().isThrownBy(() -> underTest.getPartnerInfo(expectedContractId));
        RealContractResponse partnerInfo = underTest.getPartnerInfo(expectedContractId);

        verify(restTemplate, times(2))
                .exchange("null?fakeContractId={1}", HttpMethod.GET, null, RealContractResponse.class, expectedContractId);

        assertThat(partnerInfo.getRealContractId()).isEqualTo(expectedContractId);
    }
}