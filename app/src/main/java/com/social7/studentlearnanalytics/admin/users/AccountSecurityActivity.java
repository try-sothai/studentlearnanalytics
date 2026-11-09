package com.social7.studentlearnanalytics.admin.users;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountSecurityActivity extends AppCompatActivity {

    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnUpdatePassword, btnEnable2FA;
    private TextView tvBadge2FAStatus;
    private boolean is2FAEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_security);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAccountSecurity);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnEnable2FA = findViewById(R.id.btnEnable2FA);
        tvBadge2FAStatus = findViewById(R.id.tvBadge2FAStatus);
    }

    private void setupListeners() {
        if (btnUpdatePassword != null) {
            btnUpdatePassword.setOnClickListener(v -> attemptUpdatePassword());
        }

        if (btnEnable2FA != null) {
            btnEnable2FA.setOnClickListener(v -> toggle2FA());
        }
    }

    private void attemptUpdatePassword() {
        if (etCurrentPassword == null || etNewPassword == null || etConfirmNewPassword == null) return;

        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmNewPassword.getText().toString().trim();

        boolean isValid = true;

        if (currentPass.isEmpty()) {
            if (tilCurrentPassword != null) tilCurrentPassword.setError("Current password is required");
            isValid = false;
        } else {
            if (tilCurrentPassword != null) tilCurrentPassword.setError(null);
        }

        if (newPass.isEmpty() || newPass.length() < 8) {
            if (tilNewPassword != null) tilNewPassword.setError("New password must be at least 8 characters");
            isValid = false;
        } else {
            if (tilNewPassword != null) tilNewPassword.setError(null);
        }

        if (!confirmPass.equals(newPass)) {
            if (tilConfirmNewPassword != null) tilConfirmNewPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            if (tilConfirmNewPassword != null) tilConfirmNewPassword.setError(null);
        }

        if (isValid) {
            JsonObject body = new JsonObject();
            body.addProperty("current_password", currentPass);
            body.addProperty("new_password", newPass);
            body.addProperty("new_password_confirmation", confirmPass);

            if (btnUpdatePassword != null) btnUpdatePassword.setEnabled(false);

            ApiClient.getApiService(this).changePassword(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnUpdatePassword != null) btnUpdatePassword.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(AccountSecurityActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = "Failed to update password (HTTP " + response.code() + ")";
                        try (ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                String rawError = errorBody.string();
                                JsonObject errObj = JsonParser.parseString(rawError).getAsJsonObject();
                                if (errObj.has("message") && !errObj.get("message").isJsonNull()) {
                                    errorMsg = errObj.get("message").getAsString();
                                }
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(AccountSecurityActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    if (btnUpdatePassword != null) btnUpdatePassword.setEnabled(true);
                    Toast.makeText(AccountSecurityActivity.this, "Network error updating password", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toggle2FA() {
        is2FAEnabled = !is2FAEnabled;
        if (tvBadge2FAStatus != null) {
            tvBadge2FAStatus.setText(is2FAEnabled ? "Enabled" : "Disabled");
            tvBadge2FAStatus.setTextColor(Color.parseColor(is2FAEnabled ? "#16A34A" : "#EF4444"));
            tvBadge2FAStatus.setBackgroundResource(is2FAEnabled ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }
        if (btnEnable2FA != null) {
            btnEnable2FA.setText(is2FAEnabled ? "Disable 2FA" : "Enable 2FA");
        }
        Toast.makeText(this, "Two-Factor Authentication " + (is2FAEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }
}
