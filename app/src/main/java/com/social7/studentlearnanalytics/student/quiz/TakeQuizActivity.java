package com.social7.studentlearnanalytics.student.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TakeQuizActivity extends AppCompatActivity {

    private String quizId = "";
    private String attemptId = null;
    private int quizDurationMinutes = 30;
    private double quizTotalMarks = 0;

    private TextView tvHeaderTitle, tvTimer;
    private ProgressBar pbLoading;
    private ScrollView scrollQuizContent;
    private LinearLayout layoutQuizError;
    private TextView tvQuizErrorTitle, tvQuizErrorMsg;
    private MaterialButton btnRetryQuizLoad;

    // Screen A: Instructions / Summary
    private LinearLayout layoutInstructions;
    private TextView tvInstructionsTitle, tvCourseInfo, tvQuizMetaDetails, tvQuizDueDate, tvInstructionsBody, tvCannotStartReason;
    private MaterialCardView cardAttemptSummary;
    private TextView tvAttemptStatusTitle, tvAttemptStatusDetail;
    private MaterialButton btnReviewFromSummary, btnStartQuiz;

    // Screen B: Taking
    private LinearLayout layoutTaking;
    private TextView tvQuestionProgress, tvAnsweredCounter, tvQuestionTypeTag, tvQuestionMarksTag, tvQuestionText;
    private RadioGroup rgAnswers;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private TextInputLayout tilShortAnswer;
    private TextInputEditText etShortAnswerInput;
    private MaterialButton btnPrevQuestion, btnNextQuestion, btnSubmitQuiz;
    private RecyclerView rvQuestionNav;
    private QuizNavAdapter navAdapter;

    // Screen C: Result
    private LinearLayout layoutResult;
    private TextView tvResultTitle, tvResultSubtitle;
    private MaterialButton btnReviewFromResult, btnRetakeFromResult, btnReturnHub;

    private CountDownTimer countDownTimer;
    private final List<JsonObject> questions = new ArrayList<>();
    private final List<String> selectedAnswers = new ArrayList<>();
    private final List<List<String>> currentOptionKeys = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private boolean suppressRadioCallback = false;
    private boolean suppressShortAnswerCallback = false;
    private int maxAttemptsCap = 1; // 0 = unlimited
    private int attemptsUsedCount = 0;
    private String latestAttemptId = null;
    private boolean hasLoadedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_quiz);

        if (getIntent() != null && getIntent().hasExtra("quiz_id")) {
            quizId = getIntent().getStringExtra("quiz_id");
        }
        if (quizId == null || quizId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        fetchQuizDetails();
        hasLoadedOnce = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLoadedOnce && layoutTaking != null && layoutTaking.getVisibility() != View.VISIBLE) {
            fetchQuizDetails();
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBackNavigation());

        tvHeaderTitle = findViewById(R.id.tvQuizHeaderTitle);
        tvTimer = findViewById(R.id.tvTimer);
        pbLoading = findViewById(R.id.pbLoading);
        scrollQuizContent = findViewById(R.id.scrollQuizContent);
        layoutQuizError = findViewById(R.id.layoutQuizError);
        tvQuizErrorTitle = findViewById(R.id.tvQuizErrorTitle);
        tvQuizErrorMsg = findViewById(R.id.tvQuizErrorMsg);
        btnRetryQuizLoad = findViewById(R.id.btnRetryQuizLoad);
        btnRetryQuizLoad.setOnClickListener(v -> fetchQuizDetails());

        layoutInstructions = findViewById(R.id.layoutInstructions);
        tvInstructionsTitle = findViewById(R.id.tvInstructionsTitle);
        tvCourseInfo = findViewById(R.id.tvCourseInfo);
        tvQuizMetaDetails = findViewById(R.id.tvQuizMetaDetails);
        tvQuizDueDate = findViewById(R.id.tvQuizDueDate);
        tvInstructionsBody = findViewById(R.id.tvInstructionsBody);
        tvCannotStartReason = findViewById(R.id.tvCannotStartReason);
        cardAttemptSummary = findViewById(R.id.cardAttemptSummary);
        tvAttemptStatusTitle = findViewById(R.id.tvAttemptStatusTitle);
        tvAttemptStatusDetail = findViewById(R.id.tvAttemptStatusDetail);
        btnReviewFromSummary = findViewById(R.id.btnReviewFromSummary);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnStartQuiz.setOnClickListener(v -> startQuizAttempt());

        layoutTaking = findViewById(R.id.layoutTaking);
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        tvAnsweredCounter = findViewById(R.id.tvAnsweredCounter);
        tvQuestionTypeTag = findViewById(R.id.tvQuestionTypeTag);
        tvQuestionMarksTag = findViewById(R.id.tvQuestionMarksTag);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbOptionA = findViewById(R.id.rbOptionA);
        rbOptionB = findViewById(R.id.rbOptionB);
        rbOptionC = findViewById(R.id.rbOptionC);
        rbOptionD = findViewById(R.id.rbOptionD);
        tilShortAnswer = findViewById(R.id.tilShortAnswer);
        etShortAnswerInput = findViewById(R.id.etShortAnswerInput);

        btnPrevQuestion = findViewById(R.id.btnPrevQuestion);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);

        rvQuestionNav = findViewById(R.id.rvQuestionNav);
        rvQuestionNav.setLayoutManager(new GridLayoutManager(this, 5));
        navAdapter = new QuizNavAdapter(selectedAnswers, this::jumpToQuestion);
        rvQuestionNav.setAdapter(navAdapter);

        btnPrevQuestion.setOnClickListener(v -> jumpToQuestion(currentQuestionIndex - 1));
        btnNextQuestion.setOnClickListener(v -> jumpToQuestion(currentQuestionIndex + 1));
        btnSubmitQuiz.setOnClickListener(v -> confirmAndSubmitQuiz());

        rgAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressRadioCallback || currentQuestionIndex >= selectedAnswers.size() || currentQuestionIndex >= currentOptionKeys.size()) {
                return;
            }
            int idx = -1;
            if (checkedId == R.id.rbOptionA) idx = 0;
            else if (checkedId == R.id.rbOptionB) idx = 1;
            else if (checkedId == R.id.rbOptionC) idx = 2;
            else if (checkedId == R.id.rbOptionD) idx = 3;

            List<String> keys = currentOptionKeys.get(currentQuestionIndex);
            if (idx >= 0 && idx < keys.size()) {
                selectedAnswers.set(currentQuestionIndex, keys.get(idx));
                updateAnsweredCounter();
                navAdapter.notifyItemChanged(currentQuestionIndex);
                saveSingleAnswer(currentQuestionIndex);
            }
        });

        layoutResult = findViewById(R.id.layoutResult);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultSubtitle = findViewById(R.id.tvResultSubtitle);
        btnReviewFromResult = findViewById(R.id.btnReviewFromResult);
        btnRetakeFromResult = findViewById(R.id.btnRetakeFromResult);
        btnReturnHub = findViewById(R.id.btnReturnHub);

        etShortAnswerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (suppressShortAnswerCallback || currentQuestionIndex < 0 || currentQuestionIndex >= selectedAnswers.size()) {
                    return;
                }
                String txt = s != null ? s.toString().trim() : "";
                if (!txt.equals(selectedAnswers.get(currentQuestionIndex))) {
                    selectedAnswers.set(currentQuestionIndex, txt);
                    updateAnsweredCounter();
                    navAdapter.notifyItemChanged(currentQuestionIndex);
                }
            }
        });

        btnReviewFromSummary.setOnClickListener(v -> openAnswerReview(latestAttemptId));
        btnReviewFromResult.setOnClickListener(v -> openAnswerReview(attemptId != null ? attemptId : latestAttemptId));
        if (btnRetakeFromResult != null) {
            btnRetakeFromResult.setOnClickListener(v -> {
                attemptId = null;
                fetchQuizDetails();
            });
        }
        btnReturnHub.setOnClickListener(v -> finish());
    }

    private void fetchQuizDetails() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutQuizError.setVisibility(View.GONE);
        scrollQuizContent.setVisibility(View.GONE);

        ApiClient.getApiService(this).getStudentQuizDetail(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    scrollQuizContent.setVisibility(View.VISIBLE);
                    layoutInstructions.setVisibility(View.VISIBLE);
                    layoutTaking.setVisibility(View.GONE);
                    layoutResult.setVisibility(View.GONE);
                    bindQuizInstructions(response.body().getAsJsonObject("data"));
                } else {
                    layoutQuizError.setVisibility(View.VISIBLE);
                    tvQuizErrorTitle.setText("Cannot open quiz");
                    tvQuizErrorMsg.setText(AssessmentUiHelper.extractErrorMessage(response, "Quiz details could not be loaded."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                layoutQuizError.setVisibility(View.VISIBLE);
                tvQuizErrorTitle.setText("Network error");
                tvQuizErrorMsg.setText("Please check your connection and try again.");
            }
        });
    }

    private void bindQuizInstructions(JsonObject data) {
        String title = AssessmentUiHelper.optString(data, "title", "Quiz Assessment");
        tvInstructionsTitle.setText(title);
        tvHeaderTitle.setText(title);

        String cCode = AssessmentUiHelper.optString(data, "course_code", "");
        String cName = AssessmentUiHelper.optString(data, "course_name", "Course");
        String cls = AssessmentUiHelper.optString(data, "class_name", "");
        String sem = AssessmentUiHelper.optString(data, "semester_name", "");
        StringBuilder courseLine = new StringBuilder();
        if (!cCode.isEmpty()) courseLine.append(cCode).append(" – ");
        courseLine.append(cName);
        if (!cls.isEmpty()) courseLine.append(" • ").append(cls);
        if (!sem.isEmpty()) courseLine.append(" • ").append(sem);
        tvCourseInfo.setText(courseLine.toString());

        int totalQuestions = AssessmentUiHelper.optInt(data, "total_questions", 0);
        quizTotalMarks = AssessmentUiHelper.optDouble(data, "total_marks", 0);
        quizDurationMinutes = AssessmentUiHelper.optInt(data, "duration_minutes", 30);

        boolean hasMaxCap = data.has("max_attempts") && !data.get("max_attempts").isJsonNull();
        maxAttemptsCap = hasMaxCap ? AssessmentUiHelper.optInt(data, "max_attempts", 1) : 0;
        attemptsUsedCount = AssessmentUiHelper.optInt(data, "attempts_used", 0);
        String scoringMethod = AssessmentUiHelper.optString(data, "scoring_method", "highest");
        String scoringLabel = "latest".equalsIgnoreCase(scoringMethod) ? "Latest"
                : ("average".equalsIgnoreCase(scoringMethod) ? "Average" : "Highest");
        String attemptSummaryTag = hasMaxCap
                ? String.format(Locale.US, "Attempts: %d/%d", attemptsUsedCount, maxAttemptsCap)
                : String.format(Locale.US, "Attempts: %d/∞", attemptsUsedCount);

        tvQuizMetaDetails.setText(String.format(Locale.US,
                "Questions: %d • Marks: %s • %d mins\n%s • Scoring: %s",
                totalQuestions, AssessmentUiHelper.formatMarks(quizTotalMarks), quizDurationMinutes,
                attemptSummaryTag, scoringLabel));

        String effectiveEndAt = AssessmentUiHelper.optString(data, "effective_end_at", AssessmentUiHelper.optString(data, "end_at", ""));
        String reopenStatus = AssessmentUiHelper.optString(data, "reopen_status", "");
        if (!effectiveEndAt.isEmpty()) {
            tvQuizDueDate.setVisibility(View.VISIBLE);
            StringBuilder dueSb = new StringBuilder("Closes: ").append(AssessmentUiHelper.formatDateTime(effectiveEndAt));
            if (!reopenStatus.isEmpty()) {
                dueSb.append(" [").append(reopenStatus).append("]");
            } else if (data.has("override") && data.get("override").isJsonObject()) {
                dueSb.append(" (Extended/Override Active)");
            }
            tvQuizDueDate.setText(dueSb.toString());
        } else if (!reopenStatus.isEmpty()) {
            tvQuizDueDate.setVisibility(View.VISIBLE);
            tvQuizDueDate.setText("Status: " + reopenStatus);
        } else {
            tvQuizDueDate.setVisibility(View.GONE);
        }

        String instr = AssessmentUiHelper.optString(data, "instructions", "");
        tvInstructionsBody.setText(instr.isEmpty()
                ? "Read each question carefully. Required questions must be answered before submitting."
                : instr);

        boolean canStart = AssessmentUiHelper.optBoolean(data, "can_start", false);
        boolean canContinue = AssessmentUiHelper.optBoolean(data, "can_continue", false);
        boolean allowSubServer = !data.has("allow_submission") || AssessmentUiHelper.optBoolean(data, "allow_submission", true);
        if (!allowSubServer) {
            canStart = false;
            canContinue = false;
        }
        boolean hasPending = AssessmentUiHelper.optBoolean(data, "has_pending_grading", false);
        String windowState = AssessmentUiHelper.optString(data, "window_state", "open");
        String blockedReason = AssessmentUiHelper.optString(data, "blocked_reason", "");

        if (data.has("attempt") && data.get("attempt").isJsonObject()) {
            JsonObject att = data.getAsJsonObject("attempt");
            latestAttemptId = AssessmentUiHelper.optString(att, "id", null);
            String attStatus = AssessmentUiHelper.optString(att, "status", "");
            boolean scoreReleased = AssessmentUiHelper.optBoolean(att, "score_released", false);
            boolean awaitingManual = AssessmentUiHelper.optBoolean(att, "awaiting_manual_grading", false);
            boolean reviewAllowed = AssessmentUiHelper.optBoolean(att, "review_allowed", false);

            if (!"in_progress".equalsIgnoreCase(attStatus) || attemptsUsedCount > 0) {
                cardAttemptSummary.setVisibility(View.VISIBLE);
                if (!hasPending && data.has("effective_score") && !data.get("effective_score").isJsonNull()) {
                    double effSc = data.get("effective_score").getAsDouble();
                    tvAttemptStatusTitle.setText(String.format(Locale.US, "Official Score (%s): %s / %s",
                            scoringLabel, AssessmentUiHelper.formatMarks(effSc), AssessmentUiHelper.formatMarks(quizTotalMarks)));
                } else if (scoreReleased && att.has("score") && !att.get("score").isJsonNull() && !hasPending) {
                    double sc = att.get("score").getAsDouble();
                    tvAttemptStatusTitle.setText("Completed • Score: " + AssessmentUiHelper.formatMarks(sc) + " / " + AssessmentUiHelper.formatMarks(quizTotalMarks));
                } else if (awaitingManual || hasPending) {
                    tvAttemptStatusTitle.setText("Submitted • Pending Grading");
                } else if ("in_progress".equalsIgnoreCase(attStatus)) {
                    tvAttemptStatusTitle.setText("Attempt In Progress");
                } else {
                    tvAttemptStatusTitle.setText("Submitted");
                }

                StringBuilder historySb = new StringBuilder();
                if (awaitingManual || hasPending) {
                    historySb.append("Short-answer questions are awaiting manual teacher grading. Your final score will appear once graded.");
                } else if (scoreReleased) {
                    historySb.append("Your quiz has been graded.");
                } else {
                    historySb.append("Your attempt has been recorded.");
                }
                if (data.has("attempts_history") && data.get("attempts_history").isJsonArray()) {
                    JsonArray hist = data.getAsJsonArray("attempts_history");
                    if (hist.size() > 0) {
                        historySb.append("\n\nAttempt History:");
                        for (JsonElement hEl : hist) {
                            if (!hEl.isJsonObject()) continue;
                            JsonObject hObj = hEl.getAsJsonObject();
                            int num = AssessmentUiHelper.optInt(hObj, "attempt_number", 1);
                            boolean resetFlag = AssessmentUiHelper.optBoolean(hObj, "is_reset", false);
                            boolean hPending = AssessmentUiHelper.optBoolean(hObj, "awaiting_manual_grading", false);
                            boolean hReleased = AssessmentUiHelper.optBoolean(hObj, "score_released", false);
                            String hStatus = AssessmentUiHelper.optString(hObj, "status", "");
                            historySb.append("\n• Attempt #").append(num).append(": ");
                            if (resetFlag) {
                                historySb.append("Reset by Teacher");
                            } else if ("in_progress".equalsIgnoreCase(hStatus)) {
                                historySb.append("In Progress");
                            } else if (hPending) {
                                historySb.append("Pending Grading");
                            } else if (hReleased && hObj.has("score") && !hObj.get("score").isJsonNull()) {
                                double hs = hObj.get("score").getAsDouble();
                                historySb.append(AssessmentUiHelper.formatMarks(hs)).append(" / ").append(AssessmentUiHelper.formatMarks(quizTotalMarks));
                            } else {
                                historySb.append("Submitted");
                            }
                        }
                    }
                }
                tvAttemptStatusDetail.setText(historySb.toString());
                btnReviewFromSummary.setVisibility(reviewAllowed ? View.VISIBLE : View.GONE);
                if ((awaitingManual || hasPending) && latestAttemptId != null && !latestAttemptId.isEmpty()) {
                    fetchProvisionalSummaryForDisplay(latestAttemptId, true);
                }
            } else {
                cardAttemptSummary.setVisibility(View.GONE);
            }
        } else {
            cardAttemptSummary.setVisibility(View.GONE);
        }

        if (canContinue) {
            btnStartQuiz.setVisibility(View.VISIBLE);
            btnStartQuiz.setEnabled(true);
            btnStartQuiz.setText("Continue Quiz");
            tvCannotStartReason.setVisibility(View.GONE);
        } else if (canStart) {
            btnStartQuiz.setVisibility(View.VISIBLE);
            btnStartQuiz.setEnabled(true);
            btnStartQuiz.setText(attemptsUsedCount > 0 ? "Retake Quiz" : "Start Quiz Now");
            tvCannotStartReason.setVisibility(View.GONE);
        } else {
            btnStartQuiz.setVisibility(View.GONE);
            if (!blockedReason.isEmpty()) {
                tvCannotStartReason.setVisibility(View.VISIBLE);
                tvCannotStartReason.setText(blockedReason);
            } else if (cardAttemptSummary.getVisibility() != View.VISIBLE) {
                tvCannotStartReason.setVisibility(View.VISIBLE);
                if ("upcoming".equalsIgnoreCase(windowState)) {
                    tvCannotStartReason.setText("This quiz has not opened yet.");
                } else if ("closed".equalsIgnoreCase(windowState)) {
                    tvCannotStartReason.setText("This quiz is closed.");
                } else if (totalQuestions <= 0) {
                    tvCannotStartReason.setText("This quiz does not have any questions yet.");
                } else {
                    tvCannotStartReason.setText("This quiz is currently unavailable.");
                }
            } else {
                tvCannotStartReason.setVisibility(View.GONE);
            }
        }
    }

    private void startQuizAttempt() {
        pbLoading.setVisibility(View.VISIBLE);
        btnStartQuiz.setEnabled(false);

        ApiClient.getApiService(this).startStudentQuiz(quizId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnStartQuiz.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    attemptId = AssessmentUiHelper.optString(data, "attempt_id", null);

                    questions.clear();
                    selectedAnswers.clear();
                    currentOptionKeys.clear();

                    JsonObject savedMap = (data.has("answers") && data.get("answers").isJsonObject())
                            ? data.getAsJsonObject("answers") : new JsonObject();

                    if (data.has("questions") && data.get("questions").isJsonArray()) {
                        JsonArray qArr = data.getAsJsonArray("questions");
                        for (JsonElement el : qArr) {
                            if (!el.isJsonObject()) continue;
                            JsonObject qObj = el.getAsJsonObject();
                            questions.add(qObj);

                            String qIdStr = AssessmentUiHelper.optString(qObj, "id", "");
                            String prevAns = savedMap.has(qIdStr) && !savedMap.get(qIdStr).isJsonNull()
                                    ? savedMap.get(qIdStr).getAsString() : "";
                            selectedAnswers.add(prevAns);

                            List<String> keys = new ArrayList<>();
                            if (qObj.has("options") && qObj.get("options").isJsonArray()) {
                                for (JsonElement oEl : qObj.getAsJsonArray("options")) {
                                    if (oEl.isJsonObject()) {
                                        keys.add(AssessmentUiHelper.optString(oEl.getAsJsonObject(), "key", ""));
                                    }
                                }
                            }
                            currentOptionKeys.add(keys);
                        }
                    }

                    if (questions.isEmpty()) {
                        Toast.makeText(TakeQuizActivity.this, "No questions returned for this quiz.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long remainingMs = computeRemainingMillis(data);
                    showTakingScreen(remainingMs);
                } else {
                    String msg = AssessmentUiHelper.extractErrorMessage(response, "Unable to start quiz");
                    Toast.makeText(TakeQuizActivity.this, msg, Toast.LENGTH_LONG).show();
                    fetchQuizDetails();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnStartQuiz.setEnabled(true);
                Toast.makeText(TakeQuizActivity.this, "Network error starting quiz. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private long computeRemainingMillis(JsonObject data) {
        String expiresAt = AssessmentUiHelper.optString(data, "expires_at", "");
        String serverTime = AssessmentUiHelper.optString(data, "server_time", "");
        Date exp = AssessmentUiHelper.parseIsoDate(expiresAt);
        Date srv = AssessmentUiHelper.parseIsoDate(serverTime);
        if (exp != null && srv != null) {
            long diff = exp.getTime() - srv.getTime();
            return Math.max(diff, 10_000L);
        }
        return (quizDurationMinutes > 0 ? quizDurationMinutes : 30) * 60_000L;
    }

    private void showTakingScreen(long remainingMs) {
        layoutInstructions.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        layoutTaking.setVisibility(View.VISIBLE);
        tvTimer.setVisibility(View.VISIBLE);

        startTimer(remainingMs);
        currentQuestionIndex = 0;
        displayQuestion(0);
    }

    private void persistCurrentShortAnswer() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) return;
        JsonObject q = questions.get(currentQuestionIndex);
        String qType = AssessmentUiHelper.optString(q, "question_type", "multiple_choice");
        if ("short_answer".equalsIgnoreCase(qType) && etShortAnswerInput != null) {
            String txt = etShortAnswerInput.getText() != null ? etShortAnswerInput.getText().toString().trim() : "";
            String old = selectedAnswers.get(currentQuestionIndex);
            if (!txt.equals(old)) {
                selectedAnswers.set(currentQuestionIndex, txt);
                navAdapter.notifyItemChanged(currentQuestionIndex);
                updateAnsweredCounter();
                saveSingleAnswer(currentQuestionIndex);
            }
        }
    }

    private void jumpToQuestion(int target) {
        if (target < 0 || target >= questions.size()) return;
        persistCurrentShortAnswer();
        currentQuestionIndex = target;
        displayQuestion(currentQuestionIndex);
    }

    private void displayQuestion(int index) {
        tvQuestionProgress.setText(String.format(Locale.US, "Question %d of %d", index + 1, questions.size()));
        updateAnsweredCounter();
        navAdapter.setCurrentIndex(index);

        JsonObject q = questions.get(index);
        String qType = AssessmentUiHelper.optString(q, "question_type", "multiple_choice");
        double marks = AssessmentUiHelper.optDouble(q, "marks", 1);
        boolean isRequired = !q.has("is_required") || q.get("is_required").isJsonNull() || q.get("is_required").getAsBoolean();
        tvQuestionTypeTag.setText("true_false".equalsIgnoreCase(qType) ? "True / False"
                : ("short_answer".equalsIgnoreCase(qType) ? "Short Answer" : "Multiple Choice"));
        tvQuestionMarksTag.setText(AssessmentUiHelper.formatMarks(marks) + (marks == 1.0 ? " mark" : " marks")
                + (isRequired ? " • Required" : " • Optional"));
        tvQuestionText.setText(AssessmentUiHelper.optString(q, "question_text", "Question " + (index + 1)));

        String currentAnswer = selectedAnswers.get(index);

        if ("short_answer".equalsIgnoreCase(qType)) {
            rgAnswers.setVisibility(View.GONE);
            tilShortAnswer.setVisibility(View.VISIBLE);
            suppressShortAnswerCallback = true;
            etShortAnswerInput.setText(currentAnswer != null ? currentAnswer : "");
            suppressShortAnswerCallback = false;
        } else {
            tilShortAnswer.setVisibility(View.GONE);
            rgAnswers.setVisibility(View.VISIBLE);

            RadioButton[] radios = new RadioButton[]{rbOptionA, rbOptionB, rbOptionC, rbOptionD};
            JsonArray options = q.has("options") && q.get("options").isJsonArray() ? q.getAsJsonArray("options") : new JsonArray();

            suppressRadioCallback = true;
            rgAnswers.clearCheck();
            for (int i = 0; i < radios.length; i++) {
                if (i < options.size() && options.get(i).isJsonObject()) {
                    JsonObject opt = options.get(i).getAsJsonObject();
                    String key = AssessmentUiHelper.optString(opt, "key", "");
                    String text = AssessmentUiHelper.optString(opt, "text", "");
                    radios[i].setVisibility(View.VISIBLE);
                    radios[i].setText("true_false".equalsIgnoreCase(qType) ? text : (key + ". " + text));
                    if (key.equalsIgnoreCase(currentAnswer)) {
                        radios[i].setChecked(true);
                    }
                } else {
                    radios[i].setVisibility(View.GONE);
                }
            }
            suppressRadioCallback = false;
        }

        btnPrevQuestion.setEnabled(index > 0);
        btnNextQuestion.setEnabled(index < questions.size() - 1);
    }

    private void updateAnsweredCounter() {
        int answered = countAnswered();
        tvAnsweredCounter.setText(String.format(Locale.US, "Answered: %d / %d", answered, questions.size()));
    }

    private int countAnswered() {
        int c = 0;
        for (String a : selectedAnswers) {
            if (a != null && !a.trim().isEmpty()) c++;
        }
        return c;
    }

    private int findFirstUnansweredIndex() {
        for (int i = 0; i < selectedAnswers.size(); i++) {
            String a = selectedAnswers.get(i);
            if (a == null || a.trim().isEmpty()) return i;
        }
        return -1;
    }

    private void saveSingleAnswer(int qIndex) {
        if (attemptId == null || qIndex < 0 || qIndex >= questions.size()) return;
        JsonObject row = new JsonObject();
        row.addProperty("question_id", AssessmentUiHelper.optInt(questions.get(qIndex), "id", 0));
        row.addProperty("answer", selectedAnswers.get(qIndex));
        JsonArray arr = new JsonArray();
        arr.add(row);
        JsonObject body = new JsonObject();
        body.add("answers", arr);

        ApiClient.getApiService(this).saveStudentQuizAnswers(attemptId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {}
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private List<Integer> findUnansweredRequiredIndices() {
        List<Integer> missing = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            JsonObject q = questions.get(i);
            boolean isRequired = !q.has("is_required") || q.get("is_required").isJsonNull() || q.get("is_required").getAsBoolean();
            if (!isRequired) continue;
            String a = i < selectedAnswers.size() ? selectedAnswers.get(i) : "";
            if (a == null || a.trim().isEmpty()) {
                missing.add(i);
            }
        }
        return missing;
    }

    private void confirmAndSubmitQuiz() {
        persistCurrentShortAnswer();
        List<Integer> missingRequired = findUnansweredRequiredIndices();
        int answered = countAnswered();
        int unanswered = questions.size() - answered;

        if (!missingRequired.isEmpty()) {
            StringBuilder missingNums = new StringBuilder();
            for (int i = 0; i < missingRequired.size(); i++) {
                if (i > 0) missingNums.append(", ");
                missingNums.append("Q").append(missingRequired.get(i) + 1);
            }
            int firstMissing = missingRequired.get(0);
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Quiz")
                    .setMessage(String.format(Locale.US,
                            "Please answer all required questions before submitting.\n\nAnswered: %d of %d\nUnanswered Required: %s",
                            answered, questions.size(), missingNums.toString()))
                    .setPositiveButton("Review Q" + (firstMissing + 1), (d, w) -> jumpToQuestion(firstMissing))
                    .setNegativeButton("Close", null)
                    .show();
            return;
        }

        String msg = String.format(Locale.US,
                "Answered: %d of %d\nUnanswered Optional: %d\n\n%s",
                answered, questions.size(), unanswered,
                "Ready to submit your quiz now?");

        new AlertDialog.Builder(this)
                .setTitle(R.string.submit_quiz)
                .setMessage(msg)
                .setPositiveButton(R.string.submit, (dialog, which) -> submitQuizFinal())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitQuizFinal() {
        persistCurrentShortAnswer();
        if (attemptId == null) return;

        pbLoading.setVisibility(View.VISIBLE);
        btnSubmitQuiz.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        JsonArray answersArr = new JsonArray();
        for (int i = 0; i < questions.size(); i++) {
            JsonObject row = new JsonObject();
            row.addProperty("question_id", AssessmentUiHelper.optInt(questions.get(i), "id", 0));
            row.addProperty("answer", i < selectedAnswers.size() ? selectedAnswers.get(i) : "");
            answersArr.add(row);
        }
        JsonObject body = new JsonObject();
        body.add("answers", answersArr);

        ApiClient.getApiService(this).submitStudentQuiz(attemptId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnSubmitQuiz.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    showResultScreen(response.body().getAsJsonObject("data"));
                } else if (response.code() == 409 && response.body() != null && response.body().has("data")) {
                    showResultScreen(response.body().getAsJsonObject("data"));
                } else if (response.code() == 422) {
                    String errMsg = AssessmentUiHelper.extractErrorMessage(response, "Please answer all required questions before submitting.");
                    int targetIdx = -1;
                    try {
                        if (response.errorBody() != null) {
                            String rawErr = response.errorBody().string();
                            JsonObject errObj = new com.google.gson.Gson().fromJson(rawErr, JsonObject.class);
                            if (errObj != null) {
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errMsg = errObj.get("message").getAsString();
                                }
                                if (errObj.has("unanswered_question_numbers") && errObj.get("unanswered_question_numbers").isJsonArray()) {
                                    JsonArray nums = errObj.getAsJsonArray("unanswered_question_numbers");
                                    if (nums.size() > 0) targetIdx = nums.get(0).getAsInt() - 1;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    final int jumpIdx = targetIdx;
                    AlertDialog.Builder dlg = new AlertDialog.Builder(TakeQuizActivity.this)
                            .setTitle("Incomplete Submission Blocked")
                            .setMessage(errMsg)
                            .setNegativeButton("Close", null);
                    if (jumpIdx >= 0 && jumpIdx < questions.size()) {
                        dlg.setPositiveButton("Go to Q" + (jumpIdx + 1), (d, w) -> jumpToQuestion(jumpIdx));
                    }
                    dlg.show();
                } else {
                    Toast.makeText(TakeQuizActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to submit quiz"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnSubmitQuiz.setEnabled(true);
                Toast.makeText(TakeQuizActivity.this, "Network error submitting quiz. Please retry.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showResultScreen(JsonObject data) {
        layoutTaking.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);

        boolean scoreReleased = AssessmentUiHelper.optBoolean(data, "score_released", false);
        boolean awaitingManual = AssessmentUiHelper.optBoolean(data, "awaiting_manual_grading", false);
        boolean reviewAllowed = AssessmentUiHelper.optBoolean(data, "review_allowed", false);
        double total = AssessmentUiHelper.optDouble(data, "total_marks", quizTotalMarks);

        if (scoreReleased && data.has("score") && !data.get("score").isJsonNull() && !awaitingManual) {
            double earned = data.get("score").getAsDouble();
            int pct = total > 0 ? Math.round((float) ((earned / total) * 100.0)) : 0;
            tvResultTitle.setText(String.format(Locale.US, "Quiz Completed! Score: %s / %s (%d%%)",
                    AssessmentUiHelper.formatMarks(earned), AssessmentUiHelper.formatMarks(total), pct));
            tvResultSubtitle.setText("Your answers have been recorded and graded.");
        } else if (awaitingManual) {
            tvResultTitle.setText("Quiz Submitted! • Pending Grading");
            tvResultSubtitle.setText("Short-answer questions are awaiting manual teacher grading. Your final score will appear once graded.");
            String targetId = attemptId != null ? attemptId : latestAttemptId;
            if (targetId != null && !targetId.isEmpty()) {
                fetchProvisionalSummaryForDisplay(targetId, false);
            }
        } else {
            tvResultTitle.setText(R.string.quiz_submitted_successfully);
            tvResultSubtitle.setText("Your attempt has been recorded.");
        }

        btnReviewFromResult.setVisibility(reviewAllowed ? View.VISIBLE : View.GONE);
        if (btnRetakeFromResult != null) {
            int attemptNum = AssessmentUiHelper.optInt(data, "attempt_number", attemptsUsedCount + 1);
            boolean canRetake = maxAttemptsCap <= 0 || attemptNum < maxAttemptsCap;
            btnRetakeFromResult.setVisibility(canRetake ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchProvisionalSummaryForDisplay(String targetAttemptId, boolean forSummaryCard) {
        ApiClient.getApiService(this).reviewStudentQuizAttempt(targetAttemptId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    return;
                }
                QuizScoreUiHelper.ReviewScoreSummary summary =
                        QuizScoreUiHelper.parseStudentReviewSummary(response.body().getAsJsonObject("data"));
                if (forSummaryCard) {
                    if (summary.isProvisional) {
                        tvAttemptStatusTitle.setText(summary.formatScoreLine() + " • " + summary.gradingStatus);
                    } else {
                        tvAttemptStatusTitle.setText(summary.formatScoreLine() + " • " + summary.gradingStatus);
                    }
                } else if (summary.isProvisional) {
                    tvResultTitle.setText(summary.formatScoreLine() + " (" + summary.gradingStatus + ")");
                    String note = summary.formatProvisionalNote();
                    if (note != null) {
                        tvResultSubtitle.setText(note);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void openAnswerReview(String targetAttemptId) {
        if (targetAttemptId == null || targetAttemptId.isEmpty()) {
            Toast.makeText(this, "No submitted attempt found to review.", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, StudentQuizReviewActivity.class);
        intent.putExtra(StudentQuizReviewActivity.EXTRA_ATTEMPT_ID, targetAttemptId);
        if (tvInstructionsTitle != null && tvInstructionsTitle.getText() != null) {
            intent.putExtra(StudentQuizReviewActivity.EXTRA_QUIZ_TITLE, tvInstructionsTitle.getText().toString());
        }
        if (tvCourseInfo != null && tvCourseInfo.getText() != null) {
            intent.putExtra(StudentQuizReviewActivity.EXTRA_COURSE_INFO, tvCourseInfo.getText().toString());
        }
        startActivity(intent);
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = millisUntilFinished / 60000;
                long secs = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                Toast.makeText(TakeQuizActivity.this, "Time expired! Submitting quiz...", Toast.LENGTH_LONG).show();
                submitQuizFinal();
            }
        }.start();
    }

    private void handleBackNavigation() {
        if (layoutTaking.getVisibility() == View.VISIBLE) {
            persistCurrentShortAnswer();
            new AlertDialog.Builder(this)
                    .setTitle("Leave Quiz?")
                    .setMessage("Your answers are saved, and you can continue until the timer expires.")
                    .setPositiveButton("Leave", (dialog, which) -> finish())
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
