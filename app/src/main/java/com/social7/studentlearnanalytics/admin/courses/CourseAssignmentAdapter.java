package com.social7.studentlearnanalytics.admin.courses;

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

public class CourseAssignmentAdapter extends RecyclerView.Adapter<CourseAssignmentAdapter.ViewHolder> {

    public static class AssignmentItem {
        public String courseCode;
        public String courseName;
        public String className;
        public String teacherName;
        public String status;

        public AssignmentItem(String courseCode, String courseName, String className, String teacherName, String status) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.className = className;
            this.teacherName = teacherName;
            this.status = status;
        }
    }

    public interface OnAssignmentClickListener {
        void onAssignmentClick(AssignmentItem item);
    }

    private final Context context;
    private final List<AssignmentItem> originalList;
    private final List<AssignmentItem> filteredList;
    private final OnAssignmentClickListener listener;

    public CourseAssignmentAdapter(Context context, List<AssignmentItem> list, OnAssignmentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_course_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (AssignmentItem item : originalList) {
            if (text.isEmpty() ||
                (item.courseCode != null && item.courseCode.toLowerCase().contains(text)) ||
                (item.courseName != null && item.courseName.toLowerCase().contains(text)) ||
                (item.className != null && item.className.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvCourseName, tvAssignmentDetails, tvAssignmentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvAssignmentDetails = itemView.findViewById(R.id.tvAssignmentDetails);
            tvAssignmentStatus = itemView.findViewById(R.id.tvAssignmentStatus);
        }

        public void bind(AssignmentItem item, OnAssignmentClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvCourseName != null) tvCourseName.setText(item.courseName);
            if (tvAssignmentDetails != null) {
                tvAssignmentDetails.setText("Class: " + item.className + " • Teacher: " + item.teacherName);
            }
            if (tvAssignmentStatus != null) {
                tvAssignmentStatus.setText(item.status);
                if ("Active".equalsIgnoreCase(item.status)) {
                    tvAssignmentStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Missing Teacher".equalsIgnoreCase(item.status) || "Unassigned".equalsIgnoreCase(item.status)) {
                    tvAssignmentStatus.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvAssignmentStatus.setTextColor(Color.parseColor("#64748B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAssignmentClick(item);
            });
        }
    }
}
