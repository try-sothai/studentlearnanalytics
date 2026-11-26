package com.social7.studentlearnanalytics.student.assignment;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentSubmissionAdapter extends RecyclerView.Adapter<StudentSubmissionAdapter.ViewHolder> {

    public static class StudentSubmissionItem {
        public int id;
        public int assignmentId;
        public String studentCode = "";
        public String studentName = "";
        public String assignmentTitle = "";
        public String assessmentType = "Assignment";
        public String courseCode = "";
        public String courseName = "";
        public String className = "";
        public String submittedAt = "";
        public String submissionStatus = "";
        public String gradingStatus = "";
        public boolean isPublished = false;
        public Double score = null;
        public double maxScore = 100.0;
        public String feedback = "";
        public String publishedAt = "";
        public String fileName = "";
        public String fileType = "";
        public String fileSizeLabel = "";
        public String mimeType = "";
        public String previewType = "";
        public boolean hasFile = false;
        public boolean fileExists = false;
        public String versionLabel = "Latest Submission";
        public int versionNumber = 1;
        public boolean isLate = false;
    }

    public interface OnStudentSubmissionClickListener {
        void onViewSubmittedFile(StudentSubmissionItem item);
        void onOpenDetail(StudentSubmissionItem item);
    }

    private final Context context;
    private final List<StudentSubmissionItem> items;
    private final OnStudentSubmissionClickListener listener;

    public StudentSubmissionAdapter(
            Context context,
            List<StudentSubmissionItem> items,
            OnStudentSubmissionClickListener listener
    ) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_submission_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseBadge, tvSubmissionStatus, tvAssignmentTitle,
                tvFileSummary, tvSubmittedAt, tvGradingStatus, tvScore, tvFeedback;
        private final MaterialButton btnViewFile, btnViewDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseBadge = itemView.findViewById(R.id.tvSubCourseBadge);
            tvSubmissionStatus = itemView.findViewById(R.id.tvSubSubmissionStatus);
            tvAssignmentTitle = itemView.findViewById(R.id.tvSubAssignmentTitle);
            tvFileSummary = itemView.findViewById(R.id.tvSubFileSummary);
            tvSubmittedAt = itemView.findViewById(R.id.tvSubSubmittedAt);
            tvGradingStatus = itemView.findViewById(R.id.tvSubGradingStatus);
            tvScore = itemView.findViewById(R.id.tvSubScore);
            tvFeedback = itemView.findViewById(R.id.tvSubFeedback);
            btnViewFile = itemView.findViewById(R.id.btnStuViewFile);
            btnViewDetails = itemView.findViewById(R.id.btnStuViewDetails);
        }

        void bind(StudentSubmissionItem item, OnStudentSubmissionClickListener listener) {
            StringBuilder course = new StringBuilder();
            if (!item.courseCode.isEmpty()) course.append(item.courseCode);
            if (!item.courseName.isEmpty()) {
                if (course.length() > 0) course.append(" • ");
                course.append(item.courseName);
            }
            if (tvCourseBadge != null) tvCourseBadge.setText(course.toString());

            if (tvSubmissionStatus != null) {
                tvSubmissionStatus.setText(item.submissionStatus.isEmpty() ? "Submitted" : item.submissionStatus);
                if (item.submissionStatus.contains("Late")) {
                    tvSubmissionStatus.setTextColor(Color.parseColor("#D97706"));
                } else if (item.isPublished) {
                    tvSubmissionStatus.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvSubmissionStatus.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            if (tvAssignmentTitle != null) tvAssignmentTitle.setText(item.assignmentTitle);

            if (tvFileSummary != null) {
                if (!item.hasFile) {
                    tvFileSummary.setText(R.string.no_submission_file_attached);
                } else {
                    StringBuilder fs = new StringBuilder("File: ").append(item.fileName);
                    if (!item.fileType.isEmpty()) {
                        fs.append(" (").append(item.fileType);
                        if (!item.fileSizeLabel.isEmpty()) fs.append(" • ").append(item.fileSizeLabel);
                        fs.append(")");
                    }
                    if (!item.fileExists) {
                        fs.append(" — File Missing");
                    }
                    tvFileSummary.setText(fs.toString());
                }
            }

            if (tvSubmittedAt != null) {
                String verTag = "v" + Math.max(1, item.versionNumber)
                        + (item.versionLabel != null && !item.versionLabel.isEmpty() ? (" • " + item.versionLabel) : "");
                if (item.isLate) {
                    verTag += " • Late";
                }
                tvSubmittedAt.setText("Submitted: " + (item.submittedAt.isEmpty() ? "-" : item.submittedAt)
                        + " • " + verTag);
            }

            if (tvGradingStatus != null) {
                tvGradingStatus.setText(item.isPublished
                        ? "Published (Graded)"
                        : itemView.getContext().getString(R.string.waiting_for_teacher_grading));
                tvGradingStatus.setTextColor(Color.parseColor(item.isPublished ? "#10B981" : "#D97706"));
            }

            if (tvScore != null) {
                if (item.isPublished && item.score != null) {
                    tvScore.setVisibility(View.VISIBLE);
                    tvScore.setText(String.format(Locale.US, "%s / %s pts", formatNum(item.score), formatNum(item.maxScore)));
                } else {
                    tvScore.setVisibility(View.GONE);
                }
            }

            if (tvFeedback != null) {
                if (item.isPublished && !item.feedback.trim().isEmpty()) {
                    tvFeedback.setVisibility(View.VISIBLE);
                    tvFeedback.setText("Feedback: " + item.feedback);
                } else {
                    tvFeedback.setVisibility(View.GONE);
                }
            }

            if (btnViewFile != null) {
                btnViewFile.setVisibility(item.hasFile ? View.VISIBLE : View.GONE);
                btnViewFile.setOnClickListener(v -> {
                    if (listener != null) listener.onViewSubmittedFile(item);
                });
            }
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> {
                    if (listener != null) listener.onOpenDetail(item);
                });
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOpenDetail(item);
            });
        }

        private static String formatNum(double v) {
            return Math.abs(v - Math.round(v)) < 0.001
                    ? String.valueOf(Math.round(v))
                    : String.format(Locale.US, "%.2f", v);
        }
    }
}
