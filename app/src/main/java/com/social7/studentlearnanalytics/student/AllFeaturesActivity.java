package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

public class AllFeaturesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_features);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Navigation to other features
        findViewById(R.id.itemAttendance).setOnClickListener(v -> startActivity(new Intent(this, AttendanceDetailActivity.class)));
        findViewById(R.id.itemAssignments).setOnClickListener(v -> startActivity(new Intent(this, AssessmentDetailActivity.class)));
        findViewById(R.id.itemQuizzes).setOnClickListener(v -> startActivity(new Intent(this, QuizDetailActivity.class)));
        findViewById(R.id.itemAnalytics).setOnClickListener(v -> startActivity(new Intent(this, OverallPerformanceActivity.class)));
        findViewById(R.id.itemRecommendations).setOnClickListener(v -> startActivity(new Intent(this, RiskAnalysisActivity.class)));
        findViewById(R.id.itemReports).setOnClickListener(v -> startActivity(new Intent(this, LearningReportActivity.class)));
    }
}
