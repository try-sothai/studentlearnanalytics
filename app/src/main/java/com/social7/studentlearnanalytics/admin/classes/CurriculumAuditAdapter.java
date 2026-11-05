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

public class CurriculumAuditAdapter extends RecyclerView.Adapter<CurriculumAuditAdapter.AuditViewHolder> {

    public static class CurriculumAuditItem {
        public String code;
        public String name;
        public String className;
        public String teacher;
        public int credits;
        public String status;
        public String note;

        public CurriculumAuditItem(String code, String name, String className, String teacher, int credits, String status, String note) {
            this.code = code;
            this.name = name;
            this.className = className;
            this.teacher = teacher;
            this.credits = credits;
            this.status = status;
            this.note = note;
        }
    }

    public interface OnAuditClickListener {
        void onAuditClick(CurriculumAuditItem item);
    }

    private final Context context;
    private final List<CurriculumAuditItem> originalList;
    private final List<CurriculumAuditItem> filteredList;
    private final OnAuditClickListener listener;

    public CurriculumAuditAdapter(Context context, List<CurriculumAuditItem> list, OnAuditClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_curriculum_audit, parent, false);
        return new AuditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        CurriculumAuditItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (CurriculumAuditItem item : originalList) {
            if (text.isEmpty() ||
                item.code.toLowerCase().contains(text) ||
                item.name.toLowerCase().contains(text) ||
                item.className.toLowerCase().contains(text) ||
                item.teacher.toLowerCase().contains(text)) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class AuditViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode, tvName, tvStatus, tvDetails, tvFinding;

        public AuditViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCourseCode);
            tvName = itemView.findViewById(R.id.tvCourseName);
            tvStatus = itemView.findViewById(R.id.tvAuditStatus);
            tvDetails = itemView.findViewById(R.id.tvAssignmentDetails);
            tvFinding = itemView.findViewById(R.id.tvAuditFinding);
        }

        public void bind(CurriculumAuditItem item, OnAuditClickListener listener) {
            if (tvCode != null) tvCode.setText(item.code);
            if (tvName != null) tvName.setText(item.name);
            if (tvStatus != null) {
                tvStatus.setText(item.status);
                if ("Verified".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Missing Syllabus".equalsIgnoreCase(item.status) || "Missing Teacher".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#EF4444"));
                }
            }
            if (tvDetails != null) {
                tvDetails.setText("Class: " + item.className + " • Teacher: " + item.teacher + " • " + item.credits + " Credits");
            }
            if (tvFinding != null) {
                if (item.note != null && !item.note.isEmpty()) {
                    tvFinding.setVisibility(View.VISIBLE);
                    tvFinding.setText("Audit Note: " + item.note);
                } else {
                    tvFinding.setVisibility(View.GONE);
                }
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAuditClick(item);
            });
        }
    }
}
