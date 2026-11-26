package com.social7.studentlearnanalytics.admin.teachers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper;

public class AddTeacherActivity extends AppCompatActivity {

    private TextInputLayout tilAddTeacherKhmerName, tilAddTeacherEnglishName, tilAddTeacherId, tilAddTeacherEmail, tilAddTeacherPhone, tilAddTeacherDob, tilAddTeacherHireDate, tilAddTeacherOffice, tilAddTeacherBio;
    private EditText etAddTeacherKhmerName, etAddTeacherEnglishName, etAddTeacherId, etAddTeacherEmail, etAddTeacherPhone, etAddTeacherDob, etAddTeacherHireDate, etAddTeacherOffice, etAddTeacherBio;
    private Spinner spAddTeacherDept, spAddTeacherPosition, spAddTeacherWorkType;
    private RadioGroup rgAddTeacherAccountStatus;
    private ChipGroup chipGroupAddTeacherSpecializations;
    private android.widget.TextView tvAddTeacherSpecEmptyHint;
    private MaterialButton btnSelectAddTeacherSpecializations, btnSaveTeacher, btnCancelAddTeacher;
    private TeacherProfileFormHelper profileFormHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_teacher);

        initViews();
        setupSpinners();
        profileFormHelper = new TeacherProfileFormHelper(
                this, etAddTeacherOffice, chipGroupAddTeacherSpecializations, tvAddTeacherSpecEmptyHint);
        profileFormHelper.loadLookups(spAddTeacherDept, null);
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAddTeacher);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilAddTeacherKhmerName = findViewById(R.id.tilAddTeacherKhmerName);
        tilAddTeacherEnglishName = findViewById(R.id.tilAddTeacherEnglishName);
        tilAddTeacherId = findViewById(R.id.tilAddTeacherId);
        tilAddTeacherEmail = findViewById(R.id.tilAddTeacherEmail);
        tilAddTeacherPhone = findViewById(R.id.tilAddTeacherPhone);
        tilAddTeacherDob = findViewById(R.id.tilAddTeacherDob);
        tilAddTeacherHireDate = findViewById(R.id.tilAddTeacherHireDate);
        tilAddTeacherOffice = findViewById(R.id.tilAddTeacherOffice);
        tilAddTeacherBio = findViewById(R.id.tilAddTeacherBio);

        etAddTeacherKhmerName = findViewById(R.id.etAddTeacherKhmerName);
        etAddTeacherEnglishName = findViewById(R.id.etAddTeacherEnglishName);
        etAddTeacherId = findViewById(R.id.etAddTeacherId);
        etAddTeacherEmail = findViewById(R.id.etAddTeacherEmail);
        etAddTeacherPhone = findViewById(R.id.etAddTeacherPhone);
        etAddTeacherDob = findViewById(R.id.etAddTeacherDob);
        etAddTeacherHireDate = findViewById(R.id.etAddTeacherHireDate);
        etAddTeacherOffice = findViewById(R.id.etAddTeacherOffice);
        etAddTeacherBio = findViewById(R.id.etAddTeacherBio);
        DatePickerHelper.attachDatePicker(tilAddTeacherDob, etAddTeacherDob, true);
        DatePickerHelper.attachDatePicker(tilAddTeacherHireDate, etAddTeacherHireDate, true);

        spAddTeacherDept = findViewById(R.id.spAddTeacherDept);
        spAddTeacherPosition = findViewById(R.id.spAddTeacherPosition);
        spAddTeacherWorkType = findViewById(R.id.spAddTeacherWorkType);

        rgAddTeacherAccountStatus = findViewById(R.id.rgAddTeacherAccountStatus);

        chipGroupAddTeacherSpecializations = findViewById(R.id.chipGroupAddTeacherSpecializations);
        tvAddTeacherSpecEmptyHint = findViewById(R.id.tvAddTeacherSpecEmptyHint);
        btnSelectAddTeacherSpecializations = findViewById(R.id.btnSelectAddTeacherSpecializations);

        btnSaveTeacher = findViewById(R.id.btnSaveTeacher);
        btnCancelAddTeacher = findViewById(R.id.btnCancelAddTeacher);
    }

    private void setupSpinners() {
        String[] departments = {"Select faculty", "Computer Science", "Information Technology", "Software Engineering", "Business Administration", "Data Science"};
        String[] positions = {"Select position", "Lecturer", "Assistant Lecturer", "Senior Lecturer", "Associate Professor", "Professor"};
        String[] workTypes = {"Select work type", "Full-time", "Part-time"};

        setSpinnerAdapter(spAddTeacherDept, departments);
        setSpinnerAdapter(spAddTeacherPosition, positions);
        setSpinnerAdapter(spAddTeacherWorkType, workTypes);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupListeners() {
        if (etAddTeacherOffice != null) {
            etAddTeacherOffice.setOnClickListener(v -> profileFormHelper.showOfficeLocationPickerDialog());
        }
        if (tilAddTeacherOffice != null) {
            tilAddTeacherOffice.setEndIconOnClickListener(v -> profileFormHelper.showOfficeLocationPickerDialog());
        }
        if (btnSelectAddTeacherSpecializations != null) {
            btnSelectAddTeacherSpecializations.setOnClickListener(v -> profileFormHelper.showSpecializationPickerDialog());
        }

        if (btnCancelAddTeacher != null) {
            btnCancelAddTeacher.setOnClickListener(v -> finish());
        }

        if (btnSaveTeacher != null) {
            btnSaveTeacher.setOnClickListener(v -> attemptSaveTeacher());
        }
    }

    private void attemptSaveTeacher() {
        if ((etAddTeacherKhmerName == null && etAddTeacherEnglishName == null) || etAddTeacherId == null) return;

        String khmerName = etAddTeacherKhmerName != null
                ? PersonNameDisplayHelper.clean(etAddTeacherKhmerName.getText().toString()) : "";
        String englishName = etAddTeacherEnglishName != null
                ? PersonNameDisplayHelper.clean(etAddTeacherEnglishName.getText().toString()) : "";
        String teacherId = etAddTeacherId.getText().toString().trim().toUpperCase(java.util.Locale.US);
        String email = etAddTeacherEmail != null ? etAddTeacherEmail.getText().toString().trim().toLowerCase(java.util.Locale.US) : "";
        String dob = etAddTeacherDob != null ? etAddTeacherDob.getText().toString().trim() : "";
        String hireDate = etAddTeacherHireDate != null ? etAddTeacherHireDate.getText().toString().trim() : "";

        boolean isValid = true;

        if (!PersonNameDisplayHelper.hasAtLeastOneName(khmerName, englishName)) {
            String msg = getString(R.string.err_name_one_required);
            if (tilAddTeacherKhmerName != null) tilAddTeacherKhmerName.setError(msg);
            if (tilAddTeacherEnglishName != null) tilAddTeacherEnglishName.setError(msg);
            isValid = false;
        } else {
            if (tilAddTeacherKhmerName != null) tilAddTeacherKhmerName.setError(null);
            if (tilAddTeacherEnglishName != null) tilAddTeacherEnglishName.setError(null);
        }

        if (teacherId.isEmpty()) {
            if (tilAddTeacherId != null) tilAddTeacherId.setError("Teacher ID is required");
            isValid = false;
        } else {
            if (tilAddTeacherId != null) tilAddTeacherId.setError(null);
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilAddTeacherEmail != null) tilAddTeacherEmail.setError(getString(R.string.err_invalid_email));
            isValid = false;
        } else {
            if (tilAddTeacherEmail != null) tilAddTeacherEmail.setError(null);
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (tilAddTeacherDob != null) tilAddTeacherDob.setError(getString(R.string.err_invalid_date));
            isValid = false;
        } else {
            if (tilAddTeacherDob != null) tilAddTeacherDob.setError(null);
        }

        if (!hireDate.isEmpty() && !DatePickerHelper.isValidIsoDate(hireDate)) {
            if (tilAddTeacherHireDate != null) tilAddTeacherHireDate.setError(getString(R.string.err_invalid_date));
            isValid = false;
        } else {
            if (tilAddTeacherHireDate != null) tilAddTeacherHireDate.setError(null);
        }

        if (spAddTeacherDept != null && spAddTeacherDept.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.please_select_a_department, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spAddTeacherPosition != null && spAddTeacherPosition.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.please_select_a_position, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spAddTeacherWorkType != null && spAddTeacherWorkType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a work type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            String displayName = PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, "Teacher");
            String selectedDept = spAddTeacherDept != null ? spAddTeacherDept.getSelectedItem().toString() : "Computer Science";
            String selectedPos = spAddTeacherPosition != null ? spAddTeacherPosition.getSelectedItem().toString() : "Lecturer";
            String selectedWorkType = spAddTeacherWorkType != null ? spAddTeacherWorkType.getSelectedItem().toString() : "Full-time";
            String phone = etAddTeacherPhone != null ? etAddTeacherPhone.getText().toString().trim() : "";
            String bio = etAddTeacherBio != null ? etAddTeacherBio.getText().toString().trim() : "";
            String status = (rgAddTeacherAccountStatus != null && rgAddTeacherAccountStatus.getCheckedRadioButtonId() == R.id.rbAddTeacherInactive)
                    ? "inactive" : "active";

            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
            body.addProperty("teacher_id", teacherId);
            body.addProperty("khmer_name", khmerName);
            body.addProperty("english_name", englishName);
            body.addProperty("name", displayName);
            if (!email.isEmpty()) body.addProperty("email", email);
            body.addProperty("department", selectedDept);
            body.addProperty("position", selectedPos);
            body.addProperty("work_type", selectedWorkType);
            body.addProperty("status", status);
            if (!phone.isEmpty()) body.addProperty("phone", phone);
            if (!dob.isEmpty()) body.addProperty("date_of_birth", dob);
            if (!hireDate.isEmpty()) body.addProperty("hire_date", hireDate);
            if (profileFormHelper.getSelectedOfficeId() != null) {
                body.addProperty("office_location_id", profileFormHelper.getSelectedOfficeId());
            } else if (!profileFormHelper.getSelectedOfficeLabel().isEmpty()) {
                body.addProperty("office_location", profileFormHelper.getSelectedOfficeLabel());
            }
            if (!profileFormHelper.getSelectedSpecializations().isEmpty()) {
                body.add("specializations", profileFormHelper.getSelectedSpecializationsJson());
            }
            if (!bio.isEmpty()) {
                body.addProperty("biography", bio);
                body.addProperty("bio", bio);
            }

            if (btnSaveTeacher != null) btnSaveTeacher.setEnabled(false);

            com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).createTeacher(body).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveTeacher != null) btnSaveTeacher.setEnabled(true);

                    if (response.isSuccessful()) {
                        com.google.gson.JsonObject resObj = response.body();
                        String username = teacherId;
                        String initialPass = "TCH@12345";
                        String role = "teacher";
                        String accountStatus = status;
                        if (resObj != null) {
                            if (resObj.has("login_id") && !resObj.get("login_id").isJsonNull()) {
                                username = resObj.get("login_id").getAsString();
                            } else if (resObj.has("username") && !resObj.get("username").isJsonNull()) {
                                username = resObj.get("username").getAsString();
                            }
                            if (resObj.has("initial_password") && !resObj.get("initial_password").isJsonNull()) {
                                initialPass = resObj.get("initial_password").getAsString();
                            }
                            if (resObj.has("role") && !resObj.get("role").isJsonNull()) {
                                role = resObj.get("role").getAsString();
                            }
                            if (resObj.has("account_status") && !resObj.get("account_status").isJsonNull()) {
                                accountStatus = resObj.get("account_status").getAsString();
                            }
                        }
                        showTeacherCredentialsDialog(displayName, username, username, initialPass, role, accountStatus);
                    } else {
                        String errorMsg = "Failed to create teacher (HTTP " + response.code() + ")";
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                com.google.gson.JsonObject errObj = com.google.gson.JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("errors") && errObj.get("errors").isJsonObject()) {
                                    com.google.gson.JsonObject errs = errObj.getAsJsonObject("errors");
                                    for (String key : errs.keySet()) {
                                        if (errs.get(key).isJsonArray() && errs.getAsJsonArray(key).size() > 0) {
                                            errorMsg = errs.getAsJsonArray(key).get(0).getAsString();
                                            break;
                                        }
                                    }
                                } else if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errorMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(AddTeacherActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveTeacher != null) btnSaveTeacher.setEnabled(true);
                    Toast.makeText(AddTeacherActivity.this, "Network error creating teacher", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showTeacherCredentialsDialog(
            String teacherName,
            String teacherId,
            String username,
            String initialPassword,
            String role,
            String accountStatus
    ) {
        String credSummary = "Teacher Name: " + teacherName
                + "\nTeacher ID / Login ID: " + username
                + "\nInitial Password: " + initialPassword
                + "\nRole: " + role
                + "\nAccount Status: " + accountStatus
                + "\nLogin Account: Created & Linked";

        String loginNote = "active".equalsIgnoreCase(accountStatus)
                ? "Teacher can log in immediately using their Teacher ID (" + username + ") and initial password."
                : "Profile and Login Account are created & linked (currently Inactive). Activate the account when ready for login.";

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.teacher_created_successfully)
                .setMessage(credSummary + "\n\n" + loginNote)
                .setPositiveButton("Copy Credentials", (dialog, which) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("SLAS Teacher Credentials", credSummary));
                        Toast.makeText(this, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .setNeutralButton("Save CSV", (dialog, which) -> {
                    String safeCode = teacherId.replaceAll("[^A-Za-z0-9_-]", "_");
                    String filename = "teacher_credentials_" + safeCode + ".csv";
                    String csv = "\uFEFFteacher_id,username,role,initial_password\n"
                            + teacherId + "," + username + "," + role + "," + initialPassword + "\n";
                    byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    okhttp3.ResponseBody body = okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/csv"), bytes);
                    com.social7.studentlearnanalytics.admin.users.StudentExportDialog.saveExportFile(
                            getApplicationContext(), body, filename, "text/csv");
                    finish();
                })
                .setNegativeButton(R.string.done, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
