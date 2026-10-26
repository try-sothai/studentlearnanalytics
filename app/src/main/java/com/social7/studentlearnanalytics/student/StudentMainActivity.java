package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class StudentMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Log.d("SLAS_AUTH", "SLAS: Invalid session in StudentMainActivity onCreate. Redirecting to LoginActivity.");
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_student_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Add badge to notifications
        com.google.android.material.badge.BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notifications);
        badge.setVisible(true);
        badge.setNumber(3);
        badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.danger_red));

        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {

                openFragment(new StudentDashboardFragment());

            } else if (itemId == R.id.nav_courses) {

                // Open dedicated Courses view
                openFragment(new StudentCoursesFragment());

            } else if (itemId == R.id.nav_analytics) {

                openFragment(new LearningAnalyticsFragment());

            } else if (itemId == R.id.nav_notifications) {

                openFragment(new NotificationFragment());

            } else if (itemId == R.id.nav_profile) {

                openFragment(new ProfileFragment());

            } else {

                return false;
            }

            return true;
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && !sessionManager.isLoggedIn()) {
            Log.d("SLAS_AUTH", "SLAS: Invalid session in StudentMainActivity onResume. Redirecting to LoginActivity.");
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("select_tab")) {
            int tabId = intent.getIntExtra("select_tab", R.id.nav_home);
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(tabId);
            }
        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            openFragment(new StudentDashboardFragment());
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void openFragment(@NonNull Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
