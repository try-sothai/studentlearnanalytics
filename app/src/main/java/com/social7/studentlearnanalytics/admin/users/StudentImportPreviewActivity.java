package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.StudentImportRowAdapter;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportPreviewActivity extends AppCompatActivity {

    private ImageView btnBackPreview;
    private TextView tvTotalRows, tvValidRows, tvInvalidRows, tvDuplicateRows;
    private MaterialButton btnDownloadErrorReport, btnCancelImport, btnConfirmImport;
    private RecyclerView rvPreviewRows;

    private StudentImportItem importItem;
    private StudentImportRowAdapter rowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;

        setContentView(R.layout.activity_student_import_preview);

        if (getIntent() != null && getIntent().hasExtra("import_item")) {
            importItem = (StudentImportItem) getIntent().getSerializableExtra("import_item");
        }

        if (importItem == null) {
            Toast.makeText(this, R.string.missing_import_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displaySummary();
        setupListeners();
    }

    private void initViews() {
        btnBackPreview = findViewById(R.id.btnBackPreview);
        tvTotalRows = findViewById(R.id.tvTotalRows);
        tvValidRows = findViewById(R.id.tvValidRows);
        tvInvalidRows = findViewById(R.id.tvInvalidRows);
        tvDuplicateRows = findViewById(R.id.tvDuplicateRows);
        btnDownloadErrorReport = findViewById(R.id.btnDownloadErrorReport);
        btnCancelImport = findViewById(R.id.btnCancelImport);
        btnConfirmImport = findViewById(R.id.btnConfirmImport);
        rvPreviewRows = findViewById(R.id.rvPreviewRows);

        rowAdapter = new StudentImportRowAdapter();
        rvPreviewRows.setLayoutManager(new LinearLayoutManager(this));
        rvPreviewRows.setAdapter(rowAdapter);
    }

    private void displaySummary() {
        tvTotalRows.setText(String.valueOf(importItem.totalRows));
        tvValidRows.setText(String.valueOf(importItem.validRows));
        tvInvalidRows.setText(String.valueOf(importItem.invalidRows));
        tvDuplicateRows.setText(String.valueOf(importItem.duplicateRows));

        if (importItem.invalidRows > 0 || importItem.duplicateRows > 0) {
            btnDownloadErrorReport.setVisibility(View.VISIBLE);
        } else {
            btnDownloadErrorReport.setVisibility(View.GONE);
        }

        btnConfirmImport.setEnabled(importItem.validRows > 0);

        if (importItem.previewRows != null) {
            rowAdapter.setRows(importItem.previewRows);
        }
    }

    private void setupListeners() {
        if (btnBackPreview != null) btnBackPreview.setOnClickListener(v -> finish());

        if (btnDownloadErrorReport != null) {
            btnDownloadErrorReport.setOnClickListener(v -> downloadErrorReport());
        }

        if (btnCancelImport != null) {
            btnCancelImport.setOnClickListener(v -> cancelImport());
        }

        if (btnConfirmImport != null) {
            btnConfirmImport.setOnClickListener(v -> confirmImport());
        }
    }

    private void downloadErrorReport() {
        Toast.makeText(this, R.string.downloading_error_report, Toast.LENGTH_SHORT).show();
        ApiClient.getApiService(this).downloadImportErrorReport(importItem.id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            saveReportFile(response.body(), "import_" + importItem.id + "_error_report.csv");
                        } else {
                            Toast.makeText(StudentImportPreviewActivity.this, R.string.failed_to_download_error_report, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(StudentImportPreviewActivity.this, R.string.network_error_downloading_report, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveReportFile(ResponseBody body, String filename) {
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
                runOnUiThread(() -> Toast.makeText(StudentImportPreviewActivity.this, "Saved: " + outFile.getName(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(StudentImportPreviewActivity.this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void cancelImport() {
        btnCancelImport.setEnabled(false);
        ApiClient.getApiService(this).cancelStudentImport(importItem.id)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        Toast.makeText(StudentImportPreviewActivity.this, "Import cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        finish();
                    }
                });
    }

    private void confirmImport() {
        btnConfirmImport.setEnabled(false);
        ApiClient.getApiService(this).confirmStudentImport(importItem.id)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(StudentImportPreviewActivity.this, StudentImportProgressActivity.class);
                            intent.putExtra("import_id", importItem.id);
                            intent.putExtra("filename", importItem.originalFilename);
                            startActivity(intent);
                            finish();
                        } else {
                            btnConfirmImport.setEnabled(true);
                            Toast.makeText(StudentImportPreviewActivity.this, "Could not confirm import (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        btnConfirmImport.setEnabled(true);
                        Toast.makeText(StudentImportPreviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
