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
import java.util.Locale;

public class TeacherExamAdapter extends RecyclerView.Adapter<TeacherExamAdapter.ViewHolder> {

    public static class ExamCardItem {
        public final int id;
        public final String courseCode;
        public final String title;
        public final String classAndType;
        public final String details;
        public final String status;

        public ExamCardItem(int id, String courseCode, String title, String classAndType, String details, String status) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.classAndType = classAndType;
            this.details = details;
            this.status = status;
        }
    }

    public interface OnExamActionListener {
        void onDeleteClick(ExamCardItem item);
    }

    private final Context context;
    private final List<ExamCardItem> originalList;
    private final List<ExamCardItem> filteredList;
    private final OnExamActionListener listener;

    public TeacherExamAdapter(Context context, List<ExamCardItem> list, OnExamActionListener listener) {
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
        ExamCardItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase(Locale.US) : "";
        for (ExamCardItem item : originalList) {
            if (text.isEmpty()
                    || (item.courseCode != null && item.courseCode.toLowerCase(Locale.US).contains(text))
                    || (item.title != null && item.title.toLowerCase(Locale.US).contains(text))
                    || (item.classAndType != null && item.classAndType.toLowerCase(Locale.US).contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvAssignmentTitle, tvClassCohort, tvAssignmentDetails, tvStatusBadge;
        private final MaterialButton btnGradeAssignment, btnDeleteAssignment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvAssignmentTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvClassCohort = itemView.findViewById(R.id.tvClassCohort);
            tvAssignmentDetails = itemView.findViewById(R.id.tvAssignmentDetails);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnGradeAssignment = itemView.findViewById(R.id.btnGradeAssignment);
            btnDeleteAssignment = itemView.findViewById(R.id.btnDeleteAssignment);
        }

        void bind(ExamCardItem item, OnExamActionListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvAssignmentTitle != null) tvAssignmentTitle.setText(item.title);
            if (tvClassCohort != null) tvClassCohort.setText(item.classAndType);
            if (tvAssignmentDetails != null) tvAssignmentDetails.setText(item.details);

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(item.status);
                if ("Completed".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if ("Draft".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#9333EA"));
                } else {
                    tvStatusBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            if (btnGradeAssignment != null) {
                btnGradeAssignment.setVisibility(View.GONE);
            }
            if (btnDeleteAssignment != null) {
                btnDeleteAssignment.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }
}
