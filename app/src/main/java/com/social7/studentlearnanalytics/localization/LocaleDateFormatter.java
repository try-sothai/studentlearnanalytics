package com.social7.studentlearnanalytics.localization;

import android.content.Context;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Locale-aware date, number, percentage, and GPA formatting helpers.
 * Underlying API/database values are never modified.
 */
public final class LocaleDateFormatter {

    private LocaleDateFormatter() {
    }

    public static String formatIsoDate(Context context, String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return "";
        String trimmed = isoDate.trim();
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
        for (String p : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(p, Locale.US);
                Date d = parser.parse(trimmed);
                if (d != null) {
                    Locale loc = AppLanguageManager.getCurrentLocale(context);
                    return DateFormat.getDateInstance(DateFormat.MEDIUM, loc).format(d);
                }
            } catch (ParseException ignored) {
            }
        }
        return trimmed;
    }

    public static String formatNumber(Context context, double value, int maxFractionDigits) {
        Locale loc = AppLanguageManager.getCurrentLocale(context);
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        nf.setMaximumFractionDigits(maxFractionDigits);
        nf.setMinimumFractionDigits(0);
        return nf.format(value);
    }

    public static String formatGpa(Context context, double gpa) {
        Locale loc = AppLanguageManager.getCurrentLocale(context);
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(gpa);
    }

    public static String formatPercent(Context context, double percent) {
        Locale loc = AppLanguageManager.getCurrentLocale(context);
        NumberFormat nf = NumberFormat.getNumberInstance(loc);
        nf.setMaximumFractionDigits(1);
        return nf.format(percent) + "%";
    }
}
