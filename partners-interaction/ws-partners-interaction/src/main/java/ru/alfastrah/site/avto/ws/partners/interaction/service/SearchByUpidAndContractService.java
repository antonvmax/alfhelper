package ru.alfastrah.site.avto.ws.partners.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alfastrah.site.avto.ws.partners.interaction.dlo.PartnersPakDLO;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchByUpidAndContractService {

    private final PartnersPakDLO partnersPakDLO;
    private final PartnersPostgresService partnersPostgresService;

    public Long searchByUpidAndContractId(String upid, Long contractId) {
        // PostgreSQL вызов
        try {
            UUID upidUuid = UUID.fromString(upid);
            Long pgResult = partnersPostgresService.searchByUpidAndContractId(upidUuid, contractId);
            log.info("PostgreSQL searchByUpidAndContractId() result: {}", pgResult);
            if (pgResult != null) {
                return pgResult;
            }
        } catch (Exception e) {
            log.error("PostgreSQL searchByUpidAndContractId завершился с ошибкой для upid: {}, contractId: {}. Ошибка: {}",
                    upid, contractId, e.getMessage());
//            throw new RuntimeException(e.getMessage(), e); //todo вернуть при переходе на Postgres
        }
        // Oracle вызов
        Long oraResult = partnersPakDLO.searchByUpidAndContractId(upid, contractId);
        log.info("Oracle searchByUpidAndContractId() result: {}", oraResult);
        return oraResult;
    }
}
