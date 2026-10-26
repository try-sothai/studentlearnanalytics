package com.social7.studentlearnanalytics.admin.users;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.User;

public class AdminStudentDetailActivity extends AppCompatActivity {

    private Student currentStudent;
    private User currentUser;

    private TextView tabOverview, tabAcademic, tabAttendance, tabNotes;
    private LinearLayout containerOverview, containerOtherTabs;
    private TextView tvOtherTabTitle, tvOtherTabContent;

    private TextView tvStudentAvatar, tvDetailStudentName, tvDetailStudentId, tvDetailStatusPill;
    private TextView tvValFullName, tvValStudentId, tvValEmail, tvValPhone, tvValGender, tvValDob;
    private TextView tvValDept, tvValProgram, tvValYear, tvValAcademicYear, tvValEnrolledCourses;
    private TextView tvValAccountStatus, tvValCreatedAt, tvValLastLogin;
    private MaterialButton btnDeactivateAccount, btnEditStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_detail);

        if (getIntent() != null) {
            if (getIntent().hasExtra("student")) {
                currentStudent = (Student) getIntent().getSerializableExtra("student");
            }
            if (getIntent().hasExtra("user")) {
                currentUser = (User) getIntent().getSerializableExtra("user");
            }
        }

        if (currentStudent == null) {
            currentStudent = new Student(
                    "STU23001",
                    "Sophea",
                    "sophea@univ.edu",
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
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackStudentDetail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvStudentAvatar = findViewById(R.id.tvStudentAvatar);
        tvDetailStudentName = findViewById(R.id.tvDetailStudentName);
        tvDetailStudentId = findViewById(R.id.tvDetailStudentId);
        tvDetailStatusPill = findViewById(R.id.tvDetailStatusPill);

        tabOverview = findViewById(R.id.tabOverview);
        tabAcademic = findViewById(R.id.tabAcademic);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabNotes = findViewById(R.id.tabNotes);

        containerOverview = findViewById(R.id.containerOverview);
        containerOtherTabs = findViewById(R.id.containerOtherTabs);
        tvOtherTabTitle = findViewById(R.id.tvOtherTabTitle);
        tvOtherTabContent = findViewById(R.id.tvOtherTabContent);

        tvValFullName = findViewById(R.id.tvValFullName);
        tvValStudentId = findViewById(R.id.tvValStudentId);
        tvValEmail = findViewById(R.id.tvValEmail);
        tvValPhone = findViewById(R.id.tvValPhone);
        tvValGender = findViewById(R.id.tvValGender);
        tvValDob = findViewById(R.id.tvValDob);

        tvValDept = findViewById(R.id.tvValDept);
        tvValProgram = findViewById(R.id.tvValProgram);
        tvValYear = findViewById(R.id.tvValYear);
        tvValAcademicYear = findViewById(R.id.tvValAcademicYear);
        tvValEnrolledCourses = findViewById(R.id.tvValEnrolledCourses);

        tvValAccountStatus = findViewById(R.id.tvValAccountStatus);
        tvValCreatedAt = findViewById(R.id.tvValCreatedAt);
        tvValLastLogin = findViewById(R.id.tvValLastLogin);

        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);
        btnEditStudent = findViewById(R.id.btnEditStudent);
    }

    private void bindData() {
        if (currentStudent == null) return;

        String name = currentStudent.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "S";

        if (tvStudentAvatar != null) tvStudentAvatar.setText(initial);
        if (tvDetailStudentName != null) tvDetailStudentName.setText(name);
        if (tvDetailStudentId != null) tvDetailStudentId.setText(currentStudent.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus());
        updateStatusDisplay(isActive);

        if (tvValFullName != null) tvValFullName.setText(name);
        if (tvValStudentId != null) tvValStudentId.setText(currentStudent.getId());
        if (tvValEmail != null) tvValEmail.setText(currentStudent.getEmail());
        if (tvValPhone != null) tvValPhone.setText(currentStudent.getPhone());
        if (tvValGender != null) tvValGender.setText(currentStudent.getGender());
        if (tvValDob != null) tvValDob.setText(currentStudent.getDob());

        if (tvValDept != null) tvValDept.setText(currentStudent.getDepartment());
        if (tvValProgram != null) tvValProgram.setText(currentStudent.getProgram());
        if (tvValYear != null) tvValYear.setText(currentStudent.getYear());
        if (tvValAcademicYear != null) tvValAcademicYear.setText(currentStudent.getAcademicYear());
        if (tvValEnrolledCourses != null) tvValEnrolledCourses.setText(currentStudent.getEnrolledCoursesCount());

        if (tvValCreatedAt != null) tvValCreatedAt.setText(currentStudent.getCreatedAt());
        if (tvValLastLogin != null) tvValLastLogin.setText(currentStudent.getLastLogin());
    }

    private void updateStatusDisplay(boolean isActive) {
        String statusText = isActive ? "Active" : "Inactive";
        int color = Color.parseColor(isActive ? "#16A34A" : "#EF4444");

        if (tvDetailStatusPill != null) {
            tvDetailStatusPill.setText(statusText);
            tvDetailStatusPill.setTextColor(color);
            tvDetailStatusPill.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (tvValAccountStatus != null) {
            tvValAccountStatus.setText(statusText);
            tvValAccountStatus.setTextColor(color);
        }
        if (btnDeactivateAccount != null) {
            btnDeactivateAccount.setText(isActive ? "Deactivate Account" : "Activate Account");
        }
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabAcademic != null) tabAcademic.setOnClickListener(v -> selectTab(1));
        if (tabAttendance != null) tabAttendance.setOnClickListener(v -> selectTab(2));
        if (tabNotes != null) tabNotes.setOnClickListener(v -> selectTab(3));

        View btnCardEditBasic = findViewById(R.id.btnCardEditBasic);
        if (btnCardEditBasic != null) {
            btnCardEditBasic.setOnClickListener(v -> openEditScreen());
        }

        if (btnEditStudent != null) {
            btnEditStudent.setOnClickListener(v -> openEditScreen());
        }

        if (btnDeactivateAccount != null) {
            btnDeactivateAccount.setOnClickListener(v -> confirmToggleAccountStatus());
        }
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("student", currentStudent);
        intent.putExtra("user", currentUser);
        startActivity(intent);
    }

    private void confirmToggleAccountStatus() {
        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus());
        String action = isActive ? "deactivate" : "activate";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentStudent.getName() + " (" + currentStudent.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    boolean newActiveState = !isActive;
                    currentStudent.setStatus(newActiveState ? "Active" : "Inactive");
                    if (currentUser != null) {
                        currentUser.setStatus(currentStudent.getStatus());
                    }
                    updateStatusDisplay(newActiveState);
                    Toast.makeText(AdminStudentDetailActivity.this, "Account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectTab(int index) {
        resetTabs();
        if (index == 0) {
            if (containerOverview != null) containerOverview.setVisibility(View.VISIBLE);
            setTabActive(tabOverview);
        } else if (index == 1) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabAcademic);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Academic Information & Courses");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("Department: " + currentStudent.getDepartment() + "\nProgram: " + currentStudent.getProgram() + "\nYear Level: " + currentStudent.getYear() + "\nAcademic Year: " + currentStudent.getAcademicYear() + "\nEnrolled Courses: " + currentStudent.getEnrolledCoursesCount());
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabAttendance);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Attendance Records");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("Overall Attendance Rate: " + currentStudent.getAttendanceRate() + "%\nTotal Classes Held: 42\nAttended Classes: 37\nAbsences: 5");
        } else if (index == 3) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Admin & Advisor Notes");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• Sep 15, 2024: Student registered for Fall 2024 semester.\n• Oct 02, 2024: Academic advisement session completed with advisor " + currentStudent.getAdvisor() + ".");
        }
    }

    private void resetTabs() {
        if (containerOverview != null) containerOverview.setVisibility(View.GONE);
        if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.GONE);

        setTabInactive(tabOverview);
        setTabInactive(tabAcademic);
        setTabInactive(tabAttendance);
        setTabInactive(tabNotes);
    }

    private void setTabActive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
    }

    private void setTabInactive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setBackground(null);
    }
}
