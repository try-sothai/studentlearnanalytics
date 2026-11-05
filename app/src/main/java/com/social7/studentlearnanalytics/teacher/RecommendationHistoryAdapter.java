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

public class RecommendationHistoryAdapter extends RecyclerView.Adapter<RecommendationHistoryAdapter.ViewHolder> {

    public static class RecommendationHistoryItem {
        public String studentId;
        public String studentName;
        public String recTitle;
        public String recDates;
        public String impactBadge;

        public RecommendationHistoryItem(String studentId, String studentName, String recTitle, String recDates, String impactBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.recTitle = recTitle;
            this.recDates = recDates;
            this.impactBadge = impactBadge;
        }
    }

    public interface OnHistoryClickListener {
        void onHistoryClick(RecommendationHistoryItem item);
    }

    private final Context context;
    private final List<RecommendationHistoryItem> originalList;
    private final List<RecommendationHistoryItem> filteredList;
    private final OnHistoryClickListener listener;

    public RecommendationHistoryAdapter(Context context, List<RecommendationHistoryItem> list, OnHistoryClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_recommendation_history_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendationHistoryItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (RecommendationHistoryItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.recTitle != null && item.recTitle.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvRecTitle, tvRecDates, tvImpactBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvRecTitle = itemView.findViewById(R.id.tvRecTitle);
            tvRecDates = itemView.findViewById(R.id.tvRecDates);
            tvImpactBadge = itemView.findViewById(R.id.tvImpactBadge);
        }

        public void bind(RecommendationHistoryItem item, OnHistoryClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvRecTitle != null) tvRecTitle.setText(item.recTitle);
            if (tvRecDates != null) tvRecDates.setText(item.recDates);

            if (tvImpactBadge != null) {
                tvImpactBadge.setText(item.impactBadge);
                if (item.impactBadge.contains("+") || item.impactBadge.contains("Increase") || item.impactBadge.contains("Improved")) {
                    tvImpactBadge.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvImpactBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onHistoryClick(item);
            });
        }
    }
}
