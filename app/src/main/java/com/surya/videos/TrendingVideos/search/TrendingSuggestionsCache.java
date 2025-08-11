package com.surya.videos.TrendingVideos.search;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrendingSuggestionsCache {

    private static final String PREFS = "search_prefs";
    private static final String KEY_TRENDING = "trending_suggestions";

    private final SharedPreferences prefs;

    public TrendingSuggestionsCache(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<String> getCachedSuggestions(int limit) {
        String stored = prefs.getString(KEY_TRENDING, null);
        if (stored == null || stored.trim().isEmpty()) return new ArrayList<>();
        String[] arr = stored.split("\u001F"); // unit separator to avoid collisions
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        if (limit > 0 && list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    public void saveSuggestions(List<String> suggestions) {
        if (suggestions == null) return;
        String joined = String.join("\u001F", suggestions);
        prefs.edit().putString(KEY_TRENDING, joined).apply();
    }
}
