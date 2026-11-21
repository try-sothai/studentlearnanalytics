package com.social7.studentlearnanalytics.admin;

import android.app.Activity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.AssignTeacherClassActivity;
import com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter;
import com.social7.studentlearnanalytics.admin.classes.RecordSelectorActivity;
import com.social7.studentlearnanalytics.admin.courses.AddCourseActivity;
import com.social7.studentlearnanalytics.admin.courses.CourseAdapter;
import com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter;
import com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseManagementFragment extends Fragment {

    private static final String TAG = "CourseManagement";

    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    private final List<Course> masterCourseList = new ArrayList<>();

    private RecyclerView rvCurriculumAudit;
    private CurriculumAuditAdapter curriculumAdapter;
    private final List<CurriculumAuditAdapter.CurriculumAuditItem> auditList = new ArrayList<>();

    private RecyclerView rvCategories;
    private CourseCategoryAdapter categoryAdapter;
    private final List<CourseCategoryAdapter.CategoryItem> categoryList = new ArrayList<>();

    private RecyclerView rvAssignments;
    private CourseAssignmentAdapter assignmentAdapter;
    private final List<CourseAssignmentAdapter.AssignmentItem> assignmentList = new ArrayList<>();

    private EditText etSearchCourse;
    private EditText etSearchCurriculum;
    private EditText etSearchCategory;
    private EditText etSearchAssignment;

    private View layoutSummaryCards;
    private View scrollFilterPills;
    private View layoutPagination;
    private View layoutEmptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private MaterialButton btnResetFilter;
    private ProgressBar pbLoadingCourses;

    private TextView tvStatTotalCourses;
    private TextView tvStatActiveCourses;
    private TextView tvStatInactiveCourses;
    private TextView tvStatMissingSyllabus;

    private TextView pillFilterAllCourses;
    private TextView pillFilterActiveCourses;
    private TextView pillFilterInactiveCourses;
    private TextView pillFilterDepartment;
    private TextView pillFilterProgram;
    private TextView pillFilterAcademicYear;
    private TextView pillFilterSemester;
    private TextView pillFilterClear;

    private MaterialButton btnTabAllCourses;
    private MaterialButton btnTabCategories;
    private MaterialButton btnTabCurriculum;
    private MaterialButton btnTabAssignments;

    private TextView tvPageIndicator;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;

    private int currentPage = 1;
    private final int itemsPerPage = 8;
    private int selectedTab = 0; // 0=Course List, 1=Categories, 2=Curriculum, 3=Assignments

    private String selectedStatusFilter = "ALL";
    private int filterDeptId = -1;
    private String filterDeptName = "";
    private int filterProgramId = -1;
    private String filterProgramName = "";
    private int filterAyId = -1;
    private String filterAyName = "";
    private int filterSemesterId = -1;
    private String filterSemesterName = "";

    private String lastApiError = null;
    private boolean lastApiForbidden = false;

    private final ActivityResultLauncher<Intent> addEditCourseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadCourseData();
                }
            });

    private final ActivityResultLauncher<Intent> deptFilterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    filterDeptId = parseIntOr(result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID), -1);
                    filterDeptName = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                    if (filterDeptName == null) filterDeptName = "";
                    filterProgramId = -1;
                    filterProgramName = "";
                    applyActiveFilters();
                }
            });

    private final ActivityResultLauncher<Intent> programFilterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    filterProgramId = parseIntOr(result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID), -1);
                    filterProgramName = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                    if (filterProgramName == null) filterProgramName = "";
                    applyActiveFilters();
                }
            });

    private final ActivityResultLauncher<Intent> ayFilterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    filterAyId = parseIntOr(result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID), -1);
                    filterAyName = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                    if (filterAyName == null) filterAyName = "";
                    filterSemesterId = -1;
                    filterSemesterName = "";
                    applyActiveFilters();
                }
            });

    private final ActivityResultLauncher<Intent> semesterFilterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    filterSemesterId = parseIntOr(result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID), -1);
                    filterSemesterName = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                    if (filterSemesterName == null) filterSemesterName = "";
                    applyActiveFilters();
                }
            });

    private final ActivityResultLauncher<Intent> assignTeacherLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadCourseData();
                }
            });

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

        rvCurriculumAudit = view.findViewById(R.id.rvCurriculumAudit);
        etSearchCurriculum = view.findViewById(R.id.etSearchCurriculum);
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

        rvCategories = view.findViewById(R.id.rvCategories);
        etSearchCategory = view.findViewById(R.id.etSearchCategory);
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

        rvAssignments = view.findViewById(R.id.rvAssignments);
        etSearchAssignment = view.findViewById(R.id.etSearchAssignment);
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

        View btnAddCourse = view.findViewById(R.id.btnAddCourse);
        View btnAddAssignment = view.findViewById(R.id.btnAddAssignment);

        layoutSummaryCards = view.findViewById(R.id.layoutSummaryCards);
        scrollFilterPills = view.findViewById(R.id.scrollFilterPills);
        layoutPagination = view.findViewById(R.id.layoutPagination);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        btnResetFilter = view.findViewById(R.id.btnResetFilter);
        pbLoadingCourses = view.findViewById(R.id.pbLoadingCourses);

        tvStatTotalCourses = view.findViewById(R.id.tvStatTotalCourses);
        tvStatActiveCourses = view.findViewById(R.id.tvStatActiveCourses);
        tvStatInactiveCourses = view.findViewById(R.id.tvStatInactiveCourses);
        tvStatMissingSyllabus = view.findViewById(R.id.tvStatMissingSyllabus);

        pillFilterAllCourses = view.findViewById(R.id.pillFilterAllCourses);
        pillFilterActiveCourses = view.findViewById(R.id.pillFilterActiveCourses);
        pillFilterInactiveCourses = view.findViewById(R.id.pillFilterInactiveCourses);
        pillFilterDepartment = view.findViewById(R.id.pillFilterDepartment);
        pillFilterProgram = view.findViewById(R.id.pillFilterProgram);
        pillFilterAcademicYear = view.findViewById(R.id.pillFilterAcademicYear);
        pillFilterSemester = view.findViewById(R.id.pillFilterSemester);
        pillFilterClear = view.findViewById(R.id.pillFilterClear);

        btnTabAllCourses = view.findViewById(R.id.btnTabCourseList);
        btnTabCategories = view.findViewById(R.id.btnTabCategories);
        btnTabCurriculum = view.findViewById(R.id.btnTabCurriculum);
        btnTabAssignments = view.findViewById(R.id.btnTabAssignments);

        tvPageIndicator = view.findViewById(R.id.tvPageIndicator);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);

        View imgNotifBell = view.findViewById(R.id.imgNotifBell);
        if (imgNotifBell != null) {
            imgNotifBell.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new NotificationFragment());
                }
            });
        }

        View btnMenu = view.findViewById(R.id.btnCourseMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (btnResetFilter != null) {
            btnResetFilter.setOnClickListener(v -> {
                if (lastApiError != null) {
                    loadCourseData();
                } else {
                    resetFilters();
                }
            });
        }

        if (btnAddCourse != null) {
            btnAddCourse.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                addEditCourseLauncher.launch(intent);
            });
        }

        if (btnAddAssignment != null) {
            btnAddAssignment.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AssignTeacherClassActivity.class);
                assignTeacherLauncher.launch(intent);
            });
        }

        setupTabs();
        setupSearchAndFilterPills();
        setupPaginationControls();

        courseAdapter = new CourseAdapter(requireContext(), masterCourseList, this::showCourseDetailsDialog);
        if (rvCourses != null) {
            rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCourses.setAdapter(courseAdapter);
        }

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

        View root = getView();
        if (root == null) return;

        com.google.android.material.appbar.AppBarLayout appBar = root.findViewById(R.id.appBarCourses);
        if (appBar != null) appBar.setExpanded(true, false);

        MaterialButton[] tabs = new MaterialButton[]{
                btnTabAllCourses, btnTabCategories, btnTabCurriculum, btnTabAssignments
        };
        if (tabIndex >= 0 && tabIndex < tabs.length) {
            com.social7.studentlearnanalytics.utils.UiUtils.scrollToTab(tabs[tabIndex]);
        }

        View ctrlList = root.findViewById(R.id.layoutCourseListControls);
        View ctrlCat = root.findViewById(R.id.layoutCategoriesControls);
        View ctrlCurr = root.findViewById(R.id.layoutCurriculumControls);
        View ctrlAssign = root.findViewById(R.id.layoutAssignmentsControls);

        if (ctrlList != null) ctrlList.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (ctrlCat != null) ctrlCat.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (ctrlCurr != null) ctrlCurr.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (ctrlAssign != null) ctrlAssign.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        View layoutCourseList = root.findViewById(R.id.layoutCourseListContainer);
        if (layoutCourseList != null) layoutCourseList.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (rvCategories != null) rvCategories.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (rvCurriculumAudit != null) rvCurriculumAudit.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (rvAssignments != null) rvAssignments.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        if (layoutPagination != null) layoutPagination.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);

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
                    currentPage = 1;
                    applyActiveFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAllCourses != null) {
            pillFilterAllCourses.setOnClickListener(v -> {
                selectedStatusFilter = "ALL";
                currentPage = 1;
                updatePillSelection(pillFilterAllCourses, pillFilterActiveCourses, pillFilterInactiveCourses);
                applyActiveFilters();
            });
        }

        if (pillFilterActiveCourses != null) {
            pillFilterActiveCourses.setOnClickListener(v -> {
                selectedStatusFilter = "Active";
                currentPage = 1;
                updatePillSelection(pillFilterActiveCourses, pillFilterAllCourses, pillFilterInactiveCourses);
                applyActiveFilters();
            });
        }

        if (pillFilterInactiveCourses != null) {
            pillFilterInactiveCourses.setOnClickListener(v -> {
                selectedStatusFilter = "Inactive";
                currentPage = 1;
                updatePillSelection(pillFilterInactiveCourses, pillFilterAllCourses, pillFilterActiveCourses);
                applyActiveFilters();
            });
        }

        if (pillFilterDepartment != null) {
            pillFilterDepartment.setOnClickListener(v -> {
                Intent i = RecordSelectorActivity.createIntent(requireContext(), RecordSelectorActivity.TYPE_DEPARTMENT);
                if (filterDeptId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(filterDeptId));
                deptFilterLauncher.launch(i);
            });
        }

        if (pillFilterProgram != null) {
            pillFilterProgram.setOnClickListener(v -> {
                Intent i = RecordSelectorActivity.createIntent(requireContext(), RecordSelectorActivity.TYPE_PROGRAM);
                if (filterDeptId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, filterDeptId);
                if (filterProgramId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(filterProgramId));
                programFilterLauncher.launch(i);
            });
        }

        if (pillFilterAcademicYear != null) {
            pillFilterAcademicYear.setOnClickListener(v -> {
                Intent i = RecordSelectorActivity.createIntent(requireContext(), RecordSelectorActivity.TYPE_ACADEMIC_YEAR);
                if (filterAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(filterAyId));
                ayFilterLauncher.launch(i);
            });
        }

        if (pillFilterSemester != null) {
            pillFilterSemester.setOnClickListener(v -> {
                Intent i = RecordSelectorActivity.createIntent(requireContext(), RecordSelectorActivity.TYPE_SEMESTER);
                if (filterAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, filterAyId);
                if (filterSemesterId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(filterSemesterId));
                semesterFilterLauncher.launch(i);
            });
        }

        if (pillFilterClear != null) {
            pillFilterClear.setOnClickListener(v -> resetFilters());
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
                    if (courseAdapter != null) courseAdapter.setPage(currentPage, itemsPerPage);
                    updatePaginationAndEmptyState();
                }
            });
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                int totalItems = courseAdapter != null ? courseAdapter.getFilteredCount() : 0;
                int maxPage = (int) Math.ceil((double) totalItems / itemsPerPage);
                if (maxPage < 1) maxPage = 1;
                if (currentPage < maxPage) {
                    currentPage++;
                    if (courseAdapter != null) courseAdapter.setPage(currentPage, itemsPerPage);
                    updatePaginationAndEmptyState();
                }
            });
        }
    }

    private void resetFilters() {
        selectedStatusFilter = "ALL";
        filterDeptId = -1;
        filterDeptName = "";
        filterProgramId = -1;
        filterProgramName = "";
        filterAyId = -1;
        filterAyName = "";
        filterSemesterId = -1;
        filterSemesterName = "";
        currentPage = 1;
        if (etSearchCourse != null) etSearchCourse.setText("");
        updatePillSelection(pillFilterAllCourses, pillFilterActiveCourses, pillFilterInactiveCourses);
        applyActiveFilters();
    }

    private void applyActiveFilters() {
        if (courseAdapter == null) return;

        String query = etSearchCourse != null && etSearchCourse.getText() != null
                ? etSearchCourse.getText().toString() : "";

        // First compute statistics on the scope without the status filter so All/Active/Inactive pills reflect the scope
        courseAdapter.filterByStatus("ALL");
        courseAdapter.filterByDepartment(filterDeptId, filterDeptName);
        courseAdapter.filterByProgram(filterProgramId, filterProgramName);
        courseAdapter.filterByAcademicYear(filterAyId, filterAyName);
        courseAdapter.filterBySemester(filterSemesterId, filterSemesterName);
        courseAdapter.filter(query);

        List<Course> scopedCourses = new ArrayList<>(courseAdapter.getFilteredList());
        updateStatisticsFromScopedList(scopedCourses);

        // Now apply status filter and page slice
        courseAdapter.filterByStatus(selectedStatusFilter);
        courseAdapter.setPage(currentPage, itemsPerPage);

        updateFilterPillLabels();
        updatePaginationAndEmptyState();
    }

    private void updateFilterPillLabels() {
        if (!isAdded() || getContext() == null) return;

        if (pillFilterDepartment != null) {
            boolean active = filterDeptId > 0 || !filterDeptName.isEmpty();
            pillFilterDepartment.setText(active ? ("Dept: " + filterDeptName) : "Dept: All");
            pillFilterDepartment.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterDepartment.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        if (pillFilterProgram != null) {
            boolean active = filterProgramId > 0 || !filterProgramName.isEmpty();
            pillFilterProgram.setText(active ? ("Program: " + filterProgramName) : "Program: All");
            pillFilterProgram.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterProgram.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        if (pillFilterAcademicYear != null) {
            boolean active = filterAyId > 0 || !filterAyName.isEmpty();
            pillFilterAcademicYear.setText(active ? ("Year: " + filterAyName) : "Year: All");
            pillFilterAcademicYear.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAcademicYear.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        if (pillFilterSemester != null) {
            boolean active = filterSemesterId > 0 || !filterSemesterName.isEmpty();
            pillFilterSemester.setText(active ? ("Semester: " + filterSemesterName) : "Semester: All");
            pillFilterSemester.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterSemester.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        boolean anyFilterActive = !"ALL".equalsIgnoreCase(selectedStatusFilter)
                || filterDeptId > 0 || !filterDeptName.isEmpty()
                || filterProgramId > 0 || !filterProgramName.isEmpty()
                || filterAyId > 0 || !filterAyName.isEmpty()
                || filterSemesterId > 0 || !filterSemesterName.isEmpty()
                || (etSearchCourse != null && etSearchCourse.getText() != null && !etSearchCourse.getText().toString().trim().isEmpty());

        if (pillFilterClear != null) {
            pillFilterClear.setVisibility(anyFilterActive ? View.VISIBLE : View.GONE);
        }
    }

    private void loadCourseData() {
        if (!isAdded() || getContext() == null) return;

        if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.VISIBLE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        lastApiError = null;
        lastApiForbidden = false;

        try {
            ApiClient.getApiService(getContext()).getCoursesCatalog().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);

                    if (response.isSuccessful() && response.body() != null) {
                        masterCourseList.clear();
                        JsonObject body = response.body();
                        if (body.has("data") && body.get("data").isJsonArray()) {
                            JsonArray arr = body.getAsJsonArray("data");
                            for (JsonElement el : arr) {
                                if (el != null && el.isJsonObject()) {
                                    masterCourseList.add(Course.fromJson(el.getAsJsonObject()));
                                }
                            }
                        }
                        lastApiError = null;
                        lastApiForbidden = false;
                        bindAdapter();
                    } else {
                        lastApiForbidden = (response.code() == 401 || response.code() == 403);
                        lastApiError = lastApiForbidden
                                ? "You do not have permission to view course records."
                                : getString(R.string.network_error_server);
                        masterCourseList.clear();
                        bindAdapter();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to load courses from API", t);
                    lastApiForbidden = false;
                    lastApiError = getString(R.string.network_error_server);
                    masterCourseList.clear();
                    bindAdapter();
                }
            });
        } catch (Exception e) {
            if (pbLoadingCourses != null) pbLoadingCourses.setVisibility(View.GONE);
            Log.e(TAG, "Error initiating getCoursesCatalog request", e);
            lastApiError = getString(R.string.network_error_server);
            masterCourseList.clear();
            bindAdapter();
        }
    }

    private void bindAdapter() {
        if (!isAdded() || getContext() == null) return;

        if (courseAdapter == null) {
            courseAdapter = new CourseAdapter(getContext(), masterCourseList, this::showCourseDetailsDialog);
            if (rvCourses != null) {
                rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
                rvCourses.setAdapter(courseAdapter);
            }
        } else {
            courseAdapter.updateData(masterCourseList);
        }

        applyActiveFilters();
        if (selectedTab == 1) loadCategoriesData();
        else if (selectedTab == 2) loadCurriculumAuditData();
        else if (selectedTab == 3) loadAssignmentsData();
    }

    private void updateStatisticsFromScopedList(List<Course> courses) {
        int total = courses.size();
        int active = 0;
        int inactive = 0;
        int missingSyllabus = 0;

        for (Course c : courses) {
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
        if (pillFilterActiveCourses != null) pillFilterActiveCourses.setText("Active (" + active + ")");
        if (pillFilterInactiveCourses != null) pillFilterInactiveCourses.setText("Inactive (" + inactive + ")");
    }

    private void updatePaginationAndEmptyState() {
        if (courseAdapter == null) return;

        if (selectedTab != 0) {
            if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);
            return;
        }

        int totalCount = courseAdapter.getFilteredCount();
        if (totalCount == 0) {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvCourses != null) rvCourses.setVisibility(View.GONE);
            if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);

            if (lastApiError != null) {
                if (tvEmptyTitle != null) {
                    tvEmptyTitle.setText(lastApiForbidden ? "Access Restricted" : "Unable to Load Courses");
                }
                if (tvEmptyMessage != null) {
                    tvEmptyMessage.setText(lastApiError);
                }
                if (btnResetFilter != null) {
                    btnResetFilter.setText(R.string.retry);
                }
            } else {
                if (tvEmptyTitle != null) {
                    tvEmptyTitle.setText(R.string.no_courses_found_c50c1b);
                }
                if (tvEmptyMessage != null) {
                    tvEmptyMessage.setText(masterCourseList.isEmpty()
                            ? getString(R.string.empty_state_no_data)
                            : getString(R.string.search_no_results));
                }
                if (btnResetFilter != null) {
                    btnResetFilter.setText(R.string.reset_search_filters);
                }
            }
        } else {
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (rvCourses != null) rvCourses.setVisibility(View.VISIBLE);
            if (layoutPagination != null) layoutPagination.setVisibility(View.VISIBLE);

            int maxPage = (int) Math.ceil((double) totalCount / itemsPerPage);
            if (maxPage < 1) maxPage = 1;
            if (currentPage > maxPage) currentPage = maxPage;

            if (tvPageIndicator != null) {
                tvPageIndicator.setText("Page " + currentPage + " of " + maxPage + " (" + totalCount + " courses)");
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

        String dept = !course.getDepartment().isEmpty() ? course.getDepartment() : "N/A";
        String prog = !course.getProgramName().isEmpty()
                ? (course.getProgramName() + (!course.getProgramCode().isEmpty() ? " (" + course.getProgramCode() + ")" : ""))
                : "General Program";
        String teacher = !course.getTeacher().isEmpty() ? course.getTeacher() : "Unassigned";
        String ay = !course.getAcademicYear().isEmpty() ? course.getAcademicYear() : "Current Academic Year";
        String sem = !course.getSemester().isEmpty() ? course.getSemester() : "Semester 1";
        String classes = !course.getClassesSummary().isEmpty() ? course.getClassesSummary() : "None assigned";
        String desc = !course.getDescription().isEmpty() ? course.getDescription() : "No description provided.";

        String details = "Course Code: " + course.getCode() + "\n" +
                "Course Name: " + course.getName() + "\n" +
                "Department: " + dept + "\n" +
                "Program: " + prog + "\n" +
                "Credit Hours: " + course.getCredits() + " Credits\n" +
                "Academic Year: " + ay + "\n" +
                "Semester: " + sem + "\n" +
                "Assigned Teacher: " + teacher + "\n" +
                "Associated Classes: " + classes + "\n" +
                "Enrolled Students: " + course.getEnrollmentsCount() + "\n" +
                "Course Status: " + course.getStatus() + "\n\n" +
                "Description:\n" + desc;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(course.getCode() + " — " + course.getName())
                .setMessage(details)
                .setPositiveButton(R.string.edit_course, (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), AddCourseActivity.class);
                    intent.putExtra(AddCourseActivity.EXTRA_EDIT_COURSE, course);
                    addEditCourseLauncher.launch(intent);
                })
                .setNeutralButton("Manage Status / Delete", (dialog, which) -> showCourseManageActionsDialog(course))
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCourseManageActionsDialog(Course course) {
        if (!isAdded() || getContext() == null) return;

        boolean isActive = "Active".equalsIgnoreCase(course.getStatus());
        String toggleLabel = isActive ? "Set Status to Inactive" : "Set Status to Active";
        String[] options = new String[]{toggleLabel, "Delete Course"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(course.getCode() + " Actions")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        toggleCourseStatus(course, isActive ? "inactive" : "active");
                    } else if (which == 1) {
                        confirmDeleteCourse(course);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void toggleCourseStatus(Course course, String newStatus) {
        if (!isAdded() || getContext() == null) return;

        JsonObject body = new JsonObject();
        body.addProperty("status", newStatus);

        ApiClient.getApiService(getContext()).updateCourseStatus(course.getId(), body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Course " + course.getCode() + " marked as " + newStatus + ".", Toast.LENGTH_SHORT).show();
                    loadCourseData();
                } else {
                    Toast.makeText(getContext(), "Failed to update course status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteCourse(Course course) {
        if (!isAdded() || getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Course " + course.getCode() + "?")
                .setMessage("Are you sure you want to delete \"" + course.getName() + "\"?\n\nNote: Courses referenced by active classes, schedules, or enrollments cannot be permanently deleted.")
                .setPositiveButton(R.string.delete, (dialog, which) -> executeDeleteCourse(course))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeDeleteCourse(Course course) {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(getContext()).deleteCourse(course.getId()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Course " + course.getCode() + " deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadCourseData();
                } else {
                    String msg = "Course cannot be deleted because it has linked academic records.";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message") && !err.get("message").isJsonNull()) {
                                msg = err.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Deletion Prevented")
                            .setMessage(msg)
                            .setPositiveButton("Set to Inactive Instead", (d, w) -> toggleCourseStatus(course, "inactive"))
                            .setNegativeButton(R.string.close, null)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurriculumAuditData() {
        if (!isAdded() || getContext() == null) return;

        auditList.clear();
        int assignedCount = 0;
        int missingCount = 0;
        int issuesCount = 0;

        for (Course c : masterCourseList) {
            String code = c.getCode();
            String name = c.getName();
            String dept = !c.getDepartment().isEmpty() ? c.getDepartment() : "N/A";
            String teacher = !c.getTeacher().isEmpty() ? c.getTeacher() : "Unassigned";
            int credits = c.getCredits();
            boolean unassigned = "Unassigned".equalsIgnoreCase(teacher);
            if (unassigned) {
                missingCount++;
            } else {
                assignedCount++;
            }
            if (c.getDescription().isEmpty() || !"Active".equalsIgnoreCase(c.getStatus())) {
                issuesCount++;
            }
            String status = unassigned ? "Missing Teacher" : "Verified";
            String note = !c.getDescription().isEmpty()
                    ? c.getDescription()
                    : (unassigned ? "Pending faculty assignment." : "Course record loaded from database.");
            auditList.add(new CurriculumAuditAdapter.CurriculumAuditItem(
                    code, name, dept, teacher, credits, status, note));
        }

        View root = getView();
        if (root != null) {
            TextView tvReviewed = root.findViewById(R.id.tvAuditReviewed);
            TextView tvAssigned = root.findViewById(R.id.tvAuditAssigned);
            TextView tvMissing = root.findViewById(R.id.tvAuditMissing);
            TextView tvIssues = root.findViewById(R.id.tvAuditIssues);
            if (tvReviewed != null) tvReviewed.setText(String.valueOf(masterCourseList.size()));
            if (tvAssigned != null) tvAssigned.setText(String.valueOf(assignedCount));
            if (tvMissing != null) tvMissing.setText(String.valueOf(missingCount));
            if (tvIssues != null) tvIssues.setText(String.valueOf(issuesCount));
        }

        if (rvCurriculumAudit != null) {
            curriculumAdapter = new CurriculumAuditAdapter(getContext(), auditList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Curriculum Audit Detail")
                        .setMessage(getString(R.string.course_code) + ": " + item.code + "\n" +
                                "Course Name: " + item.name + "\n" +
                                "Department: " + item.className + "\n" +
                                "Assigned Teacher: " + item.teacher + "\n" +
                                "Credits: " + item.credits + " Units\n" +
                                "Syllabus Audit Status: " + item.status + "\n\n" +
                                "Syllabus / Description:\n" + item.note)
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
        Map<String, Integer> deptCounts = new LinkedHashMap<>();
        for (Course c : masterCourseList) {
            String dept = !c.getDepartment().trim().isEmpty() ? c.getDepartment() : "General";
            deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : deptCounts.entrySet()) {
            categoryList.add(new CourseCategoryAdapter.CategoryItem(
                    "Academic Department", entry.getKey(), entry.getValue(), "Active"));
        }

        View root = getView();
        if (root != null) {
            TextView tvColleges = root.findViewById(R.id.tvTotalColleges);
            TextView tvDepts = root.findViewById(R.id.tvTotalDepartments);
            if (tvColleges != null) tvColleges.setText(String.valueOf(Math.max(1, deptCounts.size())));
            if (tvDepts != null) tvDepts.setText(String.valueOf(deptCounts.size()));
        }

        if (rvCategories != null) {
            categoryAdapter = new CourseCategoryAdapter(getContext(), categoryList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.category_detail)
                        .setMessage("Category Type: " + item.collegeName + "\n" +
                                "Department: " + item.departmentName + "\n" +
                                "Total Courses: " + item.courseCount + "\n" +
                                "Status: " + item.head)
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
        int assignedCount = 0;
        int missingCount = 0;

        for (Course c : masterCourseList) {
            String code = c.getCode();
            String name = c.getName();
            String clsOrDept = !c.getClassesSummary().isEmpty() ? c.getClassesSummary() : (!c.getDepartment().isEmpty() ? c.getDepartment() : "N/A");
            String teacher = !c.getTeacher().trim().isEmpty() ? c.getTeacher() : "Unassigned";
            boolean unassigned = "Unassigned".equalsIgnoreCase(teacher);
            if (unassigned) missingCount++;
            else assignedCount++;
            String status = unassigned ? "Missing Teacher" : "Active";
            assignmentList.add(new CourseAssignmentAdapter.AssignmentItem(
                    code, name, clsOrDept, teacher, status));
        }

        View root = getView();
        if (root != null) {
            TextView tvTotalClasses = root.findViewById(R.id.tvTotalClasses);
            TextView tvFullyAssigned = root.findViewById(R.id.tvFullyAssigned);
            TextView tvMissingTeacher = root.findViewById(R.id.tvMissingTeacher);
            if (tvTotalClasses != null) tvTotalClasses.setText(String.valueOf(masterCourseList.size()));
            if (tvFullyAssigned != null) tvFullyAssigned.setText(String.valueOf(assignedCount));
            if (tvMissingTeacher != null) tvMissingTeacher.setText(String.valueOf(missingCount));
        }

        if (rvAssignments != null) {
            assignmentAdapter = new CourseAssignmentAdapter(getContext(), assignmentList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.course_assignment_detail)
                        .setMessage(getString(R.string.course_code) + ": " + item.courseCode + "\n" +
                                "Course Name: " + item.courseName + "\n" +
                                "Associated Class / Dept: " + item.className + "\n" +
                                "Assigned Teacher: " + item.teacherName + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvAssignments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAssignments.setAdapter(assignmentAdapter);
        }
    }

    private int parseIntOr(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourseData();
    }
}