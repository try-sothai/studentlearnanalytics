package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.student.attendance.AttendanceDetailActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

public class RecommendationDetailActivity extends AppCompatActivity {

    private NestedScrollView nestedScrollViewRec;
    private TextView tvSemesterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_detail);

        nestedScrollViewRec = findViewById(R.id.nestedScrollViewRec);
        tvSemesterText = findViewById(R.id.tvSemesterText);

        resetScrollToTop();

        // Header Back Button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Semester Selector
        View semesterSelector = findViewById(R.id.semesterSelector);
        if (semesterSelector != null) {
            semesterSelector.setOnClickListener(v -> showSemesterDialog());
        }

        // 1. View Performance Details -> Learning Analytics
        View btnViewPerformanceDetails = findViewById(R.id.btnViewPerformanceDetails);
        if (btnViewPerformanceDetails != null) {
            btnViewPerformanceDetails.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }

        // 2. Card 1: View Quiz Details -> QuizDetailActivity
        View btnViewQuizDetails = findViewById(R.id.btnViewQuizDetails);
        if (btnViewQuizDetails != null) {
            btnViewQuizDetails.setOnClickListener(v -> startActivity(new Intent(this, QuizDetailActivity.class)));
        }

        // 3. Card 2: View Attendance -> AttendanceDetailActivity
        View btnViewAttendance = findViewById(R.id.btnViewAttendance);
        if (btnViewAttendance != null) {
            btnViewAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceDetailActivity.class)));
        }

        // 4. Card 3: View Analytics -> Learning Analytics
        View btnViewWeakTopics = findViewById(R.id.btnViewWeakTopics);
        if (btnViewWeakTopics != null) {
            btnViewWeakTopics.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }

        // 5. Card 4: View Schedule -> Material Dialog
        View btnViewSchedule = findViewById(R.id.btnViewSchedule);
        if (btnViewSchedule != null) {
            btnViewSchedule.setOnClickListener(v -> showFeatureDialog("Study Schedule", "Study schedule management and reminders will be available in the upcoming release."));
        }

        // 6. Card 5: View Resources -> Material Dialog
        View btnViewResources = findViewById(R.id.btnViewResources);
        if (btnViewResources != null) {
            btnViewResources.setOnClickListener(v -> showFeatureDialog("Learning Resources", "Personalized study materials and resource library will be available in the upcoming release."));
        }

        // 7. Bottom Progress Banner -> Learning Analytics
        View btnTrackProgress = findViewById(R.id.btnTrackProgress);
        if (btnTrackProgress != null) {
            btnTrackProgress.setOnClickListener(v -> openTab(R.id.nav_analytics));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetScrollToTop();
    }

    private void resetScrollToTop() {
        if (nestedScrollViewRec != null) {
            nestedScrollViewRec.scrollTo(0, 0);
            nestedScrollViewRec.post(() -> nestedScrollViewRec.scrollTo(0, 0));
        }
    }

    private void showSemesterDialog() {
        String[] semesters = {"Semester 1, 2024", "Semester 2, 2024", "Semester 1, 2025"};
        new AlertDialog.Builder(this)
                .setTitle("Select Semester")
                .setItems(semesters, (dialog, which) -> {
                    if (tvSemesterText != null) {
                        tvSemesterText.setText(semesters[which] + " ▾");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFeatureDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openTab(int tabId) {
        Intent intent = new Intent(this, StudentMainActivity.class);
        intent.putExtra("select_tab", tabId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
