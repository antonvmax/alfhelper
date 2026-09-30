package ru.alfastrah.site.avto.ws.partners.interaction.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchByUpidAndContractServiceTest {

    private static final String VALID_UPID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String INVALID_UPID = "invalid-uuid";
    private static final Long CONTRACT_ID = 12345L;
    private static final Long PG_RESULT = 100L;
    private static final Long ORA_RESULT = 200L;

    @Mock
    private PartnersPakDLO partnersPakDLO;

    @Mock
    private PartnersPostgresService partnersPostgresService;

    private SearchByUpidAndContractService service;

    @BeforeEach
    void setUp() {
        service = new SearchByUpidAndContractService(partnersPakDLO, partnersPostgresService);
    }

    @Test
    void searchByUpidAndContractId_whenPostgresReturnsResult_shouldReturnPostgresResult() {
        when(partnersPostgresService.searchByUpidAndContractId(any(UUID.class), anyLong()))
                .thenReturn(PG_RESULT);

        Long result = service.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);

        assertEquals(PG_RESULT, result);
        verify(partnersPostgresService).searchByUpidAndContractId(UUID.fromString(VALID_UPID), CONTRACT_ID);
        verify(partnersPakDLO, never()).searchByUpidAndContractId(anyString(), anyLong());
    }

    @Test
    void searchByUpidAndContractId_whenPostgresReturnsNull_shouldFallbackToOracle() {
        when(partnersPostgresService.searchByUpidAndContractId(any(UUID.class), anyLong()))
                .thenReturn(null);
        when(partnersPakDLO.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID))
                .thenReturn(ORA_RESULT);

        Long result = service.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);

        assertEquals(ORA_RESULT, result);
        verify(partnersPakDLO).searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);
    }

    @Test
    void searchByUpidAndContractId_withInvalidUpid_shouldFallbackToOracle() {
        when(partnersPakDLO.searchByUpidAndContractId(INVALID_UPID, CONTRACT_ID))
                .thenReturn(ORA_RESULT);

        Long result = service.searchByUpidAndContractId(INVALID_UPID, CONTRACT_ID);

        assertEquals(ORA_RESULT, result);
        verify(partnersPostgresService, never()).searchByUpidAndContractId(any(UUID.class), anyLong());
        verify(partnersPakDLO).searchByUpidAndContractId(INVALID_UPID, CONTRACT_ID);
    }

    @Test
    void searchByUpidAndContractId_whenPostgresServiceFails_shouldFallbackToOracle() {
        when(partnersPostgresService.searchByUpidAndContractId(any(UUID.class), anyLong()))
                .thenThrow(new RuntimeException("Database error"));
        when(partnersPakDLO.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID))
                .thenReturn(ORA_RESULT);

        Long result = service.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);

        assertEquals(ORA_RESULT, result);
        verify(partnersPakDLO).searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);
    }

    @Test
    void searchByUpidAndContractId_whenBothReturnNull_shouldReturnNull() {
        when(partnersPostgresService.searchByUpidAndContractId(any(UUID.class), anyLong()))
                .thenReturn(null);
        when(partnersPakDLO.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID))
                .thenReturn(null);

        Long result = service.searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);

        assertNull(result);
        verify(partnersPakDLO).searchByUpidAndContractId(VALID_UPID, CONTRACT_ID);
    }

    @Test
    void searchByUpidAndContractId_withNullParameters_shouldFallbackToOracle() {
        when(partnersPostgresService.searchByUpidAndContractId(any(UUID.class), nullable(Long.class)))
                .thenReturn(null);
        when(partnersPakDLO.searchByUpidAndContractId(any(), nullable(Long.class)))
                .thenReturn(ORA_RESULT);

        SoftAssertions softy = new SoftAssertions();

        softy.assertThat(service.searchByUpidAndContractId(null, CONTRACT_ID)).isEqualTo(ORA_RESULT);
        softy.assertThat(service.searchByUpidAndContractId(VALID_UPID, null)).isEqualTo(ORA_RESULT);

        softy.assertAll();
    }
}
