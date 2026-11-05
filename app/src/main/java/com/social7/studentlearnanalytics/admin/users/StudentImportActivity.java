package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportActivity extends AppCompatActivity {

    private ImageView btnBackImport;
    private MaterialButton btnDownloadXlsx, btnDownloadCsv, btnUploadPreview;
    private LinearLayout layoutFilePicker;
    private TextView tvSelectedFileName, tvSelectedFileSize;
    private ProgressBar pbUploadProgress;

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
                    "text/csv",
                    "text/comma-separated-values",
                    "text/plain"
            }));
        }

        if (btnUploadPreview != null) {
            btnUploadPreview.setOnClickListener(v -> uploadFileForPreview());
        }
    }

    private void handleFileSelected(Uri uri) {
        this.selectedFileUri = uri;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            selectedFileName = cursor.getString(nameIndex);
            selectedFileSize = cursor.getLong(sizeIndex);
            cursor.close();
        }

        if (selectedFileName == null) selectedFileName = "selected_file";

        tvSelectedFileName.setText(selectedFileName);
        tvSelectedFileSize.setText(String.format("%.1f KB", selectedFileSize / 1024.0));
        btnUploadPreview.setEnabled(true);
    }

    private void downloadTemplate(boolean isXlsx) {
        Toast.makeText(this, "Downloading template...", Toast.LENGTH_SHORT).show();
        Call<ResponseBody> call = isXlsx
                ? ApiClient.getApiService(this).downloadImportTemplateXlsx()
                : ApiClient.getApiService(this).downloadImportTemplateCsv();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveDownloadedFile(response.body(), isXlsx ? "student_import_template.xlsx" : "student_import_template.csv");
                } else {
                    Toast.makeText(StudentImportActivity.this, "Failed to download template (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(StudentImportActivity.this, "Network error downloading template", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDownloadedFile(ResponseBody body, String filename) {
        new Thread(() -> {
            try {
                File downloadsDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS);
                if (downloadsDir == null) downloadsDir = getFilesDir();
                File outFile = new File(downloadsDir, filename);

                try (InputStream is = body.byteStream();
                     FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                }

                runOnUiThread(() -> Toast.makeText(StudentImportActivity.this, "Template saved to: " + outFile.getName(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(StudentImportActivity.this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void uploadFileForPreview() {
        if (selectedFileUri == null) return;

        pbUploadProgress.setVisibility(View.VISIBLE);
        btnUploadPreview.setEnabled(false);

        new Thread(() -> {
            try {
                File tempFile = new File(getCacheDir(), selectedFileName != null ? selectedFileName : "import_temp.xlsx");
                try (InputStream is = getContentResolver().openInputStream(selectedFileUri);
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                }

                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), tempFile);
                MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

                runOnUiThread(() -> executePreviewCall(bodyPart));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pbUploadProgress.setVisibility(View.GONE);
                    btnUploadPreview.setEnabled(true);
                    Toast.makeText(StudentImportActivity.this, "Failed to read file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void executePreviewCall(MultipartBody.Part filePart) {
        ApiClient.getApiService(this).uploadImportPreview(filePart).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbUploadProgress.setVisibility(View.GONE);
                btnUploadPreview.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                    if (d != null) {
                        StudentImportItem item = new com.google.gson.Gson().fromJson(d, StudentImportItem.class);
                        Intent intent = new Intent(StudentImportActivity.this, StudentImportPreviewActivity.class);
                        intent.putExtra("import_item", item);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    String msg = "Validation failed (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(StudentImportActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbUploadProgress.setVisibility(View.GONE);
                btnUploadPreview.setEnabled(true);
                Toast.makeText(StudentImportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
