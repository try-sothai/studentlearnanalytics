package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Exam;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

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
                if (exam.getScoreText() != null && !exam.getScoreText().isEmpty()) {
                    tvExamScore.setVisibility(View.VISIBLE);
                    tvExamScore.setText(exam.getScoreText());
                    tvExamScore.setTextColor(Color.parseColor("#10B981"));
                } else if (exam.getStatusLabel() != null && !exam.getStatusLabel().isEmpty()) {
                    tvExamScore.setVisibility(View.VISIBLE);
                    tvExamScore.setText(exam.getStatusLabel());
                    tvExamScore.setTextColor("Completed".equalsIgnoreCase(exam.getStatusLabel())
                            ? Color.parseColor("#64748B")
                            : Color.parseColor("#2563EB"));
                } else {
                    tvExamScore.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> showExamDetailsDialog(context, exam));
        }

        private void showExamDetailsDialog(Context context, Exam exam) {
            StringBuilder msg = new StringBuilder();
            if (exam.getTitle() != null && !exam.getTitle().isEmpty()) {
                msg.append("Title: ").append(exam.getTitle()).append("\n");
            }
            if (exam.getCourseCode() != null && !exam.getCourseCode().isEmpty()) {
                msg.append("Course: ").append(exam.getCourseCode()).append(" – ").append(exam.getCourseName()).append("\n");
            } else if (exam.getCourseName() != null) {
                msg.append("Course: ").append(exam.getCourseName()).append("\n");
            }
            if (exam.getClassName() != null && !exam.getClassName().isEmpty()) {
                msg.append("Class: ").append(exam.getClassName()).append("\n");
            }
            if (exam.getSemesterName() != null && !exam.getSemesterName().isEmpty()) {
                msg.append("Semester: ").append(exam.getSemesterName()).append("\n");
            }
            if (exam.getExamType() != null && !exam.getExamType().isEmpty()) {
                msg.append("Category: ").append(exam.getExamType()).append("\n");
            }
            if (exam.getExamDateFormatted() != null && !exam.getExamDateFormatted().isEmpty()) {
                msg.append("Date: ").append(exam.getExamDateFormatted()).append("\n");
            }
            if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
                msg.append("Duration: ").append(exam.getDurationMinutes()).append(" minutes\n");
            }
            if (exam.getVenue() != null && !exam.getVenue().isEmpty()) {
                msg.append("Venue / Room: ").append(exam.getVenue()).append("\n");
            }
            if (exam.getMaxScore() > 0) {
                msg.append("Total Marks: ").append(exam.getMaxScore()).append("\n");
            }
            if (exam.getStatusLabel() != null && !exam.getStatusLabel().isEmpty()) {
                msg.append("Status: ").append(exam.getStatusLabel()).append("\n");
            }
            if (exam.getScoreText() != null && !exam.getScoreText().isEmpty()) {
                msg.append("\nResult: ").append(exam.getScoreText());
                if (exam.getGrade() != null && !exam.getGrade().isEmpty()) {
                    msg.append(" (Grade ").append(exam.getGrade()).append(")");
                }
                if (exam.getRemarks() != null && !exam.getRemarks().isEmpty()) {
                    msg.append("\nRemarks: ").append(exam.getRemarks());
                }
            }
            if (exam.getDescription() != null && !exam.getDescription().isEmpty()) {
                msg.append("\n\nInstructions:\n").append(exam.getDescription());
            }

            new MaterialAlertDialogBuilder(context)
                    .setTitle(exam.getTitle() != null && !exam.getTitle().isEmpty() ? exam.getTitle() : "Exam Schedule & Details")
                    .setMessage(msg.toString().trim())
                    .setPositiveButton("Close", null)
                    .show();
        }
    }
}
