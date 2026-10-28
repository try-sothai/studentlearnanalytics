package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class AddStudentActivity extends AppCompatActivity {

    private TextInputLayout tilAddStudentName, tilAddStudentId, tilAddStudentEmail, tilAddStudentPhone, tilAddStudentDob;
    private EditText etAddStudentName, etAddStudentId, etAddStudentEmail, etAddStudentPhone, etAddStudentDob;
    private Spinner spAddDepartment, spAddProgram, spAddYearLevel, spAddAcademicYear;
    private RadioGroup rgAddAccountStatus;
    private MaterialButton btnSaveStudent, btnCancelAddStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_user);

        initViews();
        setupSpinners();
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
        ImageView btnBack = findViewById(R.id.btnBackAddStudent);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilAddStudentName = findViewById(R.id.tilAddStudentName);
        tilAddStudentId = findViewById(R.id.tilAddStudentId);
        tilAddStudentEmail = findViewById(R.id.tilAddStudentEmail);
        tilAddStudentPhone = findViewById(R.id.tilAddStudentPhone);
        tilAddStudentDob = findViewById(R.id.tilAddStudentDob);

        etAddStudentName = findViewById(R.id.etAddStudentName);
        etAddStudentId = findViewById(R.id.etAddStudentId);
        etAddStudentEmail = findViewById(R.id.etAddStudentEmail);
        etAddStudentPhone = findViewById(R.id.etAddStudentPhone);
        etAddStudentDob = findViewById(R.id.etAddStudentDob);

        spAddDepartment = findViewById(R.id.spAddDepartment);
        spAddProgram = findViewById(R.id.spAddProgram);
        spAddYearLevel = findViewById(R.id.spAddYearLevel);
        spAddAcademicYear = findViewById(R.id.spAddAcademicYear);

        rgAddAccountStatus = findViewById(R.id.rgAddAccountStatus);

        btnSaveStudent = findViewById(R.id.btnSaveStudent);
        btnCancelAddStudent = findViewById(R.id.btnCancelAddStudent);
    }

    private void setupSpinners() {
        String[] departments = {"Select department", "Computer Science", "Information Technology", "Software Engineering", "Business Administration"};
        String[] programs = {"Select program", "Bachelor of Computer Science", "Bachelor of IT", "Bachelor of Software Engineering"};
        String[] yearLevels = {"Select year level", "Year 1", "Year 2", "Year 3", "Year 4"};
        String[] academicYears = {"Select academic year", "2024–2025", "2023–2024", "2022–2023"};

        setSpinnerAdapter(spAddDepartment, departments);
        setSpinnerAdapter(spAddProgram, programs);
        setSpinnerAdapter(spAddYearLevel, yearLevels);
        setSpinnerAdapter(spAddAcademicYear, academicYears);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
        if (etAddStudentName == null || etAddStudentId == null || etAddStudentEmail == null) return;

        String name = etAddStudentName.getText().toString().trim();
        String studentId = etAddStudentId.getText().toString().trim();
        String email = etAddStudentEmail.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilAddStudentName != null) tilAddStudentName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilAddStudentName != null) tilAddStudentName.setError(null);
        }

        if (studentId.isEmpty()) {
            if (tilAddStudentId != null) tilAddStudentId.setError("Student ID is required");
            isValid = false;
        } else {
            if (tilAddStudentId != null) tilAddStudentId.setError(null);
        }

        if (email.isEmpty()) {
            if (tilAddStudentEmail != null) tilAddStudentEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilAddStudentEmail != null) tilAddStudentEmail.setError(null);
        }

        if (spAddDepartment != null && spAddDepartment.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            String firstName = name;
            String lastName = "";
            if (name.contains(" ")) {
                int idx = name.indexOf(" ");
                firstName = name.substring(0, idx).trim();
                lastName = name.substring(idx + 1).trim();
            }

            String selectedDept = spAddDepartment != null ? spAddDepartment.getSelectedItem().toString() : "Computer Science";

            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
            body.addProperty("student_id", studentId);
            body.addProperty("first_name", firstName);
            body.addProperty("last_name", !lastName.isEmpty() ? lastName : firstName);
            body.addProperty("gender", "Male");
            body.addProperty("email", email);
            body.addProperty("password", "12345678");
            body.addProperty("password_confirmation", "12345678");
            body.addProperty("department", selectedDept);

            if (btnSaveStudent != null) btnSaveStudent.setEnabled(false);

            com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).createStudent(body).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(AddStudentActivity.this, "Student " + name + " created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to create student (HTTP " + response.code() + ")";
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                com.google.gson.JsonObject errObj = com.google.gson.JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errorMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(AddStudentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);
                    Toast.makeText(AddStudentActivity.this, "Network error creating student", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
