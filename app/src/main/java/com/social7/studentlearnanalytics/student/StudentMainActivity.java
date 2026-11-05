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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.utils.RoleGuard;

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
            badge.setVisible(true);
            badge.setNumber(3);
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
            tvName.setText(userName != null && !userName.trim().isEmpty() ? userName : "Sophea");
        }

        if (tvId != null) {
            String studentId = sessionManager.getUserId();
            tvId.setText("Student ID: " + (studentId != null && !studentId.trim().isEmpty() ? studentId : "ST2024001"));
        }

        if (tvAcademic != null) {
            tvAcademic.setText("Year 2 · Semester 2");
        }

        // Fetch Real Profile
        com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).getProfile().enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonObject root = response.body();
                        com.google.gson.JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        if (data.has("name") && !data.get("name").isJsonNull() && tvName != null) {
                            tvName.setText(data.get("name").getAsString());
                        }

                        if (data.has("student") && data.get("student").isJsonObject()) {
                            com.google.gson.JsonObject studentObj = data.getAsJsonObject("student");
                            
                            if (tvId != null) {
                                if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                                    tvId.setText(studentObj.get("student_code").getAsString());
                                } else if (studentObj.has("student_id") && !studentObj.get("student_id").isJsonNull()) {
                                    tvId.setText(studentObj.get("student_id").getAsString());
                                }
                            }
                            
                            if (tvAcademic != null && studentObj.has("year_level") && !studentObj.get("year_level").isJsonNull()) {
                                String year = studentObj.get("year_level").getAsString();
                                if (!year.toLowerCase().contains("year")) year = year + " Year";
                                tvAcademic.setText(year);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("StudentMain", "Error parsing profile data", e);
                    }
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
            }
        });

        // Fetch Real Notifications
        com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).getNotifications().enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonObject body = response.body();
                        com.google.gson.JsonArray array = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            array = body.getAsJsonArray("data");
                        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
                            array = body.getAsJsonArray("notifications");
                        }

                        if (array != null) {
                            int unreadCount = 0;
                            for (int i = 0; i < array.size(); i++) {
                                com.google.gson.JsonObject notif = array.get(i).getAsJsonObject();
                                if (notif.has("read_at") && notif.get("read_at").isJsonNull()) {
                                    unreadCount++;
                                } else if (notif.has("is_read") && !notif.get("is_read").getAsBoolean()) {
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
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
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
                // Help Support handling
                break;
            case 11:
                // About handling
                break;
        }
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
            // Unregister listener to prevent infinite loop during programmatic selection
            bottomNavigation.setOnItemSelectedListener(null);
            bottomNavigation.setSelectedItemId(itemId);
            
            // Re-register listener
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
                .setTitle("Log Out?")
                .setMessage("Are you sure you want to log out of your student account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.clearSession();
                    }
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment);

        if (!(fragment instanceof StudentDashboardFragment || 
              fragment instanceof StudentCoursesFragment || 
              fragment instanceof LearningAnalyticsFragment || 
              fragment instanceof NotificationFragment || 
              fragment instanceof ProfileFragment)) {
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
