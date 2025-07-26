package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TenantProfile extends Fragment {
    private RecyclerView recyclerView;
    private TenantAdapter tenantAdapter;
    private List<Tenant> tenantList;
    private FirebaseFirestore db;

    public TenantProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_profile, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTenants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tenantList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        tenantAdapter = new TenantAdapter(tenantList, getContext(), tenantName -> {
            Log.d("TenantProfile", "Tenant folder deleted: tenants/" + tenantName);
        });

        recyclerView.setAdapter(tenantAdapter);

        // Listen for updates from Firestore
        db.collection("tenants").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("Firestore", "Error fetching tenants", e);
                    return;
                }

                tenantList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Tenant tenant = doc.toObject(Tenant.class);
                    tenant.setTenantId(doc.getId()); // set document ID manually if needed
                    tenantList.add(tenant);
                }
                tenantAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }
}
