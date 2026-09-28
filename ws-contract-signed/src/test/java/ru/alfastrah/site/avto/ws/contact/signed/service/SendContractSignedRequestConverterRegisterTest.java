package ru.alfastrah.site.avto.ws.contact.signed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedResponse;

import java.math.BigInteger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendContractSignedRequestRegisterTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EmailSendLogService emailSendLogService;

    private SendContractSignedRequestRegister underTest;

    @BeforeEach
    void setUp() {
        underTest = new SendContractSignedRequestRegister(jmsTemplate, objectMapper, emailSendLogService);
    }

    @Test
    void shouldReturnResponseWithTrueWhenRequestIsRegistered() throws JsonProcessingException {
        SendContractSignedRequest expectedSendContractSignedRequest = new SendContractSignedRequest();
        expectedSendContractSignedRequest.setContractId(BigInteger.TEN);
        expectedSendContractSignedRequest.setEmail("email");
        doNothing().when(jmsTemplate).convertAndSend(anyString(), anyString(), any(MessagePostProcessor.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("");

        SendContractSignedResponse actualResponse = underTest.registerContractSignedRequest(expectedSendContractSignedRequest);

        verify(jmsTemplate, atMostOnce()).convertAndSend(anyString(), anyString());
        ArgumentCaptor<SendContractSignedRequest> actualRequest = ArgumentCaptor.forClass(SendContractSignedRequest.class);
        verify(objectMapper).writeValueAsString(actualRequest.capture());
        assertThat(actualRequest.getValue()).isEqualTo(expectedSendContractSignedRequest);
        assertThat(actualResponse.isResult()).isTrue();
    }

    @Test
    void shouldReturnResponseWithFalseWhenExceptionIsCaught() throws JsonProcessingException {
        SendContractSignedRequest expectedSendContractSignedRequest = new SendContractSignedRequest();
        expectedSendContractSignedRequest.setContractId(BigInteger.TEN);
        expectedSendContractSignedRequest.setEmail("email");
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        SendContractSignedResponse actualResponse = underTest.registerContractSignedRequest(expectedSendContractSignedRequest);

        verify(jmsTemplate, atMostOnce()).convertAndSend(anyString(), anyString());
        ArgumentCaptor<SendContractSignedRequest> actualRequest = ArgumentCaptor.forClass(SendContractSignedRequest.class);
        verify(objectMapper).writeValueAsString(actualRequest.capture());
        assertThat(actualRequest.getValue()).isEqualTo(expectedSendContractSignedRequest);
        assertThat(actualResponse.isResult()).isFalse();
    }
}