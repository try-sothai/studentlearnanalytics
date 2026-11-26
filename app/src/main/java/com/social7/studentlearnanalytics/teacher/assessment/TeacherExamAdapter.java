package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Context;
import android.content.Intent;
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
        public final boolean isPublished;

        public ExamCardItem(int id, String courseCode, String title, String classAndType, String details, String status, boolean isPublished) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.classAndType = classAndType;
            this.details = details;
            this.status = status;
            this.isPublished = isPublished;
        }

        public ExamCardItem(int id, String courseCode, String title, String classAndType, String details, String status) {
            this(id, courseCode, title, classAndType, details, status, "published".equalsIgnoreCase(status));
        }
    }

    public interface OnExamActionListener {
        void onGradeClick(ExamCardItem item);
        void onDeleteClick(ExamCardItem item);
        default boolean handlesEditClick() { return false; }
        default void onEditClick(ExamCardItem item) {}
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_exam_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamCardItem item = filteredList.get(position);
        holder.bind(context, item, listener);
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
        private final TextView tvExamCourseCode, tvExamCardTitle, tvExamCardClassType, tvExamCardDetails, tvExamCardStatusBadge;
        private final MaterialButton btnDeleteExamCard, btnEditExamCard, btnManageGradeExamCard;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamCourseCode = itemView.findViewById(R.id.tvExamCourseCode);
            tvExamCardTitle = itemView.findViewById(R.id.tvExamCardTitle);
            tvExamCardClassType = itemView.findViewById(R.id.tvExamCardClassType);
            tvExamCardDetails = itemView.findViewById(R.id.tvExamCardDetails);
            tvExamCardStatusBadge = itemView.findViewById(R.id.tvExamCardStatusBadge);
            btnDeleteExamCard = itemView.findViewById(R.id.btnDeleteExamCard);
            btnEditExamCard = itemView.findViewById(R.id.btnEditExamCard);
            btnManageGradeExamCard = itemView.findViewById(R.id.btnManageGradeExamCard);
        }

        void bind(Context context, ExamCardItem item, OnExamActionListener listener) {
            if (tvExamCourseCode != null) tvExamCourseCode.setText(item.courseCode);
            if (tvExamCardTitle != null) tvExamCardTitle.setText(item.title);
            if (tvExamCardClassType != null) tvExamCardClassType.setText(item.classAndType);
            if (tvExamCardDetails != null) tvExamCardDetails.setText(item.details);

            if (tvExamCardStatusBadge != null) {
                String badgeText = item.status != null && !item.status.isEmpty()
                        ? item.status
                        : (item.isPublished ? "Results Published" : "Scheduled");
                tvExamCardStatusBadge.setText(badgeText);
                String lower = badgeText.toLowerCase(Locale.US);
                if (item.isPublished || lower.contains("published") || "completed".equals(lower)) {
                    tvExamCardStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if (lower.contains("pending")) {
                    tvExamCardStatusBadge.setTextColor(Color.parseColor("#D97706"));
                } else if ("draft".equals(lower)) {
                    tvExamCardStatusBadge.setTextColor(Color.parseColor("#9333EA"));
                } else if ("cancelled".equals(lower)) {
                    tvExamCardStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvExamCardStatusBadge.setTextColor(Color.parseColor("#2563EB"));
                }
            }

            if (btnManageGradeExamCard != null) {
                btnManageGradeExamCard.setOnClickListener(v -> {
                    if (listener != null) listener.onGradeClick(item);
                });
            }

            if (btnEditExamCard != null) {
                btnEditExamCard.setOnClickListener(v -> {
                    if (listener != null && listener.handlesEditClick()) {
                        listener.onEditClick(item);
                    } else if (context != null) {
                        Intent intent = new Intent(context, EditAssessmentActivity.class);
                        intent.putExtra("assessment_type", "Exam");
                        intent.putExtra("exam_id", item.id);
                        context.startActivity(intent);
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onGradeClick(item);
            });

            if (btnDeleteExamCard != null) {
                btnDeleteExamCard.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(item);
                });
            }
        }
    }
}
