package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "default_channel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    ActivityMainBinding binding;

    private FirebaseFirestore db;
    private boolean isInitialLoadMain = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Step 1: Inflate layout and show UI quickly
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Step 2: Set up bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.logs) {
                replaceFragment(new LogsFragment());
            } else if (item.getItemId() == R.id.contacts) {
                replaceFragment(new ContactFragment());
            } else if (item.getItemId() == R.id.camera) {
                replaceFragment(new CamFragment());
            }
            return true;
        });

        // Step 3: Load default or intent-passed fragment immediately
        handleIntentExtras();

        // Step 4: Delay Firestore & Notification setup to avoid ANR
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            requestNotificationPermissionIfNeeded();
            createNotificationChannel();
            listenForNewUnknownFaces();
        }, 1000); // 1-second delay
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Channel for general notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void handleIntentExtras() {
        String intentImageUrl = getIntent().getStringExtra("imageUrl");
        String intentTimestamp = getIntent().getStringExtra("timestamp");

        if (intentImageUrl != null && intentTimestamp != null) {
            UnknownDetailFragment detailFragment = new UnknownDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("imageUrl", intentImageUrl);
            bundle.putString("timestamp", intentTimestamp);
            detailFragment.setArguments(bundle);
            replaceFragment(detailFragment);
        } else {
            replaceFragment(new HomeFragment());
        }
    }

    void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void showNotification(String title, String message, String imageUrl, String timestamp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("timestamp", timestamp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
    }

    private void listenForNewUnknownFaces() {
        db = FirebaseFirestore.getInstance();

        db.collection("unknown_faces")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("MainActivity", "Listen failed.", error);
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    String imageUrl = dc.getDocument().getString("imageUrl");
                                    String timestamp = dc.getDocument().getString("timestamp");

                                    if (imageUrl != null && timestamp != null) {
                                        if (!isInitialLoadMain) {
                                            showNotification("Unknown Person Detected", "New detection at " + timestamp, imageUrl, timestamp);
                                        }
                                    }
                                }
                            }
                            isInitialLoadMain = false;
                        }
                    }
                });
    }
}
