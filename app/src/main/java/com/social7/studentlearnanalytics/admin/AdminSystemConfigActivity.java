package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.users.AddStudentActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * System Configuration & Academic Structure Setup (Academic Years, Semesters, Departments).
 */
public class AdminSystemConfigActivity extends AppCompatActivity {

    private Spinner spDefaultYear, spDefaultSemester;
    private MaterialButton btnSaveSysConfig, btnAddAcademicYear, btnAddSemester, btnAddDepartment, btnAddMajor, btnManageMajors;

    private final List<Integer> yearIds = new ArrayList<>();
    private final List<String> yearNames = new ArrayList<>();
    private final List<JsonObject> yearObjects = new ArrayList<>();
    private final List<Integer> semesterIds = new ArrayList<>();
    private final List<String> semesterNames = new ArrayList<>();
    private final List<JsonObject> semesterObjects = new ArrayList<>();
    private final List<AddStudentActivity.IdNameItem> departmentItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        setContentView(R.layout.activity_admin_system_config_settings);

        ImageView btnBack = findViewById(R.id.btnBackSystemConfig);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spDefaultYear = findViewById(R.id.spDefaultYear);
        spDefaultSemester = findViewById(R.id.spDefaultSemester);
        btnSaveSysConfig = findViewById(R.id.btnSaveSysConfig);
        btnAddAcademicYear = findViewById(R.id.btnAddAcademicYear);
        btnAddSemester = findViewById(R.id.btnAddSemester);
        btnAddDepartment = findViewById(R.id.btnAddDepartment);
        btnAddMajor = findViewById(R.id.btnAddMajor);
        btnManageMajors = findViewById(R.id.btnManageMajors);

        loadAcademicYears();
        loadSemesters();
        loadDepartments();

        if (btnAddAcademicYear != null) {
            btnAddAcademicYear.setOnClickListener(v -> showCreateAcademicYearDialog());
        }
        if (btnAddSemester != null) {
            btnAddSemester.setOnClickListener(v -> showCreateSemesterDialog());
        }
        if (btnAddDepartment != null) {
            btnAddDepartment.setOnClickListener(v -> showCreateDepartmentDialog());
        }
        if (btnAddMajor != null) {
            btnAddMajor.setOnClickListener(v -> showCreateMajorDialog(null));
        }
        if (btnManageMajors != null) {
            btnManageMajors.setOnClickListener(v -> showManageMajorsDialog());
        }
        if (btnSaveSysConfig != null) {
            btnSaveSysConfig.setOnClickListener(v -> saveSystemConfiguration());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin");
    }

