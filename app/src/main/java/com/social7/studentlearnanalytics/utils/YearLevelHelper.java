package com.social7.studentlearnanalytics.utils;

import android.content.Context;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central canonical Student Year Level helper (Y1 - Y4).
 *
 * Strictly separates Student Year Level (Y1, Y2, Y3, Y4) from calendar Academic Year
 * (e.g. 2025-2026, 2026-2027) across models, forms, filters, adapters, and reports.
 */
public final class YearLevelHelper {

    public static final int MIN_YEAR_LEVEL = 1;
    public static final int MAX_YEAR_LEVEL = 4;

    private static final Pattern ACADEMIC_YEAR_PATTERN = Pattern.compile("\\d{4}\\s*[-–]\\s*\\d{4}");
    private static final Pattern TEXT_YEAR_PATTERN = Pattern.compile("^(?:year|y|yr)?\\s*0*([1-4])$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIX_YEAR_PATTERN = Pattern.compile("^(?:year|yr|y)\\s*0*([1-4])\\b.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern KHMER_YEAR_PATTERN = Pattern.compile("^ឆ្នាំទី\\s*([1-4១២៣៤]).*$");

    private YearLevelHelper() {
    }

    public static Integer parse(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            double d = ((Number) raw).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d) || d != Math.floor(d)) {
                return null;
            }
            int v = (int) d;
            return (v >= MIN_YEAR_LEVEL && v <= MAX_YEAR_LEVEL) ? v : null;
        }

        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "n/a".equalsIgnoreCase(text)
                || "unassigned".equalsIgnoreCase(text) || "not set".equalsIgnoreCase(text)) {
            return null;
        }
        if (ACADEMIC_YEAR_PATTERN.matcher(text).find()) {
            return null;
        }

        String normalizedKhmer = normalizeKhmerDigits(text);
        if (normalizedKhmer.matches("^0*[1-4]\\.0+$")) {
            normalizedKhmer = normalizedKhmer.replaceAll("\\.0+$", "");
        }

        Matcher m = TEXT_YEAR_PATTERN.matcher(normalizedKhmer);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }

        Matcher pref = PREFIX_YEAR_PATTERN.matcher(normalizedKhmer);
        if (pref.matches()) {
            return Integer.parseInt(pref.group(1));
        }

        Matcher km = KHMER_YEAR_PATTERN.matcher(text);
        if (km.matches()) {
            String d = normalizeKhmerDigits(km.group(1));
            int v = Integer.parseInt(d);
            return (v >= MIN_YEAR_LEVEL && v <= MAX_YEAR_LEVEL) ? v : null;
        }

        return null;
    }

    public static boolean isValid(Object raw) {
        return parse(raw) != null;
    }

    public static String toCode(Object raw) {
        Integer level = parse(raw);
        return level != null ? "Y" + level : null;
    }

    public static String toStorageString(Object raw) {
        Integer level = parse(raw);
        return level != null ? String.valueOf(level) : null;
    }

    public static String toDisplayName(Context context, Object raw) {
        Integer level = parse(raw);
        if (level == null) {
            return "";
        }
        if (context != null) {
            switch (level) {
                case 1:
                    return context.getString(R.string.year_level_1_name);
                case 2:
                    return context.getString(R.string.year_level_2_name);
                case 3:
                    return context.getString(R.string.year_level_3_name);
                case 4:
                    return context.getString(R.string.year_level_4_name);
                default:
                    break;
            }
        }
        return "Year " + level;
    }

    public static String toBadgeText(Context context, Object raw) {
        String code = toCode(raw);
        if (code != null) {
            return code;
        }
        if (context != null) {
            return context.getString(R.string.year_level_unassigned);
        }
        return "Unassigned";
    }

    public static String toFullLabel(Context context, Object raw) {
        Integer level = parse(raw);
        if (level == null) {
            return context != null ? context.getString(R.string.year_level_unassigned) : "Unassigned";
        }
        String code = "Y" + level;
        String name = toDisplayName(context, level);
        return String.format(Locale.ROOT, "%s — %s", code, name);
    }

    public static String toApiFilterParam(Object raw) {
        return toCode(raw);
    }

    public static List<Integer> canonicalLevels() {
        return Arrays.asList(1, 2, 3, 4);
    }

    public static List<String> getFormDropdownOptions(Context context) {
        List<String> list = new ArrayList<>();
        for (int level : canonicalLevels()) {
            list.add(toFullLabel(context, level));
        }
        return list;
    }

    public static List<String> getFilterDropdownOptions(Context context) {
        List<String> list = new ArrayList<>();
        list.add(context != null ? context.getString(R.string.all_year_levels) : "All Year Levels");
        for (int level : canonicalLevels()) {
            list.add(toFullLabel(context, level));
        }
        return list;
    }

    public static int spinnerIndexFor(Object raw, boolean includesAllOption) {
        Integer level = parse(raw);
        if (level == null) {
            return 0;
        }
        return includesAllOption ? level : (level - 1);
    }

    private static String normalizeKhmerDigits(String input) {
        return input
                .replace('១', '1')
                .replace('២', '2')
                .replace('៣', '3')
                .replace('៤', '4')
                .replace('៥', '5')
                .replace('៦', '6')
                .replace('៧', '7')
                .replace('៨', '8')
                .replace('៩', '9')
                .replace('០', '0');
    }
}
