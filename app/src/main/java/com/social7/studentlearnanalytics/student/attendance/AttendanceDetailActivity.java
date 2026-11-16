package com.social7.studentlearnanalytics.student.attendance;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

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

public class AttendanceDetailActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceDetail";

    private static final int COLOR_PRESENT = Color.parseColor("#10B981");
    private static final int COLOR_LATE = Color.parseColor("#F59E0B");
    private static final int COLOR_EXCUSED = Color.parseColor("#6366F1");
    private static final int COLOR_ABSENT = Color.parseColor("#EF4444");
    private static final int COLOR_OTHER = Color.parseColor("#94A3B8");
    private static final int COLOR_EMPTY = Color.parseColor("#E2E8F0");

    private static final int PRESENT = 0, LATE = 1, EXCUSED = 2, ABSENT = 3, OTHER = 4;

    private ProgressBar loadingProgressBar;
    private TextView emptyStateText;
    private TextView tvTotalClasses, tvPresentCount, tvAbsentCount;
    private TextView tvPresentLegend, tvLateLegend, tvExcusedLegend, tvAbsentLegend, tvOtherLegend;
    private TextView tvSemesterLabel, tvBottomNotifBadge;
    private View rowOtherLegend, cardSemesterSelector;
    private LinearLayout layoutAttendanceRecordsContainer;

    private final List<SemesterItem> semesters = new ArrayList<>();
    private Long selectedSemesterId = null;

    private static class SemesterItem {
        int id;
        String name;
        SemesterItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

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
        tvSemesterLabel = findViewById(R.id.tvSemesterLabel);
        tvBottomNotifBadge = findViewById(R.id.tvBottomNotifBadge);
        cardSemesterSelector = findViewById(R.id.cardSemesterSelector);
        layoutAttendanceRecordsContainer = findViewById(R.id.layoutAttendanceRecordsContainer);

        if (tvSemesterLabel != null) tvSemesterLabel.setText(R.string.all_semesters);
        if (cardSemesterSelector != null) {
            cardSemesterSelector.setOnClickListener(this::showSemesterMenu);
        }

        bindSummary(0, new int[5]);
        setupDonutChart(new int[5], "Loading...");
        setupMonthlyBarChart(new ArrayList<>(), new ArrayList<>());

        loadSemestersAndBadge();
        fetchAttendanceData();

        View navHome = findViewById(R.id.nav_home);
        View navCourses = findViewById(R.id.nav_courses);
        View navAnalytics = findViewById(R.id.nav_analytics);
        View navNotifications = findViewById(R.id.nav_notifications);
        View navProfile = findViewById(R.id.nav_profile);

        if (navHome != null) navHome.setOnClickListener(v -> openTab(R.id.nav_home));
        if (navCourses != null) navCourses.setOnClickListener(v -> openTab(R.id.nav_courses));
        if (navAnalytics != null) navAnalytics.setOnClickListener(v -> openTab(R.id.nav_analytics));
        if (navNotifications != null) navNotifications.setOnClickListener(v -> openTab(R.id.nav_notifications));
        if (navProfile != null) navProfile.setOnClickListener(v -> openTab(R.id.nav_profile));
    }

    private void loadSemestersAndBadge() {
        ApiClient.getApiService(this).getStudentDashboard(null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("semesters") && body.get("semesters").isJsonArray()) {
                        semesters.clear();
                        JsonArray arr = body.getAsJsonArray("semesters");
                        for (JsonElement el : arr) {
                            if (el.isJsonObject()) {
                                JsonObject so = el.getAsJsonObject();
                                int id = so.has("id") ? so.get("id").getAsInt() : 0;
                                String name = so.has("name") && !so.get("name").isJsonNull()
                                        ? so.get("name").getAsString()
                                        : (so.has("semester_name") && !so.get("semester_name").isJsonNull()
                                        ? so.get("semester_name").getAsString() : "Semester " + id);
                                if (id > 0) semesters.add(new SemesterItem(id, name));
                            }
                        }
                    }
                    if (tvBottomNotifBadge != null && body.has("unread_notifications_count") && !body.get("unread_notifications_count").isJsonNull()) {
                        int unread = body.get("unread_notifications_count").getAsInt();
                        if (unread > 0) {
                            tvBottomNotifBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
                            tvBottomNotifBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvBottomNotifBadge.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private void showSemesterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 0, 0, "All Semesters");
        for (int i = 0; i < semesters.size(); i++) {
            SemesterItem s = semesters.get(i);
            popup.getMenu().add(0, s.id, i + 1, s.name);
        }
        popup.setOnMenuItemClickListener(item -> {
            if (tvSemesterLabel != null) {
                tvSemesterLabel.setText(item.getTitle());
            }
            selectedSemesterId = item.getItemId() == 0 ? null : (long) item.getItemId();
            fetchAttendanceData();
            return true;
        });
        popup.show();
    }

    private void fetchAttendanceData() {
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);

        ApiClient.getApiService(this).getStudentAttendance(selectedSemesterId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject body = response.body();
                        JsonObject data = body.has("data") && body.get("data").isJsonObject()
                                ? body.getAsJsonObject("data") : body;
                        JsonObject summary = data.has("summary") && data.get("summary").isJsonObject()
                                ? data.getAsJsonObject("summary") : data;
                        JsonArray details = data.has("details") && data.get("details").isJsonArray()
                                ? data.getAsJsonArray("details") : new JsonArray();
                        JsonArray courseBreakdown = data.has("course_breakdown") && data.get("course_breakdown").isJsonArray()
                                ? data.getAsJsonArray("course_breakdown")
                                : (body.has("course_breakdown") && body.get("course_breakdown").isJsonArray()
                                ? body.getAsJsonArray("course_breakdown") : new JsonArray());

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
                        populateRecordsSection(courseBreakdown, details);

                        if (total <= 0) {
                            setupDonutChart(new int[5], "0%");
                            setupMonthlyBarChart(new ArrayList<>(), new ArrayList<>());
                            showEmptyState("No attendance records found");
                            return;
                        }

                        float rate = summary.has("percentage") && !summary.get("percentage").isJsonNull()
                                ? summary.get("percentage").getAsFloat()
                                : (summary.has("attendance_rate") && !summary.get("attendance_rate").isJsonNull()
                                ? summary.get("attendance_rate").getAsFloat()
                                : counts[PRESENT] * 100f / total);
                        setupDonutChart(counts, formatPercent(rate) + "\nPresent");

                        processMonthlyData(details);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing attendance data", e);
                        Toast.makeText(AttendanceDetailActivity.this, getString(R.string.error_parsing_data), Toast.LENGTH_SHORT).show();
                        setupDonutChart(new int[5], "--");
                        showEmptyState("Error loading attendance");
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(AttendanceDetailActivity.this, getString(R.string.session_expired_login_again), Toast.LENGTH_LONG).show();
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Session expired. Please log in again.");
                } else if (response.code() == 404) {
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Student profile not found.");
                } else {
                    Log.w(TAG, "Non-success response: HTTP " + response.code());
                    Toast.makeText(AttendanceDetailActivity.this, getString(R.string.failed_load_attendance), Toast.LENGTH_SHORT).show();
                    setupDonutChart(new int[5], "--");
                    showEmptyState("Failed to load attendance (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "API failure", t);
                Toast.makeText(AttendanceDetailActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
                setupDonutChart(new int[5], "--");
                showEmptyState("Network error. Please check connection.");
            }
        });
    }

    private void populateRecordsSection(JsonArray courseBreakdown, JsonArray details) {
        if (layoutAttendanceRecordsContainer == null) return;
        layoutAttendanceRecordsContainer.removeAllViews();

        if (courseBreakdown != null && courseBreakdown.size() > 0) {
            TextView tvSubHeader = new TextView(this);
            tvSubHeader.setText(R.string.by_course);
            tvSubHeader.setTextColor(Color.parseColor("#64748B"));
            tvSubHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvSubHeader.setTypeface(null, Typeface.BOLD);
            tvSubHeader.setPadding(0, 0, 0, dp(8));
            layoutAttendanceRecordsContainer.addView(tvSubHeader);

            for (JsonElement el : courseBreakdown) {
                if (!el.isJsonObject()) continue;
                JsonObject co = el.getAsJsonObject();
                String code = co.has("course_code") && !co.get("course_code").isJsonNull()
                        ? co.get("course_code").getAsString() : "";
                String name = co.has("course_name") && !co.get("course_name").isJsonNull()
                        ? co.get("course_name").getAsString() : "Course";
                int total = getInt(co, "total");
                int present = getInt(co, "present");
                float pct = co.has("percentage") && !co.get("percentage").isJsonNull()
                        ? co.get("percentage").getAsFloat() : 0f;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(8), 0, dp(8));

                LinearLayout left = new LinearLayout(this);
                left.setOrientation(LinearLayout.VERTICAL);
                left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvTitle = new TextView(this);
                tvTitle.setText(!code.isEmpty() ? (code + " – " + name) : name);
                tvTitle.setTextColor(Color.parseColor("#1E1B4B"));
                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                tvTitle.setTypeface(null, Typeface.BOLD);
                left.addView(tvTitle);

                TextView tvSub = new TextView(this);
                tvSub.setText(present + " / " + total + " classes present");
                tvSub.setTextColor(Color.parseColor("#64748B"));
                tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                left.addView(tvSub);

                TextView tvBadge = new TextView(this);
                tvBadge.setText(formatPercent(pct));
                tvBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                tvBadge.setTypeface(null, Typeface.BOLD);
                tvBadge.setTextColor(pct >= 80f ? COLOR_PRESENT : (pct >= 65f ? COLOR_LATE : COLOR_ABSENT));

                row.addView(left);
                row.addView(tvBadge);
                layoutAttendanceRecordsContainer.addView(row);
            }
        }

        if (details != null && details.size() > 0) {
            TextView tvHistoryHeader = new TextView(this);
            tvHistoryHeader.setText(R.string.recent_sessions);
            tvHistoryHeader.setTextColor(Color.parseColor("#64748B"));
            tvHistoryHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvHistoryHeader.setTypeface(null, Typeface.BOLD);
            tvHistoryHeader.setPadding(0, dp(14), 0, dp(8));
            layoutAttendanceRecordsContainer.addView(tvHistoryHeader);

            int maxRows = Math.min(details.size(), 15);
            for (int i = 0; i < maxRows; i++) {
                JsonElement el = details.get(i);
                if (!el.isJsonObject()) continue;
                JsonObject det = el.getAsJsonObject();
                String status = det.has("status") && !det.get("status").isJsonNull()
                        ? det.get("status").getAsString() : "present";

                String dateStr = "Session";
                String courseName = "Class Session";
                String topic = "";
                if (det.has("attendance") && det.get("attendance").isJsonObject()) {
                    JsonObject att = det.getAsJsonObject("attendance");
                    if (att.has("attendance_date") && !att.get("attendance_date").isJsonNull()) {
                        dateStr = att.get("attendance_date").getAsString();
                        if (dateStr.length() >= 10) dateStr = dateStr.substring(0, 10);
                    }
                    if (att.has("topic") && !att.get("topic").isJsonNull()) {
                        topic = att.get("topic").getAsString();
                    }
                    if (att.has("course") && att.get("course").isJsonObject()) {
                        JsonObject cObj = att.getAsJsonObject("course");
                        if (cObj.has("course_name") && !cObj.get("course_name").isJsonNull()) {
                            courseName = cObj.get("course_name").getAsString();
                        } else if (cObj.has("name") && !cObj.get("name").isJsonNull()) {
                            courseName = cObj.get("name").getAsString();
                        }
                    }
                }

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(6), 0, dp(6));

                LinearLayout left = new LinearLayout(this);
                left.setOrientation(LinearLayout.VERTICAL);
                left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvCourse = new TextView(this);
                tvCourse.setText(!topic.isEmpty() ? (courseName + " • " + topic) : courseName);
                tvCourse.setTextColor(Color.parseColor("#1E1B4B"));
                tvCourse.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                tvCourse.setTypeface(null, Typeface.BOLD);
                left.addView(tvCourse);

                TextView tvDate = new TextView(this);
                tvDate.setText(dateStr);
                tvDate.setTextColor(Color.parseColor("#64748B"));
                tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                left.addView(tvDate);

                TextView tvStatus = new TextView(this);
                String capStatus = status.substring(0, 1).toUpperCase(Locale.US) + status.substring(1).toLowerCase(Locale.US);
                tvStatus.setText(capStatus);
                tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tvStatus.setTypeface(null, Typeface.BOLD);
                int idx = statusIndex(status);
                int color = idx == PRESENT ? COLOR_PRESENT : (idx == LATE ? COLOR_LATE : (idx == EXCUSED ? COLOR_EXCUSED : COLOR_ABSENT));
                tvStatus.setTextColor(color);

                row.addView(left);
                row.addView(tvStatus);
                layoutAttendanceRecordsContainer.addView(row);
            }
        }

        if (layoutAttendanceRecordsContainer.getChildCount() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(getString(R.string.no_session_history_yet));
            tvEmpty.setTextColor(Color.parseColor("#64748B"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            layoutAttendanceRecordsContainer.addView(tvEmpty);
        }
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
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
            if (dateStr == null) continue;
            try {
                if (dateStr.length() >= 10) dateStr = dateStr.substring(0, 10);
                Date date = sdfIn.parse(dateStr);
                if (date == null) continue;
                String sortKey = keyFormat.format(date);
                float[] stats = monthlyStats.get(sortKey);
                if (stats == null) stats = new float[5];
                stats[statusIndex(status)] += 1f;
                monthlyStats.put(sortKey, stats);
            } catch (Exception e) {
                Log.e(TAG, "Date parse error", e);
            }
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
        ChartFontHelper.applyKhmerFont(donutChart.getContext(), donutChart);
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
        ChartFontHelper.applyKhmerFont(barChart.getContext(), barChart);
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
