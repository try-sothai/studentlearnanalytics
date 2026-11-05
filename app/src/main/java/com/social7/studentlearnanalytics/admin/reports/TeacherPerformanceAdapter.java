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

public class TeacherPerformanceAdapter extends RecyclerView.Adapter<TeacherPerformanceAdapter.ViewHolder> {

    public static class PerformanceItem {
        public String teacherId;
        public String teacherName;
        public String department;
        public String passRateRating;
        public String score;

        public PerformanceItem(String teacherId, String teacherName, String department, String passRateRating, String score) {
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.department = department;
            this.passRateRating = passRateRating;
            this.score = score;
        }
    }

    public interface OnPerformanceClickListener {
        void onPerformanceClick(PerformanceItem item);
    }

    private final Context context;
    private final List<PerformanceItem> originalList;
    private final List<PerformanceItem> filteredList;
    private final OnPerformanceClickListener listener;

    public TeacherPerformanceAdapter(Context context, List<PerformanceItem> list, OnPerformanceClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher_performance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PerformanceItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (PerformanceItem item : originalList) {
            if (text.isEmpty() ||
                (item.teacherId != null && item.teacherId.toLowerCase().contains(text)) ||
                (item.teacherName != null && item.teacherName.toLowerCase().contains(text)) ||
                (item.department != null && item.department.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTeacherId, tvTeacherName, tvDepartment, tvPassRateRating, tvPerformanceScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeacherId = itemView.findViewById(R.id.tvTeacherId);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvPassRateRating = itemView.findViewById(R.id.tvPassRateRating);
            tvPerformanceScore = itemView.findViewById(R.id.tvPerformanceScore);
        }

        public void bind(PerformanceItem item, OnPerformanceClickListener listener) {
            if (tvTeacherId != null) tvTeacherId.setText(item.teacherId);
            if (tvTeacherName != null) tvTeacherName.setText(item.teacherName);
            if (tvDepartment != null) tvDepartment.setText(item.department);
            if (tvPassRateRating != null) tvPassRateRating.setText(item.passRateRating);
            if (tvPerformanceScore != null) {
                tvPerformanceScore.setText(item.score);
                tvPerformanceScore.setTextColor(Color.parseColor("#10B981"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPerformanceClick(item);
            });
        }
    }
}
