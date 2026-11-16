package com.social7.studentlearnanalytics.admin.users;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.User;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStudentDetailActivity extends AppCompatActivity {

    private static final String STATE_TAB_INDEX = "state_tab_index";

    private Student currentStudent;
    private User currentUser;
    private int currentTabIndex = 0;
    private boolean skipNextResumeFetch = true;

    private TextView tabOverview, tabAcademic, tabAttendance, tabNotes;
    private LinearLayout containerOverview, containerStudy, containerAttendance, containerNotes;

    // Header
    private TextView tvStudentAvatar, tvDetailStudentName, tvDetailStudentId, tvDetailStatusPill;
    private ImageView imgStudentDetailMenu;

    // Overview Tab
    private TextView tvValFullName, tvValStudentId, tvValEmail, tvValPhone, tvValGender, tvValDob;
    private TextView tvValDept, tvValProgram, tvValYear, tvValAcademicYear, tvValEnrolledCourses;
    private TextView tvValAccountStatus, tvValCreatedAt, tvValLastLogin;

    // Study Tab
    private TextView tvStudyDept, tvStudyProgram, tvStudyYearLevel, tvStudyAcademicYear;
    private TextView tvStudyAssignedClass, tvStudyGpa, tvStudyEnrolledCount, tvStudyCoursesList;
    private LinearLayout layoutStudyCoursesList;
    private TextView tvStudyPlaceOfBirth, tvStudyCurrentAddress;
    private TextView tvStudyFatherName, tvStudyFatherPhone, tvStudyMotherName, tvStudyMotherPhone;
    private TextView tvStudyEmergencyName, tvStudyEmergencyPhone;

    // Attendance Tab
    private TextView tvAttOverallRate, tvAttTotalClasses;
    private TextView tvAttPresentRow, tvAttLateRow, tvAttExcusedRow, tvAttAbsentRow;
    private TextView tvStatPresentCount, tvStatPresentPct;
    private TextView tvStatLateCount, tvStatLatePct;
    private TextView tvStatExcusedCount, tvStatExcusedPct;
    private TextView tvStatAbsentCount, tvStatAbsentPct;
    private TextView tvAttendanceEmptyState;
    private RecyclerView rvAttendanceHistory;
    private AttendanceRecordAdapter attendanceAdapter;
    private final List<AttendanceRecordItem> attendanceRecords = new ArrayList<>();

    // Notes Tab
    private MaterialButton btnAddNote, btnAddNoteEmpty;
    private View layoutNotesLoading, layoutNotesEmpty;
    private RecyclerView rvStudentNotes;
    private StudentNoteAdapter notesAdapter;
    private final List<NoteItem> studentNotes = new ArrayList<>();

    // Bottom Bar
    private MaterialButton btnDeactivateAccount, btnEditStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_detail);

        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(STATE_TAB_INDEX, 0);
        }

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
        selectTab(currentTabIndex);
        fetchStudentDetailsFromApi();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB_INDEX, currentTabIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipNextResumeFetch) {
            skipNextResumeFetch = false;
            return;
        }
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
                currentStudent.setYear(getString(R.string.year_level_format, yInt));
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

        String createdAt = formatIsoDate(getStr(d, "created_at", ""));
        if (!createdAt.isEmpty()) currentStudent.setCreatedAt(createdAt);

        String updatedAt = formatIsoDate(getStr(d, "updated_at", ""));
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

        // Phone fallback
        String phone = getStr(d, "phone", "");
        if (phone.isEmpty() && !currentStudent.getEmergencyPhone().isEmpty()) {
            phone = currentStudent.getEmergencyPhone();
        } else if (phone.isEmpty() && !currentStudent.getFatherPhone().isEmpty()) {
            phone = currentStudent.getFatherPhone();
        }
        currentStudent.setPhone(phone);

        // Attendance Summary
        if (d.has("attendance_summary") && d.get("attendance_summary").isJsonObject()) {
            JsonObject att = d.getAsJsonObject("attendance_summary");
            currentStudent.setTotalClasses(att.has("total_classes") && !att.get("total_classes").isJsonNull() ? att.get("total_classes").getAsInt() : 0);
            currentStudent.setPresentClasses(att.has("present") && !att.get("present").isJsonNull() ? att.get("present").getAsInt() : 0);
            currentStudent.setAbsentClasses(att.has("absent") && !att.get("absent").isJsonNull() ? att.get("absent").getAsInt() : 0);
            currentStudent.setLateClasses(att.has("late") && !att.get("late").isJsonNull() ? att.get("late").getAsInt() : 0);
            currentStudent.setExcusedClasses(att.has("excused") && !att.get("excused").isJsonNull() ? att.get("excused").getAsInt() : 0);
            currentStudent.setAttendanceRate(att.has("attendance_rate") && !att.get("attendance_rate").isJsonNull() ? Math.round(att.get("attendance_rate").getAsFloat()) : 0);
        }

        // Attendance Records
        attendanceRecords.clear();
        if (d.has("attendance_records") && d.get("attendance_records").isJsonArray()) {
            JsonArray arr = d.getAsJsonArray("attendance_records");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject r = el.getAsJsonObject();
                attendanceRecords.add(new AttendanceRecordItem(
                        getStr(r, "date", ""),
                        getStr(r, "course_code", ""),
                        getStr(r, "course_name", ""),
                        getStr(r, "status", ""),
                        getStr(r, "check_in_time", ""),
                        getStr(r, "remarks", "")
                ));
            }
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
            currentStudent.setEnrolledCoursesCount(formatCourseCount(courses.size()));
            StringBuilder sb = new StringBuilder();
            for (JsonElement el : courses) {
                if (!el.isJsonObject()) continue;
                JsonObject c = el.getAsJsonObject();
                String cCode = getStr(c, "code", "");
                String cName = getStr(c, "name", "");
                String grade = getStr(c, "grade", "");
                sb.append("• ");
                if (!cCode.isEmpty()) sb.append(cCode).append(" - ");
                sb.append(cName);
                if (!grade.isEmpty()) sb.append(" (").append(grade).append(")");
                sb.append("\n");
            }
            currentStudent.setEnrolledCoursesSummary(sb.toString().trim());
        } else if (d.has("enrolled_courses_count") && !d.get("enrolled_courses_count").isJsonNull()) {
            int cnt = d.get("enrolled_courses_count").getAsInt();
            currentStudent.setEnrolledCoursesCount(formatCourseCount(cnt));
        }

        // Notes from user details payload
        if (d.has("notes") && d.get("notes").isJsonArray()) {
            parseNotesArray(d.getAsJsonArray("notes"));
        }
    }

    private void parseNotesArray(JsonArray arr) {
        studentNotes.clear();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            long id = o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsLong() : 0L;
            String authorName = getStr(o, "author_name", "");
            String noteBody = getStr(o, "note", "");
            String createdAt = formatIsoDateTime(getStr(o, "created_at", ""));
            String updatedAt = formatIsoDateTime(getStr(o, "updated_at", ""));
            boolean canEdit = !o.has("can_edit") || o.get("can_edit").getAsBoolean();
            boolean canDelete = !o.has("can_delete") || o.get("can_delete").getAsBoolean();
            studentNotes.add(new NoteItem(id, authorName, noteBody, createdAt, updatedAt, canEdit, canDelete));
        }
    }

    private String formatCourseCount(int count) {
        return count == 1 ? getString(R.string.course_count_single) : getString(R.string.courses_count_format, count);
    }

    private String formatIsoDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }

    private String formatIsoDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String cleaned = raw.replace("T", " ");
        return cleaned.length() >= 16 ? cleaned.substring(0, 16) : cleaned;
    }

    private String getStr(JsonObject o, String key, String def) {
        if (o != null && o.has(key) && !o.get(key).isJsonNull()) {
            String val = o.get(key).getAsString().trim();
            if (!val.isEmpty()) return val;
        }
        return def != null ? def : "";
    }

    private String valOrEmpty(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : getString(R.string.no_data_available);
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackStudentDetail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        imgStudentDetailMenu = findViewById(R.id.imgStudentDetailMenu);

        tvStudentAvatar = findViewById(R.id.tvStudentAvatar);
        tvDetailStudentName = findViewById(R.id.tvDetailStudentName);
        tvDetailStudentId = findViewById(R.id.tvDetailStudentId);
        tvDetailStatusPill = findViewById(R.id.tvDetailStatusPill);

        tabOverview = findViewById(R.id.tabOverview);
        tabAcademic = findViewById(R.id.tabAcademic);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabNotes = findViewById(R.id.tabNotes);

        containerOverview = findViewById(R.id.containerOverview);
        containerStudy = findViewById(R.id.containerStudy);
        containerAttendance = findViewById(R.id.containerAttendance);
        containerNotes = findViewById(R.id.containerNotes);

        // Overview
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

        // Study
        tvStudyDept = findViewById(R.id.tvStudyDept);
        tvStudyProgram = findViewById(R.id.tvStudyProgram);
        tvStudyYearLevel = findViewById(R.id.tvStudyYearLevel);
        tvStudyAcademicYear = findViewById(R.id.tvStudyAcademicYear);
        tvStudyAssignedClass = findViewById(R.id.tvStudyAssignedClass);
        tvStudyGpa = findViewById(R.id.tvStudyGpa);
        tvStudyEnrolledCount = findViewById(R.id.tvStudyEnrolledCount);
        tvStudyCoursesList = findViewById(R.id.tvStudyCoursesList);
        layoutStudyCoursesList = findViewById(R.id.layoutStudyCoursesList);
        tvStudyPlaceOfBirth = findViewById(R.id.tvStudyPlaceOfBirth);
        tvStudyCurrentAddress = findViewById(R.id.tvStudyCurrentAddress);
        tvStudyFatherName = findViewById(R.id.tvStudyFatherName);
        tvStudyFatherPhone = findViewById(R.id.tvStudyFatherPhone);
        tvStudyMotherName = findViewById(R.id.tvStudyMotherName);
        tvStudyMotherPhone = findViewById(R.id.tvStudyMotherPhone);
        tvStudyEmergencyName = findViewById(R.id.tvStudyEmergencyName);
        tvStudyEmergencyPhone = findViewById(R.id.tvStudyEmergencyPhone);

        // Attendance
        tvAttOverallRate = findViewById(R.id.tvAttOverallRate);
        tvAttTotalClasses = findViewById(R.id.tvAttTotalClasses);
        tvAttPresentRow = findViewById(R.id.tvAttPresentRow);
        tvAttLateRow = findViewById(R.id.tvAttLateRow);
        tvAttExcusedRow = findViewById(R.id.tvAttExcusedRow);
        tvAttAbsentRow = findViewById(R.id.tvAttAbsentRow);
        tvStatPresentCount = findViewById(R.id.tvStatPresentCount);
        tvStatPresentPct = findViewById(R.id.tvStatPresentPct);
        tvStatLateCount = findViewById(R.id.tvStatLateCount);
        tvStatLatePct = findViewById(R.id.tvStatLatePct);
        tvStatExcusedCount = findViewById(R.id.tvStatExcusedCount);
        tvStatExcusedPct = findViewById(R.id.tvStatExcusedPct);
        tvStatAbsentCount = findViewById(R.id.tvStatAbsentCount);
        tvStatAbsentPct = findViewById(R.id.tvStatAbsentPct);
        tvAttendanceEmptyState = findViewById(R.id.tvAttendanceEmptyState);
        rvAttendanceHistory = findViewById(R.id.rvAttendanceHistory);
        if (rvAttendanceHistory != null) {
            rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(this));
            attendanceAdapter = new AttendanceRecordAdapter(attendanceRecords);
            rvAttendanceHistory.setAdapter(attendanceAdapter);
        }

        // Notes
        btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNoteEmpty = findViewById(R.id.btnAddNoteEmpty);
        layoutNotesLoading = findViewById(R.id.layoutNotesLoading);
        layoutNotesEmpty = findViewById(R.id.layoutNotesEmpty);
        rvStudentNotes = findViewById(R.id.rvStudentNotes);
        if (rvStudentNotes != null) {
            rvStudentNotes.setLayoutManager(new LinearLayoutManager(this));
            notesAdapter = new StudentNoteAdapter(studentNotes);
            rvStudentNotes.setAdapter(notesAdapter);
        }

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

        String initial = (engName != null && !engName.isEmpty()) ? engName.substring(0, 1).toUpperCase(Locale.US) : "S";

        if (tvStudentAvatar != null) tvStudentAvatar.setText(initial);
        if (tvDetailStudentName != null) tvDetailStudentName.setText(displayName);
        if (tvDetailStudentId != null) tvDetailStudentId.setText(currentStudent.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
        updateStatusDisplay(isActive);

        // Overview
        if (tvValFullName != null) tvValFullName.setText(valOrEmpty(displayName));
        if (tvValStudentId != null) tvValStudentId.setText(valOrEmpty(currentStudent.getId()));
        if (tvValEmail != null) {
            String email = currentStudent.getEmail();
            tvValEmail.setText(email != null && !email.isEmpty() ? email : getString(R.string.login_via_id_format, currentStudent.getId()));
        }
        if (tvValPhone != null) tvValPhone.setText(valOrEmpty(currentStudent.getPhone()));
        if (tvValGender != null) tvValGender.setText(valOrEmpty(currentStudent.getGender()));
        if (tvValDob != null) tvValDob.setText(valOrEmpty(currentStudent.getDob()));

        if (tvValDept != null) tvValDept.setText(valOrEmpty(currentStudent.getDepartment()));
        if (tvValProgram != null) tvValProgram.setText(valOrEmpty(currentStudent.getProgram()));
        if (tvValYear != null) tvValYear.setText(valOrEmpty(currentStudent.getYear()));
        if (tvValAcademicYear != null) tvValAcademicYear.setText(valOrEmpty(currentStudent.getAcademicYear()));
        if (tvValEnrolledCourses != null) tvValEnrolledCourses.setText(valOrEmpty(currentStudent.getEnrolledCoursesCount()));

        if (tvValCreatedAt != null) tvValCreatedAt.setText(valOrEmpty(currentStudent.getCreatedAt()));
        if (tvValLastLogin != null) tvValLastLogin.setText(valOrEmpty(currentStudent.getLastLogin()));

        bindStudyTab();
        bindAttendanceTab();
        bindNotesTab();
    }

    private void bindStudyTab() {
        if (currentStudent == null) return;
        if (tvStudyDept != null) tvStudyDept.setText(valOrEmpty(currentStudent.getDepartment()));
        if (tvStudyProgram != null) tvStudyProgram.setText(valOrEmpty(currentStudent.getProgram()));
        if (tvStudyYearLevel != null) tvStudyYearLevel.setText(valOrEmpty(currentStudent.getYear()));
        if (tvStudyAcademicYear != null) tvStudyAcademicYear.setText(valOrEmpty(currentStudent.getAcademicYear()));
        if (tvStudyAssignedClass != null) tvStudyAssignedClass.setText(valOrEmpty(currentStudent.getClassName()));

        if (tvStudyGpa != null) {
            String gpaText = String.format(Locale.US, "%.2f", currentStudent.getGpa());
            String standing = currentStudent.getRiskLevel();
            if (standing != null && !standing.isEmpty() && !"Low".equalsIgnoreCase(standing)) {
                gpaText = gpaText + " (" + standing + ")";
            }
            tvStudyGpa.setText(gpaText);
        }

        if (tvStudyEnrolledCount != null) tvStudyEnrolledCount.setText(valOrEmpty(currentStudent.getEnrolledCoursesCount()));
        if (layoutStudyCoursesList != null && tvStudyCoursesList != null) {
            String summary = currentStudent.getEnrolledCoursesSummary();
            if (summary != null && !summary.trim().isEmpty()) {
                layoutStudyCoursesList.setVisibility(View.VISIBLE);
                tvStudyCoursesList.setText(summary.trim());
            } else {
                layoutStudyCoursesList.setVisibility(View.GONE);
            }
        }

        if (tvStudyPlaceOfBirth != null) tvStudyPlaceOfBirth.setText(valOrEmpty(currentStudent.getPlaceOfBirth()));
        if (tvStudyCurrentAddress != null) tvStudyCurrentAddress.setText(valOrEmpty(currentStudent.getAddress()));

        if (tvStudyFatherName != null) {
            tvStudyFatherName.setText(formatParentName(currentStudent.getFatherEnglishName(), currentStudent.getFatherKhmerName()));
        }
        if (tvStudyFatherPhone != null) tvStudyFatherPhone.setText(valOrEmpty(currentStudent.getFatherPhone()));

        if (tvStudyMotherName != null) {
            tvStudyMotherName.setText(formatParentName(currentStudent.getMotherEnglishName(), currentStudent.getMotherKhmerName()));
        }
        if (tvStudyMotherPhone != null) tvStudyMotherPhone.setText(valOrEmpty(currentStudent.getMotherPhone()));

        if (tvStudyEmergencyName != null) {
            String emName = currentStudent.getEmergencyName();
            String emRel = currentStudent.getEmergencyRelationship();
            if (emName != null && !emName.isEmpty()) {
                tvStudyEmergencyName.setText(!emRel.isEmpty() ? emName + " (" + emRel + ")" : emName);
            } else {
                tvStudyEmergencyName.setText(getString(R.string.no_data_available));
            }
        }
        if (tvStudyEmergencyPhone != null) tvStudyEmergencyPhone.setText(valOrEmpty(currentStudent.getEmergencyPhone()));
    }

    private String formatParentName(String eng, String khm) {
        boolean hasEng = eng != null && !eng.trim().isEmpty();
        boolean hasKhm = khm != null && !khm.trim().isEmpty();
        if (hasEng && hasKhm && !eng.trim().equals(khm.trim())) {
            return eng.trim() + " (" + khm.trim() + ")";
        }
        if (hasKhm) return khm.trim();
        if (hasEng) return eng.trim();
        return getString(R.string.no_data_available);
    }

    private void bindAttendanceTab() {
        if (currentStudent == null) return;
        int total = currentStudent.getTotalClasses();
        int present = currentStudent.getPresentClasses();
        int late = currentStudent.getLateClasses();
        int excused = currentStudent.getExcusedClasses();
        int absent = currentStudent.getAbsentClasses();

        if (tvAttOverallRate != null) {
            if (total > 0) {
                tvAttOverallRate.setText(currentStudent.getAttendanceRate() + "%");
            } else {
                tvAttOverallRate.setText(R.string.attendance_rate_unavailable);
            }
        }
        if (tvAttTotalClasses != null) tvAttTotalClasses.setText(String.valueOf(total));
        if (tvAttPresentRow != null) tvAttPresentRow.setText(String.valueOf(present));
        if (tvAttLateRow != null) tvAttLateRow.setText(String.valueOf(late));
        if (tvAttExcusedRow != null) tvAttExcusedRow.setText(String.valueOf(excused));
        if (tvAttAbsentRow != null) tvAttAbsentRow.setText(String.valueOf(absent));

        if (tvStatPresentCount != null) tvStatPresentCount.setText(String.valueOf(present));
        if (tvStatLateCount != null) tvStatLateCount.setText(String.valueOf(late));
        if (tvStatExcusedCount != null) tvStatExcusedCount.setText(String.valueOf(excused));
        if (tvStatAbsentCount != null) tvStatAbsentCount.setText(String.valueOf(absent));

        if (tvStatPresentPct != null) tvStatPresentPct.setText(formatPercentage(present, total));
        if (tvStatLatePct != null) tvStatLatePct.setText(formatPercentage(late, total));
        if (tvStatExcusedPct != null) tvStatExcusedPct.setText(formatPercentage(excused, total));
        if (tvStatAbsentPct != null) tvStatAbsentPct.setText(formatPercentage(absent, total));

        if (attendanceAdapter != null) {
            attendanceAdapter.notifyDataSetChanged();
        }
        boolean hasRecords = !attendanceRecords.isEmpty();
        if (tvAttendanceEmptyState != null) tvAttendanceEmptyState.setVisibility(hasRecords ? View.GONE : View.VISIBLE);
        if (rvAttendanceHistory != null) rvAttendanceHistory.setVisibility(hasRecords ? View.VISIBLE : View.GONE);
    }

    private String formatPercentage(int count, int total) {
        if (total <= 0) return "0%";
        return Math.round((count * 100.0f) / total) + "%";
    }

    private void bindNotesTab() {
        if (notesAdapter != null) {
            notesAdapter.notifyDataSetChanged();
        }
        boolean hasNotes = !studentNotes.isEmpty();
        if (layoutNotesLoading != null) layoutNotesLoading.setVisibility(View.GONE);
        if (layoutNotesEmpty != null) layoutNotesEmpty.setVisibility(hasNotes ? View.GONE : View.VISIBLE);
        if (rvStudentNotes != null) rvStudentNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
    }

    private void fetchNotesFromApi() {
        String lookupId = resolveLookupUserId();
        if (lookupId.isEmpty()) return;

        if (layoutNotesLoading != null) layoutNotesLoading.setVisibility(View.VISIBLE);
        if (layoutNotesEmpty != null) layoutNotesEmpty.setVisibility(View.GONE);

        ApiClient.getApiService(this).getStudentNotes(lookupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (layoutNotesLoading != null) layoutNotesLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().has("data") && response.body().get("data").isJsonArray()) {
                    parseNotesArray(response.body().getAsJsonArray("data"));
                    bindNotesTab();
                } else {
                    bindNotesTab();
                    Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.note_load_failed, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (layoutNotesLoading != null) layoutNotesLoading.setVisibility(View.GONE);
                bindNotesTab();
                Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.network_error_prefix) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusDisplay(boolean isActive) {
        int statusStringRes = isActive ? R.string.status_active : R.string.status_inactive;
        int color = Color.parseColor(isActive ? "#16A34A" : "#EF4444");

        if (tvDetailStatusPill != null) {
            tvDetailStatusPill.setText(statusStringRes);
            tvDetailStatusPill.setTextColor(color);
            tvDetailStatusPill.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (tvValAccountStatus != null) {
            tvValAccountStatus.setText(statusStringRes);
            tvValAccountStatus.setTextColor(color);
        }
        if (btnDeactivateAccount != null) {
            btnDeactivateAccount.setText(isActive ? R.string.deactivate_account : R.string.activate_account);
        }
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabAcademic != null) tabAcademic.setOnClickListener(v -> selectTab(1));
        if (tabAttendance != null) tabAttendance.setOnClickListener(v -> selectTab(2));
        if (tabNotes != null) tabNotes.setOnClickListener(v -> {
            selectTab(3);
            fetchNotesFromApi();
        });

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

        if (imgStudentDetailMenu != null) {
            imgStudentDetailMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, imgStudentDetailMenu);
                boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
                popup.getMenu().add(0, 1, 0, R.string.edit_student);
                popup.getMenu().add(0, 2, 1, isActive ? R.string.deactivate_account : R.string.activate_account);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        openEditScreen();
                        return true;
                    } else if (item.getItemId() == 2) {
                        confirmToggleAccountStatus();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        if (btnAddNote != null) btnAddNote.setOnClickListener(v -> showAddOrEditNoteDialog(null));
        if (btnAddNoteEmpty != null) btnAddNoteEmpty.setOnClickListener(v -> showAddOrEditNoteDialog(null));
    }

    private void openEditScreen() {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("student", currentStudent);
        intent.putExtra("user", currentUser);
        startActivity(intent);
    }

    private void confirmToggleAccountStatus() {
        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
        String targetApiStatus = isActive ? "inactive" : "active";

        new AlertDialog.Builder(this)
                .setTitle(isActive ? R.string.deactivate_account : R.string.activate_account)
                .setMessage(getString(
                        isActive ? R.string.confirm_deactivate_account_msg : R.string.confirm_activate_account_msg,
                        currentStudent.getName(),
                        currentStudent.getId()
                ))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
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
                                Toast.makeText(
                                        AdminStudentDetailActivity.this,
                                        newActiveState ? R.string.account_activated_success : R.string.account_deactivated_success,
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.status_update_failed, response.code()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            if (btnDeactivateAccount != null) btnDeactivateAccount.setEnabled(true);
                            Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.network_error_prefix) + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void selectTab(int index) {
        currentTabIndex = index;
        resetTabs();
        if (index == 0) {
            if (containerOverview != null) containerOverview.setVisibility(View.VISIBLE);
            setTabActive(tabOverview);
        } else if (index == 1) {
            if (containerStudy != null) containerStudy.setVisibility(View.VISIBLE);
            setTabActive(tabAcademic);
            bindStudyTab();
        } else if (index == 2) {
            if (containerAttendance != null) containerAttendance.setVisibility(View.VISIBLE);
            setTabActive(tabAttendance);
            bindAttendanceTab();
        } else if (index == 3) {
            if (containerNotes != null) containerNotes.setVisibility(View.VISIBLE);
            setTabActive(tabNotes);
            bindNotesTab();
        }
    }

    private void showAddOrEditNoteDialog(@Nullable NoteItem existingNote) {
        EditText input = new EditText(this);
        input.setMinLines(4);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setHint(R.string.note_hint);
        if (existingNote != null) {
            input.setText(existingNote.note);
            input.setSelection(existingNote.note.length());
        }
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingNote == null ? R.string.add_note : R.string.edit_note)
                .setView(input)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String noteText = input.getText().toString().trim();
            if (noteText.isEmpty()) {
                input.setError(getString(R.string.note_empty_error));
                return;
            }
            String lookupId = resolveLookupUserId();
            if (lookupId.isEmpty()) return;

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            JsonObject body = new JsonObject();
            body.addProperty("note", noteText);

            Call<JsonObject> call = existingNote == null
                    ? ApiClient.getApiService(this).createStudentNote(lookupId, body)
                    : ApiClient.getApiService(this).updateStudentNote(lookupId, existingNote.id, body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(AdminStudentDetailActivity.this, R.string.note_saved_success, Toast.LENGTH_SHORT).show();
                        fetchNotesFromApi();
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.note_save_failed, response.code()), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.network_error_prefix) + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private void confirmDeleteNote(NoteItem note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_note)
                .setMessage(R.string.confirm_delete_note_msg)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    String lookupId = resolveLookupUserId();
                    if (lookupId.isEmpty()) return;

                    ApiClient.getApiService(this).deleteStudentNote(lookupId, note.id).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminStudentDetailActivity.this, R.string.note_deleted_success, Toast.LENGTH_SHORT).show();
                                fetchNotesFromApi();
                            } else {
                                Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.note_delete_failed, response.code()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(AdminStudentDetailActivity.this, getString(R.string.network_error_prefix) + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void resetTabs() {
        if (containerOverview != null) containerOverview.setVisibility(View.GONE);
        if (containerStudy != null) containerStudy.setVisibility(View.GONE);
        if (containerAttendance != null) containerAttendance.setVisibility(View.GONE);
        if (containerNotes != null) containerNotes.setVisibility(View.GONE);

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

    private static class AttendanceRecordItem {
        final String date;
        final String courseCode;
        final String courseName;
        final String status;
        final String checkInTime;
        final String remarks;

        AttendanceRecordItem(String date, String courseCode, String courseName, String status, String checkInTime, String remarks) {
            this.date = date;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.status = status;
            this.checkInTime = checkInTime;
            this.remarks = remarks;
        }
    }

    private class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.VH> {
        private final List<AttendanceRecordItem> items;

        AttendanceRecordAdapter(List<AttendanceRecordItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_student_attendance_record, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            AttendanceRecordItem item = items.get(position);
            String courseDisplay = !item.courseCode.isEmpty() && !item.courseName.isEmpty()
                    ? item.courseCode + " - " + item.courseName
                    : (!item.courseName.isEmpty() ? item.courseName : item.courseCode);
            h.tvCourse.setText(valOrEmpty(courseDisplay));
            h.tvDate.setText(valOrEmpty(item.date));

            String statusLower = item.status.toLowerCase(Locale.US);
            if ("present".equals(statusLower)) {
                h.tvStatus.setText(R.string.attendance_present);
                h.tvStatus.setTextColor(Color.parseColor("#16A34A"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
            } else if ("late".equals(statusLower)) {
                h.tvStatus.setText(R.string.attendance_late);
                h.tvStatus.setTextColor(Color.parseColor("#EA580C"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#FFEDD5"));
            } else if ("excused".equals(statusLower)) {
                h.tvStatus.setText(R.string.attendance_excused);
                h.tvStatus.setTextColor(Color.parseColor("#2563EB"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#DBEAFE"));
            } else {
                h.tvStatus.setText(R.string.attendance_absent);
                h.tvStatus.setTextColor(Color.parseColor("#DC2626"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#FFE4E6"));
            }

            if (!item.checkInTime.isEmpty()) {
                h.tvCheckIn.setVisibility(View.VISIBLE);
                h.tvCheckIn.setText(getString(R.string.check_in_label, item.checkInTime));
            } else {
                h.tvCheckIn.setVisibility(View.GONE);
            }

            if (!item.remarks.isEmpty()) {
                h.tvRemarks.setVisibility(View.VISIBLE);
                h.tvRemarks.setText(getString(R.string.remarks_label, item.remarks));
            } else {
                h.tvRemarks.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tvCourse, tvDate, tvStatus, tvCheckIn, tvRemarks;

            VH(@NonNull View itemView) {
                super(itemView);
                tvCourse = itemView.findViewById(R.id.tvAttRecordCourse);
                tvDate = itemView.findViewById(R.id.tvAttRecordDate);
                tvStatus = itemView.findViewById(R.id.tvAttRecordStatus);
                tvCheckIn = itemView.findViewById(R.id.tvAttRecordCheckIn);
                tvRemarks = itemView.findViewById(R.id.tvAttRecordRemarks);
            }
        }
    }

    private static class NoteItem {
        final long id;
        final String authorName;
        final String note;
        final String createdAt;
        final String updatedAt;
        final boolean canEdit;
        final boolean canDelete;

        NoteItem(long id, String authorName, String note, String createdAt, String updatedAt, boolean canEdit, boolean canDelete) {
            this.id = id;
            this.authorName = authorName;
            this.note = note;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.canEdit = canEdit;
            this.canDelete = canDelete;
        }
    }

    private class StudentNoteAdapter extends RecyclerView.Adapter<StudentNoteAdapter.VH> {
        private final List<NoteItem> items;

        StudentNoteAdapter(List<NoteItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_student_note, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            NoteItem item = items.get(position);
            h.tvAuthor.setText(valOrEmpty(item.authorName));
            h.tvCreatedAt.setText(valOrEmpty(item.createdAt));
            h.tvUpdatedAt.setText(valOrEmpty(item.updatedAt));
            h.tvBody.setText(item.note);

            h.btnEdit.setVisibility(item.canEdit ? View.VISIBLE : View.GONE);
            h.btnDelete.setVisibility(item.canDelete ? View.VISIBLE : View.GONE);
            h.layoutActions.setVisibility((item.canEdit || item.canDelete) ? View.VISIBLE : View.GONE);

            h.btnEdit.setOnClickListener(v -> showAddOrEditNoteDialog(item));
            h.btnDelete.setOnClickListener(v -> confirmDeleteNote(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tvAuthor, tvCreatedAt, tvUpdatedAt, tvBody;
            final View layoutActions;
            final MaterialButton btnEdit, btnDelete;

            VH(@NonNull View itemView) {
                super(itemView);
                tvAuthor = itemView.findViewById(R.id.tvNoteAuthor);
                tvCreatedAt = itemView.findViewById(R.id.tvNoteCreatedAt);
                tvUpdatedAt = itemView.findViewById(R.id.tvNoteUpdatedAt);
                tvBody = itemView.findViewById(R.id.tvNoteBody);
                layoutActions = itemView.findViewById(R.id.layoutNoteActions);
                btnEdit = itemView.findViewById(R.id.btnEditNoteItem);
                btnDelete = itemView.findViewById(R.id.btnDeleteNoteItem);
            }
        }
    }
}
