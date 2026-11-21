package com.social7.studentlearnanalytics.admin.classes;

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

public class ClassScheduleAdapter extends RecyclerView.Adapter<ClassScheduleAdapter.ViewHolder> {

    public static class ScheduleItem {
        public String scheduleId;
        public int classId;
        public String classCode;
        public String courseName;
        public String teacherName;
        public String dayOfWeek;
        public String dayAndTime;
        public String locationLabel;
        public String status;
        public double durationHours;
        public String rawJson;

        public ScheduleItem(
                String scheduleId,
                int classId,
                String classCode,
                String courseName,
                String teacherName,
                String dayOfWeek,
                String dayAndTime,
                String locationLabel,
                String status,
                double durationHours,
                String rawJson
        ) {
            this.scheduleId = scheduleId;
            this.classId = classId;
            this.classCode = classCode;
            this.courseName = courseName;
            this.teacherName = teacherName;
            this.dayOfWeek = dayOfWeek;
            this.dayAndTime = dayAndTime;
            this.locationLabel = locationLabel;
            this.status = status;
            this.durationHours = durationHours;
            this.rawJson = rawJson;
        }
    }

    public interface OnScheduleClickListener {
        void onScheduleClick(ScheduleItem item);
        void onEditSchedule(ScheduleItem item);
        void onDeleteSchedule(ScheduleItem item);
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

    public int getFilteredCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (ScheduleItem item : originalList) {
            if (text.isEmpty()
                    || (item.classCode != null && item.classCode.toLowerCase().contains(text))
                    || (item.courseName != null && item.courseName.toLowerCase().contains(text))
                    || (item.teacherName != null && item.teacherName.toLowerCase().contains(text))
                    || (item.locationLabel != null && item.locationLabel.toLowerCase().contains(text))
                    || (item.dayAndTime != null && item.dayAndTime.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassCode, tvCourseName, tvScheduleTime, tvScheduleDetails, tvConflictStatus;
        private final MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
            tvScheduleDetails = itemView.findViewById(R.id.tvScheduleDetails);
            tvConflictStatus = itemView.findViewById(R.id.tvConflictStatus);
            btnEdit = itemView.findViewById(R.id.btnEditScheduleItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteScheduleItem);
        }

        public void bind(ScheduleItem item, OnScheduleClickListener listener) {
            if (tvClassCode != null) tvClassCode.setText(item.classCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvScheduleTime != null) tvScheduleTime.setText(item.dayAndTime);
            if (tvScheduleDetails != null) {
                tvScheduleDetails.setText(item.locationLabel + " • Teacher: " + item.teacherName);
            }
            if (tvConflictStatus != null) {
                String st = item.status != null && !item.status.isEmpty() ? item.status : "Active";
                tvConflictStatus.setText(st);
                if ("Active".equalsIgnoreCase(st) || "Validated".equalsIgnoreCase(st)) {
                    tvConflictStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Conflict".equalsIgnoreCase(st) || "Cancelled".equalsIgnoreCase(st)) {
                    tvConflictStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvConflictStatus.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScheduleClick(item);
            });
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEditSchedule(item);
                });
            }
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteSchedule(item);
                });
            }
        }
    }
}
