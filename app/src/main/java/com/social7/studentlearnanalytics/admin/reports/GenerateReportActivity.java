package com.social7.studentlearnanalytics.admin.reports;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-featured SLAS Report Generator with real MySQL lookups, dependent filters,
 * live report preview, and real PDF / Excel (.xlsx) / CSV export & FileProvider sharing.
 */
public class GenerateReportActivity extends AppCompatActivity {

    private MaterialButton typeStudents, typeTeachers, typeCourses, typeAcademic, typeActivity;
    private Spinner spGenerateReportYear, spGenerateReportSemester, spGenerateReportDept, spGenerateReportProgram, spGenerateReportStatus;
    private CheckBox cbMetric1, cbMetric2, cbMetric3, cbMetric4, cbMetric5, cbMetric6, cbMetric7, cbMetric8, cbMetric9, cbMetric10, cbMetric11, cbMetric12;
    private RadioGroup rgExportFormat;
    private RadioButton rbFormatPdf, rbFormatExcel, rbFormatCsv;
    private MaterialButton btnSubmitGenerateReport, btnCancelGenerateReport;

    private String selectedReportType = "Students";

    private final List<OptionItem> ayOptions = new ArrayList<>();
    private final List<OptionItem> allSemesterOptions = new ArrayList<>();
    private final List<OptionItem> filteredSemesterOptions = new ArrayList<>();
    private final List<OptionItem> deptOptions = new ArrayList<>();
    private final List<OptionItem> allProgramOptions = new ArrayList<>();
    private final List<OptionItem> filteredProgramOptions = new ArrayList<>();

    private static class OptionItem {
        final int id;
        final int parentId;
        final String label;

        OptionItem(int id, int parentId, String label) {
            this.id = id;
            this.parentId = parentId;
            this.label = label;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        if (getIntent() != null && getIntent().hasExtra("selected_report_type")) {
            String extra = getIntent().getStringExtra("selected_report_type");
            if (extra != null && !extra.trim().isEmpty()) {
                selectedReportType = extra;
            }
        }

        initViews();
        setupReportTypeButtons();
        setupDefaultSpinners();
        setupListeners();
        selectReportType(selectedReportType);
        loadRealLookups();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackGenerateReport);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        typeStudents = findViewById(R.id.typeStudents);
        typeTeachers = findViewById(R.id.typeTeachers);
        typeCourses = findViewById(R.id.typeCourses);
        typeAcademic = findViewById(R.id.typeAcademic);
        typeActivity = findViewById(R.id.typeActivity);

        spGenerateReportYear = findViewById(R.id.spGenerateReportYear);
        spGenerateReportSemester = findViewById(R.id.spGenerateReportSemester);
        spGenerateReportDept = findViewById(R.id.spGenerateReportDept);
        spGenerateReportProgram = findViewById(R.id.spGenerateReportProgram);
        spGenerateReportStatus = findViewById(R.id.spGenerateReportStatus);

        cbMetric1 = findViewById(R.id.cbMetric1);
        cbMetric2 = findViewById(R.id.cbMetric2);
        cbMetric3 = findViewById(R.id.cbMetric3);
        cbMetric4 = findViewById(R.id.cbMetric4);
        cbMetric5 = findViewById(R.id.cbMetric5);
        cbMetric6 = findViewById(R.id.cbMetric6);
        cbMetric7 = findViewById(R.id.cbMetric7);
        cbMetric8 = findViewById(R.id.cbMetric8);
        cbMetric9 = findViewById(R.id.cbMetric9);
        cbMetric10 = findViewById(R.id.cbMetric10);
        cbMetric11 = findViewById(R.id.cbMetric11);
        cbMetric12 = findViewById(R.id.cbMetric12);

        rgExportFormat = findViewById(R.id.rgExportFormat);
        rbFormatPdf = findViewById(R.id.rbFormatPdf);
        rbFormatExcel = findViewById(R.id.rbFormatExcel);
        rbFormatCsv = findViewById(R.id.rbFormatCsv);

