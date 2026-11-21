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
    private TextView tvValTeacherDept, tvValTeacherPosition, tvValTeacherHireDate, tvValTeacherWorkType, tvValTeacherOffice, tvValTeacherSpecialization, tvValTeacherBio;
    private TextView tvValTeacherAccountStatus, tvValTeacherCreatedAt, tvValTeacherLastLogin;
    private TextView tvValTeacherLoginId, tvValTeacherProfileStatus, tvValTeacherAccountLink;
    private MaterialButton btnDeactivateTeacherAccount, btnEditTeacher, btnRepairTeacherAccount;

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
        tvValTeacherSpecialization = findViewById(R.id.tvValTeacherSpecialization);
        tvValTeacherBio = findViewById(R.id.tvValTeacherBio);

        tvValTeacherLoginId = findViewById(R.id.tvValTeacherLoginId);
        tvValTeacherProfileStatus = findViewById(R.id.tvValTeacherProfileStatus);
        tvValTeacherAccountStatus = findViewById(R.id.tvValTeacherAccountStatus);
        tvValTeacherAccountLink = findViewById(R.id.tvValTeacherAccountLink);
        tvValTeacherCreatedAt = findViewById(R.id.tvValTeacherCreatedAt);
        tvValTeacherLastLogin = findViewById(R.id.tvValTeacherLastLogin);

        btnDeactivateTeacherAccount = findViewById(R.id.btnDeactivateTeacherAccount);
        btnEditTeacher = findViewById(R.id.btnEditTeacher);
        btnRepairTeacherAccount = findViewById(R.id.btnRepairTeacherAccount);
    }

    private String getTeacherIdentifier() {
        if (currentTeacher == null) return "1";
        if (currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0) {
            return String.valueOf(currentTeacher.getUserId());
        }
        return currentTeacher.getId() != null ? currentTeacher.getId() : "1";
    }

    private void fetchTeacherDetails() {
        Call<JsonObject> detailCall = (currentTeacher != null && currentTeacher.getDbId() != null && currentTeacher.getDbId() > 0)
                ? ApiClient.getApiService(this).getTeacher(String.valueOf(currentTeacher.getDbId()))
                : ApiClient.getApiService(this).getUser(getTeacherIdentifier());

        detailCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        // Both /teachers/{id} and /admin/users/{id} populate user/teacher fields safely:
                        if (data.has("user_id") && !data.get("user_id").isJsonNull()) {
                            currentTeacher.setUserId(data.get("user_id").getAsLong());
                            currentTeacher.setDbId(data.has("id") && !data.get("id").isJsonNull() ? data.get("id").getAsLong() : currentTeacher.getDbId());
                        } else if (data.has("id") && !data.get("id").isJsonNull()) {
                            if (data.has("teacher_code") || data.has("teacher_id")) {
                                currentTeacher.setDbId(data.get("id").getAsLong());
                            } else {
                                currentTeacher.setUserId(data.get("id").getAsLong());
                            }
                        }

                        if (data.has("teacher_code") && !data.get("teacher_code").isJsonNull()) {
                            String tc = data.get("teacher_code").getAsString();
                            if (!tc.isEmpty()) currentTeacher.setId(tc);
                        }
                        if (data.has("login_id") && !data.get("login_id").isJsonNull()) {
                            currentTeacher.setLoginId(data.get("login_id").getAsString());
                        }
                        if (data.has("teacher_profile_status") && !data.get("teacher_profile_status").isJsonNull()) {
                            currentTeacher.setProfileStatus(capitalize(data.get("teacher_profile_status").getAsString()));
                        }
                        if (data.has("login_account_status") && !data.get("login_account_status").isJsonNull()) {
                            currentTeacher.setLoginAccountStatus(data.get("login_account_status").getAsString());
                        }
                        if (data.has("account_created") && !data.get("account_created").isJsonNull()) {
                            currentTeacher.setAccountCreated(data.get("account_created").getAsBoolean());
                        }
                        if (data.has("account_linked") && !data.get("account_linked").isJsonNull()) {
                            currentTeacher.setAccountLinked(data.get("account_linked").getAsBoolean());
                        }
                        if (data.has("can_login") && !data.get("can_login").isJsonNull()) {
                            currentTeacher.setCanLogin(data.get("can_login").getAsBoolean());
                        }

                        String khmerName = (data.has("khmer_name") && !data.get("khmer_name").isJsonNull())
                                ? data.get("khmer_name").getAsString() : "";
                        String englishName = (data.has("english_name") && !data.get("english_name").isJsonNull())
                                ? data.get("english_name").getAsString() : "";
                        String rawName = (data.has("name") && !data.get("name").isJsonNull())
                                ? data.get("name").getAsString() : currentTeacher.getName();
                        currentTeacher.setKhmerName(khmerName);
                        currentTeacher.setEnglishName(englishName);
                        currentTeacher.setName(com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper.resolveDisplayName(
                                englishName, khmerName, rawName));
                        if (data.has("hire_date") && !data.get("hire_date").isJsonNull()) {
                            currentTeacher.setHireDate(data.get("hire_date").getAsString());
                        }
                        if (data.has("email") && !data.get("email").isJsonNull()) {
                            currentTeacher.setEmail(data.get("email").getAsString());
                        } else if (data.has("user") && data.get("user").isJsonObject()) {
                            JsonObject uObj = data.getAsJsonObject("user");
                            currentTeacher.setEmail(uObj.has("email") && !uObj.get("email").isJsonNull()
                                    ? uObj.get("email").getAsString() : "");
                        } else {
                            currentTeacher.setEmail("");
                        }
                        if (data.has("department_name") && !data.get("department_name").isJsonNull() && !data.get("department_name").getAsString().isEmpty()) {
                            currentTeacher.setDepartment(data.get("department_name").getAsString());
                        } else if (data.has("department") && data.get("department").isJsonObject()) {
                            JsonObject dObj = data.getAsJsonObject("department");
                            if (dObj.has("name") && !dObj.get("name").isJsonNull()) {
                                currentTeacher.setDepartment(dObj.get("name").getAsString());
                            }
                        }
                        if (data.has("position") && !data.get("position").isJsonNull()) {
                            currentTeacher.setPosition(data.get("position").getAsString());
                        }
                        if (data.has("gender") && !data.get("gender").isJsonNull()) {
                            currentTeacher.setGender(data.get("gender").getAsString());
                        }
                        if (data.has("phone") && !data.get("phone").isJsonNull()) {
                            currentTeacher.setPhone(data.get("phone").getAsString());
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
                        if (data.has("office_location_id") && !data.get("office_location_id").isJsonNull()) {
                            currentTeacher.setOfficeLocationId(data.get("office_location_id").getAsInt());
                        } else {
                            currentTeacher.setOfficeLocationId(null);
                        }
                        if (data.has("office_location") && !data.get("office_location").isJsonNull()) {
                            currentTeacher.setOfficeLocation(data.get("office_location").getAsString());
                        }
                        if (data.has("work_type") && !data.get("work_type").isJsonNull()) {
                            currentTeacher.setWorkType(data.get("work_type").getAsString());
                        }
                        if (data.has("biography") && !data.get("biography").isJsonNull()) {
                            currentTeacher.setBio(data.get("biography").getAsString());
                        } else if (data.has("bio") && !data.get("bio").isJsonNull()) {
                            currentTeacher.setBio(data.get("bio").getAsString());
                        }
                        if (data.has("specializations") && data.get("specializations").isJsonArray()) {
                            java.util.List<String> specs = new java.util.ArrayList<>();
                            for (JsonElement se : data.getAsJsonArray("specializations")) {
                                if (!se.isJsonNull()) specs.add(se.getAsString());
                            }
                            currentTeacher.setSpecializations(specs);
                        } else if (data.has("specialization") && !data.get("specialization").isJsonNull()) {
                            currentTeacher.setSpecialization(data.get("specialization").getAsString());
                        }

                        JsonObject nestedSource = (data.has("teacher") && data.get("teacher").isJsonObject())
                                ? data.getAsJsonObject("teacher")
                                : data;

                        if (nestedSource != null) {
                            if (nestedSource.has("id") && !nestedSource.get("id").isJsonNull()) {
                                currentTeacher.setDbId(nestedSource.get("id").getAsLong());
                            }
                            if (nestedSource.has("khmer_name") && !nestedSource.get("khmer_name").isJsonNull()
                                    && !nestedSource.get("khmer_name").getAsString().trim().isEmpty()) {
                                currentTeacher.setKhmerName(nestedSource.get("khmer_name").getAsString());
                            }
                            if (nestedSource.has("english_name") && !nestedSource.get("english_name").isJsonNull()
                                    && !nestedSource.get("english_name").getAsString().trim().isEmpty()) {
                                currentTeacher.setEnglishName(nestedSource.get("english_name").getAsString());
                            }
                            currentTeacher.setName(com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper.resolveDisplayName(
                                    currentTeacher.getEnglishName(), currentTeacher.getKhmerName(), currentTeacher.getName()));
                            if (nestedSource.has("hire_date") && !nestedSource.get("hire_date").isJsonNull()) {
                                currentTeacher.setHireDate(nestedSource.get("hire_date").getAsString());
                            }
                            if (nestedSource.has("department_name") && !nestedSource.get("department_name").isJsonNull() && !nestedSource.get("department_name").getAsString().isEmpty()) {
                                currentTeacher.setDepartment(nestedSource.get("department_name").getAsString());
                            } else if (nestedSource.has("department") && nestedSource.get("department").isJsonPrimitive()) {
                                currentTeacher.setDepartment(nestedSource.get("department").getAsString());
                            }
                            if (nestedSource.has("position") && !nestedSource.get("position").isJsonNull()) {
                                currentTeacher.setPosition(nestedSource.get("position").getAsString());
                            }
                            if (nestedSource.has("work_type") && !nestedSource.get("work_type").isJsonNull()) {
                                currentTeacher.setWorkType(nestedSource.get("work_type").getAsString());
                            }
                            if (nestedSource.has("office_location_id") && !nestedSource.get("office_location_id").isJsonNull()) {
                                currentTeacher.setOfficeLocationId(nestedSource.get("office_location_id").getAsInt());
                            }
                            if (nestedSource.has("office_location") && !nestedSource.get("office_location").isJsonNull()) {
                                currentTeacher.setOfficeLocation(nestedSource.get("office_location").getAsString());
                            }
                            if (nestedSource.has("biography") && !nestedSource.get("biography").isJsonNull()) {
                                currentTeacher.setBio(nestedSource.get("biography").getAsString());
                            } else if (nestedSource.has("bio") && !nestedSource.get("bio").isJsonNull()) {
                                currentTeacher.setBio(nestedSource.get("bio").getAsString());
                            }
                            if (nestedSource.has("specializations") && nestedSource.get("specializations").isJsonArray()) {
                                java.util.List<String> specs = new java.util.ArrayList<>();
                                for (JsonElement se : nestedSource.getAsJsonArray("specializations")) {
                                    if (!se.isJsonNull()) specs.add(se.getAsString());
                                }
                                currentTeacher.setSpecializations(specs);
                            } else if (nestedSource.has("specialization") && !nestedSource.get("specialization").isJsonNull()) {
                                currentTeacher.setSpecialization(nestedSource.get("specialization").getAsString());
                            }

                            // Courses summary
                            if (nestedSource.has("courses") && nestedSource.get("courses").isJsonArray()) {
                                JsonArray coursesArr = nestedSource.getAsJsonArray("courses");
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
                            if (nestedSource.has("classes") && nestedSource.get("classes").isJsonArray()) {
                                JsonArray classesArr = nestedSource.getAsJsonArray("classes");
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

        String engName = currentTeacher.getEnglishName();
        String khmName = currentTeacher.getKhmerName();
        String primaryName = com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper.resolveDisplayName(
                engName, khmName, currentTeacher.getName());
        String displayName = (!engName.isEmpty() && !khmName.isEmpty() && !khmName.equals(engName))
                ? engName + " (" + khmName + ")"
                : primaryName;
        String initial = !primaryName.isEmpty() ? primaryName.substring(0, 1).toUpperCase() : "T";

        if (tvDetailTeacherAvatar != null) tvDetailTeacherAvatar.setText(initial);
        if (tvDetailTeacherName != null) tvDetailTeacherName.setText(displayName);
        if (tvDetailTeacherId != null) tvDetailTeacherId.setText(currentTeacher.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentTeacher.getStatus());
        updateStatusDisplay(isActive);

        if (tvValTeacherFullName != null) tvValTeacherFullName.setText(displayName);
        if (tvValTeacherId != null) tvValTeacherId.setText(currentTeacher.getId());
        if (tvValTeacherEmail != null) {
            String em = currentTeacher.getEmail();
            tvValTeacherEmail.setText(em != null && !em.isEmpty() ? em : getString(R.string.login_via_id_format, currentTeacher.getId()));
        }
        if (tvValTeacherPhone != null) tvValTeacherPhone.setText(currentTeacher.getPhone());
        if (tvValTeacherGender != null) tvValTeacherGender.setText(currentTeacher.getGender());
        if (tvValTeacherDob != null) tvValTeacherDob.setText(currentTeacher.getDob());

        if (tvValTeacherDept != null) tvValTeacherDept.setText(currentTeacher.getDepartment());
        if (tvValTeacherPosition != null) tvValTeacherPosition.setText(currentTeacher.getPosition());
        if (tvValTeacherHireDate != null) tvValTeacherHireDate.setText(currentTeacher.getHireDate());
        if (tvValTeacherWorkType != null) tvValTeacherWorkType.setText(currentTeacher.getWorkType());
        if (tvValTeacherOffice != null) tvValTeacherOffice.setText(currentTeacher.getOfficeLocation());
        if (tvValTeacherSpecialization != null) tvValTeacherSpecialization.setText(currentTeacher.getSpecializationDisplay());
        if (tvValTeacherBio != null) tvValTeacherBio.setText(currentTeacher.getBio());

        if (tvValTeacherCreatedAt != null) tvValTeacherCreatedAt.setText(currentTeacher.getCreatedAt());
        if (tvValTeacherLastLogin != null) tvValTeacherLastLogin.setText(currentTeacher.getLastLogin());
    }

    private void updateStatusDisplay(boolean isActive) {
        String profileStatusText = capitalize(currentTeacher.getProfileStatus());
        if (profileStatusText.isEmpty()) profileStatusText = isActive ? "Active" : "Inactive";
        boolean profileActive = "Active".equalsIgnoreCase(profileStatusText);
        int green = Color.parseColor("#16A34A");
        int red = Color.parseColor("#EF4444");
        int amber = Color.parseColor("#D97706");

        if (tvDetailTeacherStatusBadge != null) {
            tvDetailTeacherStatusBadge.setText(profileStatusText);
            tvDetailTeacherStatusBadge.setTextColor(profileActive ? green : red);
            tvDetailTeacherStatusBadge.setBackgroundResource(profileActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (tvValTeacherLoginId != null) {
            tvValTeacherLoginId.setText(currentTeacher.getLoginId());
        }
        if (tvValTeacherProfileStatus != null) {
            tvValTeacherProfileStatus.setText(profileStatusText);
            tvValTeacherProfileStatus.setTextColor(profileActive ? green : red);
        }

        String rawLoginStatus = currentTeacher.getLoginAccountStatus();
        String loginLabel;
        int loginColor;
        if (!currentTeacher.isAccountCreated() || "not_created".equalsIgnoreCase(rawLoginStatus)) {
            loginLabel = getString(R.string.account_not_created);
            loginColor = red;
        } else if ("role_mismatch".equalsIgnoreCase(rawLoginStatus)) {
            loginLabel = getString(R.string.account_role_mismatch);
            loginColor = amber;
        } else if ("no_password".equalsIgnoreCase(rawLoginStatus)) {
            loginLabel = getString(R.string.account_no_password);
            loginColor = amber;
        } else if ("active".equalsIgnoreCase(rawLoginStatus)) {
            loginLabel = getString(R.string.account_created_active);
            loginColor = green;
        } else {
            loginLabel = getString(R.string.account_created_inactive);
            loginColor = red;
        }

        if (tvValTeacherAccountStatus != null) {
            tvValTeacherAccountStatus.setText(loginLabel);
            tvValTeacherAccountStatus.setTextColor(loginColor);
        }
        if (tvValTeacherAccountLink != null) {
            boolean linked = currentTeacher.isAccountLinked();
            tvValTeacherAccountLink.setText(linked ? R.string.account_linked_label : R.string.account_not_linked_label);
            tvValTeacherAccountLink.setTextColor(linked ? green : red);
        }
        if (btnRepairTeacherAccount != null) {
            boolean needsRepair = !currentTeacher.isAccountCreated() || !currentTeacher.isAccountLinked();
            btnRepairTeacherAccount.setVisibility(needsRepair && currentTeacher.getDbId() != null ? View.VISIBLE : View.GONE);
        }
        if (btnDeactivateTeacherAccount != null) {
            btnDeactivateTeacherAccount.setText(isActive ? "Deactivate Account" : "Activate Account");
            btnDeactivateTeacherAccount.setVisibility(currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0 ? View.VISIBLE : View.GONE);
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

        if (btnRepairTeacherAccount != null) {
            btnRepairTeacherAccount.setOnClickListener(v -> confirmProvisionOrRepairAccount());
        }
    }

    private void confirmProvisionOrRepairAccount() {
        if (currentTeacher == null || currentTeacher.getDbId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_repair_login_title)
                .setMessage(getString(R.string.confirm_repair_login_msg) + "\n\nLogin ID: " + currentTeacher.getLoginId())
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    JsonObject body = new JsonObject();
                    String email = currentTeacher.getEmail();
                    if (email != null && !email.trim().isEmpty()) {
                        body.addProperty("email", email.trim());
                    }
                    btnRepairTeacherAccount.setEnabled(false);
                    ApiClient.getApiService(AdminTeacherDetailActivity.this)
                            .provisionTeacherAccount(String.valueOf(currentTeacher.getDbId()), body)
                            .enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    btnRepairTeacherAccount.setEnabled(true);
                                    if (response.isSuccessful() && response.body() != null) {
                                        JsonObject res = response.body();
                                        StringBuilder msg = new StringBuilder();
                                        msg.append(res.has("message") ? res.get("message").getAsString() : "Login account ready.");
                                        if (res.has("login_id") && !res.get("login_id").isJsonNull()) {
                                            msg.append("\n\nLogin ID: ").append(res.get("login_id").getAsString());
                                        }
                                        if (res.has("initial_password") && !res.get("initial_password").isJsonNull()) {
                                            msg.append("\nInitial Password: ").append(res.get("initial_password").getAsString());
                                        }
                                        new AlertDialog.Builder(AdminTeacherDetailActivity.this)
                                                .setTitle("Teacher Login Account")
                                                .setMessage(msg.toString())
                                                .setPositiveButton(R.string.ok, null)
                                                .show();
                                        fetchTeacherDetails();
                                    } else {
                                        Toast.makeText(AdminTeacherDetailActivity.this,
                                                "Failed to provision account (HTTP " + response.code() + ")",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    btnRepairTeacherAccount.setEnabled(true);
                                    Toast.makeText(AdminTeacherDetailActivity.this,
                                            "Network error provisioning account: " + t.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
