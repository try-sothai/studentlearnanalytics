package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class StudentRecommendationFragment extends Fragment {

    private RecyclerView rvStudentRecommendationsList;
    private StudentRecCardAdapter adapter;
    private final List<StudentRecCardAdapter.StudentRecItem> recommendationList = new ArrayList<>();
    private EditText edtSearchStudentRecommendations;

    public StudentRecommendationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_recommendations, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view,
                    "My Recommendations",
                    "Personalized AI study recommendations"
            );
        }

        edtSearchStudentRecommendations = view.findViewById(R.id.edtSearchStudentRecommendations);
        if (edtSearchStudentRecommendations != null) {
            edtSearchStudentRecommendations.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString(), "All");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvStudentRecommendationsList = view.findViewById(R.id.rvStudentRecommendationsList);
        if (rvStudentRecommendationsList != null) {
            rvStudentRecommendationsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadStudentRecommendations();
        }

        return view;
    }

    private void loadStudentRecommendations() {
        recommendationList.clear();
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("R101", "CS101", "Review Weak Topic: Linked Lists & Pointer Operations", "Your Quiz 2 score in Data Structures was 58%. Reviewing Chapter 4 practice exercises can increase your predicted exam score by +15%.", "High Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("R102", "IT402", "Improve Attendance Rate in Web Development Systems", "Your current attendance rate in IT402 is 62%. Attending the next 3 consecutive lectures will prevent academic risk alerts.", "High Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("R103", "CS204", "Practice Remedial Exercises for Recursion & Sorting", "Your assignment average in Algorithms is 65%. Completing 5 additional coding exercises will boost your final grade.", "Medium Priority", "In Progress"));
        recommendationList.add(new StudentRecCardAdapter.StudentRecItem("R104", "IS201", "Midterm Review: Enterprise Architecture Principles", "Advisory recommendation: Review study guide for Module 3 to maintain your current GPA of 3.45.", "Low Priority", "In Progress"));

        adapter = new StudentRecCardAdapter(requireContext(), recommendationList, new StudentRecCardAdapter.OnRecCardClickListener() {
            @Override
            public void onReviewTopicClick(StudentRecCardAdapter.StudentRecItem item) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Recommendation Details & Action Plan")
                        .setMessage("Course Code: " + item.courseCode + "\n" +
                                item.title + "\n\n" +
                                "AI Diagnostic Explanation:\n" +
                                item.description + "\n\n" +
                                "Suggested Action Step:\n" +
                                "Access online learning resources and complete remedial practice set for " + item.courseCode)
                        .setPositiveButton("Open Course Materials", (dialog, which) -> {
                            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                            intent.putExtra("course_code", item.courseCode);
                            startActivity(intent);
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onMarkCompletedClick(StudentRecCardAdapter.StudentRecItem item) {
                item.statusBadge = "Completed";
                if (adapter != null) adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Marked recommendation as completed!", Toast.LENGTH_SHORT).show();
            }
        });

        if (rvStudentRecommendationsList != null) {
            rvStudentRecommendationsList.setAdapter(adapter);
        }
    }
}
