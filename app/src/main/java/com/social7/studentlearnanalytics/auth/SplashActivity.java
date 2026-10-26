package com.social7.studentlearnanalytics.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.StudentMainActivity;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d("SLAS_AUTH", "SLAS: Splash checking session");
        SessionManager sessionManager = new SessionManager(this);
        boolean loggedIn = sessionManager.isLoggedIn();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (loggedIn) {
                String role = sessionManager.getUserRole();
                Log.d("SLAS_AUTH", "SLAS: Active session found for role: " + role);
                if ("Teacher".equalsIgnoreCase(role)) {
                    intent = new Intent(SplashActivity.this, TeacherMainActivity.class);
                } else if ("Admin".equalsIgnoreCase(role)) {
                    intent = new Intent(SplashActivity.this, AdminMainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, StudentMainActivity.class);
                }
            } else {
                Log.d("SLAS_AUTH", "SLAS: No active session. Navigating to LoginActivity.");
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }
}
