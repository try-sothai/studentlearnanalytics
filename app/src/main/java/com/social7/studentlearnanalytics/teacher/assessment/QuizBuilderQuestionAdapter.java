package com.social7.studentlearnanalytics.teacher.assessment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.QuizQuestionItem;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizBuilderQuestionAdapter extends RecyclerView.Adapter<QuizBuilderQuestionAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(QuizQuestionItem item);
        void onDelete(QuizQuestionItem item);
        void onDuplicate(QuizQuestionItem item);
        void onMove(int fromPos, int toPos);
    }

    private final List<QuizQuestionItem> items;
    private final Listener listener;
    private boolean locked = false;

    public QuizBuilderQuestionAdapter(List<QuizQuestionItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_builder_question, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        QuizQuestionItem q = items.get(position);
        h.tvQuestionNumber.setText("Question " + (position + 1));
        h.tvQuestionTypeBadge.setText(q.getTypeLabel());

        String markStr = AssessmentUiHelper.formatMarks(q.marks) + (q.marks == 1.0 ? " mark" : " marks");
        h.tvQuestionMarks.setText(markStr);

        h.tvQuestionText.setText(q.questionText);
        h.tvQuestionText.setMaxLines(q.expanded ? 20 : 2);
        h.tvQuestionText.setEllipsize(q.expanded ? null : TextUtils.TruncateAt.END);
        h.tvExpandToggle.setText(q.expanded ? "Collapse" : "Expand");

        String correctDisplay = formatCorrectDisplay(q);
        if ("multiple_choice".equalsIgnoreCase(q.questionType)) {
            h.tvCollapsedSummary.setText(String.format(Locale.US, "%d options • Correct: %s", q.optionCount, correctDisplay));
        } else if ("true_false".equalsIgnoreCase(q.questionType)) {
            h.tvCollapsedSummary.setText("True / False • Correct: " + correctDisplay);
        } else {
            h.tvCollapsedSummary.setText(correctDisplay.isEmpty() ? "Manual teacher grading" : ("Expected: " + correctDisplay));
        }
        h.tvCollapsedSummary.setVisibility(q.expanded ? View.GONE : View.VISIBLE);

        h.layoutExpandedBody.setVisibility(q.expanded ? View.VISIBLE : View.GONE);
        if (q.expanded) {
            h.tvOptionsBlock.setText(buildOptionsBlock(q));
            if ("short_answer".equalsIgnoreCase(q.questionType)) {
                h.tvCorrectAnswerLine.setText(correctDisplay.isEmpty()
                        ? "Grading: Manual teacher review"
                        : "Expected Answer: " + correctDisplay);
            } else {
                h.tvCorrectAnswerLine.setText("Correct Answer: " + correctDisplay);
            }
        }

        h.layoutQuestionActions.setVisibility(locked ? View.GONE : View.VISIBLE);
        h.btnMoveUp.setEnabled(position > 0);
        h.btnMoveDown.setEnabled(position < items.size() - 1);

        View.OnClickListener toggleClick = v -> {
            q.expanded = !q.expanded;
            notifyItemChanged(h.getBindingAdapterPosition());
        };
        h.layoutQuestionHeader.setOnClickListener(toggleClick);
        h.tvQuestionText.setOnClickListener(toggleClick);

        h.btnMoveUp.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos > 0 && listener != null) listener.onMove(pos, pos - 1);
        });
        h.btnMoveDown.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos >= 0 && pos < items.size() - 1 && listener != null) listener.onMove(pos, pos + 1);
        });
        h.btnDuplicateQuestion.setOnClickListener(v -> { if (listener != null) listener.onDuplicate(q); });
        h.btnEditQuestion.setOnClickListener(v -> { if (listener != null) listener.onEdit(q); });
        h.btnDeleteQuestion.setOnClickListener(v -> { if (listener != null) listener.onDelete(q); });
    }

    private String formatCorrectDisplay(QuizQuestionItem q) {
        if (q.correctAnswer == null) return "";
        if ("true_false".equalsIgnoreCase(q.questionType)) {
            return "true".equalsIgnoreCase(q.correctAnswer) ? "True" : "False";
        }
        return q.correctAnswer;
    }

    private String buildOptionsBlock(QuizQuestionItem q) {
        if ("true_false".equalsIgnoreCase(q.questionType)) {
            return "• True\n• False";
        }
        if ("short_answer".equalsIgnoreCase(q.questionType)) {
            return "Student enters a written short answer.";
        }
        List<String> lines = new ArrayList<>();
        if (q.optionA != null && !q.optionA.isEmpty()) lines.add("A. " + q.optionA);
        if (q.optionB != null && !q.optionB.isEmpty()) lines.add("B. " + q.optionB);
        if (q.optionC != null && !q.optionC.isEmpty()) lines.add("C. " + q.optionC);
        if (q.optionD != null && !q.optionD.isEmpty()) lines.add("D. " + q.optionD);
        return TextUtils.join("\n", lines);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutQuestionHeader, layoutExpandedBody, layoutQuestionActions;
        TextView tvQuestionNumber, tvQuestionTypeBadge, tvQuestionMarks, tvExpandToggle;
        TextView tvQuestionText, tvCollapsedSummary, tvOptionsBlock, tvCorrectAnswerLine;
        MaterialButton btnMoveUp, btnMoveDown, btnDuplicateQuestion, btnEditQuestion, btnDeleteQuestion;

        ViewHolder(@NonNull View v) {
            super(v);
            layoutQuestionHeader = v.findViewById(R.id.layoutQuestionHeader);
            layoutExpandedBody = v.findViewById(R.id.layoutExpandedBody);
            layoutQuestionActions = v.findViewById(R.id.layoutQuestionActions);
            tvQuestionNumber = v.findViewById(R.id.tvQuestionNumber);
            tvQuestionTypeBadge = v.findViewById(R.id.tvQuestionTypeBadge);
            tvQuestionMarks = v.findViewById(R.id.tvQuestionMarks);
            tvExpandToggle = v.findViewById(R.id.tvExpandToggle);
            tvQuestionText = v.findViewById(R.id.tvQuestionText);
            tvCollapsedSummary = v.findViewById(R.id.tvCollapsedSummary);
            tvOptionsBlock = v.findViewById(R.id.tvOptionsBlock);
            tvCorrectAnswerLine = v.findViewById(R.id.tvCorrectAnswerLine);
            btnMoveUp = v.findViewById(R.id.btnMoveUp);
            btnMoveDown = v.findViewById(R.id.btnMoveDown);
            btnDuplicateQuestion = v.findViewById(R.id.btnDuplicateQuestion);
            btnEditQuestion = v.findViewById(R.id.btnEditQuestion);
            btnDeleteQuestion = v.findViewById(R.id.btnDeleteQuestion);
        }
    }
}
