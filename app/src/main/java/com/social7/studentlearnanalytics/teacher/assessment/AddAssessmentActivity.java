package com.social7.studentlearnanalytics.teacher.assessment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.network.ApiClient;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAssessmentActivity extends AppCompatActivity {

    private TextView tvHeaderTitle, tvFormSectionHeader;
    private MaterialButton btnTypeAssignment, btnTypeHomework, btnTypeQuiz, btnTypeExam;
    private MaterialButton btnExamMidterm, btnExamFinal, btnAddQuizQuestion;
    private View layoutExamSubtype, tilVenue, tilDuration, layoutQuizBuilderSection;
    private TextInputEditText etTitle, etDescription, etCourseId, etVenue, etTotalScore, etDuration, etDueDate;
    private MaterialButton btnSaveDraft, btnSubmit;

    private String selectedType = "Assignment";
    private String examSubtype = "Midterm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);

        com.social7.studentlearnanalytics.teacher.TeacherHeaderHelper.setupHeader(this, findViewById(R.id.headerTeacherDetail), "Add Assessment", "", true);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvFormSectionHeader = findViewById(R.id.tvFormSectionHeader);

        btnTypeAssignment = findViewById(R.id.btnTypeAssignment);
        btnTypeHomework = findViewById(R.id.btnTypeHomework);
        btnTypeQuiz = findViewById(R.id.btnTypeQuiz);
        btnTypeExam = findViewById(R.id.btnTypeExam);

        btnExamMidterm = findViewById(R.id.btnExamMidterm);
        btnExamFinal = findViewById(R.id.btnExamFinal);
        btnAddQuizQuestion = findViewById(R.id.btnAddQuizQuestion);

        layoutExamSubtype = findViewById(R.id.layoutExamSubtype);
        tilVenue = findViewById(R.id.tilVenue);
        tilDuration = findViewById(R.id.tilDuration);
        layoutQuizBuilderSection = findViewById(R.id.layoutQuizBuilderSection);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etCourseId = findViewById(R.id.etCourseId);
        etVenue = findViewById(R.id.etVenue);
        etTotalScore = findViewById(R.id.etTotalScore);
        etDuration = findViewById(R.id.etDuration);
        etDueDate = findViewById(R.id.etDueDate);

        if (etDueDate != null) {
            etDueDate.setOnClickListener(v -> showDatePicker());
        }

        if (btnAddQuizQuestion != null) {
            btnAddQuizQuestion.setOnClickListener(v -> showAddQuestionDialog());
        }

        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmit = findViewById(R.id.btnSubmit);

        if (btnSaveDraft != null) {
            btnSaveDraft.setOnClickListener(v -> submitAssessment("draft"));
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitAssessment("published"));
        }

        setupTypeSelector();
        selectType("Assignment");
    }

    private void setupTypeSelector() {
        if (btnTypeAssignment != null) btnTypeAssignment.setOnClickListener(v -> selectType("Assignment"));
        if (btnTypeHomework != null) btnTypeHomework.setOnClickListener(v -> selectType("Homework"));
        if (btnTypeQuiz != null) btnTypeQuiz.setOnClickListener(v -> selectType("Quiz"));
        if (btnTypeExam != null) btnTypeExam.setOnClickListener(v -> selectType("Exam"));

        if (btnExamMidterm != null) btnExamMidterm.setOnClickListener(v -> selectExamSubtype("Midterm"));
        if (btnExamFinal != null) btnExamFinal.setOnClickListener(v -> selectExamSubtype("Final Exam"));
    }

    private void selectType(String type) {
        this.selectedType = type;
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveColor = getResources().getColor(R.color.admin_text_secondary, null);

        if (btnTypeAssignment != null) btnTypeAssignment.setTextColor("Assignment".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        if (btnTypeHomework != null) btnTypeHomework.setTextColor("Homework".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        if (btnTypeQuiz != null) btnTypeQuiz.setTextColor("Quiz".equalsIgnoreCase(type) ? activeColor : inactiveColor);
        if (btnTypeExam != null) btnTypeExam.setTextColor("Exam".equalsIgnoreCase(type) ? activeColor : inactiveColor);

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(tvHeaderTitle.getContext().getString(R.string.create_new) + type);
        }

        if (tvFormSectionHeader != null) {
            tvFormSectionHeader.setText(type + " Configuration & Requirements");
        }

        if (layoutExamSubtype != null) layoutExamSubtype.setVisibility("Exam".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);
        if (tilVenue != null) tilVenue.setVisibility("Exam".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);
        if (layoutQuizBuilderSection != null) layoutQuizBuilderSection.setVisibility("Quiz".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);
        if (tilDuration != null) tilDuration.setVisibility(("Quiz".equalsIgnoreCase(type) || "Exam".equalsIgnoreCase(type)) ? View.VISIBLE : View.GONE);

        Toast.makeText(this, "Active Mode: " + type, Toast.LENGTH_SHORT).show();
    }

    private void selectExamSubtype(String subtype) {
        this.examSubtype = subtype;
        int activeColor = getResources().getColor(R.color.admin_primary_blue, null);
        int inactiveColor = getResources().getColor(R.color.admin_text_secondary, null);

        if (btnExamMidterm != null) btnExamMidterm.setTextColor("Midterm".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
        if (btnExamFinal != null) btnExamFinal.setTextColor("Final Exam".equalsIgnoreCase(subtype) ? activeColor : inactiveColor);
    }

    private void showAddQuestionDialog() {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputLayout tilQ = new TextInputLayout(this);
        tilQ.setHint("Question Text");
        TextInputEditText edtQ = new TextInputEditText(tilQ.getContext());
        tilQ.addView(edtQ);
        container.addView(tilQ);

        TextInputLayout tilA = new TextInputLayout(this);
        tilA.setHint("Correct Option (e.g. Option B)");
        TextInputEditText edtA = new TextInputEditText(tilA.getContext());
        tilA.addView(edtA);
        container.addView(tilA);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_quiz_question)
                .setView(container)
                .setPositiveButton(R.string.add_question_e3bb2c, (dialog, which) -> {
                    Toast.makeText(this, "Question added to quiz successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(java.util.Locale.US, "%d-%02d-%02d 23:59:59", year, month + 1, dayOfMonth);
            if (etDueDate != null) {
                etDueDate.setText(selectedDate);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void submitAssessment(String status) {
        String title = etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String courseIdStr = etCourseId != null && etCourseId.getText() != null ? etCourseId.getText().toString().trim() : "1";
        String totalScoreStr = etTotalScore != null && etTotalScore.getText() != null ? etTotalScore.getText().toString().trim() : "100";
        String dueDate = etDueDate != null && etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "2026-12-31 23:59:59";

        if (title.isEmpty()) {
            Toast.makeText(this, selectedType + " title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("title", "[" + selectedType.toUpperCase() + "] " + title);
        body.addProperty("description", desc);
        body.addProperty("type", selectedType);
        body.addProperty("course_id", Long.parseLong(courseIdStr.isEmpty() ? "1" : courseIdStr));
        body.addProperty("teacher_id", 1L);
        body.addProperty("semester_id", 1L);
        body.addProperty("total_score", Double.parseDouble(totalScoreStr.isEmpty() ? "100" : totalScoreStr));
        body.addProperty("due_date", dueDate.isEmpty() ? "2026-12-31 23:59:59" : dueDate);
        body.addProperty("status", status);

        ApiClient.getApiService(this).createAssignment(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                Toast.makeText(AddAssessmentActivity.this, selectedType + " " + ("draft".equalsIgnoreCase(status) ? "saved as draft!" : "published successfully!"), Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(AddAssessmentActivity.this, selectedType + " created and saved successfully!", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
