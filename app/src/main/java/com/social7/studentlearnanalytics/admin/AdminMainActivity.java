package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class AdminMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;
    private final View[] drawerItems = new View[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Log.d("SLAS_AUTH", "SLAS: Invalid session in AdminMainActivity onCreate. Redirecting to LoginActivity.");
            redirectToLogin();
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

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
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
    }

    private void setupDrawer() {
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(Color.parseColor("#99000000"));
        }

        View closeBtn = findViewById(R.id.btnAdminDrawerClose);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> closeDrawer());
        }

        TextView tvName = findViewById(R.id.tvAdminDrawerName);
        if (sessionManager != null && tvName != null) {
            String userName = sessionManager.getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                tvName.setText(userName);
            }
        }

        bindDrawerItem(0, R.id.itemDrawerDashboard, R.drawable.ic_home, "Dashboard", null);
        bindDrawerItem(1, R.id.itemDrawerUserManagement, R.drawable.ic_users, "User Management", "Students, Teachers, Admins");
        bindDrawerItem(2, R.id.itemDrawerCourseManagement, R.drawable.ic_open_book, "Course Management", "Manage courses and curriculum");
        bindDrawerItem(3, R.id.itemDrawerReports, R.drawable.ic_chart_bar, "Reports & Analytics", "View system reports and statistics");
        bindDrawerItem(4, R.id.itemDrawerNotifications, R.drawable.ic_notifications, "Notifications", "View and manage notifications");
        bindDrawerItem(5, R.id.itemDrawerSettings, R.drawable.ic_nav_settings, "Settings", "Account and system settings");

        bindDrawerItem(6, R.id.itemDrawerHelp, R.drawable.ic_circle_help, "Help & Support", "Get help and contact support");
        bindDrawerItem(7, R.id.itemDrawerAbout, R.drawable.ic_circle_info, "About", "App version and system information");

        View btnLogout = findViewById(R.id.btnAdminDrawerLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeDrawer();
                showLogoutConfirmationDialog();
            });
        }
    }

    private void bindDrawerItem(int index, int includeId, int iconRes, String title, String subtitle) {
        View itemView = findViewById(includeId);
        if (itemView == null) return;
        drawerItems[index] = itemView;

        ImageView imgIcon = itemView.findViewById(R.id.imgDrawerItemIcon);
        TextView tvTitle = itemView.findViewById(R.id.tvDrawerItemTitle);
        TextView tvSubtitle = itemView.findViewById(R.id.tvDrawerItemSubtitle);

        if (imgIcon != null) imgIcon.setImageResource(iconRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                tvSubtitle.setText(subtitle);
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
                selectTab(R.id.nav_admin_reports);
                openFragment(new ReportsFragment());
                break;
            case 4:
                openFragment(new NotificationFragment());
                break;
            case 5:
                selectTab(R.id.nav_admin_settings);
                openFragment(new SystemSettingsFragment());
                break;
            case 6:
                showHelpDialog();
                break;
            case 7:
                showAboutDialog();
                break;
        }
    }

    public void updateDrawerActiveItem(int activeIndex) {
        int activeBlue = Color.parseColor("#155DFC");
        int darkNavy = Color.parseColor("#101A70");
        int mutedBlue = Color.parseColor("#6680B5");

        for (int i = 0; i < 6; i++) {
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
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(activeBlue));
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(activeBlue));
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
                .setTitle("Log Out?")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    redirectToLogin();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Help & Support")
                .setMessage("For system assistance or technical support, please contact support@slas.edu or reach out to your system administrator.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("About SLAS Admin")
                .setMessage("Student Learning Analytics System (SLAS)\nVersion 1.0.0\n\nBuilt for data-driven university education management and predictive analytics.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && !sessionManager.isLoggedIn()) {
            Log.d("SLAS_AUTH", "SLAS: Invalid session in AdminMainActivity onResume. Redirecting to LoginActivity.");
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_fragment_container, fragment)
                .commit();

        if (fragment instanceof AdminDashboardFragment) {
            updateDrawerActiveItem(0);
        } else if (fragment instanceof UserManagementFragment) {
            updateDrawerActiveItem(1);
        } else if (fragment instanceof CourseManagementFragment) {
            updateDrawerActiveItem(2);
        } else if (fragment instanceof ReportsFragment) {
            updateDrawerActiveItem(3);
        } else if (fragment instanceof NotificationFragment) {
            updateDrawerActiveItem(4);
        } else if (fragment instanceof SystemSettingsFragment) {
            updateDrawerActiveItem(5);
        }
    }

    public void selectTab(int itemId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(itemId);
        }
    }
}
