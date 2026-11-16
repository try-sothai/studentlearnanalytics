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
                displayUnavailableState();
            }
        });
    }

    private void bindRiskData(DashboardResponse.DashboardData data) {
        // Bind the 4 contributing factor rows from real academic metrics
        bindFactorRow(
                findViewById(R.id.tvRiskAttDesc),
                findViewById(R.id.tvRiskAttBadge),
                data.getAttendanceRateFormatted(),
                " attendance recorded"
        );
        bindFactorRow(
                findViewById(R.id.tvRiskAsnDesc),
                findViewById(R.id.tvRiskAsnBadge),
                data.getAssignmentScoreFormatted(),
                " average score"
        );
        bindFactorRow(
                findViewById(R.id.tvRiskQuizDesc),
                findViewById(R.id.tvRiskQuizBadge),
                data.getQuizScoreFormatted(),
                " average score"
        );
        bindFactorRow(
                findViewById(R.id.tvRiskExamDesc),
                findViewById(R.id.tvRiskExamBadge),
                data.getExamScoreFormatted(),
                " average score"
        );

        String risk = data.getRiskLevel();
        if (risk == null || risk.trim().isEmpty() || "Not available yet".equalsIgnoreCase(risk.trim())) {
            displayUnavailableState();
            return;
        }

        Double serverScoreVal = data.getRiskScoreVal();
        if (serverScoreVal == null) {
            // Derive from real overall performance if present, never hardcode arbitrary constants
            String overallStr = data.getOverallPerformanceFormatted();
            Double parsedOverall = parseNullableDouble(overallStr);
            if (parsedOverall != null) {
                serverScoreVal = Math.max(0.0, Math.min(100.0, 100.0 - parsedOverall));
            }
        }
        int scoreProgress = serverScoreVal != null ? (int) Math.round(serverScoreVal) : 0;
        String scoreStr = serverScoreVal != null ? (scoreProgress + " / 100") : "-- / 100";

        if ("HIGH".equalsIgnoreCase(risk)) {
            if (tvRiskLevel != null) {
                tvRiskLevel.setText(R.string.high_risk_b9f53e);
                tvRiskLevel.setTextColor(Color.parseColor("#EF4444"));
            }
            if (tvRiskBadge != null) {
                tvRiskBadge.setText(R.string.high_risk);
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
            if (tvRiskLevel != null) {
                tvRiskLevel.setText(R.string.medium_risk_cec4a7);
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
            if (tvRiskLevel != null) {
                tvRiskLevel.setText(R.string.low_risk_e60aca);
                tvRiskLevel.setTextColor(Color.parseColor("#10B981"));
            }
            if (tvRiskBadge != null) {
                tvRiskBadge.setText(R.string.low_risk);
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

    private void bindFactorRow(TextView tvDesc, TextView tvBadge, String formattedPct, String suffix) {
        if (tvDesc != null) {
            if (formattedPct == null || "N/A".equalsIgnoreCase(formattedPct)) {
                tvDesc.setText("No records yet");
            } else {
                tvDesc.setText(formattedPct + suffix);
            }
        }
        if (tvBadge != null) {
            Double val = parseNullableDouble(formattedPct);
            if (val == null) {
                tvBadge.setText(R.string.no_data);
                tvBadge.setTextColor(Color.parseColor("#64748B"));
                tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            } else if (val >= 80.0) {
                tvBadge.setText(R.string.status_good);
                tvBadge.setTextColor(Color.parseColor("#10B981"));
                tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
            } else if (val >= 60.0) {
                tvBadge.setText("Moderate");
                tvBadge.setTextColor(Color.parseColor("#F59E0B"));
                tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            } else {
                tvBadge.setText("Needs Improvement");
                tvBadge.setTextColor(Color.parseColor("#EF4444"));
                tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
            }
        }
    }

    private Double parseNullableDouble(String formatted) {
        if (formatted == null) return null;
        try {
            String clean = formatted.replace("%", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void displayUnavailableState() {
        if (tvRiskLevel != null) {
            tvRiskLevel.setText(R.string.unavailable_084f4c);
            tvRiskLevel.setTextColor(Color.parseColor("#64748B"));
        }
        if (tvRiskBadge != null) {
            tvRiskBadge.setText(R.string.no_data);
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
