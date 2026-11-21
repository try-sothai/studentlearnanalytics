package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper;
import com.social7.studentlearnanalytics.utils.SubmissionFileViewerHelper;
import com.social7.studentlearnanalytics.utils.ZoomableImageView;

import java.io.File;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dedicated full-screen submission file viewer for Teachers.
 * Supports authenticated image zoom, multi-page PDF rendering, and external open / Downloads save
 * for DOC/DOCX/other submission files.
 */
public class SubmissionFileViewerActivity extends AppCompatActivity {

    private int submissionId = 0;
    private boolean isStudentMode = false;
    private boolean fromEnterScore = false;

    private String studentCode = "";
    private String studentName = "";
    private String assessmentTitle = "";
    private String courseCode = "";
    private String courseName = "";
    private String submittedAt = "";
    private String fileName = "";
    private String fileType = "";
    private String fileSizeLabel = "";
    private String mimeType = "";
    private String previewType = "";
    private boolean hasFile = true;
    private boolean fileExists = true;

    private TextView tvViewerStudentCode, tvViewerStudentName, tvViewerFileTypeBadge,
            tvViewerAssessmentInfo, tvViewerFileMeta, tvPdfPageIndicator,
            tvFallbackTypeBadge, tvFallbackTitle, tvFallbackMessage, tvZoomHint;
    private LinearLayout layoutZoomViewer, layoutPdfControls, layoutViewerFallback, layoutViewerLoading;
    private ZoomableImageView ivZoomPreview;
    private MaterialButton btnPdfPrev, btnPdfNext, btnViewerRetry, btnViewerOpenExternal, btnViewerDownload, btnViewerGrade;

