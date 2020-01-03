package com.doubleruis.baiduspeech;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import com.baduspeech.R;
import com.doubleruis.baiduspeech.until.MyWebviews;

/**
 * Created by dell
 * 2019/5/30
 */
public class WebviewActivity  extends AppCompatActivity {
    private MyWebviews webview;

    private void WebSetting() {
        webview = (MyWebviews) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setDefaultTextEncodingName("utf-8");

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 不使用缓存，只从网络获取数据
        //webview.addJavascriptInterface(new jsSystemExit(), "android");//添加供js调用接口，接口名为android

        // 启用地理定位
        webview.getSettings().setGeolocationEnabled(true);
        // 设置定位的数据库路径
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.bringToFront();
        // 开启DOM storage API 功能
        webview.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        webview.getSettings().setDatabaseEnabled(true);
        webview.addJavascriptInterface(new jsSystemExit(), "android");//添加供js调用接口，接口名为android
        webview.clearHistory();
        MyWebviews.DisplayFinish df = new MyWebviews.DisplayFinish() {
            @Override
            public void After() {
                webview.loadUrl("javascript:(function() { " +
                        "var videos = document.getElementById('video');" +
                        "videos.play();})()");
            }
        };
        webview.setDf(df);
        String url = getIntent().getStringExtra("url");
        webview.loadUrl(url);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        WebSetting();
    }
    class jsSystemExit {
        @JavascriptInterface
        public void back(){
            finish();
        }
    }
}
