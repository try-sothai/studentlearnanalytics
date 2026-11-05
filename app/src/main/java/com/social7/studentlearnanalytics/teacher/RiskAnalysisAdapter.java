package com.social7.studentlearnanalytics.teacher;

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

public class RiskAnalysisAdapter extends RecyclerView.Adapter<RiskAnalysisAdapter.ViewHolder> {

    public static class RiskAnalysisItem {
        public String courseCode;
        public String courseName;
        public String departmentCohort;
        public String primaryRiskReason;
        public String riskBadge;

        public RiskAnalysisItem(String courseCode, String courseName, String departmentCohort, String primaryRiskReason, String riskBadge) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.departmentCohort = departmentCohort;
            this.primaryRiskReason = primaryRiskReason;
            this.riskBadge = riskBadge;
        }
    }

    public interface OnRiskAnalysisClickListener {
        void onRiskAnalysisClick(RiskAnalysisItem item);
    }

    private final Context context;
    private final List<RiskAnalysisItem> originalList;
    private final List<RiskAnalysisItem> filteredList;
    private final OnRiskAnalysisClickListener listener;

    public RiskAnalysisAdapter(Context context, List<RiskAnalysisItem> list, OnRiskAnalysisClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_risk_analysis, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiskAnalysisItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (RiskAnalysisItem item : originalList) {
            if (text.isEmpty() ||
                (item.courseCode != null && item.courseCode.toLowerCase().contains(text)) ||
                (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                (item.departmentCohort != null && item.departmentCohort.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvCourseName, tvDepartmentCohort, tvPrimaryRiskReason, tvRiskLevelBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvDepartmentCohort = itemView.findViewById(R.id.tvDepartmentCohort);
            tvPrimaryRiskReason = itemView.findViewById(R.id.tvPrimaryRiskReason);
            tvRiskLevelBadge = itemView.findViewById(R.id.tvRiskLevelBadge);
        }

        public void bind(RiskAnalysisItem item, OnRiskAnalysisClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvDepartmentCohort != null) tvDepartmentCohort.setText(item.departmentCohort);
            if (tvPrimaryRiskReason != null) tvPrimaryRiskReason.setText(item.primaryRiskReason);
            if (tvRiskLevelBadge != null) {
                tvRiskLevelBadge.setText(item.riskBadge);
                if (item.riskBadge.contains("High")) {
                    tvRiskLevelBadge.setTextColor(Color.parseColor("#EF4444"));
                } else if (item.riskBadge.contains("Medium")) {
                    tvRiskLevelBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvRiskLevelBadge.setTextColor(Color.parseColor("#10B981"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRiskAnalysisClick(item);
            });
        }
    }
}
