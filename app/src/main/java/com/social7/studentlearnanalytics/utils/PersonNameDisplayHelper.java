package com.social7.studentlearnanalytics.utils;

import androidx.annotation.Nullable;

/**
 * Normalizes Khmer/English person names and resolves consistent display names
 * without fabricating missing-language entries.
 */
public final class PersonNameDisplayHelper {

    private PersonNameDisplayHelper() {
    }

    public static String clean(@Nullable String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", " ");
    }

    public static boolean hasAtLeastOneName(@Nullable String khmerName, @Nullable String englishName) {
        return !clean(khmerName).isEmpty() || !clean(englishName).isEmpty();
    }

    public static String resolveDisplayName(
            @Nullable String englishName,
            @Nullable String khmerName,
            @Nullable String fallback
    ) {
        String en = clean(englishName);
        if (!en.isEmpty()) return en;

        String km = clean(khmerName);
        if (!km.isEmpty()) return km;

        String fb = clean(fallback);
        return !fb.isEmpty() ? fb : "";
    }
}
