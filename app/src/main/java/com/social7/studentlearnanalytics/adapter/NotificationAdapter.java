package com.social7.studentlearnanalytics.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.studentlearnanalytics.R;
import com.social7.studentlearnanalytics.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final Context context;
    private final List<Notification> notificationList;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_row, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification current = notificationList.get(position);
        Notification previous = position > 0 ? notificationList.get(position - 1) : null;
        holder.bind(current, previous, listener);
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotifDateHeader;
        ImageView imgNotifIcon;
        TextView tvNotifTitle, tvNotifMessage, tvNotifTime;
        View viewUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotifDateHeader = itemView.findViewById(R.id.tvNotifDateHeader);
            imgNotifIcon = itemView.findViewById(R.id.imgNotifIcon);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvNotifTime = itemView.findViewById(R.id.tvNotifTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }

        public void bind(final Notification notification, final Notification previous, final OnNotificationClickListener listener) {
            if (tvNotifDateHeader != null) {
                String currentGroup = notification.getDateGroup();
                String previousGroup = previous != null ? previous.getDateGroup() : null;

                if (previous == null || !currentGroup.equalsIgnoreCase(previousGroup)) {
                    tvNotifDateHeader.setText(currentGroup);
                    tvNotifDateHeader.setVisibility(View.VISIBLE);
                } else {
                    tvNotifDateHeader.setVisibility(View.GONE);
                }
            }

            if (tvNotifTitle != null) tvNotifTitle.setText(notification.getTitle());
            if (tvNotifMessage != null) tvNotifMessage.setText(notification.getMessage());
            if (tvNotifTime != null) tvNotifTime.setText(notification.getTimeAgo());

            if (viewUnreadDot != null) {
                viewUnreadDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
            }

            if (imgNotifIcon != null) {
                String category = notification.getCategory();
                if ("User".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_users);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D8E5FF")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0755F5")));
                } else if ("Course".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_book);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEAD2")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF9C32")));
                } else if ("Report".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_chart_bar);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D4F8EB")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#08C985")));
                } else if ("System".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_database_symbol);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBF3FF")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0755F5")));
                } else if ("Security".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_prediction);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCE8E8")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F04444")));
                } else if ("Assignment".equalsIgnoreCase(category) || "Homework".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_assignment);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D8E5FF")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0755F5")));
                } else if ("Quiz".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_assignment);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEAD2")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF9C32")));
                } else if ("Grade".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_chart_bar);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D4F8EB")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#08C985")));
                } else if ("Exam".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_assignment);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCE8E8")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F04444")));
                } else if ("Attendance".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_calendar);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D4F8EB")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#08C985")));
                } else if ("Announcement".equalsIgnoreCase(category)) {
                    imgNotifIcon.setImageResource(R.drawable.ic_notifications);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBF3FF")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0755F5")));
                } else {
                    imgNotifIcon.setImageResource(R.drawable.ic_notifications);
                    imgNotifIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D8E5FF")));
                    imgNotifIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#0755F5")));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNotificationClick(notification);
            });
        }
    }
}
