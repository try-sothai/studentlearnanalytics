package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSettingsActivity";

    private TextView tvProfileAvatar, tvProfileName, tvProfileRole, tvAccountStatusBadge;
    private TextInputLayout tilProfileName, tilProfileEmail;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileDob, etAdminId, etAdminDept, etAccountCreated;
    private RadioGroup rgProfileGender;
    private RadioButton rbGenderMale, rbGenderFemale;
    private MaterialButton btnSaveProfileSettings, btnCancelProfileSettings, btnChangePassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        setContentView(R.layout.activity_profile_settings);

        sessionManager = new SessionManager(this);

        initViews();
        prepopulateFromSession();
        fetchProfileFromApi();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.social7.studentlearnanalytics.utils.RoleGuard.checkAccess(this, "Admin");
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackProfileSettings);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvProfileAvatar = findViewById(R.id.tvProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvAccountStatusBadge = findViewById(R.id.tvAccountStatusBadge);

        tilProfileName = findViewById(R.id.tilProfileName);
        tilProfileEmail = findViewById(R.id.tilProfileEmail);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileDob = findViewById(R.id.etProfileDob);
        DatePickerHelper.attachDatePicker(null, etProfileDob, true);

        etAdminId = findViewById(R.id.etAdminId);
        etAdminDept = findViewById(R.id.etAdminDept);
        etAccountCreated = findViewById(R.id.etAccountCreated);

        rgProfileGender = findViewById(R.id.rgProfileGender);
        rbGenderMale = findViewById(R.id.rbGenderMale);
        rbGenderFemale = findViewById(R.id.rbGenderFemale);

        btnSaveProfileSettings = findViewById(R.id.btnSaveProfileSettings);
        btnCancelProfileSettings = findViewById(R.id.btnCancelProfileSettings);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void prepopulateFromSession() {
        String name = sessionManager != null && sessionManager.getUserName() != null && !sessionManager.getUserName().trim().isEmpty()
                ? sessionManager.getUserName() : "User";
        String email = sessionManager != null && sessionManager.getUserEmail() != null && !sessionManager.getUserEmail().trim().isEmpty()
                ? sessionManager.getUserEmail() : "";
        String role = sessionManager != null && sessionManager.getRole() != null ? sessionManager.getRole() : "Admin";
        String initial = !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "U";

        if (tvProfileAvatar != null) tvProfileAvatar.setText(initial);
        if (tvProfileName != null) tvProfileName.setText(name);
        if (tvProfileRole != null) tvProfileRole.setText(role);
        if (tvAccountStatusBadge != null) tvAccountStatusBadge.setText(R.string.status_active);

        if (etProfileName != null) etProfileName.setText(name);
        if (etProfileEmail != null) etProfileEmail.setText(email);
        if (etAdminId != null) etAdminId.setText(sessionManager != null && sessionManager.getUserId() != null ? sessionManager.getUserId() : "ADM-001");
    }

    private void fetchProfileFromApi() {
        ApiClient.getApiService(this).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject user = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        String name = user.has("name") && !user.get("name").isJsonNull() ? user.get("name").getAsString() : "";
                        String email = user.has("email") && !user.get("email").isJsonNull() ? user.get("email").getAsString() : "";
                        String phone = user.has("phone") && !user.get("phone").isJsonNull() ? user.get("phone").getAsString() : "";
                        String dob = user.has("date_of_birth") && !user.get("date_of_birth").isJsonNull() ? user.get("date_of_birth").getAsString() : "";
                        String gender = user.has("gender") && !user.get("gender").isJsonNull() ? user.get("gender").getAsString() : "";
                        String role = user.has("role") && !user.get("role").isJsonNull() ? user.get("role").getAsString() : "Admin";
                        String status = user.has("status") && !user.get("status").isJsonNull() ? user.get("status").getAsString() : "Active";
                        String createdAt = user.has("created_at") && !user.get("created_at").isJsonNull() ? user.get("created_at").getAsString() : "";
                        String code = user.has("code") && !user.get("code").isJsonNull() ? user.get("code").getAsString() : (user.has("id") ? user.get("id").getAsString() : "");

                        if (!name.isEmpty()) {
                            if (tvProfileAvatar != null) tvProfileAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                            if (tvProfileName != null) tvProfileName.setText(name);
                            if (etProfileName != null) etProfileName.setText(name);
                        }
                        if (tvProfileRole != null) tvProfileRole.setText(capitalize(role));
                        if (tvAccountStatusBadge != null) tvAccountStatusBadge.setText(capitalize(status));

                        if (etProfileEmail != null) etProfileEmail.setText(email);
                        if (etProfilePhone != null) etProfilePhone.setText(phone);
                        if (etProfileDob != null) etProfileDob.setText(dob);
                        if (etAdminId != null && !code.isEmpty()) etAdminId.setText(code);
                        if (etAccountCreated != null && !createdAt.isEmpty()) etAccountCreated.setText(createdAt.length() > 10 ? createdAt.substring(0, 10) : createdAt);

                        if ("Female".equalsIgnoreCase(gender) && rbGenderFemale != null) {
                            rbGenderFemale.setChecked(true);
                        } else if (rbGenderMale != null) {
                            rbGenderMale.setChecked(true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load profile", t);
            }
        });
    }

    private void setupListeners() {
        View btnCamera = findViewById(R.id.btnCameraUpload);
        View.OnClickListener uploadListener = v ->
                Toast.makeText(this, "Profile photo upload is not supported in this version.", Toast.LENGTH_SHORT).show();

        if (btnCamera != null) btnCamera.setOnClickListener(uploadListener);

        if (btnCancelProfileSettings != null) {
            btnCancelProfileSettings.setOnClickListener(v -> finish());
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(this, AccountSecurityActivity.class);
                startActivity(intent);
            });
        }

        if (btnSaveProfileSettings != null) {
            btnSaveProfileSettings.setOnClickListener(v -> attemptSaveProfile());
        }
    }

    private void attemptSaveProfile() {
        if (etProfileName == null || etProfileEmail == null) return;

        String name = etProfileName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();
        String phone = etProfilePhone != null ? etProfilePhone.getText().toString().trim() : "";
        String dob = etProfileDob != null ? etProfileDob.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilProfileName != null) tilProfileName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilProfileName != null) tilProfileName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilProfileEmail != null) tilProfileEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilProfileEmail != null) tilProfileEmail.setError(null);
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (etProfileDob != null) etProfileDob.setError(getString(R.string.err_invalid_date));
            isValid = false;
        }

        if (isValid) {
            String gender = "Male";
            if (rbGenderFemale != null && rbGenderFemale.isChecked()) {
                gender = "Female";
            }

            JsonObject body = new JsonObject();
            body.addProperty("name", name);
            body.addProperty("email", email);
            if (!phone.isEmpty()) body.addProperty("phone", phone);
            body.addProperty("date_of_birth", dob);
            body.addProperty("gender", gender);

            if (btnSaveProfileSettings != null) btnSaveProfileSettings.setEnabled(false);

            ApiClient.getApiService(this).updateProfile(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveProfileSettings != null) btnSaveProfileSettings.setEnabled(true);

                    if (response.isSuccessful()) {
                        if (sessionManager != null) {
                            sessionManager.updateUserProfile(name, email);
                        }
                        Toast.makeText(ProfileSettingsActivity.this, R.string.profile_updated_successfully, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to update profile (HTTP " + response.code() + ")";
                        try (ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                JsonObject errObj = JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errorMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(ProfileSettingsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnSaveProfileSettings != null) btnSaveProfileSettings.setEnabled(true);
                    Toast.makeText(ProfileSettingsActivity.this, R.string.network_error_updating_profile, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
