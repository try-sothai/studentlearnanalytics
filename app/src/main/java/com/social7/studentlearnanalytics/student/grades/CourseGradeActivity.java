package com.social7.studentlearnanalytics.student.grades;

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

public class CourseGradeActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvBreakdown;
    private TextView tvFinalScore;
    private TextView tvLetterGrade;
    private int courseOfferingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_grade);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.course_grade_details);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvBreakdown = findViewById(R.id.rvBreakdown);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvLetterGrade = findViewById(R.id.tvLetterGrade);
        rvBreakdown.setLayoutManager(new LinearLayoutManager(this));

        courseOfferingId = getIntent().getIntExtra("course_offering_id", 0);
        if (courseOfferingId > 0) {
            loadGradeDetails();
        } else {
            Toast.makeText(this, "Invalid Course Offering", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadGradeDetails() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleGradesCourse(courseOfferingId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && !body.get("data").isJsonNull()) {
                        JsonObject data = body.getAsJsonObject("data");

                        if (data.has("final_grade") && !data.get("final_grade").isJsonNull()) {
                            JsonObject grade = data.getAsJsonObject("final_grade");
                            String score = grade.has("total_score") && !grade.get("total_score").isJsonNull() ? grade.get("total_score").getAsString() : "N/A";
                            String letter = grade.has("letter_grade") && !grade.get("letter_grade").isJsonNull() ? grade.get("letter_grade").getAsString() : "N/A";
                            tvFinalScore.setText(score);
                            tvLetterGrade.setText(letter);
                        }

                        if (data.has("breakdown") && !data.get("breakdown").isJsonNull()) {
                            com.google.gson.JsonArray breakdown = data.getAsJsonArray("breakdown");
                            GradeBreakdownAdapter adapter = new GradeBreakdownAdapter(breakdown);
                            rvBreakdown.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(CourseGradeActivity.this, "Failed to load grade details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CourseGradeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
