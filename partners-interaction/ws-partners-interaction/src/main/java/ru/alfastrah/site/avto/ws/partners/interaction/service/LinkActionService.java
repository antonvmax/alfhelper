package ru.alfastrah.site.avto.ws.partners.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkActionService {

    private final PartnersPakDLO partnersPakDLO;
    private final PartnersPostgresService partnersPostgresService;

    public void linkActionToUpid(String upid, String calcId, Long contractId) {
        // Основной Oracle вызов
        partnersPakDLO.linkActionToUpid(upid, calcId, contractId);

        // PostgreSQL вызов
        try {
            UUID upidUuid = UUID.fromString(upid);
            partnersPostgresService.linkActionToUpid(upidUuid, calcId, contractId);
        } catch (Exception e) {
            log.error("PostgreSQL linkActionToUpid завершился с ошибкой для upid: {}, calcId: {}, contractId: {}. Ошибка: {}",
                    upid, calcId, contractId, e.getMessage());
//            throw new RuntimeException(e.getMessage(), e); //todo вернуть при переходе на Postgres
        }
    }
}
