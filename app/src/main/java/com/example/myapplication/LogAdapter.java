package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<LogEntry> logList;

    public LogAdapter(List<LogEntry> logList) {
        this.logList = logList;
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView, nameTextView, typeTextView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry currentLog = logList.get(position);

        holder.timestampTextView.setText(currentLog.getFormattedTimestamp());
        holder.nameTextView.setText(currentLog.getTenantName());
        holder.typeTextView.setText(currentLog.getEntryExit());
    }

    @Override
    public int getItemCount() {
        return logList != null ? logList.size() : 0;
    }
}
