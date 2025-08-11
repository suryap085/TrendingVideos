package com.surya.videos.TrendingVideos.SearchVideos;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.surya.videos.TrendingVideos.Config;
import com.surya.videos.TrendingVideos.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Surya on 5/19/2017.
 */
public class YoutubeConnector {

    private YouTube youtube;
    private YouTube.Search.List query;
    private Context context;

    public YoutubeConnector(Context context) {
        this.context = context.getApplicationContext();
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(Config.DEVELOPER_KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            query.setMaxResults(50L);
        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords){
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
            return null;
        }
    }

    public List<VideoItem> getTrendingVideos(String regionCode) {
        try {
            YouTube.Videos.List videosList = youtube.videos().list("id,snippet");
            videosList.setKey(Config.DEVELOPER_KEY);
            videosList.setChart("mostPopular");
            videosList.setRegionCode(regionCode);
            videosList.setMaxResults(50L);
            videosList.setFields("items(id,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            VideoListResponse resp = videosList.execute();
            List<Video> videos = resp.getItems();
            List<VideoItem> items = new ArrayList<>();
            for (Video v : videos) {
                VideoItem item = new VideoItem();
                item.setId(v.getId());
                item.setTitle(v.getSnippet().getTitle());
                item.setDescription(v.getSnippet().getDescription());
                if (v.getSnippet().getThumbnails() != null && v.getSnippet().getThumbnails().getDefault() != null) {
                    item.setThumbnailURL(v.getSnippet().getThumbnails().getDefault().getUrl());
                }
                items.add(item);
            }
            return items;
        } catch (IOException e) {
            Log.d("YC", "Could not load trending: "+e);
            return null;
        }
    }
}
