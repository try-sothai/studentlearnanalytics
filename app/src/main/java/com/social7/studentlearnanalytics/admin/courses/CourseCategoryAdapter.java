package com.social7.studentlearnanalytics.admin.courses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseCategoryAdapter extends RecyclerView.Adapter<CourseCategoryAdapter.ViewHolder> {

    public static class CategoryItem {
        public String id;
        public String name;
        public String code;
        public String description;
        public String status;
        public int courseCount;
        public int classCount;
        public int studentCount;
        public int teacherCount;
        public int programCount;
        public int offeringCount;
        public int departmentId = -1;
        public String departmentName = "";
        public String departmentCode = "";
        public int durationYears = 4;

        public CategoryItem() {
            this.id = "";
            this.name = "";
            this.code = "";
            this.description = "";
            this.status = "active";
        }

        public CategoryItem(String id, String name, String code, String description, String status,
                            int courseCount, int classCount, int studentCount, int teacherCount, int programCount) {
            this.id = id != null ? id : "";
            this.name = name != null ? name : "";
            this.code = code != null ? code : "";
            this.description = description != null ? description : "";
            this.status = status != null && !status.trim().isEmpty() ? status.trim().toLowerCase(Locale.ROOT) : "active";
            this.courseCount = courseCount;
            this.classCount = classCount;
            this.studentCount = studentCount;
            this.teacherCount = teacherCount;
            this.programCount = programCount;
        }

        public static CategoryItem fromJson(JsonObject obj) {
            if (obj == null) return new CategoryItem();
            String id = optString(obj, "id", "");
            String name = optString(obj, "name", "");
            String code = optString(obj, "code", "");
            String description = optString(obj, "description", "");
            String status = optString(obj, "status", "active");
            int coursesCount = optInt(obj, "courses_count", 0);
            int classesCount = optInt(obj, "classes_count", 0);
            int studentsCount = optInt(obj, "students_count", 0);
            int teachersCount = optInt(obj, "teachers_count", 0);
            int programsCount = optInt(obj, "programs_count", 0);

            CategoryItem item = new CategoryItem(
                    id, name, code, description, status,
                    coursesCount, classesCount, studentsCount, teachersCount, programsCount
            );
            item.offeringCount = optInt(obj, "offerings_count", 0);
            item.departmentId = optInt(obj, "department_id", optInt(obj, "faculty_id", -1));
            item.departmentName = optString(obj, "department_name", optString(obj, "faculty_name", ""));
            item.departmentCode = optString(obj, "department_code", optString(obj, "faculty_code", ""));
            item.durationYears = optInt(obj, "duration_years", 4);
            return item;
        }

        public boolean isActive() {
            return !"inactive".equalsIgnoreCase(status);
        }

        public String getDisplayStatus() {
            return isActive() ? "Active" : "Inactive";
        }

        public boolean hasLinkedRecords() {
            return courseCount > 0 || classCount > 0 || studentCount > 0 || teacherCount > 0 || programCount > 0 || offeringCount > 0;
        }

        public String buildDependencySummary() {
            List<String> parts = new ArrayList<>();
            if (courseCount > 0) parts.add(courseCount + (courseCount == 1 ? " course" : " courses"));
            if (programCount > 0) parts.add(programCount + (programCount == 1 ? " major" : " majors"));
            if (offeringCount > 0) parts.add(offeringCount + (offeringCount == 1 ? " offering" : " offerings"));
            if (classCount > 0) parts.add(classCount + (classCount == 1 ? " class" : " classes"));
            if (teacherCount > 0) parts.add(teacherCount + (teacherCount == 1 ? " teacher" : " teachers"));
            if (studentCount > 0) parts.add(studentCount + (studentCount == 1 ? " student" : " students"));
            return String.join(", ", parts);
        }

        public boolean matchesQuery(String query) {
            if (query == null || query.trim().isEmpty()) return true;
            String needle = query.trim().toLowerCase(Locale.ROOT);
            return (name != null && name.toLowerCase(Locale.ROOT).contains(needle))
                    || (code != null && code.toLowerCase(Locale.ROOT).contains(needle))
                    || (description != null && description.toLowerCase(Locale.ROOT).contains(needle))
                    || (departmentName != null && departmentName.toLowerCase(Locale.ROOT).contains(needle))
                    || (departmentCode != null && departmentCode.toLowerCase(Locale.ROOT).contains(needle))
                    || getDisplayStatus().toLowerCase(Locale.ROOT).contains(needle);
        }

        private static String optString(JsonObject obj, String key, String fallback) {
            if (!obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) return fallback;
            try {
                return obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : fallback;
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private static int optInt(JsonObject obj, String key, int fallback) {
            if (!obj.has(key) || obj.get(key) == null || obj.get(key).isJsonNull()) return fallback;
            try {
                return obj.get(key).isJsonPrimitive() ? obj.get(key).getAsInt() : fallback;
            } catch (Exception ignored) {
                return fallback;
            }
        }
    }

    public interface OnCategoryActionListener {
        void onViewCategory(CategoryItem item);
        void onEditCategory(CategoryItem item);
        void onToggleStatus(CategoryItem item);
        void onDeleteCategory(CategoryItem item);
    }

    private final Context context;
    private final List<CategoryItem> originalList;
    private final List<CategoryItem> filteredList;
    private final OnCategoryActionListener listener;
    private String lastQuery = "";

    public CourseCategoryAdapter(Context context, List<CategoryItem> list, OnCategoryActionListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>();
        if (list != null) {
            this.originalList.addAll(list);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void updateData(List<CategoryItem> newList) {
        originalList.clear();
        if (newList != null) {
            originalList.addAll(newList);
        }
        filter(lastQuery);
    }

    public int getFilteredCount() {
        return filteredList.size();
    }

    public int getTotalCount() {
        return originalList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_course_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        lastQuery = query != null ? query : "";
        filteredList.clear();
        for (CategoryItem item : originalList) {
            if (item.matchesQuery(lastQuery)) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryCode;
        private final TextView tvCategoryStatus;
        private final TextView tvDepartmentName;
        private final TextView tvCategoryDescription;
        private final TextView tvCourseCount;
        private final TextView tvMeta;
        private final MaterialButton btnToggleStatus;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryCode = itemView.findViewById(R.id.tvCollegeName);
            tvCategoryStatus = itemView.findViewById(R.id.tvCategoryStatus);
            tvDepartmentName = itemView.findViewById(R.id.tvDepartmentName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvCourseCount = itemView.findViewById(R.id.tvCourseCount);
            tvMeta = itemView.findViewById(R.id.tvDepartmentHead);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleCategoryStatus);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }

        public void bind(CategoryItem item, OnCategoryActionListener listener) {
            Context ctx = itemView.getContext();
            boolean active = item.isActive();

            if (tvCategoryCode != null) {
                String code = item.code != null && !item.code.trim().isEmpty() ? item.code : "CAT";
                tvCategoryCode.setText(code);
            }

            if (tvCategoryStatus != null) {
                tvCategoryStatus.setText(item.getDisplayStatus());
                tvCategoryStatus.setTextColor(ContextCompat.getColor(
                        ctx,
                        active ? R.color.admin_active_green : R.color.admin_inactive_red
                ));
            }

            if (tvDepartmentName != null) {
                tvDepartmentName.setText(item.name);
            }

            if (tvCategoryDescription != null) {
                if (item.description != null && !item.description.trim().isEmpty()) {
                    tvCategoryDescription.setVisibility(View.VISIBLE);
                    tvCategoryDescription.setText(item.description.trim());
                } else {
                    tvCategoryDescription.setVisibility(View.GONE);
                }
            }

            if (tvCourseCount != null) {
                tvCourseCount.setText(item.courseCount + (item.courseCount == 1 ? " Course" : " Courses"));
            }

            if (tvMeta != null) {
                String summary = item.buildDependencySummary();
                StringBuilder meta = new StringBuilder();
                if (item.departmentName != null && !item.departmentName.trim().isEmpty()) {
                    meta.append("Faculty: ").append(item.departmentName.trim());
                    if (!summary.isEmpty()) meta.append(" • ");
                }
                if (!summary.isEmpty()) {
                    meta.append("Linked: ").append(summary);
                } else if (meta.length() == 0) {
                    meta.append("No linked records yet");
                }
                tvMeta.setText(meta.toString());
            }

            if (btnToggleStatus != null) {
                btnToggleStatus.setText(active ? "Deactivate" : "Activate");
                btnToggleStatus.setTextColor(ContextCompat.getColor(
                        ctx,
                        active ? R.color.admin_text_secondary : R.color.admin_active_green
                ));
                btnToggleStatus.setOnClickListener(v -> {
                    if (listener != null) listener.onToggleStatus(item);
                });
            }

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEditCategory(item);
                });
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteCategory(item);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewCategory(item);
            });
        }
    }
}
