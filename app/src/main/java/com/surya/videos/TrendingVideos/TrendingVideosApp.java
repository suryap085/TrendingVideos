package com.surya.videos.TrendingVideos;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.surya.videos.TrendingVideos.database.VideoDatabase;
import com.surya.videos.TrendingVideos.progress.VideoProgressManager;

public class TrendingVideosApp extends Application {
    
    private static final String CHANNEL_ID = "trending_videos_channel";
    private static final String CHANNEL_NAME = "Trending Videos";
    private static final String CHANNEL_DESCRIPTION = "Notifications for trending videos";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {
            // No-op
        });
        
        // Initialize database
        VideoDatabase.getInstance(this);
        
        // Initialize video progress manager
        VideoProgressManager.getInstance(this);
        
        // Create notification channels
        createNotificationChannels();
        
        // Request FCM token
        requestFCMToken();
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void requestFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        // TODO: Send token to your server
                        // This is where you would send the FCM token to your backend
                        // to associate it with the user for targeted notifications
                    }
                });
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clean up resources
        VideoProgressManager.getInstance(this).shutdown();
    }
}


