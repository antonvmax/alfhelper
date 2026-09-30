package ru.alfastrah.site.avto.ws.partners.interaction.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkActionServiceTest {

    private static final String VALID_UPID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String INVALID_UPID = "invalid-uuid";
    private static final String CALC_ID = "calc123";
    private static final Long CONTRACT_ID = 12345L;

    @Mock
    private PartnersPakDLO partnersPakDLO;

    @Mock
    private PartnersPostgresService partnersPostgresService;

    private LinkActionService service;

    @BeforeEach
    void setUp() {
        service = new LinkActionService(partnersPakDLO, partnersPostgresService);
    }

    @Test
    void linkActionToUpid_withValidParameters_shouldCompleteSuccessfully() {
        assertDoesNotThrow(() -> service.linkActionToUpid(VALID_UPID, CALC_ID, CONTRACT_ID));

        verify(partnersPostgresService).linkActionToUpid(any(UUID.class), anyString(), anyLong());
    }

    @Test
    void linkActionToUpid_withInvalidUpid_shouldCompleteWithoutErrors() {
        assertDoesNotThrow(() -> service.linkActionToUpid(INVALID_UPID, CALC_ID, CONTRACT_ID));
    }

    @Test
    void linkActionToUpid_whenPostgresServiceFails_shouldNotPropagateException() {
        doThrow(new RuntimeException("Database error"))
                .when(partnersPostgresService)
                .linkActionToUpid(any(UUID.class), anyString(), anyLong());

        assertDoesNotThrow(() -> service.linkActionToUpid(VALID_UPID, CALC_ID, CONTRACT_ID));
    }

    @Test
    void linkActionToUpid_withNullParameters_shouldHandleGracefully() {
        SoftAssertions softy = new SoftAssertions();

        softy.assertThatCode(() -> service.linkActionToUpid(null, CALC_ID, CONTRACT_ID))
                .doesNotThrowAnyException();
        softy.assertThatCode(() -> service.linkActionToUpid(VALID_UPID, null, CONTRACT_ID))
                .doesNotThrowAnyException();
        softy.assertThatCode(() -> service.linkActionToUpid(VALID_UPID, CALC_ID, null))
                .doesNotThrowAnyException();

        softy.assertAll();
    }
}