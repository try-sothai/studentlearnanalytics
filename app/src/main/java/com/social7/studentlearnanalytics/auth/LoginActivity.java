package com.social7.studentlearnanalytics.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.AdminMainActivity;
import com.social7.studentlearnanalytics.model.LoginRequest;
import com.social7.studentlearnanalytics.model.LoginResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;
import com.social7.studentlearnanalytics.student.StudentMainActivity;
import com.social7.studentlearnanalytics.teacher.TeacherMainActivity;
import com.social7.studentlearnanalytics.utils.LoadingDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "SLAS_LOGIN";

    private EditText edtStudentID;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);

        edtStudentID = findViewById(R.id.edtStudentID);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String studentId = edtStudentID.getText() != null ? edtStudentID.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (studentId.isEmpty()) {
            edtStudentID.setError("Please enter your Student / Teacher / Admin ID");
            edtStudentID.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Please enter your password");
            edtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        loadingDialog.show();

        LoginRequest request = new LoginRequest(studentId, password);

        ApiClient.getApiService(this).login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                loadingDialog.dismiss();
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    LoginResponse.UserData user = loginResponse.getUser();

                    String role = user != null && !user.getRole().isEmpty() ? user.getRole() : "Student";
                    String name = user != null && !user.getName().isEmpty() ? user.getName() : "Sophea";
                    String userId = !loginResponse.getStudentId().isEmpty() ? loginResponse.getStudentId() : studentId;
                    String token = loginResponse.getToken();

                    if (token.isEmpty()) {
                        token = "session_token_" + System.currentTimeMillis();
                    }

                    Log.d(TAG, "Login successful for " + userId + " (" + role + ")");
                    sessionManager.saveSession(userId, token, role, name);

                    navigateToRoleMain(role);
                } else {
                    Log.e(TAG, "Login failed HTTP code: " + response.code());
                    String message;
                    if (response.code() == 401) {
                        message = "Invalid Student / Teacher / Admin ID or password.";
                    } else {
                        message = "Login failed (server error " + response.code() + "). Please try again.";
                    }
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                btnLogin.setEnabled(true);

                Log.e(TAG, "Login network failure: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Cannot connect to server. Please check your network or backend server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToRoleMain(String role) {
        Intent intent;
        if ("Teacher".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
        } else if ("Admin".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, StudentMainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
