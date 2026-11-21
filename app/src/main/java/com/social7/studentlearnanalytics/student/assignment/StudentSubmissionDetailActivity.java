package com.social7.studentlearnanalytics.student.assignment;

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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.assessment.SubmissionFileViewerActivity;
import com.social7.studentlearnanalytics.utils.SubmissionFileViewerHelper;

import java.io.File;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays complete Student submission details:
 * - Submission & ownership info
 * - Submitted File preview (image / PDF Page 1 thumbnail), Open File, Download, and View Full Screen
 * - Published Score, Max Score, Published Date & Feedback (or Waiting for Teacher Grading if unpublished)
 */
public class StudentSubmissionDetailActivity extends AppCompatActivity {

    private int submissionId = 0;

    private TextView tvDetailAssignmentTitle, tvDetailAssessmentType, tvDetailCourse,
            tvDetailStudentCode, tvDetailStudentName, tvDetailSubmittedAt,
            tvDetailSubmissionStatus, tvDetailGradingStatus,
            tvDetailFileTypeBadge, tvDetailFileName, tvDetailFileSize,
            tvDetailPreviewHint, tvDetailFileNotice,
            tvDetailScore, tvDetailMaxScore, tvDetailPublishedDate, tvDetailFeedback;
    private FrameLayout layoutDetailPreviewBox;
    private ImageView ivDetailInlinePreview;
    private ProgressBar pbDetailPreview;
    private LinearLayout layoutDetailFileActions, layoutGradePublished, layoutGradePending;
    private MaterialButton btnDetailViewFile, btnDetailOpenFile, btnDetailDownloadFile;

    private String studentCode = "";
    private String studentName = "";
    private String assignmentTitle = "";
    private String assessmentType = "Assignment";
    private String courseCode = "";
    private String courseName = "";
    private String submittedAt = "";
    private String submissionStatus = "";
    private String gradingStatus = "";
    private boolean isPublished = false;
    private Double score = null;
    private double maxScore = 100.0;
    private String feedback = "";
    private String publishedAt = "";

    private String fileName = "";
    private String fileType = "";
    private String fileSizeLabel = "";
    private String mimeType = "";
    private String previewType = "";
    private boolean hasFile = false;
    private boolean fileExists = false;
    private File cachedFile = null;
    private Call<ResponseBody> activeFileCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_submission_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        bindViews();
        readIntentExtras();
        renderMetadata();
        setupListeners();

