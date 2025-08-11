package com.surya.videos.TrendingVideos.search;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrendingSuggestionsProvider {

    public interface SuggestionsCallback {
        void onSuggestions(List<String> suggestions);
    }

    private final FirebaseFirestore firestore;
    private TrendingSuggestionsCache cache;

    public TrendingSuggestionsProvider() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void attachCache(TrendingSuggestionsCache cache) {
        this.cache = cache;
    }

    public void getTrending(int limit, SuggestionsCallback callback) {
        // Serve cached immediately if available
        if (cache != null) {
            List<String> cached = cache.getCachedSuggestions(limit);
            if (!cached.isEmpty()) callback.onSuggestions(cached);
        }
        // Refresh from server
        firestore.collection("trending_searches")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> suggestions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Object q = doc.get("query");
                        if (q != null) suggestions.add(String.valueOf(q));
                    }
                    if (suggestions.isEmpty()) suggestions = getFallback(limit);
                    if (cache != null) cache.saveSuggestions(suggestions);
                    callback.onSuggestions(suggestions);
                })
                .addOnFailureListener(e -> {
                    List<String> fb = getFallback(limit);
                    if (cache != null) cache.saveSuggestions(fb);
                    callback.onSuggestions(fb);
                });
    }

    private List<String> getFallback(int limit) {
        List<String> defaults = Arrays.asList(
                "Bollywood songs",
                "Cricket highlights",
                "Tech news",
                "Motivation talk",
                "Comedy clips",
                "Web series trailers",
                "Cooking recipes",
                "Gym workout"
        );
        return defaults.subList(0, Math.min(limit, defaults.size()));
    }
}
