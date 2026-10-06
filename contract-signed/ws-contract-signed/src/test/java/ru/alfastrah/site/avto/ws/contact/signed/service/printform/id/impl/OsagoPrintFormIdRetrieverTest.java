package ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.service.Product;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.partner.AgentBlockPrintFormCheck;
import tops.unicus.usr.RContractRestrictionUser;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsagoPrintFormIdRetrieverTest {

    @Mock
    private UnicusUsrService unicusUsrService;

    @Mock
    private AgentBlockPrintFormCheck agentBlockPrintFormCheck;

    private OsagoPrintFormIdRetriever underTest;


    @BeforeEach
    void setUp() {
        underTest = new OsagoPrintFormIdRetriever(unicusUsrService, agentBlockPrintFormCheck);
    }

    @Test
    void shouldReturnOsagoProductId() {
        assertThat(underTest.product()).isEqualTo(Product.OSAGO);
    }

    @Test
    void shouldReturn525WhenDriversLessThan5AndStatus5And6() throws EOsagoException {
        RSaleContract contractInfoConcluded = new RSaleContract();
        contractInfoConcluded.setContractStatusTypeId(5);
        RSaleContract contractInfoConfirmed = new RSaleContract();
        contractInfoConfirmed.setContractStatusTypeId(6);
        when(unicusUsrService.getContractVariantList(any()))
                .thenReturn(List.of(new RContractVariant()));
        when(unicusUsrService.getContractRestrictionUserList(any()))
                .thenReturn(List.of(new RContractRestrictionUser(),
                        new RContractRestrictionUser(),
                        new RContractRestrictionUser(),
                        new RContractRestrictionUser()));

        String printFormIdConcluded = underTest.retrieve(contractInfoConcluded);
        String printFormIdConfirmed = underTest.retrieve(contractInfoConfirmed);

        assertThat(printFormIdConcluded).isEqualTo("525");
        assertThat(printFormIdConfirmed).isEqualTo("525");
    }

    @Test
    void shouldReturn526WhenDriversMoreThan4AndStatus5And6() throws EOsagoException {
        RSaleContract contractInfoConcluded = new RSaleContract();
        contractInfoConcluded.setContractStatusTypeId(5);
        RSaleContract contractInfoConfirmed = new RSaleContract();
        contractInfoConfirmed.setContractStatusTypeId(6);
        when(unicusUsrService.getContractVariantList(any()))
                .thenReturn(List.of(new RContractVariant()));
        when(unicusUsrService.getContractRestrictionUserList(any()))
                .thenReturn(List.of(new RContractRestrictionUser(),
                        new RContractRestrictionUser(),
                        new RContractRestrictionUser(),
                        new RContractRestrictionUser(),
                        new RContractRestrictionUser()));

        String printFormIdConcluded = underTest.retrieve(contractInfoConcluded);
        String printFormIdConfirmed = underTest.retrieve(contractInfoConfirmed);

        assertThat(printFormIdConcluded).isEqualTo("526");
        assertThat(printFormIdConfirmed).isEqualTo("526");
    }

    @Test
    void shouldReturn541IdWhenContractStatusIs1() throws EOsagoException {
        RSaleContract contractInfo = new RSaleContract();
        contractInfo.setContractStatusTypeId(1);

        String printFormId = underTest.retrieve(contractInfo);

        assertThat(printFormId).isEqualTo("541");
    }

    @Test
    void shouldThrowExceptionWhenContractCanceled() {
        RSaleContract contractInfo = new RSaleContract();
        contractInfo.setContractStatusTypeId(8);

        assertThatThrownBy(() -> underTest.retrieve(contractInfo))
                .isInstanceOf(EOsagoException.class)
                .hasMessageContaining("Договор был аннулирован");
    }

    @Test
    void shouldThrowExceptionWhenUnknownContractStatus() {
        RSaleContract contractInfo = new RSaleContract();
        contractInfo.setContractStatusTypeId(123);

        assertThatThrownBy(() -> underTest.retrieve(contractInfo))
                .isInstanceOf(EOsagoException.class)
                .hasMessageContaining("Неизвестный статус договора");
    }

}