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

public class PredVsActualAdapter extends RecyclerView.Adapter<PredVsActualAdapter.ViewHolder> {

    public static class PredVsActualItem {
        public String studentId;
        public String studentName;
        public String courseClass;
        public String comparisonDetail;
        public String errorStatusBadge;

        public PredVsActualItem(String studentId, String studentName, String courseClass, String comparisonDetail, String errorStatusBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseClass = courseClass;
            this.comparisonDetail = comparisonDetail;
            this.errorStatusBadge = errorStatusBadge;
        }
    }

    public interface OnComparisonClickListener {
        void onComparisonClick(PredVsActualItem item);
    }

    private final Context context;
    private final List<PredVsActualItem> originalList;
    private final List<PredVsActualItem> filteredList;
    private final OnComparisonClickListener listener;

    public PredVsActualAdapter(Context context, List<PredVsActualItem> list, OnComparisonClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_pred_vs_actual_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredVsActualItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (PredVsActualItem item : originalList) {
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
        private final TextView tvStudentId, tvStudentName, tvCourseClass, tvComparisonDetail, tvErrorStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCourseClass = itemView.findViewById(R.id.tvCourseClass);
            tvComparisonDetail = itemView.findViewById(R.id.tvComparisonDetail);
            tvErrorStatusBadge = itemView.findViewById(R.id.tvErrorStatusBadge);
        }

        public void bind(PredVsActualItem item, OnComparisonClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvCourseClass != null) tvCourseClass.setText(item.courseClass);
            if (tvComparisonDetail != null) tvComparisonDetail.setText(item.comparisonDetail);

            if (tvErrorStatusBadge != null) {
                tvErrorStatusBadge.setText(item.errorStatusBadge);
                if (item.errorStatusBadge.contains("Low") || item.errorStatusBadge.contains("< 2")) {
                    tvErrorStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.errorStatusBadge.contains("Moderate")) {
                    tvErrorStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvErrorStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onComparisonClick(item);
            });
        }
    }
}
