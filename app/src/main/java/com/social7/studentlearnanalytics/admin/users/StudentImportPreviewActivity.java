package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.StudentImportRowAdapter;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportPreviewActivity extends AppCompatActivity {

    private ImageView btnBackPreview;
    private TextView tvTotalRows, tvValidRows, tvInvalidRows, tvDuplicateRows;
    private TextView tvMissingRequiredRows, tvInvalidDeptProgramRows, tvValidationBanner;
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
        tvMissingRequiredRows = findViewById(R.id.tvMissingRequiredRows);
        tvInvalidDeptProgramRows = findViewById(R.id.tvInvalidDeptProgramRows);
        tvValidationBanner = findViewById(R.id.tvValidationBanner);
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

        int missingReq = importItem.missingRequiredRows;
        int invalidDeptProg = importItem.invalidDepartmentProgramRows;
        if (missingReq == 0 && invalidDeptProg == 0 && importItem.previewRows != null) {
            for (StudentImportItem.RowItem r : importItem.previewRows) {
                if (r.errors == null) continue;
                boolean hasReq = false, hasDp = false;
                for (String err : r.errors) {
                    if (err != null && err.contains("Missing required field:")) hasReq = true;
                    if (err != null && (err.contains("department_code") || err.contains("program_code") || err.contains("faculty") || err.contains("major"))) hasDp = true;
                }
                if (hasReq) missingReq++;
                if (hasDp) invalidDeptProg++;
            }
        }
        if (tvMissingRequiredRows != null) tvMissingRequiredRows.setText(String.valueOf(missingReq));
        if (tvInvalidDeptProgramRows != null) tvInvalidDeptProgramRows.setText(String.valueOf(invalidDeptProg));

        if (importItem.invalidRows > 0 || importItem.duplicateRows > 0) {
            btnDownloadErrorReport.setVisibility(View.VISIBLE);
        } else {
            btnDownloadErrorReport.setVisibility(View.GONE);
        }

        boolean canConfirm = importItem.validRows > 0;
        btnConfirmImport.setVisibility(canConfirm ? View.VISIBLE : View.GONE);
        btnConfirmImport.setEnabled(canConfirm);

        if (tvValidationBanner != null) {
            if (canConfirm && importItem.invalidRows == 0 && importItem.duplicateRows == 0) {
                tvValidationBanner.setBackgroundColor(Color.parseColor("#ECFDF5"));
                tvValidationBanner.setTextColor(Color.parseColor("#166534"));
                tvValidationBanner.setText("File validated successfully: " + importItem.validRows
                        + " valid record(s) ready to import and create Student accounts.");
            } else if (canConfirm) {
                tvValidationBanner.setBackgroundColor(Color.parseColor("#FFFBEB"));
                tvValidationBanner.setTextColor(Color.parseColor("#9A3412"));
                tvValidationBanner.setText(importItem.validRows + " valid record(s) ready to import. "
                        + (importItem.invalidRows + importItem.duplicateRows)
                        + " invalid/duplicate row(s) will be skipped.");
            } else {
                tvValidationBanner.setBackgroundColor(Color.parseColor("#FEF2F2"));
                tvValidationBanner.setTextColor(Color.parseColor("#991B1B"));
                tvValidationBanner.setText("Validation failed: 0 valid records. Fix the errors below before importing.");
            }
        }

        if (importItem.previewRows != null && !importItem.previewRows.isEmpty()) {
            rowAdapter.setRows(importItem.previewRows);
        } else {
            fetchPreviewRows();
        }
    }

    private void fetchPreviewRows() {
        ApiClient.getApiService(this).getStudentImportRows(importItem.id, null, 1, 100)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            JsonArray arr = response.body().getAsJsonArray("data");
                            List<StudentImportItem.RowItem> loaded = new ArrayList<>();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            for (JsonElement el : arr) {
                                loaded.add(gson.fromJson(el, StudentImportItem.RowItem.class));
                            }
                            rowAdapter.setRows(loaded);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });
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
                            StudentExportDialog.saveExportFile(
                                    getApplicationContext(),
                                    response.body(),
                                    "import_" + importItem.id + "_error_report.csv",
                                    "text/csv"
                            );
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
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(StudentImportPreviewActivity.this, StudentImportProgressActivity.class);
                            intent.putExtra("import_id", importItem.id);
                            intent.putExtra("filename", importItem.originalFilename);
                            startActivity(intent);
                            finish();
                        } else {
                            btnConfirmImport.setEnabled(true);
                            String msg = "Could not confirm import (HTTP " + response.code() + ")";
                            try {
                                if (response.errorBody() != null) {
                                    JsonObject err = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                                    if (err.has("message") && !err.get("message").isJsonNull()) {
                                        msg = err.get("message").getAsString();
                                    }
                                }
                            } catch (Exception ignored) {}
                            if (tvValidationBanner != null) {
                                tvValidationBanner.setBackgroundColor(Color.parseColor("#FEF2F2"));
                                tvValidationBanner.setTextColor(Color.parseColor("#991B1B"));
                                tvValidationBanner.setText(msg);
                            }
                            Toast.makeText(StudentImportPreviewActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        btnConfirmImport.setEnabled(true);
                        Toast.makeText(StudentImportPreviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
