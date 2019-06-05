package com.doubleruis.getPersonInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.doubleruis.getPersonInfo.utils.BaseFunction;
import com.doubleruis.getPersonInfo.utils.LocalStorage;
import com.doubleruis.getPersonInfo.utils.NonUtil;
import com.doubleruis.getPersonInfo.utils.ToolUtils;
import com.facedetectcamera.activity.ui.FaceOverlayView;
import com.facedetectcamera.model.FaceResult;
import com.facedetectcamera.utils.CameraErrorCallback;
import com.facedetectcamera.utils.ImageUtils;
import com.facedetectcamera.utils.Util;
import com.seeta.sdk.AgFaceMark;
import com.seeta.sdk.FaceDetector;
import com.seeta.sdk.PointDetector;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import com.seeta.sdk.SeetaRect;
import com.seetatech.seetaverify.constants.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.seeta.sdk.AgFaceMark.Status.REAL;

//import cn.finedo.fadp.until.FocusCirceView;

/**
 * Created by Nguyen on 5/20/2016.
 */
public final class FaceDetectRGBActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public static final String TAG = FaceDetectRGBActivity.class.getSimpleName();

    private Camera mCamera;
    private float oldDist = 1f;
    private int cameraId = 0;  //设置前后摄像头(0为后置，1为前置)
    private int iBack = 1;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    private int previewWidth;
    private int previewHeight;

    // The surface view for the camera data
    private SurfaceView mView;
    private Camera.Size mPreviewSize;

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    public FaceDetector seetaFaceDetector = null;
    public PointDetector seetaPointDetector = null;
    public AgFaceMark processor = null;

    private boolean isThreadWorking = false;
    private Handler handler = new Handler();
    private FaceDetectThread detectThread = null;
    private String PATH_MODEL;

    private FaceResult face = new FaceResult();
    private float[] mFaceRect = new float[512];
    private float[] mFaceMarks = new float[212];
    private int[] mFaceStates = new int[6];

    private int BGR_DEFINE_WIDTH = 540;
    private int BGR_DEFINE_HEIGHT = 2200;//是为了保证缓冲区足够大；实际按宽高比，等比例缩放
    private byte[] mBRGdata = new byte[BGR_DEFINE_WIDTH * BGR_DEFINE_HEIGHT * 3];

    private String BUNDLE_CAMERA_ID = "camera";

    private TextView zoomText;
    //private FocusCirceView focusCirceView;

    private Button iv_take_photo;
    private boolean bCapStart = false;

    public static class DetectInfo {
        public static int statusIndex;
        public static AgFaceMark.Status status;
        public static int leftX;
        public static int leftY;
        public static SeetaRect faceInfo = new SeetaRect();
        public static long spent = -1;
    }

    public DetectInfo detectInfo = new DetectInfo();
    public int resultIndex = -1;

    private TextView showResult;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_camera_viewer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//让屏幕保持不暗不关闭
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SeetaInit();
            }
        }).start();

        findViewById();

        mFaceView = new FaceOverlayView(this);
        addContentView(mFaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initData();
    }

    private void findViewById() {
        mView = (SurfaceView) findViewById(R.id.surfaceview);
        showResult = (TextView) findViewById(R.id.show_result);
        zoomText = (TextView) findViewById(R.id.zoom);
        //focusCirceView = new FocusCirceView(FaceDetectRGBActivity.this);
        iv_take_photo = (Button) findViewById(R.id.iv_take_photo);
        iv_take_photo.setText("  开始检测  ");
        iv_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bCapStart = true;
                iv_take_photo.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 获取两指距离
     *
     * @param event
     * @return
     */
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 调焦
     *
     * @param isZoomIn true 放大 false 缩小
     * @param camera
     */
    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            zoomText.setVisibility(View.VISIBLE);
            zoomText.setText("x" + ToolUtils.getPictureZoom(camera, zoom));
            LocalStorage.putValues(FaceDetectRGBActivity.this, "progress1", "progress1", zoom + "");
            camera.setParameters(params);
        } else {

        }
    }

    /**
     * 聚焦
     *
     * @param event
     * @param camera_
     */
    private static void handleFocus(MotionEvent event, Camera camera_) {
        if (camera_ != null) {
            //cancel previous actions
            camera_.cancelAutoFocus();
            Camera.Parameters parameters = null;
            Rect focusRect = null;
            try {
                parameters = camera_.getParameters();
                Camera.Size previewSize = parameters.getPreviewSize();
                focusRect = calculateTapArea(event.getX(), event.getY(), 1f,previewSize);
                Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f,previewSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
            if (parameters != null) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    List<Camera.Area> focus = new ArrayList<Camera.Area>();
                    focus.add(new Camera.Area(focusRect, 1000));
                    parameters.setFocusAreas(focus);

                    if (parameters.getMaxNumMeteringAreas() > 0) {
                        List<Camera.Area> metering = new ArrayList<Camera.Area>();
                        metering.add(new Camera.Area(focusRect, 1000));
                        parameters.setMeteringAreas(metering);
                    }
                }

                try {
                    camera_.setParameters(parameters);
                    camera_.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
//            bCapStart = true;
//            iv_take_photo.setVisibility(View.GONE);
            //handleFocus(event, mCamera);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float x = event.getX();
                    float y = event.getY();
//                    if (focusCirceView != null) {
//                        focusCirceView.myViewScaleAnimation(focusCirceView);//动画效果
//                        focusCirceView.setPoint(x, y);
//                        addContentView(focusCirceView, new
//                                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT)); //添加视图FocusCirceView
//                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    zoomText.setVisibility(View.GONE);
                    //抬起时清除画布,并移除视图
//                    if (focusCirceView != null && (ViewGroup) focusCirceView.getParent()!=null) {
//                        ((ViewGroup) focusCirceView.getParent()).removeView(focusCirceView);
//                    }
//                    focusCirceView.deleteCanvas();
                    break;
            }
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
                case MotionEvent.ACTION_UP:
                    //抬起时清除画布,并移除视图
//                    focusCirceView.deleteCanvas();
//                    if (focusCirceView != null && (ViewGroup) focusCirceView.getParent()!=null) {
//                        ((ViewGroup) focusCirceView.getParent()).removeView(focusCirceView);
//                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.face_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.menu.face_menu:
                showResult.setText("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        new BaseFunction(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = mView.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CAMERA_ID, cameraId);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //Find the total number of cameras available
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        mCamera = Camera.open(cameraId);
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFaceView.setFront(true);
        }
        try {
            mCamera.setPreviewDisplay(mView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "Could not preview the image.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // We have no surface, return immediately:
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        // Try to stop the current preview:
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }

        configureCamera(width, height);
        setDisplayOrientation();
        setErrorCallback();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            iBack = 1;
        } else {
            iBack = 0;
        }
        // Everything is configured! Finally start the camera preview again:
        startPreview();
    }

    private void setErrorCallback() {
        mCamera.setErrorCallback(mErrorCallback);
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = Util.getDisplayRotation(FaceDetectRGBActivity.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, cameraId);

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private void configureCamera(int width, int height) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> preSupportedSizes = parameters.getSupportedPreviewSizes();
            parameters.setPreviewFormat(ImageFormat.NV21);
            // Set the PreviewSize and AutoFocus:
            //setOptimalPreviewSize(parameters, width, height);
            // And set the parameters:
            if (mPreviewSize == null) {
                mPreviewSize = ImageUtils.getOptimalPreviewSize(preSupportedSizes, mView.getWidth(), mView.getHeight());
            }
            if (mPreviewSize != null) {
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
                mFaceView.setPreviewWidth(previewWidth);
                mFaceView.setPreviewHeight(previewHeight);
            }
            setAutoFocus(parameters);
            String progress1 = LocalStorage.getValues(FaceDetectRGBActivity.this,
                    "progress1", "progress1");
            if (NonUtil.isNotNon(progress1)) {
                parameters.setZoom(Integer.parseInt(progress1));
                zoomText.setText("x" + ToolUtils.getPictureZoom(mCamera, Integer.parseInt(progress1)));
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private void startPreview() {
        if (mCamera != null) {
            isThreadWorking = false;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            counter = 0;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPreviewFrame(byte[] _data, Camera _camera) {
        if (!isThreadWorking) {
            if (counter == 0)
                start = System.currentTimeMillis();

            isThreadWorking = true;
            waitForFdetThreadComplete();
            detectThread = new FaceDetectThread(handler, this);
            detectThread.setData(_data);
            detectThread.start();
        }
    }

    String GetStateString(int resultIndex) {
        String resultString = "人像模糊";  //"face too blur"
        if (resultIndex != -1) {//存在人脸
            AgFaceMark.Status status = AgFaceMark.Status.values()[resultIndex];
            switch (status) {
                case REAL:
                    resultString = "清晰的人像";   // "real face"
                    break;
                case SPOOF:
                    resultString = "不真实的人像";   //  "fake face"
                    break;
                case FUZZY:
                    resultString = "人像模糊";
                    break;
                case DETECTING:
                    resultString = "检测中...";
                default:
                    break;
            }
        }
        return resultString;
    }

    /*

     * byte[] data保存的是纯BGR的数据，而非完整的图片文件数据

     */
    static public Bitmap createMyBitmap(byte[] data, int width, int height) {
        int[] colors = convertByteToColor(data);
        if (colors == null) {
            return null;
        }
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
        return bmp;
    }

    /*
     * 将BGR数组转化为像素数组
     */
    private static int[] convertByteToColor(byte[] data) {

        int size = data.length;
        if (size == 0) {
            return null;
        }

        // 理论上data的长度应该是3的倍数，这里做个兼容
        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }


        int[] color = new int[size / 3 + arg];

        int red, green, blue;

        if (arg == 0) {                                  //  正好是3的倍数

            for (int i = 0; i < color.length; ++i) {
                color[i] = (data[i * 3 + 2] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 0] & 0x000000FF) |
                        0xFF000000;
            }

        } else {                                      // 不是3的倍数

            for (int i = 0; i < color.length - 1; ++i) {

                color[i] = (data[i * 3 + 2] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 0] & 0x000000FF) |
                        0xFF000000;
            }
            color[color.length - 1] = 0xFF000000;                   // 最后一个像素用黑色填充
        }
        return color;
    }

    public void saveImage(Bitmap bmp) {
        String picPath = "";
        if (bmp != null) {
            File dir = new File(Environment.getExternalStorageDirectory(), "hxx");
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");//设置日期格式
            String pictureName = df.format(new Date()); //用当前时间做文件名
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                File file = new File(dir, pictureName + ".jpg");
//                compressBmpToFile(bmp,file);
                picPath = file.getAbsolutePath();
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("pic_path", picPath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }

    int VIPLFindMaxFace(SeetaRect[] face_infos)//返回最大人脸的索引值
    {
        int length = face_infos.length;
        if (length <= 0) return -1;

        int index = 0;
        int targetIndex = -1;
        double faceMaxWidth = 0;
        for (; index < length; ++index) {
            if (face_infos[index].width > faceMaxWidth) {
                faceMaxWidth = face_infos[index].width;
                targetIndex = index;
            }
        }
        return targetIndex;
    }

    private void waitForFdetThreadComplete() {
        if (detectThread == null) {
            return;
        }
        if (detectThread.isAlive()) {
            try {
                detectThread.join();
                detectThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // fps detect face (not FPS of camera)
    long start, end;
    int counter = 0;
    double fps;

    /**
     * Do face detect in thread
     */
    private class FaceDetectThread extends Thread {
        private Handler handler;
        private byte[] data = null;
        private Context ctx;
        private Bitmap faceCroped;

        public FaceDetectThread(Handler handler, Context ctx) {
            this.ctx = ctx;
            this.handler = handler;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public void run() {
            try {
//                Log.e("myerror",previewWidth+"w:h"+previewHeight);
                int rlDefineH = previewHeight * BGR_DEFINE_WIDTH / previewWidth;
                long startTime = System.currentTimeMillis();    //获取开始时间
                //we keep the image is  h>w
                //因为不同厂商的摄像头sense的原因，我们统一保证图像是竖直的
                //本函数会进行纠正处理(把转置的变成竖立的)
                //因为不同厂商的摄像头sense的原因，我们统一保证图像是竖直的
                //本函数会进行纠正处理(把转置的变成竖立的)
                if (iBack == 1) {
                    AgFaceMark.Nv12toBGRVer(data, previewWidth, previewHeight, mBRGdata, BGR_DEFINE_WIDTH, rlDefineH);
                } else {
                    AgFaceMark.Nv12toBGR(data, previewWidth, previewHeight, mBRGdata, BGR_DEFINE_WIDTH, rlDefineH);
                }
                //long endTime = System.currentTimeMillis();    //获取结束时间
//                Log.e("myerror",(endTime-startTime)+" cost @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                int newH = rlDefineH;
                int newW = BGR_DEFINE_WIDTH;
                if (newH < newW) {
                    int tmp = newW;
                    newW = newH;
                    newH = tmp;
                }

                SeetaImageData imageData = new SeetaImageData(newW, newH, 3);
                imageData.data = mBRGdata;
                startTime = System.currentTimeMillis();    //获取开始时间
                SeetaRect[] faceInfos = seetaFaceDetector.Detect(imageData);

                int maxFaceID = VIPLFindMaxFace(faceInfos);

                detectInfo.statusIndex = -1;

                if (maxFaceID != -1 && bCapStart) {//存在人脸
                    SeetaPointF[] points5 = new SeetaPointF[5];

                    boolean isCenter = seetaPointDetector.Detect(imageData, faceInfos[maxFaceID], points5);//五点检测

                    detectInfo.leftX = faceInfos[maxFaceID].x;
                    detectInfo.leftY = faceInfos[maxFaceID].y;

                    detectInfo.faceInfo.x = faceInfos[maxFaceID].x;
                    detectInfo.faceInfo.y = faceInfos[maxFaceID].y;
                    detectInfo.faceInfo.width = faceInfos[maxFaceID].width;
                    detectInfo.faceInfo.height = faceInfos[maxFaceID].height;


                    mFaceRect[0] = detectInfo.faceInfo.x * 1.0f / newW;
                    mFaceRect[1] = detectInfo.faceInfo.y * 1.0f / newH;
                    mFaceRect[2] = (detectInfo.faceInfo.x + detectInfo.faceInfo.width) * 1.0f / newW;
                    mFaceRect[3] = (detectInfo.faceInfo.y + detectInfo.faceInfo.height) * 1.0f / newH;

                    boolean incenter = false;
                    float px = mFaceRect[0] + mFaceRect[2] / 2;
                    float py = mFaceRect[1] + mFaceRect[3] / 2;
                    if (px > 0.33 && px < 0.66 && py > 0.63 && py < 0.93) {
                        //代表中心点在显示的中心，本图片可用
                        incenter = true;
                    }
                    //endTime = System.currentTimeMillis();    //获取结束时间
//                    Log.e("myerror 22",(endTime-startTime)+" cost @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    long start = System.currentTimeMillis();
                    AgFaceMark.Status status = processor.Predict(imageData, faceInfos[maxFaceID], points5);
                    long end = System.currentTimeMillis();
                    long spent = end - start;
//                    Log.e("myerror", " " + spent + "processor  cost @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    DetectInfo.spent = spent;
                    detectInfo.statusIndex = status.ordinal();
                    resultIndex = status.ordinal();
                    mFaceStates[0] = resultIndex;
//                    face.setFace(1,mFaceRect,mFaceMarks,GetStateString(resultIndex));
                    FaceDetectRGBActivity.this.runOnUiThread(mRunnable);  //更新UI,显示识别情况
                    //如果是RealPerson change to bitmap && incenter
                    if (AgFaceMark.Status.values()[resultIndex] == REAL && isCenter ) {
                        Bitmap bitmap = createMyBitmap(mBRGdata, newW, newH);
                        saveImage(bitmap);
                        finish();
                    }
                } else {//没有检测到人脸
                    detectInfo.statusIndex = -1;
                    resultIndex = -1;
                    face.setFace(0, mFaceRect, mFaceMarks, GetStateString(resultIndex));
//                    Log.e("myerror","no face !!!!!!!!!!!!!!!!!!!!!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Log.e("yutao", "返回bitmap异常:" + e);
            }
            handler.post(new Runnable() {
                public void run() {
                    //send face to FaceView to draw rect
                    mFaceView.setFace(face);
                    //calculate FPS
                    end = System.currentTimeMillis();
                    counter++;
                    double time = (double) (end - start) / 1000;
                    if (time != 0)
                        fps = counter / time;

                    mFaceView.setFPS(fps);

                    if (counter == (Integer.MAX_VALUE - 1000))
                        counter = 0;

                    isThreadWorking = false;
                }
            });
        }

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                String showText = GetStateString(resultIndex);
                showResult.setText(showText);
            }
        };
    }


    public static boolean fileIsExists(String strFile){
        try {
            File file=new File(strFile);
            if (!file.exists()){
                return false;
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    void ShowInfo(String info)
    {
        AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                .setTitle("ERROR")//标题
                .setMessage(info)//内容
                .setIcon(R.drawable.icon_logo)//图标
                .create();
        alertDialog1.show();

    }

    private void SeetaInit() {
        PATH_MODEL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/model/";
        String fdModel = PATH_MODEL + Constants.MODEL_DETECTOR_FILE_NAME;

        if(!fileIsExists(fdModel))
        {
            ShowInfo("文件不存在->fdModel");
            return;
        }

        seetaFaceDetector = new FaceDetector(fdModel);
        seetaFaceDetector.SetMinFaceSize(100);
        seetaFaceDetector.SetImagePyramidScaleFactor(1.414f);
        seetaFaceDetector.SetVideoStable(true);

        String pdModel = PATH_MODEL + Constants.MODEL_POINTER_FILE_NAME;

        if(!fileIsExists(pdModel))
        {
            ShowInfo("文件不存在->pdModel");
            return;
        }

        seetaPointDetector = new PointDetector(pdModel);

        String fasModel = PATH_MODEL + Constants.MODEL_FAS_FILE_NAME1;

        if(!fileIsExists(fasModel))
        {
            ShowInfo("文件不存在->fasModel");
            return;
        }

        if (!AgFaceMark.AddAuthor("")) {
            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                    .setTitle("ERROR")//标题
                    .setMessage("静默活体 授权验证失败")//内容
                    .setIcon(R.drawable.icon_logo)//图标
                    .create();
            alertDialog1.show();
        }
        processor = new AgFaceMark(fasModel);
        float value1 = 0.65f;
        float value2 = 0.5f;
        processor.SetThreshold(value1, value2);
    }
}
