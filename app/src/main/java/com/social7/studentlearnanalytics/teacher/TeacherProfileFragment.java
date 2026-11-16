package com.social7.studentlearnanalytics.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.auth.ChangePasswordActivity;
import com.social7.studentlearnanalytics.storage.SessionManager;

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
        String name = sessionManager != null && sessionManager.getUserName() != null && !sessionManager.getUserName().trim().isEmpty() ? sessionManager.getUserName() : "Mr. Chan";
        String email = "chan@university.edu";

        String initial = !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "T";

        if (tvTeacherAvatar != null) tvTeacherAvatar.setText(initial);
        if (tvTeacherNameSummary != null) tvTeacherNameSummary.setText(name);
        if (tvTeacherRoleDept != null) tvTeacherRoleDept.setText("Senior Lecturer • Computer Science Department");
        if (tvTeacherAccountStatus != null) tvTeacherAccountStatus.setText(R.string.status_active);

        if (etTeacherName != null) etTeacherName.setText(name);
        if (etTeacherEmail != null) etTeacherEmail.setText(email);
        if (etTeacherPhone != null) etTeacherPhone.setText("+855 12 345 678");
        if (etTeacherDob != null) etTeacherDob.setText("Mar 10, 1990");

        if (etTeacherId != null) etTeacherId.setText("TCH-001");
        if (etTeacherDepartment != null) etTeacherDepartment.setText("College of Computer Studies");
        if (etTeacherQualifications != null) etTeacherQualifications.setText("M.Sc. Computer Science");

        if (rbTeacherMale != null) rbTeacherMale.setChecked(true);
    }

    private void setupListeners(View view) {
        View btnUpload = view.findViewById(R.id.btnTeacherUploadPhoto);
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Teacher profile photo selection opened", Toast.LENGTH_SHORT).show()
            );
        }

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
        if (etTeacherName == null || etTeacherEmail == null) return;

        String name = etTeacherName.getText().toString().trim();
        String email = etTeacherEmail.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            if (tilTeacherName != null) tilTeacherName.setError("Full name is required");
            isValid = false;
        } else {
            if (tilTeacherName != null) tilTeacherName.setError(null);
        }

        if (email.isEmpty()) {
            if (tilTeacherEmail != null) tilTeacherEmail.setError("Email address is required");
            isValid = false;
        } else {
            if (tilTeacherEmail != null) tilTeacherEmail.setError(null);
        }

        if (isValid) {
            if (sessionManager != null) {
                sessionManager.saveSession(
                        sessionManager.getUserId() != null ? sessionManager.getUserId() : "TCH-001",
                        sessionManager.getToken() != null ? sessionManager.getToken() : "token",
                        "Teacher",
                        name
                );
            }
            Toast.makeText(getContext(), "Teacher profile updated and saved successfully!", Toast.LENGTH_LONG).show();
            if (getActivity() instanceof TeacherMainActivity) {
                ((TeacherMainActivity) getActivity()).openFragment(new TeacherProfileOverviewFragment());
            }
        }
    }
}
