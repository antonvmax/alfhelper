package ru.alfastrah.site.avto.ws.contact.signed.db.typehandlers;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class LocalDateTypeHandler implements TypeHandler<Object> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter,
            JdbcType jdbcType) throws SQLException {
        LocalDate ld = (LocalDate) parameter;

        if (ld != null) {
            DateTime dtUTC = ld.toDateTimeAtStartOfDay(DateTimeZone.UTC);
            Timestamp timestamp = new Timestamp(dtUTC.getMillis());

            Calendar calendar = Calendar.getInstance(TimeZone
                    .getTimeZone("UTC"));
            ps.setTimestamp(i, timestamp, calendar);
        } else {
            ps.setTimestamp(i, null);
        }

    }

    @Override
    public Object getResult(ResultSet rs, String columnName)
            throws SQLException {
        LocalDate ld = null;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp ts = rs.getTimestamp(columnName, calendar);
        if (ts != null) {
            ld = new DateTime(ts.getTime(), DateTimeZone.UTC).toLocalDate();
        }
        return ld;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        LocalDate ld = null;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp ts = cs.getTimestamp(columnIndex, calendar);
        if (ts != null) {
            ld = new DateTime(ts.getTime(), DateTimeZone.UTC).toLocalDate();
        }
        return ld;
    }

}