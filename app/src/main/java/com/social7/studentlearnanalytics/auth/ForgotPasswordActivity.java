package com.social7.studentlearnanalytics.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtForgotEmail;
    private Button btnSendReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        edtForgotEmail = findViewById(R.id.edtForgotEmail);
        btnSendReset = findViewById(R.id.btnSendReset);

        if (btnSendReset != null) {
            btnSendReset.setOnClickListener(v -> {
                String email = edtForgotEmail != null && edtForgotEmail.getText() != null
                        ? edtForgotEmail.getText().toString().trim()
                        : "";

                if (email.isEmpty()) {
                    if (edtForgotEmail != null) {
                        edtForgotEmail.setError("Please enter your email address");
                        edtForgotEmail.requestFocus();
                    }
                    return;
                }

                if (!ValidationUtils.isValidEmail(email)) {
                    if (edtForgotEmail != null) {
                        edtForgotEmail.setError("Please enter a valid email address");
                        edtForgotEmail.requestFocus();
                    }
                    return;
                }

                new MaterialAlertDialogBuilder(ForgotPasswordActivity.this)
                        .setTitle("Administrator Password Reset Required")
                        .setMessage("Self-service email reset links are not enabled on this SLAS server.\n\n"
                                + "Please contact your university System Administrator and provide your registered email ("
                                + email + ") so they can reset your password securely via User Management.")
                        .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }
}