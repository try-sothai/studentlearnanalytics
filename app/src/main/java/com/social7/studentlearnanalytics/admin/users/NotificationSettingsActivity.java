package com.social7.studentlearnanalytics.admin.users;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.social7.studentlearnanalytics.R;

import java.util.Calendar;
import java.util.Locale;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchMaterial swPushSystemAnnouncements, swPushNewUser, swPushCourseUpdates, swPushReportGen;
    private SwitchMaterial swEmailSystemAnnounce, swEmailWeeklyReports, swEmailSecurityAlerts, swEmailNewUser, swEmailCourseUpdates;
    private SwitchMaterial swQuietHours;
    private LinearLayout containerQuietHoursTime;
    private TextView tvQuietHoursStart, tvQuietHoursEnd;
    private MaterialButton btnSaveNotifSettings, btnCancelNotifSettings;

    private int startHour = 22, startMinute = 0;
    private int endHour = 7, endMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        initViews();
        loadSavedPreferences();
        setupListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackNotificationSettings);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        swPushSystemAnnouncements = findViewById(R.id.swPushSystemAnnouncements);
        swPushNewUser = findViewById(R.id.swPushNewUser);
        swPushCourseUpdates = findViewById(R.id.swPushCourseUpdates);
        swPushReportGen = findViewById(R.id.swPushReportGen);

        swEmailSystemAnnounce = findViewById(R.id.swEmailSystemAnnounce);
        swEmailWeeklyReports = findViewById(R.id.swEmailWeeklyReports);
        swEmailSecurityAlerts = findViewById(R.id.swEmailSecurityAlerts);
        swEmailNewUser = findViewById(R.id.swEmailNewUser);
        swEmailCourseUpdates = findViewById(R.id.swEmailCourseUpdates);

        swQuietHours = findViewById(R.id.swQuietHours);
        containerQuietHoursTime = findViewById(R.id.containerQuietHoursTime);
        tvQuietHoursStart = findViewById(R.id.tvQuietHoursStart);
        tvQuietHoursEnd = findViewById(R.id.tvQuietHoursEnd);

        btnSaveNotifSettings = findViewById(R.id.btnSaveNotifSettings);
        btnCancelNotifSettings = findViewById(R.id.btnCancelNotifSettings);
    }

    private void loadSavedPreferences() {
        SharedPreferences pref = getSharedPreferences("SLAS_NotifSettings", MODE_PRIVATE);

        if (swPushSystemAnnouncements != null) swPushSystemAnnouncements.setChecked(pref.getBoolean("push_sys", true));
        if (swPushNewUser != null) swPushNewUser.setChecked(pref.getBoolean("push_user", true));
        if (swPushCourseUpdates != null) swPushCourseUpdates.setChecked(pref.getBoolean("push_course", true));
        if (swPushReportGen != null) swPushReportGen.setChecked(pref.getBoolean("push_report", true));

        if (swEmailSystemAnnounce != null) swEmailSystemAnnounce.setChecked(pref.getBoolean("email_sys", true));
        if (swEmailWeeklyReports != null) swEmailWeeklyReports.setChecked(pref.getBoolean("email_weekly", true));
        if (swEmailSecurityAlerts != null) swEmailSecurityAlerts.setChecked(pref.getBoolean("email_sec", true));
        if (swEmailNewUser != null) swEmailNewUser.setChecked(pref.getBoolean("email_user", false));
        if (swEmailCourseUpdates != null) swEmailCourseUpdates.setChecked(pref.getBoolean("email_course", true));

        boolean quietHours = pref.getBoolean("quiet_hours", false);
        if (swQuietHours != null) swQuietHours.setChecked(quietHours);
        if (containerQuietHoursTime != null) containerQuietHoursTime.setVisibility(quietHours ? View.VISIBLE : View.GONE);

        String startTime = pref.getString("quiet_start", "10:00 PM");
        String endTime = pref.getString("quiet_end", "07:00 AM");

        if (tvQuietHoursStart != null) tvQuietHoursStart.setText(startTime);
        if (tvQuietHoursEnd != null) tvQuietHoursEnd.setText(endTime);
    }

    private void setupListeners() {
        if (swQuietHours != null) {
            swQuietHours.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (containerQuietHoursTime != null) {
                    containerQuietHoursTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
        }

        if (tvQuietHoursStart != null) {
            tvQuietHoursStart.setOnClickListener(v -> showTimePicker(true));
        }

        if (tvQuietHoursEnd != null) {
            tvQuietHoursEnd.setOnClickListener(v -> showTimePicker(false));
        }

        if (btnCancelNotifSettings != null) {
            btnCancelNotifSettings.setOnClickListener(v -> finish());
        }

        if (btnSaveNotifSettings != null) {
            btnSaveNotifSettings.setOnClickListener(v -> savePreferences());
        }
    }

    private void showTimePicker(final boolean isStart) {
        int initialHour = isStart ? startHour : endHour;
        int initialMinute = isStart ? startMinute : endMinute;

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (isStart) {
                startHour = hourOfDay;
                startMinute = minute;
                if (tvQuietHoursStart != null) tvQuietHoursStart.setText(formatTime(hourOfDay, minute));
            } else {
                endHour = hourOfDay;
                endMinute = minute;
                if (tvQuietHoursEnd != null) tvQuietHoursEnd.setText(formatTime(hourOfDay, minute));
            }
        }, initialHour, initialMinute, false);

        timePicker.show();
    }

    private String formatTime(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int formattedHour = hour % 12;
        if (formattedHour == 0) formattedHour = 12;
        return String.format(Locale.US, "%02d:%02d %s", formattedHour, minute, amPm);
    }

    private void savePreferences() {
        SharedPreferences pref = getSharedPreferences("SLAS_NotifSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if (swPushSystemAnnouncements != null) editor.putBoolean("push_sys", swPushSystemAnnouncements.isChecked());
        if (swPushNewUser != null) editor.putBoolean("push_user", swPushNewUser.isChecked());
        if (swPushCourseUpdates != null) editor.putBoolean("push_course", swPushCourseUpdates.isChecked());
        if (swPushReportGen != null) editor.putBoolean("push_report", swPushReportGen.isChecked());

        if (swEmailSystemAnnounce != null) editor.putBoolean("email_sys", swEmailSystemAnnounce.isChecked());
        if (swEmailWeeklyReports != null) editor.putBoolean("email_weekly", swEmailWeeklyReports.isChecked());
        if (swEmailSecurityAlerts != null) editor.putBoolean("email_sec", swEmailSecurityAlerts.isChecked());
        if (swEmailNewUser != null) editor.putBoolean("email_user", swEmailNewUser.isChecked());
        if (swEmailCourseUpdates != null) editor.putBoolean("email_course", swEmailCourseUpdates.isChecked());

        boolean quietHours = swQuietHours != null && swQuietHours.isChecked();
        editor.putBoolean("quiet_hours", quietHours);

        if (tvQuietHoursStart != null) editor.putString("quiet_start", tvQuietHoursStart.getText().toString());
        if (tvQuietHoursEnd != null) editor.putString("quiet_end", tvQuietHoursEnd.getText().toString());

        editor.apply();

        Toast.makeText(this, "Notification preferences saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
