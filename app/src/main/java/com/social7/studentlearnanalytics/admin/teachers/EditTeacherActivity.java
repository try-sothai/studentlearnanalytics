package com.social7.studentlearnanalytics.admin.teachers;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Teacher;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTeacherActivity extends AppCompatActivity {

    private static final String TAG = "EditTeacherActivity";
    private Teacher currentTeacher;

    private TextView tvEditTeacherAvatarChar, tvEditTeacherHeaderName, tvEditTeacherHeaderId, tvEditTeacherHeaderStatus;
    private TextInputLayout tilEditTeacherName, tilEditTeacherEmail, tilEditTeacherPhone, tilEditTeacherDob, tilEditTeacherOffice, tilEditTeacherBio;
    private EditText etEditTeacherName, etEditTeacherId, etEditTeacherEmail, etEditTeacherPhone, etEditTeacherDob, etEditTeacherOffice, etEditTeacherBio;

    private Spinner spEditTeacherDept, spEditTeacherPosition, spEditTeacherWorkType;
    private RadioGroup rgEditTeacherAccountStatus;
    private RadioButton rbEditTeacherActive, rbEditTeacherInactive;
    private MaterialButton btnUpdateTeacher, btnCancelEditTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        if (getIntent() != null && getIntent().hasExtra("teacher")) {
            currentTeacher = (Teacher) getIntent().getSerializableExtra("teacher");
        }

        if (currentTeacher == null) {
            currentTeacher = new Teacher("TCH001", "Mr. Chan", "chan@univ.edu", "Computer Science", "Lecturer", "Active");
        }

        initViews();
        setupSpinners();
        prepopulateFields();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackEditTeacher);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEditTeacherAvatarChar = findViewById(R.id.tvEditTeacherAvatarChar);
        tvEditTeacherHeaderName = findViewById(R.id.tvEditTeacherHeaderName);
        tvEditTeacherHeaderId = findViewById(R.id.tvEditTeacherHeaderId);
        tvEditTeacherHeaderStatus = findViewById(R.id.tvEditTeacherHeaderStatus);

        tilEditTeacherName = findViewById(R.id.tilEditTeacherName);
        tilEditTeacherEmail = findViewById(R.id.tilEditTeacherEmail);
        tilEditTeacherPhone = findViewById(R.id.tilEditTeacherPhone);
        tilEditTeacherDob = findViewById(R.id.tilEditTeacherDob);
        tilEditTeacherOffice = findViewById(R.id.tilEditTeacherOffice);
        tilEditTeacherBio = findViewById(R.id.tilEditTeacherBio);

        etEditTeacherName = findViewById(R.id.etEditTeacherName);
        etEditTeacherId = findViewById(R.id.etEditTeacherId);
        etEditTeacherEmail = findViewById(R.id.etEditTeacherEmail);
        etEditTeacherPhone = findViewById(R.id.etEditTeacherPhone);
        etEditTeacherDob = findViewById(R.id.etEditTeacherDob);
        etEditTeacherOffice = findViewById(R.id.etEditTeacherOffice);
        etEditTeacherBio = findViewById(R.id.etEditTeacherBio);

        spEditTeacherDept = findViewById(R.id.spEditTeacherDept);
        spEditTeacherPosition = findViewById(R.id.spEditTeacherPosition);
        spEditTeacherWorkType = findViewById(R.id.spEditTeacherWorkType);

        rgEditTeacherAccountStatus = findViewById(R.id.rgEditTeacherAccountStatus);
        rbEditTeacherActive = findViewById(R.id.rbEditTeacherActive);
        rbEditTeacherInactive = findViewById(R.id.rbEditTeacherInactive);

        btnUpdateTeacher = findViewById(R.id.btnUpdateTeacher);
        btnCancelEditTeacher = findViewById(R.id.btnCancelEditTeacher);
    }

    private void setupSpinners() {
        String[] departments = {"Computer Science", "Information Technology", "Software Engineering", "Business Administration", "Data Science"};
        String[] positions = {"Lecturer", "Assistant Lecturer", "Senior Lecturer", "Professor", "Associate Professor"};
        String[] workTypes = {"Full-time", "Part-time"};

        setSpinnerAdapter(spEditTeacherDept, departments);
        setSpinnerAdapter(spEditTeacherPosition, positions);
        setSpinnerAdapter(spEditTeacherWorkType, workTypes);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void prepopulateFields() {
        if (currentTeacher == null) return;

        String name = currentTeacher.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "T";

        if (tvEditTeacherAvatarChar != null) tvEditTeacherAvatarChar.setText(initial);
        if (tvEditTeacherHeaderName != null) tvEditTeacherHeaderName.setText(name);
        if (tvEditTeacherHeaderId != null) tvEditTeacherHeaderId.setText(currentTeacher.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentTeacher.getStatus());
        if (tvEditTeacherHeaderStatus != null) {
            tvEditTeacherHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditTeacherHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditTeacherHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditTeacherName != null) etEditTeacherName.setText(name);
        if (etEditTeacherId != null) etEditTeacherId.setText(currentTeacher.getId());
        if (etEditTeacherEmail != null) etEditTeacherEmail.setText(currentTeacher.getEmail());
        if (etEditTeacherPhone != null) etEditTeacherPhone.setText("N/A".equals(currentTeacher.getPhone()) ? "" : currentTeacher.getPhone());
        if (etEditTeacherDob != null) etEditTeacherDob.setText("N/A".equals(currentTeacher.getDob()) ? "" : currentTeacher.getDob());
        if (etEditTeacherOffice != null) etEditTeacherOffice.setText(currentTeacher.getOfficeLocation());
        if (etEditTeacherBio != null) etEditTeacherBio.setText(currentTeacher.getBio());

        setSpinnerSelection(spEditTeacherDept, currentTeacher.getDepartment());
        setSpinnerSelection(spEditTeacherPosition, currentTeacher.getPosition());
        setSpinnerSelection(spEditTeacherWorkType, currentTeacher.getWorkType());

        if (isActive) {
            if (rbEditTeacherActive != null) rbEditTeacherActive.setChecked(true);
        } else {
            if (rbEditTeacherInactive != null) rbEditTeacherInactive.setChecked(true);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        if (btnCancelEditTeacher != null) {
            btnCancelEditTeacher.setOnClickListener(v -> finish());
        }

        if (btnUpdateTeacher != null) {
            btnUpdateTeacher.setOnClickListener(v -> attemptUpdateTeacher());
        }
    }

    private void attemptUpdateTeacher() {
        if (etEditTeacherName == null || etEditTeacherEmail == null) return;

        String name = etEditTeacherName.getText().toString().trim();
        String email = etEditTeacherEmail.getText().toString().trim();
        String phone = etEditTeacherPhone != null ? etEditTeacherPhone.getText().toString().trim() : "";
        String dob = etEditTeacherDob != null ? etEditTeacherDob.getText().toString().trim() : "";
        String office = etEditTeacherOffice != null ? etEditTeacherOffice.getText().toString().trim() : "";
        String bio = etEditTeacherBio != null ? etEditTeacherBio.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilEditTeacherName != null) tilEditTeacherName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilEditTeacherName != null) tilEditTeacherName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilEditTeacherEmail != null) tilEditTeacherEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilEditTeacherEmail != null) tilEditTeacherEmail.setError(null);
        }

        if (isValid) {
            String dept = spEditTeacherDept != null && spEditTeacherDept.getSelectedItem() != null ? spEditTeacherDept.getSelectedItem().toString() : currentTeacher.getDepartment();
            String pos = spEditTeacherPosition != null && spEditTeacherPosition.getSelectedItem() != null ? spEditTeacherPosition.getSelectedItem().toString() : currentTeacher.getPosition();
            String workType = spEditTeacherWorkType != null && spEditTeacherWorkType.getSelectedItem() != null ? spEditTeacherWorkType.getSelectedItem().toString() : currentTeacher.getWorkType();
            boolean isCheckedActive = (rbEditTeacherActive != null && rbEditTeacherActive.isChecked());
            String status = isCheckedActive ? "active" : "inactive";

            JsonObject payload = new JsonObject();
            payload.addProperty("name", name);
            payload.addProperty("email", email);
            if (!phone.isEmpty()) payload.addProperty("phone", phone);
            if (!dob.isEmpty()) payload.addProperty("date_of_birth", dob);
            if (!office.isEmpty()) payload.addProperty("office_location", office);
            if (!bio.isEmpty()) payload.addProperty("bio", bio);
            payload.addProperty("department_name", dept);
            payload.addProperty("position", pos);
            payload.addProperty("work_type", workType);
            payload.addProperty("status", status);

            String identifier = "1";
            if (currentTeacher != null) {
                if (currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0) {
                    identifier = String.valueOf(currentTeacher.getUserId());
                } else if (currentTeacher.getId() != null) {
                    identifier = currentTeacher.getId();
                }
            }

            if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(false);

            ApiClient.getApiService(this).updateUser(identifier, payload).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(true);
                    if (response.isSuccessful()) {
                        currentTeacher.setName(name);
                        currentTeacher.setEmail(email);
                        if (!phone.isEmpty()) currentTeacher.setPhone(phone);
                        if (!dob.isEmpty()) currentTeacher.setDob(dob);
                        if (!office.isEmpty()) currentTeacher.setOfficeLocation(office);
                        if (!bio.isEmpty()) currentTeacher.setBio(bio);
                        currentTeacher.setDepartment(dept);
                        currentTeacher.setPosition(pos);
                        currentTeacher.setWorkType(workType);
                        currentTeacher.setStatus(isCheckedActive ? "Active" : "Inactive");

                        Toast.makeText(EditTeacherActivity.this, "Teacher account updated successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(EditTeacherActivity.this, "Failed to update teacher: HTTP " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(true);
                    Toast.makeText(EditTeacherActivity.this, "Network error updating teacher: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
