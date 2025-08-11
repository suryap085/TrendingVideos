package com.surya.videos.TrendingVideos.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "video_progress")
public class VideoProgress {
    
    @PrimaryKey
    private String videoId;
    
    private String title;
    private String thumbnailUrl;
    private long progressMillis;
    private long durationMillis;
    private long lastPlayedAt;
    private boolean completed;
    
    public VideoProgress(String videoId, String title, String thumbnailUrl, 
                        long progressMillis, long durationMillis) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.progressMillis = progressMillis;
        this.durationMillis = durationMillis;
        this.lastPlayedAt = System.currentTimeMillis();
        this.completed = false;
    }
    
    // Getters and Setters
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public long getProgressMillis() { return progressMillis; }
    public void setProgressMillis(long progressMillis) { this.progressMillis = progressMillis; }
    
    public long getDurationMillis() { return durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }
    
    public long getLastPlayedAt() { return lastPlayedAt; }
    public void setLastPlayedAt(long lastPlayedAt) { this.lastPlayedAt = lastPlayedAt; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public float getProgressPercentage() {
        if (durationMillis > 0) {
            return (float) progressMillis / durationMillis * 100;
        }
        return 0;
    }
}
