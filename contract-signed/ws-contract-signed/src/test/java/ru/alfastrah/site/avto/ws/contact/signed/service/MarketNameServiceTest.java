package ru.alfastrah.site.avto.ws.contact.signed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.alfastrah.site.avto.ws.contact.signed.db.MarketName;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RContractVariantAdditional;
import tops.unicus.usr.RSaleContract;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.ALPHA_REPAIR_OFFLINE;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKOGO;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_1;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_3;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_5;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_CHILD_SPORT;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.UNKNOWN;

class MarketNameServiceTest {

    @InjectMocks
    private MarketNameService marketNameService;

    @Mock
    private RSaleContract contract;

    @Mock
    private RContractVariant variant;

    @Mock
    private RContractVariantAdditional additional;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(contract.getVariants()).thenReturn(List.of(variant));
    }

    @Test
    void testGetMarketName_KASKO_1_ReturnsKaskoMarketNameWithAdditionalProgram() {
        when(variant.getProductId()).thenReturn("046");
        when(variant.getAdditionals()).thenReturn(List.of(additional));

        // Mocking getKaskoMarketNameWithAdditionalProgram logic
        when(additional.getProductAddId()).thenReturn(5962413L);
        when(additional.getValueText()).thenReturn("6911969");

        MarketName result = marketNameService.getMarketName(contract);
        assertEquals(KASKO_1, result);
    }

    @Test
    void testGetMarketName_KASKO_5_ReturnsKaskoGoMarketNameWithAdditionalProgram() {
        RContractVariantAdditional additional2 = mock(RContractVariantAdditional.class);
        when(variant.getProductId()).thenReturn("046");
        when(variant.getAdditionals()).thenReturn(List.of(additional, additional2));

        when(additional.getProductAddId()).thenReturn(5962413L);
        when(additional.getValueText()).thenReturn("6906189"); //KASKO_5

        when(additional2.getProductAddId()).thenReturn(6871812L);
        when(additional2.getValueText()).thenReturn("6920069"); //KASKOGO

        MarketName result = marketNameService.getMarketName(contract);
        assertEquals(KASKOGO, result);
    }

    @Test
    void testGetMarketName_KASKO_5_WhitUnknownAdditionalProgram_ReturnsKasko5MarketNameWithAdditionalProgram() {
        when(variant.getProductId()).thenReturn("046");
        when(variant.getAdditionals()).thenReturn(List.of(additional));

        when(additional.getProductAddId()).thenReturn(5962413L);
        when(additional.getValueText()).thenReturn("6906189"); //KASKO_5

        MarketName result = marketNameService.getMarketName(contract);
        assertEquals(KASKO_5, result);
    }

    @Test
    void testGetMarketName_NS397_ReturnsNSMarketName() {
        when(variant.getProductId()).thenReturn("397");
        when(variant.getAdditionals()).thenReturn(List.of(additional));

        when(additional.getProductAddId()).thenReturn(6878371L);
        when(additional.getValueText()).thenReturn("6896007"); //NS_CHILD_SPORT

        MarketName result = marketNameService.getMarketName(contract);
        assertEquals(NS_CHILD_SPORT, result);
    }

    @Test
    void testGetMarketName_UnknownProduct_ReturnsUnknown() {
        when(variant.getProductId()).thenReturn("999"); // Unknown product ID

        MarketName result = marketNameService.getMarketName(contract);
        assertEquals(UNKNOWN, result);
    }

    @Test
    void testGetKaskoMarketName_ReturnsCorrectMarketName() {
        when(variant.getAdditionals()).thenReturn(List.of(additional));
        when(additional.getProductAddId()).thenReturn(5962413L);
        when(additional.getValueText()).thenReturn("6910670"); //KASKO_3

        MarketName result = marketNameService.getKaskoMarketName(contract);
        assertEquals(KASKO_3, result);
    }

    @Test
    void testGetAlfaRepairMarketNameId_ReturnsCorrectMarketName() {
        when(variant.getAdditionals()).thenReturn(List.of(additional));
        when(additional.getProductAddId()).thenReturn(6422376L);
        when(additional.getValueText()).thenReturn("6422368"); //ALPHA_REPAIR_OFFLINE

        MarketName result = marketNameService.getAlfaRepairMarketNameId(contract);
        assertEquals(ALPHA_REPAIR_OFFLINE, result);
    }

    @Test
    void testGetKaskoMarketNameWithAdditionalProgram_KASKO5WithValidAdditional_ReturnsAdditionalProgram() {
        when(variant.getAdditionals()).thenReturn(List.of(additional));
        when(additional.getProductAddId()).thenReturn(5962413L);
        when(additional.getValueText()).thenReturn("6906189"); //KASKO_5

        RContractVariantAdditional additionalKasko = mock(RContractVariantAdditional.class);
        when(additionalKasko.getProductAddId()).thenReturn(6871812L);
        when(additionalKasko.getValueText()).thenReturn("6906189");
        when(variant.getAdditionals()).thenReturn(List.of(additional, additionalKasko));

        MarketName result = marketNameService.getKaskoMarketName(contract);
        assertEquals(KASKO_5, result);
    }


}
