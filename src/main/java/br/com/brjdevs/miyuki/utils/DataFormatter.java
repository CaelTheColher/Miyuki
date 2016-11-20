package br.com.brjdevs.miyuki.utils;

import java.time.OffsetDateTime;

public class DataFormatter {
    public static String format(OffsetDateTime date) {
        StringBuilder builder = new StringBuilder();
        builder.append(substring(date.getDayOfWeek())).append(", ").append(substring(date.getMonth()));
        builder.append(' ').append(date.getDayOfMonth()).append("º, ").append(date.getYear());
        builder.append(' ').append(date.getHour()).append(':').append(date.getMinute());
        return builder.toString();
    }
    private static String substring(Object s) {
        return s.toString().toLowerCase().substring(0, 3);
    }
}
