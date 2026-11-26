package com.social7.studentlearnanalytics.student.academic;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;

public class JourneySemesterAdapter extends RecyclerView.Adapter<JourneySemesterAdapter.ViewHolder> {

    private final JsonArray data;

    public JourneySemesterAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey_semester, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();

        String name = (item.has("semester_name") && !item.get("semester_name").isJsonNull())
                ? item.get("semester_name").getAsString() : "Unknown Semester";
        String state = (item.has("state") && !item.get("state").isJsonNull())
                ? item.get("state").getAsString() : "Upcoming";

        holder.tvSemesterName.setText(name);
        holder.tvStatus.setText(state);

        Drawable bg = holder.tvStatus.getBackground();
        if (bg instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) bg.mutate();
            if ("Current".equalsIgnoreCase(state)) {
                gd.setColor(Color.parseColor("#3B82F6")); // Blue
            } else if ("Completed".equalsIgnoreCase(state)) {
                gd.setColor(Color.parseColor("#10B981")); // Green
            } else {
                gd.setColor(Color.parseColor("#9CA3AF")); // Gray
            }
        }

        holder.llCourses.removeAllViews();
        if (item.has("courses") && item.get("courses").isJsonArray()) {
            JsonArray courses = item.getAsJsonArray("courses");
            for (int i = 0; i < courses.size(); i++) {
                if (!courses.get(i).isJsonObject()) continue;
                JsonObject c = courses.get(i).getAsJsonObject();
                String courseName = c.has("course_name") && !c.get("course_name").isJsonNull() ? c.get("course_name").getAsString() : "Course";

                TextView tvCourse = new TextView(holder.itemView.getContext());
                tvCourse.setText("• " + courseName);
                tvCourse.setTextColor(Color.parseColor("#4B5563"));
                tvCourse.setTextSize(14);
                tvCourse.setPadding(0, 4, 0, 4);

                holder.llCourses.addView(tvCourse);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSemesterName;
        TextView tvStatus;
        LinearLayout llCourses;

        ViewHolder(View itemView) {
            super(itemView);
            tvSemesterName = itemView.findViewById(R.id.tvSemesterName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            llCourses = itemView.findViewById(R.id.llCourses);
        }
    }
}
