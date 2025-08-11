package com.surya.videos.TrendingVideos.SearchVideos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.YouTubePlayer_Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrendingFragment extends Fragment {

    private static final String ARG_REGION = "arg_region";

    public static TrendingFragment newInstance(String regionCode) {
        Bundle b = new Bundle();
        b.putString(ARG_REGION, regionCode);
        TrendingFragment f = new TrendingFragment();
        f.setArguments(b);
        return f;
    }

    private ListView listView;
    private ProgressBar progressBar;
    private ShimmerFrameLayout shimmer;
    private ArrayAdapter<VideoItem> adapter;
    private List<VideoItem> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trending, container, false);
        listView = root.findViewById(R.id.list_trending);
        progressBar = root.findViewById(R.id.progress_trending);

        shimmer = new ShimmerFrameLayout(requireContext());
        View skeleton = inflater.inflate(R.layout.shimmer_video_item, shimmer, false);
        shimmer.addView(skeleton);
        ((ViewGroup) root).addView(shimmer);
        shimmer.startShimmer();

        adapter = new ArrayAdapter<VideoItem>(requireContext(), R.layout.video_item, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) convertView = inflater.inflate(R.layout.video_item, parent, false);
                ImageView thumb = convertView.findViewById(R.id.video_thumbnail);
                TextView title = convertView.findViewById(R.id.video_title);
                ImageView favIcon = convertView.findViewById(R.id.favorite_icon);
                favIcon.setVisibility(View.GONE);

                VideoItem item = items.get(position);
                title.setText(item.getTitle());
                Glide.with(requireContext()).load(item.getThumbnailURL())
                        .placeholder(R.drawable.video_placeholder)
                        .into(thumb);

                AlphaAnimation anim = new AlphaAnimation(0f, 1f);
                anim.setDuration(250);
                anim.setInterpolator(new DecelerateInterpolator());
                convertView.startAnimation(anim);
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent1, View view, int position, long id) -> {
            if (position < items.size()) {
                ImageView thumb = view.findViewById(R.id.video_thumbnail);
                Intent intent = new Intent(requireContext(), YouTubePlayer_Activity.class);
                intent.putExtra("VideoId", items.get(position).getId());
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), thumb, "video_thumb");
                startActivity(intent, options.toBundle());
            }
        });

        loadTrending();
        return root;
    }

    private void loadTrending() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String region = Locale.getDefault().getCountry();
            YoutubeConnector yc = new YoutubeConnector(requireContext());
            List<VideoItem> result = yc.getTrendingVideos(region);
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                items.clear();
                if (result != null) items.addAll(result);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
