package com.social7.studentlearnanalytics.teacher.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Score;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.teacher.assessment.ScoreListAdapter;

import java.util.ArrayList;
import java.util.List;

public class StudentDetailActivity extends AppCompatActivity {

    private TextView tabOverview, tabScores, tabAnalytics, tabHistory;
    private View containerOverview, containerScores, containerAnalytics, containerHistory;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        if (getIntent() != null && getIntent().hasExtra("student")) {
            currentStudent = (Student) getIntent().getSerializableExtra("student");
        }
        if (currentStudent == null) {
            currentStudent = new Student("STU023001", "Sophea", "sophea@university.edu", "Computer Science", "Year 3", 3.25, "Good", 76.5);
        }

        com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Student Details", "", true);

        TextView tvName = findViewById(R.id.tvStudentDetailName);
        TextView tvId = findViewById(R.id.tvStudentDetailId);
        TextView tvDept = findViewById(R.id.tvStudentDetailDept);
        TextView tvEmail = findViewById(R.id.tvDetailEmail);
        TextView tvGPA = findViewById(R.id.tvDetailGPA);

        if (tvName != null) tvName.setText(currentStudent.getName());
        if (tvId != null) tvId.setText(currentStudent.getId());
        if (tvDept != null) tvDept.setText(currentStudent.getDepartment() + " • " + currentStudent.getYear());
        if (tvEmail != null) tvEmail.setText(currentStudent.getEmail());
        if (tvGPA != null) tvGPA.setText(String.format("%.2f", currentStudent.getGpa()) + " / 4.0");

        tabOverview = findViewById(R.id.tabOverview);
        tabScores = findViewById(R.id.tabScores);
        tabAnalytics = findViewById(R.id.tabAnalytics);
        tabHistory = findViewById(R.id.tabHistory);

        containerOverview = findViewById(R.id.containerOverview);
        containerScores = findViewById(R.id.containerScores);
        containerAnalytics = findViewById(R.id.containerAnalytics);
        containerHistory = findViewById(R.id.containerHistory);

        tabOverview.setOnClickListener(v -> selectTab(0));
        tabScores.setOnClickListener(v -> selectTab(1));
        tabAnalytics.setOnClickListener(v -> selectTab(2));
        tabHistory.setOnClickListener(v -> selectTab(3));

        RecyclerView rvScores = findViewById(R.id.rvStudentScores);
        if (rvScores != null) {
            rvScores.setLayoutManager(new LinearLayoutManager(this));
            List<Score> scores = new ArrayList<>();
            scores.add(new Score("1", "Assignment 1", "Sep 15, 2024", 85, 100, "Assignment"));
            scores.add(new Score("2", "Assignment 2", "Oct 10, 2024", 78, 100, "Assignment"));
            scores.add(new Score("3", "Quiz 1", "Oct 20, 2024", 82, 100, "Quiz"));
            scores.add(new Score("4", "Assignment 3", "Nov 05, 2024", 82, 100, "Assignment"));
            scores.add(new Score("5", "Assignment 4", "Nov 28, 2024", 80, 100, "Assignment"));
            scores.add(new Score("6", "Midterm Exam", "Nov 15, 2024", 88, 100, "Exam"));

            ScoreListAdapter adapter = new ScoreListAdapter(this, scores, score -> {});
            rvScores.setAdapter(adapter);
        }

        LineChart chart = findViewById(R.id.lineChartStudentAnalytics);
        if (chart != null) {
            setupStudentChart(chart);
        }

        selectTab(0);
    }

    private void selectTab(int index) {
        resetTabs();
        if (index == 0) {
            containerOverview.setVisibility(View.VISIBLE);
            setTabActive(tabOverview);
        } else if (index == 1) {
            containerScores.setVisibility(View.VISIBLE);
            setTabActive(tabScores);
        } else if (index == 2) {
            containerAnalytics.setVisibility(View.VISIBLE);
            setTabActive(tabAnalytics);
        } else if (index == 3) {
            containerHistory.setVisibility(View.VISIBLE);
            setTabActive(tabHistory);
        }
    }

    private void resetTabs() {
        containerOverview.setVisibility(View.GONE);
        containerScores.setVisibility(View.GONE);
        containerAnalytics.setVisibility(View.GONE);
        containerHistory.setVisibility(View.GONE);

        setTabInactive(tabOverview);
        setTabInactive(tabScores);
        setTabInactive(tabAnalytics);
        setTabInactive(tabHistory);
    }

    private void setTabActive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
    }

    private void setTabInactive(TextView tv) {
        if (tv == null) return;
        tv.setTextColor(Color.parseColor("#7A7D8F"));
        tv.setBackground(null);
    }

    private void setupStudentChart(LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 75f));
        entries.add(new Entry(1, 80f));
        entries.add(new Entry(2, 78f));
        entries.add(new Entry(3, 85f));
        entries.add(new Entry(4, 88f));

        LineDataSet dataSet = new LineDataSet(entries, "Performance");
        dataSet.setColor(Color.parseColor("#5F34F2"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#5F34F2"));

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }
}
