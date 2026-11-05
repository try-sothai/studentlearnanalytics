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

public class TeacherAssignmentReportAdapter extends RecyclerView.Adapter<TeacherAssignmentReportAdapter.ViewHolder> {

    public static class AssignmentReportItem {
        public String classCode;
        public String courseCodeName;
        public String teacherName;
        public String deptTerm;
        public String status;

        public AssignmentReportItem(String classCode, String courseCodeName, String teacherName, String deptTerm, String status) {
            this.classCode = classCode;
            this.courseCodeName = courseCodeName;
            this.teacherName = teacherName;
            this.deptTerm = deptTerm;
            this.status = status;
        }
    }

    public interface OnAssignmentClickListener {
        void onAssignmentClick(AssignmentReportItem item);
    }

    private final Context context;
    private final List<AssignmentReportItem> originalList;
    private final List<AssignmentReportItem> filteredList;
    private final OnAssignmentClickListener listener;

    public TeacherAssignmentReportAdapter(Context context, List<AssignmentReportItem> list, OnAssignmentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher_assignment_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentReportItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (AssignmentReportItem item : originalList) {
            if (text.isEmpty() ||
                (item.classCode != null && item.classCode.toLowerCase().contains(text)) ||
                (item.courseCodeName != null && item.courseCodeName.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.deptTerm != null && item.deptTerm.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassCode, tvCourseCodeName, tvTeacherName, tvDeptTerm, tvAssignmentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvCourseCodeName = itemView.findViewById(R.id.tvCourseCodeName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDeptTerm = itemView.findViewById(R.id.tvDeptTerm);
            tvAssignmentStatus = itemView.findViewById(R.id.tvAssignmentStatus);
        }

        public void bind(AssignmentReportItem item, OnAssignmentClickListener listener) {
            if (tvClassCode != null) tvClassCode.setText(item.classCode);
            if (tvCourseCodeName != null) tvCourseCodeName.setText(item.courseCodeName);
            if (tvTeacherName != null) tvTeacherName.setText("Assigned Teacher: " + item.teacherName);
            if (tvDeptTerm != null) tvDeptTerm.setText(item.deptTerm);
            if (tvAssignmentStatus != null) {
                tvAssignmentStatus.setText(item.status);
                if ("Assigned".equalsIgnoreCase(item.status)) {
                    tvAssignmentStatus.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvAssignmentStatus.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAssignmentClick(item);
            });
        }
    }
}
