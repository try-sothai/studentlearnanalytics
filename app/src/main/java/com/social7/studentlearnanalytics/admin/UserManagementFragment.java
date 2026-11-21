package com.social7.studentlearnanalytics.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.FacultyEvaluationAdapter;
import com.social7.studentlearnanalytics.admin.teachers.AddTeacherActivity;
import com.social7.studentlearnanalytics.admin.teachers.AdminTeacherDetailActivity;
import com.social7.studentlearnanalytics.admin.teachers.EditTeacherActivity;
import com.social7.studentlearnanalytics.admin.teachers.TeacherAdapter;
import com.social7.studentlearnanalytics.admin.users.AddAdminActivity;
import com.social7.studentlearnanalytics.admin.users.AddStudentActivity;
import com.social7.studentlearnanalytics.admin.users.AdminAdapter;
import com.social7.studentlearnanalytics.admin.users.AdminDetailActivity;
import com.social7.studentlearnanalytics.admin.users.AdminStudentDetailActivity;
import com.social7.studentlearnanalytics.admin.users.EditAdminActivity;
import com.social7.studentlearnanalytics.admin.users.EditStudentActivity;
import com.social7.studentlearnanalytics.admin.users.UserAdapter;
import com.social7.studentlearnanalytics.model.Admin;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.Teacher;
import com.social7.studentlearnanalytics.model.User;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementFragment extends Fragment implements
        UserAdapter.OnUserActionListener,
        AdminAdapter.OnAdminActionListener,
        TeacherAdapter.OnTeacherActionListener {

    private static final String TAG = "UserManagement";

    // Primary Tabs
    private MaterialButton btnTabStudents, btnTabTeachers, btnTabAdmins, btnTabFacultyEval;

    // Containers & AppBar
    private com.google.android.material.appbar.AppBarLayout appBarUsers;
    private View layoutStudentsContainer, layoutTeachersContainer, layoutAdminsContainer, layoutFacultyEvalContainer;

    // Students Tab Views
    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private final List<User> masterUserList = new ArrayList<>();
    private EditText etSearchUser;
    private TextView pillFilterAll, pillFilterActive, pillFilterInactive;
    private MaterialButton btnAddUser;

    // Teachers Tab Views
    private RecyclerView rvTeachers;
    private TeacherAdapter teacherAdapter;
    private final List<Teacher> masterTeacherList = new ArrayList<>();
    private EditText etSearchTeacher;
    private TextView pillFilterAllTeachers, pillFilterActiveTeachers, pillFilterInactiveTeachers;
    private Spinner spDepartmentFilter;
    private MaterialButton btnAddTeacher, btnImportTeachers;
    private String currentTeacherDepartmentFilter = "All Departments";
    private final ActivityResultLauncher<String[]> teacherImportPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uploadTeacherImportUri(uri);
                }
            });

    // Admins Tab Views
    private RecyclerView rvAdmins;
    private AdminAdapter adminAdapter;
    private final List<Admin> masterAdminList = new ArrayList<>();
    private EditText etSearchAdmin;
    private TextView pillFilterAllAdmins, pillFilterActiveAdmins, pillFilterInactiveAdmins;
    private MaterialButton btnAddAdmin;

    // Faculty Evaluation Tab Views
    private RecyclerView rvFacultyEvaluation;
    private FacultyEvaluationAdapter facultyAdapter;
    private final List<FacultyEvaluationAdapter.FacultyItem> facultyList = new ArrayList<>();
    private EditText etSearchFaculty;

    // Pagination Views
    private View btnPrevPage, btnNextPage;
    private TextView btnPage1, btnPage2, btnPage3, btnPage4, btnPage5;

    // Current State
    private int selectedTab = 0; // 0=Students, 1=Teachers, 2=Admins, 3=Faculty Evaluation
    private String currentStudentStatusFilter = "ALL";
    private String currentTeacherStatusFilter = "ALL";
    private String currentAdminStatusFilter = "ALL";

    private int currentPage = 1;
    private int lastPage = 1;
    private static final int PER_PAGE = 10;
    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pendingSearchRunnable;

    public UserManagementFragment() {
    }

    public static UserManagementFragment newInstance(int initialTab) {
        UserManagementFragment fragment = new UserManagementFragment();
        Bundle args = new Bundle();
        args.putInt("tab_index", initialTab);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        if (getArguments() != null && getArguments().containsKey("tab_index")) {
            selectedTab = getArguments().getInt("tab_index", 0);
        }

        // Header Menu Icon
        View btnMenu = view.findViewById(R.id.btnUsersMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        // Header Notification Bell
        View imgNotifBell = view.findViewById(R.id.imgNotifBell);
        if (imgNotifBell != null) {
            imgNotifBell.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new NotificationFragment());
                }
            });
        }

        // Header Titles
        TextView tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        TextView tvHeaderSubtitle = view.findViewById(R.id.tvHeaderSubtitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.user_management);
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText(R.string.user_management_subtitle);

        // Primary Tabs
        btnTabStudents = view.findViewById(R.id.btnTabStudents);
        btnTabTeachers = view.findViewById(R.id.btnTabTeachers);
        btnTabAdmins = view.findViewById(R.id.btnTabAdmins);
        btnTabFacultyEval = view.findViewById(R.id.btnTabFacultyEval);

        // Collapsible Containers + AppBar
        appBarUsers = view.findViewById(R.id.appBarUsers);
        layoutStudentsContainer = view.findViewById(R.id.layoutStudentsControls);
        layoutTeachersContainer = view.findViewById(R.id.layoutTeachersControls);
        layoutAdminsContainer = view.findViewById(R.id.layoutAdminsControls);
        layoutFacultyEvalContainer = view.findViewById(R.id.layoutFacultyEvalControls);

        // Bind Students Views
        bindStudentsViews(view);

        // Bind Teachers Views
        bindTeachersViews(view);

        // Bind Admins Views
        bindAdminsViews(view);

        // Bind Faculty Evaluation Views
        bindFacultyEvalViews(view);

        // Bind Pagination Controls
        bindPaginationViews(view);

        setupPrimaryTabs();
        selectUserTab(selectedTab);

        fetchUserStatistics();

        return view;
    }

    private void setupPrimaryTabs() {
        if (btnTabStudents != null) {
            btnTabStudents.setOnClickListener(v -> selectUserTab(0));
        }
        if (btnTabTeachers != null) {
            btnTabTeachers.setOnClickListener(v -> selectUserTab(1));
        }
        if (btnTabAdmins != null) {
            btnTabAdmins.setOnClickListener(v -> selectUserTab(2));
        }
        if (btnTabFacultyEval != null) {
            btnTabFacultyEval.setOnClickListener(v -> selectUserTab(3));
        }
    }

    public void selectUserTab(int tabIndex) {
        this.selectedTab = tabIndex;
        this.currentPage = 1;
        updatePrimaryTabStyles(tabIndex);

        if (appBarUsers != null) appBarUsers.setExpanded(true, false);

        if (layoutStudentsContainer != null) layoutStudentsContainer.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutTeachersContainer != null) layoutTeachersContainer.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutAdminsContainer != null) layoutAdminsContainer.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutFacultyEvalContainer != null) layoutFacultyEvalContainer.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (rvUsers != null) rvUsers.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (rvTeachers != null) rvTeachers.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (rvAdmins != null) rvAdmins.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (rvFacultyEvaluation != null) rvFacultyEvaluation.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        MaterialButton[] tabs = new MaterialButton[]{btnTabStudents, btnTabTeachers, btnTabAdmins, btnTabFacultyEval};
        if (tabIndex >= 0 && tabIndex < tabs.length) {
            com.social7.studentlearnanalytics.utils.UiUtils.scrollToTab(tabs[tabIndex]);
        }

        if (tabIndex == 0) {
            loadStudentList();
        } else if (tabIndex == 1) {
            loadTeacherList();
        } else if (tabIndex == 2) {
            loadAdminList();
        } else if (tabIndex == 3) {
            loadFacultyEvaluationData();
        }
    }

    private void updatePrimaryTabStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        MaterialButton[] tabs = new MaterialButton[]{
                btnTabStudents, btnTabTeachers, btnTabAdmins, btnTabFacultyEval
        };

        int activeColor = Color.parseColor("#155DFC");
        int inactiveBg = Color.parseColor("#F4F7FF");
        int textSecondary = Color.parseColor("#64748B");

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

    private void fetchUserStatistics() {
        if (!isAdded() || getContext() == null) return;

        ApiClient.getApiService(requireContext()).getUserStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        int totalStudents = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "total_students", 0);
                        int activeStudents = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "active_students", 0);
                        int inactiveStudents = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "inactive_students", 0);

                        if (pillFilterAll != null) pillFilterAll.setText(pillFilterAll.getContext().getString(R.string.all_students) + totalStudents + ")");
                        if (pillFilterActive != null) pillFilterActive.setText(pillFilterActive.getContext().getString(R.string.active) + activeStudents + ")");
                        if (pillFilterInactive != null) pillFilterInactive.setText(pillFilterInactive.getContext().getString(R.string.inactive) + inactiveStudents + ")");

                        int totalTeachers = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "total_teachers", 0);
                        int activeTeachers = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "active_teachers", 0);
                        int inactiveTeachers = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "inactive_teachers", 0);

                        if (pillFilterAllTeachers != null) pillFilterAllTeachers.setText(pillFilterAllTeachers.getContext().getString(R.string.all_teachers) + totalTeachers + ")");
                        if (pillFilterActiveTeachers != null) pillFilterActiveTeachers.setText(pillFilterActiveTeachers.getContext().getString(R.string.active) + activeTeachers + ")");
                        if (pillFilterInactiveTeachers != null) pillFilterInactiveTeachers.setText(pillFilterInactiveTeachers.getContext().getString(R.string.inactive) + inactiveTeachers + ")");

                        int totalAdmins = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "total_admins",
                                com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "total_administrators", 0));
                        int activeAdmins = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "active_admins", 0);
                        int inactiveAdmins = com.social7.studentlearnanalytics.utils.UiUtils.optInt(data, "inactive_admins", 0);

                        if (pillFilterAllAdmins != null) pillFilterAllAdmins.setText(pillFilterAllAdmins.getContext().getString(R.string.all_admins) + totalAdmins + ")");
                        if (pillFilterActiveAdmins != null) pillFilterActiveAdmins.setText(pillFilterActiveAdmins.getContext().getString(R.string.active) + activeAdmins + ")");
                        if (pillFilterInactiveAdmins != null) pillFilterInactiveAdmins.setText(pillFilterInactiveAdmins.getContext().getString(R.string.inactive) + inactiveAdmins + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing statistics API response", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Statistics call failed: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // 1. STUDENTS TAB IMPLEMENTATION
    // ==========================================
    private void bindStudentsViews(View root) {
        rvUsers = root.findViewById(R.id.rvUsers);
        etSearchUser = root.findViewById(R.id.etSearchUser);
        btnAddUser = root.findViewById(R.id.btnAddUser);

        pillFilterAll = root.findViewById(R.id.pillFilterAll);
        pillFilterActive = root.findViewById(R.id.pillFilterActive);
        pillFilterInactive = root.findViewById(R.id.pillFilterInactive);

        if (etSearchUser != null) {
            etSearchUser.setHint(R.string.search_by_name_email_or_student_code);
            etSearchUser.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scheduleDebouncedSearch(() -> {
                        currentPage = 1;
                        loadStudentList();
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAll != null) {
            pillFilterAll.setOnClickListener(v -> {
                currentStudentStatusFilter = "ALL";
                updateStudentPillStyles();
                currentPage = 1;
                loadStudentList();
            });
        }

        if (pillFilterActive != null) {
            pillFilterActive.setOnClickListener(v -> {
                currentStudentStatusFilter = "ACTIVE";
                updateStudentPillStyles();
                currentPage = 1;
                loadStudentList();
            });
        }

        if (pillFilterInactive != null) {
            pillFilterInactive.setOnClickListener(v -> {
                currentStudentStatusFilter = "INACTIVE";
                updateStudentPillStyles();
                currentPage = 1;
                loadStudentList();
            });
        }

        if (btnAddUser != null) {
            btnAddUser.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddStudentActivity.class);
                startActivity(intent);
            });
        }

        View btnImportStudents = root.findViewById(R.id.btnImportStudents);
        if (btnImportStudents != null) {
            btnImportStudents.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.social7.studentlearnanalytics.admin.users.StudentImportActivity.class);
                startActivity(intent);
            });
        }

        View btnImportHistory = root.findViewById(R.id.btnImportHistory);
        if (btnImportHistory != null) {
            btnImportHistory.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.social7.studentlearnanalytics.admin.users.StudentImportHistoryActivity.class);
                startActivity(intent);
            });
        }

        View btnExportStudents = root.findViewById(R.id.btnExportStudents);
        if (btnExportStudents != null) {
            btnExportStudents.setOnClickListener(v -> {
                new com.social7.studentlearnanalytics.admin.users.StudentExportDialog(requireContext()).show();
            });
        }
    }

    private void updateStudentPillStyles() {
        if (!isAdded() || getContext() == null) return;

        if (pillFilterAll != null) {
            boolean isSelected = "ALL".equalsIgnoreCase(currentStudentStatusFilter);
            pillFilterAll.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAll.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#64748B"));
        }
        if (pillFilterActive != null) {
            boolean isSelected = "ACTIVE".equalsIgnoreCase(currentStudentStatusFilter);
            pillFilterActive.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterActive.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#16A34A"));
        }
        if (pillFilterInactive != null) {
            boolean isSelected = "INACTIVE".equalsIgnoreCase(currentStudentStatusFilter);
            pillFilterInactive.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterInactive.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#EF4444"));
        }
    }

    private void loadStudentList() {
        if (!isAdded() || getContext() == null) return;

        String querySearch = getSearchText(etSearchUser);
        String status = currentStudentStatusFilter.toLowerCase();

        ApiClient.getApiService(getContext()).getUsers("student", status, querySearch, currentPage, PER_PAGE)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;
                        masterUserList.clear();

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JsonObject root = response.body();
                                parsePagingMeta(root);

                                JsonArray dataArray = root.has("data") && root.get("data").isJsonArray()
                                        ? root.getAsJsonArray("data") : null;

                                if (dataArray != null) {
                                    for (JsonElement el : dataArray) {
                                        if (!el.isJsonObject()) continue;
                                        JsonObject obj = el.getAsJsonObject();

                                        Long numId = obj.has("id") ? obj.get("id").getAsLong() : null;
                                        String khmerName = obj.has("khmer_name") && !obj.get("khmer_name").isJsonNull() ? obj.get("khmer_name").getAsString() : "";
                                        String englishName = obj.has("english_name") && !obj.get("english_name").isJsonNull() ? obj.get("english_name").getAsString() : "";
                                        String rawName = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Student";
                                        String name = com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, rawName);
                                        String email = obj.has("email") && !obj.get("email").isJsonNull() ? obj.get("email").getAsString() : "";
                                        String userStatus = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";
                                        
                                        String code = "STD" + numId;
                                        if (obj.has("code") && !obj.get("code").isJsonNull()) {
                                            code = obj.get("code").getAsString();
                                        } else if (obj.has("student_code") && !obj.get("student_code").isJsonNull()) {
                                            code = obj.get("student_code").getAsString();
                                        } else if (obj.has("student") && obj.get("student").isJsonObject()) {
                                            JsonObject sObj = obj.getAsJsonObject("student");
                                            if (sObj.has("student_code") && !sObj.get("student_code").isJsonNull()) {
                                                code = sObj.get("student_code").getAsString();
                                            } else if (sObj.has("student_id") && !sObj.get("student_id").isJsonNull()) {
                                                code = sObj.get("student_id").getAsString();
                                            }
                                        }

                                        User user = new User(numId, code, name, email, "Student", capitalize(userStatus), code);
                                        masterUserList.add(user);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing users API", e);
                            }
                        }

                        if (masterUserList.isEmpty() && currentPage == 1) {
                            loadFallbackStudents();
                        } else {
                            bindStudentList();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadFallbackStudents();
                    }
                });
    }

    private void parsePagingMeta(JsonObject root) {
        if (root.has("meta") && root.get("meta").isJsonObject()) {
            JsonObject meta = root.getAsJsonObject("meta");
            if (meta.has("current_page")) currentPage = meta.get("current_page").getAsInt();
            if (meta.has("last_page")) lastPage = meta.get("last_page").getAsInt();
        } else {
            lastPage = 1;
        }
        updatePaginationBar();
    }

    private void loadFallbackStudents() {
        if (!isAdded()) return;
        masterUserList.clear();
        lastPage = 1;
        updatePaginationBar();
        bindStudentList();
        if (getContext() != null) {
            Toast.makeText(getContext(), getString(R.string.empty_state_no_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindStudentList() {
        if (!isAdded() || getContext() == null) return;

        userAdapter = new UserAdapter(getContext(), masterUserList, this);
        if (rvUsers != null) {
            rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
            rvUsers.setAdapter(userAdapter);
        }
    }

    // ==========================================
    // 2. TEACHERS TAB IMPLEMENTATION
    // ==========================================
    private void bindTeachersViews(View root) {
        rvTeachers = root.findViewById(R.id.rvTeachers);
        etSearchTeacher = root.findViewById(R.id.etSearchTeacher);
        btnAddTeacher = root.findViewById(R.id.btnAddTeacher);
        spDepartmentFilter = root.findViewById(R.id.spDepartmentFilter);

        pillFilterAllTeachers = root.findViewById(R.id.pillFilterAllTeachers);
        pillFilterActiveTeachers = root.findViewById(R.id.pillFilterActiveTeachers);
        pillFilterInactiveTeachers = root.findViewById(R.id.pillFilterInactiveTeachers);

        if (etSearchTeacher != null) {
            etSearchTeacher.setHint(R.string.search_by_name_email_or_teacher_id);
            etSearchTeacher.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scheduleDebouncedSearch(() -> {
                        currentPage = 1;
                        loadTeacherList();
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAllTeachers != null) {
            pillFilterAllTeachers.setOnClickListener(v -> {
                currentTeacherStatusFilter = "ALL";
                updateTeacherPillStyles();
                currentPage = 1;
                loadTeacherList();
            });
        }

        if (pillFilterActiveTeachers != null) {
            pillFilterActiveTeachers.setOnClickListener(v -> {
                currentTeacherStatusFilter = "ACTIVE";
                updateTeacherPillStyles();
                currentPage = 1;
                loadTeacherList();
            });
        }

        if (pillFilterInactiveTeachers != null) {
            pillFilterInactiveTeachers.setOnClickListener(v -> {
                currentTeacherStatusFilter = "INACTIVE";
                updateTeacherPillStyles();
                currentPage = 1;
                loadTeacherList();
            });
        }

        setupDepartmentSpinner();

        if (btnAddTeacher != null) {
            btnAddTeacher.setText(R.string.add_teacher_1f95de);
            btnAddTeacher.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddTeacherActivity.class);
                startActivity(intent);
            });
        }

        btnImportTeachers = root.findViewById(R.id.btnImportTeachers);
        if (btnImportTeachers != null) {
            btnImportTeachers.setOnClickListener(v -> showTeacherImportOptionsDialog());
        }
    }

    private void showTeacherImportOptionsDialog() {
        if (!isAdded() || getContext() == null) return;
        String[] options = new String[]{
                getString(R.string.select_teacher_import_file),
                getString(R.string.download_teacher_template)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.import_teachers_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        teacherImportPicker.launch(new String[]{
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "*/*"
                        });
                    } else {
                        downloadTeacherImportTemplateFile();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void downloadTeacherImportTemplateFile() {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(requireContext()).downloadTeacherImportTemplate().enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    com.social7.studentlearnanalytics.admin.users.StudentExportDialog.saveExportFile(
                            getContext().getApplicationContext(),
                            response.body(),
                            "teacher_import_template.csv",
                            "text/csv"
                    );
                    Toast.makeText(getContext(), "Template saved: teacher_import_template.csv", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to download template (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error downloading template", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadTeacherImportUri(android.net.Uri uri) {
        if (!isAdded() || getContext() == null) return;
        try (java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            if (is == null) {
                Toast.makeText(getContext(), "Cannot open selected file", Toast.LENGTH_SHORT).show();
                return;
            }
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int len;
            while ((len = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, len);
            }
            byte[] bytes = buffer.toByteArray();
            String mimeType = requireContext().getContentResolver().getType(uri);
            String filename = "teachers_import.csv";
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIdx >= 0 && cursor.getString(nameIdx) != null) {
                        filename = cursor.getString(nameIdx);
                    }
                }
            }
            okhttp3.MediaType mt = okhttp3.MediaType.parse(mimeType != null ? mimeType : "text/csv");
            okhttp3.RequestBody reqBody = okhttp3.RequestBody.create(mt, bytes);
            okhttp3.MultipartBody.Part part = okhttp3.MultipartBody.Part.createFormData("file", filename, reqBody);

            Toast.makeText(getContext(), "Importing teachers...", Toast.LENGTH_SHORT).show();
            ApiClient.getApiService(requireContext()).importTeachers(part).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (!isAdded() || getContext() == null) return;
                    JsonObject body = response.body();
                    if (body == null && response.errorBody() != null) {
                        try {
                            body = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                        } catch (Exception ignored) {}
                    }
                    StringBuilder summary = new StringBuilder();
                    if (body != null && body.has("message") && !body.get("message").isJsonNull()) {
                        summary.append(body.get("message").getAsString()).append("\n\n");
                    }
                    if (body != null && body.has("data") && body.get("data").isJsonObject()) {
                        JsonObject data = body.getAsJsonObject("data");
                        if (data.has("created_teachers") && data.get("created_teachers").isJsonArray()) {
                            JsonArray created = data.getAsJsonArray("created_teachers");
                            if (created.size() > 0) {
                                summary.append("Created Accounts (Initial Password: TCH@12345):\n");
                                for (JsonElement el : created) {
                                    JsonObject item = el.getAsJsonObject();
                                    summary.append("• ").append(item.get("login_id").getAsString())
                                            .append(" — ").append(item.get("name").getAsString())
                                            .append(" (").append(item.get("account_status").getAsString()).append(")\n");
                                }
                                summary.append("\n");
                            }
                        }
                        if (data.has("errors") && data.get("errors").isJsonArray()) {
                            JsonArray errs = data.getAsJsonArray("errors");
                            if (errs.size() > 0) {
                                summary.append("Failed Rows:\n");
                                for (JsonElement el : errs) {
                                    JsonObject errObj = el.getAsJsonObject();
                                    summary.append("• Row ").append(errObj.get("row").getAsInt());
                                    if (errObj.has("teacher_id") && !errObj.get("teacher_id").isJsonNull()) {
                                        summary.append(" (").append(errObj.get("teacher_id").getAsString()).append(")");
                                    }
                                    summary.append(": ");
                                    if (errObj.has("errors") && errObj.get("errors").isJsonArray()) {
                                        for (JsonElement em : errObj.getAsJsonArray("errors")) {
                                            summary.append(em.getAsString()).append(" ");
                                        }
                                    }
                                    summary.append("\n");
                                }
                            }
                        }
                    }
                    if (summary.length() == 0) {
                        summary.append("Import completed (HTTP ").append(response.code()).append(").");
                    }
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.import_teachers_title)
                            .setMessage(summary.toString().trim())
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    fetchUserStatistics();
                    loadTeacherList();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Network error importing teachers: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to read file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDepartmentSpinner() {
        if (spDepartmentFilter == null || getContext() == null) return;

        String[] departments = {
                "All Departments",
                "Computer Science",
                "Information Technology",
                "Information Systems",
                "Data Structures",
                "Software Engineering",
                "Mobile Development",
                "Artificial Intelligence"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartmentFilter.setAdapter(adapter);

        spDepartmentFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTeacherDepartmentFilter = departments[position];
                currentPage = 1;
                loadTeacherList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateTeacherPillStyles() {
        if (!isAdded() || getContext() == null) return;

        if (pillFilterAllTeachers != null) {
            boolean isSelected = "ALL".equalsIgnoreCase(currentTeacherStatusFilter);
            pillFilterAllTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAllTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#64748B"));
        }
        if (pillFilterActiveTeachers != null) {
            boolean isSelected = "ACTIVE".equalsIgnoreCase(currentTeacherStatusFilter);
            pillFilterActiveTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterActiveTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#16A34A"));
        }
        if (pillFilterInactiveTeachers != null) {
            boolean isSelected = "INACTIVE".equalsIgnoreCase(currentTeacherStatusFilter);
            pillFilterInactiveTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterInactiveTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#EF4444"));
        }
    }

    private void loadTeacherList() {
        if (!isAdded() || getContext() == null) return;

        String querySearch = getSearchText(etSearchTeacher);
        String status = currentTeacherStatusFilter.toLowerCase();
        String deptParam = (currentTeacherDepartmentFilter != null && !"All Departments".equalsIgnoreCase(currentTeacherDepartmentFilter))
                ? currentTeacherDepartmentFilter : null;

        ApiClient.getApiService(getContext()).getTeachersPaged(status, deptParam, querySearch, currentPage, PER_PAGE)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;
                        masterTeacherList.clear();

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JsonObject root = response.body();
                                parsePagingMeta(root);

                                JsonArray dataArray = root.has("data") && root.get("data").isJsonArray()
                                        ? root.getAsJsonArray("data") : null;

                                if (dataArray != null) {
                                    for (JsonElement el : dataArray) {
                                        if (!el.isJsonObject()) continue;
                                        JsonObject obj = el.getAsJsonObject();

                                        String id = obj.has("teacher_code") && !obj.get("teacher_code").isJsonNull()
                                                ? obj.get("teacher_code").getAsString()
                                                : (obj.has("code") && !obj.get("code").isJsonNull() ? obj.get("code").getAsString() : "TCH");
                                        String khmerName = obj.has("khmer_name") && !obj.get("khmer_name").isJsonNull() ? obj.get("khmer_name").getAsString() : "";
                                        String englishName = obj.has("english_name") && !obj.get("english_name").isJsonNull() ? obj.get("english_name").getAsString() : "";
                                        String rawName = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Teacher";
                                        String name = com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, rawName);
                                        String email = obj.has("email") && !obj.get("email").isJsonNull() ? obj.get("email").getAsString() : "";
                                        String dept = obj.has("department_name") && !obj.get("department_name").isJsonNull() && !obj.get("department_name").getAsString().isEmpty()
                                                ? obj.get("department_name").getAsString() : "Computer Science";
                                        String pos = obj.has("position") && !obj.get("position").isJsonNull() && !obj.get("position").getAsString().isEmpty()
                                                ? obj.get("position").getAsString() : "Lecturer";
                                        String teacherStatus = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";

                                        Teacher tItem = new Teacher(id, name, email, dept, pos, capitalize(teacherStatus));
                                        tItem.setKhmerName(khmerName);
                                        tItem.setEnglishName(englishName);
                                        if (obj.has("hire_date") && !obj.get("hire_date").isJsonNull()) {
                                            tItem.setHireDate(obj.get("hire_date").getAsString());
                                        }
                                        if (obj.has("id") && !obj.get("id").isJsonNull()) {
                                            tItem.setDbId(obj.get("id").getAsLong());
                                        }
                                        if (obj.has("user_id") && !obj.get("user_id").isJsonNull()) {
                                            tItem.setUserId(obj.get("user_id").getAsLong());
                                        }
                                        if (obj.has("login_id") && !obj.get("login_id").isJsonNull()) {
                                            tItem.setLoginId(obj.get("login_id").getAsString());
                                        }
                                        if (obj.has("teacher_profile_status") && !obj.get("teacher_profile_status").isJsonNull()) {
                                            tItem.setProfileStatus(capitalize(obj.get("teacher_profile_status").getAsString()));
                                        }
                                        if (obj.has("login_account_status") && !obj.get("login_account_status").isJsonNull()) {
                                            tItem.setLoginAccountStatus(obj.get("login_account_status").getAsString());
                                        }
                                        if (obj.has("account_created") && !obj.get("account_created").isJsonNull()) {
                                            tItem.setAccountCreated(obj.get("account_created").getAsBoolean());
                                        }
                                        if (obj.has("account_linked") && !obj.get("account_linked").isJsonNull()) {
                                            tItem.setAccountLinked(obj.get("account_linked").getAsBoolean());
                                        }
                                        if (obj.has("can_login") && !obj.get("can_login").isJsonNull()) {
                                            tItem.setCanLogin(obj.get("can_login").getAsBoolean());
                                        }
                                        if (obj.has("phone") && !obj.get("phone").isJsonNull()) {
                                            tItem.setPhone(obj.get("phone").getAsString());
                                        }
                                        if (obj.has("date_of_birth") && !obj.get("date_of_birth").isJsonNull()) {
                                            tItem.setDob(obj.get("date_of_birth").getAsString());
                                        }
                                        if (obj.has("work_type") && !obj.get("work_type").isJsonNull()) {
                                            tItem.setWorkType(obj.get("work_type").getAsString());
                                        }
                                        if (obj.has("office_location_id") && !obj.get("office_location_id").isJsonNull()) {
                                            tItem.setOfficeLocationId(obj.get("office_location_id").getAsInt());
                                        }
                                        if (obj.has("office_location") && !obj.get("office_location").isJsonNull()) {
                                            tItem.setOfficeLocation(obj.get("office_location").getAsString());
                                        }
                                        if (obj.has("biography") && !obj.get("biography").isJsonNull()) {
                                            tItem.setBio(obj.get("biography").getAsString());
                                        } else if (obj.has("bio") && !obj.get("bio").isJsonNull()) {
                                            tItem.setBio(obj.get("bio").getAsString());
                                        }
                                        if (obj.has("specializations") && obj.get("specializations").isJsonArray()) {
                                            java.util.List<String> specs = new java.util.ArrayList<>();
                                            for (JsonElement se : obj.getAsJsonArray("specializations")) {
                                                if (!se.isJsonNull()) specs.add(se.getAsString());
                                            }
                                            tItem.setSpecializations(specs);
                                        } else if (obj.has("specialization") && !obj.get("specialization").isJsonNull()) {
                                            tItem.setSpecialization(obj.get("specialization").getAsString());
                                        }
                                        masterTeacherList.add(tItem);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing teachers API", e);
                            }
                        }

                        if (masterTeacherList.isEmpty() && currentPage == 1) {
                            loadFallbackTeachers();
                        } else {
                            bindTeacherList();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadFallbackTeachers();
                    }
                });
    }

    private void loadFallbackTeachers() {
        if (!isAdded()) return;
        masterTeacherList.clear();
        lastPage = 1;
        updatePaginationBar();
        bindTeacherList();
        if (getContext() != null) {
            Toast.makeText(getContext(), getString(R.string.empty_state_no_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindTeacherList() {
        if (!isAdded() || getContext() == null) return;

        teacherAdapter = new TeacherAdapter(getContext(), masterTeacherList, this);
        if (rvTeachers != null) {
            rvTeachers.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeachers.setAdapter(teacherAdapter);
        }
    }

    // ==========================================
    // 3. ADMINS TAB IMPLEMENTATION
    // ==========================================
    private void bindAdminsViews(View root) {
        rvAdmins = root.findViewById(R.id.rvAdmins);
        etSearchAdmin = root.findViewById(R.id.etSearchAdmin);
        btnAddAdmin = root.findViewById(R.id.btnAddAdmin);

        pillFilterAllAdmins = root.findViewById(R.id.pillFilterAllAdmins);
        pillFilterActiveAdmins = root.findViewById(R.id.pillFilterActiveAdmins);
        pillFilterInactiveAdmins = root.findViewById(R.id.pillFilterInactiveAdmins);

        if (etSearchAdmin != null) {
            etSearchAdmin.setHint(R.string.search_admins_hint);
            etSearchAdmin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scheduleDebouncedSearch(() -> {
                        currentPage = 1;
                        loadAdminList();
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (pillFilterAllAdmins != null) {
            pillFilterAllAdmins.setOnClickListener(v -> {
                currentAdminStatusFilter = "ALL";
                updateAdminPillStyles();
                currentPage = 1;
                loadAdminList();
            });
        }

        if (pillFilterActiveAdmins != null) {
            pillFilterActiveAdmins.setOnClickListener(v -> {
                currentAdminStatusFilter = "ACTIVE";
                updateAdminPillStyles();
                currentPage = 1;
                loadAdminList();
            });
        }

        if (pillFilterInactiveAdmins != null) {
            pillFilterInactiveAdmins.setOnClickListener(v -> {
                currentAdminStatusFilter = "INACTIVE";
                updateAdminPillStyles();
                currentPage = 1;
                loadAdminList();
            });
        }

        if (btnAddAdmin != null) {
            btnAddAdmin.setText(R.string.add_admin_2f7840);
            btnAddAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddAdminActivity.class);
                startActivity(intent);
            });
        }
    }

    private void updateAdminPillStyles() {
        if (!isAdded() || getContext() == null) return;

        if (pillFilterAllAdmins != null) {
            boolean isSelected = "ALL".equalsIgnoreCase(currentAdminStatusFilter);
            pillFilterAllAdmins.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAllAdmins.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#64748B"));
        }
        if (pillFilterActiveAdmins != null) {
            boolean isSelected = "ACTIVE".equalsIgnoreCase(currentAdminStatusFilter);
            pillFilterActiveAdmins.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterActiveAdmins.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#16A34A"));
        }
        if (pillFilterInactiveAdmins != null) {
            boolean isSelected = "INACTIVE".equalsIgnoreCase(currentAdminStatusFilter);
            pillFilterInactiveAdmins.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterInactiveAdmins.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#EF4444"));
        }
    }

    private void loadAdminList() {
        if (!isAdded() || getContext() == null) return;

        String querySearch = getSearchText(etSearchAdmin);
        String status = currentAdminStatusFilter.toLowerCase();

        ApiClient.getApiService(getContext()).getUsers("admin", status, querySearch, currentPage, PER_PAGE)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;
                        masterAdminList.clear();

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JsonObject root = response.body();
                                parsePagingMeta(root);

                                JsonArray dataArray = root.has("data") && root.get("data").isJsonArray()
                                        ? root.getAsJsonArray("data") : null;

                                if (dataArray != null) {
                                    for (JsonElement el : dataArray) {
                                        if (!el.isJsonObject()) continue;
                                        JsonObject obj = el.getAsJsonObject();

                                        String id = obj.has("code") && !obj.get("code").isJsonNull() ? obj.get("code").getAsString() : (obj.has("id") ? obj.get("id").getAsString() : "ADM");
                                        String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Admin";
                                        String email = obj.has("email") && !obj.get("email").isJsonNull() ? obj.get("email").getAsString() : "";
                                        String adminStatus = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";

                                        masterAdminList.add(new Admin(id, name, email, "System Administrator", capitalize(adminStatus)));
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing admins API", e);
                            }
                        }

                        if (masterAdminList.isEmpty() && currentPage == 1) {
                            loadFallbackAdmins();
                        } else {
                            bindAdminList();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadFallbackAdmins();
                    }
                });
    }

    private void loadFallbackAdmins() {
        if (!isAdded()) return;
        masterAdminList.clear();
        lastPage = 1;
        updatePaginationBar();
        bindAdminList();
        if (getContext() != null) {
            Toast.makeText(getContext(), getString(R.string.empty_state_no_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindAdminList() {
        if (!isAdded() || getContext() == null) return;

        adminAdapter = new AdminAdapter(getContext(), masterAdminList, this);
        if (rvAdmins != null) {
            rvAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAdmins.setAdapter(adminAdapter);
        }
    }

    // ==========================================
    // 4. FACULTY EVALUATION TAB IMPLEMENTATION
    // ==========================================
    private void bindFacultyEvalViews(View root) {
        rvFacultyEvaluation = root.findViewById(R.id.rvFacultyEvaluation);
        etSearchFaculty = root.findViewById(R.id.etSearchFaculty);

        if (etSearchFaculty != null) {
            etSearchFaculty.setHint(R.string.search_faculty_hint);
            etSearchFaculty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scheduleDebouncedSearch(() -> {
                        currentPage = 1;
                        loadFacultyEvaluationData();
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadFacultyEvaluationData() {
        if (!isAdded() || getContext() == null) return;

        facultyList.clear();
        if (rvFacultyEvaluation != null && facultyAdapter == null) {
            facultyAdapter = new FacultyEvaluationAdapter(getContext(), facultyList, this::showFacultyDetailsDialog);
            rvFacultyEvaluation.setLayoutManager(new LinearLayoutManager(getContext()));
            rvFacultyEvaluation.setAdapter(facultyAdapter);
        }

        final ApiService api = ApiClient.getApiService(requireContext());
        api.getFacultyStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                JsonObject d = response.body().getAsJsonObject("data");
                if (d == null) return;
                setStatText(R.id.tvFacultyCount, d, "faculty_reviewed");
                setStatText(R.id.tvFacultyCourses, d, "courses_assigned");
                setStatText(R.id.tvFacultyClasses, d, "classes_assigned");
                setStatText(R.id.tvFacultyReviewsNeeded, d, "reviews_needed");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Faculty statistics failed: " + t.getMessage());
            }
        });

        api.getFacultyEvaluators(getSearchText(etSearchFaculty).trim(), currentPage, PER_PAGE)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), getString(R.string.network_error_server), Toast.LENGTH_SHORT).show();
                            if (facultyAdapter != null) facultyAdapter.setItems(facultyList);
                            return;
                        }
                        JsonObject root = response.body();
                        JsonObject meta = root.has("meta") && root.get("meta").isJsonObject() ? root.getAsJsonObject("meta") : null;
                        if (meta != null) {
                            lastPage = Math.max(1, com.social7.studentlearnanalytics.utils.UiUtils.optInt(meta, "last_page", 1));
                            currentPage = Math.max(1, com.social7.studentlearnanalytics.utils.UiUtils.optInt(meta, "current_page", 1));
                        }
                        if (root.has("data") && root.get("data").isJsonArray()) {
                            for (JsonElement el : root.getAsJsonArray("data")) {
                                if (!el.isJsonObject()) continue;
                                JsonObject o = el.getAsJsonObject();
                                String passRate = com.social7.studentlearnanalytics.utils.UiUtils.optString(o, "class_pass_rate", "");
                                String rating = com.social7.studentlearnanalytics.utils.UiUtils.optString(o, "student_rating", "");
                                FacultyEvaluationAdapter.FacultyItem item = new FacultyEvaluationAdapter.FacultyItem(
                                        str(o, "name", "Unknown"),
                                        str(o, "position", "—"),
                                        str(o, "department", "No department"),
                                        com.social7.studentlearnanalytics.utils.UiUtils.optInt(o, "courses_count", 0) + " assigned",
                                        com.social7.studentlearnanalytics.utils.UiUtils.optInt(o, "classes_count", 0) + " assigned",
                                        passRate.isEmpty() ? "N/A" : passRate + "%",
                                        rating.isEmpty() ? "N/A" : rating + " / 5.0",
                                        str(o, "review_status", "Pending Review"));
                                item.id = com.social7.studentlearnanalytics.utils.UiUtils.optInt(o, "id", 0);
                                item.facultyId = str(o, "faculty_id", "");
                                facultyList.add(item);
                            }
                        }
                        if (facultyAdapter != null) facultyAdapter.setItems(facultyList);
                        if (facultyList.isEmpty()) {
                            Toast.makeText(getContext(), getString(R.string.search_no_results), Toast.LENGTH_SHORT).show();
                        }
                        updatePaginationBar();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), getString(R.string.network_error_server), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static String str(JsonObject o, String key, String fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
    }

    private void setStatText(int viewId, JsonObject d, String key) {
        View root = getView();
        if (root == null) return;
        TextView tv = root.findViewById(viewId);
        if (tv != null) tv.setText(d.has(key) && !d.get(key).isJsonNull() ? d.get(key).getAsString() : "—");
    }

    private String apiError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                JsonObject e = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                if (e.has("errors")) {
                    return e.getAsJsonObject("errors").entrySet().iterator().next().getValue().getAsJsonArray().get(0).getAsString();
                }
                if (e.has("message")) return e.get("message").getAsString();
            }
        } catch (Exception ignored) {
        }
        return "Request failed (HTTP " + response.code() + ")";
    }

    /** Pulls the item array out of either {data:[...]} or a paginated {data:{data:[...]}}. */
    private JsonArray extractArray(JsonObject root) {
        if (root == null || !root.has("data")) return new JsonArray();
        JsonElement d = root.get("data");
        if (d.isJsonArray()) return d.getAsJsonArray();
        if (d.isJsonObject() && d.getAsJsonObject().has("data") && d.getAsJsonObject().get("data").isJsonArray()) {
            return d.getAsJsonObject().getAsJsonArray("data");
        }
        return new JsonArray();
    }

    private final Callback<JsonObject> facultyRefreshCallback = new Callback<JsonObject>() {
        @Override
        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
            if (!isAdded()) return;
            if (response.isSuccessful()) {
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
                loadFacultyEvaluationData();
            } else {
                Toast.makeText(requireContext(), apiError(response), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            if (isAdded()) Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void showFacultyDetailsDialog(FacultyEvaluationAdapter.FacultyItem item) {
        if (!isAdded() || getContext() == null) return;

        String[] actions = {"View Profile", "View Courses", "View Classes", "Assign Courses",
                "Assign Classes", "Remove Assignments", "Submit Review"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.name + (item.facultyId == null || item.facultyId.isEmpty() ? "" : " (" + item.facultyId + ")"))
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: showFacultyProfile(item); break;
                        case 1: showAssigned(item, true); break;
                        case 2: showAssigned(item, false); break;
                        case 3: showAssignPicker(item, true); break;
                        case 4: showAssignPicker(item, false); break;
                        case 5: showRemovePicker(item); break;
                        default: showReviewDialog(item); break;
                    }
                })
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void showFacultyProfile(FacultyEvaluationAdapter.FacultyItem item) {
        String details = "Faculty ID: " + item.facultyId + "\n" +
                "Position: " + item.title + "\n" +
                "Department: " + item.department + "\n\n" +
                "Assigned Courses: " + item.courses + "\n" +
                "Assigned Classes: " + item.classes + "\n\n" +
                "Class Pass Rate: " + item.passRate + "\n" +
                "Student Rating: " + item.rating + "\n" +
                "Review Status: " + item.status;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.faculty_profile)
                .setMessage(details)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showAssigned(FacultyEvaluationAdapter.FacultyItem item, boolean courses) {
        ApiService api = ApiClient.getApiService(requireContext());
        Call<JsonObject> call = courses ? api.getFacultyCourses(item.id) : api.getFacultyClasses(item.id);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> c, @NonNull Response<JsonObject> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(requireContext(), apiError(r), Toast.LENGTH_LONG).show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (JsonElement el : extractArray(r.body())) {
                    JsonObject o = el.getAsJsonObject();
                    sb.append("• ").append(courses ? str(o, "course_code", "") + " - " + str(o, "course_name", "")
                            : str(o, "code", "") + " - " + str(o, "name", "")).append("\n");
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(courses ? "Assigned Courses" : "Assigned Classes")
                        .setMessage(sb.length() == 0 ? "Nothing assigned yet." : sb.toString())
                        .setPositiveButton(R.string.close, null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> c, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAssignPicker(FacultyEvaluationAdapter.FacultyItem item, boolean courses) {
        ApiService api = ApiClient.getApiService(requireContext());
        Call<JsonObject> call = courses ? api.getCoursesCatalog() : api.getClassesCatalog();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> c, @NonNull Response<JsonObject> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(requireContext(), apiError(r), Toast.LENGTH_LONG).show();
                    return;
                }
                JsonArray arr = extractArray(r.body());
                if (arr.size() == 0) {
                    Toast.makeText(requireContext(), courses ? "No courses available" : "No classes available", Toast.LENGTH_SHORT).show();
                    return;
                }
                int n = arr.size();
                final int[] ids = new int[n];
                final String[] labels = new String[n];
                final boolean[] checked = new boolean[n];
                for (int i = 0; i < n; i++) {
                    JsonObject o = arr.get(i).getAsJsonObject();
                    ids[i] = o.get("id").getAsInt();
                    labels[i] = courses
                            ? str(o, "course_code", str(o, "code", "")) + " - " + str(o, "course_name", str(o, "name", ""))
                            : str(o, "code", "") + " - " + str(o, "name", "");
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(courses ? "Assign Courses" : "Assign Classes")
                        .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> checked[which] = isChecked)
                        .setPositiveButton(R.string.assign, (d, w) -> {
                            JsonArray picked = new JsonArray();
                            for (int i = 0; i < n; i++) if (checked[i]) picked.add(ids[i]);
                            if (picked.size() == 0) {
                                Toast.makeText(requireContext(), "Select at least one", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JsonObject body = new JsonObject();
                            body.add(courses ? "course_ids" : "class_ids", picked);
                            ApiService svc = ApiClient.getApiService(requireContext());
                            (courses ? svc.assignFacultyCourses(item.id, body) : svc.assignFacultyClasses(item.id, body))
                                    .enqueue(facultyRefreshCallback);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> c, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showRemovePicker(FacultyEvaluationAdapter.FacultyItem item) {
        ApiService api = ApiClient.getApiService(requireContext());
        api.getFacultyCourses(item.id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> c, @NonNull Response<JsonObject> rc) {
                if (!isAdded()) return;
                if (!rc.isSuccessful() || rc.body() == null) {
                    Toast.makeText(requireContext(), apiError(rc), Toast.LENGTH_LONG).show();
                    return;
                }
                final JsonArray courseArr = extractArray(rc.body());
                api.getFacultyClasses(item.id).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> c2, @NonNull Response<JsonObject> rk) {
                        if (!isAdded()) return;
                        if (!rk.isSuccessful() || rk.body() == null) {
                            Toast.makeText(requireContext(), apiError(rk), Toast.LENGTH_LONG).show();
                            return;
                        }
                        JsonArray classArr = extractArray(rk.body());
                        int nc = courseArr.size(), total = nc + classArr.size();
                        if (total == 0) {
                            Toast.makeText(requireContext(), R.string.no_assignments_to_remove, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final int[] ids = new int[total];
                        final String[] labels = new String[total];
                        final boolean[] checked = new boolean[total];
                        for (int i = 0; i < total; i++) {
                            boolean isCourse = i < nc;
                            JsonObject o = (isCourse ? courseArr.get(i) : classArr.get(i - nc)).getAsJsonObject();
                            ids[i] = o.get("id").getAsInt();
                            labels[i] = (isCourse ? "Course: " + str(o, "course_code", "") : "Class: " + str(o, "code", ""));
                        }
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.remove_assignments)
                                .setMultiChoiceItems(labels, checked, (d, which, ch) -> checked[which] = ch)
                                .setPositiveButton(R.string.remove, (d, w) -> {
                                    JsonArray cIds = new JsonArray(), kIds = new JsonArray();
                                    for (int i = 0; i < total; i++) {
                                        if (!checked[i]) continue;
                                        if (i < nc) cIds.add(ids[i]); else kIds.add(ids[i]);
                                    }
                                    if (cIds.size() + kIds.size() == 0) return;
                                    JsonObject body = new JsonObject();
                                    if (cIds.size() > 0) body.add("course_ids", cIds);
                                    if (kIds.size() > 0) body.add("class_ids", kIds);
                                    api.removeFacultyAssignments(item.id, body).enqueue(facultyRefreshCallback);
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> c2, @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> c, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showReviewDialog(FacultyEvaluationAdapter.FacultyItem item) {
        android.widget.LinearLayout box = new android.widget.LinearLayout(requireContext());
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText etRating = new EditText(requireContext());
        etRating.setHint("Rating 1.0 - 5.0 (required when completed)");
        etRating.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        box.addView(etRating);

        final EditText etComments = new EditText(requireContext());
        etComments.setHint(R.string.comments_optional);
        box.addView(etComments);

        final Spinner spStatus = new Spinner(requireContext());
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"completed", "pending"}));
        box.addView(spStatus);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.submit_faculty_review)
                .setView(box)
                .setPositiveButton(R.string.submit, (d, w) -> {
                    String status = (String) spStatus.getSelectedItem();
                    String ratingStr = etRating.getText().toString().trim();
                    if ("completed".equalsIgnoreCase(status) && ratingStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Rating (1.0 - 5.0) is required when completed.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("status", status);
                    if (!ratingStr.isEmpty()) {
                        try {
                            double val = Double.parseDouble(ratingStr);
                            if (val < 1.0 || val > 5.0) {
                                Toast.makeText(requireContext(), "Rating must be between 1.0 and 5.0.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            body.addProperty("rating", val);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid rating number.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    String comments = etComments.getText().toString().trim();
                    if (!comments.isEmpty()) body.addProperty("comments", comments);
                    ApiClient.getApiService(requireContext()).submitFacultyReview(item.id, body)
                            .enqueue(facultyRefreshCallback);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ==========================================
    // PAGINATION CONTROLS
    // ==========================================
    private void bindPaginationViews(View root) {
        btnPrevPage = root.findViewById(R.id.btnPrevPage);
        btnNextPage = root.findViewById(R.id.btnNextPage);
        btnPage1 = root.findViewById(R.id.btnPage1);
        btnPage2 = root.findViewById(R.id.btnPage2);
        btnPage3 = root.findViewById(R.id.btnPage3);
        btnPage4 = root.findViewById(R.id.btnPage4);
        btnPage5 = root.findViewById(R.id.btnPage5);

        if (btnPrevPage != null) {
            btnPrevPage.setOnClickListener(v -> {
                if (currentPage > 1) {
                    currentPage--;
                    reloadCurrentTab();
                }
            });
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                if (currentPage < lastPage) {
                    currentPage++;
                    reloadCurrentTab();
                }
            });
        }

        TextView[] pageBtns = new TextView[]{btnPage1, btnPage2, btnPage3, btnPage4, btnPage5};
        for (int i = 0; i < pageBtns.length; i++) {
            final int pageNum = i + 1;
            TextView tv = pageBtns[i];
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    if (pageNum <= lastPage) {
                        currentPage = pageNum;
                        reloadCurrentTab();
                    }
                });
            }
        }
    }

    private void updatePaginationBar() {
        if (!isAdded() || getContext() == null) return;

        if (btnPrevPage != null) {
            btnPrevPage.setEnabled(currentPage > 1);
            btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.4f);
        }
        if (btnNextPage != null) {
            btnNextPage.setEnabled(currentPage < lastPage);
            btnNextPage.setAlpha(currentPage < lastPage ? 1.0f : 0.4f);
        }

        TextView[] pageBtns = new TextView[]{btnPage1, btnPage2, btnPage3, btnPage4, btnPage5};
        for (int i = 0; i < pageBtns.length; i++) {
            TextView tv = pageBtns[i];
            if (tv == null) continue;
            int pageNum = i + 1;
            if (pageNum <= lastPage) {
                tv.setVisibility(View.VISIBLE);
                if (pageNum == currentPage) {
                    tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                    tv.setTextColor(Color.WHITE);
                } else {
                    tv.setBackground(null);
                    tv.setTextColor(Color.parseColor("#64748B"));
                }
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    private void reloadCurrentTab() {
        if (selectedTab == 3) { loadFacultyEvaluationData(); return; }
        if (selectedTab == 0) loadStudentList();
        else if (selectedTab == 1) loadTeacherList();
        else if (selectedTab == 2) loadAdminList();
    }

    private void scheduleDebouncedSearch(Runnable action) {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        pendingSearchRunnable = () -> {
            if (isAdded() && getView() != null) {
                action.run();
            }
        };
        searchHandler.postDelayed(pendingSearchRunnable, 300L);
    }

    @Override
    public void onDestroyView() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
        super.onDestroyView();
    }

    private String getSearchText(EditText editText) {
        return (editText != null && editText.getText() != null) ? editText.getText().toString() : "";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Active";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // ==========================================
    // ACTION LISTENERS
    // ==========================================
    @Override
    public void onViewDetails(User user) {
        String studentCode = user.getCode() != null ? user.getCode() : user.getId();
        Student student = new Student();
        student.setId(studentCode);
        student.setStudentCode(studentCode);
        student.setName(user.getName());
        student.setEnglishName(user.getName());
        student.setEmail(user.getEmail());
        student.setStatus(user.getStatus());
        if (user.getNumericId() != null) {
            student.setUserId(user.getNumericId());
        }

        Intent intent = new Intent(requireContext(), AdminStudentDetailActivity.class);
        intent.putExtra("student", student);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onEditUser(User user) {
        String studentCode = user.getCode() != null ? user.getCode() : user.getId();
        Student student = new Student();
        student.setId(studentCode);
        student.setStudentCode(studentCode);
        student.setName(user.getName());
        student.setEnglishName(user.getName());
        student.setEmail(user.getEmail());
        student.setStatus(user.getStatus());
        if (user.getNumericId() != null) {
            student.setUserId(user.getNumericId());
        }

        Intent intent = new Intent(requireContext(), EditStudentActivity.class);
        intent.putExtra("student", student);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onViewDetails(Admin admin) {
        Intent intent = new Intent(requireContext(), AdminDetailActivity.class);
        intent.putExtra("admin", admin);
        startActivity(intent);
    }

    @Override
    public void onEditAdmin(Admin admin) {
        Intent intent = new Intent(requireContext(), EditAdminActivity.class);
        intent.putExtra("admin", admin);
        startActivity(intent);
    }

    @Override
    public void onViewDetails(Teacher teacher) {
        Intent intent = new Intent(requireContext(), AdminTeacherDetailActivity.class);
        intent.putExtra("teacher", teacher);
        startActivity(intent);
    }

    @Override
    public void onEditTeacher(Teacher teacher) {
        Intent intent = new Intent(requireContext(), EditTeacherActivity.class);
        intent.putExtra("teacher", teacher);
        startActivity(intent);
    }

    @Override
    public void onToggleStatus(Teacher teacher) {
        boolean currentlyActive = "Active".equalsIgnoreCase(teacher.getStatus());
        String newStatus = currentlyActive ? "inactive" : "active";
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + teacher.getName() + " (" + teacher.getId() + ")?")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (teacher.getUserId() != null && teacher.getUserId() > 0) {
                        updateUserStatusOnBackend(String.valueOf(teacher.getUserId()), newStatus, action);
                    } else if (teacher.getDbId() != null && teacher.getDbId() > 0) {
                        JsonObject body = new JsonObject();
                        body.addProperty("status", newStatus);
                        ApiClient.getApiService(requireContext())
                                .updateTeacher(String.valueOf(teacher.getDbId()), body)
                                .enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                        if (!isAdded()) return;
                                        if (response.isSuccessful()) {
                                            Toast.makeText(requireContext(), "active".equalsIgnoreCase(newStatus)
                                                    ? R.string.account_activated_success : R.string.account_deactivated_success, Toast.LENGTH_SHORT).show();
                                            fetchUserStatistics();
                                            reloadCurrentTab();
                                        } else {
                                            Toast.makeText(requireContext(), getString(R.string.status_update_failed, response.code()), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        Toast.makeText(requireContext(), getString(R.string.network_error_updating_status_fmt, t.getMessage()), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onToggleStatus(Admin admin) {
        boolean currentlyActive = "Active".equalsIgnoreCase(admin.getStatus());
        String newStatus = currentlyActive ? "inactive" : "active";
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + admin.getName() + " (" + admin.getId() + ")?")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String targetId = admin.getUserId() != null ? String.valueOf(admin.getUserId()) : admin.getId();
                    updateUserStatusOnBackend(targetId, newStatus, action);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onToggleStatus(User user) {
        boolean currentlyActive = "Active".equalsIgnoreCase(user.getStatus());
        String newStatus = currentlyActive ? "inactive" : "active";
        String displayCode = (user.getCode() != null && !user.getCode().isEmpty()) ? user.getCode() : user.getId();

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? R.string.deactivate_account : R.string.activate_account)
                .setMessage(getString(currentlyActive ? R.string.confirm_deactivate_account_msg : R.string.confirm_activate_account_msg,
                        user.getName(), displayCode))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String targetId = user.getApiUserId();
                    updateUserStatusOnBackend(targetId, newStatus, !currentlyActive);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateUserStatusOnBackend(String userId, String newStatus, String actionLabel) {
        updateUserStatusOnBackend(userId, newStatus, "active".equalsIgnoreCase(newStatus));
    }

    private void updateUserStatusOnBackend(String userId, String newStatus, boolean activating) {
        JsonObject body = new JsonObject();
        body.addProperty("status", newStatus);

        ApiClient.getApiService(requireContext()).updateUserStatus(userId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), activating ? R.string.account_activated_success : R.string.account_deactivated_success, Toast.LENGTH_SHORT).show();
                    fetchUserStatistics();
                    reloadCurrentTab();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.status_update_failed, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), getString(R.string.network_error_updating_status_fmt, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteUser(User user) {
        String displayCode = (user.getCode() != null && !user.getCode().isEmpty()) ? user.getCode() : user.getId();
        confirmAndDeleteUser(user.getApiUserId(), user.getName(), displayCode);
    }

    @Override
    public void onDeleteTeacher(Teacher teacher) {
        if (teacher.getUserId() != null && teacher.getUserId() > 0) {
            confirmAndDeleteUser(String.valueOf(teacher.getUserId()), teacher.getName(), teacher.getId());
            return;
        }
        String teacherDbId = teacher.getDbId() != null ? String.valueOf(teacher.getDbId()) : teacher.getId();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_user_account)
                .setMessage(getString(R.string.confirm_delete_user_msg, teacher.getName(), teacher.getId()))
                .setPositiveButton(R.string.delete, (dialog, which) -> ApiClient.getApiService(requireContext())
                        .deleteTeacher(teacherDbId)
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                if (!isAdded()) return;
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
                                    fetchUserStatistics();
                                    reloadCurrentTab();
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.delete_user_failed, response.code()), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), getString(R.string.network_error_deleting_user_fmt, t.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDeleteAdmin(Admin admin) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String currentAdminEmail = sessionManager.getUserName();
        if (admin.getEmail() != null && currentAdminEmail != null && admin.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            Toast.makeText(requireContext(), "Self-deletion is prohibited. You cannot delete your own account.", Toast.LENGTH_LONG).show();
            return;
        }

        String targetId = admin.getUserId() != null ? String.valueOf(admin.getUserId()) : admin.getId();
        confirmAndDeleteUser(targetId, admin.getName());
    }

    private void confirmAndDeleteUser(String userId, String name) {
        confirmAndDeleteUser(userId, name, userId);
    }

    private void confirmAndDeleteUser(String userId, String name, String displayCode) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_user_account)
                .setMessage(getString(R.string.confirm_delete_user_msg, name, displayCode))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    ApiClient.getApiService(requireContext()).deleteUser(userId).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (!isAdded()) return;
                            if (response.code() == 403) {
                                Toast.makeText(requireContext(), "Self-deletion is prohibited. You cannot delete your own account.", Toast.LENGTH_LONG).show();
                            } else if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
                                fetchUserStatistics();
                                reloadCurrentTab();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.delete_user_failed, response.code()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), getString(R.string.network_error_deleting_user_fmt, t.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserStatistics();
        reloadCurrentTab();
    }
}
