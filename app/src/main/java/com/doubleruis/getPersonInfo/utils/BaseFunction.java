package com.doubleruis.getPersonInfo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by dell
 * 2019/6/5
 */
public class BaseFunction {
    private static Context context;
    private static BaseFunction instance;

    public BaseFunction(Context mContext) {
        context = mContext;
    }

    //单例模式,线程安全
    public static BaseFunction getInstance(Context mContext) {
        if (instance == null) {
            synchronized (BaseFunction.class) {
                instance = new BaseFunction(context);
            }
        }

        context = mContext;
        return instance;
    }

    /**
     * 弹出Toast消息
     * @param message
     */
    public static void showToast(String message){
        Toast.makeText(context, message , Toast.LENGTH_SHORT).show();
    }

    /**
     * 弹出Toast消息
     * @param message
     * @param time 消息存在时间
     */
    public static void showToast(String message, int time) {
        Toast.makeText(context, message, time).show();
    }

}
