package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_IMPORT";

    private ImageView btnBackImport;
    private MaterialButton btnDownloadXlsx, btnDownloadCsv, btnUploadPreview;
    private LinearLayout layoutFilePicker;
    private TextView tvSelectedFileName, tvSelectedFileSize;
    private ProgressBar pbUploadProgress;
    private MaterialCardView cardUploadError;
    private TextView tvUploadErrorTitle, tvUploadErrorDetail;

    private Uri selectedFileUri = null;
    private String selectedFileName = null;
    private long selectedFileSize = 0;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    handleFileSelected(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;

        setContentView(R.layout.activity_student_import);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBackImport = findViewById(R.id.btnBackImport);
        btnDownloadXlsx = findViewById(R.id.btnDownloadXlsx);
        btnDownloadCsv = findViewById(R.id.btnDownloadCsv);
        btnUploadPreview = findViewById(R.id.btnUploadPreview);
        layoutFilePicker = findViewById(R.id.layoutFilePicker);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        tvSelectedFileSize = findViewById(R.id.tvSelectedFileSize);
        pbUploadProgress = findViewById(R.id.pbUploadProgress);
        cardUploadError = findViewById(R.id.cardUploadError);
        tvUploadErrorTitle = findViewById(R.id.tvUploadErrorTitle);
        tvUploadErrorDetail = findViewById(R.id.tvUploadErrorDetail);
    }

    private void setupListeners() {
        if (btnBackImport != null) btnBackImport.setOnClickListener(v -> finish());

        if (btnDownloadXlsx != null) {
            btnDownloadXlsx.setOnClickListener(v -> downloadTemplate(true));
        }

        if (btnDownloadCsv != null) {
            btnDownloadCsv.setOnClickListener(v -> downloadTemplate(false));
        }

        if (layoutFilePicker != null) {
            layoutFilePicker.setOnClickListener(v -> filePickerLauncher.launch(new String[]{
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "text/csv",
                    "text/comma-separated-values",
                    "application/csv",
                    "text/plain"
            }));
        }

        if (btnUploadPreview != null) {
            btnUploadPreview.setOnClickListener(v -> uploadFileForPreview());
        }
    }

    private void handleFileSelected(Uri uri) {
        this.selectedFileUri = uri;
        this.selectedFileName = null;
        this.selectedFileSize = 0;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex >= 0) {
                    selectedFileName = cursor.getString(nameIndex);
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    selectedFileSize = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to query selected file metadata", e);
        }

        if (selectedFileName == null || selectedFileName.trim().isEmpty()) {
            String lastSeg = uri.getLastPathSegment();
            selectedFileName = lastSeg != null ? new File(lastSeg).getName() : "selected_file";
        } else {
            selectedFileName = new File(selectedFileName).getName();
        }

        if (!selectedFileName.contains(".")) {
            String mime = getContentResolver().getType(uri);
            String ext = mime != null ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) : null;
            if (ext == null && mime != null && mime.toLowerCase(Locale.US).contains("csv")) {
                ext = "csv";
            }
            if (ext != null && !ext.isEmpty()) {
                selectedFileName = selectedFileName + "." + ext;
            }
        }

        hideErrorBanner();
        tvSelectedFileName.setText(selectedFileName);
        tvSelectedFileSize.setText(selectedFileSize > 0
                ? String.format(Locale.US, "%.1f KB", selectedFileSize / 1024.0)
                : "Ready to upload");
        btnUploadPreview.setEnabled(true);
    }

    private void downloadTemplate(boolean isXlsx) {
        Toast.makeText(this, R.string.downloading_template, Toast.LENGTH_SHORT).show();
        Call<ResponseBody> call = isXlsx
                ? ApiClient.getApiService(this).downloadImportTemplateXlsx()
                : ApiClient.getApiService(this).downloadImportTemplateCsv();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String filename = isXlsx ? "student_import_template.xlsx" : "student_import_template.csv";
                    String mime = isXlsx
                            ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            : "text/csv";
                    StudentExportDialog.saveExportFile(getApplicationContext(), response.body(), filename, mime);
                } else {
                    Toast.makeText(StudentImportActivity.this, "Failed to download template (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(StudentImportActivity.this, R.string.network_error_downloading_template, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadFileForPreview() {
        if (selectedFileUri == null) return;

        hideErrorBanner();
        pbUploadProgress.setVisibility(View.VISIBLE);
        btnUploadPreview.setEnabled(false);

        new Thread(() -> {
            File tempFile = null;
            try {
                String safeName = (selectedFileName != null && !selectedFileName.trim().isEmpty())
                        ? new File(selectedFileName).getName()
                        : "import_temp.csv";
                tempFile = new File(getCacheDir(), safeName);
                try (InputStream is = getContentResolver().openInputStream(selectedFileUri);
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    if (is == null) {
                        throw new IllegalStateException("Cannot open input stream for selected file.");
                    }
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                }

                String lower = safeName.toLowerCase(Locale.US);
                String mimeStr = "application/octet-stream";
                if (lower.endsWith(".csv")) {
                    mimeStr = "text/csv";
                } else if (lower.endsWith(".xlsx")) {
                    mimeStr = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                } else if (lower.endsWith(".xls")) {
                    mimeStr = "application/vnd.ms-excel";
                }

                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeStr), tempFile);
                MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

                final File fileToClean = tempFile;
                runOnUiThread(() -> executePreviewCall(bodyPart, fileToClean));
            } catch (Exception e) {
                if (tempFile != null && tempFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    tempFile.delete();
                }
                Log.e(TAG, "Failed preparing upload file", e);
                runOnUiThread(() -> {
                    pbUploadProgress.setVisibility(View.GONE);
                    btnUploadPreview.setEnabled(true);
                    showErrorBanner("File Read Error", "Failed to read file: " + e.getMessage());
                });
            }
        }).start();
    }

    private void executePreviewCall(MultipartBody.Part filePart, File tempFile) {
        ApiClient.getApiService(this).uploadImportPreview(filePart).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                cleanupTempFile(tempFile);
                if (isFinishing() || isDestroyed()) return;
                pbUploadProgress.setVisibility(View.GONE);
                btnUploadPreview.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") && response.body().get("data").isJsonObject()
                            ? response.body().getAsJsonObject("data") : null;
                    if (d != null) {
                        StudentImportItem item = new com.google.gson.Gson().fromJson(d, StudentImportItem.class);
                        Intent intent = new Intent(StudentImportActivity.this, StudentImportPreviewActivity.class);
                        intent.putExtra("import_item", item);
                        startActivity(intent);
                        finish();
                    } else {
                        showErrorBanner("Unexpected Response", "Server did not return validation preview data.");
                    }
                } else {
                    String title = "Validation Failed (HTTP " + response.code() + ")";
                    if (response.code() == 401) {
                        title = "Authentication Required (HTTP 401)";
                    } else if (response.code() == 403) {
                        title = "Admin Permission Required (HTTP 403)";
                    }
                    String msg = extractApiErrorMessage(response, "Unable to validate the uploaded file.");
                    Log.w(TAG, "Preview failed HTTP " + response.code() + ": " + msg);
                    showErrorBanner(title, msg);
                    Toast.makeText(StudentImportActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                cleanupTempFile(tempFile);
                if (isFinishing() || isDestroyed()) return;
                pbUploadProgress.setVisibility(View.GONE);
                btnUploadPreview.setEnabled(true);
                String msg = "Could not connect to server (" + ApiClient.getBaseUrl() + "): " + t.getMessage();
                Log.e(TAG, "Preview network error", t);
                showErrorBanner("Network Error", msg);
                Toast.makeText(StudentImportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cleanupTempFile(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private void showErrorBanner(String title, String detail) {
        if (cardUploadError != null) {
            cardUploadError.setVisibility(View.VISIBLE);
            if (tvUploadErrorTitle != null) tvUploadErrorTitle.setText(title);
            if (tvUploadErrorDetail != null) tvUploadErrorDetail.setText(detail);
        }
    }

    private void hideErrorBanner() {
        if (cardUploadError != null) {
            cardUploadError.setVisibility(View.GONE);
        }
    }

    private String extractApiErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() == null) return fallback;
            String raw = response.errorBody().string();
            if (raw.trim().isEmpty()) return fallback;
            JsonElement parsed = com.google.gson.JsonParser.parseString(raw);
            if (!parsed.isJsonObject()) return fallback;
            JsonObject err = parsed.getAsJsonObject();

            StringBuilder sb = new StringBuilder();
            if (err.has("message") && !err.get("message").isJsonNull()) {
                sb.append(err.get("message").getAsString().trim());
            }
            if (err.has("errors") && err.get("errors").isJsonObject()) {
                JsonObject errorsObj = err.getAsJsonObject("errors");
                for (Map.Entry<String, JsonElement> entry : errorsObj.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        JsonArray arr = entry.getValue().getAsJsonArray();
                        for (JsonElement item : arr) {
                            String line = item.getAsString().trim();
                            if (!line.isEmpty() && !sb.toString().contains(line)) {
                                if (sb.length() > 0) sb.append("\n• ");
                                sb.append(line);
                            }
                        }
                    }
                }
            }
            return sb.length() > 0 ? sb.toString() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
