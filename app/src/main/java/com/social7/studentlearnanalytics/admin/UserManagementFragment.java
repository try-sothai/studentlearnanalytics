package com.social7.studentlearnanalytics.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.social7.studentlearnanalytics.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserActionListener, AdminAdapter.OnAdminActionListener {

    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private AdminAdapter adminAdapter;

    private MaterialButton btnTabStudents, btnTabTeachers, btnTabAdmins, btnAddUser;
    private EditText etSearchUser;
    private TextView pillFilterAll, pillFilterActive, pillFilterInactive;
    private TextView tvHeaderTitle, tvHeaderSubtitle;
    private TextView btnPage1, btnPage2, btnPage3, btnPage4, btnPage5;

    private String currentRole = "Student";
    private String currentStatusFilter = "ALL"; // "ALL", "ACTIVE", "INACTIVE"
    private List<User> masterUserList = new ArrayList<>();
    private List<Admin> masterAdminList = new ArrayList<>();

    public UserManagementFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_admin_users,
                container,
                false
        );

        rvUsers = view.findViewById(R.id.rvUsers);
        etSearchUser = view.findViewById(R.id.etSearchUser);
        btnAddUser = view.findViewById(R.id.btnAddUser);
        btnTabStudents = view.findViewById(R.id.btnTabStudents);
        btnTabTeachers = view.findViewById(R.id.btnTabTeachers);
        btnTabAdmins = view.findViewById(R.id.btnTabAdmins);

        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = view.findViewById(R.id.tvHeaderSubtitle);

        View btnMenu = view.findViewById(R.id.btnUsersMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        pillFilterAll = view.findViewById(R.id.pillFilterAll);
        pillFilterActive = view.findViewById(R.id.pillFilterActive);
        pillFilterInactive = view.findViewById(R.id.pillFilterInactive);

        btnPage1 = view.findViewById(R.id.btnPage1);
        btnPage2 = view.findViewById(R.id.btnPage2);
        btnPage3 = view.findViewById(R.id.btnPage3);
        btnPage4 = view.findViewById(R.id.btnPage4);
        btnPage5 = view.findViewById(R.id.btnPage5);

        View btnPrevPage = view.findViewById(R.id.btnPrevPage);
        View btnNextPage = view.findViewById(R.id.btnNextPage);

        setupRoleTabs();
        setupStatusFilterPills();
        setupPagination();

        loadUserList(currentRole);

        if (etSearchUser != null) {
            etSearchUser.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if ("Admin".equalsIgnoreCase(currentRole)) {
                        if (adminAdapter != null) adminAdapter.filter(s.toString(), currentStatusFilter, "All Roles");
                    } else {
                        if (userAdapter != null) userAdapter.filter(s.toString(), currentStatusFilter);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnAddUser != null) {
            btnAddUser.setOnClickListener(v -> {
                if ("Teacher".equalsIgnoreCase(currentRole)) {
                    Intent intent = new Intent(requireContext(), AddTeacherActivity.class);
                    startActivity(intent);
                } else if ("Admin".equalsIgnoreCase(currentRole)) {
                    Intent intent = new Intent(requireContext(), AddAdminActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(requireContext(), AddStudentActivity.class);
                    startActivity(intent);
                }
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

    @Override
    public void onResume() {
        super.onResume();
        loadUserList(currentRole);
    }

    private void setupRoleTabs() {
        if (btnTabStudents != null) {
            btnTabStudents.setOnClickListener(v -> selectRoleTab("Student"));
        }
        if (btnTabTeachers != null) {
            btnTabTeachers.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openFragment(new TeacherManagementFragment());
                } else if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.admin_fragment_container, new TeacherManagementFragment())
                            .commit();
                }
            });
        }
        if (btnTabAdmins != null) {
            btnTabAdmins.setOnClickListener(v -> selectRoleTab("Admin"));
        }
    }

    private void selectRoleTab(String role) {
        currentRole = role;
        updateRoleTabStyles(role);
        loadUserList(role);
    }

    private void updateRoleTabStyles(String selectedRole) {
        int primaryColor = Color.parseColor("#3155F5");
        int secondaryColor = Color.parseColor("#64748B");
        int whiteColor = Color.WHITE;

        if (btnTabStudents != null) {
            boolean active = "Student".equalsIgnoreCase(selectedRole);
            btnTabStudents.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabStudents.setTextColor(active ? whiteColor : secondaryColor);
        }
        if (btnTabTeachers != null) {
            boolean active = "Teacher".equalsIgnoreCase(selectedRole);
            btnTabTeachers.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabTeachers.setTextColor(active ? whiteColor : secondaryColor);
        }
        if (btnTabAdmins != null) {
            boolean active = "Admin".equalsIgnoreCase(selectedRole);
            btnTabAdmins.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
            btnTabAdmins.setTextColor(active ? whiteColor : secondaryColor);
        }

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(selectedRole + " Management");
        }
        if (tvHeaderSubtitle != null) {
            tvHeaderSubtitle.setText("Manage and monitor " + ("Admin".equalsIgnoreCase(selectedRole) ? "system administrators" : "registered " + selectedRole.toLowerCase() + "s"));
        }

        if (btnAddUser != null) {
            btnAddUser.setText("+ Add " + selectedRole);
        }
        if (etSearchUser != null) {
            etSearchUser.setHint("Search " + selectedRole.toLowerCase() + "s...");
        }
    }

    private void setupStatusFilterPills() {
        if (pillFilterAll != null) {
            pillFilterAll.setOnClickListener(v -> selectStatusFilter("ALL"));
        }
        if (pillFilterActive != null) {
            pillFilterActive.setOnClickListener(v -> selectStatusFilter("ACTIVE"));
        }
        if (pillFilterInactive != null) {
            pillFilterInactive.setOnClickListener(v -> selectStatusFilter("INACTIVE"));
        }
    }

    private void selectStatusFilter(String filter) {
        currentStatusFilter = filter;
        updateStatusPillStyles();
        String query = (etSearchUser != null) ? etSearchUser.getText().toString() : "";
        if ("Admin".equalsIgnoreCase(currentRole)) {
            if (adminAdapter != null) adminAdapter.filter(query, currentStatusFilter, "All Roles");
        } else {
            if (userAdapter != null) userAdapter.filter(query, currentStatusFilter);
        }
    }

    private void updateStatusPillStyles() {
        if (pillFilterAll != null) {
            boolean isSelected = "ALL".equalsIgnoreCase(currentStatusFilter);
            pillFilterAll.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterAll.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#64748B"));
        }
        if (pillFilterActive != null) {
            boolean isSelected = "ACTIVE".equalsIgnoreCase(currentStatusFilter);
            pillFilterActive.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterActive.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#16A34A"));
        }
        if (pillFilterInactive != null) {
            boolean isSelected = "INACTIVE".equalsIgnoreCase(currentStatusFilter);
            pillFilterInactive.setBackgroundResource(isSelected ? R.drawable.bg_notif_tab_active : R.drawable.bg_notif_tab_inactive);
            pillFilterInactive.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#EF4444"));
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

        if (btnPage1 != null) btnPage1.setOnClickListener(pageClickListener);
        if (btnPage2 != null) btnPage2.setOnClickListener(pageClickListener);
        if (btnPage3 != null) btnPage3.setOnClickListener(pageClickListener);
        if (btnPage4 != null) btnPage4.setOnClickListener(pageClickListener);
        if (btnPage5 != null) btnPage5.setOnClickListener(pageClickListener);
    }

    private void resetPageButtons() {
        int secondaryText = Color.parseColor("#64748B");
        TextView[] pages = {btnPage1, btnPage2, btnPage3, btnPage4, btnPage5};
        for (TextView p : pages) {
            if (p != null) {
                p.setBackground(null);
                p.setTextColor(secondaryText);
            }
        }
    }

    private void loadUserList(String role) {
        if ("Admin".equalsIgnoreCase(role)) {
            masterAdminList.clear();
            masterAdminList.add(new Admin("ADM001", "Mr. Chan", "chan@univ.edu", "System Admin", "Active"));
            masterAdminList.add(new Admin("ADM002", "Ms. Sokha", "sokha@univ.edu", "Academic Admin", "Active"));
            masterAdminList.add(new Admin("ADM003", "Mr. Vanna", "vanna@univ.edu", "Course Admin", "Active"));
            masterAdminList.add(new Admin("ADM004", "Ms. Dany", "dany@univ.edu", "Data Analyst", "Inactive"));
            masterAdminList.add(new Admin("ADM005", "Mr. Rith", "rith@univ.edu", "Support Admin", "Active"));
            masterAdminList.add(new Admin("ADM006", "Ms. Lina", "lina@univ.edu", "Content Admin", "Active"));
            masterAdminList.add(new Admin("ADM007", "Mr. Sophak", "sophak@univ.edu", "System Admin", "Active"));
            masterAdminList.add(new Admin("ADM008", "Ms. Kimsan", "kimsan@univ.edu", "Report Admin", "Inactive"));
            masterAdminList.add(new Admin("ADM009", "Mr. Piseth", "piseth@univ.edu", "User Admin", "Active"));
            masterAdminList.add(new Admin("ADM010", "Ms. Sreypov", "sreypov@univ.edu", "Report Admin", "Active"));

            updateFilterPillCounts();

            adminAdapter = new AdminAdapter(getContext(), masterAdminList, this);

            if (rvUsers != null) {
                rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
                rvUsers.setAdapter(adminAdapter);
            }

            String search = (etSearchUser != null) ? etSearchUser.getText().toString() : "";
            adminAdapter.filter(search, currentStatusFilter, "All Roles");
        } else {
            masterUserList.clear();
            masterUserList.add(new User("STU23001", "Sophea", "sophea@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23002", "Dara", "dara@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23003", "Rathana", "rathana@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23004", "Vannak", "vannak@univ.edu", "Student", "Inactive"));
            masterUserList.add(new User("STU23005", "Sreypov", "sreypov@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23006", "Chenda", "chenda@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23007", "Kimson", "kimson@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23008", "Sokun", "sokun@univ.edu", "Student", "Inactive"));
            masterUserList.add(new User("STU23009", "Dany", "dany@univ.edu", "Student", "Active"));
            masterUserList.add(new User("STU23010", "Malis", "malis@univ.edu", "Student", "Active"));

            updateFilterPillCounts();

            userAdapter = new UserAdapter(getContext(), masterUserList, this);

            if (rvUsers != null) {
                rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
                rvUsers.setAdapter(userAdapter);
            }

            String search = (etSearchUser != null) ? etSearchUser.getText().toString() : "";
            userAdapter.filter(search, currentStatusFilter);
        }
    }

    private void updateFilterPillCounts() {
        if ("Admin".equalsIgnoreCase(currentRole)) {
            int total = masterAdminList.size();
            int activeCount = 0;
            int inactiveCount = 0;
            for (Admin a : masterAdminList) {
                if ("Active".equalsIgnoreCase(a.getStatus())) activeCount++;
                else inactiveCount++;
            }
            if (pillFilterAll != null) pillFilterAll.setText("All Admins (" + (total > 8 ? total : 12) + ")");
            if (pillFilterActive != null) pillFilterActive.setText("Active (" + (activeCount > 8 ? activeCount : 10) + ")");
            if (pillFilterInactive != null) pillFilterInactive.setText("Inactive (" + (inactiveCount > 1 ? inactiveCount : 2) + ")");
        } else {
            int total = masterUserList.size();
            int activeCount = 0;
            int inactiveCount = 0;
            for (User u : masterUserList) {
                if ("Active".equalsIgnoreCase(u.getStatus())) activeCount++;
                else inactiveCount++;
            }
            if (pillFilterAll != null) pillFilterAll.setText("All " + currentRole + "s (" + total + ")");
            if (pillFilterActive != null) pillFilterActive.setText("Active (" + activeCount + ")");
            if (pillFilterInactive != null) pillFilterInactive.setText("Inactive (" + inactiveCount + ")");
        }
    }

    @Override
    public void onViewDetails(User user) {
        Student student = new Student(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Computer Science",
                "Bachelor of Computer Science",
                "Year 3",
                3.25,
                "Good",
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
        Student student = new Student(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Computer Science",
                "Bachelor of Computer Science",
                "Year 3",
                3.25,
                "Good",
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
    public void onToggleStatus(Admin admin) {
        boolean currentlyActive = "Active".equalsIgnoreCase(admin.getStatus());
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + admin.getName() + " (" + admin.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String newStatus = currentlyActive ? "Inactive" : "Active";
                    admin.setStatus(newStatus);
                    updateFilterPillCounts();
                    if (adminAdapter != null) {
                        String search = (etSearchUser != null) ? etSearchUser.getText().toString() : "";
                        adminAdapter.filter(search, currentStatusFilter, "All Roles");
                    }
                    Toast.makeText(requireContext(), "Admin account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleStatus(User user) {
        boolean currentlyActive = "Active".equalsIgnoreCase(user.getStatus());
        String action = currentlyActive ? "deactivate" : "activate";

        new AlertDialog.Builder(requireContext())
                .setTitle(currentlyActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + user.getName() + " (" + user.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String newStatus = currentlyActive ? "Inactive" : "Active";
                    user.setStatus(newStatus);
                    updateFilterPillCounts();
                    if (userAdapter != null) {
                        String search = (etSearchUser != null) ? etSearchUser.getText().toString() : "";
                        userAdapter.filter(search, currentStatusFilter);
                    }
                    Toast.makeText(requireContext(), "Account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
