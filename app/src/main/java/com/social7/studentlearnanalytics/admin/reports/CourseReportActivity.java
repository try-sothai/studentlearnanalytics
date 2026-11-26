package com.social7.studentlearnanalytics.admin.reports;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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

public class CourseReportActivity extends AppCompatActivity {

    private Spinner spCourseReportYear, spCourseReportSemester;
    private BarChart chartCourseTrendBar;
    private MaterialButton btnExportCourseReport, btnViewCourseReportDetails;
    private TextView tabOverview, tabEnrollment, tabCompletion, tabPerformance;
    private TextView tvStatTotalCourses, tvStatActiveCourses, tvStatTotalEnrollments, tvStatAvgClassSize;
    private TextView tvTopCoursesTitle, tvTopCoursesEmpty;
    private LinearLayout containerTopCourses;

    private final List<String> yearLabels = new ArrayList<>();
    private final List<Integer> yearIds = new ArrayList<>();
    private final List<String> semLabels = new ArrayList<>();
    private final List<Integer> semIds = new ArrayList<>();
    private Integer selectedAyId = null;
    private Integer selectedSemId = null;
    private int activeTab = 0; // 0=Overview, 1=Enrollment, 2=Completion, 3=Performance
    private JsonObject cachedData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_course_report);

        initViews();
        setupDefaultFilters();
        setupChartAppearance();
        setupListeners();
        loadLookups();
        loadCourseAnalytics();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackCourseReport);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        spCourseReportYear = findViewById(R.id.spCourseReportYear);
        spCourseReportSemester = findViewById(R.id.spCourseReportSemester);
        chartCourseTrendBar = findViewById(R.id.chartCourseTrendBar);

        tabOverview = findViewById(R.id.tabCourseReportOverview);
        tabEnrollment = findViewById(R.id.tabCourseReportEnrollment);
        tabCompletion = findViewById(R.id.tabCourseReportCompletion);
        tabPerformance = findViewById(R.id.tabCourseReportPerformance);

        tvStatTotalCourses = findViewById(R.id.tvStatTotalCourses);
        tvStatActiveCourses = findViewById(R.id.tvStatActiveCourses);
        tvStatTotalEnrollments = findViewById(R.id.tvStatTotalEnrollments);
        tvStatAvgClassSize = findViewById(R.id.tvStatAvgClassSize);

        tvTopCoursesTitle = findViewById(R.id.tvTopCoursesTitle);
        tvTopCoursesEmpty = findViewById(R.id.tvTopCoursesEmpty);
        containerTopCourses = findViewById(R.id.containerTopCourses);

        btnExportCourseReport = findViewById(R.id.btnExportCourseReport);
        btnViewCourseReportDetails = findViewById(R.id.btnViewCourseReportDetails);
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

        setSpinnerAdapter(spCourseReportYear, yearLabels);
        setSpinnerAdapter(spCourseReportSemester, semLabels);
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupChartAppearance() {
        if (chartCourseTrendBar == null) return;
        XAxis xAxis = chartCourseTrendBar.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        chartCourseTrendBar.getAxisRight().setEnabled(false);
        chartCourseTrendBar.getDescription().setEnabled(false);
        chartCourseTrendBar.setNoDataText(getString(R.string.empty_state_no_data));
        ChartFontHelper.applyKhmerFont(chartCourseTrendBar.getContext(), chartCourseTrendBar);
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

                setSpinnerAdapter(spCourseReportYear, yearLabels);
                setSpinnerAdapter(spCourseReportSemester, semLabels);
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
                int yPos = spCourseReportYear != null ? spCourseReportYear.getSelectedItemPosition() : 0;
                int sPos = spCourseReportSemester != null ? spCourseReportSemester.getSelectedItemPosition() : 0;
                selectedAyId = (yPos >= 0 && yPos < yearIds.size()) ? yearIds.get(yPos) : null;
                selectedSemId = (sPos >= 0 && sPos < semIds.size()) ? semIds.get(sPos) : null;
                loadCourseAnalytics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        if (spCourseReportYear != null) spCourseReportYear.setOnItemSelectedListener(listener);
        if (spCourseReportSemester != null) spCourseReportSemester.setOnItemSelectedListener(listener);
    }

    private void loadCourseAnalytics() {
        ApiClient.getApiService(this).getReportsAnalyticsCourses(selectedAyId, selectedSemId, null, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            cachedData = response.body().getAsJsonObject("data");
                            bindCourseAnalytics(cachedData);
                        } else {
                            Toast.makeText(CourseReportActivity.this, "Failed to load course report analytics.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(CourseReportActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindCourseAnalytics(JsonObject data) {
        if (data == null) return;

        if (data.has("enrollment_summary") && data.get("enrollment_summary").isJsonObject()) {
            JsonObject es = data.getAsJsonObject("enrollment_summary");
            if (tvStatTotalCourses != null) tvStatTotalCourses.setText(String.valueOf(UiUtils.optInt(es, "total_courses", 0)));
            if (tvStatActiveCourses != null) tvStatActiveCourses.setText(String.valueOf(UiUtils.optInt(es, "active_courses", 0)));
            if (tvStatTotalEnrollments != null) tvStatTotalEnrollments.setText(String.valueOf(UiUtils.optInt(es, "total_enrollments", 0)));
            if (tvStatAvgClassSize != null) {
                float avg = UiUtils.optFloat(es, "average_class_size", 0f);
                tvStatAvgClassSize.setText(String.format(Locale.US, "%.1f", avg));
            }
        }

        renderTopCourses();
        renderChart(data);
    }

    private void renderTopCourses() {
        if (containerTopCourses == null) return;
        containerTopCourses.removeAllViews();

        List<JsonObject> items = new ArrayList<>();
        if (cachedData != null && cachedData.has("courses") && cachedData.get("courses").isJsonArray()) {
            for (JsonElement el : cachedData.getAsJsonArray("courses")) {
                if (el.isJsonObject()) items.add(el.getAsJsonObject());
            }
        }

        if (activeTab == 2) {
            if (tvTopCoursesTitle != null) tvTopCoursesTitle.setText("Top Courses by Completion Rate");
            items.sort((a, b) -> Float.compare(UiUtils.optFloat(b, "completion_percentage", 0f), UiUtils.optFloat(a, "completion_percentage", 0f)));
        } else if (activeTab == 3) {
            if (tvTopCoursesTitle != null) tvTopCoursesTitle.setText("Top Courses by Average Score");
            items.sort((a, b) -> Float.compare(UiUtils.optFloat(b, "average_score", -1f), UiUtils.optFloat(a, "average_score", -1f)));
        } else {
            if (tvTopCoursesTitle != null) tvTopCoursesTitle.setText(R.string.top_5_enrolled_courses);
            items.sort((a, b) -> Integer.compare(UiUtils.optInt(b, "enrollment_count", 0), UiUtils.optInt(a, "enrollment_count", 0)));
        }

        if (items.isEmpty()) {
            if (tvTopCoursesEmpty != null) tvTopCoursesEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvTopCoursesEmpty != null) tvTopCoursesEmpty.setVisibility(View.GONE);

        int maxEnroll = 1;
        for (JsonObject obj : items) {
            int cnt = UiUtils.optInt(obj, "enrollment_count", 0);
            if (cnt > maxEnroll) maxEnroll = cnt;
        }

        int[] palette = new int[]{
                Color.parseColor("#3155F5"),
                Color.parseColor("#1260FF"),
                Color.parseColor("#6546F5"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#16A34A")
        };

        int limit = Math.min(5, items.size());
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < limit; i++) {
            JsonObject c = items.get(i);
            String code = UiUtils.optString(c, "code", "");
            String name = UiUtils.optString(c, "name", "");
            int enrolled = UiUtils.optInt(c, "enrollment_count", 0);
            float compPct = UiUtils.optFloat(c, "completion_percentage", 0f);
            float avgScore = UiUtils.optFloat(c, "average_score", -1f);

            String valueText;
            int barMax;
            int barProgress;
            if (activeTab == 2) {
                valueText = String.format(Locale.US, "%.0f%%", compPct);
                barMax = 100;
                barProgress = Math.round(compPct);
            } else if (activeTab == 3) {
                valueText = avgScore >= 0f ? String.format(Locale.US, "%.1f%%", avgScore) : "N/A";
                barMax = 100;
                barProgress = avgScore >= 0f ? Math.round(avgScore) : 0;
            } else {
                valueText = String.valueOf(enrolled);
                barMax = maxEnroll;
                barProgress = enrolled;
            }

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i < limit - 1) rowLp.bottomMargin = Math.round(10 * density);
            row.setLayoutParams(rowLp);

            LinearLayout headerRow = new LinearLayout(this);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameLp);
            tvName.setText((code.isEmpty() ? "" : code + " — ") + name);
            tvName.setTextColor(getColor(R.color.admin_text_primary));
            tvName.setTextSize(13f);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvVal = new TextView(this);
            tvVal.setText(valueText);
            tvVal.setTextColor(palette[i % palette.length]);
            tvVal.setTextSize(13f);
            tvVal.setTypeface(null, Typeface.BOLD);

            headerRow.addView(tvName);
            headerRow.addView(tvVal);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.round(8 * density));
            pbLp.topMargin = Math.round(4 * density);
            pb.setLayoutParams(pbLp);
            pb.setMax(Math.max(1, barMax));
            pb.setProgress(Math.max(0, barProgress));
            pb.setProgressTintList(ColorStateList.valueOf(palette[i % palette.length]));

            row.addView(headerRow);
            row.addView(pb);
            containerTopCourses.addView(row);
        }
    }

    private void renderChart(JsonObject data) {
        if (chartCourseTrendBar == null) return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (activeTab == 3 && data.has("courses") && data.get("courses").isJsonArray()) {
            JsonArray courses = data.getAsJsonArray("courses");
            int idx = 0;
            for (JsonElement el : courses) {
                if (!el.isJsonObject() || idx >= 8) continue;
                JsonObject c = el.getAsJsonObject();
                float passRate = UiUtils.optFloat(c, "pass_rate", -1f);
                if (passRate >= 0f) {
                    entries.add(new BarEntry(idx, passRate));
                    labels.add(UiUtils.optString(c, "code", "C" + (idx + 1)));
                    idx++;
                }
            }
        } else if (data.has("enrollment_by_semester") && data.get("enrollment_by_semester").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("enrollment_by_semester");
            for (int i = 0; i < arr.size(); i++) {
                if (!arr.get(i).isJsonObject()) continue;
                JsonObject item = arr.get(i).getAsJsonObject();
                entries.add(new BarEntry(entries.size(), UiUtils.optFloat(item, "count", 0f)));
                labels.add(UiUtils.optString(item, "label", ""));
            }
        }

        if (entries.isEmpty()) {
            chartCourseTrendBar.clear();
            chartCourseTrendBar.invalidate();
            return;
        }

        String labelTitle = activeTab == 3 ? getString(R.string.pass_rate) : getString(R.string.enrollments);
        BarDataSet dataSet = new BarDataSet(entries, labelTitle);
        dataSet.setColor(Color.parseColor("#3155F5"));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chartCourseTrendBar.setData(barData);
        chartCourseTrendBar.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
        ChartFontHelper.applyKhmerFont(chartCourseTrendBar.getContext(), chartCourseTrendBar);
        chartCourseTrendBar.invalidate();
    }

    private void selectTab(int tabIndex) {
        activeTab = tabIndex;
        TextView[] tabs = new TextView[]{tabOverview, tabEnrollment, tabCompletion, tabPerformance};
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
        if (cachedData != null) {
            renderTopCourses();
            renderChart(cachedData);
        }
    }

    private void setupListeners() {
        if (tabOverview != null) tabOverview.setOnClickListener(v -> selectTab(0));
        if (tabEnrollment != null) tabEnrollment.setOnClickListener(v -> selectTab(1));
        if (tabCompletion != null) tabCompletion.setOnClickListener(v -> selectTab(2));
        if (tabPerformance != null) tabPerformance.setOnClickListener(v -> selectTab(3));

        if (btnExportCourseReport != null) {
            btnExportCourseReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, GenerateReportActivity.class);
                intent.putExtra("selected_report_type", "Courses");
                startActivity(intent);
            });
        }

        if (btnViewCourseReportDetails != null) {
            btnViewCourseReportDetails.setOnClickListener(v -> showFullCourseBreakdownDialog());
        }
    }

    private void showFullCourseBreakdownDialog() {
        if (cachedData == null || !cachedData.has("courses") || !cachedData.get("courses").isJsonArray()) {
            Toast.makeText(this, "No course report data loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonArray courses = cachedData.getAsJsonArray("courses");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < courses.size(); i++) {
            if (!courses.get(i).isJsonObject()) continue;
            JsonObject c = courses.get(i).getAsJsonObject();
            String code = UiUtils.optString(c, "code", "");
            String name = UiUtils.optString(c, "name", "");
            String dept = UiUtils.optString(c, "department", "");
            int enrolled = UiUtils.optInt(c, "enrollment_count", 0);
            float compPct = UiUtils.optFloat(c, "completion_percentage", 0f);
            float passRate = UiUtils.optFloat(c, "pass_rate", -1f);
            sb.append("• ").append(code).append(" — ").append(name);
            if (!dept.isEmpty()) sb.append(" (").append(dept).append(")");
            sb.append("\n  Enrolled: ").append(enrolled)
                    .append(" • Completion: ").append(String.format(Locale.US, "%.1f%%", compPct));
            if (passRate >= 0f) {
                sb.append(" • Pass Rate: ").append(String.format(Locale.US, "%.1f%%", passRate));
            }
            sb.append("\n\n");
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Detailed Course Metrics (" + courses.size() + ")")
                .setMessage(sb.length() > 0 ? sb.toString().trim() : getString(R.string.empty_state_no_data))
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
