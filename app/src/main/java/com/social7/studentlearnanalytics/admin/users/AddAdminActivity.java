package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAdminActivity extends AppCompatActivity {

    private TextInputLayout tilAddAdminName, tilAddAdminId, tilAddAdminEmail, tilAddAdminPhone, tilAddAdminDob, tilAddAdminDesc;
    private EditText etAddAdminName, etAddAdminId, etAddAdminEmail, etAddAdminPhone, etAddAdminDob, etAddAdminDesc;
    private Spinner spAddAdminRole, spAddAdminAccessLevel, spAddAdminModules;
    private RadioGroup rgAddAdminAccountStatus;
    private MaterialButton btnSaveAdmin, btnCancelAddAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_admin);

        initViews();
        setupSpinners();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAddAdmin);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilAddAdminName = findViewById(R.id.tilAddAdminName);
        tilAddAdminId = findViewById(R.id.tilAddAdminId);
        tilAddAdminEmail = findViewById(R.id.tilAddAdminEmail);
        tilAddAdminPhone = findViewById(R.id.tilAddAdminPhone);
        tilAddAdminDob = findViewById(R.id.tilAddAdminDob);
        tilAddAdminDesc = findViewById(R.id.tilAddAdminDesc);

        etAddAdminName = findViewById(R.id.etAddAdminName);
        etAddAdminId = findViewById(R.id.etAddAdminId);
        etAddAdminEmail = findViewById(R.id.etAddAdminEmail);
        etAddAdminPhone = findViewById(R.id.etAddAdminPhone);
        etAddAdminDob = findViewById(R.id.etAddAdminDob);
        etAddAdminDesc = findViewById(R.id.etAddAdminDesc);

        spAddAdminRole = findViewById(R.id.spAddAdminRole);
        spAddAdminAccessLevel = findViewById(R.id.spAddAdminAccessLevel);
        spAddAdminModules = findViewById(R.id.spAddAdminModules);

        rgAddAdminAccountStatus = findViewById(R.id.rgAddAdminAccountStatus);

        btnSaveAdmin = findViewById(R.id.btnSaveAdmin);
        btnCancelAddAdmin = findViewById(R.id.btnCancelAddAdmin);
    }

    private void setupSpinners() {
        String[] roles = {"Select role", "System Administrator", "Academic Administrator", "Course Administrator", "Report Administrator", "Support Administrator", "Content Admin", "Data Analyst", "User Admin"};
        String[] accessLevels = {"Select access level", "Full Access", "Read & Write", "Restricted Access"};
        String[] managedModules = {"Select managed modules", "All Modules", "Course Management", "User Management & Analytics"};

        setSpinnerAdapter(spAddAdminRole, roles);
        setSpinnerAdapter(spAddAdminAccessLevel, accessLevels);
        setSpinnerAdapter(spAddAdminModules, managedModules);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnCancelAddAdmin != null) {
            btnCancelAddAdmin.setOnClickListener(v -> finish());
        }

        if (btnSaveAdmin != null) {
            btnSaveAdmin.setOnClickListener(v -> attemptSaveAdmin());
        }
    }

    private void attemptSaveAdmin() {
        if (etAddAdminName == null || etAddAdminId == null || etAddAdminEmail == null) return;

        String name = etAddAdminName.getText().toString().trim();
        String adminId = etAddAdminId.getText().toString().trim();
        String email = etAddAdminEmail.getText().toString().trim();
        String phone = etAddAdminPhone != null ? etAddAdminPhone.getText().toString().trim() : "";
        String dob = etAddAdminDob != null ? etAddAdminDob.getText().toString().trim() : "";
        String desc = etAddAdminDesc != null ? etAddAdminDesc.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilAddAdminName != null) tilAddAdminName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilAddAdminName != null) tilAddAdminName.setError(null);
        }

        if (adminId.isEmpty()) {
            if (tilAddAdminId != null) tilAddAdminId.setError("Admin ID is required");
            isValid = false;
        } else {
            if (tilAddAdminId != null) tilAddAdminId.setError(null);
        }

        if (email.isEmpty()) {
            if (tilAddAdminEmail != null) tilAddAdminEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilAddAdminEmail != null) tilAddAdminEmail.setError(null);
        }

        if (spAddAdminRole != null && spAddAdminRole.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select an admin role", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            String role = spAddAdminRole != null ? spAddAdminRole.getSelectedItem().toString() : "System Administrator";
            String accessLevel = spAddAdminAccessLevel != null && spAddAdminAccessLevel.getSelectedItemPosition() > 0
                    ? spAddAdminAccessLevel.getSelectedItem().toString() : "Full Access";
            String modules = spAddAdminModules != null && spAddAdminModules.getSelectedItemPosition() > 0
                    ? spAddAdminModules.getSelectedItem().toString() : "All Modules";

            boolean isActive = true;
            if (rgAddAdminAccountStatus != null) {
                int selectedId = rgAddAdminAccountStatus.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton rb = findViewById(selectedId);
                    if (rb != null && "Inactive".equalsIgnoreCase(rb.getText().toString())) {
                        isActive = false;
                    }
                }
            }

            JsonObject body = new JsonObject();
            body.addProperty("name", name);
            body.addProperty("email", email);
            body.addProperty("password", "12345678");
            body.addProperty("password_confirmation", "12345678");
            body.addProperty("role", "admin");
            body.addProperty("code", adminId);
            body.addProperty("status", isActive ? "active" : "inactive");
            if (!phone.isEmpty()) body.addProperty("phone", phone);
            if (!dob.isEmpty()) body.addProperty("date_of_birth", dob);
            if (!desc.isEmpty()) body.addProperty("description", desc);
            body.addProperty("role_title", role);
            body.addProperty("access_level", accessLevel);
            body.addProperty("managed_modules", modules);

            if (btnSaveAdmin != null) btnSaveAdmin.setEnabled(false);

            ApiClient.getApiService(this).createUser(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveAdmin != null) btnSaveAdmin.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(AddAdminActivity.this, "Admin " + name + " created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to create admin (HTTP " + response.code() + ")";
                        try (ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                JsonObject errObj = JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errorMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(AddAdminActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveAdmin != null) btnSaveAdmin.setEnabled(true);
                    Toast.makeText(AddAdminActivity.this, "Network error creating admin", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
