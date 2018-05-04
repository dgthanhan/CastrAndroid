package vn.evolus.castr;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.MediaRouteChooserDialog;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private static final String APP_URL = "https://play.evolus.vn/castr/movie-websites.html";
    private long lastBackAt = 0;
    XWebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        webView = this.getWebView();
        webView.loadUrl(APP_URL);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.YELLOW);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (webView.canGoBack()) {
                    webView.reload();
                    supportInvalidateOptionsMenu();
                } else {
                    gotoHomeRunnable.run();
                }
            }
        });
    }

    Runnable gotoHomeRunnable = new Runnable() {
        @Override
        public void run() {
            webView.clearHistoryRequested();
            supportInvalidateOptionsMenu();
            webView.loadUrl(APP_URL);
            System.out.println("Go home called");
        }
    };

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            gotoHomeRunnable.run();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_home).setVisible(webView != null && webView.canGoBack());
        return super.onPrepareOptionsMenu(menu);
    }

    private XWebView getWebView() {
        return this.findViewById(R.id.webView);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed:");
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            supportInvalidateOptionsMenu();
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
        this.getSupportActionBar().setTitle(title);
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
                    LocalPlayerActivity.loadRemoteMedia(MainActivity.this, mCastSession, item, 0, true, gotoHomeRunnable);
                } else {
                    pendingItem = item;
                    showMediaRouteChooser();
                }
            }
        });
    }
    private void showMediaRouteChooser() {
        final MediaRouteChooserDialog mediaRouteChooserDialog = new MediaRouteChooserDialog(MainActivity.this);
        MediaRouteSelector.Builder builder = new MediaRouteSelector.Builder();
        builder.addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK);

        mediaRouteChooserDialog.setRouteSelector(builder.build());
        mediaRouteChooserDialog.show();
        mediaRouteChooserDialog.refreshRoutes();
        mediaRouteChooserDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                MediaRouter mediaRouter = MediaRouter.getInstance(MainActivity.this);
                //No route found
                if (mediaRouter.getRoutes() == null || mediaRouter.getRoutes().size() == 0) {
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
            LocalPlayerActivity.loadRemoteMedia(this, mCastSession, pendingItem, 0, true, gotoHomeRunnable);
            pendingItem = null;
        }
    }

    public void refreshDone() {
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);

    }
}
