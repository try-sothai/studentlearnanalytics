package com.social7.studentlearnanalytics.admin.courses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.classes.RecordSelectorActivity;
import com.social7.studentlearnanalytics.model.Course;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCourseActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_COURSE = "extra_edit_course";

    private TextInputLayout tilAddCourseCode, tilAddCourseName, tilAddCourseCredits, tilAddCourseDept;
    private EditText etAddCourseCode, etAddCourseName, etAddCourseCredits;
    private EditText etAddCourseDept, etAddCourseProgram, etAddCourseAy, etAddCourseSemester, etAddCourseTeacher, etAddCourseDescription;
    private AutoCompleteTextView actvAddCourseStatus;
    private MaterialButton btnSaveCourse;
    private TextView tvHeaderTitle;

    private Course editingCourse = null;
    private int selectedDeptId = -1;
    private int selectedProgramId = -1;
    private int selectedAyId = -1;
    private int selectedSemesterId = -1;
    private int selectedTeacherId = -1;

    private interface PickerResultHandler {
        void onSelected(int id, String title);
    }

    private ActivityResultLauncher<Intent> registerPicker(PickerResultHandler handler) {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String idStr = result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID);
                String title = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                int id = parseIntOr(idStr, -1);
                handler.onSelected(id, title != null ? title : "");
            }
        });
    }

    private final ActivityResultLauncher<Intent> deptPicker = registerPicker((id, title) -> {
        selectedDeptId = id;
        if (etAddCourseDept != null) etAddCourseDept.setText(title);
        if (tilAddCourseDept != null) tilAddCourseDept.setError(null);
        selectedProgramId = -1;
        if (etAddCourseProgram != null) etAddCourseProgram.setText("");
    });

    private final ActivityResultLauncher<Intent> programPicker = registerPicker((id, title) -> {
        selectedProgramId = id;
        if (etAddCourseProgram != null) etAddCourseProgram.setText(title);
    });

    private final ActivityResultLauncher<Intent> ayPicker = registerPicker((id, title) -> {
        selectedAyId = id;
        if (etAddCourseAy != null) etAddCourseAy.setText(title);
        selectedSemesterId = -1;
        if (etAddCourseSemester != null) etAddCourseSemester.setText("");
    });

    private final ActivityResultLauncher<Intent> semesterPicker = registerPicker((id, title) -> {
        selectedSemesterId = id;
        if (etAddCourseSemester != null) etAddCourseSemester.setText(title);
    });

    private final ActivityResultLauncher<Intent> teacherPicker = registerPicker((id, title) -> {
        selectedTeacherId = id;
        if (etAddCourseTeacher != null) etAddCourseTeacher.setText(title);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_course);

        View btnBack = findViewById(R.id.btnBackAddCourse);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvHeaderTitle = findViewById(R.id.tvAddCourseHeaderTitle);
        tilAddCourseCode = findViewById(R.id.tilAddCourseCode);
        tilAddCourseName = findViewById(R.id.tilAddCourseName);
        tilAddCourseCredits = findViewById(R.id.tilAddCourseCredits);
        tilAddCourseDept = findViewById(R.id.tilAddCourseDept);

        etAddCourseCode = findViewById(R.id.etAddCourseCode);
        etAddCourseName = findViewById(R.id.etAddCourseName);
        etAddCourseCredits = findViewById(R.id.etAddCourseCredits);
        etAddCourseDept = findViewById(R.id.etAddCourseDept);
        etAddCourseProgram = findViewById(R.id.etAddCourseProgram);
        etAddCourseAy = findViewById(R.id.etAddCourseAy);
        etAddCourseSemester = findViewById(R.id.etAddCourseSemester);
        etAddCourseTeacher = findViewById(R.id.etAddCourseTeacher);
        etAddCourseDescription = findViewById(R.id.etAddCourseDescription);
        actvAddCourseStatus = findViewById(R.id.actvAddCourseStatus);
        btnSaveCourse = findViewById(R.id.btnSaveCourse);

        if (actvAddCourseStatus != null) {
            String[] statuses = {"Active", "Inactive"};
            actvAddCourseStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
            actvAddCourseStatus.setText("Active", false);
        }

        setupSearchablePickers();

        if (getIntent().hasExtra(EXTRA_EDIT_COURSE)) {
            Object extra = getIntent().getSerializableExtra(EXTRA_EDIT_COURSE);
            if (extra instanceof Course) {
                editingCourse = (Course) extra;
                populateEditingCourse(editingCourse);
            }
        }

        if (btnSaveCourse != null) {
            btnSaveCourse.setOnClickListener(v -> attemptSaveCourse());
        }
    }

    private void setupSearchablePickers() {
        View.OnClickListener openDept = v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_DEPARTMENT);
            if (selectedDeptId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedDeptId));
            deptPicker.launch(i);
        };
        if (etAddCourseDept != null) etAddCourseDept.setOnClickListener(openDept);
        if (tilAddCourseDept != null) tilAddCourseDept.setEndIconOnClickListener(openDept);

        View.OnClickListener openProgram = v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_PROGRAM);
            if (selectedDeptId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedDeptId);
            if (selectedProgramId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedProgramId));
            programPicker.launch(i);
        };
        if (etAddCourseProgram != null) etAddCourseProgram.setOnClickListener(openProgram);
        TextInputLayout tilProgram = findViewById(R.id.tilAddCourseProgram);
        if (tilProgram != null) tilProgram.setEndIconOnClickListener(openProgram);

        View.OnClickListener openAy = v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_ACADEMIC_YEAR);
            if (selectedAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedAyId));
            ayPicker.launch(i);
        };
        if (etAddCourseAy != null) etAddCourseAy.setOnClickListener(openAy);
        TextInputLayout tilAy = findViewById(R.id.tilAddCourseAy);
        if (tilAy != null) tilAy.setEndIconOnClickListener(openAy);

        View.OnClickListener openSem = v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_SEMESTER);
            if (selectedAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedAyId);
            if (selectedSemesterId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedSemesterId));
            semesterPicker.launch(i);
        };
        if (etAddCourseSemester != null) etAddCourseSemester.setOnClickListener(openSem);
        TextInputLayout tilSem = findViewById(R.id.tilAddCourseSemester);
        if (tilSem != null) tilSem.setEndIconOnClickListener(openSem);

        View.OnClickListener openTeacher = v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_TEACHER);
            if (selectedTeacherId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedTeacherId));
            teacherPicker.launch(i);
        };
        if (etAddCourseTeacher != null) etAddCourseTeacher.setOnClickListener(openTeacher);
        TextInputLayout tilTeacher = findViewById(R.id.tilAddCourseTeacher);
        if (tilTeacher != null) tilTeacher.setEndIconOnClickListener(openTeacher);
    }

    private void populateEditingCourse(Course course) {
        if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.edit_course);
        if (btnSaveCourse != null) btnSaveCourse.setText(R.string.save_course);

        if (etAddCourseCode != null) etAddCourseCode.setText(course.getCode());
        if (etAddCourseName != null) etAddCourseName.setText(course.getName());
        if (etAddCourseCredits != null) {
            double ch = course.getCreditHours();
            if (ch == Math.floor(ch)) {
                etAddCourseCredits.setText(String.valueOf((int) ch));
            } else {
                etAddCourseCredits.setText(String.valueOf(ch));
            }
        }

        selectedDeptId = course.getDepartmentId();
        if (etAddCourseDept != null) etAddCourseDept.setText(course.getDepartment());

        selectedProgramId = course.getProgramId();
        if (etAddCourseProgram != null) etAddCourseProgram.setText(course.getProgramName());

        selectedAyId = course.getAcademicYearId();
        if (etAddCourseAy != null) etAddCourseAy.setText(course.getAcademicYear());

        selectedSemesterId = course.getSemesterId();
        if (etAddCourseSemester != null) etAddCourseSemester.setText(course.getSemester());

        selectedTeacherId = course.getTeacherId();
        if (etAddCourseTeacher != null) etAddCourseTeacher.setText(course.getTeacher());

        if (etAddCourseDescription != null) etAddCourseDescription.setText(course.getDescription());

        if (actvAddCourseStatus != null) {
            String status = "Inactive".equalsIgnoreCase(course.getStatus()) ? "Inactive" : "Active";
            actvAddCourseStatus.setText(status, false);
        }
    }

    private void attemptSaveCourse() {
        if (etAddCourseCode == null || etAddCourseName == null || etAddCourseCredits == null) return;

        String code = etAddCourseCode.getText().toString().trim().toUpperCase(Locale.ROOT);
        String name = etAddCourseName.getText().toString().trim();
        String creditsStr = etAddCourseCredits.getText().toString().trim();
        String deptText = etAddCourseDept != null && etAddCourseDept.getText() != null
                ? etAddCourseDept.getText().toString().trim() : "";

        boolean isValid = true;

        if (code.isEmpty()) {
            if (tilAddCourseCode != null) tilAddCourseCode.setError("Course code is required");
            isValid = false;
        } else {
            if (tilAddCourseCode != null) tilAddCourseCode.setError(null);
        }

        if (name.isEmpty()) {
            if (tilAddCourseName != null) tilAddCourseName.setError("Course name is required");
            isValid = false;
        } else {
            if (tilAddCourseName != null) tilAddCourseName.setError(null);
        }

        double creditsVal = 3.0;
        if (creditsStr.isEmpty()) {
            if (tilAddCourseCredits != null) tilAddCourseCredits.setError("Credits is required");
            isValid = false;
        } else {
            try {
                creditsVal = Double.parseDouble(creditsStr);
                if (creditsVal < 0.5 || creditsVal > 10.0) {
                    if (tilAddCourseCredits != null) tilAddCourseCredits.setError("Credits must be between 0.5 and 10");
                    isValid = false;
                } else if (tilAddCourseCredits != null) {
                    tilAddCourseCredits.setError(null);
                }
            } catch (NumberFormatException e) {
                if (tilAddCourseCredits != null) tilAddCourseCredits.setError("Invalid credit value");
                isValid = false;
            }
        }

        if (selectedDeptId <= 0 && deptText.isEmpty()) {
            if (tilAddCourseDept != null) tilAddCourseDept.setError("Please select a Department");
            isValid = false;
        } else {
            if (tilAddCourseDept != null) tilAddCourseDept.setError(null);
        }

        if (!isValid) return;

        JsonObject body = new JsonObject();
        body.addProperty("course_code", code);
        body.addProperty("code", code);
        body.addProperty("course_name", name);
        body.addProperty("name", name);
        body.addProperty("credit", creditsVal);
        body.addProperty("credits", (int) Math.round(creditsVal));

        if (selectedDeptId > 0) {
            body.addProperty("department_id", selectedDeptId);
        }
        if (!deptText.isEmpty()) {
            body.addProperty("department", deptText);
        }
        if (selectedProgramId > 0) {
            body.addProperty("program_id", selectedProgramId);
        }
        if (selectedTeacherId > 0) {
            body.addProperty("teacher_id", selectedTeacherId);
        }
        if (selectedAyId > 0) {
            body.addProperty("academic_year_id", selectedAyId);
        }
        if (etAddCourseAy != null && etAddCourseAy.getText() != null && !etAddCourseAy.getText().toString().trim().isEmpty()) {
            body.addProperty("academic_year", etAddCourseAy.getText().toString().trim());
        }
        if (selectedSemesterId > 0) {
            body.addProperty("semester_id", selectedSemesterId);
        }
        if (etAddCourseSemester != null && etAddCourseSemester.getText() != null && !etAddCourseSemester.getText().toString().trim().isEmpty()) {
            body.addProperty("semester", etAddCourseSemester.getText().toString().trim());
        }
        if (etAddCourseDescription != null && etAddCourseDescription.getText() != null) {
            body.addProperty("description", etAddCourseDescription.getText().toString().trim());
        }

        String statusVal = "active";
        if (actvAddCourseStatus != null && actvAddCourseStatus.getText() != null) {
            if ("Inactive".equalsIgnoreCase(actvAddCourseStatus.getText().toString().trim())) {
                statusVal = "inactive";
            }
        }
        body.addProperty("status", statusVal);

        if (btnSaveCourse != null) btnSaveCourse.setEnabled(false);

        Call<JsonObject> call;
        boolean isEdit = editingCourse != null && !editingCourse.getId().isEmpty();
        if (isEdit) {
            call = ApiClient.getApiService(this).updateCourse(editingCourse.getId(), body);
        } else {
            call = ApiClient.getApiService(this).createCourse(body);
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (btnSaveCourse != null) btnSaveCourse.setEnabled(true);
                if (response.isSuccessful()) {
                    String actionMsg = isEdit
                            ? ("Course " + code + " updated successfully!")
                            : (getString(R.string.course_08702b) + code + " - " + name + " added successfully!");
                    Toast.makeText(AddCourseActivity.this, actionMsg, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (btnSaveCourse != null) btnSaveCourse.setEnabled(true);
                Toast.makeText(AddCourseActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleApiError(Response<JsonObject> response) {
        String errorMessage = "Failed to save course (HTTP " + response.code() + ")";
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                JsonObject errObj = JsonParser.parseString(raw).getAsJsonObject();
                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                    errorMessage = errObj.get("message").getAsString();
                }
                if (errObj.has("errors") && errObj.get("errors").isJsonObject()) {
                    JsonObject errors = errObj.getAsJsonObject("errors");
                    if (errors.has("course_code") && tilAddCourseCode != null) {
                        String codeErr = errors.getAsJsonArray("course_code").get(0).getAsString();
                        tilAddCourseCode.setError(codeErr);
                        errorMessage = codeErr;
                    }
                    for (Map.Entry<String, JsonElement> entry : errors.entrySet()) {
                        if (entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().size() > 0) {
                            errorMessage = entry.getValue().getAsJsonArray().get(0).getAsString();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private int parseIntOr(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoleGuard.checkAccess(this, "Admin");
    }
}