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
    private int courseOfferingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));

        courseOfferingId = getIntent().getIntExtra("course_offering_id", 0);
        if (courseOfferingId > 0) {
            loadAttendance();
        } else {
            Toast.makeText(this, R.string.invalid_course, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAttendance() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleAttendanceCourse(courseOfferingId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && !body.get("data").isJsonNull()) {
                        JsonObject data = body.getAsJsonObject("data");
                        if (data.has("records") && !data.get("records").isJsonNull()) {
                            JsonArray records = data.getAsJsonArray("records");
                            CourseAttendanceRecordsAdapter adapter = new CourseAttendanceRecordsAdapter(records);
                            rvAttendance.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(CourseAttendanceActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CourseAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
