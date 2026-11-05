package com.social7.studentlearnanalytics.teacher.attendance;

import android.content.Context;
import android.content.res.ColorStateList;
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

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public interface OnRecordClickListener {
        void onRecordClick(AttendanceRecordItem item);
    }

    public static class AttendanceRecordItem {
        public String date;
        public String status;

        public AttendanceRecordItem(String date, String status) {
            this.date = date;
            this.status = status;
        }
    }

    private final Context context;
    private final List<AttendanceRecordItem> originalList;
    private final List<AttendanceRecordItem> filteredList;
    private final OnRecordClickListener listener;

    public AttendanceAdapter(Context context, List<AttendanceRecordItem> recordList, OnRecordClickListener listener) {
        this.context = context;
        this.originalList = recordList != null ? recordList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (AttendanceRecordItem item : originalList) {
            if (text.isEmpty() ||
                (item.date != null && item.date.toLowerCase().contains(text)) ||
                (item.status != null && item.status.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_row, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecordItem record = filteredList.get(position);
        holder.bind(record, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvAttendanceDate, tvAttendanceStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAttendanceDate = itemView.findViewById(R.id.tvAttendanceDate);
            tvAttendanceStatus = itemView.findViewById(R.id.tvAttendanceStatus);
        }

        public void bind(AttendanceRecordItem record, OnRecordClickListener listener) {
            if (tvAttendanceDate != null) tvAttendanceDate.setText(record.date);
            if (tvAttendanceStatus != null) {
                tvAttendanceStatus.setText(record.status);
                if ("Absent".equalsIgnoreCase(record.status)) {
                    tvAttendanceStatus.setTextColor(Color.parseColor("#EF4444"));
                    tvAttendanceStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                } else if ("Late".equalsIgnoreCase(record.status)) {
                    tvAttendanceStatus.setTextColor(Color.parseColor("#F59E0B"));
                    tvAttendanceStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                } else {
                    tvAttendanceStatus.setTextColor(Color.parseColor("#10B981"));
                    tvAttendanceStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1FAE5")));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRecordClick(record);
            });
        }
    }
}
