package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.R;

public class AdminAboutSystemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_about_system);

        ImageView btnBack = findViewById(R.id.btnBackAbout);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
