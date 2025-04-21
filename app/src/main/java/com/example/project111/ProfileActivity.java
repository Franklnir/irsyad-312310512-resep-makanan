package com.example.project111;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String SUPABASE_STORAGE_URL = "https://zwyjincljcwqjyagzwci.supabase.co/storage/v1/object";
    private static final String SUPABASE_BUCKET = "project111";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp3eWppbmNsamN3cWp5YWd6d2NpIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTQyMTM5MSwiZXhwIjoyMDYwOTk3MzkxfQ.974CVHyzfxfqloQxLUg8AVZJT_gNWYPGjrSHOA2FqSQ"; // WARNING: Don't expose sensitive data in production

    private ImageView profileImage;
    private EditText editUsername, editPassword, editPhone;
    private Button btnSave, btnLogout, btnHelp, btnRequest;

    private Uri imageUri;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference databaseReference;
    private OkHttpClient httpClient = new OkHttpClient();

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        initializeFirebase();
        loadUserData();
        setupButtonActions();
        setupImagePickerLauncher();
        setupMenuButton();  // Call the setupMenuButton method
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        editPhone = findViewById(R.id.edit_phone);
        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);
        btnHelp = findViewById(R.id.btn_help);
        btnRequest = findViewById(R.id.btn_request);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    private void loadUserData() {
        String username = sharedPreferences.getString("username", ""); // ambil username dari lokal untuk cari di database

        if (username.isEmpty()) {
            Toast.makeText(this, "Username tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fetchedUsername = snapshot.child("username").getValue(String.class);
                    String fetchedPassword = snapshot.child("password").getValue(String.class);
                    String fetchedNomorHp = snapshot.child("nomorHp").getValue(String.class);
                    String fetchedProfileImage = snapshot.child("profileImage").getValue(String.class);

                    editUsername.setText(fetchedUsername != null ? fetchedUsername : "");
                    editPassword.setText(fetchedPassword != null ? fetchedPassword : "");
                    editPhone.setText(fetchedNomorHp != null ? fetchedNomorHp : "");

                    if (fetchedProfileImage != null && !fetchedProfileImage.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(fetchedProfileImage).into(profileImage);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Data user tidak ditemukan di database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonActions() {
        profileImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnHelp.setOnClickListener(v -> openWhatsApp("Halo, saya butuh bantuan."));
        btnRequest.setOnClickListener(v -> openWhatsApp("Halo, ini info device saya."));
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profileImage.setImageURI(imageUri);
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        saveTextData(); // Simpan data teks ke SharedPreferences dulu

        if (imageUri != null) {
            uploadImageAndSaveProfile();
        } else {
            // Kalau tidak ada gambar baru, pakai URL lama
            String existingImageUrl = sharedPreferences.getString("profileImage", "");
            saveUserData(existingImageUrl);
            Toast.makeText(this, "Profil disimpan tanpa ubah foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTextData() {
        editor.putString("username", editUsername.getText().toString().trim());
        editor.putString("password", editPassword.getText().toString().trim());
        editor.putString("nomorHp", editPhone.getText().toString().trim());
        editor.apply();
    }

    private void uploadImageAndSaveProfile() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah gambar...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) throw new IOException("Gagal membuka file.");

            String fileExtension = getFileExtension(imageUri);
            File tempFile = File.createTempFile("upload_", "." + fileExtension, getCacheDir());

            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            String filename = UUID.randomUUID().toString() + "." + fileExtension;
            MediaType mediaType = MediaType.parse(getContentResolver().getType(imageUri));
            if (mediaType == null) mediaType = MediaType.parse("image/*");

            RequestBody fileBody = RequestBody.create(tempFile, mediaType);

            Request request = new Request.Builder()
                    .url(SUPABASE_STORAGE_URL + "/" + SUPABASE_BUCKET + "/" + filename)
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .put(fileBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Gagal upload gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        String existingImageUrl = sharedPreferences.getString("profileImage", "");
                        saveUserData(existingImageUrl);
                        Toast.makeText(ProfileActivity.this, "Profil disimpan tanpa update foto.", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        String imageUrl = "https://zwyjincljcwqjyagzwci.supabase.co/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + filename;

                        editor.putString("profileImage", imageUrl);
                        editor.apply();

                        saveUserData(imageUrl);

                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Foto berhasil diunggah dan profil disimpan!", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Upload gagal: " + response.message(), Toast.LENGTH_LONG).show();
                            String existingImageUrl = sharedPreferences.getString("profileImage", "");
                            saveUserData(existingImageUrl);
                            Toast.makeText(ProfileActivity.this, "Profil disimpan tanpa update foto.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error saat upload gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            String existingImageUrl = sharedPreferences.getString("profileImage", "");
            saveUserData(existingImageUrl);
            Toast.makeText(this, "Profil disimpan tanpa update foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData(String imageUrl) {
        HashMap<String, Object> userMap = new HashMap<>();
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        String nomorHp = sharedPreferences.getString("nomorHp", "");
        String deviceInfo = "Model: " + Build.MODEL + " Brand: " + Build.BRAND + " Product: " + Build.PRODUCT + " Android: " + Build.VERSION.RELEASE;

        userMap.put("username", username);
        userMap.put("password", password);
        userMap.put("nomorHp", nomorHp);
        userMap.put("profileImage", imageUrl);
        userMap.put("device_info", deviceInfo);
        userMap.put("userId", username);

        databaseReference.child(username).setValue(userMap);
    }

    private void openWhatsApp(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp tidak terinstall.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        editor.clear();
        editor.apply();
        Toast.makeText(this, "Logout berhasil.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, login.class));
        finish();
    }

    private String getFileExtension(Uri uri) {
        if (uri != null) {
            String extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(getContentResolver().getType(uri));
            return extension != null ? extension : "jpg"; // Default ke jpg kalau gagal baca
        }
        return "jpg";
    }

    // Adding the new method for menu buttons
    private void setupMenuButton() {
        Button itemVerification = findViewById(R.id.itemVerification);
        Button itemRiwayat = findViewById(R.id.itemRiwayat);
        Button itemFavorit = findViewById(R.id.itemFavorit);
        Button itemBantuan = findViewById(R.id.itemBantuan);

        itemVerification.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, VerifikasiActivity.class);
            startActivity(intent);
        });

        itemRiwayat.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RiwayatActivity.class);
            startActivity(intent);
        });

        itemFavorit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavoriteRecipesActivity.class);
            startActivity(intent);
        });

        itemBantuan.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, BantuanActivity.class);
            startActivity(intent);
        });
    }
}
