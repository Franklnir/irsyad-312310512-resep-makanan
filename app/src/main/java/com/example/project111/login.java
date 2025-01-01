package com.example.project111;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnRegister, btnLogin;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users");

        // Check login status
        checkLoginStatus();

        // Login button listener
        btnLogin.setOnClickListener(view -> loginUser());

        // Register button listener
        btnRegister.setOnClickListener(view -> {
            Intent registerIntent = new Intent(getApplicationContext(), register.class);
            startActivity(registerIntent);
        });
    }

    private void checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, redirect to main activity
            startActivity(new Intent(login.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username atau Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbPassword = snapshot.child("password").getValue(String.class);
                    if (dbPassword != null && dbPassword.equals(password)) {
                        // Save login status to SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", username);
                        editor.apply();

                        // Redirect to device activity and pass username
                        Toast.makeText(getApplicationContext(), "Login Berhasil", Toast.LENGTH_SHORT).show();
                        Intent deviceIntent = new Intent(login.this, device.class);
                        deviceIntent.putExtra("username", username); // Kirim username ke device.java
                        startActivity(deviceIntent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Password Salah", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
