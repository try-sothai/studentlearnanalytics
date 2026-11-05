package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public static class AssignmentCardItem {
        public String id;
        public String courseCode;
        public String title;
        public String classCohort;
        public String details;
        public String status;

        public AssignmentCardItem(String id, String courseCode, String title, String classCohort, String details, String status) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.classCohort = classCohort;
            this.details = details;
            this.status = status;
        }
    }

    public interface OnAssignmentActionClickListener {
        void onGradeClick(AssignmentCardItem item);
        void onDeleteClick(AssignmentCardItem item);
    }

    private final Context context;
    private final List<AssignmentCardItem> originalList;
    private final List<AssignmentCardItem> filteredList;
    private final OnAssignmentActionClickListener listener;

    public AssignmentAdapter(Context context, List<AssignmentCardItem> list, OnAssignmentActionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_assignment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentCardItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (AssignmentCardItem item : originalList) {
            if (text.isEmpty() ||
                (item.courseCode != null && item.courseCode.toLowerCase().contains(text)) ||
                (item.title != null && item.title.toLowerCase().contains(text)) ||
                (item.classCohort != null && item.classCohort.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvAssignmentTitle, tvClassCohort, tvAssignmentDetails, tvStatusBadge;
        private final MaterialButton btnGradeAssignment, btnDeleteAssignment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvAssignmentTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvClassCohort = itemView.findViewById(R.id.tvClassCohort);
            tvAssignmentDetails = itemView.findViewById(R.id.tvAssignmentDetails);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            btnGradeAssignment = itemView.findViewById(R.id.btnGradeAssignment);
            btnDeleteAssignment = itemView.findViewById(R.id.btnDeleteAssignment);
        }

        public void bind(AssignmentCardItem item, OnAssignmentActionClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvAssignmentTitle != null) tvAssignmentTitle.setText(item.title);
            if (tvClassCohort != null) tvClassCohort.setText(item.classCohort);
            if (tvAssignmentDetails != null) tvAssignmentDetails.setText(item.details);

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(item.status);
                if ("Published".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if ("Draft".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#9333EA"));
                } else {
                    tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            if (btnGradeAssignment != null) {
                btnGradeAssignment.setOnClickListener(v -> {
                    if (listener != null) listener.onGradeClick(item);
                });
            }

            if (btnDeleteAssignment != null) {
                btnDeleteAssignment.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }
}
