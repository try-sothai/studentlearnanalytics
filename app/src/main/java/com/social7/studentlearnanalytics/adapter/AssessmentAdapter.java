package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Assessment;
import com.social7.studentlearnanalytics.student.assignment.SubmitAssignmentActivity;
import com.social7.studentlearnanalytics.student.quiz.StudentQuizReviewActivity;
import com.social7.studentlearnanalytics.student.quiz.TakeQuizActivity;
import com.social7.studentlearnanalytics.utils.AssessmentUiHelper;
import com.social7.studentlearnanalytics.utils.QuizScoreUiHelper;

import java.util.List;
import java.util.Locale;

public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder> {

    private final List<Assessment> assessmentList;

    public AssessmentAdapter(List<Assessment> assessmentList) {
        this.assessmentList = assessmentList;
    }

    @NonNull
    @Override
    public AssessmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assessment, parent, false);
        return new AssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssessmentViewHolder holder, int position) {
        Assessment a = assessmentList.get(position);
        holder.titleText.setText(a.getTitle());

        // Course badge (code + name + class)
        if (holder.courseBadgeText != null) {
            StringBuilder courseLine = new StringBuilder();
            if (a.getCourseCode() != null && !a.getCourseCode().isEmpty()) {
                courseLine.append(a.getCourseCode());
            }
            if (a.getCourseName() != null && !a.getCourseName().trim().isEmpty()) {
                if (courseLine.length() > 0) courseLine.append(" • ");
                courseLine.append(a.getCourseName().trim());
            }
            if (a.getClassName() != null && !a.getClassName().trim().isEmpty()) {
                if (courseLine.length() > 0) courseLine.append(" • ");
                courseLine.append(a.getClassName().trim());
            }
            if (courseLine.length() > 0) {
                holder.courseBadgeText.setVisibility(View.VISIBLE);
                holder.courseBadgeText.setText(courseLine.toString());
            } else {
                holder.courseBadgeText.setVisibility(View.GONE);
            }
        }

        // Subtitle: date + type-relevant metadata
        StringBuilder meta = new StringBuilder();
        if (a.getDate() != null && !a.getDate().isEmpty()) {
            meta.append(a.getDate());
        }
        boolean isQuiz = "quiz".equalsIgnoreCase(a.getType());
        if (isQuiz) {
            if (a.getSubmissionStatusLabel() != null && !a.getSubmissionStatusLabel().isEmpty()
                    && !"Not Submitted".equalsIgnoreCase(a.getSubmissionStatusLabel())) {
                if (meta.length() > 0) meta.append(" • ");
                meta.append(a.getSubmissionStatusLabel());
            }
            if (a.getQuestionsCount() > 0) {
                if (meta.length() > 0) meta.append(" • ");
                meta.append(a.getQuestionsCount()).append(" Qs");
            }
            if (a.getDurationMinutes() > 0) {
                if (meta.length() > 0) meta.append(" • ");
                meta.append(a.getDurationMinutes()).append("m");
            }
        }
        if (!a.isGraded() && !a.isPendingGrading() && a.getTotalScore() > 0) {
            if (meta.length() > 0) meta.append(" • ");
            meta.append(a.getTotalScore()).append(" pts");
        }
        holder.dateText.setText(meta.toString());

        // Provisional note (only for quizzes awaiting teacher grading / partially graded)
        if (holder.provisionalNoteText != null) {
            if (isQuiz && (a.isPendingGrading() || a.isPartiallyGraded())) {
                holder.provisionalNoteText.setVisibility(View.VISIBLE);
                holder.provisionalNoteText.setText(a.isPartiallyGraded()
                        ? "Provisional score — some answers still pending"
                        : "Provisional score — awaiting teacher grading (not final)");
            } else {
                holder.provisionalNoteText.setVisibility(View.GONE);
            }
        }

        // Status / Score pill
        String statusBadge;
        if (isQuiz && a.isGraded() && !a.isPendingGrading()) {
            statusBadge = "Score: " + a.getFormattedScore();
        } else if (isQuiz && (a.isPendingGrading() || a.isPartiallyGraded())) {
            if (a.getProvisionalEarnedScore() != null) {
                double tot = a.getExactTotalScore() != null ? a.getExactTotalScore() : a.getTotalScore();
                statusBadge = "Score: " + AssessmentUiHelper.formatMarks(a.getProvisionalEarnedScore())
                        + " / " + AssessmentUiHelper.formatMarks(tot) + " earned";
            } else {
                statusBadge = a.isPartiallyGraded()
                        ? QuizScoreUiHelper.STATUS_PARTIALLY_GRADED
                        : QuizScoreUiHelper.STATUS_PENDING_GRADING;
            }
        } else {
            statusBadge = a.getFormattedScore();
        }
        holder.scoreText.setText(statusBadge);

        int badgeColor = resolveStatusColor(a);
        holder.indicatorView.setBackgroundTintList(ColorStateList.valueOf(badgeColor));
        holder.scoreText.setTextColor(badgeColor);

        // Explicit Grading Status badge on quiz cards
        if (holder.gradingStatusText != null) {
            if (isQuiz && a.getGradingStatusLabel() != null && !a.getGradingStatusLabel().isEmpty()) {
                holder.gradingStatusText.setVisibility(View.VISIBLE);
                holder.gradingStatusText.setText("Status: " + a.getGradingStatusLabel());
                if (QuizScoreUiHelper.STATUS_GRADED.equalsIgnoreCase(a.getGradingStatusLabel())) {
                    holder.gradingStatusText.setTextColor(Color.parseColor("#047857"));
                    holder.gradingStatusText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
                } else if (QuizScoreUiHelper.STATUS_PARTIALLY_GRADED.equalsIgnoreCase(a.getGradingStatusLabel())) {
                    holder.gradingStatusText.setTextColor(Color.parseColor("#1D4ED8"));
                    holder.gradingStatusText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                } else {
                    holder.gradingStatusText.setTextColor(Color.parseColor("#B45309"));
                    holder.gradingStatusText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
                }
            } else {
                holder.gradingStatusText.setVisibility(View.GONE);
            }
        }

        // Direct Review Answers action on submitted quizzes
        if (holder.reviewBtn != null) {
            if (isQuiz && a.isReviewAllowed() && a.getLatestAttemptId() != null && !a.getLatestAttemptId().isEmpty()) {
                holder.reviewBtn.setVisibility(View.VISIBLE);
                holder.reviewBtn.setOnClickListener(v -> {
                    Context ctx = v.getContext();
                    Intent intent = new Intent(ctx, StudentQuizReviewActivity.class);
                    intent.putExtra(StudentQuizReviewActivity.EXTRA_ATTEMPT_ID, a.getLatestAttemptId());
                    intent.putExtra(StudentQuizReviewActivity.EXTRA_QUIZ_TITLE, a.getTitle());
                    String cInfo = holder.courseBadgeText != null && holder.courseBadgeText.getVisibility() == View.VISIBLE
                            ? holder.courseBadgeText.getText().toString() : "";
                    intent.putExtra(StudentQuizReviewActivity.EXTRA_COURSE_INFO, cInfo);
                    ctx.startActivity(intent);
                });
            } else {
                holder.reviewBtn.setVisibility(View.GONE);
            }
        }

        if ("quiz".equalsIgnoreCase(a.getType())) {
            holder.iconView.setImageResource(R.drawable.ic_analytics);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFBEB")));
            holder.iconView.setColorFilter(Color.parseColor("#F59E0B"));
        } else {
            holder.iconView.setImageResource(R.drawable.ic_assignment);
            holder.iconView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1EDFF")));
            holder.iconView.setColorFilter(Color.parseColor("#5B3DF5"));
        }

        if (holder.actionHintText != null) {
            String hint = a.getActionLabel();
            if (hint == null || hint.isEmpty()) {
                if ("quiz".equalsIgnoreCase(a.getType())) {
                    hint = a.isInProgress() ? "Continue ›" : (a.isSubmitted() ? "View Result ›" : "Open Quiz ›");
                } else {
                    hint = a.isGraded() ? "View Grade ›" : (a.isSubmitted() ? "View Submission ›" : "Submit ›");
                }
            } else {
                hint = hint + " ›";
            }
            holder.actionHintText.setVisibility(View.VISIBLE);
            holder.actionHintText.setText(hint);
        }

        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            if ("quiz".equalsIgnoreCase(a.getType())) {
                Intent intent = new Intent(ctx, TakeQuizActivity.class);
                intent.putExtra("quiz_id", a.getId());
                ctx.startActivity(intent);
            } else {
                Intent intent = new Intent(ctx, SubmitAssignmentActivity.class);
                intent.putExtra("assignment_id", a.getId());
                intent.putExtra("assessment_type", a.getAssessmentApiType() != null && !a.getAssessmentApiType().isEmpty()
                        ? a.getAssessmentApiType()
                        : com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.normalizeAssessmentType(a.getType()));
                ctx.startActivity(intent);
            }
        });
    }

    private int resolveStatusColor(Assessment a) {
        if (a.isPartiallyGraded()) {
            return Color.parseColor("#2563EB");
        }
        if (a.isPendingGrading()) {
            return Color.parseColor("#D97706");
        }
        if (a.isGraded()) {
            return Color.parseColor("#10B981");
        }
        String reopen = a.getReopenStatus() != null ? a.getReopenStatus() : "";
        if ("Reopened".equalsIgnoreCase(reopen)
                || "Extended".equalsIgnoreCase(reopen)
                || "Expired".equalsIgnoreCase(reopen)
                || "Late Submitted".equalsIgnoreCase(reopen)) {
            return Color.parseColor(com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.resolveBadgeColorHex(reopen));
        }
        String st = a.getStatusLabel() != null ? a.getStatusLabel().toLowerCase(Locale.US) : "";
        if (st.contains("partially")) {
            return Color.parseColor("#2563EB");
        }
        if (st.contains("pending")) {
            return Color.parseColor("#D97706");
        }
        if (st.contains("graded") || st.contains("completed")) {
            return Color.parseColor("#10B981");
        }
        if (a.isInProgress() || st.contains("progress")) {
            return Color.parseColor(com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.resolveBadgeColorHex("In Progress"));
        }
        if (a.isSubmitted() || st.contains("submitted")) {
            return Color.parseColor(com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.resolveBadgeColorHex("Submitted"));
        }
        if (!reopen.isEmpty()) {
            return Color.parseColor(com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.resolveBadgeColorHex(reopen));
        }
        return Color.parseColor(com.social7.studentlearnanalytics.utils.ReopenPermissionHelper.resolveBadgeColorHex(a.getStatusLabel()));
    }

    @Override
    public int getItemCount() {
        return assessmentList != null ? assessmentList.size() : 0;
    }

    public static class AssessmentViewHolder extends RecyclerView.ViewHolder {
        View indicatorView;
        ImageView iconView;
        TextView courseBadgeText, titleText, dateText, provisionalNoteText, scoreText, gradingStatusText, reviewBtn, actionHintText;

        public AssessmentViewHolder(@NonNull View itemView) {
            super(itemView);
            indicatorView = itemView.findViewById(R.id.indicator_view);
            iconView = itemView.findViewById(R.id.assessment_icon);
            courseBadgeText = itemView.findViewById(R.id.assessment_course_badge);
            titleText = itemView.findViewById(R.id.assessment_title);
            dateText = itemView.findViewById(R.id.assessment_date);
            provisionalNoteText = itemView.findViewById(R.id.assessment_provisional_note);
            scoreText = itemView.findViewById(R.id.assessment_score);
            gradingStatusText = itemView.findViewById(R.id.assessment_grading_status);
            reviewBtn = itemView.findViewById(R.id.assessment_review_btn);
            actionHintText = itemView.findViewById(R.id.assessment_action_hint);
        }
    }
}
