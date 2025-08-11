package com.surya.videos.TrendingVideos.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.database.entity.FavoriteVideo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    
    private Context context;
    private List<FavoriteVideo> favorites;
    private OnItemClickListener onItemClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(FavoriteVideo favoriteVideo);
    }
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteVideo favoriteVideo);
    }
    
    public FavoritesAdapter(Context context, List<FavoriteVideo> favorites) {
        this.context = context;
        this.favorites = favorites;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }
    
    public void updateFavorites(List<FavoriteVideo> newFavorites) {
        this.favorites = newFavorites;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_video_item, parent, false);
        return new FavoriteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteVideo favorite = favorites.get(position);
        holder.bind(favorite);
    }
    
    @Override
    public int getItemCount() {
        return favorites.size();
    }
    
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView channelTextView;
        private TextView publishedAtTextView;
        private TextView categoryTextView;
        private ImageView favoriteButton;
        
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            channelTextView = itemView.findViewById(R.id.channel_text_view);
            publishedAtTextView = itemView.findViewById(R.id.published_at_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(favorites.get(position));
                }
            });
            
            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onFavoriteClickListener != null) {
                    onFavoriteClickListener.onFavoriteClick(favorites.get(position));
                }
            });
        }
        
        public void bind(FavoriteVideo favorite) {
            titleTextView.setText(favorite.getTitle());
            channelTextView.setText(favorite.getChannelTitle());
            categoryTextView.setText(favorite.getCategory());
            
            // Format published date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(favorite.getAddedAt()));
            publishedAtTextView.setText("Added: " + formattedDate);
            
            // Load thumbnail
            Glide.with(context)
                    .load(favorite.getThumbnailUrl())
                    .placeholder(R.drawable.video_placeholder)
                    .error(R.drawable.video_placeholder)
                    .into(thumbnailImageView);
            
            // Set favorite button state
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        }
    }
}
