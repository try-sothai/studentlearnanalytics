package com.social7.studentlearnanalytics.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.StudentImportItem;

import java.util.ArrayList;
import java.util.List;

public class StudentImportHistoryAdapter extends RecyclerView.Adapter<StudentImportHistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryActionListener {
        void onDownloadErrors(StudentImportItem item);
    }

    private final List<StudentImportItem> list = new ArrayList<>();
    private final OnHistoryActionListener listener;

    public StudentImportHistoryAdapter(OnHistoryActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<StudentImportItem> items) {
        list.clear();
        if (items != null) list.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_import_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHistoryId, tvHistoryFilename, tvHistoryStatus, tvHistoryMeta, tvHistoryCounts;
        private final MaterialButton btnDownloadHistoryErrors;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryId = itemView.findViewById(R.id.tvHistoryId);
            tvHistoryFilename = itemView.findViewById(R.id.tvHistoryFilename);
            tvHistoryStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvHistoryMeta = itemView.findViewById(R.id.tvHistoryMeta);
            tvHistoryCounts = itemView.findViewById(R.id.tvHistoryCounts);
            btnDownloadHistoryErrors = itemView.findViewById(R.id.btnDownloadHistoryErrors);
        }

        public void bind(StudentImportItem item, OnHistoryActionListener listener) {
            tvHistoryId.setText("#" + item.id);
            tvHistoryFilename.setText(item.originalFilename != null ? item.originalFilename : "Import");

            String status = item.status != null ? item.status.toUpperCase() : "UNKNOWN";
            tvHistoryStatus.setText(status);

            if ("COMPLETED".equalsIgnoreCase(status)) {
                tvHistoryStatus.setTextColor(Color.parseColor("#16A34A"));
            } else if ("COMPLETED_WITH_ERRORS".equalsIgnoreCase(status)) {
                tvHistoryStatus.setTextColor(Color.parseColor("#EA580C"));
            } else if ("PROCESSING".equalsIgnoreCase(status)) {
                tvHistoryStatus.setTextColor(Color.parseColor("#2563EB"));
            } else {
                tvHistoryStatus.setTextColor(Color.parseColor("#DC2626"));
            }

            String by = item.importedBy != null ? item.importedBy : "System";
            String date = item.createdAt != null && item.createdAt.length() >= 10 ? item.createdAt.substring(0, 10) : "";
            tvHistoryMeta.setText(tvHistoryMeta.getContext().getString(R.string.by) + by + " • " + date);

            tvHistoryCounts.setText(tvHistoryCounts.getContext().getString(R.string.total_1fd328) + item.totalRows + " • Success: " + item.successRows + " • Failed: " + item.failedRows);

            if (item.failedRows > 0 || item.invalidRows > 0 || item.duplicateRows > 0) {
                btnDownloadHistoryErrors.setVisibility(View.VISIBLE);
                btnDownloadHistoryErrors.setOnClickListener(v -> {
                    if (listener != null) listener.onDownloadErrors(item);
                });
            } else {
                btnDownloadHistoryErrors.setVisibility(View.GONE);
            }
        }
    }
}
