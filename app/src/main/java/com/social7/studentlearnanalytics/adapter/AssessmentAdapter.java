package com.social7.studentlearnanalytics.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Assessment;

import java.util.List;

public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder> {

    private List<Assessment> assessmentList;

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
        Assessment assessment = assessmentList.get(position);
        holder.titleText.setText(assessment.getTitle());

        String subtitle = assessment.getDate();
        if (assessment.getCourseName() != null && !assessment.getCourseName().trim().isEmpty()) {
            subtitle = assessment.getCourseName().trim() + " • " + assessment.getDate();
        }
        holder.dateText.setText(subtitle);
        holder.scoreText.setText(assessment.getFormattedScore());

        if ("quiz".equalsIgnoreCase(assessment.getType())) {
            holder.iconView.setImageResource(R.drawable.ic_analytics);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            holder.iconView.setColorFilter(Color.parseColor("#F59E0B"));

            if (assessment.isGraded()) {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
                holder.scoreText.setTextColor(Color.parseColor("#1E1B4B"));
            } else if (assessment.isSubmitted()) {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                holder.scoreText.setTextColor(Color.parseColor("#2563EB"));
            } else {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64748B")));
                holder.scoreText.setTextColor(Color.parseColor("#64748B"));
            }
        } else {
            holder.iconView.setImageResource(R.drawable.ic_assignment);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1EDFF")));
            holder.iconView.setColorFilter(Color.parseColor("#5B3DF5"));

            if (assessment.isGraded()) {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
                holder.scoreText.setTextColor(Color.parseColor("#1E1B4B"));
            } else if (assessment.isSubmitted()) {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                holder.scoreText.setTextColor(Color.parseColor("#2563EB"));
            } else {
                holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
                holder.scoreText.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return assessmentList != null ? assessmentList.size() : 0;
    }

    public static class AssessmentViewHolder extends RecyclerView.ViewHolder {
        View indicatorView;
        ImageView iconView;
        TextView titleText, dateText, scoreText;

        public AssessmentViewHolder(@NonNull View itemView) {
            super(itemView);
            indicatorView = itemView.findViewById(R.id.indicator_view);
            iconView = itemView.findViewById(R.id.assessment_icon);
            titleText = itemView.findViewById(R.id.assessment_title);
            dateText = itemView.findViewById(R.id.assessment_date);
            scoreText = itemView.findViewById(R.id.assessment_score);
        }
    }
}
