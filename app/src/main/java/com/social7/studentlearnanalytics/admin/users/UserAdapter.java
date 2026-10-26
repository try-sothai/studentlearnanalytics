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
import com.social7.studentlearnanalytics.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onViewDetails(User user);
        void onEditUser(User user);
        void onToggleStatus(User user);
    }

    private final Context context;
    private final List<User> userList;
    private final List<User> filteredList;
    private final OnUserActionListener listener;
    private String currentStatusFilter = "ALL";

    public UserAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
        this.listener = listener;
    }

    public void filter(String query, String statusFilter) {
        if (statusFilter != null) {
            this.currentStatusFilter = statusFilter.toUpperCase();
        }
        filteredList.clear();
        String lowerQuery = (query != null) ? query.toLowerCase().trim() : "";

        for (User user : userList) {
            boolean matchesSearch = lowerQuery.isEmpty() ||
                    (user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                    (user.getId() != null && user.getId().toLowerCase().contains(lowerQuery)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery));

            boolean matchesStatus = "ALL".equals(currentStatusFilter) ||
                    ("ACTIVE".equals(currentStatusFilter) && "Active".equalsIgnoreCase(user.getStatus())) ||
                    ("INACTIVE".equals(currentStatusFilter) && "Inactive".equalsIgnoreCase(user.getStatus()));

            if (matchesSearch && matchesStatus) {
                filteredList.add(user);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredList.get(position);
        holder.bind(context, user, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarChar, tvUserId, tvUserName, tvUserEmail, tvUserStatus;
        ImageView imgUserActionMenu;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarChar = itemView.findViewById(R.id.tvAvatarChar);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            imgUserActionMenu = itemView.findViewById(R.id.imgUserActionMenu);
        }

        public void bind(final Context context, final User user, final OnUserActionListener listener) {
            if (tvUserId != null) tvUserId.setText(user.getId());
            if (tvUserName != null) tvUserName.setText(user.getName());
            if (tvUserEmail != null) tvUserEmail.setText(user.getEmail());

            if (tvAvatarChar != null) {
                String name = user.getName();
                String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "S";
                tvAvatarChar.setText(initial);
            }

            if (tvUserStatus != null) {
                boolean isActive = "Active".equalsIgnoreCase(user.getStatus());
                tvUserStatus.setText(isActive ? "Active" : "Inactive");
                tvUserStatus.setTextColor(Color.parseColor(isActive ? "#16A34A" : "#EF4444"));
                tvUserStatus.setBackgroundResource(isActive ? R.drawable.bg_badge_core : R.drawable.bg_badge_credit);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(user);
            });

            if (imgUserActionMenu != null) {
                imgUserActionMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, imgUserActionMenu);
                    popup.getMenu().add("View Details");
                    popup.getMenu().add("Edit Student");
                    boolean isActive = "Active".equalsIgnoreCase(user.getStatus());
                    popup.getMenu().add(isActive ? "Deactivate Account" : "Activate Account");

                    popup.setOnMenuItemClickListener(item -> {
                        String title = item.getTitle().toString();
                        if ("View Details".equals(title)) {
                            if (listener != null) listener.onViewDetails(user);
                            return true;
                        } else if ("Edit Student".equals(title)) {
                            if (listener != null) listener.onEditUser(user);
                            return true;
                        } else if ("Deactivate Account".equals(title) || "Activate Account".equals(title)) {
                            if (listener != null) listener.onToggleStatus(user);
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
