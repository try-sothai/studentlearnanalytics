package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationMenuActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvUserName, tvStudentId, tvMajor, tvYear, tvNotifBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        sessionManager = new SessionManager(this);

        tvUserName = findViewById(R.id.tvUserName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvMajor = findViewById(R.id.tvMajor);
        tvYear = findViewById(R.id.tvYear);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);

        // Pre-fill with session data
        if (tvUserName != null && sessionManager.getStudentName() != null) {
            tvUserName.setText(sessionManager.getStudentName());
        }
        if (tvStudentId != null && sessionManager.getUserId() != null) {
            tvStudentId.setText(sessionManager.getUserId());
        }

        // Close button
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // 1. Home
        findViewById(R.id.menu_home).setOnClickListener(v -> openTab(R.id.nav_home));
        
        // 2. Courses
        View menuCourses = findViewById(R.id.menu_courses);
        if (menuCourses != null) {
            menuCourses.setOnClickListener(v -> openTab(R.id.nav_courses));
        }

        // 3. Learning Analytics
        findViewById(R.id.menu_analytics).setOnClickListener(v -> openTab(R.id.nav_analytics));

        // 4. Prediction & Risk
        findViewById(R.id.menu_prediction).setOnClickListener(v -> {
            Intent intent = new Intent(this, RiskAnalysisActivity.class);
            startActivity(intent);
            finish();
        });

        // 5. Recommendations
        findViewById(R.id.menu_recommendation).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendationDetailActivity.class);
            startActivity(intent);
            finish();
        });

        // 5.5. Assessments
        View menuAssessments = findViewById(R.id.menu_assessments);
        if (menuAssessments != null) {
            menuAssessments.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 6. Notifications
        findViewById(R.id.menu_notifications).setOnClickListener(v -> openTab(R.id.nav_notifications));

        // 7. Reports
        findViewById(R.id.menu_reports).setOnClickListener(v -> {
            Intent intent = new Intent(this, LearningReportActivity.class);
            startActivity(intent);
            finish();
        });

        // 8. Profile
        findViewById(R.id.menu_profile).setOnClickListener(v -> openTab(R.id.nav_profile));

        // 9. Settings
        findViewById(R.id.menu_settings).setOnClickListener(v -> {
            // Re-route Settings to ProfileFragment as there is no Student Settings Activity
            openTab(R.id.nav_profile);
        });

        // 10. Logout
        findViewById(R.id.menu_logout).setOnClickListener(v -> showLogoutConfirmationDialog());
        
        fetchProfile();
        fetchNotifications();
    }

    private void fetchProfile() {
        ApiClient.getApiService(this).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        if (data.has("name") && !data.get("name").isJsonNull() && tvUserName != null) {
                            tvUserName.setText(data.get("name").getAsString());
                        }

                        if (data.has("student") && data.get("student").isJsonObject()) {
                            JsonObject studentObj = data.getAsJsonObject("student");
                            
                            if (tvStudentId != null) {
                                if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                                    tvStudentId.setText(studentObj.get("student_code").getAsString());
                                } else if (studentObj.has("student_id") && !studentObj.get("student_id").isJsonNull()) {
                                    tvStudentId.setText(studentObj.get("student_id").getAsString());
                                }
                            }
                            
                            if (tvYear != null && studentObj.has("year_level") && !studentObj.get("year_level").isJsonNull()) {
                                String year = studentObj.get("year_level").getAsString();
                                if (!year.toLowerCase().contains("year")) year = year + " Year";
                                tvYear.setText(year);
                            }
                            
                            if (tvMajor != null && studentObj.has("department") && studentObj.get("department").isJsonObject()) {
                                JsonObject dept = studentObj.getAsJsonObject("department");
                                if (dept.has("name") && !dept.get("name").isJsonNull()) {
                                    tvMajor.setText(dept.get("name").getAsString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("NavigationMenu", "Error parsing profile data", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d("NavigationMenu", "Profile API call failed: " + t.getMessage());
            }
        });
    }

    private void fetchNotifications() {
        ApiClient.getApiService(this).getNotifications().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        JsonArray array = null;
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            array = body.getAsJsonArray("data");
                        } else if (body.has("notifications") && body.get("notifications").isJsonArray()) {
                            array = body.getAsJsonArray("notifications");
                        }

                        if (array != null) {
                            int unreadCount = 0;
                            for (int i = 0; i < array.size(); i++) {
                                JsonObject notif = array.get(i).getAsJsonObject();
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
                        Log.e("NavigationMenu", "Error parsing notifications", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d("NavigationMenu", "Notifications API call failed: " + t.getMessage());
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage(R.string.are_you_sure_you_want_to_logout)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
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
