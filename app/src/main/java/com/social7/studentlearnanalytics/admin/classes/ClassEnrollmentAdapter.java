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

public class ClassEnrollmentAdapter extends RecyclerView.Adapter<ClassEnrollmentAdapter.ViewHolder> {

    public static class EnrollmentItem {
        public String studentId;
        public String studentName;
        public String className;
        public String enrollmentDate;
        public String status;

        public EnrollmentItem(String studentId, String studentName, String className, String enrollmentDate, String status) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.className = className;
            this.enrollmentDate = enrollmentDate;
            this.status = status;
        }
    }

    public interface OnEnrollmentClickListener {
        void onEnrollmentClick(EnrollmentItem item);
    }

    private final Context context;
    private final List<EnrollmentItem> originalList;
    private final List<EnrollmentItem> filteredList;
    private final OnEnrollmentClickListener listener;

    public ClassEnrollmentAdapter(Context context, List<EnrollmentItem> list, OnEnrollmentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_class_enrollment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrollmentItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (EnrollmentItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.className != null && item.className.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvClassName, tvEnrollmentDate, tvEnrollmentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvEnrollmentDate = itemView.findViewById(R.id.tvEnrollmentDate);
            tvEnrollmentStatus = itemView.findViewById(R.id.tvEnrollmentStatus);
        }

        public void bind(EnrollmentItem item, OnEnrollmentClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvClassName != null) tvClassName.setText(tvClassName.getContext().getString(R.string.enrolled_class) + item.className);
            if (tvEnrollmentDate != null) tvEnrollmentDate.setText(tvEnrollmentDate.getContext().getString(R.string.enrolled) + item.enrollmentDate);
            if (tvEnrollmentStatus != null) {
                tvEnrollmentStatus.setText(item.status);
                if ("Enrolled".equalsIgnoreCase(item.status) || "Active".equalsIgnoreCase(item.status)) {
                    tvEnrollmentStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Capacity Full".equalsIgnoreCase(item.status) || "Full".equalsIgnoreCase(item.status)) {
                    tvEnrollmentStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvEnrollmentStatus.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEnrollmentClick(item);
            });
        }
    }
}
