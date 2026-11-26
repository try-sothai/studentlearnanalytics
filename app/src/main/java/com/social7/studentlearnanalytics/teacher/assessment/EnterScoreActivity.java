package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.SubmissionFileViewerHelper;

import java.io.File;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Teacher grading screen for student assignment submissions.
 * Displays complete student/assessment metadata, renders/streams the student's actual submitted file
 * directly on the screen (with full-screen zoom, PDF page 1 preview, open, and download),
 * and submits score + feedback against the exact verified submission ID.
 */
public class EnterScoreActivity extends AppCompatActivity {

    private TextView tvAssessmentTitleHeader, tvAssessmentTypeBadge, tvCourseClassHeader,
            tvGradingStudentId, tvGradingStudentName, tvSubmissionStatusBadge, tvSubmissionTimestamp,
            tvSubFileTypeBadge, tvSubFileName, tvSubFileSize, tvInlinePreviewHint, tvSubFileNotice;
    private MaterialCardView cardSubmittedFile;
    private FrameLayout layoutInlinePreviewBox;
    private LinearLayout layoutSubFileActions;
    private ImageView ivInlineSubmissionPreview;
    private ProgressBar pbInlinePreview;

    private TextInputLayout tilAwardedScore;
    private TextInputEditText etStudentName, etAwardedScore, etFeedback;
    private MaterialButton btnPreviewFullScreen, btnOpenSubFileExternal, btnDownloadSubFile, btnSaveDraftScore, btnSaveScore;

    private int submissionId = 0;
    private double maxScore = 100.0;
    private String assessmentTitle = "";
    private String assessmentType = "Assignment";
    private String courseCode = "";
    private String courseName = "";
    private String studentName = "";
    private String studentCode = "";
    private String submittedAt = "";
    private String submissionStatus = "";

