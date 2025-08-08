package com.surya.videos.TrendingVideos;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class TrendingVideosApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> {
            // No-op
        });
    }
}


