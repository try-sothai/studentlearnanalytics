package com.social7.studentlearnanalytics.admin.users;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.User;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStudentActivity extends AppCompatActivity {

    private Student currentStudent;
    private User currentUser;

    private TextView tvEditAvatarChar, tvEditHeaderName, tvEditHeaderId, tvEditHeaderStatus;
    private TextInputLayout tilEditStudentKhmerName, tilEditStudentEnglishName, tilEditStudentEmail, tilEditStudentPhone, tilEditStudentDob;
    private EditText etEditStudentKhmerName, etEditStudentEnglishName, etEditStudentId, etEditStudentEmail, etEditStudentPhone, etEditStudentDob;

    private Spinner spEditDepartment, spEditProgram, spEditYearLevel, spEditAcademicYear;
    private RadioGroup rgEditAccountStatus;
    private RadioButton rbEditActive, rbEditInactive;
    private MaterialButton btnUpdateStudent, btnCancelEditStudent;

    private final List<AddStudentActivity.IdNameItem> departmentList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> allProgramList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> filteredProgramList = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> academicYearList = new ArrayList<>();
    private boolean isSyncingStudentSpinners = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        setContentView(R.layout.activity_edit_student);

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
        setupDefaultSpinners();
        prepopulateFields();
        setupListeners();
        fetchLookupsAndStudentDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        if (departmentList.isEmpty()) {
            fetchLookupsAndStudentDetails();
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackEditStudent);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEditAvatarChar = findViewById(R.id.tvEditAvatarChar);
        tvEditHeaderName = findViewById(R.id.tvEditHeaderName);
        tvEditHeaderId = findViewById(R.id.tvEditHeaderId);
        tvEditHeaderStatus = findViewById(R.id.tvEditHeaderStatus);

        tilEditStudentKhmerName = findViewById(R.id.tilEditStudentKhmerName);
        tilEditStudentEnglishName = findViewById(R.id.tilEditStudentEnglishName);
        tilEditStudentEmail = findViewById(R.id.tilEditStudentEmail);
        tilEditStudentPhone = findViewById(R.id.tilEditStudentPhone);
        tilEditStudentDob = findViewById(R.id.tilEditStudentDob);

        etEditStudentKhmerName = findViewById(R.id.etEditStudentKhmerName);
        etEditStudentEnglishName = findViewById(R.id.etEditStudentEnglishName);
        etEditStudentId = findViewById(R.id.etEditStudentId);
        etEditStudentEmail = findViewById(R.id.etEditStudentEmail);
        etEditStudentPhone = findViewById(R.id.etEditStudentPhone);
        etEditStudentDob = findViewById(R.id.etEditStudentDob);
        DatePickerHelper.attachDatePicker(tilEditStudentDob, etEditStudentDob, true);

        spEditDepartment = findViewById(R.id.spEditDepartment);
        spEditProgram = findViewById(R.id.spEditProgram);
        spEditYearLevel = findViewById(R.id.spEditYearLevel);
        spEditAcademicYear = findViewById(R.id.spEditAcademicYear);

        rgEditAccountStatus = findViewById(R.id.rgEditAccountStatus);
        rbEditActive = findViewById(R.id.rbEditActive);
        rbEditInactive = findViewById(R.id.rbEditInactive);

        btnUpdateStudent = findViewById(R.id.btnUpdateStudent);
        btnCancelEditStudent = findViewById(R.id.btnCancelEditStudent);
    }

    private void setupDefaultSpinners() {
        List<String> yearLevels = YearLevelHelper.getFormDropdownOptions(this);
        if (spEditYearLevel != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearLevels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spEditYearLevel.setAdapter(adapter);
        }

        if (spEditDepartment != null) {
            spEditDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isSyncingStudentSpinners) return;
                    if (position >= 0 && position < departmentList.size()) {
                        filterProgramsForDepartment(departmentList.get(position), false);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void filterProgramsForDepartment(AddStudentActivity.IdNameItem dept) {
        filterProgramsForDepartment(dept, false);
    }

    private void filterProgramsForDepartment(AddStudentActivity.IdNameItem dept, boolean preserveSavedMajor) {
        filteredProgramList.clear();
        if (dept == null) {
            filteredProgramList.addAll(allProgramList);
        } else {
            boolean isComputing = "CS".equalsIgnoreCase(dept.code) || "IT".equalsIgnoreCase(dept.code);
            for (AddStudentActivity.IdNameItem prog : allProgramList) {
                if (prog.parentId == dept.id) {
                    filteredProgramList.add(prog);
                } else if (isComputing && ("CS".equalsIgnoreCase(prog.code) || "BSCS".equalsIgnoreCase(prog.code)
                        || "SE".equalsIgnoreCase(prog.code) || "BSE".equalsIgnoreCase(prog.code)
                        || "IT".equalsIgnoreCase(prog.code) || "BSIT".equalsIgnoreCase(prog.code))) {
                    filteredProgramList.add(prog);
                }
            }
            if (filteredProgramList.isEmpty()) {
                filteredProgramList.add(new AddStudentActivity.IdNameItem(0, getString(R.string.no_majors_for_faculty), "", 0));
            }
        }
        if (spEditProgram != null) {
            ArrayAdapter<AddStudentActivity.IdNameItem> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new ArrayList<>(filteredProgramList));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spEditProgram.setAdapter(adapter);
            if (preserveSavedMajor) {
                selectProgramInSpinner();
            } else if (!filteredProgramList.isEmpty()) {
                spEditProgram.setSelection(0);
            }
        }
    }

    private void fetchLookupsAndStudentDetails() {
        ApiClient.getApiService(this).getStudentLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    AcademicDropdownHelper.LookupData parsed = AcademicDropdownHelper.parseLookups(response.body());

                    departmentList.clear();
                    departmentList.addAll(parsed.faculties);
                    if (spEditDepartment != null) {
                        ArrayAdapter<AddStudentActivity.IdNameItem> adapter = new ArrayAdapter<>(EditStudentActivity.this,
                                android.R.layout.simple_spinner_item, new ArrayList<>(departmentList));
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spEditDepartment.setAdapter(adapter);
                    }

                    allProgramList.clear();
                    allProgramList.addAll(parsed.majors);

                    academicYearList.clear();
                    academicYearList.addAll(parsed.academicYears);
                    if (spEditAcademicYear != null) {
                        ArrayAdapter<AddStudentActivity.IdNameItem> adapter = new ArrayAdapter<>(EditStudentActivity.this,
                                android.R.layout.simple_spinner_item, new ArrayList<>(academicYearList));
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spEditAcademicYear.setAdapter(adapter);
                    }

                    syncSpinnersWithCurrentStudent();
                }
                fetchLatestStudentDetails();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                fetchLatestStudentDetails();
            }
        });
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

    private void fetchLatestStudentDetails() {
        String lookupId = resolveLookupUserId();
        if (lookupId.isEmpty()) return;

        ApiClient.getApiService(this).getUserDetails(lookupId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject d = response.body().getAsJsonObject("data");
                    if (d.has("id") && !d.get("id").isJsonNull()) {
                        currentStudent.setUserId(d.get("id").getAsLong());
                    }
                    if (d.has("student_code") && !d.get("student_code").isJsonNull()) {
                        currentStudent.setId(d.get("student_code").getAsString());
                    }
                    String khmerName = d.has("khmer_name") && !d.get("khmer_name").isJsonNull()
                            ? d.get("khmer_name").getAsString() : "";
                    String englishName = d.has("english_name") && !d.get("english_name").isJsonNull()
                            ? d.get("english_name").getAsString() : "";
                    String fallbackName = d.has("name") && !d.get("name").isJsonNull()
                            ? d.get("name").getAsString() : currentStudent.getName();
                    currentStudent.setKhmerName(khmerName);
                    currentStudent.setEnglishName(englishName);
                    currentStudent.setName(PersonNameDisplayHelper.resolveDisplayName(
                            englishName, khmerName, fallbackName));
                    String rawEmail = d.has("raw_email") && !d.get("raw_email").isJsonNull() ? d.get("raw_email").getAsString() : "";
                    currentStudent.setEmail(rawEmail);

                    if (d.has("phone") && !d.get("phone").isJsonNull()) {
                        currentStudent.setPhone(d.get("phone").getAsString());
                    }
                    if (d.has("date_of_birth") && !d.get("date_of_birth").isJsonNull()) {
                        currentStudent.setDob(d.get("date_of_birth").getAsString());
                    }
                    if (d.has("department_id") && !d.get("department_id").isJsonNull()) {
                        currentStudent.setDepartmentId(d.get("department_id").getAsInt());
                    }
                    if (d.has("department_name") && !d.get("department_name").isJsonNull()) {
                        currentStudent.setDepartment(d.get("department_name").getAsString());
                    }
                    if (d.has("program_id") && !d.get("program_id").isJsonNull()) {
                        currentStudent.setProgramId(d.get("program_id").getAsInt());
                    }
                    if (d.has("program_name") && !d.get("program_name").isJsonNull()) {
                        currentStudent.setProgram(d.get("program_name").getAsString());
                    }
                    if (d.has("year_level") && !d.get("year_level").isJsonNull()) {
                        Integer ylParsed = YearLevelHelper.parse(d.get("year_level").getAsString());
                        if (ylParsed != null) {
                            currentStudent.setYearLevel(ylParsed);
                            currentStudent.setYear("Year " + ylParsed);
                        }
                    }
                    if (d.has("academic_year_id") && !d.get("academic_year_id").isJsonNull()) {
                        currentStudent.setAcademicYearId(d.get("academic_year_id").getAsInt());
                    }
                    if (d.has("academic_year_name") && !d.get("academic_year_name").isJsonNull()) {
                        currentStudent.setAcademicYear(d.get("academic_year_name").getAsString());
                    }
                    if (d.has("status") && !d.get("status").isJsonNull()) {
                        String st = d.get("status").getAsString();
                        currentStudent.setStatus(("active".equalsIgnoreCase(st) || "enrolled".equalsIgnoreCase(st)) ? "Active" : "Inactive");
                    }

                    prepopulateFields();
                    syncSpinnersWithCurrentStudent();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void prepopulateFields() {
        if (currentStudent == null) return;

        String khmerName = PersonNameDisplayHelper.clean(currentStudent.getKhmerName());
        String englishName = PersonNameDisplayHelper.clean(currentStudent.getEnglishName());
        String name = PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, currentStudent.getName());
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "S";

        if (tvEditAvatarChar != null) tvEditAvatarChar.setText(initial);
        if (tvEditHeaderName != null) tvEditHeaderName.setText(name);
        if (tvEditHeaderId != null) tvEditHeaderId.setText(currentStudent.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus()) || "enrolled".equalsIgnoreCase(currentStudent.getStatus());
        if (tvEditHeaderStatus != null) {
            tvEditHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditStudentKhmerName != null) etEditStudentKhmerName.setText(khmerName);
        if (etEditStudentEnglishName != null) etEditStudentEnglishName.setText(englishName);
        if (etEditStudentId != null) etEditStudentId.setText(currentStudent.getId());
        if (etEditStudentEmail != null) {
            String em = currentStudent.getEmail();
            etEditStudentEmail.setText(em != null && em.contains("@") ? em : "");
        }
        if (etEditStudentPhone != null) {
            String ph = currentStudent.getPhone();
            etEditStudentPhone.setText("N/A".equalsIgnoreCase(ph) ? "" : ph);
        }
        if (etEditStudentDob != null) {
            String dob = currentStudent.getDob();
            etEditStudentDob.setText("N/A".equalsIgnoreCase(dob) ? "" : dob);
        }

        if (spEditYearLevel != null) {
            Object rawYear = currentStudent.getYearLevel() != null ? currentStudent.getYearLevel() : currentStudent.getYear();
            spEditYearLevel.setSelection(YearLevelHelper.spinnerIndexFor(rawYear, false));
        }

        if (isActive) {
            if (rbEditActive != null) rbEditActive.setChecked(true);
        } else {
            if (rbEditInactive != null) rbEditInactive.setChecked(true);
        }
    }

    private void syncSpinnersWithCurrentStudent() {
        if (currentStudent == null) return;

        if (spEditDepartment != null && !departmentList.isEmpty()) {
            int targetIdx = 0;
            for (int i = 0; i < departmentList.size(); i++) {
                AddStudentActivity.IdNameItem item = departmentList.get(i);
                if ((currentStudent.getDepartmentId() != null && item.id == currentStudent.getDepartmentId())
                        || item.name.equalsIgnoreCase(currentStudent.getDepartment())
                        || item.code.equalsIgnoreCase(currentStudent.getDepartment())) {
                    targetIdx = i;
                    break;
                }
            }
            isSyncingStudentSpinners = true;
            spEditDepartment.setSelection(targetIdx, false);
            filterProgramsForDepartment(departmentList.get(targetIdx), true);
            spEditDepartment.post(() -> isSyncingStudentSpinners = false);
        }

        if (spEditAcademicYear != null && !academicYearList.isEmpty()) {
            for (int i = 0; i < academicYearList.size(); i++) {
                AddStudentActivity.IdNameItem item = academicYearList.get(i);
                if ((currentStudent.getAcademicYearId() != null && item.id == currentStudent.getAcademicYearId())
                        || item.name.equalsIgnoreCase(currentStudent.getAcademicYear().replace("–", "-"))) {
                    spEditAcademicYear.setSelection(i);
                    break;
                }
            }
        }
    }

    private void selectProgramInSpinner() {
        if (spEditProgram == null || filteredProgramList.isEmpty() || currentStudent == null) return;
        for (int i = 0; i < filteredProgramList.size(); i++) {
            AddStudentActivity.IdNameItem item = filteredProgramList.get(i);
            if ((currentStudent.getProgramId() != null && item.id == currentStudent.getProgramId())
                    || item.name.equalsIgnoreCase(currentStudent.getProgram())
                    || item.code.equalsIgnoreCase(currentStudent.getProgram())) {
                spEditProgram.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        if (btnCancelEditStudent != null) {
            btnCancelEditStudent.setOnClickListener(v -> finish());
        }

        if (btnUpdateStudent != null) {
            btnUpdateStudent.setOnClickListener(v -> attemptUpdateStudent());
        }
    }

    private void attemptUpdateStudent() {
        if (etEditStudentKhmerName == null && etEditStudentEnglishName == null) return;

        String khmerName = etEditStudentKhmerName != null
                ? PersonNameDisplayHelper.clean(etEditStudentKhmerName.getText().toString()) : "";
        String englishName = etEditStudentEnglishName != null
                ? PersonNameDisplayHelper.clean(etEditStudentEnglishName.getText().toString()) : "";
        String studentCode = etEditStudentId != null ? etEditStudentId.getText().toString().trim() : currentStudent.getId();
        String email = etEditStudentEmail != null ? etEditStudentEmail.getText().toString().trim() : "";
        String phone = etEditStudentPhone != null ? etEditStudentPhone.getText().toString().trim() : "";
        String dob = etEditStudentDob != null ? etEditStudentDob.getText().toString().trim() : "";

        if (!PersonNameDisplayHelper.hasAtLeastOneName(khmerName, englishName)) {
            String msg = getString(R.string.err_name_one_required);
            if (tilEditStudentKhmerName != null) tilEditStudentKhmerName.setError(msg);
            if (tilEditStudentEnglishName != null) tilEditStudentEnglishName.setError(msg);
            return;
        } else {
            if (tilEditStudentKhmerName != null) tilEditStudentKhmerName.setError(null);
            if (tilEditStudentEnglishName != null) tilEditStudentEnglishName.setError(null);
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEditStudentEmail != null) tilEditStudentEmail.setError(getString(R.string.err_invalid_email));
            return;
        } else {
            if (tilEditStudentEmail != null) tilEditStudentEmail.setError(null);
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (tilEditStudentDob != null) tilEditStudentDob.setError(getString(R.string.err_invalid_date));
            return;
        } else {
            if (tilEditStudentDob != null) tilEditStudentDob.setError(null);
        }

        String displayName = PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, "Student");
        boolean isCheckedActive = (rbEditActive == null || rbEditActive.isChecked());
        String apiStatus = isCheckedActive ? "active" : "inactive";

        JsonObject body = new JsonObject();
        body.addProperty("khmer_name", khmerName);
        body.addProperty("english_name", englishName);
        body.addProperty("name", displayName);
        if (!studentCode.isEmpty()) {
            body.addProperty("student_code", studentCode);
            body.addProperty("student_identifier", studentCode);
        }
        body.addProperty("email", email);
        body.addProperty("phone", phone);
        body.addProperty("date_of_birth", dob);
        body.addProperty("status", apiStatus);

        final AddStudentActivity.IdNameItem selectedDept = (spEditDepartment != null && spEditDepartment.getSelectedItem() instanceof AddStudentActivity.IdNameItem)
                ? (AddStudentActivity.IdNameItem) spEditDepartment.getSelectedItem() : null;
        final AddStudentActivity.IdNameItem selectedProg = (spEditProgram != null && spEditProgram.getSelectedItem() instanceof AddStudentActivity.IdNameItem)
                ? (AddStudentActivity.IdNameItem) spEditProgram.getSelectedItem() : null;

        if (!departmentList.isEmpty()) {
            if (selectedDept == null || selectedDept.id <= 0) {
                Toast.makeText(this, R.string.please_select_a_department, Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedProg == null || selectedProg.id <= 0) {
                Toast.makeText(this, R.string.please_select_a_program, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (selectedDept != null && selectedDept.id > 0) {
            body.addProperty("department_id", selectedDept.id);
            body.addProperty("department", selectedDept.name);
        }

        if (selectedProg != null && selectedProg.id > 0) {
            body.addProperty("program_id", selectedProg.id);
            body.addProperty("program", selectedProg.name);
        }

        if (spEditYearLevel != null && spEditYearLevel.getSelectedItem() != null) {
            String storageYl = YearLevelHelper.toStorageString(spEditYearLevel.getSelectedItem());
            if (storageYl == null) {
                storageYl = YearLevelHelper.toStorageString(spEditYearLevel.getSelectedItemPosition() + 1);
            }
            if (storageYl == null) {
                Toast.makeText(this, R.string.err_invalid_year_level, Toast.LENGTH_SHORT).show();
                return;
            }
            body.addProperty("year_level", storageYl);
        }

        if (spEditAcademicYear != null && spEditAcademicYear.getSelectedItem() instanceof AddStudentActivity.IdNameItem) {
            AddStudentActivity.IdNameItem ay = (AddStudentActivity.IdNameItem) spEditAcademicYear.getSelectedItem();
            body.addProperty("academic_year_id", ay.id);
            body.addProperty("academic_year", ay.name);
        }

        String lookupId = resolveLookupUserId();
        if (btnUpdateStudent != null) btnUpdateStudent.setEnabled(false);

        ApiClient.getApiService(this).updateUser(lookupId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (btnUpdateStudent != null) btnUpdateStudent.setEnabled(true);

                if (response.isSuccessful()) {
                    currentStudent.setKhmerName(khmerName);
                    currentStudent.setEnglishName(englishName);
                    currentStudent.setName(displayName);
                    currentStudent.setEmail(email);
                    if (!phone.isEmpty()) currentStudent.setPhone(phone);
                    currentStudent.setDob(dob);
                    currentStudent.setStatus(isCheckedActive ? "Active" : "Inactive");
                    if (selectedDept != null && selectedDept.id > 0) {
                        currentStudent.setDepartmentId(selectedDept.id);
                        currentStudent.setDepartment(selectedDept.name);
                    }
                    if (selectedProg != null && selectedProg.id > 0) {
                        currentStudent.setProgramId(selectedProg.id);
                        currentStudent.setProgram(selectedProg.name);
                    }

                    if (currentUser != null) {
                        currentUser.setName(displayName);
                        currentUser.setEmail(email);
                        currentUser.setStatus(currentStudent.getStatus());
                    }

                    Toast.makeText(EditStudentActivity.this, R.string.student_account_updated_successfully, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = "Failed to update student (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(EditStudentActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (btnUpdateStudent != null) btnUpdateStudent.setEnabled(true);
                Toast.makeText(EditStudentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
