package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // Chart setup
        LineChart chart = view.findViewById(R.id.lineChartTrend);
        setupLineChart(chart);

        // Navigation
        View cardPrediction = view.findViewById(R.id.cardPrediction);
        if (cardPrediction != null) {
            cardPrediction.setOnClickListener(v -> startActivity(new Intent(getActivity(), PredictionDetailActivity.class)));
        }

        View btnViewRecommendations = view.findViewById(R.id.btnViewRecommendations);
        if (btnViewRecommendations != null) {
            btnViewRecommendations.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecommendationDetailActivity.class)));
        }

        fetchPredictionData();

        return view;
    }

    private void fetchPredictionData() {
        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    updateUI(data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateUI(DashboardResponse.DashboardData data) {
        if (getView() == null) return;

        // Find main score text view
        View scoreBox = getView().findViewById(R.id.scoreBox);
        if (scoreBox instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) scoreBox;
            if (vg.getChildCount() > 0 && vg.getChildAt(0) instanceof android.widget.TextView) {
                String scoreStr = data.getPredictedScoreFormatted();
                if (scoreStr.contains("/")) {
                    scoreStr = scoreStr.split("/")[0].trim();
                }
                ((android.widget.TextView) vg.getChildAt(0)).setText(scoreStr);
            }
        }

        // Find Risk Level
        android.widget.TextView tvRisk = findTextViewWithText(getView(), "MEDIUM");
        if (tvRisk == null) tvRisk = findTextViewWithText(getView(), "HIGH");
        if (tvRisk == null) tvRisk = findTextViewWithText(getView(), "LOW");

        if (tvRisk != null) {
            String risk = data.getRiskLevel();
            tvRisk.setText(risk);
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

        // Update Chart
        LineChart chart = getView().findViewById(R.id.lineChartTrend);
        if (data.getPredictionChartData() != null && data.getPredictionChartLabels() != null) {
            setupRealLineChart(chart, data.getPredictionChartData(), data.getPredictionChartLabels());
        }

        // Update Key Factors
        android.widget.TextView tvAtt = findTextViewWithText(getView(), "88%");
        if (tvAtt != null) tvAtt.setText(data.getAttendanceRateFormatted());

        android.widget.TextView tvAssign = findTextViewWithText(getView(), "80%");
        if (tvAssign != null) tvAssign.setText(data.getAssignmentScoreFormatted());

        android.widget.TextView tvQuiz = findTextViewWithText(getView(), "78%");
        if (tvQuiz != null) tvQuiz.setText(data.getQuizScoreFormatted());

        android.widget.TextView tvExam = findTextViewWithText(getView(), "76%");
        if (tvExam != null) tvExam.setText(data.getExamScoreFormatted());

        android.widget.TextView tvGpa = findTextViewWithText(getView(), "3.25 / 4.00");
        if (tvGpa != null) tvGpa.setText(data.getGpaFormatted() + " / 4.00");
    }

    private android.widget.TextView findTextViewWithText(View view, String text) {
        if (view instanceof android.widget.TextView) {
            android.widget.TextView tv = (android.widget.TextView) view;
            if (text.equals(tv.getText().toString())) return tv;
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                android.widget.TextView found = findTextViewWithText(vg.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void setupLineChart(LineChart chart) {
        // Fallback
    }

    private void setupRealLineChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null || values == null || labels == null || values.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Prediction Score");
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

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));

        chart.animateX(1000);
        chart.invalidate();
    }
}
