package ru.alfastrah.site.avto.ws.partners.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDResponse;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UPIDProcessor {

    private final PartnersPakDLO partnersPakDLO;
    private final PartnersPostgresService partnersPostgresService;

    public UPIDResponse getUPID(UPIDRequest request, String login) {
        UUID uuid = UUID.randomUUID();

        String callerCode;
        String uuidStringFormat = uuid.toString();

        if (StringUtils.isNotBlank(login)) {
            callerCode = login;
        } else {
            callerCode = request.getCallerCode();
        }

        log.trace("UUID: " + uuidStringFormat + " callerCode: " + callerCode);

        // Основной Oracle вызов (как было)
        partnersPakDLO.saveUPID(uuidStringFormat, callerCode);

        // PostgreSQL вызов (новое)
        partnersPostgresService.saveUpid(uuid, callerCode);

        UPIDResponse response = new UPIDResponse();
        response.setUPID(uuidStringFormat);

        return response;
    }
}
