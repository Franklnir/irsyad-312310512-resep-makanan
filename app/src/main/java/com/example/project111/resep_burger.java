package com.example.project111;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class resep_burger extends AppCompatActivity {

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resep_burger);

        dbRef = FirebaseDatabase.getInstance().getReference("recipes");

        Map<String, Object> ingredients = new HashMap<>();
        ingredients.put("0", "250g daging sapi cincang");
        ingredients.put("1", "1 butir telur");
        ingredients.put("2", "Garam dan lada secukupnya");
        ingredients.put("3", "4 lembar roti burger");
        ingredients.put("4", "Daun selada");
        ingredients.put("5", "Irisan tomat");
        ingredients.put("6", "Keju slice (opsional)");
        ingredients.put("7", "Saus tomat dan mayones");

        Map<String, Object> steps = new HashMap<>();
        steps.put("0", "Campurkan daging sapi cincang dengan telur, garam, dan lada.");
        steps.put("1", "Bentuk adonan menjadi bulatan pipih seperti patty.");
        steps.put("2", "Panaskan sedikit minyak di wajan, lalu masak patty hingga matang.");
        steps.put("3", "Siapkan roti burger, beri daun selada, irisan tomat, dan patty di atasnya.");
        steps.put("4", "Tambahkan keju slice jika diinginkan.");
        steps.put("5", "Beri saus tomat dan mayones sesuai selera.");
        steps.put("6", "Tutup dengan roti atas dan burger siap disajikan.");

        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", "Burger Sapi Homemade");
        recipe.put("description", "Resep sederhana dengan bahan-bahan rumahan.");
        recipe.put("imageUrl", "https://yourdomain.com/images/burger.jpg");
        recipe.put("author", "chef_budi");
        recipe.put("category", "Fast Food");
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("timestamp", System.currentTimeMillis());

        dbRef.child("burger_001").setValue(recipe);
    }
}
