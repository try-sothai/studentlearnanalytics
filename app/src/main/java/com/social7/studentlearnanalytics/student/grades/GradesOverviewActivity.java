package com.social7.studentlearnanalytics.student.grades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.student.academic.GpaProgressActivity;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradesOverviewActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvGrades;
    private View cardSummary, btnViewAllGpaHistory;
    private TextView tvCurrentPeriodTitle, tvCurrentPeriodSub;
    private TextView tvSummaryPassed, tvSummaryPending, tvSummaryFailed, tvSemesterAverage, tvSummaryGpa;
    private View layoutEmptyState, btnRetryGrades;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private int currentStudyYear = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_grades_overview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Current Semester GPA");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvGrades = findViewById(R.id.rvGrades);
        cardSummary = findViewById(R.id.cardSummary);
        tvCurrentPeriodTitle = findViewById(R.id.tvCurrentPeriodTitle);
        tvCurrentPeriodSub = findViewById(R.id.tvCurrentPeriodSub);
        btnViewAllGpaHistory = findViewById(R.id.btnViewAllGpaHistory);
        tvSummaryPassed = findViewById(R.id.tvSummaryPassed);
        tvSummaryPending = findViewById(R.id.tvSummaryPending);
        tvSummaryFailed = findViewById(R.id.tvSummaryFailed);
        tvSemesterAverage = findViewById(R.id.tvSemesterAverage);
        tvSummaryGpa = findViewById(R.id.tvSummaryGpa);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryGrades = findViewById(R.id.btnRetryGrades);

        rvGrades.setLayoutManager(new LinearLayoutManager(this));
        if (btnRetryGrades != null) {
            btnRetryGrades.setOnClickListener(v -> loadGradesOverview());
        }
        if (btnViewAllGpaHistory != null) {
            btnViewAllGpaHistory.setOnClickListener(v -> {
                Intent intent = new Intent(GradesOverviewActivity.this, GpaProgressActivity.class);
                intent.putExtra("selected_year", currentStudyYear);
                startActivity(intent);
            });
        }

        loadGradesOverview();
    }

    private void loadGradesOverview() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleGradesOverview(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");
                        bindSummary(data);
                        JsonArray courses = (data.has("courses") && data.get("courses").isJsonArray())
                                ? data.getAsJsonArray("courses") : new JsonArray();
                        if (courses.size() > 0) {
                            rvGrades.setAdapter(new GradesAdapter(courses));
                            rvGrades.setVisibility(View.VISIBLE);
                            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                        } else {
                            showState("No Academic Results Yet",
                                    "You have no enrolled courses or released grades for this current semester.",
                                    false);
                        }
                        return;
                    }
                }
                String msg = "Failed to load academic results (HTTP " + response.code() + ").";
                Toast.makeText(GradesOverviewActivity.this, msg, Toast.LENGTH_SHORT).show();
                showState("Unable to Load Results", msg, true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                String msg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Connection failed");
                Toast.makeText(GradesOverviewActivity.this, msg, Toast.LENGTH_SHORT).show();
                showState("Connection Error", "Could not reach the server. Check your connection and retry.", true);
            }
        });
    }

    private void bindSummary(JsonObject data) {
        if (cardSummary != null) cardSummary.setVisibility(View.VISIBLE);

        if (data.has("current_period") && data.get("current_period").isJsonObject()) {
            JsonObject cp = data.getAsJsonObject("current_period");
            if (cp.has("study_year") && !cp.get("study_year").isJsonNull()) {
                currentStudyYear = cp.get("study_year").getAsInt();
            }
            String ay = cp.has("academic_year") && !cp.get("academic_year").isJsonNull()
                    ? cp.get("academic_year").getAsString() : "";
            String ylName = cp.has("year_level_name") && !cp.get("year_level_name").isJsonNull()
                    ? cp.get("year_level_name").getAsString() : ("Year " + currentStudyYear);
            String semName = cp.has("semester_name") && !cp.get("semester_name").isJsonNull()
                    ? cp.get("semester_name").getAsString()
                    : (cp.has("semester") && !cp.get("semester").isJsonNull() ? cp.get("semester").getAsString() : "Semester 1");

            if (tvCurrentPeriodTitle != null) {
                tvCurrentPeriodTitle.setText(ylName + " — " + semName);
            }
            if (tvCurrentPeriodSub != null) {
                tvCurrentPeriodSub.setText(!ay.isEmpty() ? ("Academic Year: " + ay) : "Current Academic Semester");
            }
        }

        String avgDisplay = "N/A";
        String gpaDisplay = "N/A";

        if (data.has("summary") && data.get("summary").isJsonObject()) {
            JsonObject s = data.getAsJsonObject("summary");
            int passedCount = s.has("passed") && !s.get("passed").isJsonNull() ? s.get("passed").getAsInt() : 0;
            int pendingCount = s.has("pending") && !s.get("pending").isJsonNull() ? s.get("pending").getAsInt() : 0;
            int failedCount = s.has("failed") && !s.get("failed").isJsonNull() ? s.get("failed").getAsInt() : 0;

            if (tvSummaryPassed != null) {
                tvSummaryPassed.setText(String.valueOf(passedCount));
                tvSummaryPassed.setTextColor(ContextCompat.getColor(this,
                        passedCount > 0 ? R.color.slas_success : R.color.slas_text_secondary));
            }
            if (tvSummaryPending != null) {
                tvSummaryPending.setText(String.valueOf(pendingCount));
                tvSummaryPending.setTextColor(ContextCompat.getColor(this,
                        pendingCount > 0 ? R.color.slas_warning : R.color.slas_text_secondary));
            }
            if (tvSummaryFailed != null) {
                tvSummaryFailed.setText(String.valueOf(failedCount));
                tvSummaryFailed.setTextColor(ContextCompat.getColor(this,
                        failedCount > 0 ? R.color.slas_danger : R.color.slas_text_secondary));
            }
            if (s.has("semester_average_display") && !s.get("semester_average_display").isJsonNull()) {
                avgDisplay = s.get("semester_average_display").getAsString();
            } else if (s.has("semester_average") && !s.get("semester_average").isJsonNull()) {
                avgDisplay = String.format(java.util.Locale.US, "%.2f", s.get("semester_average").getAsDouble());
            }
            if (s.has("semester_gpa_display") && !s.get("semester_gpa_display").isJsonNull()) {
                gpaDisplay = s.get("semester_gpa_display").getAsString();
            } else if (s.has("semester_gpa") && !s.get("semester_gpa").isJsonNull()) {
                gpaDisplay = String.format(java.util.Locale.US, "%.2f", s.get("semester_gpa").getAsDouble());
            }
        }

        if ("N/A".equals(gpaDisplay) && data.has("gpa_info") && data.get("gpa_info").isJsonObject()) {
            JsonObject g = data.getAsJsonObject("gpa_info");
            if (g.has("current_semester_gpa") && !g.get("current_semester_gpa").isJsonNull()) {
                gpaDisplay = g.get("current_semester_gpa").getAsString();
            } else if (g.has("semester_gpa") && !g.get("semester_gpa").isJsonNull()) {
                gpaDisplay = g.get("semester_gpa").getAsString();
            }
            if ("N/A".equals(avgDisplay) && g.has("semester_average") && !g.get("semester_average").isJsonNull()) {
                avgDisplay = g.get("semester_average").getAsString();
            }
        }

        if (tvSemesterAverage != null) {
            tvSemesterAverage.setText(avgDisplay);
            tvSemesterAverage.setTextColor(ContextCompat.getColor(this,
                    "N/A".equalsIgnoreCase(avgDisplay) ? R.color.slas_text_secondary : R.color.slas_text_primary));
        }
        if (tvSummaryGpa != null) {
            tvSummaryGpa.setText(gpaDisplay);
            tvSummaryGpa.setTextColor(ContextCompat.getColor(this,
                    "N/A".equalsIgnoreCase(gpaDisplay) ? R.color.slas_text_secondary : R.color.slas_primary_purple));
        }
    }

    private void showState(String title, String message, boolean showRetry) {
        if (rvGrades != null) rvGrades.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyTitle != null) tvEmptyTitle.setText(title);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (btnRetryGrades != null) btnRetryGrades.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
