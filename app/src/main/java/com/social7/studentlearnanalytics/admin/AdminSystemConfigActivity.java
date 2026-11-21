package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * System Configuration & Academic Structure Setup (Academic Years, Semesters, Departments).
 */
public class AdminSystemConfigActivity extends AppCompatActivity {

    private Spinner spDefaultYear, spDefaultSemester;
    private MaterialButton btnSaveSysConfig, btnAddAcademicYear, btnAddSemester, btnAddDepartment;

    private final List<Integer> yearIds = new ArrayList<>();
    private final List<String> yearNames = new ArrayList<>();
    private final List<String> semesterNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_config_settings);

        ImageView btnBack = findViewById(R.id.btnBackSystemConfig);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spDefaultYear = findViewById(R.id.spDefaultYear);
        spDefaultSemester = findViewById(R.id.spDefaultSemester);
        btnSaveSysConfig = findViewById(R.id.btnSaveSysConfig);
        btnAddAcademicYear = findViewById(R.id.btnAddAcademicYear);
        btnAddSemester = findViewById(R.id.btnAddSemester);
        btnAddDepartment = findViewById(R.id.btnAddDepartment);

        loadAcademicYears();
        loadSemesters();

        if (btnAddAcademicYear != null) {
            btnAddAcademicYear.setOnClickListener(v -> showCreateAcademicYearDialog());
        }
        if (btnAddSemester != null) {
            btnAddSemester.setOnClickListener(v -> showCreateSemesterDialog());
        }
        if (btnAddDepartment != null) {
            btnAddDepartment.setOnClickListener(v -> showCreateDepartmentDialog());
        }
        if (btnSaveSysConfig != null) {
            btnSaveSysConfig.setOnClickListener(v -> {
                Toast.makeText(this, R.string.system_configuration_saved_successfully, Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void loadAcademicYears() {
        ApiClient.getApiService(this).getAcademicYears().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                yearIds.clear();
                yearNames.clear();
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        yearIds.add(UiUtils.optInt(o, "id", 0));
                        String name = UiUtils.optString(o, "name", "Academic Year");
                        String st = UiUtils.optString(o, "status", "");
                        yearNames.add(st.isEmpty() ? name : (name + " (" + st + ")"));
                    }
                }
                if (yearNames.isEmpty()) yearNames.add("No academic years yet");
                if (spDefaultYear != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminSystemConfigActivity.this, android.R.layout.simple_spinner_item, yearNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDefaultYear.setAdapter(adapter);
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
                semesterNames.clear();
                if (response.isSuccessful() && response.body() != null && response.body().has("data")) {
                    JsonArray arr = response.body().getAsJsonArray("data");
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        String name = UiUtils.optString(o, "name", "Semester");
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
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
        TextInputEditText etName = addInput(layout, "Department Name * (e.g. Cybersecurity)", "");
        TextInputEditText etCode = addInput(layout, "Department Code * (e.g. CSEC)", "");
        TextInputEditText etDesc = addInput(layout, "Description (optional)", "");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Department")
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
                                Toast.makeText(AdminSystemConfigActivity.this, "Department created!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminSystemConfigActivity.this, "Duplicate or invalid Department.", Toast.LENGTH_LONG).show();
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
}
