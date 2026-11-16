package com.social7.studentlearnanalytics.teacher;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class RecommendationFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabRecOverview, tabRecStudent, tabRecPriority, tabRecIntervention, tabRecHistory;

    // Sub-Tab Containers
    private View secRecOverviewContainer, secRecStudentContainer, secRecPriorityContainer, secRecInterventionContainer, secRecHistoryContainer;

    // Sub-Tab 0: Overview
    private PieChart chartRecommendationPriorityPie;
    private MaterialButton btnGenerateRecs;

    // Sub-Tab 1: Student Recommendations
    private RecyclerView rvStudentRecsList;
    private StudentRecommendationAdapter studentRecAdapter;
    private final List<StudentRecommendationAdapter.StudentRecommendationItem> studentRecList = new ArrayList<>();
    private EditText edtSearchStudentRecs;

    // Sub-Tab 2: Priority Recommendations
    private RecyclerView rvPriorityRecsList;
    private StudentRecommendationAdapter priorityRecAdapter;
    private final List<StudentRecommendationAdapter.StudentRecommendationItem> priorityRecList = new ArrayList<>();

    // Sub-Tab 3: Intervention Plans
    private RecyclerView rvInterventionPlansList;
    private InterventionPlanAdapter interventionAdapter;
    private final List<InterventionPlanAdapter.InterventionPlanItem> interventionList = new ArrayList<>();
    private MaterialButton btnCreateInterventionPlan;

    // Sub-Tab 4: Recommendation History
    private RecyclerView rvRecommendationHistoryList;
    private RecommendationHistoryAdapter historyAdapter;
    private final List<RecommendationHistoryAdapter.RecommendationHistoryItem> historyList = new ArrayList<>();
    private EditText edtSearchRecHistory;

    // Selected Sub-Tab Flag (0=Overview, 1=StudentRecs, 2=PriorityRecs, 3=Interventions, 4=History)
    private int selectedSubTab = 0;

    public RecommendationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.recommendations_management), "AI academic guidance & student support plans"
            );
        }

        // Sub-Tab Buttons
        tabRecOverview = view.findViewById(R.id.tabRecOverview);
        tabRecStudent = view.findViewById(R.id.tabRecStudent);
        tabRecPriority = view.findViewById(R.id.tabRecPriority);
        tabRecIntervention = view.findViewById(R.id.tabRecIntervention);
        tabRecHistory = view.findViewById(R.id.tabRecHistory);

        // Containers
        secRecOverviewContainer = view.findViewById(R.id.secRecOverviewContainer);
        secRecStudentContainer = view.findViewById(R.id.secRecStudentContainer);
        secRecPriorityContainer = view.findViewById(R.id.secRecPriorityContainer);
        secRecInterventionContainer = view.findViewById(R.id.secRecInterventionContainer);
        secRecHistoryContainer = view.findViewById(R.id.secRecHistoryContainer);

        // Bind Sub-views
        bindOverviewViews(view);
        bindStudentRecsViews(view);
        bindPriorityRecsViews(view);
        bindInterventionViews(view);
        bindHistoryViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabRecOverview != null) tabRecOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabRecStudent != null) tabRecStudent.setOnClickListener(v -> selectSubTab(1));
        if (tabRecPriority != null) tabRecPriority.setOnClickListener(v -> selectSubTab(2));
        if (tabRecIntervention != null) tabRecIntervention.setOnClickListener(v -> selectSubTab(3));
        if (tabRecHistory != null) tabRecHistory.setOnClickListener(v -> selectSubTab(4));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secRecOverviewContainer != null) secRecOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secRecStudentContainer != null) secRecStudentContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secRecPriorityContainer != null) secRecPriorityContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secRecInterventionContainer != null) secRecInterventionContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secRecHistoryContainer != null) secRecHistoryContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewPieChart();
        } else if (subIndex == 1) {
            setupStudentRecsList();
        } else if (subIndex == 2) {
            setupPriorityRecsList();
        } else if (subIndex == 3) {
            setupInterventionList();
        } else if (subIndex == 4) {
            setupHistoryList();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabRecOverview, tabRecStudent, tabRecPriority, tabRecIntervention, tabRecHistory};
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
        chartRecommendationPriorityPie = root.findViewById(R.id.chartRecommendationPriorityPie);
        btnGenerateRecs = root.findViewById(R.id.btnGenerateRecs);

        if (btnGenerateRecs != null) {
            btnGenerateRecs.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Generated 4 new AI recommendations for assigned classes!", Toast.LENGTH_LONG).show();
                selectSubTab(1); // Open Student Recs
            });
        }
    }

    private void setupOverviewPieChart() {
        if (chartRecommendationPriorityPie == null || chartRecommendationPriorityPie.getData() != null) return;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(8f, "High Priority"));
        entries.add(new PieEntry(15f, "Medium Priority"));
        entries.add(new PieEntry(19f, "Low Priority"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#EF4444"), Color.parseColor("#F59E0B"), Color.parseColor("#10B981")
        });
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        chartRecommendationPriorityPie.setData(pieData);
        chartRecommendationPriorityPie.setDrawHoleEnabled(true);
        chartRecommendationPriorityPie.setHoleColor(Color.WHITE);
        chartRecommendationPriorityPie.setHoleRadius(65f);
        chartRecommendationPriorityPie.setTransparentCircleRadius(70f);
        chartRecommendationPriorityPie.setCenterText("42\nRecs");
        chartRecommendationPriorityPie.setCenterTextColor(Color.parseColor("#10165C"));
        chartRecommendationPriorityPie.setCenterTextSize(18f);
        chartRecommendationPriorityPie.getDescription().setEnabled(false);
        chartRecommendationPriorityPie.getLegend().setEnabled(false);
        chartRecommendationPriorityPie.animateY(800);
        ChartFontHelper.applyKhmerFont(chartRecommendationPriorityPie.getContext(), chartRecommendationPriorityPie);
        chartRecommendationPriorityPie.invalidate();
    }

    // ==========================================
    // 1. STUDENT RECOMMENDATIONS SUB-TAB
    // ==========================================
    private void bindStudentRecsViews(View root) {
        rvStudentRecsList = root.findViewById(R.id.rvStudentRecsList);
        edtSearchStudentRecs = root.findViewById(R.id.edtSearchStudentRecs);

        if (edtSearchStudentRecs != null) {
            edtSearchStudentRecs.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (studentRecAdapter != null) {
                        studentRecAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupStudentRecsList() {
        if (rvStudentRecsList == null || studentRecAdapter != null) return;

        studentRecList.clear();
        studentRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23001", "Dara", "CS101 - Intro to CS • BSIT 4-A", "AI Rec: Schedule 1-on-1 Tutoring in Data Structures", "GPA: 2.10 • Attendance: 58% • Assignment Avg: 62% • Quiz Avg: 55%", "High Priority"));
        studentRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23002", "Rathana", "IT402 - Web Systems • BSIT 4-B", "AI Rec: Assign Practice Exercises on JavaScript Promises", "GPA: 2.75 • Attendance: 62% • Assignment Avg: 65% • Quiz Avg: 60%", "High Priority"));
        studentRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23003", "Sreyneang", "CS204 - Algorithms • BSCS 3-A", "AI Rec: Issue Attendance Warning & Review Module 3", "GPA: 2.30 • Attendance: 60% • Assignment Avg: 68% • Quiz Avg: 58%", "High Priority"));
        studentRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23004", "Chenda", "CS101 - Intro to CS • BSCS 3-B", "AI Rec: Group Study Session for Recursion & Sorting", "GPA: 3.10 • Attendance: 85% • Assignment Avg: 78% • Quiz Avg: 72%", "Medium Priority"));
        studentRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23005", "Kimsan", "IS201 - Enterprise Arch • BSIS 2-A", "AI Rec: Advisory Review for Midterm Preparation", "GPA: 3.45 • Attendance: 92% • Assignment Avg: 88% • Quiz Avg: 84%", "Low Priority"));

        studentRecAdapter = new StudentRecommendationAdapter(requireContext(), studentRecList, item -> {
            showStudentRecommendationDetailDialog(item);
        });

        rvStudentRecsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudentRecsList.setAdapter(studentRecAdapter);
    }

    private void showStudentRecommendationDetailDialog(StudentRecommendationAdapter.StudentRecommendationItem item) {
        if (!isAdded() || getContext() == null) return;

        String details = "Student Name: " + item.studentName + " (" + item.studentId + ")\n" +
                "Course & Cohort: " + item.courseClass + "\n" +
                "Priority Status: " + item.priorityBadge + "\n\n" +
                "Academic Diagnosis:\n" +
                item.academicStats + "\n\n" +
                "AI Recommendation Strategy:\n" +
                item.recommendationTitle + "\n\n" +
                "Suggested Interventions:\n" +
                "1. Assign targeted remedial exercises on weak topics\n" +
                "2. Conduct weekly 1-on-1 check-in consultation\n" +
                "3. Track assignment recovery submission deadline";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.student_recommendation_detail)
                .setMessage(details)
                .setPositiveButton(R.string.assign_support_plan, (dialog, which) -> {
                    Toast.makeText(requireContext(), "Assigned support plan for " + item.studentName, Toast.LENGTH_SHORT).show();
                    selectSubTab(3); // Open Intervention Plans
                })
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==========================================
    // 2. PRIORITY RECOMMENDATIONS SUB-TAB
    // ==========================================
    private void bindPriorityRecsViews(View root) {
        rvPriorityRecsList = root.findViewById(R.id.rvPriorityRecsList);
    }

    private void setupPriorityRecsList() {
        if (rvPriorityRecsList == null || priorityRecAdapter != null) return;

        priorityRecList.clear();
        priorityRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23001", "Dara", "CS101 - Intro to CS • BSIT 4-A", "URGENT: GPA < 2.20 & Attendance < 60% Warning", "GPA: 2.10 • Attendance: 58% • Assignment Avg: 62%", "High Priority"));
        priorityRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23002", "Rathana", "IT402 - Web Systems • BSIT 4-B", "URGENT: Multiple Failed Quizzes in Web Security", "GPA: 2.75 • Attendance: 62% • Quiz Avg: 60%", "High Priority"));
        priorityRecList.add(new StudentRecommendationAdapter.StudentRecommendationItem("STU23003", "Sreyneang", "CS204 - Algorithms • BSCS 3-A", "URGENT: Consecutive Absences & Missed Submissions", "GPA: 2.30 • Attendance: 60% • Quiz Avg: 58%", "High Priority"));

        priorityRecAdapter = new StudentRecommendationAdapter(requireContext(), priorityRecList, item -> {
            showStudentRecommendationDetailDialog(item);
        });

        rvPriorityRecsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPriorityRecsList.setAdapter(priorityRecAdapter);
    }

    // ==========================================
    // 3. INTERVENTION PLANS SUB-TAB
    // ==========================================
    private void bindInterventionViews(View root) {
        rvInterventionPlansList = root.findViewById(R.id.rvInterventionPlansList);
        btnCreateInterventionPlan = root.findViewById(R.id.btnCreateInterventionPlan);

        if (btnCreateInterventionPlan != null) {
            btnCreateInterventionPlan.setOnClickListener(v -> showCreateInterventionPlanDialog());
        }
    }

    private void setupInterventionList() {
        if (rvInterventionPlansList == null || interventionAdapter != null) return;

        interventionList.clear();
        interventionList.add(new InterventionPlanAdapter.InterventionPlanItem("STU23001", "Dara", "Support Plan: 1-on-1 Academic Tutoring & Remedial Quizzes", "Target Goal: Raise GPA to 2.50 • Timeline: May 12 - Jun 30, 2026", "In Progress"));
        interventionList.add(new InterventionPlanAdapter.InterventionPlanItem("STU23002", "Rathana", "Support Plan: Attendance Warning & Weekly Consultation", "Target Goal: Attendance > 80% • Timeline: May 01 - May 31, 2026", "In Progress"));
        interventionList.add(new InterventionPlanAdapter.InterventionPlanItem("STU23003", "Sreyneang", "Support Plan: Practice Exercises in Algorithms", "Target Goal: Score > 75% • Timeline: Apr 15 - May 15, 2026", "Completed"));

        interventionAdapter = new InterventionPlanAdapter(requireContext(), interventionList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.intervention_plan_status)
                    .setMessage(getString(R.string.student_name_4f287a) + item.studentName + " (" + item.studentId + ")\n" +
                            item.activityTitle + "\n" +
                            item.timeline + "\n\n" +
                            "Current Status: " + item.statusBadge)
                    .setPositiveButton(R.string.mark_completed, (dialog, which) -> {
                        item.statusBadge = "Completed";
                        interventionAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Intervention plan marked as completed!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvInterventionPlansList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInterventionPlansList.setAdapter(interventionAdapter);
    }

    private void showCreateInterventionPlanDialog() {
        View layout = LayoutInflater.from(requireContext()).inflate(R.layout.item_admin_student_recommendation_row, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.create_academic_support_plan);

        android.widget.LinearLayout container = new android.widget.LinearLayout(requireContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilStudent = new TextInputLayout(requireContext());
        tilStudent.setHint("Student Name or ID (e.g. Dara - STU23001)");
        TextInputEditText edtStudent = new TextInputEditText(tilStudent.getContext());
        tilStudent.addView(edtStudent);
        container.addView(tilStudent);

        TextInputLayout tilActivity = new TextInputLayout(requireContext());
        tilActivity.setHint("Support Activity (e.g. Peer Tutoring in CS101)");
        TextInputEditText edtActivity = new TextInputEditText(tilActivity.getContext());
        tilActivity.addView(edtActivity);
        container.addView(tilActivity);

        builder.setView(container);

        builder.setPositiveButton(R.string.create_plan_4c7cd7, (dialog, which) -> {
            String name = edtStudent.getText() != null ? edtStudent.getText().toString().trim() : "";
            String act = edtActivity.getText() != null ? edtActivity.getText().toString().trim() : "";

            if (name.isEmpty()) name = "Dara";
            if (act.isEmpty()) act = "1-on-1 Academic Counseling";

            interventionList.add(0, new InterventionPlanAdapter.InterventionPlanItem(
                    "STU23001", name, "Support Plan: " + act, "Target Goal: Score > 75% • Timeline: May - Jun 2026", "In Progress"
            ));
            if (interventionAdapter != null) interventionAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), R.string.academic_support_plan_created_successfully, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ==========================================
    // 4. RECOMMENDATION HISTORY SUB-TAB
    // ==========================================
    private void bindHistoryViews(View root) {
        rvRecommendationHistoryList = root.findViewById(R.id.rvRecommendationHistoryList);
        edtSearchRecHistory = root.findViewById(R.id.edtSearchRecHistory);

        if (edtSearchRecHistory != null) {
            edtSearchRecHistory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (historyAdapter != null) {
                        historyAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupHistoryList() {
        if (rvRecommendationHistoryList == null || historyAdapter != null) return;

        historyList.clear();
        historyList.add(new RecommendationHistoryAdapter.RecommendationHistoryItem("STU23001", "Dara", "Rec: Additional Tutoring in CS101 Data Structures", "Generated: Jan 10, 2026 • Completed: Mar 15, 2026 • Priority: High", "+12.5% GPA Increase"));
        historyList.add(new RecommendationHistoryAdapter.RecommendationHistoryItem("STU23002", "Rathana", "Rec: Attendance Advisory Warning Agreement", "Generated: Feb 01, 2026 • Completed: Apr 10, 2026 • Priority: High", "+18.0% Attendance Boost"));
        historyList.add(new RecommendationHistoryAdapter.RecommendationHistoryItem("STU23003", "Sreyneang", "Rec: Remedial Practice Exercises in Web Systems", "Generated: Jan 20, 2026 • Completed: Mar 01, 2026 • Priority: Medium", "+15.0% Quiz Score Boost"));

        historyAdapter = new RecommendationHistoryAdapter(requireContext(), historyList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.recommendation_history_detail)
                    .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                            "Student Name: " + item.studentName + "\n" +
                            item.recTitle + "\n\n" +
                            item.recDates + "\n\n" +
                            "Academic Impact Result: " + item.impactBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvRecommendationHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecommendationHistoryList.setAdapter(historyAdapter);
    }
}
