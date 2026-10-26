package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class NavigationMenuActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        sessionManager = new SessionManager(this);

        // Close button
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // 1. Home
        findViewById(R.id.menu_home).setOnClickListener(v -> openTab(R.id.nav_home));

        // 2. Learning Analytics
        findViewById(R.id.menu_analytics).setOnClickListener(v -> openTab(R.id.nav_analytics));

        // 3. Prediction & Risk
        findViewById(R.id.menu_prediction).setOnClickListener(v -> {
            Intent intent = new Intent(this, RiskAnalysisActivity.class);
            startActivity(intent);
            finish();
        });

        // 4. Recommendations
        findViewById(R.id.menu_recommendation).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendationDetailActivity.class);
            startActivity(intent);
            finish();
        });

        // 5. Notifications
        findViewById(R.id.menu_notifications).setOnClickListener(v -> openTab(R.id.nav_notifications));

        // 6. Reports
        findViewById(R.id.menu_reports).setOnClickListener(v -> {
            Intent intent = new Intent(this, LearningReportActivity.class);
            startActivity(intent);
            finish();
        });

        // 7. Profile
        findViewById(R.id.menu_profile).setOnClickListener(v -> openTab(R.id.nav_profile));

        // 8. Settings
        findViewById(R.id.menu_settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        });

        // 9. Logout
        findViewById(R.id.menu_logout).setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openTab(int tabId) {
        Intent intent = new Intent(this, StudentMainActivity.class);
        intent.putExtra("select_tab", tabId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
