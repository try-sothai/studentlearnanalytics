package com.social7.studentlearnanalytics.admin.users;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.social7.studentlearnanalytics.utils.RoleGuard;

public class AddUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        Intent intent = new Intent(this, AddStudentActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
    }
}
