package vn.evolus.castr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.MediaRouteChooserDialog;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
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
    MediaInfo pendingItem = null;
    public void startCasting(String title, String description, String posterURL, String mediaURL) {
        Log.d(TAG, "startCasting: " + title + " -> url: " + mediaURL + "poster: " + posterURL);
        final MediaInfo item = VideoProvider.buildMediaInfo(title, "", "", 0, mediaURL, "video/mp4", posterURL, posterURL, new ArrayList<MediaTrack>());
        this.pendingItem = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCastSession != null && mCastSession.isConnected()) {
                    pendingItem = null;
                    LocalPlayerActivity.loadRemoteMedia(MainActivity.this, mCastSession, item, 0, true);
                } else {
                    pendingItem = item;
                    showMediaRouteChooser();
                }
            }
        });
    }
    private void showMediaRouteChooser() {
        MediaRouteChooserDialog mediaRouteChooserDialog = new MediaRouteChooserDialog(MainActivity.this);
        MediaRouteSelector.Builder builder = new MediaRouteSelector.Builder();
        builder.addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK);

        mediaRouteChooserDialog.setRouteSelector(builder.build());
        mediaRouteChooserDialog.show();
        mediaRouteChooserDialog.refreshRoutes();
        mediaRouteChooserDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if ((mCastSession == null || !mCastSession.isConnected()) && pendingItem != null) {
                    Intent intent = new Intent(MainActivity.this, LocalPlayerActivity.class);
                    intent.putExtra("media", pendingItem);
                    intent.putExtra("shouldStart", true);
                    intent.putExtra("playOnRemote", false);
                    startActivity(intent);
                    pendingItem = null;
                }
            }
        });
    }

    @Override
    protected void onApplicationConnected() {
        super.onApplicationConnected();
        if (pendingItem != null)  {
            LocalPlayerActivity.loadRemoteMedia(this, mCastSession, pendingItem, 0, true);
            pendingItem = null;
        }
    }
}
