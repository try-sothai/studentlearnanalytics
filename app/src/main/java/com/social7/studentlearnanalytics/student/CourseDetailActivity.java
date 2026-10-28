package com.social7.studentlearnanalytics.student;

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
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private MaterialButton tabOverview, tabStudents, tabAssignments, tabQuizzes, tabExams;
    private LinearLayout layoutOverview, layoutSubContent;
    private TextView tvSubTitle;
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Student", "Teacher", "Admin")) {
            return;
        }

        setContentView(R.layout.activity_course_detail);

        initViews();
        setupCourseData();
        setupTabListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RoleGuard.checkAccess(this, "Student", "Teacher", "Admin")) {
            return;
        }
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
            btnViewAllStudents.setOnClickListener(v -> selectTab(1));
        }
    }

    private void showMoreOptionsPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "View Schedule");
        popup.getMenu().add(0, 2, 1, "View Students");
        popup.getMenu().add(0, 3, 2, "View Course Information");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                String scheduleText = (currentCourse != null && currentCourse.getSchedule() != null && !currentCourse.getSchedule().trim().isEmpty())
                        ? "Schedule: " + currentCourse.getSchedule() + (currentCourse.getRoom() != null && !currentCourse.getRoom().trim().isEmpty() ? " (" + currentCourse.getRoom() + ")" : "")
                        : "Schedule: Mon, Wed, Fri 08:00–09:30 (Room A101)";
                Toast.makeText(this, scheduleText, Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 2) {
                selectTab(1);
            } else if (item.getItemId() == 3) {
                String infoText = (currentCourse != null)
                        ? "Course Info: " + currentCourse.getCode() + " " + currentCourse.getName() + " • " + (currentCourse.getStudentCount() > 0 ? currentCourse.getStudentCount() : 35) + " Students"
                        : "Course Info: CS101 Data Structures • 35 Students";
                Toast.makeText(this, infoText, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupCourseData() {
        currentCourse = (Course) getIntent().getSerializableExtra("course");

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

        if (currentCourse != null) {
            if (tvCourseTitle != null) tvCourseTitle.setText(currentCourse.getCode() + " – " + currentCourse.getName());
            if (tvCourseDept != null) tvCourseDept.setText(currentCourse.getDepartment() != null && !currentCourse.getDepartment().trim().isEmpty() ? currentCourse.getDepartment() : "Department");
            if (tvCourseSubtext != null) tvCourseSubtext.setText("Teacher: " + (currentCourse.getTeacher() != null && !currentCourse.getTeacher().trim().isEmpty() ? currentCourse.getTeacher() : "Lecturer"));
            if (tvStatusBadge != null) tvStatusBadge.setText(currentCourse.getStatus() != null && !currentCourse.getStatus().trim().isEmpty() ? currentCourse.getStatus() : "Active");

            if (tvChipStudentCount != null) tvChipStudentCount.setText((currentCourse.getStudentCount() > 0 ? currentCourse.getStudentCount() : 35) + " Students");
            if (tvChipSemester != null) tvChipSemester.setText(currentCourse.getSemester() != null && !currentCourse.getSemester().trim().isEmpty() ? currentCourse.getSemester() : "Semester 1, 2024");
            if (tvChipRoom != null) tvChipRoom.setText(currentCourse.getRoom() == null || currentCourse.getRoom().trim().isEmpty() ? "Room A101" : currentCourse.getRoom());

            if (tvTotalStudentsSubtext != null) tvTotalStudentsSubtext.setText("Total: " + (currentCourse.getStudentCount() > 0 ? currentCourse.getStudentCount() : 35) + " Students");

            float avg = currentCourse.getPerformance() > 0 ? currentCourse.getPerformance() : 82f;
            if (tvClassAvgVal != null) tvClassAvgVal.setText(Math.round(avg) + "%");
            if (tvPassRateVal != null) tvPassRateVal.setText(Math.round(Math.min(100f, avg * 1.05f)) + "%");
        } else {
            if (tvCourseTitle != null) tvCourseTitle.setText("Course Overview");
            if (tvCourseDept != null) tvCourseDept.setText("Academic Course Details");
            if (tvCourseSubtext != null) tvCourseSubtext.setText("Teacher: Course Lecturer");
            if (tvStatusBadge != null) tvStatusBadge.setText("Active");

            if (tvChipStudentCount != null) tvChipStudentCount.setText("35 Students");
            if (tvChipSemester != null) tvChipSemester.setText("Current Semester");
            if (tvChipRoom != null) tvChipRoom.setText("Room A101");

            if (tvTotalStudentsSubtext != null) tvTotalStudentsSubtext.setText("Total: 35 Students");
            if (tvPassRateVal != null) tvPassRateVal.setText("--%");
            if (tvClassAvgVal != null) tvClassAvgVal.setText("--%");
        }

        PieChart pieChart = findViewById(R.id.pieChartPerformanceSummary);
        if (pieChart != null) {
            setupPieChart(pieChart, currentCourse != null && currentCourse.getPerformance() > 0 ? currentCourse.getPerformance() : 82f);
        }
    }

    private void setupPieChart(PieChart chart, float avgScore) {
        float passedHigh = Math.max(0f, Math.min(100f, avgScore * 0.8f));
        float passedMid = Math.max(0f, Math.min(100f - passedHigh, avgScore * 0.15f));
        float failed = Math.max(0f, 100f - passedHigh - passedMid);

        List<PieEntry> entries = new ArrayList<>();
        if (passedHigh > 0) entries.add(new PieEntry(passedHigh, "Passed (B+)"));
        if (passedMid > 0) entries.add(new PieEntry(passedMid, "Passed (C+)"));
        if (failed > 0) entries.add(new PieEntry(failed, "Failed (F)"));

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

        chart.setCenterText(Math.round(avgScore) + "%\nAverage");
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

        int studentCount = (currentCourse != null && currentCourse.getStudentCount() > 0) ? currentCourse.getStudentCount() : 35;

        if (index == 0) {
            setButtonActive(tabOverview, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.VISIBLE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.GONE);
        } else if (index == 1) {
            setButtonActive(tabStudents, primaryBlue, white);
            if (layoutOverview != null) layoutOverview.setVisibility(View.GONE);
            if (layoutSubContent != null) layoutSubContent.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setText("Enrolled Students (" + studentCount + ")");
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
