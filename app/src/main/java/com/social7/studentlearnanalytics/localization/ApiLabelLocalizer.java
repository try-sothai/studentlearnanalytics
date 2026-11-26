package com.social7.studentlearnanalytics.localization;

import android.content.Context;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import java.util.Calendar;
import java.util.Locale;

/**
 * Presentation-layer localizer for known system-defined enum values
 * returned by the Laravel REST API. Unknown or user-generated values
 * are returned unchanged so data integrity is never altered.
 */
public final class ApiLabelLocalizer {

    private ApiLabelLocalizer() {
    }

    public static String localizeAttendanceStatus(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "present":
                return context.getString(R.string.status_present);
            case "absent":
                return context.getString(R.string.status_absent);
            case "late":
                return context.getString(R.string.status_late);
            case "excused":
                return context.getString(R.string.status_excused);
            default:
                return raw;
        }
    }

    public static String localizeRiskLevel(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "low":
            case "low risk":
                return context.getString(R.string.risk_low);
            case "medium":
            case "moderate":
            case "medium risk":
                return context.getString(R.string.risk_medium);
            case "high":
            case "high risk":
            case "at risk":
            case "at_risk":
                return context.getString(R.string.risk_high);
            case "good":
                return context.getString(R.string.status_good);
            case "attention":
            case "needs attention":
                return context.getString(R.string.status_attention);
            default:
                return raw;
        }
    }

    public static String localizeAssignmentStatus(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "pending":
                return context.getString(R.string.status_pending);
            case "submitted":
                return context.getString(R.string.status_submitted);
            case "approved":
                return context.getString(R.string.status_approved);
            case "rejected":
                return context.getString(R.string.status_rejected);
            case "graded":
                return context.getString(R.string.status_graded);
            case "completed":
                return context.getString(R.string.status_completed);
            case "overdue":
                return context.getString(R.string.status_overdue);
            default:
                return raw;
        }
    }

    public static String localizeRole(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "student":
                return context.getString(R.string.role_student);
            case "teacher":
                return context.getString(R.string.role_teacher);
            case "admin":
                return context.getString(R.string.role_admin);
            default:
                return raw;
        }
    }

    public static String localizeUserStatus(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "active":
                return context.getString(R.string.status_active);
            case "inactive":
                return context.getString(R.string.status_inactive);
            case "enrolled":
                return context.getString(R.string.status_enrolled);
            case "graduated":
                return context.getString(R.string.status_graduated);
            case "suspended":
                return context.getString(R.string.status_suspended);
            default:
                return raw;
        }
    }

    public static String localizeGender(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "male":
                return context.getString(R.string.gender_male);
            case "female":
                return context.getString(R.string.gender_female);
            default:
                return raw;
        }
    }

    public static String localizePeriod(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "weekly":
                return context.getString(R.string.period_weekly);
            case "monthly":
                return context.getString(R.string.period_monthly);
            case "semester":
                return context.getString(R.string.period_semester);
            case "yearly":
                return context.getString(R.string.period_yearly);
            default:
                return raw;
        }
    }

    public static String localizeTrendText(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String trimmed = raw.trim();
        String key = trimmed.toLowerCase(Locale.ROOT);
        if (key.equals("no change this month") || key.equals("no change")) {
            return context.getString(R.string.no_change_this_month);
        }
        if (key.equals("no comparison data") || key.equals("no comparison data available")) {
            return context.getString(R.string.no_comparison_data);
        }
        if (key.equals("no data available") || key.equals("no data")) {
            return context.getString(R.string.no_data_available);
        }
        if (key.equals("online")) {
            return context.getString(R.string.status_online);
        }
        if (key.equals("offline")) {
            return context.getString(R.string.status_offline);
        }
        if (key.endsWith("this month")) {
            String prefix = trimmed.substring(0, trimmed.length() - "this month".length()).trim();
            return context.getString(R.string.trend_this_month_format, prefix);
        }
        if (key.endsWith("from last month")) {
            String prefix = trimmed.substring(0, trimmed.length() - "from last month".length()).trim();
            return context.getString(R.string.trend_from_last_month_format, prefix);
        }
        return raw;
    }

    public static String localizeSystemDisplayName(Context context, String raw) {
        if (context == null || raw == null) return raw != null ? raw : "";
        String key = raw.trim().toLowerCase(Locale.ROOT);
        if (key.equals("admin slas") || key.equals("slas admin")) {
            return context.getString(R.string.admin_slas);
        }
        if (key.equals("system administrator")) {
            return context.getString(R.string.system_administrator);
        }
        return raw;
    }

    public static String localizeYearLevel(Context context, Object raw) {
        if (raw == null) return "";
        String formatted = YearLevelHelper.toFullLabel(context, raw);
        return YearLevelHelper.isValid(raw) ? formatted : String.valueOf(raw);
    }

    public static String getTimeGreeting(Context context) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return context.getString(R.string.good_morning);
        } else if (hour < 17) {
            return context.getString(R.string.good_afternoon);
        } else {
            return context.getString(R.string.good_evening);
        }
    }
}
