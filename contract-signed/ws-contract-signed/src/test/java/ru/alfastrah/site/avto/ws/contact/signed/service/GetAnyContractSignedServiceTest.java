package ru.alfastrah.site.avto.ws.contact.signed.service;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetAnyContractSignedRequestType;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetAnyContractSignedService;
import ru.alfastrah.site.avto.ws.contact.signed.service.get.GetContractResponseFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintFormFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAnyContractSignedServiceTest {

    @Mock
    private BuildEmail buildEmail;
    @Mock
    private PartnersDb partnersDbBean;
    @Mock
    private PrintFormFactory printFormFactory;
    @Mock
    private GetContractResponseFactory responseFactory;
    @Mock
    private ErrorToleranceSigningService signingService;
    @Mock
    private StampService stampService;

    private GetAnyContractSignedService underTest;

    @BeforeEach
    void setUp() {
        underTest = new GetAnyContractSignedService(buildEmail,
                partnersDbBean,
                printFormFactory,
                responseFactory,
                stampService,
                signingService);
    }

    @Test
    void shouldReturnNullWhenIncorrectContractIdForGetAnyContractSigned() {
        GetAnyContractSignedRequestType request = new GetAnyContractSignedRequestType();
        request.setContractId(null);
        GetContractSignedResponseType result = underTest.processGetAnyContractSigned(request);

        assertThat(result).isNull();
    }

    @Test
    void shouldNotThrowExceptionWhenProblemOccurredForGetAnyContractSigned() {
        GetAnyContractSignedRequestType request = new GetAnyContractSignedRequestType();
        long contractId = 123L;
        PrintForm printForm = new RawPrintForm(new DataHandler(new ByteArrayDataSource(new byte[]{}, "application/pdf")), "12");
        request.setContractId(BigInteger.valueOf(contractId));
        RSaleContract contractInfo = new RSaleContract();
        given(partnersDbBean.getContractInfo(any())).willReturn(contractInfo);
        given(printFormFactory.create(any(), any())).willReturn(printForm);

        assertThatNoException().isThrownBy(() -> underTest.processGetAnyContractSigned(request));
    }

    @Test
    void shouldReturnEmptyResponseWhenExceptionCaught() throws EOsagoException {
        GetAnyContractSignedRequestType requestType = new GetAnyContractSignedRequestType();
        requestType.setContractId(BigInteger.TEN);
        given(partnersDbBean.getContractInfo(any())).willReturn(new RSaleContract());
        given(buildEmail.getPrintFormId(any(), any())).willThrow(EOsagoSaveException.class);

        GetContractSignedResponseType responseType = underTest.processGetAnyContractSigned(requestType);
        assertThat(responseType).isNotNull();
        assertThat(responseType.getPrintedFormId()).isNull();
        assertThat(responseType.getMime()).isNull();
        assertThat(responseType.getContent()).isNull();

    }
}