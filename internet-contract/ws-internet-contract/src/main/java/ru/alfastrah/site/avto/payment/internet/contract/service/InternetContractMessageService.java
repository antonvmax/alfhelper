package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alfastrah.site.avto.payment.internet.contract.controller.dto.PF2M1MessageRequest;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;
import ru.alfastrah.site.avto.payment.internet.contract.repository.ProcedureRepository;

import java.math.BigDecimal;

/**
 * Сообщения об оплате для юникуса: p_f2_amount_message и f2_m1_message (с письмом и без).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractMessageService {

    private static final String REQUEST_TEMPLATE = "<request>" +
            "<mdOrder>%s</mdOrder>" +
            "<orderNumber>%s</orderNumber>" +
            "<operation>%s</operation>" +
            "<status>%s</status>" +
            "<localMode>%s</localMode>" +
            "</request>";

    private final ProcedureRepository procedureRepository;

    private final InternetContractMirrorService mirrorService;

    public String pf2AmountMessage(PF2AmountMessage request) {
        String result = procedureRepository.pF2AmountMessage(request);
        // Тот же p_f2_amount_message, что и в createPayment, но другой вход — зеркалим и здесь (best-effort).
        try {
            mirrorService.mirrorPf2AmountMessage(request);
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось отразить pf2AmountMessage для contractId={}: {}",
                    request.getContractId(), e.getMessage(), e);
        }
        return result;
    }

    public String pf2m1Message(PF2M1MessageRequest request) {
        String result = procedureRepository.pF2M1Message(BigDecimal.valueOf(request.getContractId()),
                request.getMdOrder(), buildXml(request));
        mirrorPf2m1(request);
        updateStatusLastTransact(request);
        return result;
    }

    public String f2m1MessageWithoutEmail(PF2M1MessageRequest request) {
        String result = procedureRepository.f2m1MessageWithoutEmail(BigDecimal.valueOf(request.getContractId()),
                request.getMdOrder(), buildXml(request));
        // Запись в БД для F2m1 идентична варианту с письмом — используем то же зеркало.
        mirrorPf2m1(request);
        updateStatusLastTransact(request);
        return result;
    }

    private String buildXml(PF2M1MessageRequest request) {
        return String.format(REQUEST_TEMPLATE,
                emptyIfNull(request.getMdOrder()),
                emptyIfNull(request.getOrderNumber()),
                emptyIfNull(request.getOperation()),
                emptyIfNull(request.getStatus()),
                "");
    }

    /**
     * Статус последней транзакции ({@code loyal_pkg.p_update_status_last_transact}) — вызывается после
     * фиксации F2m1, оба варианта эндпоинта обслуживаются одинаково.
     * <p>
     * Ошибку намеренно не глушим: она должна дойти до потребителя, как и при прямом вызове процедуры.
     * В Postgres не зеркалим — бонусные таблицы loyal_pkg в PG отсутствуют.
     */
    private void updateStatusLastTransact(PF2M1MessageRequest request) {
        procedureRepository.updateStatusLastTransact(request.getContractId(), request.getStatusId());
    }

    // Зеркалим фиксацию оплаты F2m1 в Postgres. Best-effort: ошибка не должна ломать основной поток (Oracle).
    private void mirrorPf2m1(PF2M1MessageRequest request) {
        try {
            mirrorService.mirrorPf2m1Message(request.getContractId(), request.getMdOrder(), request.getStatus());
        } catch (Exception e) {
            log.error("Postgres mirror: не удалось отразить pF2m1 для contractId={}: {}",
                    request.getContractId(), e.getMessage(), e);
        }
    }

    private String emptyIfNull(Object s) {
        return s == null ? "" : String.valueOf(s);
    }
}
