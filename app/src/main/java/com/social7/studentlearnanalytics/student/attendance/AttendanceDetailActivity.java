package com.social7.studentlearnanalytics.student.attendance;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.student.StudentMainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Attendance Detail screen backed by GET /api/student/attendance.
 *
 * Attendance-counting policy (applies to summary cards, donut, legend and monthly chart):
 * - Each attendance_details row is counted once, under its raw status:
 *   present, late, excused, absent. No status is merged into another
 *   (late is NOT folded into present, excused is NOT folded into absent).
 * - Any other/unknown status is counted separately as "Other" and never hidden inside a category.
 * - All per-status percentages use the same denominator: summary.total (all rows).
 * - The single headline rate is the backend's summary.percentage = present / total * 100
 *   (on-time presence rate). It is shown as "X% Present"; it is not an "attended" rate,
 *   because late and excused are in the denominator but not the numerator.
 * - The monthly chart stacks the same four statuses per month (by attendance.attendance_date),
 *   so summing a status across months equals its summary count, except rows without a date.
 */
public class AttendanceDetailActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceDetail";

    // Shared SLAS colors (donut, legend and monthly bars use the same palette)
    private static final int COLOR_PRESENT = Color.parseColor("#10B981");
    private static final int COLOR_LATE = Color.parseColor("#F59E0B");
    private static final int COLOR_EXCUSED = Color.parseColor("#6366F1");
    private static final int COLOR_ABSENT = Color.parseColor("#EF4444");
    private static final int COLOR_OTHER = Color.parseColor("#94A3B8");
    private static final int COLOR_EMPTY = Color.parseColor("#E2E8F0");

    // Index order of the per-status count arrays
    private static final int PRESENT = 0, LATE = 1, EXCUSED = 2, ABSENT = 3, OTHER = 4;

    private ProgressBar loadingProgressBar;
    private TextView emptyStateText;
    private TextView tvTotalClasses, tvPresentCount, tvAbsentCount;
    private TextView tvPresentLegend, tvLateLegend, tvExcusedLegend, tvAbsentLegend, tvOtherLegend;
    private View rowOtherLegend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        tvTotalClasses = findViewById(R.id.tvTotalClasses);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvPresentLegend = findViewById(R.id.tvPresentLegend);
        tvLateLegend = findViewById(R.id.tvLateLegend);
        tvExcusedLegend = findViewById(R.id.tvExcusedLegend);
        tvAbsentLegend = findViewById(R.id.tvAbsentLegend);
        tvOtherLegend = findViewById(R.id.tvOtherLegend);
        rowOtherLegend = findViewById(R.id.rowOtherLegend);
        TextView tvSemesterLabel = findViewById(R.id.tvSemesterLabel);
        // The endpoint returns all of the student's attendance (no semester filter).
        if (tvSemesterLabel != null) tvSemesterLabel.setText("All Semesters");

        // Clear the static placeholder values until real data arrives
        bindSummary(0, new int[5]);

        // Setup Charts with empty data initially
        setupDonutChart(new int[5], "Loading...");
        setupMonthlyBarChart(new ArrayList<>(), new ArrayList<>());

        // Fetch Real API Data
        fetchAttendanceData();

        // Bottom Navigation Listeners
        findViewById(R.id.nav_home).setOnClickListener(v -> openTab(R.id.nav_home));
        findViewById(R.id.nav_analytics).setOnClickListener(v -> openTab(R.id.nav_analytics));
        findViewById(R.id.nav_prediction).setOnClickListener(v -> openTab(R.id.nav_prediction));
        findViewById(R.id.nav_notifications).setOnClickListener(v -> openTab(R.id.nav_notifications));
        findViewById(R.id.nav_profile).setOnClickListener(v -> openTab(R.id.nav_profile));
    }

    private void fetchAttendanceData() {
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);

        ApiClient.getApiService(this).getStudentAttendance(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        JsonObject data = body.has("data") && body.get("data").isJsonObject()
                                ? body.getAsJsonObject("data") : new JsonObject();
                        JsonObject summary = data.has("summary") && data.get("summary").isJsonObject()
                                ? data.getAsJsonObject("summary") : new JsonObject();
                        JsonArray details = data.has("details") && data.get("details").isJsonArray()
                                ? data.getAsJsonArray("details") : new JsonArray();

                        int[] counts = new int[5];
                        if (details.size() > 0) {
                            for (JsonElement element : details) {
                                if (!element.isJsonObject()) continue;
                                JsonObject detail = element.getAsJsonObject();
                                String status = detail.has("status") && !detail.get("status").isJsonNull()
                                        ? detail.get("status").getAsString() : null;
                                counts[statusIndex(status)]++;
                            }
                        } else {
                            counts[PRESENT] = getInt(summary, "present");
                            counts[LATE] = getInt(summary, "late");
                            counts[EXCUSED] = getInt(summary, "excused");
                            counts[ABSENT] = getInt(summary, "absent");
                        }
                        int total = getInt(summary, "total");
                        if (total <= 0) total = counts[PRESENT] + counts[LATE] + counts[EXCUSED] + counts[ABSENT] + counts[OTHER];

                        bindSummary(total, counts);

                        if (total <= 0) {
                            setupDonutChart(new int[5], "0%");
                            setupMonthlyBarChart(new ArrayList<>(), new ArrayList<>());
                            showEmptyState("No attendance records found");
                            return;
                        }

                        float rate = summary.has("percentage") && !summary.get("percentage").isJsonNull()
                                ? summary.get("percentage").getAsFloat()
                                : counts[PRESENT] * 100f / total;
                        setupDonutChart(counts, formatPercent(rate) + "\nPresent");

                        processMonthlyData(details);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing attendance data", e);
                        Toast.makeText(AttendanceDetailActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        setupDonutChart(new int[5], "--");
                        showEmptyState("Error loading attendance");
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(AttendanceDetailActivity.this, "Session expired, please login again", Toast.LENGTH_LONG).show();
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Session expired. Please log in again.");
                } else if (response.code() == 404) {
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Student profile not found.");
                } else {
                    Log.w(TAG, "Non-success response: HTTP " + response.code());
                    Toast.makeText(AttendanceDetailActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Failed to load attendance (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "API failure", t);
                Toast.makeText(AttendanceDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                setupDonutChart(new int[5], "--");
                showEmptyState("Network error. Please check connection.");
            }
        });
    }

    private static int getInt(JsonObject obj, String key) {
        try {
            return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                    ? (int) Math.round(obj.get(key).getAsDouble()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatPercent(float value) {
        if (value == Math.round(value)) return Math.round(value) + "%";
        return String.format(Locale.US, "%.1f%%", value);
    }

    private static String countWithPercent(int count, int total) {
        float pct = total > 0 ? count * 100f / total : 0f;
        return count + " (" + formatPercent(Math.round(pct * 10f) / 10f) + ")";
    }

    private void bindSummary(int total, int[] counts) {
        if (tvTotalClasses != null) tvTotalClasses.setText(String.valueOf(total));
        if (tvPresentCount != null) tvPresentCount.setText(String.valueOf(counts[PRESENT]));
        if (tvAbsentCount != null) tvAbsentCount.setText(String.valueOf(counts[ABSENT]));
        if (tvPresentLegend != null) tvPresentLegend.setText(countWithPercent(counts[PRESENT], total));
        if (tvLateLegend != null) tvLateLegend.setText(countWithPercent(counts[LATE], total));
        if (tvExcusedLegend != null) tvExcusedLegend.setText(countWithPercent(counts[EXCUSED], total));
        if (tvAbsentLegend != null) tvAbsentLegend.setText(countWithPercent(counts[ABSENT], total));
        if (tvOtherLegend != null) tvOtherLegend.setText(countWithPercent(counts[OTHER], total));
        if (rowOtherLegend != null) rowOtherLegend.setVisibility(counts[OTHER] > 0 ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(String message) {
        if (emptyStateText != null) {
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
        }
    }

    private static int statusIndex(String status) {
        if (status == null) return OTHER;
        switch (status.trim().toLowerCase(Locale.US)) {
            case "present": return PRESENT;
            case "late": return LATE;
            case "excused": return EXCUSED;
            case "absent": return ABSENT;
            default: return OTHER;
        }
    }

    private void processMonthlyData(JsonArray details) {
        Map<String, float[]> monthlyStats = new TreeMap<>();
        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdfOut = new SimpleDateFormat("MMM", Locale.US);
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
        int undated = 0;

        for (JsonElement element : details) {
            if (!element.isJsonObject()) continue;
            JsonObject detail = element.getAsJsonObject();
            String status = detail.has("status") && !detail.get("status").isJsonNull()
                    ? detail.get("status").getAsString() : null;

            String dateStr = null;
            if (detail.has("attendance") && detail.get("attendance").isJsonObject()) {
                JsonObject attendance = detail.getAsJsonObject("attendance");
                if (attendance.has("attendance_date") && !attendance.get("attendance_date").isJsonNull()) {
                    dateStr = attendance.get("attendance_date").getAsString();
                }
            }
            if (dateStr == null) {
                undated++;
                continue;
            }
            try {
                Date date = sdfIn.parse(dateStr);
                if (date == null) {
                    undated++;
                    continue;
                }
                String sortKey = keyFormat.format(date);
                float[] stats = monthlyStats.get(sortKey);
                if (stats == null) stats = new float[5];
                stats[statusIndex(status)] += 1f;
                monthlyStats.put(sortKey, stats);
            } catch (Exception e) {
                undated++;
                Log.e(TAG, "Date parse error", e);
            }
        }
        if (undated > 0) {
            Log.w(TAG, undated + " attendance record(s) without a usable date excluded from monthly chart");
        }

        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, float[]> entry : monthlyStats.entrySet()) {
            try {
                Date d = keyFormat.parse(entry.getKey());
                labels.add(d != null ? sdfOut.format(d) : entry.getKey());
            } catch (Exception e) {
                labels.add(entry.getKey());
            }
            barEntries.add(new BarEntry(i, entry.getValue()));
            i++;
        }

        setupMonthlyBarChart(barEntries, labels);
    }

    private void setupDonutChart(int[] counts, String centerText) {
        PieChart donutChart = findViewById(R.id.donutChart);
        if (donutChart == null) return;

        String[] names = {"Present", "Late", "Excused", "Absent", "Other"};
        int[] palette = {COLOR_PRESENT, COLOR_LATE, COLOR_EXCUSED, COLOR_ABSENT, COLOR_OTHER};

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int k = 0; k < counts.length; k++) {
            if (counts[k] > 0) {
                entries.add(new PieEntry(counts[k], names[k]));
                colors.add(palette[k]);
            }
        }
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Data"));
            colors.add(COLOR_EMPTY);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        donutChart.setData(data);
        donutChart.setDrawHoleEnabled(true);
        donutChart.setHoleRadius(72f);
        donutChart.setTransparentCircleRadius(76f);
        donutChart.setHoleColor(Color.TRANSPARENT);
        donutChart.setDrawEntryLabels(false);
        donutChart.setCenterText(centerText);
        donutChart.setCenterTextSize(20f);
        donutChart.setCenterTextColor(Color.parseColor("#1E1B4B"));
        donutChart.getDescription().setEnabled(false);
        donutChart.getLegend().setEnabled(false);
        donutChart.animateY(800);
        donutChart.invalidate();
    }

    private void setupMonthlyBarChart(List<BarEntry> barEntries, List<String> labels) {
        BarChart barChart = findViewById(R.id.barChartMonthly);
        if (barChart == null) return;

        if (barEntries.isEmpty()) {
            barChart.clear();
            return;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(COLOR_PRESENT, COLOR_LATE, COLOR_EXCUSED, COLOR_ABSENT, COLOR_OTHER);
        barDataSet.setStackLabels(new String[]{"Present", "Late", "Excused", "Absent", "Other"});
        barDataSet.setDrawValues(false);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.45f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setTextColor(Color.parseColor("#64748B"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);

        float maxVal = 10f;
        for (BarEntry entry : barEntries) {
            float sum = 0f;
            float[] vals = entry.getYVals();
            if (vals != null) for (float v : vals) sum += v;
            if (sum > maxVal) maxVal = sum;
        }
        leftAxis.setAxisMaximum(maxVal + 2f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void openTab(int tabId) {
        Intent intent = new Intent(this, StudentMainActivity.class);
        intent.putExtra("select_tab", tabId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
