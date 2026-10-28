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
import com.social7.studentlearnanalytics.student.academic.GpaProgressActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;
import com.social7.studentlearnanalytics.utils.RoleGuard;

public class StudentMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigation;
    private SessionManager sessionManager;
    private final View[] drawerItems = new View[9];
    private final int[] drawerIconIds = {
            R.id.iconStudentDrawerHome, R.id.iconStudentDrawerCourses, R.id.iconStudentDrawerAnalytics,
            R.id.iconStudentDrawerPrediction, R.id.iconStudentDrawerRecommendations,
            R.id.iconStudentDrawerNotifications, R.id.iconStudentDrawerReports, R.id.iconStudentDrawerProfile,
            R.id.iconStudentDrawerSettings
    };
    private final int[] drawerTextIds = {
            R.id.tvStudentDrawerHome, R.id.tvStudentDrawerCourses, R.id.tvStudentDrawerAnalytics,
            R.id.tvStudentDrawerPrediction, R.id.tvStudentDrawerRecommendations,
            R.id.tvStudentDrawerNotifications, R.id.tvStudentDrawerReports, R.id.tvStudentDrawerProfile,
            R.id.tvStudentDrawerSettings
    };
    private final int[] drawerChevronIds = {
            R.id.chevronStudentDrawerHome, R.id.chevronStudentDrawerCourses, R.id.chevronStudentDrawerAnalytics,
            R.id.chevronStudentDrawerPrediction, R.id.chevronStudentDrawerRecommendations,
            R.id.chevronStudentDrawerNotifications, R.id.chevronStudentDrawerReports, R.id.chevronStudentDrawerProfile,
            R.id.chevronStudentDrawerSettings
    };
    private final int[] defaultIconTints = {
            Color.parseColor("#246BFD"), Color.parseColor("#246BFD"), Color.parseColor("#16B978"),
            Color.parseColor("#8B5CF6"), Color.parseColor("#F97316"),
            Color.parseColor("#246BFD"), Color.parseColor("#64748B"), Color.parseColor("#14B8A6"),
            Color.parseColor("#F59E0B")
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
        drawerItems[5] = findViewById(R.id.itemStudentDrawerNotifications);
        drawerItems[6] = findViewById(R.id.itemStudentDrawerReports);
        drawerItems[7] = findViewById(R.id.itemStudentDrawerProfile);
        drawerItems[8] = findViewById(R.id.itemStudentDrawerSettings);

        for (int i = 0; i < 9; i++) {
            final int index = i;
            if (drawerItems[i] != null) {
                drawerItems[i].setOnClickListener(v -> {
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
                        android.util.Log.e("StudentMain", "Error parsing profile data", e);
                    }
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
            }
        });

        TextView tvNotifBadge = findViewById(R.id.tvStudentDrawerNotifBadge);
        
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
                            
                            if (tvNotifBadge != null) {
                                if (unreadCount > 0) {
                                    tvNotifBadge.setVisibility(View.VISIBLE);
                                    tvNotifBadge.setText(String.valueOf(unreadCount));
                                } else {
                                    tvNotifBadge.setVisibility(View.GONE);
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("StudentMain", "Error parsing notifications", e);
                    }
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
            }
        });
    }

    private void onDrawerItemClicked(int index) {
        switch (index) {
            case 0:
                selectBottomNav(R.id.nav_home);
                openFragment(new StudentDashboardFragment());
                break;
            case 1:
                selectBottomNav(R.id.nav_courses);
                openFragment(new StudentCoursesFragment());
                break;
            case 2:
                selectBottomNav(R.id.nav_analytics);
                openFragment(new LearningAnalyticsFragment());
                break;
            case 3:
                startActivity(new Intent(this, RiskAnalysisActivity.class));
                break;
            case 4:
                openFragment(new RecommendationFragment());
                break;
            case 5:
                selectBottomNav(R.id.nav_notifications);
                openFragment(new NotificationFragment());
                break;
            case 6:
                startActivity(new Intent(this, LearningReportActivity.class));
                break;
            case 7:
                selectBottomNav(R.id.nav_profile);
                openFragment(new ProfileFragment());
                break;
            case 8:
                // Settings routes to ProfileFragment for students
                selectBottomNav(R.id.nav_profile);
                openFragment(new ProfileFragment());
                break;
        }
    }

    public void updateDrawerActiveItem(int activeIndex) {
        int primaryBlue = Color.parseColor("#246BFD");
        int primaryText = Color.parseColor("#12234A");
        int chevronMuted = Color.parseColor("#8494AD");

        for (int i = 0; i < 9; i++) {
            View itemView = drawerItems[i];
            if (itemView == null) continue;

            ImageView imgIcon = itemView.findViewById(drawerIconIds[i]);
            TextView tvTitle = itemView.findViewById(drawerTextIds[i]);
            ImageView imgChevron = itemView.findViewById(drawerChevronIds[i]);

            if (i == activeIndex) {
                itemView.setBackgroundResource(R.drawable.bg_student_drawer_active);
                if (tvTitle != null) {
                    tvTitle.setTextColor(primaryBlue);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(primaryBlue));
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(primaryBlue));
            } else {
                itemView.setBackgroundResource(android.R.color.transparent);
                if (tvTitle != null) {
                    tvTitle.setTextColor(primaryText);
                    tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                if (imgIcon != null) imgIcon.setImageTintList(ColorStateList.valueOf(defaultIconTints[i]));
                if (imgChevron != null) imgChevron.setImageTintList(ColorStateList.valueOf(chevronMuted));
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

    public void selectBottomNav(int itemId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(itemId);
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
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();

        if (fragment instanceof StudentDashboardFragment) {
            updateDrawerActiveItem(0);
        } else if (fragment instanceof StudentCoursesFragment) {
            updateDrawerActiveItem(1);
        } else if (fragment instanceof LearningAnalyticsFragment) {
            updateDrawerActiveItem(2);
        } else if (fragment instanceof PredictionFragment) {
            updateDrawerActiveItem(3);
        } else if (fragment instanceof RecommendationFragment) {
            updateDrawerActiveItem(4);
        } else if (fragment instanceof NotificationFragment) {
            updateDrawerActiveItem(5);
        } else if (fragment instanceof ProfileFragment) {
            updateDrawerActiveItem(7); // Can also be 8 (Settings)
        }
    }
}
