package com.social7.studentlearnanalytics.teacher.attendance;

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

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

    public static class StudentAttendanceRecord {
        public String studentId;
        public String studentName;
        public String courseClass;
        public String counts;
        public String rateBadge;
        public int numericStudentId = 0;

        public StudentAttendanceRecord(String studentId, String studentName, String courseClass, String counts, String rateBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseClass = courseClass;
            this.counts = counts;
            this.rateBadge = rateBadge;
        }
    }

    public interface OnStudentAttendanceClickListener {
        void onStudentAttendanceClick(StudentAttendanceRecord item);
    }

    private final Context context;
    private final List<StudentAttendanceRecord> originalList;
    private final List<StudentAttendanceRecord> filteredList;
    private final OnStudentAttendanceClickListener listener;

    public StudentAttendanceAdapter(Context context, List<StudentAttendanceRecord> list, OnStudentAttendanceClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_student_attendance_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentAttendanceRecord item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (StudentAttendanceRecord item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.courseClass != null && item.courseClass.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvCourseClass, tvAttendanceCounts, tvAttendanceRateBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCourseClass = itemView.findViewById(R.id.tvCourseClass);
            tvAttendanceCounts = itemView.findViewById(R.id.tvAttendanceCounts);
            tvAttendanceRateBadge = itemView.findViewById(R.id.tvAttendanceRateBadge);
        }

        public void bind(StudentAttendanceRecord item, OnStudentAttendanceClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvCourseClass != null) tvCourseClass.setText(item.courseClass);
            if (tvAttendanceCounts != null) tvAttendanceCounts.setText(item.counts);
            if (tvAttendanceRateBadge != null) {
                tvAttendanceRateBadge.setText(item.rateBadge);
                if (item.rateBadge.contains("9") || item.rateBadge.contains("85")) {
                    tvAttendanceRateBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.rateBadge.contains("80") || item.rateBadge.contains("75")) {
                    tvAttendanceRateBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvAttendanceRateBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentAttendanceClick(item);
            });
        }
    }
}
