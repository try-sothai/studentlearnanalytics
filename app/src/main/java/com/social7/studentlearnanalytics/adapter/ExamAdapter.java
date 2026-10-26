package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Exam;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private final Context context;
    private final List<Exam> examList;

    public ExamAdapter(Context context, List<Exam> examList) {
        this.context = context;
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.bind(exam);
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateMonth, tvDateDay, tvCourseName, tvExamDetails, tvExamScore;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateMonth = itemView.findViewById(R.id.tvDateMonth);
            tvDateDay = itemView.findViewById(R.id.tvDateDay);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvExamDetails = itemView.findViewById(R.id.tvExamDetails);
            tvExamScore = itemView.findViewById(R.id.tvExamScore);
        }

        public void bind(Exam exam) {
            if (tvDateMonth != null) tvDateMonth.setText(exam.getDateMonth());
            if (tvDateDay != null) tvDateDay.setText(exam.getDateDay());
            if (tvCourseName != null) tvCourseName.setText(exam.getCourseName());
            if (tvExamDetails != null) tvExamDetails.setText(exam.getDetails());

            if (tvExamScore != null) {
                if (exam.isCompleted() && exam.getScoreText() != null) {
                    tvExamScore.setVisibility(View.VISIBLE);
                    tvExamScore.setText(exam.getScoreText());
                } else {
                    tvExamScore.setVisibility(View.GONE);
                }
            }
        }
    }
}
