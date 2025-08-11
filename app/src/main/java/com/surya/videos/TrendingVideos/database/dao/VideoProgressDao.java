package com.surya.videos.TrendingVideos.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.surya.videos.TrendingVideos.database.entity.VideoProgress;

import java.util.List;

@Dao
public interface VideoProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VideoProgress videoProgress);
    
    @Update
    void update(VideoProgress videoProgress);
    
    @Query("SELECT * FROM video_progress WHERE videoId = :videoId")
    LiveData<VideoProgress> getProgressByVideoId(String videoId);
    
    @Query("SELECT * FROM video_progress WHERE videoId = :videoId")
    VideoProgress getProgressByVideoIdSync(String videoId);
    
    @Query("SELECT * FROM video_progress WHERE completed = 0 ORDER BY lastPlayedAt DESC")
    LiveData<List<VideoProgress>> getIncompleteVideos();
    
    @Query("SELECT * FROM video_progress WHERE completed = 0 ORDER BY lastPlayedAt DESC LIMIT 10")
    LiveData<List<VideoProgress>> getRecentIncompleteVideos();
    
    @Query("UPDATE video_progress SET progressMillis = :progressMillis, lastPlayedAt = :lastPlayedAt WHERE videoId = :videoId")
    void updateProgress(String videoId, long progressMillis, long lastPlayedAt);
    
    @Query("UPDATE video_progress SET completed = 1 WHERE videoId = :videoId")
    void markAsCompleted(String videoId);
    
    @Query("DELETE FROM video_progress WHERE videoId = :videoId")
    void deleteProgress(String videoId);
    
    @Query("DELETE FROM video_progress WHERE completed = 1")
    void deleteCompletedVideos();
}
