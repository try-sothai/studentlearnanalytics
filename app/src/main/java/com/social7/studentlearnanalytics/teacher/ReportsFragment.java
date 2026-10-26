package com.social7.studentlearnanalytics.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.teacher.analytics.CourseAnalyticsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private CourseAnalyticsAdapter adapter;
    private String selectedCategory = "All";
    private String searchQuery = "";

    private TextView tvSelectedSort;
    private MaterialButton btnFilterAll;
    private MaterialButton btnFilterGood;
    private MaterialButton btnFilterMedium;
    private MaterialButton btnFilterAtRisk;
    private LinearLayout btnFilterDropdown;

    public ReportsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_teacher_reports,
                container,
                false
        );

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterGood = view.findViewById(R.id.btnFilterGood);
        btnFilterMedium = view.findViewById(R.id.btnFilterMedium);
        btnFilterAtRisk = view.findViewById(R.id.btnFilterAtRisk);
        btnFilterDropdown = view.findViewById(R.id.btnFilterDropdown);

        if (btnFilterAll != null) btnFilterAll.setOnClickListener(v -> setFilterCategory("All"));
        if (btnFilterGood != null) btnFilterGood.setOnClickListener(v -> setFilterCategory("Good"));
        if (btnFilterMedium != null) btnFilterMedium.setOnClickListener(v -> setFilterCategory("Medium"));
        if (btnFilterAtRisk != null) btnFilterAtRisk.setOnClickListener(v -> setFilterCategory("At Risk"));
        if (btnFilterDropdown != null) btnFilterDropdown.setOnClickListener(this::showFilterDropdown);

        tvSelectedSort = view.findViewById(R.id.tvSelectedSort);
        LinearLayout btnSortByDropdown = view.findViewById(R.id.btnSortByDropdown);
        if (btnSortByDropdown != null) {
            btnSortByDropdown.setOnClickListener(this::showSortDropdown);
        }

        EditText edtSearch = view.findViewById(R.id.edtSearchAnalytics);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (adapter != null) {
                        adapter.filter(searchQuery, selectedCategory);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        BarChart barChart = view.findViewById(R.id.barChartAvgByCourse);
        if (barChart != null) {
            setupBarChart(barChart);
        }

        PieChart pieChart = view.findViewById(R.id.pieChartStatusDoughnut);
        if (pieChart != null) {
            setupPieChart(pieChart);
        }

        RecyclerView rvList = view.findViewById(R.id.rvCourseAnalyticsList);
        if (rvList != null) {
            rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
            List<CourseAnalyticsAdapter.CourseAnalyticsItem> items = getSampleCourseAnalytics();
            adapter = new CourseAnalyticsAdapter(requireContext(), items, item -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new ClassAnalyticsFragment());
                }
            });
            rvList.setAdapter(adapter);
        }

        return view;
    }

    private void showFilterDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "All Performance Levels");
        popup.getMenu().add(0, 2, 1, "Good Performance");
        popup.getMenu().add(0, 3, 2, "Medium Performance");
        popup.getMenu().add(0, 4, 3, "At Risk");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) setFilterCategory("All");
            else if (item.getItemId() == 2) setFilterCategory("Good");
            else if (item.getItemId() == 3) setFilterCategory("Medium");
            else if (item.getItemId() == 4) setFilterCategory("At Risk");
            return true;
        });
        popup.show();
    }

    private void showSortDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Sort by Course Code");
        popup.getMenu().add(0, 2, 1, "Sort by Course Name");
        popup.getMenu().add(0, 3, 2, "Sort by Average Score");
        popup.getMenu().add(0, 4, 3, "Sort by Risk Level");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1 && tvSelectedSort != null) tvSelectedSort.setText("Sort: Code");
            else if (item.getItemId() == 2 && tvSelectedSort != null) tvSelectedSort.setText("Sort: Name");
            else if (item.getItemId() == 3 && tvSelectedSort != null) tvSelectedSort.setText("Sort: Score");
            else if (item.getItemId() == 4 && tvSelectedSort != null) tvSelectedSort.setText("Sort: Risk");
            return true;
        });
        popup.show();
    }

    private void setFilterCategory(String category) {
        this.selectedCategory = category;
        updateFilterButtonStyles();
        if (adapter != null) {
            adapter.filter(searchQuery, selectedCategory);
        }
    }

    private void updateFilterButtonStyles() {
        int primaryBlue = Color.parseColor("#2563FF");
        int white = Color.WHITE;

        if (btnFilterAll != null) {
            if ("All".equalsIgnoreCase(selectedCategory)) {
                btnFilterAll.setBackgroundColor(primaryBlue);
                btnFilterAll.setTextColor(white);
            } else {
                btnFilterAll.setBackgroundColor(Color.parseColor("#EAF0FF"));
                btnFilterAll.setTextColor(primaryBlue);
            }
        }

        if (btnFilterGood != null) {
            if ("Good".equalsIgnoreCase(selectedCategory)) {
                btnFilterGood.setBackgroundColor(primaryBlue);
                btnFilterGood.setTextColor(white);
            } else {
                btnFilterGood.setBackgroundColor(Color.parseColor("#E8F8F1"));
                btnFilterGood.setTextColor(Color.parseColor("#16B879"));
            }
        }

        if (btnFilterMedium != null) {
            if ("Medium".equalsIgnoreCase(selectedCategory)) {
                btnFilterMedium.setBackgroundColor(primaryBlue);
                btnFilterMedium.setTextColor(white);
            } else {
                btnFilterMedium.setBackgroundColor(Color.parseColor("#FFF5E6"));
                btnFilterMedium.setTextColor(Color.parseColor("#FF9B16"));
            }
        }

        if (btnFilterAtRisk != null) {
            if ("At Risk".equalsIgnoreCase(selectedCategory)) {
                btnFilterAtRisk.setBackgroundColor(primaryBlue);
                btnFilterAtRisk.setTextColor(white);
            } else {
                btnFilterAtRisk.setBackgroundColor(Color.parseColor("#FFEBEF"));
                btnFilterAtRisk.setTextColor(Color.parseColor("#FF4055"));
            }
        }
    }

    private void setupBarChart(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 82f));
        entries.add(new BarEntry(1, 76f));
        entries.add(new BarEntry(2, 88f));
        entries.add(new BarEntry(3, 71f));

        BarDataSet dataSet = new BarDataSet(entries, "Average Score");
        dataSet.setColors(new int[]{
                Color.parseColor("#2563FF"), // CS101 - Blue
                Color.parseColor("#16C784"), // CS102 - Green
                Color.parseColor("#FF9B16"), // CS201 - Orange
                Color.parseColor("#873BFF")  // CS301 - Purple
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
        barData.setBarWidth(0.45f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"CS101", "CS102", "CS201", "CS301"}));
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
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(2f, "Good (50%)"));
        entries.add(new PieEntry(1f, "Medium (25%)"));
        entries.add(new PieEntry(1f, "At Risk (25%)"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#16C784"), // Good - Green
                Color.parseColor("#FF9B16"), // Medium - Orange
                Color.parseColor("#FF4055")  // At Risk - Red
        });
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));

        chart.setUsePercentValues(true);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setHoleColor(Color.WHITE);

        chart.setCenterText("4 Courses");
        chart.setCenterTextColor(Color.parseColor("#10165C"));
        chart.setCenterTextSize(14f);

        chart.getDescription().setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setTextColor(Color.parseColor("#10165C"));
        legend.setTextSize(11f);

        chart.setData(pieData);
        chart.animateXY(800, 800);
        chart.invalidate();
    }

    private List<CourseAnalyticsAdapter.CourseAnalyticsItem> getSampleCourseAnalytics() {
        List<CourseAnalyticsAdapter.CourseAnalyticsItem> list = new ArrayList<>();

        list.add(new CourseAnalyticsAdapter.CourseAnalyticsItem(
                "1", "CS101", "Data Structures", "Mr. Chan",
                "156 students", "Semester 1, 2024", 82, "Good",
                R.drawable.bg_course_icon_blue
        ));

        list.add(new CourseAnalyticsAdapter.CourseAnalyticsItem(
                "2", "CS102", "Database Systems", "Ms. Dara",
                "120 students", "Semester 1, 2024", 76, "Medium",
                R.drawable.bg_course_icon_orange
        ));

        list.add(new CourseAnalyticsAdapter.CourseAnalyticsItem(
                "3", "CS201", "Web Development", "Mr. Vireak",
                "98 students", "Semester 1, 2024", 88, "Good",
                R.drawable.bg_course_icon_green
        ));

        list.add(new CourseAnalyticsAdapter.CourseAnalyticsItem(
                "4", "CS301", "Computer Networks", "Dr. Sokha",
                "80 students", "Semester 1, 2024", 71, "At Risk",
                R.drawable.bg_metric_purple
        ));

        return list;
    }
}
