package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class UnknownFragment extends Fragment {

    private RecyclerView recyclerView;
    private UnknownPersonAdapter adapter;
    private List<UnknownPerson> unknownPersonList;

    public UnknownFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (fragment_unknown.xml)
        View view = inflater.inflate(R.layout.fragment_unknown, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.unknownRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Initialize adapter and list
        unknownPersonList = new ArrayList<>();

        // Provide an OnItemClickListener when creating the adapter
        adapter = new UnknownPersonAdapter(getContext(), unknownPersonList, new UnknownPersonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UnknownPerson person) {
                // Handle item click (optional: navigate to UnknownDetailFragment with person data)
                // You can navigate to the detail fragment here or perform any other action
            }
        });
        recyclerView.setAdapter(adapter);

        // Load data from Firestore
        loadUnknownPersonsFromFirestore();

        return view;
    }

    private void loadUnknownPersonsFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("unknown_faces")
                .orderBy("timestamp", Query.Direction.DESCENDING)  // Order by timestamp (latest first)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("FirestoreDebug", "Documents fetched: " + queryDocumentSnapshots.size());
                    unknownPersonList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UnknownPerson person = document.toObject(UnknownPerson.class);
                        if (person != null) {
                            unknownPersonList.add(person);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Log or handle the error
                    Log.e("FirestoreError", "Error loading data from Firestore", e);
                });
    }
}
