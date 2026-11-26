package com.social7.studentlearnanalytics.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.AssessmentAdapter;
import com.social7.studentlearnanalytics.adapter.ExamAdapter;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.model.LearningAnalytic;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.network.LearningAnalyticsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningAnalyticsFragment extends Fragment {

    private TextView tabAnalyticsOverview, tabAnalyticsAttendance, tabAnalyticsAssignments, tabAnalyticsQuizzes, tabAnalyticsExams, tabAnalyticsGpa;
    private View secAnalyticsOverviewContainer, secAnalyticsAttendanceContainer, secAnalyticsAssignmentsContainer, secAnalyticsQuizzesContainer, secAnalyticsExamsContainer, secAnalyticsGpaContainer;

    // Sub-Tab 0: Overview
    private TextView tvAnalyticsOverallScore, tvAnalyticsAttendanceRate, tvAnalyticsAssignmentAvg, tvAnalyticsQuizAvg, tvAnalyticsExamAvg, tvAnalyticsCurrentGpa;
    private LineChart lineChartAnalyticsTrend;
    private Spinner spinnerAnalyticsPeriod;
    private String currentPeriod = "weekly";

    // Sub-Tab 1: Attendance
    private PieChart pieChartAttendanceDetail;
    private LinearLayout layoutAnalyticsAttendanceList;

    // Sub-Tab 2: Assignments
    private RecyclerView rvAnalyticsAssignmentsRoster;

    // Sub-Tab 3: Quizzes
    private RecyclerView rvAnalyticsQuizzesRoster;

    // Sub-Tab 4: Exams
    private RecyclerView rvAnalyticsExamsRoster;

    // Sub-Tab 5: GPA
    private TextView tvGpaTabSemesterGpa, tvGpaTabCumulativeGpa, tvGpaTabCompletedCredits, tvGpaTabStanding;
    private LinearLayout layoutAnalyticsGpaList;

    private int selectedSubTab = 0;

    public LearningAnalyticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_fragment_learning_analytics, container, false);

        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.learning_analytics), "Track, Analyze & Improve"
            );
        }

        tabAnalyticsOverview = view.findViewById(R.id.tabAnalyticsOverview);
        tabAnalyticsAttendance = view.findViewById(R.id.tabAnalyticsAttendance);
        tabAnalyticsAssignments = view.findViewById(R.id.tabAnalyticsAssignments);
        tabAnalyticsQuizzes = view.findViewById(R.id.tabAnalyticsQuizzes);
        tabAnalyticsExams = view.findViewById(R.id.tabAnalyticsExams);
        tabAnalyticsGpa = view.findViewById(R.id.tabAnalyticsGpa);

        secAnalyticsOverviewContainer = view.findViewById(R.id.secAnalyticsOverviewContainer);
        secAnalyticsAttendanceContainer = view.findViewById(R.id.secAnalyticsAttendanceContainer);
        secAnalyticsAssignmentsContainer = view.findViewById(R.id.secAnalyticsAssignmentsContainer);
        secAnalyticsQuizzesContainer = view.findViewById(R.id.secAnalyticsQuizzesContainer);
        secAnalyticsExamsContainer = view.findViewById(R.id.secAnalyticsExamsContainer);
        secAnalyticsGpaContainer = view.findViewById(R.id.secAnalyticsGpaContainer);

        bindOverviewViews(view);
        bindAttendanceViews(view);
        bindAssignmentsViews(view);
        bindQuizzesViews(view);
        bindExamsViews(view);
        bindGpaViews(view);

        setupSubTabListeners();
        loadOverviewSummaryMetrics();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabAnalyticsOverview != null) tabAnalyticsOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabAnalyticsAttendance != null) tabAnalyticsAttendance.setOnClickListener(v -> selectSubTab(1));
        if (tabAnalyticsAssignments != null) tabAnalyticsAssignments.setOnClickListener(v -> selectSubTab(2));
        if (tabAnalyticsQuizzes != null) tabAnalyticsQuizzes.setOnClickListener(v -> selectSubTab(3));
        if (tabAnalyticsExams != null) tabAnalyticsExams.setOnClickListener(v -> selectSubTab(4));
        if (tabAnalyticsGpa != null) tabAnalyticsGpa.setOnClickListener(v -> selectSubTab(5));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secAnalyticsOverviewContainer != null) secAnalyticsOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secAnalyticsAttendanceContainer != null) secAnalyticsAttendanceContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secAnalyticsAssignmentsContainer != null) secAnalyticsAssignmentsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secAnalyticsQuizzesContainer != null) secAnalyticsQuizzesContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secAnalyticsExamsContainer != null) secAnalyticsExamsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
        if (secAnalyticsGpaContainer != null) secAnalyticsGpaContainer.setVisibility(subIndex == 5 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewTrendChart();
        } else if (subIndex == 1) {
            setupAttendanceSection();
        } else if (subIndex == 2) {
            setupAssignmentsSection();
        } else if (subIndex == 3) {
            setupQuizzesSection();
        } else if (subIndex == 4) {
            setupExamsSection();
        } else if (subIndex == 5) {
            setupGpaSection();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabAnalyticsOverview, tabAnalyticsAttendance, tabAnalyticsAssignments, tabAnalyticsQuizzes, tabAnalyticsExams, tabAnalyticsGpa};
        for (int i = 0; i < tabs.length; i++) {
            TextView tv = tabs[i];
            if (tv == null) continue;
            if (i == activeIndex) {
                tv.setBackgroundResource(R.drawable.bg_notif_tab_active);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
            }
        }
    }

    private void bindOverviewViews(View root) {
        tvAnalyticsOverallScore = root.findViewById(R.id.tvAnalyticsOverallScore);
        tvAnalyticsAttendanceRate = root.findViewById(R.id.tvAnalyticsAttendanceRate);
        tvAnalyticsAssignmentAvg = root.findViewById(R.id.tvAnalyticsAssignmentAvg);
        tvAnalyticsQuizAvg = root.findViewById(R.id.tvAnalyticsQuizAvg);
        tvAnalyticsExamAvg = root.findViewById(R.id.tvAnalyticsExamAvg);
        tvAnalyticsCurrentGpa = root.findViewById(R.id.tvAnalyticsCurrentGpa);
        lineChartAnalyticsTrend = root.findViewById(R.id.lineChartAnalyticsTrend);
        spinnerAnalyticsPeriod = root.findViewById(R.id.spinnerAnalyticsPeriod);

        if (spinnerAnalyticsPeriod != null) {
            spinnerAnalyticsPeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    String[] periods = {"weekly", "monthly", "semester", "yearly"};
                    if (position >= 0 && position < periods.length) {
                        currentPeriod = periods[position];
                        fetchLearningAnalyticsTrend();
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                }
            });
        }
    }

    private void loadOverviewSummaryMetrics() {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                DashboardResponse.DashboardData d = response.body().getData();
                if (tvAnalyticsOverallScore != null) tvAnalyticsOverallScore.setText(d.getOverallPerformanceFormatted());
                if (tvAnalyticsAttendanceRate != null) tvAnalyticsAttendanceRate.setText(d.getAttendanceRateFormatted());
                if (tvAnalyticsAssignmentAvg != null) tvAnalyticsAssignmentAvg.setText(d.getAssignmentScoreFormatted());
                if (tvAnalyticsQuizAvg != null) tvAnalyticsQuizAvg.setText(d.getQuizScoreFormatted());
                if (tvAnalyticsExamAvg != null) tvAnalyticsExamAvg.setText(d.getExamScoreFormatted());
                if (tvAnalyticsCurrentGpa != null) tvAnalyticsCurrentGpa.setText(d.getGpaFormatted());

                if (d.gpaSummary != null) {
                    if (tvGpaTabSemesterGpa != null) {
                        tvGpaTabSemesterGpa.setText(d.gpaSummary.semesterGpa != null
                                ? String.format(Locale.US, "%.2f", d.gpaSummary.semesterGpa) : d.getGpaFormatted());
                    }
                    if (tvGpaTabCumulativeGpa != null) {
                        tvGpaTabCumulativeGpa.setText(d.gpaSummary.cumulativeGpa != null
                                ? String.format(Locale.US, "%.2f / 4.0", d.gpaSummary.cumulativeGpa) : d.getGpaFormatted());
                    }
                    if (tvGpaTabCompletedCredits != null) {
                        int comp = d.gpaSummary.completedCredits != null ? Math.round(d.gpaSummary.completedCredits) : 0;
                        int semCred = d.gpaSummary.semesterCredits != null ? Math.round(d.gpaSummary.semesterCredits) : comp;
                        tvGpaTabCompletedCredits.setText(comp + " / " + Math.max(comp, semCred) + " Credits");
                    }
                    if (tvGpaTabStanding != null) {
                        tvGpaTabStanding.setText(d.gpaSummary.academicStanding != null && !d.gpaSummary.academicStanding.isEmpty()
                                ? d.gpaSummary.academicStanding : "Good Standing");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {}
        });
    }

    private void setupOverviewTrendChart() {
        if (lineChartAnalyticsTrend == null) return;
        fetchLearningAnalyticsTrend();
    }

    private void fetchLearningAnalyticsTrend() {
        if (!isAdded() || getContext() == null) return;
        ApiService apiService = ApiClient.getApiService(requireContext());
        apiService.getLearningAnalyticsTrend(currentPeriod, null).enqueue(new Callback<LearningAnalyticsResponse>() {
            @Override
            public void onResponse(@NonNull Call<LearningAnalyticsResponse> call, @NonNull Response<LearningAnalyticsResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    updateChartWithData(response.body().data);
                } else {
                    lineChartAnalyticsTrend.clear();
                    lineChartAnalyticsTrend.setNoDataText("No data available");
                    ChartFontHelper.applyKhmerFont(lineChartAnalyticsTrend.getContext(), lineChartAnalyticsTrend);
                    lineChartAnalyticsTrend.invalidate();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LearningAnalyticsResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                lineChartAnalyticsTrend.clear();
                lineChartAnalyticsTrend.setNoDataText("Network error");
                ChartFontHelper.applyKhmerFont(lineChartAnalyticsTrend.getContext(), lineChartAnalyticsTrend);
                lineChartAnalyticsTrend.invalidate();
            }
        });
    }

    private void updateChartWithData(List<LearningAnalytic> analytics) {
        if (analytics == null || analytics.isEmpty()) {
            lineChartAnalyticsTrend.clear();
            lineChartAnalyticsTrend.setNoDataText("No data available for this period");
            ChartFontHelper.applyKhmerFont(lineChartAnalyticsTrend.getContext(), lineChartAnalyticsTrend);
            lineChartAnalyticsTrend.invalidate();
            return;
        }

        List<Entry> attEntries = new ArrayList<>();
        List<Entry> asnEntries = new ArrayList<>();
        List<Entry> quizEntries = new ArrayList<>();
        List<Entry> examEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < analytics.size(); i++) {
            LearningAnalytic a = analytics.get(i);
            if (a.attendanceRate != null) attEntries.add(new Entry(i, a.attendanceRate));
            if (a.assignmentAverage != null) asnEntries.add(new Entry(i, a.assignmentAverage));
            if (a.quizAverage != null) quizEntries.add(new Entry(i, a.quizAverage));
            if (a.examAverage != null) examEntries.add(new Entry(i, a.examAverage));

            String label = "P" + (i + 1);
            if (a.periodLabel != null && !a.periodLabel.trim().isEmpty() && "semester".equals(currentPeriod)) {
                label = a.periodLabel.trim();
            } else if (a.periodStart != null && a.periodStart.length() >= 10) {
                if ("weekly".equals(currentPeriod)) {
                    label = "Wk " + (i + 1);
                } else if ("monthly".equals(currentPeriod)) {
                    label = a.periodStart.substring(5, 7) + "/" + a.periodStart.substring(2, 4);
                } else if ("yearly".equals(currentPeriod)) {
                    label = a.periodStart.substring(0, 4);
                } else {
                    label = a.periodStart.substring(0, 10);
                }
            }
            labels.add(label);
        }

        List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> dataSets = new ArrayList<>();

        if (!attEntries.isEmpty()) {
            LineDataSet attSet = new LineDataSet(attEntries, getString(R.string.attendance));
            attSet.setColor(Color.parseColor("#16A34A"));
            attSet.setLineWidth(2.5f);
            attSet.setCircleColor(Color.parseColor("#16A34A"));
            attSet.setDrawValues(false);
            dataSets.add(attSet);
        }

        if (!asnEntries.isEmpty()) {
            LineDataSet asnSet = new LineDataSet(asnEntries, getString(R.string.assignments));
            asnSet.setColor(Color.parseColor("#7C3AED"));
            asnSet.setLineWidth(2.5f);
            asnSet.setCircleColor(Color.parseColor("#7C3AED"));
            asnSet.setDrawValues(false);
            dataSets.add(asnSet);
        }

        if (!quizEntries.isEmpty()) {
            LineDataSet quizSet = new LineDataSet(quizEntries, getString(R.string.quizzes));
            quizSet.setColor(Color.parseColor("#F59E0B"));
            quizSet.setLineWidth(2.5f);
            quizSet.setCircleColor(Color.parseColor("#F59E0B"));
            quizSet.setDrawValues(false);
            dataSets.add(quizSet);
        }

        if (!examEntries.isEmpty()) {
            LineDataSet examSet = new LineDataSet(examEntries, getString(R.string.exams));
            examSet.setColor(Color.parseColor("#2563EB"));
            examSet.setLineWidth(2.5f);
            examSet.setCircleColor(Color.parseColor("#2563EB"));
            examSet.setDrawValues(false);
            dataSets.add(examSet);
        }

        if (dataSets.isEmpty()) {
            lineChartAnalyticsTrend.clear();
            lineChartAnalyticsTrend.setNoDataText("No analytics trend data available yet");
            ChartFontHelper.applyKhmerFont(lineChartAnalyticsTrend.getContext(), lineChartAnalyticsTrend);
            lineChartAnalyticsTrend.invalidate();
            return;
        }

        LineData lineData = new LineData(dataSets);
        lineChartAnalyticsTrend.setData(lineData);

        XAxis xAxis = lineChartAnalyticsTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#E0E0E0"));

        YAxis leftAxis = lineChartAnalyticsTrend.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        lineChartAnalyticsTrend.getAxisRight().setEnabled(false);
        lineChartAnalyticsTrend.getDescription().setEnabled(false);
        lineChartAnalyticsTrend.getLegend().setWordWrapEnabled(true);
        lineChartAnalyticsTrend.animateX(600);
        ChartFontHelper.applyKhmerFont(lineChartAnalyticsTrend.getContext(), lineChartAnalyticsTrend);
        lineChartAnalyticsTrend.invalidate();
    }

    // ==========================================
    // 1. ATTENDANCE SUB-TAB
    // ==========================================
    private void bindAttendanceViews(View root) {
        pieChartAttendanceDetail = root.findViewById(R.id.pieChartAttendanceDetail);
        layoutAnalyticsAttendanceList = root.findViewById(R.id.layoutAnalyticsAttendanceList);
    }

    private void setupAttendanceSection() {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(getContext()).getStudentAttendance(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                JsonObject body = response.body();
                JsonObject data = body.has("data") && body.get("data").isJsonObject()
                        ? body.getAsJsonObject("data") : body;
                JsonObject summary = data.has("summary") && data.get("summary").isJsonObject()
                        ? data.getAsJsonObject("summary") : data;
                JsonArray details = data.has("details") && data.get("details").isJsonArray()
                        ? data.getAsJsonArray("details") : new JsonArray();

                int present = getInt(summary, "present");
                int late = getInt(summary, "late");
                int excused = getInt(summary, "excused");
                int absent = getInt(summary, "absent");
                int total = getInt(summary, "total");
                float pct = summary.has("percentage") && !summary.get("percentage").isJsonNull()
                        ? summary.get("percentage").getAsFloat()
                        : (total > 0 ? (present * 100f / total) : 0f);

                if (pieChartAttendanceDetail != null) {
                    List<PieEntry> entries = new ArrayList<>();
                    List<Integer> colors = new ArrayList<>();
                    if (present > 0) {
                        entries.add(new PieEntry(present, "Present"));
                        colors.add(Color.parseColor("#10B981"));
                    }
                    if (late > 0) {
                        entries.add(new PieEntry(late, "Late"));
                        colors.add(Color.parseColor("#F59E0B"));
                    }
                    if (excused > 0) {
                        entries.add(new PieEntry(excused, "Excused"));
                        colors.add(Color.parseColor("#6366F1"));
                    }
                    if (absent > 0) {
                        entries.add(new PieEntry(absent, "Absent"));
                        colors.add(Color.parseColor("#EF4444"));
                    }
                    if (entries.isEmpty()) {
                        entries.add(new PieEntry(1f, "No Data"));
                        colors.add(Color.parseColor("#CBD5E1"));
                    }
                    PieDataSet ds = new PieDataSet(entries, "");
                    ds.setColors(colors);
                    ds.setSliceSpace(3f);
                    ds.setDrawValues(false);
                    pieChartAttendanceDetail.setData(new PieData(ds));
                    pieChartAttendanceDetail.setCenterText(String.format(Locale.US, "%.1f%%\nPresent", pct));
                    pieChartAttendanceDetail.setCenterTextSize(15f);
                    pieChartAttendanceDetail.getDescription().setEnabled(false);
                    ChartFontHelper.applyKhmerFont(pieChartAttendanceDetail.getContext(), pieChartAttendanceDetail);
                    pieChartAttendanceDetail.invalidate();
                }

                if (layoutAnalyticsAttendanceList != null) {
                    layoutAnalyticsAttendanceList.removeAllViews();
                    int limit = Math.min(details.size(), 15);
                    for (int i = 0; i < limit; i++) {
                        JsonElement el = details.get(i);
                        if (!el.isJsonObject()) continue;
                        JsonObject det = el.getAsJsonObject();
                        String status = det.has("status") && !det.get("status").isJsonNull()
                                ? det.get("status").getAsString() : "present";
                        String dateStr = "Session";
                        String courseName = "Course";
                        if (det.has("attendance") && det.get("attendance").isJsonObject()) {
                            JsonObject att = det.getAsJsonObject("attendance");
                            if (att.has("attendance_date") && !att.get("attendance_date").isJsonNull()) {
                                dateStr = att.get("attendance_date").getAsString();
                                if (dateStr.length() >= 10) dateStr = dateStr.substring(0, 10);
                            }
                            if (att.has("course") && att.get("course").isJsonObject()) {
                                JsonObject co = att.getAsJsonObject("course");
                                if (co.has("course_name") && !co.get("course_name").isJsonNull()) {
                                    courseName = co.get("course_name").getAsString();
                                } else if (co.has("name") && !co.get("name").isJsonNull()) {
                                    courseName = co.get("name").getAsString();
                                }
                            }
                        }
                        addSimpleRowCard(layoutAnalyticsAttendanceList,
                                courseName,
                                "Date: " + dateStr,
                                status.substring(0, 1).toUpperCase(Locale.US) + status.substring(1),
                                "present".equalsIgnoreCase(status) ? "#10B981" : ("absent".equalsIgnoreCase(status) ? "#EF4444" : "#F59E0B"));
                    }
                    if (layoutAnalyticsAttendanceList.getChildCount() == 0) {
                        addEmptyText(layoutAnalyticsAttendanceList, "No attendance sessions recorded yet.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    // ==========================================
    // 2. ASSIGNMENTS SUB-TAB
    // ==========================================
    private void bindAssignmentsViews(View root) {
        rvAnalyticsAssignmentsRoster = root.findViewById(R.id.rvAnalyticsAssignmentsRoster);
        if (rvAnalyticsAssignmentsRoster != null) {
            rvAnalyticsAssignmentsRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void setupAssignmentsSection() {
        if (!isAdded() || getContext() == null || rvAnalyticsAssignmentsRoster == null) return;
        ApiClient.getApiService(getContext()).getStudentAssignments(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                List<Assessment> list = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                            ? body.getAsJsonArray("data")
                            : (body.has("assignments") && body.get("assignments").isJsonArray()
                            ? body.getAsJsonArray("assignments") : new JsonArray());
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = obj.has("id") ? obj.get("id").getAsString() : "";
                        String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Assignment";
                        String dateStr = obj.has("due_date") && !obj.get("due_date").isJsonNull() ? formatDate(obj.get("due_date").getAsString()) : "Scheduled";
                        int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull() ? Math.round(obj.get("total_score").getAsFloat()) : 100;
                        int score = 0;
                        boolean isSubmitted = false;
                        boolean isGraded = false;
                        if (obj.has("submissions") && obj.get("submissions").isJsonArray()) {
                            JsonArray subs = obj.getAsJsonArray("submissions");
                            if (subs.size() > 0 && subs.get(0).isJsonObject()) {
                                JsonObject sub = subs.get(0).getAsJsonObject();
                                isSubmitted = true;
                                if (sub.has("score") && !sub.get("score").isJsonNull()) {
                                    score = Math.round(sub.get("score").getAsFloat());
                                    isGraded = true;
                                }
                            }
                        }
                        String courseName = extractCourseName(obj);
                        list.add(new Assessment(id, title, dateStr, score, totalScore, "assignment", null, courseName, isSubmitted, isGraded));
                    }
                }
                rvAnalyticsAssignmentsRoster.setAdapter(new AssessmentAdapter(list));
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    // ==========================================
    // 3. QUIZZES SUB-TAB
    // ==========================================
    private void bindQuizzesViews(View root) {
        rvAnalyticsQuizzesRoster = root.findViewById(R.id.rvAnalyticsQuizzesRoster);
        if (rvAnalyticsQuizzesRoster != null) {
            rvAnalyticsQuizzesRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void setupQuizzesSection() {
        if (!isAdded() || getContext() == null || rvAnalyticsQuizzesRoster == null) return;
        ApiClient.getApiService(getContext()).getStudentQuizzes(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                List<Assessment> list = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                            ? body.getAsJsonArray("data")
                            : (body.has("quizzes") && body.get("quizzes").isJsonArray()
                            ? body.getAsJsonArray("quizzes") : new JsonArray());
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = obj.has("id") ? obj.get("id").getAsString() : "";
                        String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Quiz";
                        String dateStr = obj.has("quiz_date") && !obj.get("quiz_date").isJsonNull()
                                ? formatDate(obj.get("quiz_date").getAsString())
                                : (obj.has("start_at") && !obj.get("start_at").isJsonNull() ? formatDate(obj.get("start_at").getAsString()) : "Scheduled");
                        int totalScore = obj.has("total_score") && !obj.get("total_score").isJsonNull() ? Math.round(obj.get("total_score").getAsFloat()) : 100;
                        int score = 0;
                        boolean isSubmitted = false;
                        boolean isGraded = false;
                        if (obj.has("attempts") && obj.get("attempts").isJsonArray()) {
                            JsonArray atts = obj.getAsJsonArray("attempts");
                            if (atts.size() > 0 && atts.get(0).isJsonObject()) {
                                JsonObject att = atts.get(0).getAsJsonObject();
                                isSubmitted = true;
                                if (att.has("score") && !att.get("score").isJsonNull()) {
                                    score = Math.round(att.get("score").getAsFloat());
                                    isGraded = true;
                                }
                            }
                        }
                        String courseName = extractCourseName(obj);
                        list.add(new Assessment(id, title, dateStr, score, totalScore, "quiz", null, courseName, isSubmitted, isGraded));
                    }
                }
                rvAnalyticsQuizzesRoster.setAdapter(new AssessmentAdapter(list));
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    // ==========================================
    // 4. EXAMS SUB-TAB
    // ==========================================
    private void bindExamsViews(View root) {
        rvAnalyticsExamsRoster = root.findViewById(R.id.rvAnalyticsExamsRoster);
        if (rvAnalyticsExamsRoster != null) {
            rvAnalyticsExamsRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void setupExamsSection() {
        if (!isAdded() || getContext() == null || rvAnalyticsExamsRoster == null) return;
        ApiClient.getApiService(getContext()).getStudentExams(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                List<Exam> list = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    JsonArray arr = body.has("data") && body.get("data").isJsonArray()
                            ? body.getAsJsonArray("data")
                            : (body.has("exams") && body.get("exams").isJsonArray()
                            ? body.getAsJsonArray("exams") : new JsonArray());
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();
                        String id = obj.has("id") ? obj.get("id").getAsString() : "";
                        String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "Exam";
                        String courseName = extractCourseName(obj);
                        String displayTitle = (courseName != null && !courseName.isEmpty()) ? (courseName + " – " + title) : title;
                        String examDate = obj.has("exam_date") && !obj.get("exam_date").isJsonNull() ? obj.get("exam_date").getAsString() : "";
                        String[] md = parseMonthDay(examDate);
                        int totalMarks = obj.has("total_marks") && !obj.get("total_marks").isJsonNull()
                                ? Math.round(obj.get("total_marks").getAsFloat())
                                : (obj.has("total_score") && !obj.get("total_score").isJsonNull() ? Math.round(obj.get("total_score").getAsFloat()) : 100);
                        boolean completed = false;
                        String scoreText = null;
                        if (obj.has("results") && obj.get("results").isJsonArray()) {
                            JsonArray res = obj.getAsJsonArray("results");
                            if (res.size() > 0 && res.get(0).isJsonObject()) {
                                JsonObject r = res.get(0).getAsJsonObject();
                                if (r.has("score") && !r.get("score").isJsonNull()) {
                                    completed = true;
                                    scoreText = Math.round(r.get("score").getAsFloat()) + " / " + totalMarks;
                                }
                            }
                        }
                        String examType = obj.has("exam_type") && !obj.get("exam_type").isJsonNull() ? obj.get("exam_type").getAsString() : "Exam";
                        String details = examType.substring(0, 1).toUpperCase(Locale.US) + examType.substring(1) + (completed ? " • Completed" : " • Scheduled");
                        list.add(new Exam(id, md[0], md[1], displayTitle, details, completed, scoreText));
                    }
                }
                rvAnalyticsExamsRoster.setAdapter(new ExamAdapter(requireContext(), list));
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    // ==========================================
    // 5. GPA SUB-TAB
    // ==========================================
    private void bindGpaViews(View root) {
        tvGpaTabSemesterGpa = root.findViewById(R.id.tvGpaTabSemesterGpa);
        tvGpaTabCumulativeGpa = root.findViewById(R.id.tvGpaTabCumulativeGpa);
        tvGpaTabCompletedCredits = root.findViewById(R.id.tvGpaTabCompletedCredits);
        tvGpaTabStanding = root.findViewById(R.id.tvGpaTabStanding);
        layoutAnalyticsGpaList = root.findViewById(R.id.layoutAnalyticsGpaList);
    }

    private void setupGpaSection() {
        if (!isAdded() || getContext() == null || layoutAnalyticsGpaList == null) return;
        loadOverviewSummaryMetrics();

        ApiClient.getApiService(getContext()).getStudentDashboard(null, null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || layoutAnalyticsGpaList == null) return;
                layoutAnalyticsGpaList.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject body = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;
                    if (body.has("gpa_records") && body.get("gpa_records").isJsonArray()) {
                        JsonArray records = body.getAsJsonArray("gpa_records");
                        for (JsonElement el : records) {
                            if (!el.isJsonObject()) continue;
                            JsonObject rec = el.getAsJsonObject();
                            String semName = "Semester";
                            if (rec.has("semester") && rec.get("semester").isJsonObject()) {
                                JsonObject s = rec.getAsJsonObject("semester");
                                if (s.has("name") && !s.get("name").isJsonNull()) semName = s.get("name").getAsString();
                                else if (s.has("semester_name") && !s.get("semester_name").isJsonNull()) semName = s.get("semester_name").getAsString();
                            }
                            double gpaVal = rec.has("gpa") && !rec.get("gpa").isJsonNull() ? rec.get("gpa").getAsDouble() : 0.0;
                            int credits = 0;
                            if (rec.has("earned_credits") && !rec.get("earned_credits").isJsonNull()) {
                                credits = (int) Math.round(rec.get("earned_credits").getAsDouble());
                            } else if (rec.has("total_credits") && !rec.get("total_credits").isJsonNull()) {
                                credits = (int) Math.round(rec.get("total_credits").getAsDouble());
                            } else if (rec.has("credit_hours") && !rec.get("credit_hours").isJsonNull()) {
                                credits = (int) Math.round(rec.get("credit_hours").getAsDouble());
                            }
                            String standing = rec.has("academic_standing") && !rec.get("academic_standing").isJsonNull()
                                    ? rec.get("academic_standing").getAsString() : "Good Standing";

                            addSimpleRowCard(layoutAnalyticsGpaList,
                                    semName,
                                    "Credits: " + credits + " • Standing: " + standing,
                                    String.format(Locale.US, "GPA %.2f", gpaVal),
                                    gpaVal >= 3.5 ? "#10B981" : (gpaVal >= 2.5 ? "#5B3DF5" : "#F59E0B"));
                        }
                    }
                }
                if (layoutAnalyticsGpaList.getChildCount() == 0) {
                    addEmptyText(layoutAnalyticsGpaList, "No semester GPA records found yet.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || layoutAnalyticsGpaList == null) return;
                layoutAnalyticsGpaList.removeAllViews();
                addEmptyText(layoutAnalyticsGpaList, "Network error loading GPA records.");
            }
        });
    }

    private void addSimpleRowCard(LinearLayout container, String title, String subtitle, String rightText, String rightColorHex) {
        if (!isAdded() || getContext() == null) return;
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);
        card.setRadius(dp(12));
        card.setCardElevation(dp(1));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout left = new LinearLayout(requireContext());
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvT = new TextView(requireContext());
        tvT.setText(title);
        tvT.setTextColor(Color.parseColor("#1E1B4B"));
        tvT.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvT.setTypeface(null, Typeface.BOLD);
        left.addView(tvT);

        TextView tvS = new TextView(requireContext());
        tvS.setText(subtitle);
        tvS.setTextColor(Color.parseColor("#64748B"));
        tvS.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        left.addView(tvS);

        TextView tvR = new TextView(requireContext());
        tvR.setText(rightText);
        tvR.setTextColor(Color.parseColor(rightColorHex));
        tvR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvR.setTypeface(null, Typeface.BOLD);

        row.addView(left);
        row.addView(tvR);
        card.addView(row);
        container.addView(card);
    }

    private void addEmptyText(LinearLayout container, String text) {
        if (!isAdded() || getContext() == null) return;
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setPadding(dp(8), dp(16), dp(8), dp(16));
        container.addView(tv);
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

    private String extractCourseName(JsonObject obj) {
        if (obj.has("course") && obj.get("course").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("course");
            if (c.has("course_name") && !c.get("course_name").isJsonNull()) return c.get("course_name").getAsString();
            if (c.has("name") && !c.get("name").isJsonNull()) return c.get("name").getAsString();
        }
        return null;
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return "Scheduled";
        try {
            String clean = rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date d = inFmt.parse(clean);
            if (d != null) return outFmt.format(d);
        } catch (Exception ignored) {}
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }

    private String[] parseMonthDay(String rawDate) {
        if (rawDate != null && rawDate.length() >= 10) {
            try {
                String clean = rawDate.substring(0, 10);
                SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date d = inFmt.parse(clean);
                if (d != null) {
                    String m = new SimpleDateFormat("MMM", Locale.US).format(d).toUpperCase(Locale.US);
                    String day = new SimpleDateFormat("dd", Locale.US).format(d);
                    return new String[]{m, day};
                }
            } catch (Exception ignored) {}
        }
        return new String[]{"TBA", "--"};
    }
}
