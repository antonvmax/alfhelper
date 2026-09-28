package ru.alfastrah.site.avto.ws.contact.signed.service.signing;

import jakarta.activation.DataHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.CryptoPro.JCP.JCP;
import ru.alfastrah.site.avto.ws.contact.signed.client.CertificateInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.SignatureClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignServiceInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignatureInfo;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SigningServiceTest {

    @Mock
    private SignatureClient signatureClient;

    @Mock
    private CertificateInfoClient infoClient;

    private SigningService signingService;

    @BeforeEach
    void setUp() {
        signingService = new SigningService(signatureClient, infoClient);
    }

    @Test
    @SneakyThrows
    void signFile_shouldReturnDataHandler_whenSuccessfulSigningWithoutStamps() {
        // given
        byte[] sourcePdf = loadTestPdf();
        List<StampParams> stampParams = Collections.emptyList();

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, false);
        given(infoClient.getInfo()).willReturn(infoResponse);

        byte[] mockSignature = new byte[]{1, 2, 3, 4};
        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willReturn(mockSignature);

        // when
        DataHandler result = signingService.signFile(sourcePdf, stampParams);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @SneakyThrows
    void signFile_shouldReturnDataHandler_whenStampVisible() {
        byte[] sourcePdf = loadTestPdf();
        StampParams stamp = new StampParams();
        stamp.setStampVisible(true);
        stamp.setLlx(100f);
        stamp.setLly(100f);
        stamp.setUrx(200f);
        stamp.setUry(200f);
        stamp.setFontSize(12);
        stamp.setPage(1);
        List<StampParams> stampParams = List.of(stamp);

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);

        byte[] mockSignature = new byte[]{1, 2, 3, 4};
        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willReturn(mockSignature);

        DataHandler result = signingService.signFile(sourcePdf, stampParams);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @SneakyThrows
    void signFile_shouldReturnDataHandler_whenStampNotVisible() {
        byte[] sourcePdf = loadTestPdf();
        StampParams stamp = new StampParams();
        stamp.setStampVisible(false);
        stamp.setLlx(100f);
        stamp.setLly(100f);
        stamp.setUrx(200f);
        stamp.setUry(200f);
        stamp.setFontSize(12);
        stamp.setPage(1);
        List<StampParams> stampParams = List.of(stamp);

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, false);
        given(infoClient.getInfo()).willReturn(infoResponse);

        byte[] mockSignature = new byte[]{1, 2, 3, 4};
        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willReturn(mockSignature);

        DataHandler result = signingService.signFile(sourcePdf, stampParams);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @SneakyThrows
    void signFile_shouldReturnNull_whenPdfReaderThrowsIOException() {
        // given
        byte[] invalidPdf = new byte[]{0, 1, 2, 3}; // not a valid PDF
        List<StampParams> stampParams = Collections.emptyList();

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, false);
        given(infoClient.getInfo()).willReturn(infoResponse);

        DataHandler result = signingService.signFile(invalidPdf, stampParams);

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    void signFile_shouldThrowRuntimeException_whenSignatureClientThrowsRuntimeException() {
        byte[] sourcePdf = loadTestPdf();
        List<StampParams> stampParams = Collections.emptyList();

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, false);
        given(infoClient.getInfo()).willReturn(infoResponse);

        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willThrow(new RuntimeException("Signing failed"));

        assertThatThrownBy(() -> signingService.signFile(sourcePdf, stampParams))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Signing failed");
    }

    @Test
    @SneakyThrows
    void signFile_shouldUseCorrectDigestAlgorithm_basedOnKeyAlgorithm() {
        byte[] sourcePdf = loadTestPdf();
        List<StampParams> stampParams = Collections.emptyList();

        String[] algorithms = {
                JCP.GOST_EL_2012_256_NAME,
                JCP.GOST_DH_2012_256_NAME,
                JCP.GOST_EL_2012_512_NAME,
                JCP.GOST_DH_2012_512_NAME
        };
        for (String algorithm : algorithms) {
            SignServiceInfoResponse infoResponse = createMockInfoResponse(algorithm, 8080, false);
            given(infoClient.getInfo()).willReturn(infoResponse);

            byte[] mockSignature = new byte[]{1, 2, 3, 4};
            given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                    .willReturn(mockSignature);

            DataHandler result = signingService.signFile(sourcePdf, stampParams);

            assertThat(result).isNotNull();
            org.mockito.Mockito.reset(infoClient, signatureClient);
        }
    }

    private SignServiceInfoResponse createMockInfoResponse(String algorithm, int port, boolean needStampFields) {
        SignatureInfo signatureInfo = mock(SignatureInfo.class);
        lenient().when(signatureInfo.getAlgorithm()).thenReturn(algorithm);
        if (needStampFields) {
            lenient().when(signatureInfo.getFingerprint()).thenReturn("test-fingerprint");
            lenient().when(signatureInfo.getExpirationDate()).thenReturn("2025-12-31");
            lenient().when(signatureInfo.getIssuer()).thenReturn("Test CA");
        } else {
            lenient().when(signatureInfo.getFingerprint()).thenReturn("");
            lenient().when(signatureInfo.getExpirationDate()).thenReturn("");
            lenient().when(signatureInfo.getIssuer()).thenReturn("");
        }

        SignServiceInfoResponse response = mock(SignServiceInfoResponse.class);
        lenient().when(response.getSignatureInfo()).thenReturn(signatureInfo);
        lenient().when(response.getPort()).thenReturn(port);
        return response;
    }

    private byte[] loadTestPdf() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("polis.pdf")) {
            if (is == null) {
                throw new IllegalStateException("Test resource polis.pdf not found");
            }
            return is.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test PDF", e);
        }
    }
}