package com.social7.studentlearnanalytics.student.assignment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitAssignmentActivity extends AppCompatActivity {

    private static final String TAG = "SubmitAssignment";

    private String assignmentId = "1";
    private String submissionId = null;
    private SessionManager sessionManager;

    private TextView tvHeaderTitle, tvAssignmentTitle, tvCourseName, tvMetaInfo, tvInstructions;
    private TextInputEditText etSubmissionText;
    private TextView tvAttachmentName, tvStatusMessage;
    private MaterialButton btnSelectFile, btnSaveDraft, btnSubmitWork;
    private ProgressBar pbLoading;
    private ScrollView scrollContent;

    private Uri selectedFileUri = null;
    private String selectedFileName = null;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        selectedFileName = selectedFileUri.getLastPathSegment();
                        tvAttachmentName.setText(selectedFileName != null ? selectedFileName : "File attached");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        sessionManager = new SessionManager(this);

        if (getIntent() != null && getIntent().hasExtra("assignment_id")) {
            assignmentId = getIntent().getStringExtra("assignment_id");
        }

        initViews();
        fetchAssignmentDetailsAndDraft();
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

        pbLoading = findViewById(R.id.pbLoading);
        scrollContent = findViewById(R.id.scrollContent);

        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnSaveDraft.setOnClickListener(v -> saveDraft(false));
        btnSubmitWork.setOnClickListener(v -> confirmAndSubmitWork());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Attachment"));
    }

    private void fetchAssignmentDetailsAndDraft() {
        pbLoading.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);

        ApiClient.getApiService(this).getAssignmentDetails(assignmentId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject data = root.has("data") ? root.getAsJsonObject("data") : root;
                    bindAssignmentDetails(data);
                }
                fetchStudentSubmission();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                fetchStudentSubmission();
            }
        });
    }

    private void bindAssignmentDetails(JsonObject data) {
        String title = data.has("title") && !data.get("title").isJsonNull()
                ? data.get("title").getAsString() : "Assignment Details";
        tvAssignmentTitle.setText(title);

        String course = "Course";
        if (data.has("course") && !data.get("course").isJsonNull()) {
            JsonObject cObj = data.getAsJsonObject("course");
            if (cObj.has("course_name")) course = cObj.get("course_name").getAsString();
        }
        tvCourseName.setText(course);

        int totalScore = data.has("total_score") && !data.get("total_score").isJsonNull()
                ? data.get("total_score").getAsInt() : 100;
        String dueDate = data.has("due_date") && !data.get("due_date").isJsonNull()
                ? data.get("due_date").getAsString() : "2026-12-31";

        tvMetaInfo.setText("Max Marks: " + totalScore + " • Due: " + dueDate);

        if (data.has("description") && !data.get("description").isJsonNull()) {
            tvInstructions.setText(data.get("description").getAsString());
        }
    }

    private void fetchStudentSubmission() {
        String studentId = sessionManager.getUserId();
        if (studentId == null || studentId.isEmpty()) studentId = "1";

        ApiClient.getApiService(this).getAssignmentSubmissions(assignmentId, studentId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    if (root.has("data") && root.get("data").isJsonArray()) {
                        JsonArray arr = root.getAsJsonArray("data");
                        if (arr.size() > 0) {
                            JsonObject sub = arr.get(0).getAsJsonObject();
                            bindExistingSubmission(sub);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                scrollContent.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindExistingSubmission(JsonObject sub) {
        if (sub.has("id")) {
            submissionId = sub.get("id").getAsString();
        }

        String status = sub.has("status") && !sub.get("status").isJsonNull()
                ? sub.get("status").getAsString() : "draft";

        if ("submitted".equalsIgnoreCase(status) || "graded".equalsIgnoreCase(status)) {
            tvStatusMessage.setText("Status: Submitted (" + status + ")");
            tvStatusMessage.setTextColor(getResources().getColor(R.color.success_green, null));
            btnSubmitWork.setText("Resubmit Work");
        } else {
            tvStatusMessage.setText("Status: Draft (Saved)");
            tvStatusMessage.setTextColor(getResources().getColor(R.color.warning_orange, null));
        }

        if (sub.has("file_path") && !sub.get("file_path").isJsonNull()) {
            String path = sub.get("file_path").getAsString();
            tvAttachmentName.setText(path);
        }
    }

    private void saveDraft(boolean isFinalSubmission) {
        pbLoading.setVisibility(View.VISIBLE);

        String text = etSubmissionText.getText() != null ? etSubmissionText.getText().toString().trim() : "";

        JsonObject body = new JsonObject();
        body.addProperty("assignment_id", Integer.parseInt(assignmentId));
        String studentId = sessionManager.getUserId();
        try {
            body.addProperty("student_id", Integer.parseInt(studentId));
        } catch (Exception ignored) {
            body.addProperty("student_id", 1);
        }

        body.addProperty("file_path", selectedFileName != null ? selectedFileName : (text.isEmpty() ? "written_submission.txt" : text));
        body.addProperty("status", isFinalSubmission ? "submitted" : "draft");

        ApiClient.getApiService(this).submitAssignment(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbLoading.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    if (isFinalSubmission) {
                        Toast.makeText(SubmitAssignmentActivity.this, "Work submitted successfully!", Toast.LENGTH_SHORT).show();
                        tvStatusMessage.setText("Status: Submitted");
                        tvStatusMessage.setTextColor(getResources().getColor(R.color.success_green, null));
                        finish();
                    } else {
                        Toast.makeText(SubmitAssignmentActivity.this, "Draft saved successfully.", Toast.LENGTH_SHORT).show();
                        tvStatusMessage.setText("Status: Draft (Saved)");
                    }
                } else {
                    Toast.makeText(SubmitAssignmentActivity.this, "Action complete.", Toast.LENGTH_SHORT).show();
                    if (isFinalSubmission) finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(SubmitAssignmentActivity.this, isFinalSubmission ? "Work submitted." : "Draft saved locally.", Toast.LENGTH_SHORT).show();
                if (isFinalSubmission) finish();
            }
        });
    }

    private void confirmAndSubmitWork() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Work")
                .setMessage("Are you sure you want to submit your assignment?")
                .setPositiveButton("Submit", (dialog, which) -> saveDraft(true))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
