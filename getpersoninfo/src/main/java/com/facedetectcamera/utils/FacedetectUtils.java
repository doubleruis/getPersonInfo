package com.facedetectcamera.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author 余涛
 * Description 工具类
 * Time 2018/12/6 10:08 .
 */
public class FacedetectUtils {
    private Context context;
    public static Context mContext;

    public FacedetectUtils(Context mContext){
        this.context = mContext;
        this.mContext = mContext;


    }

    /************************************数据保存本地功能模块开始*****************************************/

    /**
     * 将字符串数据保存到本地
     * @param context 上下文
     * @param fileName 生成XML的文件名
     * @param  map<生成XML中每条数据名,需要保存的数据>
     */
    public static void saveLocalData(Context context, String fileName , Map<String, String> map) {
        SharedPreferences.Editor note = context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            note.putString(entry.getKey(), entry.getValue());
        }
        note.commit();
    }

    /**
     * 从本地取出要保存的数据
     * @param context 上下文
     * @param fileName 文件名
     * @param dataName 生成XML中每条数据名
     * @return 对应的数据(找不到为NUll)
     */
    public static String getLocalData(Context context, String fileName , String dataName) {
        SharedPreferences read = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return read.getString(dataName, null);
    }

    /************************************数据保存本地功能模块结束*****************************************/

    /**
     * 获取实际意义的最大放大倍数，如4.0，10.0
     * 未完成
     *
     * @return
     */
    public static float getPictureZoom(Camera camera, int zoom) {
        List<Integer> allZoomRatio = getAllZoomRatio(camera);
        if (null == allZoomRatio) {
            return 1.0f;
        } else {
            return format(allZoomRatio.get(zoom) / 100f);
        }
    }

    /**
     * 获取全部zoomratio
     */
    public static List<Integer> getAllZoomRatio(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported()) {
            //Log.e("lawwing", "getAllZoomRation = " + parameters.getZoomRatios().toString());
            return parameters.getZoomRatios();
        } else {
            return null;
        }
    }

    private static float format(float num) {
        DecimalFormat df = new DecimalFormat("#.0");
        return Float.valueOf(df.format(num));
    }

    /**
     * 初始化活体检测权限（模型）文件
     */
    public void initFaceCheck() {
        System.loadLibrary("AgileFace");
        String state= Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            String newPath = DATABASE_PATH+"/model";
            CopyAssets(context,"model",newPath);
        }
    }

    /**
     * 复制asset文件到指定目录
     * @param oldPath  asset下的路径
     * @param newPath  SD卡下保存路径
     */
    private   void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            String fileassert = context.getAssets().toString();
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {

                    CopyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节

                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
