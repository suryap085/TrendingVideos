package com.surya.videos.TrendingVideos.favorites;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.surya.videos.TrendingVideos.database.VideoDatabase;
import com.surya.videos.TrendingVideos.database.dao.FavoriteVideoDao;
import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesViewModel extends AndroidViewModel {
    
    private FavoriteVideoDao favoriteVideoDao;
    private LiveData<List<FavoriteVideo>> allFavorites;
    private ExecutorService executorService;
    
    public FavoritesViewModel(Application application) {
        super(application);
        VideoDatabase database = VideoDatabase.getInstance(application);
        favoriteVideoDao = database.favoriteVideoDao();
        allFavorites = favoriteVideoDao.getAllFavorites();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<FavoriteVideo>> getAllFavorites() {
        return allFavorites;
    }
    
    public LiveData<List<FavoriteVideo>> getFavoritesByCategory(String category) {
        return favoriteVideoDao.getFavoritesByCategory(category);
    }
    
    public void addToFavorites(FavoriteVideo favoriteVideo) {
        executorService.execute(() -> {
            favoriteVideoDao.insert(favoriteVideo);
        });
    }
    
    public void removeFromFavorites(FavoriteVideo favoriteVideo) {
        executorService.execute(() -> {
            favoriteVideoDao.delete(favoriteVideo);
        });
    }
    
    public void removeFromFavorites(String videoId) {
        executorService.execute(() -> {
            favoriteVideoDao.deleteByVideoId(videoId);
        });
    }
    
    public void clearAllFavorites() {
        executorService.execute(() -> {
            List<FavoriteVideo> favorites = allFavorites.getValue();
            if (favorites != null) {
                for (FavoriteVideo favorite : favorites) {
                    favoriteVideoDao.delete(favorite);
                }
            }
        });
    }
    
    public LiveData<Boolean> isFavorite(String videoId) {
        return favoriteVideoDao.isFavorite(videoId);
    }
    
    public LiveData<Integer> getFavoriteCount() {
        return favoriteVideoDao.getFavoriteCount();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
