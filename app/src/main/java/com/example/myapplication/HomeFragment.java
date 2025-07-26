package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.myapplication.AudioRoomLauncher;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    Button register_tenant, profile, unknown, unlockGate;
    ImageButton notif, mic;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        register_tenant = view.findViewById(R.id.register_tenant);
        profile = view.findViewById(R.id.profile);
        unknown = view.findViewById(R.id.unknown);
        unlockGate = view.findViewById(R.id.unlock_gate);  // Unlock Gate Button
        notif = view.findViewById(R.id.notif);
        mic = view.findViewById(R.id.mic);

        // 🎤 Mic button - Start AudioRoom activity from Kotlin launcher
        mic.setOnClickListener(v -> {
            AudioRoomLauncher.launch(requireContext());
        });

        // 👤 Register Tenant
        register_tenant.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new register());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // 🧾 Tenant Profile
        profile.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new TenantProfile());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // ❓ Unknown Fragment
        unknown.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new UnknownFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // 🔔 Notification Fragment
        notif.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new NotifFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // 🔓 Unlock Gate Logic
        unlockGate.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("gate_control").document("main_gate")
                    .update("unlock", true)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Unlock signal sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to send unlock signal", Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}