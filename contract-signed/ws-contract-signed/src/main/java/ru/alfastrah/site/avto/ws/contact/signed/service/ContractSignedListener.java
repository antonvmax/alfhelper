package ru.alfastrah.site.avto.ws.contact.signed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.MessageProperies;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.process.ContractSignedProcessService;

@Service
public class ContractSignedListener {

    private final ObjectMapper objectMapper;
    private final ContractSignedProcessService contractSignedProcessService;
    private final MessageProperies messageProperies;

    public ContractSignedListener(ObjectMapper objectMapper,
                                  ContractSignedProcessService contractSignedProcessService,
                                  MessageProperies messageProperies) {
        this.objectMapper = objectMapper;
        this.contractSignedProcessService = contractSignedProcessService;
        this.messageProperies = messageProperies;
    }

    @JmsListener(destination = "sendContractSigned")
    public void printMessage(String contractSignedRequest,
                             @Header(JmsHeaders.MESSAGE_ID) String messageId,
                             @Header(name = "SID", defaultValue = "") String sId,
                             @Header(name = "traceId", required = false) String traceId) throws JsonProcessingException {
        try {
            if (StringUtils.isNotEmpty(messageId)) {
                messageProperies.setsID(messageId);
            }
            if (StringUtils.isNotEmpty(sId)) {
                messageProperies.setsID(sId);
            }
            if (StringUtils.isNotEmpty(traceId)) {
                MDC.put("traceId", traceId); // Устанавливаем traceId в MDC
            }

            contractSignedProcessService.guaranteedProcessSend(objectMapper.readValue(contractSignedRequest, SendContractSignedRequest.class));
        } finally {
            MDC.remove("traceId");
        }
    }
}