    private String fileName = "";
    private String fileType = "";
    private String fileSizeLabel = "";
    private String mimeType = "";
    private String previewType = "";
    private boolean hasFile = false;
    private boolean fileExists = false;
    private File cachedSubmissionFile = null;
    private Call<ResponseBody> activeFileCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Teacher", "Admin")) {
            return;
        }
        setContentView(R.layout.activity_enter_score);

        TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Grade Submission", "", true);

        bindViews();
        readIntentExtras();
        renderSubmissionMetadata();
        setupListeners();

        if (submissionId > 0) {
            fetchSubmissionDetail();
        } else {
            showNoSubmissionContext();
        }
    }

    private void bindViews() {
        tvAssessmentTitleHeader = findViewById(R.id.tvAssessmentTitleHeader);
        tvAssessmentTypeBadge = findViewById(R.id.tvAssessmentTypeBadge);
        tvCourseClassHeader = findViewById(R.id.tvCourseClassHeader);
        tvGradingStudentId = findViewById(R.id.tvGradingStudentId);
        tvGradingStudentName = findViewById(R.id.tvGradingStudentName);
        tvSubmissionStatusBadge = findViewById(R.id.tvSubmissionStatusBadge);
        tvSubmissionTimestamp = findViewById(R.id.tvSubmissionTimestamp);

        cardSubmittedFile = findViewById(R.id.cardSubmittedFile);
        tvSubFileTypeBadge = findViewById(R.id.tvSubFileTypeBadge);
        tvSubFileName = findViewById(R.id.tvSubFileName);
        tvSubFileSize = findViewById(R.id.tvSubFileSize);
        tvInlinePreviewHint = findViewById(R.id.tvInlinePreviewHint);
        tvSubFileNotice = findViewById(R.id.tvSubFileNotice);
        layoutInlinePreviewBox = findViewById(R.id.layoutInlinePreviewBox);
        layoutSubFileActions = findViewById(R.id.layoutSubFileActions);
        ivInlineSubmissionPreview = findViewById(R.id.ivInlineSubmissionPreview);
        pbInlinePreview = findViewById(R.id.pbInlinePreview);

        tilAwardedScore = findViewById(R.id.tilAwardedScore);
        etStudentName = findViewById(R.id.etStudentName);
        etAwardedScore = findViewById(R.id.etAwardedScore);
        etFeedback = findViewById(R.id.etFeedback);

        btnPreviewFullScreen = findViewById(R.id.btnPreviewFullScreen);
        btnOpenSubFileExternal = findViewById(R.id.btnOpenSubFileExternal);
        btnDownloadSubFile = findViewById(R.id.btnDownloadSubFile);
        btnSaveDraftScore = findViewById(R.id.btnSaveDraftScore);
        btnSaveScore = findViewById(R.id.btnSaveScore);
    }

    private void readIntentExtras() {
        Intent i = getIntent();
        if (i == null) return;

        submissionId = i.getIntExtra("submission_id_int", 0);
        if (submissionId <= 0) {
            String subIdStr = i.getStringExtra("submission_id");
            if (subIdStr != null) {
                try {
                    submissionId = Integer.parseInt(subIdStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        assessmentTitle = nonNull(i.getStringExtra("assessment_title"));
        assessmentType = nonNullOr(i.getStringExtra("assessment_type"), "Assignment");
        courseCode = nonNull(i.getStringExtra("course_code"));
        courseName = nonNull(i.getStringExtra("course_name"));
        studentName = nonNull(i.getStringExtra("student_name"));
        studentCode = nonNull(i.getStringExtra("student_code"));
        submittedAt = nonNull(i.getStringExtra("submitted_at"));
        submissionStatus = nonNullOr(i.getStringExtra("status"), "Submitted");
        double intentMax = i.getDoubleExtra("max_score", 0.0);
        if (intentMax > 0) maxScore = intentMax;

        if (i.hasExtra("existing_score") && etAwardedScore != null) {
            double sc = i.getDoubleExtra("existing_score", -1);
            if (sc >= 0) {
                etAwardedScore.setText(formatScore(sc));
            }
        }
        String fb = i.getStringExtra("existing_feedback");
        if (fb != null && !fb.isEmpty() && etFeedback != null) {
            etFeedback.setText(fb);
        }

        fileName = nonNull(i.getStringExtra("file_name"));
        fileType = nonNull(i.getStringExtra("file_type"));
        fileSizeLabel = nonNull(i.getStringExtra("file_size_label"));
        mimeType = nonNull(i.getStringExtra("mime_type"));
        previewType = nonNull(i.getStringExtra("preview_type"));
        hasFile = i.getBooleanExtra("has_file", false);
        fileExists = i.getBooleanExtra("file_exists", false);
    }

    private void renderSubmissionMetadata() {
        if (tvAssessmentTitleHeader != null) {
            tvAssessmentTitleHeader.setText(assessmentTitle.isEmpty() ? "Assignment Submission" : assessmentTitle);
        }
        if (tvAssessmentTypeBadge != null) {
            tvAssessmentTypeBadge.setText(assessmentType.isEmpty() ? "Assignment" : assessmentType);
        }
        if (tvCourseClassHeader != null) {
            String courseDisplay = courseCode.isEmpty()
                    ? (courseName.isEmpty() ? "Course" : courseName)
                    : (courseCode + (courseName.isEmpty() ? "" : " - " + courseName));
            tvCourseClassHeader.setText(String.format(Locale.US, "Course: %s • Max Score: %s pts", courseDisplay, formatScore(maxScore)));
        }
        if (tilAwardedScore != null) {
            tilAwardedScore.setHint(String.format(Locale.US, "Awarded Score (Max: %s pts)", formatScore(maxScore)));
        }

        if (tvGradingStudentId != null) {
            tvGradingStudentId.setText(studentCode.isEmpty() ? "STU" : studentCode);
        }
        if (tvGradingStudentName != null) {
            tvGradingStudentName.setText(studentName.isEmpty() ? "Student" : studentName);
        }
        if (etStudentName != null) {
            String combined = studentName.isEmpty()
                    ? studentCode
                    : (studentCode.isEmpty() ? studentName : studentName + " (" + studentCode + ")");
            etStudentName.setText(combined);
        }
        if (tvSubmissionStatusBadge != null) {
            String st = submissionStatus.isEmpty() ? "Submitted" : submissionStatus;
            tvSubmissionStatusBadge.setText(st);
            if (st.contains("Graded")) {
                tvSubmissionStatusBadge.setTextColor(Color.parseColor("#10B981"));
            } else if (st.contains("Late")) {
                tvSubmissionStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
            } else {
                tvSubmissionStatusBadge.setTextColor(Color.parseColor("#2563EB"));
            }
        }
        if (tvSubmissionTimestamp != null) {
            tvSubmissionTimestamp.setText("Submitted: " + (submittedAt.isEmpty() ? "-" : submittedAt));
        }

        renderFileCardState();
    }

    private void renderFileCardState() {
        if (cardSubmittedFile == null) return;
        cardSubmittedFile.setVisibility(View.VISIBLE);

        if (tvSubFileTypeBadge != null) {
            tvSubFileTypeBadge.setText(fileType.isEmpty() ? (hasFile ? "File" : "Online") : fileType);
        }
        if (tvSubFileName != null) {
            tvSubFileName.setText(fileName.isEmpty() ? getString(R.string.no_submission_file_attached) : fileName);
        }
        if (tvSubFileSize != null) {
            tvSubFileSize.setText(fileSizeLabel.isEmpty() ? (hasFile ? "Attachment ready" : "") : "Size: " + fileSizeLabel);
        }

        if (!hasFile) {
            if (layoutInlinePreviewBox != null) layoutInlinePreviewBox.setVisibility(View.GONE);
            if (layoutSubFileActions != null) layoutSubFileActions.setVisibility(View.GONE);
            if (tvSubFileNotice != null) {
                tvSubFileNotice.setVisibility(View.VISIBLE);
                tvSubFileNotice.setText(R.string.no_submission_file_attached);
            }
            return;
        }

        if (!fileExists) {
            if (layoutInlinePreviewBox != null) layoutInlinePreviewBox.setVisibility(View.GONE);
            if (layoutSubFileActions != null) layoutSubFileActions.setVisibility(View.GONE);
            if (tvSubFileNotice != null) {
                tvSubFileNotice.setVisibility(View.VISIBLE);
                tvSubFileNotice.setText(R.string.submission_file_missing);
            }
            return;
        }

        if (tvSubFileNotice != null) tvSubFileNotice.setVisibility(View.GONE);
        if (layoutSubFileActions != null) layoutSubFileActions.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        View.OnClickListener openFullScreen = v -> openFullScreenViewer();
        if (btnPreviewFullScreen != null) btnPreviewFullScreen.setOnClickListener(openFullScreen);
        if (ivInlineSubmissionPreview != null) ivInlineSubmissionPreview.setOnClickListener(openFullScreen);

        if (btnOpenSubFileExternal != null) {
            btnOpenSubFileExternal.setOnClickListener(v -> {
                if (cachedSubmissionFile != null && cachedSubmissionFile.exists()) {
                    SubmissionFileViewerHelper.openWithExternalViewer(this, cachedSubmissionFile, mimeType);
                } else {
                    openFullScreenViewer();
                }
            });
        }
        if (btnDownloadSubFile != null) {
            btnDownloadSubFile.setOnClickListener(v -> {
                if (cachedSubmissionFile != null && cachedSubmissionFile.exists()) {
                    SubmissionFileViewerHelper.saveToDownloads(
                            this,
                            cachedSubmissionFile,
                            fileName.isEmpty() ? cachedSubmissionFile.getName() : fileName,
                            mimeType
                    );
                } else {
                    Toast.makeText(this, "Preparing file download...", Toast.LENGTH_SHORT).show();
                    downloadAndSaveFile();
                }
            });
        }

        if (btnSaveDraftScore != null) {
            btnSaveDraftScore.setOnClickListener(v -> saveScore(false));
        }
        if (btnSaveScore != null) {
            btnSaveScore.setOnClickListener(v -> saveScore(true));
        }
    }

    private void fetchSubmissionDetail() {
        ApiClient.getApiService(this).getTeacherSubmissionDetail(submissionId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    if (response.code() == 403 || response.code() == 404) {
                        Toast.makeText(EnterScoreActivity.this,
                                response.code() == 403 ? "Unauthorized to access this submission." : "Submission not found.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }

                JsonObject d = response.body().getAsJsonObject("data");
                studentCode = getStr(d, "student_code", studentCode);
                studentName = getStr(d, "student_name", studentName);
                assessmentTitle = getStr(d, "assignment_title", assessmentTitle);
                assessmentType = getStr(d, "assessment_type", assessmentType);
                courseCode = getStr(d, "course_code", courseCode);
                courseName = getStr(d, "course_name", courseName);
                submittedAt = getStr(d, "submitted_at", submittedAt);
                submissionStatus = getStr(d, "status", submissionStatus);
                if (d.has("max_score") && !d.get("max_score").isJsonNull()) {
                    double m = d.get("max_score").getAsDouble();
                    if (m > 0) maxScore = m;
                }

                if (d.has("score") && !d.get("score").isJsonNull() && etAwardedScore != null
                        && (etAwardedScore.getText() == null || etAwardedScore.getText().toString().trim().isEmpty())) {
                    etAwardedScore.setText(formatScore(d.get("score").getAsDouble()));
                }
                if (d.has("feedback") && !d.get("feedback").isJsonNull() && etFeedback != null
                        && (etFeedback.getText() == null || etFeedback.getText().toString().trim().isEmpty())) {
                    String fb = d.get("feedback").getAsString();
                    if (!fb.isEmpty()) etFeedback.setText(fb);
                }

                fileName = getStr(d, "file_name", fileName);
                fileType = getStr(d, "file_type", fileType);
                fileSizeLabel = getStr(d, "file_size_label", fileSizeLabel);
                mimeType = getStr(d, "mime_type", mimeType);
                previewType = getStr(d, "preview_type", previewType);
                hasFile = d.has("has_file") && !d.get("has_file").isJsonNull() && d.get("has_file").getAsBoolean();
                fileExists = d.has("file_exists") && !d.get("file_exists").isJsonNull() && d.get("file_exists").getAsBoolean();

                renderSubmissionMetadata();
                if (hasFile && fileExists) {
                    loadInlinePreview();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (hasFile && fileExists) {
                    loadInlinePreview();
                }
            }
        });
    }

    private void loadInlinePreview() {
        String effectivePreview = SubmissionFileViewerHelper.inferPreviewType(previewType, fileName, mimeType);
        boolean supportsInline = "image".equals(effectivePreview) || "pdf".equals(effectivePreview);
        if (supportsInline && layoutInlinePreviewBox != null && pbInlinePreview != null) {
            layoutInlinePreviewBox.setVisibility(View.VISIBLE);
            pbInlinePreview.setVisibility(View.VISIBLE);
        }

        if (activeFileCall != null) {
            activeFileCall.cancel();
        }
        activeFileCall = SubmissionFileViewerHelper.fetchSubmissionFile(
                this,
                submissionId,
                fileName,
                mimeType,
                new SubmissionFileViewerHelper.FileFetchCallback() {
                    @Override
                    public void onSuccess(File cachedFile, String resolvedMime) {
                        if (isFinishing() || isDestroyed()) return;
                        cachedSubmissionFile = cachedFile;
                        if (mimeType.isEmpty() && resolvedMime != null) mimeType = resolvedMime;
                        if (pbInlinePreview != null) pbInlinePreview.setVisibility(View.GONE);

                        if ("image".equals(effectivePreview)) {
                            Bitmap bmp = SubmissionFileViewerHelper.decodeSampledBitmap(cachedFile, 1280);
                            if (bmp != null && ivInlineSubmissionPreview != null) {
                                layoutInlinePreviewBox.setVisibility(View.VISIBLE);
                                ivInlineSubmissionPreview.setImageBitmap(bmp);
                                if (tvInlinePreviewHint != null) {
                                    tvInlinePreviewHint.setText("Tap image to zoom full screen");
                                }
                            } else if (layoutInlinePreviewBox != null) {
                                layoutInlinePreviewBox.setVisibility(View.GONE);
                            }
                        } else if ("pdf".equals(effectivePreview)) {
                            new Thread(() -> {
                                int count = SubmissionFileViewerHelper.getPdfPageCount(cachedFile);
                                Bitmap thumb = count > 0 ? SubmissionFileViewerHelper.renderPdfPage(cachedFile, 0, 960) : null;
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    if (thumb != null && ivInlineSubmissionPreview != null) {
                                        layoutInlinePreviewBox.setVisibility(View.VISIBLE);
                                        ivInlineSubmissionPreview.setImageBitmap(thumb);
                                        if (tvInlinePreviewHint != null) {
                                            tvInlinePreviewHint.setText(String.format(Locale.US, "PDF Page 1 of %d • Tap to view all", count));
                                        }
                                    } else {
                                        if (layoutInlinePreviewBox != null) layoutInlinePreviewBox.setVisibility(View.GONE);
                                        if (tvSubFileNotice != null) {
                                            tvSubFileNotice.setVisibility(View.VISIBLE);
                                            tvSubFileNotice.setText("PDF ready. Tap 'Open File' or 'Download' to inspect.");
                                        }
                                    }
                                });
                            }).start();
                        } else {
                            if (layoutInlinePreviewBox != null) layoutInlinePreviewBox.setVisibility(View.GONE);
                            if (tvSubFileNotice != null) {
                                tvSubFileNotice.setVisibility(View.VISIBLE);
                                tvSubFileNotice.setText("Attachment ready (" + (fileType.isEmpty() ? "File" : fileType) + "). Tap 'Open File' or 'Download' to inspect before grading.");
                            }
                        }
                    }

                    @Override
                    public void onError(String message, int httpStatus) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbInlinePreview != null) pbInlinePreview.setVisibility(View.GONE);
                        if (layoutInlinePreviewBox != null) layoutInlinePreviewBox.setVisibility(View.GONE);
                        if (tvSubFileNotice != null) {
                            tvSubFileNotice.setVisibility(View.VISIBLE);
                            tvSubFileNotice.setText(message);
                        }
                    }
                }
        );
    }

    private void downloadAndSaveFile() {
        SubmissionFileViewerHelper.fetchSubmissionFile(this, submissionId, fileName, mimeType,
                new SubmissionFileViewerHelper.FileFetchCallback() {
                    @Override
                    public void onSuccess(File file, String resolvedMime) {
                        if (isFinishing() || isDestroyed()) return;
                        cachedSubmissionFile = file;
                        SubmissionFileViewerHelper.saveToDownloads(
                                EnterScoreActivity.this,
                                file,
                                fileName.isEmpty() ? file.getName() : fileName,
                                resolvedMime
                        );
                    }

                    @Override
                    public void onError(String message, int httpStatus) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(EnterScoreActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openFullScreenViewer() {
        if (submissionId <= 0) return;
        Intent intent = new Intent(this, SubmissionFileViewerActivity.class);
        intent.putExtra("submission_id_int", submissionId);
        intent.putExtra("from_enter_score", true);
        intent.putExtra("student_code", studentCode);
        intent.putExtra("student_name", studentName);
        intent.putExtra("assessment_title", assessmentTitle);
        intent.putExtra("course_code", courseCode);
        intent.putExtra("course_name", courseName);
        intent.putExtra("submitted_at", submittedAt);
        intent.putExtra("file_name", fileName);
        intent.putExtra("file_type", fileType);
        intent.putExtra("file_size_label", fileSizeLabel);
        intent.putExtra("mime_type", mimeType);
        intent.putExtra("preview_type", previewType);
        intent.putExtra("has_file", hasFile);
        intent.putExtra("file_exists", fileExists);
        startActivity(intent);
    }

    private void showNoSubmissionContext() {
        if (tvSubFileName != null) tvSubFileName.setText("No individual submission selected");
        if (tvSubFileNotice != null) {
            tvSubFileNotice.setVisibility(View.VISIBLE);
            tvSubFileNotice.setText("Open grading from Assessment Management → Submissions or Grading to inspect and score a student's work.");
        }
        if (layoutSubFileActions != null) layoutSubFileActions.setVisibility(View.GONE);
    }

    private void saveScore(boolean publish) {
        String scoreStr = etAwardedScore != null && etAwardedScore.getText() != null
                ? etAwardedScore.getText().toString().trim() : "";
        if (scoreStr.isEmpty()) {
            if (tilAwardedScore != null) tilAwardedScore.setError("Please enter a score");
            return;
        }
        double score;
        try {
            score = Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            if (tilAwardedScore != null) tilAwardedScore.setError("Invalid score number");
            return;
        }

        if (score < 0.0 || score > maxScore) {
            String msg = String.format(Locale.US, "Awarded score must be between 0 and %s pts!", formatScore(maxScore));
            if (tilAwardedScore != null) tilAwardedScore.setError(msg);
            return;
        }
        if (tilAwardedScore != null) tilAwardedScore.setError(null);

        if (submissionId <= 0) {
            Toast.makeText(this, "Select a student submission from the Submissions or Grading tab to publish a score.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String feedback = etFeedback != null && etFeedback.getText() != null ? etFeedback.getText().toString().trim() : "";

        JsonObject body = new JsonObject();
        body.addProperty("score", score);
        body.addProperty("feedback", feedback);
        body.addProperty("publish", publish);

        setGradingButtonsEnabled(false);
        ApiClient.getApiService(this).gradeTeacherSubmission(submissionId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                setGradingButtonsEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(
                            EnterScoreActivity.this,
                            publish ? "Student score saved and published successfully!" : "Draft score saved.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                } else {
                    Toast.makeText(
                            EnterScoreActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to save score (" + response.code() + ")"),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                setGradingButtonsEnabled(true);
                Toast.makeText(EnterScoreActivity.this, "Network error saving score", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGradingButtonsEnabled(boolean enabled) {
        if (btnSaveDraftScore != null) btnSaveDraftScore.setEnabled(enabled);
        if (btnSaveScore != null) btnSaveScore.setEnabled(enabled);
    }

    private static String formatScore(double val) {
        if (Math.abs(val - Math.round(val)) < 0.001) {
            return String.valueOf(Math.round(val));
        }
        return String.format(Locale.US, "%.2f", val);
    }

    private static String nonNull(String s) {
        return s != null ? s : "";
    }

    private static String nonNullOr(String s, String fallback) {
        return s != null && !s.trim().isEmpty() ? s : fallback;
    }

    private static String getStr(JsonObject obj, String key, String fallback) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            try {
                return obj.get(key).getAsString();
            } catch (Exception ignored) {}
        }
        return fallback;
    }

    @Override
    protected void onDestroy() {
        if (activeFileCall != null) {
            activeFileCall.cancel();
        }
        super.onDestroy();
    }
}
