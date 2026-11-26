package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.model.Student;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.student.AtRiskStudentAdapter;
import com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionRiskFragment extends Fragment {

    private TextView tabPredOverview, tabPredAtRisk, tabPredExamPrediction, tabPredRiskAnalysis;
    private View secPredOverviewContainer, secPredAtRiskContainer, secPredExamPredictionContainer, secPredRiskAnalysisContainer;

    private BarChart chartOverviewScoreDist;
    private PieChart chartOverviewRiskPie;
    private TextView tvPredOverviewCount, tvPredOverviewAvgScore, tvPredOverviewAtRisk, tvPredOverviewAccuracy;
    private TextView tvPredExamAcademicAvg, tvPredExamPredictedAvg, tvPredExamExpectedGrade, tvPredExamConfidence;
    private TextView tvRiskSummaryFactor, tvRiskSummaryHighCount, tvRiskSummaryIndex, tvRiskSummaryImprovement;

    private AtRiskStudentAdapter atRiskAdapter;
    private RecyclerView rvAtRiskStudents;
    private EditText edtSearchAtRisk;
    private String selectedCategory = "All";
    private String searchQuery = "";
    private final List<AtRiskStudentAdapter.AtRiskStudent> atRiskList = new ArrayList<>();

    private RecyclerView rvExamPredictionsList;
    private ExamPredictionAdapter examPredictionAdapter;
    private final List<ExamPredictionAdapter.ExamPredictionItem> examPredictionList = new ArrayList<>();

    private RecyclerView rvCourseRiskAnalysis;
    private RiskAnalysisAdapter riskAnalysisAdapter;
    private final List<RiskAnalysisAdapter.RiskAnalysisItem> riskAnalysisList = new ArrayList<>();

    private int selectedSubTab = 0;

    public PredictionRiskFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_prediction_risk, container, false);

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.prediction_risk), "Machine learning academic risk & exam score analytics"
            );
        }

        tabPredOverview = view.findViewById(R.id.tabPredOverview);
        tabPredAtRisk = view.findViewById(R.id.tabPredAtRisk);
        tabPredExamPrediction = view.findViewById(R.id.tabPredExamPrediction);
        tabPredRiskAnalysis = view.findViewById(R.id.tabPredRiskAnalysis);

        secPredOverviewContainer = view.findViewById(R.id.secPredOverviewContainer);
        secPredAtRiskContainer = view.findViewById(R.id.secPredAtRiskContainer);
        secPredExamPredictionContainer = view.findViewById(R.id.secPredExamPredictionContainer);
        secPredRiskAnalysisContainer = view.findViewById(R.id.secPredRiskAnalysisContainer);

        bindOverviewViews(view);
        bindAtRiskViews(view);
        bindExamPredictionViews(view);
        bindRiskAnalysisViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchRiskAndPredictions();

        return view;
    }

    private void setupSubTabListeners() {
        if (tabPredOverview != null) tabPredOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabPredAtRisk != null) tabPredAtRisk.setOnClickListener(v -> selectSubTab(1));
        if (tabPredExamPrediction != null) tabPredExamPrediction.setOnClickListener(v -> selectSubTab(2));
        if (tabPredRiskAnalysis != null) tabPredRiskAnalysis.setOnClickListener(v -> selectSubTab(3));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secPredOverviewContainer != null) secPredOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secPredAtRiskContainer != null) secPredAtRiskContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secPredExamPredictionContainer != null) secPredExamPredictionContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secPredRiskAnalysisContainer != null) secPredRiskAnalysisContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabPredOverview, tabPredAtRisk, tabPredExamPrediction, tabPredRiskAnalysis};
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

    private void fetchRiskAndPredictions() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherRiskPredictions().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    bindOverviewFromApi(data);
                    bindAtRiskFromApi(data.has("at_risk_students") && data.get("at_risk_students").isJsonArray()
                            ? data.getAsJsonArray("at_risk_students") : null);
                    bindPredictionsFromApi(data.has("predictions") && data.get("predictions").isJsonArray()
                            ? data.getAsJsonArray("predictions") : null);
                    bindCourseRisksFromApi(data.has("course_risks") && data.get("course_risks").isJsonArray()
                            ? data.getAsJsonArray("course_risks") : null);
                } else {
                    Toast.makeText(getContext(), "Unable to load risk & prediction data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading risk & predictions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOverviewViews(View root) {
        chartOverviewScoreDist = root.findViewById(R.id.chartOverviewScoreDist);
        chartOverviewRiskPie = root.findViewById(R.id.chartOverviewRiskPie);
        tvPredOverviewCount = root.findViewById(R.id.tvPredOverviewCount);
        tvPredOverviewAvgScore = root.findViewById(R.id.tvPredOverviewAvgScore);
        tvPredOverviewAtRisk = root.findViewById(R.id.tvPredOverviewAtRisk);
        tvPredOverviewAccuracy = root.findViewById(R.id.tvPredOverviewAccuracy);
        tvPredExamAcademicAvg = root.findViewById(R.id.tvPredExamAcademicAvg);
        tvPredExamPredictedAvg = root.findViewById(R.id.tvPredExamPredictedAvg);
        tvPredExamExpectedGrade = root.findViewById(R.id.tvPredExamExpectedGrade);
        tvPredExamConfidence = root.findViewById(R.id.tvPredExamConfidence);
        tvRiskSummaryFactor = root.findViewById(R.id.tvRiskSummaryFactor);
        tvRiskSummaryHighCount = root.findViewById(R.id.tvRiskSummaryHighCount);
        tvRiskSummaryIndex = root.findViewById(R.id.tvRiskSummaryIndex);
        tvRiskSummaryImprovement = root.findViewById(R.id.tvRiskSummaryImprovement);
        if (chartOverviewScoreDist != null) chartOverviewScoreDist.setNoDataText("Loading score distribution...");
        if (chartOverviewRiskPie != null) chartOverviewRiskPie.setNoDataText("Loading risk distribution...");
    }

    private void bindOverviewFromApi(JsonObject data) {
        int totalPreds = (data.has("predictions") && data.get("predictions").isJsonArray())
                ? data.getAsJsonArray("predictions").size() : 0;
        int atRiskTot = (data.has("at_risk_students") && data.get("at_risk_students").isJsonArray())
                ? data.getAsJsonArray("at_risk_students").size() : 0;
        if (tvPredOverviewCount != null) tvPredOverviewCount.setText(String.valueOf(totalPreds));
        if (tvPredOverviewAtRisk != null) tvPredOverviewAtRisk.setText(atRiskTot + " Students");
        if (tvPredOverviewAvgScore != null) {
            double avg = getDouble(data, "average_predicted_score");
            tvPredOverviewAvgScore.setText(totalPreds > 0 && avg > 0 ? String.format(Locale.US, "%.1f%%", avg) : "--");
        }
        if (tvPredOverviewAccuracy != null) {
            double acc = getDouble(data, "model_accuracy");
            tvPredOverviewAccuracy.setText(acc > 0 ? String.format(Locale.US, "%.1f%%", acc) : "--");
        }
        if (chartOverviewScoreDist != null) {
            JsonObject sb = data.has("score_buckets") && data.get("score_buckets").isJsonObject()
                    ? data.getAsJsonObject("score_buckets") : null;
            if (sb != null) {
                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0, getInt(sb, "90-100")));
                entries.add(new BarEntry(1, getInt(sb, "80-89")));
                entries.add(new BarEntry(2, getInt(sb, "70-79")));
                entries.add(new BarEntry(3, getInt(sb, "60-69")));
                entries.add(new BarEntry(4, getInt(sb, "<60")));

                BarDataSet dataSet = new BarDataSet(entries, getString(R.string.predicted_scores));
                dataSet.setColors(new int[]{
                        Color.parseColor("#10B981"), Color.parseColor("#3155F5"), Color.parseColor("#1260FF"), Color.parseColor("#F59E0B"), Color.parseColor("#EF4444")
                });
                dataSet.setDrawValues(false);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.5f);
                chartOverviewScoreDist.setData(barData);

                XAxis xAxis = chartOverviewScoreDist.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"90-100", "80-89", "70-79", "60-69", "<60"}));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);

                chartOverviewScoreDist.getAxisRight().setEnabled(false);
                chartOverviewScoreDist.getDescription().setEnabled(false);
                ChartFontHelper.applyKhmerFont(chartOverviewScoreDist.getContext(), chartOverviewScoreDist);
                chartOverviewScoreDist.invalidate();
            } else {
                chartOverviewScoreDist.clear();
                chartOverviewScoreDist.setNoDataText("No score distribution available");
                chartOverviewScoreDist.invalidate();
            }
        }

        if (chartOverviewRiskPie != null) {
            JsonObject rd = data.has("risk_distribution") && data.get("risk_distribution").isJsonObject()
                    ? data.getAsJsonObject("risk_distribution") : null;
            int low = getInt(rd, "low");
            int med = getInt(rd, "medium");
            int high = getInt(rd, "high");
            int tot = low + med + high;

            if (tot > 0) {
                List<PieEntry> entries = new ArrayList<>();
                List<Integer> colors = new ArrayList<>();
                if (low > 0) {
                    entries.add(new PieEntry(low, "Low Risk"));
                    colors.add(Color.parseColor("#10B981"));
                }
                if (med > 0) {
                    entries.add(new PieEntry(med, "Medium Risk"));
                    colors.add(Color.parseColor("#F59E0B"));
                }
                if (high > 0) {
                    entries.add(new PieEntry(high, "High Risk"));
                    colors.add(Color.parseColor("#EF4444"));
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);
                dataSet.setSliceSpace(2f);

                PieData pieData = new PieData(dataSet);
                pieData.setDrawValues(false);

                chartOverviewRiskPie.setData(pieData);
                chartOverviewRiskPie.setHoleRadius(50f);
                chartOverviewRiskPie.setCenterText(tot + "\nStudents");
                chartOverviewRiskPie.setCenterTextSize(14f);
                chartOverviewRiskPie.getDescription().setEnabled(false);
                ChartFontHelper.applyKhmerFont(chartOverviewRiskPie.getContext(), chartOverviewRiskPie);
                chartOverviewRiskPie.invalidate();
            } else {
                chartOverviewRiskPie.clear();
                chartOverviewRiskPie.setNoDataText("No risk assessment records available");
                chartOverviewRiskPie.invalidate();
            }
        }
    }

    private void bindAtRiskViews(View root) {
        rvAtRiskStudents = root.findViewById(R.id.rvAtRiskStudents);
        edtSearchAtRisk = root.findViewById(R.id.edtSearchAtRisk);

        if (rvAtRiskStudents != null && getContext() != null) {
            rvAtRiskStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
            atRiskAdapter = new AtRiskStudentAdapter(requireContext(), atRiskList, this::showStudentRiskProfileDetailDialog);
            rvAtRiskStudents.setAdapter(atRiskAdapter);
        }

        if (edtSearchAtRisk != null) {
            edtSearchAtRisk.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (atRiskAdapter != null) {
                        atRiskAdapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void bindAtRiskFromApi(JsonArray arr) {
        atRiskList.clear();
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                String code = getStr(o, "student_code", "STU");
                String name = getStr(o, "name", "Student");
                String cCode = getStr(o, "course_code", "Course");
                String cls = getStr(o, "class_name", "");
                String rawYl = getStr(o, "year_level", "");
                String ylBadge = com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(rawYl) != null
                        ? com.social7.studentlearnanalytics.utils.YearLevelHelper.toBadgeText(getContext(), rawYl)
                        : "";
                String yearAndClass = !ylBadge.isEmpty()
                        ? (cls.isEmpty() ? ylBadge : (ylBadge + " • " + cls))
                        : (!cls.isEmpty() ? cls : getString(R.string.year_level_unassigned));
                String riskLvl = getStr(o, "risk_level", "Medium Risk");
                String scoreStr = String.format(Locale.US, "%.0f%%", getDouble(o, "risk_score"));
                String factors = getStr(o, "risk_factors", "Academic & Attendance Risk");
                AtRiskStudentAdapter.AtRiskStudent ars = new AtRiskStudentAdapter.AtRiskStudent(
                        code, name, cCode, yearAndClass, riskLvl,
                        "-", scoreStr, "-",
                        "• " + factors, "", "",
                        "#FFE4E6", "#FF4055",
                        Collections.singletonList("At Risk")
                );
                ars.numericStudentId = getInt(o, "student_id");
                atRiskList.add(ars);
            }
        }
        if (rvAtRiskStudents != null && getContext() != null) {
            atRiskAdapter = new AtRiskStudentAdapter(requireContext(), atRiskList, this::showStudentRiskProfileDetailDialog);
            rvAtRiskStudents.setAdapter(atRiskAdapter);
            atRiskAdapter.filter(searchQuery, selectedCategory);
        }
    }

    private void showStudentRiskProfileDetailDialog(AtRiskStudentAdapter.AtRiskStudent student) {
        if (!isAdded() || getContext() == null) return;

        String details = "Student Name: " + student.name + "\n" +
                "Student ID: " + student.id + "\n" +
                "Course / Cohort: " + student.program + " (" + student.year + ")\n" +
                "Risk Level: " + student.riskBadge + "\n" +
                "Risk Score: " + student.avgScore + "\n\n" +
                "Identified Risk Factors:\n" +
                student.reason1;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Student Risk Profile & Diagnosis")
                .setMessage(details)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        if (student.numericStudentId > 0) {
            builder.setPositiveButton("View Full Student Profile", (dialog, which) -> {
                android.content.Intent intent = new android.content.Intent(
                        requireContext(), com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity.class);
                intent.putExtra("student_id", student.numericStudentId);
                intent.putExtra("student_code", student.id);
                intent.putExtra("student_name", student.name);
                startActivity(intent);
            });
        }
        builder.show();
    }

    private void bindExamPredictionViews(View root) {
        rvExamPredictionsList = root.findViewById(R.id.rvExamPredictionsList);
        if (rvExamPredictionsList != null && getContext() != null) {
            examPredictionAdapter = new ExamPredictionAdapter(requireContext(), examPredictionList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.exam_score_prediction_breakdown)
                        .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Course & Class: " + item.courseClass + "\n\n" +
                                "Prediction Details:\n" +
                                item.metrics + "\n" +
                                "Predicted Score: " + item.gradeBadge)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvExamPredictionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvExamPredictionsList.setAdapter(examPredictionAdapter);
        }
    }

    private void bindPredictionsFromApi(JsonArray arr) {
        examPredictionList.clear();
        double predSum = 0.0, confSum = 0.0;
        int cnt = 0;
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                String code = getStr(o, "student_code", "STU");
                String name = getStr(o, "student_name", "Student");
                String cCode = getStr(o, "course_code", "COURSE");
                double pred = getDouble(o, "predicted_score");
                int conf = getInt(o, "confidence");
                predSum += pred;
                confSum += conf;
                cnt++;
                String metrics = String.format(Locale.US, "Predicted Exam: %.1f%% • %d%% Confidence", pred, conf);
                String badge = String.format(Locale.US, "Pred: %.0f%%", pred);
                examPredictionList.add(new ExamPredictionAdapter.ExamPredictionItem(code, name, cCode, metrics, badge));
            }
        }
        if (cnt > 0) {
            double avgPred = predSum / cnt;
            double avgConf = confSum / cnt;
            if (tvPredExamAcademicAvg != null) tvPredExamAcademicAvg.setText(String.format(Locale.US, "%.1f%%", avgPred));
            if (tvPredExamPredictedAvg != null) tvPredExamPredictedAvg.setText(String.format(Locale.US, "%.1f%%", avgPred));
            if (tvPredOverviewAvgScore != null) tvPredOverviewAvgScore.setText(String.format(Locale.US, "%.1f%%", avgPred));
            if (tvPredExamExpectedGrade != null) {
                String g = avgPred >= 90 ? "A" : (avgPred >= 85 ? "B+" : (avgPred >= 80 ? "B" : (avgPred >= 70 ? "C" : "D")));
                tvPredExamExpectedGrade.setText("Grade: " + g);
            }
            if (tvPredExamConfidence != null) tvPredExamConfidence.setText(String.format(Locale.US, "%.0f%%", avgConf));
        } else {
            if (tvPredExamAcademicAvg != null) tvPredExamAcademicAvg.setText("--");
            if (tvPredExamPredictedAvg != null) tvPredExamPredictedAvg.setText("--");
            if (tvPredExamExpectedGrade != null) tvPredExamExpectedGrade.setText("--");
            if (tvPredExamConfidence != null) tvPredExamConfidence.setText("--");
        }
        if (examPredictionAdapter != null) {
            examPredictionAdapter.notifyDataSetChanged();
        }
    }

    private void bindRiskAnalysisViews(View root) {
        rvCourseRiskAnalysis = root.findViewById(R.id.rvCourseRiskAnalysis);
        if (rvCourseRiskAnalysis != null && getContext() != null) {
            riskAnalysisAdapter = new RiskAnalysisAdapter(requireContext(), riskAnalysisList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.course_risk_analysis_summary)
                        .setMessage(getString(R.string.course_code) + item.courseCode + "\n" +
                                "Course Name: " + item.courseName + "\n" +
                                item.departmentCohort + "\n\n" +
                                item.primaryRiskReason + "\n\n" +
                                "At-Risk Count: " + item.riskBadge)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvCourseRiskAnalysis.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvCourseRiskAnalysis.setAdapter(riskAnalysisAdapter);
        }
    }

    private void bindCourseRisksFromApi(JsonArray arr) {
        riskAnalysisList.clear();
        int highTotal = 0;
        double scoreSum = 0.0;
        int courseCnt = 0;
        if (arr != null) {
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                String code = getStr(o, "course_code", "COURSE");
                String name = getStr(o, "course_name", "Course");
                int high = getInt(o, "high_risk_count");
                int med = getInt(o, "medium_risk_count");
                int tot = getInt(o, "at_risk_total");
                double avg = getDouble(o, "average_score");
                highTotal += high;
                scoreSum += avg;
                courseCnt++;
                riskAnalysisList.add(new RiskAnalysisAdapter.RiskAnalysisItem(
                        code,
                        name,
                        String.format(Locale.US, "Average Score: %.1f%%", avg),
                        "High Risk: " + high + " • Medium Risk: " + med,
                        "At Risk (" + tot + ")"
                ));
            }
        }
        if (tvRiskSummaryHighCount != null) tvRiskSummaryHighCount.setText(highTotal + " Students");
        if (tvRiskSummaryFactor != null) {
            tvRiskSummaryFactor.setText(highTotal > 0 ? "High-Risk Alerts Active" : "None");
        }
        if (tvRiskSummaryIndex != null) {
            tvRiskSummaryIndex.setText(courseCnt > 0 ? String.format(Locale.US, "%.1f%% Avg", scoreSum / courseCnt) : "--");
        }
        if (tvRiskSummaryImprovement != null) tvRiskSummaryImprovement.setText("--");
        if (riskAnalysisAdapter != null) {
            riskAnalysisAdapter.notifyDataSetChanged();
        }
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
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

    private double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0.0;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
