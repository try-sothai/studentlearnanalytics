package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.social7.studentlearnanalytics.admin.reports.TeacherAssignmentReportAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherPerformanceAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherReportAdapter;
import com.social7.studentlearnanalytics.admin.reports.TeacherWorkloadAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    // Primary Navigation Buttons
    private MaterialButton btnTabOverview, btnTabStudentsReport, btnTabTeachersReport, btnTabCoursesReport;

    // Primary Containers
    private View layoutOverviewContainer, layoutStudentsReportContainer, layoutTeachersReportContainer, layoutCoursesReportContainer;

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
        if (tvTitle != null) tvTitle.setText("Reports & Analytics");
        if (tvSubtitle != null) tvSubtitle.setText("View, analyze and export university reports");

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

        setupPrimaryTabs();
        setupOverviewCategoryRows(view);

        // Bind Sub-sections
        bindStudentsSectionViews(view);
        bindTeachersSectionViews(view);
        bindCoursesSectionViews(view);

        selectReportTab(selectedPrimaryTab);

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
        } else if (tabIndex == 2) {
            selectTeacherSubTab(0);
        } else if (tabIndex == 3) {
            selectCourseSubTab(0);
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
            rowAcademic.setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Academic Performance Report")
                            .setMessage("Summary Statistics for AY 2024-2025:\n\n" +
                                    "• Total Enrolled Students: 2,845\n" +
                                    "• Average University GPA: 3.42 / 4.0\n" +
                                    "• Course Completion Rate: 94.8%\n" +
                                    "• Academic Honor Roll: 412 Students")
                            .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                            .show()
            );
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
        if (chartEnrollmentTrend != null && chartEnrollmentTrend.getData() == null) {
            List<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, 2400f));
            entries.add(new Entry(1, 2550f));
            entries.add(new Entry(2, 2620f));
            entries.add(new Entry(3, 2700f));
            entries.add(new Entry(4, 2780f));
            entries.add(new Entry(5, 2845f));

            LineDataSet dataSet = new LineDataSet(entries, "Enrollment");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setLineWidth(3f);
            dataSet.setCircleColor(Color.parseColor("#3155F5"));
            dataSet.setCircleRadius(5f);
            dataSet.setDrawValues(false);

            chartEnrollmentTrend.setData(new LineData(dataSet));
            XAxis xAxis = chartEnrollmentTrend.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Sep", "Oct", "Nov", "Dec", "Jan", "Feb"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartEnrollmentTrend.getAxisRight().setEnabled(false);
            chartEnrollmentTrend.getDescription().setEnabled(false);
            chartEnrollmentTrend.invalidate();
        }

        if (chartStudentDistribution != null && chartStudentDistribution.getData() == null) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(35f, "Computer Studies"));
            entries.add(new PieEntry(25f, "Business"));
            entries.add(new PieEntry(20f, "Engineering"));
            entries.add(new PieEntry(12f, "Education"));
            entries.add(new PieEntry(8f, "Other"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{
                    Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#6546F5"), Color.parseColor("#16A34A"), Color.parseColor("#F59E0B")
            });
            dataSet.setSliceSpace(2f);
            PieData pieData = new PieData(dataSet);
            pieData.setDrawValues(false);

            chartStudentDistribution.setData(pieData);
            chartStudentDistribution.setHoleRadius(50f);
            chartStudentDistribution.setCenterText("2,845\nStudents");
            chartStudentDistribution.setCenterTextSize(14f);
            chartStudentDistribution.getDescription().setEnabled(false);
            chartStudentDistribution.invalidate();
        }
    }

    private void setupStudentEnrollmentChartsAndList() {
        if (chartStudentEnrollmentBar != null && chartStudentEnrollmentBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 480f));
            entries.add(new BarEntry(1, 520f));
            entries.add(new BarEntry(2, 590f));
            entries.add(new BarEntry(3, 610f));
            entries.add(new BarEntry(4, 645f));

            BarDataSet dataSet = new BarDataSet(entries, "Enrollments");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartStudentEnrollmentBar.setData(barData);

            XAxis xAxis = chartStudentEnrollmentBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2021", "2022", "2023", "2024", "2025"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentEnrollmentBar.getAxisRight().setEnabled(false);
            chartStudentEnrollmentBar.getDescription().setEnabled(false);
            chartStudentEnrollmentBar.invalidate();
        }

        if (rvStudentEnrollmentList != null) {
            List<StudentReportListAdapter.StudentReportItem> list = new ArrayList<>();
            list.add(new StudentReportListAdapter.StudentReportItem("STU23001", "Sophea", "BSIT 4-A • College of Computer Studies", "Enrolled: AY 2024-2025 Sem 1", "Active"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23002", "Dara Miller", "BSIT 4-B • College of Computer Studies", "Enrolled: AY 2024-2025 Sem 1", "Active"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23003", "Channary", "BSCS 3-A • College of Computer Studies", "Enrolled: AY 2024-2025 Sem 1", "Active"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23004", "Sokha", "BSCS 3-B • College of Computer Studies", "Enrolled: AY 2024-2025 Sem 1", "Active"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23005", "Bopha", "BSIS 2-A • College of Computer Studies", "Enrolled: AY 2024-2025 Sem 1", "Active"));

            studentEnrollmentAdapter = new StudentReportListAdapter(getContext(), list, null);
            rvStudentEnrollmentList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentEnrollmentList.setAdapter(studentEnrollmentAdapter);
        }
    }

    private void setupStudentPerformanceChartsAndList() {
        if (chartStudentPerformanceGpaBar != null && chartStudentPerformanceGpaBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 412f)); // 3.75 - 4.0
            entries.add(new BarEntry(1, 1250f)); // 3.25 - 3.74
            entries.add(new BarEntry(2, 980f));  // 2.75 - 3.24
            entries.add(new BarEntry(3, 158f));  // 2.25 - 2.74
            entries.add(new BarEntry(4, 45f));   // Below 2.25

            BarDataSet dataSet = new BarDataSet(entries, "Students");
            dataSet.setColors(new int[]{
                    Color.parseColor("#10B981"), Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#F59E0B"), Color.parseColor("#EF4444")
            });
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartStudentPerformanceGpaBar.setData(barData);

            XAxis xAxis = chartStudentPerformanceGpaBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"3.75-4.0", "3.25-3.74", "2.75-3.24", "2.25-2.74", "<2.25"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentPerformanceGpaBar.getAxisRight().setEnabled(false);
            chartStudentPerformanceGpaBar.getDescription().setEnabled(false);
            chartStudentPerformanceGpaBar.invalidate();
        }

        if (rvStudentPerformanceList != null) {
            List<StudentReportListAdapter.StudentReportItem> list = new ArrayList<>();
            list.add(new StudentReportListAdapter.StudentReportItem("STU23001", "Sophea", "BSIT 4-A • College of Computer Studies", "GPA: 3.92 / 4.0 • Honor Roll", "Honors"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23002", "Dara Miller", "BSIT 4-B • College of Computer Studies", "GPA: 3.85 / 4.0 • Honor Roll", "Honors"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23003", "Channary", "BSCS 3-A • College of Computer Studies", "GPA: 3.78 / 4.0 • Honor Roll", "Honors"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23004", "Sokha", "BSCS 3-B • College of Computer Studies", "GPA: 3.45 / 4.0 • Good Standing", "Active"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23005", "Bopha", "BSIS 2-A • College of Computer Studies", "GPA: 2.15 / 4.0 • Tutoring Advised", "Needs Support"));

            studentPerformanceAdapter = new StudentReportListAdapter(getContext(), list, null);
            rvStudentPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentPerformanceList.setAdapter(studentPerformanceAdapter);
        }
    }

    private void setupStudentAttendanceChartsAndList() {
        if (chartStudentAttendanceTrendLine != null && chartStudentAttendanceTrendLine.getData() == null) {
            List<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, 94.5f));
            entries.add(new Entry(1, 93.2f));
            entries.add(new Entry(2, 91.8f));
            entries.add(new Entry(3, 92.5f));
            entries.add(new Entry(4, 94.0f));

            LineDataSet dataSet = new LineDataSet(entries, "Attendance %");
            dataSet.setColor(Color.parseColor("#10B981"));
            dataSet.setLineWidth(3f);
            dataSet.setCircleColor(Color.parseColor("#10B981"));
            dataSet.setCircleRadius(5f);
            dataSet.setDrawValues(false);

            chartStudentAttendanceTrendLine.setData(new LineData(dataSet));
            XAxis xAxis = chartStudentAttendanceTrendLine.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Oct", "Nov", "Dec", "Jan", "Feb"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartStudentAttendanceTrendLine.getAxisRight().setEnabled(false);
            chartStudentAttendanceTrendLine.getDescription().setEnabled(false);
            chartStudentAttendanceTrendLine.invalidate();
        }

        if (rvStudentAttendanceList != null) {
            List<StudentReportListAdapter.StudentReportItem> list = new ArrayList<>();
            list.add(new StudentReportListAdapter.StudentReportItem("STU23001", "Sophea", "BSIT 4-A • College of Computer Studies", "Attendance Rate: 98.5% (118/120 Days)", "Present"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23002", "Dara Miller", "BSIT 4-B • College of Computer Studies", "Attendance Rate: 96.0% (115/120 Days)", "Present"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23003", "Channary", "BSCS 3-A • College of Computer Studies", "Attendance Rate: 95.0% (114/120 Days)", "Present"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23004", "Sokha", "BSCS 3-B • College of Computer Studies", "Attendance Rate: 91.2% (109/120 Days)", "Present"));
            list.add(new StudentReportListAdapter.StudentReportItem("STU23005", "Bopha", "BSIS 2-A • College of Computer Studies", "Attendance Rate: 81.5% (98/120 Days)", "Absent"));

            studentAttendanceAdapter = new StudentReportListAdapter(getContext(), list, null);
            rvStudentAttendanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStudentAttendanceList.setAdapter(studentAttendanceAdapter);
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
        if (chartTeacherWorkloadBar != null && chartTeacherWorkloadBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 18f));
            entries.add(new BarEntry(1, 21f));
            entries.add(new BarEntry(2, 15f));
            entries.add(new BarEntry(3, 24f));
            entries.add(new BarEntry(4, 19f));

            BarDataSet dataSet = new BarDataSet(entries, "Teaching Hours/Wk");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartTeacherWorkloadBar.setData(barData);

            XAxis xAxis = chartTeacherWorkloadBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"CCS", "CBA", "COE", "CAS", "CED"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherWorkloadBar.getAxisRight().setEnabled(false);
            chartTeacherWorkloadBar.getDescription().setEnabled(false);
            chartTeacherWorkloadBar.invalidate();
        }

        if (rvTeacherReport != null) {
            List<TeacherReportAdapter.TeacherReportItem> list = new ArrayList<>();
            list.add(new TeacherReportAdapter.TeacherReportItem("TCH001", "Prof. Alan Santos", "Computer Studies", "CS101, IT402", "BSIT 4-A, BSIT 4-B", "18 Hrs/Wk", "94.2% Rating"));
            list.add(new TeacherReportAdapter.TeacherReportItem("TCH002", "Dr. Maria Cruz", "Computer Studies", "SE301, CS204", "BSCS 3-A", "21 Hrs/Wk", "96.5% Rating"));
            list.add(new TeacherReportAdapter.TeacherReportItem("TCH003", "Engr. John Reyes", "Engineering", "EE101, CS102", "BSCS 3-B", "15 Hrs/Wk", "91.0% Rating"));
            list.add(new TeacherReportAdapter.TeacherReportItem("TCH004", "Prof. Sarah Jenkins", "Business Admin", "BUS201, MGT101", "BSIS 2-A", "24 Hrs/Wk", "89.8% Rating"));
            list.add(new TeacherReportAdapter.TeacherReportItem("TCH005", "Prof. Robert Lim", "Computer Studies", "IS201, SEC301", "ACT 1-A", "19 Hrs/Wk", "93.4% Rating"));

            teacherOverviewAdapter = new TeacherReportAdapter(getContext(), list, null);
            rvTeacherReport.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherReport.setAdapter(teacherOverviewAdapter);
        }
    }

    private void setupTeacherWorkloadChartsAndList() {
        if (chartTeacherWorkloadBarChart != null && chartTeacherWorkloadBarChart.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 320f));
            entries.add(new BarEntry(1, 280f));
            entries.add(new BarEntry(2, 240f));
            entries.add(new BarEntry(3, 150f));
            entries.add(new BarEntry(4, 90f));

            BarDataSet dataSet = new BarDataSet(entries, "Hours");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartTeacherWorkloadBarChart.setData(barData);

            XAxis xAxis = chartTeacherWorkloadBarChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"CCS", "CBA", "COE", "CAS", "CED"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherWorkloadBarChart.getAxisRight().setEnabled(false);
            chartTeacherWorkloadBarChart.getDescription().setEnabled(false);
            chartTeacherWorkloadBarChart.invalidate();
        }

        if (rvTeacherWorkloadList != null) {
            List<TeacherWorkloadAdapter.WorkloadItem> list = new ArrayList<>();
            list.add(new TeacherWorkloadAdapter.WorkloadItem("TCH001", "Prof. Alan Santos", "College of Computer Studies", "18 Hrs/Wk (2 Courses, 2 Classes)", "Normal Workload"));
            list.add(new TeacherWorkloadAdapter.WorkloadItem("TCH002", "Dr. Maria Cruz", "College of Computer Studies", "24 Hrs/Wk (3 Courses, 3 Classes)", "High Workload"));
            list.add(new TeacherWorkloadAdapter.WorkloadItem("TCH003", "Engr. John Reyes", "College of Engineering", "15 Hrs/Wk (2 Courses, 1 Class)", "Normal Workload"));
            list.add(new TeacherWorkloadAdapter.WorkloadItem("TCH004", "Prof. Sarah Jenkins", "College of Business", "12 Hrs/Wk (1 Course, 1 Class)", "Capacity Available"));
            list.add(new TeacherWorkloadAdapter.WorkloadItem("TCH005", "Prof. Robert Lim", "College of Computer Studies", "19 Hrs/Wk (2 Courses, 2 Classes)", "Normal Workload"));

            teacherWorkloadAdapter = new TeacherWorkloadAdapter(getContext(), list, null);
            rvTeacherWorkloadList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherWorkloadList.setAdapter(teacherWorkloadAdapter);
        }
    }

    private void setupTeacherAssignmentsList() {
        if (rvTeacherAssignmentsList != null) {
            List<TeacherAssignmentReportAdapter.AssignmentReportItem> list = new ArrayList<>();
            list.add(new TeacherAssignmentReportAdapter.AssignmentReportItem("BSIT 4-A", "CS101 — Intro to Computer Science", "Prof. Alan Santos", "College of Computer Studies • AY 2024-2025 Sem 1", "Assigned"));
            list.add(new TeacherAssignmentReportAdapter.AssignmentReportItem("BSIT 4-B", "IT402 — Web Development Systems", "Prof. Alan Santos", "College of Computer Studies • AY 2024-2025 Sem 1", "Assigned"));
            list.add(new TeacherAssignmentReportAdapter.AssignmentReportItem("BSCS 3-A", "SE301 — Software Engineering", "Dr. Maria Cruz", "College of Computer Studies • AY 2024-2025 Sem 1", "Assigned"));
            list.add(new TeacherAssignmentReportAdapter.AssignmentReportItem("BSCS 3-B", "CS204 — Data Structures & Algorithms", "Engr. John Reyes", "College of Engineering • AY 2024-2025 Sem 1", "Assigned"));
            list.add(new TeacherAssignmentReportAdapter.AssignmentReportItem("BSIS 2-A", "IS201 — Enterprise Architecture", "Prof. Robert Lim", "College of Computer Studies • AY 2024-2025 Sem 1", "Assigned"));

            teacherAssignmentAdapter = new TeacherAssignmentReportAdapter(getContext(), list, null);
            rvTeacherAssignmentsList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherAssignmentsList.setAdapter(teacherAssignmentAdapter);
        }
    }

    private void setupTeacherPerformanceChartsAndList() {
        if (chartTeacherPerformanceBar != null && chartTeacherPerformanceBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 95.2f));
            entries.add(new BarEntry(1, 92.8f));
            entries.add(new BarEntry(2, 94.0f));
            entries.add(new BarEntry(3, 91.5f));
            entries.add(new BarEntry(4, 96.0f));

            BarDataSet dataSet = new BarDataSet(entries, "Pass Rate %");
            dataSet.setColor(Color.parseColor("#10B981"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartTeacherPerformanceBar.setData(barData);

            XAxis xAxis = chartTeacherPerformanceBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"CCS", "CBA", "COE", "CAS", "CED"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartTeacherPerformanceBar.getAxisRight().setEnabled(false);
            chartTeacherPerformanceBar.getDescription().setEnabled(false);
            chartTeacherPerformanceBar.invalidate();
        }

        if (rvTeacherPerformanceList != null) {
            List<TeacherPerformanceAdapter.PerformanceItem> list = new ArrayList<>();
            list.add(new TeacherPerformanceAdapter.PerformanceItem("TCH001", "Prof. Alan Santos", "College of Computer Studies", "96.5% Pass Rate • 4.9/5.0 Student Rating", "95.2% Score"));
            list.add(new TeacherPerformanceAdapter.PerformanceItem("TCH002", "Dr. Maria Cruz", "College of Computer Studies", "98.0% Pass Rate • 4.9/5.0 Student Rating", "97.1% Score"));
            list.add(new TeacherPerformanceAdapter.PerformanceItem("TCH003", "Engr. John Reyes", "College of Engineering", "92.0% Pass Rate • 4.7/5.0 Student Rating", "91.8% Score"));
            list.add(new TeacherPerformanceAdapter.PerformanceItem("TCH004", "Prof. Sarah Jenkins", "College of Business", "89.5% Pass Rate • 4.5/5.0 Student Rating", "89.2% Score"));
            list.add(new TeacherPerformanceAdapter.PerformanceItem("TCH005", "Prof. Robert Lim", "College of Computer Studies", "94.5% Pass Rate • 4.8/5.0 Student Rating", "94.0% Score"));

            teacherPerformanceAdapter = new TeacherPerformanceAdapter(getContext(), list, null);
            rvTeacherPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherPerformanceList.setAdapter(teacherPerformanceAdapter);
        }
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
        if (chartCourseTrendBar != null && chartCourseTrendBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 980f));
            entries.add(new BarEntry(1, 1050f));
            entries.add(new BarEntry(2, 1120f));
            entries.add(new BarEntry(3, 1180f));
            entries.add(new BarEntry(4, 1220f));
            entries.add(new BarEntry(5, 1245f));

            BarDataSet dataSet = new BarDataSet(entries, "Enrollments");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartCourseTrendBar.setData(barData);

            XAxis xAxis = chartCourseTrendBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Sep", "Oct", "Nov", "Dec", "Jan", "Feb"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCourseTrendBar.getAxisRight().setEnabled(false);
            chartCourseTrendBar.getDescription().setEnabled(false);
            chartCourseTrendBar.invalidate();
        }
    }

    private void setupCourseEnrollmentChartsAndList() {
        if (chartCourseEnrollmentLine != null && chartCourseEnrollmentLine.getData() == null) {
            List<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, 5100f));
            entries.add(new Entry(1, 5400f));
            entries.add(new Entry(2, 5800f));
            entries.add(new Entry(3, 6100f));
            entries.add(new Entry(4, 6245f));

            LineDataSet dataSet = new LineDataSet(entries, "Enrollments");
            dataSet.setColor(Color.parseColor("#3155F5"));
            dataSet.setLineWidth(3f);
            dataSet.setCircleColor(Color.parseColor("#3155F5"));
            dataSet.setCircleRadius(5f);
            dataSet.setDrawValues(false);

            chartCourseEnrollmentLine.setData(new LineData(dataSet));
            XAxis xAxis = chartCourseEnrollmentLine.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"2021", "2022", "2023", "2024", "2025"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCourseEnrollmentLine.getAxisRight().setEnabled(false);
            chartCourseEnrollmentLine.getDescription().setEnabled(false);
            chartCourseEnrollmentLine.invalidate();
        }

        if (rvCourseEnrollmentList != null) {
            List<CourseReportListAdapter.CourseReportItem> list = new ArrayList<>();
            list.add(new CourseReportListAdapter.CourseReportItem("CS101", "Intro to Computer Science", "College of Computer Studies", "1,240 Enrolled • 98.2% Active Utilization", "Active"));
            list.add(new CourseReportListAdapter.CourseReportItem("MA101", "Calculus I for Engineers", "College of Engineering", "1,150 Enrolled • 95.0% Active Utilization", "Active"));
            list.add(new CourseReportListAdapter.CourseReportItem("IT201", "Web Development Systems", "College of Computer Studies", "980 Enrolled • 92.4% Active Utilization", "Active"));
            list.add(new CourseReportListAdapter.CourseReportItem("BUS101", "Principles of Business Management", "College of Business", "850 Enrolled • 89.0% Active Utilization", "Active"));
            list.add(new CourseReportListAdapter.CourseReportItem("EN101", "English Communication & Writing", "College of Arts & Sciences", "720 Enrolled • 86.5% Active Utilization", "Active"));

            courseEnrollmentAdapter = new CourseReportListAdapter(getContext(), list, null);
            rvCourseEnrollmentList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourseEnrollmentList.setAdapter(courseEnrollmentAdapter);
        }
    }

    private void setupCourseCompletionList() {
        if (rvCourseCompletionList != null) {
            List<CourseReportListAdapter.CourseReportItem> list = new ArrayList<>();
            list.add(new CourseReportListAdapter.CourseReportItem("CS101", "Intro to Computer Science", "College of Computer Studies", "98.5% Completion Rate • 1,221 Completed", "Completed"));
            list.add(new CourseReportListAdapter.CourseReportItem("MA101", "Calculus I for Engineers", "College of Engineering", "94.2% Completion Rate • 1,083 Completed", "Completed"));
            list.add(new CourseReportListAdapter.CourseReportItem("IT201", "Web Development Systems", "College of Computer Studies", "92.8% Completion Rate • 909 Completed", "Completed"));
            list.add(new CourseReportListAdapter.CourseReportItem("BUS101", "Principles of Business Management", "College of Business", "91.5% Completion Rate • 778 Completed", "Completed"));
            list.add(new CourseReportListAdapter.CourseReportItem("EN101", "English Communication & Writing", "College of Arts & Sciences", "90.0% Completion Rate • 648 Completed", "Completed"));

            courseCompletionAdapter = new CourseReportListAdapter(getContext(), list, null);
            rvCourseCompletionList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourseCompletionList.setAdapter(courseCompletionAdapter);
        }
    }

    private void setupCoursePerformanceChartsAndList() {
        if (chartCoursePerformanceBar != null && chartCoursePerformanceBar.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 96f));
            entries.add(new BarEntry(1, 91f));
            entries.add(new BarEntry(2, 93f));
            entries.add(new BarEntry(3, 88f));
            entries.add(new BarEntry(4, 94f));

            BarDataSet dataSet = new BarDataSet(entries, "Pass Rate %");
            dataSet.setColor(Color.parseColor("#10B981"));
            dataSet.setDrawValues(false);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartCoursePerformanceBar.setData(barData);

            XAxis xAxis = chartCoursePerformanceBar.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"CS101", "MA101", "IT201", "BUS101", "EN101"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartCoursePerformanceBar.getAxisRight().setEnabled(false);
            chartCoursePerformanceBar.getDescription().setEnabled(false);
            chartCoursePerformanceBar.invalidate();
        }

        if (rvCoursePerformanceList != null) {
            List<CourseReportListAdapter.CourseReportItem> list = new ArrayList<>();
            list.add(new CourseReportListAdapter.CourseReportItem("CS101", "Intro to Computer Science", "College of Computer Studies", "96.0% Pass Rate • Avg Score: 88.5%", "High Pass Rate"));
            list.add(new CourseReportListAdapter.CourseReportItem("MA101", "Calculus I for Engineers", "College of Engineering", "78.0% Pass Rate • Avg Score: 74.2%", "Needs Review"));
            list.add(new CourseReportListAdapter.CourseReportItem("IT201", "Web Development Systems", "College of Computer Studies", "93.0% Pass Rate • Avg Score: 85.0%", "High Pass Rate"));
            list.add(new CourseReportListAdapter.CourseReportItem("BUS101", "Principles of Business Management", "College of Business", "88.0% Pass Rate • Avg Score: 81.4%", "Active"));
            list.add(new CourseReportListAdapter.CourseReportItem("EN101", "English Communication & Writing", "College of Arts & Sciences", "94.0% Pass Rate • Avg Score: 86.8%", "High Pass Rate"));

            coursePerformanceAdapter = new CourseReportListAdapter(getContext(), list, null);
            rvCoursePerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCoursePerformanceList.setAdapter(coursePerformanceAdapter);
        }
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
        String[] years = {"2024 - 2025", "2023 - 2024", "2022 - 2023"};
        String[] semesters = {"Semester 1", "Semester 2", "All Semesters"};

        setSpinnerAdapter(spYear, years);
        setSpinnerAdapter(spSem, semesters);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null || !isAdded() || getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
