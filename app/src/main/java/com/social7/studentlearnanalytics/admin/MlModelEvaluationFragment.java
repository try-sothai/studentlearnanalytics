package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MlModelEvaluationFragment extends Fragment {

    private TextView tvMae;
    private TextView tvRmse;
    private TextView tvR2;
    private LineChart lineChart;
    private LinearLayout layoutRecentEvaluations;
    private TextView tvEmptyState;

    public MlModelEvaluationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_ml_model_evaluation,
                container,
                false
        );

        View btnMenu = view.findViewById(R.id.btnMlMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (!isAdded()) return;
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).openDrawer();
                }
            });
        }

        tvMae = view.findViewById(R.id.tvMlMae);
        tvRmse = view.findViewById(R.id.tvMlRmse);
        tvR2 = view.findViewById(R.id.tvMlR2);
        lineChart = view.findViewById(R.id.lineChartPrediction);
        layoutRecentEvaluations = view.findViewById(R.id.layoutRecentEvaluations);
        tvEmptyState = view.findViewById(R.id.tvMlEmptyState);

        if (lineChart != null) {
            lineChart.setNoDataText("Loading model evaluation...");
        }

        fetchEvaluationData();

        return view;
    }

    private void fetchEvaluationData() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getAdminModelEvaluation().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject data = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;

                    boolean hasData = data.has("has_data") && !data.get("has_data").isJsonNull()
                            && data.get("has_data").getAsBoolean();

                    if (!hasData) {
                        String msg = root.has("message") && !root.get("message").isJsonNull()
                                ? root.get("message").getAsString()
                                : "No prediction evaluation records available yet.";
                        showEmptyState(msg);
                        return;
                    }

                    bindMetrics(data);
                    bindChart(data);
                    bindRecentEvaluations(data);
                } else {
                    showEmptyState("Unable to load model evaluation (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                showEmptyState("Network error loading model evaluation.");
            }
        });
    }

    private void bindMetrics(JsonObject data) {
        if (tvMae != null) {
            tvMae.setText(formatMetric(data, "mae"));
        }
        if (tvRmse != null) {
            tvRmse.setText(formatMetric(data, "rmse"));
        }
        if (tvR2 != null) {
            tvR2.setText(formatMetric(data, "r2"));
        }
    }

    private String formatMetric(JsonObject data, String key) {
        if (data == null || !data.has(key) || data.get(key).isJsonNull()) return "--";
        try {
            return String.format(Locale.US, "%.2f", data.get(key).getAsDouble());
        } catch (Exception e) {
            return "--";
        }
    }

    private void bindChart(JsonObject data) {
        if (lineChart == null || !isAdded() || getContext() == null) return;

        List<Float> actuals = extractFloatList(data, "actual_scores");
        List<Float> predicted = extractFloatList(data, "predicted_scores");
        List<String> labels = extractStringList(data, "evaluation_labels");

        if (actuals.isEmpty() || predicted.isEmpty()) {
            lineChart.clear();
            lineChart.setNoDataText("No prediction vs actual data available");
            lineChart.invalidate();
            return;
        }

        List<Entry> actualEntries = new ArrayList<>();
        for (int i = 0; i < actuals.size(); i++) {
            actualEntries.add(new Entry(i, actuals.get(i)));
        }

        List<Entry> predictedEntries = new ArrayList<>();
        for (int i = 0; i < predicted.size(); i++) {
            predictedEntries.add(new Entry(i, predicted.get(i)));
        }

        LineDataSet actualDataSet = new LineDataSet(actualEntries, getString(R.string.actual_score));
        actualDataSet.setColor(Color.parseColor("#4C35E9"));
        actualDataSet.setCircleColor(Color.parseColor("#4C35E9"));
        actualDataSet.setLineWidth(2.5f);
        actualDataSet.setCircleRadius(4f);
        actualDataSet.setDrawValues(false);

        LineDataSet predictedDataSet = new LineDataSet(predictedEntries, getString(R.string.predicted_score));
        predictedDataSet.setColor(Color.parseColor("#10B981"));
        predictedDataSet.setCircleColor(Color.parseColor("#10B981"));
        predictedDataSet.setLineWidth(2.5f);
        predictedDataSet.setCircleRadius(4f);
        predictedDataSet.setDrawValues(false);
        predictedDataSet.enableDashedLine(10f, 5f, 0f);

        LineData lineData = new LineData(actualDataSet, predictedDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        if (!labels.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
        }
        xAxis.setGranularity(1f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        lineChart.animateX(600);
        ChartFontHelper.applyKhmerFont(lineChart.getContext(), lineChart);
        lineChart.invalidate();
    }

    private void bindRecentEvaluations(JsonObject data) {
        if (layoutRecentEvaluations == null || getContext() == null) return;
        layoutRecentEvaluations.removeAllViews();

        JsonArray arr = data.has("recent_evaluations") && data.get("recent_evaluations").isJsonArray()
                ? data.getAsJsonArray("recent_evaluations") : null;

        if (arr == null || arr.size() == 0) {
            if (tvEmptyState != null) {
                tvEmptyState.setText("No recent evaluations found.");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        int dp8 = Math.round(8 * getResources().getDisplayMetrics().density);
        int dp14 = Math.round(14 * getResources().getDisplayMetrics().density);

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject item = el.getAsJsonObject();
            String date = item.has("date") && !item.get("date").isJsonNull() ? item.get("date").getAsString() : "-";
            String title = item.has("title") && !item.get("title").isJsonNull() ? item.get("title").getAsString() : "Evaluation";
            String mae = formatMetric(item, "mae");
            String rmse = formatMetric(item, "rmse");

            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp8;
            card.setLayoutParams(lp);
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_white));
            card.setRadius(12 * getResources().getDisplayMetrics().density);
            card.setCardElevation(1 * getResources().getDisplayMetrics().density);
            card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.divider_color));
            card.setStrokeWidth(Math.round(1 * getResources().getDisplayMetrics().density));

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp14, dp14, dp14, dp14);

            LinearLayout left = new LinearLayout(requireContext());
            left.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            left.setLayoutParams(leftLp);

            TextView tvDate = new TextView(requireContext());
            tvDate.setText(date);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvDate.setTextSize(14f);
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(requireContext());
            tvSub.setText(title);
            tvSub.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvSub.setTextSize(12f);

            left.addView(tvDate);
            left.addView(tvSub);

            TextView tvMetrics = new TextView(requireContext());
            tvMetrics.setText("MAE: " + mae + "  •  RMSE: " + rmse);
            tvMetrics.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            tvMetrics.setTextSize(12f);
            tvMetrics.setTypeface(null, android.graphics.Typeface.BOLD);

            row.addView(left);
            row.addView(tvMetrics);
            card.addView(row);
            layoutRecentEvaluations.addView(card);
        }
    }

    private void showEmptyState(String message) {
        if (tvMae != null) tvMae.setText("--");
        if (tvRmse != null) tvRmse.setText("--");
        if (tvR2 != null) tvR2.setText("--");
        if (lineChart != null) {
            lineChart.clear();
            lineChart.setNoDataText(message);
            lineChart.invalidate();
        }
        if (layoutRecentEvaluations != null) {
            layoutRecentEvaluations.removeAllViews();
        }
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private List<Float> extractFloatList(JsonObject obj, String key) {
        List<Float> list = new ArrayList<>();
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) return list;
        for (JsonElement el : obj.getAsJsonArray(key)) {
            if (el != null && !el.isJsonNull()) {
                try {
                    list.add(el.getAsFloat());
                } catch (Exception ignored) {
                }
            }
        }
        return list;
    }

    private List<String> extractStringList(JsonObject obj, String key) {
        List<String> list = new ArrayList<>();
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) return list;
        for (JsonElement el : obj.getAsJsonArray(key)) {
            if (el != null && !el.isJsonNull()) {
                list.add(el.getAsString());
            }
        }
        return list;
    }
}
