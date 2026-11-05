package com.social7.studentlearnanalytics.admin.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class ClassScheduleAdapter extends RecyclerView.Adapter<ClassScheduleAdapter.ViewHolder> {

    public static class ScheduleItem {
        public String classCode;
        public String courseName;
        public String teacherName;
        public String dayAndTime;
        public String roomName;
        public String status;

        public ScheduleItem(String classCode, String courseName, String teacherName, String dayAndTime, String roomName, String status) {
            this.classCode = classCode;
            this.courseName = courseName;
            this.teacherName = teacherName;
            this.dayAndTime = dayAndTime;
            this.roomName = roomName;
            this.status = status;
        }
    }

    public interface OnScheduleClickListener {
        void onScheduleClick(ScheduleItem item);
    }

    private final Context context;
    private final List<ScheduleItem> originalList;
    private final List<ScheduleItem> filteredList;
    private final OnScheduleClickListener listener;

    public ClassScheduleAdapter(Context context, List<ScheduleItem> list, OnScheduleClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_class_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (ScheduleItem item : originalList) {
            if (text.isEmpty() ||
                (item.classCode != null && item.classCode.toLowerCase().contains(text)) ||
                (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.roomName != null && item.roomName.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassCode, tvCourseName, tvScheduleTime, tvScheduleDetails, tvConflictStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
            tvScheduleDetails = itemView.findViewById(R.id.tvScheduleDetails);
            tvConflictStatus = itemView.findViewById(R.id.tvConflictStatus);
        }

        public void bind(ScheduleItem item, OnScheduleClickListener listener) {
            if (tvClassCode != null) tvClassCode.setText(item.classCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvScheduleTime != null) tvScheduleTime.setText(item.dayAndTime);
            if (tvScheduleDetails != null) {
                tvScheduleDetails.setText("Room: " + item.roomName + " • Teacher: " + item.teacherName);
            }
            if (tvConflictStatus != null) {
                tvConflictStatus.setText(item.status);
                if ("Validated".equalsIgnoreCase(item.status) || "Active".equalsIgnoreCase(item.status)) {
                    tvConflictStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Conflict".equalsIgnoreCase(item.status)) {
                    tvConflictStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvConflictStatus.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScheduleClick(item);
            });
        }
    }
}
