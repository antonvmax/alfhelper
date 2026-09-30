package ru.alfastrah.site.avto.ws.partners.interaction.validator;

import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.site.avto.model.partners.interaction.dto.LinkActionRequest;
import ru.alfastrah.site.avto.model.partners.interaction.dto.SearchByUpidAndContractIdRequest;


@Component
public class RequestValidator {

    public void validate(PayedContractRequest request) throws ValidationException {
        if (StringUtils.isBlank(request.getUPID())) {
            throw new ValidationException("Не указан UPID");
        }
    }

    public void validate(UPIDRequest request) throws ValidationException {
        if (StringUtils.isBlank(request.getCallerCode())) {
            throw new ValidationException("Не указан callerCode");
        }
    }

    public void validate(LinkActionRequest request) {
        if (StringUtils.isBlank(request.getUpid())) {
            throw new ValidationException("Не указан upid");
        }
        if (StringUtils.isBlank(request.getCalculationId())) {
            throw new ValidationException("Не указан calculationId");
        }
    }

    public void validate(SearchByUpidAndContractIdRequest request) {
        if (StringUtils.isBlank(request.getUpid())) {
            throw new ValidationException("Не указан upid");
        }
        if (request.getContractId() == null) {
            throw new ValidationException("Не указан contractId");
        }
    }
}
