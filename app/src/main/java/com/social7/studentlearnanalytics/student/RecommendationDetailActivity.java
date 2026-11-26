package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationDetailActivity extends AppCompatActivity {

    private NestedScrollView nestedScrollViewRec;
    private TextView tvSemesterText;
    private LinearLayout layoutRecDetailDynamicContainer;
    private View layoutRecDetailStaticContainer;
    private final List<DashboardResponse.SemesterInfo> semesterList = new ArrayList<>();
    private Integer selectedSemesterId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_detail);

        nestedScrollViewRec = findViewById(R.id.nestedScrollViewRec);
        tvSemesterText = findViewById(R.id.tvSemesterText);
        layoutRecDetailDynamicContainer = findViewById(R.id.layoutRecDetailDynamicContainer);
        layoutRecDetailStaticContainer = findViewById(R.id.layoutRecDetailStaticContainer);

        resetScrollToTop();

        // Header Back Button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Semester Selector
        View semesterSelector = findViewById(R.id.semesterSelector);
        if (semesterSelector != null) {
            semesterSelector.setOnClickListener(v -> showSemesterDialog());
        }

        // 1. View Performance Details -> Learning Analytics
        View btnViewPerformanceDetails = findViewById(R.id.btnViewPerformanceDetails);
        if (btnViewPerformanceDetails != null) {
            btnViewPerformanceDetails.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }

        // 2. Card 1: View Quiz Details -> QuizDetailActivity
        View btnViewQuizDetails = findViewById(R.id.btnViewQuizDetails);
        if (btnViewQuizDetails != null) {
            btnViewQuizDetails.setOnClickListener(v -> startActivity(new Intent(this, QuizDetailActivity.class)));
        }

        // 3. Card 2: View Attendance -> AttendanceDetailActivity
        View btnViewAttendance = findViewById(R.id.btnViewAttendance);
        if (btnViewAttendance != null) {
            btnViewAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceDetailActivity.class)));
        }

        // 4. Card 3: View Analytics -> Learning Analytics
        View btnViewWeakTopics = findViewById(R.id.btnViewWeakTopics);
        if (btnViewWeakTopics != null) {
            btnViewWeakTopics.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }

        // 5. Card 4: View Schedule -> Material Dialog
        View btnViewSchedule = findViewById(R.id.btnViewSchedule);
        if (btnViewSchedule != null) {
            btnViewSchedule.setOnClickListener(v -> openTab(R.id.nav_courses));
        }

        // 6. Card 5: View Resources -> Material Dialog
        View btnViewResources = findViewById(R.id.btnViewResources);
        if (btnViewResources != null) {
            btnViewResources.setOnClickListener(v -> openTab(R.id.nav_courses));
        }

        // 7. Bottom Progress Banner -> Learning Analytics
        View btnTrackProgress = findViewById(R.id.btnTrackProgress);
        if (btnTrackProgress != null) {
            btnTrackProgress.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }

        loadDashboardAndRecommendations();
    }

    private void loadDashboardAndRecommendations() {
        ApiClient.getApiService(this).getStudentDashboardBySemester(selectedSemesterId, null)
                .enqueue(new Callback<DashboardResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                        if (isFinishing()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            DashboardResponse.DashboardData data = response.body().getData();
                            if (data != null) {
                                bindKpiCards(data);
                                if (semesterList.isEmpty() && data.getSemesters() != null) {
                                    semesterList.addAll(data.getSemesters());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                    }
                });

        if (layoutRecDetailStaticContainer != null) {
            layoutRecDetailStaticContainer.setVisibility(View.GONE);
        }

        ApiClient.getApiService(this).getRecommendations(selectedSemesterId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonArray arr = null;
                    if (root.has("data") && root.get("data").isJsonArray()) {
                        arr = root.getAsJsonArray("data");
                    } else if (root.has("recommendations") && root.get("recommendations").isJsonArray()) {
                        arr = root.getAsJsonArray("recommendations");
                    }
                    if (arr != null && arr.size() > 0) {
                        renderDynamicRecommendations(arr);
                        return;
                    }
                }
                renderEmptyRecommendationsState("No recommendations available for the selected semester.");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                renderEmptyRecommendationsState("Unable to load recommendations. Please check your connection.");
            }
        });
    }

    private void renderEmptyRecommendationsState(String message) {
        if (layoutRecDetailStaticContainer != null) {
            layoutRecDetailStaticContainer.setVisibility(View.GONE);
        }
        if (layoutRecDetailDynamicContainer == null) return;
        layoutRecDetailDynamicContainer.removeAllViews();
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText(message);
        tvEmpty.setTextColor(Color.parseColor("#64748B"));
        tvEmpty.setTextSize(13f);
        tvEmpty.setPadding(16, 24, 16, 24);
        layoutRecDetailDynamicContainer.addView(tvEmpty);
    }

    private void bindKpiCards(DashboardResponse.DashboardData data) {
        bindKpiCard(
                findViewById(R.id.tvRecDetailAttVal),
                findViewById(R.id.tvRecDetailAttBadge),
                data.getAttendanceRateFormatted()
        );
        bindKpiCard(
                findViewById(R.id.tvRecDetailAsnVal),
                findViewById(R.id.tvRecDetailAsnBadge),
                data.getAssignmentScoreFormatted()
        );
        bindKpiCard(
                findViewById(R.id.tvRecDetailQuizVal),
                findViewById(R.id.tvRecDetailQuizBadge),
                data.getQuizScoreFormatted()
        );
    }

    private void bindKpiCard(TextView tvVal, TextView tvBadge, String formatted) {
        if (tvVal != null) {
            tvVal.setText(formatted != null ? formatted : "N/A");
        }
        if (tvBadge != null) {
            try {
                double val = Double.parseDouble(formatted.replace("%", "").trim());
                if (val >= 80.0) {
                    tvBadge.setText(R.string.status_good);
                    tvBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (val >= 60.0) {
                    tvBadge.setText(R.string.improve);
                    tvBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvBadge.setText(R.string.at_risk);
                    tvBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            } catch (Exception ignored) {
                tvBadge.setText(R.string.no_data);
                tvBadge.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }

    private void renderDynamicRecommendations(JsonArray arr) {
        if (layoutRecDetailDynamicContainer == null) return;
        layoutRecDetailDynamicContainer.removeAllViews();
        if (layoutRecDetailStaticContainer != null) {
            layoutRecDetailStaticContainer.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject ro = el.getAsJsonObject();

            final String recId = ro.has("id") && !ro.get("id").isJsonNull() ? ro.get("id").getAsString() : "";
            String courseCode = "General";
            String courseId = ro.has("course_id") && !ro.get("course_id").isJsonNull() ? ro.get("course_id").getAsString() : "";
            if (ro.has("course") && ro.get("course").isJsonObject()) {
                JsonObject co = ro.getAsJsonObject("course");
                if (co.has("course_code") && !co.get("course_code").isJsonNull()) {
                    courseCode = co.get("course_code").getAsString();
                } else if (co.has("code") && !co.get("code").isJsonNull()) {
                    courseCode = co.get("code").getAsString();
                }
            }

            String title = ro.has("title") && !ro.get("title").isJsonNull()
                    ? ro.get("title").getAsString()
                    : (ro.has("recommendation_type") && !ro.get("recommendation_type").isJsonNull()
                    ? ro.get("recommendation_type").getAsString().toUpperCase() + " Recommendation"
                    : "Study Recommendation");
            String desc = ro.has("recommendation_text") && !ro.get("recommendation_text").isJsonNull()
                    ? ro.get("recommendation_text").getAsString()
                    : (ro.has("description") && !ro.get("description").isJsonNull()
                    ? ro.get("description").getAsString() : "");
            String priority = ro.has("priority") && !ro.get("priority").isJsonNull()
                    ? ro.get("priority").getAsString() : "Medium";
            boolean isRead = ro.has("is_read") && !ro.get("is_read").isJsonNull() && ro.get("is_read").getAsBoolean();

            View card = inflater.inflate(R.layout.item_student_recommendation_card, layoutRecDetailDynamicContainer, false);
            TextView tvCourseCode = card.findViewById(R.id.tvCourseCode);
            TextView tvTitle = card.findViewById(R.id.tvTitle);
            TextView tvDescription = card.findViewById(R.id.tvDescription);
            TextView tvPriorityBadge = card.findViewById(R.id.tvPriorityBadge);
            TextView tvStatusBadge = card.findViewById(R.id.tvStatusBadge);
            MaterialButton btnReviewTopic = card.findViewById(R.id.btnReviewTopic);
            MaterialButton btnMarkCompleted = card.findViewById(R.id.btnMarkCompleted);

            if (tvCourseCode != null) tvCourseCode.setText(courseCode);
            if (tvTitle != null) tvTitle.setText(title);
            if (tvDescription != null) tvDescription.setText(desc);
            if (tvPriorityBadge != null) tvPriorityBadge.setText(priority.toUpperCase() + " PRIORITY");
            if (tvStatusBadge != null) tvStatusBadge.setText(isRead ? "Completed" : "Active");

            final String fCourseCode = courseCode;
            final String fCourseId = courseId;
            if (btnReviewTopic != null) {
                btnReviewTopic.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CourseDetailActivity.class);
                    if (!fCourseId.isEmpty()) intent.putExtra("course_id", fCourseId);
                    intent.putExtra("course_code", fCourseCode);
                    intent.putExtra("course", fCourseCode);
                    startActivity(intent);
                });
            }
            if (btnMarkCompleted != null) {
                btnMarkCompleted.setOnClickListener(v -> {
                    if (recId.isEmpty()) return;
                    JsonObject body = new JsonObject();
                    body.addProperty("is_read", true);
                    body.addProperty("status", "completed");
                    ApiClient.getApiService(this).updateRecommendation(recId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                if (tvStatusBadge != null) tvStatusBadge.setText(R.string.status_completed);
                                Toast.makeText(RecommendationDetailActivity.this, "Recommendation marked as completed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RecommendationDetailActivity.this, "Could not update recommendation", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(RecommendationDetailActivity.this, "Network error updating recommendation", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }

            layoutRecDetailDynamicContainer.addView(card);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetScrollToTop();
    }

    private void resetScrollToTop() {
        if (nestedScrollViewRec != null) {
            nestedScrollViewRec.scrollTo(0, 0);
            nestedScrollViewRec.post(() -> nestedScrollViewRec.scrollTo(0, 0));
        }
    }

    private void showSemesterDialog() {
        List<String> labels = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        labels.add("All Semesters");
        ids.add(null);
        for (DashboardResponse.SemesterInfo sem : semesterList) {
            if (sem != null && sem.name != null) {
                labels.add(sem.name);
                Integer idVal = null;
                try {
                    idVal = (int) Double.parseDouble(String.valueOf(sem.id));
                } catch (Exception ignored) {}
                ids.add(idVal);
            }
        }
        String[] items = labels.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_semester)
                .setItems(items, (dialog, which) -> {
                    selectedSemesterId = ids.get(which);
                    if (tvSemesterText != null) {
                        tvSemesterText.setText(items[which] + " ▾");
                    }
                    loadDashboardAndRecommendations();
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
