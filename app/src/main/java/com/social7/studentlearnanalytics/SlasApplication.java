package com.social7.studentlearnanalytics;

import android.app.Application;

import com.social7.studentlearnanalytics.localization.AppLanguageManager;

public class SlasApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLanguageManager.applySavedPreferences(this);
    }
}
