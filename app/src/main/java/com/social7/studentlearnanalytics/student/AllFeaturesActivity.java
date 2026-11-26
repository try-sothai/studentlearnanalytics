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
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Student")) {
            return;
        }
        setContentView(R.layout.activity_all_features);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Navigation to all student features
        findViewById(R.id.itemCourses).setOnClickListener(v -> openStudentTab(R.id.nav_courses));
        findViewById(R.id.itemAttendance).setOnClickListener(v -> startActivity(new Intent(this, AttendanceDetailActivity.class)));
        findViewById(R.id.itemAssignments).setOnClickListener(v -> startActivity(new Intent(this, AssessmentDetailActivity.class)));
        findViewById(R.id.itemQuizzes).setOnClickListener(v -> startActivity(new Intent(this, QuizDetailActivity.class)));
        findViewById(R.id.itemExams).setOnClickListener(v -> startActivity(new Intent(this, ExamDetailActivity.class)));
        findViewById(R.id.itemAnalytics).setOnClickListener(v -> startActivity(new Intent(this, OverallPerformanceActivity.class)));
        findViewById(R.id.itemRecommendations).setOnClickListener(v -> startActivity(new Intent(
                this,
                com.social7.studentlearnanalytics.student.recommendations.SmartRecommendationsActivity.class
        )));
        findViewById(R.id.itemNotifications).setOnClickListener(v -> openStudentTab(R.id.nav_notifications));
        findViewById(R.id.itemReports).setOnClickListener(v -> startActivity(new Intent(this, LearningReportActivity.class)));
    }

    private void openStudentTab(int tabId) {
        Intent intent = new Intent(this, StudentMainActivity.class);
        intent.putExtra("select_tab", tabId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
