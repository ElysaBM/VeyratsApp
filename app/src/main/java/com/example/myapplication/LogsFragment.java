package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private final List<LogEntry> logList = new ArrayList<>();
    private final Map<String, String> tenantNameCache = new HashMap<>();
    private FirebaseFirestore db;
    private ListenerRegistration logListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        recyclerView = view.findViewById(R.id.logsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        listenToLogs();

        return view;
    }

    private void listenToLogs() {
        logListener = db.collection("activity_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String tenantId = dc.getDocument().getString("tenantId");
                            String type = dc.getDocument().getString("type");
                            String imageUrl = dc.getDocument().getString("imageUrl");
                            Date timestampDate = dc.getDocument().getDate("timestamp");

                            if (tenantId == null || tenantId.isEmpty()) tenantId = "Unknown";
                            if (type == null) type = "N/A";
                            if (imageUrl == null) imageUrl = "";
                            if (timestampDate == null) timestampDate = new Date();

                            String finalTenantId = tenantId;
                            String finalType = type;
                            String finalImageUrl = imageUrl;
                            Date finalTimestampDate = timestampDate;

                            if (tenantNameCache.containsKey(finalTenantId)) {
                                addLogEntry(tenantNameCache.get(finalTenantId), finalTimestampDate, finalType, finalImageUrl);
                            } else {
                                db.collection("tenants").document(finalTenantId).get()
                                        .addOnSuccessListener(tenantDoc -> {
                                            String name = tenantDoc.contains("name") ?
                                                    tenantDoc.getString("name") : finalTenantId;
                                            tenantNameCache.put(finalTenantId, name);
                                            addLogEntry(name, finalTimestampDate, finalType, finalImageUrl);
                                        })
                                        .addOnFailureListener(e -> {
                                            tenantNameCache.put(finalTenantId, finalTenantId);
                                            addLogEntry(finalTenantId, finalTimestampDate, finalType, finalImageUrl);
                                        });
                            }
                        }
                    }
                });
    }

    private void addLogEntry(String tenantName, Date timestampDate, String type, String imageUrl) {
        LogEntry log = new LogEntry(tenantName, timestampDate, type, imageUrl);
        logList.add(log);
        logList.sort((l1, l2) -> l2.getTimestampDate().compareTo(l1.getTimestampDate())); // sort newest first
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (logListener != null) {
            logListener.remove();
        }
    }
}
