package com.social7.studentlearnanalytics.teacher;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.TeacherAnalyticsResponse;
import com.social7.studentlearnanalytics.model.TeacherDashboardSummaryResponse;
import com.social7.studentlearnanalytics.model.TeacherFiltersResponse;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_TEACHER_DASH";

    // Summary Card TextViews
    private TextView tvTotalStudentsCount;
    private TextView tvMyCoursesCount;
    private TextView tvAtRiskCount;
    private TextView tvPendingScoresCount;

    // Filters
    private Spinner spinnerAcademicYear;
    private Spinner spinnerSemester;
    private Spinner spinnerCourse;
    private Spinner spinnerClass;
    private Spinner spinnerProgram;
    private Spinner spinnerStudyYear;
    private Spinner spinnerGeneration;

    // Chart & States
    private BarChart barChartEngagement;
    private ProgressBar pbChartLoading;
    private LinearLayout layoutChartEmpty;
    private LinearLayout layoutChartError;
    private TextView tvChartErrorMessage;
    private MaterialButton btnRetryChart;
    private ImageView btnRefreshChart;
    private TextView tvEngagementSubtitle;

    // Filter Data Cache
    private TeacherFiltersResponse.Data filterData;
    private Integer selectedAcademicYearId = null;
    private Integer selectedSemesterId = null;
    private Integer selectedCourseId = null;
    private Integer selectedClassId = null;
    private Integer selectedProgramId = null;
    private Integer selectedStudyYear = null;

    private boolean isFilterInitializing = true;

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

        initViews(view);
        setupCardClicks(view);
        setupChartAppearance();

        loadDashboardSummary();
        loadFiltersAndAnalytics();

        return view;
    }

    private void initViews(View view) {
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherDashboard), getString(R.string.teacher_dashboard), getString(R.string.overview_analytics)
            );
        }

        // Summary Card Counts
        tvTotalStudentsCount = view.findViewById(R.id.tvTotalStudentsCount);
        tvMyCoursesCount = view.findViewById(R.id.tvMyCoursesCount);
        tvAtRiskCount = view.findViewById(R.id.tvAtRiskCount);
        tvPendingScoresCount = view.findViewById(R.id.tvPendingScoresCount);

        // Spinners
        spinnerAcademicYear = view.findViewById(R.id.spinnerAcademicYear);
        spinnerSemester = view.findViewById(R.id.spinnerSemester);
        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        spinnerClass = view.findViewById(R.id.spinnerClass);
        spinnerProgram = view.findViewById(R.id.spinnerProgram);
        spinnerStudyYear = view.findViewById(R.id.spinnerStudyYear);
        spinnerGeneration = view.findViewById(R.id.spinnerGeneration);

        // Chart & State Layouts
        barChartEngagement = view.findViewById(R.id.barChartEngagement);
        pbChartLoading = view.findViewById(R.id.pbChartLoading);
        layoutChartEmpty = view.findViewById(R.id.layoutChartEmpty);
        layoutChartError = view.findViewById(R.id.layoutChartError);
        tvChartErrorMessage = view.findViewById(R.id.tvChartErrorMessage);
        btnRetryChart = view.findViewById(R.id.btnRetryChart);
        btnRefreshChart = view.findViewById(R.id.btnRefreshChart);
        tvEngagementSubtitle = view.findViewById(R.id.tvEngagementSubtitle);

        if (btnRefreshChart != null) {
            btnRefreshChart.setOnClickListener(v -> {
                loadDashboardSummary();
                loadAnalytics();
            });
        }

        if (btnRetryChart != null) {
            btnRetryChart.setOnClickListener(v -> {
                loadDashboardSummary();
                loadAnalytics();
            });
        }
    }

    private void setupCardClicks(View view) {
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
    }

    private void setupChartAppearance() {
        if (barChartEngagement == null) return;

        barChartEngagement.getDescription().setEnabled(false);
        barChartEngagement.getLegend().setEnabled(false); // Using custom top legend in XML
        barChartEngagement.setPinchZoom(false);
        barChartEngagement.setScaleEnabled(false);
        barChartEngagement.setDrawGridBackground(false);
        barChartEngagement.setDrawBarShadow(false);
        barChartEngagement.setHighlightFullBarEnabled(false);

        XAxis xAxis = barChartEngagement.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Attendance", "Assignment", "Homework", "Quiz"}));

        YAxis leftAxis = barChartEngagement.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F1EDFF"));
        leftAxis.setTextColor(Color.parseColor("#64748B"));
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        barChartEngagement.getAxisRight().setEnabled(false);
    }

    /**
     * Load the four primary dashboard cards.
     */
    private void loadDashboardSummary() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext())
                .getTeacherDashboardSummary(selectedAcademicYearId, selectedSemesterId)
                .enqueue(new Callback<TeacherDashboardSummaryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TeacherDashboardSummaryResponse> call,
                                           @NonNull Response<TeacherDashboardSummaryResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            TeacherDashboardSummaryResponse.Summary s = response.body().getData().getSummary();
                            if (s != null) {
                                if (tvTotalStudentsCount != null) {
                                    tvTotalStudentsCount.setText(String.valueOf(s.getTotalStudents()));
                                }
                                if (tvMyCoursesCount != null) {
                                    tvMyCoursesCount.setText(String.valueOf(s.getMyCourses()));
                                }
                                if (tvAtRiskCount != null) {
                                    tvAtRiskCount.setText(String.valueOf(s.getAtRiskStudents()));
                                }
                                if (tvPendingScoresCount != null) {
                                    tvPendingScoresCount.setText(String.valueOf(s.getPendingScores()));
                                }
                            }
                        } else {
                            Log.w(TAG, "Summary API responded with code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TeacherDashboardSummaryResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "Failed to load dashboard summary", t);
                    }
                });
    }

    /**
     * Load authorized dynamic filters, populate Spinners, then fetch engagement analytics.
     */
    private void loadFiltersAndAnalytics() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext())
                .getTeacherFilters(null, null)
                .enqueue(new Callback<TeacherFiltersResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TeacherFiltersResponse> call,
                                           @NonNull Response<TeacherFiltersResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            filterData = response.body().getData();
                            populateFilterSpinners();
                        }

                        // Load initial analytics regardless of filter load success
                        loadAnalytics();
                    }

                    @Override
                    public void onFailure(@NonNull Call<TeacherFiltersResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "Failed to load filters", t);
                        loadAnalytics();
                    }
                });
    }

    private void populateFilterSpinners() {
        if (getContext() == null || filterData == null) return;

        isFilterInitializing = true;

        // 1. Academic Year
        List<String> yearNames = new ArrayList<>();
        yearNames.add("All Years");
        for (TeacherFiltersResponse.AcademicYearItem y : filterData.getAcademicYears()) {
            yearNames.add(y.getName());
        }
        setupSpinner(spinnerAcademicYear, yearNames, position -> {
            if (position == 0) {
                selectedAcademicYearId = null;
            } else {
                selectedAcademicYearId = filterData.getAcademicYears().get(position - 1).getId();
            }
            onFilterChanged();
        });

        // 2. Semester
        List<String> semNames = new ArrayList<>();
        semNames.add("All Semesters");
        for (TeacherFiltersResponse.SemesterItem s : filterData.getSemesters()) {
            semNames.add(s.getName());
        }
        setupSpinner(spinnerSemester, semNames, position -> {
            if (position == 0) {
                selectedSemesterId = null;
            } else {
                selectedSemesterId = filterData.getSemesters().get(position - 1).getId();
            }
            onFilterChanged();
        });

        // 3. Course
        List<String> courseNames = new ArrayList<>();
        courseNames.add("All Courses");
        for (TeacherFiltersResponse.CourseItem c : filterData.getCourses()) {
            courseNames.add(c.getCode() != null ? c.getCode() + " - " + c.getName() : c.getName());
        }
        setupSpinner(spinnerCourse, courseNames, position -> {
            if (position == 0) {
                selectedCourseId = null;
            } else {
                selectedCourseId = filterData.getCourses().get(position - 1).getId();
            }
            onFilterChanged();
        });

        // 4. Class
        List<String> classNames = new ArrayList<>();
        classNames.add("All Classes");
        for (TeacherFiltersResponse.ClassItem cl : filterData.getClasses()) {
            classNames.add(cl.getName() != null ? cl.getName() : cl.getCode());
        }
        setupSpinner(spinnerClass, classNames, position -> {
            if (position == 0) {
                selectedClassId = null;
            } else {
                selectedClassId = filterData.getClasses().get(position - 1).getId();
            }
            onFilterChanged();
        });

        // 5. Program
        List<String> programNames = new ArrayList<>();
        programNames.add("All Programs");
        for (TeacherFiltersResponse.ProgramItem p : filterData.getPrograms()) {
            programNames.add(p.getCode() != null ? p.getCode() : p.getName());
        }
        setupSpinner(spinnerProgram, programNames, position -> {
            if (position == 0) {
                selectedProgramId = null;
            } else {
                selectedProgramId = filterData.getPrograms().get(position - 1).getId();
            }
            onFilterChanged();
        });

        // 6. Study Year
        List<String> studyYearNames = new ArrayList<>();
        studyYearNames.add("All Years");
        for (TeacherFiltersResponse.StudyYearItem sy : filterData.getStudyYears()) {
            studyYearNames.add(sy.getName());
        }
        setupSpinner(spinnerStudyYear, studyYearNames, position -> {
            if (position == 0) {
                selectedStudyYear = null;
            } else {
                selectedStudyYear = filterData.getStudyYears().get(position - 1).getYear();
            }
            onFilterChanged();
        });

        // 7. Generation
        List<String> genNames = new ArrayList<>();
        genNames.add("All Generations");
        for (TeacherFiltersResponse.FilterItem g : filterData.getGenerations()) {
            genNames.add(g.getName());
        }
        setupSpinner(spinnerGeneration, genNames, position -> {
            // Generations are mapped to academic cohort
            onFilterChanged();
        });

        isFilterInitializing = false;
    }

    private void setupSpinner(Spinner spinner, List<String> items, OnItemIndexSelectedListener listener) {
        if (spinner == null || getContext() == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isFilterInitializing) {
                    listener.onSelected(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void onFilterChanged() {
        loadDashboardSummary();
        loadAnalytics();
    }

    /**
     * Load the Student Learning Engagement Overview from real backend data.
     */
    private void loadAnalytics() {
        if (getContext() == null) return;

        showLoading();

        ApiClient.getApiService(requireContext())
                .getTeacherAnalytics(
                        selectedAcademicYearId,
                        selectedSemesterId,
                        selectedCourseId,
                        selectedClassId,
                        selectedProgramId,
                        selectedStudyYear
                )
                .enqueue(new Callback<TeacherAnalyticsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TeacherAnalyticsResponse> call,
                                           @NonNull Response<TeacherAnalyticsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            TeacherAnalyticsResponse.Data data = response.body().getData();
                            updateScopeSubtitle(data.getScope());
                            renderEngagementChart(data.getEngagement());
                        } else {
                            showError("Unable to retrieve engagement data (Code: " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TeacherAnalyticsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "Analytics request failed", t);
                        showError("Connection error. Tap Retry to reload.");
                    }
                });
    }

    private void updateScopeSubtitle(TeacherAnalyticsResponse.Scope scope) {
        if (tvEngagementSubtitle == null || scope == null) return;

        StringBuilder sb = new StringBuilder();
        if (scope.getCourse() != null && !scope.getCourse().isEmpty()) {
            sb.append(scope.getCourse());
        }
        if (scope.getClassName() != null && !scope.getClassName().isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(scope.getClassName());
        }
        if (scope.getSemester() != null && !scope.getSemester().isEmpty()) {
            if (sb.length() > 0) sb.append(" (").append(scope.getSemester()).append(")");
        }

        if (sb.length() > 0) {
            tvEngagementSubtitle.setText(sb.toString());
        } else {
            tvEngagementSubtitle.setText(R.string.attendance_assignment_homework_quiz);
        }
    }

    private void renderEngagementChart(List<TeacherAnalyticsResponse.EngagementItem> items) {
        if (barChartEngagement == null) return;

        if (items == null || items.isEmpty()) {
            showEmpty();
            return;
        }

        boolean hasAnyActivityData = false;
        for (TeacherAnalyticsResponse.EngagementItem item : items) {
            if (item.isHasData() || item.getTotalEvents() > 0) {
                hasAnyActivityData = true;
                break;
            }
        }

        if (!hasAnyActivityData) {
            showEmpty();
            return;
        }

        List<BarEntry> completedEntries = new ArrayList<>();
        List<BarEntry> lateEntries = new ArrayList<>();
        List<BarEntry> missedEntries = new ArrayList<>();

        for (int i = 0; i < 4 && i < items.size(); i++) {
            TeacherAnalyticsResponse.EngagementItem item = items.get(i);
            completedEntries.add(new BarEntry(i, item.getCompletedFloat()));
            lateEntries.add(new BarEntry(i, item.getLateFloat()));
            missedEntries.add(new BarEntry(i, item.getMissedFloat()));
        }

        // Fill remaining entries up to 4 if list is smaller
        for (int i = items.size(); i < 4; i++) {
            completedEntries.add(new BarEntry(i, 0f));
            lateEntries.add(new BarEntry(i, 0f));
            missedEntries.add(new BarEntry(i, 0f));
        }

        BarDataSet setCompleted = new BarDataSet(completedEntries, getString(R.string.status_completed));
        setCompleted.setColor(Color.parseColor("#10B981")); // Emerald Green
        setCompleted.setValueTextSize(9f);
        setCompleted.setValueTextColor(Color.parseColor("#1E1B4B"));
        setCompleted.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format(Locale.US, "%.0f%%", value) : "";
            }
        });

        BarDataSet setLate = new BarDataSet(lateEntries, getString(R.string.status_late));
        setLate.setColor(Color.parseColor("#F59E0B")); // Amber Orange
        setLate.setValueTextSize(9f);
        setLate.setValueTextColor(Color.parseColor("#1E1B4B"));
        setLate.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format(Locale.US, "%.0f%%", value) : "";
            }
        });

        BarDataSet setMissed = new BarDataSet(missedEntries, getString(R.string.missed));
        setMissed.setColor(Color.parseColor("#EF4444")); // Coral Red
        setMissed.setValueTextSize(9f);
        setMissed.setValueTextColor(Color.parseColor("#1E1B4B"));
        setMissed.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format(Locale.US, "%.0f%%", value) : "";
            }
        });

        float groupSpace = 0.28f;
        float barSpace = 0.04f;
        float barWidth = 0.20f;
        // Total = 4 * (groupSpace + 3 * (barSpace + barWidth)) = 4 * (0.28 + 3 * 0.24) = 4.0

        BarData barData = new BarData(setCompleted, setLate, setMissed);
        barData.setBarWidth(barWidth);

        barChartEngagement.setData(barData);
        barChartEngagement.getXAxis().setAxisMinimum(0);
        barChartEngagement.getXAxis().setAxisMaximum(4);
        barChartEngagement.groupBars(0, groupSpace, barSpace);
        barChartEngagement.animateY(800);
        ChartFontHelper.applyKhmerFont(barChartEngagement.getContext(), barChartEngagement);
        barChartEngagement.invalidate();

        showChart();
    }

    private void showLoading() {
        if (pbChartLoading != null) pbChartLoading.setVisibility(View.VISIBLE);
        if (barChartEngagement != null) barChartEngagement.setVisibility(View.INVISIBLE);
        if (layoutChartEmpty != null) layoutChartEmpty.setVisibility(View.GONE);
        if (layoutChartError != null) layoutChartError.setVisibility(View.GONE);
    }

    private void showChart() {
        if (pbChartLoading != null) pbChartLoading.setVisibility(View.GONE);
        if (barChartEngagement != null) barChartEngagement.setVisibility(View.VISIBLE);
        if (layoutChartEmpty != null) layoutChartEmpty.setVisibility(View.GONE);
        if (layoutChartError != null) layoutChartError.setVisibility(View.GONE);
    }

    private void showEmpty() {
        if (pbChartLoading != null) pbChartLoading.setVisibility(View.GONE);
        if (barChartEngagement != null) barChartEngagement.setVisibility(View.INVISIBLE);
        if (layoutChartEmpty != null) layoutChartEmpty.setVisibility(View.VISIBLE);
        if (layoutChartError != null) layoutChartError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        if (pbChartLoading != null) pbChartLoading.setVisibility(View.GONE);
        if (barChartEngagement != null) barChartEngagement.setVisibility(View.INVISIBLE);
        if (layoutChartEmpty != null) layoutChartEmpty.setVisibility(View.GONE);
        if (layoutChartError != null) layoutChartError.setVisibility(View.VISIBLE);
        if (tvChartErrorMessage != null && message != null) {
            tvChartErrorMessage.setText(message);
        }
    }

    private interface OnItemIndexSelectedListener {
        void onSelected(int position);
    }
}
