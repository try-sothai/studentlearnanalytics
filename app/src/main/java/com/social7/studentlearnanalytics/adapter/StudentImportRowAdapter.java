package com.social7.studentlearnanalytics.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.StudentImportItem;

import java.util.ArrayList;
import java.util.List;

public class StudentImportRowAdapter extends RecyclerView.Adapter<StudentImportRowAdapter.RowViewHolder> {

    private final List<StudentImportItem.RowItem> rows = new ArrayList<>();

    public void setRows(List<StudentImportItem.RowItem> newRows) {
        rows.clear();
        if (newRows != null) rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_import_row, parent, false);
        return new RowViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRowNumber, tvStudentIdentifier, tvRowStatus, tvRowNames, tvRowMeta, tvRowErrors;

        public RowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRowNumber = itemView.findViewById(R.id.tvRowNumber);
            tvStudentIdentifier = itemView.findViewById(R.id.tvStudentIdentifier);
            tvRowStatus = itemView.findViewById(R.id.tvRowStatus);
            tvRowNames = itemView.findViewById(R.id.tvRowNames);
            tvRowMeta = itemView.findViewById(R.id.tvRowMeta);
            tvRowErrors = itemView.findViewById(R.id.tvRowErrors);
        }

        public void bind(StudentImportItem.RowItem item) {
            tvRowNumber.setText(itemView.getContext().getString(R.string.row_prefix) + item.rowNumber);
            tvStudentIdentifier.setText(item.studentIdentifier != null && !item.studentIdentifier.isEmpty()
                    ? item.studentIdentifier : "(No ID)");

            String status = item.status != null ? item.status.toUpperCase() : "UNKNOWN";
            tvRowStatus.setText(status);

            if ("VALID".equalsIgnoreCase(status) || "IMPORTED".equalsIgnoreCase(status)) {
                tvRowStatus.setTextColor(Color.parseColor("#16A34A"));
            } else if ("DUPLICATE".equalsIgnoreCase(status)) {
                tvRowStatus.setTextColor(Color.parseColor("#EA580C"));
            } else {
                tvRowStatus.setTextColor(Color.parseColor("#DC2626"));
            }

            String en = item.englishName != null ? item.englishName : "";
            String kh = item.khmerName != null && !item.khmerName.isEmpty() ? " (" + item.khmerName + ")" : "";
            tvRowNames.setText(en + kh);

            if (tvRowMeta != null) {
                StringBuilder meta = new StringBuilder();
                if (item.departmentCode != null && !item.departmentCode.isEmpty()) {
                    meta.append("Faculty: ").append(item.departmentCode);
                }
                if (item.programCode != null && !item.programCode.isEmpty()) {
                    if (meta.length() > 0) meta.append(" • ");
                    meta.append("Major: ").append(item.programCode);
                }
                if ("IMPORTED".equalsIgnoreCase(status)) {
                    if (meta.length() > 0) meta.append(" • ");
                    String uname = item.username != null && !item.username.isEmpty() ? item.username : item.studentIdentifier;
                    meta.append("Username: ").append(uname).append(" • Initial Pass: STD@12345 • Role: student");
                }
                if (meta.length() > 0) {
                    tvRowMeta.setVisibility(View.VISIBLE);
                    tvRowMeta.setText(meta.toString());
                } else {
                    tvRowMeta.setVisibility(View.GONE);
                }
            }

            if (item.errors != null && !item.errors.isEmpty()) {
                tvRowErrors.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (String err : item.errors) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("• ").append(err);
                }
                tvRowErrors.setText(sb.toString());
            } else {
                tvRowErrors.setVisibility(View.GONE);
            }
        }
    }
}
