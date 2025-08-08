package com.surya.videos.TrendingVideos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;

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
 * A RecyclerView.Adapter subclass which adapts {@link Video}'s to CardViews.
 */
public class PlaylistCardAdapter extends RecyclerView.Adapter<PlaylistCardAdapter.ViewHolder> {
    private static final DecimalFormat sFormatter = new DecimalFormat("#,###,###");
    private final PlaylistVideos mPlaylistVideos;
    private final YouTubeRecyclerViewFragment.LastItemReachedListener mListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Context mContext;
        public final TextView mTitleText;
        public final TextView mDescriptionText;
        public final ImageView mThumbnailImage;
        public final ImageView mShareIcon;
        public final TextView mShareText;
        public final TextView mDurationText;
        public final TextView mViewCountText;
        public final TextView mLikeCountText;
        public final TextView mDislikeCountText;

        public ViewHolder(View v) {
            super(v);
            mContext = v.getContext();
            mTitleText = (TextView) v.findViewById(R.id.video_title);
            mDescriptionText = (TextView) v.findViewById(R.id.video_description);
            mThumbnailImage = (ImageView) v.findViewById(R.id.video_thumbnail);
            mShareIcon = (ImageView) v.findViewById(R.id.video_share);
            mShareText = (TextView) v.findViewById(R.id.video_share_text);
            mDurationText = (TextView) v.findViewById(R.id.video_dutation_text);
            mViewCountText= (TextView) v.findViewById(R.id.video_view_count);
            mLikeCountText = (TextView) v.findViewById(R.id.video_like_count);
            mDislikeCountText = (TextView) v.findViewById(R.id.video_dislike_count);
        }
    }

    public PlaylistCardAdapter(PlaylistVideos playlistVideos, YouTubeRecyclerViewFragment.LastItemReachedListener lastItemReachedListener) {
        mPlaylistVideos = playlistVideos;
        mListener = lastItemReachedListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a card layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_video_card, parent, false);
        // populate the viewholder
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mPlaylistVideos.size() == 0) {
            return;
        }

        final Video video = mPlaylistVideos.get(position);
        final VideoSnippet videoSnippet = video.getSnippet();
        final VideoContentDetails videoContentDetails = video.getContentDetails();
        final VideoStatistics videoStatistics = video.getStatistics();

        // Safely set text with null checks
        if (videoSnippet.getTitle() != null) {
            holder.mTitleText.setText(videoSnippet.getTitle());
        } else {
            holder.mTitleText.setText("");
        }
        
        if (videoSnippet.getDescription() != null) {
            holder.mDescriptionText.setText(videoSnippet.getDescription());
        } else {
            holder.mDescriptionText.setText("");
        }

        // load the video thumbnail image with null safety
        try {
            if (videoSnippet.getThumbnails() != null && videoSnippet.getThumbnails().getHigh() != null) {
                Glide.with(holder.mContext)
                        .load(videoSnippet.getThumbnails().getHigh().getUrl())
                        .placeholder(R.drawable.video_placeholder)
                        .into(holder.mThumbnailImage);
            } else {
                holder.mThumbnailImage.setImageResource(R.drawable.video_placeholder);
            }
        } catch (Exception e) {
            holder.mThumbnailImage.setImageResource(R.drawable.video_placeholder);
        }

        // set the click listener to play the video
        holder.mThumbnailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent sendIntent = new Intent(holder.mContext,YouTubePlayer_Activity.class);
                    sendIntent.putExtra("VideoId",video.getId());
                    holder.mContext.startActivity(sendIntent);
                   // holder.mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + video.getId())));
                } catch (Exception e) {
                    // Handle activity start failure gracefully
                }
            }
        });

        // create and set the click listener for both the share icon and share text
        View.OnClickListener shareClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String title = videoSnippet.getTitle() != null ? videoSnippet.getTitle() : "Video";
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + title + "\" on YouTube");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + video.getId());
                    sendIntent.setType("text/plain");
                    holder.mContext.startActivity(sendIntent);
                } catch (Exception e) {
                    // Handle share intent failure gracefully
                }
            }
        };
        holder.mShareIcon.setOnClickListener(shareClickListener);
        holder.mShareText.setOnClickListener(shareClickListener);

        // set the video duration text with null safety
        try {
            if (videoContentDetails != null && videoContentDetails.getDuration() != null) {
                holder.mDurationText.setText(parseDuration(videoContentDetails.getDuration()));
            } else {
                holder.mDurationText.setText("");
            }
        } catch (Exception e) {
            holder.mDurationText.setText("");
        }
        
        // set the video statistics with null safety
        try {
            if (videoStatistics != null) {
                if (videoStatistics.getViewCount() != null) {
                    holder.mViewCountText.setText(sFormatter.format(videoStatistics.getViewCount()));
                } else {
                    holder.mViewCountText.setText("0");
                }
                if (videoStatistics.getLikeCount() != null) {
                    holder.mLikeCountText.setText(sFormatter.format(videoStatistics.getLikeCount()));
                } else {
                    holder.mLikeCountText.setText("0");
                }
                if (videoStatistics.getDislikeCount() != null) {
                    holder.mDislikeCountText.setText(sFormatter.format(videoStatistics.getDislikeCount()));
                } else {
                    holder.mDislikeCountText.setText("0");
                }
            } else {
                holder.mViewCountText.setText("0");
                holder.mLikeCountText.setText("0");
                holder.mDislikeCountText.setText("0");
            }
        } catch (Exception e) {
            holder.mViewCountText.setText("0");
            holder.mLikeCountText.setText("0");
            holder.mDislikeCountText.setText("0");
        }

        if (mListener != null) {
            // get the next playlist page if we're at the end of the current page and we have another page to get
            final String nextPageToken = mPlaylistVideos.getNextPageToken();
            if (!isEmpty(nextPageToken) && position == mPlaylistVideos.size() - 1) {
                holder.itemView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onLastItem(position, nextPageToken);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPlaylistVideos.size();
    }

    private boolean isEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }

    private String parseDuration(String in) {
        boolean hasSeconds = in.indexOf('S') > 0;
        boolean hasMinutes = in.indexOf('M') > 0;

        String s;
        if (hasSeconds) {
            s = in.substring(2, in.length() - 1);
        } else {
            s = in.substring(2, in.length());
        }

        String minutes = "0";
        String seconds = "00";

        if (hasMinutes && hasSeconds) {
            String[] split = s.split("M");
            minutes = split[0];
            seconds = split[1];
        } else if (hasMinutes) {
            minutes = s.substring(0, s.indexOf('M'));
        } else if (hasSeconds) {
            seconds = s;
        }

        // pad seconds with a 0 if less than 2 digits
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return minutes + ":" + seconds;
    }
}