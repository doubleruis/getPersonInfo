package com.doubleruis.baiduspeech;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baduspeech.R;
import com.baidu.speech.asr.SpeechConstant;
import com.doubleruis.baiduspeech.helper.HttpUtils;
import com.doubleruis.baiduspeech.params.OfflineRecogParams;
import com.doubleruis.baiduspeech.recog.MyRecognizer;
import com.doubleruis.baiduspeech.recog.listener.IRecogListener;
import com.doubleruis.baiduspeech.recog.listener.MessageStatusRecogListener;
import com.doubleruis.baiduspeech.until.AutoCheck;
import com.doubleruis.baiduspeech.until.Cons;
import com.doubleruis.baiduspeech.until.MyWebviews;
import com.doubleruis.baiduspeech.until.NetWorks;
import com.doubleruis.baiduspeech.until.PaoPaoTips;
import com.doubleruis.baiduspeech.until.PermissionUtil;
import com.doubleruis.baiduspeech.view.LineWaveVoiceView;
import com.doubleruis.baiduspeech.view.RecordAudioView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.finedo.adcore.async.IAsyncObject;
import cn.finedo.common.domain.ReturnValueDomain;
import cn.finedo.common.non.NonUtil;

import static com.doubleruis.baiduspeech.recog.IStatus.STATUS_FINISHED;
import static com.doubleruis.baiduspeech.recog.IStatus.STATUS_FINISHED_ERROR;

/**
 * Created by dell
 * 2019/6/27
 */
@SuppressWarnings("all")
public class SpeechActivity extends AppCompatActivity implements
        RecordAudioView.IRecordAudioListener, View.OnClickListener, IAsyncObject {
    protected boolean enableOffline = false; // 测试离线命令词，需要改成true
    protected ImageView btn;
    protected TextView txtResult;
    private String voiceparam = "";
    private MyWebviews webview;

    private RecordAudioView recordAudioView;
    private String audioFileName;
    private ImageView ivClose;
    private TextView tvRecordTips;
    private LinearLayout layoutCancelView;
    private String[] recordStatusDescription;
    private LineWaveVoiceView mHorVoiceView;
    private View emptyView;
    private ProgressDialog pd; // 等待窗口
    private String message = "等待中";
    private boolean end = false;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    end = false;
                    start();
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 2:
                    stop();
                    end = true;
                    if (!"".equals(voiceparam)) {
                        Intent i = new Intent(SpeechActivity.this, WebviewActivity.class);
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
                    end = true;
                    if (!"".equals(voiceparam)) {
                        updateTxtResult();
                        //String url = "http://192.168.8.104:8080/monitor_webapp/finedo/ai/matchpart";
                        String url = "http://wx.hefeimobile.cn/hfydwt-fd-monitorwebapp/finedo/ai/matchpart";
                        new MyAsyncTask().execute(url);
                    } else {
                        updateNullUi();
                    }
                    break;
                case 4:
                    webview.loadUrl(msg.obj.toString());
                    txtResult.setText("");
                    voiceparam = "";
                    break;
                case 5:
                    Toast.makeText(SpeechActivity.this,"服务器出小差了呢～",Toast.LENGTH_LONG);
                    break;
            }
        }
    };

    private Handler handler2 = new Handler() {

        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleMsg(msg);
        }

    };

    class MyAsyncTask extends AsyncTask<String,Void,String> {
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
                    returns = NetWorks.geturl(SpeechActivity.this)+result.substring(1,result.length()-1);
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
        setContentView(R.layout.activity_speech);
        initView();
        initPermission();
        // 基于DEMO集成第1.1, 1.2, 1.3 步骤 初始化EventManager类并注册自定义输出事件
        // DEMO集成步骤 1.2 新建一个回调类，识别引擎会回调这个类告知重要状态和识别结果
        IRecogListener listener = new MessageStatusRecogListener(handler2);
        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(this, listener);
        if (enableOffline) {
            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
            Map<String, Object> offlineParams = OfflineRecogParams.fetchOfflineParams();
            myRecognizer.loadOfflineEngine(offlineParams);
        }
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        myRecognizer.release();
        super.onDestroy();
    }

    @Override
    public Object call(Object... objects) {
        return null;
    }

    @Override
    public void callback(int requestCode, Object result) {
        switch (requestCode) {
            case 1000://
                getcallBack(result);//
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
            webview.loadUrl(NetWorks.geturl(SpeechActivity.this)+ret.getObject());
    }

    protected void handleMsg(Message msg) {
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED_ERROR:
                if (msg.arg2 == 1) {
                    txtResult.setText(msg.obj.toString());
                    mHorVoiceView.setVisibility(View.INVISIBLE);
                    mHorVoiceView.stopRecord();
                }
                break;
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    txtResult.setText(msg.obj.toString());
                    voiceparam = msg.obj.toString();
                    tvRecordTips.setVisibility(View.VISIBLE);
                    tvRecordTips.setText(voiceparam);
                    if(end){
                        handler.sendEmptyMessage(3);
                    }
                }
                break;
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
        mHorVoiceView.startRecord();
        //initTimer();
        //handler.sendEmptyMessage(1);
        end = false;
        stop();
        start();
        txtResult.setText("");
        voiceparam = "";
    }

    @Override
    public boolean onRecordStop() {
        //录制完成
//        //handler.sendEmptyMessage(2);
        handler.sendEmptyMessage(3);
        //handler.sendEmptyMessageDelayed(3,1000);
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
        updateCancelUi();
        return false;
    }

    private void updateCancelUi() {
        //mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(recordStatusDescription[0]);
        //mHorVoiceView.stopRecord();
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
        //mHorVoiceView.setVisibility(View.INVISIBLE);
        tvRecordTips.setVisibility(View.VISIBLE);
        layoutCancelView.setVisibility(View.INVISIBLE);
        tvRecordTips.setText(recordStatusDescription[2]);
        //mHorVoiceView.stopRecord();
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
        mHorVoiceView = (LineWaveVoiceView) findViewById(R.id.horvoiceview);
        emptyView = findViewById(R.id.audio_empty_layout);
        emptyView.setOnClickListener(this);
        recordStatusDescription = new String[]{
                getString(R.string.ar_feed_sound_press_record),
                getString(R.string.ar_feed_sound_slide_cancel),
                //getString(R.string.ar_feed_sound_slide_null),
                getString(R.string.ar_feed_sound_press_record)
        };

        webview = findViewById(R.id.webview);
        WebSetting();
        pd = new ProgressDialog(SpeechActivity.this);
    }

    /**
     * 打开等待窗口
     */
    public void showProcess() {
        pd = new ProgressDialog(SpeechActivity.this);
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
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    protected void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }

        params.put(SpeechConstant.SOUND_START, R.raw.bdspeech_recognition_start);
        params.put(SpeechConstant.SOUND_END, R.raw.bdspeech_speech_end);
        params.put(SpeechConstant.SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
        params.put(SpeechConstant.SOUND_ERROR, R.raw.bdspeech_recognition_error);
        params.put(SpeechConstant.SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);

        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        //Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        //txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    protected void stop() {
        myRecognizer.stop();
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
}
