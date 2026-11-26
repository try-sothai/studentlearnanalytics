package com.social7.studentlearnanalytics.student;

import android.content.Intent;
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
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.student.recommendations.SmartRecommendationsActivity;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionDetailActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ProgressBar pbAttendance, pbAssignment, pbQuiz, pbExam, pbGpa, pbRiskDetailBar;
    private TextView tvAttendanceScore, tvAssignmentScore, tvQuizScore, tvExamScore, tvGpaScore;
    private TextView tvRiskBadge;
    private TextView tvRiskTitle;
    private TextView tvRiskDescription;
    private TextView tvRiskFactorItemText;
    private TextView tvRiskFactorImpactBadge;
    private TextView tvLargeScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Student")) {
            return;
        }
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

        pbAttendance = findViewById(R.id.pbAttendance);
        pbAssignment = findViewById(R.id.pbAssignment);
        pbQuiz = findViewById(R.id.pbQuiz);
        pbExam = findViewById(R.id.pbExam);
        pbGpa = findViewById(R.id.pbGpa);
        pbRiskDetailBar = findViewById(R.id.pbRiskDetailBar);

        tvAttendanceScore = findViewById(R.id.tvAttendanceScore);
        tvAssignmentScore = findViewById(R.id.tvAssignmentScore);
        tvQuizScore = findViewById(R.id.tvQuizScore);
        tvExamScore = findViewById(R.id.tvExamScore);
        tvGpaScore = findViewById(R.id.tvGpaScore);

        tvRiskBadge = findViewById(R.id.tvRiskBadge);
        tvRiskTitle = findViewById(R.id.tvRiskTitle);
        tvRiskDescription = findViewById(R.id.tvRiskDescription);
        tvRiskFactorItemText = findViewById(R.id.tvRiskFactorItemText);
        tvRiskFactorImpactBadge = findViewById(R.id.tvRiskFactorImpactBadge);
        tvLargeScore = findViewById(R.id.tvLargeScore);

        View btnViewAllRecs = findViewById(R.id.btnViewAllRecommendations);
        if (btnViewAllRecs != null) {
            btnViewAllRecs.setOnClickListener(v ->
                    startActivity(new Intent(PredictionDetailActivity.this, SmartRecommendationsActivity.class)));
        }

        fetchPredictionData();
    }

    private void fetchPredictionData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getStudentModulePredictionsAndRisk().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().has("data")
                        && response.body().get("data").isJsonObject()) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    bindData(data);
                } else {
                    if (tvLargeScore != null) tvLargeScore.setText("Insufficient Data");
                    Toast.makeText(PredictionDetailActivity.this, "Insufficient Data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (tvLargeScore != null) tvLargeScore.setText("Unavailable");
                Toast.makeText(PredictionDetailActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindBreakdown(response.body().getData());
                } else {
                    bindBreakdownUnavailable();
                }
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                bindBreakdownUnavailable();
            }
        });
    }

    private void bindBreakdown(DashboardResponse.DashboardData data) {
        String attText = data.getAttendanceRateFormatted();
        String assignText = data.getAssignmentScoreFormatted();
        String quizText = data.getQuizScoreFormatted();
        String examText = data.getExamScoreFormatted();
        String gpaText = data.getGpaFormatted();

        if (tvAttendanceScore != null) tvAttendanceScore.setText(attText);
        if (tvAssignmentScore != null) tvAssignmentScore.setText(assignText);
        if (tvQuizScore != null) tvQuizScore.setText(quizText);
        if (tvExamScore != null) tvExamScore.setText(examText);
        if (tvGpaScore != null) tvGpaScore.setText(gpaText);

        if (pbAttendance != null) pbAttendance.setProgress(parsePercentInt(attText));
        if (pbAssignment != null) pbAssignment.setProgress(parsePercentInt(assignText));
        if (pbQuiz != null) pbQuiz.setProgress(parsePercentInt(quizText));
        if (pbExam != null) pbExam.setProgress(parsePercentInt(examText));
        if (pbGpa != null) {
            int gpaProgress = 0;
            try {
                gpaProgress = (int) Math.round(Double.parseDouble(gpaText) * 100.0);
            } catch (Exception ignored) {}
            pbGpa.setProgress(Math.max(0, Math.min(400, gpaProgress)));
        }
    }

    private void bindBreakdownUnavailable() {
        if (tvAttendanceScore != null) tvAttendanceScore.setText("N/A");
        if (tvAssignmentScore != null) tvAssignmentScore.setText("N/A");
        if (tvQuizScore != null) tvQuizScore.setText("N/A");
        if (tvExamScore != null) tvExamScore.setText("N/A");
        if (tvGpaScore != null) tvGpaScore.setText("N/A");
        if (pbAttendance != null) pbAttendance.setProgress(0);
        if (pbAssignment != null) pbAssignment.setProgress(0);
        if (pbQuiz != null) pbQuiz.setProgress(0);
        if (pbExam != null) pbExam.setProgress(0);
        if (pbGpa != null) pbGpa.setProgress(0);
    }

    private int parsePercentInt(String formatted) {
        if (formatted == null) return 0;
        String clean = formatted.replace("%", "").trim();
        try {
            return Math.max(0, Math.min(100, (int) Math.round(Double.parseDouble(clean))));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void bindData(JsonObject data) {
        if (data == null) return;
        boolean hasPred = false;
        if (data.has("predictions") && data.get("predictions").isJsonArray()) {
            JsonArray preds = data.getAsJsonArray("predictions");
            if (preds.size() > 0 && preds.get(0).isJsonObject()) {
                JsonObject pred = preds.get(0).getAsJsonObject();
                if (pred.has("predicted_score") && !pred.get("predicted_score").isJsonNull()) {
                    hasPred = true;
                    if (tvLargeScore != null) {
                        tvLargeScore.setText(pred.get("predicted_score").getAsString() + "%");
                    }
                }
            }
        }
        if (!hasPred && tvLargeScore != null) {
            tvLargeScore.setText("Insufficient Data");
        }

        boolean hasRisk = false;
        if (data.has("risks") && data.get("risks").isJsonArray()) {
            JsonArray risks = data.getAsJsonArray("risks");
            if (risks.size() > 0 && risks.get(0).isJsonObject()) {
                hasRisk = true;
                JsonObject risk = risks.get(0).getAsJsonObject();
                String level = risk.has("risk_level") && !risk.get("risk_level").isJsonNull()
                        ? risk.get("risk_level").getAsString().toUpperCase() : "UNKNOWN";
                String course = risk.has("course") && !risk.get("course").isJsonNull()
                        ? risk.get("course").getAsString() : "Overall Course";

                if (tvRiskBadge != null) tvRiskBadge.setText(level);
                if (tvRiskTitle != null) tvRiskTitle.setText(level + " RISK");
                int riskProgress = 0;
                if (risk.has("risk_score") && !risk.get("risk_score").isJsonNull()) {
                    try {
                        riskProgress = Math.max(0, Math.min(100, (int) Math.round(risk.get("risk_score").getAsDouble())));
                    } catch (Exception ignored) {}
                }
                if (pbRiskDetailBar != null) pbRiskDetailBar.setProgress(riskProgress);
                if (tvRiskFactorImpactBadge != null) tvRiskFactorImpactBadge.setText(level);
                if (tvRiskDescription != null) {
                    String riskScoreStr = risk.has("risk_score") && !risk.get("risk_score").isJsonNull()
                            ? (" (Risk Score: " + risk.get("risk_score").getAsString() + "/100)") : "";
                    tvRiskDescription.setText("Latest risk evaluation for " + course + riskScoreStr + ".");
                }
                if (tvRiskFactorItemText != null) {
                    StringBuilder factorSb = new StringBuilder();
                    if (risk.has("factors")) {
                        if (risk.get("factors").isJsonArray()) {
                            JsonArray fArr = risk.getAsJsonArray("factors");
                            for (int i = 0; i < fArr.size(); i++) {
                                if (!fArr.get(i).isJsonNull()) {
                                    if (factorSb.length() > 0) factorSb.append("\n• ");
                                    else factorSb.append("• ");
                                    factorSb.append(fArr.get(i).getAsString());
                                }
                            }
                        } else if (!risk.get("factors").isJsonNull()) {
                            factorSb.append(risk.get("factors").getAsString());
                        }
                    }
                    tvRiskFactorItemText.setText(factorSb.length() > 0 ? factorSb.toString() : "No risk factors reported.");
                }
            }
        }
        if (!hasRisk) {
            if (pbRiskDetailBar != null) pbRiskDetailBar.setProgress(0);
            if (tvRiskBadge != null) tvRiskBadge.setText("N/A");
            if (tvRiskTitle != null) tvRiskTitle.setText("No Risk Assessment");
            if (tvRiskDescription != null) tvRiskDescription.setText("No risk assessment records are available yet.");
            if (tvRiskFactorItemText != null) tvRiskFactorItemText.setText("No risk factors reported.");
            if (tvRiskFactorImpactBadge != null) tvRiskFactorImpactBadge.setText("None");
        }
    }
}
