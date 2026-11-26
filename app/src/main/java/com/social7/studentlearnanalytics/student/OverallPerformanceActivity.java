package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OverallPerformanceActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_PERFORMANCE_DET";

    private ScrollView scrollContent;
    private View layoutLoading;
    private View layoutError;
    private TextView tvErrorMessage;

    private ProgressBar pbOverallScore;
    private TextView tvOverallScore;
    private TextView tvAttendanceScore;
    private TextView tvAttendanceStatus;
    private TextView tvAssignmentsScore;
    private TextView tvAssignmentsStatus;
    private TextView tvQuizzesScore;
    private TextView tvQuizzesStatus;
    private TextView tvKeyInsightText;
    private LinearLayout layoutSubjectList;
    private BarChart barChartPerformance;
    private Spinner spinnerSemester;

    private String passedSemester = null;
    private boolean appliedPassedSemester = false;
    private Long selectedSemesterId = null;
    private final Map<String, Long> discoveredSemesters = new LinkedHashMap<>();
    private final List<String> semesterLabels = new ArrayList<>();
    private final List<Long> semesterIds = new ArrayList<>();
    private boolean suppressSemesterSelect = false;
    private DashboardResponse.DashboardData cachedData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall_performance);

        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        if (getIntent() != null && getIntent().hasExtra("selected_semester")) {
            passedSemester = getIntent().getStringExtra("selected_semester");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // State Views
        scrollContent = findViewById(R.id.scrollContent);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        View btnRetry = findViewById(R.id.btnRetry);
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> fetchPerformanceData());
        }

        // Content Views
        pbOverallScore = findViewById(R.id.pbOverallScore);
        tvOverallScore = findViewById(R.id.tvOverallScore);
        tvAttendanceScore = findViewById(R.id.tvAttendanceScore);
        tvAttendanceStatus = findViewById(R.id.tvAttendanceStatus);
        tvAssignmentsScore = findViewById(R.id.tvAssignmentsScore);
        tvAssignmentsStatus = findViewById(R.id.tvAssignmentsStatus);
        tvQuizzesScore = findViewById(R.id.tvQuizzesScore);
        tvQuizzesStatus = findViewById(R.id.tvQuizzesStatus);
        tvKeyInsightText = findViewById(R.id.tvKeyInsightText);
        layoutSubjectList = findViewById(R.id.layoutSubjectList);
        barChartPerformance = findViewById(R.id.barChartPerformance);
        spinnerSemester = findViewById(R.id.spinnerSemester);

        // Detail Navigations
        View cardAttendance = findViewById(R.id.cardPerformanceAttendance);
        if (cardAttendance != null) {
            cardAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceDetailActivity.class)));
        }

        View cardAssignments = findViewById(R.id.cardPerformanceAssignments);
        if (cardAssignments != null) {
            cardAssignments.setOnClickListener(v -> startActivity(new Intent(this, AssessmentDetailActivity.class)));
        }

        View cardQuizzes = findViewById(R.id.cardPerformanceQuizzes);
        if (cardQuizzes != null) {
            cardQuizzes.setOnClickListener(v -> startActivity(new Intent(this, QuizDetailActivity.class)));
        }

        View btnViewRecommendations = findViewById(R.id.btnViewRecommendations);
        if (btnViewRecommendations != null) {
            btnViewRecommendations.setOnClickListener(v -> startActivity(new Intent(this, RecommendationDetailActivity.class)));
        }

        View btnViewAllSubjects = findViewById(R.id.btnViewAllSubjects);
        if (btnViewAllSubjects != null) {
            btnViewAllSubjects.setOnClickListener(v -> {
                Intent intent = new Intent(this, StudentMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("select_tab", R.id.nav_courses);
                startActivity(intent);
            });
        }

        fetchPerformanceData();
    }

    private void showLoading() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (scrollContent != null) scrollContent.setVisibility(View.GONE);
    }

    private void showContent() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (scrollContent != null) scrollContent.setVisibility(View.VISIBLE);
    }

    private void showError(String msg) {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.VISIBLE);
        if (scrollContent != null) scrollContent.setVisibility(View.GONE);
        if (tvErrorMessage != null && msg != null && !msg.isEmpty()) {
            tvErrorMessage.setText(msg);
        }
    }

    private void fetchPerformanceData() {
        showLoading();

        ApiClient.getApiService(this).getStudentDashboardFiltered(selectedSemesterId, null).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    cachedData = response.body().getData();
                    bindData(cachedData);
                    showContent();
                } else {
                    Log.d(TAG, "Failed to load dashboard data: " + response.code());
                    String msg;
                    if (response.code() == 404) {
                        msg = "Student profile record not found for active user. Please sign in with a Student account.";
                    } else if (response.code() == 401 || response.code() == 403) {
                        msg = "Authentication required or session expired. Please sign in as a Student.";
                    } else {
                        msg = "Server returned code " + response.code() + ". Please tap Retry.";
                    }
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "API call error: " + t.getMessage());
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void bindData(DashboardResponse.DashboardData data) {
        if (data == null) return;

        // Overall Performance
        String perfStr = data.getOverallPerformanceFormatted();
        if (tvOverallScore != null) {
            tvOverallScore.setText(perfStr);
        }
        if (pbOverallScore != null) {
            try {
                String clean = perfStr.replace("%", "").trim();
                double val = Double.parseDouble(clean);
                pbOverallScore.setProgress((int) Math.round(val));
            } catch (Exception ignored) {
                pbOverallScore.setProgress(0);
            }
        }

        // Attendance
        String attStr = data.getAttendanceRateFormatted();
        if (tvAttendanceScore != null) {
            tvAttendanceScore.setText(attStr);
        }
        bindMetricStatus(tvAttendanceStatus, attStr);

        // Assignments
        String assignStr = data.getAssignmentScoreFormatted();
        if (tvAssignmentsScore != null) {
            tvAssignmentsScore.setText(assignStr);
        }
        bindMetricStatus(tvAssignmentsStatus, assignStr);

        // Quizzes
        String quizStr = data.getQuizScoreFormatted();
        if (tvQuizzesScore != null) {
            tvQuizzesScore.setText(quizStr);
        }
        bindMetricStatus(tvQuizzesStatus, quizStr);

        // Key Insights & Subject List
        populateKeyInsights(tvKeyInsightText, data);
        populateSubjectList(layoutSubjectList, data.getEnrollments());

        // Semesters dropdown
        setupSemesterSpinner(spinnerSemester, data.getEnrollments());

        // Chart Data
        if (data.getChartData() != null && !data.getChartData().isEmpty()) {
            setupBarChart(barChartPerformance, data.getChartData(), data.getChartLabels());
        } else {
            setupBarChart(barChartPerformance, null, null);
        }
    }

    private void bindMetricStatus(TextView tvStatus, String scoreStr) {
        if (tvStatus == null) return;
        if (scoreStr == null || "N/A".equalsIgnoreCase(scoreStr) || "Unavailable".equalsIgnoreCase(scoreStr)) {
            tvStatus.setVisibility(View.GONE);
            return;
        }
        try {
            double d = Double.parseDouble(scoreStr.replace("%", "").trim());
            tvStatus.setVisibility(View.VISIBLE);
            if (d >= 85) {
                tvStatus.setText(R.string.status_good);
                tvStatus.setTextColor(Color.parseColor("#10B981"));
                tvStatus.setBackgroundColor(Color.parseColor("#ECFDF5"));
            } else if (d >= 70) {
                tvStatus.setText(R.string.fair);
                tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                tvStatus.setBackgroundColor(Color.parseColor("#FFFBEB"));
            } else {
                tvStatus.setText(R.string.needs_attention);
                tvStatus.setTextColor(Color.parseColor("#EF4444"));
                tvStatus.setBackgroundColor(Color.parseColor("#FEF2F2"));
            }
        } catch (Exception ignored) {
            tvStatus.setVisibility(View.GONE);
        }
    }

    private void setupSemesterSpinner(Spinner spinner, List<DashboardResponse.EnrollmentInfo> enrollments) {
        if (spinner == null) return;

        if (enrollments != null) {
            for (DashboardResponse.EnrollmentInfo info : enrollments) {
                if (info != null && info.semester != null) {
                    String semName = info.semester.getDisplayName();
                    Long semId = info.semester.getIdAsLong();
                    if (!semName.isEmpty() && !discoveredSemesters.containsKey(semName)) {
                        discoveredSemesters.put(semName, semId);
                    }
                }
            }
        }

        semesterLabels.clear();
        semesterIds.clear();
        semesterLabels.add("All Semesters");
        semesterIds.add(null);

        for (Map.Entry<String, Long> entry : discoveredSemesters.entrySet()) {
            semesterLabels.add(entry.getKey());
            semesterIds.add(entry.getValue());
        }

        if (!appliedPassedSemester && passedSemester != null && !passedSemester.trim().isEmpty()) {
            appliedPassedSemester = true;
            String target = passedSemester.trim();
            for (int i = 1; i < semesterLabels.size(); i++) {
                if (semesterLabels.get(i).equalsIgnoreCase(target)) {
                    Long matchedId = semesterIds.get(i);
                    if (matchedId != null && !matchedId.equals(selectedSemesterId)) {
                        selectedSemesterId = matchedId;
                        fetchPerformanceData();
                        return;
                    }
                    break;
                }
            }
        }

        int targetPos = 0;
        if (selectedSemesterId != null) {
            for (int i = 1; i < semesterIds.size(); i++) {
                if (selectedSemesterId.equals(semesterIds.get(i))) {
                    targetPos = i;
                    break;
                }
            }
        }

        suppressSemesterSelect = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesterLabels);
        spinner.setAdapter(adapter);
        spinner.setSelection(targetPos, false);
        spinner.post(() -> suppressSemesterSelect = false);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSemesterSelect) return;
                Long newId = (position >= 0 && position < semesterIds.size()) ? semesterIds.get(position) : null;
                if ((newId == null && selectedSemesterId == null) || (newId != null && newId.equals(selectedSemesterId))) {
                    return;
                }
                selectedSemesterId = newId;
                fetchPerformanceData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void populateKeyInsights(TextView tvInsight, DashboardResponse.DashboardData data) {
        if (tvInsight == null || data == null) return;

        String risk = data.getRiskLevel();
        String att = data.getAttendanceRateFormatted();
        String assign = data.getAssignmentScoreFormatted();
        String quiz = data.getQuizScoreFormatted();

        StringBuilder sb = new StringBuilder("💡 ");
        if (!"N/A".equalsIgnoreCase(att) || !"N/A".equalsIgnoreCase(assign) || !"N/A".equalsIgnoreCase(quiz)) {
            sb.append("Current status — Attendance: ").append(att)
                    .append(", Assignments: ").append(assign)
                    .append(", Quizzes: ").append(quiz).append(".");
            if ("HIGH".equalsIgnoreCase(risk)) {
                sb.append(" Immediate action is recommended to improve your standing.");
            } else if ("LOW".equalsIgnoreCase(risk)) {
                sb.append(" Outstanding academic performance across components!");
            } else {
                sb.append(" Continue regular review to sustain and boost your scores.");
            }
        } else {
            sb.append("Performance insights will update as academic records become available.");
        }

        tvInsight.setText(sb.toString());
    }

    private void populateSubjectList(LinearLayout container, List<DashboardResponse.EnrollmentInfo> enrollments) {
        if (container == null) return;
        container.removeAllViews();

        if (enrollments == null || enrollments.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(R.string.no_subject_performance_records_available);
            tvEmpty.setTextColor(Color.parseColor("#64748B"));
            tvEmpty.setTextSize(12f);
            container.addView(tvEmpty);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < enrollments.size(); i++) {
            DashboardResponse.EnrollmentInfo info = enrollments.get(i);
            if (info == null || info.course == null) continue;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setClickable(true);
            row.setFocusable(true);
            row.setPadding((int) (8 * density), (int) (6 * density), (int) (8 * density), (int) (6 * density));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                params.topMargin = (int) (12 * density);
            }
            row.setLayoutParams(params);

            TextView tvName = new TextView(this);
            String name = info.course.courseName != null ? info.course.courseName : "Course";
            String code = info.course.courseCode != null ? info.course.courseCode : "";
            if (!code.isEmpty()) {
                name = code + " - " + name;
            }
            if (info.finalScore != null) {
                name = name + " (" + Math.round(info.finalScore) + "%) →";
            } else {
                name = name + " →";
            }
            tvName.setText(name);
            tvName.setTextSize(13f);
            tvName.setTextColor(Color.parseColor("#1E1B4B"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (6 * density)
            );
            pbParams.topMargin = (int) (4 * density);
            pb.setLayoutParams(pbParams);

            int progressVal = 0;
            if (info.finalScore != null) {
                progressVal = Math.max(0, Math.min(100, Math.round(info.finalScore)));
            }
            pb.setProgress(progressVal);
            pb.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#5B3DF5")));

            // Make subject row clickable -> opens CourseDetailActivity with real API fields
            final String finalCName = info.course.courseName != null ? info.course.courseName : "";
            final String finalCCode = code;
            final String finalCId = info.course.id != null ? info.course.id : "";
            final int finalScore = progressVal;
            final int credits = info.course.credit != null ? Math.round(info.course.credit) : 0;
            final String teacherName = (info.course.teacher != null
                    && info.course.teacher.user != null
                    && info.course.teacher.user.name != null)
                    ? info.course.teacher.user.name.trim()
                    : "";
            final String dept = (cachedData != null) ? cachedData.getMajorOrDepartment() : "";
            final String grade = info.grade != null ? info.grade.trim() : "";

            row.setOnClickListener(v -> {
                Course course = new Course(finalCId, finalCCode, finalCName, teacherName, finalScore, credits, "", 0, 0, 0, 0, dept);
                Intent intent = new Intent(OverallPerformanceActivity.this, CourseDetailActivity.class);
                intent.putExtra("course", course);
                if (!finalCId.isEmpty()) intent.putExtra("course_id", finalCId);
                if (!finalCCode.isEmpty()) intent.putExtra("course_code", finalCCode);
                if (!finalCName.isEmpty()) intent.putExtra("course_title", finalCName);
                if (!teacherName.isEmpty()) intent.putExtra("instructor", teacherName);
                if (!dept.isEmpty()) intent.putExtra("course_department", dept);
                if (!grade.isEmpty()) intent.putExtra("course_grade", grade);
                startActivity(intent);
            });

            row.addView(tvName);
            row.addView(pb);
            container.addView(row);
        }
    }

    private void setupBarChart(BarChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> validLabels = new ArrayList<>();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                Float val = values.get(i);
                if (val == null) continue;
                int xIdx = entries.size();
                entries.add(new BarEntry(xIdx, val));
                String lbl = (labels != null && i < labels.size() && labels.get(i) != null) ? labels.get(i) : String.valueOf(xIdx + 1);
                validLabels.add(lbl);
            }
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No trend data available");
            chart.setNoDataTextColor(Color.parseColor("#64748B"));
            ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
            chart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.performance_trend));
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chart.setData(barData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextColor(Color.parseColor("#64748B"));
        chart.getXAxis().setAxisLineColor(Color.parseColor("#E8E4FA"));
        chart.getXAxis().setGranularity(1f);

        com.github.mikephil.charting.components.YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#64748B"));
        leftAxis.setGridColor(Color.parseColor("#E8E4FA"));
        leftAxis.setAxisLineColor(Color.parseColor("#E8E4FA"));

        if (!validLabels.isEmpty()) {
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(validLabels));
        }

        chart.animateY(1000);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
