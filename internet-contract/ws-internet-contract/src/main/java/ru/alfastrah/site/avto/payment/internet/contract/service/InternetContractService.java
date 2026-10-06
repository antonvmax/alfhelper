package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetContractStatusRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.SetPaymentDictRequest;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.time.LocalDateTime;

/**
 * Жизненный цикл интернет-договора: создание, обновление, отмена, возврат, платёжная система, статус.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractService {

    private final InternetContractRepository contractRepo;

    private final ProcedureRepository procedureRepository;

    private final InternetContractMirrorService mirrorService;

    public void createContract(InternetContractRequest request) {
        procedureRepository.createInternetContract(
                request.contractId(),
                request.internetContractNumber(),
                request.dealerId(),
                request.productId()
        );
        try {
            mirrorService.mirrorContractCreate(request);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось сохранить internet_contract для contractId={}: {}",
                    request.contractId(), e.getMessage(), e);
        }
    }

    public String updateContract(Long contractId, String orderId, LocalDateTime paidDate) {
        try {
            contractRepo.updateInternetContract(contractId, orderId, paidDate);
        } catch (Exception e) {
            return "Не удалось обновить internet-contract: " + e.getMessage();
        }
        try {
            mirrorService.mirrorContractUpdate(contractId, orderId, paidDate);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось обновить internet_contract для contractId={}: {}", contractId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Проставляет дату отмены интернет-договора.
     *
     * @param contractId идентификатор договора
     * @return текст ошибки либо {@code null}, если запись в юникус прошла успешно
     */
    public String cancelContract(Long contractId) {
        try {
            contractRepo.updateCancelDate(contractId);
        } catch (Exception e) {
            return "Не удалось проставить дату отмены internet-contract: " + e.getMessage();
        }
        try {
            mirrorService.mirrorCancelDate(contractId);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось проставить cancel_date для contractId={}: {}",
                    contractId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Проставляет признак возврата по договору и заказу.
     *
     * @param contractId идентификатор договора
     * @param mdOrder    идентификатор заказа (mdorder)
     * @return текст ошибки либо {@code null}, если запись в юникус прошла успешно
     */
    public String refundContract(Long contractId, String mdOrder) {
        try {
            contractRepo.updateRefund(contractId, mdOrder);
        } catch (Exception e) {
            return "Не удалось проставить возврат internet-contract: " + e.getMessage();
        }
        try {
            mirrorService.mirrorRefund(contractId, mdOrder);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось проставить platron_is_refund для contractId={}, mdOrder={}: {}",
                    contractId, mdOrder, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Устанавливает платёжную систему по договору и заказу ({@code inet_card_pak.p_set_payment_dict}).
     *
     * @param request данные платёжной системы
     * @return текст ошибки либо {@code null}, если вызов процедуры прошёл успешно
     */
    public String setPaymentDict(SetPaymentDictRequest request) {
        try {
            procedureRepository.setPaymentDict(request.contractId(), request.mdOrder(), request.paymentDictId());
        } catch (Exception e) {
            return "Не удалось установить платежную систему: " + e.getMessage();
        }
        try {
            mirrorService.mirrorPaymentDict(request.contractId(), request.paymentDictId());
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось проставить payment_dict_id для contractId={}: {}",
                    request.contractId(), e.getMessage(), e);
        }
        return null;
    }

    /**
     * Переводит договор в новый статус ({@code lifecycle_pak.set_contract_status}).
     * <p>
     * В Postgres не зеркалим: процедура работает с таблицами contract/contract_status юникуса, которых
     * в PG нет (см. {@link InternetContractMirrorService}).
     *
     * @param request идентификатор договора и новый статус
     * @return текст ошибки либо {@code null}, если вызов процедуры прошёл успешно
     */
    public String setContractStatus(SetContractStatusRequest request) {
        try {
            procedureRepository.setContractStatus(request.contractId(), request.statusTypeId());
        } catch (Exception e) {
            return "Не удалось установить статус договора: " + e.getMessage();
        }
        return null;
    }

    /**
     * Количество оплаченных допов по корневому договору.
     * <p>
     * Зеркала в Postgres нет и быть не может: запрос отбирает договоры по staff.contract
     * (root_contract_id, contract_status_code, contract_option_id), а этой таблицы в PG нет
     * (см. {@link InternetContractMirrorService}).
     *
     * @param contractId идентификатор корневого договора
     * @return количество допов либо {@code null}, если запрос не удался
     */
    public Long countAdditionals(Long contractId) {
        try {
            return contractRepo.countAdditionals(contractId);
        } catch (Exception e) {
            return null;
        }
    }
}
