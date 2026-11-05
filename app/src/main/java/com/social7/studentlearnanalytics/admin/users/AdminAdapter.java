package com.social7.studentlearnanalytics.admin.users;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Admin;

import java.util.ArrayList;
import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    public interface OnAdminActionListener {
        void onViewDetails(Admin admin);
        void onEditAdmin(Admin admin);
        void onToggleStatus(Admin admin);
        void onDeleteAdmin(Admin admin);
    }

    private final Context context;
    private final List<Admin> adminList;
    private final List<Admin> filteredList;
    private final OnAdminActionListener listener;

    private String currentStatusFilter = "ALL";
    private String currentRoleFilter = "All Roles";

    public AdminAdapter(Context context, List<Admin> adminList, OnAdminActionListener listener) {
        this.context = context;
        this.adminList = adminList;
        this.filteredList = new ArrayList<>(adminList);
        this.listener = listener;
    }

    public void filter(String query, String statusFilter, String roleFilter) {
        if (statusFilter != null) {
            this.currentStatusFilter = statusFilter.toUpperCase();
        }
        if (roleFilter != null) {
            this.currentRoleFilter = roleFilter;
        }

        filteredList.clear();
        String lowerQuery = (query != null) ? query.toLowerCase().trim() : "";

        for (Admin admin : adminList) {
            boolean matchesSearch = lowerQuery.isEmpty() ||
                    (admin.getName() != null && admin.getName().toLowerCase().contains(lowerQuery)) ||
                    (admin.getId() != null && admin.getId().toLowerCase().contains(lowerQuery)) ||
                    (admin.getEmail() != null && admin.getEmail().toLowerCase().contains(lowerQuery)) ||
                    (admin.getRole() != null && admin.getRole().toLowerCase().contains(lowerQuery));

            boolean matchesStatus = "ALL".equals(currentStatusFilter) ||
                    ("ACTIVE".equals(currentStatusFilter) && "Active".equalsIgnoreCase(admin.getStatus())) ||
                    ("INACTIVE".equals(currentStatusFilter) && "Inactive".equalsIgnoreCase(admin.getStatus()));

            boolean matchesRole = "All Roles".equalsIgnoreCase(currentRoleFilter) ||
                    (admin.getRole() != null && admin.getRole().equalsIgnoreCase(currentRoleFilter));

            if (matchesSearch && matchesStatus && matchesRole) {
                filteredList.add(admin);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_row, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Admin admin = filteredList.get(position);
        holder.bind(context, admin, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminRowAvatar, tvAdminRowId, tvAdminRowName, tvAdminRowRole, tvAdminRowStatus;
        ImageView imgAdminRowActionMenu;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminRowAvatar = itemView.findViewById(R.id.tvAdminRowAvatar);
            tvAdminRowId = itemView.findViewById(R.id.tvAdminRowId);
            tvAdminRowName = itemView.findViewById(R.id.tvAdminRowName);
            tvAdminRowRole = itemView.findViewById(R.id.tvAdminRowRole);
            tvAdminRowStatus = itemView.findViewById(R.id.tvAdminRowStatus);
            imgAdminRowActionMenu = itemView.findViewById(R.id.imgAdminRowActionMenu);
        }

        public void bind(final Context context, final Admin admin, final OnAdminActionListener listener) {
            if (tvAdminRowId != null) tvAdminRowId.setText(admin.getId());
            if (tvAdminRowName != null) tvAdminRowName.setText(admin.getName());
            if (tvAdminRowRole != null) tvAdminRowRole.setText(admin.getRole());

            if (tvAdminRowAvatar != null) {
                String name = admin.getName();
                String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "A";
                tvAdminRowAvatar.setText(initial);
            }

            if (tvAdminRowStatus != null) {
                boolean isActive = "Active".equalsIgnoreCase(admin.getStatus());
                tvAdminRowStatus.setText(isActive ? "Active" : "Inactive");
                tvAdminRowStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
                tvAdminRowStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(admin);
            });

            if (imgAdminRowActionMenu != null) {
                imgAdminRowActionMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, imgAdminRowActionMenu);
                    popup.getMenu().add("View Details");
                    popup.getMenu().add("Edit Admin");
                    boolean isActive = "Active".equalsIgnoreCase(admin.getStatus());
                    popup.getMenu().add(isActive ? "Deactivate Account" : "Activate Account");
                    popup.getMenu().add("Delete Admin");

                    popup.setOnMenuItemClickListener(item -> {
                        String title = item.getTitle().toString();
                        if ("View Details".equals(title)) {
                            if (listener != null) listener.onViewDetails(admin);
                            return true;
                        } else if ("Edit Admin".equals(title)) {
                            if (listener != null) listener.onEditAdmin(admin);
                            return true;
                        } else if ("Deactivate Account".equals(title) || "Activate Account".equals(title)) {
                            if (listener != null) listener.onToggleStatus(admin);
                            return true;
                        } else if ("Delete Admin".equals(title)) {
                            if (listener != null) listener.onDeleteAdmin(admin);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }
    }
}
