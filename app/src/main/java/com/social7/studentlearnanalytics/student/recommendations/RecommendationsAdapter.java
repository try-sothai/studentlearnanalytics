package com.social7.studentlearnanalytics.student.recommendations;

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

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {

    private final JsonArray data;

    public RecommendationsAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        holder.tvTitle.setText(item.has("title") && !item.get("title").isJsonNull()
                ? item.get("title").getAsString() : "Recommendation");
        String reason = item.has("reason") && !item.get("reason").isJsonNull()
                ? item.get("reason").getAsString()
                : (item.has("message") && !item.get("message").isJsonNull()
                ? item.get("message").getAsString() : "No details provided.");
        holder.tvReason.setText(reason);
        holder.tvAction.setText(item.has("action") && !item.get("action").isJsonNull()
                ? item.get("action").getAsString() : "Review course materials");

        String course = item.has("course_name") && !item.get("course_name").isJsonNull() ? item.get("course_name").getAsString() : "General";
        holder.tvCourseName.setText(course.toUpperCase());

        String priority = item.has("priority") && !item.get("priority").isJsonNull() ? item.get("priority").getAsString().toUpperCase() : "NORMAL";
        holder.tvPriority.setText(priority);

        if ("HIGH".equals(priority)) {
            holder.tvPriority.setTextColor(Color.parseColor("#B91C1C")); // Red text
            holder.tvPriority.setBackgroundColor(Color.parseColor("#FEE2E2")); // Red bg
        } else if ("MEDIUM".equals(priority)) {
            holder.tvPriority.setTextColor(Color.parseColor("#B45309"));
            holder.tvPriority.setBackgroundColor(Color.parseColor("#FEF3C7"));
        } else {
            holder.tvPriority.setTextColor(Color.parseColor("#047857"));
            holder.tvPriority.setBackgroundColor(Color.parseColor("#D1FAE5"));
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName;
        TextView tvPriority;
        TextView tvTitle;
        TextView tvReason;
        TextView tvAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvAction = itemView.findViewById(R.id.tvAction);
        }
    }
}
