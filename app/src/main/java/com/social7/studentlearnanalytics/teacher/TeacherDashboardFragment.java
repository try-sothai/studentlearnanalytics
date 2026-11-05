package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_TEACHER_DASH";

    private SessionManager sessionManager;

    public TeacherDashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_dashboard,
                container,
                false
        );

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherDashboard),
                    "Teacher Dashboard",
                    "Overview & analytics"
            );
        }

        View cardTotalStudents = view.findViewById(R.id.cardTotalStudents);
        if (cardTotalStudents != null) {
            cardTotalStudents.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new StudentListFragment());
                }
            });
        }

        View cardMyCourses = view.findViewById(R.id.cardMyCourses);
        if (cardMyCourses != null) {
            cardMyCourses.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new CourseManagementFragment());
                }
            });
        }

        View cardAtRisk = view.findViewById(R.id.cardAtRisk);
        if (cardAtRisk != null) {
            cardAtRisk.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new PredictionRiskFragment());
                }
            });
        }

        View cardPendingScores = view.findViewById(R.id.cardPendingScores);
        if (cardPendingScores != null) {
            cardPendingScores.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new ManageScoresFragment());
                }
            });
        }

        LineChart lineChart = view.findViewById(R.id.lineChartPerformance);
        fetchTeacherDashboard(lineChart);

        return view;
    }

    private void fetchTeacherDashboard(LineChart chart) {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherDashboard().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        setupLineChart(chart);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing teacher dashboard", e);
                        setupLineChart(chart);
                    }
                } else {
                    Log.d(TAG, "Teacher Dashboard HTTP code: " + response.code());
                    setupLineChart(chart);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.d(TAG, "Teacher Dashboard API failure: " + t.getMessage());
                setupLineChart(chart);
            }
        });
    }

    private void setupLineChart(LineChart chart) {
        if (chart == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 72f));
        entries.add(new Entry(1, 78f));
        entries.add(new Entry(2, 81f));
        entries.add(new Entry(3, 75f));
        entries.add(new Entry(4, 84f));
        entries.add(new Entry(5, 88f));

        LineDataSet dataSet = new LineDataSet(entries, "Class Average");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#5F34F2"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#5F34F2"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#1D1F2A"));

        dataSet.setDrawFilled(true);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#405F34F2"), Color.parseColor("#005F34F2")}
        );
        dataSet.setFillDrawable(gradientDrawable);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"}));
        xAxis.setTextColor(Color.parseColor("#7A7D8F"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F0F0"));
        leftAxis.setTextColor(Color.parseColor("#7A7D8F"));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.invalidate();
    }
}
