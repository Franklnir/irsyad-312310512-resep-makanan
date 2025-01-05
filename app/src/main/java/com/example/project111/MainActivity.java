package com.example.project111;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText searchBar;
    private LinearLayout burgerItem, rendangItem, susuGandumItem, rotiGandumItem, satePadangItem, nasiGorengItem, dimsumItem, smoothieItem, ayamBowlItem, tumisItem, capcayItem, streetBobaItem, boluLapisItem, bakpaoItem;
    private Button spicyButton, healthyButton, veganButton, drinkButton;
    private ScrollView scrollView;
    private int scrollPosition = 0; // Store scroll position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // If not logged in, go to the login activity
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
            return; // Stop further execution of the code if user is not logged in
        }

        // Initialize views and other components
        searchBar = findViewById(R.id.search_bar);
        burgerItem = findViewById(R.id.foodItemBurger);
        rendangItem = findViewById(R.id.foodItemRendang);
        susuGandumItem = findViewById(R.id.foodItemSusuGandum);
        rotiGandumItem = findViewById(R.id.foodItemRotiGandum);
        satePadangItem = findViewById(R.id.foodItemSatePadang);
        nasiGorengItem = findViewById(R.id.foodItemNasiGoreng);
        dimsumItem = findViewById(R.id.foodItemDimsum);
        smoothieItem = findViewById(R.id.foodItemSmoothie);
        ayamBowlItem = findViewById(R.id.foodItemAyamBowl);
        tumisItem = findViewById(R.id.foodItemTumis);
        capcayItem = findViewById(R.id.foodItemCapcay); // Added Capcay item
        streetBobaItem = findViewById(R.id.foodItemStreetBoba); // Added StreetBoba item
        boluLapisItem = findViewById(R.id.foodItemBoluLapis); // Added BoluLapis item
        bakpaoItem = findViewById(R.id.foodItemBakpao); // Added Bakpao item
        spicyButton = findViewById(R.id.spicyButton);
        healthyButton = findViewById(R.id.healthyButton);
        veganButton = findViewById(R.id.veganButton);
        drinkButton = findViewById(R.id.drinkButton);
        scrollView = findViewById(R.id.foodContainerScrollView);

        // Setup search, filters, and item click listeners
        setupSearchFunctionality();
        setupButtonFilters();
        setupItemClickListeners();

        // Profile icon click listener
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, profile.class);
            startActivity(intent);
        });
    }

    private void setupSearchFunctionality() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().toLowerCase();
                filterItemsBySearch(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void filterItemsBySearch(String query) {
        saveScrollPosition();

        // Membuat list rekomendasi berdasarkan pencarian
        boolean hasMatch = false;

        burgerItem.setVisibility(query.contains("burger") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("burger");

        rendangItem.setVisibility(query.contains("rendang") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("rendang");

        susuGandumItem.setVisibility(query.contains("susu") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("susu gandum");

        rotiGandumItem.setVisibility(query.contains("roti") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("roti gandum");

        satePadangItem.setVisibility(query.contains("sate") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("sate padang");

        nasiGorengItem.setVisibility(query.contains("nasi") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("nasi goreng");

        dimsumItem.setVisibility(query.contains("dimsum") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("dimsum jamur");

        smoothieItem.setVisibility(query.contains("smoothie") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("smoothie");

        ayamBowlItem.setVisibility(query.contains("ayam") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("ayam bowl");

        tumisItem.setVisibility(query.contains("tumis") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("tumis");

        capcayItem.setVisibility(query.contains("capcay") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("capcay");

        streetBobaItem.setVisibility(query.contains("boba") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("street boba");

        boluLapisItem.setVisibility(query.contains("bolu") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("bolu lapis");

        bakpaoItem.setVisibility(query.contains("bakpao") ? View.VISIBLE : View.GONE);
        hasMatch = hasMatch || query.contains("bakpao");

        // Menampilkan rekomendasi jika tidak ada hasil pencarian
        if (!hasMatch && !query.isEmpty()) {

        } else if (query.isEmpty()) {
            // Reset jika query kosong
            resetAllItems();
        }

        restoreScrollPosition();
    }



    private void setupButtonFilters() {
        spicyButton.setOnClickListener(v -> {
            saveScrollPosition();
            filterItems("burger");
            satePadangItem.setVisibility(View.VISIBLE);
            nasiGorengItem.setVisibility(View.VISIBLE);
            ayamBowlItem.setVisibility(View.VISIBLE);
            boluLapisItem.setVisibility(View.VISIBLE); // Show BoluLapis in spicy
            bakpaoItem.setVisibility(View.VISIBLE); // Show Bakpao in spicy
            restoreScrollPosition();
        });
        healthyButton.setOnClickListener(v -> {
            saveScrollPosition();
            filterItems("susu gandum");
            tumisItem.setVisibility(View.VISIBLE);
            capcayItem.setVisibility(View.VISIBLE); // Show Capcay in healthy
            restoreScrollPosition();
        });
        veganButton.setOnClickListener(v -> {
            saveScrollPosition();
            filterItems("roti gandum");
            dimsumItem.setVisibility(View.VISIBLE);
            capcayItem.setVisibility(View.VISIBLE); // Show Capcay in vegan
            restoreScrollPosition();
        });
        drinkButton.setOnClickListener(v -> {
            saveScrollPosition();
            filterItems("susu gandum");
            smoothieItem.setVisibility(View.VISIBLE);
            streetBobaItem.setVisibility(View.VISIBLE); // Show StreetBoba in drink
            restoreScrollPosition();
        });
    }

    private void filterItems(String filter) {
        burgerItem.setVisibility(filter.equals("burger") ? View.VISIBLE : View.GONE);
        rendangItem.setVisibility(filter.equals("rendang") ? View.VISIBLE : View.GONE);
        susuGandumItem.setVisibility(filter.equals("susu gandum") ? View.VISIBLE : View.GONE);
        rotiGandumItem.setVisibility(filter.equals("roti gandum") ? View.VISIBLE : View.GONE);
        satePadangItem.setVisibility(filter.equals("sate padang") ? View.VISIBLE : View.GONE);
        nasiGorengItem.setVisibility(filter.equals("nasi goreng") ? View.VISIBLE : View.GONE);
        dimsumItem.setVisibility(filter.equals("dimsum") ? View.VISIBLE : View.GONE);
        smoothieItem.setVisibility(filter.equals("smoothie") ? View.VISIBLE : View.GONE);
        ayamBowlItem.setVisibility(filter.equals("ayam bowl") ? View.VISIBLE : View.GONE);
        tumisItem.setVisibility(filter.equals("tumis") ? View.VISIBLE : View.GONE);
        capcayItem.setVisibility(filter.equals("capcay") ? View.VISIBLE : View.GONE);
        streetBobaItem.setVisibility(filter.equals("street boba") ? View.VISIBLE : View.GONE);
        boluLapisItem.setVisibility(filter.equals("bolu lapis") ? View.VISIBLE : View.GONE);
        bakpaoItem.setVisibility(filter.equals("bakpao") ? View.VISIBLE : View.GONE);

        LinearLayout foodContainer = findViewById(R.id.foodContainer);
        foodContainer.requestLayout();
    }

    private void saveScrollPosition() {
        scrollPosition = scrollView.getScrollY();
    }

    private void restoreScrollPosition() {
        scrollView.post(() -> scrollView.scrollTo(0, scrollPosition));
    }

    private void setupItemClickListeners() {
        burgerItem.setOnClickListener(v -> openRecipeActivity(resep_burger.class));
        rendangItem.setOnClickListener(v -> openRecipeActivity(resep_rendang.class));
        susuGandumItem.setOnClickListener(v -> openRecipeActivity(susu_gandum.class));
        rotiGandumItem.setOnClickListener(v -> openRecipeActivity(rotigandum.class));
        satePadangItem.setOnClickListener(v -> openRecipeActivity(satepadang.class));
        nasiGorengItem.setOnClickListener(v -> openRecipeActivity(nasigoreng.class));
        dimsumItem.setOnClickListener(v -> openRecipeActivity(dimsum.class));
        smoothieItem.setOnClickListener(v -> openRecipeActivity(smoothie.class));
        ayamBowlItem.setOnClickListener(v -> openRecipeActivity(ayambowl.class));
        tumisItem.setOnClickListener(v -> openRecipeActivity(tumis.class));
        capcayItem.setOnClickListener(v -> openRecipeActivity(capcay.class)); // Added Capcay item listener
        streetBobaItem.setOnClickListener(v -> openRecipeActivity(streetboba.class)); // Added StreetBoba item listener
        boluLapisItem.setOnClickListener(v -> openRecipeActivity(bolulapis.class)); // Added BoluLapis item listener
        bakpaoItem.setOnClickListener(v -> openRecipeActivity(bakpao.class)); // Added Bakpao item listener
    }

    private void openRecipeActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (!searchBar.getText().toString().isEmpty() || isFiltered()) {
            // Reset tampilan ke awal tanpa dialog
            resetAllItems();
        } else {
            // Konfirmasi keluar aplikasi
            new AlertDialog.Builder(this)
                    .setMessage("Yah, ingin keluar ya?")
                    .setPositiveButton("Iya", (dialog, which) -> finish())
                    .setNegativeButton("Tidak", null)
                    .show();
        }
    }

    // Fungsi untuk memeriksa apakah tampilan sedang difilter
    private boolean isFiltered() {
        return burgerItem.getVisibility() == View.GONE ||
                rendangItem.getVisibility() == View.GONE ||
                susuGandumItem.getVisibility() == View.GONE ||
                rotiGandumItem.getVisibility() == View.GONE ||
                satePadangItem.getVisibility() == View.GONE ||
                nasiGorengItem.getVisibility() == View.GONE ||
                dimsumItem.getVisibility() == View.GONE ||
                smoothieItem.getVisibility() == View.GONE ||
                ayamBowlItem.getVisibility() == View.GONE ||
                tumisItem.getVisibility() == View.GONE ||
                capcayItem.getVisibility() == View.GONE ||
                streetBobaItem.getVisibility() == View.GONE ||
                boluLapisItem.getVisibility() == View.GONE ||
                bakpaoItem.getVisibility() == View.GONE;
    }

    // Fungsi untuk mereset tampilan ke kondisi awal
    private void resetAllItems() {
        burgerItem.setVisibility(View.VISIBLE);
        rendangItem.setVisibility(View.VISIBLE);
        susuGandumItem.setVisibility(View.VISIBLE);
        rotiGandumItem.setVisibility(View.VISIBLE);
        satePadangItem.setVisibility(View.VISIBLE);
        nasiGorengItem.setVisibility(View.VISIBLE);
        dimsumItem.setVisibility(View.VISIBLE);
        smoothieItem.setVisibility(View.VISIBLE);
        ayamBowlItem.setVisibility(View.VISIBLE);
        tumisItem.setVisibility(View.VISIBLE);
        capcayItem.setVisibility(View.VISIBLE);
        streetBobaItem.setVisibility(View.VISIBLE);
        boluLapisItem.setVisibility(View.VISIBLE);
        bakpaoItem.setVisibility(View.VISIBLE);

    }
}
