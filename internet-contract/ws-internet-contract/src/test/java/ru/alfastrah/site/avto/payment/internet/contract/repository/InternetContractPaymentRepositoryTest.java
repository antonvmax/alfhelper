package ru.alfastrah.site.avto.payment.internet.contract.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.alfastrah.site.avto.payment.internet.contract.entity.InternetContractPayment;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InternetContractPaymentRepository.class})
class InternetContractPaymentRepositoryTest {
    @Autowired
    InternetContractPaymentRepository repository;

    @MockBean
    NamedParameterJdbcTemplate jdbcTemplate;

    private static final String QUERY_BY_CONTRACT_ID = "select * from staff.INTERNET_CONTRACT_PAYMENT where CONTRACT_ID=:contractId";

    private static final String QUERY_BY_MD_ORDER = "select * from staff.INTERNET_CONTRACT_PAYMENT where MDORDER=:mdOrder";

    private static final String QUERY_CONTRACT_IDS_BY_MD_ORDER =
            "select contract_id from staff.INTERNET_CONTRACT_PAYMENT where MDORDER=:mdOrder";

    private static final String QUERY_UPDATE_PAYMENT = "UPDATE STAFF.INTERNET_CONTRACT_PAYMENT icp " +
            "SET icp.DATE_RESPONSE = :paidDate " +
            "WHERE icp.CONTRACT_ID = :contractId " +
            "AND icp.MDORDER = :orderId";

