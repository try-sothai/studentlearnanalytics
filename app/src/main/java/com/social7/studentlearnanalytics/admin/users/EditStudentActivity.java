package com.social7.studentlearnanalytics.admin.users;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.model.User;

public class EditStudentActivity extends AppCompatActivity {

    private Student currentStudent;
    private User currentUser;

    private TextView tvEditAvatarChar, tvEditHeaderName, tvEditHeaderId, tvEditHeaderStatus;
    private TextInputLayout tilEditStudentName, tilEditStudentEmail, tilEditStudentPhone, tilEditStudentDob;
    private EditText etEditStudentName, etEditStudentId, etEditStudentEmail, etEditStudentPhone, etEditStudentDob;

    private Spinner spEditDepartment, spEditProgram, spEditYearLevel, spEditAcademicYear;
    private RadioGroup rgEditAccountStatus;
    private RadioButton rbEditActive, rbEditInactive;
    private MaterialButton btnUpdateStudent, btnCancelEditStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            currentStudent = new Student(
                    "STU23001",
                    "Sophea",
                    "sophea@univ.edu",
                    "Computer Science",
                    "Bachelor of Computer Science",
                    "Year 3",
                    3.25,
                    "Good",
                    85.0,
                    "#EAF0FF",
                    "#3155F5",
                    "CS101"
            );
        }

        initViews();
        setupSpinners();
        prepopulateFields();
        setupListeners();
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

        tilEditStudentName = findViewById(R.id.tilEditStudentName);
        tilEditStudentEmail = findViewById(R.id.tilEditStudentEmail);
        tilEditStudentPhone = findViewById(R.id.tilEditStudentPhone);
        tilEditStudentDob = findViewById(R.id.tilEditStudentDob);

        etEditStudentName = findViewById(R.id.etEditStudentName);
        etEditStudentId = findViewById(R.id.etEditStudentId);
        etEditStudentEmail = findViewById(R.id.etEditStudentEmail);
        etEditStudentPhone = findViewById(R.id.etEditStudentPhone);
        etEditStudentDob = findViewById(R.id.etEditStudentDob);

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

    private void setupSpinners() {
        String[] departments = {"Computer Science", "Information Technology", "Software Engineering", "Business Administration"};
        String[] programs = {"Bachelor of Computer Science", "Bachelor of IT", "Bachelor of Software Engineering"};
        String[] yearLevels = {"Year 1", "Year 2", "Year 3", "Year 4"};
        String[] academicYears = {"2024–2025", "2023–2024", "2022–2023"};

        setSpinnerAdapter(spEditDepartment, departments);
        setSpinnerAdapter(spEditProgram, programs);
        setSpinnerAdapter(spEditYearLevel, yearLevels);
        setSpinnerAdapter(spEditAcademicYear, academicYears);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void prepopulateFields() {
        if (currentStudent == null) return;

        String name = currentStudent.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "S";

        if (tvEditAvatarChar != null) tvEditAvatarChar.setText(initial);
        if (tvEditHeaderName != null) tvEditHeaderName.setText(name);
        if (tvEditHeaderId != null) tvEditHeaderId.setText(currentStudent.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentStudent.getStatus());
        if (tvEditHeaderStatus != null) {
            tvEditHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditStudentName != null) etEditStudentName.setText(name);
        if (etEditStudentId != null) etEditStudentId.setText(currentStudent.getId());
        if (etEditStudentEmail != null) etEditStudentEmail.setText(currentStudent.getEmail());
        if (etEditStudentPhone != null) etEditStudentPhone.setText(currentStudent.getPhone());
        if (etEditStudentDob != null) etEditStudentDob.setText(currentStudent.getDob());

        setSpinnerSelection(spEditDepartment, currentStudent.getDepartment());
        setSpinnerSelection(spEditProgram, currentStudent.getProgram());
        setSpinnerSelection(spEditYearLevel, currentStudent.getYear());
        setSpinnerSelection(spEditAcademicYear, currentStudent.getAcademicYear());

        if (isActive) {
            if (rbEditActive != null) rbEditActive.setChecked(true);
        } else {
            if (rbEditInactive != null) rbEditInactive.setChecked(true);
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
        if (btnCancelEditStudent != null) {
            btnCancelEditStudent.setOnClickListener(v -> finish());
        }

        if (btnUpdateStudent != null) {
            btnUpdateStudent.setOnClickListener(v -> attemptUpdateStudent());
        }
    }

    private void attemptUpdateStudent() {
        if (etEditStudentName == null || etEditStudentEmail == null) return;

        String name = etEditStudentName.getText().toString().trim();
        String email = etEditStudentEmail.getText().toString().trim();
        String phone = etEditStudentPhone != null ? etEditStudentPhone.getText().toString().trim() : "";
        String dob = etEditStudentDob != null ? etEditStudentDob.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilEditStudentName != null) tilEditStudentName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilEditStudentName != null) tilEditStudentName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilEditStudentEmail != null) tilEditStudentEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilEditStudentEmail != null) tilEditStudentEmail.setError(null);
        }

        if (isValid) {
            currentStudent.setName(name);
            currentStudent.setEmail(email);
            if (!phone.isEmpty()) currentStudent.setPhone(phone);
            if (!dob.isEmpty()) currentStudent.setDob(dob);

            if (spEditDepartment != null && spEditDepartment.getSelectedItem() != null) {
                currentStudent.setDepartment(spEditDepartment.getSelectedItem().toString());
            }
            if (spEditProgram != null && spEditProgram.getSelectedItem() != null) {
                currentStudent.setProgram(spEditProgram.getSelectedItem().toString());
            }
            if (spEditYearLevel != null && spEditYearLevel.getSelectedItem() != null) {
                currentStudent.setYear(spEditYearLevel.getSelectedItem().toString());
            }
            if (spEditAcademicYear != null && spEditAcademicYear.getSelectedItem() != null) {
                currentStudent.setAcademicYear(spEditAcademicYear.getSelectedItem().toString());
            }

            boolean isCheckedActive = (rbEditActive != null && rbEditActive.isChecked());
            String newStatus = isCheckedActive ? "Active" : "Inactive";
            currentStudent.setStatus(newStatus);

            if (currentUser != null) {
                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setStatus(newStatus);
            }

            Toast.makeText(this, "Student account updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
