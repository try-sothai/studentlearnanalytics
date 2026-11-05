package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.student.assignment.SubmitAssignmentActivity;
import com.social7.studentlearnanalytics.student.quiz.TakeQuizActivity;
import com.social7.studentlearnanalytics.teacher.GradebookAdapter;
import com.social7.studentlearnanalytics.teacher.assessment.AssignmentAdapter;
import com.social7.studentlearnanalytics.teacher.attendance.AttendanceAdapter;

import java.util.ArrayList;
import java.util.List;

public class LearningAnalyticsFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabAnalyticsOverview, tabAnalyticsAttendance, tabAnalyticsAssignments, tabAnalyticsQuizzes, tabAnalyticsExams, tabAnalyticsGpa;

    // Sub-Tab Containers
    private View secAnalyticsOverviewContainer, secAnalyticsAttendanceContainer, secAnalyticsAssignmentsContainer, secAnalyticsQuizzesContainer, secAnalyticsExamsContainer, secAnalyticsGpaContainer;

    // Sub-Tab 0: Overview
    private LineChart lineChartAnalyticsTrend;

    // Sub-Tab 1: Attendance
    private PieChart pieChartAttendanceDetail;
    private RecyclerView rvAttendanceSessionRoster;
    private AttendanceAdapter attendanceAdapter;

    // Sub-Tab 2: Assignments
    private RecyclerView rvAnalyticsAssignmentsRoster;
    private AssignmentAdapter assignmentAdapter;

    // Sub-Tab 3: Quizzes
    private RecyclerView rvAnalyticsQuizzesRoster;
    private AssignmentAdapter quizAdapter;

    // Sub-Tab 4: Exams
    private RecyclerView rvAnalyticsExamsRoster;
    private AssignmentAdapter examAdapter;

    // Sub-Tab 5: GPA
    private RecyclerView rvGpaHistoryRoster;
    private GradebookAdapter gpaHistoryAdapter;

    // Selected Sub-Tab Flag (0=Overview, 1=Attendance, 2=Assignments, 3=Quizzes, 4=Exams, 5=GPA)
    private int selectedSubTab = 0;

    public LearningAnalyticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_fragment_learning_analytics, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view,
                    "Learning Analytics",
                    "Track, Analyze & Improve"
            );
        }

        // Sub-Tab Buttons
        tabAnalyticsOverview = view.findViewById(R.id.tabAnalyticsOverview);
        tabAnalyticsAttendance = view.findViewById(R.id.tabAnalyticsAttendance);
        tabAnalyticsAssignments = view.findViewById(R.id.tabAnalyticsAssignments);
        tabAnalyticsQuizzes = view.findViewById(R.id.tabAnalyticsQuizzes);
        tabAnalyticsExams = view.findViewById(R.id.tabAnalyticsExams);
        tabAnalyticsGpa = view.findViewById(R.id.tabAnalyticsGpa);

        // Containers
        secAnalyticsOverviewContainer = view.findViewById(R.id.secAnalyticsOverviewContainer);
        secAnalyticsAttendanceContainer = view.findViewById(R.id.secAnalyticsAttendanceContainer);
        secAnalyticsAssignmentsContainer = view.findViewById(R.id.secAnalyticsAssignmentsContainer);
        secAnalyticsQuizzesContainer = view.findViewById(R.id.secAnalyticsQuizzesContainer);
        secAnalyticsExamsContainer = view.findViewById(R.id.secAnalyticsExamsContainer);
        secAnalyticsGpaContainer = view.findViewById(R.id.secAnalyticsGpaContainer);

        // Bind Sub-views
        bindOverviewViews(view);
        bindAttendanceViews(view);
        bindAssignmentsViews(view);
        bindQuizzesViews(view);
        bindExamsViews(view);
        bindGpaViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabAnalyticsOverview != null) tabAnalyticsOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabAnalyticsAttendance != null) tabAnalyticsAttendance.setOnClickListener(v -> selectSubTab(1));
        if (tabAnalyticsAssignments != null) tabAnalyticsAssignments.setOnClickListener(v -> selectSubTab(2));
        if (tabAnalyticsQuizzes != null) tabAnalyticsQuizzes.setOnClickListener(v -> selectSubTab(3));
        if (tabAnalyticsExams != null) tabAnalyticsExams.setOnClickListener(v -> selectSubTab(4));
        if (tabAnalyticsGpa != null) tabAnalyticsGpa.setOnClickListener(v -> selectSubTab(5));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secAnalyticsOverviewContainer != null) secAnalyticsOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secAnalyticsAttendanceContainer != null) secAnalyticsAttendanceContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secAnalyticsAssignmentsContainer != null) secAnalyticsAssignmentsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secAnalyticsQuizzesContainer != null) secAnalyticsQuizzesContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secAnalyticsExamsContainer != null) secAnalyticsExamsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
        if (secAnalyticsGpaContainer != null) secAnalyticsGpaContainer.setVisibility(subIndex == 5 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewTrendChart();
        } else if (subIndex == 1) {
            setupAttendanceSection();
        } else if (subIndex == 2) {
            setupAssignmentsSection();
        } else if (subIndex == 3) {
            setupQuizzesSection();
        } else if (subIndex == 4) {
            setupExamsSection();
        } else if (subIndex == 5) {
            setupGpaSection();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabAnalyticsOverview, tabAnalyticsAttendance, tabAnalyticsAssignments, tabAnalyticsQuizzes, tabAnalyticsExams, tabAnalyticsGpa};
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
        lineChartAnalyticsTrend = root.findViewById(R.id.lineChartAnalyticsTrend);
    }

    private void setupOverviewTrendChart() {
        if (lineChartAnalyticsTrend == null || lineChartAnalyticsTrend.getData() != null) return;

        List<Entry> scoreEntries = new ArrayList<>();
        scoreEntries.add(new Entry(0, 78f));
        scoreEntries.add(new Entry(1, 84f));
        scoreEntries.add(new Entry(2, 88f));
        scoreEntries.add(new Entry(3, 92f));

        LineDataSet scoreSet = new LineDataSet(scoreEntries, "Score Performance");
        scoreSet.setColor(Color.parseColor("#3155F5"));
        scoreSet.setLineWidth(2.5f);
        scoreSet.setCircleColor(Color.parseColor("#3155F5"));

        LineData lineData = new LineData(scoreSet);
        lineChartAnalyticsTrend.setData(lineData);

        XAxis xAxis = lineChartAnalyticsTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Month 1", "Month 2", "Month 3", "Month 4"}));

        lineChartAnalyticsTrend.getAxisRight().setEnabled(false);
        lineChartAnalyticsTrend.getDescription().setEnabled(false);
        lineChartAnalyticsTrend.animateX(800);
        lineChartAnalyticsTrend.invalidate();
    }

    // ==========================================
    // 1. ATTENDANCE SUB-TAB
    // ==========================================
    private void bindAttendanceViews(View root) {
        pieChartAttendanceDetail = root.findViewById(R.id.pieChartAttendanceDetail);
        rvAttendanceSessionRoster = root.findViewById(R.id.rvAttendanceSessionRoster);
    }

    private void setupAttendanceSection() {
        if (pieChartAttendanceDetail != null && pieChartAttendanceDetail.getData() == null) {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(98.5f, "Present"));
            entries.add(new PieEntry(1.5f, "Absent"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{Color.parseColor("#10B981"), Color.parseColor("#EF4444")});
            dataSet.setSliceSpace(3f);

            PieData pieData = new PieData(dataSet);
            pieChartAttendanceDetail.setData(pieData);
            pieChartAttendanceDetail.setCenterText("98.5%\nRate");
            pieChartAttendanceDetail.setCenterTextSize(16f);
            pieChartAttendanceDetail.getDescription().setEnabled(false);
            pieChartAttendanceDetail.invalidate();
        }

        if (rvAttendanceSessionRoster != null && attendanceAdapter == null) {
            rvAttendanceSessionRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<AttendanceAdapter.AttendanceRecordItem> list = new ArrayList<>();
            list.add(new AttendanceAdapter.AttendanceRecordItem("May 10, 2026 • CS101 Intro to CS", "Present"));
            list.add(new AttendanceAdapter.AttendanceRecordItem("May 08, 2026 • IT402 Web Development", "Present"));
            list.add(new AttendanceAdapter.AttendanceRecordItem("May 06, 2026 • CS204 Software Architecture", "Absent"));
            list.add(new AttendanceAdapter.AttendanceRecordItem("May 03, 2026 • IS201 Enterprise Systems", "Present"));

            attendanceAdapter = new AttendanceAdapter(requireContext(), list, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Session Attendance Log")
                        .setMessage(item.date + "\nStatus: " + item.status)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvAttendanceSessionRoster.setAdapter(attendanceAdapter);
        }
    }

    // ==========================================
    // 2. ASSIGNMENTS SUB-TAB
    // ==========================================
    private void bindAssignmentsViews(View root) {
        rvAnalyticsAssignmentsRoster = root.findViewById(R.id.rvAnalyticsAssignmentsRoster);
    }

    private void setupAssignmentsSection() {
        if (rvAnalyticsAssignmentsRoster == null || assignmentAdapter != null) return;

        rvAnalyticsAssignmentsRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<AssignmentAdapter.AssignmentCardItem> items = new ArrayList<>();
        items.add(new AssignmentAdapter.AssignmentCardItem("A101", "CS101", "Assignment 1: Linked Lists & Pointers", "Instructor: Prof. Emily Johnson", "Submitted: Oct 10 • Grade: 88/100 (A)", "Graded"));
        items.add(new AssignmentAdapter.AssignmentCardItem("A102", "CS101", "Assignment 2: Binary Search Trees", "Instructor: Prof. Emily Johnson", "Due: Oct 28 • Pending Submission", "Pending"));

        assignmentAdapter = new AssignmentAdapter(requireContext(), items, new AssignmentAdapter.OnAssignmentActionClickListener() {
            @Override
            public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                Intent intent = new Intent(requireContext(), SubmitAssignmentActivity.class);
                intent.putExtra("assignment_id", item.id);
                intent.putExtra("assignment_title", item.title);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                Toast.makeText(requireContext(), "Opening " + item.title + "...", Toast.LENGTH_SHORT).show();
            }
        });
        rvAnalyticsAssignmentsRoster.setAdapter(assignmentAdapter);
    }

    // ==========================================
    // 3. QUIZZES SUB-TAB
    // ==========================================
    private void bindQuizzesViews(View root) {
        rvAnalyticsQuizzesRoster = root.findViewById(R.id.rvAnalyticsQuizzesRoster);
    }

    private void setupQuizzesSection() {
        if (rvAnalyticsQuizzesRoster == null || quizAdapter != null) return;

        rvAnalyticsQuizzesRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<AssignmentAdapter.AssignmentCardItem> items = new ArrayList<>();
        items.add(new AssignmentAdapter.AssignmentCardItem("Q101", "CS101", "Quiz 1: Big-O Complexity & Sorting", "Instructor: Prof. Emily Johnson", "Completed: Oct 15 • Score: 46/50 (92%)", "Completed"));
        items.add(new AssignmentAdapter.AssignmentCardItem("Q102", "IT402", "Quiz 2: REST APIs & JSON Parsing", "Instructor: Prof. Alan Santos", "Due: Oct 22 • 1 Attempt Available", "Active"));

        quizAdapter = new AssignmentAdapter(requireContext(), items, new AssignmentAdapter.OnAssignmentActionClickListener() {
            @Override
            public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                Intent intent = new Intent(requireContext(), TakeQuizActivity.class);
                intent.putExtra("quiz_id", item.id);
                intent.putExtra("quiz_title", item.title);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                Toast.makeText(requireContext(), "Opening " + item.title + "...", Toast.LENGTH_SHORT).show();
            }
        });
        rvAnalyticsQuizzesRoster.setAdapter(quizAdapter);
    }

    // ==========================================
    // 4. EXAMS SUB-TAB
    // ==========================================
    private void bindExamsViews(View root) {
        rvAnalyticsExamsRoster = root.findViewById(R.id.rvAnalyticsExamsRoster);
    }

    private void setupExamsSection() {
        if (rvAnalyticsExamsRoster == null || examAdapter != null) return;

        rvAnalyticsExamsRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<AssignmentAdapter.AssignmentCardItem> items = new ArrayList<>();
        items.add(new AssignmentAdapter.AssignmentCardItem("E101", "CS101", "Midterm Examination: Data Structures", "Lab 402 • Building B", "Exam Date: Oct 28 • Score: 85/100 (Grade A)", "Completed"));
        items.add(new AssignmentAdapter.AssignmentCardItem("E102", "IT402", "Final Examination: Mobile Systems", "Auditorium A", "Exam Date: Nov 15 • Scheduled", "Upcoming"));

        examAdapter = new AssignmentAdapter(requireContext(), items, new AssignmentAdapter.OnAssignmentActionClickListener() {
            @Override
            public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Exam Details — " + item.title)
                        .setMessage(item.classCohort + "\n" + item.details)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                Toast.makeText(requireContext(), "Opening " + item.title + "...", Toast.LENGTH_SHORT).show();
            }
        });
        rvAnalyticsExamsRoster.setAdapter(examAdapter);
    }

    // ==========================================
    // 5. GPA SUB-TAB
    // ==========================================
    private void bindGpaViews(View root) {
        rvGpaHistoryRoster = root.findViewById(R.id.rvGpaHistoryRoster);
    }

    private void setupGpaSection() {
        if (rvGpaHistoryRoster == null || gpaHistoryAdapter != null) return;

        rvGpaHistoryRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<GradebookAdapter.GradebookItem> list = new ArrayList<>();
        list.add(new GradebookAdapter.GradebookItem("SEM2024-1", "Semester 1 (2024-2025)", "CS101, IT402, CS204, IS201", "Completed Credits: 18 • GPA: 3.68", "Honor Roll"));
        list.add(new GradebookAdapter.GradebookItem("SEM2023-2", "Semester 2 (2023-2024)", "MA101, EN101, CS100", "Completed Credits: 18 • GPA: 3.62", "Honor Roll"));
        list.add(new GradebookAdapter.GradebookItem("SEM2023-1", "Semester 1 (2023-2024)", "MA100, CS099", "Completed Credits: 15 • GPA: 3.58", "Deans List"));

        gpaHistoryAdapter = new GradebookAdapter(requireContext(), list, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Semester GPA History Record")
                    .setMessage(item.studentName + "\n" +
                            "Enrolled Courses: " + item.courseClass + "\n" +
                            item.gradeBreakdown + "\n" +
                            "Standing: " + item.gradeBadge)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        rvGpaHistoryRoster.setAdapter(gpaHistoryAdapter);
    }
}