        if (submissionId > 0) {
            fetchDetailFromApi();
        } else {
            Toast.makeText(this, "Invalid submission ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        tvDetailAssignmentTitle = findViewById(R.id.tvDetailAssignmentTitle);
        tvDetailAssessmentType = findViewById(R.id.tvDetailAssessmentType);
        tvDetailCourse = findViewById(R.id.tvDetailCourse);
        tvDetailStudentCode = findViewById(R.id.tvDetailStudentCode);
        tvDetailStudentName = findViewById(R.id.tvDetailStudentName);
        tvDetailSubmittedAt = findViewById(R.id.tvDetailSubmittedAt);
        tvDetailSubmissionStatus = findViewById(R.id.tvDetailSubmissionStatus);
        tvDetailGradingStatus = findViewById(R.id.tvDetailGradingStatus);

        tvDetailFileTypeBadge = findViewById(R.id.tvDetailFileTypeBadge);
        tvDetailFileName = findViewById(R.id.tvDetailFileName);
        tvDetailFileSize = findViewById(R.id.tvDetailFileSize);
        tvDetailPreviewHint = findViewById(R.id.tvDetailPreviewHint);
        tvDetailFileNotice = findViewById(R.id.tvDetailFileNotice);
        layoutDetailPreviewBox = findViewById(R.id.layoutDetailPreviewBox);
        ivDetailInlinePreview = findViewById(R.id.ivDetailInlinePreview);
        pbDetailPreview = findViewById(R.id.pbDetailPreview);
        layoutDetailFileActions = findViewById(R.id.layoutDetailFileActions);

        btnDetailViewFile = findViewById(R.id.btnDetailViewFile);
        btnDetailOpenFile = findViewById(R.id.btnDetailOpenFile);
        btnDetailDownloadFile = findViewById(R.id.btnDetailDownloadFile);

        layoutGradePublished = findViewById(R.id.layoutGradePublished);
        layoutGradePending = findViewById(R.id.layoutGradePending);
        tvDetailScore = findViewById(R.id.tvDetailScore);
        tvDetailMaxScore = findViewById(R.id.tvDetailMaxScore);
        tvDetailPublishedDate = findViewById(R.id.tvDetailPublishedDate);
        tvDetailFeedback = findViewById(R.id.tvDetailFeedback);
    }

    private void readIntentExtras() {
        Intent i = getIntent();
        if (i == null) return;
        submissionId = i.getIntExtra("submission_id", 0);
        studentCode = nonNull(i.getStringExtra("student_code"));
        studentName = nonNull(i.getStringExtra("student_name"));
        assignmentTitle = nonNull(i.getStringExtra("assignment_title"));
        assessmentType = nonNullOr(i.getStringExtra("assessment_type"), "Assignment");
        courseCode = nonNull(i.getStringExtra("course_code"));
        courseName = nonNull(i.getStringExtra("course_name"));
        submittedAt = nonNull(i.getStringExtra("submitted_at"));
        submissionStatus = nonNull(i.getStringExtra("submission_status"));
        gradingStatus = nonNull(i.getStringExtra("grading_status"));
        isPublished = i.getBooleanExtra("is_published", false);
        if (i.hasExtra("score")) {
            double sc = i.getDoubleExtra("score", -1);
            if (sc >= 0) score = sc;
        }
        maxScore = i.getDoubleExtra("max_score", 100.0);
        feedback = nonNull(i.getStringExtra("feedback"));
        publishedAt = nonNull(i.getStringExtra("published_at"));
        fileName = nonNull(i.getStringExtra("file_name"));
        fileType = nonNull(i.getStringExtra("file_type"));
        fileSizeLabel = nonNull(i.getStringExtra("file_size_label"));
        mimeType = nonNull(i.getStringExtra("mime_type"));
        previewType = nonNull(i.getStringExtra("preview_type"));
        hasFile = i.getBooleanExtra("has_file", false);
        fileExists = i.getBooleanExtra("file_exists", false);
    }

    private void renderMetadata() {
        if (tvDetailAssignmentTitle != null) tvDetailAssignmentTitle.setText(assignmentTitle.isEmpty() ? "Assignment" : assignmentTitle);
        if (tvDetailAssessmentType != null) tvDetailAssessmentType.setText(assessmentType);

        StringBuilder course = new StringBuilder();
        if (!courseCode.isEmpty()) course.append(courseCode);
        if (!courseName.isEmpty()) {
            if (course.length() > 0) course.append(" - ");
            course.append(courseName);
        }
        if (tvDetailCourse != null) tvDetailCourse.setText(course.toString());

        if (tvDetailStudentCode != null) tvDetailStudentCode.setText(studentCode.isEmpty() ? "STU" : studentCode);
        if (tvDetailStudentName != null) tvDetailStudentName.setText(studentName.isEmpty() ? "Student" : studentName);
        if (tvDetailSubmittedAt != null) tvDetailSubmittedAt.setText("Submitted: " + (submittedAt.isEmpty() ? "-" : submittedAt));

        if (tvDetailSubmissionStatus != null) {
            tvDetailSubmissionStatus.setText(submissionStatus.isEmpty() ? "Submitted" : submissionStatus);
            tvDetailSubmissionStatus.setTextColor(Color.parseColor(submissionStatus.contains("Late") ? "#D97706" : "#2563EB"));
        }
        if (tvDetailGradingStatus != null) {
            tvDetailGradingStatus.setText(isPublished ? "Published (Graded)" : getString(R.string.waiting_for_teacher_grading));
            tvDetailGradingStatus.setTextColor(Color.parseColor(isPublished ? "#10B981" : "#D97706"));
        }

        // File section
        if (tvDetailFileTypeBadge != null) tvDetailFileTypeBadge.setText(fileType.isEmpty() ? (hasFile ? "File" : "Online") : fileType);
        if (tvDetailFileName != null) tvDetailFileName.setText(fileName.isEmpty() ? getString(R.string.no_submission_file_attached) : fileName);
        if (tvDetailFileSize != null) tvDetailFileSize.setText(fileSizeLabel.isEmpty() ? "" : "Size: " + fileSizeLabel);

        if (!hasFile) {
            if (layoutDetailPreviewBox != null) layoutDetailPreviewBox.setVisibility(View.GONE);
            if (layoutDetailFileActions != null) layoutDetailFileActions.setVisibility(View.GONE);
            if (tvDetailFileNotice != null) {
                tvDetailFileNotice.setVisibility(View.VISIBLE);
                tvDetailFileNotice.setText(R.string.no_submission_file_attached);
            }
        } else if (!fileExists) {
            if (layoutDetailPreviewBox != null) layoutDetailPreviewBox.setVisibility(View.GONE);
            if (layoutDetailFileActions != null) layoutDetailFileActions.setVisibility(View.GONE);
            if (tvDetailFileNotice != null) {
                tvDetailFileNotice.setVisibility(View.VISIBLE);
                tvDetailFileNotice.setText(R.string.submission_file_missing);
            }
        } else {
            if (tvDetailFileNotice != null) tvDetailFileNotice.setVisibility(View.GONE);
            if (layoutDetailFileActions != null) layoutDetailFileActions.setVisibility(View.VISIBLE);
        }

        // Grade section (never display unpublished scores)
        if (isPublished && score != null) {
            if (layoutGradePublished != null) layoutGradePublished.setVisibility(View.VISIBLE);
            if (layoutGradePending != null) layoutGradePending.setVisibility(View.GONE);
            if (tvDetailScore != null) {
                tvDetailScore.setText(String.format(Locale.US, "%s / %s pts", formatNum(score), formatNum(maxScore)));
            }
            if (tvDetailMaxScore != null) {
                tvDetailMaxScore.setText(String.format(Locale.US, "Max Score: %s pts", formatNum(maxScore)));
            }
            if (tvDetailPublishedDate != null) {
                tvDetailPublishedDate.setText(publishedAt.isEmpty() ? "Published" : ("Published: " + publishedAt));
            }
            if (tvDetailFeedback != null) {
                if (feedback.trim().isEmpty()) {
                    tvDetailFeedback.setVisibility(View.GONE);
                } else {
                    tvDetailFeedback.setVisibility(View.VISIBLE);
                    tvDetailFeedback.setText("Teacher Feedback:\n" + feedback);
                }
            }
        } else {
            if (layoutGradePublished != null) layoutGradePublished.setVisibility(View.GONE);
            if (layoutGradePending != null) layoutGradePending.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        View.OnClickListener viewFull = v -> openFullScreenViewer();
        if (btnDetailViewFile != null) btnDetailViewFile.setOnClickListener(viewFull);
        if (ivDetailInlinePreview != null) ivDetailInlinePreview.setOnClickListener(viewFull);

        if (btnDetailOpenFile != null) {
            btnDetailOpenFile.setOnClickListener(v -> {
                if (cachedFile != null && cachedFile.exists()) {
                    SubmissionFileViewerHelper.openWithExternalViewer(this, cachedFile, mimeType);
                } else {
                    openFullScreenViewer();
                }
            });
        }
        if (btnDetailDownloadFile != null) {
            btnDetailDownloadFile.setOnClickListener(v -> {
                if (cachedFile != null && cachedFile.exists()) {
                    SubmissionFileViewerHelper.saveToDownloads(
                            this,
                            cachedFile,
                            fileName.isEmpty() ? cachedFile.getName() : fileName,
                            mimeType
                    );
                } else {
                    Toast.makeText(this, "Preparing file download...", Toast.LENGTH_SHORT).show();
                    downloadAndSaveFile();
                }
            });
        }
    }

    private void fetchDetailFromApi() {
        ApiClient.getApiService(this).getStudentSubmissionDetail(submissionId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) {
                    if (response.code() == 403 || response.code() == 404) {
                        Toast.makeText(StudentSubmissionDetailActivity.this,
                                response.code() == 403 ? "Unauthorized to view this submission." : "Submission not found.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }

                JsonObject d = response.body().getAsJsonObject("data");
                studentCode = getStr(d, "student_code", studentCode);
                studentName = getStr(d, "student_name", studentName);
                assignmentTitle = getStr(d, "assignment_title", assignmentTitle);
                assessmentType = getStr(d, "assessment_type", assessmentType);
                courseCode = getStr(d, "course_code", courseCode);
                courseName = getStr(d, "course_name", courseName);
                submittedAt = getStr(d, "submitted_at", submittedAt);
                submissionStatus = getStr(d, "submission_status", submissionStatus);
                gradingStatus = getStr(d, "grading_status", gradingStatus);
                isPublished = d.has("is_published") && !d.get("is_published").isJsonNull() && d.get("is_published").getAsBoolean();
                score = (isPublished && d.has("score") && !d.get("score").isJsonNull()) ? d.get("score").getAsDouble() : null;
                if (d.has("max_score") && !d.get("max_score").isJsonNull()) {
                    double m = d.get("max_score").getAsDouble();
                    if (m > 0) maxScore = m;
                }
                feedback = isPublished ? getStr(d, "feedback", "") : "";
                publishedAt = getStr(d, "published_at", "");

                fileName = getStr(d, "file_name", fileName);
                fileType = getStr(d, "file_type", fileType);
                fileSizeLabel = getStr(d, "file_size_label", fileSizeLabel);
                mimeType = getStr(d, "mime_type", mimeType);
                previewType = getStr(d, "preview_type", previewType);
                hasFile = d.has("has_file") && !d.get("has_file").isJsonNull() && d.get("has_file").getAsBoolean();
                fileExists = d.has("file_exists") && !d.get("file_exists").isJsonNull() && d.get("file_exists").getAsBoolean();

                renderMetadata();
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
        if (supportsInline && layoutDetailPreviewBox != null && pbDetailPreview != null) {
            layoutDetailPreviewBox.setVisibility(View.VISIBLE);
            pbDetailPreview.setVisibility(View.VISIBLE);
        }

        if (activeFileCall != null) {
            activeFileCall.cancel();
        }
        activeFileCall = SubmissionFileViewerHelper.fetchSubmissionFileForRole(
                this,
                submissionId,
                true,
                fileName,
                mimeType,
                new SubmissionFileViewerHelper.FileFetchCallback() {
                    @Override
                    public void onSuccess(File file, String resolvedMime) {
                        if (isFinishing() || isDestroyed()) return;
                        cachedFile = file;
                        if (mimeType.isEmpty() && resolvedMime != null) mimeType = resolvedMime;
                        if (pbDetailPreview != null) pbDetailPreview.setVisibility(View.GONE);

                        if ("image".equals(effectivePreview)) {
                            Bitmap bmp = SubmissionFileViewerHelper.decodeSampledBitmap(file, 1280);
                            if (bmp != null && ivDetailInlinePreview != null) {
                                layoutDetailPreviewBox.setVisibility(View.VISIBLE);
                                ivDetailInlinePreview.setImageBitmap(bmp);
                                if (tvDetailPreviewHint != null) tvDetailPreviewHint.setText("Tap to zoom full screen");
                            } else if (layoutDetailPreviewBox != null) {
                                layoutDetailPreviewBox.setVisibility(View.GONE);
                            }
                        } else if ("pdf".equals(effectivePreview)) {
                            new Thread(() -> {
                                int count = SubmissionFileViewerHelper.getPdfPageCount(file);
                                Bitmap thumb = count > 0 ? SubmissionFileViewerHelper.renderPdfPage(file, 0, 960) : null;
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    if (thumb != null && ivDetailInlinePreview != null) {
                                        layoutDetailPreviewBox.setVisibility(View.VISIBLE);
                                        ivDetailInlinePreview.setImageBitmap(thumb);
                                        if (tvDetailPreviewHint != null) {
                                            tvDetailPreviewHint.setText(String.format(Locale.US, "PDF Page 1 of %d • Tap to view all", count));
                                        }
                                    } else {
                                        if (layoutDetailPreviewBox != null) layoutDetailPreviewBox.setVisibility(View.GONE);
                                        if (tvDetailFileNotice != null) {
                                            tvDetailFileNotice.setVisibility(View.VISIBLE);
                                            tvDetailFileNotice.setText("PDF ready. Tap 'Open File' or 'Download' to view.");
                                        }
                                    }
                                });
                            }).start();
                        } else {
                            if (layoutDetailPreviewBox != null) layoutDetailPreviewBox.setVisibility(View.GONE);
                            if (tvDetailFileNotice != null) {
                                tvDetailFileNotice.setVisibility(View.VISIBLE);
                                tvDetailFileNotice.setText("File ready (" + (fileType.isEmpty() ? "Document" : fileType) + "). Tap 'Open File' or 'Download' to inspect.");
                            }
                        }
                    }

                    @Override
                    public void onError(String message, int httpStatus) {
                        if (isFinishing() || isDestroyed()) return;
                        if (pbDetailPreview != null) pbDetailPreview.setVisibility(View.GONE);
                        if (layoutDetailPreviewBox != null) layoutDetailPreviewBox.setVisibility(View.GONE);
                        if (tvDetailFileNotice != null) {
                            tvDetailFileNotice.setVisibility(View.VISIBLE);
                            tvDetailFileNotice.setText(message);
                        }
                    }
                }
        );
    }

    private void downloadAndSaveFile() {
        SubmissionFileViewerHelper.fetchSubmissionFileForRole(this, submissionId, true, fileName, mimeType,
                new SubmissionFileViewerHelper.FileFetchCallback() {
                    @Override
                    public void onSuccess(File file, String resolvedMime) {
                        if (isFinishing() || isDestroyed()) return;
                        cachedFile = file;
                        SubmissionFileViewerHelper.saveToDownloads(
                                StudentSubmissionDetailActivity.this,
                                file,
                                fileName.isEmpty() ? file.getName() : fileName,
                                resolvedMime
                        );
                    }

                    @Override
                    public void onError(String message, int httpStatus) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(StudentSubmissionDetailActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openFullScreenViewer() {
        Intent intent = new Intent(this, SubmissionFileViewerActivity.class);
        intent.putExtra("submission_id_int", submissionId);
        intent.putExtra("is_student_mode", true);
        intent.putExtra("student_code", studentCode);
        intent.putExtra("student_name", studentName);
        intent.putExtra("assessment_title", assignmentTitle);
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

    private static String formatNum(double v) {
        return Math.abs(v - Math.round(v)) < 0.001
                ? String.valueOf(Math.round(v))
                : String.format(Locale.US, "%.2f", v);
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
