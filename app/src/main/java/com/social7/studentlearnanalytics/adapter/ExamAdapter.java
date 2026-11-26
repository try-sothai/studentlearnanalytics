package com.social7.studentlearnanalytics.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Exam;

import java.util.List;
import java.util.Locale;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private final Context context;
    private final List<Exam> examList;

    public ExamAdapter(Context context, List<Exam> examList) {
        this.context = context;
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.bind(context, exam);
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateMonth, tvDateDay, tvCourseName, tvExamDetails, tvExamScore;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateMonth = itemView.findViewById(R.id.tvDateMonth);
            tvDateDay = itemView.findViewById(R.id.tvDateDay);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvExamDetails = itemView.findViewById(R.id.tvExamDetails);
            tvExamScore = itemView.findViewById(R.id.tvExamScore);
        }

        public void bind(Context context, Exam exam) {
            if (tvDateMonth != null) tvDateMonth.setText(exam.getDateMonth());
            if (tvDateDay != null) tvDateDay.setText(exam.getDateDay());
            if (tvCourseName != null) tvCourseName.setText(exam.getCourseName());
            if (tvExamDetails != null) tvExamDetails.setText(exam.getDetails());

            if (tvExamScore != null) {
                if (exam.isPublished() && exam.getScoreText() != null && !exam.getScoreText().isEmpty()) {
                    tvExamScore.setVisibility(View.VISIBLE);
                    tvExamScore.setText(exam.getScoreText());
                    tvExamScore.setTextColor(Color.parseColor("#10B981"));
                } else if (exam.getStatusLabel() != null && !exam.getStatusLabel().isEmpty()) {
                    tvExamScore.setVisibility(View.VISIBLE);
                    tvExamScore.setText(exam.getStatusLabel());
                    String label = exam.getStatusLabel().toLowerCase(Locale.US);
                    if (label.contains("pending")) {
                        tvExamScore.setTextColor(Color.parseColor("#D97706"));
                    } else if (label.contains("published")) {
                        tvExamScore.setTextColor(Color.parseColor("#10B981"));
                    } else if (label.contains("completed")) {
                        tvExamScore.setTextColor(Color.parseColor("#64748B"));
                    } else {
                        tvExamScore.setTextColor(Color.parseColor("#2563EB"));
                    }
                } else {
                    tvExamScore.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> showExamDetailsDialog(context, exam));
        }

        private void showExamDetailsDialog(Context context, Exam exam) {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_student_exam_result);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(
                        (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }

            TextView tvTypeBadge = dialog.findViewById(R.id.tvStudentExamTypeBadge);
            TextView tvStatusBadge = dialog.findViewById(R.id.tvStudentExamStatusBadge);
            TextView tvTitle = dialog.findViewById(R.id.tvStudentExamTitle);
            TextView tvCourseClass = dialog.findViewById(R.id.tvStudentExamCourseClass);
            TextView tvSemester = dialog.findViewById(R.id.tvStudentExamSemester);
            TextView tvDate = dialog.findViewById(R.id.tvStudentExamDate);
            TextView tvTime = dialog.findViewById(R.id.tvStudentExamTime);
            TextView tvDuration = dialog.findViewById(R.id.tvStudentExamDuration);
            TextView tvVenue = dialog.findViewById(R.id.tvStudentExamVenue);
            TextView tvTotalMarks = dialog.findViewById(R.id.tvStudentExamTotalMarks);

            View layoutInstructions = dialog.findViewById(R.id.layoutStudentExamInstructions);
            TextView tvInstructions = dialog.findViewById(R.id.tvStudentExamInstructions);

            View layoutPublished = dialog.findViewById(R.id.layoutStudentPublishedResult);
            TextView tvResultPassFail = dialog.findViewById(R.id.tvStudentResultPassFail);
            TextView tvResultScore = dialog.findViewById(R.id.tvStudentResultScore);
            TextView tvResultPct = dialog.findViewById(R.id.tvStudentResultPercentage);
            TextView tvResultGrade = dialog.findViewById(R.id.tvStudentResultGrade);
            TextView tvResultPubStatus = dialog.findViewById(R.id.tvStudentResultPubStatus);
            TextView tvResultRemarks = dialog.findViewById(R.id.tvStudentResultRemarks);

            View layoutPending = dialog.findViewById(R.id.layoutStudentPendingResult);
            TextView tvPendingTitle = dialog.findViewById(R.id.tvStudentPendingResultTitle);
            TextView tvPendingDesc = dialog.findViewById(R.id.tvStudentPendingResultDesc);

            MaterialButton btnClose = dialog.findViewById(R.id.btnCloseStudentExamDialog);

            String examType = exam.getExamType() != null && !exam.getExamType().isEmpty() ? exam.getExamType() : "Exam";
            tvTypeBadge.setText(examType + " Examination");

            String statusLabel = exam.getStatusLabel() != null && !exam.getStatusLabel().isEmpty() ? exam.getStatusLabel() : "Scheduled";
            tvStatusBadge.setText(statusLabel);
            String stLower = statusLabel.toLowerCase(Locale.US);
            if (stLower.contains("published")) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_green);
                tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
            } else if (stLower.contains("pending")) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_orange);
                tvStatusBadge.setTextColor(Color.parseColor("#D97706"));
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_stat_card_purple);
                tvStatusBadge.setTextColor(Color.parseColor("#4324EB"));
            }

            tvTitle.setText(exam.getTitle() != null && !exam.getTitle().isEmpty() ? exam.getTitle() : exam.getCourseName());

            StringBuilder cc = new StringBuilder();
            if (exam.getCourseCode() != null && !exam.getCourseCode().isEmpty()) {
                cc.append(exam.getCourseCode());
            }
            if (exam.getCourseName() != null && !exam.getCourseName().isEmpty()
                    && !exam.getCourseName().equals(exam.getTitle())) {
                if (cc.length() > 0) cc.append(" • ");
                cc.append(exam.getCourseName());
            }
            if (exam.getClassName() != null && !exam.getClassName().isEmpty()) {
                if (cc.length() > 0) cc.append(" • ");
                cc.append(exam.getClassName());
            }
            tvCourseClass.setText(cc.length() > 0 ? cc.toString() : "Assigned Course");

            if (exam.getSemesterName() != null && !exam.getSemesterName().isEmpty()) {
                tvSemester.setVisibility(View.VISIBLE);
                tvSemester.setText("Semester: " + exam.getSemesterName());
            } else {
                tvSemester.setVisibility(View.GONE);
            }

            tvDate.setText(exam.getExamDateFormatted() != null && !exam.getExamDateFormatted().isEmpty()
                    ? exam.getExamDateFormatted() : (exam.getDateMonth() + " " + exam.getDateDay()));

            String timeRange = Exam.calculateTimeRange(exam.getStartTime(), exam.getEndTime(), exam.getDurationMinutes());
            tvTime.setText(!timeRange.isEmpty() ? timeRange : "TBA");

            String durStr = exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0
                    ? exam.getDurationMinutes() + " mins" : "-";
            tvDuration.setText(durStr + (exam.getMaxScore() > 0 ? " • " + exam.getMaxScore() + " Marks" : ""));

            tvVenue.setText(exam.getVenue() != null && !exam.getVenue().isEmpty() ? exam.getVenue() : "Classroom (Paper Exam)");
            tvTotalMarks.setText("Total Marks: " + (exam.getMaxScore() > 0 ? exam.getMaxScore() : 100));

            if (exam.getDescription() != null && !exam.getDescription().trim().isEmpty()) {
                layoutInstructions.setVisibility(View.VISIBLE);
                tvInstructions.setText(exam.getDescription().trim());
            } else {
                layoutInstructions.setVisibility(View.GONE);
            }

            // Strict result visibility rule: NEVER display scores unless published
            if (exam.isPublished() && exam.getScoreText() != null && !exam.getScoreText().isEmpty()) {
                layoutPublished.setVisibility(View.VISIBLE);
                layoutPending.setVisibility(View.GONE);

                tvResultScore.setText(exam.getScoreText());

                double pct = 0.0;
                if (exam.getPercentage() != null) {
                    pct = exam.getPercentage();
                } else if (exam.getRawScore() != null && exam.getMaxScore() > 0) {
                    pct = (exam.getRawScore() / exam.getMaxScore()) * 100.0;
                }
                tvResultPct.setText(String.format(Locale.US, "%.1f%%", pct));

                String grade = exam.getGrade() != null && !exam.getGrade().isEmpty() ? exam.getGrade() : "-";
                tvResultGrade.setText(grade);

                String passFail = exam.getResultStatus();
                if (passFail == null || passFail.isEmpty()) {
                    passFail = pct >= 50.0 ? "Pass" : "Fail";
                }
                tvResultPassFail.setText(passFail);
                if ("Fail".equalsIgnoreCase(passFail)) {
                    tvResultPassFail.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvResultPassFail.setTextColor(Color.parseColor("#10B981"));
                }

                tvResultPubStatus.setText("Publication Status: Results Published");
                if (exam.getRemarks() != null && !exam.getRemarks().trim().isEmpty()) {
                    tvResultRemarks.setVisibility(View.VISIBLE);
                    tvResultRemarks.setText("Remarks: " + exam.getRemarks().trim());
                } else {
                    tvResultRemarks.setVisibility(View.GONE);
                }
            } else {
                layoutPublished.setVisibility(View.GONE);
                layoutPending.setVisibility(View.VISIBLE);
                if (stLower.contains("published")) {
                    tvPendingTitle.setText("Publication Status: Results Published");
                    tvPendingDesc.setText("No individual score record was found for your enrollment in this examination.");
                } else if (stLower.contains("pending")) {
                    tvPendingTitle.setText("Publication Status: Results Pending");
                    tvPendingDesc.setText("Your teacher has conducted this classroom paper exam and is currently finalizing grades. Results will appear here after publication.");
                } else if (stLower.contains("completed")) {
                    tvPendingTitle.setText("Offline Classroom Examination • Completed");
                    tvPendingDesc.setText("This classroom examination has finished. Your score, percentage, and grade will appear here after grading and publication.");
                } else {
                    tvPendingTitle.setText("Offline Classroom Examination • Scheduled");
                    tvPendingDesc.setText("Please attend the physical examination in the assigned classroom at the scheduled date and time. Results will appear here once graded and published.");
                }
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        }
    }
}
