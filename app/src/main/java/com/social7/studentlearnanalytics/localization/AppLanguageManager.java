package com.social7.studentlearnanalytics.localization;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * Centralized manager for application-wide language and appearance preferences
 * using AppCompat per-app locales.
 */
public final class AppLanguageManager {

    private AppLanguageManager() {
    }

    public static void applySavedPreferences(Context context) {
        LanguagePreferences prefs = new LanguagePreferences(context);
        applyTheme(prefs.getThemeMode());
        applyLocale(prefs.getLanguageTag());
    }

    public static void setLanguage(Context context, String languageTag) {
        LanguagePreferences prefs = new LanguagePreferences(context);
        prefs.setLanguageTag(languageTag);
        applyLocale(prefs.getLanguageTag());
    }

    public static void setThemeMode(Context context, String themeMode) {
        LanguagePreferences prefs = new LanguagePreferences(context);
        prefs.setThemeMode(themeMode);
        applyTheme(prefs.getThemeMode());
    }

    public static void applyLocale(String languageTag) {
        LocaleListCompat desired = LocaleListCompat.forLanguageTags(languageTag);
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (!desired.toLanguageTags().equals(current.toLanguageTags())) {
            AppCompatDelegate.setApplicationLocales(desired);
        }
    }

    public static void applyTheme(String themeMode) {
        int desiredMode = AppCompatDelegate.MODE_NIGHT_NO;
        if (LanguageConstants.THEME_DARK.equals(themeMode)) {
            desiredMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (LanguageConstants.THEME_SYSTEM.equals(themeMode)) {
            desiredMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
            AppCompatDelegate.setDefaultNightMode(desiredMode);
        }
    }

    public static String getCurrentLanguageTag(Context context) {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (!current.isEmpty()) {
            String tag = current.toLanguageTags();
            if (tag.startsWith("km")) {
                return LanguageConstants.LANG_KHMER;
            }
            if (tag.startsWith("en")) {
                return LanguageConstants.LANG_ENGLISH;
            }
        }
        return new LanguagePreferences(context).getLanguageTag();
    }

    public static boolean isKhmer(Context context) {
        return LanguageConstants.LANG_KHMER.equals(getCurrentLanguageTag(context));
    }

    public static Locale getCurrentLocale(Context context) {
        return isKhmer(context) ? new Locale("km", "KH") : Locale.US;
    }
}
