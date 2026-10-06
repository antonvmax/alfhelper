package ru.alfastrah.site.avto.payment.internet.contract.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractDetails;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractPaymentRequest;
import ru.alfastrah.site.avto.model.internet.contract.dto.InternetContractRequest;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractEntity;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPaymentEntity;
import ru.alfastrah.site.avto.payment.internet.contract.entity.PF2AmountMessage;
import ru.alfastrah.site.avto.payment.internet.contract.mapper.InternetContractDetailsMapper;
import ru.alfastrah.site.avto.payment.internet.contract.mapper.InternetContractEntityMapper;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractEntityRepository;
import ru.alfastrah.site.avto.payment.internet.contract.repository.InternetContractPaymentEntityRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Пишет данные в Postgres, повторяя семантику Oracle-процедур юникуса, и читает их обратно из зеркала.
 * <p>
 * Отличия двух СУБД учтены так: где логику можно воспроизвести средствами Postgres/JPA — она реализована;
 * где логика завязана на таблицы/пакеты, которых в Postgres нет (contract, document, lifecycle_pak, письма,
 * call_journal и т.п.) — это НЕ переносится и помечено комментариями «нельзя воспроизвести в PG».
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternetContractMirrorService {

    private static final String TX_MANAGER = "postgresTransactionManager";
    private final InternetContractEntityRepository contractRepository;
    private final InternetContractPaymentEntityRepository paymentRepository;
    private final InternetContractEntityMapper mapper;
    private final InternetContractDetailsMapper detailsMapper;

    /**
     * Читает договор из Postgres по contract_id — зеркало Oracle-чтения
     * {@code InternetContractRepository.findByContractId}.
     * <p>
     * Как и в Oracle-ветке, строка подходит только с проставленным mdorder (оплата уже привязана).
     *
     * @param contractId идентификатор договора
     * @return детали договора либо {@code null}, если в Postgres строки нет
     */
    @Transactional(value = TX_MANAGER, readOnly = true)
    public InternetContractDetails findContractByContractId(Long contractId) {
        return contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(contractId)
                .filter(contract -> contract.getMdOrder() != null)
                .map(detailsMapper::from)
                .orElse(null);
    }

    /**
     * Читает строку платежа из Postgres по contract_id — зеркало Oracle-чтения
     * {@code InternetContractPaymentRepository.findByContractId}.
     *
     * @param contractId идентификатор договора
     * @return детали платежа либо {@code null}, если в Postgres строки нет
     */
    @Transactional(value = TX_MANAGER, readOnly = true)
    public InternetContractDetails findPaymentByContractId(Long contractId) {
        return paymentRepository.findFirstByContractIdOrderByInternetContractPaymentIdDesc(contractId)
                .map(detailsMapper::from)
                .orElse(null);
    }

    /**
     * Читает договор из Postgres по mdorder — зеркало Oracle-чтения
     * {@code InternetContractRepository.findByMdOrder}.
     * <p>
     * Проверка mdorder на null, как в чтении по contract_id, здесь не нужна: он и есть критерий поиска.
     *
     * @param mdOrder идентификатор заказа (mdorder)
     * @return детали договора либо {@code null}, если в Postgres строки нет
     */
    @Transactional(value = TX_MANAGER, readOnly = true)
    public InternetContractDetails findContractByMdOrder(String mdOrder) {
        return contractRepository.findFirstByMdOrderOrderByInternetContractIdDesc(mdOrder)
                .map(detailsMapper::from)
                .orElse(null);
    }

    /**
     * Читает строку платежа из Postgres по mdorder — зеркало Oracle-чтения
     * {@code InternetContractPaymentRepository.findByMdOrder}.
     *
     * @param mdOrder идентификатор заказа (mdorder)
     * @return детали платежа либо {@code null}, если в Postgres строки нет
     */
    @Transactional(value = TX_MANAGER, readOnly = true)
    public InternetContractDetails findPaymentByMdOrder(String mdOrder) {
        return paymentRepository.findFirstByMdOrderOrderByInternetContractPaymentIdDesc(mdOrder)
                .map(detailsMapper::from)
                .orElse(null);
    }

    /**
     * Зеркалит создание интернет-договора (эндпоинт createContract).
     * <p>
     * Аналог {@code kasko_imp_utils.fix_internet_sale -> ur_save_tab_pak.save_internet_contract}.
     * Это UPSERT по contract_id: берём последнюю строку договора и обновляем её (с nvl-семантикой),
     * либо вставляем новую. Поэтому здесь НЕ просто save() новой сущности.
     *
     * @param request данные интернет-договора из запроса
     */
    @Transactional(TX_MANAGER)
    public void mirrorContractCreate(InternetContractRequest request) {
        InternetContractEntity incoming = mapper.toEntity(request);

        InternetContractEntity target = contractRepository
                .findFirstByContractIdOrderByInternetContractIdDesc(request.contractId())
                .orElse(incoming);

        boolean isNew = target.getInternetContractId() == null;

        if (!isNew) {
            // save_internet_contract: nvl(пришедшее, текущее) — null НЕ затирает уже сохранённое значение.
            mergeNonNull(target, incoming);
        }

        // is_deliv := nvl(...,0), risk_double := nvl(...,0)  (ASW-8343)
        target.setIsDeliv(coalesce(target.getIsDeliv(), (short) 0));
        target.setRiskDouble(coalesce(target.getRiskDouble(), (short) 0));

        if (isNew) {
            // date_insert := sysdate только для НОВОЙ записи; при обновлении процедура сохраняет прежнее.
            target.setDateInsert(coalesce(target.getDateInsert(), LocalDateTime.now()));
        }

        // ---- Нельзя воспроизвести в PG «как в процедуре» (данные из Oracle-таблиц/пакетов) ----
        // 1) internet_contract_number в fix_internet_sale берётся из таблицы contract по contract_id,
        //    а is_paid/paid_amount вычисляются из document/payment_distrib. Этих таблиц в PG нет,
        //    поэтому значения должны приходить уже посчитанными в InternetContractRequest.
        // 2) payment_primary_rc/payment_secondary_rc при создании остаются NULL (0 = «оплата успешна»).
        //    Именно поэтому дефолт 0 убран из маппера.
        // 3) Побочные записи call_journal / call_journal_contract / contract_attr к таблицам
        //    internet_contract* не относятся и здесь не зеркалируются.

        contractRepository.save(target);
    }

    /**
     * Зеркалит регистрацию оплаты (эндпоинт createPayment).
     * <p>
     * Аналог {@code inet_card_pak.p_f2_amount_message} (перегрузка с payment_dict_id):
     * вставляет строку в internet_contract_payment и обновляет internet_contract.payment_dict_id.
     *
     * @param request данные платежа из запроса
     * @return идентификатор строки internet_contract_payment или {@code null}, если договор не найден
     */
    @Transactional(TX_MANAGER)
    public Long mirrorPaymentCreate(InternetContractPaymentRequest request) {
        return upsertPayment(request);
    }

    /**
     * Зеркалит регистрацию оплаты для эндпоинта {@code /internet-contract-details/pf2AmountMessage}.
     * <p>
     * Он вызывает тот же Oracle {@code p_f2_amount_message}, что и createPayment, поэтому переиспользуем
     * ту же логику (перекладываем поля {@link PF2AmountMessage} в InternetContractPaymentRequest:
     * amount -> paid_amount).
     *
     * @param request данные платежа в формате PF2AmountMessage
     * @return идентификатор строки internet_contract_payment или {@code null}, если договор не найден
     */
    @Transactional(TX_MANAGER)
    public Long mirrorPf2AmountMessage(PF2AmountMessage request) {
        Long contractId = request.getContractId() != null ? request.getContractId().longValue() : null;
        InternetContractPaymentRequest paymentRequest = new InternetContractPaymentRequest(
                null,
                contractId,
                request.getMdOrder(),
                null,
                null,
                null,
                null,
                request.getAmount(),
                request.getPaymentDictId());
        return upsertPayment(paymentRequest);
    }

    /**
     * Зеркалит фиксацию результата оплаты F2m1 (эндпоинты pF2m1Message и f2m1MessageWithoutEmail).
     * <p>
     * Аналог {@code inet_card_pak.p_f2m1_message / p_f2m1_message_without_email -> p_fn_message(..., 'F2m1', ...)}.
     * Запись в БД у обеих процедур одинаковая (отличается только отправка письма — это Oracle-only),
     * поэтому один метод обслуживает оба эндпоинта.
     *
     * @param contractId идентификатор договора
     * @param mdOrder    идентификатор заказа (mdorder)
     * @param status     значение из XML {@code request/status}; ветка F2m1 в p_parse_xml ИНВЕРТИРУЕТ его:
     *                   "0" -> оплата НЕ прошла (rc=1), иначе (в т.ч. пусто) -> оплата успешна (rc=0)
     */
    @Transactional(TX_MANAGER)
    public void mirrorPf2m1Message(Long contractId, String mdOrder, String status) {
        long rc = "0".equals(status) ? 1L : 0L;

        Optional<InternetContractPaymentEntity> paymentOpt =
                paymentRepository.findFirstByContractIdAndMdOrder(contractId, mdOrder);
        if (paymentOpt.isEmpty()) {
            // В процедуре при отсутствии строки платежа по mdorder кидается ошибка и записи не делаются.
            // В зеркале просто пропускаем (best-effort), чтобы не ломать основной поток.
            log.error("Postgres mirror: p_fn_message(F2m1) — платёж не найден contractId={}, mdOrder={}, запись пропущена",
                    contractId, mdOrder);
            return;
        }

        // Шаг А: результат оплаты фиксируется в строке платежа ВСЕГДА (и успех, и неуспех).
        InternetContractPaymentEntity payment = paymentOpt.get();
        payment.setPaymentPrimaryRc(rc);
        payment.setPaymentSecondaryRc(rc);
        payment.setDateResponse(LocalDateTime.now());
        paymentRepository.save(payment);

        // Шаг Б: только при успешной оплате. Повторяем ДВА апдейта договора из p_fn_message:
        if (rc == 0L) {
            // 1) основной guarded UPDATE (первый успех): mdorder + rc=0 + paid_amount + payment_dict_id.
            contractRepository.markPaidIfNotYetPaid(
                    contractId, mdOrder, payment.getPaidAmount(), payment.getPaymentDictId());
            // 2) дата оплаты (ASW-15922) — на КАЖДЫЙ успешный колбэк по (contract_id, mdorder), без guard'а.
            //    Выполняется после (1), т.к. основной апдейт мог только что проставить mdorder.
            contractRepository.updateIsPaid(
                    contractId, mdOrder, payment.getPaymentDictId(), LocalDateTime.now());
        }

        // ---- Нельзя воспроизвести в PG ----
        // Смена статуса договора (lifecycle_pak.set_contract_status), отправка письма и PayPerAction
        // работают над таблицами contract/contract_status/call_journal_contract юникуса. В PG их нет.
    }

    /**
     * Зеркалит обновление интернет-договора (эндпоинт updateInternetContract).
     * <p>
     * Аналог app-запроса {@code QUERY_UPDATE} (InternetContractRepository): mdorder + (paid_amount,
     * payment_dict_id) из строки платежа + is_paid + date_insert = now().
     *
     * @param contractId идентификатор договора
     * @param orderId    идентификатор заказа (mdorder)
     * @param paidDate   дата оплаты; если {@code null} — берётся текущее время
     */
    @Transactional(TX_MANAGER)
    public void mirrorContractUpdate(Long contractId, String orderId, LocalDateTime paidDate) {
        contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(contractId)
                .ifPresentOrElse(
                        contract -> {
                            contract.setMdOrder(orderId);

                            paymentRepository.findFirstByContractIdAndMdOrder(contractId, orderId)
                                    .ifPresentOrElse(
                                            payment -> {
                                                contract.setPaidAmount(payment.getPaidAmount());
                                                contract.setPaymentDictId(payment.getPaymentDictId());
                                            },
                                            () -> {
                                                contract.setPaidAmount(null);
                                                contract.setPaymentDictId(null);
                                            }
                                    );

                            contract.setIsPaid(paidDate != null ? paidDate : LocalDateTime.now());
                            contract.setDateInsert(LocalDateTime.now());

                            contractRepository.save(contract);
                        },
                        () -> log.error("Postgres mirror: запись не найдена для contractId={}, обновление пропущено",
                                contractId)
                );
    }

    /**
     * Зеркалит обновление платежа (эндпоинт updateInternetContractPayment).
     * <p>
     * Аналог app-запроса {@code QUERY_UPDATE_PAYMENT} (InternetContractPaymentRepository): проставляет
     * date_response у строки платежа по (contract_id, mdorder).
     *
     * @param contractId идентификатор договора
     * @param orderId    идентификатор заказа (mdorder)
     * @param paidDate   дата ответа; если {@code null} — берётся текущее время
     */
    @Transactional(TX_MANAGER)
    public void mirrorPaymentUpdate(Long contractId, String orderId, LocalDateTime paidDate) {
        paymentRepository.findFirstByContractIdAndMdOrder(contractId, orderId)
                .ifPresentOrElse(
                        payment -> {
                            payment.setDateResponse(paidDate != null ? paidDate : LocalDateTime.now());
                            paymentRepository.save(payment);
                        },
                        () -> log.error("Postgres mirror: платёж не найден для contractId={}, mdOrder={}, обновление пропущено",
                                contractId, orderId)
                );
    }

    /**
     * Зеркалит проставление даты отмены (эндпоинт cancelContract).
     * <p>
     * Аналог app-запроса {@code QUERY_UPDATE_CANCEL_DATE}: cancel_date = sysdate по contract_id.
     *
     * @param contractId идентификатор договора
     */
    @Transactional(TX_MANAGER)
    public void mirrorCancelDate(Long contractId) {
        contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(contractId)
                .ifPresentOrElse(
                        contract -> {
                            contract.setCancelDate(LocalDateTime.now());
                            contractRepository.save(contract);
                        },
                        () -> log.error("Postgres mirror: запись не найдена для contractId={}, отмена пропущена",
                                contractId)
                );
    }

    /**
     * Зеркалит признак возврата (эндпоинт refundContract).
     * <p>
     * Аналог app-запроса {@code QUERY_UPDATE_REFUND}: platron_is_refund = sysdate по (contract_id, mdorder).
     * Условие по mdorder повторяем — при несовпадении Oracle обновил бы 0 строк.
     *
     * @param contractId идентификатор договора
     * @param mdOrder    идентификатор заказа (mdorder)
     */
    @Transactional(TX_MANAGER)
    public void mirrorRefund(Long contractId, String mdOrder) {
        contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(contractId)
                .filter(contract -> Objects.equals(contract.getMdOrder(), mdOrder))
                .ifPresentOrElse(
                        contract -> {
                            contract.setPlatronIsRefund(LocalDateTime.now());
                            contractRepository.save(contract);
                        },
                        () -> log.error("Postgres mirror: запись не найдена для contractId={}, mdOrder={}, "
                                + "возврат пропущен", contractId, mdOrder)
                );
    }

    /**
     * Зеркалит установку платёжной системы (эндпоинт setPaymentDict).
     * <p>
     * Аналог {@code inet_card_pak.p_set_payment_dict}. Тело процедуры недоступно, поэтому зеркалим тот же
     * наблюдаемый эффект, что и у {@code p_f2_amount_message} (см. {@link #upsertPayment}) — payment_dict_id
     * в строке договора, именно его читают findByContractId/findByMdOrder.
     *
     * @param contractId    идентификатор договора
     * @param paymentDictId идентификатор платёжной системы
     */
    @Transactional(TX_MANAGER)
    public void mirrorPaymentDict(Long contractId, Long paymentDictId) {
        contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(contractId)
                .ifPresentOrElse(
                        contract -> {
                            contract.setPaymentDictId(paymentDictId);
                            contractRepository.save(contract);
                        },
                        () -> log.error("Postgres mirror: запись не найдена для contractId={}, "
                                + "payment_dict_id не проставлен", contractId)
                );
    }

    /**
     * Общая логика {@code p_f2_amount_message} для обоих входов (createPayment и pf2AmountMessage):
     * находит договор по contract_id, обновляет internet_contract.payment_dict_id и делает идемпотентный
     * upsert строки internet_contract_payment по (contract_id, mdorder).
     *
     * @param request данные платежа
     * @return идентификатор строки internet_contract_payment или {@code null}, если договор не найден
     */
    private Long upsertPayment(InternetContractPaymentRequest request) {
        Optional<InternetContractEntity> contractOpt =
                contractRepository.findFirstByContractIdOrderByInternetContractIdDesc(request.contractId());
        if (contractOpt.isEmpty()) {
            log.error("Postgres mirror: internet_contract не найден для contractId={}, платёж не создан", request.contractId());
            return null;
        }
        InternetContractEntity contract = contractOpt.get();

        // p_f2_amount_message дополнительно делает update internet_contract set payment_dict_id = ...
        contract.setPaymentDictId(request.paymentDictId());
        contractRepository.save(contract);

        // В Oracle строка платежа вставляется всегда. В PG на (contract_id, mdorder) есть UNIQUE
        // (udx_icp_contract_id_mdorder) — которого в Oracle нет, поэтому делаем идемпотентный upsert,
        // чтобы повторный вызов с тем же mdorder не падал на дубликате.
        InternetContractPaymentEntity payment = paymentRepository
                .findFirstByContractIdAndMdOrder(request.contractId(), request.mdOrder())
                .orElseGet(() -> mapper.toEntity(request, contract.getInternetContractId(), LocalDateTime.now()));

        if (payment.getInternetContractPaymentId() != null) {
            // строка уже была — повторная инициализация оплаты
            payment.setPaidAmount(request.paidAmount());
            payment.setPaymentDictId(request.paymentDictId());
            payment.setDateRequest(LocalDateTime.now());
        }

        // paid_amount в PG NOT NULL; в Oracle суммы подстраховываются nvl(...,0) (см. save_document).
        if (payment.getPaidAmount() == null) {
            payment.setPaidAmount(BigDecimal.ZERO);
        }

        return paymentRepository.save(payment).getInternetContractPaymentId();
    }

    /**
     * Переносит из {@code incoming} в {@code target} только НЕ-null значения (аналог nvl(пришедшее, текущее)
     * из save_internet_contract). Поля date_insert / statement_id / user_identity_code_id / statement_file
     * при обновлении не трогаются — как и в процедуре.
     *
     * @param target   сущность из БД, которую обновляем
     * @param incoming значения из запроса
     */
    private void mergeNonNull(InternetContractEntity target, InternetContractEntity incoming) {
        target.setInternetContractNumber(coalesce(incoming.getInternetContractNumber(), target.getInternetContractNumber()));
        target.setRiskDouble(coalesce(incoming.getRiskDouble(), target.getRiskDouble()));
        target.setContractId(coalesce(incoming.getContractId(), target.getContractId()));
        target.setManagerId(coalesce(incoming.getManagerId(), target.getManagerId()));
        target.setIsDeliv(coalesce(incoming.getIsDeliv(), target.getIsDeliv()));
        target.setInvoiceId(coalesce(incoming.getInvoiceId(), target.getInvoiceId()));
        target.setClientId(coalesce(incoming.getClientId(), target.getClientId()));
        target.setIntContractStatusId(coalesce(incoming.getIntContractStatusId(), target.getIntContractStatusId()));
        target.setPdp(coalesce(incoming.getPdp(), target.getPdp()));
        target.setBrowser(coalesce(incoming.getBrowser(), target.getBrowser()));
        target.setProductId(coalesce(incoming.getProductId(), target.getProductId()));
        target.setMdOrder(coalesce(incoming.getMdOrder(), target.getMdOrder()));
        target.setPaymentPrimaryRc(coalesce(incoming.getPaymentPrimaryRc(), target.getPaymentPrimaryRc()));
        target.setPaymentSecondaryRc(coalesce(incoming.getPaymentSecondaryRc(), target.getPaymentSecondaryRc()));
        target.setChangePaymentDate(coalesce(incoming.getChangePaymentDate(), target.getChangePaymentDate()));
        target.setCancelDate(coalesce(incoming.getCancelDate(), target.getCancelDate()));
        target.setTotalSumRur(coalesce(incoming.getTotalSumRur(), target.getTotalSumRur()));
        target.setTotalRate(coalesce(incoming.getTotalRate(), target.getTotalRate()));
        target.setPaymentDictId(coalesce(incoming.getPaymentDictId(), target.getPaymentDictId()));
        target.setEmoneyDictId(coalesce(incoming.getEmoneyDictId(), target.getEmoneyDictId()));
        target.setIsPaid(coalesce(incoming.getIsPaid(), target.getIsPaid()));
        target.setPlatronPgOrderId(coalesce(incoming.getPlatronPgOrderId(), target.getPlatronPgOrderId()));
        target.setPlatronPgPaymentId(coalesce(incoming.getPlatronPgPaymentId(), target.getPlatronPgPaymentId()));
        target.setPaidAmount(coalesce(incoming.getPaidAmount(), target.getPaidAmount()));
        target.setPlatronIsRefund(coalesce(incoming.getPlatronIsRefund(), target.getPlatronIsRefund()));
        target.setPlatronRefundAmount(coalesce(incoming.getPlatronRefundAmount(), target.getPlatronRefundAmount()));
    }

    /**
     * Возвращает первое НЕ-null значение из переданных (аналог Oracle {@code nvl}/{@code coalesce}).
     *
     * @param values значения в порядке приоритета
     * @param <T>    тип значений
     * @return первое не-null значение либо {@code null}, если все равны null
     */
    @SafeVarargs
    private static <T> T coalesce(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
