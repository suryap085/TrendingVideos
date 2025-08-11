package com.surya.videos.TrendingVideos.progress;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.surya.videos.TrendingVideos.database.VideoDatabase;
import com.surya.videos.TrendingVideos.database.dao.VideoProgressDao;
import com.surya.videos.TrendingVideos.database.entity.VideoProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoProgressManager {
    
    private static VideoProgressManager instance;
    private VideoProgressDao videoProgressDao;
    private ExecutorService executorService;
    
    private VideoProgressManager(Context context) {
        VideoDatabase database = VideoDatabase.getInstance(context);
        videoProgressDao = database.videoProgressDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized VideoProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new VideoProgressManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void saveProgress(String videoId, String title, String thumbnailUrl, 
                           long progressMillis, long durationMillis) {
        executorService.execute(() -> {
            VideoProgress progress = new VideoProgress(videoId, title, thumbnailUrl, progressMillis, durationMillis);
            progress.setLastPlayedAt(System.currentTimeMillis());
            videoProgressDao.insert(progress);
        });
    }
    
    public void updateProgress(String videoId, long progressMillis) {
        executorService.execute(() -> {
            videoProgressDao.updateProgress(videoId, progressMillis, System.currentTimeMillis());
        });
    }
    
    public void markAsCompleted(String videoId) {
        executorService.execute(() -> {
            videoProgressDao.markAsCompleted(videoId);
        });
    }
    
    public LiveData<VideoProgress> getProgress(String videoId) {
        return videoProgressDao.getProgressByVideoId(videoId);
    }
    
    public VideoProgress getProgressSync(String videoId) {
        return videoProgressDao.getProgressByVideoIdSync(videoId);
    }
    
    public LiveData<List<VideoProgress>> getIncompleteVideos() {
        return videoProgressDao.getIncompleteVideos();
    }
    
    public LiveData<List<VideoProgress>> getRecentIncompleteVideos() {
        return videoProgressDao.getRecentIncompleteVideos();
    }
    
    public void deleteProgress(String videoId) {
        executorService.execute(() -> {
            videoProgressDao.deleteProgress(videoId);
        });
    }
    
    public void clearCompletedVideos() {
        executorService.execute(() -> {
            videoProgressDao.deleteCompletedVideos();
        });
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
