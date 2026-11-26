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

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {

    public static class QuizCardItem {
        public String id;
        public String courseCode;
        public String title;
        public String classCohort;
        public String details;
        public String status;
        public int attemptsCount;
        public int pendingGradingCount;

        public QuizCardItem(String id, String courseCode, String title, String classCohort, String details, String status) {
            this(id, courseCode, title, classCohort, details, status, 0, 0);
        }

        public QuizCardItem(String id, String courseCode, String title, String classCohort, String details, String status,
                            int attemptsCount, int pendingGradingCount) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.classCohort = classCohort;
            this.details = details;
            this.status = status;
            this.attemptsCount = attemptsCount;
            this.pendingGradingCount = pendingGradingCount;
        }
    }

    public interface OnQuizActionClickListener {
        void onBuilderClick(QuizCardItem item);
        void onPublishClick(QuizCardItem item);
        default void onAttemptsClick(QuizCardItem item) {}
    }

    private final Context context;
    private final List<QuizCardItem> originalList;
    private final List<QuizCardItem> filteredList;
    private final OnQuizActionClickListener listener;

    public QuizAdapter(Context context, List<QuizCardItem> list, OnQuizActionClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_quiz_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizCardItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (QuizCardItem item : originalList) {
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
        private final TextView tvCourseCode, tvQuizTitle, tvClassCohort, tvQuizDetails, tvStatusBadge;
        private final MaterialButton btnQuizAttempts, btnQuizBuilder, btnPublishQuiz;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvQuizTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvClassCohort = itemView.findViewById(R.id.tvClassCohort);
            tvQuizDetails = itemView.findViewById(R.id.tvQuizDetails);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            btnQuizAttempts = itemView.findViewById(R.id.btnQuizAttempts);
            btnQuizBuilder = itemView.findViewById(R.id.btnQuizBuilder);
            btnPublishQuiz = itemView.findViewById(R.id.btnPublishQuiz);
        }

        public void bind(QuizCardItem item, OnQuizActionClickListener listener) {
            if (tvCourseCode != null) tvCourseCode.setText(item.courseCode);
            if (tvQuizTitle != null) tvQuizTitle.setText(item.title);
            if (tvClassCohort != null) tvClassCohort.setText(item.classCohort);
            if (tvQuizDetails != null) tvQuizDetails.setText(item.details);

            if (tvStatusBadge != null) {
                tvStatusBadge.setText(item.status);
                if ("Active".equalsIgnoreCase(item.status) || "Published".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                } else if ("Draft".equalsIgnoreCase(item.status)) {
                    tvStatusBadge.setTextColor(Color.parseColor("#9333EA"));
                } else {
                    tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            if (btnQuizAttempts != null) {
                if (item.pendingGradingCount > 0) {
                    btnQuizAttempts.setText("Grade (" + item.pendingGradingCount + " Pending)");
                    btnQuizAttempts.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D97706")));
                    btnQuizAttempts.setTextColor(Color.WHITE);
                    btnQuizAttempts.setStrokeWidth(0);
                } else {
                    btnQuizAttempts.setText("Attempts (" + item.attemptsCount + ")");
                    btnQuizAttempts.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                    btnQuizAttempts.setTextColor(Color.parseColor("#3155F5"));
                    btnQuizAttempts.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#3155F5")));
                    btnQuizAttempts.setStrokeWidth(2);
                }
                btnQuizAttempts.setOnClickListener(v -> {
                    if (listener != null) listener.onAttemptsClick(item);
                });
            }

            if (btnQuizBuilder != null) {
                btnQuizBuilder.setOnClickListener(v -> {
                    if (listener != null) listener.onBuilderClick(item);
                });
            }

            if (btnPublishQuiz != null) {
                boolean isPublished = "Published".equalsIgnoreCase(item.status) || "Active".equalsIgnoreCase(item.status);
                btnPublishQuiz.setVisibility(isPublished ? View.GONE : View.VISIBLE);
                btnPublishQuiz.setOnClickListener(v -> {
                    if (listener != null) listener.onPublishClick(item);
                });
            }
        }
    }
}
