package com.social7.studentlearnanalytics.admin.teachers;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Teacher;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTeacherDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminTeacherDetail";
    private Teacher currentTeacher;
    private String customNotes = "";

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

    @Override
    protected void onResume() {
        super.onResume();
        fetchTeacherDetails();
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

    private String getTeacherIdentifier() {
        if (currentTeacher == null) return "1";
        if (currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0) {
            return String.valueOf(currentTeacher.getUserId());
        }
        return currentTeacher.getId() != null ? currentTeacher.getId() : "1";
    }

    private void fetchTeacherDetails() {
        String identifier = getTeacherIdentifier();
        ApiClient.getApiService(this).getUser(identifier).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        if (data.has("id") && !data.get("id").isJsonNull()) {
                            currentTeacher.setUserId(data.get("id").getAsLong());
                        }
                        if (data.has("name") && !data.get("name").isJsonNull()) {
                            currentTeacher.setName(data.get("name").getAsString());
                        }
                        if (data.has("email") && !data.get("email").isJsonNull()) {
                            currentTeacher.setEmail(data.get("email").getAsString());
                        }
                        if (data.has("phone") && !data.get("phone").isJsonNull()) {
                            currentTeacher.setPhone(data.get("phone").getAsString());
                        }
                        if (data.has("gender") && !data.get("gender").isJsonNull()) {
                            currentTeacher.setGender(data.get("gender").getAsString());
                        }
                        if (data.has("date_of_birth") && !data.get("date_of_birth").isJsonNull()) {
                            currentTeacher.setDob(data.get("date_of_birth").getAsString());
                        }
                        if (data.has("status") && !data.get("status").isJsonNull()) {
                            currentTeacher.setStatus(capitalize(data.get("status").getAsString()));
                        }
                        if (data.has("created_at") && !data.get("created_at").isJsonNull()) {
                            currentTeacher.setCreatedAt(data.get("created_at").getAsString());
                        }
                        if (data.has("last_login_at") && !data.get("last_login_at").isJsonNull()) {
                            currentTeacher.setLastLogin(data.get("last_login_at").getAsString());
                        }

                        if (data.has("teacher") && data.get("teacher").isJsonObject()) {
                            JsonObject tObj = data.getAsJsonObject("teacher");
                            if (tObj.has("id") && !tObj.get("id").isJsonNull()) {
                                currentTeacher.setDbId(tObj.get("id").getAsLong());
                            }
                            if (tObj.has("department_name") && !tObj.get("department_name").isJsonNull()) {
                                currentTeacher.setDepartment(tObj.get("department_name").getAsString());
                            } else if (tObj.has("department") && !tObj.get("department").isJsonNull()) {
                                currentTeacher.setDepartment(tObj.get("department").getAsString());
                            }
                            if (tObj.has("position") && !tObj.get("position").isJsonNull()) {
                                currentTeacher.setPosition(tObj.get("position").getAsString());
                            }
                            if (tObj.has("work_type") && !tObj.get("work_type").isJsonNull()) {
                                currentTeacher.setWorkType(tObj.get("work_type").getAsString());
                            }
                            if (tObj.has("office_location") && !tObj.get("office_location").isJsonNull()) {
                                currentTeacher.setOfficeLocation(tObj.get("office_location").getAsString());
                            }
                            if (tObj.has("bio") && !tObj.get("bio").isJsonNull()) {
                                currentTeacher.setBio(tObj.get("bio").getAsString());
                            }

                            // Courses summary
                            if (tObj.has("courses") && tObj.get("courses").isJsonArray()) {
                                JsonArray coursesArr = tObj.getAsJsonArray("courses");
                                StringBuilder sbCourses = new StringBuilder();
                                for (JsonElement ce : coursesArr) {
                                    if (ce.isJsonObject()) {
                                        JsonObject c = ce.getAsJsonObject();
                                        String cCode = c.has("code") ? c.get("code").getAsString() : "";
                                        String cName = c.has("name") ? c.get("name").getAsString() : "";
                                        sbCourses.append("• ").append(cCode).append(" ").append(cName).append("\n");
                                    }
                                }
                                if (sbCourses.length() > 0) {
                                    currentTeacher.setCoursesSummary(sbCourses.toString().trim());
                                }
                            }

                            // Classes summary
                            if (tObj.has("classes") && tObj.get("classes").isJsonArray()) {
                                JsonArray classesArr = tObj.getAsJsonArray("classes");
                                StringBuilder sbClasses = new StringBuilder();
                                for (JsonElement cl : classesArr) {
                                    if (cl.isJsonObject()) {
                                        JsonObject cls = cl.getAsJsonObject();
                                        String clsName = cls.has("name") ? cls.get("name").getAsString() : "";
                                        sbClasses.append("• ").append(clsName).append("\n");
                                    }
                                }
                                if (sbClasses.length() > 0) {
                                    currentTeacher.setClassesSummary(sbClasses.toString().trim());
                                }
                            }
                        }

                        bindData();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing teacher details", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load teacher details", t);
            }
        });
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
        String nextStatus = isActive ? "inactive" : "active";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentTeacher.getName() + " (" + currentTeacher.getId() + ")?")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String identifier = getTeacherIdentifier();
                    JsonObject statusBody = new JsonObject();
                    statusBody.addProperty("status", nextStatus);

                    ApiClient.getApiService(AdminTeacherDetailActivity.this)
                            .updateUserStatus(identifier, statusBody)
                            .enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful()) {
                                        boolean newActiveState = !isActive;
                                        currentTeacher.setStatus(newActiveState ? "Active" : "Inactive");
                                        updateStatusDisplay(newActiveState);
                                        Toast.makeText(AdminTeacherDetailActivity.this, getString(R.string.teacher_account) + action + "d successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminTeacherDetailActivity.this, "Failed to update account status (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Toast.makeText(AdminTeacherDetailActivity.this, "Network error updating status: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
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
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText(R.string.assigned_courses);
            String courses = currentTeacher.getCoursesSummary();
            if (courses == null || courses.trim().isEmpty()) {
                courses = "No active courses assigned yet to " + currentTeacher.getName() + ".";
            }
            if (tvOtherTabContent != null) tvOtherTabContent.setText(courses);
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabClasses);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText(R.string.assigned_classes);
            String classes = currentTeacher.getClassesSummary();
            if (classes == null || classes.trim().isEmpty()) {
                classes = "No active classes assigned yet to " + currentTeacher.getName() + ".";
            }
            if (tvOtherTabContent != null) tvOtherTabContent.setText(classes);
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
        sb.append("• Role: Faculty Member / ").append(currentTeacher.getPosition()).append("\n");
        sb.append("• Department: ").append(currentTeacher.getDepartment()).append("\n");
        sb.append("• Office: ").append(currentTeacher.getOfficeLocation()).append("\n");
        sb.append("• Status: ").append(currentTeacher.getStatus()).append("\n");
        if (customNotes != null && !customNotes.trim().isEmpty()) {
            sb.append("\nNotes:\n").append(customNotes.trim());
        }
        tvOtherTabContent.setText(sb.toString());
    }

    private void showEditNotesDialog() {
        final EditText input = new EditText(this);
        input.setText(customNotes);
        input.setHint("Enter administrative note for teacher...");

        new AlertDialog.Builder(this)
                .setTitle("Teacher Administrative Notes")
                .setView(input)
                .setPositiveButton(R.string.save, (d, w) -> {
                    customNotes = input.getText().toString().trim();
                    displayNotes();
                    Toast.makeText(this, "Notes updated", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton(R.string.clear, (d, w) -> {
                    customNotes = "";
                    displayNotes();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
