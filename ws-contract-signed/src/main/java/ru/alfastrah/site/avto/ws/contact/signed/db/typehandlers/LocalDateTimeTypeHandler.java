package ru.alfastrah.site.avto.ws.contact.signed.db.typehandlers;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LocalDateTimeTypeHandler implements TypeHandler<Object> {
    /**
     * Константа - временная зона UTC, принимаемая как промежуточная для
     * пересчётов
     */
    private static final org.joda.time.DateTimeZone TZ_UTC = org.joda.time.DateTimeZone.UTC;

    /**
     * Константа - календарь в зоне UTC для установки и считывания jdbc
     * TIMESTAMP из/в переменных java.sql.Timestamp без сдвига часового пояса
     */
    private final java.util.Calendar calendarUtc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jt) throws SQLException {
        // параметр должен быть типа joda LocalDateTime
        LocalDateTime localDateTime = (LocalDateTime) parameter;
        // если параметр null, то просто устанавливаем значение в null
        if (localDateTime == null) {
            ps.setTimestamp(i, null);
        } else {
            // преобразуем местное дату время в абсолютное в зоне UTC, при этом
            // поля даты-времени остаются без изменения
            DateTime dateTimeInUTC = localDateTime.toDateTime(TZ_UTC);

            // создаём Timestamp с тем же точно значением миллисекунд от начала
            // эпохи, что и у нашего DateTime в UTC
            Timestamp timeStampInUTC = new Timestamp(dateTimeInUTC.getMillis());

            // устанавливаем значение TIMESTAMP с созданным Timestamp, явно
            // указывая, что нужно использовать часовой пояс UTC. При этом JDBC
            // не должен производить каких-либо конвертаций значения
            ps.setTimestamp(i, timeStampInUTC, calendarUtc);
        }
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        // получаем значение jdbc-TIMESTAMP в Timestamp, явно указывая, что
        // нужно использовать часовой пояс UTC.
        // При этом JDBC не должен производить каких-либо конвертаций значения
        Timestamp timeStampInUTC = rs.getTimestamp(columnName, calendarUtc);
        // если значение из TDS null, то просто возвращаем null
        if (timeStampInUTC == null) {
            return null;
        } else {
            DateTime dateTimeInUTC = new DateTime(timeStampInUTC.getTime(), TZ_UTC);
            return dateTimeInUTC.toLocalDateTime();
        }
    }

    @Override
    public Object getResult(ResultSet rs, int i) throws SQLException {
        Timestamp timeStampInUTC = rs.getTimestamp(i, calendarUtc);
        // если значение из TDS null, то просто возвращаем null
        if (timeStampInUTC == null) {
            return null;
        } else {
            DateTime dateTimeInUTC = new DateTime(timeStampInUTC.getTime(), TZ_UTC);
            return dateTimeInUTC.toLocalDateTime();
        }
    }

    @Override
    public Object getResult(CallableStatement cs, int column) throws SQLException {
        Timestamp timeStampInUTC = cs.getTimestamp(column, calendarUtc);
        // если значение из TDS null, то просто возвращаем null
        if (timeStampInUTC == null) {
            return null;
        } else {
            DateTime dateTimeInUTC = new DateTime(timeStampInUTC.getTime(), TZ_UTC);
            return dateTimeInUTC.toLocalDateTime();
        }
    }
}
