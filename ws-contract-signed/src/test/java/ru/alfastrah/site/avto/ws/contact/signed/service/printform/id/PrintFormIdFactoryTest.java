package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl.KaskoPrintFormIdRetriever;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl.OsagoPrintFormIdRetriever;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class PrintFormIdFactoryTest {

    @Mock
    private KaskoPrintFormIdRetriever kaskoRetriever;
    @Mock
    private OsagoPrintFormIdRetriever osagoRetriever;


    private PrintFormIdFactory underTest;

    @BeforeEach
    void setUp() {
        when(osagoRetriever.product()).thenCallRealMethod();
        when(kaskoRetriever.product()).thenCallRealMethod();
        underTest = new PrintFormIdFactory(List.of(kaskoRetriever, osagoRetriever));
    }

    @Test
    void shouldCallRetrieverWhenFoundForProduct() throws EOsagoException {
        RSaleContract contractInfo = createContract(Product.OSAGO.getProductId());

        underTest.byProduct(contractInfo);

        ArgumentCaptor<RSaleContract> contractInfoCaptor = ArgumentCaptor.forClass(RSaleContract.class);
        verify(kaskoRetriever, times(0)).retrieve(any());
        verify(osagoRetriever, times(1)).retrieve(contractInfoCaptor.capture());
        assertThat(contractInfoCaptor.getValue()).isEqualTo(contractInfo);
    }

    @Test
    void shouldReturnDefaultFormWhenRetrieverNofFound() {
        RSaleContract contractInfo = createContract(Product.ALFA_REPAIR.getProductId());

        String printFormId = underTest.byProduct(contractInfo);

        assertThat(printFormId).isEqualTo("-888");
    }

    @Test
    void shouldReturnDefaultFormWhenExceptionIsThrown() throws EOsagoException {
        RSaleContract contractInfo = createContract(Product.OSAGO.getProductId());

        when(osagoRetriever.retrieve(any())).thenThrow(EOsagoException.class);

        String printFormId = underTest.byProduct(contractInfo);

        assertThat(printFormId).isEqualTo("-888");

    }

    private RSaleContract createContract(String productId) {
        RSaleContract contract = new RSaleContract();
        RContractVariant contractVariant = new RContractVariant();
        contractVariant.setProductId(productId);
        contract.withVariants(contractVariant);
        return contract;
    }
}