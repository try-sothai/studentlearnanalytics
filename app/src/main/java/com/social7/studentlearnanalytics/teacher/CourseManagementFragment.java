package com.social7.studentlearnanalytics.teacher;

import android.app.Activity;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.CourseAdapter;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseManagementFragment extends Fragment {

    private CourseAdapter adapter;
    private TextView tvSelectedSemester, tvSelectedDepartment, tvSelectedSort;
    private TextView tabActiveCourses, tabArchivedCourses;

    private String selectedSemester = "All Semesters";
    private String selectedDepartment = "All Faculties";
    private String selectedSort = "Code";
    private String searchQuery = "";
    private boolean isArchivedTab = false;

    private final List<Course> loadedCourses = new ArrayList<>();
    private final Set<String> availableSemesters = new LinkedHashSet<>();
    private final Set<String> availableDepartments = new LinkedHashSet<>();

    private final ActivityResultLauncher<Intent> courseDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String action = result.getData().getStringExtra(TeacherCourseDetailActivity.EXTRA_ACTION);
                    Course c = (Course) result.getData().getSerializableExtra(TeacherCourseDetailActivity.EXTRA_COURSE);
                    if (c == null) return;
                    if (TeacherCourseDetailActivity.ACTION_VIEW_STUDENTS.equals(action)) {
                        openStudentsForCourse(c);
                    } else if (TeacherCourseDetailActivity.ACTION_VIEW_ANALYTICS.equals(action)) {
                        openAnalyticsForCourse(c);
                    }
                }
            }
    );

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

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherCourseManagement), getString(R.string.nav_courses), "Manage assigned courses"
            );
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
        if (tvSelectedSemester != null) tvSelectedSemester.setText(selectedSemester);
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
            adapter = new CourseAdapter(requireContext(), loadedCourses, new CourseAdapter.OnCourseClickListener() {
                @Override
                public void onCourseClick(Course course) {
                    showCourseDetailsDialog(course);
                }

                @Override
                public void onStudentsClick(Course course) {
                    openStudentsForCourse(course);
                }

                @Override
                public void onScheduleClick(Course course) {
                    Toast.makeText(requireContext(), course.getCode() + " Schedule: " + course.getSchedule() + " (" + course.getRoom() + ")", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAnalyticsClick(Course course) {
                    openAnalyticsForCourse(course);
                }

                @Override
                public void onMoreClick(Course course, View anchor) {
                    showMorePopupMenu(course, anchor);
                }
            });
            rvCourses.setAdapter(adapter);
        }

        fetchAssignedCourses();

        return view;
    }

    private void fetchAssignedCourses() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherCourseOfferings().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                loadedCourses.clear();
                availableSemesters.clear();
                availableDepartments.clear();

                if (response.isSuccessful() && response.body() != null && response.body().has("data") && response.body().get("data").isJsonArray()) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    int[] bgs = new int[]{R.drawable.bg_course_icon_blue, R.drawable.bg_course_icon_purple, R.drawable.bg_course_icon_green, R.drawable.bg_course_icon_orange};
                    int[] icons = new int[]{R.drawable.ic_book, R.drawable.ic_database_symbol, R.drawable.ic_network_symbol, R.drawable.ic_desktop_symbol};

                    int idx = 0;
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject off = el.getAsJsonObject();

                        JsonObject courseObj = off.has("course") && off.get("course").isJsonObject()
                                ? off.getAsJsonObject("course") : null;
                        JsonObject classObj = off.has("class_room") && off.get("class_room").isJsonObject()
                                ? off.getAsJsonObject("class_room") : null;
                        JsonObject semObj = off.has("semester") && off.get("semester").isJsonObject()
                                ? off.getAsJsonObject("semester") : null;

                        String courseId = courseObj != null && courseObj.has("id") && !courseObj.get("id").isJsonNull()
                                ? courseObj.get("id").getAsString()
                                : (off.has("course_id") && !off.get("course_id").isJsonNull() ? off.get("course_id").getAsString() : "0");

                        String code = courseObj != null ? getStr(courseObj, "code", getStr(courseObj, "course_code", "COURSE")) : "COURSE";
                        String name = courseObj != null ? getStr(courseObj, "name", getStr(courseObj, "course_name", "Course")) : "Course";

                        String deptName = "Academic Faculty";
                        if (courseObj != null) {
                            if (courseObj.has("department") && courseObj.get("department").isJsonObject()) {
                                deptName = getStr(courseObj.getAsJsonObject("department"), "name", deptName);
                            } else {
                                deptName = getStr(courseObj, "department", deptName);
                            }
                        }

                        String day = classObj != null ? getStr(classObj, "day_of_week", "") : (courseObj != null ? getStr(courseObj, "day_of_week", "") : "");
                        String start = classObj != null ? getStr(classObj, "start_time", "") : (courseObj != null ? getStr(courseObj, "start_time", "") : "");
                        String end = classObj != null ? getStr(classObj, "end_time", "") : (courseObj != null ? getStr(courseObj, "end_time", "") : "");
                        String schedule = (!day.isEmpty() ? day + " " : "") + (!start.isEmpty() ? start : "Scheduled") + (!end.isEmpty() ? "–" + end : "");
                        String room = classObj != null ? getStr(classObj, "room", "Room TBA") : (courseObj != null ? getStr(courseObj, "room", "Room TBA") : "Room TBA");

                        int enrolled = off.has("enrollments_count") && !off.get("enrollments_count").isJsonNull()
                                ? off.get("enrollments_count").getAsInt() : 0;

                        String semName = semObj != null ? getStr(semObj, "name", "Active Semester") : "Active Semester";
                        availableSemesters.add(semName);
                        availableDepartments.add(deptName);

                        Course c = new Course(
                                courseId,
                                code,
                                name,
                                deptName,
                                schedule.trim(),
                                room,
                                enrolled,
                                0,
                                0,
                                bgs[idx % bgs.length],
                                icons[idx % icons.length],
                                semName
                        );
                        String status = getStr(off, "status", "active");
                        c.setStatus(("archived".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) ? "Archived" : "Active");
                        loadedCourses.add(c);
                        idx++;
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to load assigned courses", Toast.LENGTH_SHORT).show();
                }

                if (adapter != null) {
                    adapter.setCourses(loadedCourses);
                }
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCourseDetailsDialog(Course course) {
        if (!isAdded() || getContext() == null || course == null) return;
        Intent intent = new Intent(requireContext(), TeacherCourseDetailActivity.class);
        intent.putExtra(TeacherCourseDetailActivity.EXTRA_COURSE, course);
        courseDetailLauncher.launch(intent);
    }

    private void openStudentsForCourse(Course course) {
        if (!(getActivity() instanceof TeacherMainActivity)) return;
        StudentListFragment frag = new StudentListFragment();
        if (course != null && course.getId() != null) {
            try {
                Bundle args = new Bundle();
                args.putInt("course_id", Integer.parseInt(course.getId()));
                frag.setArguments(args);
            } catch (NumberFormatException ignored) {
            }
        }
        ((TeacherMainActivity) getActivity()).openFragment(frag);
    }

    private void openAnalyticsForCourse(Course course) {
        if (!(getActivity() instanceof TeacherMainActivity)) return;
        ClassAnalyticsFragment frag = new ClassAnalyticsFragment();
        if (course != null && course.getId() != null) {
            try {
                Bundle args = new Bundle();
                args.putInt("course_id", Integer.parseInt(course.getId()));
                args.putString("course_title", course.getCode() + " - " + course.getName());
                frag.setArguments(args);
            } catch (NumberFormatException ignored) {
            }
        }
        ((TeacherMainActivity) getActivity()).openFragment(frag);
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
        popup.getMenu().add(0, 0, 0, "All Semesters");
        int i = 1;
        for (String sem : availableSemesters) {
            popup.getMenu().add(0, i, i, sem);
            i++;
        }

        popup.setOnMenuItemClickListener(item -> {
            selectedSemester = item.getTitle().toString();
            if (tvSelectedSemester != null) tvSelectedSemester.setText(selectedSemester);
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void showDepartmentDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Faculties");
        int i = 1;
        for (String dept : availableDepartments) {
            popup.getMenu().add(0, i, i, dept);
            i++;
        }

        popup.setOnMenuItemClickListener(item -> {
            selectedDepartment = item.getTitle().toString();
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

            if (tvSelectedSort != null) tvSelectedSort.setText(tvSelectedSort.getContext().getString(R.string.sort_3ee99c) + selectedSort);
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
                showCourseDetailsDialog(course);
            } else if (item.getItemId() == 2) {
                openStudentsForCourse(course);
            } else if (item.getItemId() == 3) {
                Toast.makeText(requireContext(), course.getCode() + ": " + course.getSchedule() + " (" + course.getRoom() + ")", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 4) {
                openAnalyticsForCourse(course);
            }
            return true;
        });
        popup.show();
    }

    private void applyFilter() {
        if (adapter != null) {
            String semFilter = "All Semesters".equalsIgnoreCase(selectedSemester) ? "" : selectedSemester;
            adapter.filter(searchQuery, semFilter, selectedDepartment, selectedSort, isArchivedTab);
        }
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
