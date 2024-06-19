package com.store.faskcamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class JetracerControlActivity extends AppCompatActivity {
    private Button buttonForward, buttonLeft, buttonRight, buttonForwardLeft, buttonForwardRight, buttonReverse, buttonReverseLeft, buttonReverseRight;
    private WebView webView;
    private static final String TAG = "4DBG";
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jetracer_control);

        connectionManager = ConnectionManager.getInstance();

        buttonForward = findViewById(R.id.buttonForward);
        buttonLeft = findViewById(R.id.buttonLeft);
        buttonRight = findViewById(R.id.buttonRight);
        buttonForwardLeft = findViewById(R.id.buttonForwardLeft);
        buttonForwardRight = findViewById(R.id.buttonForwardRight);
        buttonReverse = findViewById(R.id.buttonReverse);
        buttonReverseRight = findViewById(R.id.buttonReverseRight);
        buttonReverseLeft = findViewById(R.id.buttonReverseLeft);

        setupButton(buttonForward, "forward");
        setupButton(buttonLeft, "left");
        setupButton(buttonRight, "right");
        setupButton(buttonForwardLeft, "forward_left");
        setupButton(buttonForwardRight, "forward_right");
        setupButton(buttonReverse, "reverse");
        setupButton(buttonReverseRight, "reverse_right");
        setupButton(buttonReverseLeft, "reverse_left");

        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.13");
        connectionManager.setConnectionListener(() -> connectionManager.sendMessage("control_on"));
        connectionManager.startConnection(ip, Integer.parseInt(Constants.webViewPortControl));

        webView = findViewById(R.id.webViewControl);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setInitialScale(450);

        connectToStream();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButton(Button button, String startCommand) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    connectionManager.sendMessage(startCommand);
                    return true;
                case MotionEvent.ACTION_UP:
                    connectionManager.sendMessage("stop");
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionManager.stopConnection();
    }

    private void connectToStream() {
        Log.i(TAG, "<strm> CONNECT");

        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.13");
        Log.i(TAG, "http://" + ip + ":" + Constants.flaskVideoFeed);

        webView.loadUrl("http://" + ip + ":" + Constants.flaskVideoFeed);
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
