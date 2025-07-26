package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class UnknownDetailFragment extends Fragment {

    private ImageView unknownImageView;
    private TextView timestampTextView;

    private String imageUrl;
    private String timestamp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.unknown_detail_fragment, container, false);

        unknownImageView = view.findViewById(R.id.unknownImageView);
        timestampTextView = view.findViewById(R.id.timestampTextView);

        // Priority 1: Arguments passed from NotifFragment
        if (getArguments() != null) {
            imageUrl = getArguments().getString("imageUrl");
            timestamp = getArguments().getString("timestamp");
        }

        // Priority 2: Fallback if launched directly from system notification
        if ((imageUrl == null || timestamp == null) && getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (imageUrl == null) imageUrl = intent.getStringExtra("imageUrl");
                if (timestamp == null) timestamp = intent.getStringExtra("timestamp");
            }
        }

        // Display content
        if (imageUrl != null) {
            Glide.with(requireContext()).load(imageUrl).into(unknownImageView);
        }

        if (timestamp != null) {
            timestampTextView.setText("Detected at: " + timestamp);
        }

        return view;
    }
}
