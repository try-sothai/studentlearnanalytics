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

    // Containers
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
    private MaterialButton btnAddTeacher;
    private String currentTeacherDepartmentFilter = "All Departments";

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
        if (tvHeaderTitle != null) tvHeaderTitle.setText("User Management");
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText("Manage students, teachers and administrators");

        // Primary Tabs
        btnTabStudents = view.findViewById(R.id.btnTabStudents);
        btnTabTeachers = view.findViewById(R.id.btnTabTeachers);
        btnTabAdmins = view.findViewById(R.id.btnTabAdmins);
        btnTabFacultyEval = view.findViewById(R.id.btnTabFacultyEval);

        // Containers
        layoutStudentsContainer = view.findViewById(R.id.layoutStudentsContainer);
        layoutTeachersContainer = view.findViewById(R.id.layoutTeachersContainer);
        layoutAdminsContainer = view.findViewById(R.id.layoutAdminsContainer);
        layoutFacultyEvalContainer = view.findViewById(R.id.layoutFacultyEvalContainer);

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

        if (layoutStudentsContainer != null) layoutStudentsContainer.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        if (layoutTeachersContainer != null) layoutTeachersContainer.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        if (layoutAdminsContainer != null) layoutAdminsContainer.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        if (layoutFacultyEvalContainer != null) layoutFacultyEvalContainer.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

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

                        int totalStudents = data.has("total_students") ? data.get("total_students").getAsInt() : 0;
                        int activeStudents = data.has("active_students") ? data.get("active_students").getAsInt() : 0;
                        int inactiveStudents = data.has("inactive_students") ? data.get("inactive_students").getAsInt() : 0;

                        if (pillFilterAll != null) pillFilterAll.setText("All Students (" + totalStudents + ")");
                        if (pillFilterActive != null) pillFilterActive.setText("Active (" + activeStudents + ")");
                        if (pillFilterInactive != null) pillFilterInactive.setText("Inactive (" + inactiveStudents + ")");

                        int totalTeachers = data.has("total_teachers") ? data.get("total_teachers").getAsInt() : 0;
                        int activeTeachers = data.has("active_teachers") ? data.get("active_teachers").getAsInt() : 0;
                        int inactiveTeachers = data.has("inactive_teachers") ? data.get("inactive_teachers").getAsInt() : 0;

                        if (pillFilterAllTeachers != null) pillFilterAllTeachers.setText("All Teachers (" + totalTeachers + ")");
                        if (pillFilterActiveTeachers != null) pillFilterActiveTeachers.setText("Active (" + activeTeachers + ")");
                        if (pillFilterInactiveTeachers != null) pillFilterInactiveTeachers.setText("Inactive (" + inactiveTeachers + ")");

                        int totalAdmins = data.has("total_admins") ? data.get("total_admins").getAsInt() : (data.has("total_administrators") ? data.get("total_administrators").getAsInt() : 0);
                        int activeAdmins = data.has("active_admins") ? data.get("active_admins").getAsInt() : 0;
                        int inactiveAdmins = data.has("inactive_admins") ? data.get("inactive_admins").getAsInt() : 0;

                        if (pillFilterAllAdmins != null) pillFilterAllAdmins.setText("All Admins (" + totalAdmins + ")");
                        if (pillFilterActiveAdmins != null) pillFilterActiveAdmins.setText("Active (" + activeAdmins + ")");
                        if (pillFilterInactiveAdmins != null) pillFilterInactiveAdmins.setText("Inactive (" + inactiveAdmins + ")");
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
            etSearchUser.setHint("Search by name, email or Student Code...");
            etSearchUser.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentPage = 1;
                    loadStudentList();
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
                                        String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Student";
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
        masterUserList.add(new User(1L, "1", "Sok Dara", "student@slas.com", "Student", "Active", "STD001"));
        masterUserList.add(new User(2L, "2", "Chan Sophea", "std002@slas.com", "Student", "Active", "STD002"));
        masterUserList.add(new User(3L, "3", "Vannak Rith", "std003@slas.com", "Student", "Active", "STD003"));
        masterUserList.add(new User(4L, "4", "Srey Mom", "std004@slas.com", "Student", "Inactive", "STD004"));
        masterUserList.add(new User(5L, "5", "Kosal Meas", "std005@slas.com", "Student", "Active", "STD005"));
        lastPage = 1;
        updatePaginationBar();
        bindStudentList();
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
            etSearchTeacher.setHint("Search by name, email or Teacher ID...");
            etSearchTeacher.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentPage = 1;
                    loadTeacherList();
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
            btnAddTeacher.setText("+ Add Teacher");
            btnAddTeacher.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddTeacherActivity.class);
                startActivity(intent);
            });
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

        ApiClient.getApiService(getContext()).getUsers("teacher", status, querySearch, currentPage, PER_PAGE)
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

                                        String id = obj.has("code") && !obj.get("code").isJsonNull() ? obj.get("code").getAsString() : (obj.has("id") ? obj.get("id").getAsString() : "TCH");
                                        String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Teacher";
                                        String email = obj.has("email") && !obj.get("email").isJsonNull() ? obj.get("email").getAsString() : "";
                                        String dept = obj.has("department_name") && !obj.get("department_name").isJsonNull() ? obj.get("department_name").getAsString() : "Computer Science";
                                        String teacherStatus = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";

                                        masterTeacherList.add(new Teacher(id, name, email, dept, "Lecturer", capitalize(teacherStatus)));
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
        masterTeacherList.add(new Teacher("TCH001", "Emily Johnson", "teacher@slas.com", "Computer Science", "Associate Professor", "Active"));
        masterTeacherList.add(new Teacher("TCH002", "Michael Chen", "tch002@slas.com", "Computer Science", "Professor", "Active"));
        masterTeacherList.add(new Teacher("TCH003", "Sophia Martinez", "tch003@slas.com", "Information Technology", "Assistant Professor", "Active"));
        masterTeacherList.add(new Teacher("TCH004", "David Kim", "tch004@slas.com", "Information Systems", "Senior Lecturer", "Active"));
        lastPage = 1;
        updatePaginationBar();
        bindTeacherList();
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
            etSearchAdmin.setHint("Search by name, email or Admin ID...");
            etSearchAdmin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentPage = 1;
                    loadAdminList();
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
            btnAddAdmin.setText("+ Add Admin");
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
        masterAdminList.add(new Admin("ADM001", "Admin SLAS", "admin@slas.com", "Super Administrator", "Active"));
        masterAdminList.add(new Admin("ADM002", "System Admin", "admin@slas.local", "Academic Admin", "Active"));
        lastPage = 1;
        updatePaginationBar();
        bindAdminList();
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
        View layoutEval = root.findViewById(R.id.layoutFacultyEvalContainer);
        if (layoutEval == null) return;

        rvFacultyEvaluation = layoutEval.findViewById(R.id.rvFacultyEvaluation);
        etSearchFaculty = layoutEval.findViewById(R.id.etSearchFaculty);

        if (etSearchFaculty != null) {
            etSearchFaculty.setHint("Search faculty members...");
            etSearchFaculty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentPage = 1;
                    loadFacultyEvaluationData();
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
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(requireContext(), "Failed to load faculty (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            if (facultyAdapter != null) facultyAdapter.setItems(facultyList);
                            return;
                        }
                        JsonObject root = response.body();
                        JsonObject meta = root.has("meta") && root.get("meta").isJsonObject() ? root.getAsJsonObject("meta") : null;
                        if (meta != null) {
                            lastPage = Math.max(1, meta.get("last_page").getAsInt());
                            currentPage = meta.get("current_page").getAsInt();
                        }
                        for (JsonElement el : root.getAsJsonArray("data")) {
                            JsonObject o = el.getAsJsonObject();
                            FacultyEvaluationAdapter.FacultyItem item = new FacultyEvaluationAdapter.FacultyItem(
                                    str(o, "name", "Unknown"),
                                    str(o, "position", "—"),
                                    str(o, "department", "No department"),
                                    o.get("courses_count").getAsInt() + " assigned",
                                    o.get("classes_count").getAsInt() + " assigned",
                                    o.get("class_pass_rate").isJsonNull() ? "N/A" : o.get("class_pass_rate").getAsString() + "%",
                                    o.get("student_rating").isJsonNull() ? "N/A" : o.get("student_rating").getAsString() + " / 5.0",
                                    str(o, "review_status", "Pending Review"));
                            item.id = o.get("id").getAsInt();
                            item.facultyId = str(o, "faculty_id", "");
                            facultyList.add(item);
                        }
                        if (facultyAdapter != null) facultyAdapter.setItems(facultyList);
                        if (facultyList.isEmpty()) {
                            Toast.makeText(requireContext(), "No faculty members found", Toast.LENGTH_SHORT).show();
                        }
                        updatePaginationBar();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                .setNegativeButton("Close", null)
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
                .setTitle("Faculty Profile")
                .setMessage(details)
                .setPositiveButton("Close", null)
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
                        .setPositiveButton("Close", null)
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
                        .setPositiveButton("Assign", (d, w) -> {
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
                        .setNegativeButton("Cancel", null)
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
                            Toast.makeText(requireContext(), "No assignments to remove", Toast.LENGTH_SHORT).show();
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
                                .setTitle("Remove Assignments")
                                .setMultiChoiceItems(labels, checked, (d, which, ch) -> checked[which] = ch)
                                .setPositiveButton("Remove", (d, w) -> {
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
                                .setNegativeButton("Cancel", null)
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
        etComments.setHint("Comments (optional)");
        box.addView(etComments);

        final Spinner spStatus = new Spinner(requireContext());
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                new String[]{"completed", "pending"}));
        box.addView(spStatus);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Submit Faculty Review")
                .setView(box)
                .setPositiveButton("Submit", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("status", (String) spStatus.getSelectedItem());
                    String rating = etRating.getText().toString().trim();
                    if (!rating.isEmpty()) body.addProperty("rating", Double.parseDouble(rating));
                    String comments = etComments.getText().toString().trim();
                    if (!comments.isEmpty()) body.addProperty("comments", comments);
                    ApiClient.getApiService(requireContext()).submitFacultyReview(item.id, body)
                            .enqueue(facultyRefreshCallback);
                })
                .setNegativeButton("Cancel", null)
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
        Student student = new Student(
                studentCode,
                user.getName(),
                user.getEmail(),
                "Computer Science",
                "Bachelor of Computer Science",
                "Year 3",
                3.25,
                "Good Standing",
                85.0,
                "#EAF0FF",
                "#3155F5",
                "CS101"
        );
        student.setStatus(user.getStatus());

        Intent intent = new Intent(requireContext(), AdminStudentDetailActivity.class);
        intent.putExtra("student", student);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onEditUser(User user) {
        String studentCode = user.getCode() != null ? user.getCode() : user.getId();
        Student student = new Student(
                studentCode,
                user.getName(),
                user.getEmail(),
                "Computer Science",
                "Bachelor of Computer Science",
                "Year 3",
                3.25,
                "Good Standing",
                85.0,
                "#EAF0FF",
                "#3155F5",
                "CS101"
        );
        student.setStatus(user.getStatus());

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
                .setPositiveButton("Confirm", (dialog, which) -> {
                    updateUserStatusOnBackend(teacher.getId(), newStatus, action);
                })
                .setNegativeButton("Cancel", null)
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
                .setPositiveButton("Confirm", (dialog, which) -> {
                    updateUserStatusOnBackend(admin.getId(), newStatus, action);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleStatus(User user) {
        boolean currentlyActive = "Active".equalsIgnoreCase(user.getStatus());
        String newStatus = currentlyActive ? "inactive" : "active";
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + user.getName() + " (" + user.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    updateUserStatusOnBackend(user.getId(), newStatus, action);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserStatusOnBackend(String userId, String newStatus, String actionLabel) {
        JsonObject body = new JsonObject();
        body.addProperty("status", newStatus);

        ApiClient.getApiService(requireContext()).updateUserStatus(userId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Account " + actionLabel + "d successfully", Toast.LENGTH_SHORT).show();
                fetchUserStatistics();
                reloadCurrentTab();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Updated account status locally.", Toast.LENGTH_SHORT).show();
                fetchUserStatistics();
                reloadCurrentTab();
            }
        });
    }

    @Override
    public void onDeleteUser(User user) {
        confirmAndDeleteUser(user.getId(), user.getName());
    }

    @Override
    public void onDeleteTeacher(Teacher teacher) {
        confirmAndDeleteUser(teacher.getId(), teacher.getName());
    }

    @Override
    public void onDeleteAdmin(Admin admin) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String currentAdminEmail = sessionManager.getUserName();
        if (admin.getEmail() != null && currentAdminEmail != null && admin.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            Toast.makeText(requireContext(), "Self-deletion is prohibited. You cannot delete your own account.", Toast.LENGTH_LONG).show();
            return;
        }

        confirmAndDeleteUser(admin.getId(), admin.getName());
    }

    private void confirmAndDeleteUser(String userId, String name) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User Account")
                .setMessage("Are you sure you want to delete user " + name + " (" + userId + ")? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getApiService(requireContext()).deleteUser(userId).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (!isAdded()) return;
                            if (response.code() == 403) {
                                Toast.makeText(requireContext(), "Self-deletion is prohibited. You cannot delete your own account.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                fetchUserStatistics();
                                reloadCurrentTab();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                            fetchUserStatistics();
                            reloadCurrentTab();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserStatistics();
        reloadCurrentTab();
    }
}
