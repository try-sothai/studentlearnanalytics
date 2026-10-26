package com.social7.studentlearnanalytics.admin.users;

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

public class ProfileSettingsActivity extends AppCompatActivity {

    private TextView tvProfileAvatar, tvProfileName, tvProfileRole;
    private TextInputLayout tilProfileName, tilProfileEmail;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileDob, etProfilePosition, etProfileBio;
    private RadioGroup rgProfileGender;
    private RadioButton rbGenderMale, rbGenderFemale;
    private MaterialButton btnSaveProfileSettings, btnCancelProfileSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

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

        tilProfileName = findViewById(R.id.tilProfileName);
        tilProfileEmail = findViewById(R.id.tilProfileEmail);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileDob = findViewById(R.id.etProfileDob);
        etProfilePosition = findViewById(R.id.etProfilePosition);
        etProfileBio = findViewById(R.id.etProfileBio);

        rgProfileGender = findViewById(R.id.rgProfileGender);
        rbGenderMale = findViewById(R.id.rbGenderMale);
        rbGenderFemale = findViewById(R.id.rbGenderFemale);

        btnSaveProfileSettings = findViewById(R.id.btnSaveProfileSettings);
        btnCancelProfileSettings = findViewById(R.id.btnCancelProfileSettings);
    }

    private void prepopulateData() {
        if (tvProfileAvatar != null) tvProfileAvatar.setText("C");
        if (tvProfileName != null) tvProfileName.setText("Mr. Chan");
        if (tvProfileRole != null) tvProfileRole.setText("System Administrator");

        if (etProfileName != null) etProfileName.setText("Mr. Chan");
        if (etProfileEmail != null) etProfileEmail.setText("chan@univ.edu");
        if (etProfilePhone != null) etProfilePhone.setText("+855 12 345 678");
        if (etProfileDob != null) etProfileDob.setText("Mar 10, 1990");
        if (etProfilePosition != null) etProfilePosition.setText("System Administrator");
        if (etProfileBio != null) etProfileBio.setText("Experienced system administrator managing SLAS core analytics.");

        if (rbGenderMale != null) rbGenderMale.setChecked(true);
    }

    private void setupListeners() {
        View btnCamera = findViewById(R.id.btnCameraUpload);
        View btnUploadArea = findViewById(R.id.btnUploadArea);

        View.OnClickListener uploadListener = v ->
                Toast.makeText(this, "Profile image selection opened", Toast.LENGTH_SHORT).show();

        if (btnCamera != null) btnCamera.setOnClickListener(uploadListener);
        if (btnUploadArea != null) btnUploadArea.setOnClickListener(uploadListener);

        if (btnCancelProfileSettings != null) {
            btnCancelProfileSettings.setOnClickListener(v -> finish());
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
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
