package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerLogin extends AppCompatActivity {
    private String password; // Store Firestore password safely
    private FirebaseFirestore db;

    private TextView welcome;
    private EditText pass;
    private Button go;
    private String converted_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner_login);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components **after** setContentView()
        welcome = findViewById(R.id.welcome);
        pass = findViewById(R.id.pass);
        go = findViewById(R.id.go);

        go.setEnabled(false); // Disable login button until Firestore fetches password
        getCode(); // Fetch password from Firestore

        // Set up button click listener
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                converted_pass = pass.getText().toString().trim(); // Trim input to avoid whitespace errors

                if (password == null) { // Check if Firestore data has loaded
                    Toast.makeText(OwnerLogin.this, "Please wait, loading credentials...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (converted_pass.isEmpty()) { // Prevent empty password submission
                    Toast.makeText(OwnerLogin.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (converted_pass.equals(password)) { // Correct password case
                    Toast.makeText(OwnerLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OwnerLogin.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else { // Incorrect password case
                    Toast.makeText(OwnerLogin.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getCode() {
        DocumentReference docRef = db.collection("users").document("passcode");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        password = document.getString("login"); // Get the "login" field
                        Log.d("Firestore", "Login password fetched: " + password);
                        Toast.makeText(OwnerLogin.this, "Password Loaded", Toast.LENGTH_SHORT).show();

                        if (password != null && !password.isEmpty()) {
                            go.setEnabled(true); // Enable button only when password is loaded
                        } else {
                            Log.e("Firestore", "Error: Password is empty in Firestore!");
                            Toast.makeText(OwnerLogin.this, "Error: Password is empty!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "No document found!");
                        Toast.makeText(OwnerLogin.this, "Error: No password found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Firestore", "Error fetching document", task.getException());
                    Toast.makeText(OwnerLogin.this, "Firestore error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
