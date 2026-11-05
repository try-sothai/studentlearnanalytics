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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class StudentCoursesFragment extends Fragment {

    private ImageView btnSearch;
    private View layoutSearchBox, cardSemesterSelector;
    private EditText edtSearchCourses;
    private TextView tvSemesterText, tvStatCourseCount, tvStatGpa, tvStatAverage, tvStatCompleted;
    private RecyclerView rvStudentCoursesList;
    private StudentCourseCardAdapter adapter;
    private final List<StudentCourseCardAdapter.StudentCourseItem> courseList = new ArrayList<>();

    public StudentCoursesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_student_courses, container, false);

        // Header Integration
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view,
                    "My Courses",
                    "Manage and track your enrolled courses"
            );
        }

        btnSearch = view.findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setVisibility(View.VISIBLE);
        }
        layoutSearchBox = view.findViewById(R.id.layoutSearchBox);
        edtSearchCourses = view.findViewById(R.id.edtSearchCourses);

        if (btnSearch != null && layoutSearchBox != null) {
            btnSearch.setOnClickListener(v -> {
                if (layoutSearchBox.getVisibility() == View.VISIBLE) {
                    layoutSearchBox.setVisibility(View.GONE);
                } else {
                    layoutSearchBox.setVisibility(View.VISIBLE);
                    if (edtSearchCourses != null) edtSearchCourses.requestFocus();
                }
            });
        }

        if (edtSearchCourses != null) {
            edtSearchCourses.addTextChangedListener(new TextWatcher() {
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

        tvSemesterText = view.findViewById(R.id.tvSemesterText);
        cardSemesterSelector = view.findViewById(R.id.cardSemesterSelector);
        if (cardSemesterSelector != null) {
            cardSemesterSelector.setOnClickListener(this::showSemesterDropdown);
        }

        tvStatCourseCount = view.findViewById(R.id.tvStatCourseCount);
        tvStatGpa = view.findViewById(R.id.tvStatGpa);
        tvStatAverage = view.findViewById(R.id.tvStatAverage);
        tvStatCompleted = view.findViewById(R.id.tvStatCompleted);

        rvStudentCoursesList = view.findViewById(R.id.rvStudentCoursesList);
        if (rvStudentCoursesList != null) {
            rvStudentCoursesList.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadStudentCourses();
        }

        return view;
    }

    private void showSemesterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Semester 1, 2024-2025 (Year 2)");
        popup.getMenu().add(0, 2, 1, "Semester 2, 2023-2024 (Year 1)");
        popup.getMenu().add(0, 3, 2, "Semester 1, 2023-2024 (Year 1)");

        popup.setOnMenuItemClickListener(item -> {
            if (tvSemesterText != null) {
                tvSemesterText.setText(item.getTitle());
            }
            return true;
        });
        popup.show();
    }

    private void loadStudentCourses() {
        courseList.clear();
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("C101", "CS101", "Intro to Computer Science & Programming", "Prof. Emily Johnson", "3 Credits", "Core Course", 85));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("C102", "IT402", "Web Development Systems & REST APIs", "Prof. Alan Santos", "3 Credits", "Major Elective", 70));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("C103", "CS204", "Software Engineering Architecture", "Prof. Emily Johnson", "4 Credits", "Core Course", 92));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("C104", "IS201", "Enterprise Systems Architecture", "Prof. Robert Davis", "3 Credits", "Major Elective", 78));
        courseList.add(new StudentCourseCardAdapter.StudentCourseItem("C105", "MA101", "Calculus I for Engineers", "Prof. Sarah Miller", "5 Credits", "General Core", 88));

        adapter = new StudentCourseCardAdapter(requireContext(), courseList, item -> {
            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
            intent.putExtra("course_id", item.id);
            intent.putExtra("course_code", item.code);
            intent.putExtra("course_title", item.name);
            intent.putExtra("instructor", item.instructor);
            startActivity(intent);
        });

        if (rvStudentCoursesList != null) {
            rvStudentCoursesList.setAdapter(adapter);
        }
    }
}
