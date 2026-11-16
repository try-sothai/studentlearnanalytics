package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.localization.AppLanguageManager;
import com.social7.studentlearnanalytics.localization.LanguageConstants;
import com.social7.studentlearnanalytics.localization.LanguagePreferences;

public class AdminAppearanceActivity extends AppCompatActivity {

    private RadioGroup rgThemeMode;
    private RadioButton rbThemeLight, rbThemeDark, rbThemeSystem;
    private Spinner spLanguageSelector, spFontSize;
    private MaterialButton btnSaveAppearance;
    private LanguagePreferences languagePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appearance_settings);

        languagePreferences = new LanguagePreferences(this);

        ImageView btnBack = findViewById(R.id.btnBackAppearance);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rgThemeMode = findViewById(R.id.rgThemeMode);
        rbThemeLight = findViewById(R.id.rbThemeLight);
        rbThemeDark = findViewById(R.id.rbThemeDark);
        rbThemeSystem = findViewById(R.id.rbThemeSystem);
        spLanguageSelector = findViewById(R.id.spLanguageSelector);
        spFontSize = findViewById(R.id.spFontSize);
        btnSaveAppearance = findViewById(R.id.btnSaveAppearance);

        setupSpinners();
        restoreCurrentPreferences();

        if (btnSaveAppearance != null) {
            btnSaveAppearance.setOnClickListener(v -> savePreferences());
        }
    }

    private void setupSpinners() {
        String[] languages = {
                getString(R.string.lang_english_us),
                getString(R.string.lang_khmer_kh)
        };
        String[] fontSizes = {
                getString(R.string.font_size_standard),
                getString(R.string.font_size_medium),
                getString(R.string.font_size_large)
        };

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

    private void restoreCurrentPreferences() {
        String theme = languagePreferences.getThemeMode();
        if (LanguageConstants.THEME_DARK.equals(theme) && rbThemeDark != null) {
            rbThemeDark.setChecked(true);
        } else if (LanguageConstants.THEME_SYSTEM.equals(theme) && rbThemeSystem != null) {
            rbThemeSystem.setChecked(true);
        } else if (rbThemeLight != null) {
            rbThemeLight.setChecked(true);
        }

        if (spLanguageSelector != null) {
            String currentTag = AppLanguageManager.getCurrentLanguageTag(this);
            spLanguageSelector.setSelection(LanguageConstants.LANG_KHMER.equals(currentTag) ? 1 : 0);
        }

        if (spFontSize != null) {
            spFontSize.setSelection(languagePreferences.getFontSizeIndex());
        }
    }

    private void savePreferences() {
        String themeMode = LanguageConstants.THEME_LIGHT;
        if (rgThemeMode != null) {
            int checkedId = rgThemeMode.getCheckedRadioButtonId();
            if (checkedId == R.id.rbThemeDark) {
                themeMode = LanguageConstants.THEME_DARK;
            } else if (checkedId == R.id.rbThemeSystem) {
                themeMode = LanguageConstants.THEME_SYSTEM;
            }
        }

        String languageTag = LanguageConstants.LANG_ENGLISH;
        if (spLanguageSelector != null && spLanguageSelector.getSelectedItemPosition() == 1) {
            languageTag = LanguageConstants.LANG_KHMER;
        }

        int fontIndex = spFontSize != null ? spFontSize.getSelectedItemPosition() : LanguageConstants.FONT_STANDARD;
        languagePreferences.setFontSizeIndex(fontIndex);

        AppLanguageManager.setThemeMode(this, themeMode);
        AppLanguageManager.setLanguage(this, languageTag);

        Toast.makeText(this, getString(R.string.appearance_saved_success), Toast.LENGTH_SHORT).show();
        finish();
    }
}
