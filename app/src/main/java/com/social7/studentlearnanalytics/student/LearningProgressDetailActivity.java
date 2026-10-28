package com.social7.studentlearnanalytics.student;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningProgressDetailActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_PROGRESS_DET";
    private LineChart chartProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_progress_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Spinner
        Spinner spinner = findViewById(R.id.spinnerSemester);
        if (spinner != null) {
            String[] semesters = {"Semester 1, 2024", "Semester 2, 2024", "Semester 1, 2025"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters);
            spinner.setAdapter(adapter);
        }

        chartProgress = findViewById(R.id.lineChartProgress);
        fetchProgressData();
    }

    private void fetchProgressData() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    if (data != null && data.getChartData() != null && !data.getChartData().isEmpty()) {
                        setupLineChart(chartProgress, data.getChartData(), data.getChartLabels());
                    } else {
                        setupLineChart(chartProgress, null, null);
                    }
                } else {
                    Log.d(TAG, "Dashboard API code: " + response.code());
                    setupLineChart(chartProgress, null, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "Dashboard API call failed: " + t.getMessage());
                setupLineChart(chartProgress, null, null);
            }
        });
    }

    private void setupLineChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        if (values == null || values.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No learning progress trend data available yet");
            chart.setNoDataTextColor(Color.parseColor("#64748B"));
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Float val = values.get(i);
            entries.add(new Entry(i, val != null ? val : 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Performance Trend");
        dataSet.setColor(Color.parseColor("#4C35E9"));
        dataSet.setCircleColor(Color.parseColor("#4C35E9"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Fill gradient
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4C35E9"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        if (labels != null && !labels.isEmpty()) {
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        }

        chart.animateY(1000);
        chart.invalidate();
    }
}
