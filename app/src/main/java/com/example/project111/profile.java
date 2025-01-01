package com.example.project111;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class profile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView usernameLabel;
    private EditText editUsername, editPassword, editPhone;
    private Button btnLogout, btnHelpMe, btnRequest, btnSaveProfile;
    private ImageView profileImage, eyeIcon;

    private Uri imageUri;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        setContentView(R.layout.activity_profile);

        // Initialize views
        initializeViews();

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Fetch and display user data from Firebase
        loadUserProfileData();

        // Set up event listeners
        setupEventListeners();
    }

    private void initializeViews() {
        usernameLabel = findViewById(R.id.username_label);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        editPhone = findViewById(R.id.edit_phone);
        btnLogout = findViewById(R.id.btn_logout);
        btnHelpMe = findViewById(R.id.btn_help);
        btnRequest = findViewById(R.id.btn_request);
        btnSaveProfile = findViewById(R.id.btn_save);
        profileImage = findViewById(R.id.profile_image);
        eyeIcon = findViewById(R.id.mata_hide);
    }

    private void loadUserProfileData() {
        String username = getSharedPreferences("UserData", MODE_PRIVATE).getString("username", "");

        // Fetch user data from Firebase
        database.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String password = snapshot.child("password").getValue(String.class);
                    String phone = snapshot.child("nomorHp").getValue(String.class);

                    // Populate the views with user data
                    usernameLabel.setText(username);
                    editUsername.setText(username);
                    editPassword.setText(password);
                    editPhone.setText(phone);
                } else {
                    Toast.makeText(profile.this, "User not found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEventListeners() {
        // Logout button listener with confirmation
        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(profile.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("yakin ingin logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear shared preferences and redirect to login
                        getSharedPreferences("UserData", MODE_PRIVATE).edit().clear().apply();
                        startActivity(new Intent(profile.this, login.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Close the dialog
                        dialog.dismiss();
                    })
                    .show();
        });

        // Save profile button listener
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        // Help button listener (Opens WhatsApp chat with a predefined phone number)
        btnHelpMe.setOnClickListener(v -> {
            String phoneNumber = "+6289531832365";
            String url = "https://wa.me/" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Request button listener (Opens WhatsApp chat with random recipe request message)
        btnRequest.setOnClickListener(v -> {
            String phoneNumber = "+6289531832365";
            String[] messages = {
                    "Hi, saya ingin meminta resep.",
                    "Halo, bolehkah saya mendapatkan resep?",
                    "Selamat siang, saya ingin request resep."
            };
            // Generate a random message
            String randomMessage = messages[(int) (Math.random() * messages.length)];
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(randomMessage);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Profile image click listener
        profileImage.setOnClickListener(v -> openImageChooser());

        // Password visibility toggle listener
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void saveProfileChanges() {
        String newUsername = editUsername.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();

        // Validating input
        if (newUsername.isEmpty() || newPassword.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current username (if changed)
        String oldUsername = getSharedPreferences("UserData", MODE_PRIVATE).getString("username", "");

        // Update user data in Firebase
        if (!oldUsername.equals(newUsername)) {
            // If the username changed, you may need to delete the old username and create a new entry
            database.child(oldUsername).removeValue();
        }

        // Update the user's data (username, password, phone number)
        database.child(newUsername).child("username").setValue(newUsername);
        database.child(newUsername).child("password").setValue(newPassword);
        database.child(newUsername).child("nomorHp").setValue(newPhone);

        // Update the SharedPreferences with the new username
        getSharedPreferences("UserData", MODE_PRIVATE).edit().putString("username", newUsername).apply();

        // Optionally, you can upload a new profile image if available
        if (imageUri != null) {
            uploadProfileImage();
        }

        // Display a success message
        Toast.makeText(this, "updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void togglePasswordVisibility() {
        if (editPassword.getTransformationMethod() == null) {
            // Hide password
            editPassword.setTransformationMethod(new PasswordTransformationMethod());
            eyeIcon.setImageResource(R.drawable.ic_eye); // Update icon to "hide"
        } else {
            // Show password
            editPassword.setTransformationMethod(null);
            eyeIcon.setImageResource(R.drawable.ic_eye); // Update icon to "show"
        }
    }

    private void uploadProfileImage() {
        // Here, you can implement the logic to upload the profile image to Firebase Storage
        // and then store the image URL in the Firebase database.
    }
}
