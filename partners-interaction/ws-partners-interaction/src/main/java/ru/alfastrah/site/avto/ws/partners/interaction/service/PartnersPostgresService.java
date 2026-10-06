package ru.alfastrah.site.avto.ws.partners.interaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.ContractResult;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.PartnerCalculation;
import ru.alfastrah.site.avto.ws.partners.interaction.mapper.PartnersPostgresMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnersPostgresService {

    private static final String CONTRACT_NOT_CREATED = "Договор еще не создан в системе";
    private static final String CONTRACT_NOT_PAID = "Договор еще не оплачен";

    private final PartnersPostgresMapper partnersPostgresMapper;
    private final UnicusUsrService unicusUsrService;

    public void saveUpid(UUID upid, String callerCode) {
        try {
            partnersPostgresMapper.insertPartnerIdentifier(upid, callerCode);
            log.debug("PostgreSQL: UPID {} успешно сохранен с callerCode: {}", upid, callerCode);
        } catch (Exception e) {
            log.error("PostgreSQL: Ошибка сохранения UPID {} с callerCode {}, ошибка: {}", upid, callerCode, e.getMessage());
            throw new RuntimeException("Ошибка выполнения saveUpid", e);
        }
    }

    public void linkActionToUpid(UUID upid, String calcId, Long contractId) {

        try {
            if (contractId == null) {
                createPartnerCalculation(upid, calcId);
            } else {
                attachContractToCalculation(upid, calcId, contractId);
            }
        } catch (Exception e) {
            log.error("PostgreSQL: Ошибка связывания UPID {} с calcId {} и contractId {}, ошибка: {}", upid, calcId, contractId, e.getMessage());
            throw new RuntimeException("Ошибка выполнения linkActionToUpid", e);
        }
    }

    private void createPartnerCalculation(UUID upid, String calcId) {
        Optional<PartnerCalculation> record = partnersPostgresMapper.findByUpidAndCalcId(upid, calcId);
        if (record.isPresent()) {
            return;
        }
        PartnerCalculation calculation = PartnerCalculation.builder()
                .upid(upid)
                .calcId(calcId)
                .build();
        partnersPostgresMapper.insertPartnerCalculation(calculation);
        log.debug("PostgreSQL: Создана новая связь UPID {} с calcId {}", upid, calcId);
    }

    private void attachContractToCalculation(UUID upid, String calcId, Long contractId) {
        // 1. Находим запись по UPID. Если её нет - ошибка.
        Optional<PartnerCalculation> record = partnersPostgresMapper.findByUpidAndCalcId(upid, calcId);
        if (record.isEmpty()) {
            throw new RuntimeException(String.format("Запись не найдена для UPID %s", upid));
        }

        // 2. Проверяем, что contract_id равен null. Если договор уже привязан - ошибка.
        PartnerCalculation existing = record.get();
        if (existing.getContractId() != null) {
            throw new RuntimeException(String.format("К UPID %s уже привязан договор с contractId %s",
                    upid, existing.getContractId()));
        }

        // 3. Привязываем договор.
        partnersPostgresMapper.updateContractId(existing.getCalculationId(), contractId);
        log.debug("PostgreSQL: К UPID {} привязан договор с contractId {}", upid, contractId);
    }

    /**
     * Поиск договора по UPID и CONTRACT_ID
     *
     * @param upid UPID для поиска договора
     * @param contractId contractId для поиска договора
     * @return boolean - найден ли договор или нет
     */
    public Long searchByUpidAndContractId(UUID upid, Long contractId) {
        try {
            log.debug("searchByUpidAndContractId: Поиск договора для UPID {} AND CONTRACT_ID {}", upid, contractId);
            Long result = partnersPostgresMapper.findByUpidAndContractId(upid, contractId).orElse(null);
            if (result == null) {
                log.info("searchByUpidAndContractId: НЕ найден договор для UPID {} AND CONTRACT_ID {}", upid, contractId);
            } else {
                log.debug("searchByUpidAndContractId: Найден договор для UPID {} AND CONTRACT_ID {}", upid, contractId);
            }
            return result;
        } catch (Exception e) {
            log.error("searchByUpidAndContractId: Ошибка при поиске договора для для UPID {} AND CONTRACT_ID {}: {}", upid, contractId, e.getMessage(), e);
            throw new RuntimeException(String.format("Ошибка при поиске договора для UPID %s AND CONTRACT_ID %s: %s", upid, contractId, e.getMessage()), e);
        }
    }

    /**
     * Поиск договора по UPID с проверкой статуса оплаты
     *
     * @param upid UPID для поиска договора
     * @return ContractResult - полный результат с contractId, кодом ошибки и сообщением
     */
    public ContractResult getContractId(UUID upid) {
        try {
            log.debug("getContractId: Поиск договора для UPID {}", upid);

            // Ищем contractId в partner_calculation
            Long contractId = partnersPostgresMapper.findContractIdByUpid(upid)
                    .orElse(null);

            if (contractId == null) {
                log.info("getContractId: Договор для UPID {} не найден в partner_calculation", upid);
                return ContractResult.builder()
                        .code(0)
                        .message(CONTRACT_NOT_CREATED)
                        .build();
            }

            log.debug("getContractId: Найден договор {} для UPID {}", contractId, upid);

            // Проверяем статус оплаты через UnicusUsrService
            var contractDetails = unicusUsrService.getFullSaleContract(contractId);

            /*
                WHEN NO_DATA_FOUND THEN
				P_CODE := 0
				P_MESSAGE := 'Договор еще не создан в системе'
             */
            if (contractDetails == null) {
                log.warn("getContractId: Договор {} не найден в UnicusUsrService", contractId);
                return ContractResult.builder()
                        .code(0)
                        .message(CONTRACT_NOT_CREATED)
                        .build();
            }

            int statusId = contractDetails.getContractStatusTypeId();
            log.debug("getContractId: Договор {} имеет статус {}", contractId, statusId);

            /*
                Проверяем статус оплаты: статусы 5,6 означают оплачен.
                V_STATUS_ID NOT IN (5,6)
                P_CODE := 1
                THEN P_MESSAGE := 'Договор еще не оплачен
            */
            if (statusId != 5 && statusId != 6) {
                log.info("getContractId: Договор {} еще не оплачен (статус {})", contractId, statusId);
                return ContractResult.builder()
                        .code(1)
                        .message(CONTRACT_NOT_PAID)
                        .build();
            }

            log.info("getContractId: Договор {} оплачен (статус {})", contractId, statusId);
            return ContractResult.builder()
                    .contractId(contractId)
                    .build(); // Успех - возвращаем contractId без кода ошибки

        } catch (Exception e) {
            log.error("getContractId: Ошибка при поиске договора для UPID {}: {}", upid, e.getMessage(), e);
            throw new RuntimeException(String.format("Ошибка при поиске договора для UPID %s: %s", upid, e.getMessage()), e);
        }
    }
}