    @Test
    void findContractIdsByMdOrder_returnsAllContractsOfOrder() {
        // Given
        String mdOrder = "MD-777";

        when(jdbcTemplate.queryForList(eq(QUERY_CONTRACT_IDS_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                eq(Long.class)))
                .thenReturn(List.of(100L, 200L));

        // When
        List<Long> result = repository.findContractIdsByMdOrder(mdOrder);

        // Then
        assertEquals(List.of(100L, 200L), result);
    }

    @Test
    void findByContractId_whenRecordExists_returnsPayment() {
        // Given
        String contractId = "CONTRACT-123";
        LocalDateTime paidDate = LocalDateTime.of(2026, 4, 10, 12, 0);
        InternetContractPayment expected = new InternetContractPayment();
        expected.setPaymentDictId("PAY-456");
        expected.setMdOrder("MD-789");
        expected.setPaid(paidDate);
        expected.setPaidAmount(new BigDecimal("1234.56"));

        when(jdbcTemplate.query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContractPayment> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenReturn("PAY-456");
                    when(rs.getString("mdorder")).thenReturn("MD-789");
                    when(rs.getTimestamp("date_response")).thenReturn(Timestamp.valueOf(paidDate));
                    when(rs.getBigDecimal("paid_amount")).thenReturn(new BigDecimal("1234.56"));
                    return extractor.extractData(rs);
                });

        // When
        InternetContractPayment actual = repository.findByContractId(contractId);

        // Then
        assertNotNull(actual);
        assertEquals("PAY-456", actual.getPaymentDictId());
        assertEquals("MD-789", actual.getMdOrder());
        assertEquals(paidDate, actual.getPaid());
        assertEquals(expected.getPaidAmount(), actual.getPaidAmount());
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
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });

        // When
        InternetContractPayment result = repository.findByContractId(contractId);

        // Then
        assertNull(result);
        verify(jdbcTemplate).query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class));
    }

    @Test
    void findByContractId_whenExceptionThrown_propagatesException() {
        // Given
        String contractId = "CONTRACT-ERR";
        DataAccessException dbException = new DataAccessException("DB error") {};

        when(jdbcTemplate.query(eq(QUERY_BY_CONTRACT_ID),
                eq(Map.of("contractId", contractId)),
                any(ResultSetExtractor.class)))
                .thenThrow(dbException);

        // When & Then
        DataAccessException thrown = assertThrows(DataAccessException.class,
                () -> repository.findByContractId(contractId));
        assertEquals("DB error", thrown.getMessage());
    }

    @Test
    void findByMdOrder_whenRecordExists_returnsPayment() {
        // Given
        String mdOrder = "MD-ORDER-001";

        when(jdbcTemplate.query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContractPayment> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenReturn("PAY-111");
                    when(rs.getString("mdorder")).thenReturn(mdOrder);
                    return extractor.extractData(rs);
                });

        // When
        InternetContractPayment result = repository.findByMdOrder(mdOrder);

        // Then
        assertNotNull(result);
        assertEquals("PAY-111", result.getPaymentDictId());
        assertEquals(mdOrder, result.getMdOrder());
        assertNull(result.getPaid());
        verify(jdbcTemplate).query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class));
    }

    @Test
    void findByMdOrder_whenRecordNotFound_returnsNull() {
        // Given
        String mdOrder = "MD-NONEXISTENT";

        when(jdbcTemplate.query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class)))
                .thenReturn(null);

        // When
        InternetContractPayment result = repository.findByMdOrder(mdOrder);

        // Then
        assertNull(result);
    }

    @Test
    void findByMdOrder_whenSQLExceptionInExtractor_handlesGracefully() {
        // Given
        String mdOrder = "MD-ERR";

        when(jdbcTemplate.query(eq(QUERY_BY_MD_ORDER),
                eq(Map.of("mdOrder", mdOrder)),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<InternetContractPayment> extractor = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getString("payment_dict_id")).thenThrow(new SQLException("Column not found"));
                    return extractor.extractData(rs);
                });

        // When & Then
        assertThrows(SQLException.class,
                () -> repository.findByMdOrder(mdOrder));
    }

    @Test
    void updateInternetContractPayment_whenValidParams_executesUpdate() {
        // Given
        Long contractId = 12345L;
        String orderId = "ORDER-ABC";
        LocalDateTime paidDate = LocalDateTime.of(2026, 4, 10, 12, 0);

        when(jdbcTemplate.update(eq(QUERY_UPDATE_PAYMENT), any(Map.class)))
                .thenReturn(1);

        // When
        repository.updateInternetContractPayment(contractId, orderId, paidDate);

        // Then
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).update(eq(QUERY_UPDATE_PAYMENT), captor.capture());
        Map<String, Object> params = captor.getValue();
        assertEquals(contractId, params.get("contractId"));
        assertEquals(orderId, params.get("orderId"));
        assertEquals(paidDate, params.get("paidDate"));
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    void updateInternetContractPayment_whenPaidDateNull_usesNow() {
        // Given
        Long contractId = 12345L;
        String orderId = "ORDER-ABC";

        when(jdbcTemplate.update(eq(QUERY_UPDATE_PAYMENT), any(Map.class)))
                .thenReturn(1);

        // When
        repository.updateInternetContractPayment(contractId, orderId, null);

        // Then
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(jdbcTemplate).update(eq(QUERY_UPDATE_PAYMENT), captor.capture());
        Map<String, Object> params = captor.getValue();
        assertNotNull(params.get("paidDate"));
        assertInstanceOf(LocalDateTime.class, params.get("paidDate"));
    }

    @Test
    void updateInternetContractPayment_whenNoRowsUpdated_completesSuccessfully() {
        // Given
        Long contractId = 999L;
        String orderId = "ORDER-XYZ";
        LocalDateTime paidDate = LocalDateTime.now();

        when(jdbcTemplate.update(eq(QUERY_UPDATE_PAYMENT), any(Map.class)))
                .thenReturn(0);

        // When & Then
        assertDoesNotThrow(() ->
                repository.updateInternetContractPayment(contractId, orderId, paidDate));
    }

    @Test
    void updateInternetContractPayment_whenExceptionThrown_propagates() {
        // Given
        Long contractId = 1L;
        String orderId = "ORDER-ERR";
        LocalDateTime paidDate = LocalDateTime.now();
        DataAccessException dbException = new DataAccessException("Update failed") {};

        when(jdbcTemplate.update(eq(QUERY_UPDATE_PAYMENT), any(Map.class)))
                .thenThrow(dbException);

        // When & Then
        DataAccessException thrown = assertThrows(DataAccessException.class,
                () -> repository.updateInternetContractPayment(contractId, orderId, paidDate));
        assertEquals("Update failed", thrown.getMessage());
    }

    @Test
    void updateInternetContractPayment_verifiesParameterTypes() {
        // Given
        Long contractId = 42L;
        String orderId = "ORDER-123";
        LocalDateTime paidDate = LocalDateTime.of(2026, 1, 15, 10, 30);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

        when(jdbcTemplate.update(eq(QUERY_UPDATE_PAYMENT), paramsCaptor.capture()))
                .thenReturn(1);

        // When
        repository.updateInternetContractPayment(contractId, orderId, paidDate);

        // Then
        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(contractId, capturedParams.get("contractId"));
        assertEquals(orderId, capturedParams.get("orderId"));
        assertEquals(paidDate, capturedParams.get("paidDate"));
        assertInstanceOf(Long.class, capturedParams.get("contractId"));
        assertInstanceOf(String.class, capturedParams.get("orderId"));
        assertInstanceOf(LocalDateTime.class, capturedParams.get("paidDate"));
    }
}
