package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.teacher.assessment.TeacherAssessmentFragment;
import com.social7.studentlearnanalytics.teacher.attendance.AttendanceFragment;

public class TeacherMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;
    private final View[] drawerItems = new View[12];
    private boolean isNavigating = false;

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

        setupDrawer();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isDrawerOpen()) {
                    closeDrawer();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != R.id.nav_teacher_home) {
                    bottomNavigation.setSelectedItemId(R.id.nav_teacher_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            if (!handleNavigationIntent(getIntent())) {
                openFragment(new TeacherDashboardFragment());
                if (bottomNavigation != null) {
                    bottomNavigation.setSelectedItemId(R.id.nav_teacher_home);
                }
            }
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.teacher_fragment_container);
            if (current != null) {
                syncBottomNavState(current);
                syncDrawerSelectionState(current);
            }
        });

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                if (isNavigating) {
                    return true;
                }
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
                    openFragment(new TeacherProfileOverviewFragment());
                } else {
                    return false;
                }
                return true;
            });
        }
    }

    private void setupDrawer() {
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(Color.parseColor("#99000000"));
        }

        View closeBtn = findViewById(R.id.btnTeacherDrawerClose);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> closeDrawer());
        }

        TextView tvName = findViewById(R.id.tvTeacherDrawerName);
        if (sessionManager != null && tvName != null) {
            String userName = sessionManager.getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                tvName.setText(userName);
            }
        }

        View headerInfo = findViewById(R.id.layoutTeacherHeaderInfo);
        View avatarContainer = findViewById(R.id.layoutTeacherAvatarContainer);
        View.OnClickListener profileClickListener = v -> {
            closeDrawer();
            openFragment(new TeacherProfileOverviewFragment());
        };
        if (headerInfo != null) headerInfo.setOnClickListener(profileClickListener);
        if (avatarContainer != null) avatarContainer.setOnClickListener(profileClickListener);

        bindDrawerItem(0, R.id.itemTeacherDrawerDashboard, R.drawable.ic_home, R.string.dashboard, 0);
        bindDrawerItem(1, R.id.itemTeacherDrawerStudents, R.drawable.ic_users, R.string.nav_students, R.string.assigned_student_roster);
        bindDrawerItem(2, R.id.itemTeacherDrawerCourses, R.drawable.ic_open_book, R.string.nav_courses, R.string.manage_assigned_courses);
        bindDrawerItem(3, R.id.itemTeacherDrawerAssessments, R.drawable.ic_open_book, R.string.assessments, R.string.assignments_quizzes_exams);
        bindDrawerItem(4, R.id.itemTeacherDrawerPrediction, R.drawable.ic_analytics, R.string.prediction_risk, R.string.at_risk_student_predictions);
        bindDrawerItem(5, R.id.itemTeacherDrawerScores, R.drawable.ic_star_active, R.string.manage_scores, R.string.record_assignment_exam_scores);
        bindDrawerItem(6, R.id.itemTeacherDrawerAttendance, R.drawable.ic_calendar, R.string.attendance, R.string.track_class_attendance);
        bindDrawerItem(7, R.id.itemTeacherDrawerRecommendation, R.drawable.ic_circle_info, R.string.recommendations, R.string.ai_academic_support_guidance);
        bindDrawerItem(8, R.id.itemTeacherDrawerReports, R.drawable.ic_chart_bar, R.string.nav_reports, R.string.export_class_performance_reports);
        bindDrawerItem(9, R.id.itemTeacherDrawerEvaluation, R.drawable.ic_circle_help, R.string.prediction_evaluation, R.string.model_accuracy_evaluation);
        bindDrawerItem(10, R.id.itemTeacherDrawerNotifications, R.drawable.ic_notifications, R.string.nav_notifications, R.string.academic_alerts_updates);
        bindDrawerItem(11, R.id.itemTeacherDrawerProfile, R.drawable.ic_profile, R.string.nav_profile, R.string.manage_faculty_profile);

        View btnLogout = findViewById(R.id.btnTeacherDrawerLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeDrawer();
                logout();
            });
        }
    }

    private void bindDrawerItem(int index, int layoutId, int iconRes, int titleRes, int subtitleRes) {
        View itemRoot = findViewById(layoutId);
        if (itemRoot == null) return;

        drawerItems[index] = itemRoot;

        ImageView imgIcon = itemRoot.findViewById(R.id.imgDrawerItemIcon);
        TextView tvTitle = itemRoot.findViewById(R.id.tvDrawerItemTitle);
        TextView tvSubtitle = itemRoot.findViewById(R.id.tvDrawerItemSubtitle);

        if (imgIcon != null) imgIcon.setImageResource(iconRes);
        if (tvTitle != null && titleRes != 0) tvTitle.setText(titleRes);
        if (tvSubtitle != null) {
            if (subtitleRes != 0) {
                tvSubtitle.setText(subtitleRes);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }
        }

        itemRoot.setOnClickListener(v -> {
            closeDrawer();
            selectDrawerItem(index);

            if (index == 0) openFragment(new TeacherDashboardFragment());
            else if (index == 1) openFragment(new StudentListFragment());
            else if (index == 2) openFragment(new CourseManagementFragment());
            else if (index == 3) openFragment(new TeacherAssessmentFragment());
            else if (index == 4) openFragment(new PredictionRiskFragment());
            else if (index == 5) openFragment(new ManageScoresFragment());
            else if (index == 6) openFragment(new AttendanceFragment());
            else if (index == 7) openFragment(new RecommendationFragment());
            else if (index == 8) openFragment(new ReportsFragment());
            else if (index == 9) openFragment(new PredictionEvaluationFragment());
            else if (index == 10) openFragment(new NotificationFragment());
            else if (index == 11) openFragment(new TeacherProfileOverviewFragment());
        });
    }

    private void selectDrawerItem(int selectedIndex) {
        int primaryBlue = Color.parseColor("#2563EB");
        int darkNavy = Color.parseColor("#0F172A");

        for (int i = 0; i < drawerItems.length; i++) {
            View root = drawerItems[i];
            if (root == null) continue;

            ImageView imgIcon = root.findViewById(R.id.imgDrawerItemIcon);
            TextView tvTitle = root.findViewById(R.id.tvDrawerItemTitle);
            TextView tvSubtitle = root.findViewById(R.id.tvDrawerItemSubtitle);
            ImageView imgChevron = root.findViewById(R.id.imgDrawerItemChevron);

            boolean isActive = (i == selectedIndex);

            if (isActive) {
                root.setBackgroundResource(R.drawable.bg_admin_drawer_item_active);
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(primaryBlue));
                if (tvTitle != null) tvTitle.setTextColor(primaryBlue);
                if (tvSubtitle != null) tvSubtitle.setTextColor(primaryBlue);
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(primaryBlue));
            } else {
                int mutedSlate = Color.parseColor("#64748B");
                root.setBackground(null);
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(mutedSlate));
                if (tvTitle != null) tvTitle.setTextColor(darkNavy);
                if (tvSubtitle != null) tvSubtitle.setTextColor(mutedSlate);
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(mutedSlate));
            }
        }
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void openFragment(@NonNull Fragment fragment) {
        boolean isTopLevel = (fragment instanceof TeacherDashboardFragment ||
                fragment instanceof StudentListFragment ||
                fragment instanceof CourseManagementFragment ||
                fragment instanceof ReportsFragment ||
                fragment instanceof TeacherProfileOverviewFragment);

        if (isTopLevel && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.teacher_fragment_container, fragment);

        if (!isTopLevel) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        syncBottomNavState(fragment);
        syncDrawerSelectionState(fragment);
    }

    private void syncDrawerSelectionState(Fragment fragment) {
        if (fragment instanceof TeacherDashboardFragment) selectDrawerItem(0);
        else if (fragment instanceof StudentListFragment) selectDrawerItem(1);
        else if (fragment instanceof CourseManagementFragment) selectDrawerItem(2);
        else if (fragment instanceof TeacherAssessmentFragment) selectDrawerItem(3);
        else if (fragment instanceof PredictionRiskFragment) selectDrawerItem(4);
        else if (fragment instanceof ManageScoresFragment) selectDrawerItem(5);
        else if (fragment instanceof AttendanceFragment) selectDrawerItem(6);
        else if (fragment instanceof RecommendationFragment) selectDrawerItem(7);
        else if (fragment instanceof ReportsFragment) selectDrawerItem(8);
        else if (fragment instanceof PredictionEvaluationFragment) selectDrawerItem(9);
        else if (fragment instanceof NotificationFragment) selectDrawerItem(10);
        else if (fragment instanceof TeacherProfileOverviewFragment || fragment instanceof TeacherProfileFragment) selectDrawerItem(11);
    }

    private void syncBottomNavState(Fragment fragment) {
        if (bottomNavigation == null) return;
        int targetId = -1;
        if (fragment instanceof TeacherDashboardFragment) {
            targetId = R.id.nav_teacher_home;
        } else if (fragment instanceof StudentListFragment) {
            targetId = R.id.nav_teacher_students;
        } else if (fragment instanceof CourseManagementFragment) {
            targetId = R.id.nav_teacher_courses;
        } else if (fragment instanceof ReportsFragment) {
            targetId = R.id.nav_teacher_reports;
        } else if (fragment instanceof TeacherProfileOverviewFragment || fragment instanceof TeacherProfileFragment) {
            targetId = R.id.nav_teacher_profile;
        }
        if (targetId != -1 && bottomNavigation.getSelectedItemId() != targetId) {
            isNavigating = true;
            bottomNavigation.setSelectedItemId(targetId);
            isNavigating = false;
        }
    }

    public void logout() {
        try {
            com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).logout().enqueue(
                    new retrofit2.Callback<com.google.gson.JsonObject>() {
                        @Override
                        public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {}

                        @Override
                        public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {}
                    }
            );
        } catch (Exception ignored) {
        }
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
    }

    private boolean handleNavigationIntent(Intent intent) {
        if (intent == null) return false;
        String target = intent.getStringExtra("navigate_to");
        if ("notifications".equalsIgnoreCase(target)) {
            openFragment(new NotificationFragment());
            return true;
        }
        if ("profile".equalsIgnoreCase(target)) {
            openFragment(new TeacherProfileOverviewFragment());
            return true;
        }
        return false;
    }

    public void setupTeacherHeader(View root, String title, String subtitle) {
        TeacherHeaderHelper.setupHeader(this, root, title, subtitle, false);
    }
}
