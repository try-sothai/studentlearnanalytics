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

public class InterventionPlanAdapter extends RecyclerView.Adapter<InterventionPlanAdapter.ViewHolder> {

    public static class InterventionPlanItem {
        public String studentId;
        public String studentName;
        public String activityTitle;
        public String timeline;
        public String statusBadge;

        public InterventionPlanItem(String studentId, String studentName, String activityTitle, String timeline, String statusBadge) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.activityTitle = activityTitle;
            this.timeline = timeline;
            this.statusBadge = statusBadge;
        }
    }

    public interface OnInterventionClickListener {
        void onInterventionClick(InterventionPlanItem item);
    }

    private final Context context;
    private final List<InterventionPlanItem> originalList;
    private final List<InterventionPlanItem> filteredList;
    private final OnInterventionClickListener listener;

    public InterventionPlanAdapter(Context context, List<InterventionPlanItem> list, OnInterventionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_intervention_plan_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterventionPlanItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (InterventionPlanItem item : originalList) {
            if (text.isEmpty() ||
                (item.studentId != null && item.studentId.toLowerCase().contains(text)) ||
                (item.studentName != null && item.studentName.toLowerCase().contains(text)) ||
                (item.activityTitle != null && item.activityTitle.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentId, tvStudentName, tvPlanActivityTitle, tvPlanTimeline, tvPlanStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvPlanActivityTitle = itemView.findViewById(R.id.tvPlanActivityTitle);
            tvPlanTimeline = itemView.findViewById(R.id.tvPlanTimeline);
            tvPlanStatusBadge = itemView.findViewById(R.id.tvPlanStatusBadge);
        }

        public void bind(InterventionPlanItem item, OnInterventionClickListener listener) {
            if (tvStudentId != null) tvStudentId.setText(item.studentId);
            if (tvStudentName != null) tvStudentName.setText(item.studentName);
            if (tvPlanActivityTitle != null) tvPlanActivityTitle.setText(item.activityTitle);
            if (tvPlanTimeline != null) tvPlanTimeline.setText(item.timeline);

            if (tvPlanStatusBadge != null) {
                tvPlanStatusBadge.setText(item.statusBadge);
                if (item.statusBadge.contains("Completed")) {
                    tvPlanStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (item.statusBadge.contains("In Progress")) {
                    tvPlanStatusBadge.setTextColor(Color.parseColor("#2563EB"));
                } else {
                    tvPlanStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onInterventionClick(item);
            });
        }
    }
}
