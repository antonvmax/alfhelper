package ru.alfastrah.site.avto.payment.internet.contract.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPaymentEntity;

import java.util.Optional;

@Repository
public interface InternetContractPaymentEntityRepository extends JpaRepository<InternetContractPaymentEntity, Long> {

    Optional<InternetContractPaymentEntity> findFirstByContractIdAndMdOrder(Long contractId, String mdOrder);

    // На один contract_id может быть несколько платежей (рассрочка, повторные попытки оплаты).
    // Oracle-чтение берёт первую строку выборки без order by — здесь выбираем последнюю детерминированно.
    Optional<InternetContractPaymentEntity> findFirstByContractIdOrderByInternetContractPaymentIdDesc(Long contractId);

    // Один mdorder относится к нескольким договорам (единый чек), поэтому строк тоже может быть несколько.
    Optional<InternetContractPaymentEntity> findFirstByMdOrderOrderByInternetContractPaymentIdDesc(String mdOrder);
}
