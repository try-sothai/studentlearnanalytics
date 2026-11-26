package com.social7.studentlearnanalytics.admin.reports;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentReportActivity extends AppCompatActivity {

    private Spinner spStudentReportYear, spStudentReportSemester;
    private LineChart chartEnrollmentTrend;
    private PieChart chartStudentDistribution;
    private MaterialButton btnExportStudentReport, btnViewStudentReportDetails;
    private TextView tabOverview, tabEnrollment, tabPerformance, tabAttendance;
    private TextView tvStatTotalStudents, tvStatActiveStudents, tvStatNewEnrollments, tvStatGraduated;

    private final List<String> yearLabels = new ArrayList<>();
    private final List<Integer> yearIds = new ArrayList<>();
    private final List<String> semLabels = new ArrayList<>();
    private final List<Integer> semIds = new ArrayList<>();
    private Integer selectedAyId = null;
    private Integer selectedSemId = null;
    private int activeTab = 0; // 0=Overview, 1=Enrollment, 2=Performance, 3=Attendance
    private JsonObject cachedOverview = null;
    private JsonObject cachedAttendance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_admin_student_report);

        initViews();
        setupDefaultFilters();
        setupChartAppearance();
        setupListeners();
        loadLookups();
        loadStudentAnalytics();
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

        tabOverview = findViewById(R.id.tabStudentReportOverview);
        tabEnrollment = findViewById(R.id.tabStudentReportEnrollment);
        tabPerformance = findViewById(R.id.tabStudentReportPerformance);
        tabAttendance = findViewById(R.id.tabStudentReportAttendance);

        tvStatTotalStudents = findViewById(R.id.tvStatTotalStudents);
        tvStatActiveStudents = findViewById(R.id.tvStatActiveStudents);
        tvStatNewEnrollments = findViewById(R.id.tvStatNewEnrollments);
        tvStatGraduated = findViewById(R.id.tvStatGraduated);

        btnExportStudentReport = findViewById(R.id.btnExportStudentReport);
        btnViewStudentReportDetails = findViewById(R.id.btnViewStudentReportDetails);
    }

    private void setupDefaultFilters() {
        yearLabels.clear();
        yearIds.clear();
        yearLabels.add("All Academic Years");
        yearIds.add(null);

        semLabels.clear();
        semIds.clear();
        semLabels.add("All Semesters");
        semIds.add(null);

        setSpinnerAdapter(spStudentReportYear, yearLabels);
        setSpinnerAdapter(spStudentReportSemester, semLabels);
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupChartAppearance() {
        if (chartEnrollmentTrend != null) {
            XAxis xAxis = chartEnrollmentTrend.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            chartEnrollmentTrend.getAxisRight().setEnabled(false);
            chartEnrollmentTrend.getDescription().setEnabled(false);
            chartEnrollmentTrend.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartEnrollmentTrend.getContext(), chartEnrollmentTrend);
        }

        if (chartStudentDistribution != null) {
            chartStudentDistribution.setHoleRadius(50f);
            chartStudentDistribution.setTransparentCircleRadius(55f);
            chartStudentDistribution.setCenterText("--\nStudents");
            chartStudentDistribution.setCenterTextSize(14f);
            chartStudentDistribution.getDescription().setEnabled(false);
            chartStudentDistribution.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(chartStudentDistribution.getContext(), chartStudentDistribution);
        }
    }

    private void loadLookups() {
        ApiClient.getApiService(this).getReportsAnalyticsLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                JsonObject data = response.body().getAsJsonObject("data");

                yearLabels.clear();
                yearIds.clear();
                yearLabels.add("All Academic Years");
                yearIds.add(null);
                if (data.has("academic_years") && data.get("academic_years").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("academic_years")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        yearLabels.add(UiUtils.optString(obj, "name", ""));
                        yearIds.add(UiUtils.optInt(obj, "id", 0));
                    }
                }

                semLabels.clear();
                semIds.clear();
                semLabels.add("All Semesters");
                semIds.add(null);
                if (data.has("semesters") && data.get("semesters").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("semesters")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        semLabels.add(UiUtils.optString(obj, "name", ""));
                        semIds.add(UiUtils.optInt(obj, "id", 0));
                    }
                }

                setSpinnerAdapter(spStudentReportYear, yearLabels);
                setSpinnerAdapter(spStudentReportSemester, semLabels);
                attachSpinnerListeners();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void attachSpinnerListeners() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            private boolean initialized = false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!initialized) {
                    initialized = true;
                    return;
                }
                int yPos = spStudentReportYear != null ? spStudentReportYear.getSelectedItemPosition() : 0;
                int sPos = spStudentReportSemester != null ? spStudentReportSemester.getSelectedItemPosition() : 0;
                selectedAyId = (yPos >= 0 && yPos < yearIds.size()) ? yearIds.get(yPos) : null;
                selectedSemId = (sPos >= 0 && sPos < semIds.size()) ? semIds.get(sPos) : null;
                loadStudentAnalytics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        if (spStudentReportYear != null) spStudentReportYear.setOnItemSelectedListener(listener);
        if (spStudentReportSemester != null) spStudentReportSemester.setOnItemSelectedListener(listener);
    }

    private void loadStudentAnalytics() {
        ApiClient.getApiService(this).getReportsAnalyticsOverview(selectedAyId, selectedSemId, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            cachedOverview = response.body().getAsJsonObject("data");
                            bindStudentReport();
                        } else {
                            Toast.makeText(StudentReportActivity.this, "Failed to load student report data.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(StudentReportActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });

        ApiClient.getApiService(this).getReportsAnalyticsAttendance(selectedAyId, selectedSemId, null, null, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            cachedAttendance = response.body().getAsJsonObject("data");
                            bindStudentReport();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    }
                });
    }

    private void bindStudentReport() {
        int totalStudents = 0;
        if (cachedOverview != null && cachedOverview.has("metrics") && cachedOverview.get("metrics").isJsonObject()) {
            JsonObject m = cachedOverview.getAsJsonObject("metrics");
            totalStudents = UiUtils.optInt(m, "total_students", 0);
            int activeStudents = UiUtils.optInt(m, "active_students", 0);
            float attRate = UiUtils.optFloat(m, "attendance_rate", -1f);
            float avgGpa = UiUtils.optFloat(m, "average_gpa", -1f);

            if (tvStatTotalStudents != null) tvStatTotalStudents.setText(String.valueOf(totalStudents));
            if (tvStatActiveStudents != null) tvStatActiveStudents.setText(String.valueOf(activeStudents));
            if (tvStatNewEnrollments != null) {
                tvStatNewEnrollments.setText(attRate >= 0f ? String.format(Locale.US, "%.1f%%", attRate) : "--");
            }
            if (tvStatGraduated != null) {
                tvStatGraduated.setText(avgGpa >= 0f ? String.format(Locale.US, "%.2f", avgGpa) : "--");
            }
        }

        renderLineChart();
        renderPieChart(totalStudents);
    }

    private void renderLineChart() {
        if (chartEnrollmentTrend == null) return;

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String seriesLabel = getString(R.string.enrollment);
        int lineColor = Color.parseColor("#3155F5");

        if (activeTab == 3 && cachedAttendance != null && cachedAttendance.has("monthly_trend") && cachedAttendance.get("monthly_trend").isJsonArray()) {
            JsonArray arr = cachedAttendance.getAsJsonArray("monthly_trend");
            for (int i = 0; i < arr.size(); i++) {
                if (!arr.get(i).isJsonObject()) continue;
                JsonObject item = arr.get(i).getAsJsonObject();
                entries.add(new Entry(entries.size(), UiUtils.optFloat(item, "rate", 0f)));
                labels.add(UiUtils.optString(item, "label", ""));
            }
            seriesLabel = getString(R.string.attendance_7266c9);
            lineColor = Color.parseColor("#10B981");
        } else if (cachedOverview != null && cachedOverview.has("enrollment_trend") && cachedOverview.get("enrollment_trend").isJsonArray()) {
            JsonArray trend = cachedOverview.getAsJsonArray("enrollment_trend");
            for (int i = 0; i < trend.size(); i++) {
                if (!trend.get(i).isJsonObject()) continue;
                JsonObject item = trend.get(i).getAsJsonObject();
                entries.add(new Entry(entries.size(), UiUtils.optFloat(item, "count", 0f)));
                labels.add(UiUtils.optString(item, "label", ""));
            }
        }

        if (entries.isEmpty()) {
            chartEnrollmentTrend.clear();
            chartEnrollmentTrend.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, seriesLabel);
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(lineColor);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);

        chartEnrollmentTrend.setData(new LineData(dataSet));
        chartEnrollmentTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
        ChartFontHelper.applyKhmerFont(chartEnrollmentTrend.getContext(), chartEnrollmentTrend);
        chartEnrollmentTrend.invalidate();
    }

    private void renderPieChart(int totalStudents) {
        if (chartStudentDistribution == null) return;

        List<PieEntry> entries = new ArrayList<>();
        String centerText = totalStudents + "\nStudents";

        if (activeTab == 2 && cachedOverview != null && cachedOverview.has("gpa_distribution") && cachedOverview.get("gpa_distribution").isJsonArray()) {
            for (JsonElement el : cachedOverview.getAsJsonArray("gpa_distribution")) {
                if (!el.isJsonObject()) continue;
                JsonObject b = el.getAsJsonObject();
                float cnt = UiUtils.optFloat(b, "count", 0f);
                if (cnt > 0f) entries.add(new PieEntry(cnt, UiUtils.optString(b, "label", "")));
            }
            centerText = "GPA\nDistribution";
        } else if (activeTab == 3 && cachedAttendance != null && cachedAttendance.has("summary") && cachedAttendance.get("summary").isJsonObject()) {
            JsonObject s = cachedAttendance.getAsJsonObject("summary");
            float present = UiUtils.optFloat(s, "present_count", 0f);
            float late = UiUtils.optFloat(s, "late_count", 0f);
            float absent = UiUtils.optFloat(s, "absent_count", 0f);
            float excused = UiUtils.optFloat(s, "excused_count", 0f);
            if (present > 0f) entries.add(new PieEntry(present, "Present"));
            if (late > 0f) entries.add(new PieEntry(late, "Late"));
            if (absent > 0f) entries.add(new PieEntry(absent, "Absent"));
            if (excused > 0f) entries.add(new PieEntry(excused, "Excused"));
            float rate = UiUtils.optFloat(s, "overall_rate", -1f);
            centerText = (rate >= 0f ? String.format(Locale.US, "%.1f%%", rate) : "--") + "\nAttendance";
        } else if (cachedOverview != null && cachedOverview.has("department_distribution") && cachedOverview.get("department_distribution").isJsonArray()) {
            for (JsonElement el : cachedOverview.getAsJsonArray("department_distribution")) {
                if (!el.isJsonObject()) continue;
                JsonObject d = el.getAsJsonObject();
                float cnt = UiUtils.optFloat(d, "count", 0f);
                if (cnt > 0f) entries.add(new PieEntry(cnt, UiUtils.optString(d, "label", "")));
            }
        }

        if (entries.isEmpty()) {
            chartStudentDistribution.clear();
            chartStudentDistribution.setCenterText(centerText);
            chartStudentDistribution.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#3155F5"),
                Color.parseColor("#1260FF"),
                Color.parseColor("#6546F5"),
                Color.parseColor("#16A34A"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#EF4444")
        });
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(false);
        chartStudentDistribution.setData(pieData);
        chartStudentDistribution.setCenterText(centerText);
        ChartFontHelper.applyKhmerFont(chartStudentDistribution.getContext(), chartStudentDistribution);
        chartStudentDistribution.invalidate();
    }

    private void selectTab(int tabIndex) {
        activeTab = tabIndex;
        TextView[] tabs = new TextView[]{tabOverview, tabEnrollment, tabPerformance, tabAttendance};
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] == null) continue;
            if (i == tabIndex) {
                tabs[i].setBackgroundResource(R.drawable.bg_notif_tab_active);
                tabs[i].setTextColor(Color.WHITE);
            } else {
                tabs[i].setBackground(null);
                tabs[i].setTextColor(getColor(R.color.admin_text_secondary));
            }
        }
        bindStudentReport();
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabEnrollment != null) tabEnrollment.setOnClickListener(v -> selectTab(1));
        if (tabPerformance != null) tabPerformance.setOnClickListener(v -> selectTab(2));
        if (tabAttendance != null) tabAttendance.setOnClickListener(v -> selectTab(3));

        if (btnExportStudentReport != null) {
            btnExportStudentReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Students");
                startActivity(intent);
            });
        }

        if (btnViewStudentReportDetails != null) {
            btnViewStudentReportDetails.setOnClickListener(v -> showStudentDetailsDialog());
        }
    }

    private void showStudentDetailsDialog() {
        if (cachedOverview == null) {
            Toast.makeText(this, "No student report data loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (cachedOverview.has("year_level_breakdown") && cachedOverview.get("year_level_breakdown").isJsonArray()) {
            sb.append("Year Level Breakdown:\n");
            for (JsonElement el : cachedOverview.getAsJsonArray("year_level_breakdown")) {
                if (!el.isJsonObject()) continue;
                JsonObject y = el.getAsJsonObject();
                sb.append("• ").append(UiUtils.optString(y, "label", "")).append(": ")
                        .append(UiUtils.optInt(y, "count", 0)).append(" students\n");
            }
            sb.append("\n");
        }
        if (cachedOverview.has("student_enrollment_roster") && cachedOverview.get("student_enrollment_roster").isJsonArray()) {
            JsonArray roster = cachedOverview.getAsJsonArray("student_enrollment_roster");
            sb.append("Student Roster (").append(roster.size()).append("):\n");
            for (JsonElement el : roster) {
                if (!el.isJsonObject()) continue;
                JsonObject s = el.getAsJsonObject();
                sb.append("• ").append(UiUtils.optString(s, "student_code", ""))
                        .append(" — ").append(UiUtils.optString(s, "student_name", ""))
                        .append(" [").append(UiUtils.optInt(s, "enrollment_count", 0)).append(" courses]\n");
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Detailed Student Report")
                .setMessage(sb.length() > 0 ? sb.toString().trim() : getString(R.string.empty_state_no_data))
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
