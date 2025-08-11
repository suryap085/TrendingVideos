package com.surya.videos.TrendingVideos.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_queries")
public class SearchQuery {

    @PrimaryKey
    @NonNull
    private String query;

    private long lastSearchedAt;
    private int searchCount;

    public SearchQuery(@NonNull String query) {
        this.query = query;
        this.lastSearchedAt = System.currentTimeMillis();
        this.searchCount = 1;
    }

    @NonNull
    public String getQuery() { return query; }
    public void setQuery(@NonNull String query) { this.query = query; }

    public long getLastSearchedAt() { return lastSearchedAt; }
    public void setLastSearchedAt(long lastSearchedAt) { this.lastSearchedAt = lastSearchedAt; }

    public int getSearchCount() { return searchCount; }
    public void setSearchCount(int searchCount) { this.searchCount = searchCount; }
}
