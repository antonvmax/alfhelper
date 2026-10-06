package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.ws.contact.signed.service.MarketNameService;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import tops.unicus.usr.RSaleContract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_1;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_10;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.KASKO_5;

@ExtendWith(MockitoExtension.class)
class KaskoPrintFormIdRetrieverTest {

    @Mock
    private MarketNameService marketNameService;
    private KaskoPrintFormIdRetriever underTest;

    @BeforeEach
    void setUp() {
        underTest = new KaskoPrintFormIdRetriever(marketNameService);
    }

    @Test
    void shouldReturnKaskoProduct() {
        assertThat(underTest.product()).isEqualTo(Product.KASKO);
    }

    @Test
    void shouldReturn559PrintFormWhenContractIsKasko10() {
        when(marketNameService.getKaskoMarketName(any())).thenReturn(KASKO_10);

        String printFormId = underTest.retrieve(new RSaleContract());

        assertThat(printFormId).isEqualTo("559");
    }

    @Test
    void shouldReturn930PrintFormWhenContractIsKasko5AndStatusIsStatement() {
        RSaleContract contractInfo = createContract(1);
        when(marketNameService.getKaskoMarketName(any())).thenReturn(KASKO_5);

        String printFormId = underTest.retrieve(contractInfo);

        assertThat(printFormId).isEqualTo("930");
    }

    @Test
    void shouldReturn929PrintFormWhenContractIsKasko5() {
        RSaleContract contractInfo = createContract(6);

        when(marketNameService.getKaskoMarketName(any())).thenReturn(KASKO_5);

        String printFormId = underTest.retrieve(contractInfo);

        assertThat(printFormId).isEqualTo("929");
    }

    @Test
    void shouldReturnDefaultIdWhenMarketingNameIsDifferentFrom5And10() {
        RSaleContract contractInfo = createContract(6);

        when(marketNameService.getKaskoMarketName(any())).thenReturn(KASKO_1);

        String printFormId = underTest.retrieve(contractInfo);

        assertThat(printFormId).isEqualTo("-1");
    }

    private RSaleContract createContract(Integer statusId) {
        RSaleContract contract = new RSaleContract();
        contract.setContractStatusTypeId(statusId);
        return contract;
    }
}