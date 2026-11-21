package com.social7.studentlearnanalytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.social7.studentlearnanalytics.localization.AppLanguageManager;
import com.social7.studentlearnanalytics.utils.WindowInsetsHelper;

public class SlasApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLanguageManager.applySavedPreferences(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                WindowInsetsHelper.applyToActivity(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
}
