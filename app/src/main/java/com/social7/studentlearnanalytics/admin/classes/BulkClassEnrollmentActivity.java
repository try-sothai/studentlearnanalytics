package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.utils.UiUtils;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen Bulk Student Enrollment page replacing the manual single-student AlertDialog.
 */
public class BulkClassEnrollmentActivity extends AppCompatActivity {

    private TextView tvSelectedClassTitle, tvSelectedClassMeta, tvSelectedClassStatus;
    private TextView tvSelectedStudentCount, tvBulkEmptyState;
    private TextInputEditText etSearchStudents;
    private RecyclerView rvBulkStudents;
    private ProgressBar pbBulkStudents;
    private MaterialButton btnSelectAll, btnDeselectAll, btnLoadMore, btnConfirmEnroll, btnImportCsv;

    private int selectedClassId = -1;
    private String selectedClassName = "";
    private String selectedClassCode = "";
    private String selectedClassAcademicYear = "";
    private Integer selectedClassYearLevel = null;
    private String selectedClassYearLabel = "";
    private String selectedClassStatus = "";

    private int currentPage = 1;
    private int lastPage = 1;
    private String currentSearch = "";

    private final List<StudentRow> studentRows = new ArrayList<>();
    private final Set<Integer> selectedStudentIds = new LinkedHashSet<>();
    private BulkStudentAdapter adapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private final ActivityResultLauncher<Intent> classPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String raw = data.getStringExtra(RecordSelectorActivity.RESULT_RAW_JSON);
                    if (raw != null) {
                        try {
                            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                            selectedClassId = UiUtils.optInt(obj, "id", -1);
                            selectedClassName = UiUtils.optString(obj, "name", "");
                            selectedClassCode = UiUtils.optString(obj, "code", "");
                            selectedClassAcademicYear = UiUtils.optString(obj, "academic_year", "");
                            selectedClassYearLevel = YearLevelHelper.parse(UiUtils.optString(obj, "year_level", ""));
                            selectedClassYearLabel = selectedClassYearLevel != null
                                    ? YearLevelHelper.toFullLabel(this, selectedClassYearLevel) : "";
                            selectedClassStatus = UiUtils.optString(obj, "status", "active");
                            String dept = UiUtils.optString(obj, "department_name", "");
                            int count = UiUtils.optInt(obj, "students_count", 0);

                            tvSelectedClassTitle.setText(selectedClassName + " (" + selectedClassCode + ")");
                            tvSelectedClassMeta.setVisibility(View.VISIBLE);
                            tvSelectedClassMeta.setText(
                                    (dept.isEmpty() ? "" : dept + " • ")
                                            + (selectedClassYearLabel.isEmpty() ? "" : selectedClassYearLabel + " • ")
                                            + (selectedClassAcademicYear.isEmpty() ? "" : selectedClassAcademicYear + " • ")
                                            + count + " enrolled"
                            );
                            tvSelectedClassStatus.setVisibility(View.VISIBLE);
                            tvSelectedClassStatus.setText(selectedClassStatus.toUpperCase());

                            selectedStudentIds.clear();
                            updateCounter();

                            if (!"active".equalsIgnoreCase(selectedClassStatus)) {
                                Toast.makeText(this, R.string.err_inactive_class, Toast.LENGTH_LONG).show();
                            }
                            if (selectedClassId > 0) {
                                loadStudentsForClass(1, false);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            });

    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uploadAndPreviewEnrollmentFile(uri);
                }
            });

    public static class StudentRow {
        public int id;
        public String studentCode;
        public String fullName;
        public String departmentName;
        public String programName;
        public int yearLevel;
        public String currentClassCode;
        public boolean alreadyEnrolled;
        public boolean isEligible;
        public String ineligibleReason;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        setContentView(R.layout.activity_bulk_class_enrollment);
        bindViews();
        updateCounter();
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnBulkEnrollBack);
        MaterialCardView cardSelectClass = findViewById(R.id.cardSelectTargetClass);
        tvSelectedClassTitle = findViewById(R.id.tvSelectedClassTitle);
        tvSelectedClassMeta = findViewById(R.id.tvSelectedClassMeta);
        tvSelectedClassStatus = findViewById(R.id.tvSelectedClassStatus);
        tvSelectedStudentCount = findViewById(R.id.tvSelectedStudentCount);
        tvBulkEmptyState = findViewById(R.id.tvBulkEmptyState);
        etSearchStudents = findViewById(R.id.etBulkSearchStudents);
        rvBulkStudents = findViewById(R.id.rvBulkStudents);
        pbBulkStudents = findViewById(R.id.pbBulkStudents);
        btnSelectAll = findViewById(R.id.btnSelectAllEligible);
        btnDeselectAll = findViewById(R.id.btnDeselectAllStudents);
        btnLoadMore = findViewById(R.id.btnBulkLoadMore);
        btnConfirmEnroll = findViewById(R.id.btnConfirmBulkEnroll);
        btnImportCsv = findViewById(R.id.btnBulkImportCsv);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (cardSelectClass != null) {
            cardSelectClass.setOnClickListener(v -> {
                Intent intent = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_CLASS);
                if (selectedClassId > 0) {
                    intent.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedClassId));
                }
                classPickerLauncher.launch(intent);
            });
        }

        adapter = new BulkStudentAdapter();
        rvBulkStudents.setLayoutManager(new LinearLayoutManager(this));
        rvBulkStudents.setAdapter(adapter);

        if (etSearchStudents != null) {
            etSearchStudents.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                    currentSearch = s != null ? s.toString().trim() : "";
                    pendingSearch = () -> {
                        if (selectedClassId > 0) loadStudentsForClass(1, false);
                    };
                    searchHandler.postDelayed(pendingSearch, 300);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSelectAll != null) {
            btnSelectAll.setOnClickListener(v -> {
                for (StudentRow r : studentRows) {
                    if (r.isEligible) selectedStudentIds.add(r.id);
                }
                adapter.notifyDataSetChanged();
                updateCounter();
            });
        }

        if (btnDeselectAll != null) {
            btnDeselectAll.setOnClickListener(v -> {
                selectedStudentIds.clear();
                adapter.notifyDataSetChanged();
                updateCounter();
            });
        }

        if (btnLoadMore != null) {
            btnLoadMore.setOnClickListener(v -> {
                if (selectedClassId > 0 && currentPage < lastPage) {
                    loadStudentsForClass(currentPage + 1, true);
                }
            });
        }

        if (btnConfirmEnroll != null) {
            btnConfirmEnroll.setOnClickListener(v -> showConfirmationSummary());
        }

        if (btnImportCsv != null) {
            btnImportCsv.setOnClickListener(v -> showCsvImportOptions());
        }
    }

    private void loadStudentsForClass(int page, boolean append) {
        if (selectedClassId <= 0) return;
        pbBulkStudents.setVisibility(View.VISIBLE);
        if (!append) tvBulkEmptyState.setVisibility(View.GONE);

        ApiClient.getApiService(this)
                .getClassEligibleStudents(selectedClassId, currentSearch, null, null, null, page, 50)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        pbBulkStudents.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                            if (!append) studentRows.clear();
                            JsonArray arr = response.body().getAsJsonArray("data");
                            for (JsonElement el : arr) {
                                if (!el.isJsonObject()) continue;
                                JsonObject o = el.getAsJsonObject();
                                StudentRow row = new StudentRow();
                                row.id = UiUtils.optInt(o, "id", 0);
                                row.studentCode = UiUtils.optString(o, "student_code", "");
                                row.fullName = UiUtils.optString(o, "full_name", "");
                                row.departmentName = UiUtils.optString(o, "department_name", "");
                                row.programName = UiUtils.optString(o, "program_name", "");
                                Integer parsedYl = YearLevelHelper.parse(UiUtils.optString(o, "year_level", ""));
                                row.yearLevel = parsedYl != null ? parsedYl : 0;
                                row.currentClassCode = UiUtils.optString(o, "current_class_code", "");
                                row.alreadyEnrolled = o.has("already_enrolled_in_selected")
                                        && !o.get("already_enrolled_in_selected").isJsonNull()
                                        && o.get("already_enrolled_in_selected").getAsBoolean();
                                row.isEligible = o.has("is_eligible")
                                        && !o.get("is_eligible").isJsonNull()
                                        && o.get("is_eligible").getAsBoolean();
                                row.ineligibleReason = UiUtils.optString(o, "ineligible_reason", "");
                                studentRows.add(row);
                            }

                            if (response.body().has("meta") && response.body().get("meta").isJsonObject()) {
                                JsonObject meta = response.body().getAsJsonObject("meta");
                                currentPage = UiUtils.optInt(meta, "current_page", page);
                                lastPage = UiUtils.optInt(meta, "last_page", page);
                            } else {
                                currentPage = page;
                                lastPage = page;
                            }

                            btnLoadMore.setVisibility(currentPage < lastPage ? View.VISIBLE : View.GONE);
                            tvBulkEmptyState.setVisibility(studentRows.isEmpty() ? View.VISIBLE : View.GONE);
                            if (studentRows.isEmpty()) tvBulkEmptyState.setText(R.string.search_no_results);
                            adapter.notifyDataSetChanged();
                        } else {
                            tvBulkEmptyState.setVisibility(View.VISIBLE);
                            tvBulkEmptyState.setText(R.string.network_error_server);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        pbBulkStudents.setVisibility(View.GONE);
                        tvBulkEmptyState.setVisibility(View.VISIBLE);
                        tvBulkEmptyState.setText(R.string.network_error_server);
                    }
                });
    }

    private void updateCounter() {
        int count = selectedStudentIds.size();
        tvSelectedStudentCount.setText(getString(R.string.selected_count_format, count));
        btnConfirmEnroll.setEnabled(selectedClassId > 0
                && "active".equalsIgnoreCase(selectedClassStatus)
                && count > 0);
    }

    private void showConfirmationSummary() {
        if (selectedClassId <= 0) {
            Toast.makeText(this, R.string.err_select_active_class, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!"active".equalsIgnoreCase(selectedClassStatus)) {
            Toast.makeText(this, R.string.err_inactive_class, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStudentIds.isEmpty()) {
            Toast.makeText(this, R.string.err_select_at_least_one_student, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder preview = new StringBuilder();
        preview.append("Class: ").append(selectedClassName).append(" (").append(selectedClassCode).append(")\n");
        if (!selectedClassYearLabel.isEmpty()) {
            preview.append(getString(R.string.student_year_level)).append(": ").append(selectedClassYearLabel).append("\n");
        }
        if (!selectedClassAcademicYear.isEmpty()) {
            preview.append("Academic Year: ").append(selectedClassAcademicYear).append("\n");
        }
        preview.append("Students Selected: ").append(selectedStudentIds.size()).append("\n\n");

        int mismatchCount = 0;
        int shown = 0;
        for (StudentRow r : studentRows) {
            if (selectedStudentIds.contains(r.id)) {
                boolean mismatch = selectedClassYearLevel != null && r.yearLevel > 0 && !selectedClassYearLevel.equals(r.yearLevel);
                if (mismatch) mismatchCount++;
                String ylBadge = r.yearLevel > 0 ? (" [" + YearLevelHelper.toBadgeText(this, r.yearLevel) + "]") : "";
                if (shown < 10) {
                    preview.append("• ").append(r.studentCode).append(" — ").append(r.fullName).append(ylBadge).append("\n");
                    shown++;
                }
            }
        }
        if (selectedStudentIds.size() > 10) {
            preview.append("... and ").append(selectedStudentIds.size() - 10).append(" more\n");
        }
        if (mismatchCount > 0) {
            preview.append("\n⚠ ").append(getString(R.string.year_level_mismatch_warning)).append(" (").append(mismatchCount).append(")\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_enrollment)
                .setMessage(preview.toString())
                .setPositiveButton(R.string.confirm_enrollment, (dialog, which) -> executeBulkEnroll())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeBulkEnroll() {
        JsonObject body = new JsonObject();
        JsonArray ids = new JsonArray();
        for (Integer id : selectedStudentIds) {
            ids.add(id);
        }
        body.add("student_ids", ids);

        pbBulkStudents.setVisibility(View.VISIBLE);
        btnConfirmEnroll.setEnabled(false);

        ApiClient.getApiService(this).bulkEnrollStudents(selectedClassId, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbBulkStudents.setVisibility(View.GONE);
                updateCounter();
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    int enrolled = UiUtils.optInt(data, "enrolled_count", 0);
                    int skipped = UiUtils.optInt(data, "skipped_count", 0);
                    int failed = UiUtils.optInt(data, "failed_count", 0);

                    new MaterialAlertDialogBuilder(BulkClassEnrollmentActivity.this)
                            .setTitle(R.string.bulk_enrollment_title)
                            .setMessage("Enrolled: " + enrolled + "\nSkipped (Already Enrolled): " + skipped + "\nFailed: " + failed)
                            .setPositiveButton(android.R.string.ok, (d, w) -> {
                                setResult(RESULT_OK);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(BulkClassEnrollmentActivity.this, R.string.err_inactive_class, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbBulkStudents.setVisibility(View.GONE);
                updateCounter();
                Toast.makeText(BulkClassEnrollmentActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCsvImportOptions() {
        String[] options = {getString(R.string.download_template), getString(R.string.upload_enrollment_file)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.import_csv_excel)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent browser = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(ApiClient.getBaseUrl() + "admin/class-enrollments/template"));
                        startActivity(browser);
                    } else {
                        filePickerLauncher.launch(new String[]{
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "*/*"
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void uploadAndPreviewEnrollmentFile(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) buffer.write(chunk, 0, n);

            String fileName = "enrollments.csv";
            RequestBody reqFile = RequestBody.create(MediaType.parse("application/octet-stream"), buffer.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, reqFile);

            pbBulkStudents.setVisibility(View.VISIBLE);
            ApiClient.getApiService(this).previewClassEnrollmentImport(part).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    pbBulkStudents.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                        showImportPreviewDialog(response.body().getAsJsonObject("data"));
                    } else {
                        Toast.makeText(BulkClassEnrollmentActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    pbBulkStudents.setVisibility(View.GONE);
                    Toast.makeText(BulkClassEnrollmentActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportPreviewDialog(JsonObject data) {
        int validCount = UiUtils.optInt(data, "valid_count", 0);
        int invalidCount = UiUtils.optInt(data, "invalid_count", 0);
        JsonArray validRows = data.has("valid_rows") && data.get("valid_rows").isJsonArray()
                ? data.getAsJsonArray("valid_rows") : new JsonArray();
        JsonArray invalidRows = data.has("invalid_rows") && data.get("invalid_rows").isJsonArray()
                ? data.getAsJsonArray("invalid_rows") : new JsonArray();

        StringBuilder sb = new StringBuilder();
        sb.append("Valid Rows: ").append(validCount).append("\n");
        sb.append("Invalid / Duplicate Rows: ").append(invalidCount).append("\n\n");

        for (int i = 0; i < Math.min(5, invalidRows.size()); i++) {
            JsonObject inv = invalidRows.get(i).getAsJsonObject();
            sb.append("Row ").append(UiUtils.optInt(inv, "row_number", 0))
                    .append(" (").append(UiUtils.optString(inv, "student_id", "")).append("): ")
                    .append(UiUtils.optString(inv, "error", "")).append("\n");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.import_csv_excel)
                .setMessage(sb.toString())
                .setNegativeButton(R.string.cancel, null);

        if (validCount > 0) {
            builder.setPositiveButton(R.string.confirm_import, (d, w) -> confirmImportRows(validRows));
        }
        builder.show();
    }

    private void confirmImportRows(JsonArray validRows) {
        JsonObject body = new JsonObject();
        body.add("rows", validRows);
        pbBulkStudents.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).confirmClassEnrollmentImport(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                pbBulkStudents.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    int enrolled = UiUtils.optInt(data, "enrolled_count", 0);
                    int failed = UiUtils.optInt(data, "failed_count", 0);
                    Toast.makeText(BulkClassEnrollmentActivity.this,
                            "Imported " + enrolled + " enrollment(s), " + failed + " failed.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(BulkClassEnrollmentActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                pbBulkStudents.setVisibility(View.GONE);
                Toast.makeText(BulkClassEnrollmentActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class BulkStudentAdapter extends RecyclerView.Adapter<BulkStudentAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_enroll_student, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudentRow row = studentRows.get(position);
            boolean checked = selectedStudentIds.contains(row.id);

            holder.tvName.setText(row.fullName);
            holder.tvId.setText(row.studentCode);
            String ylLabel = row.yearLevel > 0 ? YearLevelHelper.toFullLabel(BulkClassEnrollmentActivity.this, row.yearLevel) : "";
            boolean isCrossYear = selectedClassYearLevel != null && row.yearLevel > 0 && !selectedClassYearLevel.equals(row.yearLevel);
            if (isCrossYear && !ylLabel.isEmpty()) {
                ylLabel = ylLabel + " (" + getString(R.string.cross_year_level) + ")";
            }
            holder.tvMeta.setText(
                    (row.departmentName.isEmpty() ? "" : row.departmentName + " • ")
                            + (row.programName.isEmpty() ? "" : row.programName + " • ")
                            + ylLabel
                            + (row.currentClassCode.isEmpty() ? "" : " • Current: " + row.currentClassCode)
            );

            if (row.alreadyEnrolled) {
                holder.tvStatus.setText(R.string.already_enrolled_badge);
                holder.tvStatus.setTextColor(Color.parseColor("#0F766E"));
            } else if (row.isEligible) {
                holder.tvStatus.setText(R.string.eligible_badge);
                holder.tvStatus.setTextColor(Color.parseColor("#3B49DF"));
            } else {
                holder.tvStatus.setText(R.string.ineligible_badge);
                holder.tvStatus.setTextColor(Color.parseColor("#B91C1C"));
            }

            holder.cb.setEnabled(row.isEligible);
            holder.cb.setChecked(checked);
            holder.card.setAlpha(row.isEligible ? 1.0f : 0.65f);
            holder.card.setStrokeColor(Color.parseColor(checked ? "#3B49DF" : "#E2E8F0"));
            holder.card.setStrokeWidth(checked ? 4 : 2);

            holder.card.setOnClickListener(v -> {
                if (!row.isEligible) {
                    String msg = row.ineligibleReason != null && !row.ineligibleReason.isEmpty()
                            ? row.ineligibleReason
                            : getString(R.string.err_duplicate_enrollment);
                    Toast.makeText(BulkClassEnrollmentActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedStudentIds.contains(row.id)) {
                    selectedStudentIds.remove(row.id);
                } else {
                    selectedStudentIds.add(row.id);
                }
                notifyItemChanged(position);
                updateCounter();
            });
        }

        @Override
        public int getItemCount() {
            return studentRows.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCardView card;
            final MaterialCheckBox cb;
            final TextView tvName, tvId, tvMeta, tvStatus;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardBulkStudentRoot);
                cb = itemView.findViewById(R.id.cbBulkStudent);
                tvName = itemView.findViewById(R.id.tvBulkStudentName);
                tvId = itemView.findViewById(R.id.tvBulkStudentId);
                tvMeta = itemView.findViewById(R.id.tvBulkStudentMeta);
                tvStatus = itemView.findViewById(R.id.tvBulkStudentStatus);
            }
        }
    }
}
