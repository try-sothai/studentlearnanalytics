package com.social7.studentlearnanalytics.student.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceOverviewActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvAttendanceRate;
    private RecyclerView rvCourses;
    private View layoutEmptyState, btnRetryAttendance;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_attendance_overview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_attendance);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        tvAttendanceRate = findViewById(R.id.tvAttendanceRate);
        rvCourses = findViewById(R.id.rvCourses);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryAttendance = findViewById(R.id.btnRetryAttendance);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        if (btnRetryAttendance != null) {
            btnRetryAttendance.setOnClickListener(v -> loadAttendanceOverview());
        }

        loadAttendanceOverview();
    }

    private void loadAttendanceOverview() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAttendanceOverview(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");
                        if (data.has("summary") && data.get("summary").isJsonObject()) {
                            JsonObject summary = data.getAsJsonObject("summary");
                            int total = summary.has("total") && !summary.get("total").isJsonNull()
                                    ? summary.get("total").getAsInt() : 0;
                            String rate = "N/A";
                            if (total > 0 && summary.has("percentage") && !summary.get("percentage").isJsonNull()) {
                                rate = summary.get("percentage").getAsString() + "%";
                            }
                            if (tvAttendanceRate != null) tvAttendanceRate.setText(rate);
                        }
                        com.google.gson.JsonArray courses = (data.has("courses") && data.get("courses").isJsonArray())
                                ? data.getAsJsonArray("courses") : new com.google.gson.JsonArray();
                        if (courses.size() > 0) {
                            rvCourses.setAdapter(new CourseAttendanceAdapter(courses));
                            rvCourses.setVisibility(View.VISIBLE);
                            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                        } else {
                            showState("No attendance records recorded yet.", false);
                        }
                        return;
                    }
                }
                Toast.makeText(AttendanceOverviewActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                showState("Unable to load attendance overview (HTTP " + response.code() + ").", true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(AttendanceOverviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showState("Network error loading attendance. Please retry.", true);
            }
        });
    }

    private void showState(String message, boolean showRetry) {
        if (rvCourses != null) rvCourses.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (btnRetryAttendance != null) btnRetryAttendance.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
