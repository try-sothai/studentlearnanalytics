package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Teacher manual grading screen for a single student quiz attempt.
 * Shows MC / True-False auto-graded results and Short Answer inputs with
 * validation, duplicate/overwrite guard, single-answer save, and atomic batch save.
 */
public class TeacherGradeQuizAttemptActivity extends AppCompatActivity {

    public static final String EXTRA_ATTEMPT_ID = "attempt_id";
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_QUIZ_TITLE = "quiz_title";

    private int attemptId = 0;
    private String quizTitle = "Grade Attempt";
    private boolean isSaving = false;

    private ProgressBar pbLoading;
    private LinearLayout layoutError, layoutQuestionsContainer, layoutBottomBar;
    private ScrollView scrollContent;
    private TextView tvErrorTitle, tvErrorMsg;
    private TextView tvStudentName, tvAttemptSubtitle, tvStatusBadge, tvTotalScore, tvBreakdown;
    private MaterialButton btnRetry, btnSaveAll;

    private final List<ShortAnswerHolder> shortAnswerHolders = new ArrayList<>();

    private static class ShortAnswerHolder {
        int questionId;
        int answerId;
        int questionNumber;
        double maxMarks;
        boolean alreadyGraded;
        @Nullable Double savedMarks;
        String savedFeedback;
        TextInputLayout tilMarks;
        TextInputEditText etMarks;
        TextInputEditText etFeedback;
        MaterialButton btnSaveSingle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_grade_quiz_attempt);

        if (getIntent() != null) {
            attemptId = getIntent().getIntExtra(EXTRA_ATTEMPT_ID, 0);
            String qt = getIntent().getStringExtra(EXTRA_QUIZ_TITLE);
            if (qt != null && !qt.trim().isEmpty()) quizTitle = qt.trim();
        }
        if (attemptId <= 0) {
            Toast.makeText(this, "Invalid attempt ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherGradeAttempt),
                "Grade Quiz Attempt", quizTitle, true);

        pbLoading = findViewById(R.id.pbGradeAttemptLoading);
        layoutError = findViewById(R.id.layoutGradeAttemptError);
        tvErrorTitle = findViewById(R.id.tvGradeAttemptErrorTitle);
        tvErrorMsg = findViewById(R.id.tvGradeAttemptErrorMsg);
        btnRetry = findViewById(R.id.btnRetryGradeAttempt);
        scrollContent = findViewById(R.id.scrollGradeAttemptContent);
        tvStudentName = findViewById(R.id.tvGradeStudentName);
        tvAttemptSubtitle = findViewById(R.id.tvGradeAttemptSubtitle);
        tvStatusBadge = findViewById(R.id.tvGradeAttemptStatusBadge);
        tvTotalScore = findViewById(R.id.tvGradeAttemptTotalScore);
        tvBreakdown = findViewById(R.id.tvGradeAttemptBreakdown);
        layoutQuestionsContainer = findViewById(R.id.layoutGradeQuestionsContainer);
        layoutBottomBar = findViewById(R.id.layoutGradeBottomBar);
        btnSaveAll = findViewById(R.id.btnSaveAllShortGrades);

        btnRetry.setOnClickListener(v -> fetchAttemptDetail());
        btnSaveAll.setOnClickListener(v -> saveAllShortAnswerGrades());

        fetchAttemptDetail();
    }

    private void fetchAttemptDetail() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        layoutBottomBar.setVisibility(View.GONE);

        ApiClient.getApiService(this).getTeacherQuizAttemptDetail(attemptId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    layoutError.setVisibility(View.VISIBLE);
                    tvErrorTitle.setText(response.code() == 403 ? "Unauthorized" : "Could not load attempt");
                    tvErrorMsg.setText(AssessmentUiHelper.extractErrorMessage(response, "Unable to fetch attempt details."));
                    return;
                }

                bindAttemptDetail(response.body().getAsJsonObject("data"));
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

