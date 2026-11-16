package com.social7.studentlearnanalytics.teacher.student;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }

    private final Context context;
    private final List<Student> originalList;
    private List<Student> filteredList;
    private final OnStudentClickListener listener;
    private boolean showPredicted = false;

    public StudentListAdapter(Context context, List<Student> studentList, OnStudentClickListener listener) {
        this.context = context;
        this.originalList = studentList != null ? studentList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void setStudents(List<Student> newStudents) {
        this.originalList.clear();
        if (newStudents != null) {
            this.originalList.addAll(newStudents);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    public void setShowPredicted(boolean showPredicted) {
        this.showPredicted = showPredicted;
        notifyDataSetChanged();
    }

    public void filter(String query, String category) {
        filteredList.clear();
        for (Student student : originalList) {
            boolean matchesQuery = query == null || query.trim().isEmpty()
                    || student.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || student.getId().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || student.getProgram().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

            boolean matchesCategory = true;
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
                String risk = student.getRiskLevel();
                if (category.equalsIgnoreCase("At Risk")) {
                    matchesCategory = "At Risk".equalsIgnoreCase(risk) || "High".equalsIgnoreCase(risk);
                } else if (category.equalsIgnoreCase("Good")) {
                    matchesCategory = "Good".equalsIgnoreCase(risk) || "Low".equalsIgnoreCase(risk);
                } else if (category.equalsIgnoreCase("Need Attention") || category.equalsIgnoreCase("Needs Attention")) {
                    matchesCategory = "Need Attention".equalsIgnoreCase(risk) || "Needs Attention".equalsIgnoreCase(risk) || "Medium".equalsIgnoreCase(risk);
                }
            }

            if (matchesQuery && matchesCategory) {
                filteredList.add(student);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_row, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = filteredList.get(position);
        holder.bind(student, showPredicted, listener, context);
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarChar, tvStudentName, tvStudentId, tvStudentGpa, tvStudentSubtext, tvPredictedScore, tvStatusBadge, tvStudentProgram, tvStudentYear;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarChar = itemView.findViewById(R.id.tvAvatarChar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvStudentGpa = itemView.findViewById(R.id.tvStudentGpa);
            tvStudentSubtext = itemView.findViewById(R.id.tvStudentSubtext);
            tvPredictedScore = itemView.findViewById(R.id.tvPredictedScore);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvStudentProgram = itemView.findViewById(R.id.tvStudentProgram);
            tvStudentYear = itemView.findViewById(R.id.tvStudentYear);
        }

        public void bind(final Student student, boolean showPredicted, final OnStudentClickListener listener, Context context) {
            if (tvAvatarChar != null) {
                if (student.getName() != null && !student.getName().isEmpty()) {
                    tvAvatarChar.setText(String.valueOf(student.getName().charAt(0)).toUpperCase(Locale.ROOT));
                }

                try {
                    int textColor = Color.parseColor(student.getAvatarTextColor() != null ? student.getAvatarTextColor() : "#2563FF");
                    int bgColor = Color.parseColor(student.getAvatarBgColor() != null ? student.getAvatarBgColor() : "#EAF0FF");

                    tvAvatarChar.setTextColor(textColor);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.OVAL);
                    shape.setColor(bgColor);
                    tvAvatarChar.setBackground(shape);
                } catch (Exception e) {
                    tvAvatarChar.setTextColor(Color.parseColor("#2563FF"));
                }
            }

            if (tvStudentName != null) {
                tvStudentName.setText(student.getName());
            }

            if (tvStudentId != null) {
                tvStudentId.setText(student.getId());
            }

            if (tvStudentGpa != null) {
                tvStudentGpa.setText(String.format(Locale.US, "GPA: %.2f", student.getGpa()));
            }

            if (tvStudentSubtext != null) {
                tvStudentSubtext.setText(student.getId() + " • GPA: " + String.format(Locale.US, "%.2f", student.getGpa()));
            }

            if (tvPredictedScore != null) {
                if (showPredicted) {
                    tvPredictedScore.setVisibility(View.VISIBLE);
                    tvPredictedScore.setText(String.format(Locale.US, "Predicted: %.1f / 100", student.getPredictedScore()));
                } else {
                    tvPredictedScore.setVisibility(View.GONE);
                }
            }

            if (tvStudentProgram != null) {
                tvStudentProgram.setText(student.getProgram());
            }

            if (tvStudentYear != null) {
                tvStudentYear.setText(student.getYear());
            }

            if (tvStatusBadge != null) {
                String risk = student.getRiskLevel();
                if ("At Risk".equalsIgnoreCase(risk) || "High".equalsIgnoreCase(risk)) {
                    tvStatusBadge.setText(R.string.at_risk);
                    tvStatusBadge.setTextColor(Color.parseColor("#FF4055"));
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                } else if (itemView.getContext().getString(R.string.need_attention).equalsIgnoreCase(risk) || "Needs Attention".equalsIgnoreCase(risk) || "Medium".equalsIgnoreCase(risk)) {
                    tvStatusBadge.setText(itemView.getContext().getString(R.string.need_attention));
                    tvStatusBadge.setTextColor(Color.parseColor("#FF9B16"));
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_attention);
                } else {
                    tvStatusBadge.setText(R.string.status_good);
                    tvStatusBadge.setTextColor(Color.parseColor("#16B879"));
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_good);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentClick(student);
            });
        }
    }
}
