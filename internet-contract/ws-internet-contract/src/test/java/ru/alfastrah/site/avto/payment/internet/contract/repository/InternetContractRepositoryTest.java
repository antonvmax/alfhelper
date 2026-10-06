package ru.alfastrah.site.avto.payment.internet.contract.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContract;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractRepository.class})
class InternetContractRepositoryTest {
    @Autowired
    InternetContractRepository repository;

    @MockBean
    NamedParameterJdbcTemplate jdbcTemplate;

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

    private static final String QUERY_COUNT_ADDITIONALS = """
            select count(distinct contract_id)
                    from staff.internet_contract
                    where contract_id in
                          (select contract_id from staff.contract
                          where root_contract_id = :contractId
                          and contract_status_code in (5,6) and contract_option_id !=5)
            """;

    @Test
    void findByContractId_whenRecordExists_returnsContract() {
        String contractId = "CONTRACT-123";
        LocalDateTime paidDate = LocalDateTime.now().minusMinutes(20).withSecond(0).withNano(0);

        InternetContract expected = new InternetContract();
        expected.setPaid(paidDate);
        expected.setMdOrder("mdOrder");
        expected.setPaymentDictId("5");
        expected.setPaidAmount(new BigDecimal("1234.56"));

        when(jdbcTemplate.query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContract> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenReturn("5");
                    when(rs.getString("mdorder")).thenReturn("mdOrder");
                    when(rs.getTimestamp("is_paid"))
                            .thenReturn(Timestamp.valueOf(paidDate));
                    when(rs.getBigDecimal("paid_amount")).thenReturn(new BigDecimal("1234.56"));
                    return extractor.extractData(rs);
                });

        InternetContract actual = repository.findByContractId(contractId);
        // Then
        assertNotNull(actual);
        assertEquals(expected.getPaymentDictId(), actual.getPaymentDictId());
        assertEquals(expected.getMdOrder(), actual.getMdOrder());
        assertEquals(expected.getPaid(), actual.getPaid());
        assertEquals(expected.getPaidAmount(), actual.getPaidAmount());
        verify(jdbcTemplate).query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class));
    }

    @Test
    void findByContractId_whenRecordExists_returnsContract_withPaidDateNull() {
        String contractId = "CONTRACT-123";
        LocalDateTime paidDate = LocalDateTime.now().minusMinutes(20).withSecond(0).withNano(0);

        InternetContract expected = new InternetContract();
        expected.setPaid(paidDate);
        expected.setMdOrder("mdOrder");
        expected.setPaymentDictId("5");

        when(jdbcTemplate.query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContract> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenReturn("5");
                    when(rs.getString("mdorder")).thenReturn("mdOrder");
                    when(rs.getTimestamp("is_paid"))
                            .thenReturn(null);
                    return extractor.extractData(rs);
                });

        InternetContract actual = repository.findByContractId(contractId);
        // Then
        assertNotNull(actual);
        assertEquals(expected.getPaymentDictId(), actual.getPaymentDictId());
        assertEquals(expected.getMdOrder(), actual.getMdOrder());
        assertNull(actual.getPaid());
        verify(jdbcTemplate).query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class));
    }

    @Test
    void findByContractId_whenRecordNotFound_returnsNull() {
        // Given
        String contractId = "CONTRACT-999";

        when(jdbcTemplate.query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContractPayment> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false); // Пустой результат
                    return extractor.extractData(rs);
                });

        // When
        InternetContract result = repository.findByContractId(contractId);

        // Then
        assertNull(result);
        verify(jdbcTemplate).query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class));
    }

    @Test
    void findByMdOrder_whenRecordExists_returnsContract() {
        String mdOrder = "MD_ORDER-123";
        LocalDateTime paidDate = LocalDateTime.now().minusMinutes(20).withSecond(0).withNano(0);

        InternetContract expected = new InternetContract();
        expected.setPaid(paidDate);
        expected.setMdOrder("mdOrder");
        expected.setPaymentDictId("5");

        when(jdbcTemplate.query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContract> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenReturn("5");
                    when(rs.getString("mdorder")).thenReturn("mdOrder");
                    when(rs.getTimestamp("is_paid"))
                            .thenReturn(Timestamp.valueOf(paidDate));
                    return extractor.extractData(rs);
                });

        InternetContract actual = repository.findByMdOrder(mdOrder);
        // Then
        assertNotNull(actual);
        assertEquals(expected.getPaymentDictId(), actual.getPaymentDictId());
        assertEquals(expected.getMdOrder(), actual.getMdOrder());
        assertEquals(expected.getPaid(), actual.getPaid());
        verify(jdbcTemplate).query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class));
    }

    @Test
    void countAdditionals_whenRecordExists_returnsContract() {
        Long contractId = 1234567L;

        Long expected = 5L;

        when(jdbcTemplate.queryForObject(eq(QUERY_COUNT_ADDITIONALS),
                eq(Map.of("contractId", contractId)),
                any(Class.class)))
                .thenAnswer(invocation -> expected);

        Long actual = repository.countAdditionals(contractId);
        // Then
        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(jdbcTemplate).queryForObject(eq(QUERY_COUNT_ADDITIONALS),
                eq(Map.of("contractId", contractId)),
                any(Class.class));
    }

    @Test
    void updateInternetContract() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID_123";
        LocalDateTime paidDate = LocalDateTime.now();

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);


        repository.updateInternetContract(contractId, orderId, paidDate);

        // Then
        verify(jdbcTemplate).update(eq(QUERY_UPDATE), paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(contractId, capturedParams.get("contractId"));
        assertEquals(orderId, capturedParams.get("orderId"));
        assertEquals(paidDate, capturedParams.get("paidDate"));
    }

    @Test
    void updateInternetContract_withNullPaidDate() {
        Long contractId = 1234567L;
        String orderId = "ORDER_ID_123";

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);


        repository.updateInternetContract(contractId, orderId, null);

        // Then
        verify(jdbcTemplate).update(eq(QUERY_UPDATE), paramsCaptor.capture());

        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(contractId, capturedParams.get("contractId"));
        assertEquals(orderId, capturedParams.get("orderId"));
        assertNotNull(capturedParams.get("paidDate"));
    }
}