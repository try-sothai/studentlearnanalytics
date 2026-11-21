package com.social7.studentlearnanalytics.utils;

import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
}
