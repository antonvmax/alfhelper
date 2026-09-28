package ru.alfastrah.site.avto.ws.contact.signed.service.get;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.Test;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;


import static org.assertj.core.api.Assertions.assertThat;

class GetContractResponseFactoryTest {

    @Test
    void shouldCreteResponseWhenPrintFormGiven() {
        String printFormId = "525";
        final DataHandler content = new DataHandler(
                new ByteArrayDataSource(new byte[]{}, "application/pdf"));
        PrintForm pf = new RawPrintForm(
                content, printFormId);

        final GetContractSignedResponseType response = new GetContractResponseFactory().create(pf);

        assertThat(response.getPrintedFormId()).isEqualTo(pf.id());
        assertThat(response.getContent()).isEqualTo(content);
    }
}