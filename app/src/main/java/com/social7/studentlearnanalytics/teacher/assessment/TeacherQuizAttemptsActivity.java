package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays all student attempts for a quiz with filters (All / Pending Grading / Graded),
 * ungraded answer counts, and direct navigation into manual Short Answer grading.
 */
public class TeacherQuizAttemptsActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_QUIZ_TITLE = "quiz_title";
    public static final String EXTRA_COURSE_CONTEXT = "course_context";
    public static final String EXTRA_FILTER_PENDING = "filter_pending";

    private int quizId = 0;
    private String quizTitle = "Quiz Attempts";
    private String courseContext = "";
    private String selectedFilter = "All"; // All | Pending | Graded
    private String searchQuery = "";
    private boolean hasLoadedOnce = false;

    private TextView tvQuizTitle, tvCourseContext, tvCountsSummary;
    private MaterialButton btnFilterAll, btnFilterPending, btnFilterGraded, btnRetry;
    private TextInputEditText edtSearch;
    private RecyclerView rvAttempts;
    private ProgressBar pbLoading;
    private LinearLayout layoutState;
    private TextView tvStateTitle, tvStateMsg;

    private final List<AttemptRow> allRows = new ArrayList<>();
    private final List<AttemptRow> filteredRows = new ArrayList<>();
    private AttemptsAdapter adapter;

    static class AttemptRow {
        int id;
        int studentId;
        String studentCode;
        String studentName;
        int attemptNumber;
        String rawStatus;
        String rawGradingStatus;
        String displayGradingStatus;
        Double score;
        double autoGradedScore;
        double manualGradedScore;
        double totalMarks;
        String submittedAt;
        boolean isLate;
        boolean isReset;
        boolean pendingManualGrading;
        int pendingQuestionsCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_quiz_attempts);

        if (getIntent() != null) {
            quizId = getIntent().getIntExtra(EXTRA_QUIZ_ID, 0);
            String t = getIntent().getStringExtra(EXTRA_QUIZ_TITLE);
            if (t != null && !t.trim().isEmpty()) quizTitle = t.trim();
            String c = getIntent().getStringExtra(EXTRA_COURSE_CONTEXT);
            if (c != null) courseContext = c.trim();
            if (getIntent().getBooleanExtra(EXTRA_FILTER_PENDING, false)) {
                selectedFilter = "Pending";
            }
        }
        if (quizId <= 0) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherQuizAttempts),
                "Quiz Attempts & Grading", quizTitle, true);

        tvQuizTitle = findViewById(R.id.tvAttemptsQuizTitle);
        tvCourseContext = findViewById(R.id.tvAttemptsCourseContext);
        tvCountsSummary = findViewById(R.id.tvAttemptsCountsSummary);
        btnFilterAll = findViewById(R.id.btnFilterAttemptsAll);
        btnFilterPending = findViewById(R.id.btnFilterAttemptsPending);
        btnFilterGraded = findViewById(R.id.btnFilterAttemptsGraded);
        edtSearch = findViewById(R.id.edtSearchQuizAttempts);
        rvAttempts = findViewById(R.id.rvQuizAttempts);
        pbLoading = findViewById(R.id.pbAttemptsLoading);
        layoutState = findViewById(R.id.layoutAttemptsState);
        tvStateTitle = findViewById(R.id.tvAttemptsStateTitle);
        tvStateMsg = findViewById(R.id.tvAttemptsStateMsg);
        btnRetry = findViewById(R.id.btnRetryAttempts);

        tvQuizTitle.setText(quizTitle);
        if (!courseContext.isEmpty()) {
            tvCourseContext.setVisibility(View.VISIBLE);
            tvCourseContext.setText(courseContext);
        }

        adapter = new AttemptsAdapter();
        rvAttempts.setLayoutManager(new LinearLayoutManager(this));
        rvAttempts.setAdapter(adapter);

        btnFilterAll.setOnClickListener(v -> setFilter("All"));
        btnFilterPending.setOnClickListener(v -> setFilter("Pending"));
        btnFilterGraded.setOnClickListener(v -> setFilter("Graded"));
        btnRetry.setOnClickListener(v -> fetchAttempts());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim().toLowerCase(Locale.US) : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        updateFilterButtonStyles();
        fetchAttempts();
        hasLoadedOnce = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLoadedOnce) {
            fetchAttempts();
        }
    }

    private void setFilter(String filter) {
        selectedFilter = filter;
        updateFilterButtonStyles();
        applyFilters();
    }

    private void updateFilterButtonStyles() {
        MaterialButton[] btns = new MaterialButton[]{btnFilterAll, btnFilterPending, btnFilterGraded};
        String[] keys = new String[]{"All", "Pending", "Graded"};
        for (int i = 0; i < btns.length; i++) {
            MaterialButton b = btns[i];
            boolean active = keys[i].equalsIgnoreCase(selectedFilter);
            if (active) {
                b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.admin_primary_blue, null)));
                b.setTextColor(Color.WHITE);
                b.setStrokeWidth(0);
            } else {
                b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                b.setTextColor(getResources().getColor(R.color.admin_text_secondary, null));
                b.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.admin_border, null)));
                b.setStrokeWidth(2);
            }
        }
    }

    private void fetchAttempts() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutState.setVisibility(View.GONE);
        rvAttempts.setVisibility(View.GONE);

        ApiClient.getApiService(this).getTeacherQuizAttempts(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    showState("Could not load attempts",
                            AssessmentUiHelper.extractErrorMessage(response, "Unable to fetch quiz attempts."),
                            true);
                    return;
                }

                allRows.clear();
                JsonArray arr = AssessmentUiHelper.extractDataArray(response.body());
                int pendingCount = 0;
                int partialCount = 0;
                int gradedCount = 0;

                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    AttemptRow row = new AttemptRow();
                    row.id = AssessmentUiHelper.optInt(o, "id", 0);
                    row.studentId = AssessmentUiHelper.optInt(o, "student_id", 0);
                    row.studentCode = AssessmentUiHelper.optString(o, "student_code", "STU");
                    row.studentName = AssessmentUiHelper.optString(o, "student_name", "Student");
                    row.attemptNumber = AssessmentUiHelper.optInt(o, "attempt_number", 1);
                    row.rawStatus = AssessmentUiHelper.optString(o, "status", "");
                    row.rawGradingStatus = AssessmentUiHelper.optString(o, "grading_status", "");
                    row.score = (o.has("score") && !o.get("score").isJsonNull()) ? o.get("score").getAsDouble() : null;
                    row.autoGradedScore = AssessmentUiHelper.optDouble(o, "auto_graded_score", 0.0);
                    row.manualGradedScore = AssessmentUiHelper.optDouble(o, "manual_graded_score", 0.0);
                    row.totalMarks = AssessmentUiHelper.optDouble(o, "total_marks", 0.0);
                    row.submittedAt = AssessmentUiHelper.optString(o, "submitted_at", "");
                    row.isLate = AssessmentUiHelper.optBoolean(o, "is_late", false);
                    row.isReset = AssessmentUiHelper.optBoolean(o, "is_reset", false);
                    row.pendingManualGrading = AssessmentUiHelper.optBoolean(o, "pending_manual_grading", false);
                    row.pendingQuestionsCount = AssessmentUiHelper.optInt(o, "pending_questions_count", 0);

                    row.displayGradingStatus = QuizScoreUiHelper.resolveAttemptGradingStatus(
                            row.rawGradingStatus,
                            row.rawStatus,
                            row.pendingManualGrading,
                            row.pendingQuestionsCount,
                            0,
                            row.manualGradedScore
                    );

                    if (!row.isReset) {
                        if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(row.displayGradingStatus)) {
                            partialCount++;
                        } else if (QuizScoreUiHelper.STATUS_PENDING_GRADING.equalsIgnoreCase(row.displayGradingStatus)) {
                            pendingCount++;
                        } else if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(row.displayGradingStatus)) {
                            gradedCount++;
                        }
                    }

                    allRows.add(row);
                }

                // Put Pending / Partially Graded attempts first so teachers can grade immediately
                Collections.sort(allRows, (a, b) -> {
                    int rankA = statusSortRank(a);
                    int rankB = statusSortRank(b);
                    if (rankA != rankB) return Integer.compare(rankA, rankB);
                    return Integer.compare(b.id, a.id);
                });

                tvCountsSummary.setText(String.format(Locale.US,
                        "Total: %d • Pending: %d • Partially Graded: %d • Graded: %d",
                        allRows.size(), pendingCount, partialCount, gradedCount));

                // If user opened Pending filter but all attempts are now graded, switch to All cleanly
                if ("Pending".equalsIgnoreCase(selectedFilter) && pendingCount == 0 && partialCount == 0 && !allRows.isEmpty()) {
                    selectedFilter = "All";
                    updateFilterButtonStyles();
                }

                applyFilters();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                showState("Network error", "Check your connection and retry.", true);
            }
        });
    }

    private int statusSortRank(AttemptRow r) {
        if (r.isReset) return 4;
        if (QuizScoreUiHelper.STATUS_PENDING_GRADING.equalsIgnoreCase(r.displayGradingStatus)) return 0;
        if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(r.displayGradingStatus)) return 1;
        if (QuizScoreUiHelper.STATUS_IN_PROGRESS.equalsIgnoreCase(r.displayGradingStatus)) return 2;
        return 3;
    }

    private void applyFilters() {
        filteredRows.clear();
        for (AttemptRow row : allRows) {
            boolean isPendingGroup = QuizScoreUiHelper.STATUS_PENDING_GRADING.equalsIgnoreCase(row.displayGradingStatus)
                    || QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(row.displayGradingStatus);
            boolean isGradedGroup = QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(row.displayGradingStatus);

            if ("Pending".equalsIgnoreCase(selectedFilter) && (!isPendingGroup || row.isReset)) continue;
            if ("Graded".equalsIgnoreCase(selectedFilter) && (!isGradedGroup || row.isReset)) continue;

            if (!searchQuery.isEmpty()) {
                String name = row.studentName != null ? row.studentName.toLowerCase(Locale.US) : "";
                String code = row.studentCode != null ? row.studentCode.toLowerCase(Locale.US) : "";
                if (!name.contains(searchQuery) && !code.contains(searchQuery)) continue;
            }
            filteredRows.add(row);
        }

        adapter.notifyDataSetChanged();
        if (filteredRows.isEmpty()) {
            if (allRows.isEmpty()) {
                showState("No student attempts yet", "Submitted student attempts for this quiz will appear here.", false);
            } else {
                showState("No matching attempts", "Try switching the filter or clearing your search.", false);
            }
        } else {
            layoutState.setVisibility(View.GONE);
            rvAttempts.setVisibility(View.VISIBLE);
        }
    }

    private void showState(String title, String msg, boolean allowRetry) {
        rvAttempts.setVisibility(View.GONE);
        layoutState.setVisibility(View.VISIBLE);
        tvStateTitle.setText(title);
        tvStateMsg.setText(msg);
        btnRetry.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
    }

    private void openAttemptGrading(AttemptRow row) {
        if ("in_progress".equalsIgnoreCase(row.rawStatus)) {
            Toast.makeText(this, "This attempt is still in progress and cannot be graded yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, TeacherGradeQuizAttemptActivity.class);
        intent.putExtra(TeacherGradeQuizAttemptActivity.EXTRA_ATTEMPT_ID, row.id);
        intent.putExtra(TeacherGradeQuizAttemptActivity.EXTRA_QUIZ_ID, quizId);
        intent.putExtra(TeacherGradeQuizAttemptActivity.EXTRA_QUIZ_TITLE, quizTitle);
        startActivity(intent);
    }

    private class AttemptsAdapter extends RecyclerView.Adapter<AttemptsAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_quiz_attempt, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            AttemptRow r = filteredRows.get(position);
            h.tvCode.setText(r.studentCode);
            h.tvName.setText(r.studentName);

            String statusText = r.isReset ? "Reset" : r.displayGradingStatus;
            h.tvStatus.setText(statusText);
            if (r.isReset) {
                h.tvStatus.setTextColor(Color.parseColor("#64748B"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            } else if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(statusText)) {
                h.tvStatus.setTextColor(Color.parseColor("#047857"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
            } else if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(statusText)) {
                h.tvStatus.setTextColor(Color.parseColor("#1D4ED8"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
            } else {
                h.tvStatus.setTextColor(Color.parseColor("#B45309"));
                h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            }

            StringBuilder metaSb = new StringBuilder();
            metaSb.append("Attempt #").append(r.attemptNumber);
            if (!r.submittedAt.isEmpty()) {
                metaSb.append(" • Submitted ").append(AssessmentUiHelper.formatDateTime(r.submittedAt));
            } else if ("in_progress".equalsIgnoreCase(r.rawStatus)) {
                metaSb.append(" • In Progress");
            }
            if (r.isLate) metaSb.append(" • [Late]");
            h.tvMeta.setText(metaSb.toString());

            boolean isPending = QuizScoreUiHelper.STATUS_PENDING_GRADING.equalsIgnoreCase(r.displayGradingStatus)
                    || QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(r.displayGradingStatus);

            if ("in_progress".equalsIgnoreCase(r.rawStatus)) {
                h.tvScore.setText("Score: In Progress");
            } else if (!isPending && r.score != null) {
                h.tvScore.setText(String.format(Locale.US, "Score: %s / %s (Auto: %s • Manual: %s)",
                        AssessmentUiHelper.formatMarks(r.score),
                        AssessmentUiHelper.formatMarks(r.totalMarks),
                        AssessmentUiHelper.formatMarks(r.autoGradedScore),
                        AssessmentUiHelper.formatMarks(r.manualGradedScore)));
            } else {
                double provisional = r.autoGradedScore + r.manualGradedScore;
                h.tvScore.setText(String.format(Locale.US, "Earned so far: %s / %s (Auto: %s • Manual: %s)",
                        AssessmentUiHelper.formatMarks(provisional),
                        AssessmentUiHelper.formatMarks(r.totalMarks),
                        AssessmentUiHelper.formatMarks(r.autoGradedScore),
                        AssessmentUiHelper.formatMarks(r.manualGradedScore)));
            }

            if (r.pendingQuestionsCount > 0 && !r.isReset) {
                h.tvPendingQs.setVisibility(View.VISIBLE);
                h.tvPendingQs.setText(r.pendingQuestionsCount == 1
                        ? "1 ungraded answer"
                        : (r.pendingQuestionsCount + " ungraded answers"));
            } else {
                h.tvPendingQs.setVisibility(View.GONE);
            }

            if ("in_progress".equalsIgnoreCase(r.rawStatus)) {
                h.btnOpen.setText("In Progress");
                h.btnOpen.setEnabled(false);
                h.btnOpen.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
            } else if (isPending) {
                h.btnOpen.setEnabled(true);
                h.btnOpen.setText("Grade Short Answers");
                h.btnOpen.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
            } else {
                h.btnOpen.setEnabled(true);
                h.btnOpen.setText("Review / Edit Grade");
                h.btnOpen.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.admin_primary_blue, null)));
            }

            View.OnClickListener openClick = v -> openAttemptGrading(r);
            h.btnOpen.setOnClickListener(openClick);
            h.itemView.setOnClickListener(openClick);
        }

        @Override
        public int getItemCount() {
            return filteredRows.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvCode, tvName, tvStatus, tvMeta, tvScore, tvPendingQs;
            MaterialButton btnOpen;

            VH(@NonNull View v) {
                super(v);
                tvCode = v.findViewById(R.id.tvAttemptStudentCode);
                tvName = v.findViewById(R.id.tvAttemptStudentName);
                tvStatus = v.findViewById(R.id.tvAttemptStatusBadge);
                tvMeta = v.findViewById(R.id.tvAttemptMetaLine);
                tvScore = v.findViewById(R.id.tvAttemptScoreLine);
                tvPendingQs = v.findViewById(R.id.tvAttemptPendingQsBadge);
                btnOpen = v.findViewById(R.id.btnOpenAttemptGrading);
            }
        }
    }
}
