package com.social7.studentlearnanalytics.admin.classes;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.model.ClassModel;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassManagementFragment extends Fragment {

    private static final String TAG = "ClassManagement";

    // Primary Tabs
    private MaterialButton btnSubTabClassDirectory, btnSubTabClassSchedules, btnSubTabBuildingManagement, btnSubTabTeacherAssignments, btnSubTabClassEnrollment;

    // Containers & AppBar
    private com.google.android.material.appbar.AppBarLayout appBarClasses;
    private View layoutClassDirectoryContainer, layoutSchedulesContainer, layoutBuildingsContainer, layoutTeacherAssignContainer, layoutClassEnrollmentContainer;
    private View layoutPagination;

    // Tab 0: Class Directory
    private RecyclerView rvClasses;
    private EditText etSearchClass;
    private MaterialButton btnAddClass;
    private ClassAdapter classAdapter;
    private final List<ClassModel> classList = new ArrayList<>();
    private TextView pillFilterAll, pillFilterActive, pillFilterInactive;

    // Tab 1: Class Schedules
    private RecyclerView rvSchedules;
    private ClassScheduleAdapter scheduleAdapter;
    private final List<ClassScheduleAdapter.ScheduleItem> scheduleList = new ArrayList<>();
    private final List<ClassModel> scheduleClassOptions = new ArrayList<>();
    private EditText etSearchSchedule;
    private MaterialButton btnAddSchedule, btnSchedulesEmptyAction;
    private TextView tvTotalSchedules, tvScheduleConflictStatus, tvScheduleActiveHours;
    private AutoCompleteTextView actvScheduleClassFilter;
    private TextView tvSelectedScheduleClassInfo, tvSchedulesEmptyTitle, tvSchedulesEmptySubtitle;
    private LinearLayout layoutScheduleDayPills, layoutSchedulesEmptyState;
    private ProgressBar pbSchedulesState;
    private Integer selectedScheduleClassId = null;
    private String selectedScheduleDay = null;
    private boolean scheduleLoading = false;
    private boolean scheduleLastRequestFailed = false;

    // Tab 2: Building Management
    private RecyclerView rvBuildings;
    private BuildingAdapter buildingAdapter;
    private final List<BuildingAdapter.BuildingItem> buildingList = new ArrayList<>();
    private EditText etSearchBuilding;
    private MaterialButton btnAddBuilding;
    private TextView tvTotalBuildings, tvTotalFloors, tvAssignedClasses;

    // Tab 3: Teacher Assignments
    private RecyclerView rvTeacherAssignments;
    private ClassTeacherAssignmentAdapter teacherAssignAdapter;
    private final List<ClassTeacherAssignmentAdapter.TeacherAssignItem> teacherAssignList = new ArrayList<>();
    private EditText etSearchTeacherAssign;
    private MaterialButton btnAssignTeacher;
    private TextView tvTotalAssignedTeachers, tvStaffedClasses, tvUnassignedClasses;

    // Tab 4: Class Enrollment
    private RecyclerView rvEnrollments;
    private ClassEnrollmentAdapter enrollmentAdapter;
    private final List<ClassEnrollmentAdapter.EnrollmentItem> enrollmentList = new ArrayList<>();
    private EditText etSearchEnrollment;
    private MaterialButton btnEnrollStudent;
    private TextView tvTotalEnrolled, tvCapacityRate, tvFullCohorts;

    // Current Active Tab (0=Directory, 1=Schedules, 2=BuildingMgmt, 3=TeacherAssign, 4=Enrollment)
    private int selectedTab = 0;

    private final ActivityResultLauncher<Intent> addClassLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadClassData();
                    loadBuildingData();
                }
            });

    private final ActivityResultLauncher<Intent> addScheduleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadScheduleData();
                    loadTeacherAssignData();
                }
            });

    private final ActivityResultLauncher<Intent> addBuildingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadBuildingData();
                }
            });

    private final ActivityResultLauncher<Intent> assignTeacherLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadTeacherAssignData();
                    loadClassData();
                }
            });

    private final ActivityResultLauncher<Intent> bulkEnrollLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadEnrollmentData();
                    loadClassData();
                }
            });

    public ClassManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_admin_classes, container, false);

        View btnMenu = view.findViewById(R.id.btnUsersMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        View btnNotifBell = view.findViewById(R.id.imgNotifBell);
        if (btnNotifBell != null) {
            btnNotifBell.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new com.social7.studentlearnanalytics.admin.NotificationFragment());
                }
            });
        }

        TextView tvTitle = view.findViewById(R.id.tvHeaderTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvHeaderSubtitle);
        if (tvTitle != null) tvTitle.setText(R.string.class_management_title);
        if (tvSubtitle != null) tvSubtitle.setText(R.string.class_management_subtitle);

        btnSubTabClassDirectory = view.findViewById(R.id.btnSubTabClassDirectory);
        btnSubTabClassSchedules = view.findViewById(R.id.btnSubTabClassSchedules);
        btnSubTabBuildingManagement = view.findViewById(R.id.btnSubTabBuildingManagement);
        btnSubTabTeacherAssignments = view.findViewById(R.id.btnSubTabTeacherAssignments);
        btnSubTabClassEnrollment = view.findViewById(R.id.btnSubTabClassEnrollment);

        appBarClasses = view.findViewById(R.id.appBarClasses);
        layoutClassDirectoryContainer = view.findViewById(R.id.layoutClassDirectoryControls);
        layoutSchedulesContainer = view.findViewById(R.id.layoutSchedulesControls);
        layoutBuildingsContainer = view.findViewById(R.id.layoutBuildingControls);
        layoutTeacherAssignContainer = view.findViewById(R.id.layoutTeacherAssignControls);
        layoutClassEnrollmentContainer = view.findViewById(R.id.layoutClassEnrollmentControls);
        layoutPagination = view.findViewById(R.id.layoutPagination);

        bindClassDirectoryViews(view);
        bindSchedulesViews(view);
        bindBuildingsViews(view);
        bindTeacherAssignmentsViews(view);
        bindEnrollmentsViews(view);
        setupPagination(view);

        setupTabListeners();
        selectClassTab(selectedTab);

        return view;
    }

    private void setupTabListeners() {
        if (btnSubTabClassDirectory != null) btnSubTabClassDirectory.setOnClickListener(v -> selectClassTab(0));
        if (btnSubTabClassSchedules != null) btnSubTabClassSchedules.setOnClickListener(v -> selectClassTab(1));
        if (btnSubTabBuildingManagement != null) btnSubTabBuildingManagement.setOnClickListener(v -> selectClassTab(2));
        if (btnSubTabTeacherAssignments != null) btnSubTabTeacherAssignments.setOnClickListener(v -> selectClassTab(3));
        if (btnSubTabClassEnrollment != null) btnSubTabClassEnrollment.setOnClickListener(v -> selectClassTab(4));
    }

    public void selectClassTab(int tabIndex) {
        this.selectedTab = tabIndex;
        updateTabButtonStyles(tabIndex);

        if (appBarClasses != null) {
            appBarClasses.setExpanded(true, false);
        }

        if (layoutClassDirectoryContainer != null) layoutClassDirectoryContainer.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutSchedulesContainer != null) layoutSchedulesContainer.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutBuildingsContainer != null) layoutBuildingsContainer.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutTeacherAssignContainer != null) layoutTeacherAssignContainer.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        if (layoutClassEnrollmentContainer != null) layoutClassEnrollmentContainer.setVisibility(tabIndex == 4 ? View.VISIBLE : View.GONE);

        if (rvClasses != null) rvClasses.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (rvSchedules != null && tabIndex != 1) rvSchedules.setVisibility(View.GONE);
        if (pbSchedulesState != null && tabIndex != 1) pbSchedulesState.setVisibility(View.GONE);
        if (layoutSchedulesEmptyState != null && tabIndex != 1) layoutSchedulesEmptyState.setVisibility(View.GONE);
        if (rvBuildings != null) rvBuildings.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (rvTeacherAssignments != null) rvTeacherAssignments.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        if (rvEnrollments != null) rvEnrollments.setVisibility(tabIndex == 4 ? View.VISIBLE : View.GONE);
        if (layoutPagination != null) layoutPagination.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);

        MaterialButton[] tabs = new MaterialButton[]{
                btnSubTabClassDirectory, btnSubTabClassSchedules, btnSubTabBuildingManagement, btnSubTabTeacherAssignments, btnSubTabClassEnrollment
        };
        if (tabIndex >= 0 && tabIndex < tabs.length) {
            com.social7.studentlearnanalytics.utils.UiUtils.scrollToTab(tabs[tabIndex]);
        }

        if (tabIndex == 0) {
            loadClassData();
        } else if (tabIndex == 1) {
            ensureScheduleClassesAndLoad();
        } else if (tabIndex == 2) {
            loadBuildingData();
        } else if (tabIndex == 3) {
            loadTeacherAssignData();
        } else if (tabIndex == 4) {
            loadEnrollmentData();
        }
    }

    private void updateTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        MaterialButton[] tabs = new MaterialButton[]{
                btnSubTabClassDirectory, btnSubTabClassSchedules, btnSubTabBuildingManagement, btnSubTabTeacherAssignments, btnSubTabClassEnrollment
        };

        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveBg = Color.parseColor("#F4F7FF");
        int textSecondary = getResources().getColor(R.color.admin_text_secondary, null);

        for (int i = 0; i < tabs.length; i++) {
            MaterialButton btn = tabs[i];
            if (btn == null) continue;
            if (i == activeIndex) {
                btn.setBackgroundTintList(ColorStateList.valueOf(activeColor));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
                btn.setTextColor(textSecondary);
            }
        }
    }

    // ==========================================
    // 0. CLASS DIRECTORY TAB
    // ==========================================
    private void bindClassDirectoryViews(View root) {
        rvClasses = root.findViewById(R.id.rvUsers);
        etSearchClass = root.findViewById(R.id.etSearchUser);
        btnAddClass = root.findViewById(R.id.btnAddUser);

        pillFilterAll = root.findViewById(R.id.pillFilterAll);
        pillFilterActive = root.findViewById(R.id.pillFilterActive);
        pillFilterInactive = root.findViewById(R.id.pillFilterInactive);

        if (btnAddClass != null) {
            btnAddClass.setText(R.string.add_new_class_d8426f);
            btnAddClass.setOnClickListener(v -> showAddClassDialog(null));
        }

        setupSearchAndFilter();
    }

    private void setupSearchAndFilter() {
        if (etSearchClass != null) {
            etSearchClass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (classAdapter != null) classAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAll != null) {
            pillFilterAll.setOnClickListener(v -> {
                updatePillSelection(pillFilterAll, pillFilterActive, pillFilterInactive);
                if (classAdapter != null) classAdapter.filterByStatus("ALL");
            });
        }

        if (pillFilterActive != null) {
            pillFilterActive.setOnClickListener(v -> {
                updatePillSelection(pillFilterActive, pillFilterAll, pillFilterInactive);
                if (classAdapter != null) classAdapter.filterByStatus("Active");
            });
        }

        if (pillFilterInactive != null) {
            pillFilterInactive.setOnClickListener(v -> {
                updatePillSelection(pillFilterInactive, pillFilterAll, pillFilterActive);
                if (classAdapter != null) classAdapter.filterByStatus("Inactive");
            });
        }
    }

    private void updatePillSelection(TextView selected, TextView unselected1, TextView unselected2) {
        if (!isAdded() || getContext() == null) return;
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_notif_tab_active);
            selected.setTextColor(Color.WHITE);
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

    private void loadClassData() {
        loadClassesFromApi(null);
    }

    private void loadClassesFromApi(@Nullable Runnable onDone) {
        if (!isAdded() || getContext() == null) return;

        try {
            ApiClient.getApiService(getContext()).getClasses().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded()) return;
                    classList.clear();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject body = response.body();
                            JsonArray array = null;
                            if (body.has("data") && body.get("data").isJsonArray()) {
                                array = body.getAsJsonArray("data");
                            } else if (body.has("classes") && body.get("classes").isJsonArray()) {
                                array = body.getAsJsonArray("classes");
                            }

                            if (array != null) {
                                for (JsonElement elem : array) {
                                    if (elem.isJsonObject()) {
                                        JsonObject obj = elem.getAsJsonObject();
                                        String id = getSafeString(obj, "id", String.valueOf(classList.size() + 1));
                                        String name = getSafeString(obj, "name", getSafeString(obj, "class_name", "Class"));
                                        String code = getSafeString(obj, "code", getSafeString(obj, "class_code", "CLASS"));
                                        String dept = getSafeString(obj, "department", "Faculty");
                                        String ay = getSafeString(obj, "academic_year", "");
                                        String course = getSafeString(obj, "courses_summary", getSafeString(obj, "course", "No courses assigned"));
                                        String teacher = getSafeString(obj, "teachers_summary", getSafeString(obj, "teacher", "Unassigned"));
                                        String status = getSafeString(obj, "status", "Active");
                                        int count = getSafeInt(obj, "students_count", getSafeInt(obj, "student_count", 0));

                                        ClassModel cm = new ClassModel(id, name, code, dept, ay, course, teacher, status, count);
                                        cm.setDepartmentId(getSafeInt(obj, "department_id", 0));
                                        cm.setAcademicYearId(getSafeInt(obj, "academic_year_id", 0));
                                        cm.setBuildingId(getSafeInt(obj, "building_id", 0));
                                        cm.setFloorId(getSafeInt(obj, "floor_id", 0));
                                        Integer parsedYl = com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(
                                                getSafeString(obj, "year_level", ""));
                                        cm.setYearLevel(parsedYl != null ? parsedYl : 0);
                                        cm.setYearLevelCode(getSafeString(obj, "year_level_code", ""));
                                        cm.setYearLevelLabel(getSafeString(obj, "year_level_label", ""));
                                        cm.setCapacity(getSafeInt(obj, "capacity", 40));
                                        cm.setBuildingName(getSafeString(obj, "building_name", getSafeString(obj, "building", "")));
                                        cm.setFloorName(getSafeString(obj, "floor_name", getSafeString(obj, "floor", "")));
                                        cm.setLocationLabel(getSafeString(obj, "location_label", ""));
                                        classList.add(cm);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing classes JSON response", e);
                    }

                    bindClassAdapter();
                    refreshScheduleClassDropdown();
                    if (onDone != null) onDone.run();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    bindClassAdapter();
                    refreshScheduleClassDropdown();
                    if (onDone != null) onDone.run();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading classes", e);
            if (onDone != null) onDone.run();
        }
    }

    private void bindClassAdapter() {
        if (!isAdded() || getContext() == null) return;

        classAdapter = new ClassAdapter(getContext(), classList, this::showClassDetailsDialog);

        if (rvClasses != null) {
            rvClasses.setLayoutManager(new LinearLayoutManager(getContext()));
            rvClasses.setAdapter(classAdapter);
        }

        if (pillFilterAll != null && isAdded()) pillFilterAll.setText(getString(R.string.all_classes_count, classList.size()));
        int activeCount = 0;
        int inactiveCount = 0;
        for (ClassModel c : classList) {
            if ("Active".equalsIgnoreCase(c.getStatus())) activeCount++;
            else inactiveCount++;
        }
        if (pillFilterActive != null && isAdded()) pillFilterActive.setText(getString(R.string.active_count, activeCount));
        if (pillFilterInactive != null && isAdded()) pillFilterInactive.setText(getString(R.string.inactive_count, inactiveCount));
    }

    private void showClassDetailsDialog(ClassModel classModel) {
        if (!isAdded() || getContext() == null || classModel == null) return;
        Intent intent = new Intent(requireContext(), AdminClassDetailActivity.class);
        intent.putExtra(AdminClassDetailActivity.EXTRA_CLASS, classModel);
        addClassLauncher.launch(intent);
    }

    private void showAddClassDialog(@Nullable ClassModel editModel) {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(getContext(), AddClassActivity.class);
        if (editModel != null) {
            intent.putExtra(AddClassActivity.EXTRA_EDIT_CLASS, editModel);
        }
        addClassLauncher.launch(intent);
    }

    // ==========================================
    // 1. CLASS SCHEDULES TAB
    // ==========================================
    private void bindSchedulesViews(View root) {
        rvSchedules = root.findViewById(R.id.rvSchedules);
        etSearchSchedule = root.findViewById(R.id.etSearchSchedule);
        btnAddSchedule = root.findViewById(R.id.btnAddSchedule);
        tvTotalSchedules = root.findViewById(R.id.tvTotalSchedules);
        tvScheduleConflictStatus = root.findViewById(R.id.tvScheduleConflictStatus);
        tvScheduleActiveHours = root.findViewById(R.id.tvScheduleActiveHours);
        actvScheduleClassFilter = root.findViewById(R.id.actvScheduleClassFilter);
        tvSelectedScheduleClassInfo = root.findViewById(R.id.tvSelectedScheduleClassInfo);
        layoutScheduleDayPills = root.findViewById(R.id.layoutScheduleDayPills);
        pbSchedulesState = root.findViewById(R.id.pbSchedulesState);
        layoutSchedulesEmptyState = root.findViewById(R.id.layoutSchedulesEmptyState);
        tvSchedulesEmptyTitle = root.findViewById(R.id.tvSchedulesEmptyTitle);
        tvSchedulesEmptySubtitle = root.findViewById(R.id.tvSchedulesEmptySubtitle);
        btnSchedulesEmptyAction = root.findViewById(R.id.btnSchedulesEmptyAction);

        if (etSearchSchedule != null) {
            etSearchSchedule.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (scheduleAdapter != null) {
                        scheduleAdapter.filter(s.toString());
                        updateSchedulesStateViews();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddSchedule != null) {
            btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());
        }

        setupScheduleDayPills();
        refreshScheduleClassDropdown();
    }

    private void ensureScheduleClassesAndLoad() {
        if (!isAdded() || getContext() == null) return;
        if (classList.isEmpty()) {
            scheduleLoading = true;
            scheduleLastRequestFailed = false;
            updateSchedulesStateViews();
            loadClassesFromApi(this::loadScheduleData);
        } else {
            refreshScheduleClassDropdown();
            loadScheduleData();
        }
    }

    private void refreshScheduleClassDropdown() {
        if (!isAdded() || getContext() == null || actvScheduleClassFilter == null) return;

        scheduleClassOptions.clear();
        scheduleClassOptions.addAll(classList);
        Collections.sort(scheduleClassOptions, (a, b) -> {
            String codeA = a.getCode() != null ? a.getCode().toUpperCase(Locale.US) : "";
            String codeB = b.getCode() != null ? b.getCode().toUpperCase(Locale.US) : "";
            boolean isCohortA = codeA.startsWith("CLASS-");
            boolean isCohortB = codeB.startsWith("CLASS-");
            if (isCohortA != isCohortB) return isCohortA ? -1 : 1;
            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.all_classes_option) + " (" + scheduleClassOptions.size() + ")");
        for (ClassModel cm : scheduleClassOptions) {
            labels.add(formatClassOptionLabel(cm));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
        actvScheduleClassFilter.setAdapter(adapter);

        int selectedIdx = 0;
        if (selectedScheduleClassId != null) {
            for (int i = 0; i < scheduleClassOptions.size(); i++) {
                if (parseIntSafe(scheduleClassOptions.get(i).getId()) == selectedScheduleClassId) {
                    selectedIdx = i + 1;
                    break;
                }
            }
            if (selectedIdx == 0) {
                selectedScheduleClassId = null;
            }
        }
        actvScheduleClassFilter.setText(labels.get(selectedIdx), false);
        updateSelectedScheduleClassInfo();

        actvScheduleClassFilter.setOnClickListener(v -> actvScheduleClassFilter.showDropDown());
        actvScheduleClassFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedScheduleClassId = null;
            } else {
                int optIndex = position - 1;
                if (optIndex >= 0 && optIndex < scheduleClassOptions.size()) {
                    int parsed = parseIntSafe(scheduleClassOptions.get(optIndex).getId());
                    selectedScheduleClassId = parsed > 0 ? parsed : null;
                }
            }
            updateSelectedScheduleClassInfo();
            loadScheduleData();
        });
    }

    private String formatClassOptionLabel(ClassModel cm) {
        String name = cm.getName() != null ? cm.getName() : "Class";
        String code = cm.getCode() != null ? cm.getCode() : "";
        return code.isEmpty() ? name : (name + " (" + code + ")");
    }

    private void updateSelectedScheduleClassInfo() {
        if (tvSelectedScheduleClassInfo == null || !isAdded()) return;
        ClassModel selected = findSelectedScheduleClassModel();
        if (selected == null) {
            tvSelectedScheduleClassInfo.setText(String.format(Locale.US,
                    "Showing timetables across %d active classes • Select a class to manage its schedule",
                    scheduleClassOptions.size()));
        } else {
            StringBuilder info = new StringBuilder(formatClassOptionLabel(selected));
            String loc = selected.getLocationLabel();
            if (loc != null && !loc.isEmpty()) {
                info.append(" • ").append(loc);
            }
            if (selected.getDepartment() != null && !selected.getDepartment().isEmpty()) {
                info.append(" • ").append(selected.getDepartment());
            }
            tvSelectedScheduleClassInfo.setText(info.toString());
        }
    }

    @Nullable
    private ClassModel findSelectedScheduleClassModel() {
        if (selectedScheduleClassId == null) return null;
        for (ClassModel cm : scheduleClassOptions) {
            if (parseIntSafe(cm.getId()) == selectedScheduleClassId) {
                return cm;
            }
        }
        return null;
    }

    private int parseIntSafe(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void setupScheduleDayPills() {
        if (layoutScheduleDayPills == null || !isAdded() || getContext() == null) return;
        layoutScheduleDayPills.removeAllViews();

        String[] dayValues = new String[]{null, "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] dayLabels = new String[]{"All Days", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        float density = getResources().getDisplayMetrics().density;
        int hPad = (int) (12 * density);
        int vPad = (int) (6 * density);
        int gap = (int) (6 * density);

        for (int i = 0; i < dayValues.length; i++) {
            final String dayVal = dayValues[i];
            TextView pill = new TextView(requireContext());
            pill.setText(dayLabels[i]);
            pill.setTextSize(12);
            pill.setPadding(hPad, vPad, hPad, vPad);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < dayValues.length - 1) lp.setMarginEnd(gap);
            pill.setLayoutParams(lp);
            pill.setTag(dayVal);
            pill.setOnClickListener(v -> {
                selectedScheduleDay = dayVal;
                refreshScheduleDayPillStyles();
                loadScheduleData();
            });
            layoutScheduleDayPills.addView(pill);
        }
        refreshScheduleDayPillStyles();
    }

    private void refreshScheduleDayPillStyles() {
        if (layoutScheduleDayPills == null || !isAdded()) return;
        int inactiveText = getResources().getColor(R.color.admin_text_secondary, null);
        for (int i = 0; i < layoutScheduleDayPills.getChildCount(); i++) {
            View child = layoutScheduleDayPills.getChildAt(i);
            if (!(child instanceof TextView)) continue;
            TextView tv = (TextView) child;
            Object tag = tv.getTag();
            boolean isSelected = (selectedScheduleDay == null && tag == null)
                    || (selectedScheduleDay != null && selectedScheduleDay.equals(tag));
            tv.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            tv.setTextColor(isSelected ? Color.WHITE : inactiveText);
        }
    }

    private void loadScheduleData() {
        if (!isAdded() || getContext() == null) return;

        if (rvSchedules != null && scheduleAdapter == null) {
            scheduleAdapter = new ClassScheduleAdapter(getContext(), scheduleList, new ClassScheduleAdapter.OnScheduleClickListener() {
                @Override
                public void onScheduleClick(ClassScheduleAdapter.ScheduleItem item) {
                    showScheduleDetailDialog(item);
                }

                @Override
                public void onEditSchedule(ClassScheduleAdapter.ScheduleItem item) {
                    showEditScheduleDialog(item);
                }

                @Override
                public void onDeleteSchedule(ClassScheduleAdapter.ScheduleItem item) {
                    confirmDeleteSchedule(item);
                }
            });
            rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSchedules.setAdapter(scheduleAdapter);
        }

        scheduleLoading = true;
        scheduleLastRequestFailed = false;
        updateSchedulesStateViews();

        ApiClient.getApiService(getContext())
                .getClassSchedules(selectedScheduleClassId, selectedScheduleDay, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        scheduleLoading = false;
                        scheduleList.clear();
                        double totalHours = 0;

                        if (response.isSuccessful() && response.body() != null) {
                            scheduleLastRequestFailed = false;
                            JsonArray arr = response.body().has("data") && response.body().get("data").isJsonArray()
                                    ? response.body().getAsJsonArray("data") : new JsonArray();
                            for (JsonElement el : arr) {
                                if (!el.isJsonObject()) continue;
                                JsonObject o = el.getAsJsonObject();
                                String sId = getSafeString(o, "id", "");
                                int sClassId = getSafeInt(o, "class_id", 0);
                                String day = getSafeString(o, "day_of_week", "");
                                String startRaw = getSafeString(o, "start_time", "");
                                String endRaw = getSafeString(o, "end_time", "");
                                String startDisp = formatDisplayTime(startRaw);
                                String endDisp = formatDisplayTime(endRaw);
                                double hrs = computeDurationHours(startRaw, endRaw);
                                totalHours += hrs;

                                JsonObject classObj = null;
                                if (o.has("class_room") && o.get("class_room").isJsonObject()) {
                                    classObj = o.getAsJsonObject("class_room");
                                } else if (o.has("classRoom") && o.get("classRoom").isJsonObject()) {
                                    classObj = o.getAsJsonObject("classRoom");
                                }
                                String className = classObj != null ? getSafeString(classObj, "name", "Class") : "Class";
                                if (sClassId <= 0 && classObj != null) {
                                    sClassId = getSafeInt(classObj, "id", 0);
                                }

                                String bName = o.has("building") && o.get("building").isJsonObject()
                                        ? getSafeString(o.getAsJsonObject("building"), "name", "") : "";
                                String fName = o.has("floor") && o.get("floor").isJsonObject()
                                        ? getSafeString(o.getAsJsonObject("floor"), "name", "") : "";
                                if (bName.isEmpty() && classObj != null && classObj.has("building") && classObj.get("building").isJsonObject()) {
                                    bName = getSafeString(classObj.getAsJsonObject("building"), "name", "");
                                }
                                if (fName.isEmpty() && classObj != null && classObj.has("floor") && classObj.get("floor").isJsonObject()) {
                                    fName = getSafeString(classObj.getAsJsonObject("floor"), "name", "");
                                }
                                String locLabel = (!bName.isEmpty() ? bName + " • " : "")
                                        + (!fName.isEmpty() ? fName + " • " : "")
                                        + className;

                                String cCode = "";
                                String cName = "Course";
                                if (o.has("course") && o.get("course").isJsonObject()) {
                                    JsonObject cObj = o.getAsJsonObject("course");
                                    cCode = getSafeString(cObj, "course_code", getSafeString(cObj, "code", ""));
                                    cName = getSafeString(cObj, "course_name", getSafeString(cObj, "name", "Course"));
                                }
                                String courseDisp = cCode.isEmpty() ? cName : (cCode + " — " + cName);

                                String tName = "Unassigned";
                                if (o.has("teacher") && o.get("teacher").isJsonObject()) {
                                    JsonObject tObj = o.getAsJsonObject("teacher");
                                    tName = getSafeString(tObj, "user",
                                            (getSafeString(tObj, "first_name", "") + " " + getSafeString(tObj, "last_name", "")).trim());
                                    if (tName.isEmpty()) tName = getSafeString(tObj, "teacher_code", "Teacher");
                                }

                                String st = getSafeString(o, "status", "active");
                                String stCap = st.isEmpty() ? "Active" : (st.substring(0, 1).toUpperCase(Locale.US) + st.substring(1));

                                scheduleList.add(new ClassScheduleAdapter.ScheduleItem(
                                        sId, sClassId, className, courseDisp, tName, day,
                                        day + " " + startDisp + " - " + endDisp,
                                        locLabel, stCap, hrs, o.toString()));
                            }
                        } else {
                            scheduleLastRequestFailed = true;
                        }

                        if (tvTotalSchedules != null) tvTotalSchedules.setText(String.valueOf(scheduleList.size()));
                        if (tvScheduleConflictStatus != null) {
                            if (scheduleLastRequestFailed) {
                                tvScheduleConflictStatus.setText("Error");
                            } else {
                                tvScheduleConflictStatus.setText(scheduleList.isEmpty() ? "Empty" : "No Conflicts");
                            }
                        }
                        if (tvScheduleActiveHours != null) {
                            String hrsStr = totalHours == Math.floor(totalHours)
                                    ? String.format(Locale.US, "%.0f hrs/wk", totalHours)
                                    : String.format(Locale.US, "%.1f hrs/wk", totalHours);
                            tvScheduleActiveHours.setText(hrsStr);
                        }
                        String currentSearch = etSearchSchedule != null ? etSearchSchedule.getText().toString() : "";
                        if (scheduleAdapter != null) scheduleAdapter.filter(currentSearch);
                        updateSchedulesStateViews();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        scheduleLoading = false;
                        scheduleLastRequestFailed = true;
                        if (tvScheduleConflictStatus != null) tvScheduleConflictStatus.setText("Error");
                        updateSchedulesStateViews();
                        Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSchedulesStateViews() {
        if (selectedTab != 1) {
            if (pbSchedulesState != null) pbSchedulesState.setVisibility(View.GONE);
            if (layoutSchedulesEmptyState != null) layoutSchedulesEmptyState.setVisibility(View.GONE);
            if (rvSchedules != null) rvSchedules.setVisibility(View.GONE);
            return;
        }
        if (scheduleLoading) {
            if (pbSchedulesState != null) pbSchedulesState.setVisibility(View.VISIBLE);
            if (layoutSchedulesEmptyState != null) layoutSchedulesEmptyState.setVisibility(View.GONE);
            if (rvSchedules != null) rvSchedules.setVisibility(View.GONE);
            return;
        }
        if (pbSchedulesState != null) pbSchedulesState.setVisibility(View.GONE);

        if (scheduleLastRequestFailed) {
            if (rvSchedules != null) rvSchedules.setVisibility(View.GONE);
            if (layoutSchedulesEmptyState != null) layoutSchedulesEmptyState.setVisibility(View.VISIBLE);
            if (tvSchedulesEmptyTitle != null) tvSchedulesEmptyTitle.setText("Unable to load schedules");
            if (tvSchedulesEmptySubtitle != null) {
                tvSchedulesEmptySubtitle.setText("Could not fetch class schedules from the server. Check your connection and retry.");
            }
            if (btnSchedulesEmptyAction != null) {
                btnSchedulesEmptyAction.setText("Retry");
                btnSchedulesEmptyAction.setOnClickListener(v -> loadScheduleData());
            }
            return;
        }

        int visibleCount = scheduleAdapter != null ? scheduleAdapter.getItemCount() : scheduleList.size();
        if (visibleCount == 0) {
            if (rvSchedules != null) rvSchedules.setVisibility(View.GONE);
            if (layoutSchedulesEmptyState != null) layoutSchedulesEmptyState.setVisibility(View.VISIBLE);

            ClassModel selected = findSelectedScheduleClassModel();
            if (tvSchedulesEmptyTitle != null) {
                tvSchedulesEmptyTitle.setText(selected != null
                        ? getString(R.string.schedules_empty_for_class)
                        : getString(R.string.schedules_empty_all));
            }
            if (tvSchedulesEmptySubtitle != null) {
                if (selected != null) {
                    tvSchedulesEmptySubtitle.setText("Tap Create Schedule to add the first timetable slot for " + selected.getName() + ".");
                } else {
                    tvSchedulesEmptySubtitle.setText("Select a class above or tap Create Schedule to add a new timetable slot.");
                }
            }
            if (btnSchedulesEmptyAction != null) {
                btnSchedulesEmptyAction.setText(R.string.create_schedule_cta);
                btnSchedulesEmptyAction.setOnClickListener(v -> showAddScheduleDialog());
            }
        } else {
            if (layoutSchedulesEmptyState != null) layoutSchedulesEmptyState.setVisibility(View.GONE);
            if (rvSchedules != null) rvSchedules.setVisibility(View.VISIBLE);
        }
    }

    private void showScheduleDetailDialog(ClassScheduleAdapter.ScheduleItem item) {
        if (!isAdded() || getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Class Schedule Detail")
                .setMessage("Class: " + item.classCode + "\n" +
                        "Course: " + item.courseName + "\n" +
                        "Teacher: " + item.teacherName + "\n" +
                        "Day & Time: " + item.dayAndTime + "\n" +
                        "Location: " + item.locationLabel + "\n" +
                        "Status: " + item.status)
                .setNegativeButton("Edit", (dialog, which) -> showEditScheduleDialog(item))
                .setNeutralButton("Delete", (dialog, which) -> confirmDeleteSchedule(item))
                .setPositiveButton("Close", null)
                .show();
    }

    private void showEditScheduleDialog(ClassScheduleAdapter.ScheduleItem item) {
        if (!isAdded() || getContext() == null) return;
        if (item.rawJson == null || item.rawJson.isEmpty()) {
            Toast.makeText(getContext(), "Schedule payload unavailable for editing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), AddClassScheduleActivity.class);
        intent.putExtra(AddClassScheduleActivity.EXTRA_EDIT_SCHEDULE_JSON, item.rawJson);
        addScheduleLauncher.launch(intent);
    }

    private void confirmDeleteSchedule(ClassScheduleAdapter.ScheduleItem item) {
        if (!isAdded() || getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_schedule_title)
                .setMessage(getString(R.string.confirm_delete_schedule_msg) + "\n\n"
                        + item.classCode + " • " + item.courseName + "\n" + item.dayAndTime)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteSchedule(item))
                .show();
    }

    private void deleteSchedule(ClassScheduleAdapter.ScheduleItem item) {
        if (item.scheduleId == null || item.scheduleId.isEmpty() || !isAdded()) return;
        ApiClient.getApiService(requireContext()).deleteClassSchedule(item.scheduleId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Schedule deleted.", Toast.LENGTH_SHORT).show();
                    loadScheduleData();
                } else {
                    Toast.makeText(requireContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddScheduleDialog() {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(getContext(), AddClassScheduleActivity.class);
        ClassModel selected = findSelectedScheduleClassModel();
        if (selected != null) {
            int cId = parseIntSafe(selected.getId());
            if (cId > 0) {
                intent.putExtra(AddClassScheduleActivity.EXTRA_PREFILL_CLASS_ID, cId);
                intent.putExtra(AddClassScheduleActivity.EXTRA_PREFILL_CLASS_TITLE, formatClassOptionLabel(selected));
                intent.putExtra(AddClassScheduleActivity.EXTRA_PREFILL_CLASS_LOCATION, selected.getLocationLabel());
            }
        }
        addScheduleLauncher.launch(intent);
    }

    // ==========================================
    // 2. BUILDING MANAGEMENT TAB
    // ==========================================
    private void bindBuildingsViews(View root) {
        rvBuildings = root.findViewById(R.id.rvBuildings);
        etSearchBuilding = root.findViewById(R.id.etSearchBuilding);
        btnAddBuilding = root.findViewById(R.id.btnAddBuilding);
        tvTotalBuildings = root.findViewById(R.id.tvTotalBuildings);
        tvTotalFloors = root.findViewById(R.id.tvTotalFloors);
        tvAssignedClasses = root.findViewById(R.id.tvAssignedClasses);

        if (etSearchBuilding != null) {
            etSearchBuilding.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (buildingAdapter != null) buildingAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddBuilding != null) {
            btnAddBuilding.setOnClickListener(v -> {
                if (!isAdded() || getContext() == null) return;
                addBuildingLauncher.launch(new Intent(getContext(), AddBuildingActivity.class));
            });
        }
    }

    private void loadBuildingData() {
        if (!isAdded() || getContext() == null) return;

        if (rvBuildings != null && buildingAdapter == null) {
            buildingAdapter = new BuildingAdapter(getContext(), buildingList, this::showBuildingManagerDialog);
            rvBuildings.setLayoutManager(new LinearLayoutManager(getContext()));
            rvBuildings.setAdapter(buildingAdapter);
        }

        ApiClient.getApiService(getContext()).getBuildings().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                buildingList.clear();
                int totalFloorsSum = 0;
                int totalClassesSum = 0;

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject b = el.getAsJsonObject();
                        String id = getSafeString(b, "id", "");
                        String code = getSafeString(b, "code", "BLDG");
                        String name = getSafeString(b, "name", "Building");
                        String status = getSafeString(b, "status", "active");
                        int fCount = getSafeInt(b, "floors_count", 0);
                        int cCount = getSafeInt(b, "classes_count", 0);
                        totalFloorsSum += fCount;
                        totalClassesSum += cCount;

                        StringBuilder summary = new StringBuilder();
                        if (b.has("floors") && b.get("floors").isJsonArray()) {
                            JsonArray floors = b.getAsJsonArray("floors");
                            for (JsonElement fe : floors) {
                                if (!fe.isJsonObject()) continue;
                                JsonObject fo = fe.getAsJsonObject();
                                String fName = getSafeString(fo, "name", "Floor");
                                int fcCount = getSafeInt(fo, "classes_count", 0);
                                if (summary.length() > 0) summary.append(" • ");
                                summary.append(fName).append(" (").append(fcCount).append(")");
                            }
                        }

                        buildingList.add(new BuildingAdapter.BuildingItem(
                                id, code, name, fCount, cCount, summary.toString(), status, b.toString()));
                    }
                }

                if (tvTotalBuildings != null) tvTotalBuildings.setText(String.valueOf(buildingList.size()));
                if (tvTotalFloors != null) tvTotalFloors.setText(String.valueOf(totalFloorsSum));
                if (tvAssignedClasses != null) tvAssignedClasses.setText(String.valueOf(totalClassesSum));
                if (buildingAdapter != null) buildingAdapter.filter("");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBuildingManagerDialog(BuildingAdapter.BuildingItem item) {
        if (!isAdded() || getContext() == null) return;

        StringBuilder msg = new StringBuilder();
        msg.append("Code: ").append(item.code != null && !item.code.isEmpty() ? item.code : "N/A").append("\n");
        msg.append("Floors: ").append(item.floorsCount).append("\n");
        msg.append("Assigned Classes: ").append(item.classesCount).append("\n");
        if (item.floorsSummary != null && !item.floorsSummary.isEmpty()) {
            msg.append("\nHierarchy:\n").append(item.floorsSummary.replace(" • ", "\n• "));
        }
        msg.append("\n\nTip: Assign Classes (e.g. Class A, B, C) to specific Floors via Class Directory -> Tap Class -> Edit / Assign Floor.");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.name + " — Building Management")
                .setMessage(msg.toString())
                .setNeutralButton("Add Floor", (d, w) -> showAddFloorDialog(item))
                .setNegativeButton("Edit Building", (d, w) -> {
                    Intent i = new Intent(requireContext(), AddBuildingActivity.class);
                    i.putExtra(AddBuildingActivity.EXTRA_BUILDING_JSON, item.rawJson);
                    addBuildingLauncher.launch(i);
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAddFloorDialog(BuildingAdapter.BuildingItem buildingItem) {
        if (!isAdded() || getContext() == null) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 20, 48, 8);

        TextInputEditText etFloorNum = createInputDialog(layout, "Floor Number * (e.g. 1, 2, 3...)");
        etFloorNum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etFloorNum.setText(String.valueOf(buildingItem.floorsCount + 1));
        TextInputEditText etFloorName = createInputDialog(layout, "Floor Name (e.g. Floor " + (buildingItem.floorsCount + 1) + ")");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Floor to " + buildingItem.name)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Save Floor", (d, w) -> {
                    String numStr = etFloorNum.getText() != null ? etFloorNum.getText().toString().trim() : "";
                    String fName = etFloorName.getText() != null ? etFloorName.getText().toString().trim() : "";
                    if (numStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Floor number is required.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int floorNum = Integer.parseInt(numStr);
                    JsonObject body = new JsonObject();
                    body.addProperty("building_id", Integer.parseInt(buildingItem.buildingId));
                    body.addProperty("floor_number", floorNum);
                    if (!fName.isEmpty()) body.addProperty("name", fName);

                    ApiClient.getApiService(requireContext()).createFloor(body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Floor added!", Toast.LENGTH_SHORT).show();
                                loadBuildingData();
                            } else {
                                Toast.makeText(requireContext(), "Floor already exists or invalid input.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    // ==========================================
    // 3. TEACHER ASSIGNMENTS TAB
    // ==========================================
    private void bindTeacherAssignmentsViews(View root) {
        rvTeacherAssignments = root.findViewById(R.id.rvTeacherAssignments);
        etSearchTeacherAssign = root.findViewById(R.id.etSearchTeacherAssign);
        btnAssignTeacher = root.findViewById(R.id.btnAssignTeacher);
        tvTotalAssignedTeachers = root.findViewById(R.id.tvTotalAssignedTeachers);
        tvStaffedClasses = root.findViewById(R.id.tvStaffedClasses);
        tvUnassignedClasses = root.findViewById(R.id.tvUnassignedClasses);

        if (etSearchTeacherAssign != null) {
            etSearchTeacherAssign.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (teacherAssignAdapter != null) teacherAssignAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAssignTeacher != null) {
            btnAssignTeacher.setOnClickListener(v -> showAssignTeacherDialog());
        }
    }

    private void loadTeacherAssignData() {
        if (!isAdded() || getContext() == null) return;

        if (rvTeacherAssignments != null && teacherAssignAdapter == null) {
            teacherAssignAdapter = new ClassTeacherAssignmentAdapter(getContext(), teacherAssignList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Teacher Assignment Detail")
                        .setMessage("Class: " + item.classCode + "\n" +
                                "Course: " + item.courseName + "\n" +
                                "Assigned Teacher: " + item.teacherName + "\n" +
                                "Faculty & Term: " + item.departmentTerm + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton("Close", null)
                        .show();
            });
            rvTeacherAssignments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeacherAssignments.setAdapter(teacherAssignAdapter);
        }

        ApiClient.getApiService(getContext()).getAdminAcademicStructure().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                teacherAssignList.clear();
                int totalClassesCount = 0;
                Set<String> staffedClassCodes = new HashSet<>();
                Set<String> assignedTeachers = new HashSet<>();

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data.has("classes") && data.get("classes").isJsonArray()) {
                        totalClassesCount = data.getAsJsonArray("classes").size();
                    }
                    JsonArray arr = data.has("teacher_assignments") && data.get("teacher_assignments").isJsonArray()
                            ? data.getAsJsonArray("teacher_assignments") : new JsonArray();
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        String cls = getSafeString(o, "class_name", getSafeString(o, "class_code", "Class"));
                        String clsCode = getSafeString(o, "class_code", cls);
                        staffedClassCodes.add(clsCode);
                        String courseCode = getSafeString(o, "course_code", "");
                        String courseName = getSafeString(o, "course_name", "Course");
                        String courseDisplay = courseCode.isEmpty() ? courseName : (courseCode + " – " + courseName);
                        String tCode = getSafeString(o, "teacher_code", "");
                        String tName = getSafeString(o, "teacher_name", "Teacher");
                        assignedTeachers.add(tCode.isEmpty() ? tName : tCode);
                        String teacherDisplay = tCode.isEmpty() ? tName : (tName + " (" + tCode + ")");
                        int enr = getSafeInt(o, "enrolled_count", 0);
                        String deptTerm = getSafeString(o, "department_term", "") + " • " + enr + " enrolled";
                        String status = getSafeString(o, "status", "Assigned");
                        teacherAssignList.add(new ClassTeacherAssignmentAdapter.TeacherAssignItem(cls, courseDisplay, teacherDisplay, deptTerm, status));
                    }
                }

                if (tvTotalAssignedTeachers != null) tvTotalAssignedTeachers.setText(String.valueOf(assignedTeachers.size()));
                if (tvStaffedClasses != null) tvStaffedClasses.setText(staffedClassCodes.size() + " / " + totalClassesCount);
                if (tvUnassignedClasses != null) tvUnassignedClasses.setText(String.valueOf(Math.max(0, totalClassesCount - staffedClassCodes.size())));
                if (teacherAssignAdapter != null) teacherAssignAdapter.filter("");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAssignTeacherDialog() {
        if (!isAdded() || getContext() == null) return;
        StringBuilder keys = new StringBuilder();
        for (ClassTeacherAssignmentAdapter.TeacherAssignItem item : teacherAssignList) {
            keys.append(item.classCode).append("|").append(item.teacherName).append("\n");
        }
        Intent intent = new Intent(getContext(), AssignTeacherClassActivity.class);
        intent.putExtra(AssignTeacherClassActivity.EXTRA_EXISTING_KEYS, keys.toString());
        assignTeacherLauncher.launch(intent);
    }

    // ==========================================
    // 4. CLASS ENROLLMENT TAB
    // ==========================================
    private void bindEnrollmentsViews(View root) {
        rvEnrollments = root.findViewById(R.id.rvEnrollments);
        etSearchEnrollment = root.findViewById(R.id.etSearchEnrollment);
        btnEnrollStudent = root.findViewById(R.id.btnEnrollStudent);
        tvTotalEnrolled = root.findViewById(R.id.tvTotalEnrolled);
        tvCapacityRate = root.findViewById(R.id.tvCapacityRate);
        tvFullCohorts = root.findViewById(R.id.tvFullCohorts);

        if (etSearchEnrollment != null) {
            etSearchEnrollment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (enrollmentAdapter != null) enrollmentAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnEnrollStudent != null) {
            btnEnrollStudent.setOnClickListener(v -> showEnrollStudentDialog());
        }
    }

    private void loadEnrollmentData() {
        if (!isAdded() || getContext() == null) return;

        if (rvEnrollments != null && enrollmentAdapter == null) {
            enrollmentAdapter = new ClassEnrollmentAdapter(getContext(), enrollmentList, item -> {
                if (!isAdded() || getContext() == null) return;
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Class Enrollment Detail")
                        .setMessage("Student ID: " + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Enrolled Class: " + item.className + "\n" +
                                "Enrollment Date: " + item.enrollmentDate + "\n" +
                                "Status: " + item.status)
                        .setPositiveButton("Close", null)
                        .show();
            });
            rvEnrollments.setLayoutManager(new LinearLayoutManager(getContext()));
            rvEnrollments.setAdapter(enrollmentAdapter);
        }

        ApiClient.getApiService(getContext()).getClassEnrollments(null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                enrollmentList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        String stuId = getSafeString(o, "student_id", "");
                        String stuName = getSafeString(o, "student_name", "");
                        String clsCode = getSafeString(o, "class_name", getSafeString(o, "class_code", ""));
                        String date = getSafeString(o, "enrollment_date", "");
                        String status = getSafeString(o, "status", "Enrolled");
                        enrollmentList.add(new ClassEnrollmentAdapter.EnrollmentItem(stuId, stuName, clsCode, date, status));
                    }
                }

                if (tvTotalEnrolled != null) tvTotalEnrolled.setText(String.valueOf(enrollmentList.size()));
                int totalCap = 0;
                int fullCount = 0;
                for (ClassModel c : classList) {
                    int cap = c.getCapacity() > 0 ? c.getCapacity() : 40;
                    totalCap += cap;
                    if (c.getStudentCount() >= cap) fullCount++;
                }
                double rate = totalCap > 0 ? Math.min(100.0, (enrollmentList.size() * 100.0) / totalCap) : 0.0;
                if (tvCapacityRate != null) tvCapacityRate.setText(String.format(Locale.US, "%.1f%%", rate));
                if (tvFullCohorts != null) tvFullCohorts.setText(String.valueOf(fullCount));
                if (enrollmentAdapter != null) enrollmentAdapter.filter("");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEnrollStudentDialog() {
        if (!isAdded() || getContext() == null) return;
        bulkEnrollLauncher.launch(new Intent(getContext(), BulkClassEnrollmentActivity.class));
    }

    private TextInputEditText createInputDialog(LinearLayout container, String hint) {
        com.google.android.material.textfield.TextInputLayout til = new com.google.android.material.textfield.TextInputLayout(requireContext());
        til.setHint(hint);
        til.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 12);
        til.setLayoutParams(params);

        TextInputEditText et = new TextInputEditText(til.getContext());
        til.addView(et);
        container.addView(til);
        return et;
    }

    private void setupPagination(View root) {
        View btnPrev = root.findViewById(R.id.btnPrevPage);
        View btnNext = root.findViewById(R.id.btnNextPage);
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> loadClassData());
        }
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> loadClassData());
        }
    }

    private String formatDisplayTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return "";
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat(timeStr.length() > 5 ? "HH:mm:ss" : "HH:mm", Locale.US);
            Date d = inFmt.parse(timeStr);
            if (d != null) {
                return new SimpleDateFormat("hh:mm a", Locale.US).format(d);
            }
        } catch (Exception ignored) {
        }
        return timeStr;
    }

    private double computeDurationHours(String startStr, String endStr) {
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat(startStr.length() > 5 ? "HH:mm:ss" : "HH:mm", Locale.US);
            Date d1 = inFmt.parse(startStr);
            Date d2 = inFmt.parse(endStr);
            if (d1 != null && d2 != null && d2.getTime() > d1.getTime()) {
                return (d2.getTime() - d1.getTime()) / 3600000.0;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private String getSafeString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            JsonElement elem = obj.get(key);
            if (elem.isJsonPrimitive()) {
                return elem.getAsString();
            } else if (elem.isJsonObject()) {
                JsonObject child = elem.getAsJsonObject();
                if (child.has("name") && !child.get("name").isJsonNull()) {
                    return child.get("name").getAsString();
                } else if (child.has("title") && !child.get("title").isJsonNull()) {
                    return child.get("title").getAsString();
                } else if (child.has("course_name") && !child.get("course_name").isJsonNull()) {
                    return child.get("course_name").getAsString();
                } else if (child.has("department_name") && !child.get("department_name").isJsonNull()) {
                    return child.get("department_name").getAsString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing string field " + key, e);
        }
        return defaultValue;
    }

    private int getSafeInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            JsonElement elem = obj.get(key);
            if (elem.isJsonPrimitive()) {
                return elem.getAsInt();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing int field " + key, e);
        }
        return defaultValue;
    }
}
