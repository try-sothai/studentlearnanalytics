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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.TeacherAnalyticsResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.analytics.CourseAnalyticsAdapter;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherReports), getString(R.string.nav_reports), getString(R.string.export_class_performance_reports)
            );
        }

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
            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(barChart.getContext(), barChart);
        }

        PieChart pieChart = view.findViewById(R.id.pieChartStatusDoughnut);
        if (pieChart != null) {
            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(50f);
            pieChart.setTransparentCircleRadius(55f);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.getDescription().setEnabled(false);
            pieChart.setNoDataText(getString(R.string.empty_state_no_data));
            ChartFontHelper.applyKhmerFont(pieChart.getContext(), pieChart);
        }

        RecyclerView rvList = view.findViewById(R.id.rvCourseAnalyticsList);
        if (rvList != null) {
            rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new CourseAnalyticsAdapter(requireContext(), new ArrayList<>(), item -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ClassAnalyticsFragment fragment = ClassAnalyticsFragment.newInstance(
                            item.code,
                            item.name,
                            item.instructor,
                            item.semester,
                            true
                    );
                    ((TeacherMainActivity) getActivity()).openFragment(fragment);
                }
            });
            rvList.setAdapter(adapter);
        }

        fetchTeacherAnalytics(barChart, pieChart);
        fetchTeacherCourseOfferings(rvList);

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
            if (item.getItemId() == 1 && tvSelectedSort != null) tvSelectedSort.setText(R.string.sort_code);
            else if (item.getItemId() == 2 && tvSelectedSort != null) tvSelectedSort.setText(R.string.sort_name);
            else if (item.getItemId() == 3 && tvSelectedSort != null) tvSelectedSort.setText(R.string.sort_score);
            else if (item.getItemId() == 4 && tvSelectedSort != null) tvSelectedSort.setText(R.string.sort_risk);
            if (adapter != null) {
                adapter.sortBy(item.getItemId());
            }
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

    private void fetchTeacherAnalytics(@Nullable BarChart barChart, @Nullable PieChart pieChart) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(requireContext()).getTeacherAnalytics(null, null, null, null, null, null)
                .enqueue(new Callback<TeacherAnalyticsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TeacherAnalyticsResponse> call, @NonNull Response<TeacherAnalyticsResponse> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) return;
                        TeacherAnalyticsResponse.Data data = response.body().getData();
                        List<TeacherAnalyticsResponse.EngagementItem> engagement = data.getEngagement();

                        if (barChart != null && !engagement.isEmpty()) {
                            List<BarEntry> entries = new ArrayList<>();
                            List<String> labels = new ArrayList<>();
                            float sumCompleted = 0f, sumLate = 0f, sumMissed = 0f;
                            int count = 0;
                            for (int i = 0; i < engagement.size(); i++) {
                                TeacherAnalyticsResponse.EngagementItem item = engagement.get(i);
                                float comp = item.getCompleted() != null ? item.getCompleted().floatValue() : 0f;
                                float late = item.getLate() != null ? item.getLate().floatValue() : 0f;
                                float miss = item.getMissed() != null ? item.getMissed().floatValue() : 0f;
                                entries.add(new BarEntry(i, comp));
                                labels.add(item.getActivity() != null ? item.getActivity() : "");
                                if (item.getTotalEvents() > 0) {
                                    sumCompleted += comp;
                                    sumLate += late;
                                    sumMissed += miss;
                                    count++;
                                }
                            }
                            BarDataSet dataSet = new BarDataSet(entries, getString(R.string.average_score));
                            dataSet.setColors(new int[]{
                                    Color.parseColor("#2563FF"),
                                    Color.parseColor("#16C784"),
                                    Color.parseColor("#FF9B16"),
                                    Color.parseColor("#873BFF")
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
                            barChart.setData(barData);

                            XAxis xAxis = barChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels.toArray(new String[0])));
                            xAxis.setTextColor(Color.parseColor("#10165C"));
                            xAxis.setTextSize(11f);

                            YAxis leftAxis = barChart.getAxisLeft();
                            leftAxis.setAxisMinimum(0f);
                            leftAxis.setAxisMaximum(100f);
                            leftAxis.setGranularity(20f);
                            leftAxis.setTextColor(Color.parseColor("#6676AD"));
                            leftAxis.setGridColor(Color.parseColor("#DCE5FF"));

                            ChartFontHelper.applyKhmerFont(barChart.getContext(), barChart);
                            barChart.invalidate();

                            if (pieChart != null && count > 0) {
                                List<PieEntry> pieEntries = new ArrayList<>();
                                if (sumCompleted > 0f) pieEntries.add(new PieEntry(sumCompleted / count, "Completed"));
                                if (sumLate > 0f) pieEntries.add(new PieEntry(sumLate / count, "Late"));
                                if (sumMissed > 0f) pieEntries.add(new PieEntry(sumMissed / count, "Missed"));
                                if (!pieEntries.isEmpty()) {
                                    PieDataSet pds = new PieDataSet(pieEntries, "");
                                    pds.setColors(new int[]{Color.parseColor("#16C784"), Color.parseColor("#FF9B16"), Color.parseColor("#FF4055")});
                                    pds.setSliceSpace(3f);
                                    pds.setValueTextSize(11f);
                                    pds.setValueTextColor(Color.WHITE);
                                    PieData pd = new PieData(pds);
                                    pd.setValueFormatter(new PercentFormatter(pieChart));
                                    pieChart.setData(pd);
                                    int coursesCnt = data.getSummary() != null ? data.getSummary().getMyCourses() : count;
                                    pieChart.setCenterText(coursesCnt + " Courses");
                                    pieChart.setCenterTextColor(Color.parseColor("#10165C"));
                                    pieChart.setCenterTextSize(14f);
                                    Legend legend = pieChart.getLegend();
                                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
                                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                                    legend.setOrientation(Legend.LegendOrientation.VERTICAL);
                                    legend.setTextColor(Color.parseColor("#10165C"));
                                    legend.setTextSize(11f);
                                    ChartFontHelper.applyKhmerFont(pieChart.getContext(), pieChart);
                                    pieChart.invalidate();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TeacherAnalyticsResponse> call, @NonNull Throwable t) {
                    }
                });
    }

    private void fetchTeacherCourseOfferings(@Nullable RecyclerView rvList) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.getApiService(requireContext()).getTeacherCourseOfferings()
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;
                        JsonElement dataEl = response.body().get("data");
                        if (!dataEl.isJsonArray()) return;
                        JsonArray arr = dataEl.getAsJsonArray();
                        List<CourseAnalyticsAdapter.CourseAnalyticsItem> items = new ArrayList<>();
                        int[] bgs = new int[]{
                                R.drawable.bg_course_icon_blue,
                                R.drawable.bg_course_icon_orange,
                                R.drawable.bg_course_icon_green,
                                R.drawable.bg_metric_purple
                        };
                        for (int i = 0; i < arr.size(); i++) {
                            if (!arr.get(i).isJsonObject()) continue;
                            JsonObject o = arr.get(i).getAsJsonObject();
                            String id = UiUtils.optString(o, "id", String.valueOf(i + 1));
                            JsonObject course = o.has("course") && o.get("course").isJsonObject() ? o.getAsJsonObject("course") : new JsonObject();
                            JsonObject cls = o.has("class_room") && o.get("class_room").isJsonObject() ? o.getAsJsonObject("class_room") : new JsonObject();
                            JsonObject sem = o.has("semester") && o.get("semester").isJsonObject() ? o.getAsJsonObject("semester") : new JsonObject();

                            String code = UiUtils.optString(course, "code", UiUtils.optString(course, "course_code", "CRS"));
                            String name = UiUtils.optString(course, "name", UiUtils.optString(course, "course_name", "Course"));
                            String className = UiUtils.optString(cls, "name", UiUtils.optString(cls, "code", ""));
                            int enrolled = UiUtils.optInt(o, "enrollments_count", 0);
                            int avgScore = (int) Math.round(o.has("average_score") && !o.get("average_score").isJsonNull() ? o.get("average_score").getAsDouble() : 0.0);
                            String riskStatus = UiUtils.optString(o, "risk_status", "Good");
                            String semName = UiUtils.optString(sem, "name", "");
                            items.add(new CourseAnalyticsAdapter.CourseAnalyticsItem(
                                    id, code, name, className,
                                    enrolled + " students", semName, avgScore, riskStatus, bgs[i % bgs.length]
                            ));
                        }
                        if (rvList != null) {
                            adapter = new CourseAnalyticsAdapter(requireContext(), items, item -> {
                                if (getActivity() instanceof TeacherMainActivity) {
                                    ClassAnalyticsFragment fragment = ClassAnalyticsFragment.newInstance(
                                            item.code, item.name, item.instructor, item.semester, true);
                                    ((TeacherMainActivity) getActivity()).openFragment(fragment);
                                }
                            });
                            adapter.filter(searchQuery, selectedCategory);
                            rvList.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    }
                });
    }
}
