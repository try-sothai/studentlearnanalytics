package com.social7.studentlearnanalytics.admin.teachers;

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
import com.social7.studentlearnanalytics.model.Teacher;

public class AdminTeacherDetailActivity extends AppCompatActivity {

    private Teacher currentTeacher;

    private TextView tabOverview, tabCourses, tabClasses, tabNotes;
    private LinearLayout containerOverview, containerOtherTabs;
    private TextView tvOtherTabTitle, tvOtherTabContent;

    private TextView tvDetailTeacherAvatar, tvDetailTeacherName, tvDetailTeacherId, tvDetailTeacherStatusBadge;
    private TextView tvValTeacherFullName, tvValTeacherId, tvValTeacherEmail, tvValTeacherPhone, tvValTeacherGender, tvValTeacherDob;
    private TextView tvValTeacherDept, tvValTeacherPosition, tvValTeacherHireDate, tvValTeacherWorkType, tvValTeacherOffice, tvValTeacherBio;
    private TextView tvValTeacherAccountStatus, tvValTeacherCreatedAt, tvValTeacherLastLogin;
    private MaterialButton btnDeactivateTeacherAccount, btnEditTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_teacher_detail);

        if (getIntent() != null && getIntent().hasExtra("teacher")) {
            currentTeacher = (Teacher) getIntent().getSerializableExtra("teacher");
        }

        if (currentTeacher == null) {
            currentTeacher = new Teacher("TCH001", "Mr. Chan", "chan@univ.edu", "Computer Science", "Lecturer", "Active");
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackTeacherDetail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvDetailTeacherAvatar = findViewById(R.id.tvDetailTeacherAvatar);
        tvDetailTeacherName = findViewById(R.id.tvDetailTeacherName);
        tvDetailTeacherId = findViewById(R.id.tvDetailTeacherId);
        tvDetailTeacherStatusBadge = findViewById(R.id.tvDetailTeacherStatusBadge);

        tabOverview = findViewById(R.id.tabTeacherOverview);
        tabCourses = findViewById(R.id.tabTeacherCourses);
        tabClasses = findViewById(R.id.tabTeacherClasses);
        tabNotes = findViewById(R.id.tabTeacherNotes);

        containerOverview = findViewById(R.id.containerTeacherOverview);
        containerOtherTabs = findViewById(R.id.containerTeacherOtherTabs);
        tvOtherTabTitle = findViewById(R.id.tvTeacherOtherTabTitle);
        tvOtherTabContent = findViewById(R.id.tvTeacherOtherTabContent);

        tvValTeacherFullName = findViewById(R.id.tvValTeacherFullName);
        tvValTeacherId = findViewById(R.id.tvValTeacherId);
        tvValTeacherEmail = findViewById(R.id.tvValTeacherEmail);
        tvValTeacherPhone = findViewById(R.id.tvValTeacherPhone);
        tvValTeacherGender = findViewById(R.id.tvValTeacherGender);
        tvValTeacherDob = findViewById(R.id.tvValTeacherDob);

        tvValTeacherDept = findViewById(R.id.tvValTeacherDept);
        tvValTeacherPosition = findViewById(R.id.tvValTeacherPosition);
        tvValTeacherHireDate = findViewById(R.id.tvValTeacherHireDate);
        tvValTeacherWorkType = findViewById(R.id.tvValTeacherWorkType);
        tvValTeacherOffice = findViewById(R.id.tvValTeacherOffice);
        tvValTeacherBio = findViewById(R.id.tvValTeacherBio);

        tvValTeacherAccountStatus = findViewById(R.id.tvValTeacherAccountStatus);
        tvValTeacherCreatedAt = findViewById(R.id.tvValTeacherCreatedAt);
        tvValTeacherLastLogin = findViewById(R.id.tvValTeacherLastLogin);

        btnDeactivateTeacherAccount = findViewById(R.id.btnDeactivateTeacherAccount);
        btnEditTeacher = findViewById(R.id.btnEditTeacher);
    }

    private void bindData() {
        if (currentTeacher == null) return;

        String name = currentTeacher.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "T";

        if (tvDetailTeacherAvatar != null) tvDetailTeacherAvatar.setText(initial);
        if (tvDetailTeacherName != null) tvDetailTeacherName.setText(name);
        if (tvDetailTeacherId != null) tvDetailTeacherId.setText(currentTeacher.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentTeacher.getStatus());
        updateStatusDisplay(isActive);

        if (tvValTeacherFullName != null) tvValTeacherFullName.setText(name);
        if (tvValTeacherId != null) tvValTeacherId.setText(currentTeacher.getId());
        if (tvValTeacherEmail != null) tvValTeacherEmail.setText(currentTeacher.getEmail());
        if (tvValTeacherPhone != null) tvValTeacherPhone.setText(currentTeacher.getPhone());
        if (tvValTeacherGender != null) tvValTeacherGender.setText(currentTeacher.getGender());
        if (tvValTeacherDob != null) tvValTeacherDob.setText(currentTeacher.getDob());

        if (tvValTeacherDept != null) tvValTeacherDept.setText(currentTeacher.getDepartment());
        if (tvValTeacherPosition != null) tvValTeacherPosition.setText(currentTeacher.getPosition());
        if (tvValTeacherHireDate != null) tvValTeacherHireDate.setText(currentTeacher.getHireDate());
        if (tvValTeacherWorkType != null) tvValTeacherWorkType.setText(currentTeacher.getWorkType());
        if (tvValTeacherOffice != null) tvValTeacherOffice.setText(currentTeacher.getOfficeLocation());
        if (tvValTeacherBio != null) tvValTeacherBio.setText(currentTeacher.getBio());

        if (tvValTeacherCreatedAt != null) tvValTeacherCreatedAt.setText(currentTeacher.getCreatedAt());
        if (tvValTeacherLastLogin != null) tvValTeacherLastLogin.setText(currentTeacher.getLastLogin());
    }

    private void updateStatusDisplay(boolean isActive) {
        String statusText = isActive ? "Active" : "Inactive";
        int color = Color.parseColor(isActive ? "#16A34A" : "#EF4444");

        if (tvDetailTeacherStatusBadge != null) {
            tvDetailTeacherStatusBadge.setText(statusText);
            tvDetailTeacherStatusBadge.setTextColor(color);
            tvDetailTeacherStatusBadge.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (tvValTeacherAccountStatus != null) {
            tvValTeacherAccountStatus.setText(statusText);
            tvValTeacherAccountStatus.setTextColor(color);
        }
        if (btnDeactivateTeacherAccount != null) {
            btnDeactivateTeacherAccount.setText(isActive ? "Deactivate Account" : "Activate Account");
        }
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabCourses != null) tabCourses.setOnClickListener(v -> selectTab(1));
        if (tabClasses != null) tabClasses.setOnClickListener(v -> selectTab(2));
        if (tabNotes != null) tabNotes.setOnClickListener(v -> selectTab(3));

        View btnCardEditPersonal = findViewById(R.id.btnTeacherCardEditPersonal);
        if (btnCardEditPersonal != null) {
            btnCardEditPersonal.setOnClickListener(v -> openEditScreen());
        }

        if (btnEditTeacher != null) {
            btnEditTeacher.setOnClickListener(v -> openEditScreen());
        }

        if (btnDeactivateTeacherAccount != null) {
            btnDeactivateTeacherAccount.setOnClickListener(v -> confirmToggleAccountStatus());
        }
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditTeacherActivity.class);
        intent.putExtra("teacher", currentTeacher);
        startActivity(intent);
    }

    private void confirmToggleAccountStatus() {
        boolean isActive = "Active".equalsIgnoreCase(currentTeacher.getStatus());
        String action = isActive ? "deactivate" : "activate";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentTeacher.getName() + " (" + currentTeacher.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    boolean newActiveState = !isActive;
                    currentTeacher.setStatus(newActiveState ? "Active" : "Inactive");
                    updateStatusDisplay(newActiveState);
                    Toast.makeText(AdminTeacherDetailActivity.this, "Teacher account " + action + "d successfully", Toast.LENGTH_SHORT).show();
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
            setTabActive(tabCourses);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Assigned Courses");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• CS101 Data Structures (Section A)\n• CS202 Database Systems (Section B)\n• CS305 Web Development (Section C)");
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabClasses);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Assigned Classes");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• Class CS-Year3-A (45 Students)\n• Class CS-Year2-B (38 Students)");
        } else if (index == 3) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Administrative Notes");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• Sep 01, 2020: Hired as Lecturer in Department of " + currentTeacher.getDepartment() + ".\n• May 15, 2024: Annual performance evaluation completed successfully.");
        }
    }

    private void resetTabs() {
        if (containerOverview != null) containerOverview.setVisibility(View.GONE);
        if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.GONE);

        setTabInactive(tabOverview);
        setTabInactive(tabCourses);
        setTabInactive(tabClasses);
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
