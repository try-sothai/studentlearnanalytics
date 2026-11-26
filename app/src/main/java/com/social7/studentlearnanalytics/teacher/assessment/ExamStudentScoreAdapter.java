package com.social7.studentlearnanalytics.teacher.assessment;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamStudentScoreAdapter extends RecyclerView.Adapter<ExamStudentScoreAdapter.ViewHolder> {

    public static class StudentScoreItem {
        public final int studentId;
        public final String studentCode;
        public final String name;
        public final String email;
        public final String className;
        public boolean hasScore;
        public Double score;
        public Double percentage;
        public String grade;
        public String resultStatus;
        public String publicationStatus;
        public String remarks;
        public String updatedAt;

        // Draft state for inline batch manual score entry across large classes
        public String draftScoreInput;
        public boolean isDirty = false;
        public boolean hasValidationError = false;

        public StudentScoreItem(int studentId, String studentCode, String name, String email, String className,
                                boolean hasScore, Double score, String grade, String remarks, String updatedAt) {
            this.studentId = studentId;
            this.studentCode = studentCode;
            this.name = name;
            this.email = email;
            this.className = className;
            this.hasScore = hasScore;
            this.score = score;
            this.grade = grade;
            this.remarks = remarks;
            this.updatedAt = updatedAt;
            if (hasScore && score != null) {
                this.draftScoreInput = formatScoreNumber(score);
            } else {
                this.draftScoreInput = "";
            }
        }

        public static String formatScoreNumber(double val) {
            return (val == (long) val)
                    ? String.format(Locale.US, "%d", (long) val)
                    : String.format(Locale.US, "%.1f", val);
        }

        public static String calculateGrade(double val, double maxMarks) {
            if (maxMarks <= 0) return "N/A";
            double pct = (val / maxMarks) * 100.0;
            if (pct >= 90) return "A";
            if (pct >= 85) return "A-";
            if (pct >= 80) return "B+";
            if (pct >= 75) return "B";
            if (pct >= 70) return "B-";
            if (pct >= 65) return "C+";
            if (pct >= 60) return "C";
            if (pct >= 50) return "D";
            return "F";
        }

        public Double applyDraftInput(String input, double maxMarks) {
            String trimmed = input != null ? input.trim() : "";
            this.draftScoreInput = trimmed;

            if (trimmed.isEmpty()) {
                this.hasValidationError = this.hasScore;
                this.isDirty = this.hasScore;
                return null;
            }

            try {
                double parsed = Double.parseDouble(trimmed);
                if (parsed < 0 || parsed > maxMarks) {
                    this.hasValidationError = true;
                    this.isDirty = true;
                    return null;
                }
                this.hasValidationError = false;
                this.isDirty = !this.hasScore || this.score == null || Math.abs(parsed - this.score) > 0.0001;
                return parsed;
            } catch (NumberFormatException e) {
                this.hasValidationError = true;
                this.isDirty = true;
                return null;
            }
        }
    }

    public interface OnScoreActionListener {
        void onGradeStudent(StudentScoreItem item);
        default void onDraftScoresChanged(int modifiedCount, boolean hasInvalid) {}
    }

    private final Context context;
    private final List<StudentScoreItem> originalList;
    private final List<StudentScoreItem> filteredList;
    private final double maxScore;
    private final OnScoreActionListener listener;

    public ExamStudentScoreAdapter(Context context, List<StudentScoreItem> list, double maxScore, OnScoreActionListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.maxScore = maxScore > 0 ? maxScore : 100.0;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam_student_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentScoreItem item = filteredList.get(position);
        holder.bind(item, maxScore, listener, this::notifyDraftChanged);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase(Locale.US) : "";
        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (StudentScoreItem item : originalList) {
                if ((item.name != null && item.name.toLowerCase(Locale.US).contains(text))
                        || (item.studentCode != null && item.studentCode.toLowerCase(Locale.US).contains(text))
                        || (item.className != null && item.className.toLowerCase(Locale.US).contains(text))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateItem(StudentScoreItem updated) {
        for (int i = 0; i < originalList.size(); i++) {
            if (originalList.get(i).studentId == updated.studentId) {
                originalList.set(i, updated);
                break;
            }
        }
        for (int i = 0; i < filteredList.size(); i++) {
            if (filteredList.get(i).studentId == updated.studentId) {
                filteredList.set(i, updated);
                notifyItemChanged(i);
                break;
            }
        }
        notifyDraftChanged();
    }

    public List<StudentScoreItem> getPendingScoreSubmissions() {
        List<StudentScoreItem> list = new ArrayList<>();
        for (StudentScoreItem item : originalList) {
            if (item.isDirty && !item.hasValidationError && item.draftScoreInput != null && !item.draftScoreInput.isEmpty()) {
                list.add(item);
            }
        }
        return list;
    }

    public boolean hasInvalidScores() {
        for (StudentScoreItem item : originalList) {
            if (item.hasValidationError) return true;
        }
        return false;
    }

    private void notifyDraftChanged() {
        if (listener == null) return;
        int modified = 0;
        boolean invalid = false;
        for (StudentScoreItem item : originalList) {
            if (item.hasValidationError) invalid = true;
            if (item.isDirty && !item.hasValidationError && item.draftScoreInput != null && !item.draftScoreInput.isEmpty()) {
                modified++;
            }
        }
        listener.onDraftScoresChanged(modified, invalid);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentAvatar, tvStudentName, tvStudentCode;
        private final EditText etInlineScore;
        private final TextView tvInlineMaxMarks, tvInlineCalcPreview, tvGradeBadge, tvRemarks, tvGradingStatus;
        private final MaterialButton btnEditScore;
        private TextWatcher activeWatcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentAvatar = itemView.findViewById(R.id.tvStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            etInlineScore = itemView.findViewById(R.id.etInlineScore);
            tvInlineMaxMarks = itemView.findViewById(R.id.tvInlineMaxMarks);
            tvInlineCalcPreview = itemView.findViewById(R.id.tvInlineCalcPreview);
            tvGradeBadge = itemView.findViewById(R.id.tvGradeBadge);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            tvGradingStatus = itemView.findViewById(R.id.tvGradingStatus);
            btnEditScore = itemView.findViewById(R.id.btnEditScore);
        }

        void bind(StudentScoreItem item, double maxScore, OnScoreActionListener listener, Runnable onDraftChanged) {
            if (tvStudentName != null) tvStudentName.setText(item.name != null ? item.name : "Student");

            String initial = "S";
            if (item.name != null && !item.name.trim().isEmpty()) {
                initial = String.valueOf(item.name.trim().charAt(0)).toUpperCase(Locale.US);
            }
            if (tvStudentAvatar != null) tvStudentAvatar.setText(initial);

            StringBuilder subInfo = new StringBuilder();
            if (item.studentCode != null && !item.studentCode.isEmpty()) {
                subInfo.append("ID: ").append(item.studentCode);
            }
            if (item.className != null && !item.className.isEmpty()) {
                if (subInfo.length() > 0) subInfo.append(" • ");
                subInfo.append(item.className);
            }
            if (tvStudentCode != null) tvStudentCode.setText(subInfo.toString());

            String maxStr = StudentScoreItem.formatScoreNumber(maxScore);
            if (tvInlineMaxMarks != null) {
                tvInlineMaxMarks.setText("/ " + maxStr);
            }

            if (etInlineScore != null) {
                if (activeWatcher != null) {
                    etInlineScore.removeTextChangedListener(activeWatcher);
                }
                etInlineScore.setText(item.draftScoreInput != null ? item.draftScoreInput : "");
                renderRowStatus(item, maxScore);

                activeWatcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        item.applyDraftInput(s != null ? s.toString() : "", maxScore);
                        renderRowStatus(item, maxScore);
                        if (onDraftChanged != null) onDraftChanged.run();
                    }
                };
                etInlineScore.addTextChangedListener(activeWatcher);
            } else {
                renderRowStatus(item, maxScore);
            }

            if (tvRemarks != null) {
                if (item.remarks != null && !item.remarks.trim().isEmpty()) {
                    tvRemarks.setVisibility(View.VISIBLE);
                    tvRemarks.setText("Remarks: " + item.remarks.trim());
                } else {
                    tvRemarks.setVisibility(View.GONE);
                }
            }

            if (btnEditScore != null) {
                btnEditScore.setText("Remarks / Edit");
                btnEditScore.setOnClickListener(v -> {
                    if (listener != null) listener.onGradeStudent(item);
                });
            }
        }

        private void renderRowStatus(StudentScoreItem item, double maxScore) {
            String maxStr = StudentScoreItem.formatScoreNumber(maxScore);

            if (item.hasValidationError) {
                if (etInlineScore != null) {
                    etInlineScore.setError("0 – " + maxStr);
                }
                if (tvInlineCalcPreview != null) {
                    tvInlineCalcPreview.setText("Invalid score (must be 0 to " + maxStr + ")");
                    tvInlineCalcPreview.setTextColor(Color.parseColor("#EF4444"));
                }
                if (tvGradeBadge != null) {
                    tvGradeBadge.setText("Invalid");
                    tvGradeBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                    tvGradeBadge.setTextColor(Color.parseColor("#EF4444"));
                }
                if (tvGradingStatus != null) {
                    tvGradingStatus.setText("Check Score");
                    tvGradingStatus.setTextColor(Color.parseColor("#EF4444"));
                }
                return;
            }

            if (etInlineScore != null) {
                etInlineScore.setError(null);
            }

            Double activeVal = null;
            if (item.draftScoreInput != null && !item.draftScoreInput.isEmpty()) {
                try {
                    activeVal = Double.parseDouble(item.draftScoreInput);
                } catch (NumberFormatException ignored) {}
            }

            if (activeVal != null) {
                double pct = maxScore > 0 ? (activeVal / maxScore) * 100.0 : 0.0;
                String grade = StudentScoreItem.calculateGrade(activeVal, maxScore);
                String passFail = pct >= 50.0 ? "Pass" : "Fail";

                if (tvInlineCalcPreview != null) {
                    tvInlineCalcPreview.setText(String.format(Locale.US, "%.1f%% • Grade %s • %s", pct, grade, passFail));
                    tvInlineCalcPreview.setTextColor(pct >= 50.0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
                }
                if (tvGradeBadge != null) {
                    tvGradeBadge.setText("Grade " + grade);
                    tvGradeBadge.setBackgroundResource(pct >= 50.0 ? R.drawable.bg_stat_card_green : R.drawable.bg_status_at_risk);
                    tvGradeBadge.setTextColor(pct >= 50.0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
                }
                if (tvGradingStatus != null) {
                    if (item.isDirty) {
                        tvGradingStatus.setText("Unsaved Draft Change");
                        tvGradingStatus.setTextColor(Color.parseColor("#2563EB"));
                    } else {
                        tvGradingStatus.setText("Graded");
                        tvGradingStatus.setTextColor(Color.parseColor("#10B981"));
                    }
                }
            } else {
                if (tvInlineCalcPreview != null) {
                    tvInlineCalcPreview.setText("Pending manual paper score entry");
                    tvInlineCalcPreview.setTextColor(Color.parseColor("#64748B"));
                }
                if (tvGradeBadge != null) {
                    tvGradeBadge.setText("Pending");
                    tvGradeBadge.setBackgroundResource(R.drawable.bg_stat_card_purple);
                    tvGradeBadge.setTextColor(Color.parseColor("#9333EA"));
                }
                if (tvGradingStatus != null) {
                    tvGradingStatus.setText("Pending");
                    tvGradingStatus.setTextColor(Color.parseColor("#64748B"));
                }
            }
        }
    }
}
