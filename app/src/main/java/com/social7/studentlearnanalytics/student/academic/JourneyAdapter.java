package com.social7.studentlearnanalytics.student.academic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.utils.YearLevelHelper;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.ViewHolder> {

    private final JsonArray data;

    public JourneyAdapter(JsonArray data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = data.get(position).getAsJsonObject();
        Object rawStudyYear = null;
        if (item.has("year_level") && !item.get("year_level").isJsonNull()) {
            rawStudyYear = item.get("year_level").getAsString();
        } else if (item.has("study_year") && !item.get("study_year").isJsonNull()) {
            rawStudyYear = item.get("study_year").getAsString();
        }
        String label = YearLevelHelper.toFullLabel(holder.itemView.getContext(), rawStudyYear);
        if (YearLevelHelper.parse(rawStudyYear) == null && item.has("study_year_label") && !item.get("study_year_label").isJsonNull()) {
            label = item.get("study_year_label").getAsString();
        }
        holder.tvStudyYear.setText(label);

        JsonArray semesterList = new JsonArray();
        if (item.has("semesters") && item.get("semesters").isJsonObject()) {
            JsonObject semesters = item.getAsJsonObject("semesters");
            for (String key : semesters.keySet()) {
                semesterList.add(semesters.getAsJsonObject(key));
            }
        }

        JourneySemesterAdapter adapter = new JourneySemesterAdapter(semesterList);
        holder.rvSemesters.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvSemesters.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudyYear;
        RecyclerView rvSemesters;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudyYear = itemView.findViewById(R.id.tvStudyYear);
            rvSemesters = itemView.findViewById(R.id.rvSemesters);
        }
    }
}
