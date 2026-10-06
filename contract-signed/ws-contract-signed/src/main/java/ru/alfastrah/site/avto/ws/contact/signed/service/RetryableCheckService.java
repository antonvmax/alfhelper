package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetryableCheckService {

    private static final String SERVICE_TEMPORARILY_UNAVAILABLE = "503 service temporarily unavailable";

    private static final String SIGNING_EXCEPTION = "ошибка подписи файла";

    private static final String BAD_GATEWAY = "bad gateway";

    private static final String IO_ERROR = "i/o error";

    private static final String INTERNAL_SERVER_ERROR = "internal server error";

    private static final List<String> retryAbleErrors = List.of(
            SERVICE_TEMPORARILY_UNAVAILABLE,
            SIGNING_EXCEPTION,
            BAD_GATEWAY,
            IO_ERROR,
            INTERNAL_SERVER_ERROR);

    public boolean isRetryable(String errorMessage) {
        return retryAbleErrors.stream()
                .anyMatch(str -> errorMessage.toLowerCase().contains(str.toLowerCase()));
    }
}