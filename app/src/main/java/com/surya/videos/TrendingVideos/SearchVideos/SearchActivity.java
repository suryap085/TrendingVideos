package com.surya.videos.TrendingVideos.SearchVideos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.surya.videos.TrendingVideos.utils.FavoritesManager;
import com.surya.videos.TrendingVideos.search.SearchHistoryManager;
import com.surya.videos.TrendingVideos.database.entity.SearchQuery;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.surya.videos.TrendingVideos.search.TrendingSuggestionsProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import androidx.lifecycle.LiveDataWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SearchActivity extends AppCompatActivity {
    private static final int REQ_VOICE = 5011;

    private EditText searchInput;
    private ListView videosFound;
    private Handler handler;
    private List<VideoItem> searchResults;
    private ProgressBar progressBar;
    private AdView adView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout categoryTabs;
    private ImageButton themeToggle;
    private ImageButton voiceBtn;
    private ThemeManager themeManager;
    private FavoritesManager favoritesManager;
    private SearchHistoryManager historyManager;
    private String currentCategory = "trending";

    private FrameLayout suggestionsContainer;
    private View suggestionsView;
    private ArrayAdapter<String> suggestionsAdapter;
    private List<String> suggestionItems = new ArrayList<>();
    private static final String HEADER_TRENDING = "— Trending searches —";
    private static final String HEADER_RECENT = "— Recent searches —";
    private TrendingSuggestionsProvider trendingProvider;
    private List<String> trendingDefaults = new ArrayList<>();
    private static final String PREFS = "search_prefs";
    private static final String KEY_TRENDING_CHIPS = "trending_chips_enabled";
    private SharedPreferences prefs;
    private boolean trendingChipsEnabled;

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
        
        prefs = getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        trendingChipsEnabled = prefs.getBoolean(KEY_TRENDING_CHIPS, true);

        initializeViews();
        setupAdMob();
        setupThemeToggle();
        setupCategoryTabs();
        setupSwipeRefresh();
        setupClickListeners();
        setupSuggestions();
        
        handler = new Handler();
        favoritesManager = FavoritesManager.getInstance(this);
        historyManager = SearchHistoryManager.getInstance(this);
        trendingProvider = new TrendingSuggestionsProvider();
        trendingProvider.attachCache(new com.surya.videos.TrendingVideos.search.TrendingSuggestionsCache(this));
        fetchTrendingDefaults();
        
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
        voiceBtn = findViewById(R.id.btn_voice);
        suggestionsContainer = findViewById(R.id.suggestions_container);
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
                if ("trending".equals(category)) {
                    showTrendingFragment();
                } else {
                    hideTrendingFragment();
                    loadCategoryContent(category);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        // Ensure default
        showTrendingFragment();
    }

    private void showTrendingFragment() {
        // Hide list and show fragment container
        if (findViewById(R.id.trending_container) == null) {
            // Inject a container above SwipeRefreshLayout
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ViewGroup decor = (ViewGroup) root.getChildAt(0);
            // Add a container just above the SwipeRefreshLayout (index 3 after search bar and suggestions and tabs)
            android.widget.FrameLayout container = new android.widget.FrameLayout(this);
            container.setId(R.id.trending_container);
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            // Insert before SwipeRefreshLayout in the layout hierarchy
            View swipe = findViewById(R.id.swipeRefreshLayout);
            if (swipe != null && decor instanceof ViewGroup) {
                int idx = ((ViewGroup) decor).indexOfChild(swipe);
                ((ViewGroup) decor).addView(container, idx);
            } else {
                ((ViewGroup) decor).addView(container);
            }
        }
        videosFound.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        Fragment f = TrendingFragment.newInstance(java.util.Locale.getDefault().getCountry());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.trending_container, f, "trending");
        ft.commitAllowingStateLoss();
    }

    private void hideTrendingFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("trending");
        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.remove(f).commitAllowingStateLoss();
        }
        View container = findViewById(R.id.trending_container);
        if (container != null && container.getParent() instanceof ViewGroup) {
            ((ViewGroup) container.getParent()).removeView(container);
        }
        videosFound.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadCategoryContent(currentCategory);
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_search).setOnClickListener(v -> performSearch());
        voiceBtn.setOnClickListener(v -> startVoice());
        
        videosFound.setOnItemClickListener((parent, view, position, id) -> {
            if (searchResults != null && position < searchResults.size()) {
                Intent intent = new Intent(getApplicationContext(), YouTubePlayer_Activity.class);
                intent.putExtra("VideoId", searchResults.get(position).getId());
                startActivity(intent);
            }
        });

        searchInput.setOnFocusChangeListener((v, hasFocus) -> toggleSuggestions(hasFocus));

        // Live suggestions as user types
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    showRecentSuggestions();
                } else {
                    showPrefixSuggestions(s.toString());
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupSuggestions() {
        suggestionsView = LayoutInflater.from(this).inflate(R.layout.search_history_suggestions, suggestionsContainer, false);
        suggestionsContainer.addView(suggestionsView);

        ListView list = suggestionsView.findViewById(R.id.list_suggestions);
        View footer = LayoutInflater.from(this).inflate(R.layout.view_clear_history_footer, list, false);
        list.addFooterView(footer, null, true);
        suggestionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suggestionItems) {
            @Override
            public boolean isEnabled(int position) {
                String item = suggestionItems.get(position);
                return !(HEADER_TRENDING.equals(item) || HEADER_RECENT.equals(item));
            }
        };
        list.setAdapter(suggestionsAdapter);
        footer.findViewById(R.id.btn_clear_history).setOnClickListener(v -> historyManager.clearAll());

        list.setOnItemClickListener((parent, view, position, id) -> {
            if (position < suggestionItems.size()) {
                String q = suggestionItems.get(position);
                if (HEADER_TRENDING.equals(q) || HEADER_RECENT.equals(q)) return;
                searchInput.setText(q);
                searchInput.setSelection(q.length());
                performSearch();
                toggleSuggestions(false);
            }
        });

        list.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < suggestionItems.size()) {
                String q = suggestionItems.get(position);
                if (!HEADER_TRENDING.equals(q) && !HEADER_RECENT.equals(q)) {
                    historyManager.delete(q);
                    Toast.makeText(this, "Removed from history", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            return false;
        });

        refreshCombinedSuggestions("");
    }

    private void toggleSuggestions(boolean show) {
        suggestionsContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) refreshCombinedSuggestions(searchInput.getText().toString());
    }

    private void showRecentSuggestions() {
        refreshCombinedSuggestions("");
    }

    private void showPrefixSuggestions(String prefix) {
        refreshCombinedSuggestions(prefix);
    }

    private void refreshCombinedSuggestions(String prefix) {
        suggestionItems.clear();
        suggestionsAdapter.notifyDataSetChanged();

        // Trending from server/fallback first
        if (!trendingDefaults.isEmpty()) {
            suggestionItems.add(HEADER_TRENDING);
            for (String s : trendingDefaults) {
                if (prefix.isEmpty() || s.toLowerCase().startsWith(prefix.toLowerCase())) {
                    suggestionItems.add(s);
                }
            }
            suggestionsAdapter.notifyDataSetChanged();
        }

        // User Top searches (reinforces trending)
        LiveDataWrapper.observeOnce(this, historyManager.getTop(5), top -> {
            if (top != null && !top.isEmpty()) {
                if (!suggestionItems.contains(HEADER_TRENDING)) suggestionItems.add(HEADER_TRENDING);
                for (SearchQuery q : top) {
                    String s = q.getQuery();
                    if (prefix.isEmpty() || s.toLowerCase().startsWith(prefix.toLowerCase())) {
                        if (!suggestionItems.contains(s)) suggestionItems.add(s);
                    }
                }
                suggestionsAdapter.notifyDataSetChanged();
            }
        });

        // Recent
        LiveDataWrapper.observeOnce(this, historyManager.getRecent(10), recent -> {
            boolean any = false;
            if (recent != null && !recent.isEmpty()) {
                List<String> recents = new ArrayList<>();
                for (SearchQuery q : recent) {
                    String s = q.getQuery();
                    if (prefix.isEmpty() || s.toLowerCase().startsWith(prefix.toLowerCase())) {
                        if (!recents.contains(s) && !suggestionItems.contains(s)) {
                            recents.add(s);
                        }
                    }
                }
                if (!recents.isEmpty()) {
                    suggestionItems.add(HEADER_RECENT);
                    suggestionItems.addAll(recents);
                    any = true;
                }
            }
            if (!any && suggestionItems.isEmpty()) {
                suggestionItems.add("No history yet");
            }
            suggestionsAdapter.notifyDataSetChanged();
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
            historyManager.record(query);
            searchOnYoutube(query);
        }
    }

    private void loadFavorites() {
        // For compatibility with legacy FavoritesManager API in this module, we just hide list if empty
        // and show an adapter with empty state otherwise. New Room-based favorites screen is separate.
        searchResults = new ArrayList<>();
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
                        
                        // Setup favorite icon (legacy compatibility: no-op if not integrated)
                        favoriteIcon.setVisibility(View.GONE);
                        
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

    private void startVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.search_hint));
        try {
            startActivityForResult(intent, REQ_VOICE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String q = results.get(0);
                searchInput.setText(q);
                searchInput.setSelection(q.length());
                performSearch();
            }
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_toggle_trending);
        if (item != null) item.setTitle(trendingChipsEnabled ? "Hide trending chips" : "Show trending chips");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_trending) {
            trendingChipsEnabled = !trendingChipsEnabled;
            prefs.edit().putBoolean(KEY_TRENDING_CHIPS, trendingChipsEnabled).apply();
            invalidateOptionsMenu();
            if (!trendingChipsEnabled) removeQuickTrendingChips(); else showQuickTrendingChips();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        if (trendingChipsEnabled && suggestionsContainer.getVisibility() != View.VISIBLE) {
            showQuickTrendingChips();
        } else {
            removeQuickTrendingChips();
        }
    }

    private void fetchTrendingDefaults() {
        trendingProvider.getTrending(6, list -> {
            trendingDefaults.clear();
            trendingDefaults.addAll(list);
        });
    }

    private void showQuickTrendingChips() {
        // Inflate a simple chip row above the tabs for quick discovery (ephemeral)
        if (trendingDefaults.isEmpty()) return;
        // Create a lightweight ChipGroup container programmatically and insert temporarily
        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setSingleLine(true);
        chipGroup.setPadding(16, 8, 16, 8);
        for (String s : trendingDefaults) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> {
                searchInput.setText(s);
                searchInput.setSelection(s.length());
                performSearch();
            });
            chipGroup.addView(chip);
        }
        // Ensure only one is present: remove previous if any
        View maybeOld = findViewById(0x7f0a0abc); // ephemeral id unlikely, best-effort cleanup
        if (maybeOld != null) ((ViewGroup) maybeOld.getParent()).removeView(maybeOld);
        chipGroup.setId(0x7f0a0abc);
        // Insert right below search bar (index 1, before suggestions container)
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        ViewGroup decor = (ViewGroup) root.getChildAt(0);
        if (decor instanceof ViewGroup) {
            ViewGroup topLayout = (ViewGroup) decor;
            // Add after the search bar
            topLayout.addView(chipGroup, 1);
        }
    }

    private void removeQuickTrendingChips() {
        View maybeOld = findViewById(0x7f0a0abc);
        if (maybeOld != null && maybeOld.getParent() instanceof ViewGroup) {
            ((ViewGroup) maybeOld.getParent()).removeView(maybeOld);
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
