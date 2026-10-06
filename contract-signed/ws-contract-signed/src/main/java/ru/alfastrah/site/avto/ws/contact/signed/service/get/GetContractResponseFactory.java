package ru.alfastrah.site.avto.ws.contact.signed.service.get;

import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.GetContractSignedResponseType;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;

@Service
public class GetContractResponseFactory {
    private static final String MIME_TYPE = "application/pdf";

    public GetContractSignedResponseType create(PrintForm printForm) {
        GetContractSignedResponseType response = new GetContractSignedResponseType();
        response.setMime(MIME_TYPE);
        response.setContent(printForm.content());
        response.setPrintedFormId(printForm.id());
        return response;
    }

}
