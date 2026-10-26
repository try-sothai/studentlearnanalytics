package com.social7.studentlearnanalytics.admin.reports;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class StudentReportActivity extends AppCompatActivity {

    private Spinner spStudentReportYear, spStudentReportSemester;
    private LineChart chartEnrollmentTrend;
    private PieChart chartStudentDistribution;
    private MaterialButton btnExportStudentReport, btnViewStudentReportDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_report);

        initViews();
        setupFilters();
        setupLineChart();
        setupPieChart();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackStudentReport);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        spStudentReportYear = findViewById(R.id.spStudentReportYear);
        spStudentReportSemester = findViewById(R.id.spStudentReportSemester);
        chartEnrollmentTrend = findViewById(R.id.chartEnrollmentTrend);
        chartStudentDistribution = findViewById(R.id.chartStudentDistribution);

        btnExportStudentReport = findViewById(R.id.btnExportStudentReport);
        btnViewStudentReportDetails = findViewById(R.id.btnViewStudentReportDetails);
    }

    private void setupFilters() {
        String[] years = {"2024 - 2025", "2023 - 2024", "2022 - 2023"};
        String[] semesters = {"Semester 1", "Semester 2", "All Semesters"};

        setSpinnerAdapter(spStudentReportYear, years);
        setSpinnerAdapter(spStudentReportSemester, semesters);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupLineChart() {
        if (chartEnrollmentTrend == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 2400f));
        entries.add(new Entry(1, 2550f));
        entries.add(new Entry(2, 2620f));
        entries.add(new Entry(3, 2700f));
        entries.add(new Entry(4, 2780f));
        entries.add(new Entry(5, 2845f));

        LineDataSet dataSet = new LineDataSet(entries, "Enrollment");
        dataSet.setColor(Color.parseColor("#3155F5"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#3155F5"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartEnrollmentTrend.setData(lineData);

        String[] months = {"Sep", "Oct", "Nov", "Dec", "Jan", "Feb"};
        XAxis xAxis = chartEnrollmentTrend.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        chartEnrollmentTrend.getAxisRight().setEnabled(false);
        chartEnrollmentTrend.getDescription().setEnabled(false);
        chartEnrollmentTrend.invalidate();
    }

    private void setupPieChart() {
        if (chartStudentDistribution == null) return;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(35f, "Computer Science"));
        entries.add(new PieEntry(25f, "Business"));
        entries.add(new PieEntry(20f, "Engineering"));
        entries.add(new PieEntry(12f, "Education"));
        entries.add(new PieEntry(8f, "Other"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = {
                Color.parseColor("#3155F5"),
                Color.parseColor("#1260FF"),
                Color.parseColor("#6546F5"),
                Color.parseColor("#16A34A"),
                Color.parseColor("#F59E0B")
        };
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(false);

        chartStudentDistribution.setData(pieData);
        chartStudentDistribution.setHoleRadius(50f);
        chartStudentDistribution.setTransparentCircleRadius(55f);
        chartStudentDistribution.setCenterText("2,845\nStudents");
        chartStudentDistribution.setCenterTextSize(14f);
        chartStudentDistribution.getDescription().setEnabled(false);
        chartStudentDistribution.invalidate();
    }

    private void setupListeners() {
        if (btnExportStudentReport != null) {
            btnExportStudentReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Students");
                startActivity(intent);
            });
        }

        if (btnViewStudentReportDetails != null) {
            btnViewStudentReportDetails.setOnClickListener(v ->
                    Toast.makeText(this, "Displaying detailed student report metrics", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
