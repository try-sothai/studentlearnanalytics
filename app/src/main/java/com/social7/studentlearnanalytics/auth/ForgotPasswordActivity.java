package com.social7.studentlearnanalytics.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtForgotEmail;
    private Button btnSendReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        edtForgotEmail = findViewById(R.id.edtForgotEmail);
        btnSendReset = findViewById(R.id.btnSendReset);

        btnSendReset.setOnClickListener(v -> {

            String email = edtForgotEmail.getText()
                    .toString()
                    .trim();

            if (email.isEmpty()) {
                edtForgotEmail.setError("Please enter your email");
                edtForgotEmail.requestFocus();
                return;
            }

            Toast.makeText(
                    ForgotPasswordActivity.this,
                    "Reset link request submitted",
                    Toast.LENGTH_LONG
            ).show();
        });
    }
}