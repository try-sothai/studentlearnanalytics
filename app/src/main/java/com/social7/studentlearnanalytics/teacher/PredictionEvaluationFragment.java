package com.social7.studentlearnanalytics.teacher;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class PredictionEvaluationFragment extends Fragment {

    // Sub-Tab Buttons
    private TextView tabEvalOverview, tabEvalPredVsActual, tabEvalModelComp, tabEvalHistory, tabEvalInsights;

    // Sub-Tab Containers
    private View secEvalOverviewContainer, secEvalPredVsActualContainer, secEvalModelCompContainer, secEvalHistoryContainer, secEvalInsightsContainer;

    // Sub-Tab 0: Overview
    private LineChart lineChartEval;

    // Sub-Tab 1: Pred vs Actual
    private RecyclerView rvPredVsActualRoster;
    private PredVsActualAdapter predVsActualAdapter;
    private final List<PredVsActualAdapter.PredVsActualItem> predVsActualList = new ArrayList<>();

    // Sub-Tab 2: Model Comparison
    private RecyclerView rvModelComparisonList;
    private ModelComparisonAdapter modelComparisonAdapter;
    private final List<ModelComparisonAdapter.ModelComparisonItem> modelComparisonList = new ArrayList<>();

    // Sub-Tab 3: Evaluation History
    private RecyclerView rvEvaluationHistoryList;
    private EvaluationHistoryAdapter evaluationHistoryAdapter;
    private final List<EvaluationHistoryAdapter.EvaluationHistoryItem> evaluationHistoryList = new ArrayList<>();
    private EditText edtSearchEvalHistory;

    // Selected Sub-Tab Flag (0=Overview, 1=PredVsActual, 2=ModelComp, 3=History, 4=Insights)
    private int selectedSubTab = 0;

    public PredictionEvaluationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_prediction_evaluation, container, false);

        // Header Integration
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.prediction_evaluation), "ML model accuracy, MAE, RMSE & R² metrics monitoring"
            );
        }

        // Sub-Tab Buttons
        tabEvalOverview = view.findViewById(R.id.tabEvalOverview);
        tabEvalPredVsActual = view.findViewById(R.id.tabEvalPredVsActual);
        tabEvalModelComp = view.findViewById(R.id.tabEvalModelComp);
        tabEvalHistory = view.findViewById(R.id.tabEvalHistory);
        tabEvalInsights = view.findViewById(R.id.tabEvalInsights);

        // Containers
        secEvalOverviewContainer = view.findViewById(R.id.secEvalOverviewContainer);
        secEvalPredVsActualContainer = view.findViewById(R.id.secEvalPredVsActualContainer);
        secEvalModelCompContainer = view.findViewById(R.id.secEvalModelCompContainer);
        secEvalHistoryContainer = view.findViewById(R.id.secEvalHistoryContainer);
        secEvalInsightsContainer = view.findViewById(R.id.secEvalInsightsContainer);

        // Bind Sub-views
        bindOverviewViews(view);
        bindPredVsActualViews(view);
        bindModelCompViews(view);
        bindHistoryViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);

        return view;
    }

    private void setupSubTabListeners() {
        if (tabEvalOverview != null) tabEvalOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabEvalPredVsActual != null) tabEvalPredVsActual.setOnClickListener(v -> selectSubTab(1));
        if (tabEvalModelComp != null) tabEvalModelComp.setOnClickListener(v -> selectSubTab(2));
        if (tabEvalHistory != null) tabEvalHistory.setOnClickListener(v -> selectSubTab(3));
        if (tabEvalInsights != null) tabEvalInsights.setOnClickListener(v -> selectSubTab(4));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secEvalOverviewContainer != null) secEvalOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secEvalPredVsActualContainer != null) secEvalPredVsActualContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secEvalModelCompContainer != null) secEvalModelCompContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secEvalHistoryContainer != null) secEvalHistoryContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secEvalInsightsContainer != null) secEvalInsightsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);

        if (subIndex == 0) {
            setupOverviewChart();
        } else if (subIndex == 1) {
            setupPredVsActualList();
        } else if (subIndex == 2) {
            setupModelCompList();
        } else if (subIndex == 3) {
            setupHistoryList();
        }
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabEvalOverview, tabEvalPredVsActual, tabEvalModelComp, tabEvalHistory, tabEvalInsights};
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

    // ==========================================
    // 0. OVERVIEW SUB-TAB
    // ==========================================
    private void bindOverviewViews(View root) {
        lineChartEval = root.findViewById(R.id.lineChartEval);
    }

    private void setupOverviewChart() {
        if (lineChartEval == null || lineChartEval.getData() != null) return;

        List<Entry> actualEntries = new ArrayList<>();
        actualEntries.add(new Entry(0, 70f));
        actualEntries.add(new Entry(1, 74f));
        actualEntries.add(new Entry(2, 78f));
        actualEntries.add(new Entry(3, 82f));
        actualEntries.add(new Entry(4, 88f));

        List<Entry> predEntries = new ArrayList<>();
        predEntries.add(new Entry(0, 68f));
        predEntries.add(new Entry(1, 76f));
        predEntries.add(new Entry(2, 80f));
        predEntries.add(new Entry(3, 80f));
        predEntries.add(new Entry(4, 86f));

        LineDataSet actualSet = new LineDataSet(actualEntries, getString(R.string.actual_score));
        actualSet.setColor(Color.parseColor("#3155F5"));
        actualSet.setLineWidth(2.5f);
        actualSet.setCircleColor(Color.parseColor("#3155F5"));

        LineDataSet predSet = new LineDataSet(predEntries, getString(R.string.predicted_score));
        predSet.setColor(Color.parseColor("#10B981"));
        predSet.setLineWidth(2f);
        predSet.enableDashedLine(10f, 5f, 0f);
        predSet.setCircleColor(Color.parseColor("#10B981"));

        LineData lineData = new LineData(actualSet, predSet);
        lineChartEval.setData(lineData);

        XAxis xAxis = lineChartEval.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"S1", "S2", "S3", "S4", "S5"}));

        lineChartEval.getAxisRight().setEnabled(false);
        lineChartEval.getDescription().setEnabled(false);
        lineChartEval.animateX(800);
        ChartFontHelper.applyKhmerFont(lineChartEval.getContext(), lineChartEval);
        lineChartEval.invalidate();
    }

    // ==========================================
    // 1. PREDICTION VS ACTUAL SUB-TAB
    // ==========================================
    private void bindPredVsActualViews(View root) {
        rvPredVsActualRoster = root.findViewById(R.id.rvPredVsActualRoster);
    }

    private void setupPredVsActualList() {
        if (rvPredVsActualRoster == null || predVsActualAdapter != null) return;

        predVsActualList.clear();
        predVsActualList.add(new PredVsActualAdapter.PredVsActualItem("STU23001", "Sophea", "CS101 - Intro to CS • BSIT 4-A", "Actual: 88.0% • Predicted: 86.5% • Absolute Error: 1.5 pts", "Low Residual (1.5 pts)"));
        predVsActualList.add(new PredVsActualAdapter.PredVsActualItem("STU23002", "Dara", "IT402 - Web Systems • BSIT 4-B", "Actual: 58.0% • Predicted: 54.0% • Absolute Error: 4.0 pts", "Moderate Residual (4.0 pts)"));
        predVsActualList.add(new PredVsActualAdapter.PredVsActualItem("STU23003", "Rathana", "CS204 - Algorithms • BSCS 3-A", "Actual: 62.0% • Predicted: 65.0% • Absolute Error: 3.0 pts", "Low Residual (3.0 pts)"));
        predVsActualList.add(new PredVsActualAdapter.PredVsActualItem("STU23004", "Sokha", "CS101 - Intro to CS • BSCS 3-B", "Actual: 82.0% • Predicted: 84.5% • Absolute Error: 2.5 pts", "Low Residual (2.5 pts)"));

        predVsActualAdapter = new PredVsActualAdapter(requireContext(), predVsActualList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Prediction vs Actual Residual Detail")
                    .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                            "Student Name: " + item.studentName + "\n" +
                            "Course & Class: " + item.courseClass + "\n\n" +
                            item.comparisonDetail + "\n\n" +
                            "Model Error Residual: " + item.errorStatusBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvPredVsActualRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPredVsActualRoster.setAdapter(predVsActualAdapter);
    }

    // ==========================================
    // 2. MODEL COMPARISON SUB-TAB
    // ==========================================
    private void bindModelCompViews(View root) {
        rvModelComparisonList = root.findViewById(R.id.rvModelComparisonList);
    }

    private void setupModelCompList() {
        if (rvModelComparisonList == null || modelComparisonAdapter != null) return;

        modelComparisonList.clear();
        modelComparisonList.add(new ModelComparisonAdapter.ModelComparisonItem("Random Forest Regressor", "Version: SLAS-ML v2.4 • 1,240 Test Samples", "MAE: 2.12 pts • RMSE: 2.85 pts • R²: 0.91 (91% Var) • Speed: 1.2s", "Best Performer"));
        modelComparisonList.add(new ModelComparisonAdapter.ModelComparisonItem("Gradient Boosting Regressor", "Version: SLAS-ML v2.3 • 1,240 Test Samples", "MAE: 2.35 pts • RMSE: 3.05 pts • R²: 0.89 (89% Var) • Speed: 1.8s", "Optimal"));
        modelComparisonList.add(new ModelComparisonAdapter.ModelComparisonItem("Decision Tree Regressor", "Version: SLAS-ML v2.1 • 1,240 Test Samples", "MAE: 3.45 pts • RMSE: 4.12 pts • R²: 0.82 (82% Var) • Speed: 0.8s", "Good"));
        modelComparisonList.add(new ModelComparisonAdapter.ModelComparisonItem("Linear Regression Model", "Version: SLAS-ML v1.8 • 1,240 Test Samples", "MAE: 4.10 pts • RMSE: 5.20 pts • R²: 0.76 (76% Var) • Speed: 0.4s", "Baseline"));

        modelComparisonAdapter = new ModelComparisonAdapter(requireContext(), modelComparisonList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("ML Model Architecture Comparison")
                    .setMessage("Model Architecture: " + item.modelName + "\n" +
                            item.modelParams + "\n\n" +
                            "Trained Performance Metrics:\n" +
                            item.modelMetrics + "\n\n" +
                            "Evaluation Rating: " + item.bestModelBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvModelComparisonList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvModelComparisonList.setAdapter(modelComparisonAdapter);
    }

    // ==========================================
    // 3. EVALUATION HISTORY SUB-TAB
    // ==========================================
    private void bindHistoryViews(View root) {
        rvEvaluationHistoryList = root.findViewById(R.id.rvEvaluationHistoryList);
        edtSearchEvalHistory = root.findViewById(R.id.edtSearchEvalHistory);

        if (edtSearchEvalHistory != null) {
            edtSearchEvalHistory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (evaluationHistoryAdapter != null) {
                        evaluationHistoryAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupHistoryList() {
        if (rvEvaluationHistoryList == null || evaluationHistoryAdapter != null) return;

        evaluationHistoryList.clear();
        evaluationHistoryList.add(new EvaluationHistoryAdapter.EvaluationHistoryItem("May 10, 2026", "Random Forest v2.4 (CS101)", "CS101 Data Structures • 1,240 Test Records", "MAE: 2.45 pts • RMSE: 3.12 pts • R²: 0.88 (88% Variance Explained)", "Optimal"));
        evaluationHistoryList.add(new EvaluationHistoryAdapter.EvaluationHistoryItem("Apr 28, 2026", "Random Forest v2.4 (IT402)", "IT402 Web Systems • 980 Test Records", "MAE: 2.60 pts • RMSE: 3.25 pts • R²: 0.86 (86% Variance Explained)", "Optimal"));
        evaluationHistoryList.add(new EvaluationHistoryAdapter.EvaluationHistoryItem("Apr 15, 2026", "Gradient Boosting v2.3 (CS204)", "CS204 Algorithms • 1,100 Test Records", "MAE: 2.80 pts • RMSE: 3.40 pts • R²: 0.85 (85% Variance Explained)", "Optimal"));

        evaluationHistoryAdapter = new EvaluationHistoryAdapter(requireContext(), evaluationHistoryList, item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.model_evaluation_session_report)
                    .setMessage(getString(R.string.evaluation_date) + item.date + "\n" +
                            "Model Version: " + item.modelVersion + "\n" +
                            "Dataset: " + item.courseDataset + "\n\n" +
                            "ML Model Evaluation Metrics:\n" +
                            item.metricsDetail + "\n\n" +
                            "Evaluation Status: " + item.statusBadge)
                    .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        rvEvaluationHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvaluationHistoryList.setAdapter(evaluationHistoryAdapter);
    }
}
