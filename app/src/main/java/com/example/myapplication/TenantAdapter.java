package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.ViewHolder> {

    public interface OnTenantDeletedListener {
        void onTenantDeleted(String tenantName);
    }

    private List<Tenant> tenantList;
    private Context context;
    private OnTenantDeletedListener listener;

    public TenantAdapter(List<Tenant> tenantList, Context context) {
        this.tenantList = tenantList;
        this.context = context;
    }

    public TenantAdapter(List<Tenant> tenantList, Context context, OnTenantDeletedListener listener) {
        this.tenantList = tenantList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);

        holder.textViewName.setText("Name: " + tenant.getName());
        String birthday = tenant.getBirthday();
        int age = calculateAge(birthday);
        holder.textViewBirthday.setText("Birthday: " + birthday);
        holder.textViewAge.setText("Age: " + age);
        holder.textViewAddress.setText("Address: " + tenant.getAddress());
        holder.textViewGender.setText("Gender: " + tenant.getGender());

        holder.btnRemoveTenant.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Tenant")
                    .setMessage("Are you sure you want to remove this tenant?")
                    .setPositiveButton("Yes", (dialog, which) -> removeTenant(tenant, holder.getAdapterPosition()))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private int calculateAge(String birthday) {
        if (birthday == null || birthday.isEmpty()) return 0;

        try {
            String[] parts = birthday.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // 0-based
            int day = Integer.parseInt(parts[2]);

            java.util.Calendar birthDate = java.util.Calendar.getInstance();
            birthDate.set(year, month, day);

            java.util.Calendar today = java.util.Calendar.getInstance();

            int age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR);

            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewBirthday, textViewAge, textViewAddress, textViewGender;
        Button btnRemoveTenant;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewBirthday = itemView.findViewById(R.id.textViewBirthday);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewGender = itemView.findViewById(R.id.textViewGender);
            btnRemoveTenant = itemView.findViewById(R.id.btnRemoveTenant);
        }
    }

    private void removeTenant(Tenant tenant, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String tenantId = tenant.getTenantId();
        String tenantName = tenant.getName();

        Toast.makeText(context, "Deleting tenant, please wait...", Toast.LENGTH_SHORT).show();

        db.collection("tenants").document(tenantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (position >= 0 && position < tenantList.size()) {
                        tenantList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Tenant deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        tenantList.remove(tenant);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }

                    if (listener != null) {
                        listener.onTenantDeleted(tenantName);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to remove tenant from Firestore", Toast.LENGTH_SHORT).show();
                    Log.e("TenantAdapter", "Failed to delete tenant from Firestore", e);
                });
    }
}
