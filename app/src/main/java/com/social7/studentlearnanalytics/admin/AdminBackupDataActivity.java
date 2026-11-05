package com.social7.studentlearnanalytics.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

public class AdminBackupDataActivity extends AppCompatActivity {

    private MaterialButton btnCreateBackup;
    private TextView btnDownloadBackup1, btnDownloadBackup2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_backup_data_settings);

        ImageView btnBack = findViewById(R.id.btnBackBackup);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnCreateBackup = findViewById(R.id.btnCreateBackup);
        btnDownloadBackup1 = findViewById(R.id.btnDownloadBackup1);
        btnDownloadBackup2 = findViewById(R.id.btnDownloadBackup2);

        if (btnCreateBackup != null) {
            btnCreateBackup.setOnClickListener(v ->
                    Toast.makeText(this, "Database backup initiated successfully!", Toast.LENGTH_SHORT).show()
            );
        }

        View.OnClickListener downloadListener = v ->
                Toast.makeText(this, "Downloading SQL database archive...", Toast.LENGTH_SHORT).show();

        if (btnDownloadBackup1 != null) btnDownloadBackup1.setOnClickListener(downloadListener);
        if (btnDownloadBackup2 != null) btnDownloadBackup2.setOnClickListener(downloadListener);
    }
}
