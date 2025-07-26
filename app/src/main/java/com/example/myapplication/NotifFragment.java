package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotifFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private String lastKnownTimestamp = null; // ✅ Track the latest timestamp to filter real-time additions

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewNotif);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize list and adapter
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList, (imageUrl, timestamp) -> {
            Bundle bundle = new Bundle();
            bundle.putString("imageUrl", imageUrl);
            bundle.putString("timestamp", timestamp);

            UnknownDetailFragment detailFragment = new UnknownDetailFragment();
            detailFragment.setArguments(bundle);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        recyclerView.setAdapter(adapter);

        // Firestore setup
        db = FirebaseFirestore.getInstance();
        listenForNewUnknownFaces();

        return view;
    }

    private boolean isAlreadyNotified(String imageUrl, String timestamp) {
        for (Notification n : notificationList) {
            if (n.getImageUrl().equals(imageUrl) && n.getTimestamp().equals(timestamp)) {
                return true;
            }
        }
        return false;
    }

    private void listenForNewUnknownFaces() {
        db.collection("unknown_faces")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("NotifFragment", "Firestore listen failed.", error);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        boolean added = false;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                DocumentSnapshot doc = dc.getDocument();
                                String imageUrl = doc.getString("imageUrl");
                                String timestamp = doc.getString("timestamp");

                                if (imageUrl == null || timestamp == null) continue;

                                if (!isAlreadyNotified(imageUrl, timestamp)) {
                                    Notification notification = new Notification(imageUrl, timestamp);
                                    notificationList.add(notification);
                                    added = true;

                                    // ✅ Only notify if timestamp is newer than the latest known
                                    if (lastKnownTimestamp != null && timestamp.compareTo(lastKnownTimestamp) > 0) {
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).showNotification(
                                                    "Unknown Person Detected",
                                                    "New detection at " + timestamp,
                                                    "",  // Optionally include imageUrl
                                                    timestamp
                                            );
                                        }
                                    }

                                    // ✅ Update last known timestamp if it's newer
                                    if (lastKnownTimestamp == null || timestamp.compareTo(lastKnownTimestamp) > 0) {
                                        lastKnownTimestamp = timestamp;
                                    }
                                }
                            }
                        }

                        if (added) {
                            // Sort descending by timestamp
                            Collections.sort(notificationList, new Comparator<Notification>() {
                                @Override
                                public int compare(Notification n1, Notification n2) {
                                    return n2.getTimestamp().compareTo(n1.getTimestamp());
                                }
                            });

                            adapter.notifyDataSetChanged();

                            recyclerView.post(() -> recyclerView.scrollToPosition(0));
                        }

                        // ✅ On first load, initialize lastKnownTimestamp
                        if (lastKnownTimestamp == null && !notificationList.isEmpty()) {
                            lastKnownTimestamp = notificationList.get(0).getTimestamp();
                        }
                    }
                });
    }
}
