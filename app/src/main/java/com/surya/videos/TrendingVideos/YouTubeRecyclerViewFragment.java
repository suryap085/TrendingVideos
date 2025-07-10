package com.surya.videos.TrendingVideos;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * COMPLIANCE UPDATE: Updated to use legitimate YouTube Data API for trending videos
 * with proper content filtering and YouTube attribution requirements.
 */
public class YouTubeRecyclerViewFragment extends Fragment {
    // COMPLIANCE FIX: Changed from playlists to video categories
    private static final String ARG_YOUTUBE_CATEGORY_IDS = "YOUTUBE_CATEGORY_IDS";
    private static final int SPINNER_ITEM_LAYOUT_ID = android.R.layout.simple_spinner_item;
    private static final int SPINNER_ITEM_DROPDOWN_LAYOUT_ID = android.R.layout.simple_spinner_dropdown_item;

    // Category names for display (compliant with YouTube policies)
    private static final String[] CATEGORY_NAMES = {
        "Music",
        "Gaming", 
        "People & Blogs",
        "Comedy",
        "Entertainment",
        "News & Politics",
        "Howto & Style",
        "Education",
        "Science & Technology"
    };

    private String[] mCategoryIds;
    private ArrayList<String> mCategoryTitles;
    private RecyclerView mRecyclerView;
    private TrendingVideos mTrendingVideos;
    private RecyclerView.LayoutManager mLayoutManager;
    private Spinner mCategorySpinner;
    private PlaylistCardAdapter mVideoCardAdapter;
    private YouTube mYouTubeDataApi;
    private TextView mAttributionText;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param youTubeDataApi
     * @param categoryIds A String array of YouTube Video Category IDs
     * @return A new instance of fragment YouTubeRecyclerViewFragment.
     */
    public static YouTubeRecyclerViewFragment newInstance(YouTube youTubeDataApi, String[] categoryIds) {
        YouTubeRecyclerViewFragment fragment = new YouTubeRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_YOUTUBE_CATEGORY_IDS, categoryIds);
        fragment.setArguments(args);
        fragment.setYouTubeDataApi(youTubeDataApi);
        return fragment;
    }

    public YouTubeRecyclerViewFragment() {
        // Required empty public constructor
    }

    public void setYouTubeDataApi(YouTube api) {
        mYouTubeDataApi = api;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mCategoryIds = getArguments().getStringArray(ARG_YOUTUBE_CATEGORY_IDS);
        }

        // COMPLIANCE: Initialize category titles (no API call needed for categories)
        mCategoryTitles = new ArrayList<>(Arrays.asList(CATEGORY_NAMES));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.youtube_recycler_view_fragment, container, false);

        // COMPLIANCE: Add YouTube attribution (required)
        mAttributionText = (TextView) rootView.findViewById(R.id.youtube_attribution);
        if (mAttributionText != null) {
            mAttributionText.setText(getString(R.string.youtube_attribution));
            mAttributionText.setVisibility(View.VISIBLE);
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.youtube_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        Resources resources = getResources();
        if (resources.getBoolean(R.bool.isTablet)) {
            // use a staggered grid layout if we're on a large screen device
            mLayoutManager = new StaggeredGridLayoutManager(resources.getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL);
        } else {
            // use a linear layout on phone devices
            mLayoutManager = new LinearLayoutManager(getActivity());
        }

        mRecyclerView.setLayoutManager(mLayoutManager);

        mCategorySpinner = (Spinner)rootView.findViewById(R.id.youtube_playlist_spinner);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // COMPLIANCE FIX: Use trending videos instead of playlists
        if (mTrendingVideos != null) {
            // reload the UI with the existing videos.  No need to fetch again
            reloadUi(mTrendingVideos, false);
        } else {
            // create trending videos list using the first category
            mTrendingVideos = new TrendingVideos(mCategoryIds[0]);
            // and reload the UI with the selected category and kick off fetching
            reloadUi(mTrendingVideos, true);
        }

        // Setup spinner with category names
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            getContext(), 
            SPINNER_ITEM_LAYOUT_ID, 
            mCategoryTitles
        );

        spinnerAdapter.setDropDownViewResource(SPINNER_ITEM_DROPDOWN_LAYOUT_ID);
        mCategorySpinner.setAdapter(spinnerAdapter);

        // set up the onItemSelectedListener for the spinner
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // reload the UI with trending videos for the selected category
                mTrendingVideos = new TrendingVideos(mCategoryIds[position]);
                reloadUi(mTrendingVideos, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void reloadUi(final TrendingVideos trendingVideos, boolean fetchVideos) {
        // initialize the cards adapter
        initCardAdapter(trendingVideos);

        if (fetchVideos) {
            // start fetching trending videos for the selected category
            new GetTrendingVideosAsyncTask(mYouTubeDataApi) {
                @Override
                public void onPostExecute(Pair<String, List<Video>> result) {
                    handleGetVideosResult(trendingVideos, result);
                }
            }.execute(trendingVideos.categoryId, trendingVideos.getNextPageToken());
        }
    }

    private void initCardAdapter(final TrendingVideos trendingVideos) {
        // create the adapter with our trending videos and a callback for pagination
        mVideoCardAdapter = new PlaylistCardAdapter(trendingVideos, new LastItemReachedListener() {
            @Override
            public void onLastItem(int position, String nextPageToken) {
                new GetTrendingVideosAsyncTask(mYouTubeDataApi) {
                    @Override
                    public void onPostExecute(Pair<String, List<Video>> result) {
                        handleGetVideosResult(trendingVideos, result);
                    }
                }.execute(trendingVideos.categoryId, trendingVideos.getNextPageToken());
            }
        });
        mRecyclerView.setAdapter(mVideoCardAdapter);
    }

    private void handleGetVideosResult(TrendingVideos trendingVideos, Pair<String, List<Video>> result) {
        if (result == null) return;
        final int positionStart = trendingVideos.size();
        trendingVideos.setNextPageToken(result.first);
        trendingVideos.addAll(result.second);
        mVideoCardAdapter.notifyItemRangeInserted(positionStart, result.second.size());
    }

    /**
     * Interface used by the adapter to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }

    /**
     * COMPLIANCE: Helper class for trending videos (replacing PlaylistVideos)
     */
    public static class TrendingVideos extends ArrayList<Video> {
        public final String categoryId;
        private String nextPageToken;

        public TrendingVideos(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getNextPageToken() {
            return nextPageToken;
        }

        public void setNextPageToken(String nextPageToken) {
            this.nextPageToken = nextPageToken;
        }
    }
}
