package com.social7.studentlearnanalytics.student.grades;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        String name = item.has("course_name") && !item.get("course_name").isJsonNull()
                ? item.get("course_name").getAsString() : "Unknown Course";
        String code = item.has("course_code") && !item.get("course_code").isJsonNull()
                ? item.get("course_code").getAsString().trim() : "";
        boolean isRetake = item.has("is_retake") && !item.get("is_retake").isJsonNull() && item.get("is_retake").getAsBoolean();
        int attemptNum = item.has("attempt_number") && !item.get("attempt_number").isJsonNull()
                ? item.get("attempt_number").getAsInt() : 1;
        String displayTitle = !code.isEmpty() ? (code + " – " + name) : name;
        if (isRetake || attemptNum > 1) {
            displayTitle = displayTitle + " [Retake #" + attemptNum + "]";
        }
        holder.tvCourseName.setText(displayTitle);

        Context ctx = holder.itemView.getContext();
        String credits = item.has("credits") && !item.get("credits").isJsonNull()
                ? item.get("credits").getAsString() : "--";
        String semester = item.has("semester") && !item.get("semester").isJsonNull()
                ? item.get("semester").getAsString().trim() : "";
        String meta = credits + " Credits";
        if (!semester.isEmpty()) {
            meta = meta + " • " + semester;
        }
        holder.tvCredits.setText(meta);

        String score = item.has("score_display") && !item.get("score_display").isJsonNull()
                ? item.get("score_display").getAsString()
                : (item.has("total_score") && !item.get("total_score").isJsonNull()
                ? item.get("total_score").getAsString() : "N/A");
        String letter = item.has("grade_display") && !item.get("grade_display").isJsonNull()
                ? item.get("grade_display").getAsString()
                : (item.has("letter_grade") && !item.get("letter_grade").isJsonNull()
                ? item.get("letter_grade").getAsString() : "N/A");

        String status = item.has("status") && !item.get("status").isJsonNull()
                ? item.get("status").getAsString() : "Pending";
        String displayStatus = status.isEmpty()
                ? "Pending"
                : status.substring(0, 1).toUpperCase() + status.substring(1);
        if ("F".equalsIgnoreCase(letter)) {
            displayStatus = "Failed";
        } else if ("official".equalsIgnoreCase(status) || "finalized".equalsIgnoreCase(status) || "published".equalsIgnoreCase(status)) {
            displayStatus = "Passed";
        }
        holder.tvStatus.setText(displayStatus);

        if ("Failed".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_slas_pill_red);
            holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.slas_danger));
        } else if ("Passed".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_slas_pill_green);
            holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.slas_success));
        } else if ("provisional".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(displayStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_slas_pill_orange);
            holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.slas_warning));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_slas_pill_neutral);
            holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.slas_text_secondary));
        }

        holder.tvScore.setText(score);
        holder.tvScore.setTextColor(ContextCompat.getColor(ctx,
                "N/A".equalsIgnoreCase(score) ? R.color.slas_text_secondary : R.color.slas_text_primary));

        holder.tvLetterGrade.setText(letter);
        if ("F".equalsIgnoreCase(letter)) {
            holder.tvLetterGrade.setTextColor(ContextCompat.getColor(ctx, R.color.slas_danger));
        } else if ("N/A".equalsIgnoreCase(letter)) {
            holder.tvLetterGrade.setTextColor(ContextCompat.getColor(ctx, R.color.slas_text_secondary));
        } else {
            holder.tvLetterGrade.setTextColor(ContextCompat.getColor(ctx, R.color.slas_success));
        }

        holder.itemView.setOnClickListener(v -> {
            int courseOfferingId = item.has("course_offering_id") && !item.get("course_offering_id").isJsonNull()
                    ? item.get("course_offering_id").getAsInt() : 0;
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
