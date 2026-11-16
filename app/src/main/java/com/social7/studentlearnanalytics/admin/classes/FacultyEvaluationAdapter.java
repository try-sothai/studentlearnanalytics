package com.social7.studentlearnanalytics.admin.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class FacultyEvaluationAdapter extends RecyclerView.Adapter<FacultyEvaluationAdapter.FacultyViewHolder> {

    public static class FacultyItem {
        public String name;
        public String title;
        public String department;
        public String courses;
        public String classes;
        public String passRate;
        public String rating;
        public String status;
        public int id;
        public String facultyId;

        public FacultyItem(String name, String title, String department, String courses, String classes, String passRate, String rating, String status) {
            this.name = name;
            this.title = title;
            this.department = department;
            this.courses = courses;
            this.classes = classes;
            this.passRate = passRate;
            this.rating = rating;
            this.status = status;
        }
    }

    public interface OnFacultyClickListener {
        void onFacultyClick(FacultyItem item);
    }

    private final Context context;
    private final List<FacultyItem> originalList;
    private final List<FacultyItem> filteredList;
    private final OnFacultyClickListener listener;

    public FacultyEvaluationAdapter(Context context, List<FacultyItem> list, OnFacultyClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_faculty_evaluation, parent, false);
        return new FacultyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacultyViewHolder holder, int position) {
        FacultyItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    /** Replaces the list with a freshly loaded server page (search is done server-side). */
    public void setItems(List<FacultyItem> items) {
        originalList.clear();
        originalList.addAll(items);
        filteredList.clear();
        filteredList.addAll(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (FacultyItem item : originalList) {
            if (text.isEmpty() ||
                item.name.toLowerCase().contains(text) ||
                item.department.toLowerCase().contains(text) ||
                item.courses.toLowerCase().contains(text) ||
                item.classes.toLowerCase().contains(text)) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar, tvName, tvDept, tvStatus, tvAssignments, tvPerformance;

        public FacultyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvFacultyAvatar);
            tvName = itemView.findViewById(R.id.tvFacultyName);
            tvDept = itemView.findViewById(R.id.tvFacultyDept);
            tvStatus = itemView.findViewById(R.id.tvEvaluationStatus);
            tvAssignments = itemView.findViewById(R.id.tvFacultyAssignments);
            tvPerformance = itemView.findViewById(R.id.tvFacultyPerformance);
        }

        public void bind(FacultyItem item, OnFacultyClickListener listener) {
            if (tvAvatar != null) {
                String initial = item.name != null && !item.name.isEmpty() ? item.name.substring(0, 1).toUpperCase() : "T";
                tvAvatar.setText(initial);
            }
            if (tvName != null) tvName.setText(item.name);
            if (tvDept != null) tvDept.setText(item.department + " • " + item.title);
            if (tvStatus != null) {
                tvStatus.setText(item.status);
                if ("Completed".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Pending Review".equalsIgnoreCase(item.status)) {
                    tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#EF4444"));
                }
            }
            if (tvAssignments != null) {
                tvAssignments.setText(tvAssignments.getContext().getString(R.string.courses) + item.courses + " • Classes: " + item.classes);
            }
            if (tvPerformance != null) {
                tvPerformance.setText(tvPerformance.getContext().getString(R.string.class_pass_rate) + item.passRate + " • Student Rating: " + item.rating);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFacultyClick(item);
            });
        }
    }
}
