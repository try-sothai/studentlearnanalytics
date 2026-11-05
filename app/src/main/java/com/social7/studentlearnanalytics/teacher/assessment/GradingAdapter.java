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

public class GradingAdapter extends RecyclerView.Adapter<GradingAdapter.ViewHolder> {

    public static class GradingCardItem {
        public String id;
        public String studentId;
        public String studentName;
        public String assessmentTitle;
        public String details;
        public String scoreBadge;
        public int maxMarks;
        public int currentScore;

        public GradingCardItem(String id, String studentId, String studentName, String assessmentTitle, String details, String scoreBadge, int maxMarks, int currentScore) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.assessmentTitle = assessmentTitle;
            this.details = details;
            this.scoreBadge = scoreBadge;
            this.maxMarks = maxMarks;
            this.currentScore = currentScore;
        }
    }

    public interface OnGradingActionClickListener {
        void onGradeFeedbackClick(GradingCardItem item);
    }

    private final Context context;
    private final List<GradingCardItem> originalList;
    private final List<GradingCardItem> filteredList;
    private final OnGradingActionClickListener listener;

    public GradingAdapter(Context context, List<GradingCardItem> list, OnGradingActionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_grading_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GradingCardItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (GradingCardItem item : originalList) {
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
        private final TextView tvStudentId, tvStudentName, tvAssessmentTitle, tvGradingDetails, tvScoreBadge;
        private final MaterialButton btnEnterGradeFeedback;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvAssessmentTitle = itemView.findViewById(R.id.tvAssessmentTitle);
            tvGradingDetails = itemView.findViewById(R.id.tvGradingDetails);
            tvScoreBadge = itemView.findViewById(R.id.tvScoreBadge);

            btnEnterGradeFeedback = itemView.findViewById(R.id.btnEnterGradeFeedback);
        }

        public void bind(GradingCardItem item, OnGradingActionClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvAssessmentTitle != null) tvAssessmentTitle.setText(item.assessmentTitle);
            if (tvGradingDetails != null) tvGradingDetails.setText(item.details);

            if (tvScoreBadge != null) {
                tvScoreBadge.setText(item.scoreBadge);
                if (item.scoreBadge.contains("Graded") || item.scoreBadge.contains("8") || item.scoreBadge.contains("9")) {
                    tvScoreBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.scoreBadge.contains("Pending")) {
                    tvScoreBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvScoreBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            if (btnEnterGradeFeedback != null) {
                btnEnterGradeFeedback.setOnClickListener(v -> {
                    if (listener != null) listener.onGradeFeedbackClick(item);
                });
            }
        }
    }
}
