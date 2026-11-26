package com.social7.studentlearnanalytics.student;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.DashboardResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.storage.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFragment extends Fragment {

    private SessionManager sessionManager;

    private TextView tvReportStudentName;
    private TextView tvReportStudentId;
    private TextView tvReportDepartment;
    private TextView tvReportAttendance;
    private TextView tvReportAssignment;
    private TextView tvReportQuiz;
    private TextView tvReportExamAverage;
    private TextView tvReportGpa;
    private TextView tvReportAcademicStanding;
    private TextView tvReportPredictedScore;
    private TextView tvReportRiskLevel;
    private TextView tvReportSemesterTitle;
    private TextView tvReportSemesterSubtitle;
    private Spinner spinnerSemester;
    private ProgressBar progressBar;
    private LinearLayout layoutCoursesContainer;

    private String loadedStudentCode = "";
    private String loadedStudentName = "";
    private String loadedDepartment = "";
    private String loadedYearLevel = "";
    private List<DashboardResponse.SemesterInfo> availableSemesters = new ArrayList<>();
    private Long selectedSemesterId = null;
    private List<DashboardResponse.EnrollmentInfo> currentEnrollments = new ArrayList<>();

    public ReportFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.activity_learning_report,
                container,
                false
        );

        sessionManager = new SessionManager(requireContext());

        setupUI(view);
        fetchReportData();

        return view;
    }

    private void setupUI(View view) {
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).setupStudentHeader(
                    view, getString(R.string.academic_reports), "Review your academic progress and performance"
            );
        }

        tvReportStudentName = view.findViewById(R.id.tvReportStudentName);
        tvReportStudentId = view.findViewById(R.id.tvReportStudentId);
        tvReportDepartment = view.findViewById(R.id.tvReportDepartment);
        tvReportAttendance = view.findViewById(R.id.tvReportAttendance);
        tvReportAssignment = view.findViewById(R.id.tvReportAssignment);
        tvReportQuiz = view.findViewById(R.id.tvReportQuiz);
        tvReportExamAverage = view.findViewById(R.id.tvReportExamAverage);
        tvReportGpa = view.findViewById(R.id.tvReportGpa);
        tvReportAcademicStanding = view.findViewById(R.id.tvReportAcademicStanding);
        tvReportPredictedScore = view.findViewById(R.id.tvReportPredictedScore);
        tvReportRiskLevel = view.findViewById(R.id.tvReportRiskLevel);
        tvReportSemesterTitle = view.findViewById(R.id.tvReportSemesterTitle);
        tvReportSemesterSubtitle = view.findViewById(R.id.tvReportSemesterSubtitle);
        spinnerSemester = view.findViewById(R.id.spinnerSemester);
        progressBar = view.findViewById(R.id.progressBar);
        layoutCoursesContainer = view.findViewById(R.id.layoutCoursesContainer);

        Button btnDownloadPdf = view.findViewById(R.id.btnDownloadPdf);
        if (btnDownloadPdf != null) {
            btnDownloadPdf.setOnClickListener(v -> generateAndOpenPdfReport());
        }
    }

    private void fetchReportData() {
        if (getContext() == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext()).getStudentDashboardFiltered(selectedSemesterId, null)
                .enqueue(new Callback<DashboardResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            DashboardResponse.DashboardData data = response.body().getData();
                            if (data != null) {
                                bindReportData(data);
                            } else {
                                Toast.makeText(getContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Log.e("SLAS_REPORT", "Failed to fetch student report data: " + t.getMessage());
                        Toast.makeText(getContext(), getString(R.string.network_error) + (t.getMessage() != null ? t.getMessage() : ""), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindReportData(DashboardResponse.DashboardData data) {
        DashboardResponse.StudentInfo student = data.getStudent();
        if (student != null) {
            String code = student.getStudentCode();
            String name = student.getDisplayName();
            String dept = student.getDepartmentName();
            Integer ylNum = student.getYearLevelNumber();
            String year = ylNum != null
                    ? com.social7.studentlearnanalytics.utils.YearLevelHelper.toFullLabel(getContext(), ylNum)
                    : student.getYearLevelFormatted();
            loadedStudentCode = code != null ? code : "";
            loadedStudentName = name != null ? name : "";
            loadedDepartment = dept != null ? dept : "";
            loadedYearLevel = year != null ? year : "";
        }
        if ((loadedStudentCode == null || loadedStudentCode.isEmpty()) && sessionManager != null) {
            String uid = sessionManager.getUserId();
            loadedStudentCode = uid != null ? uid : "";
        }
        if ((loadedStudentName == null || loadedStudentName.isEmpty()) && sessionManager != null) {
            String sname = sessionManager.getStudentName();
            loadedStudentName = sname != null ? sname : "";
        }

        if (tvReportStudentName != null) {
            tvReportStudentName.setText(tvReportStudentName.getContext().getString(R.string.student_name_4f287a) + (!loadedStudentName.isEmpty() ? loadedStudentName : "N/A"));
        }
        if (tvReportStudentId != null) {
            tvReportStudentId.setText(tvReportStudentId.getContext().getString(R.string.student_id_a8dd9e) + (!loadedStudentCode.isEmpty() ? loadedStudentCode : "N/A"));
        }
        if (tvReportDepartment != null) {
            tvReportDepartment.setText(tvReportDepartment.getContext().getString(R.string.department_dedb12) + (!loadedDepartment.isEmpty() ? loadedDepartment : "N/A")
                    + (!loadedYearLevel.isEmpty() ? " • " + loadedYearLevel : ""));
        }

        if (tvReportAttendance != null) {
            tvReportAttendance.setText(tvReportAttendance.getContext().getString(R.string.attendance_rate_58a095) + data.getAttendanceRateFormatted());
        }
        if (tvReportAssignment != null) {
            tvReportAssignment.setText(tvReportAssignment.getContext().getString(R.string.assignment_average) + data.getAssignmentScoreFormatted());
        }
        if (tvReportQuiz != null) {
            tvReportQuiz.setText(tvReportQuiz.getContext().getString(R.string.quiz_average) + data.getQuizScoreFormatted());
        }
        if (tvReportExamAverage != null) {
            tvReportExamAverage.setText(tvReportExamAverage.getContext().getString(R.string.exam_average) + data.getExamScoreFormatted());
        }
        if (tvReportGpa != null) {
            tvReportGpa.setText("Cumulative GPA: " + data.getGpaFormatted());
        }
        if (tvReportAcademicStanding != null) {
            String standing = (data.getGpaSummary() != null && data.getGpaSummary().academicStanding != null)
                    ? data.getGpaSummary().academicStanding : "Unavailable";
            tvReportAcademicStanding.setText("Academic Standing: " + standing);
        }
        if (tvReportPredictedScore != null) {
            tvReportPredictedScore.setText("Predicted Final Score: " + data.getPredictedScoreFormatted());
        }
        if (tvReportRiskLevel != null) {
            tvReportRiskLevel.setText(tvReportRiskLevel.getContext().getString(R.string.academic_risk_level_1f386a) + data.getRiskLevel());
        }

        currentEnrollments = data.getEnrollments();
        renderCourses();

        if (availableSemesters.isEmpty() && data.getSemesters() != null && !data.getSemesters().isEmpty()) {
            availableSemesters = data.getSemesters();
            setupSemesterSpinner();
        }
    }

    private void setupSemesterSpinner() {
        if (spinnerSemester == null || getContext() == null) return;
        List<String> semesterNames = new ArrayList<>();
        semesterNames.add("All Semesters");
        for (DashboardResponse.SemesterInfo sem : availableSemesters) {
            semesterNames.add(sem.name != null ? sem.name : "Semester ID: " + sem.id);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, semesterNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);

        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Long newSemesterId = null;
                String selectedName = "All Semesters";
                if (position > 0 && position - 1 < availableSemesters.size()) {
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
                if (tvReportSemesterSubtitle != null) tvReportSemesterSubtitle.setText(tvReportSemesterSubtitle.getContext().getString(R.string.academic_progress_report_for) + selectedName);

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
        if (layoutCoursesContainer == null || getContext() == null) return;
        layoutCoursesContainer.setVisibility(View.VISIBLE);
        layoutCoursesContainer.removeAllViews();

        if (currentEnrollments == null || currentEnrollments.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No courses enrolled for this period.");
            emptyView.setTextColor(Color.parseColor("#64748B"));
            emptyView.setPadding(0, 12, 0, 12);
            layoutCoursesContainer.addView(emptyView);
            return;
        }

        for (DashboardResponse.EnrollmentInfo enr : currentEnrollments) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 12, 0, 12);

            TextView tvCodeName = new TextView(requireContext());
            String courseText = (enr.course != null && enr.course.courseCode != null ? enr.course.courseCode : "") + " - " +
                    (enr.course != null && enr.course.courseName != null ? enr.course.courseName : "Course");
            tvCodeName.setText(courseText);
            tvCodeName.setTextColor(Color.parseColor("#18245A"));
            tvCodeName.setTextSize(13f);
            tvCodeName.setTypeface(null, Typeface.BOLD);
            row.addView(tvCodeName);

            LinearLayout detailsRow = new LinearLayout(requireContext());
            detailsRow.setOrientation(LinearLayout.HORIZONTAL);
            detailsRow.setPadding(0, 4, 0, 0);

            TextView tvScore = new TextView(requireContext());
            tvScore.setText(tvScore.getContext().getString(R.string.score_6b90c9) + (enr.finalScore != null ? enr.finalScore : "N/A"));
            tvScore.setTextColor(Color.parseColor("#64748B"));
            tvScore.setTextSize(12f);
            tvScore.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvScore);

            TextView tvGrade = new TextView(requireContext());
            tvGrade.setText(tvGrade.getContext().getString(R.string.grade) + (enr.grade != null ? enr.grade : "N/A"));
            tvGrade.setTextColor(Color.parseColor("#64748B"));
            tvGrade.setTextSize(12f);
            tvGrade.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvGrade);

            TextView tvStatus = new TextView(requireContext());
            tvStatus.setText(tvStatus.getContext().getString(R.string.status_b61406) + (enr.status != null ? enr.status.toUpperCase() : "N/A"));
            tvStatus.setTextColor(Color.parseColor("#64748B"));
            tvStatus.setTextSize(12f);
            tvStatus.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            detailsRow.addView(tvStatus);

            row.addView(detailsRow);
            layoutCoursesContainer.addView(row);
        }
    }

    private void generateAndOpenPdfReport() {
        if (getContext() == null) return;

        String studentName = !loadedStudentName.isEmpty() ? loadedStudentName : (sessionManager != null ? sessionManager.getStudentName() : "");
        String studentId = !loadedStudentCode.isEmpty() ? loadedStudentCode : (sessionManager != null ? sessionManager.getUserId() : "");

        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Generating PDF Report")
                .setMessage("Creating official Learning Analytics PDF document for " + studentName + " (" + studentId + ")...")
                .setCancelable(false)
                .show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getContext() == null) return;
            progressDialog.dismiss();

            File pdfFile = createPdfFileOnDisk(studentName, studentId);
            if (pdfFile != null && pdfFile.exists()) {
                Toast.makeText(getContext(), "PDF saved: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();
                openPdfDocument(pdfFile);
            } else {
                Toast.makeText(getContext(), "Failed to generate PDF document", Toast.LENGTH_SHORT).show();
            }
        }, 800);
    }

    private File createPdfFileOnDisk(String studentName, String studentId) {
        if (getContext() == null) return null;

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
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
        y += 22;
        canvas.drawText("Official Academic Performance & Learning Report", 40, y, bodyPaint);
        y += 18;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E5EDFA"));
        linePaint.setStrokeWidth(2f);
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 28;

        canvas.drawText("Student Profile Information", 40, y, headerPaint);
        y += 20;
        canvas.drawText("• Student Name: " + studentName, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Student ID: " + studentId, 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Faculty / Major: " + (!loadedDepartment.isEmpty() ? loadedDepartment : "N/A"), 40, y, bodyPaint);
        y += 18;
        canvas.drawText("• Year Level: " + (!loadedYearLevel.isEmpty() ? loadedYearLevel : "N/A"), 40, y, bodyPaint);
        y += 28;

        canvas.drawText("Academic Metrics Summary", 40, y, headerPaint);
        y += 20;

        String[] metrics = {
                tvReportAttendance != null ? tvReportAttendance.getText().toString() : "Attendance Rate: N/A",
                tvReportAssignment != null ? tvReportAssignment.getText().toString() : "Assignment Average: N/A",
                tvReportQuiz != null ? tvReportQuiz.getText().toString() : "Quiz Average: N/A",
                tvReportExamAverage != null ? tvReportExamAverage.getText().toString() : "Exam Average: N/A",
                tvReportGpa != null ? tvReportGpa.getText().toString() : "Cumulative GPA: N/A",
                tvReportAcademicStanding != null ? tvReportAcademicStanding.getText().toString() : "Academic Standing: N/A",
                tvReportPredictedScore != null ? tvReportPredictedScore.getText().toString() : "Predicted Final Score: N/A",
                tvReportRiskLevel != null ? tvReportRiskLevel.getText().toString() : "Academic Risk Level: N/A"
        };
        for (String m : metrics) {
            canvas.drawText("• " + m, 40, y, bodyPaint);
            y += 18;
        }
        y += 16;

        canvas.drawText("Enrolled Courses", 40, y, headerPaint);
        y += 20;

        if (currentEnrollments != null && !currentEnrollments.isEmpty()) {
            for (DashboardResponse.EnrollmentInfo enr : currentEnrollments) {
                if (y > 760) break;
                String code = enr.course != null && enr.course.courseCode != null ? enr.course.courseCode : "N/A";
                String name = enr.course != null && enr.course.courseName != null ? enr.course.courseName : "N/A";
                String score = enr.finalScore != null ? String.valueOf(enr.finalScore) : "N/A";
                String grade = enr.grade != null ? enr.grade : "N/A";
                canvas.drawText("• " + code + " - " + name + " | Score: " + score + " | Grade: " + grade, 40, y, bodyPaint);
                y += 16;
            }
        } else {
            canvas.drawText("• No courses enrolled for this period.", 40, y, bodyPaint);
            y += 18;
        }

        canvas.drawText("Report Generated On: " + new Date(), 40, 800, bodyPaint);
        canvas.drawText("Page 1 of 1", 500, 800, bodyPaint);

        pdfDocument.finishPage(page);

        File outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (outputDir == null) outputDir = requireContext().getCacheDir();
        File pdfFile = new File(outputDir, "SLAS_Learning_Report_" + studentId + ".pdf");

        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            pdfDocument.writeTo(out);
        } catch (IOException e) {
            Log.e("SLAS_PDF", "Error saving PDF: " + e.getMessage());
            return null;
        } finally {
            pdfDocument.close();
        }

        return pdfFile;
    }

    private void openPdfDocument(File pdfFile) {
        if (getContext() == null || pdfFile == null || !pdfFile.exists()) return;
        try {
            Uri contentUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open Learning Report PDF"));
        } catch (Exception e) {
            Log.e("SLAS_PDF", "No PDF viewer app found or Intent error: " + e.getMessage());
        }
    }
}
