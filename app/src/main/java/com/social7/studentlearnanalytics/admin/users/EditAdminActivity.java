package com.social7.studentlearnanalytics.admin.users;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Admin;

public class EditAdminActivity extends AppCompatActivity {

    private Admin currentAdmin;

    private TextView tvEditAdminAvatarChar, tvEditAdminHeaderName, tvEditAdminHeaderId, tvEditAdminHeaderStatus;
    private TextInputLayout tilEditAdminName, tilEditAdminEmail, tilEditAdminPhone, tilEditAdminDob, tilEditAdminDesc;
    private EditText etEditAdminName, etEditAdminId, etEditAdminEmail, etEditAdminPhone, etEditAdminDob, etEditAdminDesc;

    private Spinner spEditAdminRole, spEditAdminAccessLevel, spEditAdminModules;
    private RadioGroup rgEditAdminAccountStatus;
    private RadioButton rbEditAdminActive, rbEditAdminInactive;
    private MaterialButton btnUpdateAdmin, btnCancelEditAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin);

        if (getIntent() != null && getIntent().hasExtra("admin")) {
            currentAdmin = (Admin) getIntent().getSerializableExtra("admin");
        }

        if (currentAdmin == null) {
            currentAdmin = new Admin("ADM001", "Mr. Chan", "chan@univ.edu", "System Administrator", "Active");
        }

        initViews();
        setupSpinners();
        prepopulateFields();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackEditAdmin);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEditAdminAvatarChar = findViewById(R.id.tvEditAdminAvatarChar);
        tvEditAdminHeaderName = findViewById(R.id.tvEditAdminHeaderName);
        tvEditAdminHeaderId = findViewById(R.id.tvEditAdminHeaderId);
        tvEditAdminHeaderStatus = findViewById(R.id.tvEditAdminHeaderStatus);

        tilEditAdminName = findViewById(R.id.tilEditAdminName);
        tilEditAdminEmail = findViewById(R.id.tilEditAdminEmail);
        tilEditAdminPhone = findViewById(R.id.tilEditAdminPhone);
        tilEditAdminDob = findViewById(R.id.tilEditAdminDob);
        tilEditAdminDesc = findViewById(R.id.tilEditAdminDesc);

        etEditAdminName = findViewById(R.id.etEditAdminName);
        etEditAdminId = findViewById(R.id.etEditAdminId);
        etEditAdminEmail = findViewById(R.id.etEditAdminEmail);
        etEditAdminPhone = findViewById(R.id.etEditAdminPhone);
        etEditAdminDob = findViewById(R.id.etEditAdminDob);
        etEditAdminDesc = findViewById(R.id.etEditAdminDesc);

        spEditAdminRole = findViewById(R.id.spEditAdminRole);
        spEditAdminAccessLevel = findViewById(R.id.spEditAdminAccessLevel);
        spEditAdminModules = findViewById(R.id.spEditAdminModules);

        rgEditAdminAccountStatus = findViewById(R.id.rgEditAdminAccountStatus);
        rbEditAdminActive = findViewById(R.id.rbEditAdminActive);
        rbEditAdminInactive = findViewById(R.id.rbEditAdminInactive);

        btnUpdateAdmin = findViewById(R.id.btnUpdateAdmin);
        btnCancelEditAdmin = findViewById(R.id.btnCancelEditAdmin);
    }

    private void setupSpinners() {
        String[] roles = {"System Administrator", "Academic Administrator", "Course Administrator", "Report Administrator", "Support Administrator", "Content Admin", "Data Analyst", "User Admin"};
        String[] accessLevels = {"Full Access", "Read & Write", "Restricted Access"};
        String[] managedModules = {"All Modules", "Course Management", "User Management & Analytics"};

        setSpinnerAdapter(spEditAdminRole, roles);
        setSpinnerAdapter(spEditAdminAccessLevel, accessLevels);
        setSpinnerAdapter(spEditAdminModules, managedModules);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void prepopulateFields() {
        if (currentAdmin == null) return;

        String name = currentAdmin.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "A";

        if (tvEditAdminAvatarChar != null) tvEditAdminAvatarChar.setText(initial);
        if (tvEditAdminHeaderName != null) tvEditAdminHeaderName.setText(name);
        if (tvEditAdminHeaderId != null) tvEditAdminHeaderId.setText(currentAdmin.getId());

        boolean isActive = "Active".equalsIgnoreCase(currentAdmin.getStatus());
        if (tvEditAdminHeaderStatus != null) {
            tvEditAdminHeaderStatus.setText(isActive ? "Active" : "Inactive");
            tvEditAdminHeaderStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
            tvEditAdminHeaderStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
        }

        if (etEditAdminName != null) etEditAdminName.setText(name);
        if (etEditAdminId != null) etEditAdminId.setText(currentAdmin.getId());
        if (etEditAdminEmail != null) etEditAdminEmail.setText(currentAdmin.getEmail());
        if (etEditAdminPhone != null) etEditAdminPhone.setText(currentAdmin.getPhone());
        if (etEditAdminDob != null) etEditAdminDob.setText(currentAdmin.getDob());
        if (etEditAdminDesc != null) etEditAdminDesc.setText(currentAdmin.getDescription());

        setSpinnerSelection(spEditAdminRole, currentAdmin.getRole());
        setSpinnerSelection(spEditAdminAccessLevel, currentAdmin.getAccessLevel());
        setSpinnerSelection(spEditAdminModules, currentAdmin.getManagedModules());

        if (isActive) {
            if (rbEditAdminActive != null) rbEditAdminActive.setChecked(true);
        } else {
            if (rbEditAdminInactive != null) rbEditAdminInactive.setChecked(true);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        if (btnCancelEditAdmin != null) {
            btnCancelEditAdmin.setOnClickListener(v -> finish());
        }

        if (btnUpdateAdmin != null) {
            btnUpdateAdmin.setOnClickListener(v -> attemptUpdateAdmin());
        }
    }

    private void attemptUpdateAdmin() {
        if (etEditAdminName == null || etEditAdminEmail == null) return;

        String name = etEditAdminName.getText().toString().trim();
        String email = etEditAdminEmail.getText().toString().trim();
        String phone = etEditAdminPhone != null ? etEditAdminPhone.getText().toString().trim() : "";
        String dob = etEditAdminDob != null ? etEditAdminDob.getText().toString().trim() : "";
        String desc = etEditAdminDesc != null ? etEditAdminDesc.getText().toString().trim() : "";

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilEditAdminName != null) tilEditAdminName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilEditAdminName != null) tilEditAdminName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilEditAdminEmail != null) tilEditAdminEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilEditAdminEmail != null) tilEditAdminEmail.setError(null);
        }

        if (isValid) {
            currentAdmin.setName(name);
            currentAdmin.setEmail(email);
            if (!phone.isEmpty()) currentAdmin.setPhone(phone);
            if (!dob.isEmpty()) currentAdmin.setDob(dob);
            if (!desc.isEmpty()) currentAdmin.setDescription(desc);

            if (spEditAdminRole != null && spEditAdminRole.getSelectedItem() != null) {
                currentAdmin.setRole(spEditAdminRole.getSelectedItem().toString());
            }
            if (spEditAdminAccessLevel != null && spEditAdminAccessLevel.getSelectedItem() != null) {
                currentAdmin.setAccessLevel(spEditAdminAccessLevel.getSelectedItem().toString());
            }
            if (spEditAdminModules != null && spEditAdminModules.getSelectedItem() != null) {
                currentAdmin.setManagedModules(spEditAdminModules.getSelectedItem().toString());
            }

            boolean isCheckedActive = (rbEditAdminActive != null && rbEditAdminActive.isChecked());
            currentAdmin.setStatus(isCheckedActive ? "Active" : "Inactive");

            Toast.makeText(this, "Admin account updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
