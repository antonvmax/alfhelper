package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContract;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;
import ru.alfastrah.site.avto.payment.internet.contract.mapper.InternetContractDetailsMapper;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractPaymentRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractRepository;

import java.util.function.Supplier;

/**
 * Чтение деталей интернет-договора: по идентификатору договора и по заказу платёжного шлюза.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractDetailsService {

    private final InternetContractRepository contractRepo;

    private final InternetContractPaymentRepository paymentRepo;

    private final InternetContractDetailsMapper mapper;

    private final InternetContractMirrorService mirrorService;

    /**
     * Ищет детали договора: сначала internet_contract в Postgres, затем в юникусе; если договора нет —
     * так же по строке платежа internet_contract_payment (Postgres, затем юникус).
     *
     * @param contractId идентификатор договора
     * @return детали договора либо {@code null}, если данных нет ни в одной БД
     */
    public InternetContractDetails findByContractId(String contractId) {
        Long pgContractId = toLongOrNull(contractId);

        InternetContractDetails pgContract = pgContractId == null ? null
                : readFromPostgres(() -> mirrorService.findContractByContractId(pgContractId),
                "internet_contract", "contractId", contractId);
        if (pgContract != null) {
            return pgContract;
        }
        InternetContract c = contractRepo.findByContractId(contractId);
        if (c != null && c.getMdOrder() != null) {
            return mapper.from(c);
        }
        InternetContractDetails pgPayment = pgContractId == null ? null
                : readFromPostgres(() -> mirrorService.findPaymentByContractId(pgContractId),
                "internet_contract_payment", "contractId", contractId);
        if (pgPayment != null) {
            return pgPayment;
        }
        InternetContractPayment p = paymentRepo.findByContractId(contractId);
        if (p != null) {
            return mapper.from(p);
        }
        return null;
    }

    /**
     * Ищет детали по заказу платёжного шлюза тем же порядком, что и {@link #findByContractId}: сначала
     * internet_contract в Postgres, затем в юникусе, потом строка платежа (Postgres, затем юникус).
     *
     * @param mdOrder идентификатор заказа (mdorder)
     * @return детали договора либо {@code null}, если данных нет ни в одной БД
     */
    public InternetContractDetails findByMdOrder(String mdOrder) {
        InternetContractDetails pgContract = readFromPostgres(() -> mirrorService.findContractByMdOrder(mdOrder),
                "internet_contract", "mdOrder", mdOrder);
        if (pgContract != null) {
            return pgContract;
        }
        InternetContract c = contractRepo.findByMdOrder(mdOrder);
        if (c != null) {
            return mapper.from(c);
        }
        InternetContractDetails pgPayment = readFromPostgres(() -> mirrorService.findPaymentByMdOrder(mdOrder),
                "internet_contract_payment", "mdOrder", mdOrder);
        if (pgPayment != null) {
            return pgPayment;
        }
        InternetContractPayment p = paymentRepo.findByMdOrder(mdOrder);
        if (p != null) {
            return mapper.from(p);
        }
        return null;
    }

    // Чтение Postgres best-effort: при ошибке не ломаем эндпоинт, а идём за данными в юникус.
    private InternetContractDetails readFromPostgres(Supplier<InternetContractDetails> read,
                                                     String table, String field, Object value) {
        try {
            return read.get();
        } catch (Exception e) {
            log.error("Postgres: не удалось прочитать {} для {}={}: {}", table, field, value, e.getMessage(), e);
            return null;
        }
    }

    // В Postgres contract_id числовой, в Oracle-запросы contractId уходит строкой как есть.
    private Long toLongOrNull(String contractId) {
        try {
            return Long.valueOf(contractId);
        } catch (NumberFormatException e) {
            log.error("Postgres: contractId={} не число, чтение из Postgres пропущено", contractId);
            return null;
        }
    }
}
