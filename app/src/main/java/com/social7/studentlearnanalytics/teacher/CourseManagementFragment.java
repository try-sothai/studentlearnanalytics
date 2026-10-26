package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.CourseAdapter;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.student.CourseDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class CourseManagementFragment extends Fragment {

    private CourseAdapter adapter;
    private TextView tvSelectedSemester, tvSelectedDepartment, tvSelectedSort;
    private TextView tabActiveCourses, tabArchivedCourses;

    private String selectedSemester = "Semester 1, 2024";
    private String selectedDepartment = "All Depts";
    private String selectedSort = "Code";
    private String searchQuery = "";
    private boolean isArchivedTab = false;

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
                R.layout.fragment_teacher_course_management,
                container,
                false
        );

        ImageView btnBack = view.findViewById(R.id.btnTeacherCoursesBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        tabActiveCourses = view.findViewById(R.id.tabActiveCourses);
        tabArchivedCourses = view.findViewById(R.id.tabArchivedCourses);

        if (tabActiveCourses != null) {
            tabActiveCourses.setOnClickListener(v -> selectTab(false));
        }
        if (tabArchivedCourses != null) {
            tabArchivedCourses.setOnClickListener(v -> selectTab(true));
        }

        tvSelectedSemester = view.findViewById(R.id.tvSelectedSemester);
        LinearLayout btnSemesterSelector = view.findViewById(R.id.btnSemesterSelector);
        if (btnSemesterSelector != null) {
            btnSemesterSelector.setOnClickListener(this::showSemesterDropdown);
        }

        tvSelectedDepartment = view.findViewById(R.id.tvSelectedDepartment);
        LinearLayout btnDepartmentSelector = view.findViewById(R.id.btnDepartmentSelector);
        if (btnDepartmentSelector != null) {
            btnDepartmentSelector.setOnClickListener(this::showDepartmentDropdown);
        }

        tvSelectedSort = view.findViewById(R.id.tvSelectedSort);
        LinearLayout btnSortSelector = view.findViewById(R.id.btnSortSelector);
        if (btnSortSelector != null) {
            btnSortSelector.setOnClickListener(this::showSortDropdown);
        }

        EditText edtSearch = view.findViewById(R.id.edtSearchCourses);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    applyFilter();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        RecyclerView rvCourses = view.findViewById(R.id.rvCourseManagement);
        if (rvCourses != null) {
            rvCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<Course> list = getSampleCourses();
            adapter = new CourseAdapter(requireContext(), list, new CourseAdapter.OnCourseClickListener() {
                @Override
                public void onCourseClick(Course course) {
                    Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                    intent.putExtra("course", course);
                    startActivity(intent);
                }

                @Override
                public void onStudentsClick(Course course) {
                    if (getActivity() instanceof TeacherMainActivity) {
                        ((TeacherMainActivity) getActivity()).openFragment(new StudentListFragment());
                    }
                }

                @Override
                public void onScheduleClick(Course course) {
                    Toast.makeText(requireContext(), course.getCode() + " Schedule: " + course.getSchedule() + " (" + course.getRoom() + ")", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAnalyticsClick(Course course) {
                    if (getActivity() instanceof TeacherMainActivity) {
                        ((TeacherMainActivity) getActivity()).openFragment(new ClassAnalyticsFragment());
                    }
                }

                @Override
                public void onMoreClick(Course course, View anchor) {
                    showMorePopupMenu(course, anchor);
                }
            });
            rvCourses.setAdapter(adapter);
            applyFilter();
        }

        return view;
    }

    private void selectTab(boolean isArchived) {
        this.isArchivedTab = isArchived;
        if (tabActiveCourses != null && tabArchivedCourses != null) {
            if (isArchived) {
                tabArchivedCourses.setBackgroundResource(R.drawable.bg_pagination_button_active);
                tabArchivedCourses.setTextColor(getResources().getColor(R.color.white, null));
                tabActiveCourses.setBackground(null);
                tabActiveCourses.setTextColor(getResources().getColor(R.color.teacher_dark_navy, null));
            } else {
                tabActiveCourses.setBackgroundResource(R.drawable.bg_pagination_button_active);
                tabActiveCourses.setTextColor(getResources().getColor(R.color.white, null));
                tabArchivedCourses.setBackground(null);
                tabArchivedCourses.setTextColor(getResources().getColor(R.color.teacher_dark_navy, null));
            }
        }
        applyFilter();
    }

    private void showSemesterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Semester 1, 2024");
        popup.getMenu().add(0, 2, 1, "Semester 2, 2023");
        popup.getMenu().add(0, 3, 2, "Semester 1, 2023");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) selectedSemester = "Semester 1, 2024";
            else if (item.getItemId() == 2) selectedSemester = "Semester 2, 2023";
            else if (item.getItemId() == 3) selectedSemester = "Semester 1, 2023";

            if (tvSelectedSemester != null) tvSelectedSemester.setText(selectedSemester);
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void showDepartmentDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "All Departments");
        popup.getMenu().add(0, 2, 1, "Computer Science");
        popup.getMenu().add(0, 3, 2, "Data Science");
        popup.getMenu().add(0, 4, 3, "Software Engineering");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) selectedDepartment = "All Depts";
            else if (item.getItemId() == 2) selectedDepartment = "Computer Science";
            else if (item.getItemId() == 3) selectedDepartment = "Data Science";
            else if (item.getItemId() == 4) selectedDepartment = "Software Eng";

            if (tvSelectedDepartment != null) tvSelectedDepartment.setText(selectedDepartment);
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void showSortDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Sort by Course Code");
        popup.getMenu().add(0, 2, 1, "Sort by Course Name");
        popup.getMenu().add(0, 3, 2, "Sort by Student Count");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) selectedSort = "Code";
            else if (item.getItemId() == 2) selectedSort = "Name";
            else if (item.getItemId() == 3) selectedSort = "Students";

            if (tvSelectedSort != null) tvSelectedSort.setText("Sort: " + selectedSort);
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void showMorePopupMenu(Course course, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "View Details");
        popup.getMenu().add(0, 2, 1, "View Students");
        popup.getMenu().add(0, 3, 2, "View Schedule");
        popup.getMenu().add(0, 4, 3, "View Class Analytics");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                intent.putExtra("course", course);
                startActivity(intent);
            } else if (item.getItemId() == 2) {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new StudentListFragment());
                }
            } else if (item.getItemId() == 3) {
                Toast.makeText(requireContext(), course.getCode() + ": " + course.getSchedule() + " (" + course.getRoom() + ")", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 4) {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new ClassAnalyticsFragment());
                }
            }
            return true;
        });
        popup.show();
    }

    private void applyFilter() {
        if (adapter != null) {
            adapter.filter(searchQuery, selectedSemester, selectedDepartment, selectedSort, isArchivedTab);
        }
    }

    private List<Course> getSampleCourses() {
        List<Course> list = new ArrayList<>();

        // 4 active assigned courses for Mr. Chan matching Figma AI spec
        list.add(new Course("1", "CS101", "Data Structures", "Computer Science Department",
                "Mon, Wed 08:00–09:30", "Room A101", 35, 5, 12,
                R.drawable.bg_course_icon_blue, R.drawable.ic_book, "Semester 1, 2024"));

        list.add(new Course("2", "CS102", "Database Systems", "Computer Science Department",
                "Tue, Thu 13:00–14:30", "Room A102", 28, 3, 8,
                R.drawable.bg_course_icon_purple, R.drawable.ic_database_symbol, "Semester 1, 2024"));

        list.add(new Course("3", "CS201", "Computer Networks", "Computer Science Department",
                "Mon, Wed 10:00–11:30", "Room B201", 32, 4, 10,
                R.drawable.bg_course_icon_green, R.drawable.ic_network_symbol, "Semester 1, 2024"));

        list.add(new Course("4", "CS301", "Mobile App Development", "Computer Science Department",
                "Tue, Thu 10:00–11:30", "Room B202", 27, 2, 6,
                R.drawable.bg_course_icon_orange, R.drawable.ic_desktop_symbol, "Semester 1, 2024"));

        return list;
    }
}
