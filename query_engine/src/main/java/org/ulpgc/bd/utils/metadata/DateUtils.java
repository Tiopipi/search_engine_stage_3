package org.ulpgc.bd.utils.metadata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    private DateUtils() {
    }

    public static String convertDate(String dateStr, String inputPattern, String outputPattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(inputPattern).withLocale(Locale.ENGLISH);
            LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
            return date.format(DateTimeFormatter.ofPattern(outputPattern));
        } catch (Exception e) {
            System.err.println("Error while transforming the date: '" + dateStr + "'");
            return "";
        }
    }
}
