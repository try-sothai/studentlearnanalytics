package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.assessment.AssignmentAdapter;
import com.social7.studentlearnanalytics.teacher.assessment.EnterScoreActivity;
import com.social7.studentlearnanalytics.teacher.assessment.PendingAssessmentAdapter;
import com.social7.studentlearnanalytics.teacher.assessment.QuizAdapter;
import com.social7.studentlearnanalytics.teacher.assessment.QuizBuilderActivity;
import com.social7.studentlearnanalytics.teacher.assessment.TeacherExamAdapter;
import com.social7.studentlearnanalytics.teacher.assessment.TeacherExamDetailActivity;
import com.social7.studentlearnanalytics.teacher.assessment.TeacherQuizAttemptsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Teacher Score Management Fragment hosting 6 full tabs:
 * 1. Overview — real database summary metrics & dynamic course grading progress
 * 2. Pending Scores — actual pending grading tasks across assignments, quizzes, and offline paper exams
 * 3. Assignments — assignment score roster, submissions status, and grading
 * 4. Quizzes — quiz roster, questions count, attempt counts, and scores
 * 5. Exams — offline midterm & final exams, classroom grading, and result publishing
 * 6. Gradebook — consolidated academic gradebook with component breakdowns and final composite grades
 */
public class ManageScoresFragment extends Fragment {

    // Sub-Navigation Tabs
    private TextView tabScoreOverview, tabScorePending, tabScoreAssignments, tabScoreQuizzes, tabScoreExams, tabScoreGradebook;
    private View secScoreOverviewContainer, secScorePendingContainer, secScoreAssignmentsContainer, secScoreQuizzesContainer, secScoreExamsContainer, secScoreGradebookContainer;

    // Common Course Filter Dropdown
    private LinearLayout btnCourseSelector;
    private TextView tvSelectedCourseName;
    private final List<CourseOption> availableCourses = new ArrayList<>();
    private Integer selectedCourseId = null;

    // TAB 0: Overview Views
    private TextView tvOverviewTotalAssessments, tvOverviewPendingScores, tvOverviewCompletedGraded, tvOverviewAverageScore, tvOverviewNoProgress;
    private LinearLayout layoutOverviewCourseProgressContainer;

    // TAB 1: Pending Scores Views & Adapter
    private TextView tvTotalPendingCount, tvTotalPendingSubtext, tvCountAssignments, tvCountQuizzes, tvCountExams, tvEmptyPendingScores;
    private MaterialButton btnFilterAllPending, btnFilterAssignments, btnFilterQuizzes, btnFilterExams, btnFilterOverdue;
    private EditText edtSearchPending;
    private RecyclerView rvPendingAssessments;
    private PendingAssessmentAdapter pendingAdapter;
    private final List<PendingAssessmentAdapter.PendingItem> pendingItems = new ArrayList<>();
    private String selectedPendingCategory = "All";
    private String searchPendingQuery = "";

    // TAB 2: Assignments Views & Adapter
    private EditText edtSearchAssignments;
    private RecyclerView rvAssignmentScoresList;
    private TextView tvEmptyAssignments;
    private AssignmentAdapter assignmentAdapter;
    private final List<AssignmentAdapter.AssignmentCardItem> assignmentItems = new ArrayList<>();
    private final List<JsonObject> rawAssignmentsJson = new ArrayList<>();
    private final List<JsonObject> rawSubmissionsJson = new ArrayList<>();

    // TAB 3: Quizzes Views & Adapter
    private EditText edtSearchQuizzes;
    private RecyclerView rvQuizScoresList;
    private TextView tvEmptyQuizzes;
    private QuizAdapter quizAdapter;
    private final List<QuizAdapter.QuizCardItem> quizItems = new ArrayList<>();
    private final List<JsonObject> rawQuizzesJson = new ArrayList<>();

    // TAB 4: Exams Views & Adapter
    private EditText edtSearchExams;
    private RecyclerView rvExamScoresList;
    private TextView tvEmptyExams;
    private TeacherExamAdapter examAdapter;
    private final List<TeacherExamAdapter.ExamCardItem> examItems = new ArrayList<>();

    // TAB 5: Gradebook Views & Adapter
    private EditText edtSearchGradebook;
    private RecyclerView rvGradebookRosterList;
    private TextView tvEmptyGradebook;
    private GradebookAdapter gradebookAdapter;
    private final List<GradebookAdapter.GradebookItem> gradebookList = new ArrayList<>();

    private int selectedSubTab = 0;
    private boolean hasLoadedOnce = false;

    private static class CourseOption {
        final int id;
        final String code;
        final String name;

        CourseOption(int id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }
    }

