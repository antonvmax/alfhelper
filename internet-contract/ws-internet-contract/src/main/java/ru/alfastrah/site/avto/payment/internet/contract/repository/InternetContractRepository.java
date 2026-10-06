package ru.alfastrah.site.avto.payment.internet.contract.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContract;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InternetContractRepository {

    private static final String QUERY_BY_CONTRACT_ID = "select * from staff.INTERNET_CONTRACT where CONTRACT_ID=:contractId";

    private static final String QUERY_BY_MD_ORDER = "select * from staff.INTERNET_CONTRACT where MDORDER=:mdOrder";

    private static final String QUERY_UPDATE = "UPDATE STAFF.INTERNET_CONTRACT ic " +
            "        SET " +
            "        ic.mdorder = :orderId, " +
            "        (ic.paid_amount, ic.payment_dict_id) = ( " +
            "        SELECT icp.paid_amount, icp.payment_dict_id " +
            "        FROM STAFF.internet_contract_payment icp " +
            "        WHERE icp.contract_id = :contractId " +
            "        AND icp.mdorder = :orderId " +
            "        ), " +
            "        ic.is_paid = :paidDate, " +
            "        ic.date_insert = SYSDATE " +
            "        WHERE ic.CONTRACT_ID = :contractId";

    private static final String QUERY_UPDATE_CANCEL_DATE =
            "UPDATE STAFF.INTERNET_CONTRACT SET CANCEL_DATE = SYSDATE WHERE CONTRACT_ID = :contractId";

    private static final String QUERY_UPDATE_REFUND =
            "UPDATE STAFF.INTERNET_CONTRACT SET PLATRON_IS_REFUND = SYSDATE " +
                    "WHERE CONTRACT_ID = :contractId AND MDORDER = :mdOrder";

    private static final String QUERY_COUNT_ADDITIONALS = """
            select count(distinct contract_id)
                    from staff.internet_contract
                    where contract_id in
                          (select contract_id from staff.contract
                          where root_contract_id = :contractId
                          and contract_status_code in (5,6) and contract_option_id !=5)
            """;


    private static final ResultSetExtractor<InternetContract> EXTRACTOR = rs -> {
        if (!rs.next()) {
            return null;
        }
        InternetContract p = new InternetContract();
        p.setPaymentDictId(rs.getString("payment_dict_id"));
        p.setMdOrder(rs.getString("mdorder"));
        p.setPaid(toLocalDateTime(rs.getTimestamp("is_paid")));
        p.setPaidAmount(rs.getBigDecimal("paid_amount"));

        return p;
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InternetContract findByContractId(String contractId) {
        return jdbcTemplate.query(QUERY_BY_CONTRACT_ID, Map.of("contractId", contractId), EXTRACTOR);
    }

    public InternetContract findByMdOrder(String mdOrder) {
        return jdbcTemplate.query(QUERY_BY_MD_ORDER, Map.of("mdOrder", mdOrder), EXTRACTOR);
    }

    public Long countAdditionals(Long contractId) {
        return jdbcTemplate.queryForObject(QUERY_COUNT_ADDITIONALS, Map.of("contractId", contractId), Long.class);
    }

    public void updateCancelDate(Long contractId) {
        jdbcTemplate.update(QUERY_UPDATE_CANCEL_DATE, Map.of("contractId", contractId));
    }

    public void updateRefund(Long contractId, String mdOrder) {
        Map<String, Object> param = new HashMap<>();
        param.put("contractId", contractId);
        param.put("mdOrder", mdOrder);

        jdbcTemplate.update(QUERY_UPDATE_REFUND, param);
    }

    public void updateInternetContract(Long contractId, String orderId, LocalDateTime paidDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("contractId", contractId);
        param.put("orderId", orderId);
        param.put("paidDate", paidDate != null ? paidDate : LocalDateTime.now());

        jdbcTemplate.update(QUERY_UPDATE, param);
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) throws SQLException {
        return value == null ? null : value.toLocalDateTime();
    }
}
