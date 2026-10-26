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

    private String loadedStudentCode = "";
    private String loadedStudentName = "";
    private String loadedDepartment = "";

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

        Spinner exportSpinner = findViewById(R.id.export_spinner);
        if (exportSpinner != null) {
            String[] semesters = {"Semester 1, 2024", "Semester 2, 2024", "Semester 1, 2025"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            exportSpinner.setAdapter(adapter);
        }

        Button btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        if (btnDownloadPdf != null) {
            btnDownloadPdf.setOnClickListener(v -> generateAndOpenPdfReport());
        }

        fetchReportData();
    }

    private void fetchReportData() {
        ApiClient.getApiService(this).getStudentDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
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

                        if (tvReportAttendance != null) {
                            tvReportAttendance.setText(data.getAttendanceRateFormatted());
                        }
                        if (tvReportAssignment != null) {
                            tvReportAssignment.setText(data.getAssignmentScoreFormatted());
                        }
                        if (tvReportQuiz != null) {
                            tvReportQuiz.setText(data.getQuizScoreFormatted());
                        }
                        if (tvReportGpa != null) {
                            tvReportGpa.setText(data.getGpaFormatted());
                        }
                        if (tvReportRiskLevel != null) {
                            tvReportRiskLevel.setText(data.getRiskLevel());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                Log.e("SLAS_REPORT", "Failed to fetch student report data: " + t.getMessage());
            }
        });
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
        y += 40;

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
