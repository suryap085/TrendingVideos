# Integration Guide for New Features

This guide explains how to integrate the new features (Favorites, Resume, Notifications) with your existing video player and search functionality.

## 1. Adding Favorites to Video Items

### In your video item layout (e.g., `video_item.xml`):
```xml
<ImageView
    android:id="@+id/favorite_button"
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/ic_favorite"
    android:background="?attr/selectableItemBackgroundBorderless"
    android:padding="4dp"
    android:contentDescription="Add to favorites" />
```

### In your video adapter:
```java
// Add to your ViewHolder
private ImageView favoriteButton;
private FavoritesManager favoritesManager;

// In onBindViewHolder
favoritesManager = FavoritesManager.getInstance(context);
favoritesManager.isFavorite(videoId).observe(lifecycleOwner, isFavorite -> {
    favoriteButton.setImageResource(isFavorite ? 
        R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
});

favoriteButton.setOnClickListener(v -> {
    favoritesManager.toggleFavorite(
        videoId, title, description, thumbnailUrl, 
        channelTitle, publishedAt, category
    );
});
```

## 2. Adding Resume Functionality to Video Player

### In your YouTubePlayer_Activity:
```java
private VideoProgressManager progressManager;
private String currentVideoId;
private long videoDuration = 0;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    progressManager = VideoProgressManager.getInstance(this);
    currentVideoId = getIntent().getStringExtra("video_id");
    
    // Load saved progress
    VideoProgress savedProgress = progressManager.getProgressSync(currentVideoId);
    if (savedProgress != null && !savedProgress.isCompleted()) {
        // Show resume dialog
        showResumeDialog(savedProgress.getProgressMillis());
    }
}

private void showResumeDialog(long savedPosition) {
    new AlertDialog.Builder(this)
        .setTitle("Resume Video?")
        .setMessage("You have watched " + formatTime(savedPosition) + " of this video. Resume from where you left off?")
        .setPositiveButton("Resume", (dialog, which) -> {
            // Resume from saved position
            youTubePlayer.seekTo(savedPosition / 1000f);
        })
        .setNegativeButton("Start Over", (dialog, which) -> {
            // Start from beginning
        })
        .show();
}

// Save progress periodically
private void saveProgress(long currentPosition) {
    progressManager.saveProgress(
        currentVideoId, 
        videoTitle, 
        thumbnailUrl, 
        currentPosition, 
        videoDuration
    );
}

// Mark as completed when video ends
private void onVideoEnded() {
    progressManager.markAsCompleted(currentVideoId);
}
```

## 3. Adding Favorites Menu Item

### In your main activity menu (e.g., `you_tube.xml`):
```xml
<item
    android:id="@+id/action_favorites"
    android:title="My Favorites"
    android:icon="@drawable/ic_favorite"
    app:showAsAction="ifRoom" />
```

### In your activity:
```java
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_favorites) {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

## 4. Handling Notification Intents

### In your SearchActivity:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Handle notification intents
    String videoId = getIntent().getStringExtra("video_id");
    String category = getIntent().getStringExtra("category");
    
    if (videoId != null) {
        // Open specific video
        Intent intent = new Intent(this, YouTubePlayer_Activity.class);
        intent.putExtra("video_id", videoId);
        startActivity(intent);
    } else if (category != null) {
        // Filter by category
        // Implement category filtering in your search
    }
}
```

## 5. Requesting Notification Permissions

### For Android 13+ (API 33+):
```java
private void requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, 
            Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }
}
```

## 6. Database Migration (if needed)

If you have existing data that needs to be migrated:

```java
// In your Application class
@Database(entities = {FavoriteVideo.class, VideoProgress.class}, version = 2)
public abstract class VideoDatabase extends RoomDatabase {
    // Add migration if needed
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add migration logic here
        }
    };
}
```

## 7. Error Handling

### Add proper error handling for database operations:
```java
favoritesManager.addToFavorites(videoId, title, description, thumbnailUrl, 
    channelTitle, publishedAt, category)
    .addOnFailureListener(exception -> {
        // Handle error
        Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
    });
```

## 8. Testing the Integration

1. **Test Favorites**: Add/remove videos from favorites and verify they appear in the favorites screen
2. **Test Resume**: Start a video, pause it, close the app, reopen and verify resume dialog appears
3. **Test Notifications**: Send a test notification and verify it opens the correct video/category
4. **Test Authentication**: Verify OTP flow works correctly

## 9. Performance Considerations

- Use background threads for database operations
- Implement proper lifecycle management for LiveData observers
- Cache frequently accessed data
- Use pagination for large lists

## 10. Security Notes

- Validate all user inputs
- Sanitize data before storing in database
- Use proper authentication checks
- Follow YouTube API terms of service

This integration ensures your app follows best practices while adding the new functionality seamlessly.
