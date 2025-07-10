package com.surya.videos.TrendingVideos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

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
 */
public class YouTubeActivity extends AppCompatActivity {
    
    // COMPLIANCE FIX: Removed hardcoded playlists - these violated YouTube ToS
    // Instead using legitimate trending videos and search categories
    private static final String[] VIDEO_CATEGORIES = {
            "10",  // Music
            "20",  // Gaming  
            "22",  // People & Blogs
            "23",  // Comedy
            "24",  // Entertainment
            "25",  // News & Politics
            "26",  // Howto & Style
            "27",  // Education
            "28"   // Science & Technology
    };
    
    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity);

        // Show YouTube attribution and disclaimer (REQUIRED for compliance)
        showYouTubeDisclaimer();

        if (Config.DEVELOPER_KEY.startsWith("YOUR_API_KEY")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Please configure your YouTube Data API key in local.properties file.\n\nYOUTUBE_API_KEY=your_api_key_here")
                        .setTitle("API Key Required")
                        .setNeutralButton("Get API Key", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Open Google Cloud Console for API key
                                Intent intent = new Intent(Intent.ACTION_VIEW, 
                                    Uri.parse("https://console.developers.google.com/"));
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (savedInstanceState == null) {
            mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, YouTubeRecyclerViewFragment.newInstance(mYoutubeDataApi, VIDEO_CATEGORIES))
                    .commit();
        }
    }

    private void showYouTubeDisclaimer() {
        // COMPLIANCE REQUIREMENT: Show YouTube attribution and disclaimer
        Toast.makeText(this, getString(R.string.youtube_attribution), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.you_tube, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.action_search) {
            // Open search activity
            Intent intent = new Intent(this, com.surya.videos.TrendingVideos.SearchVideos.SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_disclaimer) {
            showDisclaimerDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.app_description) + "\n\n" + 
                       getString(R.string.youtube_attribution) + "\n\n" +
                       getString(R.string.data_usage_notice))
            .setPositiveButton("OK", null);
        builder.create().show();
    }
    
    private void showDisclaimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Important Notice")
            .setMessage(getString(R.string.youtube_disclaimer) + "\n\n" + 
                       getString(R.string.content_disclaimer))
            .setPositiveButton("I Understand", null);
        builder.create().show();
    }
}
