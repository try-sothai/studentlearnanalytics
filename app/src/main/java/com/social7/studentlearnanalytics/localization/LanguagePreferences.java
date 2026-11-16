package com.social7.studentlearnanalytics.localization;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists application-wide UI preferences (language, theme, font size)
 * independently from user authentication sessions so preferences survive
 * logout, login, and role switching.
 */
public class LanguagePreferences {

    private static final String PREF_NAME = "SLAS_AppPreferences";
    private static final String KEY_LANGUAGE_TAG = "app_language_tag";
    private static final String KEY_THEME_MODE = "app_theme_mode";
    private static final String KEY_FONT_SIZE_INDEX = "app_font_size_index";

    private final SharedPreferences prefs;

    public LanguagePreferences(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getLanguageTag() {
        String tag = prefs.getString(KEY_LANGUAGE_TAG, LanguageConstants.LANG_ENGLISH);
        if (LanguageConstants.LANG_KHMER.equals(tag)) {
            return LanguageConstants.LANG_KHMER;
        }
        return LanguageConstants.LANG_ENGLISH;
    }

    public void setLanguageTag(String languageTag) {
        String normalized = LanguageConstants.LANG_KHMER.equals(languageTag)
                ? LanguageConstants.LANG_KHMER
                : LanguageConstants.LANG_ENGLISH;
        prefs.edit().putString(KEY_LANGUAGE_TAG, normalized).apply();
    }

    public String getThemeMode() {
        String mode = prefs.getString(KEY_THEME_MODE, LanguageConstants.THEME_LIGHT);
        if (LanguageConstants.THEME_DARK.equals(mode) || LanguageConstants.THEME_SYSTEM.equals(mode)) {
            return mode;
        }
        return LanguageConstants.THEME_LIGHT;
    }

    public void setThemeMode(String themeMode) {
        String normalized = LanguageConstants.THEME_LIGHT;
        if (LanguageConstants.THEME_DARK.equals(themeMode)) {
            normalized = LanguageConstants.THEME_DARK;
        } else if (LanguageConstants.THEME_SYSTEM.equals(themeMode)) {
            normalized = LanguageConstants.THEME_SYSTEM;
        }
        prefs.edit().putString(KEY_THEME_MODE, normalized).apply();
    }

    public int getFontSizeIndex() {
        int index = prefs.getInt(KEY_FONT_SIZE_INDEX, LanguageConstants.FONT_STANDARD);
        if (index < LanguageConstants.FONT_STANDARD || index > LanguageConstants.FONT_LARGE) {
            return LanguageConstants.FONT_STANDARD;
        }
        return index;
    }

    public void setFontSizeIndex(int index) {
        int clamped = Math.max(LanguageConstants.FONT_STANDARD, Math.min(LanguageConstants.FONT_LARGE, index));
        prefs.edit().putInt(KEY_FONT_SIZE_INDEX, clamped).apply();
    }
}
