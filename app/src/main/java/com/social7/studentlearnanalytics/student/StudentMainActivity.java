package com.social7.studentlearnanalytics.student;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;
    private final View[] drawerItems = new View[12];
    private final int[] drawerIconIds = {
            R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon,
            R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon,
            R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon,
            R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon, R.id.imgDrawerItemIcon
    };
    private final int[] drawerTextIds = {
            R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle,
            R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle,
            R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle,
            R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle, R.id.tvDrawerItemTitle
    };
    private final int[] drawerChevronIds = {
            R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron,
            R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron,
            R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron,
            R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron, R.id.imgDrawerItemChevron
    };
    private final int[] defaultIconColorRes = {
            R.color.drawer_primary_blue, R.color.drawer_primary_blue, R.color.drawer_primary_blue,
            R.color.drawer_primary_blue, R.color.drawer_primary_blue, R.color.drawer_primary_blue,
            R.color.drawer_primary_blue, R.color.drawer_primary_blue, R.color.drawer_primary_blue,
            R.color.drawer_primary_blue, R.color.drawer_primary_blue, R.color.drawer_primary_blue
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }

        setContentView(R.layout.activity_student_main);

        drawerLayout = findViewById(R.id.student_drawer_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupDrawer();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isDrawerOpen()) {
                    closeDrawer();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != R.id.nav_home) {
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (bottomNavigation != null) {
            com.google.android.material.badge.BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notifications);
            badge.setVisible(false);
            badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.danger_red));

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    openFragment(new StudentDashboardFragment());
                } else if (itemId == R.id.nav_courses) {
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
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (current != null) {
                if (current instanceof StudentDashboardFragment) {
                    updateDrawerActiveItem(0);
                    selectBottomNav(R.id.nav_home);
                } else if (current instanceof StudentCoursesFragment) {
                    updateDrawerActiveItem(1);
                    selectBottomNav(R.id.nav_courses);
                } else if (current instanceof LearningAnalyticsFragment) {
                    updateDrawerActiveItem(2);
                    selectBottomNav(R.id.nav_analytics);
                } else if (current instanceof NotificationFragment) {
                    updateDrawerActiveItem(6);
                    selectBottomNav(R.id.nav_notifications);
                } else if (current instanceof ProfileFragment) {
                    updateDrawerActiveItem(8);
                    selectBottomNav(R.id.nav_profile);
                }
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        updateProfileDataInDrawer();
    }

    private void setupDrawer() {
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(Color.parseColor("#99000000"));
        }

        View btnClose = findViewById(R.id.btnStudentDrawerClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> closeDrawer());
        }

        updateProfileDataInDrawer();

        drawerItems[0] = findViewById(R.id.itemStudentDrawerHome);
        drawerItems[1] = findViewById(R.id.itemStudentDrawerCourses);
        drawerItems[2] = findViewById(R.id.itemStudentDrawerAnalytics);
        drawerItems[3] = findViewById(R.id.itemStudentDrawerPrediction);
        drawerItems[4] = findViewById(R.id.itemStudentDrawerRecommendations);
        drawerItems[5] = findViewById(R.id.itemStudentDrawerAssessments);
        drawerItems[6] = findViewById(R.id.itemStudentDrawerNotifications);
        drawerItems[7] = findViewById(R.id.itemStudentDrawerReports);
        drawerItems[8] = findViewById(R.id.itemStudentDrawerProfile);
        drawerItems[9] = findViewById(R.id.itemStudentDrawerSettings);
        drawerItems[10] = findViewById(R.id.itemStudentDrawerHelp);
        drawerItems[11] = findViewById(R.id.itemStudentDrawerAbout);

        for (int j = 0; j < 12; j++) {
            final int index = j;
            if (drawerItems[j] != null) {
                drawerItems[j].setOnClickListener(v -> {
                    closeDrawer();
                    onDrawerItemClicked(index);
                });
            }
        }

        View btnLogout = findViewById(R.id.itemStudentDrawerLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeDrawer();
                showLogoutConfirmationDialog();
            });
        }
    }

    private void updateProfileDataInDrawer() {
        if (sessionManager == null) return;

        TextView tvName = findViewById(R.id.tvStudentDrawerName);
        TextView tvId = findViewById(R.id.tvStudentDrawerId);
        TextView tvAcademic = findViewById(R.id.tvStudentDrawerAcademic);

        if (tvName != null) {
            String userName = sessionManager.getStudentName();
            tvName.setText(userName != null && !userName.trim().isEmpty() ? userName : "Student");
        }

        if (tvId != null) {
            String studentId = sessionManager.getUserId();
            tvId.setText(tvId.getContext().getString(R.string.student_id_a8dd9e) + (studentId != null && !studentId.trim().isEmpty() ? studentId : "N/A"));
        }

        if (tvAcademic != null) {
            tvAcademic.setText(R.string.enrolled_student);
        }

        // Fetch Real Profile
        ApiClient.getApiService(this).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data")
                                : (root.has("user") && root.get("user").isJsonObject() ? root.getAsJsonObject("user") : root);

                        if (data.has("name") && !data.get("name").isJsonNull() && tvName != null) {
                            tvName.setText(data.get("name").getAsString());
                        }

                        JsonObject studentObj = null;
                        if (data.has("student") && data.get("student").isJsonObject()) {
                            studentObj = data.getAsJsonObject("student");
                        } else if (root.has("profile") && root.get("profile").isJsonObject()) {
                            JsonObject profWrapper = root.getAsJsonObject("profile");
                            if (profWrapper.has("profile") && profWrapper.get("profile").isJsonObject()) {
                                studentObj = profWrapper.getAsJsonObject("profile");
                            }
                        }

                        if (studentObj != null) {
                            if (tvId != null) {
                                if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                                    tvId.setText(tvId.getContext().getString(R.string.student_id_a8dd9e) + studentObj.get("student_code").getAsString());
                                } else if (studentObj.has("student_id") && !studentObj.get("student_id").isJsonNull()) {
                                    tvId.setText(tvId.getContext().getString(R.string.student_id_a8dd9e) + studentObj.get("student_id").getAsString());
                                }
                            }

                            if (tvAcademic != null) {
                                String deptName = "";
                                if (studentObj.has("department") && studentObj.get("department").isJsonObject()) {
                                    JsonObject deptObj = studentObj.getAsJsonObject("department");
                                    if (deptObj.has("name") && !deptObj.get("name").isJsonNull()) {
                                        deptName = deptObj.get("name").getAsString();
                                    }
                                } else if (studentObj.has("major") && !studentObj.get("major").isJsonNull()) {
                                    deptName = studentObj.get("major").getAsString();
                                }

                                String yearStr = "";
                                if (studentObj.has("year_level") && !studentObj.get("year_level").isJsonNull()) {
                                    String rawYr = studentObj.get("year_level").getAsString();
                                    yearStr = rawYr.toLowerCase().contains("year") ? rawYr : "Year " + rawYr;
                                }

                                if (!deptName.isEmpty() && !yearStr.isEmpty()) {
                                    tvAcademic.setText(deptName + " · " + yearStr);
                                } else if (!deptName.isEmpty()) {
                                    tvAcademic.setText(deptName);
                                } else if (!yearStr.isEmpty()) {
                                    tvAcademic.setText(yearStr);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("StudentMain", "Error parsing profile data", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });

        // Fetch Real Notifications
        ApiClient.getApiService(this).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        if (body.has("unread_count") && !body.get("unread_count").isJsonNull()) {
                            updateGlobalNotificationBadge(body.get("unread_count").getAsInt());
                            return;
                        }
                        com.google.gson.JsonArray array = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            array = body.getAsJsonArray("data");
                        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
                            array = body.getAsJsonArray("notifications");
                        }

                        if (array != null) {
                            int unreadCount = 0;
                            for (int i = 0; i < array.size(); i++) {
                                JsonObject notif = array.get(i).getAsJsonObject();
                                if (notif.has("is_read") && !notif.get("is_read").isJsonNull()) {
                                    if (!notif.get("is_read").getAsBoolean()) unreadCount++;
                                } else if (notif.has("read_at") && notif.get("read_at").isJsonNull()) {
                                    unreadCount++;
                                }
                            }
                            updateGlobalNotificationBadge(unreadCount);
                        }
                    } catch (Exception e) {
                        Log.e("StudentMain", "Error parsing notifications", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    public void updateGlobalNotificationBadge(int unreadCount) {
        TextView tvNotifBadge = findViewById(R.id.tvStudentNotifBadge);
        if (tvNotifBadge == null) tvNotifBadge = findViewById(R.id.tvTeacherNotifBadge);
        if (tvNotifBadge != null) {
            if (unreadCount > 0) {
                tvNotifBadge.setVisibility(View.VISIBLE);
                tvNotifBadge.setText(String.valueOf(unreadCount));
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        }

        if (bottomNavigation != null) {
            com.google.android.material.badge.BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_notifications);
            if (unreadCount > 0) {
                badge.setVisible(true);
                badge.setNumber(unreadCount);
            } else {
                badge.setVisible(false);
            }
        }
    }

    private void onDrawerItemClicked(int index) {
        switch (index) {
            case 0:
                openFragment(new StudentDashboardFragment());
                break;
            case 1:
                openFragment(new StudentCoursesFragment());
                break;
            case 2:
                openFragment(new LearningAnalyticsFragment());
                break;
            case 3:
                openFragment(new PredictionFragment());
                break;
            case 4:
                openFragment(new StudentRecommendationFragment());
                break;
            case 5:
                openFragment(new StudentAssessmentFragment());
                break;
            case 6:
                openFragment(new NotificationFragment());
                break;
            case 7:
                openFragment(new ReportFragment());
                break;
            case 8:
                openFragment(new ProfileFragment());
                break;
            case 9:
                openFragment(new StudentSettingsFragment());
                break;
            case 10:
                showHelpAndSupportDialog();
                break;
            case 11:
                showAboutDialog();
                break;
        }
    }

    private void showHelpAndSupportDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.help_support)
                .setMessage("Need assistance with your Student Learning Analytics account?\n\n" +
                        "• Academic Office: registrar@university.edu.kh\n" +
                        "• IT Helpdesk: support@slas.edu.kh\n" +
                        "• Office Hours: Mon–Fri, 8:00 AM – 5:00 PM\n\n" +
                        "For course-specific inquiries, please contact your course instructor directly via your enrolled course page.")
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.about_slas)
                .setMessage("Student Learning Analytics System (SLAS)\nVersion 1.0.0\n\n" +
                        "SLAS empowers university students to track real-time attendance, assignments, quizzes, exams, GPA progression, AI performance predictions, and personalized study recommendations.")
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void updateDrawerActiveItem(int activeIndex) {
        int primaryBlue = androidx.core.content.ContextCompat.getColor(this, R.color.primary);
        int primaryText = androidx.core.content.ContextCompat.getColor(this, R.color.text_primary);
        int chevronMuted = androidx.core.content.ContextCompat.getColor(this, R.color.text_hint);

        for (int i = 0; i < 12; i++) {
            View itemView = drawerItems[i];
            if (itemView == null) continue;

            ImageView imgIcon = itemView.findViewById(drawerIconIds[i]);
            TextView tvTitle = itemView.findViewById(drawerTextIds[i]);
            ImageView imgChevron = itemView.findViewById(drawerChevronIds[i]);

            boolean isActive = (i == activeIndex);

            if (isActive) {
                rootItemActive(itemView, imgIcon, tvTitle, imgChevron, primaryBlue);
            } else {
                rootItemInactive(itemView, imgIcon, tvTitle, imgChevron, primaryText, chevronMuted, defaultIconColorRes[i]);
            }
        }
    }

    private void rootItemActive(View root, ImageView imgIcon, TextView tvTitle, ImageView imgChevron, int primaryBlue) {
        root.setBackgroundResource(R.drawable.bg_student_drawer_active);
        if (tvTitle != null) {
            tvTitle.setTextColor(primaryBlue);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(primaryBlue));
        if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(primaryBlue));
    }

    private void rootItemInactive(View root, ImageView imgIcon, TextView tvTitle, ImageView imgChevron, int primaryText, int chevronMuted, int iconColorRes) {
        root.setBackgroundResource(android.R.color.transparent);
        if (tvTitle != null) {
            tvTitle.setTextColor(primaryText);
            tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (imgIcon != null) {
            int defaultTint = androidx.core.content.ContextCompat.getColor(this, iconColorRes);
            imgIcon.setImageTintList(ColorStateList.valueOf(defaultTint));
        }
        if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(chevronMuted));
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

    public void selectBottomNav(int itemId) {
        if (bottomNavigation != null && bottomNavigation.getSelectedItemId() != itemId) {
            bottomNavigation.setOnItemSelectedListener(null);
            bottomNavigation.setSelectedItemId(itemId);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) openFragment(new StudentDashboardFragment());
                else if (id == R.id.nav_courses) openFragment(new StudentCoursesFragment());
                else if (id == R.id.nav_analytics) openFragment(new LearningAnalyticsFragment());
                else if (id == R.id.nav_notifications) openFragment(new NotificationFragment());
                else if (id == R.id.nav_profile) openFragment(new ProfileFragment());
                else return false;
                return true;
            });
        }
    }

    public void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.log_out_6a80a6)
                .setMessage("Are you sure you want to log out of your student account?")
                .setPositiveButton(R.string.log_out, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        try {
            ApiClient.getApiService(this).logout().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    clearSessionAndRedirect();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    clearSessionAndRedirect();
                }
            });
        } catch (Exception e) {
            clearSessionAndRedirect();
        }
    }

    private void clearSessionAndRedirect() {
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

    public void openFragment(@NonNull Fragment fragment) {
        boolean isTopLevel = (fragment instanceof StudentDashboardFragment ||
                fragment instanceof StudentCoursesFragment ||
                fragment instanceof LearningAnalyticsFragment ||
                fragment instanceof NotificationFragment ||
                fragment instanceof ProfileFragment);

        if (isTopLevel && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment);

        if (!isTopLevel) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        if (fragment instanceof StudentDashboardFragment) {
            updateDrawerActiveItem(0);
            selectBottomNav(R.id.nav_home);
        } else if (fragment instanceof StudentCoursesFragment) {
            updateDrawerActiveItem(1);
            selectBottomNav(R.id.nav_courses);
        } else if (fragment instanceof LearningAnalyticsFragment) {
            updateDrawerActiveItem(2);
            selectBottomNav(R.id.nav_analytics);
        } else if (fragment instanceof PredictionFragment) {
            updateDrawerActiveItem(3);
        } else if (fragment instanceof StudentRecommendationFragment || fragment instanceof RecommendationFragment) {
            updateDrawerActiveItem(4);
        } else if (fragment instanceof StudentAssessmentFragment) {
            updateDrawerActiveItem(5);
        } else if (fragment instanceof NotificationFragment) {
            updateDrawerActiveItem(6);
            selectBottomNav(R.id.nav_notifications);
        } else if (fragment instanceof ReportFragment) {
            updateDrawerActiveItem(7);
        } else if (fragment instanceof ProfileFragment) {
            updateDrawerActiveItem(8);
            selectBottomNav(R.id.nav_profile);
        } else if (fragment instanceof StudentSettingsFragment) {
            updateDrawerActiveItem(9);
        }
    }

    public void setupStudentHeader(View root, String title, String subtitle) {
        if (root == null) return;

        View btnMenu = root.findViewById(R.id.btnStudentMenuHeader);
        if (btnMenu == null) btnMenu = root.findViewById(R.id.btnTeacherMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> openDrawer());
        }

        TextView tvTitle = root.findViewById(R.id.tvHeaderTitle);
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }

        TextView tvSubtitle = root.findViewById(R.id.tvHeaderSubtitle);
        if (tvSubtitle != null && subtitle != null) {
            tvSubtitle.setText(subtitle);
        }

        if (com.social7.studentlearnanalytics.localization.AppLanguageManager.isKhmer(this)) {
            if (tvTitle != null) {
                tvTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f);
            }
            if (tvSubtitle != null) {
                tvSubtitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10.5f);
            }
        }

        View notifBtn = root.findViewById(R.id.layoutStudentNotifBtn);
        if (notifBtn == null) notifBtn = root.findViewById(R.id.layoutTeacherNotifBtn);
        if (notifBtn != null) {
            notifBtn.setOnClickListener(v -> {
                selectBottomNav(R.id.nav_notifications);
                openFragment(new NotificationFragment());
            });
        }

        View avatarBtn = root.findViewById(R.id.layoutStudentAvatarBtn);
        if (avatarBtn == null) avatarBtn = root.findViewById(R.id.layoutTeacherAvatarBtn);
        if (avatarBtn != null) {
            avatarBtn.setOnClickListener(v -> {
                selectBottomNav(R.id.nav_profile);
                openFragment(new ProfileFragment());
            });
        }

        TextView tvAvatarText = root.findViewById(R.id.tvStudentAvatarText);
        if (tvAvatarText != null && sessionManager != null) {
            String name = sessionManager.getStudentName();
            if (name != null && !name.trim().isEmpty()) {
                tvAvatarText.setText(String.valueOf(name.charAt(0)).toUpperCase());
            }
        }
    }
}
