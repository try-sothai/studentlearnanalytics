package com.social7.studentlearnanalytics.admin.users;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Admin;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminDetailActivity";
    private Admin currentAdmin;
    private String customNotes = "";

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

    @Override
    protected void onResume() {
        super.onResume();
        fetchAdminDetails();
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

    private String getAdminIdentifier() {
        if (currentAdmin == null) return "1";
        if (currentAdmin.getUserId() != null && currentAdmin.getUserId() > 0) {
            return String.valueOf(currentAdmin.getUserId());
        }
        return currentAdmin.getId() != null ? currentAdmin.getId() : "1";
    }

    private void fetchAdminDetails() {
        String identifier = getAdminIdentifier();
        ApiClient.getApiService(this).getUser(identifier).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        if (data.has("id") && !data.get("id").isJsonNull()) {
                            currentAdmin.setUserId(data.get("id").getAsLong());
                        }
                        if (data.has("name") && !data.get("name").isJsonNull()) {
                            currentAdmin.setName(data.get("name").getAsString());
                        }
                        if (data.has("email") && !data.get("email").isJsonNull()) {
                            currentAdmin.setEmail(data.get("email").getAsString());
                        }
                        if (data.has("phone") && !data.get("phone").isJsonNull()) {
                            currentAdmin.setPhone(data.get("phone").getAsString());
                        }
                        if (data.has("gender") && !data.get("gender").isJsonNull()) {
                            currentAdmin.setGender(data.get("gender").getAsString());
                        }
                        if (data.has("date_of_birth") && !data.get("date_of_birth").isJsonNull()) {
                            currentAdmin.setDob(data.get("date_of_birth").getAsString());
                        }
                        if (data.has("status") && !data.get("status").isJsonNull()) {
                            currentAdmin.setStatus(capitalize(data.get("status").getAsString()));
                        }
                        if (data.has("created_at") && !data.get("created_at").isJsonNull()) {
                            currentAdmin.setCreatedAt(data.get("created_at").getAsString());
                        }
                        if (data.has("last_login_at") && !data.get("last_login_at").isJsonNull()) {
                            currentAdmin.setLastLogin(data.get("last_login_at").getAsString());
                        }

                        if (data.has("admin_profile") && data.get("admin_profile").isJsonObject()) {
                            JsonObject ap = data.getAsJsonObject("admin_profile");
                            if (ap.has("role_title") && !ap.get("role_title").isJsonNull()) {
                                currentAdmin.setRole(ap.get("role_title").getAsString());
                            }
                            if (ap.has("access_level") && !ap.get("access_level").isJsonNull()) {
                                currentAdmin.setAccessLevel(ap.get("access_level").getAsString());
                            }
                            if (ap.has("managed_modules") && !ap.get("managed_modules").isJsonNull()) {
                                currentAdmin.setManagedModules(ap.get("managed_modules").getAsString());
                            }
                            if (ap.has("description") && !ap.get("description").isJsonNull()) {
                                currentAdmin.setDescription(ap.get("description").getAsString());
                            }
                        }

                        bindData();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing admin details", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load admin details", t);
            }
        });
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
        String nextStatus = isActive ? "inactive" : "active";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentAdmin.getName() + " (" + currentAdmin.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String identifier = getAdminIdentifier();
                    JsonObject statusBody = new JsonObject();
                    statusBody.addProperty("status", nextStatus);

                    ApiClient.getApiService(AdminDetailActivity.this)
                            .updateUserStatus(identifier, statusBody)
                            .enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful()) {
                                        boolean newActiveState = !isActive;
                                        currentAdmin.setStatus(newActiveState ? "Active" : "Inactive");
                                        updateStatusDisplay(newActiveState);
                                        Toast.makeText(AdminDetailActivity.this, "Admin account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminDetailActivity.this, "Failed to update account status (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Toast.makeText(AdminDetailActivity.this, "Network error updating status: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
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
            if (tvOtherTabContent != null) {
                tvOtherTabContent.setText("• User Management: " + currentAdmin.getAccessLevel() + "\n"
                        + "• Course Management: " + currentAdmin.getAccessLevel() + "\n"
                        + "• Reports & Analytics: " + currentAdmin.getAccessLevel() + "\n"
                        + "• System Settings: Full Access\n"
                        + "• ML Model Evaluation: Read-only");
            }
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabActivityLog);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Recent Activity Log");
            if (tvOtherTabContent != null) {
                tvOtherTabContent.setText("• Created at: " + currentAdmin.getCreatedAt() + "\n"
                        + "• Last active: " + currentAdmin.getLastLogin() + "\n"
                        + "• Status: " + currentAdmin.getStatus());
            }
        } else if (index == 3) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Administrative Notes (Tap to edit)");
            displayNotes();
            if (containerOtherTabs != null) {
                containerOtherTabs.setOnClickListener(v -> showEditNotesDialog());
            }
        }
    }

    private void displayNotes() {
        if (tvOtherTabContent == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("• Assigned Role: ").append(currentAdmin.getRole()).append("\n");
        sb.append("• Access Level: ").append(currentAdmin.getAccessLevel()).append("\n");
        sb.append("• Managed Modules: ").append(currentAdmin.getManagedModules()).append("\n");
        sb.append("• Status: ").append(currentAdmin.getStatus()).append("\n");
        if (customNotes != null && !customNotes.trim().isEmpty()) {
            sb.append("\nNotes:\n").append(customNotes.trim());
        }
        tvOtherTabContent.setText(sb.toString());
    }

    private void showEditNotesDialog() {
        final EditText input = new EditText(this);
        input.setText(customNotes);
        input.setHint("Enter administrative note for admin...");

        new AlertDialog.Builder(this)
                .setTitle("Admin Administrative Notes")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    customNotes = input.getText().toString().trim();
                    displayNotes();
                    Toast.makeText(this, "Notes updated", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    customNotes = "";
                    displayNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