    private void bindAttemptDetail(JsonObject data) {
        scrollContent.setVisibility(View.VISIBLE);
        shortAnswerHolders.clear();
        layoutQuestionsContainer.removeAllViews();

        String stuName = AssessmentUiHelper.optString(data, "student_name", "Student");
        String stuCode = AssessmentUiHelper.optString(data, "student_code", "");
        String qzTitle = AssessmentUiHelper.optString(data, "quiz_title", quizTitle);
        int attNum = AssessmentUiHelper.optInt(data, "attempt_number", 1);
        String rawStatus = AssessmentUiHelper.optString(data, "status", "");
        String rawGradingStatus = AssessmentUiHelper.optString(data, "grading_status", "");
        boolean pendingAttempt = AssessmentUiHelper.optBoolean(data, "pending_manual_grading", false);
        int pendingCount = AssessmentUiHelper.optInt(data, "pending_questions_count", 0);
        double autoScore = AssessmentUiHelper.optDouble(data, "auto_graded_score", 0.0);
        double manualScore = AssessmentUiHelper.optDouble(data, "manual_graded_score", 0.0);
        double totalMarks = AssessmentUiHelper.optDouble(data, "total_marks", 0.0);
        String submittedAt = AssessmentUiHelper.optString(data, "submitted_at", "");
        boolean isLate = AssessmentUiHelper.optBoolean(data, "is_late", false);

        JsonArray qs = data.has("questions") && data.get("questions").isJsonArray()
                ? data.getAsJsonArray("questions") : new JsonArray();

        int gradedManualCount = 0;
        for (JsonElement el : qs) {
            if (!el.isJsonObject()) continue;
            JsonObject q = el.getAsJsonObject();
            String qType = AssessmentUiHelper.optString(q, "question_type", "");
            String qGrStatus = AssessmentUiHelper.optString(q, "grading_status", "");
            if ("short_answer".equalsIgnoreCase(qType) && "graded".equalsIgnoreCase(qGrStatus)) {
                gradedManualCount++;
            }
        }

        String displayStatus = QuizScoreUiHelper.resolveAttemptGradingStatus(
                rawGradingStatus,
                rawStatus,
                pendingAttempt,
                pendingCount,
                gradedManualCount,
                manualScore
        );

        tvStudentName.setText(stuCode.isEmpty() ? stuName : (stuName + " (" + stuCode + ")"));
        StringBuilder subSb = new StringBuilder();
        subSb.append(qzTitle).append(" • Attempt #").append(attNum);
        if (!submittedAt.isEmpty()) {
            subSb.append(" • Submitted ").append(AssessmentUiHelper.formatDateTime(submittedAt));
        }
        if (isLate) subSb.append(" [Late]");
        tvAttemptSubtitle.setText(subSb.toString());

        tvStatusBadge.setText(displayStatus);
        if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(displayStatus)) {
            tvStatusBadge.setTextColor(Color.parseColor("#047857"));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
        } else if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(displayStatus)) {
            tvStatusBadge.setTextColor(Color.parseColor("#1D4ED8"));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
        } else {
            tvStatusBadge.setTextColor(Color.parseColor("#B45309"));
            tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
        }

        boolean isPending = QuizScoreUiHelper.STATUS_PENDING_GRADING.equalsIgnoreCase(displayStatus)
                || QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(displayStatus);
        if (!isPending && data.has("score") && !data.get("score").isJsonNull()) {
            double finalScore = data.get("score").getAsDouble();
            tvTotalScore.setText(String.format(Locale.US, "Total Score: %s / %s",
                    AssessmentUiHelper.formatMarks(finalScore),
                    AssessmentUiHelper.formatMarks(totalMarks)));
            tvTotalScore.setTextColor(Color.parseColor("#047857"));
        } else {
            double earnedSoFar = QuizScoreUiHelper.round2(autoScore + manualScore);
            tvTotalScore.setText(String.format(Locale.US, "Earned So Far: %s / %s (%d pending)",
                    AssessmentUiHelper.formatMarks(earnedSoFar),
                    AssessmentUiHelper.formatMarks(totalMarks),
                    pendingCount));
            tvTotalScore.setTextColor(Color.parseColor("#B45309"));
        }

        StringBuilder bdSb = new StringBuilder();
        bdSb.append(String.format(Locale.US, "Auto-Graded: %s • Manual-Graded: %s",
                AssessmentUiHelper.formatMarks(autoScore),
                AssessmentUiHelper.formatMarks(manualScore)));
        if (data.has("effective_student_score") && !data.get("effective_student_score").isJsonNull()) {
            bdSb.append(" • Official Student Score: ")
                    .append(AssessmentUiHelper.formatMarks(data.get("effective_student_score").getAsDouble()))
                    .append(" / ")
                    .append(AssessmentUiHelper.formatMarks(totalMarks));
        }
        tvBreakdown.setText(bdSb.toString());

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < qs.size(); i++) {
            JsonElement el = qs.get(i);
            if (!el.isJsonObject()) continue;
            JsonObject q = el.getAsJsonObject();

            View card = inflater.inflate(R.layout.item_teacher_grade_question, layoutQuestionsContainer, false);
            TextView tvQNum = card.findViewById(R.id.tvTeacherQNumber);
            TextView tvQType = card.findViewById(R.id.tvTeacherQType);
            TextView tvQMax = card.findViewById(R.id.tvTeacherQMaxMarks);
            TextView tvQStatus = card.findViewById(R.id.tvTeacherQStatus);
            TextView tvQText = card.findViewById(R.id.tvTeacherQText);
            TextView tvStuAns = card.findViewById(R.id.tvTeacherStudentAnswer);
            LinearLayout layoutExpected = card.findViewById(R.id.layoutTeacherExpected);
            TextView tvExpectedLabel = card.findViewById(R.id.tvTeacherExpectedLabel);
            TextView tvExpectedVal = card.findViewById(R.id.tvTeacherExpectedValue);
            TextView tvAutoMarks = card.findViewById(R.id.tvTeacherAutoMarks);
            LinearLayout layoutManualInputs = card.findViewById(R.id.layoutManualGradeInputs);
            TextInputLayout tilMarks = card.findViewById(R.id.tilGradeMarks);
            TextInputEditText etMarks = card.findViewById(R.id.etGradeMarks);
            TextInputEditText etFeedback = card.findViewById(R.id.etGradeFeedback);
            MaterialButton btnSaveSingle = card.findViewById(R.id.btnSaveQuestionGrade);

            int qId = AssessmentUiHelper.optInt(q, "question_id", 0);
            int ansId = AssessmentUiHelper.optInt(q, "answer_id", 0);
            int num = AssessmentUiHelper.optInt(q, "number", i + 1);
            String qType = AssessmentUiHelper.optString(q, "question_type", "multiple_choice");
            String qText = AssessmentUiHelper.optString(q, "question_text", "");
            String stuAns = AssessmentUiHelper.optString(q, "student_answer", "");
            String guidance = AssessmentUiHelper.optString(q, "grading_guidance", AssessmentUiHelper.optString(q, "correct_answer", ""));
            String corrAns = AssessmentUiHelper.optString(q, "correct_answer", "");
            String curFeedback = AssessmentUiHelper.optString(q, "teacher_feedback", "");
            String qGrStatus = AssessmentUiHelper.optString(q, "grading_status", "");
            double maxM = AssessmentUiHelper.optDouble(q, "marks", 1.0);
            boolean needsManual = AssessmentUiHelper.optBoolean(q, "needs_manual_grading", false);
            Double curMarks = (q.has("marks_obtained") && !q.get("marks_obtained").isJsonNull())
                    ? q.get("marks_obtained").getAsDouble() : null;

            tvQNum.setText(String.format(Locale.US, "Q%d.", num));
            tvQType.setText(QuizScoreUiHelper.formatQuestionType(qType));
            tvQMax.setText("Max: " + AssessmentUiHelper.formatMarks(maxM));
            tvQText.setText(qText);
            tvStuAns.setText(stuAns.isEmpty() ? "(No answer submitted)" : stuAns);

            String statusLabel = QuizScoreUiHelper.formatQuestionGradingStatus(qGrStatus, needsManual);
            tvQStatus.setText(statusLabel);
            if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(statusLabel)) {
                tvQStatus.setTextColor(Color.parseColor("#047857"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
            } else if ("Unanswered".equalsIgnoreCase(statusLabel)) {
                tvQStatus.setTextColor(Color.parseColor("#64748B"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            } else {
                tvQStatus.setTextColor(Color.parseColor("#B45309"));
                tvQStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            }

            if ("short_answer".equalsIgnoreCase(qType)) {
                if (!guidance.isEmpty()) {
                    layoutExpected.setVisibility(View.VISIBLE);
                    tvExpectedLabel.setText("Expected Answer / Grading Guidance");
                    tvExpectedVal.setText(guidance);
                } else {
                    layoutExpected.setVisibility(View.GONE);
                }

                tvAutoMarks.setVisibility(View.GONE);
                layoutManualInputs.setVisibility(View.VISIBLE);
                tilMarks.setHint("Marks Obtained (0 - " + AssessmentUiHelper.formatMarks(maxM) + ")");
                if (curMarks != null && !needsManual) {
                    etMarks.setText(AssessmentUiHelper.formatMarks(curMarks));
                }
                if (!curFeedback.isEmpty()) {
                    etFeedback.setText(curFeedback);
                }

                boolean alreadyGraded = !needsManual && "graded".equalsIgnoreCase(qGrStatus) && curMarks != null;
                btnSaveSingle.setText(alreadyGraded ? "Update Grade" : "Save Grade");

                ShortAnswerHolder holder = new ShortAnswerHolder();
                holder.questionId = qId;
                holder.answerId = ansId;
                holder.questionNumber = num;
                holder.maxMarks = maxM;
                holder.alreadyGraded = alreadyGraded;
                holder.savedMarks = curMarks;
                holder.savedFeedback = curFeedback;
                holder.tilMarks = tilMarks;
                holder.etMarks = etMarks;
                holder.etFeedback = etFeedback;
                holder.btnSaveSingle = btnSaveSingle;
                shortAnswerHolders.add(holder);

                btnSaveSingle.setOnClickListener(v -> saveSingleQuestionGrade(holder));
            } else {
                if (!corrAns.isEmpty()) {
                    layoutExpected.setVisibility(View.VISIBLE);
                    tvExpectedLabel.setText("Correct Answer");
                    tvExpectedVal.setText(corrAns);
                } else {
                    layoutExpected.setVisibility(View.GONE);
                }

                layoutManualInputs.setVisibility(View.GONE);
                tvAutoMarks.setVisibility(View.VISIBLE);
                double earned = curMarks != null ? curMarks : 0.0;
                tvAutoMarks.setText(String.format(Locale.US, "Marks: %s / %s (Auto-Graded)",
                        AssessmentUiHelper.formatMarks(earned),
                        AssessmentUiHelper.formatMarks(maxM)));
                tvAutoMarks.setTextColor(earned >= maxM ? Color.parseColor("#047857")
                        : (earned > 0 ? Color.parseColor("#1D4ED8") : Color.parseColor("#DC2626")));
            }

            layoutQuestionsContainer.addView(card);
        }

        layoutBottomBar.setVisibility(shortAnswerHolders.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void saveSingleQuestionGrade(ShortAnswerHolder holder) {
        if (isSaving) return;

        holder.tilMarks.setError(null);
        String rawMarks = holder.etMarks.getText() != null ? holder.etMarks.getText().toString().trim() : "";
        QuizScoreUiHelper.MarksValidationResult validation = QuizScoreUiHelper.validateMarksInput(rawMarks, holder.maxMarks);
        if (!validation.valid) {
            holder.tilMarks.setError(validation.errorMessage);
            holder.etMarks.requestFocus();
            return;
        }

        String fb = holder.etFeedback.getText() != null ? holder.etFeedback.getText().toString().trim() : "";

        // Prevent redundant duplicate grading or accidental silent overwrite
        if (holder.alreadyGraded && holder.savedMarks != null
                && Math.abs(validation.marks - holder.savedMarks) < 0.001
                && fb.equals(holder.savedFeedback != null ? holder.savedFeedback.trim() : "")) {
            Toast.makeText(this, "No changes to save for Q" + holder.questionNumber, Toast.LENGTH_SHORT).show();
            return;
        }

        Runnable executeSave = () -> performSingleGradeRequest(holder, validation.marks, fb);
        if (holder.alreadyGraded) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Update Existing Grade?")
                    .setMessage(String.format(Locale.US,
                            "Q%d is already graded (%s / %s). Do you want to update it to %s / %s?",
                            holder.questionNumber,
                            holder.savedMarks != null ? AssessmentUiHelper.formatMarks(holder.savedMarks) : "0",
                            AssessmentUiHelper.formatMarks(holder.maxMarks),
                            AssessmentUiHelper.formatMarks(validation.marks),
                            AssessmentUiHelper.formatMarks(holder.maxMarks)))
                    .setPositiveButton("Update Grade", (d, w) -> executeSave.run())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            executeSave.run();
        }
    }

    private void performSingleGradeRequest(ShortAnswerHolder holder, double marks, String feedback) {
        setSavingState(true);

        if (holder.answerId > 0) {
            JsonObject body = new JsonObject();
            body.addProperty("marks_obtained", marks);
            body.addProperty("teacher_feedback", feedback);

            ApiClient.getApiService(this).gradeTeacherQuizAnswer(holder.answerId, body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    setSavingState(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(TeacherGradeQuizAttemptActivity.this,
                                "Grade saved and total score updated.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        fetchAttemptDetail();
                    } else {
                        Toast.makeText(TeacherGradeQuizAttemptActivity.this,
                                AssessmentUiHelper.extractErrorMessage(response, "Failed to save grade"),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    setSavingState(false);
                    Toast.makeText(TeacherGradeQuizAttemptActivity.this, "Network error saving grade", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            JsonObject item = new JsonObject();
            item.addProperty("question_id", holder.questionId);
            item.addProperty("marks_obtained", marks);
            item.addProperty("teacher_feedback", feedback);
            JsonArray grades = new JsonArray();
            grades.add(item);
            JsonObject body = new JsonObject();
            body.add("grades", grades);
            sendBatchGradeRequest(body, "Grade saved and total score updated.");
        }
    }

    private void saveAllShortAnswerGrades() {
        if (isSaving || shortAnswerHolders.isEmpty()) return;

        JsonArray grades = new JsonArray();
        boolean hasOverwrite = false;

        for (ShortAnswerHolder holder : shortAnswerHolders) {
            holder.tilMarks.setError(null);
            String rawMarks = holder.etMarks.getText() != null ? holder.etMarks.getText().toString().trim() : "";
            String fb = holder.etFeedback.getText() != null ? holder.etFeedback.getText().toString().trim() : "";

            // Skip completely untouched ungraded rows only if user hasn't filled them yet?
            // If user taps Save All, require marks on ungraded rows unless another row was filled, or validate all filled rows.
            if (rawMarks.isEmpty()) {
                if (holder.alreadyGraded) {
                    continue;
                }
                if (shortAnswerHolders.size() == 1) {
                    holder.tilMarks.setError("Please enter marks.");
                    holder.etMarks.requestFocus();
                    return;
                }
                continue;
            }

            QuizScoreUiHelper.MarksValidationResult validation = QuizScoreUiHelper.validateMarksInput(rawMarks, holder.maxMarks);
            if (!validation.valid) {
                holder.tilMarks.setError(validation.errorMessage);
                holder.etMarks.requestFocus();
                return;
            }

            // Skip unchanged already-graded rows so we don't trigger unnecessary updates
            if (holder.alreadyGraded && holder.savedMarks != null
                    && Math.abs(validation.marks - holder.savedMarks) < 0.001
                    && fb.equals(holder.savedFeedback != null ? holder.savedFeedback.trim() : "")) {
                continue;
            }
            if (holder.alreadyGraded) {
                hasOverwrite = true;
            }

            JsonObject item = new JsonObject();
            if (holder.answerId > 0) {
                item.addProperty("answer_id", holder.answerId);
            } else {
                item.addProperty("question_id", holder.questionId);
            }
            item.addProperty("marks_obtained", validation.marks);
            item.addProperty("teacher_feedback", fb);
            grades.add(item);
        }

        if (grades.size() == 0) {
            Toast.makeText(this, "Enter marks for at least one Short Answer question to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.add("grades", grades);

        if (hasOverwrite) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Update Existing Grades?")
                    .setMessage("One or more Short Answer questions were already graded. Save the updated marks and recalculate the attempt score?")
                    .setPositiveButton("Save Updates", (d, w) ->
                            sendBatchGradeRequest(body, "All short-answer grades saved and total score updated."))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            sendBatchGradeRequest(body, "All short-answer grades saved and total score updated.");
        }
    }

    private void sendBatchGradeRequest(JsonObject body, String successMsg) {
        setSavingState(true);
        ApiClient.getApiService(this).gradeTeacherQuizAttempt(attemptId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                setSavingState(false);
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    Toast.makeText(TeacherGradeQuizAttemptActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    bindAttemptDetail(response.body().getAsJsonObject("data"));
                } else {
                    Toast.makeText(TeacherGradeQuizAttemptActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to save grades"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                setSavingState(false);
                Toast.makeText(TeacherGradeQuizAttemptActivity.this, "Network error saving grades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSavingState(boolean saving) {
        isSaving = saving;
        btnSaveAll.setEnabled(!saving);
        for (ShortAnswerHolder h : shortAnswerHolders) {
            if (h.btnSaveSingle != null) {
                h.btnSaveSingle.setEnabled(!saving);
            }
        }
    }
}
