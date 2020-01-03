package com.doubleruis.baduspeech;

import android.Manifest;
import android.app.Dialog;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baduspeech.R;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.doubleruis.baduspeech.entity.EnterRecordAudioEntity;
import com.doubleruis.baduspeech.eventbus.EventBusConfig;
import com.doubleruis.baduspeech.eventbus.MainThreadEvent;
import com.doubleruis.baduspeech.until.AudioRecordJumpUtil;
import com.doubleruis.baduspeech.until.AutoCheck;
import com.doubleruis.baduspeech.until.MyWebviews;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MainActivity extends AppCompatActivity implements EventListener  {
    protected TextView txtResult;
    protected ImageView btn;
    private EventManager asr;
    private boolean logTime = true;
    protected boolean enableOffline = false; // 测试离线命令词，需要改成true
    private String voiceparam = "";
    private boolean end = false;
    private MyWebviews webview;
    private Dialog dialog;

    /**
     * 基于SDK集成2.2 发送开始事件
     * 点击开始按钮
     * 测试参数填在这里
     */
    private void start() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params.put(SpeechConstant.NLU, "enable");
        // params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        //params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        // 复制此段可以自动检测错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        },enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
        printLog("输入参数：" + json);
    }

    /**
     * 点击停止按钮
     *  基于SDK集成4.1 发送停止事件
     */
    private void stop() {
        printLog("停止识别：ASR_STOP");
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }


    /**
     * enableOffline设为true时，在onCreate中调用
     * 基于SDK离线命令词1.4 加载离线资源(离线时使用)
     */
    private void loadOfflineEngine() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    /**
     * enableOffline为true时，在onDestory中调用，与loadOfflineEngine对应
     * 基于SDK集成5.1 卸载离线资源步骤(离线时使用)
     */
    private void unloadOfflineEngine() {
        asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    end = false;
                    start();
                    break;
                case 2:
                    stop();
                    end = true;
                    if(!"".equals(voiceparam)){
                        Intent i = new Intent(MainActivity.this,WebviewActivity.class);
                        //i.putExtra("url","http://wx.hefeimobile.cn/hfydwt-fd-hflywebapp/app/homepage/textai.jsp?voiceparam="+voiceparam);
                        i.putExtra("url","https://www.baidu.com/");
                        startActivity(i);
                    }else{
                        Intent intent =new Intent();
                        intent.setAction("action.refreshNull");
                        sendBroadcast(intent);
                    }
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 3:
                    stop();
                    end = true;
                    if(!"".equals(voiceparam)){
//                        Intent i = new Intent(MainActivity.this,WebviewActivity.class);
//                        //i.putExtra("url","http://wx.hefeimobile.cn/hfydwt-fd-hflywebapp/app/homepage/textai.jsp?voiceparam="+voiceparam);
//                        i.putExtra("url","https://www.baidu.com/");
//                        startActivity(i);
                        //关闭下方录音按钮
//                        Intent intent =new Intent();
//                        intent.setAction("action.close");
//                        sendBroadcast(intent);
                        handler.sendEmptyMessageDelayed(4,100);
                    }else{
                        Intent intent =new Intent();
                        intent.setAction("action.refreshNull");
                        sendBroadcast(intent);
                    }
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 4:
                    showDialog();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPermission();
        // 基于sdk集成1.1 初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        // 基于sdk集成1.3 注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AudioRecordJumpUtil.startRecordAudio(MainActivity.this);
            }
        });
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        txtResult.setText("");
        voiceparam = "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 基于SDK集成4.2 发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
        // 基于SDK集成5.2 退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

    // 基于sdk集成1.2 自定义输出事件类 EventListener 回调方法
    // 基于SDK集成3.1 开始回调事件
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        String result = "";
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
            Map map = (Map) JSON.parse(params);
            if(map.get("results_recognition") !=null && !"".equals(map.get("results_recognition")))
                result += map.get("results_recognition");
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params != null && params.contains("\"nlu_result\"")) {
                if (length > 0 && data.length > 0) {
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            }
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        printLog(logTxt);

        if(!"".equals(result) && result.length()>1 && !end){
            voiceparam = result.substring(2,result.length()-2);
            txtResult.setText(voiceparam);
        }
    }

    private void printLog(String text) {
        if (logTime) {
            text += "  ;time=" + System.currentTimeMillis();
        }
        text += "\n";
        Log.i(getClass().getName(), text);
    }

    private void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        btn = (ImageView) findViewById(R.id.btn);

        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction("action.refreshStart");
        intentFilter.addAction("action.refreshEnd");
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);


        IntentFilter intentFilter2 =new IntentFilter();
        intentFilter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(NetworkReceiver, intentFilter2);

        //AudioRecordJumpUtil.startRecordAudio(MainActivity.this);

        showDialog();
    }

    private void WebSetting() {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(false);
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
    }

    class jsSystemExit {
        @JavascriptInterface
        public void back(){
            finish();
        }
    }

    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.refreshStart")){//要执行的逻辑
                handler.sendEmptyMessage(1);
            }else if(action.equals("action.refreshEnd")){
                //handler.sendEmptyMessage(2);
                handler.sendEmptyMessage(3);
            }
        }
    };

    private BroadcastReceiver NetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(null==intent){
                return;
            }
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getApplicationContext()
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(null==networkInfo||!networkInfo.isConnected()){
                return;
            }
//            long currentTimeMillis = System.currentTimeMillis();
//            if(currentTimeMillis- GlobalCacheManager.g)
            Intent servicceIntent = new Intent(context,TestService.getClass());
            context.startService(servicceIntent);
        }
    };

    private IntentService TestService = new IntentService("TestService") {
        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Toast.makeText(MainActivity.this,"网络发生变化",Toast.LENGTH_LONG);
        }
    };

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    /**
     * 显示弹出框
     */
    private void showDialog() {
        if(dialog == null)
        initDialog();
        dialog.show();
    }

    private void initDialog(){
        dialog = new Dialog(MainActivity.this,R.style.dialog_center);
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        View view = View.inflate(this,R.layout.activity_dialog_webview,null);
        webview = view.findViewById(R.id.webview);
        WebSetting();
        //String url = "http://wx.hefeimobile.cn/hfydwt-fd-hflywebapp/app/homepage/textai.jsp?voiceparam="+voiceparam;
        String url = "https://www.baidu.com/";
        webview.loadUrl(url);

        window.setGravity(Gravity.CENTER);
        window.setContentView(view);
        //window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        //window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);//不遮挡背景事件
    }
}