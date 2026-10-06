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
@Table(name = "internet_contract", schema = "internet_contract")
public class InternetContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internet_contract_id")
    private Long internetContractId;

    @Column(name = "internet_contract_number", length = 50)
    private String internetContractNumber;

    @Column(name = "risk_double")
    private Short riskDouble;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "is_deliv")
    private Short isDeliv;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "client_id", length = 16)
    private String clientId;

    @Column(name = "int_contract_status_id")
    private Long intContractStatusId;

    @Column(name = "pdp", length = 64)
    private String pdp;

    @Column(name = "browser", length = 500)
    private String browser;

    @Column(name = "product_id", length = 20)
    private String productId;

    @Column(name = "mdorder", length = 150)
    private String mdOrder;

    @Column(name = "payment_primary_rc")
    private Long paymentPrimaryRc;

    @Column(name = "payment_secondary_rc")
    private Long paymentSecondaryRc;

    @Column(name = "change_payment_date")
    private LocalDateTime changePaymentDate;

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Column(name = "total_sum_rur", precision = 10, scale = 2)
    private BigDecimal totalSumRur;

    @Column(name = "total_rate", precision = 25, scale = 10)
    private BigDecimal totalRate;

    @Column(name = "payment_dict_id")
    private Long paymentDictId;

    @Column(name = "emoney_dict_id", length = 20)
    private String emoneyDictId;

    @Column(name = "is_paid")
    private LocalDateTime isPaid;

    @Column(name = "platron_pg_order_id", length = 2000)
    private String platronPgOrderId;

    @Column(name = "platron_pg_payment_id")
    private Long platronPgPaymentId;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "platron_is_refund")
    private LocalDateTime platronIsRefund;

    @Column(name = "platron_refund_amount", precision = 10, scale = 2)
    private BigDecimal platronRefundAmount;

    @Column(name = "date_insert")
    private LocalDateTime dateInsert;

    @Column(name = "statement_id", length = 255)
    private String statementId;

    @Column(name = "user_identity_code_id")
    private Long userIdentityCodeId;

    @Column(name = "statement_file")
    private String statementFile;

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
        InternetContractEntity that = (InternetContractEntity) o;
        return internetContractId != null && Objects.equals(internetContractId, that.internetContractId);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
