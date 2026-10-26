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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.assessment.EnterScoreActivity;
import com.social7.studentlearnanalytics.teacher.assessment.PendingAssessmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManageScoresFragment extends Fragment {

    private PendingAssessmentAdapter adapter;
    private String selectedCategory = "All";
    private String searchQuery = "";

    private TextView tvSelectedCourseName;
    private TextView tvTotalPendingCount;
    private TextView tvTotalPendingSubtext;
    private TextView tvCountAssignments, tvCountQuizzes, tvCountExams;

    private MaterialButton btnFilterAllPending;
    private MaterialButton btnFilterAssignments;
    private MaterialButton btnFilterQuizzes;
    private MaterialButton btnFilterExams;
    private MaterialButton btnFilterOverdue;
    private LinearLayout btnFilterDropdown;

    public ManageScoresFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_manage_scores,
                container,
                false
        );

        ImageView btnBack = view.findViewById(R.id.btnTeacherPendingScoresBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        tvSelectedCourseName = view.findViewById(R.id.tvSelectedCourseName);
        LinearLayout btnCourseSelector = view.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        tvTotalPendingCount = view.findViewById(R.id.tvTotalPendingCount);
        tvTotalPendingSubtext = view.findViewById(R.id.tvTotalPendingSubtext);
        tvCountAssignments = view.findViewById(R.id.tvCountAssignments);
        tvCountQuizzes = view.findViewById(R.id.tvCountQuizzes);
        tvCountExams = view.findViewById(R.id.tvCountExams);

        btnFilterAllPending = view.findViewById(R.id.btnFilterAllPending);
        btnFilterAssignments = view.findViewById(R.id.btnFilterAssignments);
        btnFilterQuizzes = view.findViewById(R.id.btnFilterQuizzes);
        btnFilterExams = view.findViewById(R.id.btnFilterExams);
        btnFilterOverdue = view.findViewById(R.id.btnFilterOverdue);
        btnFilterDropdown = view.findViewById(R.id.btnFilterDropdown);

        if (btnFilterAllPending != null) btnFilterAllPending.setOnClickListener(v -> setFilterCategory("All"));
        if (btnFilterAssignments != null) btnFilterAssignments.setOnClickListener(v -> setFilterCategory("Assignments"));
        if (btnFilterQuizzes != null) btnFilterQuizzes.setOnClickListener(v -> setFilterCategory("Quizzes"));
        if (btnFilterExams != null) btnFilterExams.setOnClickListener(v -> setFilterCategory("Exams"));
        if (btnFilterOverdue != null) btnFilterOverdue.setOnClickListener(v -> setFilterCategory("Overdue"));
        if (btnFilterDropdown != null) btnFilterDropdown.setOnClickListener(this::showFilterDropdown);

        EditText edtSearch = view.findViewById(R.id.edtSearchPending);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (adapter != null) {
                        adapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        RecyclerView rvPending = view.findViewById(R.id.rvPendingAssessments);
        if (rvPending != null) {
            rvPending.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<PendingAssessmentAdapter.PendingItem> items = getSamplePendingItems("CS101");
            adapter = new PendingAssessmentAdapter(requireContext(), items, item -> {
                Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                intent.putExtra("assessment_id", item.id);
                intent.putExtra("assessment_title", item.title);
                intent.putExtra("course_code", item.courseCode);
                startActivity(intent);
            });
            rvPending.setAdapter(adapter);
        }

        loadCoursePendingData("CS101");

        return view;
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Data Structures");
        popup.getMenu().add(0, 2, 1, "CS201 - Computer Networks");
        popup.getMenu().add(0, 3, 2, "CS301 - Mobile App Development");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                loadCoursePendingData("CS101");
            } else if (item.getItemId() == 2) {
                loadCoursePendingData("CS201");
            } else if (item.getItemId() == 3) {
                loadCoursePendingData("CS301");
            }
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

        if (adapter != null) {
            adapter.filter(searchQuery, selectedCategory);
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
        if (adapter != null) {
            adapter.filter(searchQuery, selectedCategory);
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

        // Group 1: CS101 - Data Structures (5 Pending Scores)
        items.add(new PendingAssessmentAdapter.PendingItem("CS101", "Data Structures", "Computer Science Department", 5, R.drawable.bg_course_icon_blue));
        items.add(new PendingAssessmentAdapter.PendingItem("A101", "CS101", "Assignment 2: Linked Lists", "Assignment", 100, "Oct 10, 2024", "Overdue", true, 18, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("Q101", "CS101", "Quiz 1: Arrays and Complexity", "Quiz", 50, "Oct 15, 2024", "Due in 2 days", false, 25, 35));
        items.add(new PendingAssessmentAdapter.PendingItem("E101", "CS101", "Midterm Exam", "Exam", 100, "Oct 25, 2024", "Due in 12 days", false, 12, 35));

        // Group 2: CS201 - Computer Networks (4 Pending Scores)
        items.add(new PendingAssessmentAdapter.PendingItem("CS201", "Computer Networks", "Computer Science Department", 4, R.drawable.bg_course_icon_green));
        items.add(new PendingAssessmentAdapter.PendingItem("Q201", "CS201", "Quiz 2: Network Protocols", "Quiz", 50, "Oct 12, 2024", "Overdue", true, 20, 32));
        items.add(new PendingAssessmentAdapter.PendingItem("A201", "CS201", "Assignment 3: Network Security", "Assignment", 100, "Oct 18, 2024", "Due in 5 days", false, 28, 32));

        // Group 3: CS301 - Mobile App Development (3 Pending Scores)
        items.add(new PendingAssessmentAdapter.PendingItem("CS301", "Mobile App Development", "Computer Science Department", 3, R.drawable.bg_course_icon_orange));
        items.add(new PendingAssessmentAdapter.PendingItem("P301", "CS301", "Final Project", "Project", 100, "Oct 28, 2024", "Due in 15 days", false, 20, 27));

        return items;
    }
}
