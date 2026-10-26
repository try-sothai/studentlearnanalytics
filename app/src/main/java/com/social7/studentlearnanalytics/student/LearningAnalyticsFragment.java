package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.academic.GpaProgressActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningAnalyticsFragment extends Fragment {

    private static final String TAG = "SLAS_ANALYTICS";

    private NestedScrollView nestedScrollViewAnalytics;
    private TextView tvAnalyticsSemester;
    private String currentSelectedSemester = "Semester 1, 2024";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.activity_fragment_learning_analytics,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nestedScrollViewAnalytics = view.findViewById(R.id.nestedScrollViewAnalytics);
        tvAnalyticsSemester = view.findViewById(R.id.tvAnalyticsSemester);

        resetScrollToTop();

        LineChart lineChart = view.findViewById(R.id.lineChartAnalytics);
        setupAnalyticsChart(lineChart);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof StudentMainActivity) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_home);
                    }
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        View semesterSelector = view.findViewById(R.id.semesterSelector);
        if (semesterSelector != null) {
            semesterSelector.setOnClickListener(v -> showSemesterDialog());
        }

        // Academic Index & View Full Performance Click Listeners -> OverallPerformanceActivity
        View cardAcademicIndex = view.findViewById(R.id.cardAcademicIndex);
        if (cardAcademicIndex != null) {
            cardAcademicIndex.setOnClickListener(v -> openPerformanceDetails());
        }

        View btnViewFullPerformance = view.findViewById(R.id.btnViewFullPerformance);
        if (btnViewFullPerformance != null) {
            btnViewFullPerformance.setOnClickListener(v -> openPerformanceDetails());
        }

        View cardPerformanceTrajectory = view.findViewById(R.id.cardPerformanceTrajectory);
        if (cardPerformanceTrajectory != null) {
            cardPerformanceTrajectory.setOnClickListener(v -> openPerformanceDetails());
        }

        // Tab category buttons
        View btnAttendance = view.findViewById(R.id.btnAttendance);
        if (btnAttendance != null) {
            btnAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceDetailActivity.class)));
        }

        View btnAssignments = view.findViewById(R.id.btnAssignments);
        if (btnAssignments != null) {
            btnAssignments.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View btnQuizzes = view.findViewById(R.id.btnQuizzes);
        if (btnQuizzes != null) {
            btnQuizzes.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View btnExams = view.findViewById(R.id.btnExams);
        if (btnExams != null) {
            btnExams.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExamDetailActivity.class)));
        }

        View btnGpa = view.findViewById(R.id.btnGpa);
        if (btnGpa != null) {
            btnGpa.setOnClickListener(v -> startActivity(new Intent(getActivity(), GpaProgressActivity.class)));
        }

        // Metric grid cards
        View cardAttendanceMetric = view.findViewById(R.id.cardAttendanceMetric);
        if (cardAttendanceMetric != null) {
            cardAttendanceMetric.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceDetailActivity.class)));
        }

        View cardAssignmentsMetric = view.findViewById(R.id.cardAssignmentsMetric);
        if (cardAssignmentsMetric != null) {
            cardAssignmentsMetric.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View cardQuizzesMetric = view.findViewById(R.id.cardQuizzesMetric);
        if (cardQuizzesMetric != null) {
            cardQuizzesMetric.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View cardExamsMetric = view.findViewById(R.id.cardExamsMetric);
        if (cardExamsMetric != null) {
            cardExamsMetric.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExamDetailActivity.class)));
        }

        View btnViewAllSubjects = view.findViewById(R.id.btnViewAllSubjects);
        if (btnViewAllSubjects != null) {
            btnViewAllSubjects.setOnClickListener(v -> switchBottomTab(R.id.nav_courses));
        }

        View cardRecommendationCTA = view.findViewById(R.id.cardRecommendationCTA);
        if (cardRecommendationCTA != null) {
            cardRecommendationCTA.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecommendationDetailActivity.class)));
        }

        fetchLearningAnalytics();
    }

    private void openPerformanceDetails() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), OverallPerformanceActivity.class);
        intent.putExtra("selected_semester", currentSelectedSemester);
        startActivity(intent);
    }

    private void fetchLearningAnalytics() {
        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    updateUI(data);
                } else {
                    Log.d(TAG, "Learning Analytics HTTP code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "Learning Analytics API failed: " + t.getMessage());
            }
        });
    }

    private void updateUI(DashboardResponse.DashboardData data) {
        if (getView() == null) return;

        TextView tvHeroPercentage = getView().findViewById(R.id.tvHeroPercentage);
        if (tvHeroPercentage != null) {
            tvHeroPercentage.setText(data.getOverallPerformanceFormatted());
        }

        updateMetricCard(R.id.cardAttendanceMetric, data.getAttendanceRateFormatted());
        updateMetricCard(R.id.cardAssignmentsMetric, data.getAssignmentScoreFormatted());
        updateMetricCard(R.id.cardQuizzesMetric, data.getQuizScoreFormatted());
        updateMetricCard(R.id.cardExamsMetric, data.getExamScoreFormatted());
        
        LineChart lineChart = getView().findViewById(R.id.lineChartAnalytics);
        if (data.getChartData() != null && data.getChartLabels() != null) {
            setupRealAnalyticsChart(lineChart, data.getChartData(), data.getChartLabels());
        }
    }

    private void updateMetricCard(int cardId, String value) {
        if (getView() == null) return;
        View card = getView().findViewById(cardId);
        if (card != null && card instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) card;
            findAndSetPercentageText(vg, value);
        }
    }

    private boolean findAndSetPercentageText(ViewGroup vg, String value) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv.getText().toString().contains("%")) {
                    tv.setText(value);
                    return true;
                }
            } else if (child instanceof ViewGroup) {
                if (findAndSetPercentageText((ViewGroup) child, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        resetScrollToTop();
    }

    private void resetScrollToTop() {
        if (nestedScrollViewAnalytics != null) {
            nestedScrollViewAnalytics.scrollTo(0, 0);
            nestedScrollViewAnalytics.post(() -> nestedScrollViewAnalytics.scrollTo(0, 0));
        }
    }

    private void switchBottomTab(int navId) {
        if (getActivity() instanceof StudentMainActivity) {
            BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) {
                nav.setSelectedItemId(navId);
            }
        }
    }

    private void showSemesterDialog() {
        String[] semesters = {
                "Semester 1, 2024",
                "Semester 2, 2024",
                "Semester 1, 2025"
        };

        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Select Semester")
                .setItems(semesters, (dialog, which) -> {
                    currentSelectedSemester = semesters[which];
                    if (tvAnalyticsSemester != null) {
                        tvAnalyticsSemester.setText(currentSelectedSemester + " ▾");
                    }
                    fetchLearningAnalytics();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupAnalyticsChart(LineChart chart) {
        // Fallback or placeholder until data loads
    }

    private void setupRealAnalyticsChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null || values == null || labels == null || values.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Overall Performance");
        dataSet.setColor(Color.parseColor("#5B35F5"));
        dataSet.setCircleColor(Color.parseColor("#4935D9"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#EEE9FF"));
        dataSet.setFillAlpha(120);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E7E2F5"));
        chart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        chart.getAxisLeft().setTextColor(Color.parseColor("#68658A"));
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setAxisLineColor(Color.TRANSPARENT);
        chart.getXAxis().setTextColor(Color.parseColor("#68658A"));
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));

        chart.animateY(1000);
        chart.invalidate();
    }
}
