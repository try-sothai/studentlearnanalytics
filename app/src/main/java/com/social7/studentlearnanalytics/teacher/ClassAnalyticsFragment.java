package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.attendance.AttendanceFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClassAnalyticsFragment extends Fragment {

    public ClassAnalyticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_analytics,
                container,
                false
        );

        ImageView btnBack = view.findViewById(R.id.btnTeacherAnalyticsBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        View btnCourseSelector = view.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        View btnSemesterSelector = view.findViewById(R.id.btnSemesterSelector);
        if (btnSemesterSelector != null) {
            btnSemesterSelector.setOnClickListener(this::showSemesterDropdown);
        }

        MaterialCardView cardAtRisk = view.findViewById(R.id.cardMetricAtRisk);
        if (cardAtRisk != null) {
            cardAtRisk.setOnClickListener(v -> navigateToAtRiskScreen());
        }

        View rowInsightAtRisk = view.findViewById(R.id.rowInsightAtRisk);
        if (rowInsightAtRisk != null) {
            rowInsightAtRisk.setOnClickListener(v -> navigateToAtRiskScreen());
        }

        View rowInsightAttendance = view.findViewById(R.id.rowInsightAttendance);
        if (rowInsightAttendance != null) {
            rowInsightAttendance.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new AttendanceFragment());
                }
            });
        }

        BarChart barChart = view.findViewById(R.id.barChartScoreDist);
        if (barChart != null) {
            setupBarChart(barChart);
        }

        LineChart lineChart = view.findViewById(R.id.lineChartAnalyticsTrend);
        if (lineChart != null) {
            setupLineChart(lineChart);
        }

        return view;
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Data Structures");
        popup.getMenu().add(0, 2, 1, "CS102 - Web Development");
        popup.getMenu().add(0, 3, 2, "CS201 - Database Systems");
        popup.show();
    }

    private void showSemesterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Semester 1, 2024");
        popup.getMenu().add(0, 2, 1, "Semester 2, 2023");
        popup.getMenu().add(0, 3, 2, "Semester 1, 2023");
        popup.show();
    }

    private void navigateToAtRiskScreen() {
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).openFragment(new PredictionRiskFragment());
        }
    }

    private void setupBarChart(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 28f));
        entries.add(new BarEntry(1, 32f));
        entries.add(new BarEntry(2, 22f));
        entries.add(new BarEntry(3, 12f));
        entries.add(new BarEntry(4, 6f));

        BarDataSet dataSet = new BarDataSet(entries, "Grade Distribution");
        dataSet.setColors(new int[]{
                Color.parseColor("#2563FF"), // A - Bright Blue
                Color.parseColor("#16C784"), // B - Green
                Color.parseColor("#FF9B16"), // C - Yellow
                Color.parseColor("#F97316"), // D - Orange
                Color.parseColor("#FF4055")  // F - Red
        });
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.parseColor("#10165C"));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f%%", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"A", "B", "C", "D", "F"}));
        xAxis.setTextColor(Color.parseColor("#10165C"));
        xAxis.setTextSize(12f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(40f);
        leftAxis.setTextColor(Color.parseColor("#6676AD"));
        leftAxis.setGridColor(Color.parseColor("#DCE5FF"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f%%", value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void setupLineChart(LineChart chart) {
        List<Entry> classEntries = new ArrayList<>();
        classEntries.add(new Entry(0, 63f));
        classEntries.add(new Entry(1, 70f));
        classEntries.add(new Entry(2, 81f));
        classEntries.add(new Entry(3, 87f));

        List<Entry> targetEntries = new ArrayList<>();
        targetEntries.add(new Entry(0, 54f));
        targetEntries.add(new Entry(1, 60f));
        targetEntries.add(new Entry(2, 68f));
        targetEntries.add(new Entry(3, 77f));

        LineDataSet classSet = new LineDataSet(classEntries, "Class Average");
        classSet.setColor(Color.parseColor("#2563FF"));
        classSet.setLineWidth(2.5f);
        classSet.setCircleColor(Color.parseColor("#2563FF"));
        classSet.setCircleRadius(4f);
        classSet.setDrawValues(false);

        LineDataSet targetSet = new LineDataSet(targetEntries, "Target");
        targetSet.setColor(Color.parseColor("#9333EA"));
        targetSet.setLineWidth(2f);
        targetSet.enableDashedLine(10f, 5f, 0f);
        targetSet.setCircleColor(Color.parseColor("#9333EA"));
        targetSet.setCircleRadius(3.5f);
        targetSet.setDrawValues(false);

        LineData lineData = new LineData(classSet, targetSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Sep", "Oct", "Nov", "Dec"}));
        xAxis.setTextColor(Color.parseColor("#10165C"));
        xAxis.setTextSize(11f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(20f);
        leftAxis.setTextColor(Color.parseColor("#6676AD"));
        leftAxis.setGridColor(Color.parseColor("#DCE5FF"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.parseColor("#10165C"));
        chart.animateX(800);
        chart.invalidate();
    }
}
