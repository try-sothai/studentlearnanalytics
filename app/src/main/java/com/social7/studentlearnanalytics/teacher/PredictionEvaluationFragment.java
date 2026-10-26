package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class PredictionEvaluationFragment extends Fragment {

    public PredictionEvaluationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_prediction_evaluation,
                container,
                false
        );

        ImageView btnMenu = view.findViewById(R.id.btnTeacherMenuEval);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openDrawer();
                }
            });
        }

        LineChart chart = view.findViewById(R.id.lineChartEval);
        if (chart != null) {
            setupEvalChart(chart);
        }

        return view;
    }

    private void setupEvalChart(LineChart chart) {
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

        LineDataSet actualSet = new LineDataSet(actualEntries, "Actual");
        actualSet.setColor(Color.parseColor("#5F34F2"));
        actualSet.setLineWidth(2.5f);
        actualSet.setCircleColor(Color.parseColor("#5F34F2"));

        LineDataSet predSet = new LineDataSet(predEntries, "Predicted");
        predSet.setColor(Color.parseColor("#10B981"));
        predSet.setLineWidth(2f);
        predSet.enableDashedLine(10f, 5f, 0f);
        predSet.setCircleColor(Color.parseColor("#10B981"));

        LineData lineData = new LineData(actualSet, predSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"S1", "S2", "S3", "S4", "S5"}));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.animateX(800);
        chart.invalidate();
    }
}
