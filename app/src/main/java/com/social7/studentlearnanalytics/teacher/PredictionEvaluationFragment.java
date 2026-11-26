package com.social7.studentlearnanalytics.teacher;

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

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionEvaluationFragment extends Fragment {

    private TextView tabEvalOverview, tabEvalPredVsActual, tabEvalModelComp, tabEvalHistory, tabEvalInsights;
    private View secEvalOverviewContainer, secEvalPredVsActualContainer, secEvalModelCompContainer, secEvalHistoryContainer, secEvalInsightsContainer;

    private LineChart lineChartEval;
    private TextView tvEvalMae, tvEvalRmse, tvEvalR2, tvEvalSamples, tvEvalModelVersion, tvEvalStatus;

    private RecyclerView rvPredVsActualRoster;
    private PredVsActualAdapter predVsActualAdapter;
    private final List<PredVsActualAdapter.PredVsActualItem> predVsActualList = new ArrayList<>();

    private RecyclerView rvModelComparisonList;
    private ModelComparisonAdapter modelComparisonAdapter;
    private final List<ModelComparisonAdapter.ModelComparisonItem> modelComparisonList = new ArrayList<>();

    private RecyclerView rvEvaluationHistoryList;
    private EvaluationHistoryAdapter evaluationHistoryAdapter;
    private final List<EvaluationHistoryAdapter.EvaluationHistoryItem> evaluationHistoryList = new ArrayList<>();
    private EditText edtSearchEvalHistory;

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

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.prediction_evaluation), "ML model accuracy, MAE, RMSE & R² metrics monitoring"
            );
        }

        tabEvalOverview = view.findViewById(R.id.tabEvalOverview);
        tabEvalPredVsActual = view.findViewById(R.id.tabEvalPredVsActual);
        tabEvalModelComp = view.findViewById(R.id.tabEvalModelComp);
        tabEvalHistory = view.findViewById(R.id.tabEvalHistory);
        tabEvalInsights = view.findViewById(R.id.tabEvalInsights);

        secEvalOverviewContainer = view.findViewById(R.id.secEvalOverviewContainer);
        secEvalPredVsActualContainer = view.findViewById(R.id.secEvalPredVsActualContainer);
        secEvalModelCompContainer = view.findViewById(R.id.secEvalModelCompContainer);
        secEvalHistoryContainer = view.findViewById(R.id.secEvalHistoryContainer);
        secEvalInsightsContainer = view.findViewById(R.id.secEvalInsightsContainer);

        bindOverviewViews(view);
        bindPredVsActualViews(view);
        bindModelCompViews(view);
        bindHistoryViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchEvaluationData();

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

    private void fetchEvaluationData() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherRiskPredictions().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                predVsActualList.clear();
                modelComparisonList.clear();
                evaluationHistoryList.clear();

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    boolean hasPreds = data.has("has_predictions") && !data.get("has_predictions").isJsonNull()
                            && data.get("has_predictions").getAsBoolean();

                    if (!hasPreds) {
                        if (tvEvalSamples != null) tvEvalSamples.setText("0 Rows");
                        if (tvEvalStatus != null) tvEvalStatus.setText("No Data");
                        if (lineChartEval != null) {
                            lineChartEval.clear();
                            lineChartEval.setNoDataText("No prediction evaluation records available yet");
                            lineChartEval.invalidate();
                        }
                    } else if (data.has("predictions") && data.get("predictions").isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray("predictions");
                        if (tvEvalSamples != null) tvEvalSamples.setText(arr.size() + " Rows");
                        if (tvEvalStatus != null) tvEvalStatus.setText(arr.size() > 0 ? "Active" : "No Data");
                        for (JsonElement el : arr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject o = el.getAsJsonObject();
                            String code = o.has("student_code") && !o.get("student_code").isJsonNull() ? o.get("student_code").getAsString() : "STU";
                            String name = o.has("student_name") && !o.get("student_name").isJsonNull() ? o.get("student_name").getAsString() : "Student";
                            String cCode = o.has("course_code") && !o.get("course_code").isJsonNull() ? o.get("course_code").getAsString() : "COURSE";
                            double pred = o.has("predicted_score") && !o.get("predicted_score").isJsonNull() ? o.get("predicted_score").getAsDouble() : 0.0;
                            predVsActualList.add(new PredVsActualAdapter.PredVsActualItem(
                                    code, name, cCode,
                                    String.format(Locale.US, "Predicted Score: %.1f%%", pred),
                                    "Evaluated"
                            ));
                        }
                    }
                } else {
                    if (lineChartEval != null) {
                        lineChartEval.clear();
                        lineChartEval.setNoDataText("Unable to load prediction evaluation");
                        lineChartEval.invalidate();
                    }
                }

                if (predVsActualAdapter != null) predVsActualAdapter.notifyDataSetChanged();
                if (modelComparisonAdapter != null) modelComparisonAdapter.notifyDataSetChanged();
                if (evaluationHistoryAdapter != null) evaluationHistoryAdapter.filter("");
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                if (lineChartEval != null) {
                    lineChartEval.clear();
                    lineChartEval.setNoDataText("Network error loading prediction evaluation");
                    lineChartEval.invalidate();
                }
                Toast.makeText(getContext(), "Network error loading evaluation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOverviewViews(View root) {
        lineChartEval = root.findViewById(R.id.lineChartEval);
        tvEvalMae = root.findViewById(R.id.tvEvalMae);
        tvEvalRmse = root.findViewById(R.id.tvEvalRmse);
        tvEvalR2 = root.findViewById(R.id.tvEvalR2);
        tvEvalSamples = root.findViewById(R.id.tvEvalSamples);
        tvEvalModelVersion = root.findViewById(R.id.tvEvalModelVersion);
        tvEvalStatus = root.findViewById(R.id.tvEvalStatus);
        if (lineChartEval != null) {
            lineChartEval.setNoDataText("No prediction evaluation records available yet");
        }
    }

    private void bindPredVsActualViews(View root) {
        rvPredVsActualRoster = root.findViewById(R.id.rvPredVsActualRoster);
        if (rvPredVsActualRoster != null && getContext() != null) {
            predVsActualAdapter = new PredVsActualAdapter(requireContext(), predVsActualList, item -> {});
            rvPredVsActualRoster.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvPredVsActualRoster.setAdapter(predVsActualAdapter);
        }
    }

    private void bindModelCompViews(View root) {
        rvModelComparisonList = root.findViewById(R.id.rvModelComparisonList);
        if (rvModelComparisonList != null && getContext() != null) {
            modelComparisonAdapter = new ModelComparisonAdapter(requireContext(), modelComparisonList, item -> {});
            rvModelComparisonList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvModelComparisonList.setAdapter(modelComparisonAdapter);
        }
    }

    private void bindHistoryViews(View root) {
        rvEvaluationHistoryList = root.findViewById(R.id.rvEvaluationHistoryList);
        edtSearchEvalHistory = root.findViewById(R.id.edtSearchEvalHistory);

        if (rvEvaluationHistoryList != null && getContext() != null) {
            evaluationHistoryAdapter = new EvaluationHistoryAdapter(requireContext(), evaluationHistoryList, item -> {});
            rvEvaluationHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvEvaluationHistoryList.setAdapter(evaluationHistoryAdapter);
        }

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
}
