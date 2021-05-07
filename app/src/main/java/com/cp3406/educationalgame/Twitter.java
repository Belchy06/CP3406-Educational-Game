package com.cp3406.educationalgame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class Twitter extends AppCompatActivity {
    public static int TWITTER_REQUEST = 1;

    private WebView webView;
    private TextView info;
    private final twitter4j.Twitter twitter = TwitterFactory.getSingleton();
    private String oauthVerifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        info = findViewById(R.id.info);
        webView = findViewById(R.id.web_view);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                String loadingText = "Loading...";
                info.setText(loadingText);
                String message;
                if (url.startsWith("http://jcu.edu.au")) {
                    Uri uri = Uri.parse(url);
                    oauthVerifier = uri.getQueryParameter("oauth_verifier");
                    if (oauthVerifier != null) {
                        message = "Authenticated";
                        updateTwitterConfiguration();
                        webView.loadData("done", "text/html", null);
                    } else {
                        message = "Not authenticated";
                    }
                    info.setText(message);
                }
            }
        });

        run(() -> {
            try {
                RequestToken requestToken = twitter.getOAuthRequestToken();
                final String requestUrl = requestToken.getAuthenticationURL();
                runOnUiThread(() -> webView.loadUrl(requestUrl));
            } catch (final Exception e) {
                runOnUiThread(() -> info.setText(e.toString()));
            }
        });
    }

    private void updateTwitterConfiguration() {
        run(() -> {
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(oauthVerifier);
                twitter.setOAuthAccessToken(accessToken);
                exitIntent(true);
            } catch (final Exception e) {
                Log.e("Authenticate", e.toString());
                runOnUiThread(() -> info.setText(e.toString()));
                exitIntent(false);
            }
        });

    }

    private void exitIntent(boolean auth) {
        Intent intent = new Intent();
        intent.putExtra("AUTH", auth);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
