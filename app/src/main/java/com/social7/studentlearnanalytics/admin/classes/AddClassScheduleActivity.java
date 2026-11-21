package com.social7.studentlearnanalytics.admin.classes;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.UiUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen Add / Edit Class Schedule form backed by /api/class-schedules
 * with real backend class and teacher overlap conflict detection.
 */
public class AddClassScheduleActivity extends AppCompatActivity {

    public static final String EXTRA_EXISTING_SCHEDULES_JSON = "existing_schedules_json";
    public static final String EXTRA_PREFILL_CLASS_ID = "prefill_class_id";
    public static final String EXTRA_PREFILL_CLASS_TITLE = "prefill_class_title";
    public static final String EXTRA_PREFILL_CLASS_LOCATION = "prefill_class_location";
    public static final String EXTRA_EDIT_SCHEDULE_JSON = "edit_schedule_json";

    private TextInputEditText etClass, etCourse, etTeacher, etStartTime, etEndTime, etAy, etSemester;
    private TextView tvClassLocation, tvTitle;
    private AutoCompleteTextView actvDay;
    private MaterialButton btnSave;

    private String editScheduleId = null;
    private int selectedClassId = -1;
    private int selectedCourseId = -1;
    private int selectedTeacherId = -1;
    private int selectedAyId = -1;
    private int selectedSemesterId = -1;

    private int startMinutes = 18 * 60;
    private int endMinutes = 21 * 60;
    private boolean hasStart = false;
    private boolean hasEnd = false;

    private final ActivityResultLauncher<Intent> classPicker = registerSelector((id, title, raw) -> {
        selectedClassId = parseIntOr(id, -1);
        etClass.setText(title);
        updateClassLocationBanner(title, raw);
    });

    private final ActivityResultLauncher<Intent> coursePicker = registerSelector((id, title, raw) -> {
        selectedCourseId = parseIntOr(id, -1);
        etCourse.setText(title);
    });

    private final ActivityResultLauncher<Intent> teacherPicker = registerSelector((id, title, raw) -> {
        selectedTeacherId = parseIntOr(id, -1);
        etTeacher.setText(title);
    });

    private final ActivityResultLauncher<Intent> ayPicker = registerSelector((id, title, raw) -> {
        selectedAyId = parseIntOr(id, -1);
        etAy.setText(title);
        selectedSemesterId = -1;
        etSemester.setText("");
    });

    private final ActivityResultLauncher<Intent> semesterPicker = registerSelector((id, title, raw) -> {
        selectedSemesterId = parseIntOr(id, -1);
        etSemester.setText(title);
    });

    private interface SimpleResult {
        void onResult(String id, String title, String rawJson);
    }

