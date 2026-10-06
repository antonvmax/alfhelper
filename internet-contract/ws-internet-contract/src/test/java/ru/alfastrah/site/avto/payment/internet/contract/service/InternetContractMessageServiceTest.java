package ru.alfastrah.site.avto.payment.internet.contract.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.payment.internet.contract.controller.dto.PF2M1MessageRequest;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractMessageService.class})
class InternetContractMessageServiceTest {

    private static final String REQUEST_TEMPLATE = "<request>" +
            "<mdOrder>%s</mdOrder>" +
            "<orderNumber>%s</orderNumber>" +
            "<operation>%s</operation>" +
            "<status>%s</status>" +
            "<localMode>%s</localMode>" +
            "</request>";

    @Autowired
    InternetContractMessageService service;

    @MockBean
    ProcedureRepository procedureRepository;
    @MockBean
    InternetContractMirrorService internetContractMirrorService;

    @Test
    void pf2AmountMessage() {
        PF2AmountMessage request = new PF2AmountMessage();

        assertDoesNotThrow(() -> service.pf2AmountMessage(request));

        verify(procedureRepository, times(1)).pF2AmountMessage(request);
        verify(internetContractMirrorService, times(1)).mirrorPf2AmountMessage(request);
    }

    @Test
    void pf2m1Message() {
        PF2M1MessageRequest request = new PF2M1MessageRequest();
        request.setContractId(123456L);
        request.setMdOrder("MD_ORDER");
        request.setStatus("STATUS");
        request.setStatusId(7);

        assertDoesNotThrow(() -> service.pf2m1Message(request));

        verify(procedureRepository, times(1)).pF2M1Message(BigDecimal.valueOf(request.getContractId()),
                request.getMdOrder(), expectedXml(request));
        verify(internetContractMirrorService, times(1))
                .mirrorPf2m1Message(request.getContractId(), request.getMdOrder(), request.getStatus());
        verify(procedureRepository, times(1))
                .updateStatusLastTransact(request.getContractId(), request.getStatusId());
    }

    @Test
    void f2m1MessageWithoutEmail() {
        PF2M1MessageRequest request = new PF2M1MessageRequest();
        request.setContractId(123456L);
        request.setMdOrder("MD_ORDER");
        request.setStatus("STATUS");
        request.setStatusId(7);

        assertDoesNotThrow(() -> service.f2m1MessageWithoutEmail(request));

        verify(procedureRepository, times(1)).f2m1MessageWithoutEmail(BigDecimal.valueOf(request.getContractId()),
                request.getMdOrder(), expectedXml(request));
        verify(internetContractMirrorService, times(1))
                .mirrorPf2m1Message(request.getContractId(), request.getMdOrder(), request.getStatus());
        verify(procedureRepository, times(1))
                .updateStatusLastTransact(request.getContractId(), request.getStatusId());
    }

    private String expectedXml(PF2M1MessageRequest request) {
        return String.format(REQUEST_TEMPLATE,
                request.getMdOrder(),
                "",
                "",
                request.getStatus(),
                "");
    }
}
