package com.social7.studentlearnanalytics.student.assignment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.teacher.assessment.SubmissionFileViewerActivity;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitAssignmentActivity extends AppCompatActivity {

    private String assignmentId = "";
    private boolean isGradedLocked = false;
    private double maxMarks = 100.0;

    private TextView tvHeaderTitle, tvAssignmentTitle, tvCourseName, tvMetaInfo, tvInstructions;
    private TextInputEditText etSubmissionText;
    private TextView tvAttachmentName, tvStatusMessage;
    private MaterialButton btnSelectFile, btnSaveDraft, btnSubmitWork, btnViewSubmittedFile, btnOpenSubmissionDetail;
    private View layoutSubmittedFileActions;
    private ProgressBar pbLoading;
    private ScrollView scrollContent;

    private Uri selectedFileUri = null;
    private String selectedFileName = null;

    private int existingSubmissionId = 0;
    private String existingFileName = "";
    private String existingFileType = "";
    private String existingFileSizeLabel = "";
    private String existingMimeType = "";
    private String existingPreviewType = "";
    private boolean existingHasFile = false;
    private boolean existingFileExists = false;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        selectedFileName = resolveDisplayName(selectedFileUri);
                        tvAttachmentName.setText(selectedFileName != null ? selectedFileName : "File attached");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        if (getIntent() != null && getIntent().hasExtra("assignment_id")) {
            assignmentId = getIntent().getStringExtra("assignment_id");
        }
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        fetchAssignmentDetails();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvAssignmentTitle = findViewById(R.id.tvAssignmentTitle);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvMetaInfo = findViewById(R.id.tvMetaInfo);
        tvInstructions = findViewById(R.id.tvInstructions);

        etSubmissionText = findViewById(R.id.etSubmissionText);
        tvAttachmentName = findViewById(R.id.tvAttachmentName);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmitWork = findViewById(R.id.btnSubmitWork);
        layoutSubmittedFileActions = findViewById(R.id.layoutSubmittedFileActions);
        btnViewSubmittedFile = findViewById(R.id.btnViewSubmittedFile);
        btnOpenSubmissionDetail = findViewById(R.id.btnOpenSubmissionDetail);

        pbLoading = findViewById(R.id.pbLoading);
        scrollContent = findViewById(R.id.scrollContent);

        if (btnSaveDraft != null) {
            // Backend records submissions immediately as submitted/late; keep single clear submit action
            btnSaveDraft.setVisibility(View.GONE);
        }
        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnSubmitWork.setOnClickListener(v -> confirmAndSubmitWork());
        if (btnViewSubmittedFile != null) {
            btnViewSubmittedFile.setOnClickListener(v -> openSubmittedFileViewer());
        }
        if (btnOpenSubmissionDetail != null) {
            btnOpenSubmissionDetail.setOnClickListener(v -> openSubmissionDetailScreen());
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/zip",
                "image/png",
                "image/jpeg"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Attachment"));
    }

    private String resolveDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        String seg = uri.getLastPathSegment();
        return seg != null ? seg : "attachment";
    }

    private void fetchAssignmentDetails() {
        pbLoading.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);

        ApiClient.getApiService(this).getAssignmentDetails(assignmentId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    scrollContent.setVisibility(View.VISIBLE);
                    JsonObject root = response.body();
                    JsonObject data = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;
                    bindAssignmentDetails(data);
                } else {
                    String msg = AssessmentUiHelper.extractErrorMessage(response, "Unable to load assignment details");
                    Toast.makeText(SubmitAssignmentActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(SubmitAssignmentActivity.this, "Network error loading details", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindAssignmentDetails(JsonObject data) {
        String title = AssessmentUiHelper.optString(data, "title", "Assignment Details");
        String type = AssessmentUiHelper.optString(data, "assignment_type", "assignment");
        boolean isHw = "homework".equalsIgnoreCase(type);
        tvHeaderTitle.setText(isHw ? "Homework Details" : "Assignment Details");
        tvAssignmentTitle.setText(title);

        String courseText = "Course";
        if (data.has("course") && data.get("course").isJsonObject()) {
            JsonObject cObj = data.getAsJsonObject("course");
            String code = AssessmentUiHelper.optString(cObj, "code", AssessmentUiHelper.optString(cObj, "course_code", ""));
            String name = AssessmentUiHelper.optString(cObj, "name", AssessmentUiHelper.optString(cObj, "course_name", "Course"));
            courseText = !code.isEmpty() ? (code + " – " + name) : name;
        }
        tvCourseName.setText(courseText);

        maxMarks = AssessmentUiHelper.optDouble(data, "total_score", 100.0);
        String rawDue = AssessmentUiHelper.optString(data, "due_date", "");
        String dueFormatted = AssessmentUiHelper.formatDateShort(rawDue);
        boolean pastDue = AssessmentUiHelper.isPast(rawDue);

        tvMetaInfo.setText(String.format(Locale.US, "Max Marks: %s • Due: %s%s",
                AssessmentUiHelper.formatMarks(maxMarks),
                dueFormatted,
                pastDue ? " (Past Due)" : ""));

        String desc = AssessmentUiHelper.optString(data, "description", "");
        tvInstructions.setText(desc.isEmpty() ? "No additional instructions provided." : desc);

        String rawStatus = AssessmentUiHelper.optString(data, "status", "published");
        if ("closed".equalsIgnoreCase(rawStatus)) {
            isGradedLocked = true;
            btnSubmitWork.setEnabled(false);
            btnSelectFile.setEnabled(false);
            tvStatusMessage.setText("Status: Closed");
            tvStatusMessage.setTextColor(getResources().getColor(R.color.danger_red, null));
        }

        if (data.has("submissions") && data.get("submissions").isJsonArray()) {
            JsonArray subs = data.getAsJsonArray("submissions");
            if (subs.size() > 0 && subs.get(0).isJsonObject()) {
                bindExistingSubmission(subs.get(0).getAsJsonObject());
                return;
            }
        }

        if (!isGradedLocked) {
            tvStatusMessage.setText(pastDue ? "Status: Overdue (Not Submitted)" : "Status: Not Submitted");
            tvStatusMessage.setTextColor(getResources().getColor(pastDue ? R.color.danger_red : R.color.warning_orange, null));
        }
    }

    private void bindExistingSubmission(JsonObject sub) {
        existingSubmissionId = AssessmentUiHelper.optInt(sub, "id", 0);
        String status = AssessmentUiHelper.optString(sub, "status", "submitted");
        String submittedAt = AssessmentUiHelper.optString(sub, "submitted_at", "");
        boolean isPublished = sub.has("is_published") && !sub.get("is_published").isJsonNull()
                ? sub.get("is_published").getAsBoolean()
                : "graded".equalsIgnoreCase(status);
        boolean hasPublishedScore = isPublished && sub.has("score") && !sub.get("score").isJsonNull();
        String feedback = isPublished ? AssessmentUiHelper.optString(sub, "feedback", "") : "";

        existingHasFile = sub.has("has_file") && !sub.get("has_file").isJsonNull()
                ? sub.get("has_file").getAsBoolean()
                : !AssessmentUiHelper.optString(sub, "file_path", "").isEmpty();
        existingFileExists = !sub.has("file_exists") || sub.get("file_exists").getAsBoolean();
        existingFileName = AssessmentUiHelper.optString(sub, "file_name",
                AssessmentUiHelper.optString(sub, "original_file_name", ""));
        if (existingFileName.isEmpty()) {
            String rawPath = AssessmentUiHelper.optString(sub, "file_path", "");
            int slash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
            existingFileName = slash >= 0 ? rawPath.substring(slash + 1) : rawPath;
        }
        existingFileType = AssessmentUiHelper.optString(sub, "file_type", "");
        existingFileSizeLabel = AssessmentUiHelper.optString(sub, "file_size_label", "");
        existingMimeType = AssessmentUiHelper.optString(sub, "mime_type", "");
        existingPreviewType = AssessmentUiHelper.optString(sub, "preview_type", "");

        if (existingHasFile && !existingFileName.isEmpty()) {
            StringBuilder label = new StringBuilder(existingFileName);
            if (!existingFileType.isEmpty()) {
                label.append(" (").append(existingFileType);
                if (!existingFileSizeLabel.isEmpty()) label.append(" • ").append(existingFileSizeLabel);
                label.append(")");
            }
            if (!existingFileExists) label.append(" — File Missing");
            tvAttachmentName.setText(label.toString());
        }

        if (layoutSubmittedFileActions != null) {
            layoutSubmittedFileActions.setVisibility(existingSubmissionId > 0 ? View.VISIBLE : View.GONE);
        }
        if (btnViewSubmittedFile != null) {
            btnViewSubmittedFile.setVisibility(existingSubmissionId > 0 && existingHasFile ? View.VISIBLE : View.GONE);
        }

        if (hasPublishedScore || "graded".equalsIgnoreCase(status)) {
            isGradedLocked = true;
            double sc = hasPublishedScore ? sub.get("score").getAsDouble() : 0;
            String msg = String.format(Locale.US, "Status: Graded • Score: %s / %s",
                    AssessmentUiHelper.formatMarks(sc), AssessmentUiHelper.formatMarks(maxMarks));
            if (!feedback.isEmpty()) {
                msg += "\nFeedback: " + feedback;
            }
            tvStatusMessage.setText(msg);
            tvStatusMessage.setTextColor(getResources().getColor(R.color.success_green, null));

            btnSubmitWork.setText("Graded");
            btnSubmitWork.setEnabled(false);
            btnSelectFile.setEnabled(false);
            etSubmissionText.setEnabled(false);
        } else {
            String when = submittedAt.isEmpty() ? "" : (" on " + AssessmentUiHelper.formatDateTime(submittedAt));
            String cap = "late".equalsIgnoreCase(status) ? "Submitted Late" : "Submitted";
            tvStatusMessage.setText("Status: " + cap + when + "\n" + getString(R.string.waiting_for_teacher_grading));
            tvStatusMessage.setTextColor(getResources().getColor(R.color.info_blue, null));
            btnSubmitWork.setText("Resubmit Work");
        }
    }

    private void openSubmittedFileViewer() {
        if (existingSubmissionId <= 0) return;
        Intent intent = new Intent(this, SubmissionFileViewerActivity.class);
        intent.putExtra("submission_id_int", existingSubmissionId);
        intent.putExtra("is_student_mode", true);
        intent.putExtra("assessment_title", tvAssignmentTitle.getText() != null ? tvAssignmentTitle.getText().toString() : "");
        intent.putExtra("file_name", existingFileName);
        intent.putExtra("file_type", existingFileType);
        intent.putExtra("file_size_label", existingFileSizeLabel);
        intent.putExtra("mime_type", existingMimeType);
        intent.putExtra("preview_type", existingPreviewType);
        intent.putExtra("has_file", existingHasFile);
        intent.putExtra("file_exists", existingFileExists);
        startActivity(intent);
    }

    private void openSubmissionDetailScreen() {
        if (existingSubmissionId <= 0) return;
        Intent intent = new Intent(this, StudentSubmissionDetailActivity.class);
        intent.putExtra("submission_id", existingSubmissionId);
        intent.putExtra("assignment_title", tvAssignmentTitle.getText() != null ? tvAssignmentTitle.getText().toString() : "");
        intent.putExtra("max_score", maxMarks);
        intent.putExtra("file_name", existingFileName);
        intent.putExtra("file_type", existingFileType);
        intent.putExtra("file_size_label", existingFileSizeLabel);
        intent.putExtra("mime_type", existingMimeType);
        intent.putExtra("preview_type", existingPreviewType);
        intent.putExtra("has_file", existingHasFile);
        intent.putExtra("file_exists", existingFileExists);
        startActivity(intent);
    }

    private void confirmAndSubmitWork() {
        if (isGradedLocked) return;

        String text = etSubmissionText.getText() != null ? etSubmissionText.getText().toString().trim() : "";
        if (selectedFileUri == null && text.isEmpty()) {
            Toast.makeText(this, "Attach a file or enter a written response before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.submit_work)
                .setMessage(R.string.are_you_sure_you_want_to_submit_your_assig)
                .setPositiveButton(R.string.submit, (dialog, which) -> submitWork())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitWork() {
        pbLoading.setVisibility(View.VISIBLE);
        btnSubmitWork.setEnabled(false);

        int aId;
        try {
            aId = Integer.parseInt(assignmentId.replaceAll("\\D+", ""));
        } catch (Exception e) {
            pbLoading.setVisibility(View.GONE);
            btnSubmitWork.setEnabled(true);
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etSubmissionText.getText() != null ? etSubmissionText.getText().toString().trim() : "";

        Callback<JsonObject> callback = new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnSubmitWork.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(SubmitAssignmentActivity.this, "Work submitted successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SubmitAssignmentActivity.this,
                            AssessmentUiHelper.extractErrorMessage(response, "Failed to submit work"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                pbLoading.setVisibility(View.GONE);
                btnSubmitWork.setEnabled(true);
                Toast.makeText(SubmitAssignmentActivity.this, "Network error submitting work", Toast.LENGTH_LONG).show();
            }
        };

        if (selectedFileUri != null) {
            byte[] bytes = readUriBytes(selectedFileUri);
            if (bytes != null && bytes.length > 0) {
                String mime = getContentResolver().getType(selectedFileUri);
                if (mime == null) mime = "application/octet-stream";
                String fname = selectedFileName != null ? selectedFileName : "submission.pdf";

                RequestBody aIdPart = RequestBody.create(String.valueOf(aId), MediaType.parse("text/plain"));
                RequestBody filePathPart = RequestBody.create(fname, MediaType.parse("text/plain"));
                RequestBody fileBody = RequestBody.create(bytes, MediaType.parse(mime));
                MultipartBody.Part part = MultipartBody.Part.createFormData("submission_file", fname, fileBody);

                ApiClient.getApiService(this).submitAssignmentMultipart(aIdPart, filePathPart, part).enqueue(callback);
                return;
            }
        }

        JsonObject body = new JsonObject();
        body.addProperty("assignment_id", aId);
        body.addProperty("file_path", selectedFileName != null ? selectedFileName : text);
        ApiClient.getApiService(this).submitAssignment(body).enqueue(callback);
    }

    private byte[] readUriBytes(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
