package ru.alfastrah.site.avto.model.contract.signed.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("email_send_log")
public class EmailSendLogDto implements Persistable<Long> {

    @Id
    private Long id;

    private String requestHash;

    private BigInteger contractId;

    private LocalDateTime lastSendDate;

    private SendContractSignedRequest inputData;

    private Boolean isRetryable;

    private Phase status;

    private String error;

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public static EmailSendLogDto from(Long id,
                                       String hashCode,
                                       SendContractSignedRequest inputData,
                                       Boolean isRetryable,
                                       Phase status,
                                       String error
    ) {
        EmailSendLogDto emailSendLogDto = new EmailSendLogDto();
        emailSendLogDto.setId(id);
        emailSendLogDto.setRequestHash(hashCode);
        emailSendLogDto.setLastSendDate(LocalDateTime.now());
        emailSendLogDto.setContractId(inputData.getContractId());
        emailSendLogDto.setInputData(inputData);
        emailSendLogDto.setIsRetryable(isRetryable);
        emailSendLogDto.setStatus(status);
        emailSendLogDto.setError(error);
        return emailSendLogDto;
    }
}
