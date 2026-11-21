package com.social7.studentlearnanalytics.admin.users;

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
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Admin;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAdminActivity extends AppCompatActivity {

    private static final String TAG = "EditAdminActivity";
    private Admin currentAdmin;

    private TextView tvEditAdminAvatarChar, tvEditAdminHeaderName, tvEditAdminHeaderId, tvEditAdminHeaderStatus;
    private TextInputLayout tilEditAdminName, tilEditAdminEmail, tilEditAdminPhone, tilEditAdminDob, tilEditAdminDesc;
    private EditText etEditAdminName, etEditAdminId, etEditAdminEmail, etEditAdminPhone, etEditAdminDob, etEditAdminDesc;

    private Spinner spEditAdminRole, spEditAdminAccessLevel, spEditAdminModules;
    private RadioGroup rgEditAdminAccountStatus;
    private RadioButton rbEditAdminActive, rbEditAdminInactive;
    private MaterialButton btnUpdateAdmin, btnCancelEditAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin);

        if (getIntent() != null && getIntent().hasExtra("admin")) {
            currentAdmin = (Admin) getIntent().getSerializableExtra("admin");
        }

        if (currentAdmin == null) {
            currentAdmin = new Admin("ADM001", "Mr. Chan", "chan@univ.edu", "System Administrator", "Active");
        }

        initViews();
        setupSpinners();
        prepopulateFields();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackEditAdmin);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEditAdminAvatarChar = findViewById(R.id.tvEditAdminAvatarChar);
        tvEditAdminHeaderName = findViewById(R.id.tvEditAdminHeaderName);
        tvEditAdminHeaderId = findViewById(R.id.tvEditAdminHeaderId);
        tvEditAdminHeaderStatus = findViewById(R.id.tvEditAdminHeaderStatus);

        tilEditAdminName = findViewById(R.id.tilEditAdminName);
        tilEditAdminEmail = findViewById(R.id.tilEditAdminEmail);
        tilEditAdminPhone = findViewById(R.id.tilEditAdminPhone);
        tilEditAdminDob = findViewById(R.id.tilEditAdminDob);
        tilEditAdminDesc = findViewById(R.id.tilEditAdminDesc);

        etEditAdminName = findViewById(R.id.etEditAdminName);
        etEditAdminId = findViewById(R.id.etEditAdminId);
        etEditAdminEmail = findViewById(R.id.etEditAdminEmail);
        etEditAdminPhone = findViewById(R.id.etEditAdminPhone);
        etEditAdminDob = findViewById(R.id.etEditAdminDob);
        etEditAdminDesc = findViewById(R.id.etEditAdminDesc);
        DatePickerHelper.attachDatePicker(tilEditAdminDob, etEditAdminDob, true);

        spEditAdminRole = findViewById(R.id.spEditAdminRole);
        spEditAdminAccessLevel = findViewById(R.id.spEditAdminAccessLevel);
        spEditAdminModules = findViewById(R.id.spEditAdminModules);

        rgEditAdminAccountStatus = findViewById(R.id.rgEditAdminAccountStatus);
        rbEditAdminActive = findViewById(R.id.rbEditAdminActive);
        rbEditAdminInactive = findViewById(R.id.rbEditAdminInactive);

        btnUpdateAdmin = findViewById(R.id.btnUpdateAdmin);
        btnCancelEditAdmin = findViewById(R.id.btnCancelEditAdmin);
    }

    private void setupSpinners() {
        String[] roles = {"System Administrator", "Academic Administrator", "Course Administrator", "Report Administrator", "Support Administrator", "Content Admin", "Data Analyst", "User Admin"};
        String[] accessLevels = {"Full Access", "Read & Write", "Restricted Access"};
        String[] managedModules = {"All Modules", "Course Management", "User Management & Analytics"};

        setSpinnerAdapter(spEditAdminRole, roles);
        setSpinnerAdapter(spEditAdminAccessLevel, accessLevels);
        setSpinnerAdapter(spEditAdminModules, managedModules);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void prepopulateFields() {
        if (currentAdmin == null) return;

        String name = currentAdmin.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "A";

        if (tvEditAdminAvatarChar != null) tvEditAdminAvatarChar.setText(initial);
        if (tvEditAdminHeaderName != null) tvEditAdminHeaderName.setText(name);
        if (tvEditAdminHeaderId != null) tvEditAdminHeaderId.setText(currentAdmin.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentAdmin.getStatus());
        if (tvEditAdminHeaderStatus != null) {
            tvEditAdminHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditAdminHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditAdminHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditAdminName != null) etEditAdminName.setText(name);
        if (etEditAdminId != null) etEditAdminId.setText(currentAdmin.getId());
        if (etEditAdminEmail != null) etEditAdminEmail.setText(currentAdmin.getEmail());
        if (etEditAdminPhone != null) etEditAdminPhone.setText("N/A".equals(currentAdmin.getPhone()) ? "" : currentAdmin.getPhone());
        if (etEditAdminDob != null) etEditAdminDob.setText("N/A".equals(currentAdmin.getDob()) ? "" : currentAdmin.getDob());
        if (etEditAdminDesc != null) etEditAdminDesc.setText(currentAdmin.getDescription());

        setSpinnerSelection(spEditAdminRole, currentAdmin.getRole());
        setSpinnerSelection(spEditAdminAccessLevel, currentAdmin.getAccessLevel());
        setSpinnerSelection(spEditAdminModules, currentAdmin.getManagedModules());

        if (isActive) {
            if (rbEditAdminActive != null) rbEditAdminActive.setChecked(true);
        } else {
            if (rbEditAdminInactive != null) rbEditAdminInactive.setChecked(true);
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
        if (btnCancelEditAdmin != null) {
            btnCancelEditAdmin.setOnClickListener(v -> finish());
        }

        if (btnUpdateAdmin != null) {
            btnUpdateAdmin.setOnClickListener(v -> attemptUpdateAdmin());
        }
    }

    private void attemptUpdateAdmin() {
        if (etEditAdminName == null || etEditAdminEmail == null) return;

        String name = etEditAdminName.getText().toString().trim();
        String email = etEditAdminEmail.getText().toString().trim();
        String phone = etEditAdminPhone != null ? etEditAdminPhone.getText().toString().trim() : "";
        String dob = etEditAdminDob != null ? etEditAdminDob.getText().toString().trim() : "";
        String desc = etEditAdminDesc != null ? etEditAdminDesc.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilEditAdminName != null) tilEditAdminName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilEditAdminName != null) tilEditAdminName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilEditAdminEmail != null) tilEditAdminEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilEditAdminEmail != null) tilEditAdminEmail.setError(null);
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (tilEditAdminDob != null) tilEditAdminDob.setError(getString(R.string.err_invalid_date));
            isValid = false;
        } else {
            if (tilEditAdminDob != null) tilEditAdminDob.setError(null);
        }

        if (isValid) {
            String role = spEditAdminRole != null && spEditAdminRole.getSelectedItem() != null ? spEditAdminRole.getSelectedItem().toString() : currentAdmin.getRole();
            String accessLevel = spEditAdminAccessLevel != null && spEditAdminAccessLevel.getSelectedItem() != null ? spEditAdminAccessLevel.getSelectedItem().toString() : currentAdmin.getAccessLevel();
            String modules = spEditAdminModules != null && spEditAdminModules.getSelectedItem() != null ? spEditAdminModules.getSelectedItem().toString() : currentAdmin.getManagedModules();
            boolean isCheckedActive = (rbEditAdminActive != null && rbEditAdminActive.isChecked());
            String status = isCheckedActive ? "active" : "inactive";

            JsonObject payload = new JsonObject();
            payload.addProperty("name", name);
            payload.addProperty("email", email);
            if (!phone.isEmpty()) payload.addProperty("phone", phone);
            payload.addProperty("date_of_birth", dob);
            if (!desc.isEmpty()) payload.addProperty("description", desc);
            payload.addProperty("role_title", role);
            payload.addProperty("access_level", accessLevel);
            payload.addProperty("managed_modules", modules);
            payload.addProperty("status", status);

            String identifier = "1";
            if (currentAdmin != null) {
                if (currentAdmin.getUserId() != null && currentAdmin.getUserId() > 0) {
                    identifier = String.valueOf(currentAdmin.getUserId());
                } else if (currentAdmin.getId() != null) {
                    identifier = currentAdmin.getId();
                }
            }

            if (btnUpdateAdmin != null) btnUpdateAdmin.setEnabled(false);

            ApiClient.getApiService(this).updateUser(identifier, payload).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (btnUpdateAdmin != null) btnUpdateAdmin.setEnabled(true);
                    if (response.isSuccessful()) {
                        currentAdmin.setName(name);
                        currentAdmin.setEmail(email);
                        if (!phone.isEmpty()) currentAdmin.setPhone(phone);
                        if (!dob.isEmpty()) currentAdmin.setDob(dob);
                        if (!desc.isEmpty()) currentAdmin.setDescription(desc);
                        currentAdmin.setRole(role);
                        currentAdmin.setAccessLevel(accessLevel);
                        currentAdmin.setManagedModules(modules);
                        currentAdmin.setStatus(isCheckedActive ? "Active" : "Inactive");

                        Toast.makeText(EditAdminActivity.this, R.string.admin_account_updated_successfully, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(EditAdminActivity.this, "Failed to update admin: HTTP " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (btnUpdateAdmin != null) btnUpdateAdmin.setEnabled(true);
                    Toast.makeText(EditAdminActivity.this, "Network error updating admin: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
