package com.social7.studentlearnanalytics.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Notification;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.RoleGuard;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationDetailActivity extends AppCompatActivity {

    private Notification currentNotification;

    private TextView tvDetailCategoryBadge, tvDetailDateTime, tvDetailTitle, tvDetailMessage;
    private TextView tvValUserName, tvValUserRole, tvValUserStatusBadge;
    private TextView tvValUserEmail, tvValUserPhone, tvValUserDept, tvValUserRegisteredAt;
    private MaterialButton btnNotifApprove, btnNotifReject, btnNotifViewFullProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RoleGuard.checkAccess(this, "Admin")) return;
        setContentView(R.layout.activity_notification_detail);

        if (getIntent() != null && getIntent().hasExtra("notification")) {
            Object extra = getIntent().getSerializableExtra("notification");
            if (extra instanceof Notification) {
                currentNotification = (Notification) extra;
            }
        }

        if (currentNotification == null) {
            Toast.makeText(this, "Notification details are unavailable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
        if (currentNotification == null) return;

        String title = currentNotification.getTitle() != null ? currentNotification.getTitle() : "Notification";
        String message = currentNotification.getMessage() != null ? currentNotification.getMessage() : "";
        String category = currentNotification.getCategory() != null ? currentNotification.getCategory() : "System";
        String timeAgo = currentNotification.getTimeAgo() != null ? currentNotification.getTimeAgo() : "Recent";
        String dateGroup = currentNotification.getDateGroup();
        String id = currentNotification.getId() != null ? currentNotification.getId() : "—";

        if (tvDetailTitle != null) tvDetailTitle.setText(title);
        if (tvDetailMessage != null) tvDetailMessage.setText(message);
        if (tvDetailDateTime != null) tvDetailDateTime.setText(timeAgo);
        if (tvDetailCategoryBadge != null) tvDetailCategoryBadge.setText(category);

        if (tvValUserName != null) tvValUserName.setText(title);
        if (tvValUserRole != null) tvValUserRole.setText(category + " • " + dateGroup);
        if (tvValUserEmail != null) tvValUserEmail.setText(id.isEmpty() ? "—" : id);
        if (tvValUserPhone != null) tvValUserPhone.setText(category);
        if (tvValUserDept != null) tvValUserDept.setText(dateGroup);
        if (tvValUserRegisteredAt != null) tvValUserRegisteredAt.setText(timeAgo);

        updateReadStateUi();

        if (btnNotifApprove != null) {
            btnNotifApprove.setText("Mark as Read");
        }
        if (btnNotifReject != null) {
            btnNotifReject.setText(R.string.delete);
        }
        if (btnNotifViewFullProfile != null) {
            btnNotifViewFullProfile.setVisibility(View.GONE);
        }
    }

    private void updateReadStateUi() {
        boolean read = currentNotification != null && currentNotification.isRead();
        if (tvValUserStatusBadge != null) {
            tvValUserStatusBadge.setText(read ? "Read" : "Unread");
            tvValUserStatusBadge.setBackgroundResource(
                    read ? R.drawable.bg_notif_badge_approved : R.drawable.bg_notif_badge_pending
            );
            tvValUserStatusBadge.setTextColor(Color.parseColor(read ? "#08C985" : "#FF9C32"));
        }
        if (btnNotifApprove != null) {
            btnNotifApprove.setVisibility(read ? View.GONE : View.VISIBLE);
        }
    }

    private void setupListeners() {
        if (btnNotifApprove != null) {
            btnNotifApprove.setOnClickListener(v -> markAsRead());
        }

        if (btnNotifReject != null) {
            btnNotifReject.setOnClickListener(v -> confirmDeleteNotification());
        }
    }

    private void markAsRead() {
        if (currentNotification == null || currentNotification.getId() == null || currentNotification.getId().trim().isEmpty()) {
            return;
        }
        btnNotifApprove.setEnabled(false);
        ApiClient.getApiService(this).markNotificationRead(currentNotification.getId())
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        btnNotifApprove.setEnabled(true);
                        if (response.isSuccessful()) {
                            currentNotification.setRead(true);
                            updateReadStateUi();
                            setResult(RESULT_OK);
                            Toast.makeText(NotificationDetailActivity.this, "Notification marked as read.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NotificationDetailActivity.this, "Failed to update notification.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        btnNotifApprove.setEnabled(true);
                        Toast.makeText(NotificationDetailActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteNotification() {
        if (currentNotification == null || currentNotification.getId() == null || currentNotification.getId().trim().isEmpty()) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Notification?")
                .setMessage("Are you sure you want to permanently remove this notification?")
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteNotificationFromApi())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteNotificationFromApi() {
        if (btnNotifReject != null) btnNotifReject.setEnabled(false);
        ApiClient.getApiService(this).deleteNotification(currentNotification.getId())
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful()) {
                            setResult(RESULT_OK);
                            Toast.makeText(NotificationDetailActivity.this, "Notification deleted.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            if (btnNotifReject != null) btnNotifReject.setEnabled(true);
                            Toast.makeText(NotificationDetailActivity.this, "Failed to delete notification.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        if (btnNotifReject != null) btnNotifReject.setEnabled(true);
                        Toast.makeText(NotificationDetailActivity.this, R.string.network_error_server, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
