package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.teacher.attendance.AttendanceFragment;

public class TeacherMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private NavigationView navigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Teacher")) {
            return;
        }

        setContentView(R.layout.activity_teacher_main);

        drawerLayout = findViewById(R.id.teacher_drawer_layout);
        bottomNavigation = findViewById(R.id.teacher_bottom_navigation);
        navigationView = findViewById(R.id.teacher_navigation_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            openFragment(new TeacherDashboardFragment());
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_teacher_home);
            }
        }

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_teacher_home) {
                    openFragment(new TeacherDashboardFragment());
                } else if (itemId == R.id.nav_teacher_students) {
                    openFragment(new StudentListFragment());
                } else if (itemId == R.id.nav_teacher_courses) {
                    openFragment(new CourseManagementFragment());
                } else if (itemId == R.id.nav_teacher_reports) {
                    openFragment(new ReportsFragment());
                } else if (itemId == R.id.nav_teacher_profile) {
                    openFragment(new TeacherProfileFragment());
                } else {
                    return false;
                }
                return true;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Teacher")) {
            return;
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void openFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.teacher_fragment_container, fragment)
                .commit();

        syncBottomNavState(fragment);
    }

    private void syncBottomNavState(Fragment fragment) {
        if (bottomNavigation == null) return;
        if (fragment instanceof TeacherDashboardFragment) {
            bottomNavigation.getMenu().findItem(R.id.nav_teacher_home).setChecked(true);
        } else if (fragment instanceof StudentListFragment) {
            bottomNavigation.getMenu().findItem(R.id.nav_teacher_students).setChecked(true);
        } else if (fragment instanceof CourseManagementFragment) {
            bottomNavigation.getMenu().findItem(R.id.nav_teacher_courses).setChecked(true);
        } else if (fragment instanceof ReportsFragment) {
            bottomNavigation.getMenu().findItem(R.id.nav_teacher_reports).setChecked(true);
        } else if (fragment instanceof TeacherProfileFragment) {
            bottomNavigation.getMenu().findItem(R.id.nav_teacher_profile).setChecked(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_drawer_dashboard) {
            openFragment(new TeacherDashboardFragment());
        } else if (id == R.id.nav_drawer_students) {
            openFragment(new StudentListFragment());
        } else if (id == R.id.nav_drawer_courses) {
            openFragment(new CourseManagementFragment());
        } else if (id == R.id.nav_drawer_analytics) {
            openFragment(new ClassAnalyticsFragment());
        } else if (id == R.id.nav_drawer_prediction_risk) {
            openFragment(new PredictionRiskFragment());
        } else if (id == R.id.nav_drawer_scores) {
            openFragment(new ManageScoresFragment());
        } else if (id == R.id.nav_drawer_attendance) {
            openFragment(new AttendanceFragment());
        } else if (id == R.id.nav_drawer_recommendation) {
            openFragment(new RecommendationFragment());
        } else if (id == R.id.nav_drawer_reports) {
            openFragment(new ReportsFragment());
        } else if (id == R.id.nav_drawer_evaluation) {
            openFragment(new PredictionEvaluationFragment());
        } else if (id == R.id.nav_drawer_notifications) {
            openFragment(new NotificationFragment());
        } else if (id == R.id.nav_drawer_profile) {
            openFragment(new TeacherProfileFragment());
        } else if (id == R.id.nav_drawer_logout) {
            logout();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void logout() {
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
        redirectToLogin();
    }
}
