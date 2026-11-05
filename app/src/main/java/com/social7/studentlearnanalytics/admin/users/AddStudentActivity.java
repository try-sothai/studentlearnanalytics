package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStudentActivity extends AppCompatActivity {

    // 01. Personal Information
    private TextInputLayout tilAddStudentKhmerName, tilAddStudentEnglishName, tilAddStudentDob, tilAddStudentPob, tilAddStudentAddress;
    private TextInputEditText etAddStudentKhmerName, etAddStudentEnglishName, etAddStudentDob, etAddStudentPob, etAddStudentAddress;
    private Spinner spAddStudentGender;

    // 02. Family Information
    private EditText etFatherKhmerName, etFatherEnglishName, etFatherPhone;
    private EditText etMotherKhmerName, etMotherEnglishName, etMotherPhone;

    // 03. Emergency Contact
    private EditText etEmergencyName, etEmergencyRelationship, etEmergencyPhone, etEmergencyAddress;

    // 04. Account Information (Single Identifier/Username field)
    private TextInputLayout tilAddStudentIdentifier, tilAddStudentPassword;
    private TextInputEditText etAddStudentIdentifier, etAddStudentPassword;

    // 05. Academic Information
    private Spinner spAddDepartment, spAddProgram, spAddYearLevel, spAddAcademicYear, spAddSemester;

    private MaterialButton btnSaveStudent, btnCancelAddStudent;

    // Lookup data lists
    private final List<IdNameItem> departmentList = new ArrayList<>();
    private final List<IdNameItem> programList = new ArrayList<>();
    private final List<IdNameItem> academicYearList = new ArrayList<>();
    private final List<IdNameItem> semesterList = new ArrayList<>();

    public static class IdNameItem {
        public final int id;
        public final String name;
        public final String code;
        public final int parentId;

        public IdNameItem(int id, String name, String code, int parentId) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.parentId = parentId;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_user);

        initViews();
        setupGenderAndYearLevelSpinners();
        fetchAcademicLookups();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAddStudent);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 01.
        tilAddStudentKhmerName = findViewById(R.id.tilAddStudentKhmerName);
        tilAddStudentEnglishName = findViewById(R.id.tilAddStudentEnglishName);
        tilAddStudentDob = findViewById(R.id.tilAddStudentDob);
        tilAddStudentPob = findViewById(R.id.tilAddStudentPob);
        tilAddStudentAddress = findViewById(R.id.tilAddStudentAddress);

        etAddStudentKhmerName = findViewById(R.id.etAddStudentKhmerName);
        etAddStudentEnglishName = findViewById(R.id.etAddStudentEnglishName);
        etAddStudentDob = findViewById(R.id.etAddStudentDob);
        etAddStudentPob = findViewById(R.id.etAddStudentPob);
        etAddStudentAddress = findViewById(R.id.etAddStudentAddress);
        spAddStudentGender = findViewById(R.id.spAddStudentGender);

        // 02.
        etFatherKhmerName = findViewById(R.id.etFatherKhmerName);
        etFatherEnglishName = findViewById(R.id.etFatherEnglishName);
        etFatherPhone = findViewById(R.id.etFatherPhone);
        etMotherKhmerName = findViewById(R.id.etMotherKhmerName);
        etMotherEnglishName = findViewById(R.id.etMotherEnglishName);
        etMotherPhone = findViewById(R.id.etMotherPhone);

        // 03.
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyRelationship = findViewById(R.id.etEmergencyRelationship);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etEmergencyAddress = findViewById(R.id.etEmergencyAddress);

        // 04.
        tilAddStudentIdentifier = findViewById(R.id.tilAddStudentIdentifier);
        tilAddStudentPassword = findViewById(R.id.tilAddStudentPassword);
        etAddStudentIdentifier = findViewById(R.id.etAddStudentIdentifier);
        etAddStudentPassword = findViewById(R.id.etAddStudentPassword);

        // 05.
        spAddDepartment = findViewById(R.id.spAddDepartment);
        spAddProgram = findViewById(R.id.spAddProgram);
        spAddYearLevel = findViewById(R.id.spAddYearLevel);
        spAddAcademicYear = findViewById(R.id.spAddAcademicYear);
        spAddSemester = findViewById(R.id.spAddSemester);

        btnSaveStudent = findViewById(R.id.btnSaveStudent);
        btnCancelAddStudent = findViewById(R.id.btnCancelAddStudent);
    }

    private void setupGenderAndYearLevelSpinners() {
        if (spAddStudentGender != null) {
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spAddStudentGender.setAdapter(genderAdapter);
        }

        if (spAddYearLevel != null) {
            ArrayAdapter<String> yearLevelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"});
            yearLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spAddYearLevel.setAdapter(yearLevelAdapter);
        }
    }

    private void fetchAcademicLookups() {
        ApiClient.getApiService(this).getStudentLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                    if (d == null) return;

                    // Departments
                    departmentList.clear();
                    if (d.has("departments") && d.get("departments").isJsonArray()) {
                        for (JsonElement el : d.getAsJsonArray("departments")) {
                            JsonObject o = el.getAsJsonObject();
                            departmentList.add(new IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.has("name") ? o.get("name").getAsString() : "",
                                    o.has("code") ? o.get("code").getAsString() : "",
                                    0
                            ));
                        }
                    }
                    setSpinner(spAddDepartment, departmentList);

                    // Programs
                    programList.clear();
                    if (d.has("programs") && d.get("programs").isJsonArray()) {
                        for (JsonElement el : d.getAsJsonArray("programs")) {
                            JsonObject o = el.getAsJsonObject();
                            programList.add(new IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.has("name") ? o.get("name").getAsString() : "",
                                    o.has("code") ? o.get("code").getAsString() : "",
                                    o.has("department_id") ? o.get("department_id").getAsInt() : 0
                            ));
                        }
                    }
                    setSpinner(spAddProgram, programList);

                    // Academic Years
                    academicYearList.clear();
                    if (d.has("academic_years") && d.get("academic_years").isJsonArray()) {
                        for (JsonElement el : d.getAsJsonArray("academic_years")) {
                            JsonObject o = el.getAsJsonObject();
                            academicYearList.add(new IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.has("name") ? o.get("name").getAsString() : "",
                                    "",
                                    0
                            ));
                        }
                    }
                    setSpinner(spAddAcademicYear, academicYearList);

                    // Semesters
                    semesterList.clear();
                    semesterList.add(new IdNameItem(0, "None", "", 0));
                    if (d.has("semesters") && d.get("semesters").isJsonArray()) {
                        for (JsonElement el : d.getAsJsonArray("semesters")) {
                            JsonObject o = el.getAsJsonObject();
                            semesterList.add(new IdNameItem(
                                    o.get("id").getAsInt(),
                                    o.has("name") ? o.get("name").getAsString() : "Semester " + o.get("semester_number").getAsInt(),
                                    "",
                                    o.has("academic_year_id") ? o.get("academic_year_id").getAsInt() : 0
                            ));
                        }
                    }
                    setSpinner(spAddSemester, semesterList);
                } else {
                    if (!isFinishing()) {
                        Toast.makeText(AddStudentActivity.this, "Server Error: Failed to load lookups", Toast.LENGTH_SHORT).show();
                        Log.e("AddStudent", "Lookups failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(AddStudentActivity.this, "Failed to load academic options", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setSpinner(Spinner sp, List<IdNameItem> items) {
        if (sp == null) return;
        ArrayAdapter<IdNameItem> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnCancelAddStudent != null) {
            btnCancelAddStudent.setOnClickListener(v -> finish());
        }

        if (btnSaveStudent != null) {
            btnSaveStudent.setOnClickListener(v -> attemptSaveStudent());
        }
    }

    private void attemptSaveStudent() {
        clearErrors();

        String khmerName = getText(etAddStudentKhmerName);
        String englishName = getText(etAddStudentEnglishName);
        String dob = getText(etAddStudentDob);
        String identifier = getText(etAddStudentIdentifier);
        String password = getText(etAddStudentPassword);
        String pob = getText(etAddStudentPob);
        String address = getText(etAddStudentAddress);

        boolean valid = true;

        if (khmerName.isEmpty()) {
            if (tilAddStudentKhmerName != null) tilAddStudentKhmerName.setError("Khmer Name is required");
            valid = false;
        }

        if (englishName.isEmpty()) {
            if (tilAddStudentEnglishName != null) tilAddStudentEnglishName.setError("English Name is required");
            valid = false;
        }

        if (dob.isEmpty()) {
            if (tilAddStudentDob != null) tilAddStudentDob.setError("Date of Birth is required (YYYY-MM-DD)");
            valid = false;
        }

        if (identifier.isEmpty()) {
            if (tilAddStudentIdentifier != null) tilAddStudentIdentifier.setError("Student ID / Username is required");
            valid = false;
        }

        if (!password.isEmpty() && password.length() < 8) {
            if (tilAddStudentPassword != null) tilAddStudentPassword.setError("Password must be at least 8 characters");
            valid = false;
        }

        IdNameItem selectedDept = spAddDepartment != null && spAddDepartment.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddDepartment.getSelectedItem() : null;
        IdNameItem selectedProg = spAddProgram != null && spAddProgram.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddProgram.getSelectedItem() : null;
        IdNameItem selectedYear = spAddAcademicYear != null && spAddAcademicYear.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddAcademicYear.getSelectedItem() : null;
        IdNameItem selectedSem = spAddSemester != null && spAddSemester.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddSemester.getSelectedItem() : null;

        if (selectedDept == null) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedProg == null) {
            Toast.makeText(this, "Please select a program", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedYear == null) {
            Toast.makeText(this, "Please select an academic year", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) return;

        int yearLevel = spAddYearLevel != null ? (spAddYearLevel.getSelectedItemPosition() + 1) : 1;
        String gender = spAddStudentGender != null ? spAddStudentGender.getSelectedItem().toString() : "Male";

        JsonObject body = new JsonObject();
        body.addProperty("khmer_name", khmerName);
        body.addProperty("english_name", englishName);
        body.addProperty("gender", gender);
        body.addProperty("date_of_birth", dob);
        body.addProperty("place_of_birth", pob);
        body.addProperty("current_address", address);
        body.addProperty("student_identifier", identifier);
        if (!password.isEmpty()) body.addProperty("password", password);

        body.addProperty("department_id", selectedDept.id);
        body.addProperty("program_id", selectedProg.id);
        body.addProperty("year_level", yearLevel);
        body.addProperty("academic_year_id", selectedYear.id);
        if (selectedSem != null && selectedSem.id > 0) {
            body.addProperty("semester_id", selectedSem.id);
        }

        // Family Information
        body.addProperty("father_khmer_name", getText(etFatherKhmerName));
        body.addProperty("father_english_name", getText(etFatherEnglishName));
        body.addProperty("father_phone", getText(etFatherPhone));
        body.addProperty("mother_khmer_name", getText(etMotherKhmerName));
        body.addProperty("mother_english_name", getText(etMotherEnglishName));
        body.addProperty("mother_phone", getText(etMotherPhone));

        // Emergency Contact
        body.addProperty("emergency_contact_name", getText(etEmergencyName));
        body.addProperty("emergency_relationship", getText(etEmergencyRelationship));
        body.addProperty("emergency_contact_phone", getText(etEmergencyPhone));
        body.addProperty("emergency_contact_address", getText(etEmergencyAddress));

        if (btnSaveStudent != null) btnSaveStudent.setEnabled(false);

        ApiClient.getApiService(this).addStudentUniversity(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                    String tempPass = d != null && d.has("temporary_password") && !d.get("temporary_password").isJsonNull()
                            ? d.get("temporary_password").getAsString() : null;

                    if (tempPass != null && !tempPass.isEmpty()) {
                        new MaterialAlertDialogBuilder(AddStudentActivity.this)
                                .setTitle("Student Created Successfully")
                                .setMessage("Student ID / Username: " + identifier + "\nTemporary Password: " + tempPass
                                        + "\n\nPlease record this password. It will not be shown again.")
                                .setPositiveButton("Done", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(AddStudentActivity.this, "Student created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    String msg = "Failed to save student (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("errors")) {
                                JsonObject errors = err.getAsJsonObject("errors");
                                if (errors.entrySet().iterator().hasNext()) {
                                    msg = errors.entrySet().iterator().next().getValue().getAsJsonArray().get(0).getAsString();
                                }
                            } else if (err.has("message")) {
                                msg = err.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AddStudentActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);
                Toast.makeText(AddStudentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearErrors() {
        if (tilAddStudentKhmerName != null) tilAddStudentKhmerName.setError(null);
        if (tilAddStudentEnglishName != null) tilAddStudentEnglishName.setError(null);
        if (tilAddStudentDob != null) tilAddStudentDob.setError(null);
        if (tilAddStudentIdentifier != null) tilAddStudentIdentifier.setError(null);
        if (tilAddStudentPassword != null) tilAddStudentPassword.setError(null);
    }

    private String getText(EditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }
}
