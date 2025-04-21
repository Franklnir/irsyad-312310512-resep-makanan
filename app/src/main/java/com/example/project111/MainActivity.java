package com.example.project111;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<Recipe> fullRecipeList = new ArrayList<>();
    private DatabaseReference databaseRef;

    private EditText searchBar;
    private Button camilanButton, kueButton, masakanButton, kulinerButton, minumanButton;
    private ImageView profileSmallIcon;
    private TextView usernameText;

    private Button activeCategoryButton = null;
    private String activeCategory = null;
    private TextWatcher searchWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setupRecyclerView();
        loadUserProfile();
        loadDataFromFirebase();
        setupSearchBar();
        setupCategoryButtons();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.search_bar);
        camilanButton = findViewById(R.id.camilanButton);
        kueButton = findViewById(R.id.KueButton);
        masakanButton = findViewById(R.id.MasakanButton);
        kulinerButton = findViewById(R.id.KulinerButton);
        minumanButton = findViewById(R.id.MinumanButton);
        profileSmallIcon = findViewById(R.id.profileSmallIcon);
        usernameText = findViewById(R.id.usernameText);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new RecipeAdapter(this, recipeList, false);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(recipe -> {
            Intent intent = new Intent(MainActivity.this, DetailRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        String photoUrl = prefs.getString("profileImage", null);

        usernameText.setText("Halo, " + username);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_eye)
                    .error(R.drawable.ic_eye)
                    .circleCrop()
                    .into(profileSmallIcon);
        } else {
            profileSmallIcon.setImageResource(R.drawable.ic_eye);
        }

        profileSmallIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadDataFromFirebase() {
        databaseRef = FirebaseDatabase.getInstance().getReference("recipes");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                fullRecipeList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                        fullRecipeList.add(recipe);
                    }
                }
                adapter.updateList(new ArrayList<>(recipeList));

                recyclerView.postDelayed(() -> {
                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(MainActivity.this, R.anim.layout_animation_fall_down);
                    recyclerView.setLayoutAnimation(animation);
                    recyclerView.scheduleLayoutAnimation();
                    adapter.notifyDataSetChanged();
                }, 100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error handling
            }
        });
    }

    private void setupSearchBar() {
        searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    resetFilter();
                } else {
                    filterRecipesByName(query);
                }
            }
        };
        searchBar.addTextChangedListener(searchWatcher);
    }

    private void setupCategoryButtons() {
        camilanButton.setOnClickListener(v -> handleCategoryClick(camilanButton, "Camilan"));
        kueButton.setOnClickListener(v -> handleCategoryClick(kueButton, "Kue"));
        masakanButton.setOnClickListener(v -> handleCategoryClick(masakanButton, "Masakan"));
        kulinerButton.setOnClickListener(v -> handleCategoryClick(kulinerButton, "Kuliner"));
        minumanButton.setOnClickListener(v -> handleCategoryClick(minumanButton, "Minuman"));
    }

    private void handleCategoryClick(Button button, String category) {
        if (activeCategory != null && activeCategory.equalsIgnoreCase(category)) {
            resetFilter();
            searchBar.removeTextChangedListener(searchWatcher);
            searchBar.setText("");
            searchBar.addTextChangedListener(searchWatcher);
        } else {
            filterByCategory(button, category);
            searchBar.removeTextChangedListener(searchWatcher);
            searchBar.setText("");
            searchBar.addTextChangedListener(searchWatcher);
        }
    }

    private void filterRecipesByName(String query) {
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe recipe : fullRecipeList) {
            if (recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(recipe);
            }
        }
        adapter.updateList(filtered);
        clearActiveCategory();
    }

    private void filterByCategory(Button button, String category) {
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe recipe : fullRecipeList) {
            if (recipe.getCategory().equalsIgnoreCase(category)) {
                filtered.add(recipe);
            }
        }
        adapter.updateList(filtered);
        setActiveCategory(button, category);
    }

    private void resetFilter() {
        adapter.updateList(new ArrayList<>(fullRecipeList));
        clearActiveCategory();
    }

    private void setActiveCategory(Button button, String category) {
        if (activeCategoryButton != null) {
            activeCategoryButton.setBackgroundColor(getResources().getColor(R.color.default_category_color));
        }
        activeCategoryButton = button;
        activeCategory = category;
        activeCategoryButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
    }

    private void clearActiveCategory() {
        if (activeCategoryButton != null) {
            activeCategoryButton.setBackgroundColor(getResources().getColor(R.color.default_category_color));
            activeCategoryButton = null;
        }
        activeCategory = null;
    }
}
