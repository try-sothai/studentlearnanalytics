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

public class ClassroomAdapter extends RecyclerView.Adapter<ClassroomAdapter.ViewHolder> {

    public static class ClassroomItem {
        public String roomNumber;
        public String roomName;
        public String buildingFloor;
        public int capacity;
        public String equipment;
        public String status;

        public ClassroomItem(String roomNumber, String roomName, String buildingFloor, int capacity, String equipment, String status) {
            this.roomNumber = roomNumber;
            this.roomName = roomName;
            this.buildingFloor = buildingFloor;
            this.capacity = capacity;
            this.equipment = equipment;
            this.status = status;
        }
    }

    public interface OnClassroomClickListener {
        void onClassroomClick(ClassroomItem item);
    }

    private final Context context;
    private final List<ClassroomItem> originalList;
    private final List<ClassroomItem> filteredList;
    private final OnClassroomClickListener listener;

    public ClassroomAdapter(Context context, List<ClassroomItem> list, OnClassroomClickListener listener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_classroom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassroomItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (ClassroomItem item : originalList) {
            if (text.isEmpty() ||
                (item.roomNumber != null && item.roomNumber.toLowerCase().contains(text)) ||
                (item.roomName != null && item.roomName.toLowerCase().contains(text)) ||
                (item.buildingFloor != null && item.buildingFloor.toLowerCase().contains(text)) ||
                (item.equipment != null && item.equipment.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoomNumber, tvRoomName, tvBuildingFloor, tvCapacityEquipment, tvAvailabilityStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvBuildingFloor = itemView.findViewById(R.id.tvBuildingFloor);
            tvCapacityEquipment = itemView.findViewById(R.id.tvCapacityEquipment);
            tvAvailabilityStatus = itemView.findViewById(R.id.tvAvailabilityStatus);
        }

        public void bind(ClassroomItem item, OnClassroomClickListener listener) {
            if (tvRoomNumber != null) tvRoomNumber.setText(item.roomNumber);
            if (tvRoomName != null) tvRoomName.setText(item.roomName);
            if (tvBuildingFloor != null) tvBuildingFloor.setText(item.buildingFloor);
            if (tvCapacityEquipment != null) {
                tvCapacityEquipment.setText(tvCapacityEquipment.getContext().getString(R.string.capacity) + item.capacity + " seats/PCs • " + item.equipment);
            }
            if (tvAvailabilityStatus != null) {
                tvAvailabilityStatus.setText(item.status);
                if ("Available".equalsIgnoreCase(item.status)) {
                    tvAvailabilityStatus.setTextColor(Color.parseColor("#10B981"));
                } else if ("Occupied".equalsIgnoreCase(item.status)) {
                    tvAvailabilityStatus.setTextColor(Color.parseColor("#155DFC"));
                } else {
                    tvAvailabilityStatus.setTextColor(Color.parseColor("#F59E0B"));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClassroomClick(item);
            });
        }
    }
}
