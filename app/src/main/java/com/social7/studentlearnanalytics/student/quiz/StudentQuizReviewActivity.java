package com.social7.studentlearnanalytics.student.quiz;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays question-by-question Student Quiz Answer Review with earned marks,
 * maximum marks, grading status (Graded / Pending Grading / Partially Graded),
 * correct answer (when allowed by server), and teacher feedback.
 */
public class StudentQuizReviewActivity extends AppCompatActivity {

    public static final String EXTRA_ATTEMPT_ID = "attempt_id";
    public static final String EXTRA_QUIZ_TITLE = "quiz_title";
    public static final String EXTRA_COURSE_INFO = "course_info";

    private String attemptId = "";
    private TextView tvToolbarTitle, tvToolbarSubtitle;
    private ProgressBar pbLoading;
    private LinearLayout layoutError, layoutQuestionsContainer;
    private NestedScrollView scrollContent;
    private TextView tvErrorTitle, tvErrorMsg;
    private TextView tvScoreHeadline, tvStatusBadge, tvAttemptMeta, tvProvisionalBanner;

    private boolean initialLoadDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_quiz_review);

        if (getIntent() != null) {
            attemptId = getIntent().getStringExtra(EXTRA_ATTEMPT_ID);
        }
        if (attemptId == null || attemptId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid quiz attempt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvToolbarTitle = findViewById(R.id.tvReviewToolbarTitle);
        tvToolbarSubtitle = findViewById(R.id.tvReviewToolbarSubtitle);
        pbLoading = findViewById(R.id.pbReviewLoading);
        layoutError = findViewById(R.id.layoutReviewError);
        tvErrorTitle = findViewById(R.id.tvReviewErrorTitle);
        tvErrorMsg = findViewById(R.id.tvReviewErrorMsg);
        scrollContent = findViewById(R.id.scrollReviewContent);
        tvScoreHeadline = findViewById(R.id.tvReviewScoreHeadline);
        tvStatusBadge = findViewById(R.id.tvReviewStatusBadge);
        tvAttemptMeta = findViewById(R.id.tvReviewAttemptMeta);
        tvProvisionalBanner = findViewById(R.id.tvReviewProvisionalBanner);
        layoutQuestionsContainer = findViewById(R.id.layoutReviewQuestionsContainer);

        String quizTitle = getIntent() != null ? getIntent().getStringExtra(EXTRA_QUIZ_TITLE) : null;
        String courseInfo = getIntent() != null ? getIntent().getStringExtra(EXTRA_COURSE_INFO) : null;
        if (quizTitle != null && !quizTitle.trim().isEmpty()) {
            tvToolbarTitle.setText(quizTitle.trim());
        }
        if (courseInfo != null && !courseInfo.trim().isEmpty()) {
            tvToolbarSubtitle.setVisibility(View.VISIBLE);
            tvToolbarSubtitle.setText(courseInfo.trim());
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        MaterialButton btnRefresh = findViewById(R.id.btnRefreshReview);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> fetchReview());
        }
        MaterialButton btnRetry = findViewById(R.id.btnRetryReview);
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> fetchReview());
        }

        fetchReview();
        initialLoadDone = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (initialLoadDone) {
            fetchReview();
        }
    }

    private void fetchReview() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);

        ApiClient.getApiService(this).reviewStudentQuizAttempt(attemptId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    layoutError.setVisibility(View.VISIBLE);
                    tvErrorTitle.setText(response.code() == 403 || response.code() == 404
                            ? "Access Unavailable" : "Could not load Answer Review");
                    tvErrorMsg.setText(AssessmentUiHelper.extractErrorMessage(response, "Answer review is not available yet."));
                    return;
                }

                JsonObject data = response.body().getAsJsonObject("data");
                bindReviewData(data);
                scrollContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                layoutError.setVisibility(View.VISIBLE);
                tvErrorTitle.setText("Network error");
                tvErrorMsg.setText("Please check your connection and try again.");
            }
        });
    }

    private void bindReviewData(JsonObject data) {
        QuizScoreUiHelper.ReviewScoreSummary summary = QuizScoreUiHelper.parseStudentReviewSummary(data);
        JsonObject result = data.has("result") && data.get("result").isJsonObject()
                ? data.getAsJsonObject("result") : new JsonObject();

        tvScoreHeadline.setText(summary.formatScoreLine());
        tvStatusBadge.setText("Status: " + summary.gradingStatus);

        int badgeTextColor;
        int badgeBgColor;
        if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(summary.gradingStatus)) {
            badgeTextColor = Color.parseColor("#047857");
            badgeBgColor = Color.parseColor("#ECFDF5");
        } else if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(summary.gradingStatus)) {
            badgeTextColor = Color.parseColor("#1D4ED8");
            badgeBgColor = Color.parseColor("#EFF6FF");
        } else {
            badgeTextColor = Color.parseColor("#B45309");
            badgeBgColor = Color.parseColor("#FFFBEB");
        }
        tvStatusBadge.setTextColor(badgeTextColor);
        tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(badgeBgColor));

        int attemptNum = AssessmentUiHelper.optInt(result, "attempt_number", 1);
        String submittedAt = AssessmentUiHelper.optString(result, "submitted_at", "");
        StringBuilder metaSb = new StringBuilder();
        metaSb.append(String.format(Locale.US, "Attempt #%d • %d Questions", attemptNum, summary.totalQuestions));
        if (!submittedAt.isEmpty()) {
            metaSb.append(" • Submitted ").append(AssessmentUiHelper.formatDateTime(submittedAt));
        }
        if (summary.pendingCount > 0) {
            metaSb.append(String.format(Locale.US, " • %d Pending", summary.pendingCount));
        }
        tvAttemptMeta.setText(metaSb.toString());

        String provisionalNote = summary.formatProvisionalNote();
        if (provisionalNote != null) {
            tvProvisionalBanner.setVisibility(View.VISIBLE);
            tvProvisionalBanner.setText(provisionalNote);
        } else {
            tvProvisionalBanner.setVisibility(View.GONE);
        }

        layoutQuestionsContainer.removeAllViews();
        JsonArray questions = data.has("questions") && data.get("questions").isJsonArray()
                ? data.getAsJsonArray("questions") : new JsonArray();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < questions.size(); i++) {
            JsonElement el = questions.get(i);
            if (!el.isJsonObject()) continue;
            JsonObject q = el.getAsJsonObject();

            View card = inflater.inflate(R.layout.item_quiz_review_question, layoutQuestionsContainer, false);
            TextView tvQNumber = card.findViewById(R.id.tvReviewQNumber);
            TextView tvQType = card.findViewById(R.id.tvReviewQType);
            TextView tvQStatus = card.findViewById(R.id.tvReviewQStatus);
            TextView tvQText = card.findViewById(R.id.tvReviewQuestionText);
            TextView tvYourAns = card.findViewById(R.id.tvReviewYourAnswer);
            TextView tvCorrAns = card.findViewById(R.id.tvReviewCorrectAnswer);
            TextView tvMarks = card.findViewById(R.id.tvReviewMarks);
            LinearLayout layoutFb = card.findViewById(R.id.layoutReviewFeedback);
            TextView tvFb = card.findViewById(R.id.tvReviewFeedback);

            int num = AssessmentUiHelper.optInt(q, "number", i + 1);
            String rawType = AssessmentUiHelper.optString(q, "question_type", "multiple_choice");
            String qText = AssessmentUiHelper.optString(q, "question_text", "");
            String yourRaw = AssessmentUiHelper.optString(q, "your_answer", "");
            String corrRaw = AssessmentUiHelper.optString(q, "correct_answer", "");
            String rawStatus = AssessmentUiHelper.optString(q, "grading_status", "");
            String feedback = AssessmentUiHelper.optString(q, "teacher_feedback", "");
            double maxM = AssessmentUiHelper.optDouble(q, "marks", 1.0);
            boolean hasMarks = q.has("marks_obtained") && !q.get("marks_obtained").isJsonNull();
            boolean isPending = "pending_grading".equalsIgnoreCase(rawStatus)
                    || ("short_answer".equalsIgnoreCase(rawType) && !"graded".equalsIgnoreCase(rawStatus)
                    && !"auto_graded".equalsIgnoreCase(rawStatus) && !"unanswered".equalsIgnoreCase(rawStatus) && !hasMarks);

            tvQNumber.setText(String.format(Locale.US, "Q%d.", num));
            tvQType.setText(QuizScoreUiHelper.formatQuestionType(rawType));

            String statusText = QuizScoreUiHelper.formatQuestionGradingStatus(rawStatus, isPending);
            tvQStatus.setText("Status: " + statusText);
            if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(statusText)) {
                tvQStatus.setTextColor(Color.parseColor("#047857"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
            } else if ("Unanswered".equalsIgnoreCase(statusText)) {
                tvQStatus.setTextColor(Color.parseColor("#64748B"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            } else {
                tvQStatus.setTextColor(Color.parseColor("#B45309"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            }

            tvQText.setText(qText);

            String displayAnswer = resolveOptionDisplay(q, rawType, yourRaw);
            tvYourAns.setText(displayAnswer.isEmpty() ? "(No answer submitted)" : displayAnswer);

            if (summary.correctAnswersVisible && !corrRaw.isEmpty()) {
                String corrDisplay = resolveOptionDisplay(q, rawType, corrRaw);
                tvCorrAns.setVisibility(View.VISIBLE);
                tvCorrAns.setText("Correct Answer: " + corrDisplay);
            } else {
                tvCorrAns.setVisibility(View.GONE);
            }

            if (isPending) {
                tvMarks.setText("Marks: Pending Grading (Max: " + AssessmentUiHelper.formatMarks(maxM) + ")");
                tvMarks.setTextColor(Color.parseColor("#B45309"));
            } else if (hasMarks) {
                double earned = q.get("marks_obtained").getAsDouble();
                tvMarks.setText("Marks: " + AssessmentUiHelper.formatMarks(earned) + " / " + AssessmentUiHelper.formatMarks(maxM));
                tvMarks.setTextColor(earned >= maxM ? Color.parseColor("#047857")
                        : (earned > 0 ? Color.parseColor("#1D4ED8") : Color.parseColor("#DC2626")));
            } else {
                tvMarks.setText("Marks: 0 / " + AssessmentUiHelper.formatMarks(maxM));
                tvMarks.setTextColor(Color.parseColor("#64748B"));
            }

            if (!feedback.isEmpty()) {
                layoutFb.setVisibility(View.VISIBLE);
                tvFb.setText(feedback);
            } else {
                layoutFb.setVisibility(View.GONE);
            }

            layoutQuestionsContainer.addView(card);
        }
    }

    private String resolveOptionDisplay(JsonObject q, String rawType, String rawVal) {
        if (rawVal == null || rawVal.trim().isEmpty()) {
            return "";
        }
        String val = rawVal.trim();
        if ("true_false".equalsIgnoreCase(rawType)) {
            if ("true".equalsIgnoreCase(val)) return "True";
            if ("false".equalsIgnoreCase(val)) return "False";
            return val;
        }
        if (("multiple_choice".equalsIgnoreCase(rawType) || "mcq".equalsIgnoreCase(rawType))
                && q.has("options") && q.get("options").isJsonArray()) {
            for (JsonElement oEl : q.getAsJsonArray("options")) {
                if (!oEl.isJsonObject()) continue;
                JsonObject opt = oEl.getAsJsonObject();
                String key = AssessmentUiHelper.optString(opt, "key", "");
                String text = AssessmentUiHelper.optString(opt, "text", "");
                if (key.equalsIgnoreCase(val) && !text.isEmpty()) {
                    return key.toUpperCase(Locale.US) + ". " + text;
                }
            }
        }
        return val;
    }
}
