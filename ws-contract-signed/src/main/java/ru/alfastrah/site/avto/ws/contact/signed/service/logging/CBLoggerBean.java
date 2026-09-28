package ru.alfastrah.site.avto.ws.contact.signed.service.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.jvnet.jaxb2_commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.bus.utils.hash.Gost341194HashException;
import ru.alfastrah.interplat4.bus.utils.hash.Gost341194HashUtils;
import ru.alfastrah.interplat4.ws.rsa.kbm.RsaKBMExceptionFault;
import ru.alfastrah.site.avto.ws.contact.signed.client.CBLogClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.SiteRecord;
import ru.alfastrah.site.avto.ws.contact.signed.service.client.info.ClientInfo;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class CBLoggerBean implements CBLogger {
    private static final int ACTION_CODE = 25;

//    private final MessageProperies messageProperies;
    private final CBLogClient cbLogClient;
    private final ObjectMapper cbLogObjectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CBLoggerBean.class);

    public CBLoggerBean(CBLogClient cbLogClient,
                        ObjectMapper cbLogObjectMapper) {
//        this.messageProperies = messageProperies;
        this.cbLogClient = cbLogClient;
        this.cbLogObjectMapper = cbLogObjectMapper;
    }

    public void logCurrentContractSignedRequest(ClientInfo client, String email) throws RsaKBMExceptionFault {
//        EmailRecipientFio fio = messageProperies.getFio();
        SiteRecord siteRecord = new SiteRecord();
        siteRecord.setRecordId(UUID.randomUUID().toString());
        siteRecord.setDt(LocalDateTime.now());
        siteRecord.setActionCode(ACTION_CODE);
        siteRecord.setName(client.firstName());
        siteRecord.setLastName(client.lastName());
        siteRecord.setPatronymic(client.middleName());
        siteRecord.setBirthDate(client.birthDate());
        if (client.birthDate() != null) {
            try {
                siteRecord.setUserCode(
                        Gost341194HashUtils.getPhisHashe(
                                client.lastName(),
                                client.firstName(),
                                client.middleName(),
                                LocalDate.parse(client.birthDate().toString())));
            } catch (Gost341194HashException ex) {
                throw new RsaKBMExceptionFault(ex.getMessage(), ex);
            }
        }
        siteRecord.setDeviceInfo(email);
//        siteRecord.setDopInfo(messageProperies.getEmailBody());
        siteRecord.setErrorMessage(StringUtils.EMPTY);
//        siteRecord.setSid(messageProperies.getsID());
        logCBEosagoJson(siteRecord);
        cbLogClient.logContractSignedRequest(siteRecord);
    }

    private void logCBEosagoJson(SiteRecord siteRecord) {
        String jsonRequest = null;
        try {
            jsonRequest = cbLogObjectMapper.writeValueAsString(siteRecord);
            LOGGER.debug(jsonRequest);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can't writeValueAsString from logCBEosagoLogging");
        }
    }
}
