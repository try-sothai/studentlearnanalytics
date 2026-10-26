package com.social7.studentlearnanalytics.student;

import android.content.Intent;
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

public class PredictionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_detail);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnViewAll = findViewById(R.id.btnViewAllRecommendations);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, RecommendationDetailActivity.class)));
        }

        fetchDashboardData();
    }

    private void fetchDashboardData() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    if (data != null && !isFinishing()) {
                        bindData(data);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindData(DashboardResponse.DashboardData data) {
        TextView tvLargeScore = findViewById(R.id.tvLargeScore);
        if (tvLargeScore != null) {
            tvLargeScore.setText(data.getPredictedScoreFormatted());
        }

        TextView tvAttendanceScore = findViewById(R.id.tvAttendanceScore);
        ProgressBar pbAttendance = findViewById(R.id.pbAttendance);
        if (tvAttendanceScore != null) {
            String att = data.getAttendanceRateFormatted();
            tvAttendanceScore.setText(att);
            if (pbAttendance != null) pbAttendance.setProgress(parsePercentage(att));
        }

        TextView tvAssignmentScore = findViewById(R.id.tvAssignmentScore);
        ProgressBar pbAssignment = findViewById(R.id.pbAssignment);
        if (tvAssignmentScore != null) {
            String assign = data.getAssignmentScoreFormatted();
            tvAssignmentScore.setText(assign);
            if (pbAssignment != null) pbAssignment.setProgress(parsePercentage(assign));
        }

        TextView tvQuizScore = findViewById(R.id.tvQuizScore);
        ProgressBar pbQuiz = findViewById(R.id.pbQuiz);
        if (tvQuizScore != null) {
            String quiz = data.getQuizScoreFormatted();
            tvQuizScore.setText(quiz);
            if (pbQuiz != null) pbQuiz.setProgress(parsePercentage(quiz));
        }

        TextView tvExamScore = findViewById(R.id.tvExamScore);
        ProgressBar pbExam = findViewById(R.id.pbExam);
        if (tvExamScore != null) {
            String exam = data.getExamScoreFormatted();
            tvExamScore.setText(exam);
            if (pbExam != null) pbExam.setProgress(parsePercentage(exam));
        }

        TextView tvGpaScore = findViewById(R.id.tvGpaScore);
        ProgressBar pbGpa = findViewById(R.id.pbGpa);
        if (tvGpaScore != null) {
            String gpa = data.getGpaFormatted();
            tvGpaScore.setText(gpa);
            if (pbGpa != null) {
                try {
                    double g = Double.parseDouble(gpa);
                    pbGpa.setProgress((int) Math.round(g * 100));
                } catch (Exception ignored) {
                    pbGpa.setProgress(325);
                }
            }
        }

        TextView tvRiskBadge = findViewById(R.id.tvRiskBadge);
        TextView tvRiskTitle = findViewById(R.id.tvRiskTitle);
        TextView tvRiskDescription = findViewById(R.id.tvRiskDescription);

        String risk = data.getRiskLevel();
        if (tvRiskBadge != null) tvRiskBadge.setText(risk);

        if ("HIGH".equalsIgnoreCase(risk)) {
            if (tvRiskTitle != null) {
                tvRiskTitle.setText("High Risk");
                tvRiskTitle.setTextColor(Color.parseColor("#EF4444"));
            }
            if (tvRiskBadge != null) tvRiskBadge.setTextColor(Color.parseColor("#EF4444"));
            if (tvRiskDescription != null) {
                tvRiskDescription.setText("Urgent attention needed! Attendance or assessment scores require immediate improvement.");
            }
        } else if ("MEDIUM".equalsIgnoreCase(risk)) {
            if (tvRiskTitle != null) {
                tvRiskTitle.setText("Medium Risk");
                tvRiskTitle.setTextColor(Color.parseColor("#F59E0B"));
            }
            if (tvRiskBadge != null) tvRiskBadge.setTextColor(Color.parseColor("#F59E0B"));
            if (tvRiskDescription != null) {
                tvRiskDescription.setText("There is a moderate chance that your performance may decline if no improvement is made.");
            }
        } else if ("LOW".equalsIgnoreCase(risk)) {
            if (tvRiskTitle != null) {
                tvRiskTitle.setText("Low Risk");
                tvRiskTitle.setTextColor(Color.parseColor("#10B981"));
            }
            if (tvRiskBadge != null) tvRiskBadge.setTextColor(Color.parseColor("#10B981"));
            if (tvRiskDescription != null) {
                tvRiskDescription.setText("Your academic progress is on track! Keep up the good work.");
            }
        } else {
            if (tvRiskTitle != null) {
                tvRiskTitle.setText("Risk Assessment Pending");
                tvRiskTitle.setTextColor(Color.parseColor("#64748B"));
            }
            if (tvRiskBadge != null) tvRiskBadge.setTextColor(Color.parseColor("#64748B"));
            if (tvRiskDescription != null) {
                tvRiskDescription.setText("Risk evaluation data is currently not available yet.");
            }
        }
    }

    private int parsePercentage(String formatted) {
        if (formatted == null) return 0;
        try {
            String clean = formatted.replace("%", "").trim();
            double val = Double.parseDouble(clean);
            return (int) Math.round(val);
        } catch (Exception ignored) {
            return 75;
        }
    }
}
