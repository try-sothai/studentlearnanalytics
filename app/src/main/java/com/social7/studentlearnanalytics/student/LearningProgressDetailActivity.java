package com.social7.studentlearnanalytics.student;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class LearningProgressDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_progress_detail);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Spinner
        Spinner spinner = findViewById(R.id.spinnerSemester);
        String[] semesters = {"Semester 1, 2024", "Semester 2, 2024", "Semester 1, 2025"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters);
        spinner.setAdapter(adapter);

        // Setup LineChart
        LineChart chart = findViewById(R.id.lineChartProgress);
        setupLineChart(chart);
    }

    private void setupLineChart(LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 20f));
        entries.add(new Entry(1, 48f));
        entries.add(new Entry(2, 58f));
        entries.add(new Entry(3, 58f));
        entries.add(new Entry(4, 78f));
        entries.add(new Entry(5, 82f));

        LineDataSet dataSet = new LineDataSet(entries, "Performance Trend");
        dataSet.setColor(Color.parseColor("#4C35E9"));
        dataSet.setCircleColor(Color.parseColor("#4C35E9"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        // Fill gradient
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4C35E9"));
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        
        chart.animateY(1000);
        chart.invalidate();
    }
}
