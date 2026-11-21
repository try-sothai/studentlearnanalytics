package com.social7.studentlearnanalytics.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.activity_fragment_prediction,
                container,
                false
        );

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.prediction_risk), "AI-powered insights for your academic success"
            );
        }

        // Chart setup
        LineChart chart = view.findViewById(R.id.lineChartTrend);
        setupLineChart(chart);

        // Navigation
        View cardPrediction = view.findViewById(R.id.cardPrediction);
        if (cardPrediction != null) {
            cardPrediction.setOnClickListener(v -> startActivity(new Intent(getActivity(), PredictionDetailActivity.class)));
        }

        View cardRiskLevel = view.findViewById(R.id.cardRiskLevel);
        if (cardRiskLevel != null) {
            cardRiskLevel.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        View btnRiskDetails = view.findViewById(R.id.btnRiskDetails);
        if (btnRiskDetails != null) {
            btnRiskDetails.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        View btnViewRecommendations = view.findViewById(R.id.btnViewRecommendations);
        if (btnViewRecommendations != null) {
            btnViewRecommendations.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecommendationDetailActivity.class)));
        }

        fetchPredictionData();

        return view;
    }

    private void fetchPredictionData() {
        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (!isAdded() || getContext() == null || getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    updateUI(data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                android.util.Log.w("PredictionFragment", "Failed to load prediction data: " + t.getMessage());
            }
        });
    }

    private void updateUI(DashboardResponse.DashboardData data) {
        if (getView() == null || data == null) return;

        // Score value
        TextView tvScoreVal = getView().findViewById(R.id.tvPredictionScoreValue);
        String scoreStr = data.getPredictedScoreFormatted();
        if (scoreStr.contains("/")) {
            scoreStr = scoreStr.split("/")[0].trim();
        }
        if (tvScoreVal != null) {
            tvScoreVal.setText(scoreStr);
        }

        TextView tvSubtitle = getView().findViewById(R.id.tvPredictionSubtitle);
        if (tvSubtitle != null) {
            String gpaStr = data.getGpaFormatted();
            String attStr = data.getAttendanceRateFormatted();
            tvSubtitle.setText("Based on current GPA (" + gpaStr + "), attendance (" + attStr + "), and assessment performance.");
        }

        // Risk Level
        TextView tvRisk = getView().findViewById(R.id.tvPredictionRiskValue);
        if (tvRisk != null) {
            String risk = data.getRiskLevel();
            if (risk == null || "Not available yet".equalsIgnoreCase(risk)) {
                tvRisk.setText(R.string.not_available);
                tvRisk.setTextColor(Color.parseColor("#64748B"));
            } else {
                String displayRisk = !risk.toUpperCase().contains("RISK") ? (risk + " RISK") : risk;
                tvRisk.setText(displayRisk);
                if ("HIGH".equalsIgnoreCase(risk)) {
                    tvRisk.setTextColor(Color.parseColor("#EF4444"));
                } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                    tvRisk.setTextColor(Color.parseColor("#F59E0B"));
                } else if ("LOW".equalsIgnoreCase(risk)) {
                    tvRisk.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvRisk.setTextColor(Color.parseColor("#64748B"));
                }
            }
        }

        // Update Chart
        LineChart chart = getView().findViewById(R.id.lineChartTrend);
        setupRealLineChart(chart, data.getPredictionChartData(), data.getPredictionChartLabels());

        // Update Key Factors by ID
        String attFormatted = data.getAttendanceRateFormatted();
        TextView tvAtt = getView().findViewById(R.id.tvPredictionAttendanceValue);
        ProgressBar pbAtt = getView().findViewById(R.id.pbPredAttendance);
        if (tvAtt != null) tvAtt.setText(attFormatted);
        if (pbAtt != null) pbAtt.setProgress(parsePercent(attFormatted));

        String assignFormatted = data.getAssignmentScoreFormatted();
        TextView tvAssign = getView().findViewById(R.id.tvPredictionAssignmentValue);
        ProgressBar pbAssign = getView().findViewById(R.id.pbPredAssignment);
        if (tvAssign != null) tvAssign.setText(assignFormatted);
        if (pbAssign != null) pbAssign.setProgress(parsePercent(assignFormatted));

        String quizFormatted = data.getQuizScoreFormatted();
        TextView tvQuiz = getView().findViewById(R.id.tvPredictionQuizValue);
        ProgressBar pbQuiz = getView().findViewById(R.id.pbPredQuiz);
        if (tvQuiz != null) tvQuiz.setText(quizFormatted);
        if (pbQuiz != null) pbQuiz.setProgress(parsePercent(quizFormatted));

        String examFormatted = data.getExamScoreFormatted();
        TextView tvExam = getView().findViewById(R.id.tvPredictionExamValue);
        ProgressBar pbExam = getView().findViewById(R.id.pbPredExam);
        if (tvExam != null) tvExam.setText(examFormatted);
        if (pbExam != null) pbExam.setProgress(parsePercent(examFormatted));

        TextView tvGpa = getView().findViewById(R.id.tvPredictionGpaValue);
        if (tvGpa != null) {
            String gpaFormatted = data.getGpaFormatted();
            tvGpa.setText("Unavailable".equalsIgnoreCase(gpaFormatted) ? "Unavailable" : (gpaFormatted + " / 4.00"));
        }
    }

    private int parsePercent(String formatted) {
        if (formatted == null) return 0;
        try {
            String clean = formatted.replace("%", "").trim();
            return (int) Math.round(Double.parseDouble(clean));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void setupLineChart(LineChart chart) {
        if (chart != null) {
            chart.setNoDataText("Loading prediction trend...");
            chart.setNoDataTextColor(Color.parseColor("#64748B"));
        }
    }

    private void setupRealLineChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        if (values == null || values.isEmpty() || labels == null || labels.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No prediction trend records available");
            chart.setNoDataTextColor(Color.parseColor("#64748B"));
            ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Float val = values.get(i);
            entries.add(new Entry(i, val != null ? val : 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.prediction_score));
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setCircleColor(Color.parseColor("#5B3DF5"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#5B3DF5"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));

        chart.animateX(800);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