    private File cachedFile = null;
    private int currentPdfPage = 0;
    private int pdfPageCount = 0;
    private Call<ResponseBody> activeDownloadCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_file_viewer);

        TeacherHeaderHelper.setupHeader(
                this,
                findViewById(R.id.headerSubmissionViewer),
                getString(R.string.submission_file_viewer),
                "",
                true
        );

        readIntentExtras();
        bindViews();
        renderHeaderMetadata();
        setupListeners();

        if (submissionId <= 0) {
            showFallbackState("Error", "Invalid submission ID", "No submission was specified to preview.", false);
            return;
        }

        fetchAuthoritativeMetadataAndFile();
    }

    private void readIntentExtras() {
        Intent i = getIntent();
        if (i == null) return;
        submissionId = i.getIntExtra("submission_id_int", 0);
        if (submissionId <= 0) {
            String raw = i.getStringExtra("submission_id");
            if (raw != null) {
                try {
                    submissionId = Integer.parseInt(raw.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        fromEnterScore = i.getBooleanExtra("from_enter_score", false);
        isStudentMode = i.getBooleanExtra("is_student_mode", false);
        studentCode = nonNull(i.getStringExtra("student_code"));
        studentName = nonNull(i.getStringExtra("student_name"));
        assessmentTitle = nonNull(i.getStringExtra("assessment_title"));
        courseCode = nonNull(i.getStringExtra("course_code"));
        courseName = nonNull(i.getStringExtra("course_name"));
        submittedAt = nonNull(i.getStringExtra("submitted_at"));
        fileName = nonNull(i.getStringExtra("file_name"));
        fileType = nonNull(i.getStringExtra("file_type"));
        fileSizeLabel = nonNull(i.getStringExtra("file_size_label"));
        mimeType = nonNull(i.getStringExtra("mime_type"));
        previewType = nonNull(i.getStringExtra("preview_type"));
        hasFile = i.getBooleanExtra("has_file", true);
        fileExists = i.getBooleanExtra("file_exists", true);
    }

    private void bindViews() {
        tvViewerStudentCode = findViewById(R.id.tvViewerStudentCode);
        tvViewerStudentName = findViewById(R.id.tvViewerStudentName);
        tvViewerFileTypeBadge = findViewById(R.id.tvViewerFileTypeBadge);
        tvViewerAssessmentInfo = findViewById(R.id.tvViewerAssessmentInfo);
        tvViewerFileMeta = findViewById(R.id.tvViewerFileMeta);
        tvPdfPageIndicator = findViewById(R.id.tvPdfPageIndicator);
        tvFallbackTypeBadge = findViewById(R.id.tvFallbackTypeBadge);
        tvFallbackTitle = findViewById(R.id.tvFallbackTitle);
        tvFallbackMessage = findViewById(R.id.tvFallbackMessage);
        tvZoomHint = findViewById(R.id.tvZoomHint);

        layoutZoomViewer = findViewById(R.id.layoutZoomViewer);
        layoutPdfControls = findViewById(R.id.layoutPdfControls);
        layoutViewerFallback = findViewById(R.id.layoutViewerFallback);
        layoutViewerLoading = findViewById(R.id.layoutViewerLoading);
        ivZoomPreview = findViewById(R.id.ivZoomPreview);

        btnPdfPrev = findViewById(R.id.btnPdfPrev);
        btnPdfNext = findViewById(R.id.btnPdfNext);
        btnViewerRetry = findViewById(R.id.btnViewerRetry);
        btnViewerOpenExternal = findViewById(R.id.btnViewerOpenExternal);
        btnViewerDownload = findViewById(R.id.btnViewerDownload);
        btnViewerGrade = findViewById(R.id.btnViewerGrade);

        if ((fromEnterScore || isStudentMode) && btnViewerGrade != null) {
            btnViewerGrade.setVisibility(View.GONE);
        }
    }

    private void renderHeaderMetadata() {
        if (tvViewerStudentCode != null) {
            tvViewerStudentCode.setText(studentCode.isEmpty() ? "STU" : studentCode);
        }
        if (tvViewerStudentName != null) {
            tvViewerStudentName.setText(studentName.isEmpty() ? "Student" : studentName);
        }
        if (tvViewerFileTypeBadge != null) {
            tvViewerFileTypeBadge.setText(fileType.isEmpty() ? "FILE" : fileType);
        }
        if (tvViewerAssessmentInfo != null) {
            String coursePart = courseCode.isEmpty() ? "" : (courseCode + (courseName.isEmpty() ? "" : " - " + courseName));
            StringBuilder sb = new StringBuilder();
            if (!assessmentTitle.isEmpty()) sb.append(assessmentTitle);
            if (!coursePart.isEmpty()) {
                if (sb.length() > 0) sb.append(" • ");
                sb.append(coursePart);
            }
            if (!submittedAt.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Submitted: ").append(submittedAt);
            }
            tvViewerAssessmentInfo.setText(sb.toString());
        }
        if (tvViewerFileMeta != null) {
            StringBuilder meta = new StringBuilder();
            meta.append("File: ").append(fileName.isEmpty() ? ("submission_" + submissionId) : fileName);
            if (!fileSizeLabel.isEmpty()) {
                meta.append(" (").append(fileSizeLabel).append(")");
            }
            tvViewerFileMeta.setText(meta.toString());
        }
    }

    private void setupListeners() {
        if (btnPdfPrev != null) {
            btnPdfPrev.setOnClickListener(v -> showPdfPage(currentPdfPage - 1));
        }
        if (btnPdfNext != null) {
            btnPdfNext.setOnClickListener(v -> showPdfPage(currentPdfPage + 1));
        }
        if (btnViewerRetry != null) {
            btnViewerRetry.setOnClickListener(v -> fetchAuthoritativeMetadataAndFile());
        }
        if (btnViewerOpenExternal != null) {
            btnViewerOpenExternal.setOnClickListener(v -> {
                if (cachedFile == null || !cachedFile.exists()) {
                    Toast.makeText(this, R.string.submission_file_missing, Toast.LENGTH_SHORT).show();
                    return;
                }
                SubmissionFileViewerHelper.openWithExternalViewer(this, cachedFile, mimeType);
            });
        }
        if (btnViewerDownload != null) {
            btnViewerDownload.setOnClickListener(v -> {
                if (cachedFile == null || !cachedFile.exists()) {
                    Toast.makeText(this, R.string.submission_file_missing, Toast.LENGTH_SHORT).show();
                    return;
                }
                SubmissionFileViewerHelper.saveToDownloads(
                        this,
                        cachedFile,
                        fileName.isEmpty() ? cachedFile.getName() : fileName,
                        mimeType
                );
            });
        }
        if (btnViewerGrade != null) {
            btnViewerGrade.setOnClickListener(v -> {
                Intent intent = new Intent(this, EnterScoreActivity.class);
                intent.putExtra("submission_id", String.valueOf(submissionId));
                intent.putExtra("assessment_title", assessmentTitle);
                intent.putExtra("course_code", courseCode);
                intent.putExtra("course_name", courseName);
                intent.putExtra("student_name", studentName);
                intent.putExtra("student_code", studentCode);
                intent.putExtra("submitted_at", submittedAt);
                startActivity(intent);
                finish();
            });
        }
    }

    private void fetchAuthoritativeMetadataAndFile() {
        showLoadingState();
        Call<JsonObject> detailCall = isStudentMode
                ? ApiClient.getApiService(this).getStudentSubmissionDetail(submissionId)
                : ApiClient.getApiService(this).getTeacherSubmissionDetail(submissionId);
        detailCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")
                        && response.body().get("data").isJsonObject()) {
                    JsonObject d = response.body().getAsJsonObject("data");
                    studentCode = getStr(d, "student_code", studentCode);
                    studentName = getStr(d, "student_name", studentName);
                    assessmentTitle = getStr(d, "assignment_title", assessmentTitle);
                    courseCode = getStr(d, "course_code", courseCode);
                    courseName = getStr(d, "course_name", courseName);
                    submittedAt = getStr(d, "submitted_at", submittedAt);
                    fileName = getStr(d, "file_name", fileName);
                    fileType = getStr(d, "file_type", fileType);
                    fileSizeLabel = getStr(d, "file_size_label", fileSizeLabel);
                    mimeType = getStr(d, "mime_type", mimeType);
                    previewType = getStr(d, "preview_type", previewType);
                    hasFile = !d.has("has_file") || d.get("has_file").getAsBoolean();
                    fileExists = !d.has("file_exists") || d.get("file_exists").getAsBoolean();
                    renderHeaderMetadata();
                } else if (response.code() == 403 || response.code() == 404) {
                    showFallbackState(
                            "Access Denied",
                            response.code() == 403 ? "Unauthorized Access" : "Submission Not Found",
                            response.code() == 403
                                    ? "You are not authorized to view this student's submission."
                                    : "The requested submission could not be found.",
                            false
                    );
                    return;
                }

                if (!hasFile) {
                    showFallbackState(
                            "Online",
                            getString(R.string.no_submission_file_attached),
                            "This submission does not contain an uploaded attachment.",
                            false
                    );
                    return;
                }

                if (!fileExists) {
                    showFallbackState(
                            "Missing",
                            getString(R.string.submission_file_missing),
                            "Expected attachment '" + (fileName.isEmpty() ? "file" : fileName) + "' could not be located in server storage.",
                            true
                    );
                    return;
                }

                loadFileStream();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (hasFile && fileExists) {
                    loadFileStream();
                } else {
                    showFallbackState("Offline", "Network Error", "Could not connect to server to verify submission.", true);
                }
            }
        });
    }

    private void loadFileStream() {
        showLoadingState();
        if (activeDownloadCall != null) {
            activeDownloadCall.cancel();
        }
        activeDownloadCall = SubmissionFileViewerHelper.fetchSubmissionFileForRole(
                this,
                submissionId,
                isStudentMode,
                fileName,
                mimeType,
                new SubmissionFileViewerHelper.FileFetchCallback() {
                    @Override
                    public void onSuccess(File file, String resolvedMime) {
                        if (isFinishing() || isDestroyed()) return;
                        cachedFile = file;
                        if (mimeType.isEmpty() && resolvedMime != null) {
                            mimeType = resolvedMime;
                        }
                        if (btnViewerOpenExternal != null) btnViewerOpenExternal.setEnabled(true);
                        if (btnViewerDownload != null) btnViewerDownload.setEnabled(true);
                        renderCachedFile();
                    }

                    @Override
                    public void onError(String message, int httpStatus) {
                        if (isFinishing() || isDestroyed()) return;
                        showFallbackState(
                                httpStatus == 404 ? "Missing" : "Error",
                                getString(R.string.submission_file_missing),
                                message,
                                true
                        );
                    }
                }
        );
    }

    private void renderCachedFile() {
        String effectivePreview = SubmissionFileViewerHelper.inferPreviewType(previewType, fileName, mimeType);

        if ("image".equals(effectivePreview)) {
            Bitmap bmp = SubmissionFileViewerHelper.decodeSampledBitmap(cachedFile, 2048);
            if (bmp == null) {
                showFallbackState(
                        fileType.isEmpty() ? "IMAGE" : fileType,
                        fileName,
                        "Unable to render image preview directly. Try Open File or Download.",
                        true
                );
                return;
            }
            showZoomViewer(false);
            ivZoomPreview.setImageBitmap(bmp);
            ivZoomPreview.resetZoom();
            return;
        }

        if ("pdf".equals(effectivePreview)) {
            pdfPageCount = SubmissionFileViewerHelper.getPdfPageCount(cachedFile);
            if (pdfPageCount <= 0) {
                showFallbackState(
                        "PDF",
                        fileName,
                        "PDF document downloaded (" + fileSizeLabel + "). Use Open File or Download to inspect it in a PDF reader.",
                        false
                );
                return;
            }
            showZoomViewer(true);
            showPdfPage(0);
            return;
        }

        String typeLabel = fileType.isEmpty() ? "Document" : fileType;
        String detail = "In-app preview is not available for " + typeLabel
                + (fileSizeLabel.isEmpty() ? "." : " (" + fileSizeLabel + ").")
                + "\nTap 'Open File' to inspect in a compatible viewer or 'Download' to save a local copy.";
        showFallbackState(typeLabel, fileName, detail, false);
    }

    private void showPdfPage(int pageIndex) {
        if (cachedFile == null || pdfPageCount <= 0) return;
        if (pageIndex < 0 || pageIndex >= pdfPageCount) return;

        currentPdfPage = pageIndex;
        if (tvPdfPageIndicator != null) {
            tvPdfPageIndicator.setText(String.format(Locale.US, "Page %d of %d", currentPdfPage + 1, pdfPageCount));
        }
        if (btnPdfPrev != null) btnPdfPrev.setEnabled(currentPdfPage > 0);
        if (btnPdfNext != null) btnPdfNext.setEnabled(currentPdfPage < pdfPageCount - 1);

        new Thread(() -> {
            Bitmap pageBmp = SubmissionFileViewerHelper.renderPdfPage(cachedFile, pageIndex, 1600);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (pageBmp != null && ivZoomPreview != null) {
                    ivZoomPreview.setImageBitmap(pageBmp);
                    ivZoomPreview.resetZoom();
                } else {
                    Toast.makeText(this, "Could not render PDF page " + (pageIndex + 1), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showLoadingState() {
        if (layoutViewerLoading != null) layoutViewerLoading.setVisibility(View.VISIBLE);
        if (layoutZoomViewer != null) layoutZoomViewer.setVisibility(View.GONE);
        if (layoutViewerFallback != null) layoutViewerFallback.setVisibility(View.GONE);
        if (btnViewerOpenExternal != null) btnViewerOpenExternal.setEnabled(false);
        if (btnViewerDownload != null) btnViewerDownload.setEnabled(false);
    }

    private void showZoomViewer(boolean isPdf) {
        if (layoutViewerLoading != null) layoutViewerLoading.setVisibility(View.GONE);
        if (layoutViewerFallback != null) layoutViewerFallback.setVisibility(View.GONE);
        if (layoutZoomViewer != null) layoutZoomViewer.setVisibility(View.VISIBLE);
        if (layoutPdfControls != null) layoutPdfControls.setVisibility(isPdf ? View.VISIBLE : View.GONE);
        if (tvZoomHint != null) tvZoomHint.setVisibility(View.VISIBLE);
    }

    private void showFallbackState(String badge, String title, String message, boolean allowRetry) {
        if (layoutViewerLoading != null) layoutViewerLoading.setVisibility(View.GONE);
        if (layoutZoomViewer != null) layoutZoomViewer.setVisibility(View.GONE);
        if (layoutViewerFallback != null) layoutViewerFallback.setVisibility(View.VISIBLE);
        if (tvFallbackTypeBadge != null) tvFallbackTypeBadge.setText(badge);
        if (tvFallbackTitle != null) tvFallbackTitle.setText(title);
        if (tvFallbackMessage != null) tvFallbackMessage.setText(message);
        if (btnViewerRetry != null) btnViewerRetry.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
        boolean canOpenOrDownload = cachedFile != null && cachedFile.exists();
        if (btnViewerOpenExternal != null) btnViewerOpenExternal.setEnabled(canOpenOrDownload);
        if (btnViewerDownload != null) btnViewerDownload.setEnabled(canOpenOrDownload);
    }

    private static String nonNull(String s) {
        return s != null ? s : "";
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
        if (activeDownloadCall != null) {
            activeDownloadCall.cancel();
        }
        super.onDestroy();
    }
}
