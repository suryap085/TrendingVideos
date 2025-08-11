package com.surya.videos.TrendingVideos.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.surya.videos.TrendingVideos.database.dao.FavoriteVideoDao;
import com.surya.videos.TrendingVideos.database.dao.SearchQueryDao;
import com.surya.videos.TrendingVideos.database.dao.VideoProgressDao;
import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;
import com.surya.videos.TrendingVideos.database.entity.SearchQuery;
import com.surya.videos.TrendingVideos.database.entity.VideoProgress;

@Database(entities = {FavoriteVideo.class, VideoProgress.class, SearchQuery.class}, version = 2, exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "trending_videos_db";
    private static VideoDatabase instance;

    public abstract FavoriteVideoDao favoriteVideoDao();
    public abstract VideoProgressDao videoProgressDao();
    public abstract SearchQueryDao searchQueryDao();

    public static synchronized VideoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    VideoDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
