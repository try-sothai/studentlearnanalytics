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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        loadAnalytics();
    }

    private void loadAnalytics() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAnalytics(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && !body.get("data").isJsonNull()) {
                        JsonObject data = body.getAsJsonObject("data");

                        String attendance = data.has("attendance_rate") && !data.get("attendance_rate").isJsonNull() ? data.get("attendance_rate").getAsString() + "%" : "N/A";
                        String assignment = data.has("assignment_average") && !data.get("assignment_average").isJsonNull() ? data.get("assignment_average").getAsString() + "%" : "N/A";
                        String quiz = data.has("quiz_average") && !data.get("quiz_average").isJsonNull() ? data.get("quiz_average").getAsString() + "%" : "N/A";
                        String performance = data.has("overall_performance") && !data.get("overall_performance").isJsonNull() ? data.get("overall_performance").getAsString() + "%" : "N/A";

                        tvAttendanceAverage.setText(attendance);
                        tvAssignmentAverage.setText(assignment);
                        tvQuizAverage.setText(quiz);
                        tvPerformance.setText(performance);
                    }
                } else {
                    Toast.makeText(LearningAnalyticsActivity.this, "Failed to load analytics.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LearningAnalyticsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
