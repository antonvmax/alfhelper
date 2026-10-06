package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RetryableCheckServiceTest {

    private RetryableCheckService retryableCheckService;

    @BeforeEach
    void setUp() {
        retryableCheckService = new RetryableCheckService();
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsServiceTemporarilyUnavailable() {
        String errorMessage = "Error: 503 service temporarily unavailable occurred";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsSigningException() {
        String errorMessage = "произошла ошибка подписи файла во время обработки";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsBadGateway() {
        String errorMessage = "Connection failed due to bad gateway error";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsIOError() {
        String errorMessage = "Network I/O error detected during processing";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsInternalServerError() {
        String errorMessage = "Request failed with internal server error";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsRetryableErrorInUpperCase() {
        String errorMessage = "NETWORK I/O ERROR OCCURRED";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsRetryableErrorInMixedCase() {
        String errorMessage = "Internal Server Error during processing";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenErrorMessageDoesNotContainRetryableErrors() {
        String errorMessage = "Invalid request parameters provided";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenErrorMessageIsEmpty() {
        String errorMessage = "";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenErrorMessageContainsOnlyNonRetryableText() {
        String errorMessage = "Authentication failed - invalid credentials";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenErrorMessageContainsMultipleRetryableErrors() {
        String errorMessage = "503 service temporarily unavailable due to internal server error";

        boolean result = retryableCheckService.isRetryable(errorMessage);

        assertTrue(result);
    }
}