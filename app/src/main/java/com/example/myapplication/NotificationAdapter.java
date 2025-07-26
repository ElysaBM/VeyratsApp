package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String imageUrl, String timestamp);
    }

    private final Context context;
    private final List<Notification> notificationList;
    private final OnItemClickListener listener;

    public NotificationAdapter(Context context, List<Notification> notificationList, OnItemClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView notifTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notifTextView = itemView.findViewById(R.id.notifTextView);
        }

        public void bind(Notification notification) {
            notifTextView.setText("Unknown person detected at " + notification.getTimestamp());

            notifTextView.setOnClickListener(v ->
                    listener.onItemClick(notification.getImageUrl(), notification.getTimestamp())
            );

            // Optional: visually highlight the newest notification if needed
            // if (getAdapterPosition() == 0) {
            //     notifTextView.setTypeface(null, Typeface.BOLD);
            // }
        }
    }
}
