package com.flick.helper.helpers;

import java.time.ZonedDateTime;
import static java.time.ZonedDateTime.now;
import java.util.Date;

public class DateHelper {

    public static Date dateNow() {
        return Date.from(now().toInstant());
    }

    public static Date toDate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.toInstant());
    }

    public static long toMillis(ZonedDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();

    }
}
