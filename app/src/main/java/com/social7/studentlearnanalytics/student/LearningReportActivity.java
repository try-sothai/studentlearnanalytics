package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;
import java.util.List;
import java.util.ArrayList;

public class LearningReportActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextView tvReportStudentName;
    private TextView tvReportStudentId;
    private TextView tvReportDepartment;
    private TextView tvReportAttendance;
    private TextView tvReportAssignment;
    private TextView tvReportQuiz;
    private TextView tvReportGpa;
    private TextView tvReportRiskLevel;
    
    private Spinner spinnerReportType;
    private Spinner spinnerSemester;
    private LinearLayout layoutCoursesContainer;

    private String loadedStudentCode = "";
    private String loadedStudentName = "";
    private String loadedDepartment = "";
    
    private android.widget.ProgressBar progressBar;
    private TextView tvReportSemesterTitle;
    private TextView tvReportSemesterSubtitle;

    private List<DashboardResponse.SemesterInfo> availableSemesters = new ArrayList<>();
    private Long selectedSemesterId = null;
    private List<DashboardResponse.EnrollmentInfo> currentEnrollments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_report);

        sessionManager = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvReportStudentName = findViewById(R.id.tvReportStudentName);
        tvReportStudentId = findViewById(R.id.tvReportStudentId);
        tvReportDepartment = findViewById(R.id.tvReportDepartment);
        tvReportAttendance = findViewById(R.id.tvReportAttendance);
        tvReportAssignment = findViewById(R.id.tvReportAssignment);
        tvReportQuiz = findViewById(R.id.tvReportQuiz);
        tvReportGpa = findViewById(R.id.tvReportGpa);
        tvReportRiskLevel = findViewById(R.id.tvReportRiskLevel);
        layoutCoursesContainer = findViewById(R.id.layoutCoursesContainer);
        progressBar = findViewById(R.id.progressBar);
        tvReportSemesterTitle = findViewById(R.id.tvReportSemesterTitle);
        tvReportSemesterSubtitle = findViewById(R.id.tvReportSemesterSubtitle);

        spinnerReportType = findViewById(R.id.spinnerReportType);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        
        if (spinnerReportType != null) {
            String[] types = {"Overall Academic Performance"};
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReportType.setAdapter(typeAdapter);
        }

        Button btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        if (btnDownloadPdf != null) {
            btnDownloadPdf.setOnClickListener(v -> generateAndOpenPdfReport());
        }

        fetchReportData();
    }

    private void fetchReportData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getStudentDashboardFiltered(selectedSemesterId, null).enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !isFinishing() && !isDestroyed()) {
                    DashboardResponse.DashboardData data = response.body().getData();
                    if (data != null) {
                        DashboardResponse.StudentInfo student = data.getStudent();
                        if (student != null) {
                            loadedStudentCode = student.getStudentCode();
                            loadedStudentName = student.getDisplayName();
                            loadedDepartment = student.getDepartmentName();

                            if (tvReportStudentId != null && !loadedStudentCode.isEmpty()) {
                                tvReportStudentId.setText(loadedStudentCode);
                            }
                            if (tvReportStudentName != null && !loadedStudentName.isEmpty()) {
                                tvReportStudentName.setText(loadedStudentName);
                            }
                            if (tvReportDepartment != null && !loadedDepartment.isEmpty()) {
                                tvReportDepartment.setText(loadedDepartment);
                            }
                        }

                        if (tvReportAttendance != null) tvReportAttendance.setText(data.getAttendanceRateFormatted());
                        if (tvReportAssignment != null) tvReportAssignment.setText(data.getAssignmentScoreFormatted());
                        if (tvReportQuiz != null) tvReportQuiz.setText(data.getQuizScoreFormatted());
                        if (tvReportGpa != null) tvReportGpa.setText(data.getGpaFormatted());
                        if (tvReportRiskLevel != null) tvReportRiskLevel.setText(data.getRiskLevel());

                        currentEnrollments = data.getEnrollments();
                        renderCourses();

                        if (availableSemesters.isEmpty() && data.getSemesters() != null && !data.getSemesters().isEmpty()) {
                            availableSemesters = data.getSemesters();
                            setupSemesterSpinner();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("SLAS_REPORT", "Failed to fetch student report data: " + t.getMessage());
            }
        });
    }

    private void setupSemesterSpinner() {
        if (spinnerSemester == null) return;
        List<String> semesterNames = new ArrayList<>();
        semesterNames.add("All Semesters");
        for (DashboardResponse.SemesterInfo sem : availableSemesters) {
            semesterNames.add(sem.name != null ? sem.name : "Semester ID: " + sem.id);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesterNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);
        
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Long newSemesterId = null;
                String selectedName = "All Semesters";
                if (position > 0) {
                    try {
                        DashboardResponse.SemesterInfo sem = availableSemesters.get(position - 1);
                        Object semIdObj = sem.id;
                        selectedName = sem.name != null ? sem.name : "Semester ID: " + sem.id;
                        if (semIdObj instanceof Number) {
                            newSemesterId = ((Number) semIdObj).longValue();
                        } else if (semIdObj instanceof String) {
                            newSemesterId = Long.parseLong((String) semIdObj);
                        }
                    } catch (Exception ignored) {}
                }
                
                if (tvReportSemesterTitle != null) tvReportSemesterTitle.setText(selectedName);
                if (tvReportSemesterSubtitle != null) tvReportSemesterSubtitle.setText(selectedName);
                
                if ((selectedSemesterId == null && newSemesterId != null) ||
                    (selectedSemesterId != null && !selectedSemesterId.equals(newSemesterId))) {
                    selectedSemesterId = newSemesterId;
                    fetchReportData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void renderCourses() {
        if (layoutCoursesContainer == null) return;
        layoutCoursesContainer.removeAllViews();
        
        if (currentEnrollments == null || currentEnrollments.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No courses enrolled for this period.");
            emptyView.setTextColor(Color.parseColor("#64748B"));
            emptyView.setPadding(0, 16, 0, 16);
            layoutCoursesContainer.addView(emptyView);
            return;
        }

        for (DashboardResponse.EnrollmentInfo enr : currentEnrollments) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 16, 0, 16);

            TextView tvCodeName = new TextView(this);
            String courseText = (enr.course != null && enr.course.courseCode != null ? enr.course.courseCode : "") + " - " + 
                                (enr.course != null && enr.course.courseName != null ? enr.course.courseName : "Unknown Course");
            tvCodeName.setText(courseText);
            tvCodeName.setTextColor(Color.parseColor("#18245A")); // Dark navy matches Performance Details
            tvCodeName.setTextSize(14f);
            tvCodeName.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvCodeName);

            LinearLayout detailsRow = new LinearLayout(this);
            detailsRow.setOrientation(LinearLayout.HORIZONTAL);
            detailsRow.setPadding(0, 8, 0, 0);

            TextView tvScore = new TextView(this);
            tvScore.setText(String.format("Score: %s", (enr.finalScore != null ? enr.finalScore : "N/A")));
            tvScore.setTextColor(Color.parseColor("#7A86A5")); // Muted blue-gray
            tvScore.setTextSize(13f);
            tvScore.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvScore);

            TextView tvGrade = new TextView(this);
            tvGrade.setText(String.format("Grade: %s", (enr.grade != null ? enr.grade : "N/A")));
            tvGrade.setTextColor(Color.parseColor("#7A86A5"));
            tvGrade.setTextSize(13f);
            tvGrade.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvGrade);
            
            TextView tvStatus = new TextView(this);
            tvStatus.setText(String.format("Status: %s", (enr.status != null ? enr.status.toUpperCase() : "N/A")));
            tvStatus.setTextColor(Color.parseColor("#7A86A5"));
            tvStatus.setTextSize(13f);
            tvStatus.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvStatus);

            row.addView(detailsRow);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#E8ECF7")); // Light gray-blue
            divider.setPadding(0, 8, 0, 0);
            row.addView(divider);

            layoutCoursesContainer.addView(row);
        }
    }

    private void generateAndOpenPdfReport() {
        String studentName = !loadedStudentName.isEmpty() ? loadedStudentName : (sessionManager != null ? sessionManager.getStudentName() : "");
        String studentId = !loadedStudentCode.isEmpty() ? loadedStudentCode : (sessionManager != null ? sessionManager.getUserId() : "");

        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Generating PDF Report")
                .setMessage("Creating official Learning Analytics PDF document for " + studentName + " (" + studentId + ")...")
                .setCancelable(false)
                .show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            progressDialog.dismiss();

            File pdfFile = createPdfFileOnDisk(studentName, studentId);
            if (pdfFile != null && pdfFile.exists()) {
                Toast.makeText(this, "PDF saved: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();
                openPdfDocument(pdfFile);
            } else {
                Toast.makeText(this, "Failed to generate PDF document", Toast.LENGTH_SHORT).show();
            }
        }, 1200);
    }

    private File createPdfFileOnDisk(String studentName, String studentId) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 Page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#101A70"));
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#155DFC"));
        headerPaint.setTextSize(13f);
        headerPaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setColor(Color.parseColor("#172554"));
        bodyPaint.setTextSize(11f);

        int y = 50;
        canvas.drawText("STUDENT LEARNING ANALYTICS SYSTEM (SLAS)", 40, y, titlePaint);
        y += 24;
        canvas.drawText("Official Academic Performance & Learning Report", 40, y, bodyPaint);
        y += 20;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E5EDFA"));
        linePaint.setStrokeWidth(2f);
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 30;

        canvas.drawText("Student Profile Information", 40, y, headerPaint);
        y += 20;
        canvas.drawText("• Student Name: " + studentName, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Student ID: " + studentId, 40, y, bodyPaint);
        y += 18;
        String deptStr = !loadedDepartment.isEmpty() ? loadedDepartment : "Computer Science";
        canvas.drawText("• Department: " + deptStr, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Major: Software Engineering", 40, y, bodyPaint);
        y += 30;

        canvas.drawText("Academic Metrics Summary", 40, y, headerPaint);
        y += 20;

        String att = tvReportAttendance != null ? tvReportAttendance.getText().toString() : "N/A";
        String assign = tvReportAssignment != null ? tvReportAssignment.getText().toString() : "N/A";
        String quiz = tvReportQuiz != null ? tvReportQuiz.getText().toString() : "N/A";
        String gpa = tvReportGpa != null ? tvReportGpa.getText().toString() : "N/A";
        String risk = tvReportRiskLevel != null ? tvReportRiskLevel.getText().toString() : "N/A";

        canvas.drawText("• Attendance Rate: " + att, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Assignment Average: " + assign, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Quiz Average: " + quiz, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Cumulative GPA: " + gpa, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Academic Risk Level: " + risk, 40, y, bodyPaint);
        y += 30;

        canvas.drawText("Enrolled Courses", 40, y, headerPaint);
        y += 20;

        if (currentEnrollments != null && !currentEnrollments.isEmpty()) {
            Paint tableHeaderPaint = new Paint();
            tableHeaderPaint.setColor(Color.parseColor("#155DFC"));
            tableHeaderPaint.setTextSize(10f);
            tableHeaderPaint.setFakeBoldText(true);

            Paint tableBodyPaint = new Paint();
            tableBodyPaint.setColor(Color.parseColor("#172554"));
            tableBodyPaint.setTextSize(10f);

            canvas.drawText("Course Code", 40, y, tableHeaderPaint);
            canvas.drawText("Course Name", 140, y, tableHeaderPaint);
            canvas.drawText("Score", 420, y, tableHeaderPaint);
            canvas.drawText("Grade", 480, y, tableHeaderPaint);
            canvas.drawText("Status", 530, y, tableHeaderPaint);
            y += 15;
            canvas.drawLine(40, y, 555, y, linePaint);
            y += 15;

            for (DashboardResponse.EnrollmentInfo enr : currentEnrollments) {
                if (y > 780) { // Very simple pagination fallback
                    canvas.drawText("... (More courses on next page)", 40, y, tableBodyPaint);
                    break;
                }
                String code = enr.course != null && enr.course.courseCode != null ? enr.course.courseCode : "N/A";
                String name = enr.course != null && enr.course.courseName != null ? enr.course.courseName : "N/A";
                if (name.length() > 30) name = name.substring(0, 27) + "..."; // prevent overlap

                String score = enr.finalScore != null ? String.valueOf(enr.finalScore) : "N/A";
                String grade = enr.grade != null ? enr.grade : "N/A";
                String status = enr.status != null ? enr.status : "N/A";

                canvas.drawText(code, 40, y, tableBodyPaint);
                canvas.drawText(name, 140, y, tableBodyPaint);
                canvas.drawText(score, 420, y, tableBodyPaint);
                canvas.drawText(grade, 480, y, tableBodyPaint);
                canvas.drawText(status, 530, y, tableBodyPaint);
                y += 18;
            }
        } else {
            canvas.drawText("No courses enrolled for this period.", 40, y, bodyPaint);
            y += 20;
        }

        y += 20;
        canvas.drawText("Report Generated On: " + new Date(), 40, y, bodyPaint);

        pdfDocument.finishPage(page);

        File outputDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (outputDir == null) outputDir = getCacheDir();
        File pdfFile = new File(outputDir, "SLAS_Learning_Report_" + studentId + ".pdf");

        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            pdfDocument.writeTo(out);
            Log.d("SLAS_PDF", "Official PDF report generated at: " + pdfFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("SLAS_PDF", "Error saving PDF: " + e.getMessage());
            return null;
        } finally {
            pdfDocument.close();
        }

        return pdfFile;
    }

    private void openPdfDocument(File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) return;
        try {
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open Learning Report PDF"));
        } catch (Exception e) {
            Log.e("SLAS_PDF", "No PDF viewer app found or Intent error: " + e.getMessage());
        }
    }
}
