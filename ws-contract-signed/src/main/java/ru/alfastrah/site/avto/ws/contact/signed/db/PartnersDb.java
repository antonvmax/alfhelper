package ru.alfastrah.site.avto.ws.contact.signed.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.client.unicus.db.partner.ContractInfoClient;
import ru.alfastrah.site.avto.model.unicus.db.dto.contract.ContractInfoDto;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PartnersDb {

    private final UnicusUsrService usrService;
    private final ContractInfoClient contractInfoClient;

    /***
     * Определение продукта по contractId
     * @param contractId
     * @return
     */
    public RSaleContract getContractInfo(BigInteger contractId) {
        return usrService.getFullSaleContract(contractId.longValue());
    }

    /***
     * @param upid - upid партнера
     * @param contractId - contractId КАСКО в 5-ку или 10-ку
     * @return
     */
    public ContractInfoDto getAdditionalKaskoInfo(String upid, BigInteger contractId) {
        try {
            return contractInfoClient.getAdditionalKaskoInfo(upid, contractId);
        } catch (Exception e) {
            log.error("Ошибка при вызове unicus-db-service метода getAdditionalKaskoInfo. UPID: {}, contractId: {}", upid, contractId, e);
            return null;
        }
    }
}
