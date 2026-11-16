package com.social7.studentlearnanalytics.admin;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class SystemStatisticsFragment extends Fragment {

    public SystemStatisticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_system_statistics,
                container,
                false
        );

        BarChart barChart = view.findViewById(R.id.barChartPerformance);
        PieChart pieChart = view.findViewById(R.id.pieChartRisk);

        setupPerformanceBarChart(barChart);
        setupRiskPieChart(pieChart);

        return view;
    }

    private void setupPerformanceBarChart(BarChart chart) {
        if (chart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 28f)); // Grade A
        entries.add(new BarEntry(1, 32f)); // Grade B
        entries.add(new BarEntry(2, 22f)); // Grade C
        entries.add(new BarEntry(3, 12f)); // Grade D
        entries.add(new BarEntry(4, 6f));  // Grade F

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.grade_distribution));
        dataSet.setColor(Color.parseColor("#4C35E9"));
        dataSet.setValueTextColor(Color.parseColor("#1D1F2A"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(40f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Grade A", "Grade B", "Grade C", "Grade D", "Grade F"}));
        xAxis.setGranularity(1f);

        chart.setFitBars(true);
        chart.animateY(1000);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void setupRiskPieChart(PieChart chart) {
        if (chart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(65f, "Low Risk"));
        entries.add(new PieEntry(25f, "Medium Risk"));
        entries.add(new PieEntry(10f, "High Risk"));

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.risk_levels));

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#10B981")); // Low Risk - Green
        colors.add(Color.parseColor("#F59E0B")); // Medium Risk - Orange
        colors.add(Color.parseColor("#EF4444")); // High Risk - Red
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));
        pieData.setValueTextSize(13f);
        pieData.setValueTextColor(Color.WHITE);

        chart.setData(pieData);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);

        // Donut chart styling
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.setCenterText(getString(R.string.risk_levels));
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(Color.parseColor("#1D1F2A"));

        // Legend configuration
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#1D1F2A"));

        chart.animateXY(1000, 1000);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
