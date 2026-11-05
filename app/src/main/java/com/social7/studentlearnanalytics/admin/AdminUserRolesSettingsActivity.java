package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;

public class AdminUserRolesSettingsActivity extends AppCompatActivity {

    private MaterialButton btnAddRole, btnSaveUserRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_roles_settings);

        ImageView btnBack = findViewById(R.id.btnBackUserRoles);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnAddRole = findViewById(R.id.btnAddRole);
        btnSaveUserRoles = findViewById(R.id.btnSaveUserRoles);

        if (btnAddRole != null) {
            btnAddRole.setOnClickListener(v -> showAddRoleDialog());
        }

        if (btnSaveUserRoles != null) {
            btnSaveUserRoles.setOnClickListener(v -> {
                Toast.makeText(this, "Role & permission settings saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void showAddRoleDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        TextInputLayout til = new TextInputLayout(this);
        til.setHint("Role Name (e.g. Department Chair)");
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText etRoleName = new TextInputEditText(til.getContext());
        til.addView(etRoleName);
        layout.addView(til);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create Custom Role")
                .setView(layout)
                .setPositiveButton("Create Role", (dialog, which) -> {
                    String roleName = etRoleName.getText() != null ? etRoleName.getText().toString().trim() : "";
                    if (!roleName.isEmpty()) {
                        Toast.makeText(this, "Role " + roleName + " created successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
