package com.surya.videos.TrendingVideos;

import android.os.AsyncTask;
import android.util.Pair;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * COMPLIANCE: AsyncTask to fetch trending videos using legitimate YouTube Data API
 * with proper content filtering and age-appropriate content restrictions.
 * 
 * This replaces the previous hardcoded playlist approach which violated YouTube ToS.
 */
public abstract class GetTrendingVideosAsyncTask extends AsyncTask<String, Void, Pair<String, List<Video>>> {

    private static final String YOUTUBE_VIDEOS_PART = "snippet,contentDetails,statistics";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    
    private YouTube mYouTubeDataApi;
    private YouTubeConnector mYouTubeConnector;

    public GetTrendingVideosAsyncTask(YouTube youtubeDataApi) {
        mYouTubeDataApi = youtubeDataApi;
        mYouTubeConnector = new YouTubeConnector();
    }

    @Override
    protected Pair<String, List<Video>> doInBackground(String... params) {
        final String categoryId = params[0];
        final String pageToken = params.length > 1 ? params[1] : null;

        try {
            // COMPLIANCE: Use legitimate YouTube Data API search instead of hardcoded playlists
            YouTube.Search.List search = mYouTubeDataApi.search().list("snippet");
            
            // Configure search parameters for compliance
            search.setKey(Config.DEVELOPER_KEY);
            search.setType("video");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            search.setOrder("relevance"); // or "viewCount" for trending
            search.setRegionCode("US"); // Set appropriate region
            search.setSafeSearch("moderate"); // COMPLIANCE: Content filtering
            search.setVideoEmbeddable("true"); // Only embeddable videos
            search.setVideoSyndicated("true"); // Only syndicated videos
            
            // Filter by category if specified
            if (categoryId != null && !categoryId.isEmpty()) {
                search.setVideoCategoryId(categoryId);
            }
            
            // Set page token for pagination
            if (pageToken != null) {
                search.setPageToken(pageToken);
            }

            // Execute the search
            com.google.api.services.youtube.model.SearchListResponse searchResponse = search.execute();
            
            if (searchResponse.getItems() == null || searchResponse.getItems().isEmpty()) {
                return null;
            }

            // Get video IDs from search results
            List<String> videoIds = new ArrayList<>();
            for (com.google.api.services.youtube.model.SearchResult searchResult : searchResponse.getItems()) {
                videoIds.add(searchResult.getId().getVideoId());
            }

            // Get detailed video information
            YouTube.Videos.List videosRequest = mYouTubeDataApi.videos().list(YOUTUBE_VIDEOS_PART);
            videosRequest.setKey(Config.DEVELOPER_KEY);
            videosRequest.setId(String.join(",", videoIds));

            VideoListResponse videosResponse = videosRequest.execute();
            List<Video> videos = videosResponse.getItems();

            // COMPLIANCE: Filter out inappropriate content
            List<Video> filteredVideos = filterAppropriateContent(videos);

            return new Pair<>(searchResponse.getNextPageToken(), filteredVideos);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * COMPLIANCE: Filter videos for age-appropriate content
     * This helps ensure compliance with Play Store policies
     */
    private List<Video> filterAppropriateContent(List<Video> videos) {
        List<Video> filteredVideos = new ArrayList<>();
        
        for (Video video : videos) {
            // Skip videos without proper content details
            if (video.getContentDetails() == null || video.getSnippet() == null) {
                continue;
            }
            
            // Check content rating - skip if restricted
            if (isContentAppropriate(video)) {
                filteredVideos.add(video);
            }
        }
        
        return filteredVideos;
    }

    /**
     * COMPLIANCE: Check if video content is appropriate
     */
    private boolean isContentAppropriate(Video video) {
        // Check video duration - skip very long videos that might be movies
        String duration = video.getContentDetails().getDuration();
        if (duration != null && isVideoTooLong(duration)) {
            return false;
        }

        // Check for age restrictions
        if (video.getContentDetails().getContentRating() != null) {
            // If any content rating exists, it might be restricted
            return false;
        }

        // Check title and description for inappropriate keywords
        String title = video.getSnippet().getTitle().toLowerCase();
        String description = video.getSnippet().getDescription() != null ? 
                            video.getSnippet().getDescription().toLowerCase() : "";

        // Basic inappropriate content filtering
        String[] inappropriateKeywords = {
            "explicit", "mature", "adult", "18+", "nsfw", 
            "violence", "graphic", "disturbing"
        };

        for (String keyword : inappropriateKeywords) {
            if (title.contains(keyword) || description.contains(keyword)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if video duration is too long (likely a movie or inappropriate content)
     */
    private boolean isVideoTooLong(String duration) {
        // Parse ISO 8601 duration format (PT15M33S)
        // Skip videos longer than 30 minutes for safety
        if (duration.contains("H")) {
            return true; // Any video with hours is too long
        }
        
        if (duration.contains("M")) {
            String minutesPart = duration.substring(duration.indexOf("PT") + 2, duration.indexOf("M"));
            try {
                int minutes = Integer.parseInt(minutesPart);
                return minutes > 30;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return false;
    }
}