package com.social7.studentlearnanalytics.admin.users;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class AddAdminActivity extends AppCompatActivity {

    private TextInputLayout tilAddAdminName, tilAddAdminId, tilAddAdminEmail, tilAddAdminPhone, tilAddAdminDob, tilAddAdminDesc;
    private EditText etAddAdminName, etAddAdminId, etAddAdminEmail, etAddAdminPhone, etAddAdminDob, etAddAdminDesc;
    private Spinner spAddAdminRole, spAddAdminAccessLevel, spAddAdminModules;
    private RadioGroup rgAddAdminAccountStatus;
    private MaterialButton btnSaveAdmin, btnCancelAddAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        initViews();
        setupSpinners();
        setupListeners();
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
            Toast.makeText(this, "Admin " + name + " created successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
