package com.university.grademanagement.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Utility for date and timestamp formatting.
 */
public class DateUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_TIMESTAMP_FORMAT = "dd MMM yyyy, hh:mm a";

    private DateUtil() {
    }

    public static String formatDate(Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat(DISPLAY_DATE_FORMAT).format(date);
    }

    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) return "-";
        return new SimpleDateFormat(DISPLAY_TIMESTAMP_FORMAT).format(ts);
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return Date.valueOf(dateStr.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
