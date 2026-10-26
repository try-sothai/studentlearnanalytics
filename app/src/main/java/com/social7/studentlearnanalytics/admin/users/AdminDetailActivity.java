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
import com.social7.studentlearnanalytics.model.Admin;

public class AdminDetailActivity extends AppCompatActivity {

    private Admin currentAdmin;

    private TextView tabOverview, tabPermissions, tabActivityLog, tabNotes;
    private LinearLayout containerOverview, containerOtherTabs;
    private TextView tvOtherTabTitle, tvOtherTabContent;

    private TextView tvDetailAdminAvatar, tvDetailAdminName, tvDetailAdminId, tvDetailAdminStatusBadge;
    private TextView tvValAdminFullName, tvValAdminId, tvValAdminEmail, tvValAdminPhone, tvValAdminGender, tvValAdminDob;
    private TextView tvValAdminRole, tvValAdminAccessLevel, tvValAdminModules, tvValAdminDesc;
    private TextView tvValAdminAccountStatus, tvValAdminCreatedAt, tvValAdminLastLogin;
    private MaterialButton btnDeactivateAdminAccount, btnEditAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detail);

        if (getIntent() != null && getIntent().hasExtra("admin")) {
            currentAdmin = (Admin) getIntent().getSerializableExtra("admin");
        }

        if (currentAdmin == null) {
            currentAdmin = new Admin("ADM001", "Mr. Chan", "chan@univ.edu", "System Administrator", "Active");
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAdminDetail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvDetailAdminAvatar = findViewById(R.id.tvDetailAdminAvatar);
        tvDetailAdminName = findViewById(R.id.tvDetailAdminName);
        tvDetailAdminId = findViewById(R.id.tvDetailAdminId);
        tvDetailAdminStatusBadge = findViewById(R.id.tvDetailAdminStatusBadge);

        tabOverview = findViewById(R.id.tabAdminOverview);
        tabPermissions = findViewById(R.id.tabAdminPermissions);
        tabActivityLog = findViewById(R.id.tabAdminActivityLog);
        tabNotes = findViewById(R.id.tabAdminNotes);

        containerOverview = findViewById(R.id.containerAdminOverview);
        containerOtherTabs = findViewById(R.id.containerAdminOtherTabs);
        tvOtherTabTitle = findViewById(R.id.tvAdminOtherTabTitle);
        tvOtherTabContent = findViewById(R.id.tvAdminOtherTabContent);

        tvValAdminFullName = findViewById(R.id.tvValAdminFullName);
        tvValAdminId = findViewById(R.id.tvValAdminId);
        tvValAdminEmail = findViewById(R.id.tvValAdminEmail);
        tvValAdminPhone = findViewById(R.id.tvValAdminPhone);
        tvValAdminGender = findViewById(R.id.tvValAdminGender);
        tvValAdminDob = findViewById(R.id.tvValAdminDob);

        tvValAdminRole = findViewById(R.id.tvValAdminRole);
        tvValAdminAccessLevel = findViewById(R.id.tvValAdminAccessLevel);
        tvValAdminModules = findViewById(R.id.tvValAdminModules);
        tvValAdminDesc = findViewById(R.id.tvValAdminDesc);

        tvValAdminAccountStatus = findViewById(R.id.tvValAdminAccountStatus);
        tvValAdminCreatedAt = findViewById(R.id.tvValAdminCreatedAt);
        tvValAdminLastLogin = findViewById(R.id.tvValAdminLastLogin);

        btnDeactivateAdminAccount = findViewById(R.id.btnDeactivateAdminAccount);
        btnEditAdmin = findViewById(R.id.btnEditAdmin);
    }

    private void bindData() {
        if (currentAdmin == null) return;

        String name = currentAdmin.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "A";

        if (tvDetailAdminAvatar != null) tvDetailAdminAvatar.setText(initial);
        if (tvDetailAdminName != null) tvDetailAdminName.setText(name);
        if (tvDetailAdminId != null) tvDetailAdminId.setText(currentAdmin.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentAdmin.getStatus());
        updateStatusDisplay(isActive);

        if (tvValAdminFullName != null) tvValAdminFullName.setText(name);
        if (tvValAdminId != null) tvValAdminId.setText(currentAdmin.getId());
        if (tvValAdminEmail != null) tvValAdminEmail.setText(currentAdmin.getEmail());
        if (tvValAdminPhone != null) tvValAdminPhone.setText(currentAdmin.getPhone());
        if (tvValAdminGender != null) tvValAdminGender.setText(currentAdmin.getGender());
        if (tvValAdminDob != null) tvValAdminDob.setText(currentAdmin.getDob());

        if (tvValAdminRole != null) tvValAdminRole.setText(currentAdmin.getRole());
        if (tvValAdminAccessLevel != null) tvValAdminAccessLevel.setText(currentAdmin.getAccessLevel());
        if (tvValAdminModules != null) tvValAdminModules.setText(currentAdmin.getManagedModules());
        if (tvValAdminDesc != null) tvValAdminDesc.setText(currentAdmin.getDescription());

        if (tvValAdminCreatedAt != null) tvValAdminCreatedAt.setText(currentAdmin.getCreatedAt());
        if (tvValAdminLastLogin != null) tvValAdminLastLogin.setText(currentAdmin.getLastLogin());
    }

    private void updateStatusDisplay(boolean isActive) {
        String statusText = isActive ? "Active" : "Inactive";
        int color = Color.parseColor(isActive ? "#16A34A" : "#EF4444");

        if (tvDetailAdminStatusBadge != null) {
            tvDetailAdminStatusBadge.setText(statusText);
            tvDetailAdminStatusBadge.setTextColor(color);
            tvDetailAdminStatusBadge.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (tvValAdminAccountStatus != null) {
            tvValAdminAccountStatus.setText(statusText);
            tvValAdminAccountStatus.setTextColor(color);
        }
        if (btnDeactivateAdminAccount != null) {
            btnDeactivateAdminAccount.setText(isActive ? "Deactivate Account" : "Activate Account");
        }
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabPermissions != null) tabPermissions.setOnClickListener(v -> selectTab(1));
        if (tabActivityLog != null) tabActivityLog.setOnClickListener(v -> selectTab(2));
        if (tabNotes != null) tabNotes.setOnClickListener(v -> selectTab(3));

        View btnCardEditBasic = findViewById(R.id.btnAdminCardEditBasic);
        if (btnCardEditBasic != null) {
            btnCardEditBasic.setOnClickListener(v -> openEditScreen());
        }

        if (btnEditAdmin != null) {
            btnEditAdmin.setOnClickListener(v -> openEditScreen());
        }

        if (btnDeactivateAdminAccount != null) {
            btnDeactivateAdminAccount.setOnClickListener(v -> confirmToggleAccountStatus());
        }
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditAdminActivity.class);
        intent.putExtra("admin", currentAdmin);
        startActivity(intent);
    }

    private void confirmToggleAccountStatus() {
        boolean isActive = "Active".equalsIgnoreCase(currentAdmin.getStatus());
        String action = isActive ? "deactivate" : "activate";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentAdmin.getName() + " (" + currentAdmin.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    boolean newActiveState = !isActive;
                    currentAdmin.setStatus(newActiveState ? "Active" : "Inactive");
                    updateStatusDisplay(newActiveState);
                    Toast.makeText(AdminDetailActivity.this, "Admin account " + action + "d successfully", Toast.LENGTH_SHORT).show();
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
            setTabActive(tabPermissions);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Module Permissions Overview");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• User Management: " + currentAdmin.getAccessLevel() + "\n• Course Management: " + currentAdmin.getAccessLevel() + "\n• Reports & Analytics: " + currentAdmin.getAccessLevel() + "\n• System Settings: Full Access\n• ML Model Evaluation: Read-only");
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabActivityLog);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Recent Activity Log");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• May 29, 2024 09:45 AM: Signed in from Building A.\n• May 20, 2024 14:10 PM: Updated system configuration settings.\n• May 10, 2024 11:30 AM: Approved new course registration batch.");
        } else if (index == 3) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Administrative Notes");
            if (tvOtherTabContent != null) tvOtherTabContent.setText("• Sep 01, 2020: Assigned " + currentAdmin.getRole() + " role.\n• Jan 15, 2024: Annual administrative policy compliance verified.");
        }
    }

    private void resetTabs() {
        if (containerOverview != null) containerOverview.setVisibility(View.GONE);
        if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.GONE);

        setTabInactive(tabOverview);
        setTabInactive(tabPermissions);
        setTabInactive(tabActivityLog);
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