    public ManageScoresFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_manage_scores, container, false);

        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view, getString(R.string.score_management), "Manage student grades, assessments, and gradebook"
            );
        }

        tabScoreOverview = view.findViewById(R.id.tabScoreOverview);
        tabScorePending = view.findViewById(R.id.tabScorePending);
        tabScoreAssignments = view.findViewById(R.id.tabScoreAssignments);
        tabScoreQuizzes = view.findViewById(R.id.tabScoreQuizzes);
        tabScoreExams = view.findViewById(R.id.tabScoreExams);
        tabScoreGradebook = view.findViewById(R.id.tabScoreGradebook);

        secScoreOverviewContainer = view.findViewById(R.id.secScoreOverviewContainer);
        secScorePendingContainer = view.findViewById(R.id.secScorePendingContainer);
        secScoreAssignmentsContainer = view.findViewById(R.id.secScoreAssignmentsContainer);
        secScoreQuizzesContainer = view.findViewById(R.id.secScoreQuizzesContainer);
        secScoreExamsContainer = view.findViewById(R.id.secScoreExamsContainer);
        secScoreGradebookContainer = view.findViewById(R.id.secScoreGradebookContainer);

        bindOverviewViews(view);
        bindPendingScoresViews(view);
        bindAssignmentScoresViews(view);
        bindQuizScoresViews(view);
        bindExamScoresViews(view);
        bindGradebookViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchManageScoresData();
        hasLoadedOnce = true;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasLoadedOnce && isAdded() && getContext() != null) {
            fetchManageScoresData();
        }
    }

    private void setupSubTabListeners() {
        if (tabScoreOverview != null) tabScoreOverview.setOnClickListener(v -> selectSubTab(0));
        if (tabScorePending != null) tabScorePending.setOnClickListener(v -> selectSubTab(1));
        if (tabScoreAssignments != null) tabScoreAssignments.setOnClickListener(v -> selectSubTab(2));
        if (tabScoreQuizzes != null) tabScoreQuizzes.setOnClickListener(v -> selectSubTab(3));
        if (tabScoreExams != null) tabScoreExams.setOnClickListener(v -> selectSubTab(4));
        if (tabScoreGradebook != null) tabScoreGradebook.setOnClickListener(v -> selectSubTab(5));
    }

    public void selectSubTab(int subIndex) {
        this.selectedSubTab = subIndex;
        updateSubTabButtonStyles(subIndex);

        if (secScoreOverviewContainer != null) secScoreOverviewContainer.setVisibility(subIndex == 0 ? View.VISIBLE : View.GONE);
        if (secScorePendingContainer != null) secScorePendingContainer.setVisibility(subIndex == 1 ? View.VISIBLE : View.GONE);
        if (secScoreAssignmentsContainer != null) secScoreAssignmentsContainer.setVisibility(subIndex == 2 ? View.VISIBLE : View.GONE);
        if (secScoreQuizzesContainer != null) secScoreQuizzesContainer.setVisibility(subIndex == 3 ? View.VISIBLE : View.GONE);
        if (secScoreExamsContainer != null) secScoreExamsContainer.setVisibility(subIndex == 4 ? View.VISIBLE : View.GONE);
        if (secScoreGradebookContainer != null) secScoreGradebookContainer.setVisibility(subIndex == 5 ? View.VISIBLE : View.GONE);
    }

    private void updateSubTabButtonStyles(int activeIndex) {
        if (!isAdded() || getContext() == null) return;

        TextView[] tabs = new TextView[]{tabScoreOverview, tabScorePending, tabScoreAssignments, tabScoreQuizzes, tabScoreExams, tabScoreGradebook};
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
    // DATA FETCHING & PARSING
    // ==========================================

    private void fetchManageScoresData() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherAssessmentsOverview(selectedCourseId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");

                    // 1. Course Options
                    if (data.has("courses") && data.get("courses").isJsonArray()) {
                        availableCourses.clear();
                        for (JsonElement cel : data.getAsJsonArray("courses")) {
                            if (!cel.isJsonObject()) continue;
                            JsonObject co = cel.getAsJsonObject();
                            availableCourses.add(new CourseOption(
                                    getInt(co, "course_id"),
                                    getStr(co, "code", "COURSE"),
                                    getStr(co, "name", "Course")
                            ));
                        }
                        if (selectedCourseId == null && tvSelectedCourseName != null) {
                            tvSelectedCourseName.setText("All Assigned Courses");
                        }
                    }

                    // 2. Overview Metrics & Dynamic Progress
                    renderOverviewTab(data);

                    // 3. Raw Submissions for assignment grading lookups
                    rawSubmissionsJson.clear();
                    if (data.has("submissions") && data.get("submissions").isJsonArray()) {
                        for (JsonElement el : data.getAsJsonArray("submissions")) {
                            if (el.isJsonObject()) rawSubmissionsJson.add(el.getAsJsonObject());
                        }
                    }

                    // 4. Pending Scores Items
                    renderPendingScoresTab(data);

                    // 5. Assignments Tab
                    renderAssignmentsTab(data);

                    // 6. Quizzes Tab
                    renderQuizzesTab(data);

                    // 7. Exams Tab
                    renderExamsTab(data);

                    // 8. Gradebook Tab
                    renderGradebookTab(data);

                } else {
                    Toast.makeText(getContext(), "Unable to load score data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading scores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // TAB 0: OVERVIEW RENDERING
    // ==========================================

    private void bindOverviewViews(View root) {
        tvOverviewTotalAssessments = root.findViewById(R.id.tvOverviewTotalAssessments);
        tvOverviewPendingScores = root.findViewById(R.id.tvOverviewPendingScores);
        tvOverviewCompletedGraded = root.findViewById(R.id.tvOverviewCompletedGraded);
        tvOverviewAverageScore = root.findViewById(R.id.tvOverviewAverageScore);
        layoutOverviewCourseProgressContainer = root.findViewById(R.id.layoutOverviewCourseProgressContainer);
        tvOverviewNoProgress = root.findViewById(R.id.tvOverviewNoProgress);
    }

    private void renderOverviewTab(JsonObject data) {
        if (data.has("summary") && data.get("summary").isJsonObject()) {
            JsonObject sum = data.getAsJsonObject("summary");
            int totAsn = getInt(sum, "total_assignments");
            int totQz = getInt(sum, "total_quizzes");
            int totEx = getInt(sum, "total_exams");
            int totalAssessments = sum.has("total_assessments") ? getInt(sum, "total_assessments") : (totAsn + totQz + totEx);
            int pending = sum.has("pending_scores_count") ? getInt(sum, "pending_scores_count") : getInt(sum, "pending_submissions");
            int graded = sum.has("completed_graded_count") ? getInt(sum, "completed_graded_count") : 0;
            double avgScore = sum.has("average_class_score") ? getDouble(sum, "average_class_score") : 84.5;

            if (tvOverviewTotalAssessments != null) tvOverviewTotalAssessments.setText(totalAssessments + " Active");
            if (tvOverviewPendingScores != null) tvOverviewPendingScores.setText(pending + " Needing Grading");
            if (tvOverviewCompletedGraded != null) tvOverviewCompletedGraded.setText(graded + " Graded");
            if (tvOverviewAverageScore != null) tvOverviewAverageScore.setText(String.format(Locale.US, "%.1f%%", avgScore));

            if (tvTotalPendingCount != null) tvTotalPendingCount.setText(String.valueOf(pending));
            if (tvTotalPendingSubtext != null) tvTotalPendingSubtext.setText("out of " + totalAssessments + " assessments");
            if (tvCountAssignments != null) tvCountAssignments.setText(String.valueOf(totAsn));
            if (tvCountQuizzes != null) tvCountQuizzes.setText(String.valueOf(totQz));
            if (tvCountExams != null) tvCountExams.setText(String.valueOf(totEx));
        }

        // Dynamic Course Progress Bars
        if (layoutOverviewCourseProgressContainer != null) {
            layoutOverviewCourseProgressContainer.removeAllViews();
            boolean hasCourseProgress = false;

            if (data.has("course_progress") && data.get("course_progress").isJsonArray()) {
                JsonArray cpArray = data.getAsJsonArray("course_progress");
                if (cpArray.size() > 0) {
                    hasCourseProgress = true;
                    for (JsonElement el : cpArray) {
                        if (!el.isJsonObject()) continue;
                        JsonObject cObj = el.getAsJsonObject();
                        addCourseProgressRow(
                                getStr(cObj, "course_code", "COURSE") + " — " + getStr(cObj, "course_name", "Course"),
                                getStr(cObj, "class_name", "Class"),
                                (int) Math.round(getDouble(cObj, "progress_percent"))
                        );
                    }
                }
            }

            if (tvOverviewNoProgress != null) {
                tvOverviewNoProgress.setVisibility(hasCourseProgress ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void addCourseProgressRow(String title, String subtitle, int percent) {
        if (getContext() == null || layoutOverviewCourseProgressContainer == null) return;

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 24;
        row.setLayoutParams(params);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(title + (subtitle.isEmpty() ? "" : " (" + subtitle + ")"));
        tvTitle.setTextSize(12f);
        tvTitle.setTextColor(getResources().getColor(R.color.admin_text_primary, null));
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvTitle.setLayoutParams(titleParams);

        TextView tvPercent = new TextView(requireContext());
        tvPercent.setText(percent + "% Graded");
        tvPercent.setTextSize(12f);
        tvPercent.setTypeface(null, android.graphics.Typeface.BOLD);
        tvPercent.setTextColor(percent >= 80 ? getResources().getColor(R.color.admin_primary_blue, null) : getResources().getColor(R.color.warning_orange, null));

        header.addView(tvTitle);
        header.addView(tvPercent);

        ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16);
        pbParams.topMargin = 10;
        pb.setLayoutParams(pbParams);
        pb.setMax(100);
        pb.setProgress(percent);
        pb.setProgressTintList(android.content.res.ColorStateList.valueOf(
                percent >= 80 ? getResources().getColor(R.color.admin_primary_blue, null) : getResources().getColor(R.color.warning_orange, null)
        ));

        row.addView(header);
        row.addView(pb);
        layoutOverviewCourseProgressContainer.addView(row);
    }

    // ==========================================
    // TAB 1: PENDING SCORES RENDERING
    // ==========================================

    private void bindPendingScoresViews(View root) {
        tvSelectedCourseName = root.findViewById(R.id.tvSelectedCourseName);
        btnCourseSelector = root.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        tvTotalPendingCount = root.findViewById(R.id.tvTotalPendingCount);
        tvTotalPendingSubtext = root.findViewById(R.id.tvTotalPendingSubtext);
        tvCountAssignments = root.findViewById(R.id.tvCountAssignments);
        tvCountQuizzes = root.findViewById(R.id.tvCountQuizzes);
        tvCountExams = root.findViewById(R.id.tvCountExams);
        tvEmptyPendingScores = root.findViewById(R.id.tvEmptyPendingScores);

        btnFilterAllPending = root.findViewById(R.id.btnFilterAllPending);
        btnFilterAssignments = root.findViewById(R.id.btnFilterAssignments);
        btnFilterQuizzes = root.findViewById(R.id.btnFilterQuizzes);
        btnFilterExams = root.findViewById(R.id.btnFilterExams);
        btnFilterOverdue = root.findViewById(R.id.btnFilterOverdue);

        setupPendingFilterButtons();

        edtSearchPending = root.findViewById(R.id.edtSearchPending);
        if (edtSearchPending != null) {
            edtSearchPending.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchPendingQuery = s.toString();
                    if (pendingAdapter != null) {
                        pendingAdapter.filter(searchPendingQuery, selectedPendingCategory);
                        checkPendingEmptyState();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rvPendingAssessments = root.findViewById(R.id.rvPendingAssessments);
        if (rvPendingAssessments != null && getContext() != null) {
            rvPendingAssessments.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void setupPendingFilterButtons() {
        if (btnFilterAllPending != null) btnFilterAllPending.setOnClickListener(v -> setPendingFilter("All"));
        if (btnFilterAssignments != null) btnFilterAssignments.setOnClickListener(v -> setPendingFilter("Assignments"));
        if (btnFilterQuizzes != null) btnFilterQuizzes.setOnClickListener(v -> setPendingFilter("Quizzes"));
        if (btnFilterExams != null) btnFilterExams.setOnClickListener(v -> setPendingFilter("Exams"));
        if (btnFilterOverdue != null) btnFilterOverdue.setOnClickListener(v -> setPendingFilter("Overdue"));
    }

    private void setPendingFilter(String category) {
        this.selectedPendingCategory = category;
        updatePendingFilterButtonStyles();
        if (pendingAdapter != null) {
            pendingAdapter.filter(searchPendingQuery, selectedPendingCategory);
            checkPendingEmptyState();
        }
    }

    private void updatePendingFilterButtonStyles() {
        if (getContext() == null) return;
        MaterialButton[] buttons = new MaterialButton[]{btnFilterAllPending, btnFilterAssignments, btnFilterQuizzes, btnFilterExams, btnFilterOverdue};
        String[] categories = new String[]{"All", "Assignments", "Quizzes", "Exams", "Overdue"};

        for (int i = 0; i < buttons.length; i++) {
            MaterialButton btn = buttons[i];
            if (btn == null) continue;
            boolean isActive = categories[i].equalsIgnoreCase(selectedPendingCategory);
            if (isActive) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.admin_primary_blue, null)));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                btn.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
                btn.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.admin_border, null)));
                btn.setStrokeWidth(2);
            }
        }
    }

    private void renderPendingScoresTab(JsonObject data) {
        pendingItems.clear();

        if (data.has("pending_assessments") && data.get("pending_assessments").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("pending_assessments")) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                int rawId = getInt(o, "assessment_id");
                int subId = getInt(o, "submission_id");
                pendingItems.add(new PendingAssessmentAdapter.PendingItem(
                        getStr(o, "id", "ASN-" + rawId),
                        rawId,
                        subId,
                        getStr(o, "course_code", "COURSE"),
                        getStr(o, "title", "Assessment"),
                        getStr(o, "type", "Assignment"),
                        getInt(o, "max_score"),
                        getStr(o, "due_date", "-"),
                        getStr(o, "due_status", "Needs Grading"),
                        getBool(o, "is_overdue"),
                        getInt(o, "completed_count"),
                        Math.max(1, getInt(o, "total_students"))
                ));
            }
        }

        if (pendingItems.isEmpty()) {
            // Fallback: build from assignments and exams if pending_assessments not provided
            if (data.has("assignments") && data.get("assignments").isJsonArray()) {
                for (JsonElement el : data.getAsJsonArray("assignments")) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    int pendingSubs = getInt(o, "pending_submissions");
                    if (pendingSubs > 0) {
                        int aId = getInt(o, "id");
                        pendingItems.add(new PendingAssessmentAdapter.PendingItem(
                                "ASN-" + aId,
                                aId,
                                0,
                                getStr(o, "course_code", "COURSE"),
                                getStr(o, "title", "Assignment"),
                                "Assignment",
                                (int) getDouble(o, "total_score"),
                                getStr(o, "due_date", "-"),
                                pendingSubs + " to grade",
                                false,
                                getInt(o, "graded_submissions"),
                                Math.max(1, getInt(o, "total_submissions"))
                        ));
                    }
                }
            }

            if (data.has("exams") && data.get("exams").isJsonArray()) {
                for (JsonElement el : data.getAsJsonArray("exams")) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    boolean isPub = getBool(o, "is_published");
                    int resCount = getInt(o, "results_count");
                    int totCount = Math.max(1, getInt(o, "total_students"));
                    if (!isPub || resCount < totCount) {
                        int exId = getInt(o, "id");
                        pendingItems.add(new PendingAssessmentAdapter.PendingItem(
                                "EX-" + exId,
                                exId,
                                0,
                                getStr(o, "course_code", "COURSE"),
                                getStr(o, "title", "Exam"),
                                "Exam",
                                (int) getDouble(o, "total_score"),
                                getStr(o, "exam_date", "-"),
                                !isPub ? "Unpublished" : (resCount + "/" + totCount + " graded"),
                                false,
                                resCount,
                                totCount
                        ));
                    }
                }
            }
        }

        if (getContext() != null) {
            pendingAdapter = new PendingAssessmentAdapter(requireContext(), pendingItems, this::handlePendingItemAction);
            if (rvPendingAssessments != null) {
                rvPendingAssessments.setAdapter(pendingAdapter);
                pendingAdapter.filter(searchPendingQuery, selectedPendingCategory);
            }
            checkPendingEmptyState();
        }
    }

    private void handlePendingItemAction(PendingAssessmentAdapter.PendingItem item) {
        if (getContext() == null || item == null) return;

        if ("Exam".equalsIgnoreCase(item.type)) {
            // Open Teacher Exam Detail & Manual Paper Score Entry Activity
            Intent intent = new Intent(requireContext(), TeacherExamDetailActivity.class);
            int examId = item.rawId > 0 ? item.rawId : parseNumericId(item.id);
            intent.putExtra("exam_id", examId);
            intent.putExtra("exam_title", item.title);
            intent.putExtra("course_code", item.courseCode);
            startActivity(intent);
        } else if ("Assignment".equalsIgnoreCase(item.type)) {
            // Check if submission ID exists directly
            int subId = item.submissionId;
            if (subId <= 0 && item.rawId > 0) {
                // Find submission for this assignment
                for (JsonObject s : rawSubmissionsJson) {
                    if (getInt(s, "assignment_id") == item.rawId && s.get("score").isJsonNull()) {
                        subId = getInt(s, "id");
                        break;
                    }
                }
            }

            if (subId > 0) {
                Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                intent.putExtra("submission_id_int", subId);
                intent.putExtra("submission_id", String.valueOf(subId));
                intent.putExtra("assessment_title", item.title);
                intent.putExtra("course_code", item.courseCode);
                intent.putExtra("max_score", (double) item.maxPoints);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Open Assignment tab to select student submission", Toast.LENGTH_SHORT).show();
                selectSubTab(2);
            }
        } else if ("Quiz".equalsIgnoreCase(item.type)) {
            int qzId = item.rawId > 0 ? item.rawId : parseNumericId(item.id);
            if (qzId > 0) {
                Intent intent = new Intent(requireContext(), TeacherQuizAttemptsActivity.class);
                intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_ID, qzId);
                intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_TITLE, item.title);
                intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_COURSE_CONTEXT, item.courseCode);
                intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_FILTER_PENDING, true);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Open Quizzes tab to view attempts", Toast.LENGTH_SHORT).show();
                selectSubTab(3);
            }
        }
    }

    private void checkPendingEmptyState() {
        if (tvEmptyPendingScores != null && pendingAdapter != null) {
            tvEmptyPendingScores.setVisibility(pendingAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    // ==========================================
    // TAB 2: ASSIGNMENTS RENDERING
    // ==========================================

    private void bindAssignmentScoresViews(View root) {
        edtSearchAssignments = root.findViewById(R.id.edtSearchAssignments);
        rvAssignmentScoresList = root.findViewById(R.id.rvAssignmentScoresList);
        tvEmptyAssignments = root.findViewById(R.id.tvEmptyAssignments);

        if (rvAssignmentScoresList != null && getContext() != null) {
            rvAssignmentScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (edtSearchAssignments != null) {
            edtSearchAssignments.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (assignmentAdapter != null) {
                        assignmentAdapter.filter(s.toString());
                        if (tvEmptyAssignments != null) {
                            tvEmptyAssignments.setVisibility(assignmentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void renderAssignmentsTab(JsonObject data) {
        assignmentItems.clear();
        rawAssignmentsJson.clear();

        if (data.has("assignments") && data.get("assignments").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("assignments")) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                rawAssignmentsJson.add(o);

                int aId = getInt(o, "id");
                String cCode = getStr(o, "course_code", "COURSE");
                String title = getStr(o, "title", "Assignment");
                String clName = getStr(o, "class_name", "All Classes");
                String due = getStr(o, "due_date", "-");
                double totScore = getDouble(o, "total_score");
                int totSubs = getInt(o, "total_submissions");
                int gradedSubs = getInt(o, "graded_submissions");
                String status = getStr(o, "status", "Published");

                String details = String.format(Locale.US, "Due: %s • Max: %.0f pts • Submissions: %d (%d Graded)",
                        due, totScore, totSubs, gradedSubs);

                assignmentItems.add(new AssignmentAdapter.AssignmentCardItem(
                        String.valueOf(aId), cCode, title, clName, details, status
                ));
            }
        }

        if (getContext() != null) {
            assignmentAdapter = new AssignmentAdapter(requireContext(), assignmentItems, new AssignmentAdapter.OnAssignmentActionClickListener() {
                @Override
                public void onGradeClick(AssignmentAdapter.AssignmentCardItem item) {
                    handleAssignmentGradingClick(item);
                }

                @Override
                public void onDeleteClick(AssignmentAdapter.AssignmentCardItem item) {
                    // Score management only manages scores; delete is handled in Assessment Fragment
                    Toast.makeText(getContext(), "Assignment management is available in Assessments module", Toast.LENGTH_SHORT).show();
                }
            });
            if (rvAssignmentScoresList != null) {
                rvAssignmentScoresList.setAdapter(assignmentAdapter);
            }
            if (tvEmptyAssignments != null) {
                tvEmptyAssignments.setVisibility(assignmentItems.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void handleAssignmentGradingClick(AssignmentAdapter.AssignmentCardItem item) {
        if (getContext() == null || item == null) return;
        int aId = parseNumericId(item.id);

        // Find submissions matching this assignment
        List<JsonObject> matchingSubs = new ArrayList<>();
        for (JsonObject s : rawSubmissionsJson) {
            if (getInt(s, "assignment_id") == aId) {
                matchingSubs.add(s);
            }
        }

        if (matchingSubs.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(item.title)
                    .setMessage("No student submissions have been turned in for this assignment yet.\n\nOnce students submit their work, you can review and grade them here.")
                    .setPositiveButton(R.string.close, (d, w) -> d.dismiss())
                    .show();
        } else if (matchingSubs.size() == 1) {
            JsonObject sub = matchingSubs.get(0);
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("submission_id_int", getInt(sub, "id"));
            intent.putExtra("submission_id", String.valueOf(getInt(sub, "id")));
            intent.putExtra("assessment_title", item.title);
            intent.putExtra("course_code", item.courseCode);
            intent.putExtra("student_name", getStr(sub, "student_name", "Student"));
            intent.putExtra("student_code", getStr(sub, "student_code", "STU"));
            intent.putExtra("max_score", getDouble(sub, "total_score"));
            startActivity(intent);
        } else {
            // Show chooser dialog of student submissions
            String[] studentNames = new String[matchingSubs.size()];
            for (int i = 0; i < matchingSubs.size(); i++) {
                JsonObject s = matchingSubs.get(i);
                String stName = getStr(s, "student_name", "Student");
                String scoreText = s.get("score").isJsonNull() ? "Pending" : (getInt(s, "score") + " pts");
                studentNames[i] = stName + " (" + scoreText + ")";
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select Student Submission to Grade")
                    .setItems(studentNames, (dialog, which) -> {
                        JsonObject chosenSub = matchingSubs.get(which);
                        Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
                        intent.putExtra("submission_id_int", getInt(chosenSub, "id"));
                        intent.putExtra("submission_id", String.valueOf(getInt(chosenSub, "id")));
                        intent.putExtra("assessment_title", item.title);
                        intent.putExtra("course_code", item.courseCode);
                        intent.putExtra("student_name", getStr(chosenSub, "student_name", "Student"));
                        intent.putExtra("student_code", getStr(chosenSub, "student_code", "STU"));
                        intent.putExtra("max_score", getDouble(chosenSub, "total_score"));
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    // ==========================================
    // TAB 3: QUIZZES RENDERING
    // ==========================================

    private void bindQuizScoresViews(View root) {
        edtSearchQuizzes = root.findViewById(R.id.edtSearchQuizzes);
        rvQuizScoresList = root.findViewById(R.id.rvQuizScoresList);
        tvEmptyQuizzes = root.findViewById(R.id.tvEmptyQuizzes);

        if (rvQuizScoresList != null && getContext() != null) {
            rvQuizScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (edtSearchQuizzes != null) {
            edtSearchQuizzes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (quizAdapter != null) {
                        quizAdapter.filter(s.toString());
                        if (tvEmptyQuizzes != null) {
                            tvEmptyQuizzes.setVisibility(quizAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void renderQuizzesTab(JsonObject data) {
        quizItems.clear();
        rawQuizzesJson.clear();

        if (data.has("quizzes") && data.get("quizzes").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("quizzes")) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                rawQuizzesJson.add(o);

                int qId = getInt(o, "id");
                String cCode = getStr(o, "course_code", "COURSE");
                String title = getStr(o, "title", "Quiz");
                String clName = getStr(o, "class_name", "All Classes");
                int dur = getInt(o, "duration_minutes");
                int qCount = getInt(o, "questions_count");
                double totScore = getDouble(o, "total_marks");
                if (totScore <= 0) totScore = getDouble(o, "total_score");
                int attCount = getInt(o, "attempts_count");
                int pendingGrading = getInt(o, "pending_grading_count");
                String status = getStr(o, "status", "Published");

                String details = String.format(Locale.US, "Duration: %d min • Questions: %d • Max: %.0f pts • %d Attempts%s",
                        dur, qCount, totScore, attCount,
                        pendingGrading > 0 ? (" • " + pendingGrading + " Pending Grading") : "");

                quizItems.add(new QuizAdapter.QuizCardItem(
                        String.valueOf(qId), cCode, title, clName, details, status, attCount, pendingGrading
                ));
            }
        }

        if (getContext() != null) {
            quizAdapter = new QuizAdapter(requireContext(), quizItems, new QuizAdapter.OnQuizActionClickListener() {
                @Override
                public void onBuilderClick(QuizAdapter.QuizCardItem item) {
                    showQuizDetailsDialog(item);
                }

                @Override
                public void onPublishClick(QuizAdapter.QuizCardItem item) {
                    showQuizDetailsDialog(item);
                }

                @Override
                public void onAttemptsClick(QuizAdapter.QuizCardItem item) {
                    openQuizAttempts(item, item.pendingGradingCount > 0);
                }
            });
            if (rvQuizScoresList != null) {
                rvQuizScoresList.setAdapter(quizAdapter);
            }
            if (tvEmptyQuizzes != null) {
                tvEmptyQuizzes.setVisibility(quizItems.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void openQuizAttempts(QuizAdapter.QuizCardItem item, boolean filterPending) {
        if (getContext() == null || item == null) return;
        int qzId = parseNumericId(item.id);
        if (qzId > 0) {
            Intent intent = new Intent(requireContext(), TeacherQuizAttemptsActivity.class);
            intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_ID, qzId);
            intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_QUIZ_TITLE, item.title);
            intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_COURSE_CONTEXT, item.courseCode + " • " + item.classCohort);
            intent.putExtra(TeacherQuizAttemptsActivity.EXTRA_FILTER_PENDING, filterPending);
            startActivity(intent);
        }
    }

    private void showQuizDetailsDialog(QuizAdapter.QuizCardItem item) {
        if (getContext() == null || item == null) return;
        int qzId = parseNumericId(item.id);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.title)
                .setMessage("Course: " + item.courseCode + " • " + item.classCohort + "\n\n" +
                        item.details + "\nStatus: " + item.status + "\n\n" +
                        "Review attempts & manually grade Short Answers, or manage quiz settings and student overrides.")
                .setPositiveButton("Grade / Review Attempts", (d, w) -> openQuizAttempts(item, item.pendingGradingCount > 0))
                .setNeutralButton("Open Quiz Builder", (d, w) -> {
                    if (qzId > 0) {
                        Intent intent = new Intent(requireContext(), QuizBuilderActivity.class);
                        intent.putExtra("quiz_id", qzId);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.close, (d, w) -> d.dismiss())
                .show();
    }

    // ==========================================
    // TAB 4: EXAMS RENDERING
    // ==========================================

    private void bindExamScoresViews(View root) {
        edtSearchExams = root.findViewById(R.id.edtSearchExams);
        rvExamScoresList = root.findViewById(R.id.rvExamScoresList);
        tvEmptyExams = root.findViewById(R.id.tvEmptyExams);

        if (rvExamScoresList != null && getContext() != null) {
            rvExamScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (edtSearchExams != null) {
            edtSearchExams.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (examAdapter != null) {
                        examAdapter.filter(s.toString());
                        if (tvEmptyExams != null) {
                            tvEmptyExams.setVisibility(examAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void renderExamsTab(JsonObject data) {
        examItems.clear();

        if (data.has("exams") && data.get("exams").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("exams")) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();

                int exId = getInt(o, "id");
                String cCode = getStr(o, "course_code", "COURSE");
                String title = getStr(o, "title", "Exam");
                String examType = getStr(o, "exam_type", "Final");
                String clName = getStr(o, "class_name", "All Classes");
                String examDate = getStr(o, "exam_date", "-");
                double totScore = getDouble(o, "total_score");
                int resCount = getInt(o, "results_count");
                int totStudents = Math.max(1, getInt(o, "total_students"));
                boolean isPub = getBool(o, "is_published");

                String details = String.format(Locale.US, "Date: %s • Max: %.0f pts • Results: %d/%d graded • %s",
                        examDate, totScore, resCount, totStudents, isPub ? "Results Published" : "Unpublished");

                examItems.add(new TeacherExamAdapter.ExamCardItem(
                        exId, cCode, title, clName + " • " + examType, details,
                        isPub ? "Published" : "Pending", isPub
                ));
            }
        }

        if (getContext() != null) {
            examAdapter = new TeacherExamAdapter(requireContext(), examItems, new TeacherExamAdapter.OnExamActionListener() {
                @Override
                public void onGradeClick(TeacherExamAdapter.ExamCardItem item) {
                    Intent intent = new Intent(requireContext(), TeacherExamDetailActivity.class);
                    intent.putExtra("exam_id", item.id);
                    intent.putExtra("exam_title", item.title);
                    intent.putExtra("course_code", item.courseCode);
                    startActivity(intent);
                }

                @Override
                public void onDeleteClick(TeacherExamAdapter.ExamCardItem item) {
                    Toast.makeText(getContext(), "Exam scheduling is managed in Assessments module", Toast.LENGTH_SHORT).show();
                }
            });
            if (rvExamScoresList != null) {
                rvExamScoresList.setAdapter(examAdapter);
            }
            if (tvEmptyExams != null) {
                tvEmptyExams.setVisibility(examItems.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    // ==========================================
    // TAB 5: GRADEBOOK RENDERING
    // ==========================================

    private void bindGradebookViews(View root) {
        edtSearchGradebook = root.findViewById(R.id.edtSearchGradebook);
        rvGradebookRosterList = root.findViewById(R.id.rvGradebookRosterList);
        tvEmptyGradebook = root.findViewById(R.id.tvEmptyGradebook);

        if (rvGradebookRosterList != null && getContext() != null) {
            rvGradebookRosterList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (edtSearchGradebook != null) {
            edtSearchGradebook.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (gradebookAdapter != null) {
                        gradebookAdapter.filter(s.toString());
                        if (tvEmptyGradebook != null) {
                            tvEmptyGradebook.setVisibility(gradebookAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void renderGradebookTab(JsonObject data) {
        gradebookList.clear();

        if (data.has("gradebook") && data.get("gradebook").isJsonArray()) {
            for (JsonElement el : data.getAsJsonArray("gradebook")) {
                if (!el.isJsonObject()) continue;
                JsonObject g = el.getAsJsonObject();

                String stCode = getStr(g, "student_code", "STU");
                String stName = getStr(g, "student_name", "Student");
                String cCode = getStr(g, "course_code", "COURSE");
                String clName = getStr(g, "class_name", "Class");

                String courseClass = cCode + " • " + clName;
                String breakdown = String.format(Locale.US,
                        "Assignments: %.0f%% • Quizzes: %.0f%% • Midterm: %.0f%% • Final: %.0f%%",
                        getDouble(g, "assignment_score"),
                        getDouble(g, "quiz_score"),
                        getDouble(g, "midterm_score"),
                        getDouble(g, "final_exam_score"));
                String badge = String.format(Locale.US, "Grade: %s (%.1f%%)",
                        getStr(g, "letter_grade", "N/A"),
                        getDouble(g, "total_score"));

                GradebookAdapter.GradebookItem gbItem = new GradebookAdapter.GradebookItem(stCode, stName, courseClass, breakdown, badge);
                gbItem.numericStudentId = getInt(g, "student_id");
                gradebookList.add(gbItem);
            }
        }

        if (getContext() != null) {
            gradebookAdapter = new GradebookAdapter(requireContext(), gradebookList, item -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Student Gradebook Record")
                        .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Course & Class: " + item.courseClass + "\n\n" +
                                "Academic Component Breakdown:\n" +
                                item.gradeBreakdown + "\n\n" +
                                "Composite Result: " + item.gradeBadge)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
                if (item.numericStudentId > 0) {
                    builder.setNeutralButton("View Full Student Profile", (dialog, which) -> {
                        Intent intent = new Intent(requireContext(), com.social7.studentlearnanalytics.teacher.student.StudentDetailActivity.class);
                        intent.putExtra("student_id", item.numericStudentId);
                        intent.putExtra("student_code", item.studentId);
                        intent.putExtra("student_name", item.studentName);
                        startActivity(intent);
                    });
                }
                builder.show();
            });
            if (rvGradebookRosterList != null) {
                rvGradebookRosterList.setAdapter(gradebookAdapter);
            }
            if (tvEmptyGradebook != null) {
                tvEmptyGradebook.setVisibility(gradebookList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    // ==========================================
    // COURSE DROPDOWN SELECTOR
    // ==========================================

    private void showCourseDropdown(View anchor) {
        if (getContext() == null) return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "All Assigned Courses");
        for (int i = 0; i < availableCourses.size(); i++) {
            CourseOption opt = availableCourses.get(i);
            popup.getMenu().add(0, i + 1, i + 1, opt.code + " - " + opt.name);
        }

        popup.setOnMenuItemClickListener(item -> {
            int idx = item.getItemId();
            if (idx == 0) {
                selectedCourseId = null;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText("All Assigned Courses");
            } else if (idx - 1 < availableCourses.size()) {
                CourseOption opt = availableCourses.get(idx - 1);
                selectedCourseId = opt.id;
                if (tvSelectedCourseName != null) tvSelectedCourseName.setText(opt.code + " - " + opt.name);
            }
            fetchManageScoresData();
            return true;
        });
        popup.show();
    }

    // ==========================================
    // UTILITY PARSERS
    // ==========================================

    private int parseNumericId(String idStr) {
        if (idStr == null) return 0;
        try {
            return Integer.parseInt(idStr.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return 0.0;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean getBool(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return false;
        try {
            return obj.get(key).getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }
}
