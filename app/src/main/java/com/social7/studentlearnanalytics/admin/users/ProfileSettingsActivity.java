package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.storage.SessionManager;

public class ProfileSettingsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_profile_settings);

        sessionManager = new SessionManager(this);

        initViews();
        prepopulateData();
        setupListeners();
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

    private void prepopulateData() {
        String name = sessionManager != null && sessionManager.getUserName() != null && !sessionManager.getUserName().trim().isEmpty() ? sessionManager.getUserName() : "Mr. Chan";
        String email = "chan@univ.edu";

        String initial = !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "A";

        if (tvProfileAvatar != null) tvProfileAvatar.setText(initial);
        if (tvProfileName != null) tvProfileName.setText(name);
        if (tvProfileRole != null) tvProfileRole.setText("System Administrator");
        if (tvAccountStatusBadge != null) tvAccountStatusBadge.setText("Active");

        if (etProfileName != null) etProfileName.setText(name);
        if (etProfileEmail != null) etProfileEmail.setText(email);
        if (etProfilePhone != null) etProfilePhone.setText("+855 12 345 678");
        if (etProfileDob != null) etProfileDob.setText("Mar 10, 1990");

        if (etAdminId != null) etAdminId.setText("ADM-001");
        if (etAdminDept != null) etAdminDept.setText("College of Computer Studies");
        if (etAccountCreated != null) etAccountCreated.setText("Jan 15, 2024");

        if (rbGenderMale != null) rbGenderMale.setChecked(true);
    }

    private void setupListeners() {
        View btnCamera = findViewById(R.id.btnCameraUpload);

        View.OnClickListener uploadListener = v ->
                Toast.makeText(this, "Profile image selection opened", Toast.LENGTH_SHORT).show();

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

        if (isValid) {
            if (sessionManager != null) {
                sessionManager.saveSession(
                        sessionManager.getUserId() != null ? sessionManager.getUserId() : "ADM-001",
                        sessionManager.getToken() != null ? sessionManager.getToken() : "token",
                        "Admin",
                        name
                );
            }
            Toast.makeText(this, "Admin profile updated and persisted successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
