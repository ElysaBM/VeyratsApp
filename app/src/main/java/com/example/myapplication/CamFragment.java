package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.*;

import java.io.File;
import java.util.*;

public class CamFragment extends Fragment {

    private static final int PICK_IMAGES_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Button selectImagesButton, nextButton;
    private ProgressBar uploadProgressBar;
    private Spinner tenantSpinner;

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> tenantNames = new ArrayList<>();
    private ArrayList<String> tenantIds = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Cloudinary cloudinary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cam, container, false);

        selectImagesButton = view.findViewById(R.id.selectImagesButton);
        nextButton = view.findViewById(R.id.nextButton);
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar);
        tenantSpinner = view.findViewById(R.id.tenantSpinner);

        cloudinary = CloudinaryManager.getInstance();

        fetchTenantsFromFirestore();

        selectImagesButton.setOnClickListener(v -> {
            if (checkPermission()) {
                openMultipleImageChooser();
            } else {
                requestPermission();
            }
        });

        nextButton.setOnClickListener(v -> {
            int selectedIndex = tenantSpinner.getSelectedItemPosition();

            if (selectedIndex <= 0) {
                Toast.makeText(getContext(), "Please select a tenant", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUris.size() < 20) {
                Toast.makeText(getContext(), "Please select at least 20 images", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImagesToCloudinary();
        });

        return view;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openMultipleImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUris.clear();

            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    imageUris.add(data.getData());
                }

                Toast.makeText(getContext(), imageUris.size() + " images selected", Toast.LENGTH_SHORT).show();

                if (imageUris.size() < 20) {
                    Toast.makeText(getContext(), "Please select at least 20 images", Toast.LENGTH_LONG).show();
                }

                nextButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadImagesToCloudinary() {
        uploadProgressBar.setVisibility(View.VISIBLE);
        int tenantIndex = tenantSpinner.getSelectedItemPosition();
        String tenantName = tenantNames.get(tenantIndex);
        String tenantId = tenantIds.get(tenantIndex);

        new Thread(() -> {
            List<String> uploadedUrls = new ArrayList<>();

            for (Uri uri : imageUris) {
                String path = getRealPathFromURI(uri);
                if (path != null) {
                    try {
                        File file = new File(path);
                        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                                "folder", "tenants/" + tenantName
                        ));
                        String url = (String) uploadResult.get("secure_url");
                        uploadedUrls.add(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            DocumentReference tenantRef = db.collection("tenants").document(tenantId);
            for (String url : uploadedUrls) {
                tenantRef.update("imageUrls", FieldValue.arrayUnion(url));
            }

            requireActivity().runOnUiThread(() -> {
                uploadProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Uploaded " + uploadedUrls.size() + " images", Toast.LENGTH_SHORT).show();

                GalleryFragment galleryFragment = GalleryFragment.newInstance(tenantId, tenantName);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, galleryFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }).start();
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void fetchTenantsFromFirestore() {
        db.collection("tenants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tenantNames.clear();
                    tenantIds.clear();

                    tenantNames.add("Choose tenant");
                    tenantIds.add("");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        tenantNames.add(document.getString("name"));
                        tenantIds.add(document.getId());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                            android.R.layout.simple_spinner_item, tenantNames) {
                        @Override
                        public boolean isEnabled(int position) {
                            return position != 0;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;
                            if (position == 0) {
                                tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                            } else {
                                tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                            }
                            return view;
                        }
                    };

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    tenantSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch tenants", Toast.LENGTH_SHORT).show());
    }
}
