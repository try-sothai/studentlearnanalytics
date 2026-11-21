package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherProfileFragment extends Fragment {

    private TextView tvTeacherAvatar, tvTeacherNameSummary, tvTeacherRoleDept, tvTeacherAccountStatus;
    private TextInputLayout tilTeacherName, tilTeacherEmail;
    private EditText etTeacherName, etTeacherEmail, etTeacherPhone, etTeacherDob, etTeacherId, etTeacherDepartment, etTeacherQualifications;
    private RadioGroup rgTeacherGender;
    private RadioButton rbTeacherMale, rbTeacherFemale;
    private MaterialButton btnTeacherSaveProfile, btnTeacherCancelProfile, btnTeacherChangePassword;
    private SessionManager sessionManager;

    public TeacherProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        if (getContext() != null) {
            sessionManager = new SessionManager(requireContext());
        }

        initViews(view);
        prepopulateData();
        setupListeners(view);

        return view;
    }

    private void initViews(View view) {
        if (getActivity() instanceof TeacherMainActivity) {
            ((TeacherMainActivity) getActivity()).setupTeacherHeader(
                    view.findViewById(R.id.headerTeacherProfile), getString(R.string.teacher_profile), ""
            );
        }

        tvTeacherAvatar = view.findViewById(R.id.tvTeacherAvatar);
        tvTeacherNameSummary = view.findViewById(R.id.tvTeacherNameSummary);
        tvTeacherRoleDept = view.findViewById(R.id.tvTeacherRoleDept);
        tvTeacherAccountStatus = view.findViewById(R.id.tvTeacherAccountStatus);

        tilTeacherName = view.findViewById(R.id.tilTeacherName);
        tilTeacherEmail = view.findViewById(R.id.tilTeacherEmail);

        etTeacherName = view.findViewById(R.id.etTeacherName);
        etTeacherEmail = view.findViewById(R.id.etTeacherEmail);
        etTeacherPhone = view.findViewById(R.id.etTeacherPhone);
        etTeacherDob = view.findViewById(R.id.etTeacherDob);
        com.social7.studentlearnanalytics.utils.DatePickerHelper.attachDatePicker(null, etTeacherDob, true);

        etTeacherId = view.findViewById(R.id.etTeacherId);
        etTeacherDepartment = view.findViewById(R.id.etTeacherDepartment);
        etTeacherQualifications = view.findViewById(R.id.etTeacherQualifications);

        rgTeacherGender = view.findViewById(R.id.rgTeacherGender);
        rbTeacherMale = view.findViewById(R.id.rbTeacherMale);
        rbTeacherFemale = view.findViewById(R.id.rbTeacherFemale);

        btnTeacherSaveProfile = view.findViewById(R.id.btnTeacherSaveProfile);
        btnTeacherCancelProfile = view.findViewById(R.id.btnTeacherCancelProfile);
        btnTeacherChangePassword = view.findViewById(R.id.btnTeacherChangePassword);
    }

    private void prepopulateData() {
        if (getContext() == null) return;
        ApiClient.getApiService(requireContext()).getProfile().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();
                    JsonObject userObj = root.has("data") && root.get("data").isJsonObject()
                            ? root.getAsJsonObject("data") : root;
                    if (userObj.has("user") && userObj.get("user").isJsonObject()) {
                        userObj = userObj.getAsJsonObject("user");
                    }

                    String name = getStr(userObj, "name", "");
                    String email = getStr(userObj, "email", "");
                    String phone = getStr(userObj, "phone", "");

                    JsonObject teacherObj = userObj.has("teacher") && userObj.get("teacher").isJsonObject()
                            ? userObj.getAsJsonObject("teacher") : null;
                    String tCode = teacherObj != null ? getStr(teacherObj, "teacher_code", "") : "";
                    String dob = teacherObj != null ? getStr(teacherObj, "date_of_birth", "") : "";
                    String spec = teacherObj != null ? getStr(teacherObj, "specialization", "") : "";
                    String pos = teacherObj != null ? getStr(teacherObj, "position", "Lecturer") : "Lecturer";
                    String gender = teacherObj != null ? getStr(teacherObj, "gender", "male") : "male";
                    if (teacherObj != null && phone.isEmpty()) {
                        phone = getStr(teacherObj, "phone", "");
                    }
                    String dept = "Academic Department";
                    if (teacherObj != null && teacherObj.has("department") && teacherObj.get("department").isJsonObject()) {
                        dept = getStr(teacherObj.getAsJsonObject("department"), "name", dept);
                    }

                    if (!name.isEmpty() && tvTeacherAvatar != null) {
                        tvTeacherAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT));
                    }
                    if (tvTeacherNameSummary != null) tvTeacherNameSummary.setText(name);
                    if (tvTeacherRoleDept != null) tvTeacherRoleDept.setText(pos + " • " + dept);
                    if (tvTeacherAccountStatus != null) tvTeacherAccountStatus.setText(R.string.status_active);

                    if (etTeacherName != null) etTeacherName.setText(name);
                    if (etTeacherEmail != null) etTeacherEmail.setText(email);
                    if (etTeacherPhone != null) etTeacherPhone.setText(phone);
                    if (etTeacherDob != null) etTeacherDob.setText(dob);
                    if (etTeacherId != null) etTeacherId.setText(tCode);
                    if (etTeacherDepartment != null) etTeacherDepartment.setText(dept);
                    if (etTeacherQualifications != null) etTeacherQualifications.setText(spec);

                    if ("female".equalsIgnoreCase(gender) && rbTeacherFemale != null) {
                        rbTeacherFemale.setChecked(true);
                    } else if (rbTeacherMale != null) {
                        rbTeacherMale.setChecked(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners(View view) {
        if (btnTeacherCancelProfile != null) {
            btnTeacherCancelProfile.setOnClickListener(v -> {
                if (getActivity() instanceof TeacherMainActivity) {
                    ((TeacherMainActivity) getActivity()).openFragment(new TeacherProfileOverviewFragment());
                }
            });
        }

        if (btnTeacherChangePassword != null) {
            btnTeacherChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            });
        }

        if (btnTeacherSaveProfile != null) {
            btnTeacherSaveProfile.setOnClickListener(v -> attemptSaveProfile());
        }
    }

    private void attemptSaveProfile() {
        if (etTeacherName == null || etTeacherEmail == null || getContext() == null) return;

        String name = etTeacherName.getText().toString().trim();
        String email = etTeacherEmail.getText().toString().trim();
        String phone = etTeacherPhone != null ? etTeacherPhone.getText().toString().trim() : "";
        String dob = etTeacherDob != null ? etTeacherDob.getText().toString().trim() : "";

        boolean isValid = true;
        if (name.isEmpty()) {
            if (tilTeacherName != null) tilTeacherName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilTeacherName != null) tilTeacherName.setError(null);
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilTeacherEmail != null) tilTeacherEmail.setError(getString(R.string.err_invalid_email));
            isValid = false;
        } else {
            if (tilTeacherEmail != null) tilTeacherEmail.setError(null);
        }

        if (!dob.isEmpty() && !com.social7.studentlearnanalytics.utils.DatePickerHelper.isValidIsoDate(dob)) {
            if (etTeacherDob != null) etTeacherDob.setError(getString(R.string.err_invalid_date));
            isValid = false;
        }

        if (!isValid) return;

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("email", email);
        if (!phone.isEmpty()) body.addProperty("phone", phone);
        body.addProperty("date_of_birth", dob);

        ApiClient.getApiService(requireContext()).updateProfile(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful()) {
                    if (sessionManager != null) {
                        sessionManager.saveSession(
                                sessionManager.getUserId() != null ? sessionManager.getUserId() : "",
                                sessionManager.getToken() != null ? sessionManager.getToken() : "",
                                "Teacher",
                                name
                        );
                    }
                    Toast.makeText(getContext(), "Teacher profile updated and saved successfully!", Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof TeacherMainActivity) {
                        ((TeacherMainActivity) getActivity()).openFragment(new TeacherProfileOverviewFragment());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to update profile (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network error updating profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStr(JsonObject obj, String key, String fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return fallback;
        try {
            String v = obj.get(key).getAsString();
            return v != null && !v.isEmpty() ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
