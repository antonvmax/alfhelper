package ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp.SignatureRepository;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampServiceTest {

    @Mock
    private PartnersDb partnersDb;

    @Mock
    private SignatureRepository signatureRepository;

    private StampService stampService;

    @BeforeEach
    void setUp() {
        stampService = new StampService(partnersDb, signatureRepository);
    }

    @Test
    void shouldReturnStampParamsWhenProductIsFound() {
        BigInteger contractId = BigInteger.valueOf(12345L);
        String formId = "TEST_FORM";
        String productId = "OSAGO";

        RSaleContract contract = createSaleContract(productId);
        List<StampParams> expectedStampParams = List.of(new StampParams());

        when(partnersDb.getContractInfo(contractId)).thenReturn(contract);
        when(signatureRepository.getStampParams(Product.OSAGO, formId)).thenReturn(expectedStampParams);

        List<StampParams> result = stampService.getStampData(contractId, formId);

        assertEquals(expectedStampParams, result);
        verify(partnersDb).getContractInfo(contractId);
        verify(signatureRepository).getStampParams(Product.OSAGO, formId);
    }

    @Test
    void shouldReturnEmptyListWhenProductIsUnknown() {
        BigInteger contractId = BigInteger.valueOf(12345L);
        String formId = "TEST_FORM";
        String unknownProductId = "UNKNOWN_PRODUCT";

        RSaleContract contract = createSaleContract(unknownProductId);

        when(partnersDb.getContractInfo(contractId)).thenReturn(contract);

        List<StampParams> result = stampService.getStampData(contractId, formId);

        assertTrue(result.isEmpty());
        verify(partnersDb).getContractInfo(contractId);
    }

    @Test
    void shouldReturnStampParamsForKaskoProduct() {
        BigInteger contractId = BigInteger.valueOf(67890L);
        String formId = "KASKO_FORM";
        String productId = "046";

        RSaleContract contract = createSaleContract(productId);
        List<StampParams> expectedStampParams = List.of(new StampParams(), new StampParams());

        when(partnersDb.getContractInfo(contractId)).thenReturn(contract);
        when(signatureRepository.getStampParams(Product.KASKO, formId)).thenReturn(expectedStampParams);

        List<StampParams> result = stampService.getStampData(contractId, formId);

        assertEquals(expectedStampParams, result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnStampParamsForWhiteCardProduct() {
        BigInteger contractId = BigInteger.valueOf(11111L);
        String formId = "WHITE_CARD_FORM";
        String productId = "190";

        RSaleContract contract = createSaleContract(productId);
        List<StampParams> expectedStampParams = new ArrayList<>();

        when(partnersDb.getContractInfo(contractId)).thenReturn(contract);
        when(signatureRepository.getStampParams(Product.WHITE_CARD, formId)).thenReturn(expectedStampParams);

        List<StampParams> result = stampService.getStampData(contractId, formId);

        assertEquals(expectedStampParams, result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenProductIdIsNull() {
        BigInteger contractId = BigInteger.valueOf(12345L);
        String formId = "TEST_FORM";

        RSaleContract contract = createSaleContract(null);

        when(partnersDb.getContractInfo(contractId)).thenReturn(contract);

        List<StampParams> result = stampService.getStampData(contractId, formId);

        assertTrue(result.isEmpty());
    }

    private RSaleContract createSaleContract(String productId) {
        RSaleContract contract = new RSaleContract();
        RContractVariant variant = new RContractVariant();
        variant.setProductId(productId);
        contract.withVariants(List.of(variant));
        return contract;
    }
}