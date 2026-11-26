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

public class ModelComparisonAdapter extends RecyclerView.Adapter<ModelComparisonAdapter.ViewHolder> {

    public static class ModelComparisonItem {
        public String modelName;
        public String modelParams;
        public String modelMetrics;
        public String bestModelBadge;

        public ModelComparisonItem(String modelName, String modelParams, String modelMetrics, String bestModelBadge) {
            this.modelName = modelName;
            this.modelParams = modelParams;
            this.modelMetrics = modelMetrics;
            this.bestModelBadge = bestModelBadge;
        }
    }

    public interface OnModelClickListener {
        void onModelClick(ModelComparisonItem item);
    }

    private final Context context;
    private final List<ModelComparisonItem> originalList;
    private final List<ModelComparisonItem> filteredList;
    private final OnModelClickListener listener;

    public ModelComparisonAdapter(Context context, List<ModelComparisonItem> list, OnModelClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_model_comparison_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelComparisonItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void refresh() {
        filter("");
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (ModelComparisonItem item : originalList) {
            if (text.isEmpty() ||
                (item.modelName != null && item.modelName.toLowerCase().contains(text)) ||
                (item.modelParams != null && item.modelParams.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvModelName, tvModelParams, tvModelMetrics, tvBestModelBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModelName = itemView.findViewById(R.id.tvModelName);
            tvModelParams = itemView.findViewById(R.id.tvModelParams);
            tvModelMetrics = itemView.findViewById(R.id.tvModelMetrics);
            tvBestModelBadge = itemView.findViewById(R.id.tvBestModelBadge);
        }

        public void bind(ModelComparisonItem item, OnModelClickListener listener) {
            if (tvModelName != null) tvModelName.setText(item.modelName);
            if (tvModelParams != null) tvModelParams.setText(item.modelParams);
            if (tvModelMetrics != null) tvModelMetrics.setText(item.modelMetrics);

            if (tvBestModelBadge != null) {
                tvBestModelBadge.setText(item.bestModelBadge);
                if (item.bestModelBadge.contains("Best") || item.bestModelBadge.contains("Optimal")) {
                    tvBestModelBadge.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvBestModelBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onModelClick(item);
            });
        }
    }
}
