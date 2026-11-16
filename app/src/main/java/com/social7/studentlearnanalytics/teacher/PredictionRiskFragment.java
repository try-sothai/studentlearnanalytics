package com.social7.studentlearnanalytics.teacher;


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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.teacher.student.AtRiskStudentAdapter;
import com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredictionRiskFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabPredOverview, tabPredAtRisk, tabPredExamPrediction, tabPredRiskAnalysis;

    // Containers
    private View secPredOverviewContainer, secPredAtRiskContainer, secPredExamPredictionContainer, secPredRiskAnalysisContainer;

    // Sub-Tab 0: Overview
    private BarChart chartOverviewScoreDist;
    private PieChart chartOverviewRiskPie;

    // Sub-Tab 1: At-Risk Students
    private AtRiskStudentAdapter atRiskAdapter;
    private RecyclerView rvAtRiskStudents;
    private EditText edtSearchAtRisk;
    private String selectedCategory = "All";
    private String searchQuery = "";

    // Sub-Tab 2: Exam Score Prediction
    private RecyclerView rvExamPredictionsList;
    private ExamPredictionAdapter examPredictionAdapter;
    private final List<ExamPredictionAdapter.ExamPredictionItem> examPredictionList = new ArrayList<>();

    // Sub-Tab 3: Risk Analysis
    private RecyclerView rvCourseRiskAnalysis;
    private RiskAnalysisAdapter riskAnalysisAdapter;
    private final List<RiskAnalysisAdapter.RiskAnalysisItem> riskAnalysisList = new ArrayList<>();

    // Selected Sub-Tab Flag (0=Overview, 1=At-Risk, 2=ExamPrediction, 3=RiskAnalysis)
    private int selectedSubTab = 0;

    public PredictionRiskFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_prediction_risk, container, false);

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.prediction_risk), "Machine learning academic risk & exam score analytics"
            );
        }

        // Sub-Tab Buttons
        tabPredOverview = view.findViewById(R.id.tabPredOverview);
        tabPredAtRisk = view.findViewById(R.id.tabPredAtRisk);
        tabPredExamPrediction = view.findViewById(R.id.tabPredExamPrediction);
        tabPredRiskAnalysis = view.findViewById(R.id.tabPredRiskAnalysis);

        // Containers
        secPredOverviewContainer = view.findViewById(R.id.secPredOverviewContainer);
        secPredAtRiskContainer = view.findViewById(R.id.secPredAtRiskContainer);
        secPredExamPredictionContainer = view.findViewById(R.id.secPredExamPredictionContainer);
        secPredRiskAnalysisContainer = view.findViewById(R.id.secPredRiskAnalysisContainer);

        // Bind Sub-views
        bindOverviewViews(view);
        bindAtRiskViews(view);
        bindExamPredictionViews(view);
        bindRiskAnalysisViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabPredOverview != null) tabPredOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabPredAtRisk != null) tabPredAtRisk.setOnClickListener(v -> selectSubTab(1));
        if (tabPredExamPrediction != null) tabPredExamPrediction.setOnClickListener(v -> selectSubTab(2));
        if (tabPredRiskAnalysis != null) tabPredRiskAnalysis.setOnClickListener(v -> selectSubTab(3));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secPredOverviewContainer != null) secPredOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secPredAtRiskContainer != null) secPredAtRiskContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secPredExamPredictionContainer != null) secPredExamPredictionContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secPredRiskAnalysisContainer != null) secPredRiskAnalysisContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewCharts();
        } else if (subIndex == 1) {
            setupAtRiskStudentsList();
        } else if (subIndex == 2) {
            setupExamPredictionList();
        } else if (subIndex == 3) {
            setupRiskAnalysisList();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabPredOverview, tabPredAtRisk, tabPredExamPrediction, tabPredRiskAnalysis};
        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
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

    // ==========================================
    // 0. OVERVIEW SUB-TAB
    // ==========================================
    private void bindOverviewViews(View root) {
        chartOverviewScoreDist = root.findViewById(R.id.chartOverviewScoreDist);
        chartOverviewRiskPie = root.findViewById(R.id.chartOverviewRiskPie);
    }

    private void setupOverviewCharts() {
        if (chartOverviewScoreDist != null && chartOverviewScoreDist.getData() == null) {
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 185f)); // 90-100
            entries.add(new BarEntry(1, 320f)); // 80-89
            entries.add(new BarEntry(2, 210f)); // 70-79
            entries.add(new BarEntry(3, 85f));  // 60-69
            entries.add(new BarEntry(4, 40f));  // <60

            BarDataSet dataSet = new BarDataSet(entries, getString(R.string.predicted_scores));
            dataSet.setColors(new int[]{
                    Color.parseColor("#10B981"), Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#F59E0B"), Color.parseColor("#EF4444")
            });
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            chartOverviewScoreDist.setData(barData);

            XAxis xAxis = chartOverviewScoreDist.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"90-100", "80-89", "70-79", "60-69", "<60"}));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);

            chartOverviewScoreDist.getAxisRight().setEnabled(false);
            chartOverviewScoreDist.getDescription().setEnabled(false);
            ChartFontHelper.applyKhmerFont(chartOverviewScoreDist.getContext(), chartOverviewScoreDist);
            chartOverviewScoreDist.invalidate();
        }

        if (chartOverviewRiskPie != null && chartOverviewRiskPie.getData() == null) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(85f, "Low Risk"));
            entries.add(new PieEntry(10f, "Medium Risk"));
            entries.add(new PieEntry(5f, "High Risk"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{
                    Color.parseColor("#10B981"), Color.parseColor("#F59E0B"), Color.parseColor("#EF4444")
            });
            dataSet.setSliceSpace(2f);

            PieData pieData = new PieData(dataSet);
            pieData.setDrawValues(false);

            chartOverviewRiskPie.setData(pieData);
            chartOverviewRiskPie.setHoleRadius(50f);
            chartOverviewRiskPie.setCenterText("840\nStudents");
            chartOverviewRiskPie.setCenterTextSize(14f);
            chartOverviewRiskPie.getDescription().setEnabled(false);
            ChartFontHelper.applyKhmerFont(chartOverviewRiskPie.getContext(), chartOverviewRiskPie);
            chartOverviewRiskPie.invalidate();
        }
    }

    // ==========================================
    // 1. AT-RISK STUDENTS SUB-TAB
    // ==========================================
    private void bindAtRiskViews(View root) {
        rvAtRiskStudents = root.findViewById(R.id.rvAtRiskStudents);
        edtSearchAtRisk = root.findViewById(R.id.edtSearchAtRisk);

        if (edtSearchAtRisk != null) {
            edtSearchAtRisk.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (atRiskAdapter != null) {
                        atRiskAdapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupAtRiskStudentsList() {
        if (rvAtRiskStudents == null || atRiskAdapter != null) return;

        rvAtRiskStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<AtRiskStudentAdapter.AtRiskStudent> list = getSampleAtRiskStudents();
        atRiskAdapter = new AtRiskStudentAdapter(requireContext(), list, student -> {
            showStudentRiskProfileDetailDialog(student);
        });
        rvAtRiskStudents.setAdapter(atRiskAdapter);
    }

    private void showStudentRiskProfileDetailDialog(AtRiskStudentAdapter.AtRiskStudent student) {
        if (!isAdded() || getContext() == null) return;

        String details = "Student Name: " + student.name + "\n" +
                "Student ID: " + student.id + "\n" +
                "Major / Program: " + student.program + " (" + student.year + ")\n\n" +
                "• Academic GPA: " + student.gpa + " / 4.0\n" +
                "• Class Absence Rate: " + student.absence + "\n" +
                "• Assignment Average: " + student.avgScore + "\n" +
                "• Risk Assessment Level: " + student.riskBadge + "\n\n" +
                "Identified Risk Factors:\n" +
                student.reason1 + "\n" +
                student.reason2 + "\n" +
                student.reason3 + "\n\n" +
                "Recommended Actions:\n" +
                "1. Schedule 1-on-1 Academic Tutoring Session\n" +
                "2. Send Attendance Advisory Warning to Student\n" +
                "3. Assign Remedial Practice Exercises in CS101";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Student Risk Profile & Diagnosis")
                .setMessage(details)
                .setPositiveButton("View Full Dossier", (dialog, which) -> {
                    Student s = new Student(
                            student.id, student.name, student.name.toLowerCase() + "@univ.edu",
                            "Computer Science", student.program, student.year,
                            Double.parseDouble(student.gpa), student.riskBadge, 58.0,
                            student.avatarBgColor, student.avatarTextColor, "CS101"
                    );
                    Intent intent = new Intent(requireContext(), StudentDetailActivity.class);
                    intent.putExtra("student", s);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==========================================
    // 2. EXAM PREDICTION SUB-TAB
    // ==========================================
    private void bindExamPredictionViews(View root) {
        rvExamPredictionsList = root.findViewById(R.id.rvExamPredictionsList);
    }

    private void setupExamPredictionList() {
        if (rvExamPredictionsList == null || examPredictionAdapter != null) return;

        examPredictionList.clear();
        examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem("STU23001", "Sophea", "CS101 - Intro to CS • BSIT 4-A", "Current Avg: 88.5% • Predicted Exam: 92.0% • 94.5% Confidence", "Grade: A (92%)"));
        examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem("STU23002", "Dara", "IT402 - Web Systems • BSIT 4-B", "Current Avg: 58.0% • Predicted Exam: 54.0% • 91.0% Confidence", "Grade: F (54%)"));
        examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem("STU23003", "Rathana", "CS204 - Algorithms • BSCS 3-A", "Current Avg: 62.0% • Predicted Exam: 65.0% • 89.5% Confidence", "Grade: D (65%)"));
        examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem("STU23004", "Sokha", "CS101 - Intro to CS • BSCS 3-B", "Current Avg: 82.0% • Predicted Exam: 84.5% • 93.0% Confidence", "Grade: B+ (85%)"));
        examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem("STU23005", "Sreyneang", "IS201 - Enterprise Arch • BSIS 2-A", "Current Avg: 60.0% • Predicted Exam: 58.5% • 90.0% Confidence", "Grade: F (58%)"));

        examPredictionAdapter = new ExamPredictionAdapter(requireContext(), examPredictionList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.exam_score_prediction_breakdown)
                    .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                            "Student Name: " + item.studentName + "\n" +
                            "Course & Class: " + item.courseClass + "\n\n" +
                            "ML Model Prediction Details:\n" +
                            item.metrics + "\n" +
                            "Expected Final Grade: " + item.gradeBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvExamPredictionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExamPredictionsList.setAdapter(examPredictionAdapter);
    }

    // ==========================================
    // 3. RISK ANALYSIS SUB-TAB
    // ==========================================
    private void bindRiskAnalysisViews(View root) {
        rvCourseRiskAnalysis = root.findViewById(R.id.rvCourseRiskAnalysis);
    }

    private void setupRiskAnalysisList() {
        if (rvCourseRiskAnalysis == null || riskAnalysisAdapter != null) return;

        riskAnalysisList.clear();
        riskAnalysisList.add(new RiskAnalysisAdapter.RiskAnalysisItem("CS101", "Data Structures & Algorithms", "College of Computer Studies • 35 Enrolled", "Primary Risk Factor: Low Attendance (<75%) and Missed Quizzes", "High Risk (5)"));
        riskAnalysisList.add(new RiskAnalysisAdapter.RiskAnalysisItem("IT402", "Web Development Systems", "College of Computer Studies • 32 Enrolled", "Primary Risk Factor: Low Assignment Submission Rate", "Medium Risk (3)"));
        riskAnalysisList.add(new RiskAnalysisAdapter.RiskAnalysisItem("CS204", "Software Engineering Architecture", "College of Computer Studies • 40 Enrolled", "Primary Risk Factor: Low Midterm Exam Scores", "Low Risk (2)"));
        riskAnalysisList.add(new RiskAnalysisAdapter.RiskAnalysisItem("IS201", "Enterprise Systems Architecture", "College of Computer Studies • 28 Enrolled", "Primary Risk Factor: Irregular Attendance", "Medium Risk (3)"));

        riskAnalysisAdapter = new RiskAnalysisAdapter(requireContext(), riskAnalysisList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.course_risk_analysis_summary)
                    .setMessage(getString(R.string.course_code) + item.courseCode + "\n" +
                            "Course Name: " + item.courseName + "\n" +
                            "Department Cohort: " + item.departmentCohort + "\n\n" +
                            "Root Cause Risk Diagnosis:\n" +
                            item.primaryRiskReason + "\n\n" +
                            "Current Risk Level: " + item.riskBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvCourseRiskAnalysis.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCourseRiskAnalysis.setAdapter(riskAnalysisAdapter);
    }

    private List<AtRiskStudentAdapter.AtRiskStudent> getSampleAtRiskStudents() {
        List<AtRiskStudentAdapter.AtRiskStudent> list = new ArrayList<>();

        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023002", "Dara", "Data Science", "2nd Year", "High Risk",
                "2.10", "58%", "12%",
                "• Low GPA (< 2.20)", "• Low assignment scores", "• High absence rate (> 15%)",
                "#FFE4E6", "#FF4055",
                Arrays.asList("Low GPA", "Low Scores", "High Absence", "Multiple Issues")
        ));

        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023003", "Rathana", "Web Development", "3rd Year", "High Risk",
                "2.75", "62%", "10%",
                "• Low assignment scores", "• Irregular class attendance", "• Declining performance",
                "#FEF3C7", "#D97706",
                Arrays.asList("Low Scores", "High Absence")
        ));

        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023005", "Sreyneang", "Artificial Intelligence", "3rd Year", "High Risk",
                "2.30", "60%", "14%",
                "• Low GPA (< 2.40)", "• High absence rate (> 15%)", "• Missed quiz submissions",
                "#FFE4E6", "#FF4055",
                Arrays.asList("Low GPA", "Low Scores", "High Absence")
        ));

        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023006", "Chenda", "Data Structures", "2nd Year", "At Risk",
                "2.10", "65%", "8%",
                "• Low GPA (< 2.20)", "• Low quiz scores", "• Needs additional academic support",
                "#F3E8FF", "#7C3AED",
                Arrays.asList("Low GPA", "Low Scores")
        ));

        list.add(new AtRiskStudentAdapter.AtRiskStudent(
                "STU023007", "Kimsan", "Database Systems", "3rd Year", "At Risk",
                "2.95", "68%", "9%",
                "• Low assignment scores", "• Irregular attendance", "• Needs study guidance",
                "#FFEDD5", "#D97706",
                Arrays.asList("Low GPA", "Low Scores")
        ));

        return list;
    }
}
