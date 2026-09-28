package ru.alfastrah.site.avto.ws.contact.signed.client;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.ws.soap.client.SoapFaultClientException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoProccessException;
import ru.alfastrah.site.avto.ws.contact.signed.client.model.GetPrintedFormByContractId;

import java.math.BigInteger;

@Service
public class PdfClient {

    private final ReportClient reportClient;

    public PdfClient(ReportClient reportClient) {
        this.reportClient = reportClient;
    }

    public DataHandler getPrintedFormByContractId(BigInteger contractId, String form, String params) {
        GetPrintedFormByContractId request = new GetPrintedFormByContractId();
        request.setContractId(contractId);
        request.setPrintedFormId(form);
        request.setParams(params);
        try {
            byte[] content =  reportClient.getPrintedFormByContractId(request);
            DataSource dataSource = new ByteArrayDataSource(content, MediaType.APPLICATION_PDF_VALUE);
            return new DataHandler(dataSource);
        } catch (Exception ex) {
            throw new EOsagoProccessException("Не удалось получить данные печатной формы, " + ex.getMessage(), "Server error");
        }
    }
}
