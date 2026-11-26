package com.social7.studentlearnanalytics.admin.teachers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Teacher;

import java.util.ArrayList;
import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    public interface OnTeacherActionListener {
        void onViewDetails(Teacher teacher);
        void onEditTeacher(Teacher teacher);
        void onToggleStatus(Teacher teacher);
        void onDeleteTeacher(Teacher teacher);
    }

    private final Context context;
    private final List<Teacher> teacherList;
    private final List<Teacher> filteredList;
    private final OnTeacherActionListener listener;

    private String currentStatusFilter = "ALL";
    private String currentDepartmentFilter = "All Faculties";

    public TeacherAdapter(Context context, List<Teacher> teacherList, OnTeacherActionListener listener) {
        this.context = context;
        this.teacherList = teacherList;
        this.filteredList = new ArrayList<>(teacherList);
        this.listener = listener;
    }

    public void filter(String query, String statusFilter, String departmentFilter) {
        if (statusFilter != null) {
            this.currentStatusFilter = statusFilter.toUpperCase();
        }
        if (departmentFilter != null) {
            this.currentDepartmentFilter = departmentFilter;
        }

        filteredList.clear();
        String lowerQuery = (query != null) ? query.toLowerCase().trim() : "";

        for (Teacher teacher : teacherList) {
            boolean matchesSearch = lowerQuery.isEmpty() ||
                    (teacher.getName() != null && teacher.getName().toLowerCase().contains(lowerQuery)) ||
                    (teacher.getId() != null && teacher.getId().toLowerCase().contains(lowerQuery)) ||
                    (teacher.getEmail() != null && teacher.getEmail().toLowerCase().contains(lowerQuery)) ||
                    (teacher.getDepartment() != null && teacher.getDepartment().toLowerCase().contains(lowerQuery));

            boolean matchesStatus = "ALL".equals(currentStatusFilter) ||
                    ("ACTIVE".equals(currentStatusFilter) && "Active".equalsIgnoreCase(teacher.getStatus())) ||
                    ("INACTIVE".equals(currentStatusFilter) && "Inactive".equalsIgnoreCase(teacher.getStatus()));

            boolean matchesDepartment = "All Faculties".equalsIgnoreCase(currentDepartmentFilter) ||
                    "All Departments".equalsIgnoreCase(currentDepartmentFilter) ||
                    (teacher.getDepartment() != null && teacher.getDepartment().equalsIgnoreCase(currentDepartmentFilter));

            if (matchesSearch && matchesStatus && matchesDepartment) {
                filteredList.add(teacher);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = filteredList.get(position);
        holder.bind(context, teacher, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeacherAvatar, tvTeacherId, tvTeacherName, tvTeacherDept, tvTeacherStatus;
        ImageView imgTeacherActionMenu;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeacherAvatar = itemView.findViewById(R.id.tvTeacherAvatar);
            tvTeacherId = itemView.findViewById(R.id.tvTeacherId);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvTeacherDept = itemView.findViewById(R.id.tvTeacherDept);
            tvTeacherStatus = itemView.findViewById(R.id.tvTeacherStatus);
            imgTeacherActionMenu = itemView.findViewById(R.id.imgTeacherActionMenu);
        }

        public void bind(final Context context, final Teacher teacher, final OnTeacherActionListener listener) {
            if (tvTeacherId != null) tvTeacherId.setText(teacher.getId());
            if (tvTeacherName != null) tvTeacherName.setText(teacher.getName());
            if (tvTeacherDept != null) tvTeacherDept.setText(teacher.getDepartment());

            if (tvTeacherAvatar != null) {
                String name = teacher.getName();
                String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "T";
                tvTeacherAvatar.setText(initial);
            }

            if (tvTeacherStatus != null) {
                boolean isActive = "Active".equalsIgnoreCase(teacher.getStatus());
                tvTeacherStatus.setText(isActive ? "Active" : "Inactive");
                tvTeacherStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
                tvTeacherStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(teacher);
            });

            if (imgTeacherActionMenu != null) {
                imgTeacherActionMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, imgTeacherActionMenu);
                    popup.getMenu().add("View Details");
                    popup.getMenu().add("Edit Teacher");
                    boolean isActive = "Active".equalsIgnoreCase(teacher.getStatus());
                    popup.getMenu().add(isActive ? "Deactivate Account" : "Activate Account");
                    popup.getMenu().add("Delete Teacher");

                    popup.setOnMenuItemClickListener(item -> {
                        String title = item.getTitle().toString();
                        if ("View Details".equals(title)) {
                            if (listener != null) listener.onViewDetails(teacher);
                            return true;
                        } else if ("Edit Teacher".equals(title)) {
                            if (listener != null) listener.onEditTeacher(teacher);
                            return true;
                        } else if ("Deactivate Account".equals(title) || "Activate Account".equals(title)) {
                            if (listener != null) listener.onToggleStatus(teacher);
                            return true;
                        } else if ("Delete Teacher".equals(title)) {
                            if (listener != null) listener.onDeleteTeacher(teacher);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }
    }
}
