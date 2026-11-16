package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.CourseCurriculumAuditFragment;
import com.social7.studentlearnanalytics.admin.courses.AddCourseActivity;
import com.social7.studentlearnanalytics.admin.courses.CourseAdapter;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseManagementFragment extends Fragment {

    private static final String TAG = "CourseManagement";

    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    private final List<Course> masterCourseList = new ArrayList<>();

    private RecyclerView rvCurriculumAudit;
    private com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter curriculumAdapter;
    private final List<com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem> auditList = new ArrayList<>();

    private RecyclerView rvCategories;
    private com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter categoryAdapter;
    private final List<com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem> categoryList = new ArrayList<>();

    private RecyclerView rvAssignments;
    private com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter assignmentAdapter;
    private final List<com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem> assignmentList = new ArrayList<>();

    private EditText etSearchCourse;
    private EditText etSearchCurriculum;
    private EditText etSearchCategory;
    private EditText etSearchAssignment;

    private View layoutSummaryCards;
    private View scrollFilterPills;
    private View layoutPagination;
    private View layoutEmptyState;
    private ProgressBar pbLoadingCourses;

    private TextView tvStatTotalCourses;
    private TextView tvStatActiveCourses;
    private TextView tvStatInactiveCourses;
    private TextView tvStatMissingSyllabus;

    private TextView pillFilterAllCourses;
    private TextView pillFilterActiveCourses;
    private TextView pillFilterInactiveCourses;

    private MaterialButton btnTabAllCourses;
    private MaterialButton btnTabCategories;
    private MaterialButton btnTabCurriculum;
    private MaterialButton btnTabAssignments;

    private TextView tvPageIndicator;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;

    private int currentPage = 1;
    private final int itemsPerPage = 8;
    private int selectedTab = 0; // 0=All, 1=Active, 2=Inactive, 3=Categories, 4=Curriculum, 5=Assignments

    public CourseManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_course_management, container, false);

        rvCourses = view.findViewById(R.id.rvCourses);
        etSearchCourse = view.findViewById(R.id.etSearchCourse);

        // Curriculum views
        View layoutCurriculum = view.findViewById(R.id.layoutCurriculumAuditContainer);
        if (layoutCurriculum != null) {
            rvCurriculumAudit = layoutCurriculum.findViewById(R.id.rvCurriculumAudit);
            etSearchCurriculum = layoutCurriculum.findViewById(R.id.etSearchCurriculum);
            
            if (etSearchCurriculum != null) {
                etSearchCurriculum.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (curriculumAdapter != null) curriculumAdapter.filter(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        }

        // Categories views
        View layoutCategories = view.findViewById(R.id.layoutCategoriesContainer);
        if (layoutCategories != null) {
            rvCategories = layoutCategories.findViewById(R.id.rvCategories);
            etSearchCategory = layoutCategories.findViewById(R.id.etSearchCategory);
            
            if (etSearchCategory != null) {
                etSearchCategory.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (categoryAdapter != null) categoryAdapter.filter(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        }

        // Assignments views
        View layoutAssignments = view.findViewById(R.id.layoutAssignmentsContainer);
        if (layoutAssignments != null) {
            rvAssignments = layoutAssignments.findViewById(R.id.rvAssignments);
            etSearchAssignment = layoutAssignments.findViewById(R.id.etSearchAssignment);
            
            if (etSearchAssignment != null) {
                etSearchAssignment.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (assignmentAdapter != null) assignmentAdapter.filter(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        }

        View btnAddCourse = view.findViewById(R.id.btnAddCourse);

        layoutSummaryCards = view.findViewById(R.id.layoutSummaryCards);
        scrollFilterPills = view.findViewById(R.id.scrollFilterPills);
        layoutPagination = view.findViewById(R.id.layoutPagination);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        pbLoadingCourses = view.findViewById(R.id.pbLoadingCourses);

        tvStatTotalCourses = view.findViewById(R.id.tvStatTotalCourses);
        tvStatActiveCourses = view.findViewById(R.id.tvStatActiveCourses);
        tvStatInactiveCourses = view.findViewById(R.id.tvStatInactiveCourses);
        tvStatMissingSyllabus = view.findViewById(R.id.tvStatMissingSyllabus);

        pillFilterAllCourses = view.findViewById(R.id.pillFilterAllCourses);
        pillFilterActiveCourses = view.findViewById(R.id.pillFilterActiveCourses);
        pillFilterInactiveCourses = view.findViewById(R.id.pillFilterInactiveCourses);

        btnTabAllCourses = view.findViewById(R.id.btnTabCourseList);
        btnTabCategories = view.findViewById(R.id.btnTabCategories);
        btnTabCurriculum = view.findViewById(R.id.btnTabCurriculum);
        btnTabAssignments = view.findViewById(R.id.btnTabAssignments);

        tvPageIndicator = view.findViewById(R.id.tvPageIndicator);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);

        View btnMenu = view.findViewById(R.id.btnCourseMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        View btnResetFilter = view.findViewById(R.id.btnResetFilter);
        if (btnResetFilter != null) {
            btnResetFilter.setOnClickListener(v -> resetFilters());
        }

        if (btnAddCourse != null) {
            btnAddCourse.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                startActivity(intent);
            });
        }

        setupTabs();
        setupSearchAndFilterPills();
        setupPaginationControls();

        loadCourseData();

        return view;
    }

    private void setupTabs() {
        if (btnTabAllCourses != null) {
            btnTabAllCourses.setOnClickListener(v -> selectSectionTab(0));
        }
        if (btnTabCategories != null) {
            btnTabCategories.setOnClickListener(v -> selectSectionTab(1));
        }
        if (btnTabCurriculum != null) {
            btnTabCurriculum.setOnClickListener(v -> selectSectionTab(2));
        }
        if (btnTabAssignments != null) {
            btnTabAssignments.setOnClickListener(v -> selectSectionTab(3));
        }
    }

    private void selectSectionTab(int tabIndex) {
        this.selectedTab = tabIndex;
        updateTabStyles(tabIndex);

        View layoutCourseList = getView() != null ? getView().findViewById(R.id.layoutCourseListContainer) : null;
        View layoutCategories = getView() != null ? getView().findViewById(R.id.layoutCategoriesContainer) : null;
        View layoutCurriculum = getView() != null ? getView().findViewById(R.id.layoutCurriculumAuditContainer) : null;
        View layoutAssignments = getView() != null ? getView().findViewById(R.id.layoutAssignmentsContainer) : null;

        if (layoutCourseList != null) layoutCourseList.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutCategories != null) layoutCategories.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutCurriculum != null) layoutCurriculum.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutAssignments != null) layoutAssignments.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) {
            updatePaginationAndEmptyState();
        } else if (tabIndex == 1) {
            loadCategoriesData();
        } else if (tabIndex == 2) {
            loadCurriculumAuditData();
        } else if (tabIndex == 3) {
            loadAssignmentsData();
        }
    }

    private void updateTabStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        MaterialButton[] tabs = new MaterialButton[]{
                btnTabAllCourses, btnTabCategories, btnTabCurriculum, btnTabAssignments
        };

        for (int i = 0; i < tabs.length; i++) {
            MaterialButton btn = tabs[i];
            if (btn == null) continue;
            if (i == activeIndex) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.admin_primary_blue, null)));
                btn.setTextColor(getResources().getColor(R.color.white, null));
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F4F7FF")));
                btn.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void setupSearchAndFilterPills() {
        if (etSearchCourse != null) {
            etSearchCourse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (courseAdapter != null) {
                        courseAdapter.filter(s.toString());
                        updatePaginationAndEmptyState();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAllCourses != null) {
            pillFilterAllCourses.setOnClickListener(v -> {
                updatePillSelection(pillFilterAllCourses, pillFilterActiveCourses, pillFilterInactiveCourses);
                if (courseAdapter != null) courseAdapter.filterByStatus("ALL");
                updatePaginationAndEmptyState();
            });
        }

        if (pillFilterActiveCourses != null) {
            pillFilterActiveCourses.setOnClickListener(v -> {
                updatePillSelection(pillFilterActiveCourses, pillFilterAllCourses, pillFilterInactiveCourses);
                if (courseAdapter != null) courseAdapter.filterByStatus("Active");
                updatePaginationAndEmptyState();
            });
        }

        if (pillFilterInactiveCourses != null) {
            pillFilterInactiveCourses.setOnClickListener(v -> {
                updatePillSelection(pillFilterInactiveCourses, pillFilterAllCourses, pillFilterActiveCourses);
                if (courseAdapter != null) courseAdapter.filterByStatus("Inactive");
                updatePaginationAndEmptyState();
            });
        }
    }

    private void updatePillSelection(TextView selected, TextView unselected1, TextView unselected2) {
        if (!isAdded() || getContext() == null) return;
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_notif_tab_active);
            selected.setTextColor(getResources().getColor(R.color.white, null));
        }
        if (unselected1 != null) {
            unselected1.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
            unselected1.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
        }
        if (unselected2 != null) {
            unselected2.setBackgroundResource(R.drawable.bg_notif_tab_inactive);
            unselected2.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
        }
    }

    private void setupPaginationControls() {
        if (btnPrevPage != null) {
            btnPrevPage.setOnClickListener(v -> {
                if (currentPage > 1) {
                    currentPage--;
                    updatePaginationAndEmptyState();
                }
            });
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                int totalItems = courseAdapter != null ? courseAdapter.getItemCount() : 0;
                int maxPage = (int) Math.ceil((double) totalItems / itemsPerPage);
                if (maxPage < 1) maxPage = 1;
                if (currentPage < maxPage) {
                    currentPage++;
                    updatePaginationAndEmptyState();
                }
            });
        }
    }

    private void resetFilters() {
        if (etSearchCourse != null) etSearchCourse.setText("");
        selectSectionTab(0);
        updatePillSelection(pillFilterAllCourses, pillFilterActiveCourses, pillFilterInactiveCourses);
    }

    private void loadCourseData() {
        if (!isAdded() || getContext() == null) return;

        if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.VISIBLE);

        try {
            ApiClient.getApiService(getContext()).getCourses().enqueue(new Callback<List<Course>>() {
                @Override
                public void onResponse(@NonNull Call<List<Course>> call, @NonNull Response<List<Course>> response) {
                    if (!isAdded()) return;
                    if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);

                    masterCourseList.clear();
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        masterCourseList.addAll(response.body());
                    }

                    if (masterCourseList.isEmpty()) {
                        loadFallbackCourses();
                    } else {
                        bindAdapter();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to load courses from API, using fallback data", t);
                    loadFallbackCourses();
                }
            });
        } catch (Exception e) {
            if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);
            Log.e(TAG, "Error initiating getCourses request", e);
            loadFallbackCourses();
        }
    }

    private void loadFallbackCourses() {
        if (!isAdded()) return;
        masterCourseList.clear();

        masterCourseList.add(createCourse("CS101", "Data Structures & Algorithms", 3, "College of Computer Studies", "Engr. John Reyes", "Active"));
        masterCourseList.add(createCourse("CS102", "Database Systems", 3, "College of Computer Studies", "Prof. Alan Santos", "Active"));
        masterCourseList.add(createCourse("CS201", "Web Systems & Development", 3, "College of Computer Studies", "Dr. Maria Cruz", "Active"));
        masterCourseList.add(createCourse("CS202", "Object-Oriented Programming", 3, "College of Computer Studies", "Prof. Sarah Jenkins", "Active"));
        masterCourseList.add(createCourse("IT301", "Computer Networks & Security", 3, "College of Computer Studies", "Prof. Robert Lim", "Active"));
        masterCourseList.add(createCourse("IT302", "Software Engineering", 3, "College of Computer Studies", "Prof. Alan Santos", "Active"));
        masterCourseList.add(createCourse("CS401", "Artificial Intelligence & ML", 3, "College of Computer Studies", "Dr. Maria Cruz", "Active"));
        masterCourseList.add(createCourse("CS402", "Operating Systems", 3, "College of Computer Studies", "Engr. John Reyes", "Active"));
        masterCourseList.add(createCourse("IT403", "Mobile Application Development", 3, "College of Computer Studies", "Prof. Clara Garcia", "Active"));
        masterCourseList.add(createCourse("IS201", "Information Security & Audit", 3, "College of Computer Studies", "Prof. Robert Lim", "Inactive"));
        masterCourseList.add(createCourse("MATH101", "Discrete Mathematics", 3, "College of Arts & Sciences", "Prof. David Vance", "Active"));
        masterCourseList.add(createCourse("IT404", "Cloud Computing & DevOps", 3, "College of Computer Studies", "Unassigned", "Inactive"));
        masterCourseList.add(createCourse("IS302", "Systems Analysis & Design", 3, "College of Computer Studies", "Prof. Clara Garcia", "Active"));
        masterCourseList.add(createCourse("GEN101", "Ethics in Information Tech", 3, "College of Arts & Sciences", "Prof. Elena Torres", "Active"));

        bindAdapter();
    }

    private Course createCourse(String code, String name, int credits, String dept, String teacher, String status) {
        Course course = new Course(code, name, credits, status);
        try {
            java.lang.reflect.Field fDept = Course.class.getDeclaredField("department");
            fDept.setAccessible(true);
            fDept.set(course, dept);

            java.lang.reflect.Field fTeacher = Course.class.getDeclaredField("teacher");
            fTeacher.setAccessible(true);
            fTeacher.set(course, teacher);
        } catch (Exception e) {
            Log.e(TAG, "Error setting reflection fields on Course", e);
        }
        return course;
    }

    private void bindAdapter() {
        if (!isAdded() || getContext() == null) return;

        courseAdapter = new CourseAdapter(getContext(), masterCourseList, this::showCourseDetailsDialog);

        if (rvCourses != null) {
            rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourses.setAdapter(courseAdapter);
        }

        updateStatistics();
        updatePaginationAndEmptyState();
    }

    private void updateStatistics() {
        int total = masterCourseList.size();
        int active = 0;
        int inactive = 0;
        int missingSyllabus = 0;

        for (Course c : masterCourseList) {
            if ("Active".equalsIgnoreCase(c.getStatus())) {
                active++;
            } else {
                inactive++;
            }
            if ("Unassigned".equalsIgnoreCase(c.getTeacher()) || c.getTeacher().isEmpty()) {
                missingSyllabus++;
            }
        }

        if (tvStatTotalCourses != null) tvStatTotalCourses.setText(String.valueOf(total));
        if (tvStatActiveCourses != null) tvStatActiveCourses.setText(String.valueOf(active));
        if (tvStatInactiveCourses != null) tvStatInactiveCourses.setText(String.valueOf(inactive));
        if (tvStatMissingSyllabus != null) tvStatMissingSyllabus.setText(String.valueOf(missingSyllabus));

        if (pillFilterAllCourses != null) pillFilterAllCourses.setText("All Statuses (" + total + ")");
        if (pillFilterActiveCourses != null) pillFilterActiveCourses.setText(pillFilterActiveCourses.getContext().getString(R.string.active) + active + ")");
        if (pillFilterInactiveCourses != null) pillFilterInactiveCourses.setText(pillFilterInactiveCourses.getContext().getString(R.string.inactive) + inactive + ")");
    }

    private void updatePaginationAndEmptyState() {
        if (courseAdapter == null) return;

        int totalCount = courseAdapter.getItemCount();
        if (totalCount == 0) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvCourses != null) rvCourses.setVisibility(View.GONE);
            if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvCourses != null) rvCourses.setVisibility(View.VISIBLE);
            if (layoutPagination != null) layoutPagination.setVisibility(View.VISIBLE);

            int maxPage = (int) Math.ceil((double) totalCount / itemsPerPage);
            if (maxPage < 1) maxPage = 1;
            if (currentPage > maxPage) currentPage = maxPage;

            if (tvPageIndicator != null) {
                tvPageIndicator.setText("Page " + currentPage + " of " + maxPage + " (" + totalCount + " items)");
            }

            if (btnPrevPage != null) {
                btnPrevPage.setEnabled(currentPage > 1);
                btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.5f);
            }

            if (btnNextPage != null) {
                btnNextPage.setEnabled(currentPage < maxPage);
                btnNextPage.setAlpha(currentPage < maxPage ? 1.0f : 0.5f);
            }
        }
    }

    private void showCourseDetailsDialog(Course course) {
        if (!isAdded() || getContext() == null) return;

        String details = "Course Code: " + course.getCode() + "\n" +
                "Course Name: " + course.getName() + "\n" +
                "Credits: " + course.getCredits() + " Credits\n" +
                "Department: " + course.getDepartment() + "\n" +
                "Assigned Teacher: " + (course.getTeacher().isEmpty() ? "Unassigned" : course.getTeacher()) + "\n" +
                "Semester: " + course.getSemester() + "\n" +
                "Status: " + course.getStatus();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.course_detail)
                .setMessage(details)
                .setPositiveButton(R.string.edit_course, (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadCurriculumAuditData() {
        if (!isAdded() || getContext() == null) return;
        
        auditList.clear();
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("CS101", "Data Structures & Algorithms", "BSCS 3-A", "Engr. John Reyes", 3, "Verified", "All course modules & syllabi verified for AY 2025-2026."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("CS102", "Database Management Systems", "BSIT 4-A", "Prof. Alan Santos", 3, "Verified", "Syllabus updated with relational design & indexing."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("IT201", "Web Systems & Technologies", "BSIT 4-B", "Unassigned", 3, "Missing Teacher", "Pending faculty assignment for upcoming semester."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("CS202", "Object-Oriented Programming", "BSCS 3-B", "Prof. Sarah Jenkins", 3, "Missing Syllabus", "Course syllabus pending department head sign-off."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("IS301", "Enterprise Systems Architecture", "BSIS 2-A", "Prof. Robert Lim", 3, "Verified", "Curriculum aligned with international IS standards."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("CS401", "Artificial Intelligence & ML", "BSCS 4-A", "Dr. Maria Cruz", 3, "Verified", "Prerequisites and credit distribution verified."));
        auditList.add(new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter.CurriculumAuditItem("IT404", "Cloud Computing & DevOps", "BSIT 4-A", "Unassigned", 3, "Missing Syllabus", "Syllabus draft under academic committee review."));

        if (rvCurriculumAudit != null) {
            curriculumAdapter = new com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter(getContext(), auditList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Curriculum Audit Detail")
                        .setMessage(getString(R.string.course_code) + item.code + "\n" +
                                "Course Name: " + item.name + "\n" +
                                "Associated Class: " + item.className + "\n" +
                                "Assigned Teacher: " + item.teacher + "\n" +
                                "Credits: " + item.credits + " Units\n" +
                                "Syllabus Audit Status: " + item.status + "\n\n" +
                                "Audit Findings & Notes:\n" + item.note)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvCurriculumAudit.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCurriculumAudit.setAdapter(curriculumAdapter);
        }
    }

    private void loadCategoriesData() {
        if (!isAdded() || getContext() == null) return;
        
        categoryList.clear();
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Computer Studies", "Information Technology", 11, "Dr. Maria Cruz"));
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Computer Studies", "Computer Science", 14, "Engr. John Reyes"));
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Computer Studies", "Information Systems", 8, "Prof. Robert Lim"));
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Arts & Sciences", "Mathematics & Natural Sciences", 12, "Prof. David Vance"));
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Arts & Sciences", "General Education", 9, "Prof. Elena Torres"));
        categoryList.add(new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter.CategoryItem("College of Business Administration", "Business Analytics & Tech", 6, "Prof. Alan Santos"));

        if (rvCategories != null) {
            categoryAdapter = new com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter(getContext(), categoryList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.category_detail)
                        .setMessage("College: " + item.collegeName + "\n" +
                                "Department: " + item.departmentName + "\n" +
                                "Total Courses: " + item.courseCount + "\n" +
                                "Department Head: " + item.head)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCategories.setAdapter(categoryAdapter);
        }
    }

    private void loadAssignmentsData() {
        if (!isAdded() || getContext() == null) return;
        
        assignmentList.clear();
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("CS101", "Data Structures & Algorithms", "BSCS 3-A", "Engr. John Reyes", "Active"));
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("CS102", "Database Management Systems", "BSIT 4-A", "Prof. Alan Santos", "Active"));
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("IT201", "Web Systems & Technologies", "BSIT 4-B", "Unassigned", "Missing Teacher"));
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("CS202", "Object-Oriented Programming", "BSCS 3-B", "Prof. Sarah Jenkins", "Active"));
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("IS301", "Enterprise Systems Architecture", "BSIS 2-A", "Prof. Robert Lim", "Active"));
        assignmentList.add(new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter.AssignmentItem("IT404", "Cloud Computing & DevOps", "Unassigned", "Unassigned", "Unassigned"));

        if (rvAssignments != null) {
            assignmentAdapter = new com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter(getContext(), assignmentList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.course_assignment_detail)
                        .setMessage(getString(R.string.course_code) + item.courseCode + "\n" +
                                "Course Name: " + item.courseName + "\n" +
                                "Assigned Class: " + item.className + "\n" +
                                "Assigned Teacher: " + item.teacherName + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvAssignments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAssignments.setAdapter(assignmentAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourseData();
    }
}
