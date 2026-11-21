package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.adapter.StudentImportHistoryAdapter;
import com.social7.studentlearnanalytics.model.StudentImportItem;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentImportHistoryActivity extends AppCompatActivity {

    private ImageView btnBackHistory;
    private EditText etSearchHistory;
    private RecyclerView rvImportHistory;

    private StudentImportHistoryAdapter adapter;
    private final List<StudentImportItem> importList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;

        setContentView(R.layout.activity_student_import_history);

        initViews();
        setupListeners();
        loadHistory("");
    }

    private void initViews() {
        btnBackHistory = findViewById(R.id.btnBackHistory);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        rvImportHistory = findViewById(R.id.rvImportHistory);

        adapter = new StudentImportHistoryAdapter(this::downloadErrors);
        rvImportHistory.setLayoutManager(new LinearLayoutManager(this));
        rvImportHistory.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBackHistory != null) btnBackHistory.setOnClickListener(v -> finish());

        if (etSearchHistory != null) {
            etSearchHistory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    loadHistory(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadHistory(String search) {
        ApiClient.getApiService(this).getStudentImports(search, null, 1, 50)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        importList.clear();

                        if (response.isSuccessful() && response.body() != null) {
                            JsonArray arr = response.body().has("data") && response.body().get("data").isJsonArray()
                                    ? response.body().getAsJsonArray("data") : null;
                            if (arr != null) {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                for (JsonElement el : arr) {
                                    importList.add(gson.fromJson(el, StudentImportItem.class));
                                }
                            }
                        }
                        adapter.setList(importList);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isFinishing()) {
                            Toast.makeText(StudentImportHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void downloadErrors(StudentImportItem item) {
        Toast.makeText(this, R.string.downloading_error_report, Toast.LENGTH_SHORT).show();
        ApiClient.getApiService(this).downloadImportErrorReport(item.id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StudentExportDialog.saveExportFile(
                                    getApplicationContext(),
                                    response.body(),
                                    "import_" + item.id + "_error_report.csv",
                                    "text/csv"
                            );
                        } else {
                            Toast.makeText(StudentImportHistoryActivity.this, R.string.failed_to_download_error_report, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(StudentImportHistoryActivity.this, R.string.network_error_42cd08, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
