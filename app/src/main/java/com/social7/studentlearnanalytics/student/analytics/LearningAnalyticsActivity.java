package com.social7.studentlearnanalytics.student.analytics;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningAnalyticsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvAttendanceAverage;
    private TextView tvAssignmentAverage;
    private TextView tvQuizAverage;
    private TextView tvPerformance;
    private View cardAnalyticsStatus, btnRetryAnalytics;
    private TextView tvAnalyticsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_learning_analytics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.learning_analytics);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        tvAttendanceAverage = findViewById(R.id.tvAttendanceAverage);
        tvAssignmentAverage = findViewById(R.id.tvAssignmentAverage);
        tvQuizAverage = findViewById(R.id.tvQuizAverage);
        tvPerformance = findViewById(R.id.tvPerformance);
        cardAnalyticsStatus = findViewById(R.id.cardAnalyticsStatus);
        tvAnalyticsStatus = findViewById(R.id.tvAnalyticsStatus);
        btnRetryAnalytics = findViewById(R.id.btnRetryAnalytics);
        if (btnRetryAnalytics != null) {
            btnRetryAnalytics.setOnClickListener(v -> loadAnalytics());
        }

        loadAnalytics();
    }

    private void loadAnalytics() {
        progressBar.setVisibility(View.VISIBLE);
        if (cardAnalyticsStatus != null) cardAnalyticsStatus.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAnalytics(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");
                        JsonObject src = (data.has("current") && data.get("current").isJsonObject())
                                ? data.getAsJsonObject("current") : data;

                        String attendance = formatMetricPct(src, data, "attendance_rate");
                        String assignment = formatMetricPct(src, data, "assignment_average");
                        String quiz = formatMetricPct(src, data, "quiz_average");
                        String performance = formatMetricPct(src, data, "overall_performance");

                        tvAttendanceAverage.setText(attendance);
                        tvAssignmentAverage.setText(assignment);
                        tvQuizAverage.setText(quiz);
                        tvPerformance.setText(performance);
                        return;
                    }
                    showStatus("No analytics data returned.");
                } else {
                    String msg = "Failed to load analytics (HTTP " + response.code() + ").";
                    Toast.makeText(LearningAnalyticsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    showStatus(msg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                String msg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Connection failed");
                Toast.makeText(LearningAnalyticsActivity.this, msg, Toast.LENGTH_SHORT).show();
                showStatus("Could not reach the server. Tap Retry to try again.");
            }
        });
    }

    private void showStatus(String message) {
        if (cardAnalyticsStatus != null) cardAnalyticsStatus.setVisibility(View.VISIBLE);
        if (tvAnalyticsStatus != null) tvAnalyticsStatus.setText(message);
    }

    private String formatMetricPct(JsonObject primary, JsonObject fallback, String key) {
        JsonObject target = (primary != null && primary.has(key) && !primary.get(key).isJsonNull())
                ? primary : fallback;
        if (target != null && target.has(key) && !target.get(key).isJsonNull()) {
            try {
                return String.format(java.util.Locale.US, "%.1f%%", target.get(key).getAsDouble());
            } catch (Exception ignored) {
                return target.get(key).getAsString() + "%";
            }
        }
        return "N/A";
    }
}
