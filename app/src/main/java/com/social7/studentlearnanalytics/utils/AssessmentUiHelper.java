package com.social7.studentlearnanalytics.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Response;

public final class AssessmentUiHelper {

    private static final DecimalFormat MARK_FORMAT = new DecimalFormat("0.##");

    private AssessmentUiHelper() {}

    public static String formatMarks(double val) {
        return MARK_FORMAT.format(val);
    }

    public static String extractErrorMessage(Response<?> response, String fallback) {
        if (response == null) return fallback;
        if (response.code() == 401) {
            return "Session expired. Please log in again.";
        }
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (raw != null && !raw.trim().isEmpty()) {
                    JsonElement el = JsonParser.parseString(raw);
                    if (el.isJsonObject()) {
                        JsonObject obj = el.getAsJsonObject();
                        if (obj.has("errors") && obj.get("errors").isJsonObject()) {
                            JsonObject errs = obj.getAsJsonObject("errors");
                            for (Map.Entry<String, JsonElement> entry : errs.entrySet()) {
                                if (entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().size() > 0) {
                                    return entry.getValue().getAsJsonArray().get(0).getAsString();
                                }
                            }
                        }
                        if (obj.has("message") && !obj.get("message").isJsonNull()) {
                            String msg = obj.get("message").getAsString();
                            if (!msg.trim().isEmpty()) return msg.trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return fallback + " (HTTP " + response.code() + ")";
    }

    public static String optString(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null ? v.trim() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int optInt(JsonObject obj, String key, int fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return fallback;
        }
    }

    public static double optDouble(JsonObject obj, String key, double fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return fallback;
        }
    }

    public static boolean optBoolean(JsonObject obj, String key, boolean fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            return obj.get(key).getAsBoolean();
        } catch (Exception e) {
            return fallback;
        }
    }

    public static JsonArray extractDataArray(JsonObject body, String... fallbackKeys) {
        if (body == null) return new JsonArray();
        if (body.has("data") && body.get("data").isJsonArray()) {
            return body.getAsJsonArray("data");
        }
        for (String k : fallbackKeys) {
            if (body.has(k) && body.get(k).isJsonArray()) {
                return body.getAsJsonArray(k);
            }
        }
        return new JsonArray();
    }

    public static Date parseIsoDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String s = raw.trim();
        String[] utcPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };
        for (String p : utcPatterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(p, Locale.US);
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = fmt.parse(s);
                if (d != null) return d;
            } catch (Exception ignored) {}
        }
        String[] localPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String p : localPatterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(p, Locale.US);
                Date d = fmt.parse(s.length() > 19 ? s.substring(0, 19) : s);
                if (d != null) return d;
            } catch (Exception ignored) {}
        }
        if (s.length() >= 10) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s.substring(0, 10));
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static String formatDateShort(String raw) {
        Date d = parseIsoDate(raw);
        if (d == null) return raw != null && !raw.trim().isEmpty() ? raw : "TBA";
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(d);
    }

    public static String formatDateTime(String raw) {
        Date d = parseIsoDate(raw);
        if (d == null) return raw != null && !raw.trim().isEmpty() ? raw : "TBA";
        return new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(d);
    }

    public static boolean isPast(String raw) {
        Date d = parseIsoDate(raw);
        return d != null && d.before(new Date());
    }

    public static boolean isFuture(String raw) {
        Date d = parseIsoDate(raw);
        return d != null && d.after(new Date());
    }
}
