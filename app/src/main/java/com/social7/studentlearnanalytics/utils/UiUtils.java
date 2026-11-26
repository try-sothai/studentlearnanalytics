package com.social7.studentlearnanalytics.utils;

import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared UI and JSON null-safety utilities across Admin, Teacher, and Student screens.
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * Smoothly centers a selected tab inside its enclosing HorizontalScrollView so labels never stay clipped.
     */
    public static void scrollToTab(@Nullable View tabView) {
        if (tabView == null) return;
        ViewParent parent = tabView.getParent();
        while (parent != null && !(parent instanceof HorizontalScrollView)) {
            parent = parent.getParent();
        }
        if (parent instanceof HorizontalScrollView) {
            HorizontalScrollView hsv = (HorizontalScrollView) parent;
            hsv.post(() -> {
                int targetX = tabView.getLeft() - ((hsv.getWidth() - tabView.getWidth()) / 2);
                hsv.smoothScrollTo(Math.max(0, targetX), 0);
            });
        }
    }

    public static String optString(@Nullable JsonObject obj, @Nullable String key, String fallback) {
        if (obj == null || key == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static int optInt(@Nullable JsonObject obj, @Nullable String key, int fallback) {
        if (obj == null || key == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static float optFloat(@Nullable JsonObject obj, @Nullable String key, float fallback) {
        if (obj == null || key == null || !obj.has(key)) return fallback;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsFloat();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * Computes percentage growth between two periods rounded to 1 decimal place.
     */
    public static double calculateGrowthPercentage(double current, double previous) {
        if (previous <= 0.0) return 0.0;
        return Math.round(((current - previous) / previous) * 1000.0) / 10.0;
    }

    /**
     * Parses Laravel NotificationController JSON payload ({data: [...]} or {notifications: [...]}) into Notification items.
     */
    public static List<Notification> parseNotificationsJson(@Nullable JsonObject body) {
        List<Notification> result = new ArrayList<>();
        if (body == null) return result;
        JsonArray array = null;
        if (body.has("data") && body.get("data").isJsonArray()) {
            array = body.getAsJsonArray("data");
        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
            array = body.getAsJsonArray("notifications");
        }
        if (array == null) return result;

        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            String id = optString(obj, "id", "");
            String title = optString(obj, "title", "Notification");
            String message = optString(obj, "message", "");
            String category = optString(obj, "category", "");
            if (category.isEmpty()) {
                category = optString(obj, "type", "System");
            }
            String dateGroup = optString(obj, "date_group", "RECENT");
            String timeAgo = optString(obj, "time_ago", "");
            if (timeAgo.isEmpty()) {
                timeAgo = optString(obj, "created_at", "");
                if (timeAgo.length() > 16) {
                    timeAgo = timeAgo.substring(0, 16).replace('T', ' ');
                }
            }
            if (timeAgo.isEmpty()) timeAgo = "Recent";

            boolean isRead = false;
            if (obj.has("read_at") && !obj.get("read_at").isJsonNull()) {
                isRead = true;
            } else if (obj.has("is_read") && !obj.get("is_read").isJsonNull()) {
                JsonElement readEl = obj.get("is_read");
                if (readEl.isJsonPrimitive()) {
                    com.google.gson.JsonPrimitive prim = readEl.getAsJsonPrimitive();
                    if (prim.isBoolean()) {
                        isRead = prim.getAsBoolean();
                    } else if (prim.isNumber()) {
                        isRead = prim.getAsInt() == 1;
                    } else {
                        String s = prim.getAsString();
                        isRead = "true".equalsIgnoreCase(s) || "1".equals(s);
                    }
                }
            }

            boolean isImportant = "security".equalsIgnoreCase(category)
                    || "alert".equalsIgnoreCase(category)
                    || "high".equalsIgnoreCase(optString(obj, "priority", ""));

            result.add(new Notification(id, title, message, timeAgo, normalizeNotifCategory(category), dateGroup, isRead, isImportant));
        }
        return result;
    }

    public static String normalizeNotifCategory(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty()) return "System";
        String lower = raw.trim().toLowerCase(java.util.Locale.US);
        if ("user".equals(lower) || "users".equals(lower) || "account".equals(lower)) return "User";
        if ("course".equals(lower) || "courses".equals(lower) || "academic".equals(lower)) return "Course";
        if ("system".equals(lower)) return "System";
        if ("security".equals(lower)) return "Security";
        if ("report".equals(lower)) return "Report";
        if ("assignment".equals(lower)) return "Assignment";
        if ("homework".equals(lower)) return "Homework";
        if ("quiz".equals(lower)) return "Quiz";
        if ("exam".equals(lower)) return "Exam";
        if ("grade".equals(lower)) return "Grade";
        if ("attendance".equals(lower)) return "Attendance";
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
