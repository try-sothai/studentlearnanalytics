package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.ClassManagementFragment;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class AdminMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;
    private final View[] drawerItems = new View[13];
    private boolean isNavigating = false;
    private boolean isAnalyticsGroupExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_admin_main);

        drawerLayout = findViewById(R.id.admin_drawer_layout);
        bottomNavigation = findViewById(R.id.admin_bottom_navigation);

        setupDrawer();

        if (savedInstanceState == null) {
            openFragment(new AdminDashboardFragment());
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_admin_dashboard);
            }
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.admin_fragment_container);
            if (current != null) {
                syncAdminNavState(current);
            }
        });

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                if (isNavigating) {
                    return true;
                }

                int itemId = item.getItemId();

                if (itemId == R.id.nav_admin_dashboard) {
                    openFragment(new AdminDashboardFragment());
                } else if (itemId == R.id.nav_admin_users) {
                    openFragment(new UserManagementFragment());
                } else if (itemId == R.id.nav_admin_courses) {
                    openFragment(new CourseManagementFragment());
                } else if (itemId == R.id.nav_admin_reports) {
                    openFragment(new ReportsFragment());
                } else if (itemId == R.id.nav_admin_settings) {
                    openFragment(new SystemSettingsFragment());
                } else {
                    return false;
                }

                return true;
            });
        }
        
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isDrawerOpen()) {
                    closeDrawer();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != R.id.nav_admin_dashboard) {
                    bottomNavigation.setSelectedItemId(R.id.nav_admin_dashboard);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupDrawer() {
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(Color.parseColor("#99000000"));
            drawerLayout.setDrawerElevation(24f * getResources().getDisplayMetrics().density);
        }

        View closeBtn = findViewById(R.id.btnAdminDrawerClose);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> closeDrawer());
        }

        TextView tvName = findViewById(R.id.tvAdminDrawerName);
        if (sessionManager != null && tvName != null) {
            String userName = sessionManager.getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                tvName.setText(com.social7.studentlearnanalytics.localization.ApiLabelLocalizer
                        .localizeSystemDisplayName(this, userName.trim()));
            }
        }

        View avatarContainer = findViewById(R.id.layoutAvatarContainer);
        View.OnClickListener profileClickListener = v -> {
            closeDrawer();
            Intent intent = new Intent(this, com.social7.studentlearnanalytics.admin.users.ProfileSettingsActivity.class);
            startActivity(intent);
        };

        if (avatarContainer != null) avatarContainer.setOnClickListener(profileClickListener);
        if (tvName != null) tvName.setOnClickListener(profileClickListener);
        View tvRole = findViewById(R.id.tvAdminDrawerRole);
        if (tvRole != null) tvRole.setOnClickListener(profileClickListener);

        bindDrawerItem(0, R.id.itemDrawerDashboard, R.drawable.ic_home, R.string.dashboard, R.string.dashboard_overview_subtitle);
        bindDrawerItem(1, R.id.itemDrawerUserManagement, R.drawable.ic_users, R.string.user_management, R.string.students_teachers_admins);
        bindDrawerItem(2, R.id.itemDrawerCourseManagement, R.drawable.ic_open_book, R.string.course_management, R.string.manage_courses_and_curriculum);
        bindDrawerItem(3, R.id.itemDrawerClassManagement, R.drawable.ic_group, R.string.class_management, R.string.manage_classes_schedules_and_buildings);
        bindDrawerItem(4, R.id.itemDrawerReports, R.drawable.ic_chart_bar, R.string.reports_analytics, R.string.view_system_reports_and_statistics);
        bindDrawerItem(5, R.id.itemDrawerNotifications, R.drawable.ic_notifications, R.string.nav_notifications, R.string.view_and_manage_notifications);
        bindDrawerItem(6, R.id.itemDrawerSettings, R.drawable.ic_nav_settings, R.string.nav_settings, R.string.account_and_system_settings);

        bindDrawerItem(7, R.id.itemDrawerHelp, R.drawable.ic_circle_help, R.string.help_support, R.string.get_help_and_contact_support);
        bindDrawerItem(8, R.id.itemDrawerAbout, R.drawable.ic_circle_info, R.string.about, R.string.app_version_and_system_information);

        bindDrawerItem(9, R.id.itemDrawerMlAnalytics, R.drawable.ic_prediction, R.string.ml_risk_analytics, 0);
        bindDrawerItem(10, R.id.itemDrawerFacultyEval, R.drawable.ic_graduation_cap, R.string.faculty_evaluations, 0);
        bindDrawerItem(11, R.id.itemDrawerIncidentWorkspace, R.drawable.ic_warning_triangle, R.string.incident_workspace, 0);
        bindDrawerItem(12, R.id.itemDrawerSystemStats, R.drawable.ic_analytics, R.string.system_statistics, 0);

        View analyticsToggle = findViewById(R.id.layoutDrawerAnalyticsToggle);
        View analyticsChildren = findViewById(R.id.layoutDrawerAnalyticsChildren);
        ImageView analyticsChevron = findViewById(R.id.imgDrawerAnalyticsChevron);
        if (analyticsToggle != null && analyticsChildren != null) {
            analyticsToggle.setOnClickListener(v -> {
                isAnalyticsGroupExpanded = !isAnalyticsGroupExpanded;
                analyticsChildren.setVisibility(isAnalyticsGroupExpanded ? View.VISIBLE : View.GONE);
                if (analyticsChevron != null) {
                    analyticsChevron.animate().rotation(isAnalyticsGroupExpanded ? 180f : 0f).setDuration(180).start();
                }
            });
        }

        View btnLogout = findViewById(R.id.btnAdminDrawerLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeDrawer();
                showLogoutConfirmationDialog();
            });
        }
    }

    private void bindDrawerItem(int index, int includeId, int iconRes, int titleRes, int subtitleRes) {
        View itemView = findViewById(includeId);
        if (itemView == null) return;
        drawerItems[index] = itemView;

        ImageView imgIcon = itemView.findViewById(R.id.imgDrawerItemIcon);
        TextView tvTitle = itemView.findViewById(R.id.tvDrawerItemTitle);
        TextView tvSubtitle = itemView.findViewById(R.id.tvDrawerItemSubtitle);

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

        itemView.setOnClickListener(v -> {
            closeDrawer();
            onDrawerItemClicked(index);
        });
    }

    private void onDrawerItemClicked(int index) {
        switch (index) {
            case 0:
                selectTab(R.id.nav_admin_dashboard);
                openFragment(new AdminDashboardFragment());
                break;
            case 1:
                selectTab(R.id.nav_admin_users);
                openFragment(new UserManagementFragment());
                break;
            case 2:
                selectTab(R.id.nav_admin_courses);
                openFragment(new CourseManagementFragment());
                break;
            case 3:
                openFragment(new ClassManagementFragment());
                break;
            case 4:
                selectTab(R.id.nav_admin_reports);
                openFragment(new ReportsFragment());
                break;
            case 5:
                openFragment(new NotificationFragment());
                break;
            case 6:
                selectTab(R.id.nav_admin_settings);
                openFragment(new SystemSettingsFragment());
                break;
            case 7:
                showHelpDialog();
                break;
            case 8:
                showAboutDialog();
                break;
            case 9:
                openFragment(new MlModelEvaluationFragment());
                break;
            case 10:
                openFragment(UserManagementFragment.newInstance(3));
                break;
            case 11:
                openFragment(new com.social7.studentlearnanalytics.admin.classes.IncidentResolutionWorkspaceFragment());
                break;
            case 12:
                openFragment(new SystemStatisticsFragment());
                break;
        }
    }

    public void updateDrawerActiveItem(int activeIndex) {
        int activeBlue = Color.parseColor("#155DFC");
        int darkNavy = Color.parseColor("#101A70");
        int mutedBlue = Color.parseColor("#6680B5");

        for (int i = 0; i < drawerItems.length; i++) {
            View itemView = drawerItems[i];
            if (itemView == null) continue;

            TextView tvTitle = itemView.findViewById(R.id.tvDrawerItemTitle);
            TextView tvSubtitle = itemView.findViewById(R.id.tvDrawerItemSubtitle);
            ImageView imgIcon = itemView.findViewById(R.id.imgDrawerItemIcon);
            ImageView imgChevron = itemView.findViewById(R.id.imgDrawerItemChevron);

            if (i == activeIndex) {
                itemView.setBackgroundResource(R.drawable.bg_admin_drawer_item_active);
                if (tvTitle != null) tvTitle.setTextColor(activeBlue);
                if (tvSubtitle != null) tvSubtitle.setTextColor(activeBlue);
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(activeBlue));
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(activeBlue));
            } else {
                itemView.setBackgroundResource(android.R.color.transparent);
                if (tvTitle != null) tvTitle.setTextColor(darkNavy);
                if (tvSubtitle != null) tvSubtitle.setTextColor(mutedBlue);
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(mutedBlue));
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(mutedBlue));
            }
        }
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.log_out_6a80a6)
                .setMessage(R.string.are_you_sure_you_want_to_log_out_of_your_a_4f0898)
                .setPositiveButton(R.string.log_out, (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    redirectToLogin();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.help_support)
                .setMessage(R.string.for_system_assistance_or_technical_support)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAboutDialog() {
        Intent intent = new Intent(this, AdminAboutSystemActivity.class);
        startActivity(intent);
    }

    protected void onResume() {
        super.onResume();
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openFragment(@NonNull Fragment fragment) {
        boolean isTopLevel = (fragment instanceof AdminDashboardFragment ||
                fragment instanceof UserManagementFragment ||
                fragment instanceof CourseManagementFragment ||
                fragment instanceof ClassManagementFragment ||
                fragment instanceof ReportsFragment ||
                fragment instanceof SystemSettingsFragment);

        if (isTopLevel && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_fragment_container, fragment);

        if (!isTopLevel) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        syncAdminNavState(fragment);
    }

    private void syncAdminNavState(@NonNull Fragment fragment) {
        if (fragment instanceof AdminDashboardFragment) {
            updateDrawerActiveItem(0);
            selectTab(R.id.nav_admin_dashboard);
        } else if (fragment instanceof UserManagementFragment &&
                fragment.getArguments() != null &&
                fragment.getArguments().getInt("tab_index", 0) == 3) {
            updateDrawerActiveItem(10);
        } else if (fragment instanceof UserManagementFragment ||
                fragment instanceof TeacherManagementFragment) {
            updateDrawerActiveItem(1);
            selectTab(R.id.nav_admin_users);
        } else if (fragment instanceof CourseManagementFragment) {
            updateDrawerActiveItem(2);
            selectTab(R.id.nav_admin_courses);
        } else if (fragment instanceof ClassManagementFragment) {
            updateDrawerActiveItem(3);
        } else if (fragment instanceof ReportsFragment) {
            updateDrawerActiveItem(4);
            selectTab(R.id.nav_admin_reports);
        } else if (fragment instanceof NotificationFragment) {
            updateDrawerActiveItem(5);
        } else if (fragment instanceof SystemSettingsFragment) {
            updateDrawerActiveItem(6);
            selectTab(R.id.nav_admin_settings);
        } else if (fragment instanceof MlModelEvaluationFragment) {
            updateDrawerActiveItem(9);
        } else if (fragment instanceof com.social7.studentlearnanalytics.admin.classes.FacultyEvaluationReviewFragment) {
            updateDrawerActiveItem(10);
        } else if (fragment instanceof com.social7.studentlearnanalytics.admin.classes.IncidentResolutionWorkspaceFragment) {
            updateDrawerActiveItem(11);
        } else if (fragment instanceof SystemStatisticsFragment) {
            updateDrawerActiveItem(12);
        }
    }

    public void selectTab(int itemId) {
        if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != itemId) {
            isNavigating = true;
            bottomNavigation.setSelectedItemId(itemId);
            isNavigating = false;
        }
    }
}
