package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemStatisticsFragment extends Fragment {

    private TextView tvTotalStudents;
    private TextView tvTotalTeachers;
    private TextView tvTotalCourses;
    private TextView tvTotalPredictions;
    private BarChart barChart;
    private PieChart pieChart;

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

        View btnMenu = view.findViewById(R.id.btnSystemStatsMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        tvTotalStudents = view.findViewById(R.id.tvStatTotalStudents);
        tvTotalTeachers = view.findViewById(R.id.tvStatTotalTeachers);
        tvTotalCourses = view.findViewById(R.id.tvStatTotalCourses);
        tvTotalPredictions = view.findViewById(R.id.tvStatTotalPredictions);
        barChart = view.findViewById(R.id.barChartPerformance);
        pieChart = view.findViewById(R.id.pieChartRisk);

        if (barChart != null) {
            barChart.setNoDataText("Loading performance distribution...");
        }
        if (pieChart != null) {
            pieChart.setNoDataText("Loading risk distribution...");
        }

        fetchSystemStatistics();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getContext() != null) {
            fetchSystemStatistics();
        }
    }

    private void fetchSystemStatistics() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getAdminStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    bindSummaryCards(data);
                    bindPerformanceChart(data);
                    bindRiskChart(data);
                } else {
                    showEmptyOrErrorState("Unable to load system statistics (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                showEmptyOrErrorState("Network error loading system statistics");
            }
        });
    }

    private void bindSummaryCards(JsonObject data) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
        if (tvTotalStudents != null) {
            tvTotalStudents.setText(nf.format(getInt(data, "total_students")));
        }
        if (tvTotalTeachers != null) {
            tvTotalTeachers.setText(nf.format(getInt(data, "total_teachers")));
        }
        if (tvTotalCourses != null) {
            tvTotalCourses.setText(nf.format(getInt(data, "total_courses")));
        }
        if (tvTotalPredictions != null) {
            tvTotalPredictions.setText(nf.format(getInt(data, "total_predictions")));
        }
    }

    private void bindPerformanceChart(JsonObject data) {
        if (barChart == null) return;

        int totalGraded = getInt(data, "total_graded_records");
        JsonObject dist = data.has("grade_distribution") && data.get("grade_distribution").isJsonObject()
                ? data.getAsJsonObject("grade_distribution")
                : null;

        if (dist == null || totalGraded == 0) {
            barChart.clear();
            barChart.setNoDataText("No graded records available");
            barChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, getFloat(dist, "A")));
        entries.add(new BarEntry(1, getFloat(dist, "B")));
        entries.add(new BarEntry(2, getFloat(dist, "C")));
        entries.add(new BarEntry(3, getFloat(dist, "D")));
        entries.add(new BarEntry(4, getFloat(dist, "F")));

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.grade_distribution));
        dataSet.setColor(Color.parseColor("#4C35E9"));
        dataSet.setValueTextColor(Color.parseColor("#1D1F2A"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Grade A", "Grade B", "Grade C", "Grade D", "Grade F"}));
        xAxis.setGranularity(1f);

        barChart.setFitBars(true);
        barChart.animateY(600);
        ChartFontHelper.applyKhmerFont(barChart.getContext(), barChart);
        barChart.invalidate();
    }

    private void bindRiskChart(JsonObject data) {
        if (pieChart == null) return;

        JsonObject risk = data.has("risk_distribution") && data.get("risk_distribution").isJsonObject()
                ? data.getAsJsonObject("risk_distribution")
                : null;

        if (risk == null) {
            pieChart.clear();
            pieChart.setNoDataText("No risk assessment data available");
            pieChart.invalidate();
            return;
        }

        int low = getInt(risk, "low");
        int medium = getInt(risk, "medium");
        int high = getInt(risk, "high");
        int unassessed = getInt(risk, "unassessed");
        int total = low + medium + high + unassessed;

        if (total == 0) {
            pieChart.clear();
            pieChart.setNoDataText("No risk assessment data available");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (low > 0) {
            entries.add(new PieEntry(low, "Low Risk"));
            colors.add(Color.parseColor("#10B981"));
        }
        if (medium > 0) {
            entries.add(new PieEntry(medium, "Medium Risk"));
            colors.add(Color.parseColor("#F59E0B"));
        }
        if (high > 0) {
            entries.add(new PieEntry(high, "High Risk"));
            colors.add(Color.parseColor("#EF4444"));
        }
        if (unassessed > 0) {
            entries.add(new PieEntry(unassessed, "Unassessed"));
            colors.add(Color.parseColor("#9CA3AF"));
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.risk_levels));
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setCenterText(getString(R.string.risk_levels));
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#1D1F2A"));

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#1D1F2A"));

        pieChart.animateXY(600, 600);
        ChartFontHelper.applyKhmerFont(pieChart.getContext(), pieChart);
        pieChart.invalidate();
    }

    private void showEmptyOrErrorState(String message) {
        if (barChart != null) {
            barChart.clear();
            barChart.setNoDataText(message);
            barChart.invalidate();
        }
        if (pieChart != null) {
            pieChart.clear();
            pieChart.setNoDataText(message);
            pieChart.invalidate();
        }
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private float getFloat(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0f;
        try {
            return obj.get(key).getAsFloat();
        } catch (Exception e) {
            return 0f;
        }
    }
}
