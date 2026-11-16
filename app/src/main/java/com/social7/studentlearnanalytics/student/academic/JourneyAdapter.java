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
        int studyYear = item.get("study_year").getAsInt();
        holder.tvStudyYear.setText(String.format("Year %d", studyYear));

        JsonObject semesters = item.getAsJsonObject("semesters");
        JsonArray semesterList = new JsonArray();
        for (String key : semesters.keySet()) {
            semesterList.add(semesters.getAsJsonObject(key));
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
