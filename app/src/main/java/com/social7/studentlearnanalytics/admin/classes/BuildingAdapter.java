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

/**
 * Adapter for Building Management cards showing Building -> Floor -> Class hierarchy.
 */
public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.ViewHolder> {

    public static class BuildingItem {
        public String buildingId;
        public String code;
        public String name;
        public String floorsSummary;
        public int floorsCount;
        public int classesCount;
        public String status;
        public String rawJson;

        public BuildingItem(String buildingId, String code, String name, int floorsCount, int classesCount, String floorsSummary, String status, String rawJson) {
            this.buildingId = buildingId;
            this.code = code;
            this.name = name;
            this.floorsCount = floorsCount;
            this.classesCount = classesCount;
            this.floorsSummary = floorsSummary;
            this.status = status;
            this.rawJson = rawJson;
        }
    }

    public interface OnBuildingClickListener {
        void onBuildingClick(BuildingItem item);
    }

    private final Context context;
    private final List<BuildingItem> originalList;
    private final List<BuildingItem> filteredList;
    private final OnBuildingClickListener listener;

    public BuildingAdapter(Context context, List<BuildingItem> list, OnBuildingClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_building, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BuildingItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (BuildingItem item : originalList) {
            if (text.isEmpty()
                    || (item.code != null && item.code.toLowerCase().contains(text))
                    || (item.name != null && item.name.toLowerCase().contains(text))
                    || (item.floorsSummary != null && item.floorsSummary.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode, tvName, tvFloorSummary, tvClassHierarchy, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvBuildingCode);
            tvName = itemView.findViewById(R.id.tvBuildingName);
            tvFloorSummary = itemView.findViewById(R.id.tvBuildingSummary);
            tvClassHierarchy = itemView.findViewById(R.id.tvBuildingHierarchy);
            tvStatus = itemView.findViewById(R.id.tvBuildingStatus);
        }

        public void bind(BuildingItem item, OnBuildingClickListener listener) {
            if (tvCode != null) tvCode.setText(item.code != null && !item.code.isEmpty() ? item.code : "BLDG");
            if (tvName != null) tvName.setText(item.name);
            if (tvFloorSummary != null) {
                tvFloorSummary.setText(item.floorsCount + " Floors • " + item.classesCount + " Classes");
            }
            if (tvClassHierarchy != null) {
                tvClassHierarchy.setText(item.floorsSummary != null && !item.floorsSummary.isEmpty()
                        ? item.floorsSummary
                        : "Tap to configure floors and assign classes");
            }
            if (tvStatus != null) {
                String st = item.status != null && !item.status.isEmpty() ? item.status : "Active";
                String displaySt = st.substring(0, 1).toUpperCase() + st.substring(1).toLowerCase();
                tvStatus.setText(displaySt);
                tvStatus.setTextColor("active".equalsIgnoreCase(st)
                        ? Color.parseColor("#10B981")
                        : Color.parseColor("#EF4444"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBuildingClick(item);
            });
        }
    }
}
