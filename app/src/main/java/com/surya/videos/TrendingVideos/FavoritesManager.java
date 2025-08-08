package com.surya.videos.TrendingVideos;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREF_NAME = "FavoritesPrefs";
    private static final String KEY_FAVORITES = "favorite_videos";
    
    private SharedPreferences preferences;
    private Gson gson;
    
    public FavoritesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public void addToFavorites(FavoriteVideo video) {
        List<FavoriteVideo> favorites = getFavorites();
        if (!isFavorite(video.getVideoId())) {
            favorites.add(video);
            saveFavorites(favorites);
        }
    }
    
    public void removeFromFavorites(String videoId) {
        List<FavoriteVideo> favorites = getFavorites();
        favorites.removeIf(video -> video.getVideoId().equals(videoId));
        saveFavorites(favorites);
    }
    
    public boolean isFavorite(String videoId) {
        List<FavoriteVideo> favorites = getFavorites();
        for (FavoriteVideo video : favorites) {
            if (video.getVideoId().equals(videoId)) {
                return true;
            }
        }
        return false;
    }
    
    public List<FavoriteVideo> getFavorites() {
        String json = preferences.getString(KEY_FAVORITES, "[]");
        Type type = new TypeToken<ArrayList<FavoriteVideo>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    private void saveFavorites(List<FavoriteVideo> favorites) {
        String json = gson.toJson(favorites);
        preferences.edit().putString(KEY_FAVORITES, json).apply();
    }
    
    public static class FavoriteVideo {
        private String videoId;
        private String title;
        private String thumbnailUrl;
        private String duration;
        private long addedTime;
        
        public FavoriteVideo(String videoId, String title, String thumbnailUrl, String duration) {
            this.videoId = videoId;
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.duration = duration;
            this.addedTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getDuration() { return duration; }
        public long getAddedTime() { return addedTime; }
    }
}