        btnSubmitGenerateReport = findViewById(R.id.btnSubmitGenerateReport);
        btnCancelGenerateReport = findViewById(R.id.btnCancelGenerateReport);
    }

    private void setupReportTypeButtons() {
        if (typeStudents != null) typeStudents.setOnClickListener(v -> selectReportType("Students"));
        if (typeTeachers != null) typeTeachers.setOnClickListener(v -> selectReportType("Teachers"));
        if (typeCourses != null) typeCourses.setOnClickListener(v -> selectReportType("Courses"));
        if (typeAcademic != null) typeAcademic.setOnClickListener(v -> selectReportType("Academic"));
        if (typeActivity != null) typeActivity.setOnClickListener(v -> selectReportType("Attendance"));
    }

    private void selectReportType(String type) {
        this.selectedReportType = type;
        int primaryColor = Color.parseColor("#3155F5");
        int secondaryColor = Color.parseColor("#64748B");
        int whiteColor = Color.WHITE;

        updateTypeButton(typeStudents, "Students".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeTeachers, "Teachers".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeCourses, "Courses".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeAcademic, "Academic".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeActivity, "Attendance".equalsIgnoreCase(type) || "Activity".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
    }

    private void updateTypeButton(MaterialButton button, boolean active, int primaryColor, int secondaryColor, int whiteColor) {
        if (button == null) return;
        button.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
        button.setTextColor(active ? whiteColor : secondaryColor);
    }

    private void setupDefaultSpinners() {
        ayOptions.clear();
        ayOptions.add(new OptionItem(0, 0, "All Academic Years"));
        setOptionAdapter(spGenerateReportYear, ayOptions);

        filteredSemesterOptions.clear();
        filteredSemesterOptions.add(new OptionItem(0, 0, "All Semesters"));
        setOptionAdapter(spGenerateReportSemester, filteredSemesterOptions);

        deptOptions.clear();
        deptOptions.add(new OptionItem(0, 0, "All Departments"));
        setOptionAdapter(spGenerateReportDept, deptOptions);

        filteredProgramOptions.clear();
        filteredProgramOptions.add(new OptionItem(0, 0, "All Programs"));
        setOptionAdapter(spGenerateReportProgram, filteredProgramOptions);

        String[] statuses = {"All", "Active", "Enrolled", "Inactive"};
        if (spGenerateReportStatus != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spGenerateReportStatus.setAdapter(adapter);
        }
    }

    private void loadRealLookups() {
        ApiClient.getApiService(this).getReportsAnalyticsLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")) return;

                JsonObject data = response.body().getAsJsonObject("data");
                ayOptions.clear();
                ayOptions.add(new OptionItem(0, 0, "All Academic Years"));
                if (data.has("academic_years") && data.get("academic_years").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("academic_years")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        ayOptions.add(new OptionItem(UiUtils.optInt(o, "id", 0), 0, UiUtils.optString(o, "name", "")));
                    }
                }
                setOptionAdapter(spGenerateReportYear, ayOptions);

                allSemesterOptions.clear();
                if (data.has("semesters") && data.get("semesters").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("semesters")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        allSemesterOptions.add(new OptionItem(
                                UiUtils.optInt(o, "id", 0),
                                UiUtils.optInt(o, "academic_year_id", 0),
                                UiUtils.optString(o, "name", "")));
                    }
                }
                updateSemesterOptions(0);

