package com.social7.studentlearnanalytics.admin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.AssignTeacherClassActivity;
import com.social7.studentlearnanalytics.admin.classes.CurriculumAuditAdapter;
import com.social7.studentlearnanalytics.admin.classes.RecordSelectorActivity;
import com.social7.studentlearnanalytics.admin.courses.AddCourseActivity;
import com.social7.studentlearnanalytics.admin.courses.AdminCourseDetailActivity;
import com.social7.studentlearnanalytics.admin.courses.CourseAdapter;
import com.social7.studentlearnanalytics.admin.courses.CourseAssignmentAdapter;
import com.social7.studentlearnanalytics.admin.courses.CourseCategoryAdapter;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    private View layoutCategoriesContainer;
    private View layoutCategoriesEmptyState;
    private TextView tvCategoriesEmptyTitle;
    private TextView tvCategoriesEmptyMessage;
    private MaterialButton btnCategoriesEmptyAction;
    private ProgressBar pbLoadingCategories;
    private String lastCategoriesApiError = null;

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
    private MaterialButton btnTabFaculties;
    private MaterialButton btnTabMajors;
    private MaterialButton btnTabCurriculum;
    private MaterialButton btnTabAssignments;

    private TextView tvPageIndicator;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;

    private int currentPage = 1;
    private final int itemsPerPage = 8;
    // 0=Course List, 1=Course Categories, 2=Curriculum, 3=Assignments, 4=Faculties, 5=Majors
    private int selectedTab = 0;

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
        layoutCategoriesContainer = view.findViewById(R.id.layoutCategoriesContainer);
        layoutCategoriesEmptyState = view.findViewById(R.id.layoutCategoriesEmptyState);
        tvCategoriesEmptyTitle = view.findViewById(R.id.tvCategoriesEmptyTitle);
        tvCategoriesEmptyMessage = view.findViewById(R.id.tvCategoriesEmptyMessage);
        btnCategoriesEmptyAction = view.findViewById(R.id.btnCategoriesEmptyAction);
        pbLoadingCategories = view.findViewById(R.id.pbLoadingCategories);

        etSearchCategory = view.findViewById(R.id.etSearchCategory);
        if (etSearchCategory != null) {
            etSearchCategory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (categoryAdapter != null) {
                        categoryAdapter.filter(s != null ? s.toString() : "");
                        updateCategoriesEmptyState();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnCategoriesEmptyAction != null) {
            btnCategoriesEmptyAction.setOnClickListener(v -> {
                if (lastCategoriesApiError != null) {
                    loadCategoriesData();
                } else if (etSearchCategory != null && etSearchCategory.getText() != null
                        && !etSearchCategory.getText().toString().trim().isEmpty()) {
                    etSearchCategory.setText("");
                } else {
                    showCategoryFormDialog(null);
                }
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
        View btnAddCategory = view.findViewById(R.id.btnAddCategory);
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
        btnTabFaculties = view.findViewById(R.id.btnTabFaculties);
        btnTabMajors = view.findViewById(R.id.btnTabMajors);
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

        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> showCategoryFormDialog(null));
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
        if (btnTabFaculties != null) {
            btnTabFaculties.setOnClickListener(v -> selectSectionTab(4));
        }
        if (btnTabMajors != null) {
            btnTabMajors.setOnClickListener(v -> selectSectionTab(5));
        }
        if (btnTabCurriculum != null) {
            btnTabCurriculum.setOnClickListener(v -> selectSectionTab(2));
        }
        if (btnTabAssignments != null) {
            btnTabAssignments.setOnClickListener(v -> selectSectionTab(3));
        }
    }

    private boolean isCatalogEntityTab(int tabIndex) {
        return tabIndex == 1 || tabIndex == 4 || tabIndex == 5;
    }

    private String getCatalogSingularLabel() {
        if (selectedTab == 4) return "Faculty";
        if (selectedTab == 5) return "Major";
        return "Course Category";
    }

    private String getCatalogPluralLabel() {
        if (selectedTab == 4) return "Faculties";
        if (selectedTab == 5) return "Majors";
        return "Categories";
    }

    private void selectSectionTab(int tabIndex) {
        this.selectedTab = tabIndex;
        updateTabStyles(tabIndex);

        View root = getView();
        if (root == null) return;

        com.google.android.material.appbar.AppBarLayout appBar = root.findViewById(R.id.appBarCourses);
        if (appBar != null) appBar.setExpanded(true, false);

        MaterialButton targetBtn = null;
        if (tabIndex == 0) targetBtn = btnTabAllCourses;
        else if (tabIndex == 1) targetBtn = btnTabCategories;
        else if (tabIndex == 4) targetBtn = btnTabFaculties;
        else if (tabIndex == 5) targetBtn = btnTabMajors;
        else if (tabIndex == 2) targetBtn = btnTabCurriculum;
        else if (tabIndex == 3) targetBtn = btnTabAssignments;
        if (targetBtn != null) {
            com.social7.studentlearnanalytics.utils.UiUtils.scrollToTab(targetBtn);
        }

        View ctrlList = root.findViewById(R.id.layoutCourseListControls);
        View ctrlCat = root.findViewById(R.id.layoutCategoriesControls);
        View ctrlCurr = root.findViewById(R.id.layoutCurriculumControls);
        View ctrlAssign = root.findViewById(R.id.layoutAssignmentsControls);

        boolean isCatTab = isCatalogEntityTab(tabIndex);
        if (ctrlList != null) ctrlList.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (ctrlCat != null) ctrlCat.setVisibility(isCatTab ? View.VISIBLE : View.GONE);
        if (ctrlCurr != null) ctrlCurr.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (ctrlAssign != null) ctrlAssign.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        View layoutCourseList = root.findViewById(R.id.layoutCourseListContainer);
        if (layoutCourseList != null) layoutCourseList.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutCategoriesContainer != null) layoutCategoriesContainer.setVisibility(isCatTab ? View.VISIBLE : View.GONE);
        if (rvCategories != null) rvCategories.setVisibility(isCatTab ? View.VISIBLE : View.GONE);
        if (rvCurriculumAudit != null) rvCurriculumAudit.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (rvAssignments != null) rvAssignments.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        if (layoutPagination != null) layoutPagination.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);

        if (isCatTab) {
            MaterialButton btnAddCat = root.findViewById(R.id.btnAddCategory);
            TextView tvLblTotal = root.findViewById(R.id.tvLabelTotalCatalog);
            TextView tvLblActive = root.findViewById(R.id.tvLabelActiveCatalog);
            String singular = getCatalogSingularLabel();
            String plural = getCatalogPluralLabel();
            if (btnAddCat != null) {
                btnAddCat.setText(tabIndex == 1 ? getString(R.string.add_category) : ("+ " + singular));
            }
            if (etSearchCategory != null) {
                etSearchCategory.setHint("Search " + plural.toLowerCase(Locale.ROOT) + "...");
            }
            if (tvLblTotal != null) tvLblTotal.setText("Total " + plural);
            if (tvLblActive != null) tvLblActive.setText("Active " + plural);
        }

        if (tabIndex == 0) {
            updatePaginationAndEmptyState();
        } else if (isCatTab) {
            loadCategoriesData();
        } else if (tabIndex == 2) {
            loadCurriculumAuditData();
        } else if (tabIndex == 3) {
            loadAssignmentsData();
        }
    }

    private void updateTabStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        int[] indices = new int[]{0, 1, 4, 5, 2, 3};
        MaterialButton[] tabs = new MaterialButton[]{
                btnTabAllCourses, btnTabCategories, btnTabFaculties, btnTabMajors, btnTabCurriculum, btnTabAssignments
        };

        for (int i = 0; i < tabs.length; i++) {
            MaterialButton btn = tabs[i];
            if (btn == null) continue;
            if (indices[i] == activeIndex) {
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
            pillFilterDepartment.setText(active ? ("Faculty: " + filterDeptName) : "Faculty: All");
            pillFilterDepartment.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterDepartment.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        if (pillFilterProgram != null) {
            boolean active = filterProgramId > 0 || !filterProgramName.isEmpty();
            pillFilterProgram.setText(active ? ("Major: " + filterProgramName) : "Major: All");
            pillFilterProgram.setBackgroundResource(active ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterProgram.setTextColor(getResources().getColor(active ? R.color.white : R.color.admin_text_primary, null));
        }

        if (pillFilterAcademicYear != null) {
            boolean active = filterAyId > 0 || !filterAyName.isEmpty();
            pillFilterAcademicYear.setText(active
                    ? getString(R.string.academic_year_filter_format, filterAyName)
                    : getString(R.string.academic_year_all));
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
        if (isCatalogEntityTab(selectedTab)) loadCategoriesData();
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
        if (!isAdded() || getContext() == null || course == null) return;
        Intent intent = new Intent(requireContext(), AdminCourseDetailActivity.class);
        intent.putExtra(AdminCourseDetailActivity.EXTRA_COURSE, course);
        addEditCourseLauncher.launch(intent);
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
                                "Faculty: " + item.className + "\n" +
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

        ensureCategoryAdapter();
        lastCategoriesApiError = null;
        if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.VISIBLE);
        if (layoutCategoriesEmptyState != null) layoutCategoriesEmptyState.setVisibility(View.GONE);

        final int requestTab = selectedTab;
        Call<JsonObject> call;
        if (requestTab == 4) {
            call = ApiClient.getApiService(getContext()).getDepartments();
        } else if (requestTab == 5) {
            call = ApiClient.getApiService(getContext()).getPrograms(null);
        } else {
            call = ApiClient.getApiService(getContext()).getCourseCategories();
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null || selectedTab != requestTab) return;
                if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonArray()) {
                        JsonArray arr = body.getAsJsonArray("data");
                        for (JsonElement el : arr) {
                            if (el != null && el.isJsonObject()) {
                                categoryList.add(CourseCategoryAdapter.CategoryItem.fromJson(el.getAsJsonObject()));
                            }
                        }
                    }
                    lastCategoriesApiError = null;
                    updateCategorySummaryCards();
                    categoryAdapter.updateData(categoryList);
                    updateCategoriesEmptyState();
                } else {
                    lastCategoriesApiError = extractApiErrorMessage(response, getString(R.string.network_error_server));
                    updateCategorySummaryCards();
                    categoryAdapter.updateData(categoryList);
                    updateCategoriesEmptyState();
                    if (isCatalogEntityTab(selectedTab)) {
                        Toast.makeText(getContext(), lastCategoriesApiError, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null || selectedTab != requestTab) return;
                if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load academic catalog data", t);
                lastCategoriesApiError = getString(R.string.network_error_server);
                updateCategorySummaryCards();
                categoryAdapter.updateData(categoryList);
                updateCategoriesEmptyState();
                if (isCatalogEntityTab(selectedTab)) {
                    Toast.makeText(getContext(), lastCategoriesApiError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ensureCategoryAdapter() {
        if (categoryAdapter != null || getContext() == null) return;
        categoryAdapter = new CourseCategoryAdapter(getContext(), categoryList, new CourseCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onViewCategory(CourseCategoryAdapter.CategoryItem item) {
                showCategoryDetailDialog(item);
            }

            @Override
            public void onEditCategory(CourseCategoryAdapter.CategoryItem item) {
                showCategoryFormDialog(item);
            }

            @Override
            public void onToggleStatus(CourseCategoryAdapter.CategoryItem item) {
                toggleCategoryStatus(item);
            }

            @Override
            public void onDeleteCategory(CourseCategoryAdapter.CategoryItem item) {
                confirmDeleteCategory(item);
            }
        });
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCategories.setAdapter(categoryAdapter);
        }
    }

    private void updateCategorySummaryCards() {
        View root = getView();
        if (root == null) return;
        int totalCount = categoryList.size();
        int activeCount = 0;
        for (CourseCategoryAdapter.CategoryItem item : categoryList) {
            if (item.isActive()) activeCount++;
        }
        TextView tvTotal = root.findViewById(R.id.tvTotalColleges);
        TextView tvActive = root.findViewById(R.id.tvTotalDepartments);
        if (tvTotal != null) tvTotal.setText(String.valueOf(totalCount));
        if (tvActive != null) tvActive.setText(String.valueOf(activeCount));
    }

    private void updateCategoriesEmptyState() {
        if (!isAdded() || categoryAdapter == null) return;

        int filteredCount = categoryAdapter.getFilteredCount();
        if (filteredCount > 0) {
            if (layoutCategoriesEmptyState != null) layoutCategoriesEmptyState.setVisibility(View.GONE);
            if (rvCategories != null) rvCategories.setVisibility(View.VISIBLE);
            return;
        }

        if (rvCategories != null) rvCategories.setVisibility(View.GONE);
        if (layoutCategoriesEmptyState != null) layoutCategoriesEmptyState.setVisibility(View.VISIBLE);

        String searchText = etSearchCategory != null && etSearchCategory.getText() != null
                ? etSearchCategory.getText().toString().trim()
                : "";
        String singular = getCatalogSingularLabel();
        String plural = getCatalogPluralLabel();

        if (lastCategoriesApiError != null) {
            if (tvCategoriesEmptyTitle != null) tvCategoriesEmptyTitle.setText("Unable to Load " + plural);
            if (tvCategoriesEmptyMessage != null) tvCategoriesEmptyMessage.setText(lastCategoriesApiError);
            if (btnCategoriesEmptyAction != null) btnCategoriesEmptyAction.setText(R.string.retry);
        } else if (!searchText.isEmpty() && !categoryList.isEmpty()) {
            if (tvCategoriesEmptyTitle != null) tvCategoriesEmptyTitle.setText("No Matching " + plural);
            if (tvCategoriesEmptyMessage != null) tvCategoriesEmptyMessage.setText(R.string.search_no_results);
            if (btnCategoriesEmptyAction != null) btnCategoriesEmptyAction.setText(R.string.reset_search_filters);
        } else {
            if (tvCategoriesEmptyTitle != null) tvCategoriesEmptyTitle.setText("No " + plural + " Yet");
            if (tvCategoriesEmptyMessage != null) tvCategoriesEmptyMessage.setText("Tap + " + singular + " to create your first " + singular.toLowerCase(Locale.ROOT) + ".");
            if (btnCategoriesEmptyAction != null) {
                btnCategoriesEmptyAction.setText(selectedTab == 1 ? getString(R.string.add_category) : ("+ " + singular));
            }
        }
    }

    private void showCategoryDetailDialog(CourseCategoryAdapter.CategoryItem item) {
        if (!isAdded() || getContext() == null || item == null) return;
        String desc = item.description != null && !item.description.trim().isEmpty()
                ? item.description.trim()
                : "No description provided.";
        String linked = item.buildDependencySummary();
        StringBuilder msg = new StringBuilder();
        msg.append("Code: ").append(item.code.isEmpty() ? "—" : item.code).append("\n");
        if (selectedTab == 5 && item.departmentName != null && !item.departmentName.isEmpty()) {
            msg.append("Faculty: ").append(item.departmentName).append("\n");
            msg.append("Duration: ").append(item.durationYears).append(" Years\n");
        }
        msg.append("Status: ").append(item.getDisplayStatus()).append("\n");
        msg.append("Courses: ").append(item.courseCount).append("\n");
        if (!linked.isEmpty()) {
            msg.append("Linked Records: ").append(linked).append("\n");
        }
        msg.append("\nDescription:\n").append(desc);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.name)
                .setMessage(msg.toString())
                .setPositiveButton("Edit", (d, w) -> showCategoryFormDialog(item))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void showCategoryFormDialog(@Nullable CourseCategoryAdapter.CategoryItem existing) {
        if (!isAdded() || getContext() == null) return;
        if (selectedTab == 5) {
            showMajorFormDialog(existing);
            return;
        }

        boolean isFaculty = (selectedTab == 4);
        String entityLabel = isFaculty ? "Faculty" : "Course Category";
        boolean isEdit = existing != null;
        float density = getResources().getDisplayMetrics().density;
        int padH = Math.round(20 * density);
        int padV = Math.round(12 * density);
        int marginBottom = Math.round(12 * density);

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padH, padV, padH, padV / 2);
        scrollView.addView(container);

        TextInputLayout tilName = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilName.setHint(entityLabel + " Name *");
        LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpName.bottomMargin = marginBottom;
        tilName.setLayoutParams(lpName);
        TextInputEditText etName = new TextInputEditText(tilName.getContext());
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etName.setSingleLine(true);
        if (isEdit) etName.setText(existing.name);
        tilName.addView(etName);
        container.addView(tilName);

        TextInputLayout tilCode = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilCode.setHint(entityLabel + " Code (Optional)");
        tilCode.setHelperText("Optional — auto-generated from name if left blank");
        LinearLayout.LayoutParams lpCode = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCode.bottomMargin = marginBottom;
        tilCode.setLayoutParams(lpCode);
        TextInputEditText etCode = new TextInputEditText(tilCode.getContext());
        etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etCode.setSingleLine(true);
        if (isEdit) etCode.setText(existing.code);
        tilCode.addView(etCode);
        container.addView(tilCode);

        TextInputLayout tilDesc = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilDesc.setHint("Description (Optional)");
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpDesc.bottomMargin = marginBottom;
        tilDesc.setLayoutParams(lpDesc);
        TextInputEditText etDesc = new TextInputEditText(tilDesc.getContext());
        etDesc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etDesc.setMinLines(2);
        etDesc.setMaxLines(4);
        if (isEdit && existing.description != null) etDesc.setText(existing.description);
        tilDesc.addView(etDesc);
        container.addView(tilDesc);

        TextView tvStatusLabel = new TextView(requireContext());
        tvStatusLabel.setText(entityLabel + " Status");
        tvStatusLabel.setTextSize(13f);
        tvStatusLabel.setPadding(0, 4, 0, 6);
        container.addView(tvStatusLabel);

        RadioGroup rgStatus = new RadioGroup(requireContext());
        rgStatus.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton rbActive = new RadioButton(requireContext());
        rbActive.setId(View.generateViewId());
        rbActive.setText(R.string.status_active);
        rbActive.setPadding(0, 0, Math.round(24 * density), 0);
        RadioButton rbInactive = new RadioButton(requireContext());
        rbInactive.setId(View.generateViewId());
        rbInactive.setText(R.string.status_inactive);
        rgStatus.addView(rbActive);
        rgStatus.addView(rbInactive);
        if (isEdit && !existing.isActive()) {
            rbInactive.setChecked(true);
        } else {
            rbActive.setChecked(true);
        }
        container.addView(rgStatus);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle((isEdit ? "Edit " : "Add ") + entityLabel)
                .setView(scrollView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnSave = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button btnCancel = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnSave.setOnClickListener(v -> {
                tilName.setError(null);
                tilCode.setError(null);

                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String code = etCode.getText() != null ? etCode.getText().toString().trim().toUpperCase(Locale.ROOT) : "";
                String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                String status = rbInactive.isChecked() ? "inactive" : "active";

                if (name.isEmpty()) {
                    tilName.setError(entityLabel + " name is required.");
                    etName.requestFocus();
                    return;
                }

                JsonObject body = new JsonObject();
                body.addProperty("name", name);
                if (!code.isEmpty()) {
                    body.addProperty("code", code);
                }
                body.addProperty("description", desc);
                body.addProperty("status", status);

                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);
                btnSave.setText("Saving...");

                Call<JsonObject> call;
                if (isFaculty) {
                    call = isEdit
                            ? ApiClient.getApiService(requireContext()).updateDepartment(existing.id, body)
                            : ApiClient.getApiService(requireContext()).createDepartment(body);
                } else {
                    call = isEdit
                            ? ApiClient.getApiService(requireContext()).updateCourseCategory(existing.id, body)
                            : ApiClient.getApiService(requireContext()).createCourseCategory(body);
                }

                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(
                                    getContext(),
                                    entityLabel + (isEdit ? " updated successfully." : " created successfully."),
                                    Toast.LENGTH_SHORT
                            ).show();
                            loadCategoriesData();
                            loadCourseData();
                        } else {
                            btnSave.setEnabled(true);
                            btnCancel.setEnabled(true);
                            btnSave.setText(R.string.save);
                            String errMsg = extractApiErrorMessage(response, "Unable to save " + entityLabel.toLowerCase(Locale.ROOT) + ".");
                            String lower = errMsg.toLowerCase(Locale.ROOT);
                            if (lower.contains("code")) {
                                tilCode.setError(errMsg);
                            } else {
                                tilName.setError(errMsg);
                            }
                            Toast.makeText(getContext(), errMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        btnSave.setEnabled(true);
                        btnCancel.setEnabled(true);
                        btnSave.setText(R.string.save);
                        Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void showMajorFormDialog(@Nullable CourseCategoryAdapter.CategoryItem existing) {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(requireContext()).getDepartments().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                List<Integer> facultyIds = new ArrayList<>();
                List<String> facultyNames = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().has("data")
                        && response.body().get("data").isJsonArray()) {
                    for (JsonElement el : response.body().getAsJsonArray("data")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject fObj = el.getAsJsonObject();
                        int fid = fObj.has("id") ? fObj.get("id").getAsInt() : -1;
                        String fName = fObj.has("name") && !fObj.get("name").isJsonNull() ? fObj.get("name").getAsString() : "";
                        String fCode = fObj.has("code") && !fObj.get("code").isJsonNull() ? fObj.get("code").getAsString() : "";
                        String fStatus = fObj.has("status") && !fObj.get("status").isJsonNull() ? fObj.get("status").getAsString() : "active";
                        if (fid > 0 && (!"inactive".equalsIgnoreCase(fStatus) || (existing != null && existing.departmentId == fid))) {
                            facultyIds.add(fid);
                            facultyNames.add(fCode.isEmpty() ? fName : (fName + " (" + fCode + ")"));
                        }
                    }
                }
                if (facultyIds.isEmpty()) {
                    Toast.makeText(getContext(), "Please create an active Faculty first before adding a Major.", Toast.LENGTH_LONG).show();
                    return;
                }
                renderMajorFormDialogWithFaculties(existing, facultyIds, facultyNames);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMajorFormDialogWithFaculties(
            @Nullable CourseCategoryAdapter.CategoryItem existing,
            List<Integer> facultyIds,
            List<String> facultyNames
    ) {
        boolean isEdit = existing != null;
        float density = getResources().getDisplayMetrics().density;
        int padH = Math.round(20 * density);
        int padV = Math.round(12 * density);
        int marginBottom = Math.round(12 * density);

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padH, padV, padH, padV / 2);
        scrollView.addView(container);

        TextView tvFacLabel = new TextView(requireContext());
        tvFacLabel.setText("Parent Faculty *");
        tvFacLabel.setTextSize(13f);
        tvFacLabel.setPadding(0, 0, 0, 6);
        container.addView(tvFacLabel);

        android.widget.Spinner spFaculty = new android.widget.Spinner(requireContext());
        android.widget.ArrayAdapter<String> facAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, facultyNames);
        spFaculty.setAdapter(facAdapter);
        LinearLayout.LayoutParams lpSp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpSp.bottomMargin = marginBottom;
        spFaculty.setLayoutParams(lpSp);
        if (isEdit && existing.departmentId > 0) {
            int idx = facultyIds.indexOf(existing.departmentId);
            if (idx >= 0) spFaculty.setSelection(idx);
        }
        container.addView(spFaculty);

        TextInputLayout tilName = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilName.setHint("Major Name *");
        LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpName.bottomMargin = marginBottom;
        tilName.setLayoutParams(lpName);
        TextInputEditText etName = new TextInputEditText(tilName.getContext());
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etName.setSingleLine(true);
        if (isEdit) etName.setText(existing.name);
        tilName.addView(etName);
        container.addView(tilName);

        TextInputLayout tilCode = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilCode.setHint("Major Code (Optional)");
        tilCode.setHelperText("Optional — auto-generated from name if left blank");
        LinearLayout.LayoutParams lpCode = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCode.bottomMargin = marginBottom;
        tilCode.setLayoutParams(lpCode);
        TextInputEditText etCode = new TextInputEditText(tilCode.getContext());
        etCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etCode.setSingleLine(true);
        if (isEdit) etCode.setText(existing.code);
        tilCode.addView(etCode);
        container.addView(tilCode);

        TextInputLayout tilDesc = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilDesc.setHint("Description (Optional)");
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpDesc.bottomMargin = marginBottom;
        tilDesc.setLayoutParams(lpDesc);
        TextInputEditText etDesc = new TextInputEditText(tilDesc.getContext());
        etDesc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etDesc.setMinLines(2);
        etDesc.setMaxLines(4);
        if (isEdit && existing.description != null) etDesc.setText(existing.description);
        tilDesc.addView(etDesc);
        container.addView(tilDesc);

        TextView tvStatusLabel = new TextView(requireContext());
        tvStatusLabel.setText("Major Status");
        tvStatusLabel.setTextSize(13f);
        tvStatusLabel.setPadding(0, 4, 0, 6);
        container.addView(tvStatusLabel);

        RadioGroup rgStatus = new RadioGroup(requireContext());
        rgStatus.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton rbActive = new RadioButton(requireContext());
        rbActive.setId(View.generateViewId());
        rbActive.setText(R.string.status_active);
        rbActive.setPadding(0, 0, Math.round(24 * density), 0);
        RadioButton rbInactive = new RadioButton(requireContext());
        rbInactive.setId(View.generateViewId());
        rbInactive.setText(R.string.status_inactive);
        rgStatus.addView(rbActive);
        rgStatus.addView(rbInactive);
        if (isEdit && !existing.isActive()) {
            rbInactive.setChecked(true);
        } else {
            rbActive.setChecked(true);
        }
        container.addView(rgStatus);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEdit ? "Edit Major" : "Add Major")
                .setView(scrollView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnSave = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button btnCancel = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnSave.setOnClickListener(v -> {
                tilName.setError(null);
                tilCode.setError(null);

                int facPos = spFaculty.getSelectedItemPosition();
                int selectedFacultyId = (facPos >= 0 && facPos < facultyIds.size()) ? facultyIds.get(facPos) : facultyIds.get(0);
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String code = etCode.getText() != null ? etCode.getText().toString().trim().toUpperCase(Locale.ROOT) : "";
                String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                String status = rbInactive.isChecked() ? "inactive" : "active";

                if (name.isEmpty()) {
                    tilName.setError("Major name is required.");
                    etName.requestFocus();
                    return;
                }

                JsonObject body = new JsonObject();
                body.addProperty("department_id", selectedFacultyId);
                body.addProperty("name", name);
                if (!code.isEmpty()) {
                    body.addProperty("code", code);
                }
                body.addProperty("description", desc);
                body.addProperty("status", status);

                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);
                btnSave.setText("Saving...");

                Call<JsonObject> call = isEdit
                        ? ApiClient.getApiService(requireContext()).updateProgram(parseIntOr(existing.id, 0), body)
                        : ApiClient.getApiService(requireContext()).createProgram(body);

                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(
                                    getContext(),
                                    isEdit ? "Major updated successfully." : "Major created successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            loadCategoriesData();
                            loadCourseData();
                        } else {
                            btnSave.setEnabled(true);
                            btnCancel.setEnabled(true);
                            btnSave.setText(R.string.save);
                            String errMsg = extractApiErrorMessage(response, "Unable to save major.");
                            String lower = errMsg.toLowerCase(Locale.ROOT);
                            if (lower.contains("code")) {
                                tilCode.setError(errMsg);
                            } else {
                                tilName.setError(errMsg);
                            }
                            Toast.makeText(getContext(), errMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        btnSave.setEnabled(true);
                        btnCancel.setEnabled(true);
                        btnSave.setText(R.string.save);
                        Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void toggleCategoryStatus(CourseCategoryAdapter.CategoryItem item) {
        if (!isAdded() || getContext() == null || item == null || item.id.isEmpty()) return;

        String entityLabel = getCatalogSingularLabel();
        String nextStatus = item.isActive() ? "inactive" : "active";
        JsonObject body = new JsonObject();
        body.addProperty("status", nextStatus);

        Call<JsonObject> call;
        if (selectedTab == 4) {
            call = ApiClient.getApiService(getContext()).updateDepartment(item.id, body);
        } else if (selectedTab == 5) {
            call = ApiClient.getApiService(getContext()).updateProgram(parseIntOr(item.id, 0), body);
        } else {
            call = ApiClient.getApiService(getContext()).updateCourseCategory(item.id, body);
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    Toast.makeText(
                            getContext(),
                            entityLabel + " \"" + item.name + "\" is now " + ("active".equals(nextStatus) ? "Active" : "Inactive") + ".",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadCategoriesData();
                } else {
                    String msg = extractApiErrorMessage(response, "Failed to update " + entityLabel.toLowerCase(Locale.ROOT) + " status.");
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteCategory(CourseCategoryAdapter.CategoryItem item) {
        if (!isAdded() || getContext() == null || item == null || item.id.isEmpty()) return;

        String entityLabel = getCatalogSingularLabel();
        if (item.hasLinkedRecords()) {
            String summary = item.buildDependencySummary();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cannot Delete " + entityLabel)
                    .setMessage("\"" + item.name + "\" is linked to " + summary
                            + ". Deleting it could break existing academic relationships.\n\n"
                            + "Deactivate this " + entityLabel.toLowerCase(Locale.ROOT) + " instead to keep existing records intact.")
                    .setNegativeButton(R.string.close, null);
            if (item.isActive()) {
                builder.setPositiveButton("Deactivate Instead", (d, w) -> toggleCategoryStatus(item));
            }
            builder.show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete " + entityLabel + "?")
                .setMessage("Are you sure you want to delete \"" + item.name + "\"? This action cannot be undone.")
                .setPositiveButton(R.string.delete, (dialog, which) -> executeDeleteCategory(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeDeleteCategory(CourseCategoryAdapter.CategoryItem item) {
        if (!isAdded() || getContext() == null) return;

        String entityLabel = getCatalogSingularLabel();
        Call<JsonObject> call;
        if (selectedTab == 4) {
            call = ApiClient.getApiService(getContext()).deleteDepartment(item.id);
        } else if (selectedTab == 5) {
            call = ApiClient.getApiService(getContext()).deleteProgram(parseIntOr(item.id, 0));
        } else {
            call = ApiClient.getApiService(getContext()).deleteCourseCategory(item.id);
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), entityLabel + " \"" + item.name + "\" deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadCategoriesData();
                } else if (response.code() == 422) {
                    String msg = extractApiErrorMessage(
                            response,
                            "This " + entityLabel.toLowerCase(Locale.ROOT) + " is linked to existing records and cannot be deleted."
                    );
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Deletion Prevented")
                            .setMessage(msg)
                            .setNegativeButton(R.string.close, null);
                    if (item.isActive()) {
                        builder.setPositiveButton("Deactivate Instead", (d, w) -> toggleCategoryStatus(item));
                    }
                    builder.show();
                    loadCategoriesData();
                } else {
                    String msg = extractApiErrorMessage(response, "Unable to delete " + entityLabel.toLowerCase(Locale.ROOT) + ".");
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractApiErrorMessage(@NonNull Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                JsonObject err = JsonParser.parseString(raw).getAsJsonObject();
                if (err.has("errors") && err.get("errors").isJsonObject()) {
                    JsonObject errors = err.getAsJsonObject("errors");
                    for (String key : errors.keySet()) {
                        if (errors.get(key).isJsonArray() && errors.getAsJsonArray(key).size() > 0) {
                            return errors.getAsJsonArray(key).get(0).getAsString();
                        }
                    }
                }
                if (err.has("message") && !err.get("message").isJsonNull()) {
                    String msg = err.get("message").getAsString();
                    if (msg != null && !msg.trim().isEmpty()) {
                        return msg;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
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
                                "Associated Class / Faculty: " + item.className + "\n" +
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