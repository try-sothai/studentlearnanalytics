package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.reports.GenerateReportActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBackupDataActivity extends AppCompatActivity {

    private MaterialButton btnCreateBackup;
    private TextView tvBackupDbSize, tvBackupTotalRecords, tvLastBackupDate, tvBackupDbEngine;
    private TextView tvBackupSummaryEntities, tvBackupDatabaseName, btnDownloadBackup1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_admin_backup_data_settings);

        ImageView btnBack = findViewById(R.id.btnBackBackup);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnCreateBackup = findViewById(R.id.btnCreateBackup);
        tvBackupDbSize = findViewById(R.id.tvBackupDbSize);
        tvBackupTotalRecords = findViewById(R.id.tvBackupTotalRecords);
        tvLastBackupDate = findViewById(R.id.tvLastBackupDate);
        tvBackupDbEngine = findViewById(R.id.tvBackupDbEngine);
        tvBackupSummaryEntities = findViewById(R.id.tvBackupSummaryEntities);
        tvBackupDatabaseName = findViewById(R.id.tvBackupDatabaseName);
        btnDownloadBackup1 = findViewById(R.id.btnDownloadBackup1);

        if (btnCreateBackup != null) {
            btnCreateBackup.setOnClickListener(v -> loadLiveDatabaseDiagnostics(true));
        }

        if (btnDownloadBackup1 != null) {
            btnDownloadBackup1.setOnClickListener(v -> {
                Intent intent = new Intent(this, GenerateReportActivity.class);
                startActivity(intent);
            });
        }

        loadLiveDatabaseDiagnostics(false);
    }

    private void loadLiveDatabaseDiagnostics(boolean showFeedback) {
        ApiClient.getApiService(this).getAdminSystemStatus().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    JsonObject db = data.has("database") && data.get("database").isJsonObject()
                            ? data.getAsJsonObject("database") : null;
                    if (db != null) {
                        String engine = UiUtils.optString(db, "engine", "MySQL");
                        String schema = UiUtils.optString(db, "database_name", "student_learning_analytics");
                        String status = UiUtils.optString(db, "status", "online");
                        float latency = UiUtils.optFloat(db, "latency_ms", 0f);
                        int tableCount = UiUtils.optInt(db, "table_count", 0);
                        int totalRows = UiUtils.optInt(db, "total_records", 0);
                        float sizeMb = UiUtils.optFloat(db, "size_mb", 0f);

                        if (tvBackupDbSize != null) {
                            tvBackupDbSize.setText(sizeMb > 0f ? String.format(Locale.US, "%.2f MB", sizeMb) : "< 0.1 MB");
                        }
                        if (tvBackupTotalRecords != null) {
                            tvBackupTotalRecords.setText(String.format(Locale.US, "%d rows (%d tables)", totalRows, tableCount));
                        }
                        if (tvLastBackupDate != null) {
                            tvLastBackupDate.setText(String.format(Locale.US, "%s • %.1f ms",
                                    "online".equalsIgnoreCase(status) ? "Online" : status, latency));
                        }
                        if (tvBackupDbEngine != null) {
                            tvBackupDbEngine.setText(engine);
                        }
                        if (tvBackupDatabaseName != null) {
                            tvBackupDatabaseName.setText("Schema: " + schema + " (" + tableCount + " tables)");
                        }
                    }
                    if (showFeedback) {
                        Toast.makeText(AdminBackupDataActivity.this, "Database diagnostics refreshed.", Toast.LENGTH_SHORT).show();
                    }
                } else if (showFeedback) {
                    Toast.makeText(AdminBackupDataActivity.this, "Failed to retrieve database status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (tvLastBackupDate != null) tvLastBackupDate.setText("Offline / Unreachable");
                if (showFeedback) {
                    Toast.makeText(AdminBackupDataActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ApiClient.getApiService(this).getAdminStatistics().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject stats = response.body().getAsJsonObject("data");
                    int users = UiUtils.optInt(stats, "user_count", 0);
                    int students = UiUtils.optInt(stats, "total_students", 0);
                    int teachers = UiUtils.optInt(stats, "total_teachers", 0);
                    int courses = UiUtils.optInt(stats, "total_courses", 0);
                    int classes = UiUtils.optInt(stats, "total_classes", 0);
                    if (tvBackupSummaryEntities != null) {
                        tvBackupSummaryEntities.setText(String.format(Locale.US,
                                "%d Users • %d Students • %d Teachers • %d Courses • %d Classes",
                                users, students, teachers, courses, classes));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }
}
