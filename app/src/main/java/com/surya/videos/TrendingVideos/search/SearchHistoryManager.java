package com.surya.videos.TrendingVideos.search;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.surya.videos.TrendingVideos.database.VideoDatabase;
import com.surya.videos.TrendingVideos.database.dao.SearchQueryDao;
import com.surya.videos.TrendingVideos.database.entity.SearchQuery;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchHistoryManager {

    private static SearchHistoryManager instance;
    private final SearchQueryDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SearchHistoryManager(Context context) {
        dao = VideoDatabase.getInstance(context).searchQueryDao();
    }

    public static synchronized SearchHistoryManager getInstance(Context context) {
        if (instance == null) instance = new SearchHistoryManager(context.getApplicationContext());
        return instance;
    }

    public LiveData<List<SearchQuery>> getRecent(int limit) {
        return dao.getRecent(limit);
    }

    public LiveData<List<SearchQuery>> getTop(int limit) {
        return dao.getTop(limit);
    }

    public LiveData<List<SearchQuery>> searchByPrefix(String prefix, int limit) {
        return dao.searchByPrefix(prefix, limit);
    }

    public void record(String rawQuery) {
        if (rawQuery == null) return;
        final String query = rawQuery.trim();
        if (query.isEmpty()) return;
        long now = System.currentTimeMillis();
        executor.execute(() -> {
            try {
                dao.touch(query, now);
            } catch (Exception ignored) {
                dao.insert(new SearchQuery(query));
            }
        });
    }

    public void delete(String query) {
        executor.execute(() -> dao.delete(query));
    }

    public void clearAll() {
        executor.execute(dao::clearAll);
    }
}
