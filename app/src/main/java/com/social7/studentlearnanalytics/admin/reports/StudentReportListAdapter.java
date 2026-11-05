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

public class StudentReportListAdapter extends RecyclerView.Adapter<StudentReportListAdapter.ViewHolder> {

    public static class StudentReportItem {
        public String studentId;
        public String studentName;
        public String deptClass;
        public String metricDetail;
        public String status;

        public StudentReportItem(String studentId, String studentName, String deptClass, String metricDetail, String status) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.deptClass = deptClass;
            this.metricDetail = metricDetail;
            this.status = status;
        }
    }

    public interface OnStudentClickListener {
        void onStudentClick(StudentReportItem item);
    }

    private final Context context;
    private final List<StudentReportItem> originalList;
    private final List<StudentReportItem> filteredList;
    private final OnStudentClickListener listener;

    public StudentReportListAdapter(Context context, List<StudentReportItem> list, OnStudentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_student_report_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentReportItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (StudentReportItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.deptClass != null && item.deptClass.toLowerCase().contains(text)) ||
                (item.metricDetail != null && item.metricDetail.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvStudentDept, tvStudentMetric, tvStudentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentDept = itemView.findViewById(R.id.tvStudentDept);
            tvStudentMetric = itemView.findViewById(R.id.tvStudentMetric);
            tvStudentStatus = itemView.findViewById(R.id.tvStudentStatus);
        }

        public void bind(StudentReportItem item, OnStudentClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvStudentDept != null) tvStudentDept.setText(item.deptClass);
            if (tvStudentMetric != null) tvStudentMetric.setText(item.metricDetail);
            if (tvStudentStatus != null) {
                tvStudentStatus.setText(item.status);
                if ("Active".equalsIgnoreCase(item.status) || "Present".equalsIgnoreCase(item.status) || "Honors".equalsIgnoreCase(item.status)) {
                    tvStudentStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Absent".equalsIgnoreCase(item.status) || "Needs Improvement".equalsIgnoreCase(item.status)) {
                    tvStudentStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvStudentStatus.setTextColor(Color.parseColor("#155DFC"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentClick(item);
            });
        }
    }
}
