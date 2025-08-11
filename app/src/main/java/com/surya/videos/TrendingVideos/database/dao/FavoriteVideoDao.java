package com.surya.videos.TrendingVideos.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;

import java.util.List;

@Dao
public interface FavoriteVideoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteVideo favoriteVideo);
    
    @Delete
    void delete(FavoriteVideo favoriteVideo);
    
    @Query("DELETE FROM favorite_videos WHERE videoId = :videoId")
    void deleteByVideoId(String videoId);
    
    @Query("SELECT * FROM favorite_videos ORDER BY addedAt DESC")
    LiveData<List<FavoriteVideo>> getAllFavorites();
    
    @Query("SELECT * FROM favorite_videos WHERE category = :category ORDER BY addedAt DESC")
    LiveData<List<FavoriteVideo>> getFavoritesByCategory(String category);
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE videoId = :videoId)")
    LiveData<Boolean> isFavorite(String videoId);
    
    @Query("SELECT COUNT(*) FROM favorite_videos")
    LiveData<Integer> getFavoriteCount();
    
    @Query("SELECT * FROM favorite_videos WHERE videoId = :videoId")
    FavoriteVideo getFavoriteByVideoId(String videoId);
}
