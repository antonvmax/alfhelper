package ru.alfastrah.site.avto.payment.internet.contract.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InternetContractPaymentRepository {

    private static final String QUERY_BY_CONTRACT_ID = "select * from staff.INTERNET_CONTRACT_PAYMENT where CONTRACT_ID=:contractId";

    private static final String QUERY_BY_MD_ORDER = "select * from staff.INTERNET_CONTRACT_PAYMENT where MDORDER=:mdOrder";

    // Один mdorder может относиться к нескольким договорам (единый чек), поэтому список, а не одна строка.
    private static final String QUERY_CONTRACT_IDS_BY_MD_ORDER =
            "select contract_id from staff.INTERNET_CONTRACT_PAYMENT where MDORDER=:mdOrder";

    private static final String QUERY_UPDATE_PAYMENT = "UPDATE STAFF.INTERNET_CONTRACT_PAYMENT icp " +
            "SET icp.DATE_RESPONSE = :paidDate " +
            "WHERE icp.CONTRACT_ID = :contractId " +
            "AND icp.MDORDER = :orderId";

    private static final ResultSetExtractor<InternetContractPayment> EXTRACTOR = rs -> {
        if (!rs.next()) {
            return null;
        }
        InternetContractPayment p = new InternetContractPayment();
        p.setPaymentDictId(rs.getString("payment_dict_id"));
        p.setMdOrder(rs.getString("mdorder"));
        p.setPaid(toLocalDateTime(rs.getTimestamp("date_response")));
        p.setPaidAmount(rs.getBigDecimal("paid_amount"));
        return p;
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InternetContractPayment findByContractId(String contractId) {
        return jdbcTemplate.query(QUERY_BY_CONTRACT_ID, Map.of("contractId", contractId), EXTRACTOR);
    }

    public InternetContractPayment findByMdOrder(String mdOrder) {
        return jdbcTemplate.query(QUERY_BY_MD_ORDER, Map.of("mdOrder", mdOrder), EXTRACTOR);
    }

    public List<Long> findContractIdsByMdOrder(String mdOrder) {
        return jdbcTemplate.queryForList(QUERY_CONTRACT_IDS_BY_MD_ORDER, Map.of("mdOrder", mdOrder), Long.class);
    }

    public void updateInternetContractPayment(Long contractId, String orderId, LocalDateTime paidDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("orderId", orderId);
        params.put("paidDate", paidDate != null ? paidDate : LocalDateTime.now());
        jdbcTemplate.update(QUERY_UPDATE_PAYMENT, params);
    }

    private static LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
