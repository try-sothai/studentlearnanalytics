package com.social7.studentlearnanalytics.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionDetailActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvRiskBadge;
    private TextView tvRiskTitle;
    private TextView tvRiskDescription;
    private TextView tvRiskFactorItemText;
    private TextView tvLargeScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.prediction_risk);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            View btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        }

        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            progressBar = new ProgressBar(this); // Fallback if XML is different
        }

        tvRiskBadge = findViewById(R.id.tvRiskBadge);
        tvRiskTitle = findViewById(R.id.tvRiskTitle);
        tvRiskDescription = findViewById(R.id.tvRiskDescription);
        tvRiskFactorItemText = findViewById(R.id.tvRiskFactorItemText);
        tvLargeScore = findViewById(R.id.tvLargeScore);

        fetchPredictionData();
    }

    private void fetchPredictionData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getStudentModulePredictionsAndRisk().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    bindData(data);
                } else {
                    Toast.makeText(PredictionDetailActivity.this, "Insufficient Data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PredictionDetailActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(JsonObject data) {
        if (data.has("predictions")) {
            JsonArray preds = data.getAsJsonArray("predictions");
            if (preds.size() > 0) {
                JsonObject pred = preds.get(0).getAsJsonObject();
                if (tvLargeScore != null) {
                    tvLargeScore.setText(pred.has("predicted_score") && !pred.get("predicted_score").isJsonNull() ? pred.get("predicted_score").getAsString() + "%" : "Insufficient Data");
                }
            }
        }

        if (data.has("risks")) {
            JsonArray risks = data.getAsJsonArray("risks");
            if (risks.size() > 0) {
                JsonObject risk = risks.get(0).getAsJsonObject();
                String level = risk.has("risk_level") && !risk.get("risk_level").isJsonNull() ? risk.get("risk_level").getAsString().toUpperCase() : "UNKNOWN";

                if (tvRiskBadge != null) tvRiskBadge.setText(level);
                if (tvRiskTitle != null) tvRiskTitle.setText(level + " RISK");

                if (tvRiskFactorItemText != null) {
                    tvRiskFactorItemText.setText(risk.has("factors") ? risk.get("factors").toString() : "N/A");
                }
            }
        }
    }
}
