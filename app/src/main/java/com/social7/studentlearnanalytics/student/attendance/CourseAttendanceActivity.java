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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseAttendanceActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvAttendance;
    private View layoutEmptyState, btnRetryCourseAttendance;
    private TextView tvEmptyMessage;
    private int courseOfferingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_course_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.course_attendance);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvAttendance = findViewById(R.id.rvAttendance);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryCourseAttendance = findViewById(R.id.btnRetryCourseAttendance);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));

        if (btnRetryCourseAttendance != null) {
            btnRetryCourseAttendance.setOnClickListener(v -> loadAttendance());
        }

        courseOfferingId = getIntent().getIntExtra("course_offering_id", 0);
        if (courseOfferingId > 0) {
            loadAttendance();
        } else {
            Toast.makeText(this, R.string.invalid_course, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAttendance() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAttendanceCourse(courseOfferingId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");
                        JsonArray records = (data.has("records") && data.get("records").isJsonArray())
                                ? data.getAsJsonArray("records") : new JsonArray();
                        if (records.size() > 0) {
                            rvAttendance.setAdapter(new CourseAttendanceRecordsAdapter(records));
                            rvAttendance.setVisibility(View.VISIBLE);
                            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                        } else {
                            showState("No attendance sessions recorded for this course.", false);
                        }
                        return;
                    }
                }
                Toast.makeText(CourseAttendanceActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                showState("Unable to load course attendance (HTTP " + response.code() + ").", true);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(CourseAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showState("Network error loading attendance. Please retry.", true);
            }
        });
    }

    private void showState(String message, boolean showRetry) {
        if (rvAttendance != null) rvAttendance.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (btnRetryCourseAttendance != null) btnRetryCourseAttendance.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
