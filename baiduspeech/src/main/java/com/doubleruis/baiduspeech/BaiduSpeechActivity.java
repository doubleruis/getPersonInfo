package com.doubleruis.baiduspeech;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baduspeech.R;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.doubleruis.baiduspeech.entity.EnterRecordAudioEntity;
import com.doubleruis.baiduspeech.helper.HttpUtils;
import com.doubleruis.baiduspeech.until.AutoCheck;
import com.doubleruis.baiduspeech.until.Cons;
import com.doubleruis.baiduspeech.until.MyWebviews;
import com.doubleruis.baiduspeech.until.NetWorks;
import com.doubleruis.baiduspeech.until.PaoPaoTips;
import com.doubleruis.baiduspeech.until.PermissionUtil;
import com.doubleruis.baiduspeech.view.LineWaveVoiceView;
import com.doubleruis.baiduspeech.view.RecordAudioView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.finedo.adcore.async.IAsyncObject;
import cn.finedo.common.domain.ReturnValueDomain;
import cn.finedo.common.non.NonUtil;

/**
 * Created by dell
 * 2019/6/26
 */
public class BaiduSpeechActivity extends AppCompatActivity implements EventListener,
        RecordAudioView.IRecordAudioListener, View.OnClickListener, IAsyncObject {
    protected boolean enableOffline = false; // 测试离线命令词，需要改成true
    protected ImageView btn;
    private EventManager asr;
    protected TextView txtResult;
    private String voiceparam = "";
    private MyWebviews webview;
    private Dialog dialog;

    private static final String TAG = "AudioRecordActivity";
    public static final String KEY_ENTER_RECORD_AUDIO_ENTITY = "enter_record_audio";
    public static final String KEY_AUDIO_BUNDLE = "audio_bundle";
    public static final long DEFAULT_MAX_RECORD_TIME = 600000;
    public static final long DEFAULT_MIN_RECORD_TIME = 2000;
    protected static final int DEFAULT_MIN_TIME_UPDATE_TIME = 1000;

    private RecordAudioView recordAudioView;
    private String audioFileName;
    private ImageView ivClose;
    private TextView tvRecordTips;
    private LinearLayout layoutCancelView;
    private String[] recordStatusDescription;
    private long maxRecordTime = DEFAULT_MAX_RECORD_TIME;
    private long minRecordTime = DEFAULT_MIN_RECORD_TIME;
    private Timer timer;
    private TimerTask timerTask;
    private Handler mainHandler;
    private long recordTotalTime;
    private EnterRecordAudioEntity entity;
    private View contentView;
    private View recordAudioContent;
    private TextView tvReplyName;
    private LineWaveVoiceView mHorVoiceView;
    private View emptyView;
    private String url = "https://www.baidu.com/";//跳转路径
    private ProgressDialog pd; // 等待窗口
    private String message = "等待中";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //end = false;
                    start();
                    break;
                case 2:
                    stop();
                    //end = true;
                    if (!"".equals(voiceparam)) {
                        Intent i = new Intent(BaiduSpeechActivity.this, WebviewActivity.class);
                        //i.putExtra("url","http://wx.hefeimobile.cn/hfydwt-fd-hflywebapp/app/homepage/textai.jsp?voiceparam="+voiceparam);
                        i.putExtra("url", "https://www.baidu.com/");
                        startActivity(i);
                    } else {
                        updateNullUi();
                    }
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 3:
                    stop();
                    //end = true;
                    if (!"".equals(voiceparam)) {
                        updateTxtResult();
                        //String url = "http://192.168.8.104:8080/monitor_webapp/finedo/ai/matchpart";
                        String url = "http://wx.hefeimobile.cn/hfydwt-fd-monitorwebapp/finedo/ai/matchpart";
                        new MyAsyncTask().execute(url);
                    } else {
                        updateNullUi();
                    }
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 4:
                    webview.loadUrl(msg.obj.toString());
                    break;
                case 5:
                    Toast.makeText(BaiduSpeechActivity.this,"服务器出小差了呢～",Toast.LENGTH_LONG);
                    break;
            }
        }
    };

    class MyAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProcess();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProcess();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String returns = "";
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("indexword",voiceparam);
            String result = HttpUtils.dopost(url,map);
            if(result!=null&&!result.equals("")){
                if(!result.equals("1000")){
                    returns = NetWorks.geturl(BaiduSpeechActivity.this)+result.substring(1,result.length()-1);
                }else {
                    handler.sendEmptyMessage(5);
                    return returns;
                }
            }
            Message message = new Message();
            message.what = 4;
            message.obj = returns;
            handler.sendMessage(message);
            return returns;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baiduspeech);
        initView();
        initPermission();
        // 基于sdk集成1.1 初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        // 基于sdk集成1.3 注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public Object call(Object... objects) {
        return null;
    }

    @Override
    public void callback(int requestCode, Object result) {
        switch (requestCode) {
            case 1000:// 获取亲情号码配置
                getcallBack(result);// 获取亲情号码配置-回调
                break;
        }
    }

    public void getcallBack(Object result) {
        ReturnValueDomain<String> ret = (ReturnValueDomain<String>) result;
        if (ret == null) {
            return;
        }

        if (!ret.getResultcode().equals("SUCCESS")) {
            return;
        }

        if (ret.getObject() != null && !ret.getObject().equals(""))
            webview.loadUrl(NetWorks.geturl(BaiduSpeechActivity.this)+ret.getObject());
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
            if (map.get("results_recognition") != null && !"".equals(map.get("results_recognition")))
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

        if (!"".equals(result) && result.length() > 1) {
            voiceparam = result.substring(2, result.length() - 2);
            txtResult.setText(voiceparam);
            stop();
        }
    }

    @Override
    public boolean onRecordPrepare() {
        //检查录音权限
        if (!PermissionUtil.hasSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
            String[] pp = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };
            ActivityCompat.requestPermissions(this, pp, Cons.PERMISSIONS_REQUEST_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRecordStart() {
        recordTotalTime = 0;
        mHorVoiceView.startRecord();
        //initTimer();
        handler.sendEmptyMessage(1);
    }

    @Override
    public boolean onRecordStop() {
        //录制完成
//        //handler.sendEmptyMessage(2);
        handler.sendEmptyMessage(3);
        timer.cancel();
        onBackPressed();
        updateCancelUi();
        return false;
    }

    @Override
    public void onBackPressed() {
//        finish();
//        overridePendingTransition(R.anim.pp_bottom_in, R.anim.pp_bottom_out);
    }

    @Override
    public boolean onRecordCancel() {
        if (timer != null) {
            timer.cancel();
        }
        updateCancelUi();
        return false;
    }

    private void updateCancelUi() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(recordStatusDescription[0]);
        mHorVoiceView.stopRecord();
        deleteTempFile();
    }

    private void updateTxtResult() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(voiceparam);
        mHorVoiceView.stopRecord();
        deleteTempFile();
    }

    private void updateNullUi() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(recordStatusDescription[2]);
        mHorVoiceView.stopRecord();
        deleteTempFile();
    }

    private void deleteTempFile() {
        //取消录制后删除文件
        if (audioFileName != null) {
            File tempFile = new File(audioFileName);
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 上划取消
     */
    @Override
    public void onSlideTop() {
        mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.INVISIBLE);
        layoutCancelView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFingerPress() {
        mHorVoiceView.setVisibility(View.VISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        tvRecordTips.setText(recordStatusDescription[1]);
        layoutCancelView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length <= 0
                || grantResults == null || grantResults.length <= 0) {
            return;
        }
        boolean isGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (isGranted) {
            //暂时先弹出提示,用户需要再次按下语音键
            PaoPaoTips.showDefault(this, getResources().getString(R.string.ar_record_audio_again));
        } else {
            PaoPaoTips.showDefault(this, getResources().getString(R.string.ar_record_audio_fail));
        }
        updateCancelUi();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_record) {
            //onBackPressed();
        } else if (v.getId() == R.id.audio_empty_layout) {
            onBackPressed();
        }
    }

    private void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        btn = (ImageView) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        recordAudioView = (RecordAudioView) findViewById(R.id.iv_recording);
        recordAudioView.setRecordAudioListener(this);
        ivClose = (ImageView) findViewById(R.id.close_record);
        ivClose.setOnClickListener(this);
        tvRecordTips = (TextView) findViewById(R.id.record_tips);
        layoutCancelView = (LinearLayout) findViewById(R.id.pp_layout_cancel);
        contentView = findViewById(R.id.record_content);
        recordAudioContent = findViewById(R.id.layout_record_audio);
        mHorVoiceView = (LineWaveVoiceView) findViewById(R.id.horvoiceview);
        emptyView = findViewById(R.id.audio_empty_layout);
        emptyView.setOnClickListener(this);
        recordStatusDescription = new String[]{
                getString(R.string.ar_feed_sound_press_record),
                getString(R.string.ar_feed_sound_slide_cancel),
                getString(R.string.ar_feed_sound_slide_null),
        };
        mainHandler = new Handler();
        EnterRecordAudioEntity entity = new EnterRecordAudioEntity();
        entity.setSourceType(EnterRecordAudioEntity.SourceType.AUDIO_FEED);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(AudioRecordActivity.KEY_ENTER_RECORD_AUDIO_ENTITY, entity);
//        entity = (EnterRecordAudioEntity) bundle.getSerializable(KEY_ENTER_RECORD_AUDIO_ENTITY);

        //showDialog();
        webview = findViewById(R.id.webview);
        WebSetting();

        //handler.sendEmptyMessage(4);

        pd = new ProgressDialog(BaiduSpeechActivity.this);
    }

    /**
     * 打开等待窗口
     */
    public void showProcess() {
        pd = new ProgressDialog(BaiduSpeechActivity.this);
        pd.setCancelable(false);
        pd.setMessage(message);
        pd.show();
    }

    /**
     * 关闭等待窗口
     */
    public void hideProcess() {
        if (NonUtil.isNotNon(pd)){
            pd.dismiss();
        }
    }

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
        }, enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
    }

    /**
     * 点击停止按钮
     * 基于SDK集成4.1 发送停止事件
     */
    private void stop() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }

    /**
     * 显示弹出框
     */
    private void showDialog() {
        if (dialog == null)
            initDialog();
        dialog.show();
        webview.loadUrl(url);
    }

    private void initDialog() {
        dialog = new Dialog(BaiduSpeechActivity.this, R.style.dialog_center);
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        View view = View.inflate(this, R.layout.activity_dialog_webview, null);
        webview = view.findViewById(R.id.webview);
        //url = "http://wx.hefeimobile.cn/hfydwt-fd-hflywebapp/app/homepage/textai.jsp?voiceparam="+voiceparam;
        window.setGravity(Gravity.CENTER);
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);//不遮挡背景事件
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
        //webview.addJavascriptInterface(new jsSystemExit(), "android");//添加供js调用接口，接口名为android
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
}
