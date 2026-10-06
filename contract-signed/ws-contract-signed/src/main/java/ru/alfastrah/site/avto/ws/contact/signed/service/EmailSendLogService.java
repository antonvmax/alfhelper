package ru.alfastrah.site.avto.ws.contact.signed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.model.EmailSendLogDto;
import ru.alfastrah.site.avto.model.contract.signed.model.Phase;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.db.repository.EmailSendLogRepository;
import ru.alfastrah.site.avto.ws.contact.signed.utils.HashUtil;

import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendLogService {

    private final EmailSendLogRepository repo;
    private final RetryableCheckService checkService;
    private final HashUtil hashUtil;

    public void handleException(SendContractSignedRequest request, Boolean isRetryable, String message) {
        Long id;

        try {
            String hashCode = hashUtil.getHashCode(request);
            id = getExistingId(hashCode);

            isRetryable = isRetryable == null ? isRetryable(message) : isRetryable;
            repo.save(EmailSendLogDto.from(id, hashCode, request, isRetryable, Phase.FAILED, message));
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            log.error("Не удалось получить хеш-запроса: {}", e.getMessage());
            repo.save(EmailSendLogDto.from(null, null, request, false, Phase.FAILED, message));
        }
    }

    public void handleSuccess(SendContractSignedRequest request) {
        try {
            String hashCode = hashUtil.getHashCode(request);
            Long id = getExistingId(hashCode);
            if (id != null) {
                repo.deleteByRequestHash(hashCode);
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении записей в email_send_log после успешной отправки письма: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Long getExistingId(String hashCode) {
        return repo.findByRequestHash(hashCode)
                .map(EmailSendLogDto::getId)
                .orElse(null);
    }

    private boolean isRetryable(String errorMessage) {
        return checkService.isRetryable(errorMessage);
    }
}
