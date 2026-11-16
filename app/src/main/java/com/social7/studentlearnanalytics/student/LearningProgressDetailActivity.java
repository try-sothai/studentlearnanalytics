package com.social7.studentlearnanalytics.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.model.LearningAnalytic;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.LearningAnalyticsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningProgressDetailActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_PROGRESS_DET";
    private LineChart chartProgress;
    private Spinner spinnerSemester;
    private TextView tvBreakdownAttendance, tvBreakdownAttendanceSub;
    private TextView tvBreakdownAssignments, tvBreakdownAssignmentsSub;
    private TextView tvBreakdownQuizzes, tvBreakdownQuizzesSub;
    private TextView tvBreakdownExams, tvBreakdownExamsSub;

    private final List<String> semesterNames = new ArrayList<>();
    private final List<Long> semesterIds = new ArrayList<>();
    private Long selectedSemesterId = null;
    private int selectedCategoryId = R.id.btnOverall;
    private List<LearningAnalytic> cachedAnalytics = new ArrayList<>();
    private DashboardResponse.DashboardData cachedDashboard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_progress_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        spinnerSemester = findViewById(R.id.spinnerSemester);
        chartProgress = findViewById(R.id.lineChartProgress);
        tvBreakdownAttendance = findViewById(R.id.tvBreakdownAttendance);
        tvBreakdownAttendanceSub = findViewById(R.id.tvBreakdownAttendanceSub);
        tvBreakdownAssignments = findViewById(R.id.tvBreakdownAssignments);
        tvBreakdownAssignmentsSub = findViewById(R.id.tvBreakdownAssignmentsSub);
        tvBreakdownQuizzes = findViewById(R.id.tvBreakdownQuizzes);
        tvBreakdownQuizzesSub = findViewById(R.id.tvBreakdownQuizzesSub);
        tvBreakdownExams = findViewById(R.id.tvBreakdownExams);
        tvBreakdownExamsSub = findViewById(R.id.tvBreakdownExamsSub);

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    selectedCategoryId = checkedId;
                    renderSelectedTrend();
                }
            });
        }

        fetchProgressData();
        fetchAnalyticsSeries();
    }

    private void fetchProgressData() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    cachedDashboard = response.body().getData();
                    bindBreakdownCards(cachedDashboard);
                    setupSpinnerIfNeeded(cachedDashboard);
                    renderSelectedTrend();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "Dashboard API call failed: " + t.getMessage());
            }
        });
    }

    private void setupSpinnerIfNeeded(DashboardResponse.DashboardData data) {
        if (spinnerSemester == null || !semesterNames.isEmpty()) return;
        semesterNames.add("All Semesters");
        semesterIds.add(null);
        if (data != null && data.semesters != null) {
            for (DashboardResponse.SemesterInfo s : data.semesters) {
                if (s != null && s.id != null) {
                    semesterNames.add(s.getDisplayName());
                    semesterIds.add(s.getIdAsLong());
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesterNames);
        spinnerSemester.setAdapter(adapter);
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < semesterIds.size()) {
                    Long newId = semesterIds.get(position);
                    if ((selectedSemesterId == null && newId != null)
                            || (selectedSemesterId != null && !selectedSemesterId.equals(newId))) {
                        selectedSemesterId = newId;
                        fetchAnalyticsSeries();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchAnalyticsSeries() {
        ApiClient.getApiService(this).getLearningAnalyticsTrend("monthly", selectedSemesterId)
                .enqueue(new Callback<LearningAnalyticsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LearningAnalyticsResponse> call, @NonNull Response<LearningAnalyticsResponse> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            cachedAnalytics = response.body().data;
                        } else {
                            cachedAnalytics = new ArrayList<>();
                        }
                        renderSelectedTrend();
                    }

                    @Override
                    public void onFailure(@NonNull Call<LearningAnalyticsResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        renderSelectedTrend();
                    }
                });
    }

    private void bindBreakdownCards(DashboardResponse.DashboardData d) {
        if (d == null) return;
        if (tvBreakdownAttendance != null) tvBreakdownAttendance.setText(d.getAttendanceRateFormatted());
        if (tvBreakdownAttendanceSub != null && d.attendanceSummary != null) {
            int p = d.attendanceSummary.present != null ? d.attendanceSummary.present : 0;
            int t = d.attendanceSummary.total != null ? d.attendanceSummary.total : 0;
            tvBreakdownAttendanceSub.setText(t > 0 ? (p + "/" + t + " classes present") : "No sessions yet");
        }
        if (tvBreakdownAssignments != null) tvBreakdownAssignments.setText(d.getAssignmentScoreFormatted());
        if (tvBreakdownAssignmentsSub != null && d.assignmentsSummary != null) {
            int c = d.assignmentsSummary.completed != null ? d.assignmentsSummary.completed : 0;
            int t = d.assignmentsSummary.total != null ? d.assignmentsSummary.total : 0;
            tvBreakdownAssignmentsSub.setText(t > 0 ? (c + "/" + t + " submitted") : "No assignments yet");
        }
        if (tvBreakdownQuizzes != null) tvBreakdownQuizzes.setText(d.getQuizScoreFormatted());
        if (tvBreakdownQuizzesSub != null && d.quizzesSummary != null) {
            int c = d.quizzesSummary.completed != null ? d.quizzesSummary.completed : 0;
            int t = d.quizzesSummary.total != null ? d.quizzesSummary.total : 0;
            tvBreakdownQuizzesSub.setText(t > 0 ? (c + "/" + t + " completed") : "No quizzes yet");
        }
        if (tvBreakdownExams != null) tvBreakdownExams.setText(d.getExamScoreFormatted());
        if (tvBreakdownExamsSub != null && d.examsSummary != null) {
            int c = d.examsSummary.completed != null ? d.examsSummary.completed : 0;
            int t = d.examsSummary.total != null ? d.examsSummary.total : 0;
            tvBreakdownExamsSub.setText(t > 0 ? (c + "/" + t + " completed") : "No exams yet");
        }
    }

    private void renderSelectedTrend() {
        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (cachedAnalytics != null && !cachedAnalytics.isEmpty()) {
            for (int i = 0; i < cachedAnalytics.size(); i++) {
                LearningAnalytic a = cachedAnalytics.get(i);
                float v = 0f;
                if (selectedCategoryId == R.id.btnAttendance) {
                    v = a.attendanceRate != null ? a.attendanceRate : 0f;
                } else if (selectedCategoryId == R.id.btnAssignments) {
                    v = a.assignmentAverage != null ? a.assignmentAverage : 0f;
                } else if (selectedCategoryId == R.id.btnQuizzes) {
                    v = a.quizAverage != null ? a.quizAverage : 0f;
                } else if (selectedCategoryId == R.id.btnExams) {
                    v = a.examAverage != null ? a.examAverage : 0f;
                } else {
                    v = a.overallPerformance != null ? a.overallPerformance : 0f;
                }
                values.add(v);
                String lbl = "P" + (i + 1);
                if (a.periodStart != null && a.periodStart.length() >= 7) {
                    lbl = a.periodStart.substring(5, 7) + "/" + a.periodStart.substring(2, 4);
                }
                labels.add(lbl);
            }
        } else if (cachedDashboard != null && selectedCategoryId == R.id.btnOverall) {
            values = cachedDashboard.getChartData();
            labels = cachedDashboard.getChartLabels();
        }

        setupLineChart(chartProgress, values, labels);
    }

    private void setupLineChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        if (values == null || values.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No learning progress trend data available yet");
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

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.performance_trend));
        dataSet.setColor(Color.parseColor("#4C35E9"));
        dataSet.setCircleColor(Color.parseColor("#4C35E9"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4C35E9"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        if (labels != null && !labels.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }

        chart.animateY(600);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
