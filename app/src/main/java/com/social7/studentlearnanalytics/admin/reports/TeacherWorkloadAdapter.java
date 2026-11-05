package com.social7.studentlearnanalytics.admin.reports;

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

public class TeacherWorkloadAdapter extends RecyclerView.Adapter<TeacherWorkloadAdapter.ViewHolder> {

    public static class WorkloadItem {
        public String teacherId;
        public String teacherName;
        public String department;
        public String teachingHours;
        public String status;

        public WorkloadItem(String teacherId, String teacherName, String department, String teachingHours, String status) {
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.department = department;
            this.teachingHours = teachingHours;
            this.status = status;
        }
    }

    public interface OnWorkloadClickListener {
        void onWorkloadClick(WorkloadItem item);
    }

    private final Context context;
    private final List<WorkloadItem> originalList;
    private final List<WorkloadItem> filteredList;
    private final OnWorkloadClickListener listener;

    public TeacherWorkloadAdapter(Context context, List<WorkloadItem> list, OnWorkloadClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher_workload, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkloadItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (WorkloadItem item : originalList) {
            if (text.isEmpty() ||
                (item.teacherId != null && item.teacherId.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.department != null && item.department.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTeacherId, tvTeacherName, tvTeacherDept, tvTeachingHours, tvWorkloadStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeacherId = itemView.findViewById(R.id.tvTeacherId);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvTeacherDept = itemView.findViewById(R.id.tvTeacherDept);
            tvTeachingHours = itemView.findViewById(R.id.tvTeachingHours);
            tvWorkloadStatus = itemView.findViewById(R.id.tvWorkloadStatus);
        }

        public void bind(WorkloadItem item, OnWorkloadClickListener listener) {
            if (tvTeacherId != null) tvTeacherId.setText(item.teacherId);
            if (tvTeacherName != null) tvTeacherName.setText(item.teacherName);
            if (tvTeacherDept != null) tvTeacherDept.setText("Department: " + item.department);
            if (tvTeachingHours != null) tvTeachingHours.setText("Weekly Teaching Load: " + item.teachingHours);
            if (tvWorkloadStatus != null) {
                tvWorkloadStatus.setText(item.status);
                if ("Normal Workload".equalsIgnoreCase(item.status) || "Normal".equalsIgnoreCase(item.status)) {
                    tvWorkloadStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("High Workload".equalsIgnoreCase(item.status)) {
                    tvWorkloadStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvWorkloadStatus.setTextColor(Color.parseColor("#155DFC"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onWorkloadClick(item);
            });
        }
    }
}
