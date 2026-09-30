package ru.alfastrah.site.avto.ws.partners.interaction.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.PayedContractResponse;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLOImpl;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.ContractResult;
import ru.alfastrah.site.avto.ws.partners.interaction.parameters.PartnerContract;

import java.math.BigInteger;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayedContractProcessorTest {

    private static final String VALID_UPID = "550e8400-e29b-41d4-a716-446655440000";
    private static final BigInteger CONTRACT_ID = BigInteger.valueOf(12345L);

    @Mock
    private PartnersPakDLOImpl partnersPakDLO;

    @Mock
    private PartnersPostgresService partnersPostgresService;

    private PayedContractProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PayedContractProcessor(partnersPakDLO, partnersPostgresService);
    }

    @Test
    void getContract_shouldReturnOracleResultAndCallPostgres() {
        PayedContractRequest request = new PayedContractRequest();
        request.setUPID(VALID_UPID);

        PartnerContract oracleContract = new PartnerContract();
        oracleContract.setContractId(CONTRACT_ID);
        oracleContract.setCode(BigInteger.ZERO);

        when(partnersPakDLO.getContractId(VALID_UPID)).thenReturn(oracleContract);
        when(partnersPostgresService.getContractId(any(UUID.class)))
                .thenReturn(ContractResult.builder().contractId(12345L).build());

        PayedContractResponse response = processor.getContract(request);

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(response).isNotNull();
        softy.assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
        softy.assertThat(response.getCode()).isEqualTo(BigInteger.ZERO);
        softy.assertAll();
    }

    @Test
    void getContract_whenPostgresServiceFails_shouldContinueWithOracleResult() {
        PayedContractRequest request = new PayedContractRequest();
        request.setUPID(VALID_UPID);

        PartnerContract oracleContract = new PartnerContract();
        oracleContract.setContractId(CONTRACT_ID);

        when(partnersPakDLO.getContractId(VALID_UPID)).thenReturn(oracleContract);
        doThrow(new RuntimeException("DB error")).when(partnersPostgresService)
                .getContractId(any(UUID.class));

        PayedContractResponse response = assertDoesNotThrow(() -> processor.getContract(request));

        assertNotNull(response);
    }

    @Test
    void getContract_whenOracleServiceFails_shouldThrowException() {
        PayedContractRequest request = new PayedContractRequest();
        request.setUPID(VALID_UPID);

        when(partnersPakDLO.getContractId(anyString()))
                .thenThrow(new RuntimeException("Oracle error"));

        assertThatThrownBy(() -> processor.getContract(request))
                .isInstanceOf(RuntimeException.class);
    }
}