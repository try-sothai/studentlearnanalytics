package com.social7.studentlearnanalytics.teacher.attendance;

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

public class TakeAttendanceAdapter extends RecyclerView.Adapter<TakeAttendanceAdapter.ViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChange();
    }

    public static class StudentAttendanceItem {
        public String id;
        public String name;
        public String status; // "P", "A", "L", "E"

        public StudentAttendanceItem(String id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
    }

    private final Context context;
    private final List<StudentAttendanceItem> originalList;
    private final List<StudentAttendanceItem> filteredList;
    private final OnStatusChangeListener statusChangeListener;

    public TakeAttendanceAdapter(Context context, List<StudentAttendanceItem> list, OnStatusChangeListener statusChangeListener) {
        this.context = context;
        this.originalList = list != null ? list : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.statusChangeListener = statusChangeListener;
    }

    public void markAllPresent() {
        for (StudentAttendanceItem item : originalList) {
            item.status = "P";
        }
        notifyDataSetChanged();
        if (statusChangeListener != null) statusChangeListener.onStatusChange();
    }

    public void filter(String query) {
        filteredList.clear();
        String text = query != null ? query.trim().toLowerCase() : "";
        for (StudentAttendanceItem item : originalList) {
            if (text.isEmpty() ||
                (item.id != null && item.id.toLowerCase().contains(text)) ||
                (item.name != null && item.name.toLowerCase().contains(text))) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public List<StudentAttendanceItem> getItems() {
        return originalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_take_attendance_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentAttendanceItem item = filteredList.get(position);
        holder.bind(item, statusChangeListener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentAvatar, tvStudentName, tvStudentId;
        TextView btnPresent, btnAbsent, btnLate, btnExcused;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentAvatar = itemView.findViewById(R.id.tvStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);

            btnPresent = itemView.findViewById(R.id.btnStatusPresent);
            btnAbsent = itemView.findViewById(R.id.btnStatusAbsent);
            btnLate = itemView.findViewById(R.id.btnStatusLate);
            btnExcused = itemView.findViewById(R.id.btnStatusExcused);
        }

        public void bind(StudentAttendanceItem item, OnStatusChangeListener listener) {
            if (tvStudentAvatar != null) {
                String initial = item.name != null && !item.name.isEmpty() ? String.valueOf(item.name.charAt(0)).toUpperCase() : "S";
                tvStudentAvatar.setText(initial);
            }
            if (tvStudentName != null) tvStudentName.setText(item.name);
            if (tvStudentId != null) tvStudentId.setText(item.id);

            updateToggleState(item, item.status);

            if (btnPresent != null) btnPresent.setOnClickListener(v -> { item.status = "P"; updateToggleState(item, "P"); if (listener != null) listener.onStatusChange(); });
            if (btnAbsent != null) btnAbsent.setOnClickListener(v -> { item.status = "A"; updateToggleState(item, "A"); if (listener != null) listener.onStatusChange(); });
            if (btnLate != null) btnLate.setOnClickListener(v -> { item.status = "L"; updateToggleState(item, "L"); if (listener != null) listener.onStatusChange(); });
            if (btnExcused != null) btnExcused.setOnClickListener(v -> { item.status = "E"; updateToggleState(item, "E"); if (listener != null) listener.onStatusChange(); });
        }

        private void updateToggleState(StudentAttendanceItem item, String status) {
            resetButton(btnPresent);
            resetButton(btnAbsent);
            resetButton(btnLate);
            resetButton(btnExcused);

            if ("A".equalsIgnoreCase(status) && btnAbsent != null) {
                btnAbsent.setBackgroundResource(R.drawable.bg_notif_tab_active);
                btnAbsent.setTextColor(Color.WHITE);
            } else if ("L".equalsIgnoreCase(status) && btnLate != null) {
                btnLate.setBackgroundResource(R.drawable.bg_notif_tab_active);
                btnLate.setTextColor(Color.WHITE);
            } else if ("E".equalsIgnoreCase(status) && btnExcused != null) {
                btnExcused.setBackgroundResource(R.drawable.bg_notif_tab_active);
                btnExcused.setTextColor(Color.WHITE);
            } else if (btnPresent != null) {
                btnPresent.setBackgroundResource(R.drawable.bg_notif_tab_active);
                btnPresent.setTextColor(Color.WHITE);
            }
        }

        private void resetButton(TextView btn) {
            if (btn != null) {
                btn.setBackground(null);
                btn.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }
}
