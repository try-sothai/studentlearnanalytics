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
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicJourneyActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView rvJourney;
    private View layoutEmptyState, btnRetryJourney;
    private TextView tvEmptyTitle, tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_academic_journey);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_academic_journey);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvJourney = findViewById(R.id.rvJourney);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnRetryJourney = findViewById(R.id.btnRetryJourney);
        rvJourney.setLayoutManager(new LinearLayoutManager(this));
        if (btnRetryJourney != null) {
            btnRetryJourney.setOnClickListener(v -> loadAcademicJourney());
        }

        loadAcademicJourney();
    }

    private void loadAcademicJourney() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentAcademicJourney().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    JsonArray data = (body.has("data") && body.get("data").isJsonArray())
                            ? body.getAsJsonArray("data") : new JsonArray();
                    if (data.size() > 0) {
                        rvJourney.setAdapter(new JourneyAdapter(data));
                        rvJourney.setVisibility(View.VISIBLE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        showState("No Academic Journey Data",
                                "No semester progression records are available yet.",
                                false);
                    }
                } else {
                    String msg = "Failed to load academic journey (HTTP " + response.code() + ").";
                    Toast.makeText(AcademicJourneyActivity.this, msg, Toast.LENGTH_SHORT).show();
                    showState("Unable to Load Journey", msg, true);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                String msg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Connection failed");
                Toast.makeText(AcademicJourneyActivity.this, msg, Toast.LENGTH_SHORT).show();
                showState("Connection Error", "Could not reach the server. Check your connection and retry.", true);
            }
        });
    }

    private void showState(String title, String message, boolean showRetry) {
        if (rvJourney != null) rvJourney.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (tvEmptyTitle != null) tvEmptyTitle.setText(title);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        if (btnRetryJourney != null) btnRetryJourney.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
