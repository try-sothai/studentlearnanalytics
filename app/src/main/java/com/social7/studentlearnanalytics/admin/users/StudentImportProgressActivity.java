package com.social7.studentlearnanalytics.admin.users;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.StudentImportRowAdapter;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportProgressActivity extends AppCompatActivity {

    private ImageView btnBackProgress;
    private TextView tvImportStatus, tvImportFilename, tvImportSummaryBanner, tvProgressPercent;
    private TextView tvProcessedCount, tvSuccessCount, tvFailedCount, tvCreatedAccountsCount, tvSkippedCount;
    private TextView tvCredentialsHint, tvResultRowsTitle;
    private ProgressBar pbImportProgress;
    private LinearLayout layoutCredentialsSection;
    private MaterialButton btnDownloadCredentials, btnDownloadImportErrors, btnDoneOrCancel;
    private RecyclerView rvImportResultRows;
    private StudentImportRowAdapter resultAdapter;

    private int importId;
    private String filename;
    private boolean isFinished = false;
    private boolean rowsLoaded = false;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinished) {
                fetchStatus();
                pollHandler.postDelayed(this, 2000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;

        setContentView(R.layout.activity_student_import_progress);

        importId = getIntent().getIntExtra("import_id", 0);
        filename = getIntent().getStringExtra("filename");

        initViews();
        setupListeners();

        pollHandler.post(pollRunnable);
    }

    private void initViews() {
        btnBackProgress = findViewById(R.id.btnBackProgress);
        tvImportStatus = findViewById(R.id.tvImportStatus);
        tvImportFilename = findViewById(R.id.tvImportFilename);
        tvImportSummaryBanner = findViewById(R.id.tvImportSummaryBanner);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProcessedCount = findViewById(R.id.tvProcessedCount);
        tvSuccessCount = findViewById(R.id.tvSuccessCount);
        tvFailedCount = findViewById(R.id.tvFailedCount);
        tvCreatedAccountsCount = findViewById(R.id.tvCreatedAccountsCount);
        tvSkippedCount = findViewById(R.id.tvSkippedCount);
        tvCredentialsHint = findViewById(R.id.tvCredentialsHint);
        tvResultRowsTitle = findViewById(R.id.tvResultRowsTitle);
        pbImportProgress = findViewById(R.id.pbImportProgress);
        layoutCredentialsSection = findViewById(R.id.layoutCredentialsSection);
        btnDownloadCredentials = findViewById(R.id.btnDownloadCredentials);
        btnDownloadImportErrors = findViewById(R.id.btnDownloadImportErrors);
        btnDoneOrCancel = findViewById(R.id.btnDoneOrCancel);
        rvImportResultRows = findViewById(R.id.rvImportResultRows);

        resultAdapter = new StudentImportRowAdapter();
        if (rvImportResultRows != null) {
            rvImportResultRows.setLayoutManager(new LinearLayoutManager(this));
            rvImportResultRows.setAdapter(resultAdapter);
        }

        if (filename != null) tvImportFilename.setText(filename);
    }

    private void setupListeners() {
        if (btnBackProgress != null) btnBackProgress.setOnClickListener(v -> finish());

        if (btnDownloadCredentials != null) {
            btnDownloadCredentials.setOnClickListener(v -> retrieveAndSaveCredentials());
        }

        if (btnDownloadImportErrors != null) {
            btnDownloadImportErrors.setOnClickListener(v -> downloadImportErrors());
        }

        if (btnDoneOrCancel != null) {
            btnDoneOrCancel.setOnClickListener(v -> {
                if (isFinished) {
                    finish();
                } else {
                    cancelImport();
                }
            });
        }
    }

    private void fetchStatus() {
        ApiClient.getApiService(this).getStudentImportStatus(importId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                            if (d != null) {
                                StudentImportItem item = new com.google.gson.Gson().fromJson(d, StudentImportItem.class);
                                updateUI(item);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        // ignore transient poll failure
                    }
                });
    }

    private void updateUI(StudentImportItem item) {
        String status = item.status != null ? item.status.toUpperCase() : "PROCESSING";
        tvImportStatus.setText(status);

        int percent = Math.max(0, Math.min(100, item.progressPercent));
        pbImportProgress.setProgress(percent);
        tvProgressPercent.setText(percent + "%");

        tvProcessedCount.setText(item.processedRows + " / " + item.validRows);
        tvSuccessCount.setText(String.valueOf(item.successRows));
        tvFailedCount.setText(String.valueOf(item.failedRows));
        int createdAccounts = item.createdUserAccounts > 0 ? item.createdUserAccounts : item.successRows;
        if (tvCreatedAccountsCount != null) {
            tvCreatedAccountsCount.setText(createdAccounts + " (Role: Student)");
        }
        if (tvSkippedCount != null) {
            tvSkippedCount.setText(String.valueOf(item.skippedRows));
        }

        if ("COMPLETED".equalsIgnoreCase(status) || "COMPLETED_WITH_ERRORS".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            isFinished = true;
            pollHandler.removeCallbacks(pollRunnable);

            btnDoneOrCancel.setText(R.string.done);
            btnDoneOrCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.admin_primary_blue)));
            btnDoneOrCancel.setTextColor(Color.WHITE);

            if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)
                    || (item.successRows == 0 && item.failedRows > 0)) {
                tvImportStatus.setTextColor(Color.parseColor("#DC2626"));
                if (tvImportSummaryBanner != null) {
                    tvImportSummaryBanner.setBackgroundColor(Color.parseColor("#FEF2F2"));
                    tvImportSummaryBanner.setTextColor(Color.parseColor("#991B1B"));
                    String failMsg = (item.failureMessage != null && !item.failureMessage.trim().isEmpty())
                            ? item.failureMessage
                            : ("CANCELLED".equalsIgnoreCase(status)
                                ? "Import was cancelled. No student accounts were created."
                                : "Import failed: student records were not saved.");
                    tvImportSummaryBanner.setText(failMsg);
                }
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                tvImportStatus.setTextColor(Color.parseColor("#16A34A"));
                if (tvImportSummaryBanner != null) {
                    tvImportSummaryBanner.setBackgroundColor(Color.parseColor("#ECFDF5"));
                    tvImportSummaryBanner.setTextColor(Color.parseColor("#166534"));
                    tvImportSummaryBanner.setText("Import complete! Created " + item.successRows
                            + " student record(s) and " + createdAccounts + " Student login account(s).\n"
                            + "Username: Student ID  •  Initial Password: STD@12345  •  Role: student");
                }
            } else {
                tvImportStatus.setTextColor(Color.parseColor("#EA580C"));
                if (tvImportSummaryBanner != null) {
                    tvImportSummaryBanner.setBackgroundColor(Color.parseColor("#FFFBEB"));
                    tvImportSummaryBanner.setTextColor(Color.parseColor("#9A3412"));
                    tvImportSummaryBanner.setText("Import completed with warnings: " + item.successRows
                            + " imported, " + item.failedRows + " failed, " + item.skippedRows + " skipped.");
                }
            }

            if (layoutCredentialsSection != null) {
                layoutCredentialsSection.setVisibility(item.hasGeneratedCredentials ? View.VISIBLE : View.GONE);
            }
            if (btnDownloadImportErrors != null) {
                boolean hasErrors = item.failedRows > 0 || item.invalidRows > 0 || item.duplicateRows > 0 || item.skippedRows > 0;
                btnDownloadImportErrors.setVisibility(hasErrors ? View.VISIBLE : View.GONE);
            }

            if (!rowsLoaded) {
                rowsLoaded = true;
                loadImportResultRows();
            }
        }
    }

    private void loadImportResultRows() {
        ApiClient.getApiService(this).getStudentImportRows(importId, null, 1, 100)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            JsonArray arr = response.body().getAsJsonArray("data");
                            List<StudentImportItem.RowItem> rows = new ArrayList<>();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            for (JsonElement el : arr) {
                                rows.add(gson.fromJson(el, StudentImportItem.RowItem.class));
                            }
                            if (!rows.isEmpty()) {
                                if (tvResultRowsTitle != null) tvResultRowsTitle.setVisibility(View.VISIBLE);
                                if (rvImportResultRows != null) rvImportResultRows.setVisibility(View.VISIBLE);
                                resultAdapter.setRows(rows);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
                });
    }

    private void retrieveAndSaveCredentials() {
        btnDownloadCredentials.setEnabled(false);
        Toast.makeText(this, "Retrieving initial student credentials...", Toast.LENGTH_SHORT).show();

        ApiClient.getApiService(this).downloadImportCredentials(importId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                byte[] bytes = response.body().bytes();
                                String csvText = new String(bytes, StandardCharsets.UTF_8).replace("\uFEFF", "");
                                String outName = "import_" + importId + "_temporary_credentials.csv";

                                // Save CSV file to Downloads using a fresh ResponseBody from the buffered bytes
                                ResponseBody copyBody = ResponseBody.create(MediaType.parse("text/csv"), bytes);
                                StudentExportDialog.saveExportFile(getApplicationContext(), copyBody, outName, "text/csv");

                                String formatted = formatCredentialsDialogText(csvText);
                                btnDownloadCredentials.setText("Credentials Retrieved & Saved");
                                if (tvCredentialsHint != null) {
                                    tvCredentialsHint.setText("Credentials saved to Downloads/" + outName + " and cleared from server storage.");
                                }

                                new MaterialAlertDialogBuilder(StudentImportProgressActivity.this)
                                        .setTitle("Initial Student Login Credentials")
                                        .setMessage(formatted)
                                        .setPositiveButton("Copy All", (d, w) -> {
                                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            if (cm != null) {
                                                cm.setPrimaryClip(ClipData.newPlainText("SLAS Credentials", formatted));
                                                Toast.makeText(StudentImportProgressActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton(R.string.close, null)
                                        .show();
                            } catch (Exception e) {
                                btnDownloadCredentials.setEnabled(true);
                                Toast.makeText(StudentImportProgressActivity.this, "Error reading credentials: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            btnDownloadCredentials.setEnabled(true);
                            Toast.makeText(StudentImportProgressActivity.this, "Credentials unavailable (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        btnDownloadCredentials.setEnabled(true);
                        Toast.makeText(StudentImportProgressActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatCredentialsDialogText(String csvText) {
        String[] lines = csvText.replace("\r", "").split("\n");
        StringBuilder sb = new StringBuilder();
        sb.append("Students log in with Student ID as username.\nSave these passwords now — they cannot be viewed again:\n\n");
        int count = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            List<String> cols = parseCsvLine(line);
            if (cols.size() >= 3) {
                String id = cols.get(0).trim();
                String name = cols.get(1).trim();
                String pass = cols.get(2).trim();
                sb.append(++count).append(". ").append(name)
                        .append("\n   Username / Student ID: ").append(id)
                        .append("\n   Initial Password: ").append(pass)
                        .append("\n   Role: Student\n\n");
            }
        }
        if (count == 0) {
            return "No newly generated temporary passwords were found (custom passwords may have been provided in the file, or credentials were already retrieved).";
        }
        return sb.toString().trim();
    }

    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());
        return cols;
    }

    private void downloadImportErrors() {
        Toast.makeText(this, R.string.downloading_error_report, Toast.LENGTH_SHORT).show();
        ApiClient.getApiService(this).downloadImportErrorReport(importId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StudentExportDialog.saveExportFile(
                                    getApplicationContext(),
                                    response.body(),
                                    "import_" + importId + "_error_report.csv",
                                    "text/csv"
                            );
                        } else {
                            Toast.makeText(StudentImportProgressActivity.this, R.string.failed_to_download_error_report, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(StudentImportProgressActivity.this, R.string.network_error_downloading_report, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelImport() {
        btnDoneOrCancel.setEnabled(false);
        ApiClient.getApiService(this).cancelStudentImport(importId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        Toast.makeText(StudentImportProgressActivity.this, "Cancellation requested", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(StudentImportProgressActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
