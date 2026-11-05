package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.assessment.EnterScoreActivity;
import com.social7.studentlearnanalytics.teacher.assessment.PendingAssessmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManageScoresFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabScoreOverview, tabScorePending, tabScoreAssignments, tabScoreQuizzes, tabScoreExams, tabScoreGradebook;

    // Sub-Tab Containers
    private View secScoreOverviewContainer, secScorePendingContainer, secScoreAssignmentsContainer, secScoreQuizzesContainer, secScoreExamsContainer, secScoreGradebookContainer;

    // Sub-Tab 1: Pending Scores
    private PendingAssessmentAdapter pendingAdapter;
    private RecyclerView rvPendingAssessments;
    private TextView tvSelectedCourseName, tvTotalPendingCount, tvTotalPendingSubtext, tvCountAssignments, tvCountQuizzes, tvCountExams;
    private MaterialButton btnFilterAllPending, btnFilterAssignments, btnFilterQuizzes, btnFilterExams, btnFilterOverdue;
    private String selectedCategory = "All";
    private String searchQuery = "";

    // Sub-Tab 2: Assignment Scores
    private RecyclerView rvAssignmentScoresList;
    private PendingAssessmentAdapter assignmentAdapter;

    // Sub-Tab 3: Quiz Scores
    private RecyclerView rvQuizScoresList;
    private PendingAssessmentAdapter quizAdapter;

    // Sub-Tab 4: Exam Scores
    private RecyclerView rvExamScoresList;
    private PendingAssessmentAdapter examAdapter;

    // Sub-Tab 5: Gradebook
    private RecyclerView rvGradebookRosterList;
    private GradebookAdapter gradebookAdapter;
    private final List<GradebookAdapter.GradebookItem> gradebookList = new ArrayList<>();

    // Current Selected Sub-Tab Flag (0=Overview, 1=Pending, 2=Assignments, 3=Quizzes, 4=Exams, 5=Gradebook)
    private int selectedSubTab = 0;

    public ManageScoresFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_manage_scores, container, false);

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view,
                    "Score Management",
                    "Manage student grades, assessments, and gradebook"
            );
        }

        // Sub-Tab Buttons
        tabScoreOverview = view.findViewById(R.id.tabScoreOverview);
        tabScorePending = view.findViewById(R.id.tabScorePending);
        tabScoreAssignments = view.findViewById(R.id.tabScoreAssignments);
        tabScoreQuizzes = view.findViewById(R.id.tabScoreQuizzes);
        tabScoreExams = view.findViewById(R.id.tabScoreExams);
        tabScoreGradebook = view.findViewById(R.id.tabScoreGradebook);

        // Sub-Tab Containers
        secScoreOverviewContainer = view.findViewById(R.id.secScoreOverviewContainer);
        secScorePendingContainer = view.findViewById(R.id.secScorePendingContainer);
        secScoreAssignmentsContainer = view.findViewById(R.id.secScoreAssignmentsContainer);
        secScoreQuizzesContainer = view.findViewById(R.id.secScoreQuizzesContainer);
        secScoreExamsContainer = view.findViewById(R.id.secScoreExamsContainer);
        secScoreGradebookContainer = view.findViewById(R.id.secScoreGradebookContainer);

        // Bind Sub-views
        bindPendingScoresViews(view);
        bindAssignmentScoresViews(view);
        bindQuizScoresViews(view);
        bindExamScoresViews(view);
        bindGradebookViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabScoreOverview != null) tabScoreOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabScorePending != null) tabScorePending.setOnClickListener(v -> selectSubTab(1));
        if (tabScoreAssignments != null) tabScoreAssignments.setOnClickListener(v -> selectSubTab(2));
        if (tabScoreQuizzes != null) tabScoreQuizzes.setOnClickListener(v -> selectSubTab(3));
        if (tabScoreExams != null) tabScoreExams.setOnClickListener(v -> selectSubTab(4));
        if (tabScoreGradebook != null) tabScoreGradebook.setOnClickListener(v -> selectSubTab(5));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secScoreOverviewContainer != null) secScoreOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secScorePendingContainer != null) secScorePendingContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secScoreAssignmentsContainer != null) secScoreAssignmentsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secScoreQuizzesContainer != null) secScoreQuizzesContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secScoreExamsContainer != null) secScoreExamsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
        if (secScoreGradebookContainer != null) secScoreGradebookContainer.setVisibility(subIndex == 5 ? View.VISIBLE : View.GONE);

        if (subIndex == 1) {
            setupPendingScoresList();
        } else if (subIndex == 2) {
            setupAssignmentScoresList();
        } else if (subIndex == 3) {
            setupQuizScoresList();
        } else if (subIndex == 4) {
            setupExamScoresList();
        } else if (subIndex == 5) {
            setupGradebookList();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabScoreOverview, tabScorePending, tabScoreAssignments, tabScoreQuizzes, tabScoreExams, tabScoreGradebook};
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
    // 1. PENDING SCORES SUB-TAB
    // ==========================================
    private void bindPendingScoresViews(View root) {
        tvSelectedCourseName = root.findViewById(R.id.tvSelectedCourseName);
        LinearLayout btnCourseSelector = root.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        tvTotalPendingCount = root.findViewById(R.id.tvTotalPendingCount);
        tvTotalPendingSubtext = root.findViewById(R.id.tvTotalPendingSubtext);
        tvCountAssignments = root.findViewById(R.id.tvCountAssignments);
        tvCountQuizzes = root.findViewById(R.id.tvCountQuizzes);
        tvCountExams = root.findViewById(R.id.tvCountExams);

        EditText edtSearch = root.findViewById(R.id.edtSearchPending);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (pendingAdapter != null) {
                        pendingAdapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvPendingAssessments = root.findViewById(R.id.rvPendingAssessments);
    }

    private void setupPendingScoresList() {
        if (rvPendingAssessments == null || pendingAdapter != null) return;

        rvPendingAssessments.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<PendingAssessmentAdapter.PendingItem> items = getSamplePendingItems("CS101");
        pendingAdapter = new PendingAssessmentAdapter(requireContext(), items, item -> {
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("assessment_id", item.id);
            intent.putExtra("assessment_title", item.title);
            intent.putExtra("course_code", item.courseCode);
            startActivity(intent);
        });
        rvPendingAssessments.setAdapter(pendingAdapter);
        loadCoursePendingData("CS101");
    }

    // ==========================================
    // 2. ASSIGNMENT SCORES SUB-TAB
    // ==========================================
    private void bindAssignmentScoresViews(View root) {
        rvAssignmentScoresList = root.findViewById(R.id.rvAssignmentScoresList);
    }

    private void setupAssignmentScoresList() {
        if (rvAssignmentScoresList == null || assignmentAdapter != null) return;

        rvAssignmentScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<PendingAssessmentAdapter.PendingItem> items = new ArrayList<>();
        items.add(new PendingAssessmentAdapter.PendingItem("A101", "CS101", "Assignment 1: Linked Lists & Pointers", "Assignment", 100, "Oct 10, 2024", "Graded", false, 35, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("A102", "CS101", "Assignment 2: Binary Search Trees", "Assignment", 100, "Oct 20, 2024", "Overdue", true, 18, 35));

        assignmentAdapter = new PendingAssessmentAdapter(requireContext(), items, item -> {
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("assessment_id", item.id);
            intent.putExtra("assessment_title", item.title);
            startActivity(intent);
        });
        rvAssignmentScoresList.setAdapter(assignmentAdapter);
    }

    // ==========================================
    // 3. QUIZ SCORES SUB-TAB
    // ==========================================
    private void bindQuizScoresViews(View root) {
        rvQuizScoresList = root.findViewById(R.id.rvQuizScoresList);
    }

    private void setupQuizScoresList() {
        if (rvQuizScoresList == null || quizAdapter != null) return;

        rvQuizScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<PendingAssessmentAdapter.PendingItem> items = new ArrayList<>();
        items.add(new PendingAssessmentAdapter.PendingItem("Q101", "CS101", "Quiz 1: Arrays and Big-O Notation", "Quiz", 50, "Oct 15, 2024", "Due in 2 days", false, 25, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("Q102", "IT402", "Quiz 2: REST APIs & JSON Parsing", "Quiz", 50, "Oct 22, 2024", "Pending", false, 15, 32));

        quizAdapter = new PendingAssessmentAdapter(requireContext(), items, item -> {
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("assessment_id", item.id);
            intent.putExtra("assessment_title", item.title);
            startActivity(intent);
        });
        rvQuizScoresList.setAdapter(quizAdapter);
    }

    // ==========================================
    // 4. EXAM SCORES SUB-TAB
    // ==========================================
    private void bindExamScoresViews(View root) {
        rvExamScoresList = root.findViewById(R.id.rvExamScoresList);
    }

    private void setupExamScoresList() {
        if (rvExamScoresList == null || examAdapter != null) return;

        rvExamScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<PendingAssessmentAdapter.PendingItem> items = new ArrayList<>();
        items.add(new PendingAssessmentAdapter.PendingItem("E101", "CS101", "Midterm Exam — Data Structures", "Exam", 100, "Oct 25, 2024", "Due in 12 days", false, 12, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("E102", "IT402", "Final Exam — Mobile Systems", "Exam", 100, "Nov 15, 2024", "Pending", false, 0, 32));

        examAdapter = new PendingAssessmentAdapter(requireContext(), items, item -> {
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("assessment_id", item.id);
            intent.putExtra("assessment_title", item.title);
            startActivity(intent);
        });
        rvExamScoresList.setAdapter(examAdapter);
    }

    // ==========================================
    // 5. GRADEBOOK SUB-TAB
    // ==========================================
    private void bindGradebookViews(View root) {
        rvGradebookRosterList = root.findViewById(R.id.rvGradebookRosterList);
    }

    private void setupGradebookList() {
        if (rvGradebookRosterList == null || gradebookAdapter != null) return;

        gradebookList.clear();
        gradebookList.add(new GradebookAdapter.GradebookItem("STU23001", "Sophea", "CS101 - Intro to CS • BSIT 4-A", "Assignments: 90% • Quizzes: 88% • Midterm: 85% • Final: 92%", "Grade: A (88.8%)"));
        gradebookList.add(new GradebookAdapter.GradebookItem("STU23002", "Dara", "IT402 - Web Systems • BSIT 4-B", "Assignments: 58% • Quizzes: 52% • Midterm: 54% • Final: 58%", "Grade: F (55.5%)"));
        gradebookList.add(new GradebookAdapter.GradebookItem("STU23003", "Rathana", "CS204 - Algorithms • BSCS 3-A", "Assignments: 68% • Quizzes: 62% • Midterm: 65% • Final: 68%", "Grade: C (65.8%)"));
        gradebookList.add(new GradebookAdapter.GradebookItem("STU23004", "Sokha", "CS101 - Intro to CS • BSCS 3-B", "Assignments: 85% • Quizzes: 82% • Midterm: 84% • Final: 86%", "Grade: B+ (84.2%)"));
        gradebookList.add(new GradebookAdapter.GradebookItem("STU23005", "Bopha", "IS201 - Enterprise Arch • BSIS 2-A", "Assignments: 78% • Quizzes: 75% • Midterm: 72% • Final: 78%", "Grade: B (75.8%)"));

        gradebookAdapter = new GradebookAdapter(requireContext(), gradebookList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Student Gradebook Breakdown")
                    .setMessage("Student ID: " + item.studentId + "\n" +
                            "Student Name: " + item.studentName + "\n" +
                            "Course & Class: " + item.courseClass + "\n\n" +
                            "Academic Score Breakdown:\n" +
                            item.gradeBreakdown + "\n\n" +
                            "Composite Final Result: " + item.gradeBadge)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvGradebookRosterList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGradebookRosterList.setAdapter(gradebookAdapter);
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Data Structures");
        popup.getMenu().add(0, 2, 1, "CS201 - Computer Networks");
        popup.getMenu().add(0, 3, 2, "CS301 - Mobile App Development");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) loadCoursePendingData("CS101");
            else if (item.getItemId() == 2) loadCoursePendingData("CS201");
            else if (item.getItemId() == 3) loadCoursePendingData("CS301");
            return true;
        });
        popup.show();
    }

    private void showFilterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "All Pending Categories");
        popup.getMenu().add(0, 2, 1, "Assignments");
        popup.getMenu().add(0, 3, 2, "Quizzes");
        popup.getMenu().add(0, 4, 3, "Exams");
        popup.getMenu().add(0, 5, 4, "Overdue");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) setFilterCategory("All");
            else if (item.getItemId() == 2) setFilterCategory("Assignments");
            else if (item.getItemId() == 3) setFilterCategory("Quizzes");
            else if (item.getItemId() == 4) setFilterCategory("Exams");
            else if (item.getItemId() == 5) setFilterCategory("Overdue");
            return true;
        });
        popup.show();
    }

    private void loadCoursePendingData(String courseCode) {
        String courseTitle;
        if ("CS201".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS201 - Computer Networks";
        } else if ("CS301".equalsIgnoreCase(courseCode)) {
            courseTitle = "CS301 - Mobile App Development";
        } else {
            courseTitle = "CS101 - Data Structures";
        }

        if (tvSelectedCourseName != null) tvSelectedCourseName.setText(courseTitle);

        if (pendingAdapter != null) {
            pendingAdapter.filter(searchQuery, selectedCategory);
        }

        if (tvTotalPendingCount != null) tvTotalPendingCount.setText("12");
        if (tvTotalPendingSubtext != null) tvTotalPendingSubtext.setText("out of 28 assessments");
        if (tvCountAssignments != null) tvCountAssignments.setText("6");
        if (tvCountQuizzes != null) tvCountQuizzes.setText("4");
        if (tvCountExams != null) tvCountExams.setText("2");
    }

    private void setFilterCategory(String category) {
        this.selectedCategory = category;
        updateFilterButtonStyles();
        if (pendingAdapter != null) {
            pendingAdapter.filter(searchQuery, selectedCategory);
        }
    }

    private void updateFilterButtonStyles() {
        int primaryBlue = Color.parseColor("#2563FF");
        int white = Color.WHITE;

        if (btnFilterAllPending != null) {
            if ("All".equalsIgnoreCase(selectedCategory)) {
                btnFilterAllPending.setBackgroundColor(primaryBlue);
                btnFilterAllPending.setTextColor(white);
            } else {
                btnFilterAllPending.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterAllPending.setTextColor(primaryBlue);
            }
        }

        if (btnFilterAssignments != null) {
            if ("Assignments".equalsIgnoreCase(selectedCategory)) {
                btnFilterAssignments.setBackgroundColor(primaryBlue);
                btnFilterAssignments.setTextColor(white);
            } else {
                btnFilterAssignments.setBackgroundColor(Color.parseColor("#FFEBEF"));
                btnFilterAssignments.setTextColor(Color.parseColor("#FF4055"));
            }
        }

        if (btnFilterQuizzes != null) {
            if ("Quizzes".equalsIgnoreCase(selectedCategory)) {
                btnFilterQuizzes.setBackgroundColor(primaryBlue);
                btnFilterQuizzes.setTextColor(white);
            } else {
                btnFilterQuizzes.setBackgroundColor(Color.parseColor("#FFF5E6"));
                btnFilterQuizzes.setTextColor(Color.parseColor("#FF9B16"));
            }
        }

        if (btnFilterExams != null) {
            if ("Exams".equalsIgnoreCase(selectedCategory)) {
                btnFilterExams.setBackgroundColor(primaryBlue);
                btnFilterExams.setTextColor(white);
            } else {
                btnFilterExams.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterExams.setTextColor(primaryBlue);
            }
        }

        if (btnFilterOverdue != null) {
            if ("Overdue".equalsIgnoreCase(selectedCategory)) {
                btnFilterOverdue.setBackgroundColor(primaryBlue);
                btnFilterOverdue.setTextColor(white);
            } else {
                btnFilterOverdue.setBackgroundColor(Color.parseColor("#FFEBEF"));
                btnFilterOverdue.setTextColor(Color.parseColor("#FF4055"));
            }
        }
    }

    private List<PendingAssessmentAdapter.PendingItem> getSamplePendingItems(String filterCourse) {
        List<PendingAssessmentAdapter.PendingItem> items = new ArrayList<>();

        items.add(new PendingAssessmentAdapter.PendingItem("CS101", "Data Structures", "Computer Science Department", 5, R.drawable.bg_course_icon_blue));
        items.add(new PendingAssessmentAdapter.PendingItem("A101", "CS101", "Assignment 2: Linked Lists", "Assignment", 100, "Oct 10, 2024", "Overdue", true, 18, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("Q101", "CS101", "Quiz 1: Arrays and Complexity", "Quiz", 50, "Oct 15, 2024", "Due in 2 days", false, 25, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("E101", "CS101", "Midterm Exam", "Exam", 100, "Oct 25, 2024", "Due in 12 days", false, 12, 35));

        items.add(new PendingAssessmentAdapter.PendingItem("CS201", "Computer Networks", "Computer Science Department", 4, R.drawable.bg_course_icon_green));
        items.add(new PendingAssessmentAdapter.PendingItem("Q201", "CS201", "Quiz 2: Network Protocols", "Quiz", 50, "Oct 12, 2024", "Overdue", true, 20, 32));
        items.add(new PendingAssessmentAdapter.PendingItem("A201", "CS201", "Assignment 3: Network Security", "Assignment", 100, "Oct 18, 2024", "Due in 5 days", false, 28, 32));

        items.add(new PendingAssessmentAdapter.PendingItem("CS301", "Mobile App Development", "Computer Science Department", 3, R.drawable.bg_course_icon_orange));
        items.add(new PendingAssessmentAdapter.PendingItem("P301", "CS301", "Final Project", "Project", 100, "Oct 28, 2024", "Due in 15 days", false, 20, 27));

        return items;
    }
}
