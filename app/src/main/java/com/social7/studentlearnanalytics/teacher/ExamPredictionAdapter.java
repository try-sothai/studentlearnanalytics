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

public class ExamPredictionAdapter extends RecyclerView.Adapter<ExamPredictionAdapter.ViewHolder> {

    public static class ExamPredictionItem {
        public String studentId;
        public String studentName;
        public String courseClass;
        public String metrics;
        public String gradeBadge;

        public ExamPredictionItem(String studentId, String studentName, String courseClass, String metrics, String gradeBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseClass = courseClass;
            this.metrics = metrics;
            this.gradeBadge = gradeBadge;
        }
    }

    public interface OnPredictionClickListener {
        void onPredictionClick(ExamPredictionItem item);
    }

    private final Context context;
    private final List<ExamPredictionItem> originalList;
    private final List<ExamPredictionItem> filteredList;
    private final OnPredictionClickListener listener;

    public ExamPredictionAdapter(Context context, List<ExamPredictionItem> list, OnPredictionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_exam_prediction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamPredictionItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (ExamPredictionItem item : originalList) {
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
        private final TextView tvStudentId, tvStudentName, tvCourseClass, tvPredictionMetrics, tvPredictedGradeBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCourseClass = itemView.findViewById(R.id.tvCourseClass);
            tvPredictionMetrics = itemView.findViewById(R.id.tvPredictionMetrics);
            tvPredictedGradeBadge = itemView.findViewById(R.id.tvPredictedGradeBadge);
        }

        public void bind(ExamPredictionItem item, OnPredictionClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvCourseClass != null) tvCourseClass.setText(item.courseClass);
            if (tvPredictionMetrics != null) tvPredictionMetrics.setText(item.metrics);
            if (tvPredictedGradeBadge != null) {
                tvPredictedGradeBadge.setText(item.gradeBadge);
                if (item.gradeBadge.contains("A") || item.gradeBadge.contains("B+")) {
                    tvPredictedGradeBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.gradeBadge.contains("C") || item.gradeBadge.contains("D")) {
                    tvPredictedGradeBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvPredictedGradeBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPredictionClick(item);
            });
        }
    }
}
