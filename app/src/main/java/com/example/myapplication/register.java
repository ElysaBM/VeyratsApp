package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class register extends Fragment {
    private static final String TAG = "Firestore";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText name, birthday, address;
    Spinner genderSpinner;
    Button signup;

    public register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        name = view.findViewById(R.id.name);
        birthday = view.findViewById(R.id.birthday); // changed from age to birthday
        address = view.findViewById(R.id.address);
        genderSpinner = view.findViewById(R.id.gender_spinner);
        signup = view.findViewById(R.id.signup);

        // Setup gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Open date picker when clicking birthday field
        birthday.setOnClickListener(v -> showDatePickerDialog());

        signup.setOnClickListener(v -> registerTenant());

        return view;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    birthday.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Optional: Prevent future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void registerTenant() {
        String userName = name.getText().toString().trim();
        String userBirthday = birthday.getText().toString().trim();
        String userAddress = address.getText().toString().trim();
        String userGender = genderSpinner.getSelectedItem().toString();

        if (userName.isEmpty() || userBirthday.isEmpty() || userAddress.isEmpty() || userGender.equals("Gender")) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("name", userName);
        tenantData.put("birthday", userBirthday);
        tenantData.put("address", userAddress);
        tenantData.put("gender", userGender);

        String documentId = "tenant_" + userName.replace(" ", "_");

        db.collection("tenants").document(documentId)
                .set(tenantData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Document successfully written as: " + documentId);
                    Toast toast = Toast.makeText(getContext(), "Tenant Registered Successfully!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 200);
                    toast.show();

                    new android.os.Handler().postDelayed(() -> navigateToTenantProfile(documentId), 1500);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToTenantProfile(String tenantId) {
        TenantProfile tenantProfileFragment = new TenantProfile();

        Bundle bundle = new Bundle();
        bundle.putString("tenantId", tenantId);
        tenantProfileFragment.setArguments(bundle);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, tenantProfileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
