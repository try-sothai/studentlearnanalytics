package com.social7.studentlearnanalytics.student.attendance;

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

public class CourseAttendanceRecordsAdapter extends RecyclerView.Adapter<CourseAttendanceRecordsAdapter.ViewHolder> {

    private final JsonArray data;

    public CourseAttendanceRecordsAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        holder.tvDate.setText(item.has("date") && !item.get("date").isJsonNull() ? item.get("date").getAsString() : "Unknown");
        holder.tvTopic.setText(item.has("topic") && !item.get("topic").isJsonNull() ? item.get("topic").getAsString() : "Session");

        String status = item.has("status") && !item.get("status").isJsonNull() ? item.get("status").getAsString() : "Unknown";
        holder.tvStatusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

        if ("present".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#10B981"));
        } else if ("absent".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#EF4444"));
        } else if ("late".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#F59E0B"));
        } else if ("excused".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#3B82F6"));
        } else {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#9CA3AF"));
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTopic;
        TextView tvStatusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
