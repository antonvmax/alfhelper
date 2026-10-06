package ru.alfastrah.site.avto.payment.internet.contract.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InternetContractEntityRepository extends JpaRepository<InternetContractEntity, Long> {

    // В таблице бывает несколько строк на один contract_id (легаси-«бардак»); fix_internet_sale берёт
    // последнюю (order by internet_contract_id desc). Воспроизводим детерминированный выбор.
    Optional<InternetContractEntity> findFirstByContractIdOrderByInternetContractIdDesc(Long contractId);

    // Один mdorder тоже может встретиться в нескольких строках; Oracle-чтение берёт первую строку выборки
    // без order by — здесь выбираем последнюю детерминированно.
    Optional<InternetContractEntity> findFirstByMdOrderOrderByInternetContractIdDesc(String mdOrder);

    // Основной guarded-UPDATE из p_fn_message (строки 1362-1375): фиксируем факт оплаты ОДНИМ атомарным
    // запросом и только если ранее успешной оплаты не было (coalesce(rc,-1) <> 0). Защищает от повторных
    // колбэков РБС/гонок. is_paid здесь НЕ трогаем — он ставится отдельным апдейтом (см. updateIsPaid).
    @Modifying
    @Query("""
            update InternetContractEntity ic
               set ic.mdOrder = :mdOrder,
                   ic.paymentPrimaryRc = 0,
                   ic.paymentSecondaryRc = 0,
                   ic.paidAmount = :paidAmount,
                   ic.paymentDictId = :paymentDictId
             where ic.contractId = :contractId
               and coalesce(ic.paymentPrimaryRc, -1) <> 0
               and coalesce(ic.paymentSecondaryRc, -1) <> 0
            """)
    int markPaidIfNotYetPaid(@Param("contractId") Long contractId,
                             @Param("mdOrder") String mdOrder,
                             @Param("paidAmount") BigDecimal paidAmount,
                             @Param("paymentDictId") Long paymentDictId);

    // Отдельный (негардированный) апдейт даты оплаты из p_fn_message (ASW-15922, строки 1386-1390):
    // выполняется при КАЖДОМ успешном F2m1 по (contract_id, mdorder), без проверки rc — как в процедуре.
    @Modifying
    @Query("""
            update InternetContractEntity ic
               set ic.isPaid = :paidDate,
                   ic.paymentDictId = :paymentDictId
             where ic.contractId = :contractId
               and ic.mdOrder = :mdOrder
            """)
    int updateIsPaid(@Param("contractId") Long contractId,
                     @Param("mdOrder") String mdOrder,
                     @Param("paymentDictId") Long paymentDictId,
                     @Param("paidDate") LocalDateTime paidDate);
}
