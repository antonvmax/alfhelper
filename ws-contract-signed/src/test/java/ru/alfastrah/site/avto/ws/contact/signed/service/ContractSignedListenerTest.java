package ru.alfastrah.site.avto.ws.contact.signed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.ws.contact.signed.model.MessageProperies;
import ru.alfastrah.site.avto.ws.contact.signed.service.send.process.ContractSignedProcessService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;

@ExtendWith(MockitoExtension.class)
class ContractSignedListenerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ContractSignedProcessService contractSignedProcessService;
    @Mock
    private MessageProperies messageProperies;
    private ContractSignedListener underTest;

    @BeforeEach
    void setUp() {
        underTest = new ContractSignedListener(objectMapper, contractSignedProcessService, messageProperies);
    }

    @Test
    void shouldProcessRequestWhenReceived() throws JsonProcessingException {
        String expectedRequest = "123";
        when(objectMapper.readValue(expectedRequest, SendContractSignedRequest.class)).thenReturn(new SendContractSignedRequest());
        underTest.printMessage(expectedRequest, "", "", "");

        ArgumentCaptor<String> actualRequest = ArgumentCaptor.forClass(String.class);
        verify(objectMapper).readValue(actualRequest.capture(), eq(SendContractSignedRequest.class));
        assertThat(actualRequest.getValue()).isEqualTo(expectedRequest);
        verify(contractSignedProcessService, atMostOnce()).processSendContractSigned(any());
    }

    @Test
    void shouldSetIdWithMessageIdWhenReceived() throws JsonProcessingException {

        underTest.printMessage("", "1", "", "");

        ArgumentCaptor<String> actualId = ArgumentCaptor.forClass(String.class);
        verify(messageProperies).setsID(actualId.capture());
        assertThat(actualId.getValue()).isEqualTo("1");
        verify(messageProperies, atMostOnce()).setsID(anyString());
    }

    @Test
    void shouldSetIdWithSIDWhenReceived() throws JsonProcessingException {

        underTest.printMessage("", "", "2", "");

        ArgumentCaptor<String> actualId = ArgumentCaptor.forClass(String.class);
        verify(messageProperies).setsID(actualId.capture());
        assertThat(actualId.getValue()).isEqualTo("2");
        verify(messageProperies, atMostOnce()).setsID(anyString());
    }

    @Test
    void shouldNotSetIdWithSIDWhenReceivedNullValue() throws JsonProcessingException {

        underTest.printMessage("", "", null, "");

        verify(messageProperies, noInteractions()).setsID(anyString());
    }
}