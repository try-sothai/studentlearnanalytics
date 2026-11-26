package com.social7.studentlearnanalytics.student;


import com.social7.studentlearnanalytics.localization.ChartFontHelper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.grades.GradesOverviewActivity;
import com.social7.studentlearnanalytics.student.recommendations.SmartRecommendationsActivity;
import com.social7.studentlearnanalytics.student.assignment.AssessmentDetailActivity;
import com.social7.studentlearnanalytics.student.attendance.AttendanceOverviewActivity;
import com.social7.studentlearnanalytics.student.analytics.LearningAnalyticsActivity;
import com.social7.studentlearnanalytics.student.academic.AcademicJourneyActivity;
import com.social7.studentlearnanalytics.student.quiz.QuizDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardFragment extends Fragment {

    private static final String TAG = "SLAS_DASHBOARD";

    private TextView tvUserName;
    private TextView tvPerfScore;
    private LineChart lineChartPerformance;
    private SessionManager sessionManager;

    public StudentDashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.activity_fragment_student_dashboard,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        tvUserName = view.findViewById(R.id.tvUserName);
        tvPerfScore = view.findViewById(R.id.tvPerfScore);
        lineChartPerformance = view.findViewById(R.id.lineChartPerformance);

        if (tvUserName != null) {
            String name = sessionManager.getStudentName();
            if (name != null && !name.trim().isEmpty()) {
                tvUserName.setText(name + "! 👋");
            }
        }

        setupPerformanceChart(lineChartPerformance, null, null);

        // Header Navigation Listeners
        View btnMenu = view.findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof StudentMainActivity) {
                    ((StudentMainActivity) getActivity()).openDrawer();
                } else {
                    startActivity(new Intent(getActivity(), NavigationMenuActivity.class));
                }
            });
        }

        View profileContainer = view.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> switchBottomTab(R.id.nav_profile));
        }

        View notifContainer = view.findViewById(R.id.notifContainer);
        if (notifContainer != null) {
            notifContainer.setOnClickListener(v -> switchBottomTab(R.id.nav_notifications));
        }

        // Metrics Grid Click Listeners
        View cardAttendance = view.findViewById(R.id.cardAttendance);
        if (cardAttendance != null) {
            cardAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceOverviewActivity.class)));
        }

        View cardAssignments = view.findViewById(R.id.cardAssignments);
        if (cardAssignments != null) {
            cardAssignments.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View cardQuizzes = view.findViewById(R.id.cardQuizzes);
        if (cardQuizzes != null) {
            cardQuizzes.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View cardGpa = view.findViewById(R.id.cardGpa);
        if (cardGpa != null) {
            cardGpa.setOnClickListener(v -> startActivity(new Intent(getActivity(), GradesOverviewActivity.class)));
        }

        View cardOverallPerformance = view.findViewById(R.id.cardOverallPerformance);
        if (cardOverallPerformance != null) {
            cardOverallPerformance.setOnClickListener(v -> startActivity(new Intent(getActivity(), LearningAnalyticsActivity.class)));
        }

        View btnViewDetails = view.findViewById(R.id.btnViewDetails);
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> startActivity(new Intent(getActivity(), AcademicJourneyActivity.class)));
        }

        // Predicted Score Card Navigation Listener
        View cardPredictedScore = view.findViewById(R.id.cardPredictedScore);
        if (cardPredictedScore != null) {
            cardPredictedScore.setOnClickListener(v -> startActivity(new Intent(getActivity(), PredictionDetailActivity.class)));
        }

        // Risk Level Card Navigation Listeners
        View cardRiskLevel = view.findViewById(R.id.cardRiskLevel);
        if (cardRiskLevel != null) {
            cardRiskLevel.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        View btnRiskDetails = view.findViewById(R.id.btnRiskDetails);
        if (btnRiskDetails != null) {
            btnRiskDetails.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiskAnalysisActivity.class)));
        }

        // Quick Actions Section Listeners
        View btnViewAllActions = view.findViewById(R.id.btnViewAllActions);
        if (btnViewAllActions != null) {
            btnViewAllActions.setOnClickListener(v -> startActivity(new Intent(getActivity(), AllFeaturesActivity.class)));
        }

        View actionCourses = view.findViewById(R.id.actionCourses);
        if (actionCourses != null) {
            actionCourses.setOnClickListener(v -> switchBottomTab(R.id.nav_courses));
        }

        View actionAttendance = view.findViewById(R.id.actionAttendance);
        if (actionAttendance != null) {
            actionAttendance.setOnClickListener(v -> startActivity(new Intent(getActivity(), AttendanceOverviewActivity.class)));
        }

        View actionAssignments = view.findViewById(R.id.actionAssignments);
        if (actionAssignments != null) {
            actionAssignments.setOnClickListener(v -> startActivity(new Intent(getActivity(), AssessmentDetailActivity.class)));
        }

        View actionQuizzes = view.findViewById(R.id.actionQuizzes);
        if (actionQuizzes != null) {
            actionQuizzes.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuizDetailActivity.class)));
        }

        View actionExams = view.findViewById(R.id.actionExams);
        if (actionExams != null) {
            actionExams.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExamDetailActivity.class)));
        }

        View actionAnalytics = view.findViewById(R.id.actionAnalytics);
        if (actionAnalytics != null) {
            actionAnalytics.setOnClickListener(v -> startActivity(new Intent(getActivity(), OverallPerformanceActivity.class)));
        }

        // Recent Activities Section Listeners
        View btnViewAllActivities = view.findViewById(R.id.btnViewAllActivities);
        if (btnViewAllActivities != null) {
            btnViewAllActivities.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecentActivitiesActivity.class)));
        }

        View cardLearningProgress = view.findViewById(R.id.cardLearningProgress);
        if (cardLearningProgress != null) {
            cardLearningProgress.setOnClickListener(v -> switchBottomTab(R.id.nav_analytics));
        }

        fetchDashboardData(view);

        return view;
    }

    private void fetchDashboardData(View root) {
        ApiClient.getApiService(requireContext()).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse dashboard = response.body();
                    DashboardResponse.DashboardData data = dashboard.getData();
                    if (data != null && isAdded()) {
                        bindDashboardData(root, data);
                    }
                } else {
                    Log.d(TAG, "Dashboard API response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "Dashboard API call failed: " + t.getMessage());
            }
        });
    }

    private void bindDashboardData(View root, DashboardResponse.DashboardData data) {
        if (data == null || root == null) return;

        // Update notification badge in header & bottom nav
        int unreadCount = data.getUnreadNotificationsCount();
        TextView tvNotifBadge = root.findViewById(R.id.tvDashboardNotifBadge);
        if (tvNotifBadge != null) {
            if (unreadCount > 0) {
                tvNotifBadge.setVisibility(View.VISIBLE);
                tvNotifBadge.setText(String.valueOf(unreadCount));
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        }
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).updateGlobalNotificationBadge(unreadCount);
        }

        if (tvUserName != null) {
            String studentName = data.getStudentName();
            if (studentName != null && !studentName.trim().isEmpty()) {
                tvUserName.setText(studentName + "! 👋");
            } else if (sessionManager != null && !sessionManager.getStudentName().trim().isEmpty()) {
                tvUserName.setText(sessionManager.getStudentName() + "! 👋");
            }
        }

        TextView tvStudentId = root.findViewById(R.id.tvStudentId);
        if (tvStudentId != null) {
            String studentId = data.getStudentId();
            if (studentId == null || studentId.trim().isEmpty()) {
                studentId = sessionManager != null ? sessionManager.getUserId() : "";
            }
            if (studentId != null && !studentId.trim().isEmpty()) {
                tvStudentId.setText(tvStudentId.getContext().getString(R.string.student_id_a8dd9e) + studentId.trim());
            } else {
                tvStudentId.setText(R.string.student_id_unavailable);
            }
        }

        TextView tvDepartmentYear = root.findViewById(R.id.tvDepartmentYear);
        if (tvDepartmentYear != null) {
            String majorOrDept = data.getMajorOrDepartment();
            Integer ylNum = data.getStudent() != null ? data.getStudent().getYearLevelNumber() : null;
            String yearLvl = ylNum != null
                    ? com.social7.studentlearnanalytics.utils.YearLevelHelper.toFullLabel(getContext(), ylNum)
                    : data.getYearLevelFormatted();
            String ay = data.getAcademicYear();
            String yearAndAy = (ay != null && !ay.isEmpty()) ? (yearLvl + " • " + ay) : yearLvl;

            if (majorOrDept != null && !majorOrDept.trim().isEmpty()) {
                tvDepartmentYear.setText(majorOrDept.trim() + " | " + yearAndAy);
            } else {
                tvDepartmentYear.setText(getString(R.string.major_info_unavailable_sep) + yearAndAy);
            }
        }

        if (tvPerfScore != null) {
            tvPerfScore.setText(data.getOverallPerformanceFormatted());
        }

        TextView tvPerfTrend = root.findViewById(R.id.tvPerfTrend);
        if (tvPerfTrend != null) {
            if (data.getAnalytics() != null && data.getAnalytics().performanceChange != null) {
                float change = data.getAnalytics().performanceChange;
                tvPerfTrend.setVisibility(View.VISIBLE);
                if (change >= 0) {
                    tvPerfTrend.setText(String.format(Locale.US, "▲ +%.1f%%", change));
                    tvPerfTrend.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvPerfTrend.setText(String.format(Locale.US, "▼ %.1f%%", change));
                    tvPerfTrend.setTextColor(Color.parseColor("#FCA5A5"));
                }
            } else {
                tvPerfTrend.setVisibility(View.GONE);
            }
        }

        TextView tvMetricAttendance = root.findViewById(R.id.tvMetricAttendance);
        if (tvMetricAttendance != null) {
            tvMetricAttendance.setText(data.getAttendanceRateFormatted());
        }
        TextView tvAttendanceSub = root.findViewById(R.id.tvAttendanceSub);
        if (tvAttendanceSub != null && data.getAttendanceSummary() != null) {
            DashboardResponse.AttendanceSummaryInfo att = data.getAttendanceSummary();
            int present = att.present != null ? att.present : 0;
            int total = att.total != null ? att.total : 0;
            if (total > 0) {
                tvAttendanceSub.setText(present + " / " + total + " Present");
                tvAttendanceSub.setTextColor(Color.parseColor("#10B981"));
            }
        }

        TextView tvMetricAssignments = root.findViewById(R.id.tvMetricAssignments);
        if (tvMetricAssignments != null) {
            tvMetricAssignments.setText(data.getAssignmentScoreFormatted());
        }
        TextView tvAssignmentsSub = root.findViewById(R.id.tvAssignmentsSub);
        if (tvAssignmentsSub != null && data.getAssignmentsSummary() != null) {
            DashboardResponse.AssignmentsSummaryInfo asn = data.getAssignmentsSummary();
            int comp = asn.completed != null ? asn.completed : 0;
            int pend = asn.pending != null ? asn.pending : 0;
            tvAssignmentsSub.setText(comp + " Done · " + pend + " Pending");
            tvAssignmentsSub.setTextColor(pend > 0 ? Color.parseColor("#F59E0B") : Color.parseColor("#10B981"));
        }

        TextView tvMetricQuizzes = root.findViewById(R.id.tvMetricQuizzes);
        if (tvMetricQuizzes != null) {
            tvMetricQuizzes.setText(data.getQuizScoreFormatted());
        }
        TextView tvQuizzesSub = root.findViewById(R.id.tvQuizzesSub);
        if (tvQuizzesSub != null && data.getQuizzesSummary() != null) {
            DashboardResponse.QuizzesSummaryInfo qz = data.getQuizzesSummary();
            int comp = qz.completed != null ? qz.completed : 0;
            int upc = qz.upcoming != null ? qz.upcoming : 0;
            tvQuizzesSub.setText(comp + " Done · " + upc + " Upcoming");
            tvQuizzesSub.setTextColor(Color.parseColor("#10B981"));
        }

        TextView tvMetricGpa = root.findViewById(R.id.tvMetricGpa);
        if (tvMetricGpa != null) {
            tvMetricGpa.setText(data.getGpaFormatted());
        }
        TextView tvGpaSub = root.findViewById(R.id.tvGpaSub);
        if (tvGpaSub != null) {
            DashboardResponse.GpaSummaryInfo gpaSum = data.getGpaSummary();
            if (gpaSum != null && gpaSum.academicStanding != null && !gpaSum.academicStanding.isEmpty()) {
                String standing = gpaSum.academicStanding.substring(0, 1).toUpperCase() + gpaSum.academicStanding.substring(1);
                tvGpaSub.setText(standing);
                tvGpaSub.setTextColor(Color.parseColor("#10B981"));
            } else {
                tvGpaSub.setText("No grades yet");
                tvGpaSub.setTextColor(Color.parseColor("#64748B"));
            }
        }

        TextView tvPredictedScore = root.findViewById(R.id.tvPredictedScore);
        ProgressBar pbPredictedScore = root.findViewById(R.id.pbPredictedScore);
        TextView tvConfidence = root.findViewById(R.id.tvConfidence);

        if (tvPredictedScore != null) {
            String predStr = data.getPredictedScoreFormatted();
            tvPredictedScore.setText(predStr);

            if ("Not available yet".equalsIgnoreCase(predStr)) {
                if (pbPredictedScore != null) pbPredictedScore.setProgress(0);
                if (tvConfidence != null) {
                    tvConfidence.setText(R.string.prediction_model_pending);
                    tvConfidence.setTextColor(Color.parseColor("#64748B"));
                }
            } else {
                if (pbPredictedScore != null) {
                    try {
                        String clean = predStr.split("/")[0].trim();
                        double score = Double.parseDouble(clean);
                        pbPredictedScore.setProgress((int) Math.round(score));
                    } catch (Exception ignored) {
                        pbPredictedScore.setProgress(50);
                    }
                }
                if (tvConfidence != null) {
                    Double conf = data.getPredictionConfidence();
                    String ver = data.getModelVersion();
                    if (conf != null) {
                        int confPct = (int) Math.round(conf * 100);
                        String verSuffix = (ver != null && !ver.isEmpty()) ? (" (" + ver + ")") : "";
                        tvConfidence.setText(getString(R.string.model_confidence_prefix) + confPct + "%" + verSuffix);
                        tvConfidence.setTextColor(Color.parseColor("#10B981"));
                    } else {
                        tvConfidence.setText(getString(R.string.model_confidence_prefix) + "N/A");
                        tvConfidence.setTextColor(Color.parseColor("#64748B"));
                    }
                }
            }
        }

        TextView tvRiskLevel = root.findViewById(R.id.tvRiskLevel);
        TextView tvRiskMsg = root.findViewById(R.id.tvRiskMsg);
        if (tvRiskLevel != null) {
            String risk = data.getRiskLevel();
            tvRiskLevel.setText(risk);
            if ("HIGH".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#EF4444"));
                if (tvRiskMsg != null) tvRiskMsg.setText(getString(R.string.high_risk_action_recommended));
            } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#F59E0B"));
                if (tvRiskMsg != null) tvRiskMsg.setText(R.string.keep_improving_performance);
            } else if ("LOW".equalsIgnoreCase(risk)) {
                tvRiskLevel.setTextColor(Color.parseColor("#10B981"));
                if (tvRiskMsg != null) tvRiskMsg.setText(getString(R.string.great_job_low_risk));
            } else {
                tvRiskLevel.setTextColor(Color.parseColor("#64748B"));
                if (tvRiskMsg != null) tvRiskMsg.setText(R.string.risk_assessment_pending);
            }
        }

        // Bind Semester Badge on Learning Progress
        TextView tvDashboardSemesterBadge = root.findViewById(R.id.tvDashboardSemesterBadge);
        if (tvDashboardSemesterBadge != null && data.getSemesters() != null && !data.getSemesters().isEmpty()) {
            String semName = data.getSemesters().get(0).name;
            if (semName != null && !semName.trim().isEmpty()) {
                tvDashboardSemesterBadge.setText(semName);
            }
        }

        // Bind Dynamic Recent Activities
        LinearLayout layoutRecent = root.findViewById(R.id.layoutRecentActivitiesContainer);
        if (layoutRecent != null) {
            bindRecentActivities(layoutRecent, data.getRecentActivities());
        }

        if (data.getChartData() != null && !data.getChartData().isEmpty()) {
            setupPerformanceChart(lineChartPerformance, data.getChartData(), data.getChartLabels());
        } else {
            setupPerformanceChart(lineChartPerformance, null, null);
        }
    }

    private void bindRecentActivities(LinearLayout container, List<DashboardResponse.RecentActivityInfo> activities) {
        if (!isAdded() || container == null) return;
        container.removeAllViews();

        if (activities == null || activities.isEmpty()) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText(getString(R.string.no_recent_academic_activities));
            tvEmpty.setTextColor(Color.parseColor("#64748B"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, dpToPx(16), 0, dpToPx(16));
            container.addView(tvEmpty);
            return;
        }

        int count = Math.min(activities.size(), 4);
        for (int i = 0; i < count; i++) {
            final DashboardResponse.RecentActivityInfo item = activities.get(i);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setClickable(true);
            row.setFocusable(true);

            ImageView icon = new ImageView(requireContext());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            icon.setLayoutParams(iconParams);
            int pad = dpToPx(7);
            icon.setPadding(pad, pad, pad, pad);

            String type = item.type != null ? item.type.toLowerCase() : "";
            if ("assignment".equals(type)) {
                icon.setBackgroundResource(R.drawable.bg_stat_card_purple);
                icon.setImageResource(R.drawable.ic_assignment);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slas_primary_purple));
            } else if ("quiz".equals(type)) {
                icon.setBackgroundResource(R.drawable.bg_stat_card_orange);
                icon.setImageResource(R.drawable.ic_analytics);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slas_warning));
            } else if ("grade".equals(type) || "exam".equals(type)) {
                icon.setBackgroundResource(R.drawable.bg_stat_card_green);
                icon.setImageResource(R.drawable.ic_prediction);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slas_success));
            } else if ("attendance".equals(type)) {
                icon.setBackgroundResource(R.drawable.bg_stat_card_green);
                icon.setImageResource(R.drawable.ic_home);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slas_success));
            } else {
                icon.setBackgroundResource(R.drawable.bg_stat_card_purple);
                icon.setImageResource(R.drawable.ic_book);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.slas_primary_purple));
            }
            row.addView(icon);

            LinearLayout textCol = new LinearLayout(requireContext());
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textColParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textColParams.setMarginStart(dpToPx(12));
            textColParams.setMarginEnd(dpToPx(8));
            textCol.setLayoutParams(textColParams);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(item.title != null ? item.title : "Academic Activity");
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.slas_text_primary));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvTitle);

            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(item.description != null ? item.description : "");
            tvDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.slas_text_secondary));
            tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            textCol.addView(tvDesc);

            row.addView(textCol);

            TextView tvTime = new TextView(requireContext());
            tvTime.setText(item.timeAgo != null ? item.timeAgo : "Recently");
            tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.slas_text_secondary));
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
            row.addView(tvTime);

            row.setOnClickListener(v -> navigateToActivityTarget(item));
            container.addView(row);

            if (i < count - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1));
                divParams.topMargin = dpToPx(10);
                divParams.bottomMargin = dpToPx(10);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.slas_border));
                container.addView(divider);
            }
        }
    }

    private void navigateToActivityTarget(DashboardResponse.RecentActivityInfo item) {
        if (!isAdded() || item == null) return;
        String type = item.type != null ? item.type.toLowerCase() : "";
        if ("assignment".equals(type)) {
            startActivity(new Intent(requireContext(), AssessmentDetailActivity.class));
        } else if ("quiz".equals(type)) {
            startActivity(new Intent(requireContext(), QuizDetailActivity.class));
        } else if ("exam".equals(type)) {
            startActivity(new Intent(requireContext(), ExamDetailActivity.class));
        } else if ("grade".equals(type)) {
            startActivity(new Intent(requireContext(), com.social7.studentlearnanalytics.student.academic.GpaProgressActivity.class));
        } else if ("attendance".equals(type)) {
            startActivity(new Intent(requireContext(), AttendanceOverviewActivity.class));
        } else if ("course".equals(type)) {
            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
            if (item.referenceId != null) {
                intent.putExtra("course_id", String.valueOf(item.referenceId));
            }
            if (item.courseCode != null) intent.putExtra("course_code", item.courseCode);
            if (item.courseName != null) intent.putExtra("course_name", item.courseName);
            startActivity(intent);
        } else {
            startActivity(new Intent(requireContext(), RecentActivitiesActivity.class));
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void switchBottomTab(int navId) {
        if (getActivity() instanceof StudentMainActivity) {
            BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) {
                nav.setSelectedItemId(navId);
            }
        }
    }

    private void setupPerformanceChart(LineChart chart, List<Float> values, List<String> labels) {
        if (chart == null) return;

        if (values == null || values.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No historical trend data available");
            chart.setNoDataTextColor(Color.parseColor("#64748B"));
            ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Float val = values.get(i);
            entries.add(new Entry(i, val != null ? val : 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.performance));
        dataSet.setColor(Color.parseColor("#5B3DF5"));
        dataSet.setCircleColor(Color.parseColor("#5B3DF5"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#F1EDFF"));
        dataSet.setFillAlpha(120);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E2E8F0"));
        chart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        chart.getAxisLeft().setTextColor(Color.parseColor("#64748B"));
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.parseColor("#64748B"));
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        if (labels != null && !labels.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.animateY(800);
        ChartFontHelper.applyKhmerFont(chart.getContext(), chart);
        chart.invalidate();
    }
}
