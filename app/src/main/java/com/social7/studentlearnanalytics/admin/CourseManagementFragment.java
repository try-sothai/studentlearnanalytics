package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.courses.AddCourseActivity;
import com.social7.studentlearnanalytics.admin.courses.CourseAdapter;
import com.social7.studentlearnanalytics.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseManagementFragment extends Fragment {

    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;

    public CourseManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_course_management,
                container,
                false
        );

        rvCourses = view.findViewById(R.id.rvCourses);
        EditText etSearch = view.findViewById(R.id.etSearchCourse);
        View btnAddCourse = view.findViewById(R.id.btnAddCourse);
        View btnPrevPage = view.findViewById(R.id.btnPrevPage);
        View btnNextPage = view.findViewById(R.id.btnNextPage);

        View btnMenu = view.findViewById(R.id.btnCourseMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        setupCourseList();

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (courseAdapter != null) {
                        courseAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddCourse != null) {
            btnAddCourse.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                startActivity(intent);
            });
        }

        if (btnPrevPage != null) {
            btnPrevPage.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Previous page", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Next page", Toast.LENGTH_SHORT).show()
            );
        }

        return view;
    }

    private void setupCourseList() {
        if (getContext() == null) return;

        com.social7.studentlearnanalytics.network.ApiClient.getApiService(getContext()).getCourses().enqueue(new retrofit2.Callback<List<Course>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<Course>> call, @NonNull retrofit2.Response<List<Course>> response) {
                if (!isAdded()) return;
                List<Course> list = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    list = response.body();
                } else {
                    list.add(new Course("CS101", "Data Structures", 3, "Active"));
                    list.add(new Course("CS102", "Database Systems", 3, "Active"));
                    list.add(new Course("CS201", "Web Development", 3, "Active"));
                }
                bindCourseList(list);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<Course>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                List<Course> list = new ArrayList<>();
                list.add(new Course("CS101", "Data Structures", 3, "Active"));
                list.add(new Course("CS102", "Database Systems", 3, "Active"));
                bindCourseList(list);
            }
        });
    }

    private void bindCourseList(List<Course> courseList) {
        courseAdapter = new CourseAdapter(getContext(), courseList, course -> 
            Toast.makeText(getContext(), "Selected: " + course.getName(), Toast.LENGTH_SHORT).show()
        );

        if (rvCourses != null) {
            rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourses.setAdapter(courseAdapter);
        }
    }
}
