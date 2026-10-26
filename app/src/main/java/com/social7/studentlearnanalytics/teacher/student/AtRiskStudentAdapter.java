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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AtRiskStudentAdapter extends RecyclerView.Adapter<AtRiskStudentAdapter.ViewHolder> {

    public interface OnAtRiskStudentClickListener {
        void onStudentClick(AtRiskStudent student);
    }

    public static class AtRiskStudent implements Serializable {
        public String id;
        public String name;
        public String program;
        public String year;
        public String riskBadge; // "High Risk" or "At Risk"
        public String gpa;
        public String avgScore;
        public String absence;
        public String reason1;
        public String reason2;
        public String reason3;
        public String avatarBgColor;
        public String avatarTextColor;
        public List<String> categories;

        public AtRiskStudent(String id, String name, String program, String year, String riskBadge,
                              String gpa, String avgScore, String absence,
                              String reason1, String reason2, String reason3,
                              String avatarBgColor, String avatarTextColor, List<String> categories) {
            this.id = id;
            this.name = name;
            this.program = program;
            this.year = year;
            this.riskBadge = riskBadge;
            this.gpa = gpa;
            this.avgScore = avgScore;
            this.absence = absence;
            this.reason1 = reason1;
            this.reason2 = reason2;
            this.reason3 = reason3;
            this.avatarBgColor = avatarBgColor;
            this.avatarTextColor = avatarTextColor;
            this.categories = categories != null ? categories : new ArrayList<>();
        }
    }

    private final Context context;
    private final List<AtRiskStudent> originalList;
    private List<AtRiskStudent> filteredList;
    private final OnAtRiskStudentClickListener listener;

    public AtRiskStudentAdapter(Context context, List<AtRiskStudent> list, OnAtRiskStudentClickListener listener) {
        this.context = context;
        this.originalList = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void setStudents(List<AtRiskStudent> newStudents) {
        this.originalList.clear();
        if (newStudents != null) {
            this.originalList.addAll(newStudents);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    public void filter(String query, String category) {
        filteredList.clear();
        for (AtRiskStudent student : originalList) {
            boolean matchesQuery = query == null || query.trim().isEmpty()
                    || student.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || student.id.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || student.program.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));

            boolean matchesCategory = true;
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
                matchesCategory = false;
                for (String cat : student.categories) {
                    if (cat.equalsIgnoreCase(category)) {
                        matchesCategory = true;
                        break;
                    }
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_at_risk_student_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AtRiskStudent student = filteredList.get(position);
        holder.bind(student, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarChar, tvStudentName, tvStudentId, tvProgram, tvYear;
        TextView tvRiskBadge, tvMetricGpa, tvMetricAvgScore, tvMetricAbsence;
        TextView tvReason1, tvReason2, tvReason3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarChar = itemView.findViewById(R.id.tvAvatarChar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvProgram = itemView.findViewById(R.id.tvProgram);
            tvYear = itemView.findViewById(R.id.tvYear);

            tvRiskBadge = itemView.findViewById(R.id.tvRiskBadge);
            tvMetricGpa = itemView.findViewById(R.id.tvMetricGpa);
            tvMetricAvgScore = itemView.findViewById(R.id.tvMetricAvgScore);
            tvMetricAbsence = itemView.findViewById(R.id.tvMetricAbsence);

            tvReason1 = itemView.findViewById(R.id.tvReason1);
            tvReason2 = itemView.findViewById(R.id.tvReason2);
            tvReason3 = itemView.findViewById(R.id.tvReason3);
        }

        public void bind(final AtRiskStudent student, final OnAtRiskStudentClickListener listener) {
            if (tvAvatarChar != null && student.name != null && !student.name.isEmpty()) {
                tvAvatarChar.setText(String.valueOf(student.name.charAt(0)).toUpperCase(Locale.ROOT));
                try {
                    int textColor = Color.parseColor(student.avatarTextColor != null ? student.avatarTextColor : "#FF4055");
                    int bgColor = Color.parseColor(student.avatarBgColor != null ? student.avatarBgColor : "#FFEBEF");
                    tvAvatarChar.setTextColor(textColor);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.OVAL);
                    shape.setColor(bgColor);
                    tvAvatarChar.setBackground(shape);
                } catch (Exception ignored) {}
            }

            if (tvStudentName != null) tvStudentName.setText(student.name);
            if (tvStudentId != null) tvStudentId.setText(student.id);
            if (tvProgram != null) tvProgram.setText(student.program);
            if (tvYear != null) tvYear.setText(student.year);

            if (tvRiskBadge != null) {
                tvRiskBadge.setText(student.riskBadge);
                if ("High Risk".equalsIgnoreCase(student.riskBadge)) {
                    tvRiskBadge.setTextColor(Color.parseColor("#FF4055"));
                    tvRiskBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                } else {
                    tvRiskBadge.setTextColor(Color.parseColor("#FF4055"));
                    tvRiskBadge.setBackgroundResource(R.drawable.bg_status_at_risk);
                }
            }

            if (tvMetricGpa != null) tvMetricGpa.setText(student.gpa);
            if (tvMetricAvgScore != null) tvMetricAvgScore.setText(student.avgScore);
            if (tvMetricAbsence != null) tvMetricAbsence.setText(student.absence);

            if (tvReason1 != null) tvReason1.setText(student.reason1);
            if (tvReason2 != null) tvReason2.setText(student.reason2);
            if (tvReason3 != null) tvReason3.setText(student.reason3);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentClick(student);
            });
        }
    }
}
