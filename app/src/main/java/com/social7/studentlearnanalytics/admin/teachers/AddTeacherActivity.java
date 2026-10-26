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
        setContentView(R.layout.activity_add_teacher);

        initViews();
        setupSpinners();
        setupListeners();
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
            Toast.makeText(this, "Teacher " + name + " created successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
