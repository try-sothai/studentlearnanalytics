package com.social7.studentlearnanalytics.admin.users;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.User;
import com.social7.studentlearnanalytics.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStudentDetailActivity extends AppCompatActivity {

    private Student currentStudent;
    private User currentUser;
    private int currentTabIndex = 0;

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
            currentStudent = new Student();
            if (currentUser != null) {
                currentStudent.setUserId(currentUser.getNumericId());
                currentStudent.setId(currentUser.getCode());
                currentStudent.setName(currentUser.getName());
                currentStudent.setEmail(currentUser.getEmail());
                currentStudent.setStatus(currentUser.getStatus());
            }
        }

        initViews();
        bindData();
        setupListeners();
        fetchStudentDetailsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudentDetailsFromApi();
    }

    private String resolveLookupUserId() {
        if (currentUser != null && currentUser.getNumericId() != null && currentUser.getNumericId() > 0) {
            return String.valueOf(currentUser.getNumericId());
        }
        if (currentStudent != null && currentStudent.getUserId() != null && currentStudent.getUserId() > 0) {
            return String.valueOf(currentStudent.getUserId());
        }
        if (currentStudent != null && currentStudent.getId() != null && !currentStudent.getId().isEmpty()) {
            return currentStudent.getId();
        }
        if (currentUser != null && currentUser.getId() != null && !currentUser.getId().isEmpty()) {
            return currentUser.getId();
        }
        return "";
    }

    private void fetchStudentDetailsFromApi() {
        String lookupId = resolveLookupUserId();
        if (lookupId.isEmpty()) return;

        ApiClient.getApiService(this).getUserDetails(lookupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject d = response.body().getAsJsonObject("data");
                    populateStudentFromApi(d);
                    bindData();
                    selectTab(currentTabIndex);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                // Keep existing bound data if offline
            }
        });
    }

    private void populateStudentFromApi(JsonObject d) {
        if (currentStudent == null) currentStudent = new Student();

        if (d.has("id") && !d.get("id").isJsonNull()) {
            long uid = d.get("id").getAsLong();
            currentStudent.setUserId(uid);
            if (currentUser != null) currentUser.setNumericId(uid);
        }

        String code = getStr(d, "student_code", getStr(d, "code", currentStudent.getId()));
        currentStudent.setId(code);
        if (currentUser != null) currentUser.setCode(code);

        String khmerName = getStr(d, "khmer_name", "");
        String englishName = getStr(d, "english_name", getStr(d, "name", currentStudent.getName()));
        currentStudent.setKhmerName(khmerName);
        currentStudent.setEnglishName(englishName);
        currentStudent.setName(englishName);
        if (currentUser != null) currentUser.setName(englishName);

        String rawEmail = getStr(d, "raw_email", "");
        if (rawEmail.isEmpty()) {
            String emailVal = getStr(d, "email", "");
            if (emailVal.contains("@")) rawEmail = emailVal;
        }
        currentStudent.setEmail(rawEmail);
        if (currentUser != null) currentUser.setEmail(rawEmail);

        currentStudent.setGender(getStr(d, "gender", currentStudent.getGender()));
        currentStudent.setDob(getStr(d, "date_of_birth", currentStudent.getDob()));
        currentStudent.setPlaceOfBirth(getStr(d, "place_of_birth", ""));
        currentStudent.setAddress(getStr(d, "current_address", getStr(d, "address", "")));

        if (d.has("department_id") && !d.get("department_id").isJsonNull()) {
            currentStudent.setDepartmentId(d.get("department_id").getAsInt());
        }
        currentStudent.setDepartment(getStr(d, "department_name", currentStudent.getDepartment()));
        currentStudent.setDepartmentCode(getStr(d, "department_code", ""));

        if (d.has("program_id") && !d.get("program_id").isJsonNull()) {
            currentStudent.setProgramId(d.get("program_id").getAsInt());
        }
        currentStudent.setProgram(getStr(d, "program_name", currentStudent.getProgram()));
        currentStudent.setProgramCode(getStr(d, "program_code", ""));

        String yl = getStr(d, "year_level", "");
        if (!yl.isEmpty()) {
            try {
                int yInt = Integer.parseInt(yl.replaceAll("\\D+", ""));
                currentStudent.setYearLevel(yInt);
                currentStudent.setYear("Year " + yInt);
            } catch (Exception e) {
                currentStudent.setYear(yl);
            }
        }

        if (d.has("academic_year_id") && !d.get("academic_year_id").isJsonNull()) {
            currentStudent.setAcademicYearId(d.get("academic_year_id").getAsInt());
        }
        currentStudent.setAcademicYear(getStr(d, "academic_year_name", currentStudent.getAcademicYear()));
        currentStudent.setClassName(getStr(d, "class_name", ""));

        String statusVal = getStr(d, "status", "active");
        String displayStatus = ("active".equalsIgnoreCase(statusVal) || "enrolled".equalsIgnoreCase(statusVal)) ? "Active" : "Inactive";
        currentStudent.setStatus(displayStatus);
        if (currentUser != null) currentUser.setStatus(displayStatus);

        String createdAt = getStr(d, "created_at", "");
        if (createdAt.length() >= 10) createdAt = createdAt.substring(0, 10);
        if (!createdAt.isEmpty()) currentStudent.setCreatedAt(createdAt);

        String updatedAt = getStr(d, "updated_at", "");
        if (updatedAt.length() >= 10) updatedAt = updatedAt.substring(0, 10);
        if (!updatedAt.isEmpty()) currentStudent.setLastLogin(updatedAt);

        // Family Information
        if (d.has("family_information") && d.get("family_information").isJsonObject()) {
            JsonObject fam = d.getAsJsonObject("family_information");
            currentStudent.setFatherKhmerName(getStr(fam, "father_khmer_name", ""));
            currentStudent.setFatherEnglishName(getStr(fam, "father_english_name", ""));
            currentStudent.setFatherPhone(getStr(fam, "father_phone", ""));
            currentStudent.setMotherKhmerName(getStr(fam, "mother_khmer_name", ""));
            currentStudent.setMotherEnglishName(getStr(fam, "mother_english_name", ""));
            currentStudent.setMotherPhone(getStr(fam, "mother_phone", ""));
        }

        // Emergency Contact
        if (d.has("emergency_contact") && d.get("emergency_contact").isJsonObject()) {
            JsonObject em = d.getAsJsonObject("emergency_contact");
            currentStudent.setEmergencyName(getStr(em, "name", ""));
            currentStudent.setEmergencyRelationship(getStr(em, "relationship", ""));
            currentStudent.setEmergencyPhone(getStr(em, "phone", ""));
            currentStudent.setEmergencyAddress(getStr(em, "address", ""));
        }

        // Phone fallback to Emergency or Father phone if direct phone is empty
        String phone = getStr(d, "phone", "");
        if (phone.isEmpty() && !currentStudent.getEmergencyPhone().isEmpty()) {
            phone = currentStudent.getEmergencyPhone() + " (" + (currentStudent.getEmergencyRelationship().isEmpty() ? "Emergency" : currentStudent.getEmergencyRelationship()) + ")";
        } else if (phone.isEmpty() && !currentStudent.getFatherPhone().isEmpty()) {
            phone = currentStudent.getFatherPhone() + " (Father)";
        }
        currentStudent.setPhone(phone);

        // Attendance Summary
        if (d.has("attendance_summary") && d.get("attendance_summary").isJsonObject()) {
            JsonObject att = d.getAsJsonObject("attendance_summary");
            currentStudent.setTotalClasses(att.has("total_classes") ? att.get("total_classes").getAsInt() : 0);
            currentStudent.setPresentClasses(att.has("present") ? att.get("present").getAsInt() : 0);
            currentStudent.setAbsentClasses(att.has("absent") ? att.get("absent").getAsInt() : 0);
            currentStudent.setLateClasses(att.has("late") ? att.get("late").getAsInt() : 0);
            currentStudent.setExcusedClasses(att.has("excused") ? att.get("excused").getAsInt() : 0);
            currentStudent.setAttendanceRate(att.has("attendance_rate") ? Math.round(att.get("attendance_rate").getAsFloat()) : 0);
        }

        // GPA Summary
        if (d.has("gpa_summary") && d.get("gpa_summary").isJsonObject()) {
            JsonObject gpa = d.getAsJsonObject("gpa_summary");
            if (gpa.has("gpa") && !gpa.get("gpa").isJsonNull()) {
                currentStudent.setGpa(gpa.get("gpa").getAsDouble());
            }
            if (gpa.has("standing") && !gpa.get("standing").isJsonNull()) {
                currentStudent.setRiskLevel(gpa.get("standing").getAsString());
            }
        }

        // Enrolled Courses
        if (d.has("enrolled_courses") && d.get("enrolled_courses").isJsonArray()) {
            JsonArray courses = d.getAsJsonArray("enrolled_courses");
            currentStudent.setEnrolledCoursesCount(courses.size() + (courses.size() == 1 ? " Course" : " Courses"));
            StringBuilder sb = new StringBuilder();
            for (JsonElement el : courses) {
                JsonObject c = el.getAsJsonObject();
                String cCode = getStr(c, "code", "");
                String cName = getStr(c, "name", "");
                String grade = getStr(c, "grade", "");
                sb.append("• ").append(cCode).append(" - ").append(cName);
                if (!grade.isEmpty()) sb.append(" (Grade: ").append(grade).append(")");
                sb.append("\n");
            }
            currentStudent.setEnrolledCoursesSummary(sb.toString().trim());
        } else if (d.has("enrolled_courses_count") && !d.get("enrolled_courses_count").isJsonNull()) {
            int cnt = d.get("enrolled_courses_count").getAsInt();
            currentStudent.setEnrolledCoursesCount(cnt + (cnt == 1 ? " Course" : " Courses"));
        }
    }

    private String getStr(JsonObject o, String key, String def) {
        if (o != null && o.has(key) && !o.get(key).isJsonNull()) {
            String val = o.get(key).getAsString().trim();
            if (!val.isEmpty()) return val;
        }
        return def != null ? def : "";
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

        String engName = currentStudent.getName();
        String khmName = currentStudent.getKhmerName();
        String displayName = (!khmName.isEmpty() && !khmName.equals(engName))
                ? engName + " (" + khmName + ")"
                : engName;

        String initial = (engName != null && !engName.isEmpty()) ? engName.substring(0, 1).toUpperCase() : "S";

        if (tvStudentAvatar != null) tvStudentAvatar.setText(initial);
        if (tvDetailStudentName != null) tvDetailStudentName.setText(displayName);
        if (tvDetailStudentId != null) tvDetailStudentId.setText(currentStudent.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
        updateStatusDisplay(isActive);

        if (tvValFullName != null) tvValFullName.setText(displayName);
        if (tvValStudentId != null) tvValStudentId.setText(currentStudent.getId());
        if (tvValEmail != null) {
            String email = currentStudent.getEmail();
            tvValEmail.setText(email != null && !email.isEmpty() ? email : "Login via ID (" + currentStudent.getId() + ")");
        }
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

        if (containerOtherTabs != null) {
            containerOtherTabs.setOnClickListener(v -> {
                if (currentTabIndex == 3) {
                    showEditNotesDialog();
                }
            });
        }
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("student", currentStudent);
        intent.putExtra("user", currentUser);
        startActivity(intent);
    }

    private void confirmToggleAccountStatus() {
        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
        String action = isActive ? "deactivate" : "activate";
        String targetApiStatus = isActive ? "inactive" : "active";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? "Deactivate Account" : "Activate Account")
                .setMessage("Are you sure you want to " + action + " account for " + currentStudent.getName() + " (" + currentStudent.getId() + ")?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String lookupId = resolveLookupUserId();
                    JsonObject body = new JsonObject();
                    body.addProperty("status", targetApiStatus);

                    if (btnDeactivateAccount != null) btnDeactivateAccount.setEnabled(false);

                    ApiClient.getApiService(this).updateUserStatus(lookupId, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (btnDeactivateAccount != null) btnDeactivateAccount.setEnabled(true);

                            if (response.isSuccessful()) {
                                boolean newActiveState = !isActive;
                                currentStudent.setStatus(newActiveState ? "Active" : "Inactive");
                                if (currentUser != null) {
                                    currentUser.setStatus(currentStudent.getStatus());
                                }
                                updateStatusDisplay(newActiveState);
                                Toast.makeText(AdminStudentDetailActivity.this, "Account " + action + "d successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminStudentDetailActivity.this, "Failed to update status (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            if (btnDeactivateAccount != null) btnDeactivateAccount.setEnabled(true);
                            Toast.makeText(AdminStudentDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectTab(int index) {
        currentTabIndex = index;
        resetTabs();
        if (index == 0) {
            if (containerOverview != null) containerOverview.setVisibility(View.VISIBLE);
            setTabActive(tabOverview);
        } else if (index == 1) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabAcademic);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Academic, Family & Address Details");
            StringBuilder sb = new StringBuilder();
            sb.append("Department: ").append(currentStudent.getDepartment()).append("\n");
            sb.append("Program: ").append(currentStudent.getProgram()).append("\n");
            sb.append("Year Level: ").append(currentStudent.getYear()).append("\n");
            sb.append("Academic Year: ").append(currentStudent.getAcademicYear()).append("\n");
            if (!currentStudent.getClassName().isEmpty()) {
                sb.append("Assigned Class: ").append(currentStudent.getClassName()).append("\n");
            }
            sb.append("Cumulative GPA: ").append(String.format("%.2f", currentStudent.getGpa())).append(" (").append(currentStudent.getRiskLevel()).append(")\n");
            sb.append("Enrolled Courses: ").append(currentStudent.getEnrolledCoursesCount()).append("\n");
            if (!currentStudent.getEnrolledCoursesSummary().isEmpty()) {
                sb.append(currentStudent.getEnrolledCoursesSummary()).append("\n");
            }
            if (!currentStudent.getPlaceOfBirth().isEmpty() || !currentStudent.getAddress().isEmpty()) {
                sb.append("\n--- Address Information ---\n");
                if (!currentStudent.getPlaceOfBirth().isEmpty()) sb.append("Place of Birth: ").append(currentStudent.getPlaceOfBirth()).append("\n");
                if (!currentStudent.getAddress().isEmpty()) sb.append("Current Address: ").append(currentStudent.getAddress()).append("\n");
            }
            if (!currentStudent.getFatherEnglishName().isEmpty() || !currentStudent.getMotherEnglishName().isEmpty() || !currentStudent.getEmergencyName().isEmpty()) {
                sb.append("\n--- Family & Emergency Contact ---\n");
                if (!currentStudent.getFatherEnglishName().isEmpty() || !currentStudent.getFatherKhmerName().isEmpty()) {
                    sb.append("Father: ").append(currentStudent.getFatherEnglishName());
                    if (!currentStudent.getFatherKhmerName().isEmpty()) sb.append(" (").append(currentStudent.getFatherKhmerName()).append(")");
                    if (!currentStudent.getFatherPhone().isEmpty()) sb.append(" • ").append(currentStudent.getFatherPhone());
                    sb.append("\n");
                }
                if (!currentStudent.getMotherEnglishName().isEmpty() || !currentStudent.getMotherKhmerName().isEmpty()) {
                    sb.append("Mother: ").append(currentStudent.getMotherEnglishName());
                    if (!currentStudent.getMotherKhmerName().isEmpty()) sb.append(" (").append(currentStudent.getMotherKhmerName()).append(")");
                    if (!currentStudent.getMotherPhone().isEmpty()) sb.append(" • ").append(currentStudent.getMotherPhone());
                    sb.append("\n");
                }
                if (!currentStudent.getEmergencyName().isEmpty()) {
                    sb.append("Emergency: ").append(currentStudent.getEmergencyName());
                    if (!currentStudent.getEmergencyRelationship().isEmpty()) sb.append(" (").append(currentStudent.getEmergencyRelationship()).append(")");
                    if (!currentStudent.getEmergencyPhone().isEmpty()) sb.append(" • ").append(currentStudent.getEmergencyPhone());
                }
            }
            if (tvOtherTabContent != null) tvOtherTabContent.setText(sb.toString().trim());
        } else if (index == 2) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabAttendance);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Attendance Records");
            String attText = "Overall Attendance Rate: " + currentStudent.getAttendanceRate() + "%\n"
                    + "Total Classes Recorded: " + currentStudent.getTotalClasses() + "\n"
                    + "Present: " + currentStudent.getPresentClasses() + "\n"
                    + "Late: " + currentStudent.getLateClasses() + "\n"
                    + "Excused: " + currentStudent.getExcusedClasses() + "\n"
                    + "Absent: " + currentStudent.getAbsentClasses();
            if (tvOtherTabContent != null) tvOtherTabContent.setText(attText);
        } else if (index == 3) {
            if (containerOtherTabs != null) containerOtherTabs.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            if (tvOtherTabTitle != null) tvOtherTabTitle.setText("Admin & Advisor Notes (Tap card to Add / Edit)");
            if (tvOtherTabContent != null) tvOtherTabContent.setText(getSavedNotes());
        }
    }

    private String getSavedNotes() {
        SharedPreferences prefs = getSharedPreferences("SLAS_Admin_Student_Notes", MODE_PRIVATE);
        String key = "notes_" + currentStudent.getId();
        String defaultNotes = "• Student account active under " + currentStudent.getDepartment() + " (" + currentStudent.getProgram() + ").\n"
                + "• Tap here to add or edit advisor/admin notes for " + currentStudent.getName() + ".";
        return prefs.getString(key, defaultNotes);
    }

    private void showEditNotesDialog() {
        SharedPreferences prefs = getSharedPreferences("SLAS_Admin_Student_Notes", MODE_PRIVATE);
        String key = "notes_" + currentStudent.getId();
        EditText input = new EditText(this);
        input.setMinLines(4);
        input.setText(prefs.getString(key, ""));
        input.setHint("Enter advisor or administrative notes for " + currentStudent.getName() + "...");
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Admin & Advisor Notes")
                .setView(input)
                .setPositiveButton("Save Note", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (val.isEmpty()) {
                        prefs.edit().remove(key).apply();
                    } else {
                        prefs.edit().putString(key, val).apply();
                    }
                    if (currentTabIndex == 3 && tvOtherTabContent != null) {
                        tvOtherTabContent.setText(getSavedNotes());
                    }
                    Toast.makeText(this, "Notes saved", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Clear", (d, w) -> {
                    prefs.edit().remove(key).apply();
                    if (currentTabIndex == 3 && tvOtherTabContent != null) {
                        tvOtherTabContent.setText(getSavedNotes());
                    }
                    Toast.makeText(this, "Notes cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
