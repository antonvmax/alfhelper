package ru.alfastrah.site.avto.ws.contact.signed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedResponse;

import java.util.UUID;

@Service
public class SendContractSignedRequestRegister {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final EmailSendLogService emailSendLogService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SendContractSignedRequestRegister.class);

    public SendContractSignedRequestRegister(JmsTemplate jmsTemplate, ObjectMapper objectMapper, EmailSendLogService emailSendLogService) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.emailSendLogService = emailSendLogService;
    }

    public SendContractSignedResponse registerContractSignedRequest(SendContractSignedRequest contractSignedRequest) {
        SendContractSignedResponse contractSignedResponse = new SendContractSignedResponse();
        try {
            String traceId = getTraceId();

            jmsTemplate.convertAndSend(
                    "sendContractSigned",
                    objectMapper.writeValueAsString(contractSignedRequest),
                    message -> {
                        message.setStringProperty("traceId", traceId);
                        return message;
                    });
        } catch (JsonProcessingException e) {
            LOGGER.error("Ошибка отправки запроса в очередь: {0}", e);
            emailSendLogService.handleException(contractSignedRequest, true, e.getMessage());
            return contractSignedResponse;
        }
        contractSignedResponse.setResult(true);
        return contractSignedResponse;
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }

        return traceId;
    }
}
