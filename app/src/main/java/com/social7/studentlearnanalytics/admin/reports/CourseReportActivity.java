package com.social7.studentlearnanalytics.admin.reports;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class CourseReportActivity extends AppCompatActivity {

    private Spinner spCourseReportYear, spCourseReportSemester;
    private BarChart chartCourseTrendBar;
    private MaterialButton btnExportCourseReport, btnViewCourseReportDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_report);

        initViews();
        setupFilters();
        setupBarChart();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackCourseReport);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        spCourseReportYear = findViewById(R.id.spCourseReportYear);
        spCourseReportSemester = findViewById(R.id.spCourseReportSemester);
        chartCourseTrendBar = findViewById(R.id.chartCourseTrendBar);

        btnExportCourseReport = findViewById(R.id.btnExportCourseReport);
        btnViewCourseReportDetails = findViewById(R.id.btnViewCourseReportDetails);
    }

    private void setupFilters() {
        String[] years = {"2024 - 2025", "2023 - 2024", "2022 - 2023"};
        String[] semesters = {"Semester 1", "Semester 2", "All Semesters"};

        setSpinnerAdapter(spCourseReportYear, years);
        setSpinnerAdapter(spCourseReportSemester, semesters);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupBarChart() {
        if (chartCourseTrendBar == null) return;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 980f));
        entries.add(new BarEntry(1, 1050f));
        entries.add(new BarEntry(2, 1120f));
        entries.add(new BarEntry(3, 1180f));
        entries.add(new BarEntry(4, 1220f));
        entries.add(new BarEntry(5, 1245f));

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.enrollments));
        dataSet.setColor(Color.parseColor("#3155F5"));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chartCourseTrendBar.setData(barData);

        String[] months = {"Sep", "Oct", "Nov", "Dec", "Jan", "Feb"};
        XAxis xAxis = chartCourseTrendBar.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        chartCourseTrendBar.getAxisRight().setEnabled(false);
        chartCourseTrendBar.getDescription().setEnabled(false);
        ChartFontHelper.applyKhmerFont(chartCourseTrendBar.getContext(), chartCourseTrendBar);
        chartCourseTrendBar.invalidate();
    }

    private void setupListeners() {
        if (btnExportCourseReport != null) {
            btnExportCourseReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Courses");
                startActivity(intent);
            });
        }

        if (btnViewCourseReportDetails != null) {
            btnViewCourseReportDetails.setOnClickListener(v ->
                    Toast.makeText(this, getString(R.string.displaying_detailed_course_metrics), Toast.LENGTH_SHORT).show()
            );
        }
    }
}
