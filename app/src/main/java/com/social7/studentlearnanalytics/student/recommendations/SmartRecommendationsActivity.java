package com.social7.studentlearnanalytics.student.recommendations;

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
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartRecommendationsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvRecommendations;
    private View layoutEmptyState, btnRetryRecommendations;
    private TextView tvEmptyTitle, tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_smart_recommendations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_recommendations);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvRecommendations = findViewById(R.id.rvRecommendations);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryRecommendations = findViewById(R.id.btnRetryRecommendations);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        if (btnRetryRecommendations != null) {
            btnRetryRecommendations.setOnClickListener(v -> loadRecommendations());
        }

        loadRecommendations();
    }

    private void loadRecommendations() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModuleRecommendations().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    JsonArray data = (body.has("data") && body.get("data").isJsonArray())
                            ? body.getAsJsonArray("data") : new JsonArray();
                    if (data.size() > 0) {
                        rvRecommendations.setAdapter(new RecommendationsAdapter(data));
                        rvRecommendations.setVisibility(View.VISIBLE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        showState("No Recommendations Yet",
                                "No study recommendations are currently available for your courses.",
                                false);
                    }
                } else {
                    String msg = "Failed to load recommendations (HTTP " + response.code() + ").";
                    Toast.makeText(SmartRecommendationsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    showState("Unable to Load Recommendations", msg, true);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                String msg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Connection failed");
                Toast.makeText(SmartRecommendationsActivity.this, msg, Toast.LENGTH_SHORT).show();
                showState("Connection Error", "Could not reach the server. Check your connection and retry.", true);
            }
        });
    }

    private void showState(String title, String message, boolean showRetry) {
        if (rvRecommendations != null) rvRecommendations.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyTitle != null) tvEmptyTitle.setText(title);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (btnRetryRecommendations != null) btnRetryRecommendations.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
