package com.social7.studentlearnanalytics.student;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiskAnalysisActivity extends AppCompatActivity {

    private TextView tvRiskLevel;
    private TextView tvRiskBadge;
    private TextView tvRiskMsg;
    private TextView tvRiskScore;
    private ProgressBar pbRiskScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_analysis);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskBadge = findViewById(R.id.tvRiskBadge);
        tvRiskMsg = findViewById(R.id.tvRiskMsg);
        tvRiskScore = findViewById(R.id.tvRiskScore);
        pbRiskScore = findViewById(R.id.pbRiskScore);

        fetchRiskData();
    }

    private void fetchRiskData() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    if (data != null && !isFinishing()) {
                        bindRiskData(data);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                // Retain layout fallbacks on network failure
            }
        });
    }

    private void bindRiskData(DashboardResponse.DashboardData data) {
        String risk = data.getRiskLevel();
        if (risk == null || risk.trim().isEmpty() || "Not available yet".equalsIgnoreCase(risk.trim())) {
            displayUnavailableState();
            return;
        }

        Double serverScoreVal = data.getRiskScoreVal();
        String scoreStr = data.getRiskScoreFormatted();

        if ("HIGH".equalsIgnoreCase(risk)) {
            int scoreProgress = serverScoreVal != null ? (int) Math.round(serverScoreVal) : 82;
            if (scoreStr == null) scoreStr = scoreProgress + " / 100";

            if (tvRiskLevel != null) {
                tvRiskLevel.setText("HIGH RISK");
                tvRiskLevel.setTextColor(Color.parseColor("#EF4444"));
            }
            if (tvRiskBadge != null) {
                tvRiskBadge.setText("High Risk");
                tvRiskBadge.setTextColor(Color.parseColor("#EF4444"));
                tvRiskBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
            }
            if (tvRiskMsg != null) {
                tvRiskMsg.setText("Your academic progress is at high risk. Urgent attention is required to improve attendance and assessment scores.");
            }
            if (tvRiskScore != null) tvRiskScore.setText(scoreStr);
            if (pbRiskScore != null) {
                pbRiskScore.setProgress(scoreProgress);
                pbRiskScore.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#EF4444")));
            }
        } else if ("MEDIUM".equalsIgnoreCase(risk)) {
            int scoreProgress = serverScoreVal != null ? (int) Math.round(serverScoreVal) : 65;
            if (scoreStr == null) scoreStr = scoreProgress + " / 100";

            if (tvRiskLevel != null) {
                tvRiskLevel.setText("MEDIUM RISK");
                tvRiskLevel.setTextColor(Color.parseColor("#F59E0B"));
            }
            if (tvRiskBadge != null) {
                tvRiskBadge.setText("Moderate Risk");
                tvRiskBadge.setTextColor(Color.parseColor("#F59E0B"));
                tvRiskBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            }
            if (tvRiskMsg != null) {
                tvRiskMsg.setText("You are at medium risk. Keep improving your attendance and quiz performance to lower academic risk.");
            }
            if (tvRiskScore != null) tvRiskScore.setText(scoreStr);
            if (pbRiskScore != null) {
                pbRiskScore.setProgress(scoreProgress);
                pbRiskScore.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
            }
        } else if ("LOW".equalsIgnoreCase(risk)) {
            int scoreProgress = serverScoreVal != null ? (int) Math.round(serverScoreVal) : 25;
            if (scoreStr == null) scoreStr = scoreProgress + " / 100";

            if (tvRiskLevel != null) {
                tvRiskLevel.setText("LOW RISK");
                tvRiskLevel.setTextColor(Color.parseColor("#10B981"));
            }
            if (tvRiskBadge != null) {
                tvRiskBadge.setText("Low Risk");
                tvRiskBadge.setTextColor(Color.parseColor("#10B981"));
                tvRiskBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
            }
            if (tvRiskMsg != null) {
                tvRiskMsg.setText("Your academic progress is on track! Maintain your current study habits and high class attendance.");
            }
            if (tvRiskScore != null) tvRiskScore.setText(scoreStr);
            if (pbRiskScore != null) {
                pbRiskScore.setProgress(scoreProgress);
                pbRiskScore.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
            }
        } else {
            displayUnavailableState();
        }
    }

    private void displayUnavailableState() {
        if (tvRiskLevel != null) {
            tvRiskLevel.setText("UNAVAILABLE");
            tvRiskLevel.setTextColor(Color.parseColor("#64748B"));
        }
        if (tvRiskBadge != null) {
            tvRiskBadge.setText("No Data");
            tvRiskBadge.setTextColor(Color.parseColor("#64748B"));
            tvRiskBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
        }
        if (tvRiskMsg != null) {
            tvRiskMsg.setText("Risk assessment evaluation is currently unavailable.");
        }
        if (tvRiskScore != null) tvRiskScore.setText("-- / 100");
        if (pbRiskScore != null) {
            pbRiskScore.setProgress(0);
            pbRiskScore.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#64748B")));
        }
    }
}
