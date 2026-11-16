package com.social7.studentlearnanalytics.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.admin.teachers.AdminTeacherDetailActivity;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.model.Teacher;

public class NotificationDetailActivity extends AppCompatActivity {

    private Notification currentNotification;

    private ImageView imgDetailCategoryIcon;
    private TextView tvDetailCategoryBadge, tvDetailDateTime, tvDetailTitle, tvDetailMessage;
    private TextView tvValUserName, tvValUserRole, tvValUserStatusBadge;
    private TextView tvValUserEmail, tvValUserPhone, tvValUserDept, tvValUserRegisteredAt;
    private MaterialButton btnNotifApprove, btnNotifReject, btnNotifViewFullProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        if (getIntent() != null && getIntent().hasExtra("notification")) {
            currentNotification = (Notification) getIntent().getSerializableExtra("notification");
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackNotificationDetail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        imgDetailCategoryIcon = findViewById(R.id.imgDetailCategoryIcon);
        tvDetailCategoryBadge = findViewById(R.id.tvDetailCategoryBadge);
        tvDetailDateTime = findViewById(R.id.tvDetailDateTime);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailMessage = findViewById(R.id.tvDetailMessage);

        tvValUserName = findViewById(R.id.tvValUserName);
        tvValUserRole = findViewById(R.id.tvValUserRole);
        tvValUserStatusBadge = findViewById(R.id.tvValUserStatusBadge);
        tvValUserEmail = findViewById(R.id.tvValUserEmail);
        tvValUserPhone = findViewById(R.id.tvValUserPhone);
        tvValUserDept = findViewById(R.id.tvValUserDept);
        tvValUserRegisteredAt = findViewById(R.id.tvValUserRegisteredAt);

        btnNotifApprove = findViewById(R.id.btnNotifApprove);
        btnNotifReject = findViewById(R.id.btnNotifReject);
        btnNotifViewFullProfile = findViewById(R.id.btnNotifViewFullProfile);
    }

    private void bindData() {
        if (currentNotification != null) {
            if (tvDetailTitle != null) tvDetailTitle.setText(currentNotification.getTitle());
            if (tvDetailMessage != null) tvDetailMessage.setText(currentNotification.getMessage());
            if (tvDetailDateTime != null) tvDetailDateTime.setText("Oct 28, 2024 " + currentNotification.getTimeAgo());
            if (tvDetailCategoryBadge != null) tvDetailCategoryBadge.setText(currentNotification.getCategory());
        }
    }

    private void setupListeners() {
        if (btnNotifApprove != null) {
            btnNotifApprove.setOnClickListener(v -> showApproveConfirmationDialog());
        }

        if (btnNotifReject != null) {
            btnNotifReject.setOnClickListener(v -> showRejectConfirmationDialog());
        }

        if (btnNotifViewFullProfile != null) {
            btnNotifViewFullProfile.setOnClickListener(v -> {
                Teacher teacher = new Teacher("TCH001", "Ms. Dany", "dany@teacher.edu", "Computer Science", "Lecturer", "Pending");
                Intent intent = new Intent(NotificationDetailActivity.this, AdminTeacherDetailActivity.class);
                intent.putExtra("teacher", teacher);
                startActivity(intent);
            });
        }
    }

    private void showApproveConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Approve Registration?")
                .setMessage("Are you sure you want to approve Ms. Dany's teacher registration?")
                .setPositiveButton(R.string.approve, (dialog, which) -> {
                    if (tvValUserStatusBadge != null) {
                        tvValUserStatusBadge.setText(R.string.status_approved);
                        tvValUserStatusBadge.setBackgroundResource(R.drawable.bg_notif_badge_approved);
                        tvValUserStatusBadge.setTextColor(Color.parseColor("#08C985"));
                    }
                    if (btnNotifApprove != null) btnNotifApprove.setEnabled(false);
                    if (btnNotifReject != null) btnNotifReject.setEnabled(false);
                    Toast.makeText(this, "Ms. Dany's teacher registration approved successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRejectConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reject Registration?")
                .setMessage("Are you sure you want to reject Ms. Dany's teacher registration?")
                .setPositiveButton(R.string.reject, (dialog, which) -> {
                    if (tvValUserStatusBadge != null) {
                        tvValUserStatusBadge.setText(R.string.status_rejected);
                        tvValUserStatusBadge.setBackgroundResource(R.drawable.bg_notif_badge_rejected);
                        tvValUserStatusBadge.setTextColor(Color.parseColor("#F04444"));
                    }
                    if (btnNotifApprove != null) btnNotifApprove.setEnabled(false);
                    if (btnNotifReject != null) btnNotifReject.setEnabled(false);
                    Toast.makeText(this, "Ms. Dany's teacher registration rejected.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
