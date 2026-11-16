package com.social7.studentlearnanalytics.student.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;

public class CourseAttendanceAdapter extends RecyclerView.Adapter<CourseAttendanceAdapter.ViewHolder> {

    private final JsonArray data;

    public CourseAttendanceAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        String name = item.has("course_name") ? item.get("course_name").getAsString() : "Unknown Course";
        holder.tvCourseName.setText(name);

        holder.tvTotal.setText(item.has("total") ? item.get("total").getAsString() : "0");
        holder.tvPresent.setText(item.has("present") ? item.get("present").getAsString() : "0");
        holder.tvAbsent.setText(item.has("absent") ? item.get("absent").getAsString() : "0");

        String percent = item.has("percentage") ? item.get("percentage").getAsString() + "%" : "0%";
        holder.tvPercentage.setText(percent);

        holder.itemView.setOnClickListener(v -> {
            int courseOfferingId = item.has("course_offering_id") ? item.get("course_offering_id").getAsInt() : 0;
            if (courseOfferingId > 0) {
                android.content.Intent intent = new android.content.Intent(v.getContext(), CourseAttendanceActivity.class);
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
        TextView tvTotal;
        TextView tvPresent;
        TextView tvAbsent;
        TextView tvPercentage;

        ViewHolder(View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvPresent = itemView.findViewById(R.id.tvPresent);
            tvAbsent = itemView.findViewById(R.id.tvAbsent);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
    }
}
