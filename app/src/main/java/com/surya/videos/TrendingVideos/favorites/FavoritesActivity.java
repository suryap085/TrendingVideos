package com.surya.videos.TrendingVideos.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.YouTubePlayer_Activity;
import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private FavoritesViewModel viewModel;
    private TextView emptyStateText;
    private ChipGroup categoryChipGroup;
    
    private List<FavoriteVideo> allFavorites = new ArrayList<>();
    private String selectedCategory = "All";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupViewModel();
        setupCategoryFilter();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.favorites_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        categoryChipGroup = findViewById(R.id.category_chip_group);
    }
    
    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(favoriteVideo -> {
            Intent intent = new Intent(this, YouTubePlayer_Activity.class);
            intent.putExtra("video_id", favoriteVideo.getVideoId());
            intent.putExtra("video_title", favoriteVideo.getTitle());
            startActivity(intent);
        });
        
        adapter.setOnFavoriteClickListener(favoriteVideo -> {
            viewModel.removeFromFavorites(favoriteVideo);
        });
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        
        viewModel.getAllFavorites().observe(this, favorites -> {
            allFavorites = favorites;
            filterFavoritesByCategory(selectedCategory);
            updateEmptyState();
        });
    }
    
    private void setupCategoryFilter() {
        // Add "All" category chip
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        categoryChipGroup.addView(allChip);
        
        // Add category chips
        String[] categories = {"Music", "Gaming", "Sports", "News", "Entertainment", "Education"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            categoryChipGroup.addView(chip);
        }
        
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                Chip checkedChip = findViewById(checkedIds.get(0));
                selectedCategory = checkedChip.getText().toString();
                filterFavoritesByCategory(selectedCategory);
            }
        });
    }
    
    private void filterFavoritesByCategory(String category) {
        List<FavoriteVideo> filteredList = new ArrayList<>();
        
        if ("All".equals(category)) {
            filteredList.addAll(allFavorites);
        } else {
            for (FavoriteVideo video : allFavorites) {
                if (category.equals(video.getCategory())) {
                    filteredList.add(video);
                }
            }
        }
        
        adapter.updateFavorites(filteredList);
    }
    
    private void updateEmptyState() {
        if (allFavorites.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            categoryChipGroup.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            categoryChipGroup.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_all) {
            viewModel.clearAllFavorites();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
