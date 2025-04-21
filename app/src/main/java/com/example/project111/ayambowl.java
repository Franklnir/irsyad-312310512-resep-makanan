package com.example.project111;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ayambowl extends AppCompatActivity {

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayambowl);

        dbRef = FirebaseDatabase.getInstance().getReference("recipes");
        uploadRecipeToFirebase();
    }

    private void uploadRecipeToFirebase() {
        Map<String, Object> ingredients = new HashMap<>();
        ingredients.put("0", "200 gram dada ayam, potong dadu");
        ingredients.put("1", "2 sdm minyak zaitun");
        ingredients.put("2", "1 sdm kecap asin");
        ingredients.put("3", "1 sdm madu");
        ingredients.put("4", "1 sdt jahe parut");
        ingredients.put("5", "1 siung bawang putih, cincang halus");
        ingredients.put("6", "1/2 sdt merica hitam");
        ingredients.put("7", "1 sdt garam");
        ingredients.put("8", "1/2 sdt kaldu ayam bubuk");
        ingredients.put("9", "200 gram nasi putih");
        ingredients.put("10", "1 sdt minyak wijen");
        ingredients.put("11", "1 batang daun bawang, iris tipis");

        Map<String, Object> steps = new HashMap<>();
        steps.put("0", "Panaskan minyak zaitun dalam wajan, tumis bawang putih dan jahe hingga harum.");
        steps.put("1", "Masukkan ayam, tambahkan kecap asin, madu, merica, garam, dan kaldu ayam.");
        steps.put("2", "Masak ayam hingga matang dan berwarna kecokelatan.");
        steps.put("3", "Masak nasi, tambahkan minyak wijen dan daun bawang, aduk rata.");
        steps.put("4", "Sajikan ayam di atas nasi, bisa ditambah sayuran.");

        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", "Ayam Bowl");
        recipe.put("description", "Resep ayam bowl ala restoran yang praktis dan lezat.");
        recipe.put("imageUrl", "https://yourdomain.com/images/ayambowl.jpg"); // Ganti sesuai lokasi gambarnya
        recipe.put("author", "chef_ayu");
        recipe.put("category", "Rice Bowl");
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("timestamp", System.currentTimeMillis());

        dbRef.child("ayambowl_001").setValue(recipe);
    }
}
