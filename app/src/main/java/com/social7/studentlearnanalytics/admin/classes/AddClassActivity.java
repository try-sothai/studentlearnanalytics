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
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.ClassModel;
import com.social7.studentlearnanalytics.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen Add / Edit Class form linked to Department, Academic Year,
 * Building, and Floor (Building -> Floor -> Class).
 */
public class AddClassActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_CLASS = "extra_edit_class";

    private TextInputEditText etName, etCode, etDept, etProgram, etAy, etSemester, etCourse, etTeacher;
    private TextInputEditText etBuilding, etFloor, etCapacity;
    private AutoCompleteTextView actvYearLevel, actvStatus;
    private MaterialButton btnSave, btnCancel;

    private int selectedDeptId = -1;
    private int selectedProgramId = -1;
    private int selectedAyId = -1;
    private int selectedSemesterId = -1;
    private int selectedBuildingId = -1;
    private int selectedFloorId = -1;
    private int selectedCourseId = -1;
    private int selectedTeacherId = -1;
    private String editClassId = null;

    private final ActivityResultLauncher<Intent> deptPicker = registerSelector(RecordSelectorActivity.TYPE_DEPARTMENT, (id, title) -> {
        selectedDeptId = parseIntOr(id, -1);
        etDept.setText(title);
    });

    private final ActivityResultLauncher<Intent> programPicker = registerSelector(RecordSelectorActivity.TYPE_PROGRAM, (id, title) -> {
        selectedProgramId = parseIntOr(id, -1);
        etProgram.setText(title);
    });

    private final ActivityResultLauncher<Intent> ayPicker = registerSelector(RecordSelectorActivity.TYPE_ACADEMIC_YEAR, (id, title) -> {
        selectedAyId = parseIntOr(id, -1);
        etAy.setText(title);
    });

    private final ActivityResultLauncher<Intent> semesterPicker = registerSelector(RecordSelectorActivity.TYPE_SEMESTER, (id, title) -> {
        selectedSemesterId = parseIntOr(id, -1);
        etSemester.setText(title);
    });

    private final ActivityResultLauncher<Intent> buildingPicker = registerSelector(RecordSelectorActivity.TYPE_BUILDING, (id, title) -> {
        int newBId = parseIntOr(id, -1);
        if (newBId != selectedBuildingId) {
            selectedFloorId = -1;
            if (etFloor != null) etFloor.setText("");
        }
        selectedBuildingId = newBId;
        if (etBuilding != null) etBuilding.setText(title);
    });

    private final ActivityResultLauncher<Intent> floorPicker = registerSelector(RecordSelectorActivity.TYPE_FLOOR, (id, title) -> {
        selectedFloorId = parseIntOr(id, -1);
        if (etFloor != null) etFloor.setText(title);
    });

    private final ActivityResultLauncher<Intent> coursePicker = registerSelector(RecordSelectorActivity.TYPE_COURSE, (id, title) -> {
        selectedCourseId = parseIntOr(id, -1);
        etCourse.setText(title);
    });

    private final ActivityResultLauncher<Intent> teacherPicker = registerSelector(RecordSelectorActivity.TYPE_TEACHER, (id, title) -> {
        selectedTeacherId = parseIntOr(id, -1);
        etTeacher.setText(title);
    });

    private interface SelectionHandler {
        void onSelected(String id, String title);
    }

    private ActivityResultLauncher<Intent> registerSelector(String type, SelectionHandler handler) {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String id = result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID);
                String title = result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE);
                handler.onSelected(id, title != null ? title : "");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        bindViews();
        populateEditingClass();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnAddClassBack);
        etName = findViewById(R.id.etClassName);
        etCode = findViewById(R.id.etClassCode);
        etDept = findViewById(R.id.etClassDepartment);
        etProgram = findViewById(R.id.etClassProgram);
        etAy = findViewById(R.id.etClassAcademicYear);
        etSemester = findViewById(R.id.etClassSemester);
        etBuilding = findViewById(R.id.etClassBuilding);
        etFloor = findViewById(R.id.etClassFloor);
        etCapacity = findViewById(R.id.etClassCapacity);
        etCourse = findViewById(R.id.etClassCourse);
        etTeacher = findViewById(R.id.etClassTeacher);
        actvYearLevel = findViewById(R.id.actvClassYearLevel);
        actvStatus = findViewById(R.id.actvClassStatus);
        btnSave = findViewById(R.id.btnAddClassSave);
        btnCancel = findViewById(R.id.btnAddClassCancel);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());

        String[] years = {"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"};
        actvYearLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
        actvYearLevel.setText("Year 1", false);

        String[] statuses = {"Active", "Inactive"};
        actvStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
        actvStatus.setText("Active", false);

        etDept.setOnClickListener(v -> deptPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_DEPARTMENT)));
        etProgram.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_PROGRAM);
            if (selectedDeptId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedDeptId);
            programPicker.launch(i);
        });
        etAy.setOnClickListener(v -> ayPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_ACADEMIC_YEAR)));
        etSemester.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_SEMESTER);
            if (selectedAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedAyId);
            semesterPicker.launch(i);
        });
        if (etBuilding != null) {
            etBuilding.setOnClickListener(v -> buildingPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_BUILDING)));
        }
        if (etFloor != null) {
            etFloor.setOnClickListener(v -> {
                if (selectedBuildingId <= 0) {
                    Toast.makeText(this, "Please select a Building first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_FLOOR);
                i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedBuildingId);
                floorPicker.launch(i);
            });
        }
        etCourse.setOnClickListener(v -> coursePicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_COURSE)));
        etTeacher.setOnClickListener(v -> teacherPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_TEACHER)));

        btnSave.setOnClickListener(v -> saveClass());
    }

    private void populateEditingClass() {
        Object extra = getIntent().getSerializableExtra(EXTRA_EDIT_CLASS);
        if (!(extra instanceof ClassModel)) return;
        ClassModel cm = (ClassModel) extra;
        editClassId = cm.getId();
        if (etName != null) etName.setText(cm.getName());
        if (etCode != null) etCode.setText(cm.getCode());
        if (cm.getDepartmentId() > 0) {
            selectedDeptId = cm.getDepartmentId();
            if (etDept != null) etDept.setText(cm.getDepartment());
        }
        if (cm.getAcademicYearId() > 0) {
            selectedAyId = cm.getAcademicYearId();
            if (etAy != null) etAy.setText(cm.getAcademicYear());
        }
        if (cm.getBuildingId() > 0) {
            selectedBuildingId = cm.getBuildingId();
            if (etBuilding != null) etBuilding.setText(cm.getBuildingName());
        }
        if (cm.getFloorId() > 0) {
            selectedFloorId = cm.getFloorId();
            if (etFloor != null) etFloor.setText(cm.getFloorName());
        }
        if (etCapacity != null && cm.getCapacity() > 0) {
            etCapacity.setText(String.valueOf(cm.getCapacity()));
        }
        if (actvYearLevel != null && cm.getYearLevel() > 0) {
            actvYearLevel.setText("Year " + cm.getYearLevel(), false);
        }
        if (actvStatus != null && cm.getStatus() != null) {
            actvStatus.setText("inactive".equalsIgnoreCase(cm.getStatus()) ? "Inactive" : "Active", false);
        }
        if (btnSave != null) btnSave.setText("Update Class");
    }

    private void saveClass() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        if (name.isEmpty() || code.isEmpty() || selectedDeptId <= 0 || selectedAyId <= 0) {
            Toast.makeText(this, R.string.err_class_required, Toast.LENGTH_LONG).show();
            return;
        }

        int yearLevel = 1;
        String yearStr = actvYearLevel.getText() != null ? actvYearLevel.getText().toString().replaceAll("[^0-9]", "") : "1";
        if (!yearStr.isEmpty()) yearLevel = parseIntOr(yearStr, 1);

        int capacity = parseIntOr(etCapacity != null && etCapacity.getText() != null ? etCapacity.getText().toString().trim() : "40", 40);
        if (capacity <= 0) capacity = 40;

        String status = actvStatus.getText() != null && "Inactive".equalsIgnoreCase(actvStatus.getText().toString())
                ? "inactive" : "active";

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("code", code.toUpperCase());
        body.addProperty("department_id", selectedDeptId);
        body.addProperty("academic_year_id", selectedAyId);
        body.addProperty("year_level", yearLevel);
        body.addProperty("capacity", capacity);
        body.addProperty("status", status);
        if (selectedBuildingId > 0) body.addProperty("building_id", selectedBuildingId);
        if (selectedFloorId > 0) body.addProperty("floor_id", selectedFloorId);
        if (selectedCourseId > 0) body.addProperty("course_id", selectedCourseId);
        if (selectedTeacherId > 0) body.addProperty("teacher_id", selectedTeacherId);

        btnSave.setEnabled(false);
        Call<JsonObject> call = editClassId == null
                ? ApiClient.getApiService(this).createClass(body)
                : ApiClient.getApiService(this).updateClass(editClassId, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddClassActivity.this,
                            editClassId == null ? ("Class " + name + " created!") : ("Class " + name + " updated!"),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddClassActivity.this, R.string.err_class_required, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddClassActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int parseIntOr(String val, int fallback) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return fallback;
        }
    }
}
