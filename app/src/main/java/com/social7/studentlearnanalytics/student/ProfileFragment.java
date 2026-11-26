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
    private TextView tvDepartment;
    private TextView tvStudentYearLevel;
    private TextView tvAcademicYear;

    private String profileEmail = "";
    private String profilePhone = "";
    private String profileDepartment = "";
    private String profileMajor = "";
    private String profileClassName = "";
    private String profileYearLevel = "";
    private String profileAcademicYear = "";
    private String profileEnrollmentStatus = "Active Student";

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
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvStudentYearLevel = view.findViewById(R.id.tvStudentYearLevel);
        tvAcademicYear = view.findViewById(R.id.tvAcademicYear);

        if (tvStudentName != null && sessionManager.getStudentName() != null) {
            tvStudentName.setText(sessionManager.getStudentName());
        }

        if (tvStudentId != null && sessionManager.getUserId() != null) {
            tvStudentId.setText(tvStudentId.getContext().getString(R.string.id) + sessionManager.getUserId());
        }

        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.my_profile), getString(R.string.manage_your_personal_and_academic_informat)
            );
        }
        View cardPersonalInfo = view.findViewById(R.id.cardPersonalInfo);
        View cardAcademicInfo = view.findViewById(R.id.cardAcademicInfo);
        View cardChangePassword = view.findViewById(R.id.cardChangePassword);
        View cardAppSettings = view.findViewById(R.id.cardAppSettings);
        View cardHelpSupport = view.findViewById(R.id.cardHelpSupport);
        View cardLogout = view.findViewById(R.id.cardLogout);

        if (cardPersonalInfo != null) {
            cardPersonalInfo.setOnClickListener(v -> {
                String name = (tvStudentName != null && tvStudentName.getText() != null) ? tvStudentName.getText().toString() : sessionManager.getStudentName();
                String id = (tvStudentId != null && tvStudentId.getText() != null) ? tvStudentId.getText().toString().replace("ID: ", "") : sessionManager.getUserId();
                String email = !profileEmail.isEmpty() ? profileEmail : "Not provided";
                String phone = !profilePhone.isEmpty() ? profilePhone : "Not provided";
                String dept = !profileDepartment.isEmpty() ? profileDepartment : "Not assigned";
                String year = !profileYearLevel.isEmpty() ? profileYearLevel : getString(R.string.year_level_unassigned);

                showInfoDialog("Personal Information",
                        "• Full Name: " + name + "\n" +
                        "• Student ID / Username: " + id + "\n" +
                        "• Email: " + email + "\n" +
                        "• Phone: " + phone + "\n" +
                        "• Faculty: " + dept + "\n" +
                        "• " + getString(R.string.student_year_level) + ": " + year);
            });
        }

        if (cardAcademicInfo != null) {
            cardAcademicInfo.setOnClickListener(v -> {
                String dept = !profileDepartment.isEmpty() ? profileDepartment : "Not assigned";
                String major = !profileMajor.isEmpty() ? profileMajor : dept;
                String cls = !profileClassName.isEmpty() ? profileClassName : "Standard Section";
                String year = !profileYearLevel.isEmpty() ? profileYearLevel : getString(R.string.year_level_unassigned);
                String ay = !profileAcademicYear.isEmpty() ? profileAcademicYear : "N/A";
                showInfoDialog("Academic Information",
                        "• Faculty: " + dept + "\n" +
                        "• Major: " + major + "\n" +
                        "• Class / Section: " + cls + "\n" +
                        "• " + getString(R.string.student_year_level) + ": " + year + "\n" +
                        "• Academic Year: " + ay + "\n" +
                        "• Enrollment Status: " + profileEnrollmentStatus);
            });
        }

        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (cardAppSettings != null) {
            cardAppSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.social7.studentlearnanalytics.admin.AdminAppearanceActivity.class);
                startActivity(intent);
            });
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

                        if (root.has("user") && root.get("user").isJsonObject()) {
                            JsonObject u = root.getAsJsonObject("user");
                            if (u.has("name") && !u.get("name").isJsonNull()) name = u.get("name").getAsString();
                            if (u.has("email") && !u.get("email").isJsonNull()) profileEmail = u.get("email").getAsString();
                            if (u.has("username") && !u.get("username").isJsonNull()) code = u.get("username").getAsString();
                        }

                        if (name.isEmpty() && data.has("name") && !data.get("name").isJsonNull()) {
                            name = data.get("name").getAsString();
                        }
                        if (profileEmail.isEmpty() && data.has("email") && !data.get("email").isJsonNull()) {
                            profileEmail = data.get("email").getAsString();
                        }

                        JsonObject studentObj = null;
                        if (data.has("student") && data.get("student").isJsonObject()) {
                            studentObj = data.getAsJsonObject("student");
                        } else if (root.has("profile") && root.get("profile").isJsonObject()) {
                            JsonObject profWrapper = root.getAsJsonObject("profile");
                            if (profWrapper.has("profile") && profWrapper.get("profile").isJsonObject()) {
                                studentObj = profWrapper.getAsJsonObject("profile");
                            }
                        }

                        if (studentObj != null) {
                            if (studentObj.has("student_code") && !studentObj.get("student_code").isJsonNull()) {
                                code = studentObj.get("student_code").getAsString();
                            } else if (studentObj.has("student_id") && !studentObj.get("student_id").isJsonNull()) {
                                code = studentObj.get("student_id").getAsString();
                            }
                            if (studentObj.has("phone") && !studentObj.get("phone").isJsonNull()) {
                                profilePhone = studentObj.get("phone").getAsString();
                            }
                            if (studentObj.has("major") && !studentObj.get("major").isJsonNull()) {
                                profileMajor = studentObj.get("major").getAsString();
                            }
                            if (studentObj.has("status") && !studentObj.get("status").isJsonNull()) {
                                profileEnrollmentStatus = studentObj.get("status").getAsString();
                            }
                            if (studentObj.has("year_level") && !studentObj.get("year_level").isJsonNull()) {
                                String rawYr = studentObj.get("year_level").getAsString();
                                if (com.social7.studentlearnanalytics.utils.YearLevelHelper.parse(rawYr) != null) {
                                    profileYearLevel = com.social7.studentlearnanalytics.utils.YearLevelHelper.toFullLabel(getContext(), rawYr);
                                }
                            }
                            if (studentObj.has("academic_year") && !studentObj.get("academic_year").isJsonNull()) {
                                com.google.gson.JsonElement ayEl = studentObj.get("academic_year");
                                if (ayEl.isJsonPrimitive()) {
                                    profileAcademicYear = ayEl.getAsString().trim();
                                } else if (ayEl.isJsonObject() && ayEl.getAsJsonObject().has("name")
                                        && !ayEl.getAsJsonObject().get("name").isJsonNull()) {
                                    profileAcademicYear = ayEl.getAsJsonObject().get("name").getAsString().trim();
                                }
                            }
                            if (studentObj.has("department") && studentObj.get("department").isJsonObject()) {
                                JsonObject dept = studentObj.getAsJsonObject("department");
                                if (dept.has("name") && !dept.get("name").isJsonNull()) {
                                    profileDepartment = dept.get("name").getAsString();
                                }
                            }
                            if (studentObj.has("program") && studentObj.get("program").isJsonObject()) {
                                JsonObject prog = studentObj.getAsJsonObject("program");
                                if (prog.has("name") && !prog.get("name").isJsonNull()) {
                                    profileMajor = prog.get("name").getAsString();
                                }
                            }
                            if (studentObj.has("class_room") && studentObj.get("class_room").isJsonObject()) {
                                JsonObject cls = studentObj.getAsJsonObject("class_room");
                                if (cls.has("name") && !cls.get("name").isJsonNull()) {
                                    profileClassName = cls.get("name").getAsString();
                                }
                            }
                        }

                        if (profileAcademicYear.isEmpty() && root.has("profile") && root.get("profile").isJsonObject()) {
                            JsonObject profWrapper = root.getAsJsonObject("profile");
                            if (profWrapper.has("profile") && profWrapper.get("profile").isJsonObject()) {
                                JsonObject profObj = profWrapper.getAsJsonObject("profile");
                                if (profObj.has("academic_year") && !profObj.get("academic_year").isJsonNull()
                                        && profObj.get("academic_year").isJsonPrimitive()) {
                                    profileAcademicYear = profObj.get("academic_year").getAsString().trim();
                                }
                            }
                        }

                        if (profileDepartment.isEmpty() && !profileMajor.isEmpty()) {
                            profileDepartment = profileMajor;
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
                            tvStudentId.setText(tvStudentId.getContext().getString(R.string.id) + code);
                        }
                        if (tvDepartment != null && !profileDepartment.isEmpty()) {
                            tvDepartment.setText(profileDepartment);
                        }
                        if (tvStudentYearLevel != null) {
                            tvStudentYearLevel.setText(!profileYearLevel.isEmpty()
                                    ? profileYearLevel : getString(R.string.year_level_unassigned));
                        }
                        if (tvAcademicYear != null) {
                            if (!profileAcademicYear.isEmpty()) {
                                tvAcademicYear.setText(profileAcademicYear);
                                tvAcademicYear.setVisibility(View.VISIBLE);
                            } else {
                                tvAcademicYear.setVisibility(View.GONE);
                            }
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
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout Confirmation")
                .setMessage(R.string.are_you_sure_you_want_to_logout)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    if (getContext() != null) {
                        ApiClient.getApiService(requireContext()).logout().enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                performLocalLogout();
                            }

                            @Override
                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                performLocalLogout();
                            }
                        });
                    } else {
                        performLocalLogout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performLocalLogout() {
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
        if (isAdded() && getContext() != null) {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