    private ActivityResultLauncher<Intent> registerSelector(SimpleResult cb) {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                cb.onResult(
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_ID),
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_TITLE),
                        result.getData().getStringExtra(RecordSelectorActivity.RESULT_RAW_JSON)
                );
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_schedule);
        bindViews();
        applyPrefillOrEdit();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnAddScheduleBack);
        tvTitle = findViewById(R.id.tvAddScheduleTitle);
        etClass = findViewById(R.id.etScheduleClass);
        etCourse = findViewById(R.id.etScheduleCourse);
        etTeacher = findViewById(R.id.etScheduleTeacher);
        tvClassLocation = findViewById(R.id.tvScheduleClassLocation);
        actvDay = findViewById(R.id.actvScheduleDay);
        etStartTime = findViewById(R.id.etScheduleStartTime);
        etEndTime = findViewById(R.id.etScheduleEndTime);
        etAy = findViewById(R.id.etScheduleAy);
        etSemester = findViewById(R.id.etScheduleSemester);
        MaterialButton btnCancel = findViewById(R.id.btnAddScheduleCancel);
        btnSave = findViewById(R.id.btnAddScheduleSave);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        actvDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, days));

        etClass.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_CLASS);
            if (selectedClassId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedClassId));
            classPicker.launch(i);
        });
        etCourse.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_COURSE);
            if (selectedCourseId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedCourseId));
            coursePicker.launch(i);
        });
        etTeacher.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_TEACHER);
            if (selectedTeacherId > 0) i.putExtra(RecordSelectorActivity.EXTRA_SELECTED_ID, String.valueOf(selectedTeacherId));
            teacherPicker.launch(i);
        });
        etAy.setOnClickListener(v -> ayPicker.launch(RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_ACADEMIC_YEAR)));
        etSemester.setOnClickListener(v -> {
            Intent i = RecordSelectorActivity.createIntent(this, RecordSelectorActivity.TYPE_SEMESTER);
            if (selectedAyId > 0) i.putExtra(RecordSelectorActivity.EXTRA_FILTER_PARENT_ID, selectedAyId);
            semesterPicker.launch(i);
        });

        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void applyPrefillOrEdit() {
        String editRaw = getIntent().getStringExtra(EXTRA_EDIT_SCHEDULE_JSON);
        if (editRaw != null && !editRaw.trim().isEmpty()) {
            try {
                JsonObject o = JsonParser.parseString(editRaw).getAsJsonObject();
                editScheduleId = UiUtils.optString(o, "id", "");
                if (editScheduleId.isEmpty()) editScheduleId = null;

                if (tvTitle != null) tvTitle.setText(R.string.edit_schedule_title);
                if (btnSave != null) btnSave.setText(R.string.update_schedule_action);

                selectedClassId = UiUtils.optInt(o, "class_id", -1);
                selectedCourseId = UiUtils.optInt(o, "course_id", -1);
                selectedTeacherId = UiUtils.optInt(o, "teacher_id", -1);
                selectedAyId = UiUtils.optInt(o, "academic_year_id", -1);
                selectedSemesterId = UiUtils.optInt(o, "semester_id", -1);

                String clsTitle = "Class";
                JsonObject clsObj = o.has("class_room") && o.get("class_room").isJsonObject()
                        ? o.getAsJsonObject("class_room")
                        : (o.has("classRoom") && o.get("classRoom").isJsonObject() ? o.getAsJsonObject("classRoom") : null);
                if (clsObj != null) {
                    String n = UiUtils.optString(clsObj, "name", "Class");
                    String c = UiUtils.optString(clsObj, "code", "");
                    clsTitle = c.isEmpty() ? n : (n + " (" + c + ")");
                    etClass.setText(clsTitle);
                    String b = o.has("building") && o.get("building").isJsonObject()
                            ? UiUtils.optString(o.getAsJsonObject("building"), "name", "")
                            : (clsObj.has("building") && clsObj.get("building").isJsonObject()
                                    ? UiUtils.optString(clsObj.getAsJsonObject("building"), "name", "") : "");
                    String f = o.has("floor") && o.get("floor").isJsonObject()
                            ? UiUtils.optString(o.getAsJsonObject("floor"), "name", "")
                            : (clsObj.has("floor") && clsObj.get("floor").isJsonObject()
                                    ? UiUtils.optString(clsObj.getAsJsonObject("floor"), "name", "") : "");
                    if (tvClassLocation != null) {
                        tvClassLocation.setText(!b.isEmpty()
                                ? ("Location: " + b + (f.isEmpty() ? "" : " • " + f) + " • " + n)
                                : ("Physical Class: " + n));
                    }
                }

                if (o.has("course") && o.get("course").isJsonObject()) {
                    JsonObject cObj = o.getAsJsonObject("course");
                    String code = UiUtils.optString(cObj, "course_code", UiUtils.optString(cObj, "code", ""));
                    String name = UiUtils.optString(cObj, "course_name", UiUtils.optString(cObj, "name", ""));
                    etCourse.setText(code.isEmpty() ? name : (code + " — " + name));
                }

                if (o.has("teacher") && o.get("teacher").isJsonObject()) {
                    JsonObject tObj = o.getAsJsonObject("teacher");
                    String tName = (UiUtils.optString(tObj, "first_name", "") + " " + UiUtils.optString(tObj, "last_name", "")).trim();
                    if (tName.isEmpty() && tObj.has("user") && tObj.get("user").isJsonObject()) {
                        tName = UiUtils.optString(tObj.getAsJsonObject("user"), "name", "");
                    }
                    etTeacher.setText(tName);
                }

                if (o.has("academic_year") && o.get("academic_year").isJsonObject()) {
                    etAy.setText(UiUtils.optString(o.getAsJsonObject("academic_year"), "name", ""));
                }
                if (o.has("semester") && o.get("semester").isJsonObject()) {
                    etSemester.setText(UiUtils.optString(o.getAsJsonObject("semester"), "name", ""));
                }

                String day = UiUtils.optString(o, "day_of_week", "");
                if (!day.isEmpty() && actvDay != null) actvDay.setText(day, false);

                String sRaw = UiUtils.optString(o, "start_time", "");
                String eRaw = UiUtils.optString(o, "end_time", "");
                int sMin = parseMinutesFromTimeString(sRaw);
                int eMin = parseMinutesFromTimeString(eRaw);
                if (sMin >= 0) {
                    startMinutes = sMin;
                    hasStart = true;
                    etStartTime.setText(formatTimeDisplay(sMin / 60, sMin % 60));
                }
                if (eMin >= 0) {
                    endMinutes = eMin;
                    hasEnd = true;
                    etEndTime.setText(formatTimeDisplay(eMin / 60, eMin % 60));
                }
                return;
            } catch (Exception ignored) {
            }
        }

        int prefillId = getIntent().getIntExtra(EXTRA_PREFILL_CLASS_ID, -1);
        String prefillTitle = getIntent().getStringExtra(EXTRA_PREFILL_CLASS_TITLE);
        String prefillLoc = getIntent().getStringExtra(EXTRA_PREFILL_CLASS_LOCATION);
        if (prefillId > 0) {
            selectedClassId = prefillId;
            if (prefillTitle != null && etClass != null) etClass.setText(prefillTitle);
            if (tvClassLocation != null && prefillLoc != null && !prefillLoc.isEmpty()) {
                tvClassLocation.setText("Location: " + prefillLoc);
            }
        }
    }

    private void updateClassLocationBanner(String title, String raw) {
        if (tvClassLocation == null) return;
        String loc = "Location: Physical Class — " + title;
        if (raw != null && !raw.isEmpty()) {
            try {
                JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                String b = UiUtils.optString(obj, "building_name", "");
                String f = UiUtils.optString(obj, "floor_name", "");
                String n = UiUtils.optString(obj, "name", title);
                if (!b.isEmpty()) {
                    loc = "Location: " + b + (f.isEmpty() ? "" : " • " + f) + " • " + n;
                } else {
                    loc = "Physical Class: " + n;
                }
            } catch (Exception ignored) {
            }
        }
        tvClassLocation.setText(loc);
    }

    private void showTimePicker(boolean isStart) {
        int hour = (isStart ? startMinutes : endMinutes) / 60;
        int min = (isStart ? startMinutes : endMinutes) % 60;
        new TimePickerDialog(this, (view, h, m) -> {
            int total = h * 60 + m;
            String formatted = formatTimeDisplay(h, m);
            if (isStart) {
                startMinutes = total;
                hasStart = true;
                etStartTime.setText(formatted);
            } else {
                endMinutes = total;
                hasEnd = true;
                etEndTime.setText(formatted);
            }
        }, hour, min, false).show();
    }

    private String formatTimeDisplay(int h24, int m) {
        String amPm = h24 >= 12 ? "PM" : "AM";
        int h12 = h24 % 12;
        if (h12 == 0) h12 = 12;
        return String.format(Locale.US, "%02d:%02d %s", h12, m, amPm);
    }

    private String formatTime24(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.US, "%02d:%02d", h, m);
    }

    private int parseMinutesFromTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return -1;
        String[] parts = timeStr.trim().split(":");
        if (parts.length >= 2) {
            try {
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                return h * 60 + m;
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    private void validateAndSave() {
        String day = actvDay.getText() != null ? actvDay.getText().toString().trim() : "";

        if (selectedClassId <= 0 || selectedCourseId <= 0 || day.isEmpty() || !hasStart || !hasEnd) {
            Toast.makeText(this, R.string.err_schedule_required, Toast.LENGTH_LONG).show();
            return;
        }

        if (startMinutes >= endMinutes) {
            Toast.makeText(this, R.string.err_schedule_time_order, Toast.LENGTH_LONG).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("class_id", selectedClassId);
        body.addProperty("course_id", selectedCourseId);
        if (selectedTeacherId > 0) body.addProperty("teacher_id", selectedTeacherId);
        if (selectedAyId > 0) body.addProperty("academic_year_id", selectedAyId);
        if (selectedSemesterId > 0) body.addProperty("semester_id", selectedSemesterId);
        body.addProperty("day_of_week", day);
        body.addProperty("start_time", formatTime24(startMinutes));
        body.addProperty("end_time", formatTime24(endMinutes));
        body.addProperty("status", "active");

        btnSave.setEnabled(false);
        Call<JsonObject> call = editScheduleId == null
                ? ApiClient.getApiService(this).createClassSchedule(body)
                : ApiClient.getApiService(this).updateClassSchedule(editScheduleId, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddClassScheduleActivity.this,
                            editScheduleId == null ? "Schedule created successfully!" : "Schedule updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errMsg = getString(R.string.err_schedule_conflict);
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message") && !err.get("message").isJsonNull()) {
                                errMsg = err.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    Toast.makeText(AddClassScheduleActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddClassScheduleActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
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
