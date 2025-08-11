package com.surya.videos.TrendingVideos.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.surya.videos.TrendingVideos.database.entity.SearchQuery;

import java.util.List;

@Dao
public interface SearchQueryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SearchQuery query);

    @Update
    void update(SearchQuery query);

    @Query("SELECT * FROM search_queries ORDER BY lastSearchedAt DESC LIMIT :limit")
    LiveData<List<SearchQuery>> getRecent(int limit);

    @Query("SELECT * FROM search_queries ORDER BY searchCount DESC, lastSearchedAt DESC LIMIT :limit")
    LiveData<List<SearchQuery>> getTop(int limit);

    @Query("SELECT * FROM search_queries WHERE query LIKE :prefix || '%' ORDER BY lastSearchedAt DESC LIMIT :limit")
    LiveData<List<SearchQuery>> searchByPrefix(String prefix, int limit);

    @Query("UPDATE search_queries SET lastSearchedAt = :time, searchCount = searchCount + 1 WHERE query = :query")
    void touch(String query, long time);

    @Query("DELETE FROM search_queries WHERE query = :query")
    void delete(String query);

    @Query("DELETE FROM search_queries")
    void clearAll();
}