    private void loadAcademicYears() {
        ApiClient.getApiService(this).getAcademicYears().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                yearIds.clear();
                yearNames.clear();
                yearObjects.clear();
                int activeIndex = 0;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        yearIds.add(UiUtils.optInt(o, "id", 0));
                        yearObjects.add(o);
                        String name = UiUtils.optString(o, "name", "Academic Year");
                        String st = UiUtils.optString(o, "status", "");
                        if ("active".equalsIgnoreCase(st)) {
                            activeIndex = yearIds.size() - 1;
                        }
                        yearNames.add(st.isEmpty() ? name : (name + " (" + st + ")"));
                    }
                }
                if (yearNames.isEmpty()) yearNames.add("No academic years yet");
                if (spDefaultYear != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminSystemConfigActivity.this, android.R.layout.simple_spinner_item, yearNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDefaultYear.setAdapter(adapter);
                    if (!yearIds.isEmpty() && activeIndex < yearNames.size()) {
                        spDefaultYear.setSelection(activeIndex);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void loadSemesters() {
        ApiClient.getApiService(this).getSemesters().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                semesterIds.clear();
                semesterNames.clear();
                semesterObjects.clear();
                int activeIndex = 0;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        semesterIds.add(UiUtils.optInt(o, "id", 0));
                        semesterObjects.add(o);
                        String name = UiUtils.optString(o, "name", "Semester");
                        String st = UiUtils.optString(o, "status", "");
                        if ("active".equalsIgnoreCase(st)) {
                            activeIndex = semesterIds.size() - 1;
                        }
                        String ayName = "";
                        if (o.has("academic_year") && o.get("academic_year").isJsonObject()) {
                            ayName = UiUtils.optString(o.getAsJsonObject("academic_year"), "name", "");
                        }
                        semesterNames.add(ayName.isEmpty() ? name : (name + " (" + ayName + ")"));
                    }
                }
                if (semesterNames.isEmpty()) semesterNames.add("No semesters yet");
                if (spDefaultSemester != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminSystemConfigActivity.this, android.R.layout.simple_spinner_item, semesterNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDefaultSemester.setAdapter(adapter);
                    if (!semesterIds.isEmpty() && activeIndex < semesterNames.size()) {
                        spDefaultSemester.setSelection(activeIndex);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private String normalizeIsoDate(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        return t.length() >= 10 ? t.substring(0, 10) : t;
    }

    private void saveSystemConfiguration() {
        int yearPos = spDefaultYear != null ? spDefaultYear.getSelectedItemPosition() : -1;
        int semPos = spDefaultSemester != null ? spDefaultSemester.getSelectedItemPosition() : -1;

        JsonObject selectedYear = (yearPos >= 0 && yearPos < yearObjects.size()) ? yearObjects.get(yearPos) : null;
        JsonObject selectedSem = (semPos >= 0 && semPos < semesterObjects.size()) ? semesterObjects.get(semPos) : null;

        if (selectedYear == null && selectedSem == null) {
            Toast.makeText(this, "No Academic Year or Semester available to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(false);

        if (selectedYear != null) {
            int yearId = UiUtils.optInt(selectedYear, "id", 0);
            JsonObject yearBody = new JsonObject();
            yearBody.addProperty("name", UiUtils.optString(selectedYear, "name", ""));
            yearBody.addProperty("start_date", normalizeIsoDate(UiUtils.optString(selectedYear, "start_date", "")));
            yearBody.addProperty("end_date", normalizeIsoDate(UiUtils.optString(selectedYear, "end_date", "")));
            yearBody.addProperty("status", "active");

            ApiClient.getApiService(this).updateAcademicYear(String.valueOf(yearId), yearBody).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (!response.isSuccessful()) {
                        if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(true);
                        Toast.makeText(AdminSystemConfigActivity.this, "Failed to update Academic Year (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                        return;
                    }
                    persistSelectedSemester(selectedSem);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(true);
                    Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            persistSelectedSemester(selectedSem);
        }
    }

    private void persistSelectedSemester(JsonObject selectedSem) {
        if (selectedSem == null) {
            if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(true);
            Toast.makeText(this, R.string.system_configuration_saved_successfully, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int semId = UiUtils.optInt(selectedSem, "id", 0);
        JsonObject semBody = new JsonObject();
        semBody.addProperty("academic_year_id", UiUtils.optInt(selectedSem, "academic_year_id", 0));
        semBody.addProperty("name", UiUtils.optString(selectedSem, "name", ""));
        semBody.addProperty("semester_number", UiUtils.optInt(selectedSem, "semester_number", 1));
        semBody.addProperty("start_date", normalizeIsoDate(UiUtils.optString(selectedSem, "start_date", "")));
        semBody.addProperty("end_date", normalizeIsoDate(UiUtils.optString(selectedSem, "end_date", "")));
        semBody.addProperty("status", "active");

        ApiClient.getApiService(this).updateSemester(String.valueOf(semId), semBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminSystemConfigActivity.this, R.string.system_configuration_saved_successfully, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminSystemConfigActivity.this, "Failed to update Semester (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveSysConfig != null) btnSaveSysConfig.setEnabled(true);
                Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAcademicYearDialog() {
        LinearLayout layout = makeDialogContainer();
        TextInputEditText etName = addInput(layout, "Academic Year Name * (e.g. 2028-2029)", "");
        TextInputEditText etStart = addInput(layout, "Start Date * (YYYY-MM-DD)", "2026-10-01");
        TextInputEditText etEnd = addInput(layout, "End Date * (YYYY-MM-DD)", "2027-07-31");
        DatePickerHelper.attachDatePicker(null, etStart, false);
        DatePickerHelper.attachDatePicker(null, etEnd, false);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Academic Year")
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Create", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("name", textOf(etName));
                    body.addProperty("start_date", textOf(etStart));
                    body.addProperty("end_date", textOf(etEnd));
                    body.addProperty("status", "active");
                    ApiClient.getApiService(this).createAcademicYear(body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminSystemConfigActivity.this, "Academic Year created!", Toast.LENGTH_SHORT).show();
                                loadAcademicYears();
                            } else {
                                Toast.makeText(AdminSystemConfigActivity.this, "Invalid or duplicate Academic Year.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void showCreateSemesterDialog() {
        if (yearIds.isEmpty()) {
            Toast.makeText(this, "Create an Academic Year first.", Toast.LENGTH_SHORT).show();
            return;
        }
        int selectedIdx = spDefaultYear != null ? spDefaultYear.getSelectedItemPosition() : 0;
        int ayId = (selectedIdx >= 0 && selectedIdx < yearIds.size()) ? yearIds.get(selectedIdx) : yearIds.get(0);

        LinearLayout layout = makeDialogContainer();
        TextInputEditText etName = addInput(layout, "Semester Name * (e.g. Semester 1)", "Semester 1");
        TextInputEditText etNum = addInput(layout, "Semester Number * (1 or 2)", "1");
        TextInputEditText etStart = addInput(layout, "Start Date * (YYYY-MM-DD)", "2026-10-01");
        TextInputEditText etEnd = addInput(layout, "End Date * (YYYY-MM-DD)", "2027-02-28");
        DatePickerHelper.attachDatePicker(null, etStart, false);
        DatePickerHelper.attachDatePicker(null, etEnd, false);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Semester")
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Create", (d, w) -> {
                    int num = 1;
                    try {
                        num = Integer.parseInt(textOf(etNum));
                    } catch (Exception ignored) {
                    }
                    JsonObject body = new JsonObject();
                    body.addProperty("academic_year_id", ayId);
                    body.addProperty("name", textOf(etName));
                    body.addProperty("semester_number", num);
                    body.addProperty("start_date", textOf(etStart));
                    body.addProperty("end_date", textOf(etEnd));
                    body.addProperty("status", "active");
                    ApiClient.getApiService(this).createSemester(body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminSystemConfigActivity.this, "Semester created!", Toast.LENGTH_SHORT).show();
                                loadSemesters();
                            } else {
                                Toast.makeText(AdminSystemConfigActivity.this, "Validation failed.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void showCreateDepartmentDialog() {
        LinearLayout layout = makeDialogContainer();
        TextInputEditText etName = addInput(layout, "Faculty Name * (e.g. Cybersecurity)", "");
        TextInputEditText etCode = addInput(layout, "Faculty Code * (e.g. CSEC)", "");
        TextInputEditText etDesc = addInput(layout, "Description (optional)", "");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Faculty")
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Create", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("name", textOf(etName));
                    body.addProperty("code", textOf(etCode).toUpperCase());
                    if (!textOf(etDesc).isEmpty()) body.addProperty("description", textOf(etDesc));
                    body.addProperty("status", "active");
                    ApiClient.getApiService(this).createDepartment(body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminSystemConfigActivity.this, "Faculty created!", Toast.LENGTH_SHORT).show();
                                loadDepartments();
                            } else {
                                Toast.makeText(AdminSystemConfigActivity.this, "Duplicate or invalid Faculty.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private LinearLayout makeDialogContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 20, 48, 8);
        return layout;
    }

    private TextInputEditText addInput(LinearLayout container, String hint, String prefill) {
        TextInputLayout til = new TextInputLayout(this);
        til.setHint(hint);
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        til.setLayoutParams(lp);
        TextInputEditText et = new TextInputEditText(til.getContext());
        if (prefill != null && !prefill.isEmpty()) et.setText(prefill);
        til.addView(et);
        container.addView(til);
        return et;
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void loadDepartments() {
        ApiClient.getApiService(this).getDepartments().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().has("data")
                        && response.body().get("data").isJsonArray()) {
                    departmentItems.clear();
                    for (JsonElement el : response.body().getAsJsonArray("data")) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        int id = UiUtils.optInt(o, "id", 0);
                        String status = UiUtils.optString(o, "status", "active");
                        if (id <= 0 || "inactive".equalsIgnoreCase(status)) continue;
                        String name = UiUtils.optString(o, "name", "");
                        String code = UiUtils.optString(o, "code", "");
                        departmentItems.add(new AddStudentActivity.IdNameItem(id, name, code, 0));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    private void showCreateMajorDialog(@Nullable Integer preselectedDepartmentId) {
        if (departmentItems.isEmpty()) {
            loadDepartments();
            Toast.makeText(this, "Loading faculties — please try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = makeDialogContainer();

        TextView tvFacultyLabel = new TextView(this);
        tvFacultyLabel.setText(R.string.department_0e3338);
        tvFacultyLabel.setTextSize(12f);
        layout.addView(tvFacultyLabel);

        Spinner spFaculty = new Spinner(this);
        ArrayAdapter<AddStudentActivity.IdNameItem> facultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>(departmentItems));
        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFaculty.setAdapter(facultyAdapter);
        if (preselectedDepartmentId != null) {
            for (int i = 0; i < departmentItems.size(); i++) {
                if (departmentItems.get(i).id == preselectedDepartmentId) {
                    spFaculty.setSelection(i);
                    break;
                }
            }
        }
        LinearLayout.LayoutParams spParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spParams.setMargins(0, 6, 0, 12);
        spFaculty.setLayoutParams(spParams);
        layout.addView(spFaculty);

        TextInputEditText etMajorName = addInput(layout, "Major Name * (e.g. Computer Science)", "");
        TextInputEditText etMajorCode = addInput(layout, "Major Code (optional, e.g. FOSAT-CS)", "");
        TextInputEditText etDuration = addInput(layout, "Duration Years (1-6)", "4");
        etDuration.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.create_major)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Create", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Object sel = spFaculty.getSelectedItem();
            if (!(sel instanceof AddStudentActivity.IdNameItem)) {
                Toast.makeText(this, R.string.please_select_a_department, Toast.LENGTH_SHORT).show();
                return;
            }
            AddStudentActivity.IdNameItem dept = (AddStudentActivity.IdNameItem) sel;
            String majorName = textOf(etMajorName);
            String majorCode = textOf(etMajorCode).toUpperCase(Locale.ROOT);
            String durRaw = textOf(etDuration);
            if (majorName.isEmpty()) {
                etMajorName.setError("Major name is required");
                return;
            }
            int duration = 4;
            if (!durRaw.isEmpty()) {
                try {
                    duration = Integer.parseInt(durRaw);
                } catch (NumberFormatException ignored) {
                    duration = 4;
                }
            }
            if (duration < 1 || duration > 6) {
                etDuration.setError("Duration must be between 1 and 6 years");
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("department_id", dept.id);
            body.addProperty("name", majorName);
            if (!majorCode.isEmpty()) {
                body.addProperty("code", majorCode);
            }
            body.addProperty("duration_years", duration);
            body.addProperty("status", "active");

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ApiClient.getApiService(this).createProgram(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminSystemConfigActivity.this, R.string.major_created_successfully, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(AdminSystemConfigActivity.this, extractApiError(response, "Failed to create major."), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private void showManageMajorsDialog() {
        ApiClient.getApiService(this).getPrograms(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().has("data")
                        || !response.body().get("data").isJsonArray()) {
                    Toast.makeText(AdminSystemConfigActivity.this, "Failed to load majors.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JsonArray arr = response.body().getAsJsonArray("data");
                List<JsonObject> majorObjects = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    majorObjects.add(o);
                    String name = UiUtils.optString(o, "name", "");
                    String code = UiUtils.optString(o, "code", "");
                    String facName = UiUtils.optString(o, "faculty_name", UiUtils.optString(o, "department_name", ""));
                    String status = UiUtils.optString(o, "status", "active");
                    StringBuilder sb = new StringBuilder(name);
                    if (!code.isEmpty()) sb.append(" (").append(code).append(")");
                    if (!facName.isEmpty()) sb.append(" — ").append(facName);
                    sb.append(" [").append(status).append("]");
                    labels.add(sb.toString());
                }

                if (majorObjects.isEmpty()) {
                    new MaterialAlertDialogBuilder(AdminSystemConfigActivity.this)
                            .setTitle(R.string.manage_majors)
                            .setMessage("No majors found yet.")
                            .setNegativeButton(R.string.close, null)
                            .setPositiveButton(R.string.add_major, (d, w) -> showCreateMajorDialog(null))
                            .show();
                    return;
                }

                new MaterialAlertDialogBuilder(AdminSystemConfigActivity.this)
                        .setTitle(R.string.manage_majors)
                        .setItems(labels.toArray(new String[0]), (d, which) -> showMajorActionDialog(majorObjects.get(which)))
                        .setNegativeButton(R.string.close, null)
                        .setPositiveButton(R.string.add_major, (d, w) -> showCreateMajorDialog(null))
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMajorActionDialog(JsonObject majorObj) {
        int id = UiUtils.optInt(majorObj, "id", 0);
        if (id <= 0) return;
        String name = UiUtils.optString(majorObj, "name", "Major");
        String status = UiUtils.optString(majorObj, "status", "active");
        boolean isActive = "active".equalsIgnoreCase(status);
        String nextStatus = isActive ? "inactive" : "active";

        new MaterialAlertDialogBuilder(this)
                .setTitle(name)
                .setMessage("Status: " + status)
                .setNeutralButton(isActive ? "Deactivate" : "Activate", (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("status", nextStatus);
                    ApiClient.getApiService(this).updateProgram(id, body).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            if (isFinishing() || isDestroyed()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminSystemConfigActivity.this, "Major updated to " + nextStatus, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminSystemConfigActivity.this, extractApiError(response, "Failed to update major."), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                            Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Delete", (d, w) -> ApiClient.getApiService(this).deleteProgram(id).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminSystemConfigActivity.this, "Major deleted.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminSystemConfigActivity.this, extractApiError(response, "Failed to delete major."), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(AdminSystemConfigActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private String extractApiError(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                JsonObject err = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                if (err.has("errors") && err.get("errors").isJsonObject()) {
                    JsonObject errors = err.getAsJsonObject("errors");
                    if (!errors.entrySet().isEmpty()) {
                        return errors.entrySet().iterator().next().getValue().getAsJsonArray().get(0).getAsString();
                    }
                }
                if (err.has("message") && !err.get("message").isJsonNull()) {
                    return err.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return fallback + " (HTTP " + response.code() + ")";
    }
}
