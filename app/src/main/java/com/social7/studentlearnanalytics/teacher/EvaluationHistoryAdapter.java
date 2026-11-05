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

public class EvaluationHistoryAdapter extends RecyclerView.Adapter<EvaluationHistoryAdapter.ViewHolder> {

    public static class EvaluationHistoryItem {
        public String date;
        public String modelVersion;
        public String courseDataset;
        public String metricsDetail;
        public String statusBadge;

        public EvaluationHistoryItem(String date, String modelVersion, String courseDataset, String metricsDetail, String statusBadge) {
            this.date = date;
            this.modelVersion = modelVersion;
            this.courseDataset = courseDataset;
            this.metricsDetail = metricsDetail;
            this.statusBadge = statusBadge;
        }
    }

    public interface OnEvaluationHistoryClickListener {
        void onHistoryClick(EvaluationHistoryItem item);
    }

    private final Context context;
    private final List<EvaluationHistoryItem> originalList;
    private final List<EvaluationHistoryItem> filteredList;
    private final OnEvaluationHistoryClickListener listener;

    public EvaluationHistoryAdapter(Context context, List<EvaluationHistoryItem> list, OnEvaluationHistoryClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_evaluation_history_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EvaluationHistoryItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (EvaluationHistoryItem item : originalList) {
            if (text.isEmpty() ||
                (item.date != null && item.date.toLowerCase().contains(text)) ||
                (item.modelVersion != null && item.modelVersion.toLowerCase().contains(text)) ||
                (item.courseDataset != null && item.courseDataset.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEvalDate, tvModelVersion, tvCourseDataset, tvEvalMetricsDetail, tvEvalStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEvalDate = itemView.findViewById(R.id.tvEvalDate);
            tvModelVersion = itemView.findViewById(R.id.tvModelVersion);
            tvCourseDataset = itemView.findViewById(R.id.tvCourseDataset);
            tvEvalMetricsDetail = itemView.findViewById(R.id.tvEvalMetricsDetail);
            tvEvalStatusBadge = itemView.findViewById(R.id.tvEvalStatusBadge);
        }

        public void bind(EvaluationHistoryItem item, OnEvaluationHistoryClickListener listener) {
            if (tvEvalDate != null) tvEvalDate.setText(item.date);
            if (tvModelVersion != null) tvModelVersion.setText(item.modelVersion);
            if (tvCourseDataset != null) tvCourseDataset.setText(item.courseDataset);
            if (tvEvalMetricsDetail != null) tvEvalMetricsDetail.setText(item.metricsDetail);

            if (tvEvalStatusBadge != null) {
                tvEvalStatusBadge.setText(item.statusBadge);
                if (item.statusBadge.contains("Optimal") || item.statusBadge.contains("Good")) {
                    tvEvalStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.statusBadge.contains("Moderate")) {
                    tvEvalStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvEvalStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onHistoryClick(item);
            });
        }
    }
}
