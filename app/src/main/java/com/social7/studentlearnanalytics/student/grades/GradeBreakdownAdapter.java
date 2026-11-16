package com.social7.studentlearnanalytics.student.grades;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;

public class GradeBreakdownAdapter extends RecyclerView.Adapter<GradeBreakdownAdapter.ViewHolder> {

    private final JsonArray data;

    public GradeBreakdownAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade_breakdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        holder.tvComponentName.setText(item.has("component_name") ? item.get("component_name").getAsString() : "Component");
        holder.tvType.setText(item.has("type") ? item.get("type").getAsString() : "Unknown");

        String earned = item.has("earned_score") && !item.get("earned_score").isJsonNull() ? item.get("earned_score").getAsString() : "--";
        String max = item.has("maximum_score") && !item.get("maximum_score").isJsonNull() ? item.get("maximum_score").getAsString() : "0";

        holder.tvEarned.setText(earned);
        holder.tvMax.setText(max);

        if (item.has("teacher_feedback") && !item.get("teacher_feedback").isJsonNull()) {
            holder.tvFeedback.setVisibility(View.VISIBLE);
            holder.tvFeedback.setText(holder.tvFeedback.getContext().getString(R.string.feedback) + item.get("teacher_feedback").getAsString());
        } else {
            holder.tvFeedback.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvComponentName;
        TextView tvType;
        TextView tvEarned;
        TextView tvMax;
        TextView tvFeedback;

        ViewHolder(View itemView) {
            super(itemView);
            tvComponentName = itemView.findViewById(R.id.tvComponentName);
            tvType = itemView.findViewById(R.id.tvType);
            tvEarned = itemView.findViewById(R.id.tvEarned);
            tvMax = itemView.findViewById(R.id.tvMax);
            tvFeedback = itemView.findViewById(R.id.tvFeedback);
        }
    }
}
