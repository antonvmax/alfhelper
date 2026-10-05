package ru.alfastrah.site.avto.payment.cheque.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PartnerCalculationRepository.class})
class PartnerCalculationRepositoryTest {
    @Autowired
    PartnerCalculationRepository repository;

    @MockBean
    NamedParameterJdbcTemplate unicusJdbcTemplate;

    @Test
    void returnFalseWhenResultSetIsEmpty() {
        when(unicusJdbcTemplate.query(anyString(), any(Map.class), any(RowMapper.class))).thenReturn(null);

        assertFalse(repository.isAssociatedUpidWithContract("1234", "123"));
    }

    @Test
    void returnTrueWhenContractIsAssociatedWithUpid() {
        when(unicusJdbcTemplate.query(anyString(), any(Map.class), any(RowMapper.class))).thenReturn(List.of(true));

        assertTrue(repository.isAssociatedUpidWithContract("1234", "123"));
    }

    @Test
    void returnTrueWhenContractIsNotAssociatedWithUpid() {
        when(unicusJdbcTemplate.query(anyString(), any(Map.class), any(RowMapper.class))).thenReturn(List.of(false));

        assertFalse(repository.isAssociatedUpidWithContract("1234", "123"));
    }

}