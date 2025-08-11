package com.surya.videos.TrendingVideos.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.SearchVideos.SearchActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {
    
    private static final String CHANNEL_ID = "trending_videos_service_channel";
    private static final String CHANNEL_NAME = "Video Updates";
    private static final String CHANNEL_DESCRIPTION = "Periodic updates about new trending videos";
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    @Override
    public void onCreate() {
        super.onCreate();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check for new videos and send notifications
        checkForNewVideos();
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    private void checkForNewVideos() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        
        // Get user's preferred categories
        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null && userData.containsKey("preferredCategories")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Boolean> categories = (Map<String, Boolean>) userData.get("preferredCategories");
                            
                            for (String category : categories.keySet()) {
                                if (categories.get(category)) {
                                    checkCategoryForNewVideos(category);
                                }
                            }
                        }
                    }
                });
    }
    
    private void checkCategoryForNewVideos(String category) {
        // Check for videos published in the last hour
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        
        firestore.collection("videos")
                .whereEqualTo("category", category)
                .whereGreaterThan("publishedAt", oneHourAgo)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        sendCategoryNotification(category, queryDocumentSnapshots.size());
                    }
                });
    }
    
    private void sendCategoryNotification(String category, int videoCount) {
        String title = "New " + category + " Videos!";
        String message = videoCount + " new trending videos in " + category;
        
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("category", category);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 
                category.hashCode(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        createNotificationChannel(notificationManager);
        
        notificationManager.notify(category.hashCode(), notificationBuilder.build());
    }
    
    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
