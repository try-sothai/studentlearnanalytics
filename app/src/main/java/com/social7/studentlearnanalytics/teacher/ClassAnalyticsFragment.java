package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    private static final String ARG_COURSE_CODE = "course_code";
    private static final String ARG_COURSE_NAME = "course_name";
    private static final String ARG_CLASS_NAME = "class_name";
    private static final String ARG_SEMESTER = "semester";
    private static final String ARG_OPENED_FROM_REPORTS = "opened_from_reports";

    private TextView tvSelectedCourseName, tvSelectedSemester;
    private TextView tvClassAvgScore, tvClassPassRate, tvAtRiskCount, tvTopPerformerCount;
    private BarChart barChartScoreDist;
    private LineChart lineChartAnalyticsTrend;

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

        boolean openedFromReports = getArguments() != null && getArguments().getBoolean(ARG_OPENED_FROM_REPORTS, false);

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherClassAnalytics),
                    "Class Analytics",
                    "Detailed performance metrics"
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

        applySelectedCourseArguments();
        fetchTeacherAnalytics(barChartScoreDist, lineChartAnalyticsTrend);

        return view;
    }

    private void applySelectedCourseArguments() {
        if (getArguments() == null) return;

        String courseCode = getArguments().getString(ARG_COURSE_CODE);
        String courseName = getArguments().getString(ARG_COURSE_NAME);
        String className = getArguments().getString(ARG_CLASS_NAME);
        String semester = getArguments().getString(ARG_SEMESTER);

        if (tvSelectedCourseName != null && courseCode != null && courseName != null) {
            tvSelectedCourseName.setText(courseCode + " - " + courseName);
        }

        if (tvSelectedSemester != null && semester != null) {
            tvSelectedSemester.setText(semester);
        }
    }

    private void fetchTeacherAnalytics(BarChart barChart, LineChart lineChart) {
        if (getContext() == null) return;

        com.social7.studentlearnanalytics.network.ApiClient.getApiService(requireContext()).getTeacherDashboard().enqueue(
                new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                com.google.gson.JsonObject root = response.body();
                                com.google.gson.JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                        ? root.getAsJsonObject("data") : root;

                                bindBarChartFromApi(barChart, data);
                                bindLineChartFromApi(lineChart, data);
                            } catch (Exception e) {
                                setupBarChart(barChart, null);
                                setupLineChart(lineChart, null, null, null);
                            }
                        } else {
                            setupBarChart(barChart, null);
                            setupLineChart(lineChart, null, null, null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setupBarChart(barChart, null);
                        setupLineChart(lineChart, null, null, null);
                    }
                }
        );
    }

    private void bindBarChartFromApi(BarChart barChart, com.google.gson.JsonObject data) {
        if (barChart == null) return;
        List<Float> dist = new ArrayList<>();
        if (data != null && data.has("grade_distribution") && data.get("grade_distribution").isJsonObject()) {
            com.google.gson.JsonObject gd = data.getAsJsonObject("grade_distribution");
            dist.add(gd.has("A") ? gd.get("A").getAsFloat() : 28f);
            dist.add(gd.has("B") ? gd.get("B").getAsFloat() : 32f);
            dist.add(gd.has("C") ? gd.get("C").getAsFloat() : 22f);
            dist.add(gd.has("D") ? gd.get("D").getAsFloat() : 12f);
            dist.add(gd.has("F") ? gd.get("F").getAsFloat() : 6f);
        } else {
            dist.add(28f); dist.add(32f); dist.add(22f); dist.add(12f); dist.add(6f);
        }
        setupBarChart(barChart, dist);
    }

    private void bindLineChartFromApi(LineChart lineChart, com.google.gson.JsonObject data) {
        if (lineChart == null) return;
        List<Float> classData = new ArrayList<>();
        List<Float> targetData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (data != null && data.has("class_trend_data") && data.get("class_trend_data").isJsonArray()) {
            com.google.gson.JsonArray arr = data.getAsJsonArray("class_trend_data");
            for (com.google.gson.JsonElement el : arr) classData.add(el.getAsFloat());
        }
        if (data != null && data.has("target_trend_data") && data.get("target_trend_data").isJsonArray()) {
            com.google.gson.JsonArray arr = data.getAsJsonArray("target_trend_data");
            for (com.google.gson.JsonElement el : arr) targetData.add(el.getAsFloat());
        }
        if (data != null && data.has("class_trend_labels") && data.get("class_trend_labels").isJsonArray()) {
            com.google.gson.JsonArray arr = data.getAsJsonArray("class_trend_labels");
            for (com.google.gson.JsonElement el : arr) labels.add(el.getAsString());
        }

        setupLineChart(lineChart, classData, targetData, labels);
    }

    private void showCourseDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "CS101 - Intro to Computer Science");
        popup.getMenu().add(0, 2, 1, "IT201 - Web Development Systems");
        popup.getMenu().add(0, 3, 2, "MA101 - Calculus I for Engineers");
        popup.getMenu().add(0, 4, 3, "BUS101 - Principles of Business");
        popup.getMenu().add(0, 5, 4, "EN101 - English Communication");

        popup.setOnMenuItemClickListener(item -> {
            if (tvSelectedCourseName != null) {
                tvSelectedCourseName.setText(item.getTitle());
            }
            fetchTeacherAnalytics(barChartScoreDist, lineChartAnalyticsTrend);
            return true;
        });

        popup.show();
    }

    private void showSemesterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Semester 1, 2024-2025");
        popup.getMenu().add(0, 2, 1, "Semester 2, 2023-2024");
        popup.getMenu().add(0, 3, 2, "Semester 1, 2023-2024");

        popup.setOnMenuItemClickListener(item -> {
            if (tvSelectedSemester != null) {
                tvSelectedSemester.setText(item.getTitle());
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
        List<BarEntry> entries = new ArrayList<>();
        if (dist != null && dist.size() >= 5) {
            for (int i = 0; i < 5; i++) {
                entries.add(new BarEntry(i, dist.get(i)));
            }
        } else {
            entries.add(new BarEntry(0, 28f));
            entries.add(new BarEntry(1, 32f));
            entries.add(new BarEntry(2, 22f));
            entries.add(new BarEntry(3, 12f));
            entries.add(new BarEntry(4, 6f));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Grade Distribution");
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
        chart.animateY(800);
        chart.invalidate();
    }

    private void setupLineChart(LineChart chart, List<Float> classData, List<Float> targetData, List<String> labels) {
        if (chart == null) return;

        List<Entry> classEntries = new ArrayList<>();
        if (classData != null && !classData.isEmpty()) {
            for (int i = 0; i < classData.size(); i++) {
                classEntries.add(new Entry(i, classData.get(i)));
            }
        } else {
            classEntries.add(new Entry(0, 63f));
            classEntries.add(new Entry(1, 70f));
            classEntries.add(new Entry(2, 81f));
            classEntries.add(new Entry(3, 87f));
        }

        List<Entry> targetEntries = new ArrayList<>();
        if (targetData != null && !targetData.isEmpty()) {
            for (int i = 0; i < targetData.size(); i++) {
                targetEntries.add(new Entry(i, targetData.get(i)));
            }
        } else {
            targetEntries.add(new Entry(0, 54f));
            targetEntries.add(new Entry(1, 60f));
            targetEntries.add(new Entry(2, 68f));
            targetEntries.add(new Entry(3, 77f));
        }

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
        String[] xAxisLabels = (labels != null && !labels.isEmpty())
                ? labels.toArray(new String[0])
                : new String[]{"Sep", "Oct", "Nov", "Dec"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
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
