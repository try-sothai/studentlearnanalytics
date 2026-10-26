package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardFragment extends Fragment {

    public TeacherDashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_dashboard,
                container,
                false
        );

        ImageView btnTeacherMenu = view.findViewById(R.id.btnTeacherMenu);
        if (btnTeacherMenu != null) {
            btnTeacherMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        View profileContainer = view.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new TeacherProfileFragment());
                }
            });
        }

        View cardTotalStudents = view.findViewById(R.id.cardTotalStudents);
        if (cardTotalStudents != null) {
            cardTotalStudents.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new StudentListFragment());
                }
            });
        }

        View cardMyCourses = view.findViewById(R.id.cardMyCourses);
        if (cardMyCourses != null) {
            cardMyCourses.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new CourseManagementFragment());
                }
            });
        }

        View cardAtRisk = view.findViewById(R.id.cardAtRisk);
        if (cardAtRisk != null) {
            cardAtRisk.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new PredictionRiskFragment());
                }
            });
        }

        View cardPendingScores = view.findViewById(R.id.cardPendingScores);
        if (cardPendingScores != null) {
            cardPendingScores.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new ManageScoresFragment());
                }
            });
        }

        LineChart lineChart = view.findViewById(R.id.lineChartPerformance);
        setupLineChart(lineChart);

        return view;
    }

    private void setupLineChart(LineChart chart) {
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 72f));
        entries.add(new Entry(1, 78f));
        entries.add(new Entry(2, 81f));
        entries.add(new Entry(3, 75f));
        entries.add(new Entry(4, 84f));
        entries.add(new Entry(5, 88f));

        LineDataSet dataSet = new LineDataSet(entries, "Class Average");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#5F34F2"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#5F34F2"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#1D1F2A"));

        dataSet.setDrawFilled(true);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#405F34F2"), Color.parseColor("#005F34F2")}
        );
        dataSet.setFillDrawable(gradientDrawable);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        xAxis.setTextColor(Color.parseColor("#7A7D8F"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setTextColor(Color.parseColor("#7A7D8F"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.invalidate();
    }
}
