package ru.alfastrah.site.avto.ws.contact.signed.service.signing;

import jakarta.activation.DataHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.CryptoPro.JCP.JCP;
import ru.alfastrah.site.avto.model.contract.signed.exception.FileSigningException;
import ru.alfastrah.site.avto.ws.contact.signed.client.CertificateInfoClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.SignatureClient;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignServiceInfoResponse;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.SignatureInfo;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp.SignatureRepository;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorToleranceSigningServiceTest {

    @Mock
    private SignatureClient signatureClient;

    @Mock
    private CertificateInfoClient infoClient;

    @Mock
    private SignatureRepository signatureRepository;

    private ErrorToleranceSigningService errorToleranceSigningService;

    @BeforeEach
    void setUp() {
        errorToleranceSigningService = new ErrorToleranceSigningService(signatureClient, infoClient, signatureRepository);
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

    @Test
    @SneakyThrows
    void sign_shouldReturnOriginalPdf_whenStampParamsEmpty() {
        byte[] sourcePdf = loadTestPdf();
        List<StampParams> stampParams = Collections.emptyList();
        BigInteger contractId = BigInteger.ONE;

        DataHandler result = errorToleranceSigningService.sign(sourcePdf, stampParams, contractId);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @SneakyThrows
    void sign_shouldReturnSignedPdf_whenSigningSucceeds() {
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
        BigInteger contractId = BigInteger.ONE;

        given(signatureRepository.getConfigValue("sendUnsignedPolicies")).willReturn("false");
        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);
        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willReturn(new byte[]{1, 2, 3, 4});

        DataHandler result = errorToleranceSigningService.sign(sourcePdf, stampParams, contractId);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
        verify(infoClient).getInfo();
        verify(signatureClient).signBytes(any(byte[].class), eq(8080));
    }

    @Test
    @SneakyThrows
    void sign_shouldReturnOriginalPdfAndLog_whenSigningFailsAndSendFailedFilesTrue() {
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
        BigInteger contractId = BigInteger.ONE;

        given(signatureRepository.getConfigValue("sendUnsignedPolicies")).willReturn("true");
        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);
        given(signatureClient.signBytes(any(byte[].class), eq(8080)))
                .willThrow(new RuntimeException("Signing error"));

        DataHandler result = errorToleranceSigningService.sign(sourcePdf, stampParams, contractId);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
        verify(signatureRepository).logNotSignedPolicy(contractId);
    }

    @Test
    @SneakyThrows
    void sign_shouldThrowFileSigningException_whenSigningFailsAndSendFailedFilesFalse() {
        byte[] sourcePdf = loadTestPdf();
        StampParams stamp = new StampParams();
        stamp.setStampVisible(true);
        List<StampParams> stampParams = List.of(stamp);
        BigInteger contractId = BigInteger.ONE;

        given(signatureRepository.getConfigValue("sendUnsignedPolicies")).willReturn("false");
        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);

        assertThatThrownBy(() -> errorToleranceSigningService.sign(sourcePdf, stampParams, contractId))
                .isInstanceOf(FileSigningException.class)
                .hasMessageContaining("Ошибка подписи файла");
        verify(signatureRepository, never()).logNotSignedPolicy(any());
    }

    @Test
    @SneakyThrows
    void sign_withExplicitSendFailedFilesTrue_shouldReturnOriginalPdfAndLogOnFailure() {
        byte[] sourcePdf = loadTestPdf();
        StampParams stamp = new StampParams();
        stamp.setStampVisible(true);
        List<StampParams> stampParams = List.of(stamp);
        BigInteger contractId = BigInteger.ONE;
        boolean sendFailedFiles = true;

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);

        DataHandler result = errorToleranceSigningService.sign(sourcePdf, stampParams, sendFailedFiles, contractId);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
        verify(signatureRepository).logNotSignedPolicy(contractId);
    }

    @Test
    @SneakyThrows
    void sign_withExplicitSendFailedFilesFalse_shouldThrowFileSigningException() {
        byte[] sourcePdf = loadTestPdf();
        StampParams stamp = new StampParams();
        stamp.setStampVisible(true);
        List<StampParams> stampParams = List.of(stamp);
        BigInteger contractId = BigInteger.ONE;
        boolean sendFailedFiles = false;

        SignServiceInfoResponse infoResponse = createMockInfoResponse(JCP.GOST_EL_2012_256_NAME, 8080, true);
        given(infoClient.getInfo()).willReturn(infoResponse);

        assertThatThrownBy(() -> errorToleranceSigningService.sign(sourcePdf, stampParams, sendFailedFiles, contractId))
                .isInstanceOf(FileSigningException.class)
                .hasMessageContaining("Ошибка подписи файла");
        verify(signatureRepository, never()).logNotSignedPolicy(any());
    }
}