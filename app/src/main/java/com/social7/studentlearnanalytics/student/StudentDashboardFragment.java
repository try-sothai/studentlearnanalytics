package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.academic.GpaProgressActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_DASHBOARD";

    private TextView tvUserName;
    private TextView tvPerfScore;
    private LineChart lineChartPerformance;
    private SessionManager sessionManager;

    public StudentDashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.activity_fragment_student_dashboard,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        tvUserName = view.findViewById(R.id.tvUserName);
        tvPerfScore = view.findViewById(R.id.tvPerfScore);
        lineChartPerformance = view.findViewById(R.id.lineChartPerformance);

        if (tvUserName != null) {
            String name = sessionManager.getStudentName();
            if (name != null && !name.trim().isEmpty()) {
                tvUserName.setText(name + "! 👋");
            }
        }

        setupPerformanceChart(lineChartPerformance, null);

        // Header Navigation Listeners
        View btnMenu = view.findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> startActivity(new Intent(getActivity(), NavigationMenuActivity.class)));
        }

        View profileContainer = view.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> switchBottomTab(R.id.nav_profile));
        }

        View notifContainer = view.findViewById(R.id.notifContainer);
        if (notifContainer != null) {
            notifContainer.setOnClickListener(v -> switchBottomTab(R.id.nav_notifications));
        }

        // Metrics Grid Click Listeners
        View cardAttendance = view.findViewById(R.id.cardAttendance);
        if (cardAttendance != null) {
            cardAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceDetailActivity.class)));
        }

        View cardAssignments = view.findViewById(R.id.cardAssignments);
        if (cardAssignments != null) {
            cardAssignments.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View cardQuizzes = view.findViewById(R.id.cardQuizzes);
        if (cardQuizzes != null) {
            cardQuizzes.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View cardGpa = view.findViewById(R.id.cardGpa);
        if (cardGpa != null) {
            cardGpa.setOnClickListener(v -> startActivity(new Intent(getActivity(), GpaProgressActivity.class)));
        }

        View cardOverallPerformance = view.findViewById(R.id.cardOverallPerformance);
        if (cardOverallPerformance != null) {
            cardOverallPerformance.setOnClickListener(v -> startActivity(new Intent(getActivity(), OverallPerformanceActivity.class)));
        }

        View btnViewDetails = view.findViewById(R.id.btnViewDetails);
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> startActivity(new Intent(getActivity(), OverallPerformanceActivity.class)));
        }

        // Predicted Score Card Navigation Listener
        View cardPredictedScore = view.findViewById(R.id.cardPredictedScore);
        if (cardPredictedScore != null) {
            cardPredictedScore.setOnClickListener(v -> startActivity(new Intent(getActivity(), PredictionDetailActivity.class)));
        }

        // Risk Level Card Navigation Listeners
        View cardRiskLevel = view.findViewById(R.id.cardRiskLevel);
        if (cardRiskLevel != null) {
            cardRiskLevel.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        View btnRiskDetails = view.findViewById(R.id.btnRiskDetails);
        if (btnRiskDetails != null) {
            btnRiskDetails.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        // Quick Actions Section Listeners
        View btnViewAllActions = view.findViewById(R.id.btnViewAllActions);
        if (btnViewAllActions != null) {
            btnViewAllActions.setOnClickListener(v -> startActivity(new Intent(getActivity(), AllFeaturesActivity.class)));
        }

        View actionCourses = view.findViewById(R.id.actionCourses);
        if (actionCourses != null) {
            actionCourses.setOnClickListener(v -> switchBottomTab(R.id.nav_courses));
        }

        View actionAttendance = view.findViewById(R.id.actionAttendance);
        if (actionAttendance != null) {
            actionAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceDetailActivity.class)));
        }

        View actionAssignments = view.findViewById(R.id.actionAssignments);
        if (actionAssignments != null) {
            actionAssignments.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View actionQuizzes = view.findViewById(R.id.actionQuizzes);
        if (actionQuizzes != null) {
            actionQuizzes.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View actionExams = view.findViewById(R.id.actionExams);
        if (actionExams != null) {
            actionExams.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExamDetailActivity.class)));
        }

        View actionAnalytics = view.findViewById(R.id.actionAnalytics);
        if (actionAnalytics != null) {
            actionAnalytics.setOnClickListener(v -> switchBottomTab(R.id.nav_analytics));
        }

        // Recent Activities Section Listeners
        View btnViewAllActivities = view.findViewById(R.id.btnViewAllActivities);
        if (btnViewAllActivities != null) {
            btnViewAllActivities.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecentActivitiesActivity.class)));
        }

        View cardLearningProgress = view.findViewById(R.id.cardLearningProgress);
        if (cardLearningProgress != null) {
            cardLearningProgress.setOnClickListener(v -> switchBottomTab(R.id.nav_analytics));
        }

        fetchDashboardData(view);

        return view;
    }

    private void fetchDashboardData(View root) {
        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse dashboard = response.body();
                    DashboardResponse.DashboardData data = dashboard.getData();
                    if (data != null && isAdded()) {
                        bindDashboardData(root, data);
                    }
                } else {
                    Log.d(TAG, "Dashboard API response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "Dashboard API call failed: " + t.getMessage());
            }
        });
    }

    private void bindDashboardData(View root, DashboardResponse.DashboardData data) {
        if (data == null || root == null) return;

        if (tvUserName != null && !data.getStudentName().isEmpty()) {
            tvUserName.setText(data.getStudentName() + "! 👋");
        }

        TextView tvStudentId = root.findViewById(R.id.tvStudentId);
        if (tvStudentId != null && !data.getStudentId().isEmpty()) {
            tvStudentId.setText("Student ID: " + data.getStudentId());
        }

        TextView tvDepartmentYear = root.findViewById(R.id.tvDepartmentYear);
        if (tvDepartmentYear != null && !data.getDepartment().isEmpty()) {
            String yearLvl = data.getYearLevel();
            String suffix = "th";
            if ("1".equals(yearLvl)) suffix = "st";
            else if ("2".equals(yearLvl)) suffix = "nd";
            else if ("3".equals(yearLvl)) suffix = "rd";
            tvDepartmentYear.setText(data.getDepartment() + " | " + yearLvl + suffix + " Year");
        }

        if (tvPerfScore != null) {
            tvPerfScore.setText(data.getOverallPerformanceFormatted());
        }

        TextView tvMetricAttendance = root.findViewById(R.id.tvMetricAttendance);
        if (tvMetricAttendance != null) {
            tvMetricAttendance.setText(data.getAttendanceRateFormatted());
        }

        TextView tvMetricAssignments = root.findViewById(R.id.tvMetricAssignments);
        if (tvMetricAssignments != null) {
            tvMetricAssignments.setText(data.getAssignmentScoreFormatted());
        }

        TextView tvMetricQuizzes = root.findViewById(R.id.tvMetricQuizzes);
        if (tvMetricQuizzes != null) {
            tvMetricQuizzes.setText(data.getQuizScoreFormatted());
        }

        TextView tvMetricGpa = root.findViewById(R.id.tvMetricGpa);
        if (tvMetricGpa != null) {
            tvMetricGpa.setText(data.getGpaFormatted());
        }

        TextView tvPredictedScore = root.findViewById(R.id.tvPredictedScore);
        ProgressBar pbPredictedScore = root.findViewById(R.id.pbPredictedScore);
        TextView tvConfidence = root.findViewById(R.id.tvConfidence);

        if (tvPredictedScore != null) {
            String predStr = data.getPredictedScoreFormatted();
            tvPredictedScore.setText(predStr);

            if ("Not available yet".equalsIgnoreCase(predStr)) {
                if (pbPredictedScore != null) pbPredictedScore.setProgress(0);
                if (tvConfidence != null) {
                    tvConfidence.setText("Prediction model pending");
                    tvConfidence.setTextColor(Color.parseColor("#64748B"));
                }
            } else {
                if (pbPredictedScore != null) {
                    try {
                        String clean = predStr.split("/")[0].trim();
                        double score = Double.parseDouble(clean);
                        pbPredictedScore.setProgress((int) Math.round(score));
                    } catch (Exception ignored) {
                        pbPredictedScore.setProgress(50);
                    }
                }
                if (tvConfidence != null) {
                    tvConfidence.setText("✓ Model Confidence: 87%");
                    tvConfidence.setTextColor(Color.parseColor("#10B981"));
                }
            }
        }

        TextView tvRiskLevel = root.findViewById(R.id.tvRiskLevel);
        TextView tvRiskMsg = root.findViewById(R.id.tvRiskMsg);
        if (tvRiskLevel != null) {
            String risk = data.getRiskLevel();
            tvRiskLevel.setText(risk);
            if ("HIGH".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#EF4444")); // Danger red
                if (tvRiskMsg != null) tvRiskMsg.setText("High risk! Action recommended.");
            } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#F59E0B")); // Warning orange
                if (tvRiskMsg != null) tvRiskMsg.setText("Keep improving performance.");
            } else if ("LOW".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#10B981")); // Success green
                if (tvRiskMsg != null) tvRiskMsg.setText("Great job! Low risk.");
            } else {
                tvRiskLevel.setTextColor(Color.parseColor("#64748B")); // Secondary grey
                if (tvRiskMsg != null) tvRiskMsg.setText("Risk assessment pending.");
            }
        }

        if (data.getChartData() != null && !data.getChartData().isEmpty()) {
            setupPerformanceChart(lineChartPerformance, data.getChartData());
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

    private void setupPerformanceChart(LineChart chart, List<Float> values) {
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            for (int i = 0; i < values.size(); i++) {
                entries.add(new Entry(i, values.get(i)));
            }
        } else {
            entries.add(new Entry(0, 20));
            entries.add(new Entry(1, 45));
            entries.add(new Entry(2, 60));
            entries.add(new Entry(3, 75));
            entries.add(new Entry(4, 82));
            entries.add(new Entry(5, 85));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Performance");
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setCircleColor(Color.parseColor("#5B3DF5"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#F1EDFF"));
        dataSet.setFillAlpha(120);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E2E8F0"));
        chart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        chart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        chart.getAxisLeft().setTextSize(10f);

        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setAxisLineColor(Color.TRANSPARENT);
        chart.getXAxis().setTextColor(Color.parseColor("#64748B"));
        chart.getXAxis().setTextSize(10f);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
