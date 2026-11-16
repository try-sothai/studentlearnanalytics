package com.social7.studentlearnanalytics.teacher.assessment;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.social7.studentlearnanalytics.R;

public class EnterScoreActivity extends AppCompatActivity {

    private TextView tvAssessmentTitleHeader, tvCourseClassHeader;
    private TextInputEditText etStudentName, etAwardedScore, etFeedback;
    private MaterialButton btnSaveScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_score);

        com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Enter Score", "", true);

        tvAssessmentTitleHeader = findViewById(R.id.tvAssessmentTitleHeader);
        tvCourseClassHeader = findViewById(R.id.tvCourseClassHeader);

        etStudentName = findViewById(R.id.etStudentName);
        etAwardedScore = findViewById(R.id.etAwardedScore);
        etFeedback = findViewById(R.id.etFeedback);
        btnSaveScore = findViewById(R.id.btnSaveScore);

        String title = getIntent().getStringExtra("assessment_title");
        String courseCode = getIntent().getStringExtra("course_code");

        if (title != null && !title.isEmpty() && tvAssessmentTitleHeader != null) {
            tvAssessmentTitleHeader.setText(title);
        }

        if (courseCode != null && !courseCode.isEmpty() && tvCourseClassHeader != null) {
            tvCourseClassHeader.setText(tvCourseClassHeader.getContext().getString(R.string.course_2f9b25) + courseCode + " • Max Score: 100 pts");
        }

        if (btnSaveScore != null) {
            btnSaveScore.setOnClickListener(v -> saveScore());
        }
    }

    private void saveScore() {
        String scoreStr = etAwardedScore != null && etAwardedScore.getText() != null ? etAwardedScore.getText().toString().trim() : "88";
        double score = Double.parseDouble(scoreStr.isEmpty() ? "0" : scoreStr);

        if (score > 100.0) {
            Toast.makeText(this, "Awarded score cannot exceed 100 pts!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Student score saved and published successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
