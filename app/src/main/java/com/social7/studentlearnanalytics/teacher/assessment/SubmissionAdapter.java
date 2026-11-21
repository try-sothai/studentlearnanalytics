package com.social7.studentlearnanalytics.teacher.assessment;

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

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {

    public static class SubmissionCardItem {
        public String id;
        public String studentId;
        public String studentName;
        public String assessmentTitle;
        public String rawAssessmentTitle;
        public String assessmentType = "Assignment";
        public String courseCode = "";
        public String courseName = "";
        public String submittedAt = "";
        public String details;
        public String status;
        public double maxScore = 100.0;
        public Double score = null;
        public String feedback = "";
        public String fileName = "";
        public String fileType = "";
        public String fileSizeLabel = "";
        public String mimeType = "";
        public String previewType = "";
        public boolean hasFile = false;
        public boolean fileExists = false;

        public SubmissionCardItem(String id, String studentId, String studentName, String assessmentTitle, String details, String status) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.assessmentTitle = assessmentTitle;
            this.rawAssessmentTitle = assessmentTitle;
            this.details = details;
            this.status = status;
        }
    }

    public interface OnSubmissionActionClickListener {
        void onViewFileClick(SubmissionCardItem item);
        void onGradeClick(SubmissionCardItem item);
        default void onDetailClick(SubmissionCardItem item) {
            onViewFileClick(item);
        }
    }

    private final Context context;
    private final List<SubmissionCardItem> originalList;
    private final List<SubmissionCardItem> filteredList;
    private final OnSubmissionActionClickListener listener;

    public SubmissionAdapter(Context context, List<SubmissionCardItem> list, OnSubmissionActionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_submission_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubmissionCardItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (SubmissionCardItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.assessmentTitle != null && item.assessmentTitle.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvAssessmentTitle, tvSubmissionDetails, tvStatusBadge;
        private final MaterialButton btnViewSubmission, btnGradeSubmission;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvAssessmentTitle = itemView.findViewById(R.id.tvAssessmentTitle);
            tvSubmissionDetails = itemView.findViewById(R.id.tvSubmissionDetails);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            btnViewSubmission = itemView.findViewById(R.id.btnViewSubmission);
            btnGradeSubmission = itemView.findViewById(R.id.btnGradeSubmission);
        }

        public void bind(SubmissionCardItem item, OnSubmissionActionClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvAssessmentTitle != null) tvAssessmentTitle.setText(item.assessmentTitle);
            if (tvSubmissionDetails != null) tvSubmissionDetails.setText(item.details);

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(item.status);
                if (item.status.contains("On Time") || item.status.contains("Graded")) {
                    tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.status.contains("Late")) {
                    tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            if (btnViewSubmission != null) {
                btnViewSubmission.setOnClickListener(v -> {
                    if (listener != null) listener.onViewFileClick(item);
                });
            }

            if (btnGradeSubmission != null) {
                btnGradeSubmission.setOnClickListener(v -> {
                    if (listener != null) listener.onGradeClick(item);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDetailClick(item);
            });
        }
    }
}
