package com.store.faskcamera;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LiveCameraJetracer extends AppCompatActivity {
    private static final String TAG = "4DBG";
    private WebView webView;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera_jetracer);

        connectionManager = ConnectionManager.getInstance();

        webView = findViewById(R.id.streamWebView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setInitialScale(450);

        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.13");
        connectionManager.setConnectionListener(() -> connectionManager.sendMessage("control_off"));
        connectionManager.startConnection(ip, Integer.parseInt(Constants.webViewPortControl));

        connectToStream();
    }

    private void connectToStream() {
        Log.i(TAG, "<strm> CONNECT");

        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.13");
        Log.i(TAG, "http://" + ip + ":5000");

        webView.loadUrl("http://" + ip + ":5000");
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectFromStream();
    }

    private void disconnectFromStream() {
        Log.i(TAG, "<strm> DISCONNECT");
        webView.loadUrl("");
    }
}
