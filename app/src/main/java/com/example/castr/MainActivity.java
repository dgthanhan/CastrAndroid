package com.example.castr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Castr";
    private long lastBackAt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWebView().loadUrl("https://play.evolus.vn/castr/movie-websites.html");
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
    }
}
