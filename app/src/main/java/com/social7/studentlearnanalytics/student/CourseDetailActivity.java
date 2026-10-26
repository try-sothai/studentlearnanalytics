package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private MaterialButton tabOverview, tabStudents, tabAssignments, tabQuizzes, tabExams;
    private LinearLayout layoutOverview, layoutSubContent;
    private TextView tvSubTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        initViews();
        setupCourseData();
        setupTabListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageView btnMoreOptions = findViewById(R.id.btnMoreOptions);
        if (btnMoreOptions != null) btnMoreOptions.setOnClickListener(this::showMoreOptionsPopup);

        tabOverview = findViewById(R.id.tabOverview);
        tabStudents = findViewById(R.id.tabStudents);
        tabAssignments = findViewById(R.id.tabAssignments);
        tabQuizzes = findViewById(R.id.tabQuizzes);
        tabExams = findViewById(R.id.tabExams);

        layoutOverview = findViewById(R.id.layoutOverview);
        layoutSubContent = findViewById(R.id.layoutSubContent);
        tvSubTitle = findViewById(R.id.tvSubTitle);

        TextView btnViewAllStudents = findViewById(R.id.btnViewAllStudents);
        if (btnViewAllStudents != null) {
            btnViewAllStudents.setOnClickListener(v -> {
                Intent intent = new Intent(this, TeacherMainActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showMoreOptionsPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "View Schedule");
        popup.getMenu().add(0, 2, 1, "View Students");
        popup.getMenu().add(0, 3, 2, "View Course Information");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                Toast.makeText(this, "Schedule: Mon, Wed, Fri 08:00–09:30 (Room A101)", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 2) {
                Intent intent = new Intent(this, TeacherMainActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == 3) {
                Toast.makeText(this, "Course Info: CS101 Data Structures • 35 Students", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupCourseData() {
        Course course = (Course) getIntent().getSerializableExtra("course");

        TextView tvCourseTitle = findViewById(R.id.tvCourseTitle);
        TextView tvCourseDept = findViewById(R.id.tvCourseDept);
        TextView tvCourseSubtext = findViewById(R.id.tvCourseSubtext);
        TextView tvStatusBadge = findViewById(R.id.tvStatusBadge);

        TextView tvChipStudentCount = findViewById(R.id.tvChipStudentCount);
        TextView tvChipSemester = findViewById(R.id.tvChipSemester);
        TextView tvChipRoom = findViewById(R.id.tvChipRoom);

        TextView tvPassRateVal = findViewById(R.id.tvPassRateVal);
        TextView tvClassAvgVal = findViewById(R.id.tvClassAvgVal);
        TextView tvTotalStudentsSubtext = findViewById(R.id.tvTotalStudentsSubtext);

        if (course != null) {
            if (tvCourseTitle != null) tvCourseTitle.setText(course.getCode() + " – " + course.getName());
            if (tvCourseDept != null) tvCourseDept.setText(course.getDepartment() != null ? course.getDepartment() : "Computer Science Department");
            if (tvCourseSubtext != null) tvCourseSubtext.setText("Teacher: " + course.getTeacher());
            if (tvStatusBadge != null) tvStatusBadge.setText(course.getStatus());

            if (tvChipStudentCount != null) tvChipStudentCount.setText(course.getStudentCount() + " Students");
            if (tvChipSemester != null) tvChipSemester.setText(course.getSemester());
            if (tvChipRoom != null) tvChipRoom.setText(course.getRoom().isEmpty() ? "Room A101" : course.getRoom());

            if (tvTotalStudentsSubtext != null) tvTotalStudentsSubtext.setText("Total: " + course.getStudentCount() + " Students");
        } else {
            if (tvCourseTitle != null) tvCourseTitle.setText("CS101 – Data Structures");
            if (tvCourseDept != null) tvCourseDept.setText("Computer Science Department");
            if (tvCourseSubtext != null) tvCourseSubtext.setText("Teacher: Mr. Chan");
            if (tvStatusBadge != null) tvStatusBadge.setText("Active");

            if (tvChipStudentCount != null) tvChipStudentCount.setText("35 Students");
            if (tvChipSemester != null) tvChipSemester.setText("Semester 1, 2024");
            if (tvChipRoom != null) tvChipRoom.setText("Room A101");

            if (tvTotalStudentsSubtext != null) tvTotalStudentsSubtext.setText("Total: 35 Students");
        }

        if (tvPassRateVal != null) tvPassRateVal.setText("86%");
        if (tvClassAvgVal != null) tvClassAvgVal.setText("82%");

        PieChart pieChart = findViewById(R.id.pieChartPerformanceSummary);
        if (pieChart != null) {
            setupPieChart(pieChart);
        }
    }

    private void setupPieChart(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(68f, "Passed (B+)"));
        entries.add(new PieEntry(14f, "Passed (C+)"));
        entries.add(new PieEntry(18f, "Failed (F)"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#16B978"), // Passed B+ Green
                Color.parseColor("#FF9B16"), // Passed C+ Orange
                Color.parseColor("#F0445E")  // Failed F Red
        });
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));

        chart.setUsePercentValues(true);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setHoleColor(Color.WHITE);

        chart.setCenterText("82%\nAverage");
        chart.setCenterTextColor(Color.parseColor("#111B67"));
        chart.setCenterTextSize(13f);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setData(pieData);
        chart.animateXY(800, 800);
        chart.invalidate();
    }

    private void setupTabListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabStudents != null) tabStudents.setOnClickListener(v -> selectTab(1));
        if (tabAssignments != null) tabAssignments.setOnClickListener(v -> selectTab(2));
        if (tabQuizzes != null) tabQuizzes.setOnClickListener(v -> selectTab(3));
        if (tabExams != null) tabExams.setOnClickListener(v -> selectTab(4));
    }

    private void selectTab(int index) {
        resetTabStyles();

        int primaryBlue = Color.parseColor("#2463FF");
        int white = Color.WHITE;

        if (index == 0) {
            setButtonActive(tabOverview, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.VISIBLE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.GONE);
        } else if (index == 1) {
            setButtonActive(tabStudents, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText("Enrolled Students (35)");
        } else if (index == 2) {
            setButtonActive(tabAssignments, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText("Course Assignments (6)");
        } else if (index == 3) {
            setButtonActive(tabQuizzes, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText("Course Quizzes (4)");
        } else if (index == 4) {
            setButtonActive(tabExams, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText("Course Exams (2)");
        }
    }

    private void setButtonActive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.white);
    }

    private void resetTabStyles() {
        int surfaceLight = Color.parseColor("#EAF2FF");
        int primaryBlue = Color.parseColor("#2463FF");

        setButtonInactive(tabOverview, surfaceLight, primaryBlue);
        setButtonInactive(tabStudents, surfaceLight, primaryBlue);
        setButtonInactive(tabAssignments, surfaceLight, primaryBlue);
        setButtonInactive(tabQuizzes, surfaceLight, primaryBlue);
        setButtonInactive(tabExams, surfaceLight, primaryBlue);
    }

    private void setButtonInactive(MaterialButton btn, int bgTint, int textColor) {
        if (btn == null) return;
        btn.setBackgroundColor(bgTint);
        btn.setTextColor(textColor);
        btn.setIconTintResource(R.color.teacher_primary_blue);
    }
}
