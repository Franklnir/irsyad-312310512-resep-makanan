package com.example.project111;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {

    private EditText etUsername, etPassword, etKonfirmasi, etNomorHp;
    private Button btnRegister;

    private DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etusername);
        etPassword = findViewById(R.id.etpassword);
        etKonfirmasi = findViewById(R.id.etkonfirmasi);
        etNomorHp = findViewById(R.id.etnomorhp);
        btnRegister = findViewById(R.id.btnregister);

        database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://project111-aa20b-default-rtdb.firebaseio.com/");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String konfirmasi = etKonfirmasi.getText().toString().trim();
                String nomorHp = etNomorHp.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || konfirmasi.isEmpty() || nomorHp.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ada Data Yang Masih Kosong!!", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(konfirmasi)) {
                    Toast.makeText(getApplicationContext(), "Konfirmasi Password Tidak Sesuai!!", Toast.LENGTH_SHORT).show();
                } else {
                    database = FirebaseDatabase.getInstance().getReference("users");
                    database.child(username).child("username").setValue(username);
                    database.child(username).child("password").setValue(password);
                    database.child(username).child("nomorHp").setValue(nomorHp);

                    Toast.makeText(getApplicationContext(), "Register Berhasil", Toast.LENGTH_SHORT).show();
                    Intent register = new Intent(getApplicationContext(), login.class);
                    startActivity(register);
                }
            }
        });
    }
}
