package com.social7.studentlearnanalytics.admin.classes;

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

public class IncidentWorkspaceAdapter extends RecyclerView.Adapter<IncidentWorkspaceAdapter.IncidentViewHolder> {

    public static class IncidentItem {
        public String id;
        public String title;
        public String studentName;
        public String studentId;
        public String className;
        public String category;
        public String priority;
        public String status;
        public String date;
        public String assignedStaff;

        public IncidentItem(String id, String title, String studentName, String studentId, String className, String category, String priority, String status, String date, String assignedStaff) {
            this.id = id;
            this.title = title;
            this.studentName = studentName;
            this.studentId = studentId;
            this.className = className;
            this.category = category;
            this.priority = priority;
            this.status = status;
            this.date = date;
            this.assignedStaff = assignedStaff;
        }
    }

    public interface OnIncidentClickListener {
        void onIncidentClick(IncidentItem item);
        void onStudentClick(IncidentItem item);
    }

    private final Context context;
    private final List<IncidentItem> originalList;
    private final List<IncidentItem> filteredList;
    private final OnIncidentClickListener listener;

    public IncidentWorkspaceAdapter(Context context, List<IncidentItem> list, OnIncidentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        IncidentItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (IncidentItem item : originalList) {
            if (text.isEmpty() ||
                item.title.toLowerCase().contains(text) ||
                item.studentName.toLowerCase().contains(text) ||
                item.studentId.toLowerCase().contains(text) ||
                item.className.toLowerCase().contains(text)) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class IncidentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPriority, tvCategory, tvStatus, tvTitle, tvStudent, tvDetails;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPriority = itemView.findViewById(R.id.tvIncidentPriority);
            tvCategory = itemView.findViewById(R.id.tvIncidentCategory);
            tvStatus = itemView.findViewById(R.id.tvIncidentStatus);
            tvTitle = itemView.findViewById(R.id.tvIncidentTitle);
            tvStudent = itemView.findViewById(R.id.tvIncidentStudent);
            tvDetails = itemView.findViewById(R.id.tvIncidentDetails);
        }

        public void bind(IncidentItem item, OnIncidentClickListener listener) {
            if (tvPriority != null) {
                tvPriority.setText(item.priority);
                if ("High Priority".equalsIgnoreCase(item.priority)) {
                    tvPriority.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvPriority.setTextColor(Color.parseColor("#3155F5"));
                }
            }
            if (tvCategory != null) tvCategory.setText(item.category);
            if (tvStatus != null) {
                tvStatus.setText(item.status);
                if ("Resolved".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Under Review".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#EF4444"));
                }
            }
            if (tvTitle != null) tvTitle.setText(item.title);
            if (tvStudent != null) {
                tvStudent.setText(tvStudent.getContext().getString(R.string.student_d0b71d) + item.studentName + " (" + item.studentId + ") [View Dossier]");
                tvStudent.setOnClickListener(v -> {
                    if (listener != null) listener.onStudentClick(item);
                });
            }
            if (tvDetails != null) {
                tvDetails.setText(tvDetails.getContext().getString(R.string.class_4530a9) + item.className + " • Logged: " + item.date + " • Staff: " + item.assignedStaff);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onIncidentClick(item);
            });
        }
    }
}
