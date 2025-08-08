package com.surya.videos.TrendingVideos.SearchVideos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import com.bumptech.glide.Glide;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.YouTubeActivity;
import com.surya.videos.TrendingVideos.YouTubePlayer_Activity;
import com.surya.videos.TrendingVideos.ThemeManager;
import com.surya.videos.TrendingVideos.FavoritesManager;
import com.surya.videos.TrendingVideos.FavoritesManager.FavoriteVideo;

import java.util.List;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;
    private ProgressBar progressBar;
    private AdView adView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout categoryTabs;
    private ImageButton themeToggle;
    private ThemeManager themeManager;
    private FavoritesManager favoritesManager;
    private String currentCategory = "trending";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate()
        themeManager = new ThemeManager(this);
        if (themeManager.isDarkThemeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        initializeViews();
        setupAdMob();
        setupThemeToggle();
        setupCategoryTabs();
        setupSwipeRefresh();
        setupClickListeners();
        
        handler = new Handler();
        favoritesManager = new FavoritesManager(this);
        
        // Load initial content
        loadCategoryContent(currentCategory);
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.search_input);
        videosFound = findViewById(R.id.videos_found);
        progressBar = findViewById(R.id.progressBar);
        adView = findViewById(R.id.adView_search);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        categoryTabs = findViewById(R.id.category_tabs);
        themeToggle = findViewById(R.id.btn_theme_toggle);
    }

    private void setupAdMob() {
        if (adView != null) {
            try {
                AdSize adSize = getAdSize();
                adView.setAdSize(adSize);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            } catch (Exception e) {
                adView.setVisibility(View.GONE);
            }
        }
    }

    private void setupThemeToggle() {
        themeToggle.setOnClickListener(v -> {
            themeManager.toggleTheme();
            recreate();
        });
    }

    private void setupCategoryTabs() {
        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = getCategoryFromTab(tab.getPosition());
                currentCategory = category;
                loadCategoryContent(category);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadCategoryContent(currentCategory);
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_search).setOnClickListener(v -> performSearch());
        
        videosFound.setOnItemClickListener((parent, view, position, id) -> {
            if (searchResults != null && position < searchResults.size()) {
                Intent intent = new Intent(getApplicationContext(), YouTubePlayer_Activity.class);
                intent.putExtra("VideoId", searchResults.get(position).getId());
                startActivity(intent);
            }
        });
    }

    private String getCategoryFromTab(int position) {
        switch (position) {
            case 0: return "trending";
            case 1: return "music";
            case 2: return "gaming";
            case 3: return "news";
            case 4: return "sports";
            case 5: return "favorites";
            default: return "trending";
        }
    }

    private void loadCategoryContent(String category) {
        if ("favorites".equals(category)) {
            loadFavorites();
        } else {
            searchOnYoutube(category + " videos");
        }
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (!query.isEmpty()) {
            searchOnYoutube(query);
        }
    }

    private void loadFavorites() {
        List<FavoriteVideo> favorites = favoritesManager.getFavorites();
        if (favorites.isEmpty()) {
            showEmptyFavoritesMessage();
        } else {
            convertFavoritesToVideoItems(favorites);
        }
    }

    private void showEmptyFavoritesMessage() {
        searchResults = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, 
            new String[]{getString(R.string.no_favorites)});
        videosFound.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void convertFavoritesToVideoItems(List<FavoriteVideo> favorites) {
        searchResults = new ArrayList<>();
        for (FavoriteVideo favorite : favorites) {
            VideoItem item = new VideoItem();
            item.setId(favorite.getVideoId());
            item.setTitle(favorite.getTitle());
            item.setThumbnailURL(favorite.getThumbnailUrl());
            searchResults.add(item);
        }
        updateVideosFound();
    }

    private void searchOnYoutube(final String keywords) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread() {
            public void run() {
                try {
                    YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
                    searchResults = yc.search(keywords);
                    handler.post(() -> updateVideosFound());
                } catch (Exception e) {
                    handler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(SearchActivity.this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }.start();
    }

    private void updateVideosFound() {
        try {
            if (searchResults == null) {
                searchResults = new ArrayList<>();
            }
            
            ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                    }
                    
                    ImageView thumbnail = convertView.findViewById(R.id.video_thumbnail);
                    TextView title = convertView.findViewById(R.id.video_title);
                    ImageView favoriteIcon = convertView.findViewById(R.id.favorite_icon);

                    VideoItem searchResult = searchResults.get(position);

                    try {
                        if (searchResult.getThumbnailURL() != null) {
                            Glide.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                        }
                        if (searchResult.getTitle() != null) {
                            title.setText(searchResult.getTitle());
                        } else {
                            title.setText("");
                        }
                        
                        // Setup favorite icon
                        setupFavoriteIcon(favoriteIcon, searchResult.getId());
                        
                    } catch (Exception e) {
                        title.setText("");
                    }
                    return convertView;
                }
            };

            videosFound.setAdapter(adapter);
        } catch (Exception e) {
            // Handle adapter creation failure
        } finally {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupFavoriteIcon(ImageView favoriteIcon, String videoId) {
        if (favoritesManager.isFavorite(videoId)) {
            favoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
        }
        
        favoriteIcon.setOnClickListener(v -> {
            if (favoritesManager.isFavorite(videoId)) {
                favoritesManager.removeFromFavorites(videoId);
                favoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
                Toast.makeText(this, R.string.remove_from_favorites, Toast.LENGTH_SHORT).show();
            } else {
                VideoItem video = findVideoById(videoId);
                if (video != null) {
                    FavoriteVideo favorite = new FavoriteVideo(
                        video.getId(), 
                        video.getTitle(), 
                        video.getThumbnailURL(), 
                        ""
                    );
                    favoritesManager.addToFavorites(favorite);
                    favoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(this, R.string.add_to_favorites, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private VideoItem findVideoById(String videoId) {
        for (VideoItem video : searchResults) {
            if (video.getId().equals(videoId)) {
                return video;
            }
        }
        return null;
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private AdSize getAdSize() {
        try {
            android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float density = displayMetrics.density;
            float dpWidth = displayMetrics.widthPixels / density;
            int adWidth = (int) (dpWidth);
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
        } catch (Exception e) {
            return AdSize.BANNER;
        }
    }
}
