package com.social7.studentlearnanalytics.student.academic;

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

public class GpaProgressActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvCumulativeGpa;
    private TextView tvCompletedCredits;
    private TextView tvSemesterGpa;

    // UI elements for comparisons could be added here, but for now we display text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa_progress);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) progressBar = new ProgressBar(this);

        tvCumulativeGpa = findViewById(R.id.tvCumulativeGpa);
        tvCompletedCredits = findViewById(R.id.tvCompletedCredits);
        tvSemesterGpa = findViewById(R.id.tvSemesterGpa);

        loadGpaData();
    }

    private void loadGpaData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data.has("metrics")) {
                        JsonObject metrics = data.getAsJsonObject("metrics");
                        String cgpa = metrics.has("cumulative_gpa") && !metrics.get("cumulative_gpa").isJsonNull() ? metrics.get("cumulative_gpa").getAsString() : "N/A";
                        if (tvCumulativeGpa != null) tvCumulativeGpa.setText(cgpa);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GpaProgressActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getStudentModuleComparisons().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data.has("semester_comparison")) {
                        JsonArray semComp = data.getAsJsonArray("semester_comparison");
                        if (semComp.size() > 0) {
                            JsonObject latest = semComp.get(semComp.size() - 1).getAsJsonObject();
                            String gpa = latest.has("gpa") ? latest.get("gpa").getAsString() : "N/A";
                            String credits = latest.has("credits") ? latest.get("credits").getAsString() : "0";
                            if (tvSemesterGpa != null) tvSemesterGpa.setText(gpa);
                            if (tvCompletedCredits != null) tvCompletedCredits.setText(credits);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) { }
        });
    }
}
