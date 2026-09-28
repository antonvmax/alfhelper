package ru.alfastrah.site.avto.ws.contact.signed.service.send;

import jakarta.activation.DataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.altcraft.model.Attachment;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.exception.SendContractServerException;
import ru.alfastrah.site.avto.ws.contact.signed.service.RetryableCheckService;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.PrintFormFactory;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.impl.RawPrintForm;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.alfastrah.site.avto.ws.contact.signed.utils.Const.ATTACHMENT_FILENAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentFactory {


    private static final Map<String, String> FORM_ID_NAME = new HashMap<>();

    private final PrintFormFactory printFormFactory;

    private final ErrorToleranceSigningService signingService;
    private final StampService stampService;

    static {
        // Идентификатор формы, Название печатной формы
        FORM_ID_NAME.put("716", "Заявление.pdf");
        FORM_ID_NAME.put("525", "Полис.pdf");
        FORM_ID_NAME.put("526", "Полис.pdf");
        FORM_ID_NAME.put("917", "Уведомление о заключении ЕОСАГО.pdf");
        FORM_ID_NAME.put("1492", "Полис.pdf");
    }

    public List<Attachment> create(BigInteger contractId, String formSequence) throws EOsagoSaveException {
        List<Attachment> attachments = new ArrayList<>();
        String[] forms = formSequence.split(",");
        for (String form : forms) {
            if (StringUtils.isEmpty(form)) {
                form = "-1";
            }
            try {
                PrintForm printForm = printFormFactory.create(contractId, form);
                DataHandler signedContent = signingService.sign(
                        printForm.content().getInputStream().readAllBytes(),
                        stampService.getStampData(contractId, form), contractId);
                PrintForm signedPrintForm = new RawPrintForm(signedContent, form);
                String fileName = FORM_ID_NAME.getOrDefault(form, ATTACHMENT_FILENAME);
                byte[] bytes = signedPrintForm.content().getInputStream().readAllBytes();
                String s = java.util.Base64.getEncoder().encodeToString(bytes);
                attachments.add(new Attachment("data:application/pdf;base64," + s, fileName));
                log.info("Файл {} добавлен в письмо", fileName);
            } catch (Exception e) {
                log.error("Не получилось сформировать форму: {}. Для contractId={}. Ошибка: {}",
                        form, contractId, e.getMessage());
                throw new SendContractServerException(e.getMessage());
            }
        }

        return attachments;
    }
}
