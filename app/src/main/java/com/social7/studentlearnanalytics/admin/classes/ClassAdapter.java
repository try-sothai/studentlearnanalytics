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
import com.social7.studentlearnanalytics.model.ClassModel;

import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    public interface OnClassClickListener {
        void onClassClick(ClassModel classModel);
    }

    private final Context context;
    private final List<ClassModel> originalList;
    private List<ClassModel> filteredList;
    private final OnClassClickListener listener;
    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";

    public ClassAdapter(Context context, List<ClassModel> classList, OnClassClickListener listener) {
        this.context = context;
        this.originalList = classList != null ? classList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = filteredList.get(position);
        holder.bind(classModel, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        this.currentSearchQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilters();
    }

    public void filterByStatus(String status) {
        this.currentStatusFilter = status != null ? status.trim().toUpperCase() : "ALL";
        applyFilters();
    }

    private void applyFilters() {
        filteredList.clear();
        for (ClassModel item : originalList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (item.getName() != null && item.getName().toLowerCase().contains(currentSearchQuery)) ||
                    (item.getCode() != null && item.getCode().toLowerCase().contains(currentSearchQuery)) ||
                    (item.getDepartment() != null && item.getDepartment().toLowerCase().contains(currentSearchQuery)) ||
                    (item.getCourse() != null && item.getCourse().toLowerCase().contains(currentSearchQuery)) ||
                    (item.getTeacher() != null && item.getTeacher().toLowerCase().contains(currentSearchQuery));

            boolean matchesStatus = currentStatusFilter.equals("ALL") ||
                    (item.getStatus() != null && item.getStatus().equalsIgnoreCase(currentStatusFilter));

            if (matchesSearch && matchesStatus) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode;
        private final TextView tvName;
        private final TextView tvDetails;
        private final TextView tvStatus;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCourseCode);
            tvName = itemView.findViewById(R.id.tvCourseName);
            tvDetails = itemView.findViewById(R.id.tvCourseCredits);
            tvStatus = itemView.findViewById(R.id.tvCourseStatus);
        }

        public void bind(ClassModel classModel, OnClassClickListener listener) {
            if (tvCode != null) {
                tvCode.setText(classModel.getCode() != null ? classModel.getCode() : "CLASS");
            }

            if (tvName != null) {
                tvName.setText(classModel.getName() != null ? classModel.getName() : "Class Name");
            }

            if (tvDetails != null) {
                StringBuilder details = new StringBuilder();
                if (classModel.getLocationLabel() != null && !classModel.getLocationLabel().isEmpty()) {
                    details.append(classModel.getLocationLabel());
                }
                if (classModel.getCourse() != null && !classModel.getCourse().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append(classModel.getCourse());
                }
                if (classModel.getTeacher() != null && !classModel.getTeacher().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append(classModel.getTeacher());
                }
                if (classModel.getDepartment() != null && !classModel.getDepartment().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append(classModel.getDepartment());
                }
                if (details.length() > 0) details.append(" • ");
                details.append(classModel.getStudentCount()).append(" Students");
                tvDetails.setText(details.toString());
            }

            if (tvStatus != null) {
                String status = classModel.getStatus() != null ? classModel.getStatus() : "Active";
                tvStatus.setText(status);
                if ("Active".equalsIgnoreCase(status)) {
                    tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
                } else {
                    tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClassClick(classModel);
                }
            });
        }
    }
}
