package ru.alfastrah.site.avto.payment.internet.contract.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "internet_contract_payment", schema = "internet_contract")
public class InternetContractPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internet_contract_payment_id")
    private Long internetContractPaymentId;

    @Column(name = "internet_contract_id", nullable = false)
    private Long internetContractId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "mdorder", length = 150)
    private String mdOrder;

    @Column(name = "payment_primary_rc")
    private Long paymentPrimaryRc;

    @Column(name = "payment_secondary_rc")
    private Long paymentSecondaryRc;

    @Column(name = "date_request")
    private LocalDateTime dateRequest;

    @Column(name = "date_response")
    private LocalDateTime dateResponse;

    @Column(name = "paid_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "payment_dict_id")
    private Long paymentDictId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> thisClass = this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        Class<?> otherClass = o instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        if (!thisClass.equals(otherClass)) return false;
        InternetContractPaymentEntity that = (InternetContractPaymentEntity) o;
        return internetContractPaymentId != null && Objects.equals(internetContractPaymentId, that.internetContractPaymentId);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
