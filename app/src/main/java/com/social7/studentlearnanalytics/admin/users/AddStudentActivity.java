package com.social7.studentlearnanalytics.admin.users;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.utils.DatePickerHelper;
import com.social7.studentlearnanalytics.utils.PersonNameDisplayHelper;
import com.social7.studentlearnanalytics.utils.RoleGuard;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStudentActivity extends AppCompatActivity {

    // 01. Personal Information
    private TextInputLayout tilAddStudentKhmerName, tilAddStudentEnglishName, tilAddStudentDob, tilAddStudentPob, tilAddStudentAddress;
    private TextInputEditText etAddStudentKhmerName, etAddStudentEnglishName, etAddStudentDob, etAddStudentPob, etAddStudentAddress;
    private Spinner spAddStudentGender;

    // 02. Family Information
    private EditText etFatherKhmerName, etFatherEnglishName, etFatherPhone;
    private EditText etMotherKhmerName, etMotherEnglishName, etMotherPhone;

    // 03. Emergency Contact
    private EditText etEmergencyName, etEmergencyRelationship, etEmergencyPhone, etEmergencyAddress;

    // 04. Account Information (Single Identifier/Username field)
    private TextInputLayout tilAddStudentIdentifier, tilAddStudentPassword;
    private TextInputEditText etAddStudentIdentifier, etAddStudentPassword;

    // 05. Academic Information
    private Spinner spAddDepartment, spAddProgram, spAddYearLevel, spAddAcademicYear, spAddSemester;

    private MaterialButton btnSaveStudent, btnCancelAddStudent;

    // Lookup data lists
    private final List<IdNameItem> departmentList = new ArrayList<>();
    private final List<IdNameItem> allProgramList = new ArrayList<>();
    private final List<IdNameItem> filteredProgramList = new ArrayList<>();
    private final List<IdNameItem> academicYearList = new ArrayList<>();
    private final List<IdNameItem> allSemesterList = new ArrayList<>();
    private final List<IdNameItem> filteredSemesterList = new ArrayList<>();

    private boolean lookupsLoaded = false;
    private boolean isLoadingLookups = false;
    private boolean isSyncingSpinners = false;
    private int lastSelectedDepartmentId = -1;
    private int lastSelectedAcademicYearId = -1;
    private int majorFetchSeq = 0;
    private boolean majorLoadFailed = false;
    private boolean isLoadingMajors = false;
    private IdNameItem selectedDepartmentForMajors = null;

    public static class IdNameItem {
        public final int id;
        public final String name;
        public final String code;
        public final int parentId;

        public IdNameItem(int id, String name, String code, int parentId) {
            this.id = id;
            this.name = name != null ? name : "";
            this.code = code != null ? code : "";
            this.parentId = parentId;
        }

        @NonNull
        @Override
        public String toString() {
            if (id <= 0) {
                return name;
            }
            if (code != null && !code.isEmpty() && !name.contains("(" + code + ")")) {
                return name + " (" + code + ")";
            }
            return name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }

        setContentView(R.layout.activity_add_user);

        initViews();
        setupGenderAndYearLevelSpinners();
        setupInitialAcademicSpinners();
        setupDependentSpinners();
        fetchAcademicLookups();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RoleGuard.checkAccess(this, "Admin")) {
            return;
        }
        if (!lookupsLoaded && !isLoadingLookups) {
            fetchAcademicLookups();
        } else if (lookupsLoaded
                && !isLoadingMajors
                && selectedDepartmentForMajors != null
                && selectedDepartmentForMajors.id > 0
                && (majorLoadFailed || filteredProgramList.size() <= 1)) {
            fetchMajorsForDepartment(selectedDepartmentForMajors);
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackAddStudent);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 01.
        tilAddStudentKhmerName = findViewById(R.id.tilAddStudentKhmerName);
        tilAddStudentEnglishName = findViewById(R.id.tilAddStudentEnglishName);
        tilAddStudentDob = findViewById(R.id.tilAddStudentDob);
        tilAddStudentPob = findViewById(R.id.tilAddStudentPob);
        tilAddStudentAddress = findViewById(R.id.tilAddStudentAddress);

        etAddStudentKhmerName = findViewById(R.id.etAddStudentKhmerName);
        etAddStudentEnglishName = findViewById(R.id.etAddStudentEnglishName);
        etAddStudentDob = findViewById(R.id.etAddStudentDob);
        etAddStudentPob = findViewById(R.id.etAddStudentPob);
        etAddStudentAddress = findViewById(R.id.etAddStudentAddress);
        spAddStudentGender = findViewById(R.id.spAddStudentGender);

        // 02.
        etFatherKhmerName = findViewById(R.id.etFatherKhmerName);
        etFatherEnglishName = findViewById(R.id.etFatherEnglishName);
        etFatherPhone = findViewById(R.id.etFatherPhone);
        etMotherKhmerName = findViewById(R.id.etMotherKhmerName);
        etMotherEnglishName = findViewById(R.id.etMotherEnglishName);
        etMotherPhone = findViewById(R.id.etMotherPhone);

        // 03.
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyRelationship = findViewById(R.id.etEmergencyRelationship);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etEmergencyAddress = findViewById(R.id.etEmergencyAddress);

        // 04.
        tilAddStudentIdentifier = findViewById(R.id.tilAddStudentIdentifier);
        tilAddStudentPassword = findViewById(R.id.tilAddStudentPassword);
        etAddStudentIdentifier = findViewById(R.id.etAddStudentIdentifier);
        etAddStudentPassword = findViewById(R.id.etAddStudentPassword);
        if (etAddStudentPassword != null) {
            etAddStudentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            etAddStudentPassword.setText("STD@12345");
            etAddStudentPassword.setEnabled(false);
        }
        if (tilAddStudentPassword != null) {
            tilAddStudentPassword.setHelperText("Standardized initial password assigned automatically to all new students.");
        }

        // 05.
        spAddDepartment = findViewById(R.id.spAddDepartment);
        spAddProgram = findViewById(R.id.spAddProgram);
        spAddYearLevel = findViewById(R.id.spAddYearLevel);
        spAddAcademicYear = findViewById(R.id.spAddAcademicYear);
        spAddSemester = findViewById(R.id.spAddSemester);

        btnSaveStudent = findViewById(R.id.btnSaveStudent);
        btnCancelAddStudent = findViewById(R.id.btnCancelAddStudent);
    }

    private void setupGenderAndYearLevelSpinners() {
        setStringSpinner(spAddStudentGender, java.util.Arrays.asList("Male", "Female"));
        setStringSpinner(spAddYearLevel, YearLevelHelper.getFormDropdownOptions(this));
    }

    private void setupInitialAcademicSpinners() {
        isSyncingSpinners = true;

        departmentList.clear();
        departmentList.add(new IdNameItem(0, getString(R.string.loading_options), "", 0));
        setSpinner(spAddDepartment, departmentList);

        filteredProgramList.clear();
        filteredProgramList.add(new IdNameItem(0, getString(R.string.select_faculty_first), "", 0));
        setSpinner(spAddProgram, filteredProgramList);
        if (spAddProgram != null) {
            spAddProgram.setEnabled(false);
            spAddProgram.setAlpha(0.6f);
        }

        academicYearList.clear();
        academicYearList.add(new IdNameItem(0, getString(R.string.loading_options), "", 0));
        setSpinner(spAddAcademicYear, academicYearList);

        filteredSemesterList.clear();
        filteredSemesterList.add(new IdNameItem(0, getString(R.string.none_optional), "", 0));
        setSpinner(spAddSemester, filteredSemesterList);

        releaseSyncGuard();
    }

    private void setupDependentSpinners() {
        View.OnTouchListener retryTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && !lookupsLoaded && !isLoadingLookups) {
                fetchAcademicLookups();
                return true;
            }
            return false;
        };

        if (spAddDepartment != null) {
            spAddDepartment.setOnTouchListener(retryTouchListener);
            spAddDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isSyncingSpinners || !lookupsLoaded) return;
                    IdNameItem dept = position >= 0 && position < departmentList.size() ? departmentList.get(position) : null;
                    int deptId = dept != null ? dept.id : 0;
                    if (deptId == lastSelectedDepartmentId) return;
                    lastSelectedDepartmentId = deptId;
                    selectedDepartmentForMajors = dept;
                    if (dept == null || dept.id <= 0) {
                        majorFetchSeq++;
                        isLoadingMajors = false;
                        majorLoadFailed = false;
                        filterProgramsForDepartment(null);
                    } else {
                        fetchMajorsForDepartment(dept);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (spAddProgram != null) {
            spAddProgram.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP
                        && majorLoadFailed
                        && !isLoadingMajors
                        && selectedDepartmentForMajors != null
                        && selectedDepartmentForMajors.id > 0) {
                    fetchMajorsForDepartment(selectedDepartmentForMajors);
                    return true;
                }
                return false;
            });
        }

        if (spAddAcademicYear != null) {
            spAddAcademicYear.setOnTouchListener(retryTouchListener);
            spAddAcademicYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isSyncingSpinners || !lookupsLoaded) return;
                    IdNameItem ay = position >= 0 && position < academicYearList.size() ? academicYearList.get(position) : null;
                    int ayId = ay != null ? ay.id : 0;
                    if (ayId == lastSelectedAcademicYearId) return;
                    lastSelectedAcademicYearId = ayId;
                    filterSemestersForAcademicYear(ay);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void filterProgramsForDepartment(IdNameItem dept) {
        AcademicDropdownHelper.MajorOptionsResult result = AcademicDropdownHelper.buildMajorOptions(
                dept,
                allProgramList,
                getString(R.string.select_faculty_first),
                getString(R.string.select_major),
                getString(R.string.no_majors_for_faculty)
        );
        filteredProgramList.clear();
        filteredProgramList.addAll(result.items);
        setSpinner(spAddProgram, filteredProgramList);
        if (spAddProgram != null) {
            spAddProgram.setEnabled(result.enabled);
            spAddProgram.setAlpha(result.enabled ? 1.0f : 0.6f);
        }
    }

    private void fetchMajorsForDepartment(@NonNull final IdNameItem dept) {
        final int requestSeq = ++majorFetchSeq;
        isLoadingMajors = true;
        majorLoadFailed = false;

        filteredProgramList.clear();
        filteredProgramList.add(new IdNameItem(0, getString(R.string.loading_majors), "", 0));
        setSpinner(spAddProgram, filteredProgramList);
        if (spAddProgram != null) {
            spAddProgram.setEnabled(false);
            spAddProgram.setAlpha(0.6f);
        }

        ApiClient.getApiService(this).getProgramsByDepartment(dept.id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed() || requestSeq != majorFetchSeq) return;
                isLoadingMajors = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<IdNameItem> fetched = AcademicDropdownHelper.parseMajorsResponse(response.body());
                    AcademicDropdownHelper.MajorOptionsResult result = AcademicDropdownHelper.buildMajorOptionsFromApiResult(
                            dept,
                            fetched,
                            getString(R.string.select_faculty_first),
                            getString(R.string.select_major),
                            getString(R.string.no_majors_for_faculty)
                    );
                    filteredProgramList.clear();
                    filteredProgramList.addAll(result.items);
                    setSpinner(spAddProgram, filteredProgramList);
                    if (spAddProgram != null) {
                        spAddProgram.setEnabled(result.enabled);
                        spAddProgram.setAlpha(result.enabled ? 1.0f : 0.6f);
                    }
                } else {
                    applyMajorLoadErrorState("Failed to load majors for faculty " + dept.id + " (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || requestSeq != majorFetchSeq) return;
                isLoadingMajors = false;
                applyMajorLoadErrorState("Network error loading majors for faculty " + dept.id + ": " + t.getMessage());
            }
        });
    }

    private void applyMajorLoadErrorState(String logMessage) {
        majorLoadFailed = true;
        Log.e("AddStudent", logMessage);

        filteredProgramList.clear();
        filteredProgramList.add(new IdNameItem(0, getString(R.string.failed_to_load_majors_retry), "", 0));
        setSpinner(spAddProgram, filteredProgramList);
        if (spAddProgram != null) {
            spAddProgram.setEnabled(true);
            spAddProgram.setAlpha(1.0f);
        }

        Toast.makeText(AddStudentActivity.this, getString(R.string.failed_to_load_majors_retry), Toast.LENGTH_SHORT).show();
    }

    private void filterSemestersForAcademicYear(IdNameItem ay) {
        List<IdNameItem> options = AcademicDropdownHelper.buildSemesterOptions(
                ay,
                allSemesterList,
                getString(R.string.none_optional)
        );
        filteredSemesterList.clear();
        filteredSemesterList.addAll(options);
        setSpinner(spAddSemester, filteredSemesterList);
    }

    private void fetchAcademicLookups() {
        if (isLoadingLookups) return;
        isLoadingLookups = true;

        if (!lookupsLoaded) {
            isSyncingSpinners = true;
            departmentList.clear();
            departmentList.add(new IdNameItem(0, getString(R.string.loading_options), "", 0));
            setSpinner(spAddDepartment, departmentList);

            academicYearList.clear();
            academicYearList.add(new IdNameItem(0, getString(R.string.loading_options), "", 0));
            setSpinner(spAddAcademicYear, academicYearList);
            releaseSyncGuard();
        }

        ApiClient.getApiService(this).getStudentLookups().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                isLoadingLookups = false;
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    AcademicDropdownHelper.LookupData parsed = AcademicDropdownHelper.parseLookups(response.body());
                    if (parsed.faculties.isEmpty() && parsed.academicYears.isEmpty()) {
                        applyLookupFailureState("No academic lookup records returned by server");
                        return;
                    }

                    isSyncingSpinners = true;

                    departmentList.clear();
                    departmentList.addAll(AcademicDropdownHelper.buildFacultyOptions(
                            parsed.faculties, getString(R.string.select_faculty)));
                    setSpinner(spAddDepartment, departmentList);

                    allProgramList.clear();
                    allProgramList.addAll(parsed.majors);
                    lastSelectedDepartmentId = 0;
                    selectedDepartmentForMajors = null;
                    majorFetchSeq++;
                    isLoadingMajors = false;
                    majorLoadFailed = false;
                    filterProgramsForDepartment(null);

                    academicYearList.clear();
                    academicYearList.addAll(AcademicDropdownHelper.buildAcademicYearOptions(
                            parsed.academicYears, getString(R.string.select_academic_year_title)));
                    setSpinner(spAddAcademicYear, academicYearList);

                    allSemesterList.clear();
                    allSemesterList.addAll(parsed.semesters);
                    lastSelectedAcademicYearId = 0;
                    filterSemestersForAcademicYear(null);

                    lookupsLoaded = true;
                    releaseSyncGuard();
                } else {
                    applyLookupFailureState("Server Error: Failed to load lookups (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                isLoadingLookups = false;
                if (isFinishing() || isDestroyed()) return;
                applyLookupFailureState("Failed to load academic options: " + t.getMessage());
            }
        });
    }

    private void applyLookupFailureState(String logMessage) {
        lookupsLoaded = false;
        Log.e("AddStudent", logMessage);

        isSyncingSpinners = true;
        departmentList.clear();
        departmentList.add(new IdNameItem(0, getString(R.string.tap_to_retry_loading), "", 0));
        setSpinner(spAddDepartment, departmentList);

        allProgramList.clear();
        lastSelectedDepartmentId = 0;
        selectedDepartmentForMajors = null;
        majorFetchSeq++;
        isLoadingMajors = false;
        majorLoadFailed = false;
        filterProgramsForDepartment(null);

        academicYearList.clear();
        academicYearList.add(new IdNameItem(0, getString(R.string.tap_to_retry_loading), "", 0));
        setSpinner(spAddAcademicYear, academicYearList);

        allSemesterList.clear();
        lastSelectedAcademicYearId = 0;
        filterSemestersForAcademicYear(null);
        releaseSyncGuard();

        Toast.makeText(AddStudentActivity.this, getString(R.string.tap_to_retry_loading), Toast.LENGTH_SHORT).show();
    }

    private void releaseSyncGuard() {
        if (spAddDepartment != null) {
            spAddDepartment.post(() -> isSyncingSpinners = false);
        } else {
            isSyncingSpinners = false;
        }
    }

    private void setSpinner(Spinner sp, List<IdNameItem> items) {
        if (sp == null) return;
        final List<IdNameItem> copy = new ArrayList<>(items);
        ArrayAdapter<IdNameItem> adapter = new ArrayAdapter<IdNameItem>(this,
                android.R.layout.simple_spinner_item, copy) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView && position >= 0 && position < copy.size()) {
                    IdNameItem item = copy.get(position);
                    TextView tv = (TextView) view;
                    tv.setTextColor(ContextCompat.getColor(AddStudentActivity.this,
                            item.id > 0 ? R.color.admin_text_primary : R.color.admin_text_secondary));
                    tv.setTextSize(14f);
                    tv.setSingleLine(true);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    tv.setPadding(dp(12), 0, dp(32), 0);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView && position >= 0 && position < copy.size()) {
                    IdNameItem item = copy.get(position);
                    TextView tv = (TextView) view;
                    tv.setBackgroundColor(ContextCompat.getColor(AddStudentActivity.this, R.color.card_white));
                    tv.setTextColor(ContextCompat.getColor(AddStudentActivity.this,
                            item.id > 0 ? R.color.admin_text_primary : R.color.admin_text_secondary));
                    tv.setTextSize(14f);
                    tv.setPadding(dp(12), dp(14), dp(12), dp(14));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setSelection(0, false);
    }

    private void setStringSpinner(Spinner sp, List<String> items) {
        if (sp == null) return;
        final List<String> copy = new ArrayList<>(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, copy) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setTextColor(ContextCompat.getColor(AddStudentActivity.this, R.color.admin_text_primary));
                    tv.setTextSize(14f);
                    tv.setSingleLine(true);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    tv.setPadding(dp(12), 0, dp(32), 0);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setBackgroundColor(ContextCompat.getColor(AddStudentActivity.this, R.color.card_white));
                    tv.setTextColor(ContextCompat.getColor(AddStudentActivity.this, R.color.admin_text_primary));
                    tv.setTextSize(14f);
                    tv.setPadding(dp(12), dp(14), dp(12), dp(14));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setSelection(0, false);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupListeners() {
        DatePickerHelper.attachDatePicker(tilAddStudentDob, etAddStudentDob, true);

        if (btnCancelAddStudent != null) {
            btnCancelAddStudent.setOnClickListener(v -> finish());
        }

        if (btnSaveStudent != null) {
            btnSaveStudent.setOnClickListener(v -> attemptSaveStudent());
        }
    }

    private void attemptSaveStudent() {
        clearErrors();

        String khmerName = PersonNameDisplayHelper.clean(getText(etAddStudentKhmerName));
        String englishName = PersonNameDisplayHelper.clean(getText(etAddStudentEnglishName));
        String dob = getText(etAddStudentDob);
        String identifier = getText(etAddStudentIdentifier);
        String password = getText(etAddStudentPassword);
        String pob = getText(etAddStudentPob);
        String address = getText(etAddStudentAddress);

        boolean valid = true;

        if (!PersonNameDisplayHelper.hasAtLeastOneName(khmerName, englishName)) {
            String errMsg = getString(R.string.err_name_one_required);
            if (tilAddStudentKhmerName != null) tilAddStudentKhmerName.setError(errMsg);
            if (tilAddStudentEnglishName != null) tilAddStudentEnglishName.setError(errMsg);
            valid = false;
        }

        if (!dob.isEmpty() && !DatePickerHelper.isValidIsoDate(dob)) {
            if (tilAddStudentDob != null) tilAddStudentDob.setError(getString(R.string.err_invalid_date));
            valid = false;
        }

        if (identifier.isEmpty()) {
            if (tilAddStudentIdentifier != null) tilAddStudentIdentifier.setError("Student ID / Username is required");
            valid = false;
        }

        IdNameItem selectedDept = spAddDepartment != null && spAddDepartment.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddDepartment.getSelectedItem() : null;
        IdNameItem selectedProg = spAddProgram != null && spAddProgram.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddProgram.getSelectedItem() : null;
        IdNameItem selectedYear = spAddAcademicYear != null && spAddAcademicYear.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddAcademicYear.getSelectedItem() : null;
        IdNameItem selectedSem = spAddSemester != null && spAddSemester.getSelectedItem() instanceof IdNameItem
                ? (IdNameItem) spAddSemester.getSelectedItem() : null;

        if (selectedDept == null || selectedDept.id <= 0) {
            Toast.makeText(this, R.string.please_select_a_department, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedProg == null || selectedProg.id <= 0) {
            Toast.makeText(this, R.string.please_select_a_program, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedYear == null || selectedYear.id <= 0) {
            Toast.makeText(this, R.string.please_select_an_academic_year, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) return;

        Integer parsedYl = spAddYearLevel != null ? YearLevelHelper.parse(spAddYearLevel.getSelectedItem()) : 1;
        if (parsedYl == null && spAddYearLevel != null) {
            parsedYl = YearLevelHelper.parse(spAddYearLevel.getSelectedItemPosition() + 1);
        }
        if (parsedYl == null) {
            Toast.makeText(this, R.string.err_invalid_year_level, Toast.LENGTH_SHORT).show();
            return;
        }
        int yearLevel = parsedYl;
        String gender = spAddStudentGender != null ? spAddStudentGender.getSelectedItem().toString() : "Male";

        JsonObject body = new JsonObject();
        if (!khmerName.isEmpty()) body.addProperty("khmer_name", khmerName);
        if (!englishName.isEmpty()) body.addProperty("english_name", englishName);
        body.addProperty("gender", gender);
        if (!dob.isEmpty()) body.addProperty("date_of_birth", dob);
        body.addProperty("place_of_birth", pob);
        body.addProperty("current_address", address);
        body.addProperty("student_identifier", identifier);

        body.addProperty("department_id", selectedDept.id);
        body.addProperty("program_id", selectedProg.id);
        body.addProperty("year_level", yearLevel);
        body.addProperty("academic_year_id", selectedYear.id);
        if (selectedSem != null && selectedSem.id > 0) {
            body.addProperty("semester_id", selectedSem.id);
        }

        // Family Information
        body.addProperty("father_khmer_name", getText(etFatherKhmerName));
        body.addProperty("father_english_name", getText(etFatherEnglishName));
        body.addProperty("father_phone", getText(etFatherPhone));
        body.addProperty("mother_khmer_name", getText(etMotherKhmerName));
        body.addProperty("mother_english_name", getText(etMotherEnglishName));
        body.addProperty("mother_phone", getText(etMotherPhone));

        // Emergency Contact
        body.addProperty("emergency_contact_name", getText(etEmergencyName));
        body.addProperty("emergency_relationship", getText(etEmergencyRelationship));
        body.addProperty("emergency_contact_phone", getText(etEmergencyPhone));
        body.addProperty("emergency_contact_address", getText(etEmergencyAddress));

        if (btnSaveStudent != null) btnSaveStudent.setEnabled(false);

        ApiClient.getApiService(this).addStudentUniversity(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject d = response.body().has("data") ? response.body().getAsJsonObject("data") : null;
                    String initialPass = d != null && d.has("initial_password") && !d.get("initial_password").isJsonNull()
                            ? d.get("initial_password").getAsString()
                            : (d != null && d.has("temporary_password") && !d.get("temporary_password").isJsonNull()
                                ? d.get("temporary_password").getAsString() : "STD@12345");
                    String username = d != null && d.has("username") && !d.get("username").isJsonNull()
                            ? d.get("username").getAsString() : identifier;

                    String credSummary = "Student ID: " + username
                            + "\nUsername: " + username
                            + "\nInitial Password: " + initialPass
                            + "\nRole: student";
                    new MaterialAlertDialogBuilder(AddStudentActivity.this)
                            .setTitle(R.string.student_created_successfully)
                            .setMessage(credSummary + "\n\nStudent can log in immediately using their Student ID and initial password.")
                            .setPositiveButton("Copy Credentials", (dialog, which) -> {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) {
                                    cm.setPrimaryClip(ClipData.newPlainText("SLAS Student Credentials", credSummary));
                                    Toast.makeText(AddStudentActivity.this, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            })
                            .setNegativeButton(R.string.done, (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    String msg = "Failed to save student (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (err.has("errors")) {
                                JsonObject errors = err.getAsJsonObject("errors");
                                if (errors.entrySet().iterator().hasNext()) {
                                    msg = errors.entrySet().iterator().next().getValue().getAsJsonArray().get(0).getAsString();
                                }
                            } else if (err.has("message")) {
                                msg = err.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AddStudentActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (btnSaveStudent != null) btnSaveStudent.setEnabled(true);
                Toast.makeText(AddStudentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearErrors() {
        if (tilAddStudentKhmerName != null) tilAddStudentKhmerName.setError(null);
        if (tilAddStudentEnglishName != null) tilAddStudentEnglishName.setError(null);
        if (tilAddStudentDob != null) tilAddStudentDob.setError(null);
        if (tilAddStudentIdentifier != null) tilAddStudentIdentifier.setError(null);
        if (tilAddStudentPassword != null) tilAddStudentPassword.setError(null);
    }

    private String getText(EditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }
}
