package vn.evolus.castr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;

/**
 * Created by dgthanhan on 3/3/17.
 */

public class XWebView extends WebView {

    public XWebView(Context context) {
        super(context);
        this.init();
    }

    public XWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    private MainActivity findMainActivity() {
        return (MainActivity) this.getContext();
    }

    private void init() {
        this.setWebViewClient(new WebViewClient() {
                                  @Override
                                  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                                      return false;
                                  }

                                  private WebResourceResponse blankResponse() {
                                      return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
                                  }


                                  @Override
                                  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                                      System.out.println("request: " + request.getUrl().toString());
                                      String url = request.getUrl().toString();
                                      if (url.contains("uniad") || url.contains("admicro") || url.contains("ambientplatform") || url.contains("gammaplatform")
                                              || url.contains("scorecardresearch") || url.contains("facebook")
                                              || (url.endsWith(".jpg") && !url.contains("poster"))
                                              || url.contains("google-analytics")
                                              || url.contains("newsuncdn")) {
                                          return blankResponse();
                                      }

                                      return super.shouldInterceptRequest(view, request);
                                  }

                                  @Override
                                  public void onPageFinished(WebView view, String url) {
                                      findMainActivity().setPageTitle(view.getTitle());
                                      String js = "https://play.evolus.vn/castr/services/_generic.js";
                                      view.evaluateJavascript("(function() {" +
                                              "var parent = document.getElementsByTagName('head').item(0);" +
                                              "var script = document.createElement('script');" +
                                              "script.type = 'text/javascript';" +
                                              // Tell the browser to BASE64-decode the string into your script !!!
                                              "script.src = '" + js + "';" +
                                              "parent.appendChild(script);" +
                                              "})()", new ValueCallback<String>() {
                                          @Override
                                          public void onReceiveValue(String s) {
                                              Log.d("DEBUG", "onReceiveValue: " + s);
                                          }
                                      });

                                      super.onPageFinished(view, url);
                                  }
                              }
        );

        WebSettings setting = this.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setDomStorageEnabled(true);

        this.addJavascriptInterface(new JSBridge(), "Castr");

        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                System.out.println("request = [" + request.getResources() + "]");
                request.grant(request.getResources());
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;

            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
        this.clearCache(true);
    }

    public class JSBridge {
        @JavascriptInterface
        public void cast(String title, String description, String posterURL, String mediaURL) {
            findMainActivity().startCasting(title, description, posterURL, mediaURL);
        }
    }
}
