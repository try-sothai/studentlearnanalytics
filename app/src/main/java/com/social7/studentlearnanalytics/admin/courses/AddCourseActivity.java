package com.social7.studentlearnanalytics.admin.courses;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class AddCourseActivity extends AppCompatActivity {

    private TextInputLayout tilAddCourseCode, tilAddCourseName, tilAddCourseCredits;
    private EditText etAddCourseCode, etAddCourseName, etAddCourseCredits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_course);

        View btnBack = findViewById(R.id.btnBackAddCourse);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilAddCourseCode = findViewById(R.id.tilAddCourseCode);
        tilAddCourseName = findViewById(R.id.tilAddCourseName);
        tilAddCourseCredits = findViewById(R.id.tilAddCourseCredits);

        etAddCourseCode = findViewById(R.id.etAddCourseCode);
        etAddCourseName = findViewById(R.id.etAddCourseName);
        etAddCourseCredits = findViewById(R.id.etAddCourseCredits);

        View btnSave = findViewById(R.id.btnSaveCourse);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> attemptSaveCourse());
        }
    }

    private void attemptSaveCourse() {
        if (etAddCourseCode == null || etAddCourseName == null || etAddCourseCredits == null) return;

        String code = etAddCourseCode.getText().toString().trim();
        String name = etAddCourseName.getText().toString().trim();
        String creditsStr = etAddCourseCredits.getText().toString().trim();

        boolean isValid = true;

        if (code.isEmpty()) {
            if (tilAddCourseCode != null) tilAddCourseCode.setError("Course code is required");
            isValid = false;
        } else {
            if (tilAddCourseCode != null) tilAddCourseCode.setError(null);
        }

        if (name.isEmpty()) {
            if (tilAddCourseName != null) tilAddCourseName.setError("Course name is required");
            isValid = false;
        } else {
            if (tilAddCourseName != null) tilAddCourseName.setError(null);
        }

        if (creditsStr.isEmpty()) {
            if (tilAddCourseCredits != null) tilAddCourseCredits.setError("Credits is required");
            isValid = false;
        } else {
            if (tilAddCourseCredits != null) tilAddCourseCredits.setError(null);
        }

        if (isValid) {
            Toast.makeText(this, "Course " + code + " - " + name + " added successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
