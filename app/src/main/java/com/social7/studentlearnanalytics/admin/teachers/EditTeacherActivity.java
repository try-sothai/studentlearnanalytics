package com.social7.studentlearnanalytics.admin.teachers;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Teacher;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTeacherActivity extends AppCompatActivity {

    private static final String TAG = "EditTeacherActivity";
    private Teacher currentTeacher;

    private TextView tvEditTeacherAvatarChar, tvEditTeacherHeaderName, tvEditTeacherHeaderId, tvEditTeacherHeaderStatus;
    private TextInputLayout tilEditTeacherKhmerName, tilEditTeacherEnglishName, tilEditTeacherEmail, tilEditTeacherPhone, tilEditTeacherDob, tilEditTeacherHireDate, tilEditTeacherOffice, tilEditTeacherBio;
    private EditText etEditTeacherKhmerName, etEditTeacherEnglishName, etEditTeacherId, etEditTeacherEmail, etEditTeacherPhone, etEditTeacherDob, etEditTeacherHireDate, etEditTeacherOffice, etEditTeacherBio;

    private Spinner spEditTeacherDept, spEditTeacherPosition, spEditTeacherWorkType;
    private RadioGroup rgEditTeacherAccountStatus;
    private RadioButton rbEditTeacherActive, rbEditTeacherInactive;
    private ChipGroup chipGroupEditTeacherSpecializations;
    private TextView tvEditTeacherSpecEmptyHint;
    private MaterialButton btnSelectEditTeacherSpecializations, btnUpdateTeacher, btnCancelEditTeacher;
    private TeacherProfileFormHelper profileFormHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_edit_teacher);

        if (getIntent() != null && getIntent().hasExtra("teacher")) {
            currentTeacher = (Teacher) getIntent().getSerializableExtra("teacher");
        }

        if (currentTeacher == null) {
            currentTeacher = new Teacher("TCH001", "Mr. Chan", "chan@univ.edu", "Computer Science", "Lecturer", "Active");
        }

        initViews();
        setupSpinners();
        profileFormHelper = new TeacherProfileFormHelper(
                this, etEditTeacherOffice, chipGroupEditTeacherSpecializations, tvEditTeacherSpecEmptyHint);
        profileFormHelper.loadLookups(spEditTeacherDept, currentTeacher.getDepartment());
        prepopulateFields();
        fetchLatestTeacherDetails();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoleGuard.checkAccess(this, "Admin");
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackEditTeacher);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEditTeacherAvatarChar = findViewById(R.id.tvEditTeacherAvatarChar);
        tvEditTeacherHeaderName = findViewById(R.id.tvEditTeacherHeaderName);
        tvEditTeacherHeaderId = findViewById(R.id.tvEditTeacherHeaderId);
        tvEditTeacherHeaderStatus = findViewById(R.id.tvEditTeacherHeaderStatus);

        tilEditTeacherKhmerName = findViewById(R.id.tilEditTeacherKhmerName);
        tilEditTeacherEnglishName = findViewById(R.id.tilEditTeacherEnglishName);
        tilEditTeacherEmail = findViewById(R.id.tilEditTeacherEmail);
        tilEditTeacherPhone = findViewById(R.id.tilEditTeacherPhone);
        tilEditTeacherDob = findViewById(R.id.tilEditTeacherDob);
        tilEditTeacherHireDate = findViewById(R.id.tilEditTeacherHireDate);
        tilEditTeacherOffice = findViewById(R.id.tilEditTeacherOffice);
        tilEditTeacherBio = findViewById(R.id.tilEditTeacherBio);

        etEditTeacherKhmerName = findViewById(R.id.etEditTeacherKhmerName);
        etEditTeacherEnglishName = findViewById(R.id.etEditTeacherEnglishName);
        etEditTeacherId = findViewById(R.id.etEditTeacherId);
        etEditTeacherEmail = findViewById(R.id.etEditTeacherEmail);
        etEditTeacherPhone = findViewById(R.id.etEditTeacherPhone);
        etEditTeacherDob = findViewById(R.id.etEditTeacherDob);
        etEditTeacherHireDate = findViewById(R.id.etEditTeacherHireDate);
        etEditTeacherOffice = findViewById(R.id.etEditTeacherOffice);
        etEditTeacherBio = findViewById(R.id.etEditTeacherBio);
        DatePickerHelper.attachDatePicker(tilEditTeacherDob, etEditTeacherDob, true);
        DatePickerHelper.attachDatePicker(tilEditTeacherHireDate, etEditTeacherHireDate, true);

        spEditTeacherDept = findViewById(R.id.spEditTeacherDept);
        spEditTeacherPosition = findViewById(R.id.spEditTeacherPosition);
        spEditTeacherWorkType = findViewById(R.id.spEditTeacherWorkType);

        rgEditTeacherAccountStatus = findViewById(R.id.rgEditTeacherAccountStatus);
        rbEditTeacherActive = findViewById(R.id.rbEditTeacherActive);
        rbEditTeacherInactive = findViewById(R.id.rbEditTeacherInactive);

        chipGroupEditTeacherSpecializations = findViewById(R.id.chipGroupEditTeacherSpecializations);
        tvEditTeacherSpecEmptyHint = findViewById(R.id.tvEditTeacherSpecEmptyHint);
        btnSelectEditTeacherSpecializations = findViewById(R.id.btnSelectEditTeacherSpecializations);

        btnUpdateTeacher = findViewById(R.id.btnUpdateTeacher);
        btnCancelEditTeacher = findViewById(R.id.btnCancelEditTeacher);
    }

    private void setupSpinners() {
        String[] departments = {"Computer Science", "Information Technology", "Software Engineering", "Business Administration", "Data Science"};
        String[] positions = {"Lecturer", "Assistant Lecturer", "Senior Lecturer", "Associate Professor", "Professor"};
        String[] workTypes = {"Full-time", "Part-time"};

        setSpinnerAdapter(spEditTeacherDept, departments);
        setSpinnerAdapter(spEditTeacherPosition, positions);
        setSpinnerAdapter(spEditTeacherWorkType, workTypes);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void fetchLatestTeacherDetails() {
        if (currentTeacher == null) return;
        Call<JsonObject> call;
        if (currentTeacher.getDbId() != null && currentTeacher.getDbId() > 0) {
            call = ApiClient.getApiService(this).getTeacher(String.valueOf(currentTeacher.getDbId()));
        } else if (currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0) {
            call = ApiClient.getApiService(this).getUser(String.valueOf(currentTeacher.getUserId()));
        } else {
            return;
        }
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed() || !response.isSuccessful() || response.body() == null) return;
                JsonObject root = response.body();
                JsonObject data = root.has("data") && root.get("data").isJsonObject() ? root.getAsJsonObject("data") : root;
                JsonObject nested = data.has("teacher") && data.get("teacher").isJsonObject() ? data.getAsJsonObject("teacher") : data;

                String kh = getOptStr(nested, "khmer_name", getOptStr(data, "khmer_name", ""));
                String en = getOptStr(nested, "english_name", getOptStr(data, "english_name", ""));
                String rawName = getOptStr(data, "name", currentTeacher.getName());
                currentTeacher.setKhmerName(kh);
                currentTeacher.setEnglishName(en);
                currentTeacher.setName(PersonNameDisplayHelper.resolveDisplayName(en, kh, rawName));

                String em = getOptStr(data, "email", "");
                if (em.isEmpty() && data.has("user") && data.get("user").isJsonObject()) {
                    em = getOptStr(data.getAsJsonObject("user"), "email", "");
                }
                currentTeacher.setEmail(em);
                String dob = getOptStr(data, "date_of_birth", getOptStr(nested, "date_of_birth", ""));
                if (!dob.isEmpty()) currentTeacher.setDob(dob);
                String hireDate = getOptStr(nested, "hire_date", getOptStr(data, "hire_date", ""));
                if (!hireDate.isEmpty()) currentTeacher.setHireDate(hireDate);
                String phone = getOptStr(data, "phone", "");
                if (!phone.isEmpty()) currentTeacher.setPhone(phone);

                prepopulateFields();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {}
        });
    }

    private String getOptStr(JsonObject obj, String key, String fallback) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return fallback;
    }

    private void prepopulateFields() {
        if (currentTeacher == null) return;

        String khmerName = PersonNameDisplayHelper.clean(currentTeacher.getKhmerName());
        String englishName = PersonNameDisplayHelper.clean(currentTeacher.getEnglishName());
        String name = PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, currentTeacher.getName());
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "T";

        if (tvEditTeacherAvatarChar != null) tvEditTeacherAvatarChar.setText(initial);
        if (tvEditTeacherHeaderName != null) tvEditTeacherHeaderName.setText(name);
        if (tvEditTeacherHeaderId != null) tvEditTeacherHeaderId.setText(currentTeacher.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentTeacher.getStatus());
        if (tvEditTeacherHeaderStatus != null) {
            tvEditTeacherHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditTeacherHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditTeacherHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditTeacherKhmerName != null) etEditTeacherKhmerName.setText(khmerName);
        if (etEditTeacherEnglishName != null) etEditTeacherEnglishName.setText(englishName);
        if (etEditTeacherId != null) etEditTeacherId.setText(currentTeacher.getId());
        if (etEditTeacherEmail != null) {
            String em = currentTeacher.getEmail();
            etEditTeacherEmail.setText(em != null && em.contains("@") ? em : "");
        }
        if (etEditTeacherPhone != null) etEditTeacherPhone.setText("N/A".equals(currentTeacher.getPhone()) ? "" : currentTeacher.getPhone());
        if (etEditTeacherDob != null) etEditTeacherDob.setText("N/A".equals(currentTeacher.getDob()) ? "" : currentTeacher.getDob());
        if (etEditTeacherHireDate != null) etEditTeacherHireDate.setText("N/A".equals(currentTeacher.getHireDate()) ? "" : currentTeacher.getHireDate());
        if (etEditTeacherBio != null) etEditTeacherBio.setText(currentTeacher.getRawBio());

        profileFormHelper.setInitialValues(
                currentTeacher.getOfficeLocationId(),
                currentTeacher.getRawOfficeLocation(),
                currentTeacher.getSpecializations()
        );

        setSpinnerSelection(spEditTeacherDept, currentTeacher.getDepartment());
        setSpinnerSelection(spEditTeacherPosition, currentTeacher.getPosition());
        setSpinnerSelection(spEditTeacherWorkType, currentTeacher.getWorkType());

        if (isActive) {
            if (rbEditTeacherActive != null) rbEditTeacherActive.setChecked(true);
        } else {
            if (rbEditTeacherInactive != null) rbEditTeacherInactive.setChecked(true);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        if (etEditTeacherOffice != null) {
            etEditTeacherOffice.setOnClickListener(v -> profileFormHelper.showOfficeLocationPickerDialog());
        }
        if (tilEditTeacherOffice != null) {
            tilEditTeacherOffice.setEndIconOnClickListener(v -> profileFormHelper.showOfficeLocationPickerDialog());
        }
        if (btnSelectEditTeacherSpecializations != null) {
            btnSelectEditTeacherSpecializations.setOnClickListener(v -> profileFormHelper.showSpecializationPickerDialog());
        }

        if (btnCancelEditTeacher != null) {
            btnCancelEditTeacher.setOnClickListener(v -> finish());
        }

        if (btnUpdateTeacher != null) {
            btnUpdateTeacher.setOnClickListener(v -> attemptUpdateTeacher());
        }
    }

    private void attemptUpdateTeacher() {
        if (etEditTeacherKhmerName == null && etEditTeacherEnglishName == null) return;

        String khmerName = etEditTeacherKhmerName != null
                ? PersonNameDisplayHelper.clean(etEditTeacherKhmerName.getText().toString()) : "";
        String englishName = etEditTeacherEnglishName != null
                ? PersonNameDisplayHelper.clean(etEditTeacherEnglishName.getText().toString()) : "";
        String email = etEditTeacherEmail != null ? etEditTeacherEmail.getText().toString().trim() : "";
        String phone = etEditTeacherPhone != null ? etEditTeacherPhone.getText().toString().trim() : "";
        String dob = etEditTeacherDob != null ? etEditTeacherDob.getText().toString().trim() : "";
        String hireDate = etEditTeacherHireDate != null ? etEditTeacherHireDate.getText().toString().trim() : "";
        String office = profileFormHelper.getSelectedOfficeLabel();
        Integer officeId = profileFormHelper.getSelectedOfficeId();
        String bio = etEditTeacherBio != null ? etEditTeacherBio.getText().toString().trim() : "";

        boolean isValid = true;

        if (!PersonNameDisplayHelper.hasAtLeastOneName(khmerName, englishName)) {
            String msg = getString(R.string.err_name_one_required);
            if (tilEditTeacherKhmerName != null) tilEditTeacherKhmerName.setError(msg);
            if (tilEditTeacherEnglishName != null) tilEditTeacherEnglishName.setError(msg);
            isValid = false;
        } else {
            if (tilEditTeacherKhmerName != null) tilEditTeacherKhmerName.setError(null);
            if (tilEditTeacherEnglishName != null) tilEditTeacherEnglishName.setError(null);
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEditTeacherEmail != null) tilEditTeacherEmail.setError(getString(R.string.err_invalid_email));
            isValid = false;
        } else {
            if (tilEditTeacherEmail != null) tilEditTeacherEmail.setError(null);
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (tilEditTeacherDob != null) tilEditTeacherDob.setError(getString(R.string.err_invalid_date));
            isValid = false;
        } else {
            if (tilEditTeacherDob != null) tilEditTeacherDob.setError(null);
        }

        if (!hireDate.isEmpty() && !DatePickerHelper.isValidIsoDate(hireDate)) {
            if (tilEditTeacherHireDate != null) tilEditTeacherHireDate.setError(getString(R.string.err_invalid_date));
            isValid = false;
        } else {
            if (tilEditTeacherHireDate != null) tilEditTeacherHireDate.setError(null);
        }

        if (isValid) {
            String displayName = PersonNameDisplayHelper.resolveDisplayName(englishName, khmerName, "Teacher");
            String dept = spEditTeacherDept != null && spEditTeacherDept.getSelectedItem() != null ? spEditTeacherDept.getSelectedItem().toString() : currentTeacher.getDepartment();
            String pos = spEditTeacherPosition != null && spEditTeacherPosition.getSelectedItem() != null ? spEditTeacherPosition.getSelectedItem().toString() : currentTeacher.getPosition();
            String workType = spEditTeacherWorkType != null && spEditTeacherWorkType.getSelectedItem() != null ? spEditTeacherWorkType.getSelectedItem().toString() : currentTeacher.getWorkType();
            boolean isCheckedActive = (rbEditTeacherActive != null && rbEditTeacherActive.isChecked());
            String status = isCheckedActive ? "active" : "inactive";

            JsonObject payload = new JsonObject();
            payload.addProperty("khmer_name", khmerName);
            payload.addProperty("english_name", englishName);
            payload.addProperty("name", displayName);
            payload.addProperty("email", email);
            payload.addProperty("phone", phone);
            payload.addProperty("date_of_birth", dob);
            payload.addProperty("hire_date", hireDate);
            if (officeId != null) {
                payload.addProperty("office_location_id", officeId);
            } else {
                payload.add("office_location_id", JsonNull.INSTANCE);
            }
            payload.addProperty("office_location", office);
            payload.add("specializations", profileFormHelper.getSelectedSpecializationsJson());
            payload.addProperty("biography", bio);
            payload.addProperty("bio", bio);
            payload.addProperty("department", dept);
            payload.addProperty("department_name", dept);
            payload.addProperty("position", pos);
            payload.addProperty("work_type", workType);
            payload.addProperty("status", status);

            String identifier = "1";
            boolean useTeacherEndpoint = false;
            if (currentTeacher != null) {
                if (currentTeacher.getUserId() != null && currentTeacher.getUserId() > 0) {
                    identifier = String.valueOf(currentTeacher.getUserId());
                } else if (currentTeacher.getDbId() != null && currentTeacher.getDbId() > 0) {
                    identifier = String.valueOf(currentTeacher.getDbId());
                    useTeacherEndpoint = true;
                } else if (currentTeacher.getId() != null) {
                    identifier = currentTeacher.getId();
                }
            }

            if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(false);

            Call<JsonObject> call = useTeacherEndpoint
                    ? ApiClient.getApiService(this).updateTeacher(identifier, payload)
                    : ApiClient.getApiService(this).updateUser(identifier, payload);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(true);
                    if (response.isSuccessful()) {
                        currentTeacher.setKhmerName(khmerName);
                        currentTeacher.setEnglishName(englishName);
                        currentTeacher.setName(displayName);
                        currentTeacher.setEmail(email);
                        currentTeacher.setPhone(phone);
                        currentTeacher.setDob(dob);
                        currentTeacher.setHireDate(hireDate);
                        currentTeacher.setOfficeLocationId(officeId);
                        currentTeacher.setOfficeLocation(office);
                        currentTeacher.setSpecializations(profileFormHelper.getSelectedSpecializations());
                        currentTeacher.setBio(bio);
                        currentTeacher.setDepartment(dept);
                        currentTeacher.setPosition(pos);
                        currentTeacher.setWorkType(workType);
                        currentTeacher.setStatus(isCheckedActive ? "Active" : "Inactive");

                        Toast.makeText(EditTeacherActivity.this, R.string.teacher_account_updated_successfully, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errMsg = "Failed to update teacher: HTTP " + response.code();
                        try (okhttp3.ResponseBody eb = response.errorBody()) {
                            if (eb != null) {
                                JsonObject errObj = com.google.gson.JsonParser.parseString(eb.string()).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(EditTeacherActivity.this, errMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (btnUpdateTeacher != null) btnUpdateTeacher.setEnabled(true);
                    Toast.makeText(EditTeacherActivity.this, "Network error updating teacher: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