                deptOptions.clear();
                deptOptions.add(new OptionItem(0, 0, "All Departments"));
                if (data.has("departments") && data.get("departments").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("departments")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        deptOptions.add(new OptionItem(UiUtils.optInt(o, "id", 0), 0, UiUtils.optString(o, "name", "")));
                    }
                }
                setOptionAdapter(spGenerateReportDept, deptOptions);

                allProgramOptions.clear();
                if (data.has("programs") && data.get("programs").isJsonArray()) {
                    for (JsonElement el : data.getAsJsonArray("programs")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        allProgramOptions.add(new OptionItem(
                                UiUtils.optInt(o, "id", 0),
                                UiUtils.optInt(o, "department_id", 0),
                                UiUtils.optString(o, "name", "")));
                    }
                }
                updateProgramOptions(0);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateSemesterOptions(int selectedAyId) {
        filteredSemesterOptions.clear();
        filteredSemesterOptions.add(new OptionItem(0, 0, "All Semesters"));
        for (OptionItem s : allSemesterOptions) {
            if (selectedAyId <= 0 || s.parentId == selectedAyId) {
                filteredSemesterOptions.add(s);
            }
        }
        setOptionAdapter(spGenerateReportSemester, filteredSemesterOptions);
    }

    private void updateProgramOptions(int selectedDeptId) {
        filteredProgramOptions.clear();
        filteredProgramOptions.add(new OptionItem(0, 0, "All Programs"));
        for (OptionItem p : allProgramOptions) {
            if (selectedDeptId <= 0 || p.parentId == selectedDeptId) {
                filteredProgramOptions.add(p);
            }
        }
        setOptionAdapter(spGenerateReportProgram, filteredProgramOptions);
    }

    private void setOptionAdapter(Spinner spinner, List<OptionItem> items) {
        if (spinner == null) return;
        ArrayAdapter<OptionItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupListeners() {
        if (spGenerateReportYear != null) {
            spGenerateReportYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < ayOptions.size()) {
                        updateSemesterOptions(ayOptions.get(position).id);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (spGenerateReportDept != null) {
            spGenerateReportDept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < deptOptions.size()) {
                        updateProgramOptions(deptOptions.get(position).id);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (btnCancelGenerateReport != null) {
            btnCancelGenerateReport.setOnClickListener(v -> finish());
        }

        if (btnSubmitGenerateReport != null) {
            btnSubmitGenerateReport.setOnClickListener(v -> attemptGenerateReport());
        }
    }

    private String mapCategory() {
        if ("Teachers".equalsIgnoreCase(selectedReportType)) return "teacher";
        if ("Courses".equalsIgnoreCase(selectedReportType)) return "course";
        if ("Academic".equalsIgnoreCase(selectedReportType)) return "academic";
        if ("Attendance".equalsIgnoreCase(selectedReportType) || "Activity".equalsIgnoreCase(selectedReportType)) return "attendance";
        return "student";
    }

    private String selectedFormat() {
        if (rbFormatExcel != null && rbFormatExcel.isChecked()) return "xlsx";
        if (rbFormatCsv != null && rbFormatCsv.isChecked()) return "csv";
        return "pdf";
    }

    private JsonObject buildFilterBody() {
        JsonObject body = new JsonObject();
        body.addProperty("category", mapCategory());
        body.addProperty("report_key", cbMetric12 != null && cbMetric12.isChecked() ? "at_risk" : "information");

        if (spGenerateReportYear != null && spGenerateReportYear.getSelectedItem() instanceof OptionItem) {
            int ayId = ((OptionItem) spGenerateReportYear.getSelectedItem()).id;
            if (ayId > 0) body.addProperty("academic_year_id", ayId);
        }
        if (spGenerateReportSemester != null && spGenerateReportSemester.getSelectedItem() instanceof OptionItem) {
            int semId = ((OptionItem) spGenerateReportSemester.getSelectedItem()).id;
            if (semId > 0) body.addProperty("semester_id", semId);
        }
        if (spGenerateReportDept != null && spGenerateReportDept.getSelectedItem() instanceof OptionItem) {
            int deptId = ((OptionItem) spGenerateReportDept.getSelectedItem()).id;
            if (deptId > 0) body.addProperty("department_id", deptId);
        }
        if (spGenerateReportProgram != null && spGenerateReportProgram.getSelectedItem() instanceof OptionItem) {
            int progId = ((OptionItem) spGenerateReportProgram.getSelectedItem()).id;
            if (progId > 0) body.addProperty("program_id", progId);
        }
        if (spGenerateReportStatus != null && spGenerateReportStatus.getSelectedItem() != null) {
            String st = spGenerateReportStatus.getSelectedItem().toString();
            if (!"All".equalsIgnoreCase(st)) body.addProperty("status", st);
        }
        return body;
    }

    private void attemptGenerateReport() {
        boolean anyMetricChecked = (cbMetric1 != null && cbMetric1.isChecked()) ||
                (cbMetric2 != null && cbMetric2.isChecked()) ||
                (cbMetric3 != null && cbMetric3.isChecked()) ||
                (cbMetric4 != null && cbMetric4.isChecked()) ||
                (cbMetric5 != null && cbMetric5.isChecked()) ||
                (cbMetric6 != null && cbMetric6.isChecked()) ||
                (cbMetric7 != null && cbMetric7.isChecked()) ||
                (cbMetric8 != null && cbMetric8.isChecked()) ||
                (cbMetric9 != null && cbMetric9.isChecked()) ||
                (cbMetric10 != null && cbMetric10.isChecked()) ||
                (cbMetric11 != null && cbMetric11.isChecked()) ||
                (cbMetric12 != null && cbMetric12.isChecked());

        if (!anyMetricChecked) {
            Toast.makeText(this, "Please select at least one report metric", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitGenerateReport.setEnabled(false);
        JsonObject body = buildFilterBody();

        ApiClient.getApiService(this).generateAdminReport(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                btnSubmitGenerateReport.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    showPreviewDialog(response.body().getAsJsonObject("data"), body);
                } else {
                    Toast.makeText(GenerateReportActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSubmitGenerateReport.setEnabled(true);
                Toast.makeText(GenerateReportActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPreviewDialog(JsonObject data, JsonObject filterBody) {
        String title = UiUtils.optString(data, "title", selectedReportType + " Report");
        String generatedAt = UiUtils.optString(data, "generated_at", "");
        int totalRecords = UiUtils.optInt(data, "total_records", 0);

        StringBuilder sb = new StringBuilder();
        sb.append("Generated: ").append(generatedAt).append("\n");
        sb.append("Total Records: ").append(totalRecords).append("\n");
        sb.append("Export Format: ").append(selectedFormat().toUpperCase()).append("\n\n");

        if (data.has("rows") && data.get("rows").isJsonArray()) {
            JsonArray rows = data.getAsJsonArray("rows");
            int limit = Math.min(8, rows.size());
            for (int i = 0; i < limit; i++) {
                JsonArray row = rows.get(i).getAsJsonArray();
                List<String> cells = new ArrayList<>();
                for (JsonElement cell : row) {
                    cells.add(cell.isJsonNull() ? "" : cell.getAsString());
                }
                sb.append("• ").append(String.join(" | ", cells)).append("\n");
            }
            if (rows.size() > limit) {
                sb.append("\n... and ").append(rows.size() - limit).append(" more record(s) in exported file.");
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(sb.toString())
                .setPositiveButton("Export & Share " + selectedFormat().toUpperCase(), (d, w) -> exportAndOpenFile(filterBody))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void exportAndOpenFile(JsonObject body) {
        String format = selectedFormat();
        body.addProperty("format", format);
        btnSubmitGenerateReport.setEnabled(false);

        ApiClient.getApiService(this).exportAdminReport(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                btnSubmitGenerateReport.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(GenerateReportActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (dir == null) dir = getCacheDir();
                    String fileName = "slas_" + mapCategory() + "_report_" + System.currentTimeMillis() + "." + format;
                    File outFile = new File(dir, fileName);

                    try (InputStream in = response.body().byteStream();
                         FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) != -1) {
                            out.write(buf, 0, n);
                        }
                    }

                    Uri uri = FileProvider.getUriForFile(
                            GenerateReportActivity.this,
                            getPackageName() + ".fileprovider",
                            outFile
                    );
                    String mime = "csv".equals(format) ? "text/csv"
                            : ("xlsx".equals(format) ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "application/pdf");

                    Toast.makeText(GenerateReportActivity.this, "Saved: " + outFile.getName(), Toast.LENGTH_LONG).show();

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType(mime);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share or Open Report"));
                } catch (Exception e) {
                    Toast.makeText(GenerateReportActivity.this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                btnSubmitGenerateReport.setEnabled(true);
                Toast.makeText(GenerateReportActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }
}
