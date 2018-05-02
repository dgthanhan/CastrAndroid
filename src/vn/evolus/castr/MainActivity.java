package vn.evolus.castr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaTrack;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.VideoBrowserActivity;
import com.google.sample.cast.refplayer.browser.VideoProvider;
import com.google.sample.cast.refplayer.mediaplayer.LocalPlayerActivity;

import java.util.ArrayList;

public class MainActivity extends VideoBrowserActivity {
    public static final String TAG = "Castr";
    private long lastBackAt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        this.getWebView().loadUrl("https://play.evolus.vn/castr/movie-websites.html");
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private XWebView getWebView() {
        return this.findViewById(R.id.webView);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed:");
        if (getWebView().canGoBack()) {
            getWebView().goBack();
            return;
        }

        long now = System.currentTimeMillis();
        if (lastBackAt > now - 1000) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to quit.", Toast.LENGTH_SHORT).show();
            lastBackAt = now;
        }
    }

    public void setPageTitle(String title) {
        this.setTitle(title);
    }

    public void startCasting(String title, String description, String posterURL, String mediaURL) {
        //TODO: implement this
        Log.d(TAG, "startCasting: " + title + " -> url: " + mediaURL + "poster: " + posterURL);
        Intent intent = new Intent(this, LocalPlayerActivity.class);
        /* public static MediaInfo buildMediaInfo(String title, String studio, String subTitle,
            int duration, String url, String mimeType, String imgUrl, String bigImageUrl,
            List<MediaTrack> tracks)*/
        MediaInfo item = VideoProvider.buildMediaInfo(title, "", "", 0, mediaURL, "video/mp4", posterURL, posterURL, new ArrayList<MediaTrack>());
        intent.putExtra("media", item);
        intent.putExtra("shouldStart", false);
        startActivity(intent);
    }
}
