package com.social7.studentlearnanalytics.admin.classes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen Assign Teacher to Class workflow using searchable selection pages.
 */
public class AssignTeacherClassActivity extends AppCompatActivity {

    public static final String EXTRA_EXISTING_KEYS = "existing_assign_keys";

    private TextInputEditText etClass, etCourse, etTeacher, etAy, etSemester;
    private AutoCompleteTextView actvStatus;
    private MaterialButton btnSave, btnCancel;

    private int selectedClassId = -1;
    private int selectedCourseId = -1;
    private int selectedTeacherId = -1;
    private int selectedAyId = -1;
    private int selectedSemesterId = -1;

    private String selectedClassCode = "";
    private String selectedCourseName = "";
    private String selectedTeacherName = "";
    private String selectedTerm = "";
    private String existingKeys = "";

    private final ActivityResultLauncher<Intent> classPicker = registerSelector((id, code, title) -> {
        selectedClassId = parseIntOr(id, -1);
        selectedClassCode = code != null && !code.isEmpty() ? code : title;
        etClass.setText(title);
    });

    private final ActivityResultLauncher<Intent> coursePicker = registerSelector((id, code, title) -> {
        selectedCourseId = parseIntOr(id, -1);
        selectedCourseName = title;
        etCourse.setText(title);
    });

    private final ActivityResultLauncher<Intent> teacherPicker = registerSelector((id, code, title) -> {
        selectedTeacherId = parseIntOr(id, -1);
        selectedTeacherName = title;
        etTeacher.setText(title);
    });

    private final ActivityResultLauncher<Intent> ayPicker = registerSelector((id, code, title) -> {
        selectedAyId = parseIntOr(id, -1);
        selectedTerm = title;
        etAy.setText(title);
    });

    private final ActivityResultLauncher<Intent> semesterPicker = registerSelector((id, code, title) -> {
        selectedSemesterId = parseIntOr(id, -1);
        etSemester.setText(title);
    });

    private interface HandlerCallback {
        void onResult(String id, String code, String title);
    }

    private ActivityResultLauncher<Intent> registerSelector(HandlerCallback cb) {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                cb.onResult(
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID),
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_CODE),
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE)
                );
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        setContentView(R.layout.activity_assign_teacher_class);
        existingKeys = getIntent().getStringExtra(EXTRA_EXISTING_KEYS);
        if (existingKeys == null) existingKeys = "";
        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin");
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnAssignTeacherBack);
        etClass = findViewById(R.id.etAssignClass);
        etCourse = findViewById(R.id.etAssignCourse);
        etTeacher = findViewById(R.id.etAssignTeacher);
        etAy = findViewById(R.id.etAssignAy);
        etSemester = findViewById(R.id.etAssignSemester);
        actvStatus = findViewById(R.id.actvAssignStatus);
        btnSave = findViewById(R.id.btnAssignSave);
        btnCancel = findViewById(R.id.btnAssignCancel);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());

        String[] statuses = {"Assigned", "Primary", "Co-Teacher"};
        actvStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
        actvStatus.setText("Assigned", false);

        etClass.setOnClickListener(v -> classPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_CLASS)));
        etCourse.setOnClickListener(v -> coursePicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_COURSE)));
        etTeacher.setOnClickListener(v -> teacherPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_TEACHER)));
        etAy.setOnClickListener(v -> ayPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_ACADEMIC_YEAR)));
        etSemester.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_SEMESTER);
            if (selectedAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedAyId);
            semesterPicker.launch(i);
        });

        btnSave.setOnClickListener(v -> submitAssignment());
    }

    private void submitAssignment() {
        if (selectedClassId <= 0 || selectedTeacherId <= 0) {
            Toast.makeText(this, R.string.err_assign_teacher_required, Toast.LENGTH_LONG).show();
            return;
        }

        String pairKey = (selectedClassCode + "|" + selectedTeacherName).toLowerCase();
        if (!existingKeys.isEmpty() && existingKeys.toLowerCase().contains(pairKey)) {
            Toast.makeText(this, R.string.err_duplicate_teacher_assign, Toast.LENGTH_LONG).show();
            return;
        }

        btnSave.setEnabled(false);
        ApiService api = ApiClient.getApiService(this);
        JsonObject classBody = new JsonObject();
        JsonArray classIds = new JsonArray();
        classIds.add(selectedClassId);
        classBody.add("class_ids", classIds);
        if (selectedCourseId > 0) classBody.addProperty("course_id", selectedCourseId);
        if (selectedSemesterId > 0) classBody.addProperty("semester_id", selectedSemesterId);
        if (selectedAyId > 0) classBody.addProperty("academic_year_id", selectedAyId);

        api.assignFacultyClasses(selectedTeacherId, classBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful()) {
                    btnSave.setEnabled(true);
                    Toast.makeText(AssignTeacherClassActivity.this, "Failed to assign teacher to class (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                if (selectedCourseId > 0) {
                    JsonObject courseBody = new JsonObject();
                    JsonArray courseIds = new JsonArray();
                    courseIds.add(selectedCourseId);
                    courseBody.add("course_ids", courseIds);
                    api.assignFacultyCourses(selectedTeacherId, courseBody).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> c2, @NonNull Response<JsonObject> r2) {
                            if (isFinishing() || isDestroyed()) return;
                            if (!r2.isSuccessful()) {
                                btnSave.setEnabled(true);
                                Toast.makeText(AssignTeacherClassActivity.this, "Class assigned, but failed to assign course (HTTP " + r2.code() + ")", Toast.LENGTH_LONG).show();
                                return;
                            }
                            finishWithSuccess();
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> c2, @NonNull Throwable t2) {
                            if (isFinishing() || isDestroyed()) return;
                            btnSave.setEnabled(true);
                            Toast.makeText(AssignTeacherClassActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    finishWithSuccess();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AssignTeacherClassActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishWithSuccess() {
        btnSave.setEnabled(true);
        Intent res = new Intent();
        res.putExtra("class_code", selectedClassCode);
        res.putExtra("course_name", selectedCourseName.isEmpty() ? "General Cohort" : selectedCourseName);
        res.putExtra("teacher_name", selectedTeacherName);
        String sem = etSemester.getText() != null ? etSemester.getText().toString().trim() : "";
        String term = (selectedTerm.isEmpty() ? "AY 2025-2026" : selectedTerm) + (sem.isEmpty() ? "" : " • " + sem);
        res.putExtra("department_term", term);
        res.putExtra("status", actvStatus.getText() != null ? actvStatus.getText().toString() : "Assigned");
        setResult(RESULT_OK, res);
        Toast.makeText(this, "Assigned " + selectedTeacherName + " to " + selectedClassCode, Toast.LENGTH_SHORT).show();
        finish();
    }

    private int parseIntOr(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return fallback;
        }
    }
}
