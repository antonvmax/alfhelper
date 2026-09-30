package ru.alfastrah.site.avto.ws.partners.interaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDResponse;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UPIDProcessorTest {

    @Mock
    private PartnersPakDLO mockPartnersPakDLO;

    @Mock
    private PartnersPostgresService partnersPostgresService;

    private UPIDProcessor upidProcessorUnderTest;

    @BeforeEach
    void setUp() {
        upidProcessorUnderTest = new UPIDProcessor(mockPartnersPakDLO, partnersPostgresService);
    }

    @Test
    void testGetUPID() {
        final UPIDRequest request = new UPIDRequest();
        request.setCallerCode("login");

        upidProcessorUnderTest.getUPID(request, "login");

        verify(mockPartnersPakDLO, times(1)).saveUPID(any(), any());
        verify(partnersPostgresService, times(1)).saveUpid(any(), any());
    }

    @Test
    void testGetUPID_notLogin() {
        final UPIDRequest request = new UPIDRequest();
        request.setCallerCode("login");

        upidProcessorUnderTest.getUPID(request, null);

        verify(mockPartnersPakDLO, times(1)).saveUPID(any(), any());
        verify(partnersPostgresService, times(1)).saveUpid(any(), any());
    }
}
