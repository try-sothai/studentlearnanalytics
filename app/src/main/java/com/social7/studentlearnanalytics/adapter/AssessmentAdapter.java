package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.student.assignment.SubmitAssignmentActivity;
import com.social7.studentlearnanalytics.student.quiz.TakeQuizActivity;

import java.util.List;
import java.util.Locale;

public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder> {

    private final List<Assessment> assessmentList;

    public AssessmentAdapter(List<Assessment> assessmentList) {
        this.assessmentList = assessmentList;
    }

    @NonNull
    @Override
    public AssessmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assessment, parent, false);
        return new AssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssessmentViewHolder holder, int position) {
        Assessment a = assessmentList.get(position);
        holder.titleText.setText(a.getTitle());

        // Course badge (code + name + class)
        if (holder.courseBadgeText != null) {
            StringBuilder courseLine = new StringBuilder();
            if (a.getCourseCode() != null && !a.getCourseCode().isEmpty()) {
                courseLine.append(a.getCourseCode());
            }
            if (a.getCourseName() != null && !a.getCourseName().trim().isEmpty()) {
                if (courseLine.length() > 0) courseLine.append(" • ");
                courseLine.append(a.getCourseName().trim());
            }
            if (a.getClassName() != null && !a.getClassName().trim().isEmpty()) {
                if (courseLine.length() > 0) courseLine.append(" • ");
                courseLine.append(a.getClassName().trim());
            }
            if (courseLine.length() > 0) {
                holder.courseBadgeText.setVisibility(View.VISIBLE);
                holder.courseBadgeText.setText(courseLine.toString());
            } else {
                holder.courseBadgeText.setVisibility(View.GONE);
            }
        }

        // Subtitle: date + type-relevant metadata
        StringBuilder meta = new StringBuilder();
        if (a.getDate() != null && !a.getDate().isEmpty()) {
            meta.append(a.getDate());
        }
        if ("quiz".equalsIgnoreCase(a.getType())) {
            if (a.getQuestionsCount() > 0) {
                meta.append(" • ").append(a.getQuestionsCount()).append(" Qs");
            }
            if (a.getDurationMinutes() > 0) {
                meta.append(" • ").append(a.getDurationMinutes()).append("m");
            }
        }
        if (!a.isGraded() && a.getTotalScore() > 0) {
            meta.append(" • ").append(a.getTotalScore()).append(" pts");
        }
        holder.dateText.setText(meta.toString());

        // Status / Score pill
        String statusBadge = a.getFormattedScore();
        holder.scoreText.setText(statusBadge);

        int badgeColor = resolveStatusColor(a);
        holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(badgeColor));
        holder.scoreText.setTextColor(badgeColor);

        if ("quiz".equalsIgnoreCase(a.getType())) {
            holder.iconView.setImageResource(R.drawable.ic_analytics);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            holder.iconView.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            holder.iconView.setImageResource(R.drawable.ic_assignment);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1EDFF")));
            holder.iconView.setColorFilter(Color.parseColor("#5B3DF5"));
        }

        if (holder.actionHintText != null) {
            String hint = a.getActionLabel();
            if (hint == null || hint.isEmpty()) {
                if ("quiz".equalsIgnoreCase(a.getType())) {
                    hint = a.isInProgress() ? "Continue ›" : (a.isSubmitted() ? "View Result ›" : "Open Quiz ›");
                } else {
                    hint = a.isGraded() ? "View Grade ›" : (a.isSubmitted() ? "View Submission ›" : "Submit ›");
                }
            } else {
                hint = hint + " ›";
            }
            holder.actionHintText.setVisibility(View.VISIBLE);
            holder.actionHintText.setText(hint);
        }

        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            if ("quiz".equalsIgnoreCase(a.getType())) {
                Intent intent = new Intent(ctx, TakeQuizActivity.class);
                intent.putExtra("quiz_id", a.getId());
                ctx.startActivity(intent);
            } else {
                Intent intent = new Intent(ctx, SubmitAssignmentActivity.class);
                intent.putExtra("assignment_id", a.getId());
                ctx.startActivity(intent);
            }
        });
    }

    private int resolveStatusColor(Assessment a) {
        String st = a.getStatusLabel() != null ? a.getStatusLabel().toLowerCase(Locale.US) : "";
        if (a.isGraded() || st.contains("graded") || st.contains("completed")) {
            return Color.parseColor("#10B981");
        }
        if (st.contains("overdue")) {
            return Color.parseColor("#EF4444");
        }
        if (st.contains("closed")) {
            return Color.parseColor("#64748B");
        }
        if (a.isInProgress() || st.contains("progress")) {
            return Color.parseColor("#F59E0B");
        }
        if (a.isSubmitted() || st.contains("submitted")) {
            return Color.parseColor("#2563EB");
        }
        if (st.contains("upcoming")) {
            return Color.parseColor("#64748B");
        }
        return Color.parseColor("#5B3DF5");
    }

    @Override
    public int getItemCount() {
        return assessmentList != null ? assessmentList.size() : 0;
    }

    public static class AssessmentViewHolder extends RecyclerView.ViewHolder {
        View indicatorView;
        ImageView iconView;
        TextView courseBadgeText, titleText, dateText, scoreText, actionHintText;

        public AssessmentViewHolder(@NonNull View itemView) {
            super(itemView);
            indicatorView = itemView.findViewById(R.id.indicator_view);
            iconView = itemView.findViewById(R.id.assessment_icon);
            courseBadgeText = itemView.findViewById(R.id.assessment_course_badge);
            titleText = itemView.findViewById(R.id.assessment_title);
            dateText = itemView.findViewById(R.id.assessment_date);
            scoreText = itemView.findViewById(R.id.assessment_score);
            actionHintText = itemView.findViewById(R.id.assessment_action_hint);
        }
    }
}
