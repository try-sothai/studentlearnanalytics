package com.social7.studentlearnanalytics.student;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.student.assignment.SubmitAssignmentActivity;
import com.social7.studentlearnanalytics.student.quiz.TakeQuizActivity;
import com.social7.studentlearnanalytics.teacher.assessment.AssignmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class StudentAssessmentFragment extends Fragment {

    private TextView tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams;
    private RecyclerView rvStudentAssessmentList;
    private AssignmentAdapter adapter;
    private final List<AssignmentAdapter.AssignmentCardItem> assessmentList = new ArrayList<>();
    private EditText edtSearchStudentAssessments;
    private String selectedCategory = "Assignment";

    public StudentAssessmentFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_assessment, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view,
                    "My Assessments",
                    "Manage assignments, quizzes, and homework"
            );
        }

        tabStudentAssignments = view.findViewById(R.id.tabStudentAssignments);
        tabStudentHomework = view.findViewById(R.id.tabStudentHomework);
        tabStudentQuizzes = view.findViewById(R.id.tabStudentQuizzes);
        tabStudentExams = view.findViewById(R.id.tabStudentExams);

        setupSubTabListeners();

        edtSearchStudentAssessments = view.findViewById(R.id.edtSearchStudentAssessments);
        if (edtSearchStudentAssessments != null) {
            edtSearchStudentAssessments.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvStudentAssessmentList = view.findViewById(R.id.rvStudentAssessmentList);
        if (rvStudentAssessmentList != null) {
            rvStudentAssessmentList.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadAssessmentsForCategory("Assignment");
        }

        return view;
    }

    private void setupSubTabListeners() {
        if (tabStudentAssignments != null) tabStudentAssignments.setOnClickListener(v -> selectCategory("Assignment"));
        if (tabStudentHomework != null) tabStudentHomework.setOnClickListener(v -> selectCategory("Homework"));
        if (tabStudentQuizzes != null) tabStudentQuizzes.setOnClickListener(v -> selectCategory("Quiz"));
        if (tabStudentExams != null) tabStudentExams.setOnClickListener(v -> selectCategory("Exam"));
    }

    private void selectCategory(String category) {
        this.selectedCategory = category;
        updateSubTabButtonStyles(category);
        loadAssessmentsForCategory(category);
    }

    private void updateSubTabButtonStyles(String activeCategory) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabStudentAssignments, tabStudentHomework, tabStudentQuizzes, tabStudentExams};
        String[] categories = new String[]{"Assignment", "Homework", "Quiz", "Exam"};

        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
            if (tv == null) continue;
            if (categories[i].equalsIgnoreCase(activeCategory)) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void loadAssessmentsForCategory(String category) {
        assessmentList.clear();

        if ("Homework".equalsIgnoreCase(category)) {
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("HW101", "CS101", "Homework 1: Arrays & Pointers Practice", "Instructor: Prof. Chan • Class: BSIT 4-A", "Due: Oct 18, 2024 • Total Marks: 50 pts • Status: Pending Submission", "Pending"));
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("HW102", "IT402", "Homework 2: HTML5 Canvas Exercises", "Instructor: Prof. Santos • Class: BSIT 4-B", "Due: Oct 25, 2024 • Total Marks: 50 pts • Status: Submitted", "Submitted"));
        } else if ("Quiz".equalsIgnoreCase(category)) {
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("Q101", "CS101", "Quiz 1: Big-O Complexity & Sorting", "Instructor: Prof. Chan • 10 Questions", "Duration: 30 Mins • Total Marks: 50 pts • Attempts: 1 Available", "Active"));
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("Q102", "IT402", "Quiz 2: REST APIs & JSON Parsing", "Instructor: Prof. Santos • 12 Questions", "Duration: 25 Mins • Total Marks: 50 pts • Score: 46/50 (92%)", "Completed"));
        } else if ("Exam".equalsIgnoreCase(category)) {
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("E101", "CS101", "Midterm Examination: Data Structures", "Instructor: Prof. Chan • Lab 402 - Building B", "Exam Date: Oct 28, 2024 • Marks: 100 pts • Status: Scheduled", "Scheduled"));
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("E102", "IT402", "Final Examination: Mobile Systems", "Instructor: Prof. Santos • Auditorium A", "Exam Date: Nov 15, 2024 • Marks: 100 pts • Status: Scheduled", "Scheduled"));
        } else {
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("A101", "CS101", "Assignment 1: Linked Lists & Pointers", "Instructor: Prof. Chan • Class: BSIT 4-A", "Due: Oct 20, 2024 • Total Marks: 100 pts • Status: Submitted", "Submitted"));
            assessmentList.add(new AssignmentAdapter.AssignmentCardItem("A102", "CS101", "Assignment 2: Binary Search Trees & Heaps", "Instructor: Prof. Chan • Class: BSIT 4-A", "Due: Oct 28, 2024 • Total Marks: 100 pts • Status: Pending Submission", "Pending"));
        }

        adapter = new AssignmentAdapter(requireContext(), assessmentList, new AssignmentAdapter.OnAssignmentActionClickListener() {
            @Override
            public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                if ("Quiz".equalsIgnoreCase(category)) {
                    Intent intent = new Intent(requireContext(), TakeQuizActivity.class);
                    intent.putExtra("quiz_id", item.id);
                    intent.putExtra("quiz_title", item.title);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(requireContext(), SubmitAssignmentActivity.class);
                    intent.putExtra("assignment_id", item.id);
                    intent.putExtra("assignment_title", item.title);
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                Toast.makeText(requireContext(), "Opening " + item.title + " details...", Toast.LENGTH_SHORT).show();
            }
        });

        if (rvStudentAssessmentList != null) {
            rvStudentAssessmentList.setAdapter(adapter);
        }
    }
}
