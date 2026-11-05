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

public class ClassTeacherAssignmentAdapter extends RecyclerView.Adapter<ClassTeacherAssignmentAdapter.ViewHolder> {

    public static class TeacherAssignItem {
        public String classCode;
        public String courseName;
        public String teacherName;
        public String departmentTerm;
        public String status;

        public TeacherAssignItem(String classCode, String courseName, String teacherName, String departmentTerm, String status) {
            this.classCode = classCode;
            this.courseName = courseName;
            this.teacherName = teacherName;
            this.departmentTerm = departmentTerm;
            this.status = status;
        }
    }

    public interface OnTeacherAssignClickListener {
        void onTeacherAssignClick(TeacherAssignItem item);
    }

    private final Context context;
    private final List<TeacherAssignItem> originalList;
    private final List<TeacherAssignItem> filteredList;
    private final OnTeacherAssignClickListener listener;

    public ClassTeacherAssignmentAdapter(Context context, List<TeacherAssignItem> list, OnTeacherAssignClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeacherAssignItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (TeacherAssignItem item : originalList) {
            if (text.isEmpty() ||
                (item.classCode != null && item.classCode.toLowerCase().contains(text)) ||
                (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.departmentTerm != null && item.departmentTerm.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassCode, tvCourseName, tvTeacherName, tvDepartmentTerm, tvStaffingStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDepartmentTerm = itemView.findViewById(R.id.tvDepartmentTerm);
            tvStaffingStatus = itemView.findViewById(R.id.tvStaffingStatus);
        }

        public void bind(TeacherAssignItem item, OnTeacherAssignClickListener listener) {
            if (tvClassCode != null) tvClassCode.setText(item.classCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvTeacherName != null) tvTeacherName.setText("Assigned Teacher: " + item.teacherName);
            if (tvDepartmentTerm != null) tvDepartmentTerm.setText(item.departmentTerm);
            if (tvStaffingStatus != null) {
                tvStaffingStatus.setText(item.status);
                if ("Assigned".equalsIgnoreCase(item.status) || "Active".equalsIgnoreCase(item.status)) {
                    tvStaffingStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Unassigned".equalsIgnoreCase(item.status)) {
                    tvStaffingStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvStaffingStatus.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTeacherAssignClick(item);
            });
        }
    }
}
