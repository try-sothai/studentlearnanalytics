package com.social7.studentlearnanalytics.admin.reports;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

public class GenerateReportActivity extends AppCompatActivity {

    private MaterialButton typeStudents, typeTeachers, typeCourses, typeAcademic, typeActivity;
    private Spinner spGenerateReportYear, spGenerateReportSemester, spGenerateReportDept, spGenerateReportProgram, spGenerateReportStatus;
    private CheckBox cbMetric1, cbMetric2, cbMetric3, cbMetric4, cbMetric5, cbMetric6, cbMetric7, cbMetric8, cbMetric9, cbMetric10, cbMetric11, cbMetric12;
    private RadioGroup rgExportFormat;
    private RadioButton rbFormatPdf, rbFormatExcel, rbFormatCsv;
    private MaterialButton btnSubmitGenerateReport, btnCancelGenerateReport;

    private String selectedReportType = "Students";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        if (getIntent() != null && getIntent().hasExtra("selected_report_type")) {
            selectedReportType = getIntent().getStringExtra("selected_report_type");
        }

        initViews();
        setupReportTypeButtons();
        setupSpinners();
        setupListeners();

        selectReportType(selectedReportType);
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackGenerateReport);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        typeStudents = findViewById(R.id.typeStudents);
        typeTeachers = findViewById(R.id.typeTeachers);
        typeCourses = findViewById(R.id.typeCourses);
        typeAcademic = findViewById(R.id.typeAcademic);
        typeActivity = findViewById(R.id.typeActivity);

        spGenerateReportYear = findViewById(R.id.spGenerateReportYear);
        spGenerateReportSemester = findViewById(R.id.spGenerateReportSemester);
        spGenerateReportDept = findViewById(R.id.spGenerateReportDept);
        spGenerateReportProgram = findViewById(R.id.spGenerateReportProgram);
        spGenerateReportStatus = findViewById(R.id.spGenerateReportStatus);

        cbMetric1 = findViewById(R.id.cbMetric1);
        cbMetric2 = findViewById(R.id.cbMetric2);
        cbMetric3 = findViewById(R.id.cbMetric3);
        cbMetric4 = findViewById(R.id.cbMetric4);
        cbMetric5 = findViewById(R.id.cbMetric5);
        cbMetric6 = findViewById(R.id.cbMetric6);
        cbMetric7 = findViewById(R.id.cbMetric7);
        cbMetric8 = findViewById(R.id.cbMetric8);
        cbMetric9 = findViewById(R.id.cbMetric9);
        cbMetric10 = findViewById(R.id.cbMetric10);
        cbMetric11 = findViewById(R.id.cbMetric11);
        cbMetric12 = findViewById(R.id.cbMetric12);

        rgExportFormat = findViewById(R.id.rgExportFormat);
        rbFormatPdf = findViewById(R.id.rbFormatPdf);
        rbFormatExcel = findViewById(R.id.rbFormatExcel);
        rbFormatCsv = findViewById(R.id.rbFormatCsv);

        btnSubmitGenerateReport = findViewById(R.id.btnSubmitGenerateReport);
        btnCancelGenerateReport = findViewById(R.id.btnCancelGenerateReport);
    }

    private void setupReportTypeButtons() {
        if (typeStudents != null) typeStudents.setOnClickListener(v -> selectReportType("Students"));
        if (typeTeachers != null) typeTeachers.setOnClickListener(v -> selectReportType("Teachers"));
        if (typeCourses != null) typeCourses.setOnClickListener(v -> selectReportType("Courses"));
        if (typeAcademic != null) typeAcademic.setOnClickListener(v -> selectReportType("Academic"));
        if (typeActivity != null) typeActivity.setOnClickListener(v -> selectReportType("Activity"));
    }

    private void selectReportType(String type) {
        this.selectedReportType = type;
        int primaryColor = Color.parseColor("#3155F5");
        int secondaryColor = Color.parseColor("#64748B");
        int whiteColor = Color.WHITE;

        updateTypeButton(typeStudents, "Students".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeTeachers, "Teachers".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeCourses, "Courses".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeAcademic, "Academic".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
        updateTypeButton(typeActivity, "Activity".equalsIgnoreCase(type), primaryColor, secondaryColor, whiteColor);
    }

    private void updateTypeButton(MaterialButton button, boolean active, int primaryColor, int secondaryColor, int whiteColor) {
        if (button == null) return;
        button.setBackgroundTintList(ColorStateList.valueOf(active ? primaryColor : Color.parseColor("#F4F7FF")));
        button.setTextColor(active ? whiteColor : secondaryColor);
    }

    private void setupSpinners() {
        String[] years = {"2024 - 2025", "2023 - 2024", "2022 - 2023"};
        String[] semesters = {"All Semesters", "Semester 1", "Semester 2"};
        String[] depts = {"All Departments", "Computer Science", "Information Technology", "Software Engineering"};
        String[] programs = {"All Programs", "Bachelor of Computer Science", "Bachelor of IT"};
        String[] statuses = {"All Students", "Active", "Inactive"};

        setSpinnerAdapter(spGenerateReportYear, years);
        setSpinnerAdapter(spGenerateReportSemester, semesters);
        setSpinnerAdapter(spGenerateReportDept, depts);
        setSpinnerAdapter(spGenerateReportProgram, programs);
        setSpinnerAdapter(spGenerateReportStatus, statuses);
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnCancelGenerateReport != null) {
            btnCancelGenerateReport.setOnClickListener(v -> finish());
        }

        if (btnSubmitGenerateReport != null) {
            btnSubmitGenerateReport.setOnClickListener(v -> attemptGenerateReport());
        }
    }

    private void attemptGenerateReport() {
        boolean anyMetricChecked = (cbMetric1 != null && cbMetric1.isChecked()) ||
                (cbMetric2 != null && cbMetric2.isChecked()) ||
                (cbMetric3 != null && cbMetric3.isChecked()) ||
                (cbMetric4 != null && cbMetric4.isChecked()) ||
                (cbMetric5 != null && cbMetric5.isChecked()) ||
                (cbMetric6 != null && cbMetric6.isChecked()) ||
                (cbMetric7 != null && cbMetric7.isChecked()) ||
                (cbMetric8 != null && cbMetric8.isChecked()) ||
                (cbMetric9 != null && cbMetric9.isChecked()) ||
                (cbMetric10 != null && cbMetric10.isChecked()) ||
                (cbMetric11 != null && cbMetric11.isChecked()) ||
                (cbMetric12 != null && cbMetric12.isChecked());

        if (!anyMetricChecked) {
            Toast.makeText(this, "Please select at least one report metric", Toast.LENGTH_SHORT).show();
            return;
        }

        String format = "PDF";
        if (rbFormatExcel != null && rbFormatExcel.isChecked()) format = "Excel";
        else if (rbFormatCsv != null && rbFormatCsv.isChecked()) format = "CSV";

        Toast.makeText(this, selectedReportType + " Report generated and exported as " + format + "!", Toast.LENGTH_LONG).show();
        finish();
    }
}
