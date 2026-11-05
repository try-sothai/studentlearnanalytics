package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportProgressActivity extends AppCompatActivity {

    private ImageView btnBackProgress;
    private TextView tvImportStatus, tvImportFilename, tvProgressPercent, tvProcessedCount, tvSuccessCount, tvFailedCount;
    private ProgressBar pbImportProgress;
    private MaterialButton btnDoneOrCancel;

    private int importId;
    private String filename;
    private boolean isFinished = false;

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
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProcessedCount = findViewById(R.id.tvProcessedCount);
        tvSuccessCount = findViewById(R.id.tvSuccessCount);
        tvFailedCount = findViewById(R.id.tvFailedCount);
        pbImportProgress = findViewById(R.id.pbImportProgress);
        btnDoneOrCancel = findViewById(R.id.btnDoneOrCancel);

        if (filename != null) tvImportFilename.setText(filename);
    }

    private void setupListeners() {
        if (btnBackProgress != null) btnBackProgress.setOnClickListener(v -> finish());

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

        if ("COMPLETED".equalsIgnoreCase(status) || "COMPLETED_WITH_ERRORS".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            isFinished = true;
            pollHandler.removeCallbacks(pollRunnable);

            btnDoneOrCancel.setText("Done");
            btnDoneOrCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.admin_primary_blue)));
            btnDoneOrCancel.setTextColor(android.graphics.Color.WHITE);

            if ("COMPLETED".equalsIgnoreCase(status)) {
                tvImportStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"));
            } else if ("COMPLETED_WITH_ERRORS".equalsIgnoreCase(status)) {
                tvImportStatus.setTextColor(android.graphics.Color.parseColor("#EA580C"));
            } else {
                tvImportStatus.setTextColor(android.graphics.Color.parseColor("#DC2626"));
            }
        }
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
                        Toast.makeText(StudentImportProgressActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
