package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

public class AdminSystemConfigActivity extends AppCompatActivity {

    private Spinner spDefaultYear, spDefaultSemester;
    private MaterialButton btnSaveSysConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_config_settings);

        ImageView btnBack = findViewById(R.id.btnBackSystemConfig);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spDefaultYear = findViewById(R.id.spDefaultYear);
        spDefaultSemester = findViewById(R.id.spDefaultSemester);
        btnSaveSysConfig = findViewById(R.id.btnSaveSysConfig);

        setupSpinners();

        if (btnSaveSysConfig != null) {
            btnSaveSysConfig.setOnClickListener(v -> {
                Toast.makeText(this, "System configuration saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setupSpinners() {
        String[] years = {"AY 2024-2025", "AY 2023-2024", "AY 2022-2023"};
        String[] semesters = {"Semester 1", "Semester 2", "Summer Term"};

        if (spDefaultYear != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDefaultYear.setAdapter(adapter);
        }

        if (spDefaultSemester != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDefaultSemester.setAdapter(adapter);
        }
    }
}
