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

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public static class AttendanceRecordItem {
        public String date;
        public String status;

        public AttendanceRecordItem(String date, String status) {
            this.date = date;
            this.status = status;
        }
    }

    private final Context context;
    private final List<AttendanceRecordItem> recordList;

    public AttendanceAdapter(Context context, List<AttendanceRecordItem> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_row, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecordItem record = recordList.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvAttendanceDate, tvAttendanceStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAttendanceDate = itemView.findViewById(R.id.tvAttendanceDate);
            tvAttendanceStatus = itemView.findViewById(R.id.tvAttendanceStatus);
        }

        public void bind(AttendanceRecordItem record) {
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
        }
    }
}
