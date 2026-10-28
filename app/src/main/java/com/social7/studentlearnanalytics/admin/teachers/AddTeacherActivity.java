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
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class AddTeacherActivity extends AppCompatActivity {

    private TextInputLayout tilAddTeacherName, tilAddTeacherId, tilAddTeacherEmail, tilAddTeacherPhone, tilAddTeacherDob, tilAddTeacherOffice, tilAddTeacherBio;
    private EditText etAddTeacherName, etAddTeacherId, etAddTeacherEmail, etAddTeacherPhone, etAddTeacherDob, etAddTeacherOffice, etAddTeacherBio;
    private Spinner spAddTeacherDept, spAddTeacherPosition, spAddTeacherWorkType;
    private RadioGroup rgAddTeacherAccountStatus;
    private MaterialButton btnSaveTeacher, btnCancelAddTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_teacher);

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
        ImageView btnBack = findViewById(R.id.btnBackAddTeacher);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilAddTeacherName = findViewById(R.id.tilAddTeacherName);
        tilAddTeacherId = findViewById(R.id.tilAddTeacherId);
        tilAddTeacherEmail = findViewById(R.id.tilAddTeacherEmail);
        tilAddTeacherPhone = findViewById(R.id.tilAddTeacherPhone);
        tilAddTeacherDob = findViewById(R.id.tilAddTeacherDob);
        tilAddTeacherOffice = findViewById(R.id.tilAddTeacherOffice);
        tilAddTeacherBio = findViewById(R.id.tilAddTeacherBio);

        etAddTeacherName = findViewById(R.id.etAddTeacherName);
        etAddTeacherId = findViewById(R.id.etAddTeacherId);
        etAddTeacherEmail = findViewById(R.id.etAddTeacherEmail);
        etAddTeacherPhone = findViewById(R.id.etAddTeacherPhone);
        etAddTeacherDob = findViewById(R.id.etAddTeacherDob);
        etAddTeacherOffice = findViewById(R.id.etAddTeacherOffice);
        etAddTeacherBio = findViewById(R.id.etAddTeacherBio);

        spAddTeacherDept = findViewById(R.id.spAddTeacherDept);
        spAddTeacherPosition = findViewById(R.id.spAddTeacherPosition);
        spAddTeacherWorkType = findViewById(R.id.spAddTeacherWorkType);

        rgAddTeacherAccountStatus = findViewById(R.id.rgAddTeacherAccountStatus);

        btnSaveTeacher = findViewById(R.id.btnSaveTeacher);
        btnCancelAddTeacher = findViewById(R.id.btnCancelAddTeacher);
    }

    private void setupSpinners() {
        String[] departments = {"Select department", "Computer Science", "Information Technology", "Software Engineering", "Data Science", "Data Structures", "Database Systems"};
        String[] positions = {"Select position", "Lecturer", "Assistant Lecturer", "Senior Lecturer", "Professor"};
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
        if (btnCancelAddTeacher != null) {
            btnCancelAddTeacher.setOnClickListener(v -> finish());
        }

        if (btnSaveTeacher != null) {
            btnSaveTeacher.setOnClickListener(v -> attemptSaveTeacher());
        }
    }

    private void attemptSaveTeacher() {
        if (etAddTeacherName == null || etAddTeacherId == null || etAddTeacherEmail == null) return;

        String name = etAddTeacherName.getText().toString().trim();
        String teacherId = etAddTeacherId.getText().toString().trim();
        String email = etAddTeacherEmail.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilAddTeacherName != null) tilAddTeacherName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilAddTeacherName != null) tilAddTeacherName.setError(null);
        }

        if (teacherId.isEmpty()) {
            if (tilAddTeacherId != null) tilAddTeacherId.setError("Teacher ID is required");
            isValid = false;
        } else {
            if (tilAddTeacherId != null) tilAddTeacherId.setError(null);
        }

        if (email.isEmpty()) {
            if (tilAddTeacherEmail != null) tilAddTeacherEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilAddTeacherEmail != null) tilAddTeacherEmail.setError(null);
        }

        if (spAddTeacherDept != null && spAddTeacherDept.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spAddTeacherPosition != null && spAddTeacherPosition.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a position", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (spAddTeacherWorkType != null && spAddTeacherWorkType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a work type", Toast.LENGTH_SHORT).show();
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

            String selectedDept = spAddTeacherDept != null ? spAddTeacherDept.getSelectedItem().toString() : "Computer Science";
            String selectedPos = spAddTeacherPosition != null ? spAddTeacherPosition.getSelectedItem().toString() : "Lecturer";

            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
            body.addProperty("teacher_id", teacherId);
            body.addProperty("first_name", firstName);
            body.addProperty("last_name", !lastName.isEmpty() ? lastName : firstName);
            body.addProperty("email", email);
            body.addProperty("password", "12345678");
            body.addProperty("password_confirmation", "12345678");
            body.addProperty("department", selectedDept);
            body.addProperty("position", selectedPos);

            if (btnSaveTeacher != null) btnSaveTeacher.setEnabled(false);

            com.social7.studentlearnanalytics.network.ApiClient.getApiService(this).createTeacher(body).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.google.gson.JsonObject> call, @androidx.annotation.NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveTeacher != null) btnSaveTeacher.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(AddTeacherActivity.this, "Teacher " + name + " created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to create teacher (HTTP " + response.code() + ")";
                        try (okhttp3.ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                com.google.gson.JsonObject errObj = com.google.gson.JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
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
}
