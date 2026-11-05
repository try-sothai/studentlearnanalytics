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

public class GradebookAdapter extends RecyclerView.Adapter<GradebookAdapter.ViewHolder> {

    public static class GradebookItem {
        public String studentId;
        public String studentName;
        public String courseClass;
        public String gradeBreakdown;
        public String gradeBadge;

        public GradebookItem(String studentId, String studentName, String courseClass, String gradeBreakdown, String gradeBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseClass = courseClass;
            this.gradeBreakdown = gradeBreakdown;
            this.gradeBadge = gradeBadge;
        }
    }

    public interface OnGradebookClickListener {
        void onGradebookClick(GradebookItem item);
    }

    private final Context context;
    private final List<GradebookItem> originalList;
    private final List<GradebookItem> filteredList;
    private final OnGradebookClickListener listener;

    public GradebookAdapter(Context context, List<GradebookItem> list, OnGradebookClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_gradebook_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GradebookItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (GradebookItem item : originalList) {
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
        private final TextView tvStudentId, tvStudentName, tvCourseClass, tvGradeBreakdown, tvGradeBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCourseClass = itemView.findViewById(R.id.tvCourseClass);
            tvGradeBreakdown = itemView.findViewById(R.id.tvGradeBreakdown);
            tvGradeBadge = itemView.findViewById(R.id.tvGradeBadge);
        }

        public void bind(GradebookItem item, OnGradebookClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvCourseClass != null) tvCourseClass.setText(item.courseClass);
            if (tvGradeBreakdown != null) tvGradeBreakdown.setText(item.gradeBreakdown);
            if (tvGradeBadge != null) {
                tvGradeBadge.setText(item.gradeBadge);
                if (item.gradeBadge.contains("A") || item.gradeBadge.contains("B+")) {
                    tvGradeBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.gradeBadge.contains("C") || item.gradeBadge.contains("D")) {
                    tvGradeBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvGradeBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onGradebookClick(item);
            });
        }
    }
}
