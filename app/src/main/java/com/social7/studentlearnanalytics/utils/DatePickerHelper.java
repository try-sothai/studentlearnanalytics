package com.social7.studentlearnanalytics.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Shared Calendar DatePicker (and Date+Time picker) utility enforcing ISO-8601
 * YYYY-MM-DD output without timezone day shifts and disabling raw keyboard typing.
 */
public final class DatePickerHelper {

    private DatePickerHelper() {
    }

    public static void attachDatePicker(
            @Nullable TextInputLayout til,
            @Nullable EditText et,
            boolean allowClear
    ) {
        attachDatePicker(til, et, allowClear, null);
    }

    public static void attachDatePicker(
            @Nullable TextInputLayout til,
            @Nullable EditText et,
            boolean allowClear,
            @Nullable Runnable onChanged
    ) {
        if (et == null) return;

        configureReadOnlyTrigger(et);
        if (til != null) {
            til.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            til.setEndIconDrawable(R.drawable.ic_calendar);
            til.setEndIconContentDescription(et.getContext().getString(R.string.calendar));
            til.setEndIconOnClickListener(v -> showDatePicker(et.getContext(), til, et, allowClear, onChanged));
        }
        et.setOnClickListener(v -> showDatePicker(et.getContext(), til, et, allowClear, onChanged));
    }

    public static void showDatePicker(
            Context context,
            @Nullable TextInputLayout til,
            EditText et,
            boolean allowClear,
            @Nullable Runnable onChanged
    ) {
        int[] ymd = parseYmdOrDefault(et.getText() != null ? et.getText().toString().trim() : "");
        DatePickerDialog dialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            et.setText(formatted);
            if (til != null) til.setError(null);
            et.setError(null);
            if (onChanged != null) onChanged.run();
        }, ymd[0], ymd[1], ymd[2]);

        if (allowClear) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.clear_date), (d, w) -> {
                et.setText("");
                if (til != null) til.setError(null);
                et.setError(null);
                if (onChanged != null) onChanged.run();
            });
        }
        dialog.show();
    }

    public static void showDateTimePicker(
            Context context,
            @Nullable TextInputLayout til,
            EditText et,
            boolean dateOnly,
            boolean allowClear
    ) {
        String existing = et.getText() != null ? et.getText().toString().trim() : "";
        int[] ymd = parseYmdOrDefault(existing);
        int[] hm = parseHmOrDefault(existing);

        DatePickerDialog dateDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            if (dateOnly) {
                et.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth));
                if (til != null) til.setError(null);
                return;
            }
            TimePickerDialog timeDialog = new TimePickerDialog(context, (tv, hourOfDay, minute) -> {
                et.setText(String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:00",
                        year, month + 1, dayOfMonth, hourOfDay, minute));
                if (til != null) til.setError(null);
            }, hm[0], hm[1], true);
            timeDialog.show();
        }, ymd[0], ymd[1], ymd[2]);

        if (allowClear) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.clear_date), (d, w) -> {
                et.setText("");
                if (til != null) til.setError(null);
            });
        }
        dateDialog.show();
    }

    public static boolean isValidIsoDate(@Nullable String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (!trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) return false;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fmt.setLenient(false);
        try {
            Date parsed = fmt.parse(trimmed);
            return parsed != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private static void configureReadOnlyTrigger(EditText et) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setClickable(true);
        et.setCursorVisible(false);
        et.setLongClickable(false);
        et.setInputType(InputType.TYPE_NULL);
    }

    private static int[] parseYmdOrDefault(String raw) {
        if (raw != null && raw.length() >= 10 && raw.substring(0, 10).matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            try {
                String[] parts = raw.substring(0, 10).split("-");
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int d = Integer.parseInt(parts[2]);
                if (y >= 1900 && y <= 2100 && m >= 0 && m <= 11 && d >= 1 && d <= 31) {
                    return new int[]{y, m, d};
                }
            } catch (Exception ignored) {
            }
        }
        Calendar c = Calendar.getInstance();
        return new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)};
    }

    private static int[] parseHmOrDefault(String raw) {
        if (raw != null && raw.length() >= 16) {
            String timePart = raw.substring(11, 16);
            if (timePart.matches("^\\d{2}:\\d{2}$")) {
                try {
                    String[] parts = timePart.split(":");
                    int h = Integer.parseInt(parts[0]);
                    int min = Integer.parseInt(parts[1]);
                    if (h >= 0 && h <= 23 && min >= 0 && min <= 59) {
                        return new int[]{h, min};
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return new int[]{23, 59};
    }
}
