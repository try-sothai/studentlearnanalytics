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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        loadAttendanceOverview();
    }

    private void loadAttendanceOverview() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAttendanceOverview(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && !body.get("data").isJsonNull()) {
                        JsonObject data = body.getAsJsonObject("data");
                        if (data.has("summary")) {
                            JsonObject summary = data.getAsJsonObject("summary");
                            String rate = summary.get("percentage").getAsString() + "%";
                            tvAttendanceRate.setText(rate);
                        }
                        if (data.has("courses") && !data.get("courses").isJsonNull()) {
                            com.google.gson.JsonArray courses = data.getAsJsonArray("courses");
                            CourseAttendanceAdapter adapter = new CourseAttendanceAdapter(courses);
                            rvCourses.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(AttendanceOverviewActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AttendanceOverviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
