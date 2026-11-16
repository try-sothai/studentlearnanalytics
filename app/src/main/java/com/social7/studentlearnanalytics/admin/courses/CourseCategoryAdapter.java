package com.social7.studentlearnanalytics.admin.courses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;

import java.util.ArrayList;
import java.util.List;

public class CourseCategoryAdapter extends RecyclerView.Adapter<CourseCategoryAdapter.ViewHolder> {

    public static class CategoryItem {
        public String collegeName;
        public String departmentName;
        public int courseCount;
        public String head;

        public CategoryItem(String collegeName, String departmentName, int courseCount, String head) {
            this.collegeName = collegeName;
            this.departmentName = departmentName;
            this.courseCount = courseCount;
            this.head = head;
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem item);
    }

    private final Context context;
    private final List<CategoryItem> originalList;
    private final List<CategoryItem> filteredList;
    private final OnCategoryClickListener listener;

    public CourseCategoryAdapter(Context context, List<CategoryItem> list, OnCategoryClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
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
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (CategoryItem item : originalList) {
            if (text.isEmpty() ||
                (item.collegeName != null && item.collegeName.toLowerCase().contains(text)) ||
                (item.departmentName != null && item.departmentName.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCollegeName, tvDepartmentName, tvCourseCount, tvHead;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollegeName = itemView.findViewById(R.id.tvCollegeName);
            tvDepartmentName = itemView.findViewById(R.id.tvDepartmentName);
            tvCourseCount = itemView.findViewById(R.id.tvCourseCount);
            tvHead = itemView.findViewById(R.id.tvDepartmentHead);
        }

        public void bind(CategoryItem item, OnCategoryClickListener listener) {
            if (tvCollegeName != null) tvCollegeName.setText(item.collegeName);
            if (tvDepartmentName != null) tvDepartmentName.setText(item.departmentName);
            if (tvCourseCount != null) tvCourseCount.setText(item.courseCount + " Courses");
            if (tvHead != null) tvHead.setText(itemView.getContext().getString(R.string.head_prefix) + item.head);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCategoryClick(item);
            });
        }
    }
}
