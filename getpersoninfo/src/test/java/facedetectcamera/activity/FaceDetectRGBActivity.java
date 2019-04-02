package facedetectcamera.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.getpersoninfo.R;
import com.seeta.sdk.AgFaceMark;
import com.seeta.sdk.FaceDetector;
import com.seeta.sdk.PointDetector;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import com.seeta.sdk.SeetaRect;
import com.seetatech.seetaverify.constants.Constants;
import com.ytlibrary.BaseFunction;
import com.ytlibrary.dialog.impl.DialogLibrary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import facedetectcamera.activity.ui.FaceOverlayView;
import facedetectcamera.model.FaceResult;
import facedetectcamera.utils.CameraErrorCallback;
import facedetectcamera.utils.Util;

import static com.seeta.sdk.AgFaceMark.Status.REAL;

/**
 * Created by Nguyen on 5/20/2016.
 */
public final class FaceDetectRGBActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private int numberOfCameras;
    public static final String TAG = FaceDetectRGBActivity.class.getSimpleName();

    private Camera mCamera;
    private int cameraId = 0;  //设置前后摄像头(0为后置，1为前置)
    private int iBack = 1;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    private int previewWidth;
    private int previewHeight;

    // The surface view for the camera data
    private SurfaceView mView;

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    public FaceDetector seetaFaceDetector = null;
    public PointDetector seetaPointDetector = null;
    public AgFaceMark processor = null;

    private static final int MAX_FACE = 10;
    private boolean isThreadWorking = false;
    private Handler handler = new Handler();
    private FaceDetectThread detectThread = null;
    private int prevSettingWidth;
    private int prevSettingHeight;
    private String PATH_MODEL;

    private FaceResult face = new FaceResult();
    private float[] mFaceRect = new float[512];
    private float[] mFaceMarks = new float[212];
    private int[] mFaceStates = new int[6];

    private int BGR_DEFINE_WIDTH = 540;
    private int BGR_DEFINE_HEIGHT = 2200;//是为了保证缓冲区足够大；实际按宽高比，等比例缩放
    private byte[] mBRGdata = new byte[BGR_DEFINE_WIDTH * BGR_DEFINE_HEIGHT * 3];

    private int Id = 0;

    private String BUNDLE_CAMERA_ID = "camera";
    private DialogLibrary mDialogLibrary;

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
    private ImageView faceCheckPic;

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
        faceCheckPic = (ImageView) findViewById(R.id.face_check_pic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.face_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.menu.face_menu){
            showResult.setText("");
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        new BaseFunction(this);
        mDialogLibrary = new DialogLibrary(this);
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
        numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                cameraId = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                cameraId = i;
            }

        }

        mCamera = Camera.open(cameraId);

        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFaceView.setFront(true);
        }

        try {
            mCamera.setPreviewDisplay(mView.getHolder());
        } catch (Exception e) {
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

        // Create media.FaceDetector
        float aspect = (float) previewHeight / (float) previewWidth;

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
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        // Set the PreviewSize and AutoFocus:
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        // And set the parameters:
        mCamera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

//        Log.e(TAG, "previewWidth" + previewWidth);
//        Log.e(TAG, "previewHeight" + previewHeight);

        /**
         * Calculate size to scale full frame bitmap to smaller bitmap
         * Detect face in scaled bitmap have high performance than full bitmap.
         * The smaller image size -> detect faster, but distance to detect face shorter,
         * so calculate the size follow your purpose
         */
        if (previewWidth > 640) {
            prevSettingWidth = 640;
            prevSettingHeight = 480;
        } else if (previewWidth > 720) {
            prevSettingWidth = 720;
            prevSettingHeight = 540;
        } else if (previewWidth / 4 > 240) {
            prevSettingWidth = 240;
            prevSettingHeight = 160;
        } else {
            prevSettingWidth = 160;
            prevSettingHeight = 120;
        }

        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);

        mFaceView.setPreviewWidth(previewWidth);
        mFaceView.setPreviewHeight(previewHeight);
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
//        finish();
    }

    public void compressBmpToFile(Bitmap bmp, File file) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;//个人喜欢从80开始,
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
//        //log.e("初始图片内存大小：", baos.toByteArray().length / 1024 + "");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                long endTime = System.currentTimeMillis();    //获取结束时间
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

                if (maxFaceID != -1) {//存在人脸
                    SeetaPointF[] points5 = new SeetaPointF[5];

                    seetaPointDetector.Detect(imageData, faceInfos[maxFaceID], points5);

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

                    endTime = System.currentTimeMillis();    //获取结束时间
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
                    //如果是RealPerson change to bitmap
                    if (AgFaceMark.Status.values()[resultIndex] == REAL) {
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
//                if (showText == null && "".equals(showText)) {
//                    showResult.setText(showText);  //空结果
//                } else {
//                    showResult.setText("请调整拍摄角度");  //显示检测结果
//                }
            }
        };

    }

    private void SeetaInit() {
        PATH_MODEL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/model/";
        String fdModel = PATH_MODEL + Constants.MODEL_DETECTOR_FILE_NAME;
        seetaFaceDetector = new FaceDetector(fdModel);
        seetaFaceDetector.SetMinFaceSize(100);
        seetaFaceDetector.SetImagePyramidScaleFactor(1.414f);
        seetaFaceDetector.SetVideoStable(true);

        String pdModel = PATH_MODEL + Constants.MODEL_POINTER_FILE_NAME;
        seetaPointDetector = new PointDetector(pdModel);

        String fasModel = PATH_MODEL + Constants.MODEL_FAS_FILE_NAME1;

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
