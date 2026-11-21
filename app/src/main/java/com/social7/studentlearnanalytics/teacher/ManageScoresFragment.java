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
import com.social7.studentlearnanalytics.teacher.assessment.EnterScoreActivity;
import com.social7.studentlearnanalytics.teacher.assessment.PendingAssessmentAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageScoresFragment extends Fragment {

    private TextView tabScoreOverview, tabScorePending, tabScoreAssignments, tabScoreQuizzes, tabScoreExams, tabScoreGradebook;
    private View secScoreOverviewContainer, secScorePendingContainer, secScoreAssignmentsContainer, secScoreQuizzesContainer, secScoreExamsContainer, secScoreGradebookContainer;

    private PendingAssessmentAdapter pendingAdapter;
    private RecyclerView rvPendingAssessments;
    private TextView tvSelectedCourseName, tvTotalPendingCount, tvTotalPendingSubtext, tvCountAssignments, tvCountQuizzes, tvCountExams;
    private MaterialButton btnFilterAllPending, btnFilterAssignments, btnFilterQuizzes, btnFilterExams, btnFilterOverdue;
    private String selectedCategory = "All";
    private String searchQuery = "";
    private Integer selectedCourseId = null;

    private RecyclerView rvAssignmentScoresList;
    private PendingAssessmentAdapter assignmentAdapter;
    private final List<PendingAssessmentAdapter.PendingItem> assignmentItems = new ArrayList<>();

    private RecyclerView rvQuizScoresList;
    private PendingAssessmentAdapter quizAdapter;
    private final List<PendingAssessmentAdapter.PendingItem> quizItems = new ArrayList<>();

    private RecyclerView rvExamScoresList;
    private PendingAssessmentAdapter examAdapter;
    private final List<PendingAssessmentAdapter.PendingItem> examItems = new ArrayList<>();

    private final List<PendingAssessmentAdapter.PendingItem> pendingItems = new ArrayList<>();

    private RecyclerView rvGradebookRosterList;
    private GradebookAdapter gradebookAdapter;
    private final List<GradebookAdapter.GradebookItem> gradebookList = new ArrayList<>();

    private final List<CourseOption> availableCourses = new ArrayList<>();
    private int selectedSubTab = 0;

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

        bindPendingScoresViews(view);
        bindAssignmentScoresViews(view);
        bindQuizScoresViews(view);
        bindExamScoresViews(view);
        bindGradebookViews(view);

        setupSubTabListeners();
        selectSubTab(selectedSubTab);
        fetchManageScoresData();

        return view;
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

    private void fetchManageScoresData() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getTeacherAssessmentsOverview(selectedCourseId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");

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

                    if (data.has("summary") && data.get("summary").isJsonObject()) {
                        JsonObject sum = data.getAsJsonObject("summary");
                        int totAsn = getInt(sum, "total_assignments");
                        int totQz = getInt(sum, "total_quizzes");
                        int totEx = getInt(sum, "total_exams");
                        int pend = getInt(sum, "pending_submissions");
                        if (tvTotalPendingCount != null) tvTotalPendingCount.setText(String.valueOf(pend));
                        if (tvTotalPendingSubtext != null) tvTotalPendingSubtext.setText("out of " + (totAsn + totQz + totEx) + " assessments");
                        if (tvCountAssignments != null) tvCountAssignments.setText(String.valueOf(totAsn));
                        if (tvCountQuizzes != null) tvCountQuizzes.setText(String.valueOf(totQz));
                        if (tvCountExams != null) tvCountExams.setText(String.valueOf(totEx));
                    }

                    pendingItems.clear();
                    assignmentItems.clear();
                    quizItems.clear();
                    examItems.clear();

                    if (data.has("assignments") && data.get("assignments").isJsonArray()) {
                        for (JsonElement el : data.getAsJsonArray("assignments")) {
                            if (!el.isJsonObject()) continue;
                            JsonObject o = el.getAsJsonObject();
                            PendingAssessmentAdapter.PendingItem pi = new PendingAssessmentAdapter.PendingItem(
                                    getStr(o, "code", "ASN"),
                                    getStr(o, "course_code", "COURSE"),
                                    getStr(o, "title", "Assignment"),
                                    "Assignment",
                                    (int) getDouble(o, "total_score"),
                                    getStr(o, "due_date", "-"),
                                    getStr(o, "status", "Published"),
                                    false,
                                    getInt(o, "graded_submissions"),
                                    Math.max(1, getInt(o, "total_submissions"))
                            );
                            assignmentItems.add(pi);
                            pendingItems.add(pi);
                        }
                    }

                    if (data.has("quizzes") && data.get("quizzes").isJsonArray()) {
                        for (JsonElement el : data.getAsJsonArray("quizzes")) {
                            if (!el.isJsonObject()) continue;
                            JsonObject o = el.getAsJsonObject();
                            PendingAssessmentAdapter.PendingItem pi = new PendingAssessmentAdapter.PendingItem(
                                    getStr(o, "code", "QZ"),
                                    getStr(o, "course_code", "COURSE"),
                                    getStr(o, "title", "Quiz"),
                                    "Quiz",
                                    (int) getDouble(o, "total_score"),
                                    "-",
                                    getStr(o, "status", "Published"),
                                    false,
                                    getInt(o, "attempts_count"),
                                    Math.max(1, getInt(o, "attempts_count"))
                            );
                            quizItems.add(pi);
                            pendingItems.add(pi);
                        }
                    }

                    if (data.has("exams") && data.get("exams").isJsonArray()) {
                        for (JsonElement el : data.getAsJsonArray("exams")) {
                            if (!el.isJsonObject()) continue;
                            JsonObject o = el.getAsJsonObject();
                            PendingAssessmentAdapter.PendingItem pi = new PendingAssessmentAdapter.PendingItem(
                                    getStr(o, "code", "EX"),
                                    getStr(o, "course_code", "COURSE"),
                                    getStr(o, "title", "Exam"),
                                    "Exam",
                                    (int) getDouble(o, "total_score"),
                                    getStr(o, "exam_date", "-"),
                                    "Scheduled",
                                    false,
                                    getInt(o, "results_count"),
                                    Math.max(1, getInt(o, "results_count"))
                            );
                            examItems.add(pi);
                            pendingItems.add(pi);
                        }
                    }

                    gradebookList.clear();
                    if (data.has("gradebook") && data.get("gradebook").isJsonArray()) {
                        for (JsonElement el : data.getAsJsonArray("gradebook")) {
                            if (!el.isJsonObject()) continue;
                            JsonObject g = el.getAsJsonObject();
                            String stCode = getStr(g, "student_code", "STU");
                            String stName = getStr(g, "student_name", "Student");
                            String cCode = getStr(g, "course_code", "COURSE");
                            String breakdown = String.format(Locale.US,
                                    "Assignments: %.0f%% • Quizzes: %.0f%% • Midterm: %.0f%% • Final: %.0f%%",
                                    getDouble(g, "assignment_score"),
                                    getDouble(g, "quiz_score"),
                                    getDouble(g, "midterm_score"),
                                    getDouble(g, "final_exam_score"));
                            String badge = String.format(Locale.US, "Grade: %s (%.1f%%)",
                                    getStr(g, "letter_grade", "N/A"),
                                    getDouble(g, "total_score"));
                            gradebookList.add(new GradebookAdapter.GradebookItem(stCode, stName, cCode, breakdown, badge));
                        }
                    }

                    refreshAdapters();
                } else {
                    Toast.makeText(getContext(), "Unable to load scores data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading scores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAdapters() {
        if (getContext() == null) return;
        PendingAssessmentAdapter.OnPendingAssessmentClickListener listener = item -> {
            Intent intent = new Intent(requireContext(), EnterScoreActivity.class);
            intent.putExtra("assessment_id", item.id);
            intent.putExtra("assessment_title", item.title);
            intent.putExtra("course_code", item.courseCode);
            startActivity(intent);
        };
        if (rvPendingAssessments != null) {
            pendingAdapter = new PendingAssessmentAdapter(requireContext(), pendingItems, listener);
            rvPendingAssessments.setAdapter(pendingAdapter);
            pendingAdapter.filter(searchQuery, selectedCategory);
        }
        if (rvAssignmentScoresList != null) {
            assignmentAdapter = new PendingAssessmentAdapter(requireContext(), assignmentItems, listener);
            rvAssignmentScoresList.setAdapter(assignmentAdapter);
        }
        if (rvQuizScoresList != null) {
            quizAdapter = new PendingAssessmentAdapter(requireContext(), quizItems, listener);
            rvQuizScoresList.setAdapter(quizAdapter);
        }
        if (rvExamScoresList != null) {
            examAdapter = new PendingAssessmentAdapter(requireContext(), examItems, listener);
            rvExamScoresList.setAdapter(examAdapter);
        }
        if (gradebookAdapter != null) {
            gradebookAdapter.notifyDataSetChanged();
        }
    }

    private void bindPendingScoresViews(View root) {
        tvSelectedCourseName = root.findViewById(R.id.tvSelectedCourseName);
        LinearLayout btnCourseSelector = root.findViewById(R.id.btnCourseSelector);
        if (btnCourseSelector != null) {
            btnCourseSelector.setOnClickListener(this::showCourseDropdown);
        }

        tvTotalPendingCount = root.findViewById(R.id.tvTotalPendingCount);
        tvTotalPendingSubtext = root.findViewById(R.id.tvTotalPendingSubtext);
        tvCountAssignments = root.findViewById(R.id.tvCountAssignments);
        tvCountQuizzes = root.findViewById(R.id.tvCountQuizzes);
        tvCountExams = root.findViewById(R.id.tvCountExams);

        EditText edtSearch = root.findViewById(R.id.edtSearchPending);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString();
                    if (pendingAdapter != null) {
                        pendingAdapter.filter(searchQuery, selectedCategory);
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

    private void bindAssignmentScoresViews(View root) {
        rvAssignmentScoresList = root.findViewById(R.id.rvAssignmentScoresList);
        if (rvAssignmentScoresList != null && getContext() != null) {
            rvAssignmentScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void bindQuizScoresViews(View root) {
        rvQuizScoresList = root.findViewById(R.id.rvQuizScoresList);
        if (rvQuizScoresList != null && getContext() != null) {
            rvQuizScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void bindExamScoresViews(View root) {
        rvExamScoresList = root.findViewById(R.id.rvExamScoresList);
        if (rvExamScoresList != null && getContext() != null) {
            rvExamScoresList.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void bindGradebookViews(View root) {
        rvGradebookRosterList = root.findViewById(R.id.rvGradebookRosterList);
        if (rvGradebookRosterList != null && getContext() != null) {
            gradebookAdapter = new GradebookAdapter(requireContext(), gradebookList, item -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Student Gradebook Breakdown")
                        .setMessage(getString(R.string.student_id_a8dd9e) + item.studentId + "\n" +
                                "Student Name: " + item.studentName + "\n" +
                                "Course & Class: " + item.courseClass + "\n\n" +
                                "Academic Score Breakdown:\n" +
                                item.gradeBreakdown + "\n\n" +
                                "Composite Final Result: " + item.gradeBadge)
                        .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                        .show();
            });
            rvGradebookRosterList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvGradebookRosterList.setAdapter(gradebookAdapter);
        }
    }

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
}
