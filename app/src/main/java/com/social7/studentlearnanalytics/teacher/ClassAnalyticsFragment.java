package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.attendance.AttendanceFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassAnalyticsFragment extends Fragment {

    private static final String ARG_COURSE_CODE = "course_code";
    private static final String ARG_COURSE_NAME = "course_name";
    private static final String ARG_CLASS_NAME = "class_name";
    private static final String ARG_SEMESTER = "semester";
    private static final String ARG_OPENED_FROM_REPORTS = "opened_from_reports";

    private TextView tvSelectedCourseName, tvSelectedSemester;
    private TextView tvValClassAvg, tvValPassRate, tvValAtRiskCount, tvValTopPerformers;
    private BarChart barChartScoreDist;
    private LineChart lineChartAnalyticsTrend;
    private Integer selectedCourseId = null;
    private Integer selectedSemesterId = null;
    private final List<CourseOption> availableCourses = new ArrayList<>();
    private final List<SemesterOption> availableSemesters = new ArrayList<>();

    private static class CourseOption {
        final int id;
        final String code;
        final String name;

        CourseOption(int id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }
    }

    private static class SemesterOption {
        final int id;
        final String name;

        SemesterOption(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static ClassAnalyticsFragment newInstance(String courseCode, String courseName, String className, String semester, boolean openedFromReports) {
        ClassAnalyticsFragment fragment = new ClassAnalyticsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_CODE, courseCode);
        args.putString(ARG_COURSE_NAME, courseName);
        args.putString(ARG_CLASS_NAME, className);
        args.putString(ARG_SEMESTER, semester);
        args.putBoolean(ARG_OPENED_FROM_REPORTS, openedFromReports);
        fragment.setArguments(args);
        return fragment;
    }

    public ClassAnalyticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_teacher_analytics, container, false);

        tvSelectedCourseName = view.findViewById(R.id.tvSelectedCourseName);
        tvSelectedSemester = view.findViewById(R.id.tvSelectedSemester);
        tvValClassAvg = view.findViewById(R.id.tvValClassAvg);
        tvValPassRate = view.findViewById(R.id.tvValPassRate);
        tvValAtRiskCount = view.findViewById(R.id.tvValAtRiskCount);
        tvValTopPerformers = view.findViewById(R.id.tvValTopPerformers);

        if (tvValClassAvg != null) tvValClassAvg.setText("--");
        if (tvValPassRate != null) tvValPassRate.setText("--");
        if (tvValAtRiskCount != null) tvValAtRiskCount.setText("--");
        if (tvValTopPerformers != null) tvValTopPerformers.setText("--");

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherClassAnalytics), getString(R.string.class_analytics), "Detailed performance metrics"
            );
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

        barChartScoreDist = view.findViewById(R.id.barChartScoreDist);
        lineChartAnalyticsTrend = view.findViewById(R.id.lineChartAnalyticsTrend);

        if (barChartScoreDist != null) barChartScoreDist.setNoDataText("Loading grade distribution...");
        if (lineChartAnalyticsTrend != null) lineChartAnalyticsTrend.setNoDataText("Loading performance trend...");

        applySelectedCourseArguments();
        fetchTeacherAnalytics(barChartScoreDist, lineChartAnalyticsTrend);

        return view;
    }

    private void applySelectedCourseArguments() {
        if (getArguments() == null) return;

        if (getArguments().containsKey("course_id")) {
            int cid = getArguments().getInt("course_id", -1);
            if (cid > 0) selectedCourseId = cid;
        }
        String courseTitle = getArguments().getString("course_title");
        String courseCode = getArguments().getString(ARG_COURSE_CODE);
        String courseName = getArguments().getString(ARG_COURSE_NAME);
        String semester = getArguments().getString(ARG_SEMESTER);

        if (tvSelectedCourseName != null) {
            if (courseTitle != null && !courseTitle.isEmpty()) {
                tvSelectedCourseName.setText(courseTitle);
            } else if (courseCode != null && courseName != null) {
                tvSelectedCourseName.setText(courseCode + " - " + courseName);
            }
        }

        if (tvSelectedSemester != null && semester != null && !semester.isEmpty()) {
            tvSelectedSemester.setText(semester);
        }
    }

    private void fetchTeacherAnalytics(BarChart barChart, LineChart lineChart) {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherDashboardFiltered(selectedCourseId, selectedSemesterId).enqueue(
                new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject root = response.body();
                            JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                    ? root.getAsJsonObject("data") : root;

                            if (data.has("courses") && data.get("courses").isJsonArray()) {
                                availableCourses.clear();
                                for (JsonElement cel : data.getAsJsonArray("courses")) {
                                    if (!cel.isJsonObject()) continue;
                                    JsonObject co = cel.getAsJsonObject();
                                    int cid = co.has("id") && !co.get("id").isJsonNull() ? co.get("id").getAsInt() : 0;
                                    String cCode = co.has("code") && !co.get("code").isJsonNull() ? co.get("code").getAsString()
                                            : (co.has("course_code") && !co.get("course_code").isJsonNull() ? co.get("course_code").getAsString() : "COURSE");
                                    String cName = co.has("name") && !co.get("name").isJsonNull() ? co.get("name").getAsString()
                                            : (co.has("course_name") && !co.get("course_name").isJsonNull() ? co.get("course_name").getAsString() : "Course");
                                    if (cid > 0) availableCourses.add(new CourseOption(cid, cCode, cName));
                                }
                            }

                            if (data.has("semesters") && data.get("semesters").isJsonArray()) {
                                availableSemesters.clear();
                                for (JsonElement sel : data.getAsJsonArray("semesters")) {
                                    if (!sel.isJsonObject()) continue;
                                    JsonObject so = sel.getAsJsonObject();
                                    int sid = so.has("id") && !so.get("id").isJsonNull() ? so.get("id").getAsInt() : 0;
                                    String sName = so.has("name") && !so.get("name").isJsonNull() ? so.get("name").getAsString() : "";
                                    if (sid > 0 && !sName.isEmpty()) availableSemesters.add(new SemesterOption(sid, sName));
                                }
                            }

                            bindKpiMetricsFromApi(data);
                            bindBarChartFromApi(barChart, data);
                            bindLineChartFromApi(lineChart, data);
                        } else {
                            clearChartsWithMessage("Unable to load analytics (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        clearChartsWithMessage("Network error loading analytics");
                    }
                }
        );
    }

    private void bindKpiMetricsFromApi(JsonObject data) {
        if (data == null) return;
        int totalGraded = data.has("total_graded_records") && !data.get("total_graded_records").isJsonNull()
                ? data.get("total_graded_records").getAsInt() : 0;

        if (tvValClassAvg != null) {
            if (totalGraded > 0 && data.has("average_score") && !data.get("average_score").isJsonNull()) {
                tvValClassAvg.setText(String.format(Locale.US, "%.1f%%", data.get("average_score").getAsDouble()));
            } else {
                tvValClassAvg.setText("--");
            }
        }
        if (tvValPassRate != null) {
            if (totalGraded > 0 && data.has("pass_rate") && !data.get("pass_rate").isJsonNull()) {
                tvValPassRate.setText(String.format(Locale.US, "%.0f%%", data.get("pass_rate").getAsDouble()));
            } else {
                tvValPassRate.setText("--");
            }
        }
        if (tvValAtRiskCount != null) {
            if (data.has("at_risk_students_count") && !data.get("at_risk_students_count").isJsonNull()) {
                tvValAtRiskCount.setText(String.valueOf(data.get("at_risk_students_count").getAsInt()));
            } else {
                tvValAtRiskCount.setText("--");
            }
        }
        if (tvValTopPerformers != null) {
            if (data.has("top_performers_count") && !data.get("top_performers_count").isJsonNull()) {
                tvValTopPerformers.setText(String.valueOf(data.get("top_performers_count").getAsInt()));
            } else {
                tvValTopPerformers.setText("--");
            }
        }
    }

    private void clearChartsWithMessage(String msg) {
        if (barChartScoreDist != null) {
            barChartScoreDist.clear();
            barChartScoreDist.setNoDataText(msg);
            barChartScoreDist.invalidate();
        }
        if (lineChartAnalyticsTrend != null) {
            lineChartAnalyticsTrend.clear();
            lineChartAnalyticsTrend.setNoDataText(msg);
            lineChartAnalyticsTrend.invalidate();
        }
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void bindBarChartFromApi(BarChart barChart, JsonObject data) {
        if (barChart == null) return;
        int totalGraded = data != null && data.has("total_graded_records") && !data.get("total_graded_records").isJsonNull()
                ? data.get("total_graded_records").getAsInt() : 0;

        if (data == null || !data.has("grade_distribution") || !data.get("grade_distribution").isJsonObject() || totalGraded == 0) {
            barChart.clear();
            barChart.setNoDataText("No graded records available");
            barChart.invalidate();
            return;
        }

        JsonObject gd = data.getAsJsonObject("grade_distribution");
        List<Float> dist = new ArrayList<>();
        dist.add(gd.has("A") && !gd.get("A").isJsonNull() ? gd.get("A").getAsFloat() : 0f);
        dist.add(gd.has("B") && !gd.get("B").isJsonNull() ? gd.get("B").getAsFloat() : 0f);
        dist.add(gd.has("C") && !gd.get("C").isJsonNull() ? gd.get("C").getAsFloat() : 0f);
        dist.add(gd.has("D") && !gd.get("D").isJsonNull() ? gd.get("D").getAsFloat() : 0f);
        dist.add(gd.has("F") && !gd.get("F").isJsonNull() ? gd.get("F").getAsFloat() : 0f);
        setupBarChart(barChart, dist);
    }

    private void bindLineChartFromApi(LineChart lineChart, JsonObject data) {
        if (lineChart == null) return;
        List<Float> classData = new ArrayList<>();
        List<Float> targetData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (data != null && data.has("class_trend_data") && data.get("class_trend_data").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("class_trend_data");
            for (JsonElement el : arr) {
                if (el != null && !el.isJsonNull()) classData.add(el.getAsFloat());
            }
        }
        if (data != null && data.has("target_trend_data") && data.get("target_trend_data").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("target_trend_data");
            for (JsonElement el : arr) {
                if (el != null && !el.isJsonNull()) targetData.add(el.getAsFloat());
            }
        }
        if (data != null && data.has("class_trend_labels") && data.get("class_trend_labels").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("class_trend_labels");
            for (JsonElement el : arr) {
                if (el != null && !el.isJsonNull()) labels.add(el.getAsString());
            }
        }

        setupLineChart(lineChart, classData, targetData, labels);
    }

    private void showCourseDropdown(View anchor) {
        if (getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Assigned Courses");
        for (int i = 0; i < availableCourses.size(); i++) {
            CourseOption opt = availableCourses.get(i);
            popup.getMenu().add(0, i + 1, i + 1, opt.code + " - " + opt.name);
        }

        popup.setOnMenuItemClickListener(item -> {
            int idx = item.getItemId();
            if (idx == 0) {
                selectedCourseId = null;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText("All Assigned Courses");
            } else if (idx - 1 < availableCourses.size()) {
                CourseOption opt = availableCourses.get(idx - 1);
                selectedCourseId = opt.id;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText(opt.code + " - " + opt.name);
            }
            fetchTeacherAnalytics(barChartScoreDist, lineChartAnalyticsTrend);
            return true;
        });

        popup.show();
    }

    private void showSemesterDropdown(View anchor) {
        if (getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Semesters");
        for (int i = 0; i < availableSemesters.size(); i++) {
            SemesterOption opt = availableSemesters.get(i);
            popup.getMenu().add(0, i + 1, i + 1, opt.name);
        }
        popup.setOnMenuItemClickListener(item -> {
            int idx = item.getItemId();
            if (idx == 0) {
                selectedSemesterId = null;
                if (tvSelectedSemester != null) tvSelectedSemester.setText("All Semesters");
            } else if (idx - 1 < availableSemesters.size()) {
                SemesterOption opt = availableSemesters.get(idx - 1);
                selectedSemesterId = opt.id;
                if (tvSelectedSemester != null) tvSelectedSemester.setText(opt.name);
            }
            fetchTeacherAnalytics(barChartScoreDist, lineChartAnalyticsTrend);
            return true;
        });
        popup.show();
    }

    private void navigateToAtRiskScreen() {
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).openFragment(new PredictionRiskFragment());
        }
    }

    private void setupBarChart(BarChart chart, List<Float> dist) {
        if (chart == null) return;
        if (dist == null || dist.size() < 5) {
            chart.clear();
            chart.setNoDataText("No grade distribution data available");
            chart.invalidate();
            return;
        }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            entries.add(new BarEntry(i, dist.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.grade_distribution_5b81da));
        dataSet.setColors(new int[]{
                Color.parseColor("#2563FF"),
                Color.parseColor("#16C784"),
                Color.parseColor("#FF9B16"),
                Color.parseColor("#F97316"),
                Color.parseColor("#FF4055")
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
        leftAxis.setAxisMaximum(100f);
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
        chart.animateY(600);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }

    private void setupLineChart(LineChart chart, List<Float> classData, List<Float> targetData, List<String> labels) {
        if (chart == null) return;
        if (classData == null || classData.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No trend data available yet");
            chart.invalidate();
            return;
        }

        List<Entry> classEntries = new ArrayList<>();
        for (int i = 0; i < classData.size(); i++) {
            classEntries.add(new Entry(i, classData.get(i)));
        }

        LineDataSet classSet = new LineDataSet(classEntries, getString(R.string.class_average));
        classSet.setColor(Color.parseColor("#2563FF"));
        classSet.setLineWidth(2.5f);
        classSet.setCircleColor(Color.parseColor("#2563FF"));
        classSet.setCircleRadius(4f);
        classSet.setDrawValues(false);

        LineData lineData = new LineData(classSet);

        if (targetData != null && !targetData.isEmpty()) {
            List<Entry> targetEntries = new ArrayList<>();
            for (int i = 0; i < targetData.size(); i++) {
                targetEntries.add(new Entry(i, targetData.get(i)));
            }
            LineDataSet targetSet = new LineDataSet(targetEntries, "Target");
            targetSet.setColor(Color.parseColor("#9333EA"));
            targetSet.setLineWidth(2f);
            targetSet.enableDashedLine(10f, 5f, 0f);
            targetSet.setCircleColor(Color.parseColor("#9333EA"));
            targetSet.setCircleRadius(3.5f);
            targetSet.setDrawValues(false);
            lineData.addDataSet(targetSet);
        }

        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        if (labels != null && !labels.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
        }
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
        chart.animateX(600);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
