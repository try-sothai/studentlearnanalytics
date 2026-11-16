package com.social7.studentlearnanalytics.student.grades;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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

public class GradesOverviewActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvGrades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades_overview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Academic Results");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvGrades = findViewById(R.id.rvGrades);
        rvGrades.setLayoutManager(new LinearLayoutManager(this));

        loadGradesOverview();
    }

    private void loadGradesOverview() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleGradesOverview(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && !body.get("data").isJsonNull()) {
                        JsonObject data = body.getAsJsonObject("data");
                        if (data.has("courses") && !data.get("courses").isJsonNull()) {
                            JsonArray courses = data.getAsJsonArray("courses");
                            GradesAdapter adapter = new GradesAdapter(courses);
                            rvGrades.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(GradesOverviewActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GradesOverviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
