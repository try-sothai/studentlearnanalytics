package com.social7.studentlearnanalytics.admin.classes;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Full-screen Add / Edit Building form (Building Management -> Building -> Floor -> Class).
 */
public class AddBuildingActivity extends AppCompatActivity {

    public static final String EXTRA_BUILDING_JSON = "extra_building_json";

    private TextInputEditText etCode, etName, etDescription, etTotalFloors;
    private AutoCompleteTextView actvStatus;
    private MaterialButton btnSave;
    private TextView tvTitle, tvSubtitle;
    private String editBuildingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building);
        bindViews();
        populateEditIfPresent();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnAddBuildingBack);
        tvTitle = findViewById(R.id.tvAddBuildingTitle);
        tvSubtitle = findViewById(R.id.tvAddBuildingSubtitle);
        etCode = findViewById(R.id.etBuildingCode);
        etName = findViewById(R.id.etBuildingName);
        etDescription = findViewById(R.id.etBuildingDescription);
        etTotalFloors = findViewById(R.id.etBuildingFloorsCount);
        actvStatus = findViewById(R.id.actvBuildingStatus);
        MaterialButton btnCancel = findViewById(R.id.btnAddBuildingCancel);
        btnSave = findViewById(R.id.btnAddBuildingSave);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());

        String[] statuses = {"Active", "Inactive"};
        actvStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
        actvStatus.setText("Active", false);

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void populateEditIfPresent() {
        String raw = getIntent().getStringExtra(EXTRA_BUILDING_JSON);
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            editBuildingId = UiUtils.optString(obj, "id", "");
            if (editBuildingId.isEmpty()) editBuildingId = null;
            if (editBuildingId != null) {
                if (tvTitle != null) tvTitle.setText(R.string.edit_building_title);
                if (tvSubtitle != null) tvSubtitle.setText(R.string.edit_building_subtitle);
            }
            if (etName != null) etName.setText(UiUtils.optString(obj, "name", ""));
            if (etCode != null) etCode.setText(UiUtils.optString(obj, "code", ""));
            if (etDescription != null) etDescription.setText(UiUtils.optString(obj, "description", ""));
            if (etTotalFloors != null) {
                int fc = UiUtils.optInt(obj, "floors_count", 0);
                etTotalFloors.setText(String.valueOf(Math.max(1, fc)));
                etTotalFloors.setEnabled(false);
            }
            String st = UiUtils.optString(obj, "status", "active");
            if (actvStatus != null) {
                actvStatus.setText("inactive".equalsIgnoreCase(st) ? "Inactive" : "Active", false);
            }
        } catch (Exception ignored) {
        }
    }

    private void validateAndSave() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String floorsStr = etTotalFloors.getText() != null ? etTotalFloors.getText().toString().trim() : "4";
        String status = actvStatus.getText() != null && "Inactive".equalsIgnoreCase(actvStatus.getText().toString())
                ? "inactive" : "active";

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.err_building_required, Toast.LENGTH_LONG).show();
            return;
        }

        int totalFloors = 0;
        try {
            totalFloors = Integer.parseInt(floorsStr);
        } catch (Exception ignored) {
        }

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        if (!code.isEmpty()) body.addProperty("code", code.toUpperCase());
        if (!desc.isEmpty()) body.addProperty("description", desc);
        body.addProperty("status", status);
        if (editBuildingId == null && totalFloors > 0) {
            body.addProperty("total_floors", Math.min(totalFloors, 50));
        }

        btnSave.setEnabled(false);
        Call<JsonObject> call = editBuildingId == null
                ? ApiClient.getApiService(this).createBuilding(body)
                : ApiClient.getApiService(this).updateBuilding(editBuildingId, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddBuildingActivity.this,
                            editBuildingId == null ? "Building " + name + " created!" : "Building updated!",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddBuildingActivity.this, R.string.err_building_duplicate, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddBuildingActivity.this, R.string.network_error_server, Toast.LENGTH_LONG).show();
            }
        });
    }
}
