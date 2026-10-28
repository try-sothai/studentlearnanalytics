package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.auth.LoginActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "SLAS_PROFILE";

    private SessionManager sessionManager;
    private TextView tvStudentName;
    private TextView tvStudentId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.activity_fragment_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentId = view.findViewById(R.id.tvStudentId);

        if (tvStudentName != null && sessionManager.getStudentName() != null) {
            tvStudentName.setText(sessionManager.getStudentName());
        }

        if (tvStudentId != null && sessionManager.getUserId() != null) {
            tvStudentId.setText("ID: " + sessionManager.getUserId());
        }

        View btnBack = view.findViewById(R.id.btnBack);
        View cardPersonalInfo = view.findViewById(R.id.cardPersonalInfo);
        View cardAcademicInfo = view.findViewById(R.id.cardAcademicInfo);
        View cardChangePassword = view.findViewById(R.id.cardChangePassword);
        View cardAppSettings = view.findViewById(R.id.cardAppSettings);
        View cardHelpSupport = view.findViewById(R.id.cardHelpSupport);
        View cardLogout = view.findViewById(R.id.cardLogout);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        if (cardPersonalInfo != null) {
            cardPersonalInfo.setOnClickListener(v -> {
                String name = (tvStudentName != null && tvStudentName.getText() != null) ? tvStudentName.getText().toString() : sessionManager.getStudentName();
                String id = sessionManager.getUserId();
                String email = !profileEmail.isEmpty() ? profileEmail : "Authenticated Student";
                String phone = !profilePhone.isEmpty() ? profilePhone : "Not provided";
                String dept = !profileDepartment.isEmpty() ? profileDepartment : "Computer Science";
                String year = !profileYearLevel.isEmpty() ? profileYearLevel + " Year" : "3rd Year";

                showInfoDialog("Personal Information",
                        "• Full Name: " + name + "\n" +
                        "• Student ID: " + id + "\n" +
                        "• Email: " + email + "\n" +
                        "• Phone: " + phone + "\n" +
                        "• Department: " + dept + "\n" +
                        "• Year Level: " + year);
            });
        }

        if (cardAcademicInfo != null) {
            cardAcademicInfo.setOnClickListener(v -> {
                String dept = !profileDepartment.isEmpty() ? profileDepartment : "Computer Science";
                showInfoDialog("Academic Information",
                        "• Department: " + dept + "\n" +
                        "• Enrollment Status: Active Student");
            });
        }

        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (cardAppSettings != null) {
            cardAppSettings.setOnClickListener(v -> showInfoDialog("App Settings",
                    "• Language: English (Default)\n" +
                    "• Theme: SLAS Purple Modern\n" +
                    "• Push Notifications: Enabled\n" +
                    "• Version: 1.0.0 (SLAS Build 2026)"));
        }

        if (cardHelpSupport != null) {
            cardHelpSupport.setOnClickListener(v -> showInfoDialog("Help & Support",
                    "• Email: support@slas.edu.kh\n" +
                    "• Helpline: +855 23 888 999\n" +
                    "• Office: Academic Block A, Room 102\n" +
                    "• Working Hours: Mon - Fri (8:00 AM - 5:00 PM)"));
        }

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        fetchProfile();
    }

    private String profileEmail = "";
    private String profilePhone = "";
    private String profileDepartment = "";
    private String profileYearLevel = "";

    private void fetchProfile() {
        if (getContext() == null) return;

        ApiClient.getApiService(requireContext()).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject root = response.body();
                        JsonObject data = root.has("data") && root.get("data").isJsonObject()
                                ? root.getAsJsonObject("data") : root;

                        String name = "";
                        String code = "";

                        if (data.has("name") && !data.get("name").isJsonNull()) {
                            name = data.get("name").getAsString();
                        }
                        if (data.has("email") && !data.get("email").isJsonNull()) {
                            profileEmail = data.get("email").getAsString();
                        }

                        if (data.has("student") && data.get("student").isJsonObject()) {
                            JsonObject studentObj = data.getAsJsonObject("student");
                            if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                                code = studentObj.get("student_code").getAsString();
                            } else if (studentObj.has("student_id") && !studentObj.get("student_id").isJsonNull()) {
                                code = studentObj.get("student_id").getAsString();
                            }
                            if (studentObj.has("phone") && !studentObj.get("phone").isJsonNull()) {
                                profilePhone = studentObj.get("phone").getAsString();
                            }
                            if (studentObj.has("year_level") && !studentObj.get("year_level").isJsonNull()) {
                                profileYearLevel = studentObj.get("year_level").getAsString();
                            }
                            if (studentObj.has("department") && studentObj.get("department").isJsonObject()) {
                                JsonObject dept = studentObj.getAsJsonObject("department");
                                if (dept.has("name") && !dept.get("name").isJsonNull()) {
                                    profileDepartment = dept.get("name").getAsString();
                                }
                            }
                        }

                        if (name.isEmpty() && sessionManager != null) {
                            name = sessionManager.getStudentName();
                        }
                        if (code.isEmpty() && sessionManager != null) {
                            code = sessionManager.getUserId();
                        }

                        if (tvStudentName != null && !name.isEmpty()) {
                            tvStudentName.setText(name);
                        }
                        if (tvStudentId != null && !code.isEmpty()) {
                            tvStudentId.setText("ID: " + code);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile data", e);
                    }
                } else {
                    Log.d(TAG, "Profile HTTP code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.d(TAG, "Profile API call failed: " + t.getMessage());
            }
        });
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
