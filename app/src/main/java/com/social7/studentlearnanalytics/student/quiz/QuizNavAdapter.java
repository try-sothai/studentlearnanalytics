package com.social7.studentlearnanalytics.student.quiz;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.List;

public class QuizNavAdapter extends RecyclerView.Adapter<QuizNavAdapter.ViewHolder> {

    public interface OnQuestionSelectListener {
        void onSelect(int index);
    }

    private final List<String> answers;
    private int currentIndex = 0;
    private final OnQuestionSelectListener listener;

    public QuizNavAdapter(List<String> answers, OnQuestionSelectListener listener) {
        this.answers = answers;
        this.listener = listener;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_nav_number, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tv = holder.tvNavNumber;
        tv.setText(String.valueOf(position + 1));

        boolean isCurrent = position == currentIndex;
        boolean isAnswered = position < answers.size() && answers.get(position) != null && !answers.get(position).trim().isEmpty();

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16f);

        if (isCurrent) {
            bg.setColor(Color.parseColor("#5B3DF5"));
            bg.setStroke(3, Color.parseColor("#1E1B4B"));
            tv.setTextColor(Color.WHITE);
        } else if (isAnswered) {
            bg.setColor(Color.parseColor("#ECFDF5"));
            bg.setStroke(2, Color.parseColor("#10B981"));
            tv.setTextColor(Color.parseColor("#047857"));
        } else {
            bg.setColor(Color.parseColor("#F8FAFC"));
            bg.setStroke(2, Color.parseColor("#CBD5E1"));
            tv.setTextColor(Color.parseColor("#64748B"));
        }

        tv.setBackground(bg);
        tv.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNavNumber;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNavNumber = itemView.findViewById(R.id.tvNavNumber);
        }
    }
}
