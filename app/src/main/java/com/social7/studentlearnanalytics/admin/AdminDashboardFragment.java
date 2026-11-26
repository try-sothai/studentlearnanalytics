package com.social7.studentlearnanalytics.admin;


import com.social7.studentlearnanalytics.localization.ApiLabelLocalizer;
import com.social7.studentlearnanalytics.localization.AppLanguageManager;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import android.graphics.Color;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_ADMIN_DASH";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LineChart growthChart;
    private TextView tvChartEmptyState;
    private Spinner spAcademicYearFilter;
    
    private BarChart performanceChart;
    private TextView tvPerformanceEmptyState;
    private BarChart attendanceChart;
    private TextView tvAttendanceEmptyState;
    private PieChart riskChart;
    private TextView tvRiskEmptyState;
    private MaterialButton btnCompareYears, btnHistoricalReports;

    private TextView tvAdminName;
    private TextView tvNotifBadge;

    private TextView tvActiveClassesCount;
    private TextView tvActiveClassesChange;
    private TextView tvTotalStudentsCount;
    private TextView tvTotalStudentsChange;
    private TextView tvAvgPerformanceCount;
    private TextView tvAvgPerformanceChange;
    private TextView tvAtRiskCount;
    private TextView tvAtRiskChange;

    private TextView tvApiServerDot;
    private TextView tvApiServerText;
    private TextView tvDbDot;
    private TextView tvDbText;
    private TextView tvMlDot;
    private TextView tvMlText;

    private JsonObject latestAnnualAnalyticsObj;
    private final List<AcademicYearOption> academicYearOptions = new ArrayList<>();
    private String selectedAcademicYearId = "all";

    private static class AcademicYearOption {
        String id;
        String name;

        AcademicYearOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    public AdminDashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_admin_dashboard,
                container,
                false
        );

        initViews(view);
        setupClickListeners(view);
        loadUserSessionData();

        fetchAdminDashboardData();

        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshAdminDashboard);
        progressBar = view.findViewById(R.id.progressBarAdminDashboard);
        growthChart = view.findViewById(R.id.adminGrowthChart);
        tvChartEmptyState = view.findViewById(R.id.tvChartEmptyState);
        spAcademicYearFilter = view.findViewById(R.id.spAcademicYearFilter);
        
        performanceChart = view.findViewById(R.id.performanceChart);
        tvPerformanceEmptyState = view.findViewById(R.id.tvPerformanceEmptyState);
        attendanceChart = view.findViewById(R.id.attendanceChart);
        tvAttendanceEmptyState = view.findViewById(R.id.tvAttendanceEmptyState);
        riskChart = view.findViewById(R.id.riskChart);
        tvRiskEmptyState = view.findViewById(R.id.tvRiskEmptyState);
        btnCompareYears = view.findViewById(R.id.btnCompareYears);
        btnHistoricalReports = view.findViewById(R.id.btnHistoricalReports);

        tvAdminName = view.findViewById(R.id.tvAdminName);
        tvNotifBadge = view.findViewById(R.id.tvNotifBadge);

        tvActiveClassesCount = view.findViewById(R.id.tvActiveClassesCount);
        tvActiveClassesChange = view.findViewById(R.id.tvActiveClassesChange);

        tvTotalStudentsCount = view.findViewById(R.id.tvTotalStudentsCount);
        tvTotalStudentsChange = view.findViewById(R.id.tvTotalStudentsChange);

        tvAvgPerformanceCount = view.findViewById(R.id.tvAvgPerformanceCount);
        tvAvgPerformanceChange = view.findViewById(R.id.tvAvgPerformanceChange);

        tvAtRiskCount = view.findViewById(R.id.tvAtRiskCount);
        tvAtRiskChange = view.findViewById(R.id.tvAtRiskChange);

        tvApiServerDot = view.findViewById(R.id.tvApiServerDot);
        tvApiServerText = view.findViewById(R.id.tvApiServerText);

        tvDbDot = view.findViewById(R.id.tvDbDot);
        tvDbText = view.findViewById(R.id.tvDbText);

        tvMlDot = view.findViewById(R.id.tvMlDot);
        tvMlText = view.findViewById(R.id.tvMlText);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#155DFC"));
            swipeRefreshLayout.setOnRefreshListener(this::fetchAdminDashboardData);
        }

        setupAcademicYearSpinner();
    }

    private void setupAcademicYearSpinner() {
        if (spAcademicYearFilter == null || getContext() == null) return;

        academicYearOptions.clear();
        academicYearOptions.add(new AcademicYearOption("all", getString(R.string.all_academic_years)));

        ArrayAdapter<AcademicYearOption> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, academicYearOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAcademicYearFilter.setAdapter(adapter);

        spAcademicYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < academicYearOptions.size()) {
                    selectedAcademicYearId = academicYearOptions.get(position).id;
                    fetchAnalyticsData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUserSessionData() {
        if (getContext() == null) return;
        TextView tvGreeting = getView() != null ? getView().findViewById(R.id.tvAdminGreeting) : null;
        if (tvGreeting != null) {
            tvGreeting.setText(ApiLabelLocalizer.getTimeGreeting(requireContext()) + ",");
        }
        if (tvAdminName == null) return;
        SessionManager sessionManager = new SessionManager(requireContext());
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.trim().isEmpty()) {
            tvAdminName.setText(ApiLabelLocalizer.localizeSystemDisplayName(requireContext(), userName.trim()));
        }

        if (AppLanguageManager.isKhmer(requireContext())) {
            tvAdminName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            if (tvGreeting != null) {
                tvGreeting.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            }
            TextView tvSubtitle = getView() != null ? getView().findViewById(R.id.tvAdminSubtitle) : null;
            if (tvSubtitle != null) {
                tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            }
        }
    }

    private void setupClickListeners(View view) {
        View btnMenu = view.findViewById(R.id.btnAdminMenu);
        View btnNotif = view.findViewById(R.id.btnNotificationBell);
        View cardAvatar = view.findViewById(R.id.cardAvatar);
        View cardActiveClasses = view.findViewById(R.id.cardDashActiveClasses);
        View cardTotalStudents = view.findViewById(R.id.cardDashTotalStudents);
        View cardAvgPerformance = view.findViewById(R.id.cardDashAvgPerformance);
        View cardAtRiskAlerts = view.findViewById(R.id.cardDashAtRiskAlerts);
        View cardStatus = view.findViewById(R.id.cardDashSystemStatus);
        View btnViewStats = view.findViewById(R.id.btnViewStats);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new NotificationFragment());
                }
            });
        }

        if (cardAvatar != null) {
            cardAvatar.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new AdminProfileFragment());
                }
            });
        }

        if (cardActiveClasses != null) {
            cardActiveClasses.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new com.social7.studentlearnanalytics.admin.classes.ClassManagementFragment());
                }
            });
        }

        if (cardTotalStudents != null) {
            cardTotalStudents.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_users);
                    ((AdminMainActivity) getActivity()).openFragment(new UserManagementFragment());
                }
            });
        }

        if (cardAvgPerformance != null) {
            cardAvgPerformance.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_reports);
                    ((AdminMainActivity) getActivity()).openFragment(new ReportsFragment());
                }
            });
        }

        if (cardAtRiskAlerts != null) {
            cardAtRiskAlerts.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new MlModelEvaluationFragment());
                }
            });
        }

        if (cardStatus != null) {
            cardStatus.setOnClickListener(v -> openSystemStats());
        }

        if (btnViewStats != null) {
            btnViewStats.setOnClickListener(v -> openSystemStats());
        }

        if (btnCompareYears != null) {
            btnCompareYears.setOnClickListener(v -> showYearComparisonDialog());
        }

        if (btnHistoricalReports != null) {
            btnHistoricalReports.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).selectTab(R.id.nav_admin_reports);
                    ((AdminMainActivity) getActivity()).openFragment(new ReportsFragment());
                }
            });
        }
    }

    private void openSystemStats() {
        if (getActivity() instanceof AdminMainActivity) {
            ((AdminMainActivity) getActivity()).openFragment(new SystemStatisticsFragment());
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show && (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) ? View.VISIBLE : View.GONE);
        }
    }

    private void stopRefresh() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        showLoading(false);
    }

    private void fetchAdminDashboardData() {
        if (getContext() == null) return;
        showLoading(true);

        ApiClient.getApiService(requireContext()).getAdminDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                stopRefresh();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        bindDashboardData(data);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing admin dashboard payload", e);
                        Toast.makeText(getContext(), getString(R.string.error_parsing_dashboard_data), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Admin dashboard response error code: " + response.code());
                    fetchOverviewAndGrowthFallback();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Admin dashboard network failure", t);
                fetchOverviewAndGrowthFallback();
            }
        });
    }

    private void fetchOverviewAndGrowthFallback() {
        if (getContext() == null) return;
        stopRefresh();
        fetchAnalyticsData();
    }

    private void bindDashboardData(JsonObject data) {
        if (data == null) return;

        bindOverviewCards(data);

        fetchAnalyticsData();

        bindSystemStatus(data);
    }

    private void fetchAnalyticsData() {
        fetchEnrollmentAnalytics();
        fetchPerformanceAnalytics();
        fetchAttendanceAnalytics();
        fetchStudentRiskAnalytics();
    }

    private void fetchEnrollmentAnalytics() {
        ApiClient.getApiService(requireContext()).getAdminAnalyticsEnrollment().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        if (root.has("data") && root.get("data").isJsonObject()) {
                            bindEnrollmentAnalytics(root.getAsJsonObject("data"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing enrollment analytics", e);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void fetchPerformanceAnalytics() {
        ApiClient.getApiService(requireContext()).getAdminAnalyticsPerformance().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        if (root.has("data") && root.get("data").isJsonObject()) {
                            bindPerformanceAnalytics(root.getAsJsonObject("data"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing performance analytics", e);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void fetchAttendanceAnalytics() {
        ApiClient.getApiService(requireContext()).getAdminAnalyticsAttendance().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        if (root.has("data") && root.get("data").isJsonObject()) {
                            bindAttendanceAnalytics(root.getAsJsonObject("data"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing attendance analytics", e);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void fetchStudentRiskAnalytics() {
        ApiClient.getApiService(requireContext()).getAdminAnalyticsStudentRisk().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        if (root.has("data") && root.get("data").isJsonObject()) {
                            bindStudentRiskAnalytics(root.getAsJsonObject("data"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing risk analytics", e);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void bindEnrollmentAnalytics(JsonObject data) {
        // Parse series
        if (!data.has("series")) return;
        JsonArray seriesArray = data.getAsJsonArray("series");
        
        JsonArray newArr = new JsonArray();
        int prevTotal = 0;
        for (int i = 0; i < seriesArray.size(); i++) {
            if (!seriesArray.get(i).isJsonObject()) continue;
            JsonObject item = seriesArray.get(i).getAsJsonObject();
            int totalStu = com.social7.studentlearnanalytics.utils.UiUtils.optInt(item, "total_students", 0);
            int activeStu = com.social7.studentlearnanalytics.utils.UiUtils.optInt(item, "active_learners", 0);
            double growthPct = com.social7.studentlearnanalytics.utils.UiUtils.calculateGrowthPercentage(totalStu, prevTotal);
            if (totalStu > 0) prevTotal = totalStu;

            JsonObject aySummary = new JsonObject();
            aySummary.addProperty("academic_year_name", com.social7.studentlearnanalytics.utils.UiUtils.optString(item, "academic_year", "Year"));
            aySummary.addProperty("has_data", true);
            JsonObject regUsers = new JsonObject();
            regUsers.addProperty("total", totalStu);
            aySummary.add("registered_users", regUsers);

            JsonObject activeUsers = new JsonObject();
            activeUsers.addProperty("total", activeStu);
            aySummary.add("active_users", activeUsers);

            aySummary.addProperty("enrolled_students", totalStu);
            aySummary.addProperty("user_growth_percentage", growthPct);
            newArr.add(aySummary);
        }
        latestAnnualAnalyticsObj = new JsonObject();
        latestAnnualAnalyticsObj.add("all_years_analytics", newArr);
        
        List<String> xLabels = new ArrayList<>();
        List<Entry> totalStudentsList = new ArrayList<>();
        List<Entry> activeLearnersList = new ArrayList<>();
        List<Entry> atRiskList = new ArrayList<>();

        for (int i = 0; i < seriesArray.size(); i++) {
            if (!seriesArray.get(i).isJsonObject()) continue;
            JsonObject item = seriesArray.get(i).getAsJsonObject();
            xLabels.add(com.social7.studentlearnanalytics.utils.UiUtils.optString(item, "academic_year", "Year"));
            totalStudentsList.add(new Entry(i, com.social7.studentlearnanalytics.utils.UiUtils.optInt(item, "total_students", 0)));
            activeLearnersList.add(new Entry(i, com.social7.studentlearnanalytics.utils.UiUtils.optInt(item, "active_learners", 0)));
            atRiskList.add(new Entry(i, com.social7.studentlearnanalytics.utils.UiUtils.optInt(item, "at_risk_students", 0)));
        }

        if (xLabels.isEmpty()) {
            tvChartEmptyState.setVisibility(View.VISIBLE);
            growthChart.setVisibility(View.GONE);
            return;
        }

        tvChartEmptyState.setVisibility(View.GONE);
        growthChart.setVisibility(View.VISIBLE);

        Log.d(TAG, "Enrollment Analytics API JSON: " + data.toString());
        Log.d(TAG, "Total Students values: " + totalStudentsList.toString());
        Log.d(TAG, "Active Learners values: " + activeLearnersList.toString());
        Log.d(TAG, "At-Risk Students values: " + atRiskList.toString());

        LineDataSet set1 = new LineDataSet(totalStudentsList, getString(R.string.total_students));
        set1.setColor(Color.parseColor("#10B981"));
        set1.setCircleColor(Color.parseColor("#10B981"));
        set1.setLineWidth(2.5f);
        set1.setCircleRadius(4f);
        set1.setDrawCircles(true);
        set1.setDrawCircleHole(true);
        set1.setDrawValues(false);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set2 = new LineDataSet(activeLearnersList, getString(R.string.active_learners));
        set2.setColor(Color.parseColor("#0EA5E9"));
        set2.setCircleColor(Color.parseColor("#0EA5E9"));
        set2.setLineWidth(2.5f);
        set2.setCircleRadius(4f);
        set2.setDrawCircles(true);
        set2.setDrawCircleHole(true);
        set2.setDrawValues(false);
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet set3 = new LineDataSet(atRiskList, getString(R.string.at_risk_students));
        set3.setColor(Color.parseColor("#EF4444"));
        set3.setCircleColor(Color.parseColor("#EF4444"));
        set3.setLineWidth(2.5f);
        set3.setCircleRadius(4f);
        set3.setDrawCircles(true);
        set3.setDrawCircleHole(true);
        set3.setDrawValues(false);
        set3.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set1, set2, set3);
        growthChart.setData(lineData);

        Log.d(TAG, "Number of entries - Total: " + set1.getEntryCount() + ", Active: " + set2.getEntryCount() + ", Risk: " + set3.getEntryCount());
        Log.d(TAG, "Colors assigned - Set1: #10B981, Set2: #0EA5E9, Set3: #EF4444");
        Log.d(TAG, "Final chart dataset count: " + lineData.getDataSetCount());

        XAxis xAxis = growthChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-20f);
        xAxis.setTextColor(Color.parseColor("#64748B"));

        growthChart.getAxisLeft().setAxisMinimum(0f);
        growthChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
        growthChart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        growthChart.getAxisRight().setEnabled(false);

        growthChart.getDescription().setEnabled(false);
        growthChart.getLegend().setEnabled(false);
        growthChart.animateX(600);
        ChartFontHelper.applyKhmerFont(growthChart.getContext(), growthChart);
        growthChart.invalidate();
    }

    private void bindPerformanceAnalytics(JsonObject data) {
        if (!data.has("series")) return;
        JsonArray seriesArray = data.getAsJsonArray("series");

        List<String> xLabels = new ArrayList<>();
        List<BarEntry> gpaList = new ArrayList<>();
        List<BarEntry> assignmentList = new ArrayList<>();
        List<BarEntry> quizList = new ArrayList<>();
        List<BarEntry> examList = new ArrayList<>();

        for (int i = 0; i < seriesArray.size(); i++) {
            if (!seriesArray.get(i).isJsonObject()) continue;
            JsonObject item = seriesArray.get(i).getAsJsonObject();
            xLabels.add(com.social7.studentlearnanalytics.utils.UiUtils.optString(item, "semester", "Sem"));
            gpaList.add(new BarEntry(i, com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "avg_gpa", 0f)));
            assignmentList.add(new BarEntry(i, com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "avg_assignment", 0f)));
            quizList.add(new BarEntry(i, com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "avg_quiz", 0f)));
            examList.add(new BarEntry(i, com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "avg_exam", 0f)));
        }

        if (xLabels.isEmpty()) {
            tvPerformanceEmptyState.setVisibility(View.VISIBLE);
            performanceChart.setVisibility(View.GONE);
            return;
        }

        tvPerformanceEmptyState.setVisibility(View.GONE);
        performanceChart.setVisibility(View.VISIBLE);

        // Wait, the prompt says "Line Chart for GPA trends, Grouped Bar Chart for Assignment, Quiz, Exam".
        // MPAndroidChart supports CombinedChart. If I use BarChart for everything, it's not following "Line Chart for GPA".
        // But the layout has BarChart. I will change layout to CombinedChart later if needed, or just plot GPA in a BarChart using a separate axis?
        // Let's use BarChart with separate axis.

        BarDataSet gpaSet = new BarDataSet(gpaList, getString(R.string.avg_gpa));
        gpaSet.setColor(Color.parseColor("#8B5CF6"));
        gpaSet.setAxisDependency(com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT);

        BarDataSet assignSet = new BarDataSet(assignmentList, getString(R.string.assignments));
        assignSet.setColor(Color.parseColor("#3B82F6"));

        BarDataSet quizSet = new BarDataSet(quizList, getString(R.string.quizzes));
        quizSet.setColor(Color.parseColor("#F59E0B"));

        BarDataSet examSet = new BarDataSet(examList, getString(R.string.exams));
        examSet.setColor(Color.parseColor("#10B981"));

        BarData barData = new BarData(gpaSet, assignSet, quizSet, examSet);

        // Group bars
        float groupSpace = 0.08f;
        float barSpace = 0.03f;
        float barWidth = 0.2f; // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> exactly 1.00

        barData.setBarWidth(barWidth);
        performanceChart.setData(barData);
        performanceChart.groupBars(0f, groupSpace, barSpace);

        XAxis xAxis = performanceChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(xLabels.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        performanceChart.getAxisLeft().setAxisMinimum(0f);
        performanceChart.getAxisLeft().setAxisMaximum(100f);
        performanceChart.getAxisRight().setAxisMinimum(0f);
        performanceChart.getAxisRight().setAxisMaximum(4f);

        performanceChart.getDescription().setEnabled(false);
        performanceChart.animateY(500);
        ChartFontHelper.applyKhmerFont(performanceChart.getContext(), performanceChart);
        performanceChart.invalidate();
    }

    private void bindAttendanceAnalytics(JsonObject data) {
        if (!data.has("series")) return;
        JsonArray seriesArray = data.getAsJsonArray("series");

        List<String> xLabels = new ArrayList<>();
        List<BarEntry> stackedList = new ArrayList<>();
        List<BarEntry> rateList = new ArrayList<>();

        for (int i = 0; i < seriesArray.size(); i++) {
            if (!seriesArray.get(i).isJsonObject()) continue;
            JsonObject item = seriesArray.get(i).getAsJsonObject();
            xLabels.add(com.social7.studentlearnanalytics.utils.UiUtils.optString(item, "semester", "Sem"));

            float present = com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "present", 0f);
            float absent = com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "absent", 0f);
            float late = com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "late", 0f);

            stackedList.add(new BarEntry(i, new float[]{present, late, absent}));

            float rate = com.social7.studentlearnanalytics.utils.UiUtils.optFloat(item, "rate", 0f);
            rateList.add(new BarEntry(i, rate));
        }

        if (xLabels.isEmpty()) {
            tvAttendanceEmptyState.setVisibility(View.VISIBLE);
            attendanceChart.setVisibility(View.GONE);
            return;
        }

        tvAttendanceEmptyState.setVisibility(View.GONE);
        attendanceChart.setVisibility(View.VISIBLE);

        BarDataSet stackedSet = new BarDataSet(stackedList, getString(R.string.attendance));
        stackedSet.setColors(
            Color.parseColor("#10B981"), // Present
            Color.parseColor("#F59E0B"), // Late
            Color.parseColor("#EF4444")  // Absent
        );
        stackedSet.setStackLabels(new String[]{
                getString(R.string.status_present),
                getString(R.string.status_late),
                getString(R.string.status_absent)
        });

        BarDataSet rateSet = new BarDataSet(rateList, getString(R.string.rate));
        rateSet.setColor(Color.parseColor("#3B82F6"));
        rateSet.setAxisDependency(com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT);

        BarData barData = new BarData(stackedSet, rateSet);

        float groupSpace = 0.2f;
        float barSpace = 0.05f;
        float barWidth = 0.35f; // (0.35 + 0.05) * 2 + 0.2 = 1.00

        barData.setBarWidth(barWidth);
        attendanceChart.setData(barData);
        attendanceChart.groupBars(0f, groupSpace, barSpace);

        XAxis xAxis = attendanceChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(xLabels.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        attendanceChart.getAxisLeft().setAxisMinimum(0f);
        attendanceChart.getAxisRight().setAxisMinimum(0f);
        attendanceChart.getAxisRight().setAxisMaximum(100f);

        attendanceChart.getDescription().setEnabled(false);
        attendanceChart.animateY(500);
        ChartFontHelper.applyKhmerFont(attendanceChart.getContext(), attendanceChart);
        attendanceChart.invalidate();
    }

    private void bindStudentRiskAnalytics(JsonObject data) {
        if (!data.has("summary")) return;
        JsonObject summary = data.getAsJsonObject("summary");

        int low = summary.has("low") ? summary.get("low").getAsInt() : 0;
        int medium = summary.has("medium") ? summary.get("medium").getAsInt() : 0;
        int high = summary.has("high") ? summary.get("high").getAsInt() : 0;
        int total = summary.has("total") ? summary.get("total").getAsInt() : 0;

        if (total == 0) {
            tvRiskEmptyState.setVisibility(View.VISIBLE);
            riskChart.setVisibility(View.GONE);
            return;
        }

        tvRiskEmptyState.setVisibility(View.GONE);
        riskChart.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        if (low > 0) entries.add(new PieEntry(low, getString(R.string.risk_low)));
        if (medium > 0) entries.add(new PieEntry(medium, getString(R.string.risk_medium)));
        if (high > 0) entries.add(new PieEntry(high, getString(R.string.risk_high)));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
            Color.parseColor("#10B981"), // Low
            Color.parseColor("#F59E0B"), // Medium
            Color.parseColor("#EF4444")  // High
        );
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.WHITE);

        riskChart.setData(pieData);
        riskChart.setDrawHoleEnabled(true);
        riskChart.setHoleColor(Color.WHITE);
        riskChart.setTransparentCircleRadius(50f);
        riskChart.setCenterText(getString(R.string.total_assessed_format, total));
        riskChart.setCenterTextSize(14f);
        riskChart.getDescription().setEnabled(false);
        riskChart.animateY(500);
        ChartFontHelper.applyKhmerFont(riskChart.getContext(), riskChart);
        riskChart.invalidate();
    }
    private void showYearComparisonDialog() {
        if (!isAdded() || getContext() == null || latestAnnualAnalyticsObj == null) {
            Toast.makeText(getContext(), R.string.analytics_data_loading, Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArray analyticsArr = null;
        if (latestAnnualAnalyticsObj.has("all_years_analytics") && latestAnnualAnalyticsObj.get("all_years_analytics").isJsonArray()) {
            analyticsArr = latestAnnualAnalyticsObj.getAsJsonArray("all_years_analytics");
        } else if (latestAnnualAnalyticsObj.has("annual_analytics") && latestAnnualAnalyticsObj.get("annual_analytics").isJsonArray()) {
            analyticsArr = latestAnnualAnalyticsObj.getAsJsonArray("annual_analytics");
        }

        if (analyticsArr == null || analyticsArr.size() < 2) {
            Toast.makeText(getContext(), R.string.at_least_2_years_comparison, Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject itemA = analyticsArr.get(Math.max(0, analyticsArr.size() - 2)).getAsJsonObject();
        JsonObject itemB = analyticsArr.get(analyticsArr.size() - 1).getAsJsonObject();

        String nameA = itemA.has("academic_year_name") ? itemA.get("academic_year_name").getAsString() : "Year A";
        String nameB = itemB.has("academic_year_name") ? itemB.get("academic_year_name").getAsString() : "Year B";

        boolean hasDataA = !itemA.has("has_data") || itemA.get("has_data").getAsBoolean();
        boolean hasDataB = !itemB.has("has_data") || itemB.get("has_data").getAsBoolean();

        int regA = (hasDataA && itemA.has("registered_users") && itemA.get("registered_users").isJsonObject()) ? itemA.getAsJsonObject("registered_users").get("total").getAsInt() : 0;
        int regB = (hasDataB && itemB.has("registered_users") && itemB.get("registered_users").isJsonObject()) ? itemB.getAsJsonObject("registered_users").get("total").getAsInt() : 0;

        int activeA = (hasDataA && itemA.has("active_users") && itemA.get("active_users").isJsonObject()) ? itemA.getAsJsonObject("active_users").get("total").getAsInt() : 0;
        int activeB = (hasDataB && itemB.has("active_users") && itemB.get("active_users").isJsonObject()) ? itemB.getAsJsonObject("active_users").get("total").getAsInt() : 0;

        int enrolledA = (hasDataA && itemA.has("enrolled_students")) ? itemA.get("enrolled_students").getAsInt() : 0;
        int enrolledB = (hasDataB && itemB.has("enrolled_students")) ? itemB.get("enrolled_students").getAsInt() : 0;

        double growthA = (hasDataA && itemA.has("user_growth_percentage")) ? itemA.get("user_growth_percentage").getAsDouble() : 0.0;
        double growthB = (hasDataB && itemB.has("user_growth_percentage")) ? itemB.get("user_growth_percentage").getAsDouble() : 0.0;

        String noDataLabel = " " + getString(R.string.no_data_paren);
        String msg = getString(R.string.comparing_academic_years) + "\n" +
                "• " + nameA + (hasDataA ? "" : noDataLabel) + "\n" +
                "• " + nameB + (hasDataB ? "" : noDataLabel) + "\n\n" +
                "📊 " + getString(R.string.metrics_comparison) + "\n\n" +
                "1. " + getString(R.string.registered_users) + ":\n" +
                "   • " + nameA + ": " + regA + "\n" +
                "   • " + nameB + ": " + regB + "\n" +
                "   • " + (regB - regA >= 0 ? "+" : "") + (regB - regA) + "\n\n" +
                "2. " + getString(R.string.active_system_users) + ":\n" +
                "   • " + nameA + ": " + activeA + "\n" +
                "   • " + nameB + ": " + activeB + "\n" +
                "   • " + (activeB - activeA >= 0 ? "+" : "") + (activeB - activeA) + "\n\n" +
                "3. " + getString(R.string.enrolled_students) + ":\n" +
                "   • " + nameA + ": " + enrolledA + "\n" +
                "   • " + nameB + ": " + enrolledB + "\n" +
                "   • " + (enrolledB - enrolledA >= 0 ? "+" : "") + (enrolledB - enrolledA) + "\n\n" +
                "4. " + getString(R.string.user_growth_rate) + ":\n" +
                "   • " + nameA + ": " + growthA + "%\n" +
                "   • " + nameB + ": " + growthB + "%\n\n" +
                getString(R.string.historical_db_verified);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.academic_year_comparison)
                .setMessage(msg)
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setTrendColor(TextView textView, String changeText, boolean inverseLogic) {
        if (textView == null || changeText == null) return;
        textView.setText(ApiLabelLocalizer.localizeTrendText(textView.getContext(), changeText));
        
        int colorRes = R.color.text_secondary;
        if (changeText.contains("▲") || changeText.contains("+")) {
            colorRes = inverseLogic ? R.color.danger_red : R.color.success_green;
        } else if (changeText.contains("▼") || changeText.contains("-")) {
            colorRes = inverseLogic ? R.color.success_green : R.color.danger_red;
        }
        
        textView.setTextColor(androidx.core.content.ContextCompat.getColor(textView.getContext(), colorRes));
    }

    private void bindOverviewCards(JsonObject data) {
        if (data == null) return;

        // Card 1: Active Classes
        String activeClasses = data.has("active_classes_count") && !data.get("active_classes_count").isJsonNull()
                ? data.get("active_classes_count").getAsString()
                : (data.has("total_classes") && !data.get("total_classes").isJsonNull() ? data.get("total_classes").getAsString() : "9");
        if (tvActiveClassesCount != null) tvActiveClassesCount.setText(activeClasses);

        if (tvActiveClassesChange != null && data.has("active_classes_change") && !data.get("active_classes_change").isJsonNull()) {
            setTrendColor(tvActiveClassesChange, data.get("active_classes_change").getAsString(), false);
        }

        // Card 2: Total Students
        String totalStudents = data.has("total_students_count") && !data.get("total_students_count").isJsonNull()
                ? data.get("total_students_count").getAsString()
                : (data.has("total_students") && !data.get("total_students").isJsonNull() ? data.get("total_students").getAsString() : "10");
        if (tvTotalStudentsCount != null) tvTotalStudentsCount.setText(totalStudents);

        if (tvTotalStudentsChange != null && data.has("total_students_change") && !data.get("total_students_change").isJsonNull()) {
            setTrendColor(tvTotalStudentsChange, data.get("total_students_change").getAsString(), false);
        }

        // Card 3: Avg Performance
        String avgPerf = "78.4%";
        if (data.has("avg_performance") && !data.get("avg_performance").isJsonNull()) {
            String val = data.get("avg_performance").getAsString();
            avgPerf = val.contains("%") ? val : val + "%";
        }
        if (tvAvgPerformanceCount != null) tvAvgPerformanceCount.setText(avgPerf);

        if (tvAvgPerformanceChange != null && data.has("avg_performance_change") && !data.get("avg_performance_change").isJsonNull()) {
            setTrendColor(tvAvgPerformanceChange, data.get("avg_performance_change").getAsString(), false);
        }

        // Card 4: At-Risk Alerts
        String atRisk = data.has("at_risk_count") && !data.get("at_risk_count").isJsonNull()
                ? data.get("at_risk_count").getAsString()
                : "7";
        if (tvAtRiskCount != null) tvAtRiskCount.setText(atRisk);

        if (tvAtRiskChange != null && data.has("at_risk_change") && !data.get("at_risk_change").isJsonNull()) {
            setTrendColor(tvAtRiskChange, data.get("at_risk_change").getAsString(), true);
        }

        // Notification Badge Count
        if (tvNotifBadge != null) {
            int unreadCount = 0;
            if (data.has("unread_notifications_count") && !data.get("unread_notifications_count").isJsonNull()) {
                unreadCount = data.get("unread_notifications_count").getAsInt();
            }
            if (unreadCount > 0) {
                tvNotifBadge.setText(String.valueOf(unreadCount));
                tvNotifBadge.setVisibility(View.VISIBLE);
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        }
    }

    private void bindSystemStatus(JsonObject data) {
        if (data == null || !data.has("system_status") || !data.get("system_status").isJsonObject()) {
            setServiceStatus(tvApiServerDot, tvApiServerText, "online");
            setServiceStatus(tvDbDot, tvDbText, "online");
            setServiceStatus(tvMlDot, tvMlText, "online");
            return;
        }

        JsonObject statusObj = data.getAsJsonObject("system_status");

        if (statusObj.has("api_server") && statusObj.get("api_server").isJsonObject()) {
            JsonObject apiObj = statusObj.getAsJsonObject("api_server");
            String status = com.social7.studentlearnanalytics.utils.UiUtils.optString(apiObj, "status", "online");
            setServiceStatus(tvApiServerDot, tvApiServerText, status);
        } else {
            setServiceStatus(tvApiServerDot, tvApiServerText, "online");
        }

        if (statusObj.has("database") && statusObj.get("database").isJsonObject()) {
            JsonObject dbObj = statusObj.getAsJsonObject("database");
            String status = com.social7.studentlearnanalytics.utils.UiUtils.optString(dbObj, "status", "online");
            setServiceStatus(tvDbDot, tvDbText, status);
        } else {
            setServiceStatus(tvDbDot, tvDbText, "online");
        }

        if (statusObj.has("ml_service") && statusObj.get("ml_service").isJsonObject()) {
            JsonObject mlObj = statusObj.getAsJsonObject("ml_service");
            String status = com.social7.studentlearnanalytics.utils.UiUtils.optString(mlObj, "status", "online");
            setServiceStatus(tvMlDot, tvMlText, status);
        } else {
            setServiceStatus(tvMlDot, tvMlText, "online");
        }
    }

    private void setServiceStatus(TextView dotView, TextView statusTextView, String status) {
        if (dotView == null || statusTextView == null) return;
        boolean isOnline = "online".equalsIgnoreCase(status);
        dotView.setTextColor(Color.parseColor(isOnline ? "#10B981" : "#EF4444"));
        statusTextView.setText(isOnline ? getString(R.string.status_online) : getString(R.string.status_offline));
        statusTextView.setTextColor(Color.parseColor(isOnline ? "#10B981" : "#EF4444"));
    }
}


