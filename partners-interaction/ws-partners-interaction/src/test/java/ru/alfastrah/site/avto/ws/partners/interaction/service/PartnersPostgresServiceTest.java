package ru.alfastrah.site.avto.ws.partners.interaction.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import tops.unicus.usr.RSaleContract;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.ContractResult;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.PartnerCalculation;
import ru.alfastrah.site.avto.ws.partners.interaction.mapper.PartnersPostgresMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnersPostgresServiceTest {

    private static final UUID TEST_UPID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String CALLER_CODE = "testCaller";
    private static final String CALC_ID = "5d67a1c6-1998-4714-882e-98104b1c7931";
    private static final Long CONTRACT_ID = 12345L;
    private static final Long CALCULATION_ID = 999L;

    @Mock
    private PartnersPostgresMapper mapper;

    @Mock
    private UnicusUsrService unicusService;

    private PartnersPostgresService service;

    @BeforeEach
    void setUp() {
        service = new PartnersPostgresService(mapper, unicusService);
    }

    @Test
    void saveUpid_shouldCallMapperAndComplete() {
        assertDoesNotThrow(() -> service.saveUpid(TEST_UPID, CALLER_CODE));

        verify(mapper).insertPartnerIdentifier(TEST_UPID, CALLER_CODE);
    }

    @Test
    void saveUpid_whenMapperThrows_shouldThrowRuntimeException() {
        doThrow(new RuntimeException("DB error")).when(mapper)
                .insertPartnerIdentifier(any(UUID.class), any(String.class));

        assertThatThrownBy(() -> service.saveUpid(TEST_UPID, CALLER_CODE))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ошибка выполнения saveUpid");
    }

    @Test
    void linkActionToUpid_withNullContractId_shouldCreateCalculation() {
        assertDoesNotThrow(() -> service.linkActionToUpid(TEST_UPID, CALC_ID, null));

        verify(mapper).insertPartnerCalculation(any(PartnerCalculation.class));
    }

    @Test
    void linkActionToUpid_withContractId_shouldAttachContract() {
        PartnerCalculation existing = PartnerCalculation.builder()
                .calculationId(CALCULATION_ID)
                .build();
        when(mapper.findByUpidAndCalcId(TEST_UPID, CALC_ID)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> service.linkActionToUpid(TEST_UPID, CALC_ID, CONTRACT_ID));

        verify(mapper).updateContractId(CALCULATION_ID, CONTRACT_ID);
    }

    @Test
    void linkActionToUpid_whenContractAlreadyExists_shouldThrowException() {
        PartnerCalculation existing = PartnerCalculation.builder()
                .calculationId(CALCULATION_ID)
                .contractId(999L).build();
        when(mapper.findByUpidAndCalcId(TEST_UPID, CALC_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.linkActionToUpid(TEST_UPID, CALC_ID, CONTRACT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ошибка выполнения linkActionToUpid")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void linkActionToUpid_whenRecordNotFound_shouldThrowException() {
        when(mapper.findByUpidAndCalcId(TEST_UPID, CALC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.linkActionToUpid(TEST_UPID, CALC_ID, CONTRACT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ошибка выполнения linkActionToUpid")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void getContractId_whenContractNotFound_shouldReturnCodeZero() {
        when(mapper.findContractIdByUpid(TEST_UPID)).thenReturn(Optional.empty());

        ContractResult result = service.getContractId(TEST_UPID);

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(result).isNotNull();
        softy.assertThat(result.getContractId()).isNull();
        softy.assertThat(result.getCode()).isEqualTo(0);
        softy.assertThat(result.getMessage()).isEqualTo("Договор еще не создан в системе");
        softy.assertAll();
    }

    @Test
    void getContractId_whenUnicusReturnsNull_shouldReturnCodeZero() {
        when(mapper.findContractIdByUpid(TEST_UPID)).thenReturn(Optional.of(CONTRACT_ID));
        when(unicusService.getFullSaleContract(CONTRACT_ID)).thenReturn(null);

        ContractResult result = service.getContractId(TEST_UPID);

        assertEquals(0, result.getCode());
        assertEquals("Договор еще не создан в системе", result.getMessage());
    }

    @Test
    void getContractId_whenContractNotPaid_shouldReturnCodeOne() {
        RSaleContract contract = new RSaleContract();
        contract.setContractStatusTypeId(1);

        when(mapper.findContractIdByUpid(TEST_UPID)).thenReturn(Optional.of(CONTRACT_ID));
        when(unicusService.getFullSaleContract(CONTRACT_ID)).thenReturn(contract);

        ContractResult result = service.getContractId(TEST_UPID);

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(result.getCode()).isEqualTo(1);
        softy.assertThat(result.getMessage()).isEqualTo("Договор еще не оплачен");
        softy.assertThat(result.getContractId()).isNull();
        softy.assertAll();
    }

    @Test
    void getContractId_whenContractPaid_shouldReturnSuccess() {
        RSaleContract contract = new RSaleContract();
        contract.setContractStatusTypeId(5);

        when(mapper.findContractIdByUpid(TEST_UPID)).thenReturn(Optional.of(CONTRACT_ID));
        when(unicusService.getFullSaleContract(CONTRACT_ID)).thenReturn(contract);

        ContractResult result = service.getContractId(TEST_UPID);

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(result.getContractId()).isEqualTo(CONTRACT_ID);
        softy.assertThat(result.getCode()).isNull();
        softy.assertThat(result.getMessage()).isNull();
        softy.assertAll();
    }

    @Test
    void getContractId_whenMapperThrows_shouldThrowRuntimeException() {
        when(mapper.findContractIdByUpid(TEST_UPID))
                .thenThrow(new RuntimeException("DB connection failed"));

        assertThatThrownBy(() -> service.getContractId(TEST_UPID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Ошибка при поиске договора для UPID");
    }
}