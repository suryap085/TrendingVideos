package com.surya.videos.TrendingVideos.utils;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.surya.videos.TrendingVideos.database.VideoDatabase;
import com.surya.videos.TrendingVideos.database.dao.FavoriteVideoDao;
import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesManager {
    
    private static FavoritesManager instance;
    private FavoriteVideoDao favoriteVideoDao;
    private ExecutorService executorService;
    
    private FavoritesManager(Context context) {
        VideoDatabase database = VideoDatabase.getInstance(context);
        favoriteVideoDao = database.favoriteVideoDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void addToFavorites(String videoId, String title, String description, 
                              String thumbnailUrl, String channelTitle, String publishedAt, String category) {
        executorService.execute(() -> {
            FavoriteVideo favoriteVideo = new FavoriteVideo(videoId, title, description, 
                    thumbnailUrl, channelTitle, publishedAt, category);
            favoriteVideoDao.insert(favoriteVideo);
        });
    }
    
    public void removeFromFavorites(String videoId) {
        executorService.execute(() -> {
            favoriteVideoDao.deleteByVideoId(videoId);
        });
    }
    
    public LiveData<Boolean> isFavorite(String videoId) {
        return favoriteVideoDao.isFavorite(videoId);
    }
    
    public LiveData<Integer> getFavoriteCount() {
        return favoriteVideoDao.getFavoriteCount();
    }
    
    public void toggleFavorite(String videoId, String title, String description, 
                              String thumbnailUrl, String channelTitle, String publishedAt, String category) {
        executorService.execute(() -> {
            FavoriteVideo existing = favoriteVideoDao.getFavoriteByVideoId(videoId);
            if (existing != null) {
                favoriteVideoDao.deleteByVideoId(videoId);
            } else {
                FavoriteVideo favoriteVideo = new FavoriteVideo(videoId, title, description, 
                        thumbnailUrl, channelTitle, publishedAt, category);
                favoriteVideoDao.insert(favoriteVideo);
            }
        });
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
