package com.example.mc.group28;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;



public class WebGraphView extends AppCompatActivity {

    private WebView webview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_graph_view);

        webview =(WebView)findViewById(R.id.webView);

        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(true);

        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
//        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl("https://htmlpreview.github.io/?https://raw.githubusercontent.com/janithmehta/CSE535-Assignment-3/master/index.html?token=AM1N8ez-aIKbbCTbohtCVR5z_lG1SihOks5a3UM8wA%3D%3D");


    }
}
