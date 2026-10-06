package ru.alfastrah.site.avto.ws.contact.signed.service.personal;

import jakarta.activation.DataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;
import ru.alfastrah.site.avto.ws.contact.signed.model.exception.printform.id.PrintFormIdException;
import ru.alfastrah.site.avto.ws.contact.signed.model.personal.SendingPersonalPolicyEmailRequest;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPolicyBase64 {

    private final PdfClient pdfClient;
    private static final String AVIS_SYSTEM = "system=AVIS";
    private final StampService stampService;
    private final ErrorToleranceSigningService signingService;

    public String getPolicyByContractId(Long contractId, SendingPersonalPolicyEmailRequest request) {
        DataHandler response;

        try {
            String param = Optional.ofNullable(request.getSystem()).orElse("").contains("AVIS") ? AVIS_SYSTEM : null;
            response = pdfClient.getPrintedFormByContractId(BigInteger.valueOf(contractId),request.getPrintedFormId().toString(),param);//получаем печатную форму ПДФ.
        } catch (Exception ex) {
            log.error("Ошибка при получении ПФ для контракта {}", request.getContractNumber(), ex);
            return null;
        }

        BigInteger contractIdBigInt = BigInteger.valueOf(contractId);
        String printedFormId = request.getPrintedFormId();

        byte[] printedFormBytes = null;
        try {
            printedFormBytes = dataHandlerToByteArray(response);
        } catch (Exception e) {
            log.error("Ошибка преобразования данных из DataHandler в массив байтов {}", e.getMessage());
            throw new PrintFormIdException("Ошибка преобразования данных",e.getMessage());
        }

        List<StampParams> stampData = stampService.getStampData(contractIdBigInt, printedFormId);
        DataHandler dataHandler = signingService.sign(printedFormBytes, stampData, false, contractIdBigInt);

        return encode(request.getContractNumber(), dataHandler);
    }

    public static byte[] dataHandlerToByteArray(DataHandler response) throws IOException {
        try (InputStream inputStream = response.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    private String encode(String contractNumber, DataHandler response) {
        if (response == null) {
            return null;
        }
        var stream = new ByteArrayOutputStream();
        try {
            response.writeTo(stream);
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (RuntimeException | IOException ex) {
            log.error("Ошибка при кодировании ПФ для контракта {}", contractNumber, ex);
            return null;
        }
    }
}
