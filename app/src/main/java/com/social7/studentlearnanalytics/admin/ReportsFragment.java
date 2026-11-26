package com.social7.studentlearnanalytics.admin;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.reports.CourseReportListAdapter;
import com.social7.studentlearnanalytics.admin.reports.GenerateReportActivity;
import com.social7.studentlearnanalytics.admin.reports.StudentReportListAdapter;
import com.social7.studentlearnanalytics.utils.UiUtils;
import com.social7.studentlearnanalytics.admin.reports.TeacherAssignmentReportAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherPerformanceAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherReportAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherWorkloadAdapter;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    // Primary Navigation Buttons
    private MaterialButton btnTabOverview, btnTabStudentsReport, btnTabTeachersReport, btnTabCoursesReport;

    // Primary Containers
    private View layoutOverviewContainer, layoutStudentsReportContainer, layoutTeachersReportContainer, layoutCoursesReportContainer;
    private TextView tvOverviewTotalStudents, tvOverviewTotalTeachers, tvOverviewTotalCourses, tvOverviewTotalUsers;
    private TextView tvStatTotalStudents, tvStatActiveStudents, tvStatNewEnrollments, tvStatGraduated;
    private TextView tvStudentEnrollMetricTotal, tvStudentEnrollMetricNew, tvStudentEnrollMetricReturning, tvStudentEnrollMetricGradRate;
    private android.widget.LinearLayout llTopEnrolledCoursesContainer;
    private TextView tvAttOverallRate, tvAttPresentCount, tvAttAbsentCount, tvAttLateCount;
    private TextView tvStatTotalTeachers, tvStatActiveTeachers, tvStatAssignedCourses, tvStatPendingReviews;
    private TextView tvStatTotalCourses, tvStatActiveCourses, tvStatTotalEnrollments, tvStatAvgClassSize;
    private TextView tvCourseEnrollStatTotal, tvCourseEnrollStatStudents, tvCourseEnrollStatCohorts, tvCourseEnrollStatUtilization;
    private TextView tvCourseCompletionRate, tvCourseCompletedCount, tvCourseIncompleteCount;

    // ==========================================
    // STUDENTS SECTION VIEWS & ADAPTERS
    // ==========================================
    private TextView tabStudentOverview, tabStudentEnrollment, tabStudentPerformance, tabStudentAttendance;
    private View secStudentOverviewContainer, secStudentEnrollmentContainer, secStudentPerformanceContainer, secStudentAttendanceContainer;
    private Spinner spStudentReportYear, spStudentReportSemester;
    private LineChart chartEnrollmentTrend, chartStudentAttendanceTrendLine;
    private PieChart chartStudentDistribution;
    private BarChart chartStudentEnrollmentBar, chartStudentPerformanceGpaBar;
    private RecyclerView rvStudentEnrollmentList, rvStudentPerformanceList, rvStudentAttendanceList;
    private StudentReportListAdapter studentEnrollmentAdapter, studentPerformanceAdapter, studentAttendanceAdapter;

    // ==========================================
    // TEACHERS SECTION VIEWS & ADAPTERS
    // ==========================================
    private TextView tabTeacherOverview, tabTeacherWorkload, tabTeacherAssignments, tabTeacherPerformance;
    private View secTeacherOverviewContainer, secTeacherWorkloadContainer, secTeacherAssignmentsContainer, secTeacherPerformanceContainer;
    private Spinner spTeacherReportYear, spTeacherReportSemester;
    private BarChart chartTeacherWorkloadBar, chartTeacherWorkloadBarChart, chartTeacherPerformanceBar;
    private RecyclerView rvTeacherReport, rvTeacherWorkloadList, rvTeacherAssignmentsList, rvTeacherPerformanceList;
    private TeacherReportAdapter teacherOverviewAdapter;
    private TeacherWorkloadAdapter teacherWorkloadAdapter;
    private TeacherAssignmentReportAdapter teacherAssignmentAdapter;
    private TeacherPerformanceAdapter teacherPerformanceAdapter;

    // ==========================================
    // COURSES SECTION VIEWS & ADAPTERS
    // ==========================================
    private TextView tabCourseOverview, tabCourseEnrollment, tabCourseCompletion, tabCoursePerformance;
    private View secCourseOverviewContainer, secCourseEnrollmentContainer, secCourseCompletionContainer, secCoursePerformanceContainer;
    private Spinner spCourseReportYear, spCourseReportSemester;
    private BarChart chartCourseTrendBar, chartCoursePerformanceBar;
    private LineChart chartCourseEnrollmentLine;
    private RecyclerView rvCourseEnrollmentList, rvCourseCompletionList, rvCoursePerformanceList;
    private CourseReportListAdapter courseEnrollmentAdapter, courseCompletionAdapter, coursePerformanceAdapter;

    // Current State Flags
    private int selectedPrimaryTab = 0; // 0=Overview, 1=Students, 2=Teachers, 3=Courses
    private JsonObject cachedOverviewData = null;
    private JsonObject cachedAttendanceData = null;
    private JsonObject cachedTeachersData = null;
    private JsonObject cachedCoursesData = null;
    private final List<String> apiYearLabels = new ArrayList<>();
    private final List<Integer> apiYearIds = new ArrayList<>();
    private final List<String> apiSemLabels = new ArrayList<>();
    private final List<Integer> apiSemIds = new ArrayList<>();
    private Integer selectedStudentAyId = null, selectedStudentSemId = null;
    private Integer selectedTeacherAyId = null, selectedTeacherSemId = null;
    private Integer selectedCourseAyId = null, selectedCourseSemId = null;

    public ReportsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_admin_reports, container, false);

        // Header Menu Icon
        View btnMenu = view.findViewById(R.id.btnReportsMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        // Header Titles
        TextView tvTitle = view.findViewById(R.id.tvHeaderTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvHeaderSubtitle);
        if (tvTitle != null) tvTitle.setText(R.string.reports_analytics);
        if (tvSubtitle != null) tvSubtitle.setText(getString(R.string.view_analyze_export_reports));

        // Primary Tab Buttons
        btnTabOverview = view.findViewById(R.id.btnTabOverview);
        btnTabStudentsReport = view.findViewById(R.id.btnTabStudentsReport);
        btnTabTeachersReport = view.findViewById(R.id.btnTabTeachersReport);
        btnTabCoursesReport = view.findViewById(R.id.btnTabCoursesReport);

        // Primary Containers
        layoutOverviewContainer = view.findViewById(R.id.layoutOverviewContainer);
        layoutStudentsReportContainer = view.findViewById(R.id.layoutStudentsReportContainer);
        layoutTeachersReportContainer = view.findViewById(R.id.layoutTeachersReportContainer);
        layoutCoursesReportContainer = view.findViewById(R.id.layoutCoursesReportContainer);

        tvOverviewTotalStudents = view.findViewById(R.id.tvOverviewTotalStudents);
        tvOverviewTotalTeachers = view.findViewById(R.id.tvOverviewTotalTeachers);
        tvOverviewTotalCourses = view.findViewById(R.id.tvOverviewTotalCourses);
        tvOverviewTotalUsers = view.findViewById(R.id.tvOverviewTotalUsers);
        tvStatTotalStudents = view.findViewById(R.id.tvStatTotalStudents);
        tvStatActiveStudents = view.findViewById(R.id.tvStatActiveStudents);
        tvStatNewEnrollments = view.findViewById(R.id.tvStatNewEnrollments);
        tvStatGraduated = view.findViewById(R.id.tvStatGraduated);
        tvStudentEnrollMetricTotal = view.findViewById(R.id.tvStudentEnrollMetricTotal);
        tvStudentEnrollMetricNew = view.findViewById(R.id.tvStudentEnrollMetricNew);
        tvStudentEnrollMetricReturning = view.findViewById(R.id.tvStudentEnrollMetricReturning);
        tvStudentEnrollMetricGradRate = view.findViewById(R.id.tvStudentEnrollMetricGradRate);
        llTopEnrolledCoursesContainer = view.findViewById(R.id.llTopEnrolledCoursesContainer);
        tvStatTotalTeachers = view.findViewById(R.id.tvStatTotalTeachers);
        tvStatActiveTeachers = view.findViewById(R.id.tvStatActiveTeachers);
        tvStatAssignedCourses = view.findViewById(R.id.tvStatAssignedCourses);
        tvStatPendingReviews = view.findViewById(R.id.tvStatPendingReviews);
        tvStatTotalCourses = view.findViewById(R.id.tvStatTotalCourses);
        tvStatActiveCourses = view.findViewById(R.id.tvStatActiveCourses);
        tvStatTotalEnrollments = view.findViewById(R.id.tvStatTotalEnrollments);
        tvStatAvgClassSize = view.findViewById(R.id.tvStatAvgClassSize);
        tvCourseEnrollStatTotal = view.findViewById(R.id.tvCourseEnrollStatTotal);
        tvCourseEnrollStatStudents = view.findViewById(R.id.tvCourseEnrollStatStudents);
        tvCourseEnrollStatCohorts = view.findViewById(R.id.tvCourseEnrollStatCohorts);
        tvCourseEnrollStatUtilization = view.findViewById(R.id.tvCourseEnrollStatUtilization);
        tvCourseCompletionRate = view.findViewById(R.id.tvCourseCompletionRate);
        tvCourseCompletedCount = view.findViewById(R.id.tvCourseCompletedCount);
        tvCourseIncompleteCount = view.findViewById(R.id.tvCourseIncompleteCount);

        setupPrimaryTabs();
        setupOverviewCategoryRows(view);

        // Bind Sub-sections
        bindStudentsSectionViews(view);
        bindTeachersSectionViews(view);
        bindCoursesSectionViews(view);

        selectReportTab(selectedPrimaryTab);
        fetchRealReportLookups();
        fetchRealOverviewAnalytics(null, null);
        fetchRealAttendanceAnalytics(null, null);

        return view;
    }

    private void setupPrimaryTabs() {
        if (btnTabOverview != null) btnTabOverview.setOnClickListener(v -> selectReportTab(0));
        if (btnTabStudentsReport != null) btnTabStudentsReport.setOnClickListener(v -> selectReportTab(1));
        if (btnTabTeachersReport != null) btnTabTeachersReport.setOnClickListener(v -> selectReportTab(2));
        if (btnTabCoursesReport != null) btnTabCoursesReport.setOnClickListener(v -> selectReportTab(3));
    }

    public void selectReportTab(int tabIndex) {
        this.selectedPrimaryTab = tabIndex;
        updatePrimaryTabStyles(tabIndex);

        if (layoutOverviewContainer != null) layoutOverviewContainer.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutStudentsReportContainer != null) layoutStudentsReportContainer.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutTeachersReportContainer != null) layoutTeachersReportContainer.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutCoursesReportContainer != null) layoutCoursesReportContainer.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (tabIndex == 1) {
            selectStudentSubTab(0);
            fetchRealOverviewAnalytics(selectedStudentAyId, selectedStudentSemId);
            fetchRealAttendanceAnalytics(selectedStudentAyId, selectedStudentSemId);
        } else if (tabIndex == 2) {
            selectTeacherSubTab(0);
            fetchRealTeachersAnalytics(selectedTeacherAyId, selectedTeacherSemId);
        } else if (tabIndex == 3) {
            selectCourseSubTab(0);
            fetchRealCoursesAnalytics(selectedCourseAyId, selectedCourseSemId);
        }
    }

    private void updatePrimaryTabStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        MaterialButton[] tabs = new MaterialButton[]{btnTabOverview, btnTabStudentsReport, btnTabTeachersReport, btnTabCoursesReport};
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveBg = Color.parseColor("#F4F7FF");
        int textSecondary = getResources().getColor(R.color.admin_text_secondary, null);

        for (int i = 0; i < tabs.length; i++) {
            MaterialButton btn = tabs[i];
            if (btn == null) continue;
            if (i == activeIndex) {
                btn.setBackgroundTintList(ColorStateList.valueOf(activeColor));
                btn.setTextColor(Color.WHITE);
                UiUtils.scrollToTab(btn);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
                btn.setTextColor(textSecondary);
            }
        }
    }

    private void setupOverviewCategoryRows(View view) {
        View rowStudent = view.findViewById(R.id.rowStudentReports);
        View rowTeacher = view.findViewById(R.id.rowTeacherReports);
        View rowCourse = view.findViewById(R.id.rowCourseReports);
        View rowAcademic = view.findViewById(R.id.rowAcademicReports);

        if (rowStudent != null) rowStudent.setOnClickListener(v -> selectReportTab(1));
        if (rowTeacher != null) rowTeacher.setOnClickListener(v -> selectReportTab(2));
        if (rowCourse != null) rowCourse.setOnClickListener(v -> selectReportTab(3));
        if (rowAcademic != null) {
            rowAcademic.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    String msg = "Loading real-time university analytics...";
                    if (cachedOverviewData != null && cachedOverviewData.has("metrics") && cachedOverviewData.get("metrics").isJsonObject()) {
                        JsonObject m = cachedOverviewData.getAsJsonObject("metrics");
                        int totalStu = UiUtils.optInt(m, "total_students", 0);
                        int activeStu = UiUtils.optInt(m, "active_students", 0);
                        int totalCourses = UiUtils.optInt(m, "total_courses", 0);
                        int totalTeachers = UiUtils.optInt(m, "total_teachers", 0);
                        float avgGpa = UiUtils.optFloat(m, "average_gpa", 0f);
                        float attRate = UiUtils.optFloat(m, "attendance_rate", 0f);
                        msg = String.format(Locale.US,
                                "• Total Students: %d (%d Active)\n" +
                                "• Total Teachers: %d\n" +
                                "• Total Courses: %d\n" +
                                "• Average University GPA: %s / 4.0\n" +
                                "• Overall Attendance Rate: %s",
                                totalStu, activeStu, totalTeachers, totalCourses,
                                avgGpa > 0f ? String.format(Locale.US, "%.2f", avgGpa) : "N/A",
                                attRate > 0f ? String.format(Locale.US, "%.1f%%", attRate) : "N/A");
                    }
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.academic_performance_report)
                            .setMessage(msg)
                            .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                            .setNeutralButton(R.string.reports_analytics, (d, w) -> {
                                Intent i = new Intent(getContext(), GenerateReportActivity.class);
                                i.putExtra("selected_report_type", "Academic");
                                startActivity(i);
                            })
                            .show();
            });
        }
    }

    // =========================================================================
    // 1. STUDENTS SECTION IMPLEMENTATION (Overview, Enrollment, Performance, Attendance)
    // =========================================================================
    private void bindStudentsSectionViews(View root) {
        View layout = root.findViewById(R.id.layoutStudentsReportContainer);
        if (layout == null) return;

        tabStudentOverview = layout.findViewById(R.id.tabStudentOverview);
        tabStudentEnrollment = layout.findViewById(R.id.tabStudentEnrollment);
        tabStudentPerformance = layout.findViewById(R.id.tabStudentPerformance);
        tabStudentAttendance = layout.findViewById(R.id.tabStudentAttendance);

        secStudentOverviewContainer = layout.findViewById(R.id.secStudentOverviewContainer);
        secStudentEnrollmentContainer = layout.findViewById(R.id.secStudentEnrollmentContainer);
        secStudentPerformanceContainer = layout.findViewById(R.id.secStudentPerformanceContainer);
        secStudentAttendanceContainer = layout.findViewById(R.id.secStudentAttendanceContainer);

        spStudentReportYear = layout.findViewById(R.id.spStudentReportYear);
        spStudentReportSemester = layout.findViewById(R.id.spStudentReportSemester);
        chartEnrollmentTrend = layout.findViewById(R.id.chartEnrollmentTrend);
        chartStudentDistribution = layout.findViewById(R.id.chartStudentDistribution);
        chartStudentEnrollmentBar = layout.findViewById(R.id.chartStudentEnrollmentBar);
        chartStudentPerformanceGpaBar = layout.findViewById(R.id.chartStudentPerformanceGpaBar);
        chartStudentAttendanceTrendLine = layout.findViewById(R.id.chartStudentAttendanceTrendLine);

        rvStudentEnrollmentList = layout.findViewById(R.id.rvStudentEnrollmentList);
        rvStudentPerformanceList = layout.findViewById(R.id.rvStudentPerformanceList);
        rvStudentAttendanceList = layout.findViewById(R.id.rvStudentAttendanceList);
        tvAttOverallRate = layout.findViewById(R.id.tvAttOverallRate);
        tvAttPresentCount = layout.findViewById(R.id.tvAttPresentCount);
        tvAttAbsentCount = layout.findViewById(R.id.tvAttAbsentCount);
        tvAttLateCount = layout.findViewById(R.id.tvAttLateCount);

        setupFilters(spStudentReportYear, spStudentReportSemester);

        if (tabStudentOverview != null) tabStudentOverview.setOnClickListener(v -> selectStudentSubTab(0));
        if (tabStudentEnrollment != null) tabStudentEnrollment.setOnClickListener(v -> selectStudentSubTab(1));
        if (tabStudentPerformance != null) tabStudentPerformance.setOnClickListener(v -> selectStudentSubTab(2));
        if (tabStudentAttendance != null) tabStudentAttendance.setOnClickListener(v -> selectStudentSubTab(3));

        MaterialButton btnExport = layout.findViewById(R.id.btnExportStudentReport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Students");
                startActivity(intent);
            });
        }
    }

    private void selectStudentSubTab(int subIndex) {
        updateSecondarySubTabStyles(
                new TextView[]{tabStudentOverview, tabStudentEnrollment, tabStudentPerformance, tabStudentAttendance},
                subIndex
        );

        if (secStudentOverviewContainer != null) secStudentOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secStudentEnrollmentContainer != null) secStudentEnrollmentContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secStudentPerformanceContainer != null) secStudentPerformanceContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secStudentAttendanceContainer != null) secStudentAttendanceContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupStudentOverviewCharts();
        } else if (subIndex == 1) {
            setupStudentEnrollmentChartsAndList();
        } else if (subIndex == 2) {
            setupStudentPerformanceChartsAndList();
        } else if (subIndex == 3) {
            setupStudentAttendanceChartsAndList();
        }
    }

    private void setupStudentOverviewCharts() {
        if (chartEnrollmentTrend != null) {
            XAxis xAxis = chartEnrollmentTrend.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartEnrollmentTrend.getAxisRight().setEnabled(false);
            chartEnrollmentTrend.getDescription().setEnabled(false);
            chartEnrollmentTrend.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartEnrollmentTrend.getContext(), chartEnrollmentTrend);
        }

        if (chartStudentDistribution != null) {
            chartStudentDistribution.setHoleRadius(50f);
            chartStudentDistribution.setCenterText("--\nStudents");
            chartStudentDistribution.setCenterTextSize(14f);
            chartStudentDistribution.getDescription().setEnabled(false);
            chartStudentDistribution.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartStudentDistribution.getContext(), chartStudentDistribution);
        }
    }

    private void setupStudentEnrollmentChartsAndList() {
        if (chartStudentEnrollmentBar != null) {
            XAxis xAxis = chartStudentEnrollmentBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentEnrollmentBar.getAxisRight().setEnabled(false);
            chartStudentEnrollmentBar.getDescription().setEnabled(false);
            chartStudentEnrollmentBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartStudentEnrollmentBar.getContext(), chartStudentEnrollmentBar);
        }
        if (cachedOverviewData != null) {
            bindOverviewToStudentViews(cachedOverviewData);
        }
    }

    private void setupStudentPerformanceChartsAndList() {
        if (chartStudentPerformanceGpaBar != null) {
            XAxis xAxis = chartStudentPerformanceGpaBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentPerformanceGpaBar.getAxisRight().setEnabled(false);
            chartStudentPerformanceGpaBar.getDescription().setEnabled(false);
            chartStudentPerformanceGpaBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartStudentPerformanceGpaBar.getContext(), chartStudentPerformanceGpaBar);
        }
        if (cachedOverviewData != null) {
            bindOverviewToStudentViews(cachedOverviewData);
        }
    }

    private void setupStudentAttendanceChartsAndList() {
        if (chartStudentAttendanceTrendLine != null) {
            XAxis xAxis = chartStudentAttendanceTrendLine.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentAttendanceTrendLine.getAxisRight().setEnabled(false);
            chartStudentAttendanceTrendLine.getDescription().setEnabled(false);
            chartStudentAttendanceTrendLine.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartStudentAttendanceTrendLine.getContext(), chartStudentAttendanceTrendLine);
        }
        if (cachedAttendanceData != null) {
            bindAttendanceToViews(cachedAttendanceData);
        }
    }

    // =========================================================================
    // 2. TEACHERS SECTION IMPLEMENTATION (Overview, Workload, Assignments, Performance)
    // =========================================================================
    private void bindTeachersSectionViews(View root) {
        View layout = root.findViewById(R.id.layoutTeachersReportContainer);
        if (layout == null) return;

        tabTeacherOverview = layout.findViewById(R.id.tabTeacherOverview);
        tabTeacherWorkload = layout.findViewById(R.id.tabTeacherWorkload);
        tabTeacherAssignments = layout.findViewById(R.id.tabTeacherAssignments);
        tabTeacherPerformance = layout.findViewById(R.id.tabTeacherPerformance);

        secTeacherOverviewContainer = layout.findViewById(R.id.secTeacherOverviewContainer);
        secTeacherWorkloadContainer = layout.findViewById(R.id.secTeacherWorkloadContainer);
        secTeacherAssignmentsContainer = layout.findViewById(R.id.secTeacherAssignmentsContainer);
        secTeacherPerformanceContainer = layout.findViewById(R.id.secTeacherPerformanceContainer);

        spTeacherReportYear = layout.findViewById(R.id.spTeacherReportYear);
        spTeacherReportSemester = layout.findViewById(R.id.spTeacherReportSemester);
        chartTeacherWorkloadBar = layout.findViewById(R.id.chartTeacherWorkloadBar);
        chartTeacherWorkloadBarChart = layout.findViewById(R.id.chartTeacherWorkloadBarChart);
        chartTeacherPerformanceBar = layout.findViewById(R.id.chartTeacherPerformanceBar);

        rvTeacherReport = layout.findViewById(R.id.rvTeacherReport);
        rvTeacherWorkloadList = layout.findViewById(R.id.rvTeacherWorkloadList);
        rvTeacherAssignmentsList = layout.findViewById(R.id.rvTeacherAssignmentsList);
        rvTeacherPerformanceList = layout.findViewById(R.id.rvTeacherPerformanceList);

        setupFilters(spTeacherReportYear, spTeacherReportSemester);

        if (tabTeacherOverview != null) tabTeacherOverview.setOnClickListener(v -> selectTeacherSubTab(0));
        if (tabTeacherWorkload != null) tabTeacherWorkload.setOnClickListener(v -> selectTeacherSubTab(1));
        if (tabTeacherAssignments != null) tabTeacherAssignments.setOnClickListener(v -> selectTeacherSubTab(2));
        if (tabTeacherPerformance != null) tabTeacherPerformance.setOnClickListener(v -> selectTeacherSubTab(3));

        MaterialButton btnExport = layout.findViewById(R.id.btnExportTeacherReport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Teachers");
                startActivity(intent);
            });
        }
    }

    private void selectTeacherSubTab(int subIndex) {
        updateSecondarySubTabStyles(
                new TextView[]{tabTeacherOverview, tabTeacherWorkload, tabTeacherAssignments, tabTeacherPerformance},
                subIndex
        );

        if (secTeacherOverviewContainer != null) secTeacherOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secTeacherWorkloadContainer != null) secTeacherWorkloadContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secTeacherAssignmentsContainer != null) secTeacherAssignmentsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secTeacherPerformanceContainer != null) secTeacherPerformanceContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupTeacherOverviewChartsAndList();
        } else if (subIndex == 1) {
            setupTeacherWorkloadChartsAndList();
        } else if (subIndex == 2) {
            setupTeacherAssignmentsList();
        } else if (subIndex == 3) {
            setupTeacherPerformanceChartsAndList();
        }
    }

    private void setupTeacherOverviewChartsAndList() {
        if (chartTeacherWorkloadBar != null) {
            XAxis xAxis = chartTeacherWorkloadBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherWorkloadBar.getAxisRight().setEnabled(false);
            chartTeacherWorkloadBar.getDescription().setEnabled(false);
            chartTeacherWorkloadBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartTeacherWorkloadBar.getContext(), chartTeacherWorkloadBar);
        }
        if (cachedTeachersData != null) bindTeachersToViews(cachedTeachersData);
    }

    private void setupTeacherWorkloadChartsAndList() {
        if (chartTeacherWorkloadBarChart != null) {
            XAxis xAxis = chartTeacherWorkloadBarChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherWorkloadBarChart.getAxisRight().setEnabled(false);
            chartTeacherWorkloadBarChart.getDescription().setEnabled(false);
            chartTeacherWorkloadBarChart.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartTeacherWorkloadBarChart.getContext(), chartTeacherWorkloadBarChart);
        }
        if (cachedTeachersData != null) bindTeachersToViews(cachedTeachersData);
    }

    private void setupTeacherAssignmentsList() {
        if (cachedTeachersData != null) bindTeachersToViews(cachedTeachersData);
    }

    private void setupTeacherPerformanceChartsAndList() {
        if (chartTeacherPerformanceBar != null) {
            XAxis xAxis = chartTeacherPerformanceBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherPerformanceBar.getAxisRight().setEnabled(false);
            chartTeacherPerformanceBar.getDescription().setEnabled(false);
            chartTeacherPerformanceBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartTeacherPerformanceBar.getContext(), chartTeacherPerformanceBar);
        }
        if (cachedTeachersData != null) bindTeachersToViews(cachedTeachersData);
    }

    // =========================================================================
    // 3. COURSES SECTION IMPLEMENTATION (Overview, Enrollment, Completion, Performance)
    // =========================================================================
    private void bindCoursesSectionViews(View root) {
        View layout = root.findViewById(R.id.layoutCoursesReportContainer);
        if (layout == null) return;

        tabCourseOverview = layout.findViewById(R.id.tabCourseOverview);
        tabCourseEnrollment = layout.findViewById(R.id.tabCourseEnrollment);
        tabCourseCompletion = layout.findViewById(R.id.tabCourseCompletion);
        tabCoursePerformance = layout.findViewById(R.id.tabCoursePerformance);

        secCourseOverviewContainer = layout.findViewById(R.id.secCourseOverviewContainer);
        secCourseEnrollmentContainer = layout.findViewById(R.id.secCourseEnrollmentContainer);
        secCourseCompletionContainer = layout.findViewById(R.id.secCourseCompletionContainer);
        secCoursePerformanceContainer = layout.findViewById(R.id.secCoursePerformanceContainer);

        spCourseReportYear = layout.findViewById(R.id.spCourseReportYear);
        spCourseReportSemester = layout.findViewById(R.id.spCourseReportSemester);
        chartCourseTrendBar = layout.findViewById(R.id.chartCourseTrendBar);
        chartCourseEnrollmentLine = layout.findViewById(R.id.chartCourseEnrollmentLine);
        chartCoursePerformanceBar = layout.findViewById(R.id.chartCoursePerformanceBar);

        rvCourseEnrollmentList = layout.findViewById(R.id.rvCourseEnrollmentList);
        rvCourseCompletionList = layout.findViewById(R.id.rvCourseCompletionList);
        rvCoursePerformanceList = layout.findViewById(R.id.rvCoursePerformanceList);

        setupFilters(spCourseReportYear, spCourseReportSemester);

        if (tabCourseOverview != null) tabCourseOverview.setOnClickListener(v -> selectCourseSubTab(0));
        if (tabCourseEnrollment != null) tabCourseEnrollment.setOnClickListener(v -> selectCourseSubTab(1));
        if (tabCourseCompletion != null) tabCourseCompletion.setOnClickListener(v -> selectCourseSubTab(2));
        if (tabCoursePerformance != null) tabCoursePerformance.setOnClickListener(v -> selectCourseSubTab(3));

        MaterialButton btnExport = layout.findViewById(R.id.btnExportCourseReport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Courses");
                startActivity(intent);
            });
        }
    }

    private void selectCourseSubTab(int subIndex) {
        updateSecondarySubTabStyles(
                new TextView[]{tabCourseOverview, tabCourseEnrollment, tabCourseCompletion, tabCoursePerformance},
                subIndex
        );

        if (secCourseOverviewContainer != null) secCourseOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secCourseEnrollmentContainer != null) secCourseEnrollmentContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secCourseCompletionContainer != null) secCourseCompletionContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secCoursePerformanceContainer != null) secCoursePerformanceContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupCourseOverviewCharts();
        } else if (subIndex == 1) {
            setupCourseEnrollmentChartsAndList();
        } else if (subIndex == 2) {
            setupCourseCompletionList();
        } else if (subIndex == 3) {
            setupCoursePerformanceChartsAndList();
        }
    }

    private void setupCourseOverviewCharts() {
        if (chartCourseTrendBar != null) {
            XAxis xAxis = chartCourseTrendBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCourseTrendBar.getAxisRight().setEnabled(false);
            chartCourseTrendBar.getDescription().setEnabled(false);
            chartCourseTrendBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartCourseTrendBar.getContext(), chartCourseTrendBar);
        }
    }

    private void setupCourseEnrollmentChartsAndList() {
        if (chartCourseEnrollmentLine != null) {
            XAxis xAxis = chartCourseEnrollmentLine.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCourseEnrollmentLine.getAxisRight().setEnabled(false);
            chartCourseEnrollmentLine.getDescription().setEnabled(false);
            chartCourseEnrollmentLine.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartCourseEnrollmentLine.getContext(), chartCourseEnrollmentLine);
        }
        if (cachedCoursesData != null) bindCoursesToViews(cachedCoursesData);
    }

    private void setupCourseCompletionList() {
        if (cachedCoursesData != null) bindCoursesToViews(cachedCoursesData);
    }

    private void setupCoursePerformanceChartsAndList() {
        if (chartCoursePerformanceBar != null) {
            XAxis xAxis = chartCoursePerformanceBar.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCoursePerformanceBar.getAxisRight().setEnabled(false);
            chartCoursePerformanceBar.getDescription().setEnabled(false);
            chartCoursePerformanceBar.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartCoursePerformanceBar.getContext(), chartCoursePerformanceBar);
        }
        if (cachedCoursesData != null) bindCoursesToViews(cachedCoursesData);
    }

    // Helper method to toggle sub-tab styles
    private void updateSecondarySubTabStyles(TextView[] subTabs, int activeIndex) {
        if (!isAdded() || getContext() == null) return;
        for (int i = 0; i < subTabs.length; i++) {
            TextView tv = subTabs[i];
            if (tv == null) continue;
            if (i == activeIndex) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void setupFilters(Spinner spYear, Spinner spSem) {
        if (!isAdded() || getContext() == null) return;
        String[] years = apiYearLabels.isEmpty()
                ? new String[]{"All Academic Years"}
                : apiYearLabels.toArray(new String[0]);
        String[] semesters = apiSemLabels.isEmpty()
                ? new String[]{"All Semesters"}
                : apiSemLabels.toArray(new String[0]);

        setSpinnerAdapter(spYear, years);
        setSpinnerAdapter(spSem, semesters);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null || !isAdded() || getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void fetchRealReportLookups() {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getReportsAnalyticsLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");
                apiYearLabels.clear();
                apiYearIds.clear();
                apiYearLabels.add("All Academic Years");
                apiYearIds.add(null);
                if (data.has("academic_years") && data.get("academic_years").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("academic_years")) {
                        if (el.isJsonObject()) {
                            JsonObject obj = el.getAsJsonObject();
                            apiYearLabels.add(UiUtils.optString(obj, "name", ""));
                            apiYearIds.add(UiUtils.optInt(obj, "id", 0));
                        }
                    }
                }
                apiSemLabels.clear();
                apiSemIds.clear();
                apiSemLabels.add("All Semesters");
                apiSemIds.add(null);
                if (data.has("semesters") && data.get("semesters").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("semesters")) {
                        if (el.isJsonObject()) {
                            JsonObject obj = el.getAsJsonObject();
                            apiSemLabels.add(UiUtils.optString(obj, "name", ""));
                            apiSemIds.add(UiUtils.optInt(obj, "id", 0));
                        }
                    }
                }
                setupFilters(spStudentReportYear, spStudentReportSemester);
                setupFilters(spTeacherReportYear, spTeacherReportSemester);
                setupFilters(spCourseReportYear, spCourseReportSemester);
                attachFilterListeners(spStudentReportYear, spStudentReportSemester, 0);
                attachFilterListeners(spTeacherReportYear, spTeacherReportSemester, 1);
                attachFilterListeners(spCourseReportYear, spCourseReportSemester, 2);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void attachFilterListeners(Spinner spYear, Spinner spSem, int section) {
        if (spYear == null && spSem == null) return;
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            private boolean firstCall = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                    return;
                }
                Integer ayId = (spYear != null && spYear.getSelectedItemPosition() >= 0 && spYear.getSelectedItemPosition() < apiYearIds.size())
                        ? apiYearIds.get(spYear.getSelectedItemPosition()) : null;
                Integer semId = (spSem != null && spSem.getSelectedItemPosition() >= 0 && spSem.getSelectedItemPosition() < apiSemIds.size())
                        ? apiSemIds.get(spSem.getSelectedItemPosition()) : null;
                if (section == 0) {
                    selectedStudentAyId = ayId;
                    selectedStudentSemId = semId;
                    fetchRealOverviewAnalytics(ayId, semId);
                    fetchRealAttendanceAnalytics(ayId, semId);
                } else if (section == 1) {
                    selectedTeacherAyId = ayId;
                    selectedTeacherSemId = semId;
                    fetchRealTeachersAnalytics(ayId, semId);
                } else if (section == 2) {
                    selectedCourseAyId = ayId;
                    selectedCourseSemId = semId;
                    fetchRealCoursesAnalytics(ayId, semId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        if (spYear != null) spYear.setOnItemSelectedListener(listener);
        if (spSem != null) spSem.setOnItemSelectedListener(listener);
    }

    private void fetchRealOverviewAnalytics(Integer ayId, Integer semId) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getReportsAnalyticsOverview(ayId, semId, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");
                cachedOverviewData = data;
                bindOverviewToStudentViews(data);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindOverviewToStudentViews(JsonObject data) {
        if (data == null || !isAdded() || getContext() == null) return;

        if (data.has("metrics") && data.get("metrics").isJsonObject()) {
            JsonObject m = data.getAsJsonObject("metrics");
            String stuCount = String.valueOf(UiUtils.optInt(m, "total_students", 0));
            String activeStu = String.valueOf(UiUtils.optInt(m, "active_students", 0));
            String teacherCount = String.valueOf(UiUtils.optInt(m, "total_teachers", 0));
            String courseCount = String.valueOf(UiUtils.optInt(m, "total_courses", 0));
            String userCount = String.valueOf(UiUtils.optInt(m, "total_users", 0));

            if (tvOverviewTotalStudents != null) tvOverviewTotalStudents.setText(stuCount);
            if (tvOverviewTotalTeachers != null) tvOverviewTotalTeachers.setText(teacherCount);
            if (tvOverviewTotalCourses != null) tvOverviewTotalCourses.setText(courseCount);
            if (tvOverviewTotalUsers != null) tvOverviewTotalUsers.setText(userCount);
            int totalStuInt = UiUtils.optInt(m, "total_students", 0);
            int activeStuInt = UiUtils.optInt(m, "active_students", 0);
            int graduatedStuInt = UiUtils.optInt(m, "graduated_students", 0);
            if (tvStatTotalStudents != null) tvStatTotalStudents.setText(stuCount);
            if (tvStatActiveStudents != null) tvStatActiveStudents.setText(activeStu);
            if (tvStatNewEnrollments != null) tvStatNewEnrollments.setText(activeStu);
            if (tvStatGraduated != null) tvStatGraduated.setText(String.valueOf(graduatedStuInt));
            if (tvStudentEnrollMetricTotal != null) tvStudentEnrollMetricTotal.setText(stuCount);
            if (tvStudentEnrollMetricNew != null) tvStudentEnrollMetricNew.setText(activeStu);
            if (tvStudentEnrollMetricReturning != null) tvStudentEnrollMetricReturning.setText(String.valueOf(graduatedStuInt));
            if (tvStudentEnrollMetricGradRate != null) {
                tvStudentEnrollMetricGradRate.setText(totalStuInt > 0
                        ? String.format(Locale.US, "%.1f%%", (activeStuInt * 100.0) / totalStuInt) : "--");
            }
            if (chartStudentDistribution != null) {
                chartStudentDistribution.setCenterText(stuCount + "\nStudents");
            }
        }

        if (data.has("enrollment_trend") && data.get("enrollment_trend").isJsonArray()) {
            JsonArray trend = data.getAsJsonArray("enrollment_trend");
            List<Entry> entries = new ArrayList<>();
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < trend.size(); i++) {
                JsonObject item = trend.get(i).getAsJsonObject();
                float cnt = UiUtils.optFloat(item, "count", 0f);
                entries.add(new Entry(i, cnt));
                barEntries.add(new BarEntry(i, cnt));
                labels.add(UiUtils.optString(item, "label", ""));
            }
            if (chartEnrollmentTrend != null) {
                if (!entries.isEmpty()) {
                    LineDataSet ds = new LineDataSet(entries, getString(R.string.enrollment));
                    ds.setColor(Color.parseColor("#3155F5"));
                    ds.setLineWidth(3f);
                    ds.setCircleColor(Color.parseColor("#3155F5"));
                    ds.setDrawValues(false);
                    chartEnrollmentTrend.setData(new LineData(ds));
                    chartEnrollmentTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
                } else {
                    chartEnrollmentTrend.clear();
                }
                chartEnrollmentTrend.invalidate();
            }
            if (chartStudentEnrollmentBar != null) {
                if (!barEntries.isEmpty()) {
                    BarDataSet ds = new BarDataSet(barEntries, getString(R.string.enrollments));
                    ds.setColor(Color.parseColor("#3155F5"));
                    ds.setDrawValues(false);
                    BarData bd = new BarData(ds);
                    bd.setBarWidth(0.5f);
                    chartStudentEnrollmentBar.setData(bd);
                    chartStudentEnrollmentBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
                } else {
                    chartStudentEnrollmentBar.clear();
                }
                chartStudentEnrollmentBar.invalidate();
            }
        }

        if (chartStudentDistribution != null && data.has("department_distribution") && data.get("department_distribution").isJsonArray()) {
            JsonArray depts = data.getAsJsonArray("department_distribution");
            List<PieEntry> entries = new ArrayList<>();
            for (JsonElement el : depts) {
                if (!el.isJsonObject()) continue;
                JsonObject item = el.getAsJsonObject();
                float cnt = UiUtils.optFloat(item, "count", 0f);
                if (cnt > 0f) entries.add(new PieEntry(cnt, UiUtils.optString(item, "label", "")));
            }
            if (!entries.isEmpty()) {
                PieDataSet ds = new PieDataSet(entries, "");
                ds.setColors(new int[]{Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#6546F5"), Color.parseColor("#16A34A"), Color.parseColor("#F59E0B")});
                PieData pd = new PieData(ds);
                pd.setDrawValues(false);
                chartStudentDistribution.setData(pd);
            } else {
                chartStudentDistribution.clear();
            }
            chartStudentDistribution.invalidate();
        }

        if (chartStudentPerformanceGpaBar != null && data.has("gpa_distribution") && data.get("gpa_distribution").isJsonArray()) {
            JsonArray buckets = data.getAsJsonArray("gpa_distribution");
            List<BarEntry> gpaEntries = new ArrayList<>();
            List<String> gpaLabels = new ArrayList<>();
            for (int i = 0; i < buckets.size(); i++) {
                JsonObject b = buckets.get(i).getAsJsonObject();
                gpaEntries.add(new BarEntry(i, UiUtils.optFloat(b, "count", 0f)));
                gpaLabels.add(UiUtils.optString(b, "label", ""));
            }
            if (!gpaEntries.isEmpty()) {
                BarDataSet ds = new BarDataSet(gpaEntries, getString(R.string.nav_students));
                ds.setColors(new int[]{Color.parseColor("#10B981"), Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#F59E0B"), Color.parseColor("#EF4444")});
                ds.setDrawValues(false);
                BarData bd = new BarData(ds);
                bd.setBarWidth(0.5f);
                chartStudentPerformanceGpaBar.setData(bd);
                chartStudentPerformanceGpaBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(gpaLabels.toArray(new String[0])));
            } else {
                chartStudentPerformanceGpaBar.clear();
            }
            chartStudentPerformanceGpaBar.invalidate();
        }

        if (rvStudentEnrollmentList != null && data.has("student_enrollment_roster") && data.get("student_enrollment_roster").isJsonArray()) {
            List<StudentReportListAdapter.StudentReportItem> rosterList = new ArrayList<>();
            for (JsonElement el : data.getAsJsonArray("student_enrollment_roster")) {
                if (!el.isJsonObject()) continue;
                JsonObject s = el.getAsJsonObject();
                String code = UiUtils.optString(s, "student_code", "");
                String name = UiUtils.optString(s, "student_name", "");
                String dept = UiUtils.optString(s, "department", "");
                String cls = UiUtils.optString(s, "class_code", "");
                String status = UiUtils.optString(s, "status", "Active");
                int cnt = UiUtils.optInt(s, "enrollment_count", 0);
                rosterList.add(new StudentReportListAdapter.StudentReportItem(
                        code, name,
                        (cls.isEmpty() ? "" : cls + " • ") + dept,
                        cnt + " Course Enrollments",
                        status));
            }
            studentEnrollmentAdapter = new StudentReportListAdapter(getContext(), rosterList, null);
            rvStudentEnrollmentList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentEnrollmentList.setAdapter(studentEnrollmentAdapter);
        }

        if (rvStudentPerformanceList != null && data.has("top_students") && data.get("top_students").isJsonArray()) {
            List<StudentReportListAdapter.StudentReportItem> list = new ArrayList<>();
            for (JsonElement el : data.getAsJsonArray("top_students")) {
                if (!el.isJsonObject()) continue;
                JsonObject s = el.getAsJsonObject();
                String code = UiUtils.optString(s, "student_code", "");
                String name = UiUtils.optString(s, "student_name", "");
                String dept = UiUtils.optString(s, "department", "");
                String cls = UiUtils.optString(s, "class_code", "");
                float gpa = UiUtils.optFloat(s, "gpa", 0f);
                list.add(new StudentReportListAdapter.StudentReportItem(
                        code, name,
                        (cls.isEmpty() ? "" : cls + " • ") + dept,
                        String.format(Locale.US, "GPA: %.2f / 4.0", gpa),
                        gpa >= 3.5f ? "Honors" : "Active"));
            }
            studentPerformanceAdapter = new StudentReportListAdapter(getContext(), list, null);
            rvStudentPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentPerformanceList.setAdapter(studentPerformanceAdapter);
        }
    }

    private void fetchRealAttendanceAnalytics(Integer ayId, Integer semId) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getReportsAnalyticsAttendance(ayId, semId, null, null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");
                cachedAttendanceData = data;
                bindAttendanceToViews(data);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindAttendanceToViews(JsonObject data) {
        if (data == null || !isAdded() || getContext() == null) return;

        if (data.has("summary") && data.get("summary").isJsonObject()) {
            JsonObject s = data.getAsJsonObject("summary");
            float rate = UiUtils.optFloat(s, "overall_rate", -1f);
            int present = UiUtils.optInt(s, "present_count", 0);
            int absent = UiUtils.optInt(s, "absent_count", 0);
            int late = UiUtils.optInt(s, "late_count", 0);
            if (tvAttOverallRate != null) tvAttOverallRate.setText(rate >= 0f ? String.format(Locale.US, "%.1f%%", rate) : "--");
            if (tvAttPresentCount != null) tvAttPresentCount.setText(String.valueOf(present));
            if (tvAttAbsentCount != null) tvAttAbsentCount.setText(String.valueOf(absent));
            if (tvAttLateCount != null) tvAttLateCount.setText(String.valueOf(late));
        }

        if (chartStudentAttendanceTrendLine != null && data.has("monthly_trend") && data.get("monthly_trend").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("monthly_trend");
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject item = arr.get(i).getAsJsonObject();
                entries.add(new Entry(i, UiUtils.optFloat(item, "rate", 0f)));
                labels.add(UiUtils.optString(item, "label", ""));
            }
            if (!entries.isEmpty()) {
                LineDataSet ds = new LineDataSet(entries, getString(R.string.attendance_7266c9));
                ds.setColor(Color.parseColor("#10B981"));
                ds.setLineWidth(3f);
                ds.setCircleColor(Color.parseColor("#10B981"));
                ds.setCircleRadius(4f);
                ds.setDrawValues(false);
                chartStudentAttendanceTrendLine.setData(new LineData(ds));
                chartStudentAttendanceTrendLine.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
            } else {
                chartStudentAttendanceTrendLine.clear();
            }
            chartStudentAttendanceTrendLine.invalidate();
        }

        if (rvStudentAttendanceList != null && data.has("student_attendance_roster") && data.get("student_attendance_roster").isJsonArray()) {
            List<StudentReportListAdapter.StudentReportItem> list = new ArrayList<>();
            for (JsonElement el : data.getAsJsonArray("student_attendance_roster")) {
                if (!el.isJsonObject()) continue;
                JsonObject s = el.getAsJsonObject();
                String code = UiUtils.optString(s, "student_code", "");
                String name = UiUtils.optString(s, "student_name", "");
                String dept = UiUtils.optString(s, "department", "");
                String cls = UiUtils.optString(s, "class_code", "");
                int present = UiUtils.optInt(s, "present_days", 0);
                int late = UiUtils.optInt(s, "late_days", 0);
                int total = UiUtils.optInt(s, "total_days", 0);
                float rate = UiUtils.optFloat(s, "attendance_rate", 0f);
                list.add(new StudentReportListAdapter.StudentReportItem(
                        code, name,
                        (cls.isEmpty() ? "" : cls + " • ") + dept,
                        String.format(Locale.US, "Attendance: %.1f%% (%d/%d Sessions)", rate, present + late, total),
                        rate >= 75f ? "Present" : "At Risk"));
            }
            studentAttendanceAdapter = new StudentReportListAdapter(getContext(), list, null);
            rvStudentAttendanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentAttendanceList.setAdapter(studentAttendanceAdapter);
        }
    }

    private void fetchRealTeachersAnalytics(Integer ayId, Integer semId) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getReportsAnalyticsTeachers(ayId, semId, null, null, null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");
                cachedTeachersData = data;
                bindTeachersToViews(data);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindTeachersToViews(JsonObject data) {
        if (data == null || !isAdded() || getContext() == null) return;
        if (data.has("summary") && data.get("summary").isJsonObject()) {
            JsonObject sum = data.getAsJsonObject("summary");
            String totalT = String.valueOf(UiUtils.optInt(sum, "total_teachers", 0));
            if (tvStatTotalTeachers != null) tvStatTotalTeachers.setText(totalT);
            if (tvStatActiveTeachers != null) tvStatActiveTeachers.setText(totalT);
        }
        if (!data.has("teachers") || !data.get("teachers").isJsonArray()) return;

        JsonArray teachers = data.getAsJsonArray("teachers");
        List<TeacherReportAdapter.TeacherReportItem> overviewList = new ArrayList<>();
        List<TeacherWorkloadAdapter.WorkloadItem> workloadList = new ArrayList<>();
        List<TeacherAssignmentReportAdapter.AssignmentReportItem> assignmentList = new ArrayList<>();
        List<TeacherPerformanceAdapter.PerformanceItem> perfList = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<BarEntry> perfEntries = new ArrayList<>();
        List<String> barLabels = new ArrayList<>();
        List<String> perfLabels = new ArrayList<>();

        for (int i = 0; i < teachers.size(); i++) {
            JsonObject t = teachers.get(i).getAsJsonObject();
            String code = UiUtils.optString(t, "teacher_code", "");
            String name = UiUtils.optString(t, "name", "");
            String dept = UiUtils.optString(t, "department", "");
            int courses = UiUtils.optInt(t, "assigned_courses", 0);
            int classes = UiUtils.optInt(t, "assigned_classes", 0);
            float hrs = UiUtils.optFloat(t, "weekly_hours", 0f);
            String status = UiUtils.optString(t, "workload_status", "Normal Workload");
            float rating = UiUtils.optFloat(t, "rating", -1f);
            float passRate = UiUtils.optFloat(t, "pass_rate", -1f);

            String ratingText = rating >= 0 ? String.format(Locale.US, "%.1f/5.0", rating) : "No Ratings";
            overviewList.add(new TeacherReportAdapter.TeacherReportItem(
                    code, name, dept,
                    courses + " Courses", classes + " Classes",
                    String.format(Locale.US, "%.1f Hrs/Wk", hrs), ratingText));

            workloadList.add(new TeacherWorkloadAdapter.WorkloadItem(
                    code, name, dept,
                    String.format(Locale.US, "%.1f Hrs/Wk", hrs), status));

            assignmentList.add(new TeacherAssignmentReportAdapter.AssignmentReportItem(
                    code, courses + " Courses • " + classes + " Classes", name, dept,
                    (courses > 0 || classes > 0) ? "Assigned" : "Unassigned"));

            perfList.add(new TeacherPerformanceAdapter.PerformanceItem(
                    code, name, dept,
                    passRate >= 0 ? String.format(Locale.US, "%.1f%% Pass", passRate) : "N/A",
                    ratingText));

            if (i < 8) {
                barEntries.add(new BarEntry(i, hrs));
                barLabels.add(code.isEmpty() ? name : code);
                if (passRate >= 0f) {
                    perfEntries.add(new BarEntry(perfEntries.size(), passRate));
                    perfLabels.add(code.isEmpty() ? name : code);
                }
            }
        }

        if (chartTeacherWorkloadBar != null) {
            if (!barEntries.isEmpty()) {
                BarDataSet ds = new BarDataSet(barEntries, getString(R.string.teaching_hours_wk));
                ds.setColor(Color.parseColor("#3155F5"));
                ds.setDrawValues(false);
                BarData bd = new BarData(ds);
                bd.setBarWidth(0.5f);
                chartTeacherWorkloadBar.setData(bd);
                chartTeacherWorkloadBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barLabels.toArray(new String[0])));
            } else {
                chartTeacherWorkloadBar.clear();
            }
            chartTeacherWorkloadBar.invalidate();
        }
        if (chartTeacherWorkloadBarChart != null) {
            if (!barEntries.isEmpty()) {
                BarDataSet ds = new BarDataSet(barEntries, getString(R.string.hours));
                ds.setColor(Color.parseColor("#3155F5"));
                ds.setDrawValues(false);
                BarData bd = new BarData(ds);
                bd.setBarWidth(0.5f);
                chartTeacherWorkloadBarChart.setData(bd);
                chartTeacherWorkloadBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barLabels.toArray(new String[0])));
            } else {
                chartTeacherWorkloadBarChart.clear();
            }
            chartTeacherWorkloadBarChart.invalidate();
        }
        if (chartTeacherPerformanceBar != null) {
            if (!perfEntries.isEmpty()) {
                BarDataSet ds = new BarDataSet(perfEntries, getString(R.string.pass_rate_2646b3));
                ds.setColor(Color.parseColor("#10B981"));
                ds.setDrawValues(false);
                BarData bd = new BarData(ds);
                bd.setBarWidth(0.5f);
                chartTeacherPerformanceBar.setData(bd);
                chartTeacherPerformanceBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(perfLabels.toArray(new String[0])));
            } else {
                chartTeacherPerformanceBar.clear();
            }
            chartTeacherPerformanceBar.invalidate();
        }

        if (rvTeacherReport != null) {
            teacherOverviewAdapter = new TeacherReportAdapter(getContext(), overviewList, null);
            rvTeacherReport.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherReport.setAdapter(teacherOverviewAdapter);
        }
        if (rvTeacherWorkloadList != null) {
            teacherWorkloadAdapter = new TeacherWorkloadAdapter(getContext(), workloadList, null);
            rvTeacherWorkloadList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherWorkloadList.setAdapter(teacherWorkloadAdapter);
        }
        if (rvTeacherAssignmentsList != null) {
            teacherAssignmentAdapter = new TeacherAssignmentReportAdapter(getContext(), assignmentList, null);
            rvTeacherAssignmentsList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherAssignmentsList.setAdapter(teacherAssignmentAdapter);
        }
        if (rvTeacherPerformanceList != null) {
            teacherPerformanceAdapter = new TeacherPerformanceAdapter(getContext(), perfList, null);
            rvTeacherPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherPerformanceList.setAdapter(teacherPerformanceAdapter);
        }
    }

    private void fetchRealCoursesAnalytics(Integer ayId, Integer semId) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getReportsAnalyticsCourses(ayId, semId, null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");
                cachedCoursesData = data;
                bindCoursesToViews(data);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void bindCoursesToViews(JsonObject data) {
        if (data == null || !isAdded() || getContext() == null) return;
        if (data.has("enrollment_summary") && data.get("enrollment_summary").isJsonObject()) {
            JsonObject es = data.getAsJsonObject("enrollment_summary");
            int totalCourses = UiUtils.optInt(es, "total_courses", 0);
            int activeCourses = UiUtils.optInt(es, "active_courses", 0);
            int totalEnr = UiUtils.optInt(es, "total_enrollments", 0);
            int enrStudents = UiUtils.optInt(es, "enrolled_students", 0);
            int completedEnr = UiUtils.optInt(es, "completed_enrollments", 0);
            int incompleteEnr = UiUtils.optInt(es, "incomplete_enrollments", 0);
            int activeCohorts = UiUtils.optInt(es, "active_cohorts", 0);
            float avgClassSize = UiUtils.optFloat(es, "average_class_size", 0f);
            float compRate = UiUtils.optFloat(es, "completion_rate", 0f);

            if (tvStatTotalCourses != null) tvStatTotalCourses.setText(String.valueOf(totalCourses));
            if (tvStatActiveCourses != null) tvStatActiveCourses.setText(String.valueOf(activeCourses));
            if (tvStatTotalEnrollments != null) tvStatTotalEnrollments.setText(String.valueOf(totalEnr));
            if (tvStatAvgClassSize != null) tvStatAvgClassSize.setText(String.format(Locale.US, "%.1f", avgClassSize));
            if (tvCourseEnrollStatTotal != null) tvCourseEnrollStatTotal.setText(String.valueOf(totalEnr));
            if (tvCourseEnrollStatStudents != null) tvCourseEnrollStatStudents.setText(String.valueOf(enrStudents));
            if (tvCourseEnrollStatCohorts != null) tvCourseEnrollStatCohorts.setText(String.valueOf(activeCohorts));
            if (tvCourseEnrollStatUtilization != null) tvCourseEnrollStatUtilization.setText("--");
            if (tvCourseCompletionRate != null) tvCourseCompletionRate.setText(String.format(Locale.US, "%.1f%%", compRate));
            if (tvCourseCompletedCount != null) tvCourseCompletedCount.setText(String.valueOf(completedEnr));
            if (tvCourseIncompleteCount != null) tvCourseIncompleteCount.setText(String.valueOf(incompleteEnr));
        }

        if (chartCourseEnrollmentLine != null && data.has("enrollment_by_semester") && data.get("enrollment_by_semester").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("enrollment_by_semester");
            List<Entry> lineEntries = new ArrayList<>();
            List<String> lineLabels = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject item = arr.get(i).getAsJsonObject();
                lineEntries.add(new Entry(i, UiUtils.optFloat(item, "count", 0f)));
                lineLabels.add(UiUtils.optString(item, "label", ""));
            }
            if (!lineEntries.isEmpty()) {
                LineDataSet ds = new LineDataSet(lineEntries, getString(R.string.enrollments));
                ds.setColor(Color.parseColor("#3155F5"));
                ds.setLineWidth(3f);
                ds.setCircleColor(Color.parseColor("#3155F5"));
                ds.setCircleRadius(4f);
                ds.setDrawValues(false);
                chartCourseEnrollmentLine.setData(new LineData(ds));
                chartCourseEnrollmentLine.getXAxis().setValueFormatter(new IndexAxisValueFormatter(lineLabels.toArray(new String[0])));
            } else {
                chartCourseEnrollmentLine.clear();
            }
            chartCourseEnrollmentLine.invalidate();
        }

        if (!data.has("courses") || !data.get("courses").isJsonArray()) return;

        JsonArray courses = data.getAsJsonArray("courses");
        List<CourseReportListAdapter.CourseReportItem> enrollList = new ArrayList<>();
        List<CourseReportListAdapter.CourseReportItem> completionList = new ArrayList<>();
        List<CourseReportListAdapter.CourseReportItem> perfList = new ArrayList<>();
        List<BarEntry> passEntries = new ArrayList<>();
        List<String> passLabels = new ArrayList<>();

        for (int i = 0; i < courses.size(); i++) {
            JsonObject c = courses.get(i).getAsJsonObject();
            String code = UiUtils.optString(c, "code", "");
            String name = UiUtils.optString(c, "name", "");
            String dept = UiUtils.optString(c, "department", "");
            String status = UiUtils.optString(c, "status", "Active");
            int enrolled = UiUtils.optInt(c, "enrollment_count", 0);
            int completed = UiUtils.optInt(c, "completed_count", 0);
            float compPct = UiUtils.optFloat(c, "completion_percentage", 0f);
            float passRate = UiUtils.optFloat(c, "pass_rate", -1f);
            float avgScore = UiUtils.optFloat(c, "average_score", -1f);

            enrollList.add(new CourseReportListAdapter.CourseReportItem(
                    code, name, dept, enrolled + " Enrolled", status));
            completionList.add(new CourseReportListAdapter.CourseReportItem(
                    code, name, dept,
                    String.format(Locale.US, "%d/%d Completed (%.1f%%)", completed, enrolled, compPct),
                    status));
            perfList.add(new CourseReportListAdapter.CourseReportItem(
                    code, name, dept,
                    passRate >= 0
                            ? String.format(Locale.US, "%.1f%% Pass • Avg %.1f", passRate, avgScore)
                            : "No Graded Records",
                    passRate >= 80f ? "High Pass Rate" : status));

            if (i < 8 && passRate >= 0) {
                passEntries.add(new BarEntry(passEntries.size(), passRate));
                passLabels.add(code);
            }
        }

        if (chartCoursePerformanceBar != null) {
            if (!passEntries.isEmpty()) {
                BarDataSet ds = new BarDataSet(passEntries, getString(R.string.pass_rate));
                ds.setColor(Color.parseColor("#3155F5"));
                ds.setDrawValues(false);
                BarData bd = new BarData(ds);
                bd.setBarWidth(0.5f);
                chartCoursePerformanceBar.setData(bd);
                chartCoursePerformanceBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(passLabels.toArray(new String[0])));
            } else {
                chartCoursePerformanceBar.clear();
            }
            chartCoursePerformanceBar.invalidate();
        }

        if (rvCourseEnrollmentList != null) {
            courseEnrollmentAdapter = new CourseReportListAdapter(getContext(), enrollList, null);
            rvCourseEnrollmentList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourseEnrollmentList.setAdapter(courseEnrollmentAdapter);
        }
        if (rvCourseCompletionList != null) {
            courseCompletionAdapter = new CourseReportListAdapter(getContext(), completionList, null);
            rvCourseCompletionList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourseCompletionList.setAdapter(courseCompletionAdapter);
        }
        if (rvCoursePerformanceList != null) {
            coursePerformanceAdapter = new CourseReportListAdapter(getContext(), perfList, null);
            rvCoursePerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCoursePerformanceList.setAdapter(coursePerformanceAdapter);
        }

        if (llTopEnrolledCoursesContainer != null && getContext() != null) {
            llTopEnrolledCoursesContainer.removeAllViews();
            List<JsonObject> sortedCourses = new ArrayList<>();
            for (int i = 0; i < courses.size(); i++) {
                if (courses.get(i).isJsonObject()) sortedCourses.add(courses.get(i).getAsJsonObject());
            }
            sortedCourses.sort((a, b) -> Integer.compare(
                    UiUtils.optInt(b, "enrollment_count", 0),
                    UiUtils.optInt(a, "enrollment_count", 0)));
            int maxEnrolled = sortedCourses.isEmpty() ? 1 : Math.max(1, UiUtils.optInt(sortedCourses.get(0), "enrollment_count", 1));
            int limit = Math.min(5, sortedCourses.size());
            if (limit == 0) {
                TextView emptyTv = new TextView(getContext());
                emptyTv.setText("No course enrollment records available.");
                emptyTv.setTextColor(Color.parseColor("#64748B"));
                emptyTv.setTextSize(13f);
                llTopEnrolledCoursesContainer.addView(emptyTv);
            } else {
                for (int i = 0; i < limit; i++) {
                    JsonObject c = sortedCourses.get(i);
                    String code = UiUtils.optString(c, "code", "");
                    String name = UiUtils.optString(c, "name", "");
                    int enrolled = UiUtils.optInt(c, "enrollment_count", 0);

                    android.widget.LinearLayout row = new android.widget.LinearLayout(getContext());
                    row.setOrientation(android.widget.LinearLayout.VERTICAL);
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.bottomMargin = (int) (10 * getResources().getDisplayMetrics().density);
                    row.setLayoutParams(lp);

                    android.widget.LinearLayout top = new android.widget.LinearLayout(getContext());
                    top.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    TextView tvTitle = new TextView(getContext());
                    tvTitle.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    tvTitle.setText((!code.isEmpty() ? code + " — " : "") + name);
                    tvTitle.setTextColor(Color.parseColor("#1E293B"));
                    tvTitle.setTextSize(13f);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    TextView tvCount = new TextView(getContext());
                    tvCount.setText(String.valueOf(enrolled));
                    tvCount.setTextColor(Color.parseColor("#3155F5"));
                    tvCount.setTextSize(13f);
                    tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
                    top.addView(tvTitle);
                    top.addView(tvCount);
                    row.addView(top);

                    android.widget.ProgressBar pb = new android.widget.ProgressBar(getContext(), null,
                            android.R.attr.progressBarStyleHorizontal);
                    android.widget.LinearLayout.LayoutParams pbLp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (8 * getResources().getDisplayMetrics().density));
                    pbLp.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
                    pb.setLayoutParams(pbLp);
                    pb.setMax(maxEnrolled);
                    pb.setProgress(enrolled);
                    row.addView(pb);

                    llTopEnrolledCoursesContainer.addView(row);
                }
            }
        }
    }
}
