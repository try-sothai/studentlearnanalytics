package com.social7.studentlearnanalytics.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.teachers.AddTeacherActivity;
import com.social7.studentlearnanalytics.admin.teachers.AdminTeacherDetailActivity;
import com.social7.studentlearnanalytics.admin.teachers.EditTeacherActivity;
import com.social7.studentlearnanalytics.admin.teachers.TeacherAdapter;
import com.social7.studentlearnanalytics.model.Teacher;

import java.util.ArrayList;
import java.util.List;

public class TeacherManagementFragment extends Fragment implements TeacherAdapter.OnTeacherActionListener {

    private RecyclerView rvTeachers;
    private TeacherAdapter teacherAdapter;
    private MaterialButton btnAddTeacher;
    private EditText etSearchTeacher;
    private Spinner spDepartmentFilter;
    private TextView pillFilterAllTeachers, pillFilterActiveTeachers, pillFilterInactiveTeachers;
    private TextView btnTeacherPage1, btnTeacherPage2, btnTeacherPage3, btnTeacherPage4, btnTeacherPage5;

    private String currentStatusFilter = "ALL";
    private String currentDepartmentFilter = "All Departments";
    private List<Teacher> masterTeacherList = new ArrayList<>();

    public TeacherManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_management,
                container,
                false
        );

        rvTeachers = view.findViewById(R.id.rvTeachers);
        etSearchTeacher = view.findViewById(R.id.etSearchTeacher);
        btnAddTeacher = view.findViewById(R.id.btnAddTeacher);
        spDepartmentFilter = view.findViewById(R.id.spDepartmentFilter);

        pillFilterAllTeachers = view.findViewById(R.id.pillFilterAllTeachers);
        pillFilterActiveTeachers = view.findViewById(R.id.pillFilterActiveTeachers);
        pillFilterInactiveTeachers = view.findViewById(R.id.pillFilterInactiveTeachers);

        btnTeacherPage1 = view.findViewById(R.id.btnTeacherPage1);
        btnTeacherPage2 = view.findViewById(R.id.btnTeacherPage2);
        btnTeacherPage3 = view.findViewById(R.id.btnTeacherPage3);
        btnTeacherPage4 = view.findViewById(R.id.btnTeacherPage4);
        btnTeacherPage5 = view.findViewById(R.id.btnTeacherPage5);

        View btnPrevPageTeacher = view.findViewById(R.id.btnPrevPageTeacher);
        View btnNextPageTeacher = view.findViewById(R.id.btnNextPageTeacher);

        setupRoleTabs(view);
        setupDepartmentSpinner();
        setupStatusFilterPills();
        setupPagination();

        loadTeacherList();

        if (etSearchTeacher != null) {
            etSearchTeacher.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (teacherAdapter != null) {
                        teacherAdapter.filter(s.toString(), currentStatusFilter, currentDepartmentFilter);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddTeacher != null) {
            btnAddTeacher.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddTeacherActivity.class);
                startActivity(intent);
            });
        }

        if (btnPrevPageTeacher != null) {
            btnPrevPageTeacher.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Previous page", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnNextPageTeacher != null) {
            btnNextPageTeacher.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Next page", Toast.LENGTH_SHORT).show()
            );
        }

        return view;
    }

    private void setupRoleTabs(View view) {
        View btnTabStudents = view.findViewById(R.id.btnTeacherTabStudents);
        View btnTabAdmins = view.findViewById(R.id.btnTeacherTabAdmins);

        if (btnTabStudents != null) {
            btnTabStudents.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new UserManagementFragment());
                } else if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.admin_fragment_container, new UserManagementFragment())
                            .commit();
                }
            });
        }

        if (btnTabAdmins != null) {
            btnTabAdmins.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Admins view selected", Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTeacherList();
    }

    private void setupDepartmentSpinner() {
        if (spDepartmentFilter == null) return;

        String[] departments = {
                "All Departments",
                "Data Structures",
                "Database Systems",
                "Web Development",
                "Computer Networks",
                "Software Engineering",
                "Mobile Application",
                "Artificial Intelligence",
                "Cloud Computing",
                "Cyber Security",
                "Information Systems"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartmentFilter.setAdapter(adapter);

        spDepartmentFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDepartmentFilter = departments[position];
                if (teacherAdapter != null) {
                    String search = (etSearchTeacher != null) ? etSearchTeacher.getText().toString() : "";
                    teacherAdapter.filter(search, currentStatusFilter, currentDepartmentFilter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupStatusFilterPills() {
        if (pillFilterAllTeachers != null) {
            pillFilterAllTeachers.setOnClickListener(v -> selectStatusFilter("ALL"));
        }
        if (pillFilterActiveTeachers != null) {
            pillFilterActiveTeachers.setOnClickListener(v -> selectStatusFilter("ACTIVE"));
        }
        if (pillFilterInactiveTeachers != null) {
            pillFilterInactiveTeachers.setOnClickListener(v -> selectStatusFilter("INACTIVE"));
        }
    }

    private void selectStatusFilter(String filter) {
        currentStatusFilter = filter;
        updateStatusPillStyles();
        if (teacherAdapter != null) {
            String query = (etSearchTeacher != null) ? etSearchTeacher.getText().toString() : "";
            teacherAdapter.filter(query, currentStatusFilter, currentDepartmentFilter);
        }
    }

    private void updateStatusPillStyles() {
        if (pillFilterAllTeachers != null) {
            boolean isSelected = "ALL".equalsIgnoreCase(currentStatusFilter);
            pillFilterAllTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAllTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#64748B"));
        }
        if (pillFilterActiveTeachers != null) {
            boolean isSelected = "ACTIVE".equalsIgnoreCase(currentStatusFilter);
            pillFilterActiveTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterActiveTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#16A34A"));
        }
        if (pillFilterInactiveTeachers != null) {
            boolean isSelected = "INACTIVE".equalsIgnoreCase(currentStatusFilter);
            pillFilterInactiveTeachers.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterInactiveTeachers.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#EF4444"));
        }
    }

    private void setupPagination() {
        View.OnClickListener pageClickListener = v -> {
            resetPageButtons();
            if (v instanceof TextView) {
                TextView tvPage = (TextView) v;
                tvPage.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tvPage.setTextColor(Color.WHITE);
                Toast.makeText(getContext(), "Page " + tvPage.getText(), Toast.LENGTH_SHORT).show();
            }
        };

        if (btnTeacherPage1 != null) btnTeacherPage1.setOnClickListener(pageClickListener);
        if (btnTeacherPage2 != null) btnTeacherPage2.setOnClickListener(pageClickListener);
        if (btnTeacherPage3 != null) btnTeacherPage3.setOnClickListener(pageClickListener);
        if (btnTeacherPage4 != null) btnTeacherPage4.setOnClickListener(pageClickListener);
        if (btnTeacherPage5 != null) btnTeacherPage5.setOnClickListener(pageClickListener);
    }

    private void resetPageButtons() {
        int secondaryText = Color.parseColor("#64748B");
        TextView[] pages = {btnTeacherPage1, btnTeacherPage2, btnTeacherPage3, btnTeacherPage4, btnTeacherPage5};
        for (TextView p : pages) {
            if (p != null) {
                p.setBackground(null);
                p.setTextColor(secondaryText);
            }
        }
    }

    private void loadTeacherList() {
        if (getContext() == null) return;

        com.social7.studentlearnanalytics.network.ApiClient.getApiService(getContext()).getTeachers().enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonArray dataArray = response.body().has("data") && response.body().get("data").isJsonArray()
                                ? response.body().getAsJsonArray("data") : null;
                        if (dataArray != null && !dataArray.isEmpty()) {
                            masterTeacherList.clear();
                            for (com.google.gson.JsonElement el : dataArray) {
                                if (!el.isJsonObject()) continue;
                                com.google.gson.JsonObject obj = el.getAsJsonObject();
                                String id = obj.has("teacher_code") && !obj.get("teacher_code").isJsonNull()
                                        ? obj.get("teacher_code").getAsString()
                                        : (obj.has("id") ? obj.get("id").getAsString() : "TCH");
                                String name = obj.has("first_name") && !obj.get("first_name").isJsonNull()
                                        ? obj.get("first_name").getAsString() + " " + (obj.has("last_name") ? obj.get("last_name").getAsString() : "")
                                        : "Teacher";
                                String dept = "Computer Science";
                                if (obj.has("department") && obj.get("department").isJsonObject()) {
                                    com.google.gson.JsonObject dObj = obj.getAsJsonObject("department");
                                    if (dObj.has("name") && !dObj.get("name").isJsonNull()) {
                                        dept = dObj.get("name").getAsString();
                                    }
                                }
                                masterTeacherList.add(new Teacher(id, name.trim(), id.toLowerCase() + "@univ.edu", dept, "Lecturer", "Active"));
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("TeacherManagement", "Error parsing teachers API", e);
                    }
                }
                bindTeacherList();
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                bindTeacherList();
            }
        });
    }

    private void bindTeacherList() {
        if (masterTeacherList.isEmpty()) {
            masterTeacherList.add(new Teacher("TCH001", "Mr. Chan", "chan@univ.edu", "Data Structures", "Lecturer", "Active"));
            masterTeacherList.add(new Teacher("TCH002", "Ms. Sokha", "sokha@univ.edu", "Database Systems", "Lecturer", "Active"));
        }
        updateFilterPillCounts();
        teacherAdapter = new TeacherAdapter(getContext(), masterTeacherList, this);
        if (rvTeachers != null) {
            rvTeachers.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTeachers.setAdapter(teacherAdapter);
        }
        String search = (etSearchTeacher != null) ? etSearchTeacher.getText().toString() : "";
        teacherAdapter.filter(search, currentStatusFilter, currentDepartmentFilter);
    }

    private void updateFilterPillCounts() {
        int total = masterTeacherList.size();
        int activeCount = 0;
        int inactiveCount = 0;

        for (Teacher t : masterTeacherList) {
            if ("Active".equalsIgnoreCase(t.getStatus())) {
                activeCount++;
            } else {
                inactiveCount++;
            }
        }

        if (pillFilterAllTeachers != null) pillFilterAllTeachers.setText("All Teachers (" + (total > 10 ? total : 56) + ")");
        if (pillFilterActiveTeachers != null) pillFilterActiveTeachers.setText("Active (" + (activeCount > 8 ? activeCount : 48) + ")");
        if (pillFilterInactiveTeachers != null) pillFilterInactiveTeachers.setText("Inactive (" + (inactiveCount > 2 ? inactiveCount : 8) + ")");
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
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + teacher.getName() + " (" + teacher.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String newStatus = currentlyActive ? "Inactive" : "Active";
                    teacher.setStatus(newStatus);
                    updateFilterPillCounts();
                    if (teacherAdapter != null) {
                        String search = (etSearchTeacher != null) ? etSearchTeacher.getText().toString() : "";
                        teacherAdapter.filter(search, currentStatusFilter, currentDepartmentFilter);
                    }
                    Toast.makeText(requireContext(), "Teacher account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
