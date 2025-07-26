package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView galleryRecyclerView;
    private ProgressBar galleryProgressBar;
    private TextView galleryTitleTextView;

    private String tenantId;
    private String tenantName;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> imageUrls = new ArrayList<>();
    private GalleryAdapter adapter;

    public static GalleryFragment newInstance(String tenantId, String tenantName) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString("tenantId", tenantId);
        args.putString("tenantName", tenantName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryRecyclerView = view.findViewById(R.id.galleryRecyclerView);
        galleryProgressBar = view.findViewById(R.id.galleryProgressBar);
        galleryTitleTextView = view.findViewById(R.id.galleryTitleTextView);

        galleryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new GalleryAdapter(imageUrls);
        galleryRecyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            tenantId = getArguments().getString("tenantId");
            tenantName = getArguments().getString("tenantName");

            galleryTitleTextView.setText(tenantName + "'s Gallery");
            loadImagesFromFirestore();
        }

        return view;
    }

    private void loadImagesFromFirestore() {
        galleryProgressBar.setVisibility(View.VISIBLE);

        db.collection("tenants").document(tenantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    galleryProgressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        List<String> urls = (List<String>) documentSnapshot.get("imageUrls");
                        if (urls != null) {
                            imageUrls.clear();
                            imageUrls.addAll(urls);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    galleryProgressBar.setVisibility(View.GONE);
                });
    }
}
