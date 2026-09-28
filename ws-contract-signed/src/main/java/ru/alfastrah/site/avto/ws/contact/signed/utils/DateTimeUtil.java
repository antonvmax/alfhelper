package ru.alfastrah.site.avto.ws.contact.signed.utils;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.sql.Timestamp;

public class DateTimeUtil {
    private DateTimeUtil() {
    }

    public static LocalDate convertD(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return new LocalDate(timestamp.getTime());
    }

    public static LocalDateTime convert(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new LocalDateTime(timestamp.getTime());
    }

    public static Timestamp convert(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return new Timestamp(localDate.toDate().getTime());
    }

    public static Timestamp convert(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return new Timestamp(localDateTime.toDate().getTime());
    }
}
