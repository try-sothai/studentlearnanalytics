package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Score;

import java.util.List;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ScoreViewHolder> {

    public interface OnScoreClickListener {
        void onScoreClick(Score score);
    }

    private final Context context;
    private final List<Score> scoreList;
    private final OnScoreClickListener listener;

    public ScoreListAdapter(Context context, List<Score> scoreList, OnScoreClickListener listener) {
        this.context = context;
        this.scoreList = scoreList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_score_row, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.bind(score, listener);
    }

    @Override
    public int getItemCount() {
        return scoreList != null ? scoreList.size() : 0;
    }

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        ImageView imgScoreTypeIcon;
        TextView tvScoreTitle, tvScoreDate, tvScoreValue;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            imgScoreTypeIcon = itemView.findViewById(R.id.imgScoreTypeIcon);
            tvScoreTitle = itemView.findViewById(R.id.tvScoreTitle);
            tvScoreDate = itemView.findViewById(R.id.tvScoreDate);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
        }

        public void bind(final Score score, final OnScoreClickListener listener) {
            if (tvScoreTitle != null) tvScoreTitle.setText(score.getTitle());
            if (tvScoreDate != null) tvScoreDate.setText(score.getDate());
            if (tvScoreValue != null) tvScoreValue.setText(score.getScore() + " / " + score.getMaxScore());

            if (imgScoreTypeIcon != null) {
                if ("Quiz".equalsIgnoreCase(score.getType())) {
                    imgScoreTypeIcon.setImageResource(R.drawable.ic_analytics);
                } else if ("Exam".equalsIgnoreCase(score.getType())) {
                    imgScoreTypeIcon.setImageResource(R.drawable.ic_prediction);
                } else {
                    imgScoreTypeIcon.setImageResource(R.drawable.ic_assignment);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScoreClick(score);
            });
        }
    }
}
