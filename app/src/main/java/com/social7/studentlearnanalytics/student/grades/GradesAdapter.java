package com.social7.studentlearnanalytics.student.grades;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.ViewHolder> {

    private final JsonArray data;

    public GradesAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        String name = item.has("course_name") ? item.get("course_name").getAsString() : "Unknown Course";
        holder.tvCourseName.setText(name);

        String credits = item.has("credits") ? item.get("credits").getAsString() : "3";
        holder.tvCredits.setText(holder.tvCredits.getContext().getString(R.string.credits) + credits);

        String status = item.has("status") ? item.get("status").getAsString() : "Pending";
        holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

        if ("official".equalsIgnoreCase(status) || "finalized".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Orange
        }

        String score = item.has("total_score") && !item.get("total_score").isJsonNull() ? item.get("total_score").getAsString() : "N/A";
        String letter = item.has("letter_grade") && !item.get("letter_grade").isJsonNull() ? item.get("letter_grade").getAsString() : "N/A";

        holder.tvScore.setText(score);
        holder.tvLetterGrade.setText(letter);

        holder.itemView.setOnClickListener(v -> {
            int courseOfferingId = item.has("course_offering_id") ? item.get("course_offering_id").getAsInt() : 0;
            if (courseOfferingId > 0) {
                Intent intent = new Intent(v.getContext(), CourseGradeActivity.class);
                intent.putExtra("course_offering_id", courseOfferingId);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName;
        TextView tvCredits;
        TextView tvStatus;
        TextView tvScore;
        TextView tvLetterGrade;

        ViewHolder(View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvLetterGrade = itemView.findViewById(R.id.tvLetterGrade);
        }
    }
}
