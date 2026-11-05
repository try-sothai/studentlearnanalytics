package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

public class AdminAppearanceActivity extends AppCompatActivity {

    private Spinner spLanguageSelector, spFontSize;
    private MaterialButton btnSaveAppearance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appearance_settings);

        ImageView btnBack = findViewById(R.id.btnBackAppearance);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spLanguageSelector = findViewById(R.id.spLanguageSelector);
        spFontSize = findViewById(R.id.spFontSize);
        btnSaveAppearance = findViewById(R.id.btnSaveAppearance);

        setupSpinners();

        if (btnSaveAppearance != null) {
            btnSaveAppearance.setOnClickListener(v -> {
                Toast.makeText(this, "Appearance preferences saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setupSpinners() {
        String[] languages = {"English (United States)", "Khmer (Cambodia)"};
        String[] fontSizes = {"Standard (Default)", "Medium (+10%)", "Large (+20%)"};

        if (spLanguageSelector != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spLanguageSelector.setAdapter(adapter);
        }

        if (spFontSize != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontSizes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spFontSize.setAdapter(adapter);
        }
    }
}
