package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class MlModelEvaluationFragment extends Fragment {

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

        LineChart chart = view.findViewById(R.id.lineChartPrediction);
        setupPredictionChart(chart);

        View btnBack = view.findViewById(R.id.btnBackMl);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        return view;
    }

    private void setupPredictionChart(LineChart chart) {
        if (chart == null) return;

        // Actual scores line
        List<Entry> actualEntries = new ArrayList<>();
        actualEntries.add(new Entry(0, 75f));
        actualEntries.add(new Entry(1, 82f));
        actualEntries.add(new Entry(2, 68f));
        actualEntries.add(new Entry(3, 90f));
        actualEntries.add(new Entry(4, 85f));
        actualEntries.add(new Entry(5, 78f));

        LineDataSet actualDataSet = new LineDataSet(actualEntries, "Actual Score");
        actualDataSet.setColor(Color.parseColor("#4C35E9"));
        actualDataSet.setCircleColor(Color.parseColor("#4C35E9"));
        actualDataSet.setLineWidth(2.5f);
        actualDataSet.setCircleRadius(4f);
        actualDataSet.setDrawValues(false);

        // Predicted scores line
        List<Entry> predictedEntries = new ArrayList<>();
        predictedEntries.add(new Entry(0, 78f));
        predictedEntries.add(new Entry(1, 80f));
        predictedEntries.add(new Entry(2, 72f));
        predictedEntries.add(new Entry(3, 87f));
        predictedEntries.add(new Entry(4, 83f));
        predictedEntries.add(new Entry(5, 81f));

        LineDataSet predictedDataSet = new LineDataSet(predictedEntries, "Predicted Score");
        predictedDataSet.setColor(Color.parseColor("#10B981"));
        predictedDataSet.setCircleColor(Color.parseColor("#10B981"));
        predictedDataSet.setLineWidth(2.5f);
        predictedDataSet.setCircleRadius(4f);
        predictedDataSet.setDrawValues(false);
        predictedDataSet.enableDashedLine(10f, 5f, 0f);

        LineData lineData = new LineData(actualDataSet, predictedDataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Stu 1", "Stu 2", "Stu 3", "Stu 4", "Stu 5", "Stu 6"}));
        xAxis.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F0F0F0"));

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        chart.animateX(1000);
        chart.invalidate();
    }
}
