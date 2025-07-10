package com.surya.videos.TrendingVideos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by Surya on 5/17/2017.
 * Updated for YouTube API compliance and Play Store policies.
 */
public class YouTubePlayer_Activity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // YouTube player view
    private YouTubePlayerView youTubeView;
    private TextView attributionText;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().setFactory(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        youTubeView = (YouTubePlayerView) findViewById(R.id.player_view);
        
        // COMPLIANCE: Show YouTube attribution (required)
        attributionText = (TextView) findViewById(R.id.youtube_attribution_player);
        if (attributionText != null) {
            attributionText.setText(getString(R.string.youtube_attribution));
        }

        // Get video ID from intent
        videoId = getIntent().getStringExtra("VideoId");
        
        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "Invalid video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // COMPLIANCE: Validate API key before initializing player
        if (Config.DEVELOPER_KEY.startsWith("YOUR_API_KEY")) {
            Toast.makeText(this, getString(R.string.api_key_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initializing video player with developer key
        youTubeView.initialize(Config.DEVELOPER_KEY, this);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            // COMPLIANCE: Proper error handling with user-friendly messages
            String errorMessage = getErrorMessage(errorReason);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            // Load and play the video
            player.loadVideo(videoId);
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            
            // COMPLIANCE: Set player event listeners for better UX
            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {
                    // Show loading state
                }

                @Override
                public void onLoaded(String s) {
                    // Video loaded successfully
                }

                @Override
                public void onAdStarted() {
                    // Ad started - required for monetization compliance
                }

                @Override
                public void onVideoStarted() {
                    // Video playback started
                }

                @Override
                public void onVideoEnded() {
                    // Video ended - could show related videos or close
                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {
                    // Handle playback errors
                    String error = "Playback error: " + errorReason.toString();
                    Toast.makeText(YouTubePlayer_Activity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.DEVELOPER_KEY, this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.player_view);
    }

    /**
     * COMPLIANCE: Provide user-friendly error messages
     */
    private String getErrorMessage(YouTubeInitializationResult errorReason) {
        switch (errorReason) {
            case NETWORK_ERROR:
                return getString(R.string.error_network);
            case INVALID_APPLICATION_SIGNATURE:
                return "Invalid app signature. Please check your keystore.";
            case DEVELOPER_KEY_INVALID:
                return getString(R.string.api_key_invalid);
            case SERVICE_MISSING:
                return "YouTube service is not available on this device.";
            case SERVICE_VERSION_UPDATE_REQUIRED:
                return "Please update YouTube app.";
            case SERVICE_DISABLED:
                return "YouTube service is disabled on this device.";
            default:
                return "YouTube player initialization failed: " + errorReason.toString();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
    }
}
