package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractPaymentRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Платежи по интернет-договору: регистрация оплаты, поиск по заказу, обновление строки оплаты.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractPaymentService {

    private final InternetContractPaymentRepository paymentRepo;

    private final ProcedureRepository procedureRepository;

    private final InternetContractMirrorService mirrorService;

    public Long createPayment(InternetContractPaymentRequest request) {
        procedureRepository.pF2AmountMessage(
                BigDecimal.valueOf(request.contractId()),
                request.mdOrder(),
                request.paidAmount(),
                request.paymentDictId()
        );
        try {
            return mirrorService.mirrorPaymentCreate(request);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось сохранить internet_contract_payment для contractId={}: {}",
                    request.contractId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Договоры, по которым есть строка оплаты с указанным заказом. Для единого чека их несколько.
     * <p>
     * Читаем только из юникуса, в отличие от одиночных чтений в
     * {@link InternetContractDetailsService#findByMdOrder}: здесь результат — список, и непустой ответ
     * Postgres не означает, что он полный. Если зеркало одного из договоров единого чека не записалось
     * (запись best-effort), список молча окажется короче, а потребитель проверяет им принадлежность
     * договора заказу — договор был бы отвергнут как «не соответствующий mdOrder».
     *
     * @param mdOrder идентификатор заказа (mdorder)
     * @return список идентификаторов договоров, пустой — если оплат по заказу нет
     */
    public List<Long> findContractIdsByMdOrder(String mdOrder) {
        return paymentRepo.findContractIdsByMdOrder(mdOrder);
    }

    public String updatePayment(Long contractId, String orderId, LocalDateTime paidDate) {
        try {
            paymentRepo.updateInternetContractPayment(contractId, orderId, paidDate);
        } catch (Exception e) {
            return "Не удалось обновить internet-contract-payment: " + e.getMessage();
        }
        try {
            mirrorService.mirrorPaymentUpdate(contractId, orderId, paidDate);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось обновить internet_contract_payment для contractId={}: {}", contractId, e.getMessage(), e);
        }
        return null;
    }
}
