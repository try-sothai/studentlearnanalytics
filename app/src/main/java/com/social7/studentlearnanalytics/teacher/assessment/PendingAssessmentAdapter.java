package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PendingAssessmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public interface OnPendingAssessmentClickListener {
        void onEnterScoresClick(PendingItem item);
    }

    public static class PendingItem implements Serializable {
        public boolean isHeader;
        public String courseCode;
        public String courseName;
        public String department;
        public int pendingCount;
        public int iconBgRes;
        public boolean isExpanded = true;

        // Item fields
        public String id;
        public int rawId = 0;
        public int submissionId = 0;
        public String title;
        public String type; // "Assignment", "Quiz", "Exam", "Project"
        public int maxPoints;
        public String dueDate;
        public String dueStatus; // "Overdue", "Due in 2 days", "Due in 12 days", etc.
        public boolean isOverdue;
        public int completedCount;
        public int totalStudents;

        // Constructor for Header
        public PendingItem(String courseCode, String courseName, String department, int pendingCount, int iconBgRes) {
            this.isHeader = true;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.department = department;
            this.pendingCount = pendingCount;
            this.iconBgRes = iconBgRes;
            this.isExpanded = true;
        }

        // Constructor for Item
        public PendingItem(String id, String courseCode, String title, String type, int maxPoints,
                           String dueDate, String dueStatus, boolean isOverdue,
                           int completedCount, int totalStudents) {
            this.isHeader = false;
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.type = type;
            this.maxPoints = maxPoints;
            this.dueDate = dueDate;
            this.dueStatus = dueStatus;
            this.isOverdue = isOverdue;
            this.completedCount = completedCount;
            this.totalStudents = totalStudents;
        }

        // Extended Constructor for Item with integer IDs
        public PendingItem(String id, int rawId, int submissionId, String courseCode, String title, String type, int maxPoints,
                           String dueDate, String dueStatus, boolean isOverdue,
                           int completedCount, int totalStudents) {
            this.isHeader = false;
            this.id = id;
            this.rawId = rawId;
            this.submissionId = submissionId;
            this.courseCode = courseCode;
            this.title = title;
            this.type = type;
            this.maxPoints = maxPoints;
            this.dueDate = dueDate;
            this.dueStatus = dueStatus;
            this.isOverdue = isOverdue;
            this.completedCount = completedCount;
            this.totalStudents = totalStudents;
        }
    }

    private final Context context;
    private final List<PendingItem> allItems;
    private List<PendingItem> visibleItems;
    private final OnPendingAssessmentClickListener listener;

    public PendingAssessmentAdapter(Context context, List<PendingItem> items, OnPendingAssessmentClickListener listener) {
        this.context = context;
        this.allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.visibleItems = new ArrayList<>();
        this.listener = listener;
        rebuildVisibleList("", "All");
    }

    public void filter(String query, String category) {
        rebuildVisibleList(query, category);
        notifyDataSetChanged();
    }

    private void rebuildVisibleList(String query, String category) {
        visibleItems.clear();
        PendingItem currentHeader = null;
        List<PendingItem> currentGroupItems = new ArrayList<>();

        for (PendingItem item : allItems) {
            if (item.isHeader) {
                if (currentHeader != null && !currentGroupItems.isEmpty()) {
                    visibleItems.add(currentHeader);
                    if (currentHeader.isExpanded) {
                        visibleItems.addAll(currentGroupItems);
                    }
                }
                currentHeader = item;
                currentGroupItems = new ArrayList<>();
            } else {
                boolean matchesQuery = query == null || query.trim().isEmpty()
                        || item.title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                        || item.courseCode.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                        || item.type.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

                boolean matchesCategory = true;
                if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
                    if (category.equalsIgnoreCase("Assignments")) {
                        matchesCategory = item.type.equalsIgnoreCase("Assignment");
                    } else if (category.equalsIgnoreCase("Quizzes")) {
                        matchesCategory = item.type.equalsIgnoreCase("Quiz");
                    } else if (category.equalsIgnoreCase("Exams")) {
                        matchesCategory = item.type.equalsIgnoreCase("Exam") || item.type.equalsIgnoreCase("Project");
                    } else if (category.equalsIgnoreCase("Overdue")) {
                        matchesCategory = item.isOverdue;
                    }
                }

                if (matchesQuery && matchesCategory) {
                    currentGroupItems.add(item);
                }
            }
        }

        if (currentHeader != null && !currentGroupItems.isEmpty()) {
            visibleItems.add(currentHeader);
            if (currentHeader.isExpanded) {
                visibleItems.addAll(currentGroupItems);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return visibleItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_pending_course_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_pending_assessment_row, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PendingItem item = visibleItems.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item, () -> {
                item.isExpanded = !item.isExpanded;
                filter("", "All");
            });
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconHeaderContainer;
        ImageView imgHeaderCourseIcon, imgHeaderChevron;
        TextView tvHeaderCourseCodeName, tvHeaderDepartment, tvHeaderPendingBadge;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            iconHeaderContainer = itemView.findViewById(R.id.iconHeaderContainer);
            imgHeaderCourseIcon = itemView.findViewById(R.id.imgHeaderCourseIcon);
            imgHeaderChevron = itemView.findViewById(R.id.imgHeaderChevron);
            tvHeaderCourseCodeName = itemView.findViewById(R.id.tvHeaderCourseCodeName);
            tvHeaderDepartment = itemView.findViewById(R.id.tvHeaderDepartment);
            tvHeaderPendingBadge = itemView.findViewById(R.id.tvHeaderPendingBadge);
        }

        public void bind(final PendingItem header, final Runnable onToggleExpand) {
            if (tvHeaderCourseCodeName != null) {
                tvHeaderCourseCodeName.setText(header.courseCode + " - " + header.courseName);
            }
            if (tvHeaderDepartment != null) {
                tvHeaderDepartment.setText(header.department);
            }
            if (tvHeaderPendingBadge != null) {
                tvHeaderPendingBadge.setText(header.pendingCount + " Pending Scores");
            }
            if (iconHeaderContainer != null && header.iconBgRes != 0) {
                iconHeaderContainer.setBackgroundResource(header.iconBgRes);
            }
            if (imgHeaderChevron != null) {
                imgHeaderChevron.setRotation(header.isExpanded ? 0f : 180f);
            }

            itemView.setOnClickListener(v -> {
                if (onToggleExpand != null) onToggleExpand.run();
            });
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        FrameLayout typeIconContainer;
        ImageView imgTypeIcon;
        TextView tvAssessmentTitle, tvTypePoints, tvDueDate, tvDueStatusBadge;
        TextView tvProgressRatio, tvRemainingCount;
        ProgressBar progressBarScoreEntry;
        MaterialButton btnEnterScores;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIconContainer = itemView.findViewById(R.id.typeIconContainer);
            imgTypeIcon = itemView.findViewById(R.id.imgTypeIcon);
            tvAssessmentTitle = itemView.findViewById(R.id.tvAssessmentTitle);
            tvTypePoints = itemView.findViewById(R.id.tvTypePoints);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvDueStatusBadge = itemView.findViewById(R.id.tvDueStatusBadge);

            tvProgressRatio = itemView.findViewById(R.id.tvProgressRatio);
            tvRemainingCount = itemView.findViewById(R.id.tvRemainingCount);
            progressBarScoreEntry = itemView.findViewById(R.id.progressBarScoreEntry);
            btnEnterScores = itemView.findViewById(R.id.btnEnterScores);
        }

        public void bind(final PendingItem item, final OnPendingAssessmentClickListener listener) {
            if (tvAssessmentTitle != null) tvAssessmentTitle.setText(item.title);
            if (tvTypePoints != null) tvTypePoints.setText(item.type + " • " + item.maxPoints + " points");
            if (tvDueDate != null) tvDueDate.setText(item.dueDate);

            if (tvDueStatusBadge != null) {
                tvDueStatusBadge.setText(item.dueStatus);
                if (item.isOverdue) {
                    tvDueStatusBadge.setTextColor(Color.parseColor("#FF4055"));
                    tvDueStatusBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                } else if (item.dueStatus.contains("2 days") || item.dueStatus.contains("5 days")) {
                    tvDueStatusBadge.setTextColor(Color.parseColor("#FF9B16"));
                    tvDueStatusBadge.setBackgroundResource(R.drawable.bg_status_attention);
                } else {
                    tvDueStatusBadge.setTextColor(Color.parseColor("#2563FF"));
                    tvDueStatusBadge.setBackgroundResource(R.drawable.bg_circle_soft_blue);
                }
            }

            if (tvProgressRatio != null) {
                tvProgressRatio.setText(item.completedCount + " / " + item.totalStudents);
            }

            int remaining = Math.max(0, item.totalStudents - item.completedCount);
            if (tvRemainingCount != null) {
                tvRemainingCount.setText(remaining + " remaining");
            }

            if (progressBarScoreEntry != null && item.totalStudents > 0) {
                int percent = (int) (((double) item.completedCount / item.totalStudents) * 100);
                progressBarScoreEntry.setProgress(percent);
            }

            if (typeIconContainer != null) {
                if ("Assignment".equalsIgnoreCase(item.type)) {
                    typeIconContainer.setBackgroundResource(R.drawable.bg_metric_red);
                    if (imgTypeIcon != null) {
                        imgTypeIcon.setImageResource(R.drawable.ic_assignment);
                        imgTypeIcon.setColorFilter(Color.parseColor("#FF4055"));
                    }
                } else if ("Quiz".equalsIgnoreCase(item.type)) {
                    typeIconContainer.setBackgroundResource(R.drawable.bg_metric_purple);
                    if (imgTypeIcon != null) {
                        imgTypeIcon.setImageResource(R.drawable.ic_pencil_edit);
                        imgTypeIcon.setColorFilter(Color.parseColor("#FF9B16"));
                    }
                } else {
                    typeIconContainer.setBackgroundResource(R.drawable.bg_metric_blue);
                    if (imgTypeIcon != null) {
                        imgTypeIcon.setImageResource(R.drawable.ic_chart_bar);
                        imgTypeIcon.setColorFilter(Color.parseColor("#2563FF"));
                    }
                }
            }

            View.OnClickListener clickListener = v -> {
                if (listener != null) listener.onEnterScoresClick(item);
            };

            if (btnEnterScores != null) btnEnterScores.setOnClickListener(clickListener);
            itemView.setOnClickListener(clickListener);
        }
    }
}
