package ru.alfastrah.site.avto.ws.contact.signed.db.typehandlers;

import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDateTypeHandlerTest {

    private final LocalDateTypeHandler handler = new LocalDateTypeHandler();

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    @Test
    void setParameter_withNullValue_shouldSetNull() throws SQLException {
        handler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);

        verify(preparedStatement).setTimestamp(eq(1), eq(null));
    }

    @Test
    void setParameter_withLocalDate_shouldSetTimestampWithUTC() throws SQLException {
        LocalDate localDate = new LocalDate(2023, 5, 14);
        handler.setParameter(preparedStatement, 1, localDate, JdbcType.TIMESTAMP);

        verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class), any(java.util.Calendar.class));
    }

    @Test
    void getResult_fromResultSetByColumnName_withNullValue_shouldReturnNull() throws SQLException {
        when(resultSet.getTimestamp(eq("column"), any(java.util.Calendar.class))).thenReturn(null);

        Object result = handler.getResult(resultSet, "column");

        assertThat(result).isNull();
    }

    @Test
    void getResult_fromResultSetByColumnName_withTimestamp_shouldReturnLocalDate() throws SQLException {
        Timestamp timestamp = new Timestamp(1684053000000L);
        when(resultSet.getTimestamp(eq("column"), any(java.util.Calendar.class))).thenReturn(timestamp);

        Object result = handler.getResult(resultSet, "column");

        assertThat(result).isInstanceOf(LocalDate.class);
        LocalDate localDate = (LocalDate) result;
        assertThat(localDate.getYear()).isEqualTo(2023);
        assertThat(localDate.getMonthOfYear()).isEqualTo(5);
        assertThat(localDate.getDayOfMonth()).isEqualTo(14);
    }

    @Test
    void getResult_fromCallableStatement_withNullValue_shouldReturnNull() throws SQLException {
        when(callableStatement.getTimestamp(eq(1), any(java.util.Calendar.class))).thenReturn(null);

        Object result = handler.getResult(callableStatement, 1);

        assertThat(result).isNull();
    }

    @Test
    void getResult_fromCallableStatement_withTimestamp_shouldReturnLocalDate() throws SQLException {
        Timestamp timestamp = new Timestamp(1684053000000L);
        when(callableStatement.getTimestamp(eq(1), any(java.util.Calendar.class))).thenReturn(timestamp);

        Object result = handler.getResult(callableStatement, 1);

        assertThat(result).isInstanceOf(LocalDate.class);
        LocalDate localDate = (LocalDate) result;
        assertThat(localDate.getYear()).isEqualTo(2023);
        assertThat(localDate.getMonthOfYear()).isEqualTo(5);
        assertThat(localDate.getDayOfMonth()).isEqualTo(14);
    }
}