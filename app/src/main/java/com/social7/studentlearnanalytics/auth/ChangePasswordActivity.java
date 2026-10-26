package com.social7.studentlearnanalytics.auth;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextView tvReqLength, tvReqUpper, tvReqLower, tvReqNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);

        View btnBack = findViewById(R.id.btnBackPassword);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tvReqLength = findViewById(R.id.tvReqLength);
        tvReqUpper = findViewById(R.id.tvReqUpper);
        tvReqLower = findViewById(R.id.tvReqLower);
        tvReqNumber = findViewById(R.id.tvReqNumber);

        View btnUpdate = findViewById(R.id.btnUpdatePassword);

        if (etNewPassword != null) {
            etNewPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateRequirements(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> attemptUpdatePassword());
        }
    }

    private void validateRequirements(String password) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");

        int activeColor = Color.parseColor("#16A34A"); // Success green
        int inactiveColor = Color.parseColor("#64748B"); // Gray

        if (tvReqLength != null) tvReqLength.setTextColor(hasLength ? activeColor : inactiveColor);
        if (tvReqUpper != null) tvReqUpper.setTextColor(hasUpper ? activeColor : inactiveColor);
        if (tvReqLower != null) tvReqLower.setTextColor(hasLower ? activeColor : inactiveColor);
        if (tvReqNumber != null) tvReqNumber.setTextColor(hasNumber ? activeColor : inactiveColor);
    }

    private void attemptUpdatePassword() {
        if (etCurrentPassword == null || etNewPassword == null || etConfirmPassword == null) return;

        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        if (currentPass.isEmpty()) {
            if (tilCurrentPassword != null) tilCurrentPassword.setError("Please enter current password");
            isValid = false;
        } else {
            if (tilCurrentPassword != null) tilCurrentPassword.setError(null);
        }

        if (newPass.isEmpty()) {
            if (tilNewPassword != null) tilNewPassword.setError("Please enter new password");
            isValid = false;
        } else if (newPass.length() < 8 || !newPass.matches(".*[A-Z].*") || !newPass.matches(".*[a-z].*") || !newPass.matches(".*[0-9].*")) {
            if (tilNewPassword != null) tilNewPassword.setError("Password does not meet all requirements");
            isValid = false;
        } else {
            if (tilNewPassword != null) tilNewPassword.setError(null);
        }

        if (confirmPass.isEmpty()) {
            if (tilConfirmPassword != null) tilConfirmPassword.setError("Please confirm new password");
            isValid = false;
        } else if (!confirmPass.equals(newPass)) {
            if (tilConfirmPassword != null) tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
        }

        if (isValid) {
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
