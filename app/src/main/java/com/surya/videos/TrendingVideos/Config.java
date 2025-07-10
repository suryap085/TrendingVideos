package com.surya.videos.TrendingVideos;

/**
 * Created by Surya on 5/17/2017.
 * Updated for security and modern Android practices.
 */
public class Config {
    // SECURITY FIX: Never hardcode API keys in source code!
    // The API key should be stored securely and loaded at runtime
    // For development, you can add it to your local.properties file:
    // YOUTUBE_API_KEY="your_actual_api_key_here"
    // Then access it via BuildConfig.YOUTUBE_API_KEY
    
    public static final String DEVELOPER_KEY = BuildConfig.YOUTUBE_API_KEY != null ? 
            BuildConfig.YOUTUBE_API_KEY : "YOUR_API_KEY_HERE";

    // YouTube video id
    public static final String YOUTUBE_VIDEO_CODE = "_oEA18Y8gM0";
    
    // App constants
    public static final int REQUEST_TIMEOUT = 30000; // 30 seconds
    public static final int MAX_RESULTS_PER_PAGE = 25;
}
